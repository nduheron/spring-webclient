package fr.nduheron.poc.springwebclient.filters;

import org.slf4j.MDC;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Filtre permettant de propager des champs du context MDC dans le header
 */
public class WebClientMdcToHeadersFilter implements ExchangeFilterFunction {

    private final List<String> fields;

    public WebClientMdcToHeadersFilter(List<String> fields) {
        this.fields = fields;
    }

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        ClientRequest.Builder requestWithMdc = ClientRequest.from(request);
        fields.forEach(it -> {
            String value = MDC.get(it);
            if (value != null) {
                requestWithMdc.headers(header -> header.set(it, value));
            }
        });
        return next.exchange(requestWithMdc.build());
    }

}
