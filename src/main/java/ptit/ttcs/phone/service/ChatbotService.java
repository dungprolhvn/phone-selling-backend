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
import ptit.ttcs.phone.dto.Turn;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatbotService {
  
  private static final Set<String> STOPWORDS = Set.of(
      "tôi", "muốn", "mua", "tìm", "cho", "xem", "giá", "bao", "nhiêu",
      "có", "không", "và", "của", "với", "là", "một", "các"
  );
  private final RedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;
  private final ProductSearchService productSearchService;
  private final Client geminiClient = new Client();
  
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
      }
      catch (JsonProcessingException e) {
        log.warn("Lich su chat bi loi dinh dang, bat dau phien moi: {}", e.getMessage());
      }
    }
    // trich xuat tu khoa
    List<String> keywords = extractKeywords(chatMessage);
    // truy van ES
    List<ProductDocument> relatedProducts = productSearchService.searchByKeywords(keywords);
    // xay dung prompt
    List<Turn> turns = (convHistoryObj != null)
        ? convHistoryObj.getTurns()
        : new ArrayList<>();
    String prompt = buildPrompt(turns, relatedProducts, chatMessage);
    // goi api gemini
    String chatResponse = "";
    List<String> geminiModels = List.of(
        "gemini-3.1-pro-preview",
        "gemini-2.5-pro",
        "gemini-3-flash",
        "gemini-2.5-flash",
        "gemini-3.1-flash-lite",
        "gemini-2.5-flash-lite"
    );
    for (String model : geminiModels) {
      try {
        chatResponse = geminiClient.models.generateContent(
            model,
            prompt,
            null).text();
        Thread.sleep(250);
        if (chatResponse.length() > 0) {
          break;
        }
      }
      catch (Exception e) {
        e.printStackTrace();
        continue;
      }
    }
    if (chatResponse.equals("")) {
      throw new Exception("Chatbot hien khong hoat dong, mong ban thong cam");
    }
    // cap nhat lich su chat
    if (convHistoryObj == null) {
      convHistoryObj = new ChatbotConversation();
      convHistoryObj.setTurns(new ArrayList<>());
    }
    
    turns = convHistoryObj.getTurns();
    turns.add(new Turn(chatMessage, chatResponse));
    
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
  
  public List<String> extractKeywords(String message) {
    return Arrays.stream(message.toLowerCase().split("\\s+"))
        .map(w -> w.replaceAll("[^a-z0-9àáảãạăắặẳẵặâấầẩẫậđèéẻẽẹêếềểễệìíỉĩịòóỏõọôốồổỗộơớờởỡợùúủũụưứừửữựỳýỷỹỵ]", ""))
        .filter(w -> !w.isBlank() && !STOPWORDS.contains(w) && w.length() > 1)
        .distinct()
        .collect(Collectors.toList());
  }
  
  public String buildPrompt(
      List<Turn> chatHistory,
      List<ProductDocument> products,
      String userMessage
  ) {
    StringBuilder prompt = new StringBuilder();
    
    // ── SYSTEM ROLE ───────────────────────────────────────────
    // ── SYSTEM ROLE ───────────────────────────────────────────
    prompt.append("""
        Bạn là trợ lý tư vấn mua hàng của PHONEix - cửa hàng điện thoại và phụ kiện trực tuyến.
        Nhiệm vụ: giúp khách tìm sản phẩm, tư vấn thông số, so sánh và trả lời câu hỏi liên quan đến điện thoại/phụ kiện.
        
        === QUY TẮC TRẢ LỜI ===
        
        [PHẠM VI]
        - CHỈ trả lời các câu hỏi liên quan đến điện thoại, phụ kiện, nhu cầu sử dụng (gaming, chụp ảnh, xem phim...).
        - TỪ CHỐI lịch sự nếu câu hỏi ngoài phạm vi hoặc chứa nội dung 18+.
        
        [SẢN PHẨM]
        - Chỉ tư vấn sản phẩm có trong danh sách bên dưới. TUYỆT ĐỐI không bịa thông tin.
        - Nếu không tìm thấy sản phẩm phù hợp, thành thật thông báo và gợi ý khách dùng thanh tìm kiếm.
        - Nếu khách đang hỏi tiếp về sản phẩm đã tư vấn trước đó (thấy trong lịch sử), ưu tiên dùng thông tin đó dù sản phẩm không còn trong danh sách bên dưới.
        
        [ĐỊNH DẠNG LINK SẢN PHẨM]
        - Khi đề cập sản phẩm, BẮT BUỘC dùng markdown link: [Tên sản phẩm](/products/{id})
        - Ví dụ: Mình gợi ý [iPhone 15 Pro Max](/products/42) vì camera rất xuất sắc.
        - KHÔNG được tự bịa ID. Chỉ dùng ID từ danh sách sản phẩm bên dưới.
        
        [PHONG CÁCH]
        - Ngắn gọn, thân thiện, dễ hiểu. Trả lời bằng tiếng Việt.
        
        ======================
        
        """);
    
    // ── PRODUCT CONTEXT ───────────────────────────────────────
    // ── PRODUCT CONTEXT ───────────────────────────────────────
    if (!products.isEmpty()) {
      prompt.append("=== SẢN PHẨM LIÊN QUAN ===\n");
      for (int i = 0; i < products.size(); i++) {
        ProductDocument p = products.get(i);
        StringBuilder specs = new StringBuilder();
        if (p.getRam() != null) specs.append("RAM: ").append(p.getRam()).append(" | ");
        if (p.getStorage() != null) specs.append("Bộ nhớ: ").append(p.getStorage()).append(" | ");
        if (p.getChipset() != null) specs.append("Chipset: ").append(p.getChipset()).append(" | ");
        if (p.getScreenType() != null) specs.append("Màn hình: ").append(p.getScreenType());
        if (p.getScreenSize() != null) specs.append("(").append(p.getScreenSize()).append(")");
        if (p.getRearCamera() != null) specs.append(" | Camera sau: ").append(p.getRearCamera());
        if (p.getFrontCamera() != null) specs.append(" | Camera trước: ").append(p.getFrontCamera());
        if (p.getBattery() != null) specs.append(" | Pin: ").append(p.getBattery());
        if (p.getOs() != null) specs.append(" | HĐH: ").append(p.getOs());
        
        prompt.append(String.format(
            "[%d] [%s](/products/%s) — %s — %,.0f VND — %s\n%s\n\n",
            i + 1,
            p.getName(), p.getId(),
            p.getBrandName(),
            p.getBasePrice(),
            p.getInStock() ? "Còn hàng" : "Hết hàng",
            specs
        ));
      }
      prompt.append("==========================\n\n");
    }
    else {
      prompt.append("""
          === SẢN PHẨM LIÊN QUAN ===
          Không tìm thấy sản phẩm phù hợp.
          ==========================
          
          """);
    }
    
    // ── CHAT HISTORY ──────────────────────────────────────────
    if (!chatHistory.isEmpty()) {
      prompt.append("=== LỊCH SỬ HỘI THOẠI ===\n");
      chatHistory.forEach(turn ->
          prompt.append(String.format("[%s]: %s\n",
              turn.getRole().equalsIgnoreCase("user") ? "Khách" : "Trợ lý",
              turn.getContent()))
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
