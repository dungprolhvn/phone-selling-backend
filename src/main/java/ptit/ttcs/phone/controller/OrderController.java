package ptit.ttcs.phone.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ptit.ttcs.phone.dto.OrderRequest;
import ptit.ttcs.phone.dto.OrderResponse;
import ptit.ttcs.phone.dto.OrderStatusLookupRequest;
import ptit.ttcs.phone.dto.OrderStatusResponse;
import ptit.ttcs.phone.service.OrderService;
import ptit.ttcs.phone.service.OrderStatusService;

@RestController
@RequestMapping("/api/orders")
@Slf4j
@RequiredArgsConstructor
public class OrderController {
  
  private final OrderService orderService;
  private final OrderStatusService orderStatusService;
  
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

  @GetMapping("/status")
  public ResponseEntity<OrderStatusResponse> lookupOrderStatus(@ModelAttribute @Valid OrderStatusLookupRequest request) {
    return ResponseEntity.ok(orderStatusService.lookupOrderStatus(request.getPhone(), request.getOrderId()));
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
