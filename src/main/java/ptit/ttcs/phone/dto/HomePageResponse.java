package ptit.ttcs.phone.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ptit.ttcs.phone.enums.DiscountType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HomePageResponse {
  private List<BrandSummary> brands;
  private List<PromoSummary> promotions;
  private List<ProductSummary> featuredProducts;
  private List<ProductSummary> topRatedProducts;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class BrandSummary {
    private Integer id;
    private String name;
    private String logoUrl;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PromoSummary {
    private Integer id;
    private String name;
    private String description;
    private Instant startDate;
    private Instant endDate;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private String voucherCode;
    private String imageUrl;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ProductSummary {
    private Integer id;
    private String name;
    private String type;
    private String brandName;
    private BigDecimal basePrice;
    private Boolean inStock;
    private String imageUrl;
    private Instant createdAt;
    private Double averageRating;
    private Long ratingCount;
  }
}