# expense_splitter

[![CI](https://github.com/eastezz/expense_splitter/actions/workflows/ci.yml/badge.svg)](https://github.com/eastezz/expense_splitter/actions/workflows/ci.yml)

A REST API for splitting group expenses, inspired by apps like Settle Up or Splitwise.
You create a group, add members and record expenses that are split equally or by exact amounts.
The API then shows each member's balance and suggests how to settle up with as few transfers as possible.

## Tech stack

- Java 21, Spring Boot 4
- Spring Web MVC, Spring Data JPA / Hibernate, Bean Validation
- H2 in-memory database
- springdoc-openapi (Swagger UI)
- JUnit 5, MockMvc, AssertJ
- GitHub Actions (CI)
- React frontend: planned

## Run it locally

You need a JDK 21 or newer. Maven is not needed, because the project ships the Maven wrapper.

```bash
cd backend
```

Windows:

```bash
.\mvnw.cmd spring-boot:run
```

macOS / Linux:

```bash
./mvnw spring-boot:run
```

Then open http://localhost:8080/swagger-ui.html to try every endpoint in the browser.

Run the tests with `.\mvnw.cmd verify` (Windows) or `./mvnw verify` (macOS / Linux).

## Endpoints

| Method | Path | What it does |
| ------ | ---- | ------------ |
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

## Example

A trip with Anna, Ben and Cem:

- Anna pays 90 EUR for the hotel, split equally by three (30 each).
- Ben pays 30 EUR for pizza, split equally by three (10 each).

Balances (positive = gets money back, negative = owes money):

| Member | Paid | Share | Balance |
| ------ | ---: | ----: | ------: |
| Anna | 90 | 40 | +50 |
| Ben | 30 | 40 | -10 |
| Cem | 0 | 40 | -40 |

Suggested transfers:

- Cem pays Anna 40 EUR
- Ben pays Anna 10 EUR

## Design notes

- **Money in whole cents.** All amounts are stored as `long` cents, never as floating point, so there are no rounding errors.
- **Leftover cents.** When an amount cannot be split evenly (10.00 EUR by three), the extra cents go to the members with the smallest ids, so the shares always add up to the total.
- **Balances are calculated, not stored.** Every balance is computed from the expenses and payments of the group, so it can never get out of sync.
- **Greedy settle-up.** The biggest debtor pays the biggest creditor, again and again. This needs at most n-1 transfers for n members. It is not always the absolute minimum, because finding that is NP-hard.
- **Clear errors.** Errors are returned as ProblemDetail (RFC 9457) JSON responses.

## Roadmap

- React frontend
- PostgreSQL with Flyway migrations and Docker Compose
- Login
