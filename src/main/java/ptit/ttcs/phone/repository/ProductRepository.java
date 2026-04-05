package ptit.ttcs.phone.repository;

import java.util.List;
import java.util.Optional;

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
  List<Product> findByStockAvailableGreaterThanOrderByUpdatedAtDesc(Integer stockAvailable, Pageable pageable);

  @EntityGraph(attributePaths = "brand")
  List<Product> findByOrderByReleaseDateDesc(Pageable pageable);
  
  @Query("SELECT p FROM Product p JOIN FETCH p.brand")
  List<Product> findAllWithBrand();
  
  Optional<Product> getProductById(Integer productId);
}