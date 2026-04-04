package ptit.ttcs.phone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ptit.ttcs.phone.entity.Promo;

@Repository
public interface PromoRepository extends JpaRepository<Promo, Integer> {
}