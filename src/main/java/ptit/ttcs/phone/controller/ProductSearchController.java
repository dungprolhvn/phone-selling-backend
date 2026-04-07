package ptit.ttcs.phone.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ptit.ttcs.phone.document.ProductDocument;
import ptit.ttcs.phone.dto.HomeProductResponse;
import ptit.ttcs.phone.dto.ProductDetailResponse;
import ptit.ttcs.phone.dto.ProductSearchRequest;
import ptit.ttcs.phone.service.HomePageService;
import ptit.ttcs.phone.service.ProductDetailService;
import ptit.ttcs.phone.service.ProductSearchService;
import ptit.ttcs.phone.service.ProductService;

@RestController
@RequestMapping("/api/products")
@Slf4j
@RequiredArgsConstructor
public class ProductSearchController {
  private final ProductSearchService searchService;
  private final ProductService productService;
  private final HomePageService homePageService;
  private final ProductDetailService productDetailService;

  @GetMapping("/featured")
  public ResponseEntity<List<HomeProductResponse>> getFeaturedProducts() {
    return ResponseEntity.ok(homePageService.getFeaturedProducts());
  }

  @GetMapping("/new-arrivals")
  public ResponseEntity<List<HomeProductResponse>> getNewArrivals() {
    return ResponseEntity.ok(homePageService.getNewArrivals());
  }

  @GetMapping("/{productId}")
  public ResponseEntity<ProductDetailResponse> getProductDetail(@PathVariable Integer productId) {
    return ResponseEntity.ok(productDetailService.getProductDetail(productId));
  }
  
  @GetMapping("/search")
  public ResponseEntity<List<ProductDocument>> search(@ModelAttribute ProductSearchRequest request) {
    try {
      return ResponseEntity.ok(searchService.search(request));
    } catch (Exception e) {
      // Fallback to MySQL if Elasticsearch is down
      log.error(e.getMessage());
      log.warn("Elasticsearch unavailable, falling back to MySQL");
      return ResponseEntity.ok(productService.searchFallback(request.getKeyword()));
    }
  }
}
