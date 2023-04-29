package fr.nduheron.poc.springwebclient.configuration;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties;
import org.springframework.boot.actuate.metrics.web.reactive.client.MetricsWebClientFilterFunction;
import org.springframework.boot.actuate.metrics.web.reactive.client.WebClientExchangeTagsProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;

@Configuration
@ConditionalOnBean({MeterRegistry.class, WebClientExchangeTagsProvider.class, MetricsProperties.class})
public class WebClientMonitoringFilterConfiguration {

    @Bean
    public ExchangeFilterFunction metricsWebClientFilterFunction(
            MeterRegistry meterRegistry,
            WebClientExchangeTagsProvider tagsProvider,
            MetricsProperties metricsProperties
    ) {
        MetricsProperties.Web.Client.ClientRequest request = metricsProperties.getWeb().getClient().getRequest();
        return new MetricsWebClientFilterFunction(
                meterRegistry,
                tagsProvider,
                request.getMetricName(),
                request.getAutotime()
        );
    }

}
