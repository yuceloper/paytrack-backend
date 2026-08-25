# PayTrack Backend Roadmap

## Phase 0 — Foundation

- [x] Spring Boot project initialization
- [x] Java 21
- [x] PostgreSQL dependency
- [x] Base Payment model
- [ ] Environment configuration
- [ ] Docker Compose for PostgreSQL
- [ ] Global exception handling
- [ ] Standard API response format
- [ ] OpenAPI / Swagger

## Phase 1 — Core Domain

- [ ] User entity
- [ ] CreditCard entity
- [ ] Loan entity
- [ ] Subscription entity
- [ ] Bill entity
- [ ] Other payment source support
- [ ] PaymentSchedule model
- [ ] PaymentOccurrence model
- [ ] Payment status lifecycle
- [ ] Currency support

## Phase 2 — Payment Engine

- [ ] Monthly recurring payments
- [ ] Weekly recurring payments
- [ ] Yearly recurring payments
- [ ] Installment schedules
- [ ] Automatic next occurrence generation
- [ ] Mark payment as paid
- [ ] Skip / postpone occurrence
- [ ] Overdue payment detection

## Phase 3 — Dashboard

- [ ] Upcoming payments endpoint
- [ ] Today endpoint
- [ ] Next 7 days total
- [ ] Monthly total
- [ ] Credit card debt summary
- [ ] Subscription monthly/yearly cost
- [ ] Salary-date-aware required balance calculation

## Phase 4 — Notifications

- [ ] Notification preferences
- [ ] Reminder offsets (7 days / 3 days / 1 day / due date)
- [ ] Device token registration
- [ ] Push notification integration
- [ ] Notification scheduler
- [ ] Retry and delivery logging

## Phase 5 — Authentication & Security

- [ ] Registration/login
- [ ] JWT access token
- [ ] Refresh token
- [ ] User ownership checks
- [ ] Secure secrets configuration
- [ ] Rate limiting strategy

## Phase 6 — Smart Import

- [ ] Email statement parsing architecture
- [ ] Bank notification parsing abstraction
- [ ] Duplicate detection
- [ ] Suggested payment creation
- [ ] User approval workflow for imported data

## Phase 7 — Analytics

- [ ] Monthly cash-out trends
- [ ] Subscription cost trends
- [ ] Debt payoff timeline
- [ ] Category summaries
- [ ] Annual recurring cost overview

## Phase 8 — Production Readiness

- [ ] Unit tests
- [ ] Integration tests
- [ ] Testcontainers
- [ ] CI pipeline
- [ ] Docker image
- [ ] Production profile
- [ ] Database migrations with Flyway
- [ ] Structured logging
- [ ] Health checks
- [ ] Monitoring/metrics
