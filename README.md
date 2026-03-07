# Chat-O-Mania
Real-Time Messaging Platform built using Spring Boot, WebSockets, Redis, and PostgreSQL.

## Table of Contents
- Overview
- Features
- Architecture
- Tech Stack
- System Design Concepts
- Message Flow
- Project Structure
- Getting Started
- API Documentation
- Scalability Considerations
- Future Improvements
- Author

## Overview

Chat-O-Mania is a scalable real-time messaging backend designed to support one-to-one and group communication.
The system demonstrates backend engineering concepts such as:
- real-time communication using WebSockets.
- caching and presence tracking using Redis.
- secure authentication using JWT and OAuth2.
- media sharing through cloud storage.
- scalable messaging architecture.

The project focuses on backend system design and distributed systems concepts rather than UI.

## Features

- Real-time messaging using WebSockets.
- One-to-one chat support.
- Group chat functionality.
- Image and audio media sharing.
- Online/offline presence tracking.
- Typing indicators.
- Message edit and delete with time limits.
- JWT-based authentication.
- OAuth2 login support.
- Redis caching for performance.

## Architecture
```
Client (Web / Mobile)
        |
     WebSocket
        |
   Spring Boot API
        |
 ------------------------------
 |            |               |
PostgreSQL    Redis        Cloudinary
(Database)   (Cache &     (Media Storage)
             Presence)
```
Core responsibilities:
- Spring Boot handles REST APIs and WebSocket messaging
- Redis manages caching and presence tracking
- PostgreSQL stores users, chats, and messages
- Cloudinary handles image and audio storage

## Tech Stack
| Layer                   | Technology         |
| ----------------------- | ------------------ |
| Language                | Java               |
| Backend                 | Spring Boot        |
| Real-time Communication | WebSockets (STOMP) |
| Database                | PostgreSQL         |
| Caching                 | Redis              |
| Authentication          | JWT + OAuth2       |
| Media Storage           | Cloudinary         |
| Build Tool              | Maven / Gradle     |
| Containerization        | Docker             |

## System Design Concepts

This project demonstrates several backend engineering concepts:
- Real-time communication with WebSockets
- Stateless authentication with JWT
- Distributed caching with Redis
- Event-driven messaging architecture
- Secure API design using Spring Security
- External media storage integration

## Message Flow
```
User A sends message
        |
WebSocket Gateway
        |
Message Service
        |
Store message in PostgreSQL
        |
Publish message event
        |
Deliver message to User B via WebSocket
```
Steps:
1. Sender sends message through WebSocket connection
2. Backend validates and processes the message
3. Message is stored in the database
4. Event is published internally
5. Recipient receives the message in real time

## Project Structure
```
src/main/java/com/chatomania

config/
controller/
entity/
exception/
repository/
request/
response/
service/
utility/
```
Component responsibities:
- config -> Configuration files for the application
- controller -> Controllers that handle incoming HTTP requests and return responses
- entity -> Entity classes that represent the data model of the application
- exception -> Custom exceptions used in the application
- repository -> Interfaces for data access and manipulation
- request -> Classes representing incoming HTTP request payloads
- response -> Classes representing outgoing HTTP response payloads
- service -> Service classes that contain the business logic of the application
- utility -> Utility classes that provide common functionality used across the application

## Getting Started
### Prerequisites
- Java 21
- PostgreSQL
- Redis
- Maven or Gradle
- Docker

### Clone The Repository
```
git clone https://github.com/yourusername/chat-o-mania.git
cd chat-o-mania
```
### Configure Application Properties
Configure from below:
```yaml
spring:
  application:
    name: chatomania
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
    driver-class-name: org.postgresql.Driver
  data:
    redis:
      host: ${SPRING_REDIS_HOST}
      port: ${SPRING_REDIS_PORT}
  cache:
    type: redis
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope:
              - email
              - profile
  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 100MB           

management:
  endpoints:
    web:
      exposure:
        include: health
  endpoint:
    health:
      probes:
        enabled: true

cloudinary:
  cloud-name: ${CLOUDINARY_CLOUD_NAME}
  api-key: ${CLOUDINARY_CLOUD_KEY}
  api-secret: ${CLOUDINARY_CLOUD_KEY_SECRET}
```
For the environment variables refer `.env.sample` file or define as below:
```env
GOOGLE_CLIENT_ID=your-google-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your-google-client-secret
CLOUDINARY_CLOUD_NAME=your-cloudinary-cloud-name
CLOUDINARY_CLOUD_KEY=your-cloudinary-cloud-key
CLOUDINARY_CLOUD_KEY_SECRET=your-cloudinary-cloud-key-secret
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/chatomaniadb
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your-database-password
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379
```

### Run the Application

Using Maven:
```bash
mvn spring-boot:run
```
Using Gradle:
```bash
./gradlew bootRun
```

## API Documentation

API documentation is available through OpenAPI / Swagger.
After starting the server:
```bash
http://localhost:8080/swagger-ui.html
```

## Scalability Considerations

The architecture allows for future scaling through:

### Horizontal WebSocket Scaling
```
Multiple WebSocket Servers
        |
     Load Balancer
```

### Redis Pub/Sub

Used for synchronizing messages between multiple WebSocket servers.

### Event Streaming

Future integration with Kafka for large-scale message processing and asynchronous delivery.

Benefits:

- high throughput messaging
- fault tolerance
- service decoupling

## Future Improvements

Planned enhancements include:
- Kafka-based message streaming
- WebSocket scaling across multiple instances
- message delivery acknowledgment and retries
- Redis-based distributed presence service
- end-to-end encryption for messages

## Author

Sabarno Biswas

Software Engineer at [Airbus](https://www.airbus.com/en/about-us/our-worldwide-presence/airbus-in-asia-pacific/airbus-in-india)

[![Github](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)](https://github.com/Sabarno-15102002)
[![Linkedin](https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/sabarno-biswas-3163a61ba/)

## License
[![License](https://img.shields.io/github/license/Sabarno-15102002/Chat-O-Mania-Backend)](LICENSE)
