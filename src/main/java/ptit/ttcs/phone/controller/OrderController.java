package ptit.ttcs.phone.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ptit.ttcs.phone.dto.OrderRequest;
import ptit.ttcs.phone.dto.OrderResponse;
import ptit.ttcs.phone.service.OrderService;

@RestController
@RequestMapping("/api/orders")
@Slf4j
@RequiredArgsConstructor
public class OrderController {
  
  private final OrderService orderService;
  
  @PreAuthorize("hasRole('USER')")
  @PostMapping("/place")
  public ResponseEntity<OrderResponse> placeOrder(
    Authentication authentication,
    @RequestBody @Valid OrderRequest request,
    HttpServletRequest httpServletRequest
  ) {
    String clientIp = getClientIp(httpServletRequest);
    OrderResponse res = orderService.processOrder(authentication, request, clientIp);
    return ResponseEntity.status(HttpStatus.OK).body(res);
  }
  
  private String getClientIp(HttpServletRequest request) {
    // Check forwarded headers first — in case you're behind a proxy/nginx
    String ip = request.getHeader("X-Forwarded-For");
    if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
      // X-Forwarded-For can contain multiple IPs — take the first one
      return ip.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
  
}
