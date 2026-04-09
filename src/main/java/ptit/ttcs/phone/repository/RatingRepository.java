package ptit.ttcs.phone.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ptit.ttcs.phone.entity.Rating;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Integer> {
	@EntityGraph(attributePaths = {"user", "product"})
	@Query("SELECT r FROM Rating r WHERE r.user.id = :userId AND r.product.id = :productId")
	Optional<Rating> findByUserIdAndProductId(@Param("userId") Integer userId, @Param("productId") Integer productId);

	@EntityGraph(attributePaths = {"user", "product"})
	@Query("SELECT r FROM Rating r WHERE r.id = :ratingId")
	Optional<Rating> findByIdWithDetails(@Param("ratingId") Integer ratingId);

	@EntityGraph(attributePaths = {"user", "product"})
	Page<Rating> findAllByOrderByCreatedAtDesc(Pageable pageable);

	@EntityGraph(attributePaths = {"user", "product"})
	Page<Rating> findByHiddenOrderByCreatedAtDesc(Boolean hidden, Pageable pageable);

	@Query("SELECT r FROM Rating r WHERE r.product.id = :productId AND r.hidden = false ORDER BY r.createdAt DESC")
	List<Rating> findByProductIdAndHiddenFalseOrderByCreatedAtDesc(@Param("productId") Integer productId);
}