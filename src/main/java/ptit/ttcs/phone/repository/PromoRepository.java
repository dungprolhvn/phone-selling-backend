package ptit.ttcs.phone.repository;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ptit.ttcs.phone.entity.Promo;

@Repository
public interface PromoRepository extends JpaRepository<Promo, Integer> {
  Page<Promo> findByStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByCreatedAtDesc(
      Instant startAt,
      Instant endAt,
      Pageable pageable);
}