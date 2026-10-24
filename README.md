# ShopLite – Order & Inventory Microservices

A small microservices project built with **Java 17** and **Spring Boot 3**. Two independent services, an **order-service** and an **inventory-service**, each with its own PostgreSQL database. They talk to each other over HTTP using **Spring Cloud OpenFeign**. A small **Next.js + Material UI** frontend lets you browse products, place orders, and watch the stock change.

## Live Demo

- **App:** https://shoplite-microservices.vercel.app
- **order-service API (Swagger):** https://shoplite-order-9eu9.onrender.com/swagger-ui.html
- **inventory-service API (Swagger):** https://shoplite-inventory.onrender.com/swagger-ui.html

The services run on Render's free plan and sleep when idle, so the **first request can take up to a minute** while they wake up.

| Part              | Hosted on |
|-------------------|-----------|
| Frontend          | Vercel |
| order-service     | Render (Docker, from [`render.yaml`](render.yaml)) |
| inventory-service | Render (Docker, from [`render.yaml`](render.yaml)) |
| Databases         | Neon (serverless PostgreSQL): `inventory_db` and `order_db` |

## What are microservices?

Microservices split an application into small, independent services that each own one job and their own data. They communicate over the network (here, REST over HTTP), so each service can be built, deployed, and scaled on its own.

## Tech Stack

| Area            | Technologies |
|-----------------|--------------|
| Language        | Java 17 |
| Framework       | Spring Boot 3, Spring Web (REST), Spring Data JPA |
| Service-to-service calls | Spring Cloud OpenFeign |
| Database        | PostgreSQL 16 (separate database per service) |
| Error handling  | `@RestControllerAdvice` (global exception handling), Bean Validation |
| API docs        | Swagger UI / OpenAPI (springdoc) |
| Testing         | JUnit 5, Mockito, MockMvc |
| Frontend        | Next.js 15 (App Router), React 19, TypeScript, Material UI 7 |
| DevOps          | Docker (multi-stage builds), Docker Compose |

## Architecture

```
                        ┌──────────────────────────────────────────────┐
                        │   frontend (Next.js + MUI)   :3001           │
                        │   Products page · Orders page · Buy dialog   │
                        └──────────┬────────────────────────┬──────────┘
                POST /orders,      │                        │  GET /products,
                GET /orders        │                        │  POST /products
                                   ▼                        └─────────────────────────────────────┐
                                                                                                  ▼
┌──────────────────────────────────────────┐   OpenFeign (HTTP)   ┌──────────────────────────────────────────┐
│          order-service  :8081            │ ───────────────────▶ │        inventory-service  :8082          │
│                                          │ 1. GET /products/{id}│                                          │
│  OrderController → OrderService          │ 2. PUT /products/{id}│  ProductController → ProductService      │
│                  → InventoryClient(Feign)│    /reduce?qty=      │                    → ProductRepository   │
│                  → OrderRepository       │ ◀─────────────────── │                                          │
└───────────────────┬──────────────────────┘   JSON / errors      └───────────────────┬──────────────────────┘
                    │                                                                  │
                    ▼                                                                  ▼
            ┌──────────────┐                                                  ┌────────────────┐
            │   order_db   │       (one PostgreSQL server, two databases)     │  inventory_db  │
            │   orders     │                                                  │  products      │
            └──────────────┘                                                  └────────────────┘
```

**How an order is placed:**

1. `order-service` asks `inventory-service` for the product (`GET /products/{id}`).
2. If the stock is too low, it returns a clear **400** error right away.
3. Otherwise it asks `inventory-service` to reduce the stock (`PUT /products/{id}/reduce?qty=`).
4. It saves the order with status `CONFIRMED` in its own database.

**Design points:**

- **Database per service:** `order-service` never reads `inventory_db`. An order stores only the `productId`, with no cross-service foreign key.
- **No overselling:** `inventory-service` checks and reduces stock in one atomic SQL statement (`UPDATE ... WHERE stock >= qty`), so parallel orders can't take more than what's available.
- **Clear errors across services:** a Feign `ErrorDecoder` turns inventory-service's 404/400 responses into order-service exceptions, and `@RestControllerAdvice` returns them as JSON. If inventory-service is down, order-service answers **503** instead of crashing.

## Frontend

| Page | What it does |
|------|--------------|
| **Products** (`/products`) | Table of products with live stock (green / "5 left" / "Out of stock"), summary cards, **Buy** dialog, **Add product** dialog |
| **Orders** (`/orders`) | All orders with product names, quantity, status, and time |

- **Buy** calls `order-service`, which calls `inventory-service`. Errors from the backend are shown as-is in the dialog, e.g. *"Not enough stock for Keyboard: requested 9, available 5"*.
- Orders only store a `productId`, so the Orders page also asks `inventory-service` for product names and joins them in the UI. Services never share a database.
- Both services allow the frontend's origin through **CORS** (`CORS_ALLOWED_ORIGINS`).

## Endpoints

### order-service (port 8081)

| Method | Endpoint  | Description | Responses |
|--------|-----------|-------------|-----------|
| POST   | `/orders` | Place an order: checks and reduces stock in inventory-service, then saves the order | `201` created · `400` not enough stock / invalid body · `404` unknown product · `503` inventory-service down |
| GET    | `/orders` | List all orders (newest first) | `200` |

### inventory-service (port 8082)

| Method | Endpoint                          | Description | Responses |
|--------|-----------------------------------|-------------|-----------|
| GET    | `/products`                       | List all products | `200` |
| GET    | `/products/{id}`                  | Get one product | `200` · `404` |
| POST   | `/products`                       | Create a product | `201` · `400` |
| PUT    | `/products/{id}/reduce?qty={qty}` | Reduce stock (used by order-service) | `200` · `400` not enough stock · `404` |

Four products are seeded on first start: Laptop (10), Smartphone (25), Headphones (50), Keyboard (5).

Swagger UI:

- order-service: http://localhost:8081/swagger-ui.html
- inventory-service: http://localhost:8082/swagger-ui.html

## Running with Docker Compose

Requirements: [Docker Desktop](https://www.docker.com/products/docker-desktop/).

```bash
git clone https://github.com/bhavadharanithangaraj/shoplite-microservices.git
cd shoplite-microservices
docker compose up --build
```

Then open **http://localhost:3001**.

This starts four containers:

| Container           | Port on your machine |
|---------------------|----------------------|
| `shoplite-frontend` | 3001 |
| `order-service`     | 8081 |
| `inventory-service` | 8082 |
| `shoplite-postgres` | 5433 (holds `inventory_db` and `order_db`) |

Stop with `docker compose down`. Add `-v` to also reset the databases and the seed data.

## Sample Requests

These use `curl` in bash (macOS, Linux, Git Bash). In Windows PowerShell, use `curl.exe` and escape the inner quotes, e.g. `-d '{\"productId\":1,\"quantity\":2}'`.

**List products**

```bash
curl http://localhost:8082/products
```
```json
[{"id":1,"name":"Laptop","stock":10},{"id":2,"name":"Smartphone","stock":25},{"id":3,"name":"Headphones","stock":50},{"id":4,"name":"Keyboard","stock":5}]
```

**Place an order**

```bash
curl -X POST http://localhost:8081/orders \
  -H "Content-Type: application/json" \
  -d '{"productId": 1, "quantity": 2}'
```
```json
{"id":1,"productId":1,"quantity":2,"status":"CONFIRMED","createdAt":"2026-10-24T12:05:26.000446Z"}
```

**Check that the stock went down (10 → 8)**

```bash
curl http://localhost:8082/products/1
```
```json
{"id":1,"name":"Laptop","stock":8}
```

**Order more than is in stock → 400**

```bash
curl -X POST http://localhost:8081/orders \
  -H "Content-Type: application/json" \
  -d '{"productId": 4, "quantity": 6}'
```
```json
{"timestamp":"...","status":400,"error":"Bad Request","message":"Not enough stock for Keyboard: requested 6, available 5"}
```

**Order a product that doesn't exist → 404**

```bash
curl -X POST http://localhost:8081/orders \
  -H "Content-Type: application/json" \
  -d '{"productId": 999, "quantity": 1}'
```
```json
{"timestamp":"...","status":404,"error":"Not Found","message":"Product 999 not found"}
```

**List orders**

```bash
curl http://localhost:8081/orders
```

**Create a product**

```bash
curl -X POST http://localhost:8082/products \
  -H "Content-Type: application/json" \
  -d '{"name": "Mouse", "stock": 30}'
```

## Running Tests

```bash
cd order-service
mvn test
```

The tests mock the Feign client with Mockito, so no running services or database are needed:

- `OrderServiceTest`: successful order reduces stock and saves a `CONFIRMED` order; not enough stock throws and changes nothing; unknown product saves nothing
- `OrderControllerTest` (MockMvc): low stock returns a clear `400` JSON error through `@RestControllerAdvice`

## Running Without Docker (for development)

Requirements: Java 17, Maven 3.9+.

```bash
docker compose up -d postgres                  # just the database

cd inventory-service && mvn spring-boot:run    # terminal 1, port 8082
cd order-service && mvn spring-boot:run        # terminal 2, port 8081
cd frontend && npm install && npm run dev      # terminal 3, http://localhost:3000
```

## Screenshots

_Screenshots coming soon._

<!--
Save screenshots in docs/screenshots/ and uncomment:

| Products | Buy dialog (error) | Orders |
|----------|--------------------|--------|
| ![Products](docs/screenshots/products.png) | ![Buy](docs/screenshots/buy-error.png) | ![Orders](docs/screenshots/orders.png) |
-->


## Project Structure

```
shoplite-microservices/
├── inventory-service/          Spring Boot app (products, stock)
│   ├── src/main/java/com/shoplite/inventory/
│   │   ├── controller/  service/  repository/  entity/  dto/  exception/  config/
│   ├── Dockerfile
│   └── pom.xml
├── order-service/              Spring Boot app (orders, calls inventory via Feign)
│   ├── src/main/java/com/shoplite/order/
│   │   ├── controller/  service/  repository/  entity/  dto/  exception/  client/
│   ├── src/test/java/...       JUnit + Mockito tests
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                   Next.js + TypeScript + Material UI
│   ├── src/app/                Pages: products, orders
│   ├── src/components/         Buy dialog, Add product dialog, navbar
│   ├── src/lib/api.ts          Calls both services
│   └── Dockerfile
├── docker/postgres/init.sql    Creates the second database (order_db)
├── docker-compose.yml
└── README.md
```

## Possible Improvements

- **Saga / compensation:** if saving an order fails *after* stock was reduced, the stock should be put back. Real systems handle this with the Saga pattern or events (e.g. Kafka).
- **Resilience:** add retries and a circuit breaker (Resilience4j) around the Feign client.
- **Service discovery and gateway:** Eureka plus Spring Cloud Gateway instead of fixed URLs.
- **More order statuses:** `CANCELLED`, `SHIPPED`, with stock restored on cancel.
