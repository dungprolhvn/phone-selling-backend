package ptit.ttcs.phone.service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ptit.ttcs.phone.dto.AdminProductResponse;
import ptit.ttcs.phone.dto.ProductRequest;
import ptit.ttcs.phone.entity.Brand;
import ptit.ttcs.phone.entity.Product;
import ptit.ttcs.phone.exception.ConflictException;
import ptit.ttcs.phone.exception.NotFoundException;
import ptit.ttcs.phone.repository.BrandRepository;
import ptit.ttcs.phone.repository.ProductRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminProductService {

  private static final String FEATURED_PRODUCTS_CACHE_KEY = "home:products:featured";
  private static final String NEW_ARRIVALS_CACHE_KEY = "home:products:new-arrivals";

  private final ProductRepository productRepository;
  private final BrandRepository brandRepository;
  private final ProductSearchService productSearchService;
  private final RedisTemplate<String, String> redisTemplate;

  @Transactional
  public AdminProductResponse createProduct(ProductRequest request) {
    validateDuplicateNameForCreate(request.getName());

    Brand brand = brandRepository.findById(request.getBrandId())
        .orElseThrow(() -> new NotFoundException("Khong tim thay thuong hieu"));

    Product product = new Product();
    mapCommonFields(product, request, brand);
    Integer stockAvailable = request.getStockAvailable();
    product.setStockAvailable(stockAvailable != null ? stockAvailable : 0);
    product.setStockReserved(0);
    product.setCreatedAt(Instant.now());
    product.setUpdatedAt(Instant.now());

    Product savedProduct = productRepository.save(product);
    tryIndexProduct(savedProduct);
    invalidateProductCaches(savedProduct.getId());

    log.info("Admin created product {}", savedProduct.getId());
    return toAdminProductResponse(savedProduct);
  }

  @Transactional
  public AdminProductResponse updateProductInfo(Integer productId, ProductRequest request) {
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new NotFoundException("Khong tim thay san pham"));

    validateDuplicateNameForUpdate(request.getName(), productId);

    Brand brand = brandRepository.findById(request.getBrandId())
        .orElseThrow(() -> new NotFoundException("Khong tim thay thuong hieu"));

    mapCommonFields(product, request, brand);
    product.setUpdatedAt(Instant.now());

    Product savedProduct = productRepository.save(product);
    tryIndexProduct(savedProduct);
    invalidateProductCaches(savedProduct.getId());

    log.info("Admin updated product info {}", savedProduct.getId());
    return toAdminProductResponse(savedProduct);
  }

  private void mapCommonFields(Product product, ProductRequest request, Brand brand) {
    product.setName(request.getName().trim());
    product.setType(request.getType());
    product.setBasePrice(request.getBasePrice());
    product.setBrand(brand);
    product.setDescription(request.getDescription());
    product.setImageUrls(request.getImageUrls());

    if (request.getSpecs() != null) {
      Map<String, Object> specs = new HashMap<>();
      specs.putAll(request.getSpecs());
      product.setSpecs(specs);
    } else {
      product.setSpecs(null);
    }

    if (request.getReleaseDate() != null) {
      product.setReleaseDate(request.getReleaseDate().atZone(ZoneId.systemDefault()).toInstant());
    } else {
      product.setReleaseDate(null);
    }
  }

  private void tryIndexProduct(Product product) {
    try {
      productSearchService.indexProduct(product);
    } catch (Exception e) {
      log.warn("Failed to index product {} in Elasticsearch, will be indexed on next search fallback: {}", product.getId(), e.getMessage());
    }
  }

  private void validateDuplicateNameForCreate(String name) {
    if (productRepository.existsByNameIgnoreCase(name.trim())) {
      throw new ConflictException("San pham da ton tai");
    }
  }

  private void validateDuplicateNameForUpdate(String name, Integer productId) {
    if (productRepository.existsByNameIgnoreCaseAndIdNot(name.trim(), productId)) {
      throw new ConflictException("San pham da ton tai");
    }
  }

  private void invalidateProductCaches(Integer productId) {
    redisTemplate.delete(FEATURED_PRODUCTS_CACHE_KEY);
    redisTemplate.delete(NEW_ARRIVALS_CACHE_KEY);
    redisTemplate.delete("product:" + productId);
  }

  private AdminProductResponse toAdminProductResponse(Product product) {
    return AdminProductResponse.builder()
        .id(product.getId())
        .name(product.getName())
        .type(product.getType().name())
        .basePrice(product.getBasePrice())
        .brandId(product.getBrand() != null ? product.getBrand().getId() : null)
        .stockAvailable(product.getStockAvailable())
        .releaseDate(product.getReleaseDate())
        .updatedAt(product.getUpdatedAt())
        .build();
  }
}
