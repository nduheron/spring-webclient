package fr.nduheron.poc.springwebclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.nduheron.poc.springwebclient.filters.*;
import fr.nduheron.poc.springwebclient.properties.WebClientProperties;
import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.web.reactive.client.MetricsWebClientFilterFunction;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.util.Optional;

public class WebClientFactory implements FactoryBean<WebClient>, InitializingBean {

    /**
     * Le serializer/deserializer JSON
     */
    @Autowired(required = false)
    private ObjectMapper mapper;

    @Autowired(required = false)
    private MetricsWebClientFilterFunction metricsWebClientFilterFunction;

    @Autowired(required = false)
    private WebClientHttpHeadersFilter webClientHttpHeadersFilter;

    /**
     * Les filtres "custom" à ajouter
     */
    private ExchangeFilterFunction[] customFilters;

    /**
     * Les paramètres du client
     */
    private WebClientProperties properties;

    @Override
    public void afterPropertiesSet() {
        Assert.notNull(properties, "properties must not be null");
        Assert.notNull(mapper, "mapper must not be null");
    }

    @Override
    public WebClient getObject() {

        ExchangeStrategies strategies = ExchangeStrategies.builder().codecs(it -> {
            it.registerDefaults(true);
            it.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(mapper, MediaType.APPLICATION_JSON));
            it.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(mapper, MediaType.APPLICATION_JSON));
            it.defaultCodecs().maxInMemorySize(properties.getBufferSize());
        }).build();


        ConnectionProvider connectionProvider = Optional.ofNullable(properties.getPool().getMaxSize())
                .map(maxSize -> ConnectionProvider.create(properties.getPool().getName() + "Pool", maxSize))
                .orElseGet(() -> ConnectionProvider.create(properties.getPool().getName() + "Pool"));

        HttpClient httpClient = HttpClient.create(connectionProvider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) properties.getTimeout().getConnection().toMillis())
                .keepAlive(properties.isKeepAlive())
                .compress(properties.isCompress())
                .responseTimeout(properties.getTimeout().getRead());

        ReactorClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);

        Builder exchangeStrategies = WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader(HttpHeaders.ACCEPT_LANGUAGE, LocaleContextHolder.getLocale().getLanguage())
                .exchangeStrategies(strategies);

        if (properties.getHeaders().getMdc() != null) {
            exchangeStrategies.filter(new WebClientMdcToHeadersFilter(properties.getHeaders().getMdc()));
        }

        if (webClientHttpHeadersFilter != null) {
            exchangeStrategies.filter(webClientHttpHeadersFilter);
        }

        if (properties.getLog().isEnable()) {
            exchangeStrategies.filter(new WebClientLoggingFilter(properties.getLog().getObfuscateHeaders()));
        }


        if (properties.getRetry() != null && properties.getRetry().getCount() > 0) {
            exchangeStrategies.filter(new WebClientRetryHandler(properties.getRetry()));
        }

        if (metricsWebClientFilterFunction != null) {
            exchangeStrategies.filter(metricsWebClientFilterFunction);
        }

        if (customFilters != null) {
            for (ExchangeFilterFunction filter : customFilters) {
                exchangeStrategies.filter(filter);
            }
        }

        exchangeStrategies.filter(new WebClientMdcContextFilter());

        return exchangeStrategies.clientConnector(connector).build();
    }

    @Override
    public Class<?> getObjectType() {
        return WebClient.class;
    }

    public void setMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public void setCustomFilters(ExchangeFilterFunction[] customFilters) {
        this.customFilters = customFilters;
    }

    public void setProperties(WebClientProperties properties) {
        this.properties = properties;
    }
}
