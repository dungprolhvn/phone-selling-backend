package ptit.ttcs.phone.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ptit.ttcs.phone.enums.OrderStatus;

@Getter
@AllArgsConstructor
public class OrderStatusResponse {
  private Integer orderId;
  private String phone;
  private OrderStatus status;
  private String paymentMethod;
  private Boolean shipAtStore;
  private String trackingNumber;
  private String cancelReason;
  private BigDecimal totalAmount;
  private BigDecimal discountAmount;
  private Instant createdAt;
  private Instant updatedAt;
  private String recipientName;
  private String recipientPhone;
  private String address;
  private List<OrderStatusItemResponse> items;
}
