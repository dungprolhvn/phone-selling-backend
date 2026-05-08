package ptit.ttcs.phone.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.json.JsonData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ptit.ttcs.phone.document.ProductDocument;
import ptit.ttcs.phone.dto.ProductSearchRequest;
import ptit.ttcs.phone.entity.Product;
import ptit.ttcs.phone.repository.ProductSearchRepository;

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
  
  public List<ProductDocument> searchTopKSimilar(Product p, int k) {
    if (p == null || k <= 0) {
      return List.of();
    }

    BoolQuery.Builder boolQuery = new BoolQuery.Builder();

    if (p.getType() != null) {
      boolQuery.filter(f -> f
          .term(t -> t
              .field("type")
              .value(p.getType().name())
          )
      );
    }

    if (p.getId() != null) {
      boolQuery.mustNot(mn -> mn
          .term(t -> t
              .field("mysqlId")
              .value(p.getId())
          )
      );
    }

    if (p.getBasePrice() != null) {
      double basePrice = p.getBasePrice().doubleValue();
      double minPrice = Math.max(0, basePrice - 500000);
      double maxPrice = basePrice + 500000;
      boolQuery.filter(f -> f
          .range(r -> r
              .untyped(u -> u
                  .field("basePrice")
                  .gte(JsonData.of(minPrice))
                  .lte(JsonData.of(maxPrice))
              )
          )
      );
    }

    Map<String, Object> specs = p.getSpecs() != null ? p.getSpecs() : Map.of();
    boolean hasShould = false;

    String ram = getSpec(specs, "Dung lượng RAM");
    hasShould |= addShouldTerm(boolQuery, "ram", ram, 5.0f);

    String storage = getSpec(specs, "Bộ nhớ trong");
    hasShould |= addShouldTerm(boolQuery, "storage", storage, 4.5f);

    String chipset = getSpec(specs, "Chipset");
    hasShould |= addShouldTerm(boolQuery, "chipset", chipset, 4.0f);

    String cpuType = getSpec(specs, "Loại CPU");
    hasShould |= addShouldTerm(boolQuery, "cpuType", cpuType, 3.5f);

    String screenType = getSpec(specs, "Công nghệ màn hình");
    hasShould |= addShouldTerm(boolQuery, "screenType", screenType, 2.0f);

    String os = getSpec(specs, "Hệ điều hành");
    hasShould |= addShouldTerm(boolQuery, "os", os, 1.5f);

    String battery = getSpec(specs, "Pin");
    hasShould |= addShouldTerm(boolQuery, "battery", battery, 1.2f);

    if (p.getType() != null) {
      String type = p.getType().name();
      if ("CHARGER".equals(type) || "CABLE".equals(type)) {
        String chargingPower = getSpec(specs, "Công suất sạc");
        hasShould |= addShouldTerm(boolQuery, "specs.chargingPower", chargingPower, 4.0f);

        String input = getSpec(specs, "Đầu vào");
        hasShould |= addShouldTerm(boolQuery, "specs.input", input, 2.0f);

        String output = getSpec(specs, "Đầu ra");
        hasShould |= addShouldTerm(boolQuery, "specs.output", output, 2.0f);

        String maxUsage = getSpec(specs, "Sử dụng tối đa");
        hasShould |= addShouldTerm(boolQuery, "specs.maxUsage", maxUsage, 1.5f);
      }

      if ("CASE".equals(type)) {
        String compatibleWith = getSpec(specs, "Dùng được cho");
        hasShould |= addShouldTerm(boolQuery, "specs.compatibleWith", compatibleWith, 3.0f);

        String caseType = getSpec(specs, "Phân loại ốp");
        hasShould |= addShouldTerm(boolQuery, "specs.caseType", caseType, 2.5f);

        String material = getSpec(specs, "Chất liệu");
        hasShould |= addShouldTerm(boolQuery, "specs.material", material, 2.0f);

        String productLine = getSpec(specs, "Dòng sản phẩm");
        hasShould |= addShouldTerm(boolQuery, "specs.productLine", productLine, 1.5f);
      }
    }

    if (hasShould) {
      boolQuery.minimumShouldMatch("1");
    }

    Query searchQuery = NativeQuery.builder()
        .withQuery(boolQuery.build()._toQuery())
        .withPageable(PageRequest.of(0, k))
        .build();

    SearchHits<ProductDocument> hits = elasticsearchOperations.search(searchQuery, ProductDocument.class);
    return hits.stream()
        .map(SearchHit::getContent)
        .collect(Collectors.toList());
  }

  public List<ProductDocument> searchCrossSellForPhone(Product phone, int k, List<Integer> excludeIds) {
    if (phone == null || k <= 0) {
      return List.of();
    }

    Set<Integer> excludeSet = new HashSet<>();
    if (excludeIds != null) {
      excludeSet.addAll(excludeIds);
    }
    if (phone.getId() != null) {
      excludeSet.add(phone.getId());
    }

    int caseCount = (int) Math.ceil(k / 3.0);
    int chargerCount = (int) Math.ceil((k - caseCount) / 2.0);
    int cableCount = k - caseCount - chargerCount;

    List<ProductDocument> combined = new ArrayList<>();
    
    // CASES
    List<ProductDocument> cases = searchAccessoriesByType(phone, "CASE", caseCount, excludeSet);
    combined.addAll(cases);
    cases.forEach(doc -> {
      if (doc.getMysqlId() != null) excludeSet.add(doc.getMysqlId());
    });

    // CHARGERS 
    int remainingForCharger = chargerCount + (caseCount - cases.size());
    List<ProductDocument> chargers = searchAccessoriesByType(phone, "CHARGER", remainingForCharger, excludeSet);
    combined.addAll(chargers);
    chargers.forEach(doc -> {
      if (doc.getMysqlId() != null) excludeSet.add(doc.getMysqlId());
    });

    // CABLES
    int remainingForCable = k - combined.size();
    List<ProductDocument> cables = searchAccessoriesByType(phone, "CABLE", remainingForCable, excludeSet);
    combined.addAll(cables);
    cables.forEach(doc -> {
      if (doc.getMysqlId() != null) excludeSet.add(doc.getMysqlId());
    });

    return combined;
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
    
    // 6. Filter by phone specs (backward-compatible params)
    addSpecFilter(boolQuery, "storage", request.getStorage());
    addSpecFilter(boolQuery, "ram", request.getRam());
    addSpecFilter(boolQuery, "screenType", request.getScreenType());
    addSpecFilter(boolQuery, "scanFrequency", request.getScanFrequency());

    // 7. Filter by accessory specs
    addSpecFilter(boolQuery, "cableType", request.getCableType());
    addSpecFilter(boolQuery, "certification", request.getCertification());
    addSpecFilter(boolQuery, "input", request.getInput());
    addSpecFilter(boolQuery, "output", request.getOutput());
    addSpecFilter(boolQuery, "maxUsage", request.getMaxUsage());
    addSpecFilter(boolQuery, "cableLength", request.getCableLength());
    addSpecFilter(boolQuery, "manufacturer", request.getManufacturer());
    addSpecFilter(boolQuery, "material", request.getMaterial());
    addSpecFilter(boolQuery, "chargingPower", request.getChargingPower());
    addSpecFilter(boolQuery, "contentQuality", request.getContentQuality());
    addSpecFilter(boolQuery, "function", request.getFunction());
    addSpecFilter(boolQuery, "utility", request.getUtility());
    addSpecFilter(boolQuery, "inputCurrent", request.getInputCurrent());
    addSpecFilter(boolQuery, "outputCurrent", request.getOutputCurrent());
    addSpecFilter(boolQuery, "caseType", request.getCaseType());
    addSpecFilter(boolQuery, "compatibleWith", request.getCompatibleWith());
    addSpecFilter(boolQuery, "dimensions", request.getDimensions());
    addSpecFilter(boolQuery, "productLine", request.getProductLine());
    addSpecFilter(boolQuery, "features", request.getFeatures());
    
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
      Map<String, String> normalizedSpecs = new HashMap<>();

      String storage = getSpec(specs, "Bộ nhớ trong");
      putSpec(normalizedSpecs, "storage", storage);
      doc.setStorage(storage);

      String rearCamera = getSpec(specs, "Camera sau");
      putSpec(normalizedSpecs, "rearCamera", rearCamera);
      doc.setRearCamera(rearCamera);

      String frontCamera = getSpec(specs, "Camera trước");
      putSpec(normalizedSpecs, "frontCamera", frontCamera);
      doc.setFrontCamera(frontCamera);

      String chipset = getSpec(specs, "Chipset");
      putSpec(normalizedSpecs, "chipset", chipset);
      doc.setChipset(chipset);

      String nfc = getSpec(specs, "Công nghệ NFC");
      putSpec(normalizedSpecs, "nfc", nfc);
      doc.setNfc(nfc);

      String screenType = getSpec(specs, "Công nghệ màn hình");
      putSpec(normalizedSpecs, "screenType", screenType);
      doc.setScreenType(screenType);

      String scanFrequency = getFirstSpec(specs,
          "Tần số quét",
          "Tần số quét màn hình",
          "Refresh rate"
      );
      putSpec(normalizedSpecs, "scanFrequency", scanFrequency);
      doc.setScanFrequency(scanFrequency);

      String sensor = getSpec(specs, "Cảm biến");
      putSpec(normalizedSpecs, "sensor", sensor);
      doc.setSensor(sensor);

      String ram = getSpec(specs, "Dung lượng RAM");
      putSpec(normalizedSpecs, "ram", ram);
      doc.setRam(ram);

      String os = getSpec(specs, "Hệ điều hành");
      putSpec(normalizedSpecs, "os", os);
      doc.setOs(os);

      String screenSize = getSpec(specs, "Kích thước màn hình");
      putSpec(normalizedSpecs, "screenSize", screenSize);
      doc.setScreenSize(screenSize);

      String cpuType = getSpec(specs, "Loại CPU");
      putSpec(normalizedSpecs, "cpuType", cpuType);
      doc.setCpuType(cpuType);

      String battery = getSpec(specs, "Pin");
      putSpec(normalizedSpecs, "battery", battery);
      doc.setBattery(battery);

      String sim = getSpec(specs, "Thẻ SIM");
      putSpec(normalizedSpecs, "sim", sim);
      doc.setSim(sim);

      String screenFeatures = getSpec(specs, "Tính năng màn hình");
      putSpec(normalizedSpecs, "screenFeatures", screenFeatures);
      doc.setScreenFeatures(screenFeatures);

      String compatibility = getSpec(specs, "Tương thích");
      putSpec(normalizedSpecs, "compatibility", compatibility);
      doc.setCompatibility(compatibility);

      String screenResolution = getSpec(specs, "Độ phân giải màn hình");
      putSpec(normalizedSpecs, "screenResolution", screenResolution);
      doc.setScreenResolution(screenResolution);

      // Accessory specs
      putSpec(normalizedSpecs, "cableType", getSpec(specs, "Loại cáp sạc"));
      putSpec(normalizedSpecs, "certification", getSpec(specs, "Công nghệ/Đạt chứng nhận"));
      putSpec(normalizedSpecs, "input", getSpec(specs, "Đầu vào"));
      putSpec(normalizedSpecs, "output", getSpec(specs, "Đầu ra"));
      putSpec(normalizedSpecs, "maxUsage", getSpec(specs, "Sử dụng tối đa"));
      putSpec(normalizedSpecs, "cableLength", getSpec(specs, "Chiều dài dây"));
      putSpec(normalizedSpecs, "manufacturer", getSpec(specs, "Hãng sản xuất"));
      putSpec(normalizedSpecs, "material", getSpec(specs, "Chất liệu"));
      putSpec(normalizedSpecs, "chargingPower", getSpec(specs, "Công suất sạc"));
      putSpec(normalizedSpecs, "contentQuality", getSpec(specs, "Chất lượng nội dung"));
      putSpec(normalizedSpecs, "function", getSpec(specs, "Chức năng"));
      putSpec(normalizedSpecs, "utility", getSpec(specs, "Tiện ích"));
      putSpec(normalizedSpecs, "inputCurrent", getSpec(specs, "Dòng điện vào"));
      putSpec(normalizedSpecs, "outputCurrent", getSpec(specs, "Dòng điện ra"));
      putSpec(normalizedSpecs, "caseType", getSpec(specs, "Phân loại ốp"));
      putSpec(normalizedSpecs, "compatibleWith", getSpec(specs, "Dùng được cho"));
      putSpec(normalizedSpecs, "dimensions", getSpec(specs, "Kích thước"));
      putSpec(normalizedSpecs, "productLine", getSpec(specs, "Dòng sản phẩm"));
      putSpec(normalizedSpecs, "features", getSpec(specs, "Tính năng"));

      doc.setSpecs(normalizedSpecs);
    }
    
    // First image for display in search results
    if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
      doc.setImageUrl((String) product.getImageUrls().get(0));
    }
    return doc;
  }

  private List<ProductDocument> searchAccessoriesByType(Product phone, String type, int k, Set<Integer> excludeIds) {
    if (k <= 0) {
      return List.of();
    }

    Map<String, Object> specs = phone.getSpecs() != null ? phone.getSpecs() : Map.of();
    BoolQuery.Builder boolQuery = new BoolQuery.Builder();
    addTypeFilter(boolQuery, List.of(type));
    addMustNotIds(boolQuery, excludeIds);

    boolean hasShould = false;

    if ("CASE".equals(type)) {
      String phoneName = normalizePhoneNameForAccessory(phone.getName());
      if (phoneName != null) {
        boolQuery.should(s -> s
          .matchPhrase(m -> m
            .field("name")
            .query(phoneName)
            .boost(10.0f)
          )
        );
        boolQuery.should(s -> s
          .match(m -> m
            .field("name")
            .query(phoneName)
            .minimumShouldMatch("2<70%")
            .boost(4.0f)
          )
        );
        boolQuery.should(s -> s
          .matchPhrase(m -> m
            .field("specs.compatibleWith")
            .query(phoneName)
            .boost(8.0f)
          )
        );
        hasShould = true;
      }
    } else if ("CHARGER".equals(type) || "CABLE".equals(type)) {
      String maxCharging = getFirstSpec(specs, "Hỗ trợ sạc tối đa", "Hỗ trợ sạc", "Sạc pin", "Công suất sạc", "Công suất");
      String port = getFirstSpec(specs, "Cổng kết nối/sạc", "Cổng sạc", "Cổng kết nối");
      String brand = phone.getBrand() != null ? phone.getBrand().getName() : null;

      if (brand != null) {
        // Boost accessories with same brand
        hasShould |= addShouldTerm(boolQuery, "brandName", brand, 3.0f);
      }
      
      if (maxCharging != null) {
        boolQuery.should(s -> s
          .match(m -> m
            .field("specs.chargingPower")
            .query(maxCharging)
            .boost(5.0f)
          )
        );
        boolQuery.should(s -> s
          .match(m -> m
            .field("name")
            .query(maxCharging)
            .boost(3.0f)
          )
        );
        hasShould = true;
      }
      
      if (port != null && "CABLE".equals(type)) {
        boolQuery.should(s -> s
          .match(m -> m
            .field("specs.output")
            .query(port)
            .boost(5.0f)
          )
        );
        boolQuery.should(s -> s
          .match(m -> m
            .field("specs.cableType")
            .query(port)
            .boost(4.0f)
          )
        );
        hasShould = true;
      }
    }

    if (hasShould && "CASE".equals(type)) {
      boolQuery.minimumShouldMatch("1");
    }

    List<ProductDocument> results = new ArrayList<>(executeSearch(boolQuery, k));

    if (results.size() < k) {
      int remaining = k - results.size();
      Set<Integer> newExcludes = new HashSet<>();
      if (excludeIds != null) newExcludes.addAll(excludeIds);
      for (ProductDocument doc : results) {
        if (doc.getMysqlId() != null) newExcludes.add(doc.getMysqlId());
      }

      BoolQuery.Builder fallbackQuery = new BoolQuery.Builder();
      addTypeFilter(fallbackQuery, List.of(type));
      addMustNotIds(fallbackQuery, newExcludes);

      Query fallbackSearchQuery = NativeQuery.builder()
          .withQuery(fallbackQuery.build()._toQuery())
          .withPageable(PageRequest.of(0, remaining, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "basePrice")))
          .build();

      SearchHits<ProductDocument> fallbackHits = elasticsearchOperations.search(fallbackSearchQuery, ProductDocument.class);
      results.addAll(fallbackHits.stream().map(SearchHit::getContent).collect(Collectors.toList()));
    }

    return results;
  }

  private void addSpecFilter(BoolQuery.Builder boolQuery, String key, String value) {
    if (value == null || value.isBlank()) {
      return;
    }
    boolQuery.filter(f -> f
        .term(t -> t
            .field("specs." + key)
            .value(value)
        )
    );
  }

  private void addTypeFilter(BoolQuery.Builder boolQuery, List<String> types) {
    if (types == null || types.isEmpty()) {
      return;
    }
    if (types.size() == 1) {
      String type = types.get(0);
      boolQuery.filter(f -> f
          .term(t -> t
              .field("type")
              .value(type)
          )
      );
      return;
    }

    boolQuery.filter(f -> f
        .bool(b -> {
          for (String type : types) {
            b.should(s -> s
                .term(t -> t
                    .field("type")
                    .value(type)
                )
            );
          }
          b.minimumShouldMatch("1");
          return b;
        })
    );
  }

  private void addMustNotIds(BoolQuery.Builder boolQuery, Set<Integer> excludeIds) {
    if (excludeIds == null || excludeIds.isEmpty()) {
      return;
    }
    for (Integer id : excludeIds) {
      if (id == null) {
        continue;
      }
      boolQuery.mustNot(mn -> mn
          .term(t -> t
              .field("mysqlId")
              .value(id)
          )
      );
    }
  }

  private void putSpec(Map<String, String> target, String key, String value) {
    if (value == null || value.isBlank()) {
      return;
    }
    target.put(key, value);
  }

  private String getSpec(Map<String, Object> specs, String key) {
    Object value = specs.get(key);
    if (value == null) {
      return null;
    }
    String text = String.valueOf(value).trim();
    return text.isEmpty() ? null : text;
  }

  private String getFirstSpec(Map<String, Object> specs, String... keys) {
    for (String key : keys) {
      String value = getSpec(specs, key);
      if (value != null) {
        return value;
      }
    }
    return null;
  }

  private String normalizePhoneNameForAccessory(String phoneName) {
    if (phoneName == null) {
      return null;
    }
    String normalized = phoneName;
    normalized = normalized.replaceAll("(?i)điện thoại", " ");
    normalized = normalized.replaceAll("(?i)chính hãng", " ");
    normalized = normalized.replaceAll("(?i)apple|samsung|xiaomi|oppo|vivo|realme|nokia|huawei|asus", " ");
    normalized = normalized.replaceAll("\\(.*?\\)", " ");
    normalized = normalized.replaceAll("(?i)\\b\\d+\\s?(gb|tb)\\b", " ");
    normalized = normalized.replaceAll("\\s+", " ").trim();
    return normalized.isEmpty() ? null : normalized;
  }

  private boolean addShouldTerm(BoolQuery.Builder boolQuery, String field, String value, float boost) {
    if (value == null || value.isBlank()) {
      return false;
    }
    boolQuery.should(s -> s
        .term(t -> t
            .field(field)
            .value(value)
            .boost(boost)
        )
    );
    return true;
  }

  private List<ProductDocument> executeSearch(BoolQuery.Builder boolQuery, int k) {
    Query searchQuery = NativeQuery.builder()
        .withQuery(boolQuery.build()._toQuery())
        .withPageable(PageRequest.of(0, k))
        .build();
    SearchHits<ProductDocument> hits = elasticsearchOperations.search(searchQuery, ProductDocument.class);
    return hits.stream()
        .map(SearchHit::getContent)
        .collect(Collectors.toList());
  }
  
}
