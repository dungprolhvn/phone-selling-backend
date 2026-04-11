package ptit.ttcs.phone.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatDTO {
  private List<RevenueByDayItem> revenueByDay;
  private List<TotalRevenueItem> totalRevenue;
  private List<TopPurchasedProductItem> topPurchasedProducts;
  private List<TopPaidOrderItem> topPaidOrders;
  private List<OrderRateItem> orderRates;
  private List<LowSellingNewProductItem> lowSellingNewProducts;

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class RevenueByDayItem {
    private LocalDate day;
    private BigDecimal revenue;
  }

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class TotalRevenueItem {
    private BigDecimal totalRevenue;
  }

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class TopPurchasedProductItem {
    private Integer productId;
    private String productName;
    private Long totalPurchased;
  }

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class TopPaidOrderItem {
    private Integer orderId;
    private BigDecimal paidAmount;
    private String status;
    private Instant createdAt;
  }

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class OrderRateItem {
    private String status;
    private Long count;
    private Double rate;
  }

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class LowSellingNewProductItem {
    private Integer productId;
    private String productName;
    private Instant releaseDate;
    private Long soldQuantity;
  }
}