package ptit.ttcs.phone.repository;

import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ptit.ttcs.phone.entity.Product;
import ptit.ttcs.phone.entity.Promo;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromoRepository extends JpaRepository<Promo, Integer> {
  @Query("SELECT p FROM Promo p WHERE p.voucherCode = :promoCode AND p.endDate > :date AND p.usageCount < p.usageLimit")
  Optional<Promo> getUsablePromoByVoucherCode(String promoCode, Instant date);
  @Query("SELECT CASE WHEN COUNT(pe) > 0 THEN true ELSE false END FROM PromoEligibility pe WHERE pe.product = :productId AND pe.promo = :promoId")
  boolean isEligibleForPromo(int productId, int promoId);
  
  @Nullable List<Promo> findAll();
}