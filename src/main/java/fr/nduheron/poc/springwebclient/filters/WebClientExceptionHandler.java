package fr.nduheron.poc.springwebclient.filters;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;

import fr.nduheron.poc.springwebclient.exception.BadRequestException;
import fr.nduheron.poc.springwebclient.exception.NotFoundException;
import fr.nduheron.poc.springwebclient.exception.TechnicalException;
import fr.nduheron.poc.springwebclient.exception.model.ErrorParameter;
import reactor.core.publisher.Mono;

public class WebClientExceptionHandler implements ExchangeFilterFunction {
	private static final Logger logger = LoggerFactory.getLogger(WebClientExceptionHandler.class);

	@Override
	public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {

		return next.exchange(request).flatMap(clientResponse -> {
			Mono<ClientResponse> result = Mono.just(clientResponse);
			if (clientResponse.statusCode().isError()) {
				switch (clientResponse.statusCode()) {
				case NOT_FOUND:
					logger.warn("Response: {} {}: {}", request.method(), request.url(),
							clientResponse.statusCode().getReasonPhrase());
					result = Mono.error(new NotFoundException(request.url().toString()));
					break;
				case BAD_REQUEST:
					logger.warn("Response: {} {}: {}", request.method(), request.url(),
							clientResponse.statusCode().getReasonPhrase());
					result = clientResponse.bodyToMono(new ParameterizedTypeReference<List<ErrorParameter>>() {
					}).flatMap(errorDetails -> Mono.error(new BadRequestException(errorDetails)));
					break;
				case CONFLICT:
					// on ne fait rien, charge à chaque client de traiter une exception
					// fonctionnelle venant d'un client
					break;
				default:
					logger.error("Response: {} {}: {}", request.method(), request.url(),
							clientResponse.statusCode().getReasonPhrase());
					result = Mono.error(
							new TechnicalException(String.format("Erreur lors de l'appel de l'url %s. Status code %s.",
									request.url(), clientResponse.statusCode().value())));
					break;
				}
			}

			return result;
		});
	}
}
