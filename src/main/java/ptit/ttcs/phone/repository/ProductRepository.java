package ptit.ttcs.phone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ptit.ttcs.phone.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
}