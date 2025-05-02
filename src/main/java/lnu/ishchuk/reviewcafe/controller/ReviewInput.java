package lnu.ishchuk.reviewcafe.controller;

public record ReviewInput(
        String restaurantId,
        Integer cleanliness,
        Integer service,
        Integer food,
        String comment
) {}
