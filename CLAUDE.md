# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Backend application for BLSchool intranet, built with Spring Boot 4.0.2 and Java 21. Uses PostgreSQL as the database and Spring Data JPA for persistence.

## Build & Run Commands

```bash
# Build the project
./mvnw clean package

# Build skipping tests
./mvnw clean package -DskipTests

# Run the application
./mvnw spring-boot:run

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=BlschoolIntranetBackendApplicationTests

# Run a single test method
./mvnw test -Dtest=ClassName#methodName
```

## Architecture

- **Base package**: `ar.com.blschool.blschool_intranet_backend`
- **Framework**: Spring Boot 4.0.2 with Spring Web MVC (REST APIs)
- **Database**: PostgreSQL with Spring Data JPA / Hibernate
- **Code generation**: Lombok (use `@Data`, `@Builder`, `@AllArgsConstructor`, etc. to reduce boilerplate)
- **Entry point**: `BlschoolIntranetBackendApplication.java`

Follow the standard Spring Boot layered architecture: Controller → Service → Repository → Entity. Configuration goes in `application.properties`.
