# Spring webclient

Factory aidant à la construction d'un client web [WebClient](http://www.baeldung.com/spring-5-webclient).

[![Build Status](https://travis-ci.org/nduheron/spring-webclient.svg?branch=master)](https://travis-ci.org/nduheron/spring-webclient)

[![codecov.io](https://codecov.io/gh/nduheron/spring-webclient/branch/master/graphs/badge.svg?branch=master)](https://codecov.io/github/nduheron/spring-webclient?branch=master)

## Fonctionnalités

- Stratégie de retry [WebClientRetryHandler](./src/main/java/fr/nduheron/poc/springwebclient/filters/WebClientRetryHandler.java) (par défaut 2 retry en cas de timeout)
- Gestion centralisée des exceptions [WebClientExceptionHandler](./src/main/java/fr/nduheron/poc/springwebclient/filters/WebClientExceptionHandler.java)
- Gestion des logs [WebClientLoggingFilter](./src/main/java/fr/nduheron/poc/springwebclient/filters/WebClientLoggingFilter.java)
- Gestion des timeout : par défaut le timeout de connection est de 500ms et le timeout de lecture de 5 secondes
- Gestion d'un pool de connection

## Usage

Construction d'un client pour l'API "user" :

```java
		@Bean
		WebClientFactory webClientFactory() {
			WebClientFactory factory = new WebClientFactory();
			factory.setName("user");
			return factory;
		}
```
Configuration minimale :

```properties
client.user.baseUrl=http://localhost:8080
```
Customiser les timeout (10 secondes en lecture et 1 seconde pour la connection) :

```properties
client.user.readTimeoutMillis=10000
client.user.connectionTimeoutMillis=1000
```
Customiser la stratégie de retry :

```properties
# pas de retry
client.user.retryCount=0
# 5 retry
client.user.retryCount=5
```
Désactiver les logs :

```properties
client.user.log.disable=true
```
Customiser la taille du pool de connection :

```properties
client.user.maxTotalConnections=10
```
