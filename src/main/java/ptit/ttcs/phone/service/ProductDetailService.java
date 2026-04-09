package ptit.ttcs.phone.service;

import java.time.Duration;
import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ptit.ttcs.phone.dto.ProductDetailResponse;
import ptit.ttcs.phone.dto.RatingResponse;
import ptit.ttcs.phone.entity.Product;
import ptit.ttcs.phone.exception.NotFoundException;
import ptit.ttcs.phone.repository.ProductRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductDetailService {

  private static final Duration PRODUCT_DETAIL_CACHE_TTL = Duration.ofMinutes(10);

  private final ProductRepository productRepository;
  private final RatingService ratingService;
  private final RedisTemplate<String, String> redisTemplate;
  private final ObjectMapper objectMapper;

  public ProductDetailResponse getProductDetail(Integer productId) {
    String cacheKey = "product:" + productId;

    try {
      String cachedValue = redisTemplate.opsForValue().get(cacheKey);
      if (cachedValue != null) {
        return objectMapper.readValue(cachedValue, ProductDetailResponse.class);
      }
    } catch (Exception e) {
      log.warn("Cache read failed for key {}: {}", cacheKey, e.getMessage());
    }

    Product product = productRepository.findDetailById(productId)
        .orElseThrow(() -> new NotFoundException("Khong tim thay san pham"));

    List<RatingResponse> reviews = ratingService.getVisibleRatingsByProductId(productId);

    double averageRating = reviews.stream()
        .mapToInt(r -> r.getStar() == null ? 0 : r.getStar())
        .average()
        .orElse(0.0);

    ProductDetailResponse response = new ProductDetailResponse(
        product.getId(),
        product.getName(),
        product.getType().name(),
        product.getBasePrice(),
        product.getBrand().getName(),
        product.getSpecs(),
        product.getDescription(),
        product.getReleaseDate(),
        product.getImageUrls(),
        product.getStockAvailable() > 0,
        product.getStockAvailable(),
        averageRating,
        reviews
    );

    try {
      redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(response), PRODUCT_DETAIL_CACHE_TTL);
    } catch (Exception e) {
      log.warn("Cache write failed for key {}: {}", cacheKey, e.getMessage());
    }

    return response;
  }
}
