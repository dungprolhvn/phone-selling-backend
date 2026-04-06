package ptit.ttcs.phone.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HomeProductResponse {
  private Integer id;
  private String name;
  private BigDecimal basePrice;
  private String type;
  private String brandName;
  private String imageUrl;
  private Boolean inStock;
}
