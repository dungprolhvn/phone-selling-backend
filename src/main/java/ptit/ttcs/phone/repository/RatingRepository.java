package ptit.ttcs.phone.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import ptit.ttcs.phone.entity.Rating;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Integer> {
	@Query("""
			SELECT r.product.id AS productId,
						 AVG(r.star) AS averageRating,
						 COUNT(r.id) AS ratingCount
			FROM Rating r
			WHERE r.hidden = false
			GROUP BY r.product.id
			ORDER BY AVG(r.star) DESC, COUNT(r.id) DESC
			""")
	Page<ProductRatingStats> findTopRatedProductStats(Pageable pageable);

	interface ProductRatingStats {
		Integer getProductId();

		Double getAverageRating();

		Long getRatingCount();
	}
}