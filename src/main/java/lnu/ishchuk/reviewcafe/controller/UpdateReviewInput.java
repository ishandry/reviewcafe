package lnu.ishchuk.reviewcafe.controller;

public record UpdateReviewInput(
        Long id,
        Integer cleanliness,
        Integer service,
        Integer food,
        String comment
) {}
