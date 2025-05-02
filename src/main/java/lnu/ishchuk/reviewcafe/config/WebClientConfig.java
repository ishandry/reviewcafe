package lnu.ishchuk.reviewcafe.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient restaurantWebClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:3000/api/restaurants")
                .build();
    }
}
