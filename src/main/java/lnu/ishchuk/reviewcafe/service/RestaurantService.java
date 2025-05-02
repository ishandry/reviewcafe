package lnu.ishchuk.reviewcafe.service;

import lnu.ishchuk.reviewcafe.dto.RestaurantDto;
import lnu.ishchuk.reviewcafe.dto.RestaurantPage;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class RestaurantService {

    private final WebClient client;

    public RestaurantService(WebClient restaurantWebClient) {
        this.client = restaurantWebClient;
    }

    public Flux<RestaurantDto> getAllRestaurants() {
        return client.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("page", 0)
                        .queryParam("size", Integer.MAX_VALUE)
                        .build())
                .retrieve()
                .bodyToMono(RestaurantPage.class)
                .flatMapMany(page -> Flux.fromIterable(page.data()));
    }

    public Flux<RestaurantDto> getRestaurantsByIds(List<String> ids) {
        return getAllRestaurants()
                .filter(r -> ids.contains(r.id()));
    }
}
