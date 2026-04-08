package ptit.ttcs.phone.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductDetailResponse {
  private Integer id;
  private String name;
  private String type;
  private BigDecimal basePrice;
  private String brandName;
  private Map<String, Object> specs;
  private String description;
  private Instant releaseDate;
  private List<String> imageUrls;
  private Boolean inStock;
  private Integer stockAvailable;
  private Double averageRating;
  private List<ProductReviewResponse> reviews;
}
