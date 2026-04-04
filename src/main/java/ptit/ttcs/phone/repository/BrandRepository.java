package ptit.ttcs.phone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ptit.ttcs.phone.entity.Brand;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Integer> {
}