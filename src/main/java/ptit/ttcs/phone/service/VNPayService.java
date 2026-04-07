package ptit.ttcs.phone.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.time.LocalDateTime;

@Service
@Slf4j
public class VNPayService {
  
  @Value("${app.vnpay.tmnCode}")
  private String tmnCode;
  
  @Value("${app.vnpay.hashSecret}")
  private String hashSecret;
  
  @Value("${app.vnpay.payUrl}")
  private String payUrl;
  
  @Value("${app.vnpay.returnUrl}")
  private String returnUrl;
  
  @Value("${app.vnpay.ipnUrl}")
  private String ipnUrl;
  
  public String createPaymentUrl(Integer orderId, BigDecimal amount, String orderInfo, String clientIp) {
    long vnpayAmount = amount.longValue() * 100;
    
    Map<String, String> vnp_Params = new HashMap<>();
    vnp_Params.put("vnp_Version", "2.1.0");
    vnp_Params.put("vnp_Command", "pay");
    vnp_Params.put("vnp_TmnCode", tmnCode);
    vnp_Params.put("vnp_Amount", String.valueOf(vnpayAmount));
    vnp_Params.put("vnp_CurrCode", "VND");
    vnp_Params.put("vnp_TxnRef", String.valueOf(orderId));
    vnp_Params.put("vnp_OrderInfo", orderInfo);
    vnp_Params.put("vnp_OrderType", "other");
    vnp_Params.put("vnp_Locale", "vn");
    vnp_Params.put("vnp_ReturnUrl", returnUrl);
    
    // Ensure IPv4 for localhost testing
    String ip = clientIp;
    if (ip == null || ip.equals("0:0:0:0:0:0:0:1")) {
      ip = "127.0.0.1";
    }
    vnp_Params.put("vnp_IpAddr", ip);
    
    // ==========================================
    // CRITICAL FIX: STRICTLY USE GMT+7 TIMEZONE
    // ==========================================
    Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
    
    String vnp_CreateDate = formatter.format(cld.getTime());
    vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
    
    cld.add(Calendar.MINUTE, 15);
    String vnp_ExpireDate = formatter.format(cld.getTime());
    vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);
    
    // ==========================================
    // EXACT VNPAY HASHING & ENCODING LOOP
    // ==========================================
    List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
    Collections.sort(fieldNames);
    
    StringBuilder hashData = new StringBuilder();
    StringBuilder query = new StringBuilder();
    
    try {
      Iterator<String> itr = fieldNames.iterator();
      while (itr.hasNext()) {
        String fieldName = itr.next();
        String fieldValue = vnp_Params.get(fieldName);
        
        if ((fieldValue != null) && (fieldValue.length() > 0)) {
          // Build hash data
          hashData.append(fieldName);
          hashData.append('=');
          hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
          
          // Build query
          query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
          query.append('=');
          query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
          
          if (itr.hasNext()) {
            query.append('&');
            hashData.append('&');
          }
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("URL encoding failed", e);
    }
    
    String queryUrl = query.toString();
    String vnp_SecureHash = hmacSHA512(hashSecret, hashData.toString());
    
    return payUrl + "?" + queryUrl + "&vnp_SecureHash=" + vnp_SecureHash;
  }
  
  public boolean verifyWebhook(Map<String, String> params) {
    String vnp_SecureHash = params.get("vnp_SecureHash");
    
    if (vnp_SecureHash == null) {
      return false;
    }
    
    // VNPay expects hash generation to exclude the secure hash fields
    Map<String, String> paramsWithoutHash = new HashMap<>(params);
    paramsWithoutHash.remove("vnp_SecureHash");
    paramsWithoutHash.remove("vnp_SecureHashType");
    
    // ==========================================
    // EXACT VNPAY HASHING LOOP FOR VERIFICATION
    // ==========================================
    List<String> fieldNames = new ArrayList<>(paramsWithoutHash.keySet());
    Collections.sort(fieldNames);
    
    StringBuilder hashData = new StringBuilder();
    
    try {
      Iterator<String> itr = fieldNames.iterator();
      while (itr.hasNext()) {
        String fieldName = itr.next();
        String fieldValue = paramsWithoutHash.get(fieldName);
        
        if ((fieldValue != null) && (fieldValue.length() > 0)) {
          // VNPay only encodes the Value when building the hashData string
          hashData.append(fieldName);
          hashData.append('=');
          hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
          
          if (itr.hasNext()) {
            hashData.append('&');
          }
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("URL encoding failed during verification", e);
    }
    
    String expectedHash = hmacSHA512(hashSecret, hashData.toString());
    
    return expectedHash.equalsIgnoreCase(vnp_SecureHash);
  }
  
  // ── PRIVATE HELPERS ───────────────────────────────────────────────────
  
  private String hmacSHA512(String key, String data) {
    try {
      Mac mac = Mac.getInstance("HmacSHA512");
      SecretKeySpec secretKey = new SecretKeySpec(
          key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"
      );
      mac.init(secretKey);
      byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
      
      StringBuilder sb = new StringBuilder();
      for (byte b : hash) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    }
    catch (Exception e) {
      throw new RuntimeException("HMAC-SHA512 failed", e);
    }
  }
  
  private String getCurrentDateTime() {
    return LocalDateTime.now()
        .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
  }
  
  private String getExpireDateTime(int minutes) {
    return LocalDateTime.now()
        .plusMinutes(minutes)
        .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
  }
}
