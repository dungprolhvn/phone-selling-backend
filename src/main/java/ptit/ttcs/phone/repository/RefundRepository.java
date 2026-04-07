package ptit.ttcs.phone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ptit.ttcs.phone.entity.Refund;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Integer> {
}
