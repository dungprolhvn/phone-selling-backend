package ptit.ttcs.phone.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import ptit.ttcs.phone.document.ProductDocument;

import java.util.List;

@Repository
public interface ProductSearchRepository
    extends ElasticsearchRepository<ProductDocument, String> {
}
