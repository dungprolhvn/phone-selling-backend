package ptit.ttcs.phone;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import ptit.ttcs.phone.entity.Product;
import ptit.ttcs.phone.repository.ProductRepository;
import ptit.ttcs.phone.repository.ProductSearchRepository;
import ptit.ttcs.phone.service.ProductSearchService;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchIndexer implements ApplicationRunner {
  
  private final ProductRepository productRepository;
  private final ProductSearchService productSearchService;
  private final ProductSearchRepository productSearchRepository;
  
  @Override
  public void run(ApplicationArguments args) {
    boolean shouldReindex = getBooleanArg(args, "shouldReindex", false);
    long count = productSearchRepository.count();

    if (shouldReindex) {
      log.info("Reindexing ES forcefully!");
      reindexAll();
    }

    if (count > 0) {
      log.info("Elasticsearch already has {} products — skipping indexing", count);
      return;
    }
    
    log.info("Elasticsearch index is empty — starting bulk indexing from MySQL...");
    reindexAll();
  }
  
  private void reindexAll() {
    List<Product> products = productRepository.findAllWithBrand();
    
    int success = 0;
    int failed = 0;
    
    for (Product product : products) {
      try {
        productSearchService.indexProduct(product);
        success++;
      } catch (Exception e) {
        log.error("Failed to index product id={}", product.getId(), e);
        failed++;
      }
    }
    
    log.info("Indexing complete — success: {}, failed: {}", success, failed);
  }

  
  private boolean getBooleanArg(ApplicationArguments args, String name, boolean defaultValue) {
    if (!args.containsOption(name)) {
      return defaultValue;
    }

    List<String> values = args.getOptionValues(name);

    if (values == null || values.isEmpty()) {
      return true;
    }

    return Boolean.parseBoolean(values.get(0));
  }

}
