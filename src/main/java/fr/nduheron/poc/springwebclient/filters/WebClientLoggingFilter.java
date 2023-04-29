package fr.nduheron.poc.springwebclient.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WebClientLoggingFilter implements ExchangeFilterFunction {
    private static final Logger logger = LoggerFactory.getLogger(WebClientLoggingFilter.class);

    private static final String OBFUSCATE_HEADER = "xxxxx";
    private final List<String> obfuscateHeader;

    public WebClientLoggingFilter(List<String> obfuscateHeader) {
        this.obfuscateHeader = obfuscateHeader;
    }

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        return next.exchange(request)
                .elapsed()
                .flatMap(tuple ->
                        buildHttpModel(request, tuple.getT1(), tuple.getT2())
                                .doOnNext(httpModel -> {
                                    if (httpModel.status.is5xxServerError() && logger.isErrorEnabled()) {
                                        logger.error(httpModel.toString());
                                    } else if (httpModel.status.is4xxClientError() && logger.isWarnEnabled()) {
                                        logger.warn(httpModel.toString());
                                    } else if (logger.isInfoEnabled()) {
                                        logger.info(httpModel.toString());
                                    }
                                })
                                .map(it -> it.response)
                );

    }

    private Mono<HttpModel> buildHttpModel(ClientRequest request, long durationInMs, ClientResponse response) {
        HttpModel httpModel = new HttpModel();

        httpModel.method = request.method().name();
        httpModel.url = request.url().toString();
        httpModel.status = response.statusCode();
        httpModel.durationInMs = durationInMs;
        httpModel.response = response;
        if (logger.isDebugEnabled()) {
            httpModel.requestHeaders = getHeaders(request.headers());
            httpModel.responseHeaders = getHeaders(response.headers().asHttpHeaders());
        }

        if (logger.isDebugEnabled() || response.statusCode().isError()) {
            return response.bodyToMono(DataBuffer.class)
                    .map(body -> {
                        ClientResponse cloned = response.mutate()
                                .body(Flux.just(body))
                                .build();

                        httpModel.responseContent = body.toString(Charset.defaultCharset());
                        httpModel.response = cloned;
                        return httpModel;
                    })
                    .defaultIfEmpty(httpModel);
        }
        return Mono.just(httpModel);
    }

    private Map<String, String> getHeaders(HttpHeaders headers) {
        return headers.entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                    if (obfuscateHeader.contains(entry.getKey())) {
                        return OBFUSCATE_HEADER;
                    } else {
                        return entry.getValue().stream().collect(Collectors.joining(",", "\"", "\""));
                    }
                }));
    }

    private static class HttpModel {

        public String method;
        public String url;
        public HttpStatus status;
        public long durationInMs;
        public Map<String, String> requestHeaders;
        public Map<String, String> responseHeaders;
        public String responseContent;
        public ClientResponse response;


        @Override
        public String toString() {

            StringBuilder sb = new StringBuilder();
            sb.append("method='").append(method).append('\'');
            sb.append(", url='").append(url).append('\'');
            sb.append(", statusCode=").append(status.value());
            sb.append(", durationInMs=").append(durationInMs);
            if (requestHeaders != null) {
                sb.append(", requestHeaders=").append(requestHeaders);
            }
            if (responseHeaders != null) {
                sb.append(", responseHeaders=").append(responseHeaders);
            }
            if (responseContent != null) {
                sb.append(", responseContent='").append(responseContent).append('\'');
            }
            return sb.toString();
        }
    }
}
