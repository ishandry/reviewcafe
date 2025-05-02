package lnu.ishchuk.reviewcafe.config;

import lnu.ishchuk.reviewcafe.dto.RestaurantDto;
import lnu.ishchuk.reviewcafe.service.RestaurantService;
import org.dataloader.BatchLoaderEnvironment;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.BatchLoaderRegistry;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class DataLoaderConfig {

    public DataLoaderConfig(BatchLoaderRegistry registry,
                            RestaurantService restaurantService) {
        registry.forTypePair(String.class, RestaurantDto.class)
                .registerBatchLoader((keys, env) ->
                        restaurantService.getRestaurantsByIds(keys)
                                .collectList()
                                .flatMapMany(list -> {
                                    Map<String, RestaurantDto> map = list.stream()
                                            .collect(Collectors.toMap(
                                                    RestaurantDto::id,
                                                    Function.identity()
                                            ));
                                    List<RestaurantDto> ordered = keys.stream()
                                            .map(map::get)
                                            .collect(Collectors.toList());
                                    return Flux.fromIterable(ordered);
                                })
                );
    }
}
