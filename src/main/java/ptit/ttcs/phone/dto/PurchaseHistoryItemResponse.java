package ptit.ttcs.phone.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseHistoryItemResponse {
  @JsonProperty("orderId")
  private Integer orderId;
  
  @JsonProperty("orderDate")
  private Instant orderDate;
  
  @JsonProperty("status")
  private String status;
  
  @JsonProperty("totalAmount")
  private BigDecimal totalAmount;
  
  @JsonProperty("discountAmount")
  private BigDecimal discountAmount;
  
  @JsonProperty("paymentMethod")
  private String paymentMethod;
  
  @JsonProperty("trackingNumber")
  private String trackingNumber;
  
  @JsonProperty("items")
  private java.util.List<OrderItemDetailResponse> items;
}
