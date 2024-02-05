# Movie Library

Movie library service providing api for working with movies, customers and other entities.

## Description

Features:
1. CRUD for entities
2. Different relations between entities
3. Logs are collected by ELK and can be easily traced with requestId in Kibana (using MDC) 
4. Jvm metrics can be viewed in Grafana (accessable via Spring Actuator)
5. 80%+ service methods are covered with unit tests
6. Implemented integration tests using testcontainers (not the best decision for multistage dockerfile, had to add additional container to docker compose for providing access to host's docker daemon in building context)
7. Exceptions are handled with centralized controllerAdvice for user readable responses
8. Added swagger api documentation
9. The service is built with Coroutines and r2dbc driver for PostgreSQL
10. Api documentation also available as postman collection

Used technologies:
* Kotlin 1.8
* Spring boot 3.1
* Spring Webflux
* Coroutines
* PostgreSQL (r2dbc driver)
* Flyway
* Docker
* Postman
* Testcontainers
* Mockk
* JUnit
* ELK (Elastic, Logstash, Kibana)
* Grafana + Prometheus
* Swagger

## Getting Started

### Prerequisites

* Docker

### Installing

* No installation needed, just run it in containers

### Executing program

* Clone the repo
```
git clone https://github.com/Nikik0/libproj.git
```
* Go to the directory with docker compose files
```
cd libproj/src/main/docker
```
* Get the bridge container up (needed only for the first service image build with integration tests)
```
docker-compose -f docker-compose-all.yml up testcontainers_bridge_to_docker_daemon_on_host -d
```
* After the bridge container is up, run the rest of the containers (might require alot of ram (up to 6 gb), proceed without elk if running on older machine)
```
docker-compose -f docker-compose-all.yml up -d
```
### Accessing api

* Swagger is available here
```
http://localhost:8083/v3/webjars/swagger-ui/index.html#/
```
* Logs can be checked here
```
http://localhost:5601/
```
* Metrics are available at
```
http://localhost:3000/
```
Api is available at localhost on port 8083 
```
http://localhost:8083/api/v1/customer/save
```
Import postman collection to postman and test it
