package fr.nduheron.poc.springwebclient.filters;

import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import reactor.core.publisher.Mono;

public class WebClientMonitoringFilter implements ExchangeFilterFunction {

	private MeterRegistry registry;

	public WebClientMonitoringFilter(MeterRegistry registry) {
		super();
		this.registry = registry;
	}

	@Override
	public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
		Timer.Sample sample = Timer.start(registry);

		return next.exchange(request).doAfterSuccessOrError((r, t) -> {
			Tags tags = null;
			if (r != null) {
				tags = Tags.of("uri",
						(String) request
								.attribute("org.springframework.web.reactive.function.client.WebClient.uriTemplate")
								.orElse(request.url().getPath()),
						"method", request.method().name(), "status", String.valueOf(r.statusCode().value()));
			} else {
				tags = Tags.of("uri",
						(String) request
								.attribute("org.springframework.web.reactive.function.client.WebClient.uriTemplate")
								.orElse(request.url().getPath()),
						"method", request.method().name(), "status", t.toString());

			}
			sample.stop(
					Timer.builder("webclient").description("Monitoring des appels http").tags(tags).register(registry));

		});
	}
}
