# Product Catalog — Frontend

A small Vite + TypeScript single-page app for the product-composite API.
Pages (hash router): **list**, **detail**, **create**, **search**.

Features: product list with thumbnails + 5s auto-refresh (polling), product
detail with images, reviews and recommendations (read / add / delete), name
search, and product creation.

## How it talks to the backend

The app calls the relative path `/product-composite`. In dev, the Vite server
**proxies** that to the backend (see `vite.config.ts`), so the browser stays
same-origin and **no CORS config is needed** on the microservices.

- Default proxy target: `http://localhost:8080` (the port `product-composite`
  is published on by `docker-compose`).
- Running the backend **without** docker? The composite service listens on
  `7000` locally — start the frontend with:
  ```
  VITE_API_TARGET=http://localhost:7000 npm run dev
  ```

## Live test — step by step

1. **Start the backend** (from `../backend`):
   ```
   cd ../backend
   docker-compose up --build
   ```
   `product-composite` is then reachable at `http://localhost:8080`.

   > After any Java change, rebuild the images: `docker-compose build product product-composite`.

2. **Seed a product** (new terminal):
   ```
   curl -X POST localhost:8080/product-composite ^
     -H "Content-Type: application/json" ^
     -d "{\"productId\":1,\"name\":\"Test Phone\",\"weight\":1,\"brand\":\"Acme\",\"category\":\"Phones\",\"price\":9.99,\"countInStock\":5,\"description\":\"demo\",\"image\":\"\",\"rating\":0}"
   ```
   (On bash/macOS/Linux use `\` line-continuations and single quotes instead of `^`/escaped quotes.)

3. **Start the frontend** (from this folder):
   ```
   npm install
   npm run dev
   ```
   Open http://localhost:5173

4. **Verify in the browser:**
   - The list page shows the seeded product.
   - Click the product name → detail page shows all fields.
   - **Add Product** → fill the form → on success you return to the list with the new item.
   - **Search box** → type part of a name (e.g. `phone`) → results page shows matches.

## Composite API endpoints

All paths are relative to the composite base `http://localhost:8080` (docker)
or `http://localhost:7000` (local run). The frontend calls them via the Vite
proxy as `/product-composite/...`.

### Products

| UI action      | Method | Path                                 | Notes |
|----------------|--------|--------------------------------------|-------|
| List all       | GET    | `/product-composite`                 | reviews/recommendations are `null` in the list |
| Detail         | GET    | `/product-composite/{id}`            | includes reviews + recommendations |
| Search by name | GET    | `/product-composite/search?name=...` | partial, case-insensitive |
| Create         | POST   | `/product-composite`                 | body = full product aggregate |
| Delete product | DELETE | `/product-composite/{id}`            | also deletes its reviews + recommendations |

### Reviews

| UI action     | Method | Path                                          | Body |
|---------------|--------|-----------------------------------------------|------|
| Add review    | POST   | `/product-composite/{id}/review`              | `{ "author", "subject", "content" }` — `reviewId` auto-assigned |
| Delete review | DELETE | `/product-composite/{id}/review/{reviewId}`   | — |

### Recommendations

| UI action            | Method | Path                                                          | Body |
|----------------------|--------|--------------------------------------------------------------|------|
| Add recommendation   | POST   | `/product-composite/{id}/recommendation`                     | `{ "author", "rate" (1-5), "content" }` — `recommendationId` auto-assigned |
| Delete recommendation| DELETE | `/product-composite/{id}/recommendation/{recommendationId}`  | — |

### Examples

```bash
# add a review to product 100
curl -X POST localhost:8080/product-composite/100/review \
  -H "Content-Type: application/json" \
  -d '{"author":"Eve","subject":"Great","content":"Works well"}'

# add a recommendation
curl -X POST localhost:8080/product-composite/100/recommendation \
  -H "Content-Type: application/json" \
  -d '{"author":"Frank","rate":5,"content":"Highly recommend"}'

# delete review #3 / recommendation #3
curl -X DELETE localhost:8080/product-composite/100/review/3
curl -X DELETE localhost:8080/product-composite/100/recommendation/3
```

## How it flows (composite → core service → DB)

| Composite endpoint | Core service | Storage |
|--------------------|--------------|---------|
| product (CRUD + search) | `product-service` `/product...` | MongoDB `product-db.products` |
| review (add/delete)     | `review-service` `/review...`   | MySQL `review-db.reviews` |
| recommendation (add/delete) | `recommendation-service` `/recommendation...` | MongoDB `recommendation-db` |

`reviewId` / `recommendationId` are assigned server-side as `max(existing for
that product) + 1`, so they are unique **per product**, not globally.

## Browsing / querying the databases

### Web UIs (start automatically with the stack)

| Tool | URL | Views | Login |
|------|-----|-------|-------|
| **Adminer** | http://localhost:8082 | MySQL `review-db` (reviews) | System `MySQL`, Server `mysql`, User `user`, Password `pwd`, Database `review-db` |
| **mongo-express** | http://localhost:8081 | Mongo `product-db`, `recommendation-db` | none |

Both are defined as services in `docker-compose.yml`, so `docker-compose up -d`
brings them up with everything else.

### Command line — MySQL (reviews)

```bash
# interactive SQL shell
docker exec -it backend-mysql-1 mysql -uuser -ppwd review-db
#   SHOW TABLES;
#   SELECT * FROM reviews;
#   SELECT * FROM reviews WHERE product_id = 100;

# one-shot query
docker exec backend-mysql-1 mysql -uuser -ppwd review-db -e "SELECT * FROM reviews;"

# from a host-installed mysql client (note: host port is 3307)
mysql -h 127.0.0.1 -P 3307 -uuser -ppwd review-db
```

### Command line — MongoDB (products, recommendations)

```bash
# interactive mongo shell
docker exec -it backend-mongodb-1 mongo product-db
#   show collections
#   db.products.find().pretty()
#   db.products.find({ productId: 100 }).pretty()
#   use recommendation-db
#   db.recommendations.find().pretty()

# one-shot queries
docker exec backend-mongodb-1 mongo product-db --quiet --eval "db.products.find().pretty()"
docker exec backend-mongodb-1 mongo recommendation-db --quiet --eval "db.recommendations.find().pretty()"

# from a host-installed client (port 27017 is published)
mongo mongodb://localhost:27017/product-db
```

> Drop `-it` for one-shot `--eval` / `-e` queries. MySQL columns are snake_case
> (`product_id`, `review_id`); Mongo fields are camelCase (`productId`,
> `recommendationId`).

## Rebuilding after backend changes

Each service is packaged into its own Docker image, so rebuild the ones you
changed and recreate the containers:

```bash
cd ../backend
docker-compose up -d --build product product-composite review recommendation
```

> The `api` module is shared by all services; if you change it, rebuild every
> service that depends on it.

## Testing & coverage

### Unit tests (composite controller)

`ProductCompositeServiceControllerTest` (13 tests) covers the business logic in
the composite controller — aggregate mapping (real fields, not hardcoded),
create fan-out, the `addReview`/`addRecommendation` next-id assignment
(`max(existing)+1`), `getProduct` not-found handling, and the delete
delegations. They are plain Mockito tests (no Docker/DB needed).

```bash
cd ../backend
# run the unit tests
./gradlew :microservices:product-composite-service:test

# run only the controller test
./gradlew :microservices:product-composite-service:test \
  --tests "com.light.microservices.composite.product.ProductCompositeServiceControllerTest"
```

### Coverage (JaCoCo)

JaCoCo is enabled on the composite service (`build.gradle`). The `test` task is
`finalizedBy jacocoTestReport`, so a report is produced on every test run:

- **HTML:** `microservices/product-composite-service/build/reports/jacoco/test/html/index.html`
- **CSV:**  `microservices/product-composite-service/build/reports/jacoco/test/jacocoTestReport.csv`

Current: **`ProductCompositeServiceController` ≈ 98%** instruction coverage.

> JaCoCo only measures code run inside the unit-test JVM. The
> `ProductCompositeIntegration` class (RestTemplate calls) is exercised by the
> end-to-end regression script instead, so its unit-coverage number is low by
> design — that path is validated against the live stack, not in-process.

### End-to-end regression test

`backend/test-em-all.bash` drives the **running** composite service and asserts
the full flow: create product → verify real fields → list → search → add review
→ add recommendation → delete review → delete recommendation → delete product
(expects 404 after). Exits non-zero on any failure.

```bash
cd ../backend
./test-em-all.bash                 # defaults to localhost:8080
HOST=localhost PORT=8080 ./test-em-all.bash
```

Run it after `docker-compose up -d` to confirm the whole stack is healthy.

## Notes

- **No WebSocket needed** — all operations are request/response REST. The list
  page uses **polling** (re-fetches every 5s) to reflect changes made by other
  clients. Vite's dev server uses its own internal WebSocket only for hot-reload.
- **Search** is wired end-to-end: `product-composite/search` →
  `product-service /product/search` → Mongo `findByNameContainingIgnoreCase`.
  Because it goes through the composite (port 8080), it's reachable from the
  host even though `product-service` itself isn't published by docker-compose.
- **MySQL host port** is mapped to **3307** (not 3306) in `docker-compose.yml`
  to avoid clashing with a locally-installed MySQL. The `review` service still
  reaches it internally on `mysql:3306`.
- **Swagger UI** for the composite API: `http://localhost:8080/openapi/swagger-ui.html`.
