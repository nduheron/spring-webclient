package fr.nduheron.poc.springwebclient.filters;

import fr.nduheron.poc.springwebclient.properties.RetryProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

public class WebClientRetryHandler implements ExchangeFilterFunction {
    private static final Logger logger = LoggerFactory.getLogger(WebClientRetryHandler.class);
    private final RetryProperties properties;

    public WebClientRetryHandler(RetryProperties properties) {
        this.properties = properties;
    }

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        Retry retry = Retry.max(properties.getCount())
                .filter(e -> properties.getMethods().contains(request.method()) && properties.getExceptions().stream().anyMatch(clazz -> clazz.isInstance(e) || clazz.isInstance(NestedExceptionUtils.getRootCause(e))))
                .doBeforeRetry(retrySignal -> {
                    logger.warn("Retrying: {}; Cause: {}.", retrySignal.totalRetries(), retrySignal.failure());
                })
                .onRetryExhaustedThrow(((retrySpec, retrySignal) -> retrySignal.failure()));

        return next.exchange(request).retryWhen(retry);
    }
}
