package ptit.ttcs.phone.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
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
    // VNPay requires amount in VND * 100 (no decimals)
    long vnpayAmount = amount.longValue() * 100;
    
    // Build params map — must be sorted alphabetically for correct signature
    Map<String, String> params = new TreeMap<>();
    params.put("vnp_Version", "2.1.0");
    params.put("vnp_Command", "pay");
    params.put("vnp_TmnCode", tmnCode);
    params.put("vnp_Amount", String.valueOf(vnpayAmount));
    params.put("vnp_CurrCode", "VND");
    params.put("vnp_TxnRef", String.valueOf(orderId));   // your order ID
    params.put("vnp_OrderInfo", orderInfo);              // e.g. "Thanh toan don hang #1001"
    params.put("vnp_OrderType", "other");
    params.put("vnp_Locale", "vn");
    params.put("vnp_ReturnUrl", returnUrl);
    params.put("vnp_IpnUrl", ipnUrl);
    params.put("vnp_CreateDate", getCurrentDateTime());  // yyyyMMddHHmmss
    params.put("vnp_ExpireDate", getExpireDateTime(15)); // 15 min TTL
    params.put("vnp_IpAddr", clientIp);
    
    // Build query string for signing
    String queryString = buildQueryString(params, false);
    
    // Sign with HMAC-SHA512
    String signature = hmacSHA512(hashSecret, queryString);
    
    // Final URL = query string + signature
    return payUrl + "?" + queryString + "&vnp_SecureHash=" + signature;
  }
  
  public boolean verifyWebhook(Map<String, String> params) {
    // Extract and remove signature from params before verifying
    String receivedHash = params.get("vnp_SecureHash");
    Map<String, String> paramsWithoutHash = new TreeMap<>(params);
    paramsWithoutHash.remove("vnp_SecureHash");
    paramsWithoutHash.remove("vnp_SecureHashType");
    
    String queryString = buildQueryString(paramsWithoutHash, false);
    String expectedHash = hmacSHA512(hashSecret, queryString);
    
    return expectedHash.equalsIgnoreCase(receivedHash);
  }
  
  // ── PRIVATE HELPERS ───────────────────────────────────────────────────
  
  private String buildQueryString(Map<String, String> params, boolean encode) {
    StringBuilder sb = new StringBuilder();
    params.forEach((key, value) -> {
      if (sb.length() > 0) sb.append("&");
      if (encode) {
        try {
          sb.append(URLEncoder.encode(key, StandardCharsets.UTF_8));
          sb.append("=");
          sb.append(URLEncoder.encode(value, StandardCharsets.UTF_8));
        } catch (Exception e) {
          throw new RuntimeException("URL encoding failed", e);
        }
      } else {
        sb.append(key).append("=").append(value);
      }
    });
    return sb.toString();
  }
  
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
