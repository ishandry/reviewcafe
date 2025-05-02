package lnu.ishchuk.reviewcafe;

import com.github.tomakehurst.wiremock.WireMockServer;
import lnu.ishchuk.reviewcafe.model.Review;
import lnu.ishchuk.reviewcafe.repository.ReviewRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertySource;
import lnu.ishchuk.reviewcafe.TestSecurityConfig;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.http.HttpHeaders;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
@Testcontainers
@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = ReviewcafeIntegrationTests.Initializer.class)

public class ReviewcafeIntegrationTests {

	private static String keycloakToken;

	@Autowired
	ReviewRepository reviewRepo;

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
			.withDatabaseName("testdb")
			.withUsername("test")
			.withPassword("test");

	static WireMockServer wireMock;

	@Autowired
	private WebTestClient client;

	@BeforeAll
	static void beforeAll() {
		wireMock = new WireMockServer(0);
		wireMock.start();
		wireMock.stubFor(get(urlPathEqualTo("/api/restaurants"))
				.willReturn(aResponse()
						.withHeader("Content-Type", "application/json")
						.withBody("{\"data\":[{\"id\":\"1\",\"name\":\"Test Restaurant\",\"address\":\"Test St\",\"latitude\":0.0,\"longitude\":0.0,\"description\":\"desc\"}]}")));

		keycloakToken = WebClient.create("http://localhost:8180")
				.post()
				.uri("/realms/myrealm/protocol/openid-connect/token")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.bodyValue("grant_type=password"
						+ "&client_id=reviewcafe-graphql"
						+ "&username=andri@test.com"
						+ "&password=123")
				.retrieve()
				.bodyToMono(JsonNode.class)
				.map(json -> json.get("access_token").asText())
				.block();
	}

	@AfterAll
	static void afterAll() {
		wireMock.stop();
	}

	@DynamicPropertySource
	static void registerProps(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
		registry.add("restaurant.base-url", () -> wireMock.baseUrl() + "/api/restaurants");
	}

	public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
		@Override
		public void initialize(ConfigurableApplicationContext context) {
			// Assuming your Keycloak runs on localhost:8180 and the realm is 'myrealm'
			String keycloakBaseUrl = "http://localhost:8180/realms/myrealm";

			TestPropertyValues.of(
					"spring.graphql.graphiql.enabled=false",
					// Point to the actual JWK Set URI of your Keycloak instance
					"spring.security.oauth2.resourceserver.jwt.jwk-set-uri=" + keycloakBaseUrl + "/protocol/openid-connect/certs",
					// Point to the actual Issuer URI of your Keycloak instance
					"spring.security.oauth2.resourceserver.jwt.issuer-uri=" + keycloakBaseUrl
			).applyTo(context.getEnvironment());
		}
	}

	@Test
	void testRestaurantsRequiresAuth() {
		client.post().uri("/graphql")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue("{\"query\":\"{ restaurants { id name } }\"}")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.errors[0].message")
				.isEqualTo("Unauthorized: доступ заборонено або потребує аутентифікації");
	}

	@Test
	void testGetRestaurantsWithMockUser() {
		client.mutate()
				.defaultHeaders(headers -> headers.setBearerAuth(keycloakToken))
				.build()
				.post()
				.uri("/graphql")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue("{\"query\":\"{ restaurants { id name } }\"}")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.data.restaurants[0].id").isEqualTo("1")
				.jsonPath("$.data.restaurants[0].name").isEqualTo("Cafe Blue");
	}

	@Test
	void testCreateReviewMutation() {
		String mutation = """
				    mutation CreateReview($input: ReviewInput!) {
				      createReview(input: $input) {
				        id
				        restaurantId
				        cleanliness
				        service
				        food
				        comment
				      }
				    }
				""";

		Map<String, Object> variables = Map.of(
				"input", Map.of(
						"restaurantId", "1",
						"cleanliness", 5,
						"service", 4,
						"food", 5,
						"comment", "Test comment"
				)
		);

		Map<String, Object> payload = Map.of(
				"query", mutation,
				"variables", variables
		);

		client.mutate()
				.defaultHeaders(headers -> headers.setBearerAuth(keycloakToken))
				.build()
				.post()
				.uri("/graphql")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(payload)
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.data.createReview.id").isNotEmpty()
				.jsonPath("$.data.createReview.restaurantId").isEqualTo("1")
				.jsonPath("$.data.createReview.cleanliness").isEqualTo(5)
				.jsonPath("$.data.createReview.service").isEqualTo(4)
				.jsonPath("$.data.createReview.food").isEqualTo(5)
				.jsonPath("$.data.createReview.comment").isEqualTo("Test comment");
	}

	@Test
	void testUpdateReviewMutation() {
		Review toSave = new Review();
		toSave.setRestaurantId("1");
		toSave.setCleanliness(2);
		toSave.setService(2);
		toSave.setFood(2);
		toSave.setComment("initial comment");
		Review original = reviewRepo.save(toSave);

		String mutation = """
          mutation Update($input: UpdateReviewInput!) {
            updateReview(input: $input) {
              success
              message
              review {
                id
                cleanliness
                comment
              }
            }
          }
        """;

		var payload = Map.of(
				"query", mutation,
				"variables", Map.of("input", Map.of(
						"id", original.getId().toString(),
						"cleanliness", 4,
						"comment", "updated!"
				))
		);

		client.mutate()
				.defaultHeaders(h -> h.setBearerAuth(keycloakToken))
				.build()
				.post().uri("/graphql")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(payload)
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.data.updateReview.success").isEqualTo(true)
				.jsonPath("$.data.updateReview.message").isEqualTo("Review updated successfully")
				.jsonPath("$.data.updateReview.review.cleanliness").isEqualTo(4)
				.jsonPath("$.data.updateReview.review.comment").isEqualTo("updated!");
	}
}
