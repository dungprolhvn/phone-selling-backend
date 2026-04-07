package ptit.ttcs.phone.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ptit.ttcs.phone.entity.ShippingAddress;

@Repository
public interface ShippingAddressRepository extends JpaRepository<ShippingAddress, Integer> {
	List<ShippingAddress> findByUserIdOrderByIsDefaultDescCreatedAtDesc(Integer userId);

	Optional<ShippingAddress> findByIdAndUserId(Integer id, Integer userId);

	@Modifying
	@Query("UPDATE ShippingAddress s SET s.isDefault = false WHERE s.user.id = :userId")
	void clearDefaultByUserId(@Param("userId") Integer userId);
}
