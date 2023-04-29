package fr.nduheron.poc.springwebclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import fr.nduheron.poc.springwebclient.configuration.WebClientMonitoringFilterConfiguration;
import fr.nduheron.poc.springwebclient.model.User;
import fr.nduheron.poc.springwebclient.model.UserDuplicateException;
import fr.nduheron.poc.springwebclient.properties.WebClientProperties;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.logging.LoggingMeterRegistry;
import io.netty.handler.timeout.TimeoutException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties;
import org.springframework.boot.actuate.metrics.web.reactive.client.DefaultWebClientExchangeTagsProvider;
import org.springframework.boot.actuate.metrics.web.reactive.client.WebClientExchangeTagsProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties
@ContextConfiguration(
        loader = AnnotationConfigContextLoader.class,
        classes = {WebClientTest.ContextConfiguration.class, WebClientMonitoringFilterConfiguration.class}
)
@TestPropertySource(locations = "classpath:application-test.properties")
public class WebClientTest {
    private static final Logger logger = LoggerFactory.getLogger(WebClientTest.class);

    public static WireMockServer wireMockServer;

    @Autowired
    private WebClient webClient;

    @Test
    void testOk() {
        StepVerifier.create(webClient.get().uri("/users/{id}", 1).retrieve().bodyToMono(User.class)).assertNext(u -> {
            assertThat(u.getFirstName()).isEqualTo("Nicolas");
            assertThat(u.getLastName()).isEqualTo("Duhéron");
        }).verifyComplete();

        wireMockServer.verify(1, getRequestedFor(urlEqualTo("/users/1")));
    }

    @Test
    void testTimeoutWithRetry() {
        StepVerifier.create(webClient.get().uri("/users/{id}", 6)
                .retrieve()
                .bodyToMono(User.class))
                .expectErrorMatches(e -> (e instanceof WebClientRequestException) && e.getCause() instanceof TimeoutException)
                .verify();

        wireMockServer.verify(2, getRequestedFor(urlEqualTo("/users/6")));
    }

    @Test
    void testBadRequest() {
        StepVerifier.create(webClient.get().uri("/users/{id}", 2).retrieve()
                .bodyToMono(User.class))
                .expectError(WebClientResponseException.BadRequest.class)
                .verify();
        wireMockServer.verify(1, getRequestedFor(urlEqualTo("/users/2")));
    }

    @Test
    void testInternalServerError() {
        StepVerifier.create(webClient.get().uri("/users/{id}", 3).retrieve()
                .bodyToMono(User.class))
                .expectError(WebClientResponseException.InternalServerError.class)
                .verify();
    }

    @Test
    void testIgnoreError() {
        Mono<User> user = webClient.get().uri("/users/{id}", 3).retrieve().bodyToMono(User.class)
                .onErrorResume(error -> {
                    logger.warn("Error", error);
                    return Mono.empty();
                });
        assertThat(user.block()).isNull();

        wireMockServer.verify(1, getRequestedFor(urlEqualTo("/users/3")));
    }

    @Test
    void testNotFound() {
        StepVerifier.create(webClient.get().uri("/users/{id}", 4).retrieve()
                .bodyToMono(User.class))
                .expectError(WebClientResponseException.NotFound.class)
                .verify();

        wireMockServer.verify(1, getRequestedFor(urlEqualTo("/users/4")));
    }

    @Test
    void testConflict() {
        StepVerifier.create(webClient.get().uri("/users/{id}", 5).retrieve()
                .onStatus(HttpStatus.CONFLICT::equals, e -> Mono.error(new UserDuplicateException()))
                .bodyToMono(User.class))
                .expectError(UserDuplicateException.class)
                .verify();

        wireMockServer.verify(1, getRequestedFor(urlEqualTo("/users/5")));
    }

    @BeforeAll
    public static void start() {
        wireMockServer = new WireMockServer();
        wireMockServer.start();
    }

    @AfterAll
    public static void stop() {
        wireMockServer.stop();
    }

    @AfterEach
    public void reset() {
        wireMockServer.resetAll();
    }

    @Configuration
    static class ContextConfiguration {

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        WebClientFactory webClientFactory() {
            WebClientFactory factory = new WebClientFactory();
            factory.setProperties(userWebClientProperties());
            return factory;
        }

        @Bean
        MeterRegistry meterRegistry() {
            return new LoggingMeterRegistry();
        }

        @Bean
        WebClientExchangeTagsProvider tagsProviders() {
            return new DefaultWebClientExchangeTagsProvider();
        }

        @Bean
        MetricsProperties metricsProperties() {
            return new MetricsProperties();
        }

        @Bean
        @ConfigurationProperties(prefix = "client.user")
        WebClientProperties userWebClientProperties() {
            return new WebClientProperties();
        }
    }
}
