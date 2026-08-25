# PayTrack Architecture

## Architectural Style

PayTrack backend starts as a **modular monolith**.

The application is deployed as one Spring Boot service, while business capabilities are isolated as feature modules. This keeps operational complexity low during MVP development without coupling unrelated domains together.

## Module Structure

Each business module follows the same internal shape:

```text
<module>/
├── api/              # REST controllers and transport DTOs
├── application/      # use cases, orchestration, commands/queries
├── domain/           # entities, value objects, domain rules
└── infrastructure/   # JPA repositories, external adapters, persistence
```

Cross-cutting technical code belongs under:

```text
shared/
├── api/
├── config/
├── exception/
└── util/
```

`shared` must not become a dumping ground for business logic.

## Planned Business Modules

- user
- auth
- payment
- creditcard
- loan
- subscription
- bill
- dashboard
- notification

Future modules can be added without restructuring existing features:

- bankintegration
- emailimport
- sm/import
- analytics
- household
- budget
- ai

## Dependency Rules

1. `api` may call `application`.
2. `application` may use `domain` and ports/interfaces it owns.
3. `infrastructure` implements application/domain ports.
4. `domain` must not depend on controllers, JPA repositories, HTTP clients, or another module's infrastructure.
5. Modules should communicate through explicit application services/events rather than reaching into each other's persistence layer.
6. A module must not import another module's repository implementation.

## Database Strategy

The MVP uses one PostgreSQL database, but tables are owned conceptually by modules.

A module may reference another module by identifier where practical instead of building large bidirectional JPA graphs. This helps preserve module boundaries and makes later extraction into services possible if ever needed.

## API Strategy

Public REST endpoints are versioned under `/api/v1`.

Controllers remain thin. Validation happens at the transport boundary and business rules stay in domain/application layers.

## Events

Where one business action affects multiple modules, prefer an internal domain/application event. Example:

```text
PaymentMarkedPaid
  -> dashboard projections refresh
  -> notification reminder is cancelled
  -> analytics receives payment event
```

Initially events can use Spring's in-process event mechanism. The contracts should not assume Kafka/RabbitMQ so infrastructure can be swapped later.

## Mobile Architecture

The Flutter client uses a feature-first structure that mirrors backend capabilities:

```text
features/<feature>/
├── data/
├── domain/
└── presentation/
```

Reusable UI/network/navigation infrastructure goes under `core/`.

## Guiding Principle

**Easy to add, easy to remove, hard to accidentally couple.**

New functionality should usually enter as a new module or an extension of one clear module instead of adding conditions throughout the codebase.
