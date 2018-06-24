package fr.nduheron.poc.springwebclient.filters;

import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;

import io.netty.handler.timeout.TimeoutException;
import reactor.core.publisher.Mono;

public class WebClientRetryHandler implements ExchangeFilterFunction {

	private int retryCount;

	public WebClientRetryHandler(int retryCount) {
		this.retryCount = retryCount;
	}

	@Override
	public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
		return next.exchange(request).retry(retryCount, e -> e instanceof TimeoutException);
	}
}
