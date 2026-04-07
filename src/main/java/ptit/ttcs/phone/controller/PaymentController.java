package ptit.ttcs.phone.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ptit.ttcs.phone.service.OrderService;
import ptit.ttcs.phone.service.VNPayService;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
  
  private final VNPayService vnPayService;
  private final OrderService orderService;
  
  // VNPay calls this server-to-server after payment
  @GetMapping("/vnpay/ipn")
  public ResponseEntity<Map<String, String>> handleIPN(
      @RequestParam Map<String, String> params) {
    
    // 1. Verify signature
    if (!vnPayService.verifyWebhook(params)) {
      return ResponseEntity.ok(Map.of("RspCode", "97", "Message", "Invalid signature"));
    }
    
    String vnpResponseCode = params.get("vnp_ResponseCode");
    Integer orderId = Integer.parseInt(params.get("vnp_TxnRef"));
    String transactionId = params.get("vnp_TransactionNo");
    
    if ("00".equals(vnpResponseCode)) {
      // Payment success
      orderService.confirmPayment(orderId, transactionId);
    }
    
    // Always return 200 with RspCode 00 to VNPay
    // Otherwise VNPay will retry the webhook
    return ResponseEntity.ok(Map.of("RspCode", "00", "Message", "Confirm Success"));
  }
}
