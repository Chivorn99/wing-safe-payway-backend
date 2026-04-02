# WingSafePay — Backend API

A Spring Boot REST API for the WingView financial management application. Provides user authentication, transaction tracking, savings goals, spending analytics, and receipt OCR scanning.

## Tech Stack

- **Java 21** + **Spring Boot 3**
- **PostgreSQL 16** (via Docker or local)
- **Spring Security** + **JWT** authentication
- **Hibernate / JPA** for ORM
- **Tesseract OCR** for receipt scanning
- **Lombok** for boilerplate reduction

## Prerequisites

- Java 21+
- Maven 3.9+
- PostgreSQL 16 (or Docker)

## Quick Start

### 1. Clone & configure environment

```bash
cp .env.example .env
# Edit .env with your database credentials
```

### 2. Start PostgreSQL (via Docker)

```bash
docker-compose up -d
```

### 3. Run the application

```bash
./mvnw spring-boot:run
```

The server starts on `http://localhost:8081`.

## Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `DB_URL` | JDBC connection string | `jdbc:postgresql://localhost:5432/safepay_db` |
| `DB_USERNAME` | Database user | `postgres` |
| `DB_PASSWORD` | Database password | `yourpassword` |
| `JWT_SECRET` | JWT signing key (min 32 chars) | `my-super-secret-key-at-least-32-characters` |
| `JWT_EXPIRATION` | Token TTL in milliseconds | `86400000` (24h) |
| `SERVER_PORT` | Server port | `8081` |
| `TESSERACT_PATH` | Path to Tesseract binary | `C:/Program Files/Tesseract-OCR/tesseract` |

## Database Schema

```
┌──────────┐       ┌──────────────┐       ┌───────────┐
│  users   │──1:N──│ transactions │──N:1──│ merchants │
│          │       └──────────────┘       └───────────┘
│          │──1:N──│ saving_goals │
└──────────┘       └──────────────┘
```

**4 tables** with foreign key relationships:
- `users` → `transactions` (OneToMany)
- `users` → `saving_goals` (OneToMany)
- `transactions` → `merchants` (ManyToOne)

## API Endpoints

### Authentication (public)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/auth/register` | Register new user |
| `POST` | `/api/auth/login` | Login, returns JWT |

### User Profile (requires JWT)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/users/me` | Get current user profile |
| `PUT` | `/api/users/change-password` | Change password |

### Transactions (requires JWT)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/transactions` | Create transaction |
| `GET` | `/api/transactions/me` | List my transactions |
| `GET` | `/api/transactions/summary` | Spending analytics |
| `GET` | `/api/transactions/filter?category=FOOD` | Filter by category (JOIN query) |
| `GET` | `/api/transactions/search?keyword=coffee` | Search transactions (JOIN query) |

### Savings Goals (requires JWT)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/goals` | Create goal |
| `GET` | `/api/goals/me` | List my goals |
| `PATCH` | `/api/goals/{id}/progress` | Add money to goal |
| `DELETE` | `/api/goals/{id}` | Delete goal |

### Admin (requires ADMIN role)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/admin/users` | List all users |
| `GET` | `/api/admin/stats` | Platform-wide stats |

### Receipt OCR
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/receipts/scan` | Scan receipt image |

## Authentication & Roles

- **JWT-based** authentication with role claims
- **Two roles**: `USER` (default), `ADMIN`
- Admin endpoints are protected with `@PreAuthorize("hasRole('ADMIN')")`
- Passwords are hashed with **BCrypt**

## Seeded Demo Data

On first start, the app seeds:
- **Demo user**: `0961234567` / `123456` (role: USER) with 40+ transactions and 4 goals
- **Admin user**: `0960000000` / `admin123` (role: ADMIN)

## Running Tests

```bash
./mvnw test
```

## Project Structure

```
src/main/java/com/wingsafepay/wing_safe_pay/
├── config/          # SecurityConfig, GlobalExceptionHandler, DataSeeder
├── controller/      # REST controllers (Auth, User, Transaction, Goal, Admin, Receipt)
├── dto/             # Request/Response DTOs with validation
├── enums/           # Role, TransactionCategory, TransactionStatus, etc.
├── exception/       # Custom exceptions (BadRequest, NotFound, Forbidden, etc.)
├── model/           # JPA entities (User, Transaction, Merchant, SavingGoal)
├── repository/      # Spring Data JPA repositories
├── security/        # JwtAuthenticationFilter
├── service/         # Business logic services
└── util/            # JwtUtil
```
