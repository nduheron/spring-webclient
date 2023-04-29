package fr.nduheron.poc.springwebclient.filters;

import org.slf4j.MDC;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

/**
 * Refer to <a href="http://ttddyy.github.io/mdc-with-webclient-in-webmvc/">MDC with WebClient</a>
 */
public class WebClientMdcContextFilter implements ExchangeFilterFunction {

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        Map<String, String> map = MDC.getCopyOfContextMap();
        return next.exchange(request).doOnNext(it -> {
            if (map != null) {
                MDC.setContextMap(map);
            }
        });
    }

    static {
        Schedulers.onScheduleHook("mdc", runnable -> {
            Map<String, String> map = MDC.getCopyOfContextMap();
            return () -> {
                if (map != null) {
                    MDC.setContextMap(map);
                }
                try {
                    runnable.run();
                } finally {
                    MDC.clear();
                }
            };
        });
    }

}
