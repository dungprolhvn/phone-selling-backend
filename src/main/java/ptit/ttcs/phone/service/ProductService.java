package ptit.ttcs.phone.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import ptit.ttcs.phone.document.ProductDocument;
import ptit.ttcs.phone.dto.ProductRequest;
import ptit.ttcs.phone.dto.ProductSearchRequest;
import ptit.ttcs.phone.entity.Product;
import ptit.ttcs.phone.repository.ProductRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
  
  private final ProductRepository productRepository;
  private final ProductSearchService productSearchService;
  
  // Called by ProductSearchController — tries ES first, falls back to MySQL
  public List<ProductDocument> search(ProductSearchRequest request) {
    try {
      return productSearchService.search(request);
    } catch (Exception e) {
      log.warn("Elasticsearch unavailable, falling back to MySQL search");
      return searchFallback(request.getKeyword());
    }
  }
  
  // MySQL fallback when ES is down
  public List<ProductDocument> searchFallback(String keyword) {
    if (keyword == null || keyword.isBlank()) {
      return List.of(); // no keyword + no ES = return empty
    }
    return productRepository
        .findByNameContainingIgnoreCase(keyword)
        .stream()
        .map(this::toSimpleDocument)
        .collect(Collectors.toList());
  }
  
  // Minimal mapper — only fields available without ES
  private ProductDocument toSimpleDocument(Product product) {
    ProductDocument doc = new ProductDocument();
    doc.setId(String.valueOf(product.getId()));
    doc.setMysqlId(product.getId());
    doc.setName(product.getName());
    doc.setBasePrice(product.getBasePrice().doubleValue());
    doc.setType(product.getType().name());
    doc.setInStock(product.getStockAvailable() > 0);
    if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
      doc.setImageUrl((String) product.getImageUrls().get(0));
    }
    return doc;
  }
}
