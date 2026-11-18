# expense_splitter

[![CI](https://github.com/eastezz/expense_splitter/actions/workflows/ci.yml/badge.svg)](https://github.com/eastezz/expense_splitter/actions/workflows/ci.yml)

A REST API for splitting group expenses, inspired by apps like Settle Up or Splitwise. Built with Java 21 and Spring Boot 4.

## Example: settling a trip

![Swagger UI of the running app with all endpoints](assets/swagger.png)

Anna, Ben and Cem go on a trip. Anna pays 90 EUR for the hotel and Ben pays 30 EUR for pizza, both split equally by three. I entered this through the API, and it returned these balances (positive = gets money back, negative = owes money):

| Member | Paid | Share | Balance |
|---|---:|---:|---:|
| Anna | 90 | 40 | +50 |
| Ben | 30 | 40 | -10 |
| Cem | 0 | 40 | -40 |

The API suggests two transfers to settle up:

- Cem pays Anna 40 EUR
- Ben pays Anna 10 EUR

After I recorded both transfers as payments, every balance was 0 and there were no transfers left to suggest.

## How it works

- Money is stored as whole cents in a `long`, never as floating point, so there are no rounding errors.
- An equal split gives the leftover cents to the members with the smallest ids: 1000 cents split by three is 334 / 333 / 333. An exact split must add up to the total, otherwise the request is rejected.
- A member's balance is what they paid minus their share, plus the payments they sent minus the payments they received. The balances of a group always add up to 0. They are calculated on every request and never stored, so they can't get out of sync.
- Settle-up is greedy: the biggest debtor pays the biggest creditor until everyone is at 0. This needs at most n-1 transfers for n members. It is not always the absolute minimum, because finding that is NP-hard.
- Errors are returned as ProblemDetail (RFC 9457) JSON.

The key code is in [SplitCalculator.java](backend/src/main/java/io/github/eastezz/fairshare/expense/SplitCalculator.java), [BalanceCalculator.java](backend/src/main/java/io/github/eastezz/fairshare/settlement/BalanceCalculator.java) and [SettlementCalculator.java](backend/src/main/java/io/github/eastezz/fairshare/settlement/SettlementCalculator.java).

## Quick start

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Then open http://localhost:8080/swagger-ui.html to try every endpoint in the browser. On macOS or Linux, use `./mvnw` instead. You need JDK 21 or newer; Maven is not needed, because the project ships the Maven wrapper.

To create a group and a member from a second PowerShell window:

```powershell
$api = "http://localhost:8080/api"
$group = Invoke-RestMethod -Method Post "$api/groups" -ContentType "application/json" -Body '{"name":"Trip"}'
Invoke-RestMethod -Method Post "$api/groups/$($group.id)/members" -ContentType "application/json" -Body '{"name":"Anna"}'
```

This way the data is kept in an in-memory H2 database, so it is gone after a restart. To keep it, run the app with Docker (see below).

## Run with Docker

The whole app (PostgreSQL, the Spring Boot backend and the React frontend) starts with one command. You only need [Docker Desktop](https://www.docker.com/products/docker-desktop/).

```powershell
docker compose up --build
```

Then open http://localhost:3000. The backend is also reachable directly at http://localhost:8080/swagger-ui.html. Stop everything with `docker compose down`.

- The tables are created by the Flyway migrations in [db/migration](backend/src/main/resources/db/migration). Hibernate only checks that they match the entities (`ddl-auto=validate`).
- The data is stored in the Docker volume `db-data`, so it survives `docker compose down` and a restart. `docker compose down -v` deletes it.
- To use your own database password, copy `.env.example` to `.env` and change the values there. `.env` is ignored by Git.

## Endpoints

| Method | Path | What it does |
|---|---|---|
| GET | `/api/health` | Health check |
| POST | `/api/groups` | Create a group |
| GET | `/api/groups` | List all groups |
| GET | `/api/groups/{id}` | Get one group |
| POST | `/api/groups/{groupId}/members` | Add a member to a group |
| GET | `/api/groups/{groupId}/members` | List the members of a group |
| POST | `/api/groups/{groupId}/expenses` | Add an expense (split `EQUAL` or `EXACT`) |
| GET | `/api/groups/{groupId}/expenses` | List the expenses of a group |
| GET | `/api/groups/{groupId}/expenses/{expenseId}` | Get one expense |
| DELETE | `/api/groups/{groupId}/expenses/{expenseId}` | Delete an expense |
| GET | `/api/groups/{groupId}/balances` | Balance of every member |
| GET | `/api/groups/{groupId}/settlements` | Suggested transfers to settle up |
| POST | `/api/groups/{groupId}/payments` | Record a payment between two members |
| GET | `/api/groups/{groupId}/payments` | List the payments of a group |

## Tests

```powershell
cd backend
.\mvnw.cmd verify
```

The 67 tests check the split rounding, the balances, the settle-up (including a property test over 500 random groups), every endpoint with MockMvc including the error cases, and an end-to-end test that pays all suggested transfers and ends with every balance at 0. GitHub Actions runs them on every push to `main` and on every pull request.

## Roadmap

- [x] REST API for groups, members, expenses, balances, settle-up and payments
- [x] Tests and a GitHub Actions workflow that runs them
- [ ] React frontend
- [x] PostgreSQL with Flyway and Docker Compose
- [ ] Login
