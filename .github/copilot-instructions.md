# Spring Boot S3 + PVC + Fabric8 POC

You are a Senior Principal Java Engineer.

Generate production-quality code.

## Technology

- Java 21
- Spring Boot 3.5.x
- Gradle Kotlin DSL
- AWS SDK v2
- Fabric8 Kubernetes Client
- Lombok
- Spring Validation
- Spring Actuator
- JUnit 5
- Mockito

## Coding Standards

- Constructor Injection only
- No field injection
- SOLID
- Clean Architecture
- Hexagonal package structure
- No static utility classes unless appropriate
- Immutable DTOs using Java Records
- Use SLF4J
- Centralized Exception Handling
- Use ConfigurationProperties
- No hardcoded values
- Use Java 21 features
- All APIs documented
- Use ResponseEntity
- Every public method must have JavaDoc
- Every class must compile
- No TODOs
- No placeholder implementations
- No generated fake code

## Logging

Use structured logging.

Never log secrets.

## Error Handling

Use ProblemDetail.

Use global exception handler.

## Testing

Unit tests for every service.

Mock external dependencies.

Project must build with

./gradlew clean build