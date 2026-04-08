package ptit.ttcs.phone.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDetailResponse {
  @JsonProperty("productId")
  private Integer productId;
  
  @JsonProperty("productName")
  private String productName;
  
  @JsonProperty("quantity")
  private Byte quantity;
  
  @JsonProperty("purchasedAtPrice")
  private BigDecimal purchasedAtPrice;
  
  @JsonProperty("brandName")
  private String brandName;
  
  @JsonProperty("thumbnailUrl")
  private String thumbnailUrl;
}
