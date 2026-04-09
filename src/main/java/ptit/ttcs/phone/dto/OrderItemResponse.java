package ptit.ttcs.phone.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderItemResponse {
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
