package ptit.ttcs.phone.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import ptit.ttcs.phone.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
  List<Product> findByNameContainingIgnoreCase(String keyword);

  @EntityGraph(attributePaths = "brand")
  Page<Product> findAllByStockAvailableGreaterThanOrderByCreatedAtDesc(Integer stockAvailable, Pageable pageable);

  Page<Product> findAllByOrderByCreatedAtDesc(Pageable pageable);

  @EntityGraph(attributePaths = "brand")
  List<Product> findByIdIn(Collection<Integer> ids);
  
  @Query("SELECT p FROM Product p JOIN FETCH p.brand")
  List<Product> findAllWithBrand();
  
  Optional<Product> getProductById(Integer productId);
}