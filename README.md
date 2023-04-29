# Spring webclient

Factory aidant à la construction d'un client web [WebClient](http://www.baeldung.com/spring-5-webclient).

[![Build Status](https://travis-ci.org/nduheron/spring-webclient.svg?branch=master)](https://travis-ci.org/nduheron/spring-webclient)

[![codecov.io](https://codecov.io/gh/nduheron/spring-webclient/branch/master/graphs/badge.svg?branch=master)](https://codecov.io/github/nduheron/spring-webclient?branch=master)

## Fonctionnalités

- Stratégie de retry [WebClientRetryHandler](./src/main/java/fr/nduheron/poc/springwebclient/filters/WebClientRetryHandler.java) (par défaut 2 retry en cas de timeout)
- Gestion des logs [WebClientLoggingFilter](./src/main/java/fr/nduheron/poc/springwebclient/filters/WebClientLoggingFilter.java)
- Monitoring [WebClientMonitoringFilter](./src/main/java/fr/nduheron/poc/springwebclient/filters/WebClientMonitoringFilter.java)
- Gestion des timeout : par défaut le timeout de connection est de 500ms et le timeout de lecture de 5 secondes
- Gestion d'un pool de connection
-
## Usage

Construction d'un client pour l'API "user" :

```java
@Bean
WebClientFactory webClientFactory() {
        WebClientFactory factory = new WebClientFactory();
        factory.setProperties(userWebClientProperties());
        factory.setMeterRegistry(meterRegistry());
        return factory;
}

@Bean
MeterRegistry meterRegistry() {
        return new LoggingMeterRegistry();
}

@Bean
@ConfigurationProperties(prefix = "client.user")
WebClientProperties userWebClientProperties() {
        return new WebClientProperties();
}
```
Configuration minimale :

```properties
client.user.baseUrl=http://localhost:8080
```

Customiser les timeout (10 secondes en lecture et 1 seconde pour la connection) :

```properties
client.user.timeout.read=10000
client.user.timeout.connection=1000
```
Customiser la stratégie de retry :

```properties
# pas de retry
client.user.retry.count=0
# 5 retry
client.user.retry.count=5
```
Désactiver les logs :

```properties
client.user.log.enable=false
```

Customiser la taille du pool de connection :

```properties
client.user.pool.name=user
client.user.pool.size=10
```

## Références

* http://ttddyy.github.io/mdc-with-webclient-in-webmvc/
