# Ready-for-scaling-out Spring Boot application example
![Maven Central](https://img.shields.io/maven-central/v/org.springframework.boot/spring-boot?versionSuffix=3.2.0&label=spring%20boot&color=green)
[![Kotlin](https://img.shields.io/badge/kotlin-1.9.20-blue.svg?logo=kotlin)](http://kotlinlang.org)

## Challenge
Horizontal scaling is a method of increasing the capacity of a system by adding more application instances. This process may be challenging as it requires your service to be either **stateless** or to have some form of state synchronization mechanism.

The goal of this repository is to show an example of ready-for-scaling-out **stateful** Spring Boot application, synchronizing updates between instances using PostgreSQL data store and embedded messaging system.

## Related articles
1. [Spring Boot Scaling Strategies: Cache](https://medium.com/gitconnected/spring-boot-scaling-strategies-cache-159c53cf4060)
2. [Spring Boot Scaling Strategies: WebSockets](https://medium.com/gitconnected/spring-boot-scaling-strategies-websockets-f7acf7ef45e5)

## Plot
Hopsify (like Shopify but for beer 🍻) is a young ambitious startup aims to conquer the world with their innovative beer-commerce solutions.

Recently company introduced a new service for tap rooms and pubs to deliver announcements for their customers.

The service consist of 3 main parts:
* REST API for read, create and delete venue announcements
* WebSocket API for broadcasting a "Cheers" message to the venue attenders
* Scheduled task for a daliy email newsletter

## Testing

To test multi-instance environment the application packed into a docker container, and three containers launched before testing starts:
* PostgreSQL container
* 2 application containers

### Tests

#### Cache synchronisation
Firstly this test ensures that caching database data is enabled for the application instances, secondly it verifies that cache eviction on one instance leads to eviction on the others.

#### WebSockets

This test makes two WebSocket connections: one connection for the first instance, another for the second instance. Using second connection a client broadcasts a message, and the test verifies that the first connection receives this message.

#### Scheduled task

Highly frequent scheduled tasks from both instances trying to scan database for not processed email addresses and to send them emails respectively. The test ensures that no email address processed more than once and each address received exactly one email.

### Run the tests

To run all tests use the command below:
```
./gradlew test
```
