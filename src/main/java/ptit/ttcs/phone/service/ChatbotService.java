package ptit.ttcs.phone.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import ptit.ttcs.phone.document.ProductDocument;
import ptit.ttcs.phone.dto.ChatbotConversation;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatbotService {
  
  private final RedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;
  private final ProductSearchService productSearchService;
  private final Client geminiClient = new Client();
  
  private static final Set<String> STOPWORDS = Set.of(
      "tôi", "muốn", "mua", "tìm", "cho", "xem", "giá", "bao", "nhiêu",
      "có", "không", "và", "của", "với", "là", "một", "các"
  );
  
  public List<String> extractKeywords(String message) {
    return Arrays.stream(message.toLowerCase().split("\\s+"))
        .map(w -> w.replaceAll("[^a-z0-9àáảãạăắặẳẵặâấầẩẫậđèéẻẽẹêếềểễệìíỉĩịòóỏõọôốồổỗộơớờởỡợùúủũụưứừửữựỳýỷỹỵ]", ""))
        .filter(w -> !w.isBlank() && !STOPWORDS.contains(w) && w.length() > 1)
        .distinct()
        .collect(Collectors.toList());
  }
  
  public String processChatMessage(int userId, @Size(min = 5, max = 65535, message = "Do dai tin nhan khong hop le") String chatMessage) throws Exception {
    String convHistory = "";
    try {
      convHistory = (String) redisTemplate.opsForValue().get(String.format("chat:session:%d", userId));
    }
    catch (Exception e) {
      log.warn(e.getMessage());
    }
    ChatbotConversation convHistoryObj = null;
    if (convHistory != null) {
      try {
        convHistoryObj = objectMapper.readValue(convHistory, ChatbotConversation.class);
      } catch (JsonProcessingException e) {
        log.warn("Lich su chat bi loi dinh dang, bat dau phien moi: {}", e.getMessage());
      }
    }
    // trich xuat tu khoa
    List<String> keywords = extractKeywords(chatMessage);
    // truy van ES
    List<ProductDocument> relatedProducts = productSearchService.searchByKeywords(keywords);
    // xay dung prompt
    LinkedHashMap<String, String> turns = (convHistoryObj != null)
        ? convHistoryObj.getTurns()
        : new LinkedHashMap<>();
    String prompt = buildPrompt(turns, relatedProducts, chatMessage);
    // goi api gemini
    String chatResponse = "";
    List<String> geminiModels = List.of(
        "gemini-2.5-flash, gemini-3-flash",
        "gemini-2.5-pro", "gemini-3.1-flash-lite",
        "gemini-3.1-pro-preview", "gemini-2.5-flash-lite"
    );
    for (String model : geminiModels) {
      try {
        chatResponse = geminiClient.models.generateContent(
            model,
            prompt,
            null).text();
        if (chatResponse.length() > 0) {
          break;
        }
      }
      catch (Exception e) {
        continue;
      }
    }
    if (chatResponse.equals("")) {
      throw new Exception("Chatbot hien khong hoat dong, mong ban thong cam");
    }
    // cap nhat lich su chat
    if (convHistoryObj == null) {
      convHistoryObj = new ChatbotConversation();
      convHistoryObj.setTurns(new LinkedHashMap<>());
    }
    
    turns = convHistoryObj.getTurns();
    turns.put("user", chatMessage);
    turns.put("assistant", chatResponse);
    
    try {
      String updatedHistory = objectMapper.writeValueAsString(convHistoryObj);
      redisTemplate.opsForValue().set(
          String.format("chat:session:%d", userId),
          updatedHistory,
          30, TimeUnit.MINUTES
      );
    }
    catch (Exception e) {
      log.warn("Khong the luu lich su chat vao Redis: {}", e.getMessage());
      throw new Exception("Khong the luu lich su chat vao Redis");
    }
    // tra ve
    return chatResponse;
  }
  
  public String buildPrompt(
      LinkedHashMap<String, String> chatHistory,
      List<ProductDocument> products,
      String userMessage
  ) {
    StringBuilder prompt = new StringBuilder();
    
    // ── SYSTEM ROLE ───────────────────────────────────────────
    prompt.append("""
        Bạn là trợ lý tư vấn mua hàng của PHONEix - cửa hàng điện thoại và phụ kiện trực tuyến.
        Nhiệm vụ của bạn là giúp khách hàng tìm sản phẩm phù hợp, tư vấn thông số kỹ thuật,
        so sánh sản phẩm và trả lời các câu hỏi liên quan đến sản phẩm.
        
        Nguyên tắc trả lời:
        - Chỉ tư vấn về sản phẩm có trong danh sách bên dưới nếu có.
        - Trả lời ngắn gọn, thân thiện, dễ hiểu.
        - Nếu không có sản phẩm phù hợp, hãy thành thật nói không tìm thấy và gợi ý khách tìm kiếm trực tiếp.
        - Không bịa đặt thông tin sản phẩm không có trong dữ liệu.
        - Trả lời bằng tiếng Việt.
        
        """);
    
    // ── PRODUCT CONTEXT ───────────────────────────────────────
    if (!products.isEmpty()) {
      prompt.append("=== SẢN PHẨM LIÊN QUAN ===\n");
      for (int i = 0; i < products.size(); i++) {
        ProductDocument p = products.get(i);
        prompt.append(String.format("""
                [Sản phẩm %d]
                Tên: %s
                Thương hiệu: %s
                Giá: %,.0f VND
                Loại: %s
                RAM: %s | Bộ nhớ: %s | Chipset: %s
                Màn hình: %s (%s)
                Camera sau: %s | Camera trước: %s
                Pin: %s | Hệ điều hành: %s
                Còn hàng: %s
                
                """,
            i + 1,
            p.getName(),
            p.getBrandName(),
            p.getBasePrice(),
            p.getType(),
            nvl(p.getRam()), nvl(p.getStorage()), nvl(p.getChipset()),
            nvl(p.getScreenType()), nvl(p.getScreenSize()),
            nvl(p.getRearCamera()), nvl(p.getFrontCamera()),
            nvl(p.getBattery()), nvl(p.getOs()),
            p.getInStock() ? "Còn hàng" : "Hết hàng"
        ));
      }
      prompt.append("==========================\n\n");
    } else {
      prompt.append("""
            === SẢN PHẨM LIÊN QUAN ===
            Không tìm thấy sản phẩm phù hợp trong hệ thống.
            ==========================
            
            """);
    }
    
    // ── CHAT HISTORY ──────────────────────────────────────────
    if (!chatHistory.isEmpty()) {
      prompt.append("=== LỊCH SỬ HỘI THOẠI ===\n");
      chatHistory.forEach((role, content) ->
          prompt.append(String.format("[%s]: %s\n",
              role.equalsIgnoreCase("user") ? "Khách" : "Trợ lý",
              content))
      );
      prompt.append("==========================\n\n");
    }
    
    // ── CURRENT MESSAGE ───────────────────────────────────────
    prompt.append("=== CÂU HỎI HIỆN TẠI CỦA KHÁCH ===\n");
    prompt.append(userMessage).append("\n");
    prompt.append("====================================\n\n");
    prompt.append("Hãy trả lời câu hỏi của khách hàng dựa trên thông tin sản phẩm và lịch sử hội thoại ở trên.");
    
    return prompt.toString();
  }
  
  // Null-safe helper
  private String nvl(String value) {
    return value != null ? value : "Không có thông tin";
  }
}
