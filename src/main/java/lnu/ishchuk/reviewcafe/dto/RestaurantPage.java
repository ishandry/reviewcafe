package lnu.ishchuk.reviewcafe.dto;

import java.util.List;

public record RestaurantPage(
        int page,
        int size,
        int total,
        List<RestaurantDto> data
) {}
