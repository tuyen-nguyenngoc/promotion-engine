# Promotion Engine — Order Pricing & Promotion Engine

A Spring Boot service that calculates the final price of an order after applying multiple promotion rules. Built as a solution to **Challenge 2** of the Senior Software Engineer take-home assignment.

---

## 1. Challenge Chosen — Why Challenge 2?

**Challenge 2 — Order Pricing & Promotion Engine** was selected because it showcases extensible domain logic through the Strategy and Chain of Responsibility patterns. The rule-based pricing engine is a realistic, commercially relevant problem that clearly demonstrates OCP and SRP from SOLID principles. It also allows a richer test suite that verifies individual rule isolation as well as combined pipeline correctness.

---

## 2. Architecture Overview

```
com.example.promotionengine/
├── controller/         HTTP entry points — thin, delegate to services
├── service/            Orchestration layer — transaction boundary, persistence
├── domain/
│   ├── model/          OrderContext (pipeline data carrier), DiscountDetail
│   ├── strategy/       PromotionStrategy interface + 4 rule implementations
│   └── chain/          PromotionHandler (abstract), StrategyPromotionHandler, PromotionPipeline
├── entity/             JPA entities (Product, Promotion, Coupon, Order, OrderItem)
├── repository/         Spring Data JPA repositories
├── dto/                Request / Response DTOs + ApiResponse envelope
├── exception/          BusinessException + GlobalExceptionHandler
└── config/             PromotionPipelineConfig — wires the handler chain at startup
```

**Flow for `POST /api/v1/orders/calculate`:**

1. `OrderController` validates the request (`@Valid`)
2. `OrderService` computes the subtotal, loads active promotions + coupon from DB
3. Builds an `OrderContext` (immutable data bag)
4. Passes context through `PromotionPipeline` → list of `DiscountDetail`
5. Calculates `totalDiscount` and `finalPrice` (clamped to ≥ 0)
6. Atomically redeems limited-use coupons inside the order transaction
7. Persists the order snapshot to DB
8. Returns `ApiResponse<OrderCalculateResponse>`

---

## 3. Design Patterns

### Strategy Pattern
**Files:** `domain/strategy/PromotionStrategy.java` (interface) and four implementations:
- `PercentageDiscountStrategy` — 10% of subtotal
- `Buy2Get1FreeStrategy` — `floor(qty/2) × unitPrice` per SKU
- `VipDiscountStrategy` — 5% extra for VIP customers only
- `CouponDiscountStrategy` — flat amount from the `coupons` table

Each rule is a self-contained Spring bean. Adding a new rule means creating a class that implements `PromotionStrategy` and assigning an `@Order` value to place it in the pipeline (Open/Closed Principle).

### Chain of Responsibility
**Files:** `domain/chain/PromotionHandler.java`, `StrategyPromotionHandler.java`, `PromotionPipeline.java`, `config/PromotionPipelineConfig.java`

Each `StrategyPromotionHandler` wraps a `PromotionStrategy` and passes control to the next node. The `PromotionPipeline` holds the head of the chain and collects all discount results. Chain order (PERCENTAGE → VIP → COUPON → BUY2GET1FREE) is controlled by Spring `@Order` on each strategy bean.

---

## 4. SOLID Principles

| Principle | Where |
|-----------|-------|
| **SRP** | `PercentageDiscountStrategy` only computes percentage discount; `OrderService` only orchestrates; `PromotionPipeline` only routes |
| **OCP** | New promotion rule = new class; no modifications to `PromotionPipeline`, `OrderService`, or any existing strategy |
| **LSP** | Any `PromotionStrategy` implementation can be substituted in the pipeline without breaking behaviour |
| **ISP** | `PromotionStrategy` has only two methods (`getType`, `calculate`) — no unused method burden |
| **DIP** | `OrderService` depends on `PromotionPipeline` injected by Spring; strategies are Spring `@Component`s injected via DI |

---

## 5. Database Design

| Table | Purpose |
|-------|---------|
| `products` | Catalogue (SKU, name, price). Present per spec; price lookup not used in calculate (price comes from request) |
| `promotions` | Rule type, percentage/flat value, active flag. `value` is nullable (BUY2_GET1_FREE has no fixed value) |
| `coupons` | Code (PK), flat discount, active flag, expiry date, optional usage limit, current usage count |
| `orders` | Snapshot of each calculation — subtotal, total discount, final price, customer type |
| `order_items` | Line items per order snapshot |

**Key decisions:**
- `promotions.value` is `NULLABLE` because `BUY2_GET1_FREE` calculates from item price, not a stored percentage
- No unique constraint on `promotions.type` — allows multiple versions (e.g., seasonal % changes); only `active=true` rows are used
- `orders` stores a snapshot so historical pricing is preserved even if promotions change later
- `coupons` has its own table because it has extra fields (`expiry_date`, unique `code` PK, `max_usage`, `usage_count`) that don't fit `promotions`
- Limited-use coupons are redeemed with a single atomic database update inside the order transaction, so concurrent requests cannot over-redeem the same coupon

---

## 6. How to Run the System

**Prerequisites:** Docker & Docker Compose

```bash
git clone <repo-url>
cd promotion-engine
docker compose up
```

The service will be available at `http://localhost:8080`. `docker compose` starts PostgreSQL, runs the dedicated `database` migration image once, then starts the application. The migration seeds the three default promotions (`PERCENTAGE_DISCOUNT 10%`, `BUY2_GET1_FREE`, `VIP_DISCOUNT 5%`) and two coupons (`SUMMER10`, `SAVE20`). `SUMMER10` is unlimited; `SAVE20` is limited to one redemption to demonstrate concurrency safety.

**Build and run the migration image separately:**
```bash
docker build -t promotion-engine-db-migrations ./database
docker run --rm \
  -e LIQUIBASE_COMMAND_URL=jdbc:postgresql://host.docker.internal:5432/promotiondb \
  -e LIQUIBASE_COMMAND_USERNAME=user \
  -e LIQUIBASE_COMMAND_PASSWORD=password \
  promotion-engine-db-migrations
```

The application disables Liquibase by default (`SPRING_LIQUIBASE_ENABLED=false`) so production can run migrations through a deployment job before starting the service.

### API Endpoints

**POST /api/v1/orders/calculate** — Calculate order price after applying all active promotions:
```bash
curl -X POST http://localhost:8080/api/v1/orders/calculate \
  -H "Content-Type: application/json" \
  -d '{
    "customerType": "VIP",
    "items": [
      { "sku": "A100", "price": 100, "quantity": 2 },
      { "sku": "B200", "price": 50,  "quantity": 1 }
    ],
    "couponCode": "SUMMER10"
  }'
```

```json
{
  "data": {
    "subtotal": 250.00,
    "discounts": [
      { "type": "PERCENTAGE_DISCOUNT", "amount": 25.00 },
      { "type": "VIP_DISCOUNT",        "amount": 12.50 },
      { "type": "COUPON_SUMMER10",     "amount": 10.00 },
      { "type": "BUY2_GET1_FREE",      "amount": 100.00 }
    ],
    "totalDiscount": 147.50,
    "finalPrice": 102.50
  },
  "error": null
}
```

**GET /api/v1/promotions** — List all active promotions:
```bash
curl http://localhost:8080/api/v1/promotions
```

```json
{
  "data": [
    { "id": 1, "type": "PERCENTAGE_DISCOUNT", "value": 10.0000, "active": true, "createdAt": "..." },
    { "id": 2, "type": "BUY2_GET1_FREE",      "value": null,    "active": true, "createdAt": "..." },
    { "id": 3, "type": "VIP_DISCOUNT",         "value": 5.0000,  "active": true, "createdAt": "..." }
  ],
  "error": null
}
```

**POST /api/v1/promotions** — Create a new promotion (e.g. a seasonal 15% winter discount):
```bash
curl -X POST http://localhost:8080/api/v1/promotions \
  -H "Content-Type: application/json" \
  -d '{
    "type": "PERCENTAGE_DISCOUNT",
    "value": 15,
    "active": true
  }'
```

```json
{
  "data": { "id": 4, "type": "PERCENTAGE_DISCOUNT", "value": 15.0000, "active": true, "createdAt": "..." },
  "error": null
}
```

> **Note:** `type` must be one of: `PERCENTAGE_DISCOUNT`, `BUY2_GET1_FREE`, `VIP_DISCOUNT`.
> Multiple records with the same type are allowed — all records with `active: true` are applied during calculate.
> `value` may be omitted when `type` is `BUY2_GET1_FREE` (that rule derives the discount from the item price, not a stored value).

**GET /api/v1/coupons** — List all coupons:
```bash
curl http://localhost:8080/api/v1/coupons
```

```json
{
  "data": [
    { "code": "SUMMER10", "discountAmount": 10.00, "active": true, "expiryDate": "2099-12-31", "maxUsage": null, "usageCount": 0, "createdAt": "..." },
    { "code": "SAVE20",   "discountAmount": 20.00, "active": true, "expiryDate": "2099-12-31", "maxUsage": 1, "usageCount": 0, "createdAt": "..." }
  ],
  "error": null
}
```

**POST /api/v1/coupons** — Create a new coupon:
```bash
curl -X POST http://localhost:8080/api/v1/coupons \
  -H "Content-Type: application/json" \
  -d '{
    "code": "WINTER30",
    "discountAmount": 30,
    "active": true,
    "expiryDate": "2026-12-31",
    "maxUsage": 100
  }'
```

```json
{
  "data": { "code": "WINTER30", "discountAmount": 30.00, "active": true, "expiryDate": "2026-12-31", "maxUsage": 100, "usageCount": 0, "createdAt": "..." },
  "error": null
}
```

> **Note:** `code` is case-sensitive and stored exactly as provided. Creating a duplicate code returns `409 COUPON_ALREADY_EXISTS`.
> `expiryDate` is optional — if omitted, the coupon never expires. `maxUsage` is optional — if omitted, the coupon can be redeemed unlimited times.

**PATCH /api/v1/promotions/{id}/deactivate** — Deactivate a promotion by ID:
```bash
curl -X PATCH http://localhost:8080/api/v1/promotions/1/deactivate
```

```json
{ "data": { "id": 1, "type": "PERCENTAGE_DISCOUNT", "value": 10.0000, "active": false, "createdAt": "..." }, "error": null }
```

> Returns `404 PROMOTION_NOT_FOUND` if the id does not exist. Returns `409 PROMOTION_ALREADY_INACTIVE` if already inactive.

**PATCH /api/v1/coupons/{code}/deactivate** — Deactivate a coupon by code:
```bash
curl -X PATCH http://localhost:8080/api/v1/coupons/SUMMER10/deactivate
```

```json
{ "data": { "code": "SUMMER10", "discountAmount": 10.00, "active": false, "expiryDate": "2099-12-31", "maxUsage": null, "usageCount": 0, "createdAt": "..." }, "error": null }
```

> `{code}` is case-sensitive and must match exactly as stored. Returns `409 COUPON_ALREADY_INACTIVE` if already inactive.

---

## 7. How to Run the Tests

**Unit tests only (no Docker required):**
```bash
./mvnw test -Dtest="*StrategyTest,PromotionPipelineTest,OrderServiceTest"
```

**All tests including integration tests (Docker required for Testcontainers):**
```bash
./mvnw verify
```

Testcontainers automatically spins up a PostgreSQL 16 container for integration tests. Tests explicitly enable Liquibase and point it at `file:./database/changelog/db.changelog-master.yaml`, so no external database is needed.

---

## 8. Trade-offs & Improvements

| Trade-off | Detail |
|-----------|--------|
| **Discounts are independent** | All rules calculate against the original subtotal, not a cascading remainder. This matches the assignment example but differs from some real-world engines where discount order matters. |
| **Coupon usage is counter-based** | Limited-use coupons are protected by an atomic `usage_count` increment. A production audit trail could add a `coupon_usages` table keyed by coupon and order/customer. |
| **No pagination on `GET /promotions`** | Dataset is expected to be small; a real system would add `Page<PromotionResponse>`. |
| **`products` table exists but is unused in calculate** | Required by the spec. A real enhancement would validate that request SKUs exist in the products catalogue. |
| **No authentication/authorisation** | Out of scope. In production, `POST /promotions` should require an admin role. |
| **Simple error envelope** | The `ApiResponse` envelope is minimal. A production system might include a `requestId` and timestamp for traceability. |

---

## 9. What Would Break at Scale & How to Fix It

| Problem | Scale threshold | Fix |
|---------|----------------|-----|
| **Full table scan of `promotions`** | Millions of promotions | Index on `(active, type)`; cache active promotions in Redis with a short TTL |
| **Synchronous DB write on every calculate** | High RPS pricing lookups | Make the order-persistence step async (publish to a Kafka topic, write via consumer) |
| **Testcontainers startup per test class** | Large test suite | Use a shared `@Container` with `@DynamicPropertySource` (already done) or a singleton container pattern |
| **Single Spring Boot instance** | Horizontal scale | The service is stateless (no in-memory state between requests), so it scales horizontally with a load balancer; the only shared state is the DB |
| **Coupon redemption hot row** | High contention on one limited coupon | The current atomic update prevents over-redemption. At very high contention, shard coupon allocations or pre-allocate redemption tokens. |
