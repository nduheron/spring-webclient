package fr.nduheron.poc.springwebclient.filters;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import org.springframework.boot.actuate.metrics.web.reactive.client.DefaultWebClientExchangeTagsProvider;
import org.springframework.boot.actuate.metrics.web.reactive.client.WebClientExchangeTagsProvider;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

public class WebClientMonitoringFilter implements ExchangeFilterFunction {
    private static final String METRICS_WEBCLIENT_START_TIME = WebClientMonitoringFilter.class.getName() + ".START_TIME";
    private final MeterRegistry meterRegistry;
    private final WebClientExchangeTagsProvider tagsProvider;

    public WebClientMonitoringFilter(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        tagsProvider = new DefaultWebClientExchangeTagsProvider();
    }

    @Override
    public Mono<ClientResponse> filter(ClientRequest clientRequest, ExchangeFunction exchangeFunction) {
        return exchangeFunction.exchange(clientRequest).doOnEach(signal -> {
            if (!signal.isOnComplete()) {
                Long startTime = signal.getContextView().get(METRICS_WEBCLIENT_START_TIME);
                ClientResponse clientResponse = signal.get();
                Throwable throwable = signal.getThrowable();
                Iterable<Tag> tags = tagsProvider.tags(clientRequest, clientResponse, throwable);
                Timer.builder("http.client.requests")
                        .tags(tags)
                        .description("Timer of WebClient operation")
                        .publishPercentiles(0.95, 0.99)
                        .register(meterRegistry)
                        .record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);

            }
        }).contextWrite((contextView) -> contextView.put(METRICS_WEBCLIENT_START_TIME, System.nanoTime()));
    }
}
