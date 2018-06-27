package fr.nduheron.poc.springwebclient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;

import java.time.Duration;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import fr.nduheron.poc.springwebclient.exception.BadRequestException;
import fr.nduheron.poc.springwebclient.exception.NotFoundException;
import fr.nduheron.poc.springwebclient.exception.TechnicalException;
import fr.nduheron.poc.springwebclient.filters.WebClientExceptionHandler;
import fr.nduheron.poc.springwebclient.filters.WebClientLoggingFilter;
import fr.nduheron.poc.springwebclient.model.User;
import fr.nduheron.poc.springwebclient.model.UserDuplicateException;
import io.netty.handler.timeout.TimeoutException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = { WebClientLoggingFilter.class,
		WebClientExceptionHandler.class, WebClientTest.ContextConfiguration.class })
@TestPropertySource(locations = "classpath:application-test.properties")
public class WebClientTest {
	private static final Logger logger = LoggerFactory.getLogger(WebClientTest.class);

	@Rule
	public WireMockRule wireMockRule = new WireMockRule();

	@Configuration
	static class ContextConfiguration {

		@Bean
		ObjectMapper objectMapper() {
			return new ObjectMapper();
		}

		@Bean
		WebClientFactory webClientFactory() {
			WebClientFactory factory = new WebClientFactory();
			factory.setName("user");
			return factory;
		}
	}

	@Autowired
	private WebClient webClient;

	@Test
	public void testOk() {
		StepVerifier.create(webClient.get().uri("/users/{id}", 1).retrieve().bodyToMono(User.class)).assertNext(u -> {
			assertThat(u.getFirstName()).isEqualTo("Nicolas");
			assertThat(u.getLastName()).isEqualTo("Duhéron");
		}).verifyComplete();
	}

	@Test
	public void testTimeoutWithRetry() {
		Duration duration = StepVerifier.create(webClient.get().uri("/users/{id}", 6).retrieve().bodyToMono(User.class))
				.expectError(TimeoutException.class).verify();

		assertThat(duration.getSeconds()).isGreaterThanOrEqualTo(15l).isLessThan(20L);
	}

	@Test
	public void testBadRequest() {
		StepVerifier.create(webClient.get().uri("/users/{id}", 2).retrieve().bodyToMono(User.class))
				.expectError(BadRequestException.class).verify();
	}

	@Test
	public void testInternalServerError() {
		StepVerifier.create(webClient.get().uri("/users/{id}", 3).retrieve().bodyToMono(User.class))
				.expectError(TechnicalException.class).verify();
	}

	@Test
	public void testIgnoreError() {
		Mono<User> user = webClient.get().uri("/users/{id}", 3).retrieve().bodyToMono(User.class)
				.onErrorResume(error -> {
					logger.warn("Error", error);
					return Mono.empty();
				});
		assertNull(user.block());
	}

	@Test
	public void testNotFound() {
		StepVerifier.create(webClient.get().uri("/users/{id}", 4).retrieve().bodyToMono(User.class))
				.expectError(NotFoundException.class).verify();
	}

	@Test
	public void testConflict() {
		StepVerifier
				.create(webClient.get().uri("/users/{id}", 5).retrieve().onStatus(HttpStatus.CONFLICT::equals, e -> {
					return Mono.error(new UserDuplicateException(5));
				}).bodyToMono(User.class)).expectError(UserDuplicateException.class).verify();
	}
}
