package ptit.ttcs.phone.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import ptit.ttcs.phone.dto.BrandResponse;
import ptit.ttcs.phone.dto.HomeProductResponse;
import ptit.ttcs.phone.entity.Brand;
import ptit.ttcs.phone.entity.Product;
import ptit.ttcs.phone.repository.BrandRepository;
import ptit.ttcs.phone.repository.ProductRepository;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HomePageService {

  private static final String FEATURED_PRODUCTS_CACHE_KEY = "home:products:featured";
  private static final String NEW_ARRIVALS_CACHE_KEY = "home:products:new-arrivals";
  private static final String BRANDS_CACHE_KEY = "home:brands";
  private static final Duration HOMEPAGE_CACHE_TTL = Duration.ofMinutes(10);
  private static final int DEFAULT_HOME_PRODUCT_LIMIT = 12;

  private final ProductRepository productRepository;
  private final BrandRepository brandRepository;
  private final RedisTemplate<String, String> redisTemplate;
  private final ObjectMapper objectMapper;

  public List<HomeProductResponse> getFeaturedProducts() {
    return readThroughCache(
        FEATURED_PRODUCTS_CACHE_KEY,
        new TypeReference<List<HomeProductResponse>>() { },
        () -> productRepository
            .findByStockAvailableGreaterThanOrderByUpdatedAtDesc(
                0,
                PageRequest.of(0, DEFAULT_HOME_PRODUCT_LIMIT)
            )
            .stream()
            .map(this::toHomeProductResponse)
            .toList()
    );
  }

  public List<HomeProductResponse> getNewArrivals() {
    return readThroughCache(
        NEW_ARRIVALS_CACHE_KEY,
        new TypeReference<List<HomeProductResponse>>() { },
        () -> productRepository
            .findByOrderByReleaseDateDesc(PageRequest.of(0, DEFAULT_HOME_PRODUCT_LIMIT))
            .stream()
            .map(this::toHomeProductResponse)
            .toList()
    );
  }

  public List<BrandResponse> getBrands() {
    return readThroughCache(
        BRANDS_CACHE_KEY,
        new TypeReference<List<BrandResponse>>() { },
        () -> brandRepository
            .findAll()
            .stream()
            .sorted(Comparator.comparing(Brand::getName, String.CASE_INSENSITIVE_ORDER))
            .map(this::toBrandResponse)
            .toList()
    );
  }

  private <T> List<T> readThroughCache(String cacheKey, TypeReference<List<T>> typeReference, DataSupplier<T> supplier) {
    try {
      String cachedValue = redisTemplate.opsForValue().get(cacheKey);
      if (cachedValue != null) {
        return objectMapper.readValue(cachedValue, typeReference);
      }
    } catch (Exception e) {
      log.warn("Cache read failed for key {}: {}", cacheKey, e.getMessage());
    }

    List<T> data = supplier.get();

    try {
      redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(data), HOMEPAGE_CACHE_TTL);
    } catch (Exception e) {
      log.warn("Cache write failed for key {}: {}", cacheKey, e.getMessage());
    }

    return data;
  }

  private HomeProductResponse toHomeProductResponse(Product product) {
    String imageUrl = null;
    if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
      imageUrl = product.getImageUrls().get(0);
    }

    return new HomeProductResponse(
        product.getId(),
        product.getName(),
        product.getBasePrice(),
        product.getType().name(),
        product.getBrand().getName(),
        imageUrl,
        product.getStockAvailable() > 0
    );
  }

  private BrandResponse toBrandResponse(Brand brand) {
    return new BrandResponse(brand.getId(), brand.getName(), brand.getLogoImageUrls());
  }

  @FunctionalInterface
  private interface DataSupplier<T> {
    List<T> get();
  }
}
