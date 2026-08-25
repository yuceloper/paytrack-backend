# Backend Testing

## Local smoke test

```bash
docker compose up -d
mvn clean package
mvn spring-boot:run
```

Then verify:

```bash
curl "http://localhost:8080/api/v1/dashboard/summary?userId=1"
curl "http://localhost:8080/api/v1/payments/upcoming?userId=1&days=7"
```

Core resource APIs are available under:

- `/api/v1/credit-cards`
- `/api/v1/loans`
- `/api/v1/subscriptions`
- `/api/v1/bills`

The first end-to-end smoke test should create sample resources and payments, then verify dashboard totals and upcoming payment ordering.
