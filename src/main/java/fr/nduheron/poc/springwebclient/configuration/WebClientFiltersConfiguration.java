package fr.nduheron.poc.springwebclient.configuration;

import fr.nduheron.poc.springwebclient.filters.WebClientHttpHeadersFilter;
import fr.nduheron.poc.springwebclient.properties.WebClientProperties;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties;
import org.springframework.boot.actuate.metrics.web.reactive.client.MetricsWebClientFilterFunction;
import org.springframework.boot.actuate.metrics.web.reactive.client.WebClientExchangeTagsProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;

@Configuration
public class WebClientFiltersConfiguration {

    @Bean
    @ConditionalOnBean({MeterRegistry.class, WebClientExchangeTagsProvider.class, MetricsProperties.class})
    public MetricsWebClientFilterFunction metricsWebClientFilterFunction(
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


    @Bean
    @ConditionalOnClass(HttpServletRequest.class)
    public WebClientHttpHeadersFilter webClientHttpHeadersFilter(WebClientProperties properties) {
        if (properties.getHeaders().getHttp().isEmpty()) {
            return null;
        }
        return new WebClientHttpHeadersFilter(properties.getHeaders().getHttp());
    }

}
