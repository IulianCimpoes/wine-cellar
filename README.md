# WineCellar

WineCellar is a **Spring Boot REST API** designed to demonstrate backend
engineering maturity across API design, data integrity, security,
testing, delivery, and operational readiness.

The project intentionally evolves from a simple CRUD service into a
**production-ready, versioned API** with explicit backward compatibility
guarantees.

------------------------------------------------------------------------

## Purpose

This application was built as a technical alignment project to
demonstrate competencies expected from a **Java Backend Developer**,
including:

-   Clean REST API design
-   Transactional correctness and concurrency handling
-   Backward-compatible API evolution
-   Security and authorization
-   Automated testing and CI-friendly builds
-   Containerization and operational readiness

------------------------------------------------------------------------

## Tech Stack

-   **Java 21 (LTS)**
-   Spring Boot
-   Spring Web (REST)
-   Spring Data JPA (Hibernate)
-   Spring Security
-   Spring Boot Actuator
-   OpenAPI / Swagger (springdoc)
-   H2 (in-memory, for local & tests)
-   Maven
-   Docker

------------------------------------------------------------------------

## Core Features

### Domain Model

-   **Winery** and **Wine** resources
-   Full CRUD operations
-   Pagination and sorting
-   Nested read endpoints

### Validation & Error Handling

-   Bean Validation (`@Valid`)
-   Consistent error response model
-   Correct HTTP semantics:
    -   `400 Bad Request` -- validation errors
    -   `404 Not Found` -- resource not found
    -   `409 Conflict` -- duplicates or stale updates

### Request Correlation ID

Each incoming HTTP request is assigned a correlation identifier (`X-Request-Id`).

- If the client provides `X-Request-Id`, it is propagated
- Otherwise, the server generates one
- The value is:
    - Returned in the response header
    - Included in application logs
    - Included in error responses

This enables end-to-end request tracing and simplifies debugging in distributed systems.

### Data Integrity, Concurrency & Schema Management

- Database-level unique constraints
- Service-level duplicate checks
- **Optimistic locking** using `@Version`
- Stale updates return `409 Conflict`

Database schema evolution is managed with **Flyway**:
- Initial schema is defined via versioned migrations
- Hibernate is configured with `ddl-auto=validate` and does not modify the schema
- Migrations are applied automatically on application startup

This ensures deterministic schema evolution and early failure in case of schema mismatch.

### Dev seed data

For local development and API exploration (e.g. via Swagger), the application provides **demo seed data** (a small set of wineries and wines).

- Seed data is applied via a **Flyway repeatable migration**
- Seed data runs **only when the application is started with the `dev` profile**
- Seed data is **disabled by default** and **never runs in production**

This allows the API to be usable immediately in development without affecting test isolation or production safety.

------------------------------------------------------------------------

## Asynchronous Eventing (Kafka + Transactional Outbox)

WineCellar publishes domain events (e.g., `WineryCreated`) to Kafka using a **Transactional Outbox** design:

- Request path writes the domain change and an `outbox_event` row in the same DB transaction
- A scheduled dispatcher (`OutboxKafkaDispatcher`) publishes eligible outbox rows to Kafka
- Failures are retried with exponential backoff (maxRetries=10, maxBackoffSeconds=60)

For detailed architecture, failure modes, retry policy, and diagrams, see:

- [Eventing Architecture (Kafka + Transactional Outbox)](docs/eventing-kafka-outbox.md)

------------------------------------------------------------------------


## API Versioning Strategy

### Strategy Used

**URL-based versioning**

    /api/v1/...
    /api/v2/...


This approach was chosen for:
- High clarity
- Excellent Swagger visibility
- Simple client adoption
- Explicit migration paths

---

### API v1 (Deprecated)

- The original unversioned API (`/api/wineries`) is treated as **v1**
- `/api/v1/wineries` is an explicit alias
- Some v1 endpoints are **marked as deprecated** in Swagger

Example:
GET /api/v1/wineries

### API v2 (Active)

- Introduces a **new response contract**
- Pagination metadata is returned explicitly
- Existing v1 clients are not affected

Example:
GET /api/v2/wineries

------------------------------------------------------------------------

## Optimistic Locking

-   Entities use @Version
-   Update requests must include a version field
-   If the provided version is stale: request fails with 409 Conflict

------------------------------------------------------------------------
## Security

-   HTTP Basic authentication
-   Roles:
    -   USER -- read-only
    -   ADMIN -- write access
-   Authorization enforced at **service layer** using @PreAuthorize
-   Correct semantics: 401 / 403

------------------------------------------------------------------------

## Testing Strategy

### Test Types

- Unit tests (Mockito)
- Integration tests (@SpringBootTest, MockMvc)
- Security integration tests
- API versioning regression tests

### Maven Lifecycle

- mvn test – unit tests
- mvn verify – unit + integration tests

### Test Reports

Generated under:
``` bash
target/site/surefire-report.html
target/site/failsafe-report.html
```
------------------------------------------------------------------------
## Observability & Health

-   Spring Boot Actuator enabled

## Exposed Endpoints
``` bash
/actuator/health
/actuator/health/liveness
/actuator/health/readiness
```

------------------------------------------------------------------------
## Docker & CI

- Multi-stage Docker build
- Java 21 runtime
- Non-root container user
- Healthcheck defined at container level

``` bash
docker build -t winecellar:local .
docker run -p 8080:8080 winecellar:local
```

------------------------------------------------------------------------

## Swagger

    http://localhost:8080/swagger-ui/index.html
