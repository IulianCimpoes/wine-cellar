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

### Data Integrity & Concurrency

-   Database-level unique constraints
-   Service-level duplicate checks
-   **Optimistic locking** using `@Version`
-   Stale updates return `409 Conflict`

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
