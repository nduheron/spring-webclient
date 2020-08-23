package fr.nduheron.poc.springwebclient.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;

import io.netty.handler.timeout.TimeoutException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.util.Arrays;
import java.util.List;

public class WebClientRetryHandler implements ExchangeFilterFunction {
    private static final Logger logger = LoggerFactory.getLogger(WebClientRetryHandler.class);
    private List<HttpMethod> retryMethods = Arrays.asList(HttpMethod.GET, HttpMethod.DELETE, HttpMethod.PUT);
    private int retryCount;

	public WebClientRetryHandler(int retryCount) {
		this.retryCount = retryCount;
	}

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        Retry retry = Retry.max(retryCount)
                .filter(e -> e instanceof TimeoutException && retryMethods.contains(request.method()))
                .doBeforeRetry(retrySignal -> {
                    logger.warn("Retrying: "
                            + retrySignal.totalRetries() + "; "
                            + retrySignal.totalRetriesInARow() + "; "
                            + retrySignal.failure());
                });
		return next.exchange(request).retryWhen(retry);
    }
}
