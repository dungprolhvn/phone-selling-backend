package ptit.ttcs.phone.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ptit.ttcs.phone.entity.Rating;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Integer> {
	List<Rating> findByProductIdAndHiddenFalseOrderByCreatedAtDesc(Integer productId);
	
	@Query("SELECT r FROM Rating r WHERE r.user.id = :userId AND r.product.id = :productId")
	Optional<Rating> findByUserIdAndProductId(@Param("userId") Integer userId, @Param("productId") Integer productId);
	
	@Query("SELECT r FROM Rating r JOIN FETCH r.user JOIN FETCH r.product WHERE r.id = :ratingId")
	Optional<Rating> findByIdWithDetails(@Param("ratingId") Integer ratingId);
}