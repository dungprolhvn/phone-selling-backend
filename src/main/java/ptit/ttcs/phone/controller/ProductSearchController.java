package ptit.ttcs.phone.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ptit.ttcs.phone.document.ProductDocument;
import ptit.ttcs.phone.dto.ProductSearchRequest;
import ptit.ttcs.phone.service.ProductSearchService;
import ptit.ttcs.phone.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@Slf4j
@RequiredArgsConstructor
public class ProductSearchController {
  private final ProductSearchService searchService;
  private final ProductService productService;
  
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
