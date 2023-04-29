package fr.nduheron.poc.springwebclient.filters;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Filtre permettant de propager des champs du header HTTP dans le header
 */
public class WebClientHttpHeadersFilter implements ExchangeFilterFunction {

    private final List<String> fields;

    public WebClientHttpHeadersFilter(List<String> fields) {
        this.fields = fields;
    }

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        ClientRequest.Builder requestWithHttpHeaders = ClientRequest.from(request);
        fields.forEach(it -> {
            String headerValue = getHeader(it);
            if (headerValue != null) {
                requestWithHttpHeaders.headers(header -> header.set(it, headerValue));
            }
        });
        return next.exchange(requestWithHttpHeaders.build());
    }


    private String getHeader(String name) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            return ((ServletRequestAttributes) requestAttributes).getRequest().getHeader(name);
        }
        return null;
    }
}
