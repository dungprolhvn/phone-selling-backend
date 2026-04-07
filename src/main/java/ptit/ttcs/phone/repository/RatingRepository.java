package ptit.ttcs.phone.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ptit.ttcs.phone.entity.Rating;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Integer> {
	List<Rating> findByProductIdAndHiddenFalseOrderByCreatedAtDesc(Integer productId);
}