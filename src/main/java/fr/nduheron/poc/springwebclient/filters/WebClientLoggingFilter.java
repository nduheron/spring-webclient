package fr.nduheron.poc.springwebclient.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;

import reactor.core.publisher.Mono;

public class WebClientLoggingFilter implements ExchangeFilterFunction {
	private static final Logger logger = LoggerFactory.getLogger(WebClientLoggingFilter.class);

	@Override
	public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
		logger.info("Call API: {} {} ...", request.method(), request.url());
		if (logger.isDebugEnabled()) {
			request.headers().forEach(
					(name, values) -> values.forEach(value -> logger.debug("Request header: {}={}", name, value)));
		}

		return next.exchange(request).flatMap(clientResponse -> {
			logger.info("Response: {} {}: {}", request.method(), request.url(),
					clientResponse.statusCode().getReasonPhrase());
			if (logger.isDebugEnabled()) {
				clientResponse.headers().asHttpHeaders().forEach(
						(name, values) -> values.forEach(value -> logger.debug("Response header: {}={}", name, value)));
			}
			return Mono.just(clientResponse);
		});
	}
}
