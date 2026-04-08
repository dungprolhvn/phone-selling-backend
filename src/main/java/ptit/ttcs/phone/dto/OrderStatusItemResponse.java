package ptit.ttcs.phone.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderStatusItemResponse {
  private Integer productId;
  private String productName;
  private Integer quantity;
  private BigDecimal purchasedAtPrice;
}
