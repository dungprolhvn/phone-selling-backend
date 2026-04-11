package ptit.ttcs.phone.service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.json.JsonData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import ptit.ttcs.phone.document.ProductDocument;
import ptit.ttcs.phone.dto.ProductSearchRequest;
import ptit.ttcs.phone.entity.Product;
import ptit.ttcs.phone.repository.ProductSearchRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductSearchService {
  private final ElasticsearchOperations elasticsearchOperations;
  private final ProductSearchRepository productSearchRepository;
  // ── INDEXING ──────────────────────────────────────────────
  // Called by ProductService after adding/updating a product in MySQL
  public void indexProduct(Product product) {
    ProductDocument doc = toDocument(product);
    productSearchRepository.save(doc);
  }
  
  // Called by ProductService after deleting a product
  public void removeProduct(Integer mysqlId) {
    productSearchRepository.deleteById(String.valueOf(mysqlId));
  }
  
  // ── SEARCHING ─────────────────────────────────────────────
  public List<ProductDocument> search(ProductSearchRequest request) {
    Query searchQuery = buildSearchQuery(request);
    SearchHits<ProductDocument> hits = elasticsearchOperations.search(searchQuery, ProductDocument.class);
    return hits
        .stream()
        .map(SearchHit::getContent)
        .collect(Collectors.toList());
  }
  
  // ── QUERY BUILDER ─────────────────────────────────────────
  private Query buildSearchQuery(ProductSearchRequest request) {
    
    // Create the builder directly
    BoolQuery.Builder boolQuery = new BoolQuery.Builder();
    
    // 1. Full-text search (Must match)
    if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
      boolQuery.must(m -> m
          .multiMatch(mm -> mm
              .fields("name^3", "description")
              .query(request.getKeyword())
              .fuzziness("AUTO")
          )
      );
    }
    
    // 2. Filter by type
    if (request.getType() != null) {
      boolQuery.filter(f -> f
          .term(t -> t
              .field("type")
              .value(request.getType())
          )
      );
    }
    
    // 3. Filter by brand
    if (request.getBrandName() != null) {
      boolQuery.filter(f -> f
          .term(t -> t
              .field("brandName")
              .value(request.getBrandName())
          )
      );
    }
    
    // 4. Filter by price range
    if (request.getMinPrice() != null || request.getMaxPrice() != null) {
      boolQuery.filter(f -> f
          .range(r -> r
              .untyped(u -> {
                u.field("basePrice");
                if (request.getMinPrice() != null) {
                  u.gte(JsonData.of(request.getMinPrice()));
                }
                if (request.getMaxPrice() != null) {
                  u.lte(JsonData.of(request.getMaxPrice()));
                }
                return u;
              })
          )
      );
    }
    
    // 5. Filter by inStock
    if (Boolean.TRUE.equals(request.getInStockOnly())) {
      boolQuery.filter(f -> f
          .term(t -> t
              .field("inStock")
              .value(true)
          )
      );
    }
    
    // 6. Filter by storage
    if (request.getStorage() != null) {
      boolQuery.filter(f -> f
          .term(t -> t
              .field("storage")
              .value(request.getStorage())
          )
      );
    }
    
    // 7. Filter by RAM
    if (request.getRam() != null) {
      boolQuery.filter(f -> f
          .term(t -> t
              .field("ram")
              .value(request.getRam())
          )
      );
    }
    
    // 8. Filter by screen type
    if (request.getScreenType() != null) {
      boolQuery.filter(f -> f
          .term(t -> t
              .field("screenType")
              .value(request.getScreenType())
          )
      );
    }
    
    // 9. Filter by scan frequency
    if (request.getScanFrequency() != null) {
      boolQuery.filter(f -> f
          .term(t -> t
              .field("scanFrequency")
              .value(request.getScanFrequency())
          )
      );
    }
    
    // Bridge the Elasticsearch Client query into the Spring Data NativeQuery
    return NativeQuery.builder()
        .withQuery(boolQuery.build()._toQuery())
        .withPageable(PageRequest.of(
            request.getPage() != null ? request.getPage() : 0,
            request.getSize() != null ? request.getSize() : 20
        ))
        .build();
  }
  
  public List<ProductDocument> searchByKeywords(List<String> keywords) {
    if (keywords == null || keywords.isEmpty()) {
      return List.of();
    }
    
    String combinedKeywords = String.join(" ", keywords);
    
    BoolQuery.Builder boolQuery = new BoolQuery.Builder();
    
    boolQuery.must(m -> m
        .multiMatch(mm -> mm
            .fields("name^3", "description", "brandName", "storage", "ram",
                "chipset", "screenType", "os", "battery")
            .query(combinedKeywords)
            .fuzziness("AUTO")
        )
    );
    
    // Only return in-stock products for chatbot recommendations
    boolQuery.filter(f -> f
        .term(t -> t
            .field("inStock")
            .value(true)
        )
    );
    
    Query searchQuery = NativeQuery.builder()
        .withQuery(boolQuery.build()._toQuery())
        .withPageable(PageRequest.of(0, 5))
        .build();
    
    SearchHits<ProductDocument> hits = elasticsearchOperations.search(searchQuery, ProductDocument.class);
    
    return hits.stream()
        .map(SearchHit::getContent)
        .collect(Collectors.toList());
  }
  
  private ProductDocument toDocument(Product product) {
    ProductDocument doc = new ProductDocument();
    doc.setId(String.valueOf(product.getId()));
    doc.setMysqlId(product.getId());
    doc.setType(product.getType().name());
    doc.setName(product.getName());
    doc.setBasePrice(product.getBasePrice().doubleValue());
    doc.setBrandName(product.getBrand().getName());
    doc.setInStock(product.getStockAvailable() > 0);
    doc.setDescription(product.getDescription());
    
    // Extract spec fields from JSON
    if (product.getSpecs() != null) {
      Map<String, Object> specs = product.getSpecs();
      doc.setStorage((String) specs.get("Bộ nhớ trong"));
      doc.setRearCamera((String) specs.get("Camera sau"));
      doc.setFrontCamera((String) specs.get("Camera trước"));
      doc.setChipset((String) specs.get("Chipset"));
      doc.setNfc((String) specs.get("Công nghệ NFC"));
      doc.setScreenType((String) specs.get("Công nghệ màn hình"));
      doc.setSensor((String) specs.get("Cảm biến"));
      doc.setRam((String) specs.get("Dung lượng RAM"));
      doc.setOs((String) specs.get("Hệ điều hành"));
      doc.setScreenSize((String) specs.get("Kích thước màn hình"));
      doc.setCpuType((String) specs.get("Loại CPU"));
      doc.setBattery((String) specs.get("Pin"));
      doc.setSim((String) specs.get("Thẻ SIM"));
      doc.setScreenFeatures((String) specs.get("Tính năng màn hình"));
      doc.setCompatibility((String) specs.get("Tương thích"));
      doc.setScreenResolution((String) specs.get("Độ phân giải màn hình"));
    }
    
    // First image for display in search results
    if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
      doc.setImageUrl((String) product.getImageUrls().get(0));
    }
    return doc;
  }
  
}
