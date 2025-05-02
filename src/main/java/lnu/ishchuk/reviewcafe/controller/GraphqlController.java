package lnu.ishchuk.reviewcafe.controller;

import lnu.ishchuk.reviewcafe.dto.RestaurantDto;
import lnu.ishchuk.reviewcafe.model.Review;
import lnu.ishchuk.reviewcafe.repository.ReviewRepository;
import lnu.ishchuk.reviewcafe.service.RestaurantService;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class GraphqlController {

    private final RestaurantService restaurantService;
    private final ReviewRepository reviewRepo;

    public GraphqlController(RestaurantService restaurantService,
                             ReviewRepository reviewRepo) {
        this.restaurantService = restaurantService;
        this.reviewRepo = reviewRepo;
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @QueryMapping
    public List<RestaurantDto> restaurants(@Argument List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return restaurantService.getAllRestaurants()
                    .collectList()
                    .block();
        }
        return restaurantService.getRestaurantsByIds(ids)
                .collectList()
                .block();
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @QueryMapping
    public List<Review> reviews(@Argument String restaurantId) {
        return reviewRepo.findByRestaurantId(restaurantId);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @MutationMapping
    public Review createReview(@Argument("input") ReviewInput input) {
        Review r = new Review();
        r.setRestaurantId(input.restaurantId());
        r.setCleanliness(input.cleanliness());
        r.setService(input.service());
        r.setFood(input.food());
        r.setComment(input.comment());
        return reviewRepo.save(r);
    }

    @PreAuthorize("hasRole('USER')")
    @MutationMapping
    public UpdateReviewResponse updateReview(@Argument("input") UpdateReviewInput input) {
        return reviewRepo.findById(input.id())
                .map(review -> {
                    if (input.cleanliness() != null) review.setCleanliness(input.cleanliness());
                    if (input.service()     != null) review.setService(input.service());
                    if (input.food()        != null) review.setFood(input.food());
                    if (input.comment()     != null) review.setComment(input.comment());
                    Review saved = reviewRepo.save(review);
                    return new UpdateReviewResponse(saved, true, "Review updated successfully");
                })
                .orElse(new UpdateReviewResponse(null, false, "Review not found"));
    }

    @BatchMapping(typeName = "Restaurant", field = "reviews")
    public List<List<Review>> batchReviews(List<RestaurantDto> restaurants) {
        return restaurants.stream()
                .map(r -> reviewRepo.findByRestaurantId(r.id()))
                .collect(Collectors.toList());
    }

    @GraphQlExceptionHandler({AuthenticationException.class, AccessDeniedException.class})
    public GraphQLError handleAuthorizationError(Exception ex, DataFetchingEnvironment env) {
        return GraphqlErrorBuilder.newError(env)
                .errorType(ErrorType.UNAUTHORIZED)
                .message("Unauthorized: no access or authentication required")
                .build();
    }
}
