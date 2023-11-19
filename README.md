# Ready-for-scaling-out Spring Boot application example
![Maven Central](https://img.shields.io/maven-central/v/org.springframework.boot/spring-boot?versionSuffix=3.1.5&label=spring%20boot&color=green)
[![Kotlin](https://img.shields.io/badge/kotlin-1.9.20-blue.svg?logo=kotlin)](http://kotlinlang.org)

## About
Horizontal scaling is a method of increasing the capacity of a system by adding more application instances. This process is not so trivial as it requires from your service to be either **stateless** or to have some sort of state synchronization mechanism.

This repository contains an example of ready-for-scaling-out **stateful** Spring Boot application, synchronizing updates between instances using PostgreSQL data store and embedded messaging system.

## Plot
Hopsify (like Shopify but for beer 🍻) is a young ambitious startup aims to conquer the world with their innovative beer-commerce solutions.

Recently company introduced a new service for tap rooms and pubs to deliver announcements for their customers.

The service consist of 3 main parts:
1. REST API for read, create and delete venue announcements
2. Scheduled task for email newsletter
3. WebSocket API for broadcasting a "Cheers" message to the venue attenders

## Tests
To run all tests use the command below:
```
./gradlew test
```

### Cache synchronisation

### WebSockets

### Scheduled task
