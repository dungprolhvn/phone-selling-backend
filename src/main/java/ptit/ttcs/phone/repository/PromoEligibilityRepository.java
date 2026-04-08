package ptit.ttcs.phone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ptit.ttcs.phone.entity.PromoEligibility;
import ptit.ttcs.phone.entity.PromoEligibilityId;

@Repository
public interface PromoEligibilityRepository extends JpaRepository<PromoEligibility, PromoEligibilityId> {
}
