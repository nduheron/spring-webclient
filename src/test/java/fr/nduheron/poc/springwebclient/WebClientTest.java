package fr.nduheron.poc.springwebclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import fr.nduheron.poc.springwebclient.exception.BadRequestException;
import fr.nduheron.poc.springwebclient.exception.NotFoundException;
import fr.nduheron.poc.springwebclient.exception.TechnicalException;
import fr.nduheron.poc.springwebclient.filters.WebClientExceptionHandler;
import fr.nduheron.poc.springwebclient.filters.WebClientLoggingFilter;
import fr.nduheron.poc.springwebclient.model.User;
import fr.nduheron.poc.springwebclient.model.UserDuplicateException;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {WebClientLoggingFilter.class,
        WebClientExceptionHandler.class, WebClientTest.ContextConfiguration.class})
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

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Autowired
    private WebClient webClient;

    @Test
    public void testOk() {
        User user = webClient.get().uri("/users/{id}", 1).retrieve().bodyToMono(User.class).block();

        assertThat(user.getFirstName()).isEqualTo("Nicolas");
        assertThat(user.getLastName()).isEqualTo("Duhéron");
    }

    @Test
    public void testTimeoutWithRetry() {
        System.out.println("début");

        webClient.get().uri("/users/{id}", 6).retrieve().bodyToMono(User.class).block();

        System.out.println("fin");
//		assertThat(duration.getSeconds()).isGreaterThanOrEqualTo(15l).isLessThan(20L);
    }

    @Test
    public void testBadRequest() {
        exception.expect(BadRequestException.class);
        webClient.get().uri("/users/{id}", 2).retrieve().bodyToMono(User.class).block();
    }

    @Test
    public void testInternalServerError() {
        exception.expect(TechnicalException.class);
        webClient.get().uri("/users/{id}", 3).retrieve().bodyToMono(User.class).block();
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
        exception.expect(NotFoundException.class);
        webClient.get().uri("/users/{id}", 4).retrieve().bodyToMono(User.class).block();
    }

    @Test
    public void testConflict() {
       exception.expect(UserDuplicateException.class);
       webClient.get().uri("/users/{id}", 5).retrieve().onStatus(HttpStatus.CONFLICT::equals, e -> Mono.error(new UserDuplicateException(5))).bodyToMono(User.class).block();
    }
}
