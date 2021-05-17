package fr.nduheron.poc.springwebclient.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.util.List;

public class WebClientLoggingFilter implements ExchangeFilterFunction {
    private static final Logger logger = LoggerFactory.getLogger(WebClientLoggingFilter.class);
    private static final String OBFUSCATE_HEADER = "xxxxx";
    private final List<String> obfuscateHeader;

    public WebClientLoggingFilter(List<String> obfuscateHeader) {
        this.obfuscateHeader = obfuscateHeader;
    }

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        logger.info("Call API: {} {} ...", request.method(), request.url());
        if (logger.isDebugEnabled()) {
            request.headers().forEach(
                    (name, values) -> values.forEach(value -> logger.debug("Request header: {}={}", name, obfuscateHeader.contains(name) ? OBFUSCATE_HEADER : value)));
        }

        return next.exchange(request).flatMap(clientResponse -> {
            logger.info("Response: {} {}: {}", request.method(), request.url(), clientResponse.statusCode().getReasonPhrase());
            if (logger.isDebugEnabled()) {
                clientResponse.headers().asHttpHeaders().forEach(
                        (name, values) -> values.forEach(value -> logger.debug("Response header: {}={}", name, obfuscateHeader.contains(name) ? OBFUSCATE_HEADER : value)));
            }
            return Mono.just(clientResponse);
        });
    }
}
