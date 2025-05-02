package lnu.ishchuk.reviewcafe.repository;

import lnu.ishchuk.reviewcafe.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByRestaurantId(String restaurantId);
}
