package ptit.ttcs.phone.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderItemResponse {
  private final ObjectMapper objectMapper;

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
  
  
  private Byte rating;

  @Override
  public String toString() {
    try {
      return objectMapper.writeValueAsString(this);
    } 
    catch (Exception ex) {
      return super.toString();
    }
  }
}
