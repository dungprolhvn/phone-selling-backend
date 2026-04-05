package ptit.ttcs.phone.service;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ptit.ttcs.phone.dto.HomePageResponse;
import ptit.ttcs.phone.entity.Brand;
import ptit.ttcs.phone.entity.Product;
import ptit.ttcs.phone.entity.Promo;
import ptit.ttcs.phone.repository.BrandRepository;
import ptit.ttcs.phone.repository.ProductRepository;
import ptit.ttcs.phone.repository.PromoRepository;
import ptit.ttcs.phone.repository.RatingRepository;

@Service
@RequiredArgsConstructor
public class HomeService {
  private static final int BRAND_LIMIT = 12;
  private static final int PROMO_LIMIT = 6;
  private static final int PRODUCT_LIMIT = 8;

  private final BrandRepository brandRepository;
  private final PromoRepository promoRepository;
  private final ProductRepository productRepository;
  private final RatingRepository ratingRepository;

  @Transactional(readOnly = true)
  public HomePageResponse getHomePage() {
    Instant now = Instant.now();

    List<HomePageResponse.BrandSummary> brands = brandRepository
        .findAllByOrderByNameAsc(PageRequest.of(0, BRAND_LIMIT))
        .stream()
        .map(this::toBrandSummary)
        .toList();

    List<HomePageResponse.PromoSummary> promotions = promoRepository
        .findByStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByCreatedAtDesc(
            now,
            now,
            PageRequest.of(0, PROMO_LIMIT))
        .stream()
        .map(this::toPromoSummary)
        .toList();

    List<Product> featuredProducts = productRepository
        .findAllByStockAvailableGreaterThanOrderByCreatedAtDesc(0, PageRequest.of(0, PRODUCT_LIMIT))
        .getContent();

    List<HomePageResponse.ProductSummary> featured = featuredProducts
        .stream()
        .map(product -> toProductSummary(product, null, null))
        .toList();

    List<HomePageResponse.ProductSummary> topRated = loadTopRatedProducts();

    return new HomePageResponse(brands, promotions, featured, topRated);
  }

  private List<HomePageResponse.ProductSummary> loadTopRatedProducts() {
    List<RatingRepository.ProductRatingStats> stats = ratingRepository
        .findTopRatedProductStats(PageRequest.of(0, PRODUCT_LIMIT))
        .getContent();

    if (stats.isEmpty()) {
      return List.of();
    }

    List<Integer> productIds = stats.stream()
        .map(RatingRepository.ProductRatingStats::getProductId)
        .toList();

    Map<Integer, Product> productsById = StreamSupport.stream(
            productRepository.findAllById(productIds).spliterator(),
            false)
        .collect(Collectors.toMap(Product::getId, Function.identity()));

    return stats.stream()
        .map(stat -> {
          Product product = productsById.get(stat.getProductId());
          if (product == null) {
            return null;
          }
          return toProductSummary(product, stat.getAverageRating(), stat.getRatingCount());
        })
        .filter(Objects::nonNull)
        .toList();
  }

  private HomePageResponse.BrandSummary toBrandSummary(Brand brand) {
    return new HomePageResponse.BrandSummary(
        brand.getId(),
        brand.getName(),
        firstStringValue(brand.getLogoImageUrls())
    );
  }

  private HomePageResponse.PromoSummary toPromoSummary(Promo promo) {
    return new HomePageResponse.PromoSummary(
        promo.getId(),
        promo.getName(),
        promo.getDescription(),
        promo.getStartDate(),
        promo.getEndDate(),
        promo.getDiscountType(),
        promo.getDiscountValue(),
        promo.getVoucherCode(),
        firstStringValue(promo.getPromoImageUrls())
    );
  }

  private HomePageResponse.ProductSummary toProductSummary(
      Product product,
      Double averageRating,
      Long ratingCount) {
    return new HomePageResponse.ProductSummary(
        product.getId(),
        product.getName(),
        product.getType() != null ? product.getType().name() : null,
        product.getBrand() != null ? product.getBrand().getName() : null,
        product.getBasePrice(),
        product.getStockAvailable() != null && product.getStockAvailable() > 0,
        firstValue(product.getImageUrls()),
        product.getCreatedAt(),
        averageRating,
        ratingCount
    );
  }

  private String firstValue(Collection<?> values) {
    if (values == null || values.isEmpty()) {
      return null;
    }
    Object first = values.iterator().next();
    return first != null ? first.toString() : null;
  }

  private String firstStringValue(Map<String, Object> values) {
    if (values == null || values.isEmpty()) {
      return null;
    }
    for (Object value : values.values()) {
      if (value != null) {
        return value.toString();
      }
    }
    return null;
  }
}