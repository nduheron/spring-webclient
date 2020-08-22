package fr.nduheron.poc.springwebclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.nduheron.poc.springwebclient.filters.WebClientExceptionHandler;
import fr.nduheron.poc.springwebclient.filters.WebClientLoggingFilter;
import fr.nduheron.poc.springwebclient.filters.WebClientRetryHandler;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.JettyClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;

public class WebClientFactory implements FactoryBean<WebClient>, InitializingBean {

    @Autowired
    private Environment env;

    /**
     * Le nom du client web
     */
    private String name;

    /**
     * Le serializer/deserializer JSON
     */
    @Autowired(required = false)
    private ObjectMapper mapper;

    /**
     * Les filtres "custom" à ajouter
     */
    private ExchangeFilterFunction[] customFilters;

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(name, "name must not be null");
        Assert.notNull(mapper, "mapper must not be null");
    }

    @Override
    public WebClient getObject() throws Exception {
        ExchangeStrategies strategies = ExchangeStrategies.builder().codecs(configurer -> {
            configurer.registerDefaults(false);
            configurer.customCodecs().encoder(new Jackson2JsonEncoder(mapper, MediaType.APPLICATION_JSON));
            configurer.customCodecs().decoder(new Jackson2JsonDecoder(mapper, MediaType.APPLICATION_JSON));
        }).build();

        SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();
        HttpClient httpClient = new HttpClient(sslContextFactory);

        if (env.containsProperty("client." + name + ".maxTotalConnections")) {
            QueuedThreadPool queuedThreadpool = new QueuedThreadPool(env.getProperty("client." + name + ".maxTotalConnections", Integer.class));
            queuedThreadpool.setName(name + "Pool");
            httpClient.setExecutor(queuedThreadpool);
        }
        // Par défaut, on plante si l'on a pas réussi à établir la connection au bout de 500ms
        httpClient.setConnectTimeout(env.getProperty("client." + name + ".connectionTimeoutMillis", Integer.class, 500));
        // Par défaut, on plante si l'on a pas réussi à obtenir une réponse au bout de 5 secondes
        httpClient.setIdleTimeout(env.getProperty("client." + name + ".readTimeoutMillis", Integer.class, 5000));
        JettyClientHttpConnector connector = new JettyClientHttpConnector(httpClient);

        Builder exchangeStrategies = WebClient.builder()
                .baseUrl(env.getRequiredProperty("client." + name + ".baseUrl", String.class))
                .defaultHeader(HttpHeaders.ACCEPT_LANGUAGE, LocaleContextHolder.getLocale().getLanguage())
                .exchangeStrategies(strategies);

        if (!env.getProperty("client." + name + ".log.disable", Boolean.class, false)) {
            exchangeStrategies.filter(new WebClientLoggingFilter());
        }
        exchangeStrategies.filter(new WebClientExceptionHandler());

        // par défaut, on fait 2 retry en cas de timeout
        int retryCount = env.getProperty("client." + name + ".retryCount", Integer.class, 2);
        if (retryCount > 0) {
            exchangeStrategies.filter(new WebClientRetryHandler(retryCount));
        }

        if (customFilters != null) {
            for (ExchangeFilterFunction filter : customFilters) {
                exchangeStrategies.filter(filter);
            }
        }

        return exchangeStrategies.clientConnector(connector).build();
    }

    @Override
    public Class<?> getObjectType() {
        return WebClient.class;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public void setCustomFilters(ExchangeFilterFunction[] customFilters) {
        this.customFilters = customFilters;
    }

}
