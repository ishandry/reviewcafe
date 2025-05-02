package lnu.ishchuk.reviewcafe.dto;

public record RestaurantDto(
        String id,
        String name,
        String address,
        double latitude,
        double longitude,
        String description
) {}
