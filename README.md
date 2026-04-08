# 🚀 EasyCommerce — Spring Boot E-Commerce Backend  
  
> Production-ready e-commerce backend built with Java 21, Spring Boot and JWT authentication.  
> Clean architecture, real business logic, and a complete purchase flow — not just a CRUD demo.  
  
[![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square)](https://openjdk.org/)  
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?style=flat-square)](https://spring.io/projects/spring-boot)  
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-blue?style=flat-square)](https://www.postgresql.org/)  
[![License](https://img.shields.io/badge/License-MIT-lightgrey?style=flat-square)](LICENSE)  
  
**👤 Built by [Fabrizio Armada](https://www.linkedin.com/in/fabrizio-armada) — Open to remote backend opportunities** ---  
  
## 📋 Table of Contents  
  
- [Overview](#overview)  
- [Live Demo](#-live-demo)
- [Features](#features)  
- [Tech Stack](#tech-stack)  
- [API Endpoints](#api-endpoints)  
- [Project Structure](#-project-structure)  
- [Database Design](#-database-design-entity-relationship)
- [How to Run](#-getting-started)  
- [Authentication Flow](#-authentication-flow)  
- [Architecture](#-architecture--components)  
  
---  
  
## Overview  

EasyCommerce simulates a real-world e-commerce backend system. It goes beyond basic CRUD by implementing actual business logic: a persistent cart, a checkout process with price snapshots, order tracking, and role-based access control.  
  
Built as a portfolio project to demonstrate production-grade backend development with Java and Spring Boot.  

---

## 🌐 Live Demo
You can explore the API instantly without running anything locally.

* **Swagger UI (Render):** [https://easyecommerce.onrender.com/swagger-ui.html](https://easyecommerce.onrender.com/swagger-ui.html)
📖
*(Note: Since this is hosted on a free tier, the first request might take a few seconds to wake up the server).*

---
  
## Features  
  
### Authentication & Security  
- JWT-based login and registration  
- Role-based access control — `USER` and `ADMIN` roles  
- Spring Security 6 with `@PreAuthorize` guards  
- Password hashing with BCrypt  
  
### Cart System  
- Add, update, and remove products  
- Persistent cart per user  
- Cart cleared automatically on checkout  
  
### Checkout & Orders  
- Checkout creates an order from the active cart  
- `priceAtPurchase` snapshot — price is locked at time of purchase  
- Order history per user  
- OrderItem tracking with quantity and price  
  
### Product & Categories  
- Full CRUD for products (ADMIN only)  
- Category management  
- Product filtering by category  
  
### User Profile  
- Auth-me endpoint to retrieve current user  
- Address management (street, city, postal code)  
  
### Developer Experience  
- Swagger / OpenAPI documentation  
- Global exception handling with proper HTTP status codes  
- DTO pattern with MapStruct mapping  
- Clean layered architecture  
  
---  
  
## Tech Stack  
  
| Technology | Version |  
|---|---|  
| Java | 21 (LTS) |  
| Spring Boot | 3.x |  
| Spring Security | 6.x |  
| JPA / Hibernate | Latest |  
| PostgreSQL | 15+ |  
| MapStruct | 1.5+ |  
| Swagger / OpenAPI | 3.0 |  
  
---  
  
## API Endpoints  
  
### Auth  
| Method | Endpoint | Access |  
|---|---|---|  
| POST | `/auth/register` | Public |  
| POST | `/auth/login` | Public |  
| GET | `/auth/me` | USER |  
  
### Products  
| Method | Endpoint | Access |  
|---|---|---|  
| GET | `/products` | Public |  
| GET | `/products/{id}` | Public |  
| POST | `/products` | ADMIN |  
| PUT | `/products/{id}` | ADMIN |  
| DELETE | `/products/{id}` | ADMIN |  
  
### Categories  
| Method | Endpoint | Access |  
|---|---|---|  
| GET | `/categories` | Public |  
| POST | `/categories` | ADMIN |  
| PUT | `/categories/{id}` | ADMIN |  
| DELETE | `/categories/{id}` | ADMIN |  
  
### Cart  
| Method | Endpoint | Access |  
|---|---|---|  
| GET | `/cart` | USER |  
| POST | `/cart/items` | USER |  
| PUT | `/cart/items/{id}` | USER |  
| DELETE | `/cart/items/{id}` | USER |  
  
### Checkout & Orders  
| Method | Endpoint | Access |  
|---|---|---|  
| POST | `/checkout` | USER |  
| GET | `/orders` | USER |  
| GET | `/orders/{id}` | USER |  
  
---  
  
## 📂 Project Structure

```text
src/main/java/com/easycommerce/
├── 🎮 controller/      # REST endpoints & API entry points
├── ⚙️  service/         # Core business logic & validations
├── 🗄️  repository/      # Data access layer (Spring Data JPA)
├── 📦 model/           # JPA entities & database schema
│   ├── User.java
│   ├── Product.java
│   ├── Category.java
│   └── ... (Orders, Cart, Items)
├── ✉️  dto/             # Request & Response Data Transfer Objects
├── 🛡️  security/        # JWT filters, Auth config & Security filters
├── ⚠️  exception/       # Global error handling & custom exceptions
└── 🔄  mapper/          # MapStruct interface definitions
```

## 🗄️ Database Design (Entity-Relationship)
Understanding the data flow is key. The system handles persistent carts and converts them into orders with historical price snapshots.

User (1) ↔ (1) Cart: Every user has one persistent shopping cart.

Cart (1) ↔ (N) CartItem: Items in the cart reference products and quantities.

Order (1) ↔ (N) OrderItem: When checking out, cart items are converted to order items.

Price Snapshot: OrderItem stores the priceAtPurchase to ensure historical accuracy if the Product price changes later.

## 🚀 Getting Started (Manual) 🛠️

Requirements: Java 21+ and PostgreSQL running locally.

### Clone the repo
```bash
git clone https://github.com/fabrizzio323/easyecommerce.git
cd easyecommerce
```

### Create the database
```bash
psql -U postgres -c "CREATE DATABASE easyecommerce;"
```

### Configure environment variables
This project reads the database configuration from environment variables (see `src/main/resources/application.yml`):

```bash
DB_NAME=jdbc:postgresql://localhost:5432/easyecommerce
DB_USER=postgres
DB_PASSWORD=yourpassword
```

(Optional) You can also override the server port (default is `8081`):

```bash
PORT=8081
```

### Run the application
```bash
./mvnw spring-boot:run
```
## 🔗 Access Points
API Base URL: http://localhost:8081

Interactive Documentation: Swagger UI 📖

## 🔐 Authentication Flow
Register: POST /auth/register → Create your account.

Login: POST /auth/login → Receive your JWT token.

Authorize: Use Authorization: Bearer YOUR_TOKEN in all protected requests.

## 🏗️ Architecture & Components

Request → Controller → Service → Repository → PostgreSQL

↓

DTOs (MapStruct)

↓

JWT Filter (Spring Security)

Controller: Handles HTTP requests and delegates to the service layer.

Service: Contains all core business logic and validations.

Repository: Clean data access using Spring Data JPA.

DTO: Ensures a strict separation between API contracts and DB entities.

Security: Full JWT filter chain with role-based access control (RBAC).

## 📩 Contact
Fabrizio Armada — Backend Java Developer based in Jujuy, Argentina.

## 🌐 Open to remote opportunities.

LinkedIn: linkedin.com/in/fabrizio3

GitHub: github.com/fabrizzio323

Gmail: fabrizioarmada3@gmail.com
