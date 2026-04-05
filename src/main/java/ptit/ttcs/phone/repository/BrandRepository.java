package ptit.ttcs.phone.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ptit.ttcs.phone.entity.Brand;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Integer> {
	Page<Brand> findAllByOrderByNameAsc(Pageable pageable);
}