# 🍽️ Pararest — Restaurant Management System

A full-stack microservices application for restaurant management (hotel restaurant scenario), featuring order processing, inventory tracking, analytics, and payment handling.

---

## 🏗️ Architecture Overview

```
                        ┌─────────────┐
                        │   Frontend  │
                        │ React + TS  │
                        └──────┬──────┘
                               │
                        ┌──────▼──────┐
                        │  API Gateway│
                        └──────┬──────┘
                               │
        ┌──────────────────────┼──────────────────────┐
        │                      │                       │
┌───────▼──────┐    ┌──────────▼────────┐   ┌────────▼────────┐
│  AuthService │    │  CompositeService  │   │ AnalyticsService│
│  (JWT auth)  │    │  (reactive aggr.)  │   │ (reactive aggr.)│
└──────────────┘    └───────────────────┘   └─────────────────┘
        │                      │                       │
┌───────▼──────────────────────▼───────────────────────▼──────┐
│              Core Domain Services                             │
│  MenuService │ InventoryService │ UserService │ OrderService │
└──────────────────────────────────────┬───────────────────────┘
                                       │ Kafka Events
                              ┌────────▼────────┐
                              │  PaymentService  │
                              └─────────────────┘
```

---

## ⚙️ Tech Stack

| Layer | Technologies |
|---|---|
| **Backend** | Java 17, Spring Boot, Spring Cloud, Spring Security |
| **Reactive** | Spring WebFlux (analytics, composite aggregation) |
| **Messaging** | Apache Kafka |
| **Database** | PostgreSQL, Hibernate / Spring Data JPA |
| **Auth** | JWT (authservice) |
| **Frontend** | React, TypeScript |
| **DevOps** | Docker, Docker Compose, GitHub Actions CI/CD |

---

## 📦 Microservices

| Service | Description |
|---|---|
| `api-gateway` | Request routing and load balancing |
| `auth-service` | User registration, login, JWT token management |
| `user-service` | User profile data (name, hotel room number, phone, wallet) |
| `menu-service` | Restaurant menu storage and management |
| `inventory-service` | Ingredient stock and dish recipe management |
| `order-service` | Order creation and lifecycle management |
| `payment-service` | Payment processing and transaction records |
| `composite-service` | Reactive aggregation of menu, ingredients, and multi-service data |
| `analytics-service` | Reactive aggregation of supply costs and order revenue into profit reports |
| `common-lib` | Shared DTOs and common configurations across services |

---

## 🔄 Kafka Event Flow

Order processing is fully event-driven:

```
OrderService  ──[ORDER_CREATED]──►  PaymentService
                                         │
                                   Creates payment record
                                         │
                               ──[CHECK_BALANCE]──►  UserService
                                                          │
                                                    Validates wallet
                                                          │
                               ◄──[PAYMENT_RESULT]────────┘

Each service updates order status:
  CREATED → PAID / NOT_PAID / ERROR
```

---

## 🌱 Planned Features

- `notification-service` — email/push notifications (order status updates)
- `staff-service` — employee scheduling and payroll tracking
