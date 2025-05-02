package lnu.ishchuk.reviewcafe.controller;

import lnu.ishchuk.reviewcafe.model.Review;

public record UpdateReviewResponse(
        Review review,
        boolean success,
        String message
) {}
