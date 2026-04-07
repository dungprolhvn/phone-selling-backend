package ptit.ttcs.phone.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ptit.ttcs.phone.enums.OrderStatus;

// OrderResponse DTO
@Getter
@AllArgsConstructor
public class OrderResponse {
  private Integer orderId;
  private String paymentUrl;   // frontend redirects user here
  private OrderStatus status;
}
