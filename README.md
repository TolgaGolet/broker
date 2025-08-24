# Broker Backend Application

A comprehensive backend application for a brokerage firm built with Spring Boot, providing secure APIs for customer
management, asset management, and order processing.

## 🎯 Overview

This broker backend application provides a complete trading platform backend with user authentication, role-based
authorization, asset management, and order processing capabilities. The application is designed for brokerage firms to
manage their customers, assets, and trading orders efficiently.

## ✨ Features

- **User Authentication & Authorization**
    - JWT-based authentication
    - Role-based access control (ADMIN, USER)
    - User registration and login
    - Token refresh functionality

- **Customer Management**
    - Customer registration and management
    - Role assignment and management
    - Customer information retrieval

- **Asset Management**
    - Create and manage trading assets
    - View customer assets with filtering
    - Asset deletion (Admin only)

- **Order Management**
    - Create buy/sell orders
    - View customer orders with filtering
    - Cancel pending orders
    - Match orders (Admin only)

- **Additional Features**
    - Audit trail for all entities
    - Pagination support
    - Input validation
    - Comprehensive error handling
    - OpenAPI/Swagger documentation

## 🛠 Technology Stack

- **Framework**: Spring Boot 3.5.5
- **Language**: Java 21
- **Database**: H2 Database (in-memory for development/testing, persistent mode or other databases can be used easily)
- **Security**: Spring Security with JWT
- **Documentation**: OpenAPI 3 (Swagger)
- **Build Tool**: Maven
- **Additional Libraries**:
    - Lombok (boilerplate code reduction)
    - MapStruct (object mapping)
    - JJWT (JWT handling)

## 📋 Prerequisites

Before running this application, make sure you have the following installed:

- **Java Development Kit (JDK) 21** or higher
- **Maven 3.6+** (or use the included Maven wrapper)
- **Git** (for cloning the repository)

## ⚙️ Configuration

➡️**The application supports multiple configuration profiles. It uses some default values. You can set the values for
yourself before running the application:**

### Default Profile (`application.properties`)

- Uses the `test` profile by default
- Suitable for development

### Test Profile (`application-test.properties`)

- H2 in-memory database
- JWT security enabled
- H2 console accessible at: http://localhost:8080/h2-console

### Disabled Security Profile (`application-disabled-security.properties`)

- Security features disabled
- Useful for testing APIs without authentication

### Database Configuration (H2)

**Default Connection Details:**

- **H2 Console**: http://localhost:8080/h2-console
- **JDBC URL**: `jdbc:h2:mem:testdb`
- **Username**: `sa`
- **Password**: `password`

## 🚀 Installation & Setup & Running the Application

### Step 1: Clone the Repository and Navigate to the Project Directory

```bash
git clone https://github.com/TolgaGolet/broker.git
```

```bash
cd broker
```

### Step 2: Build the Application

```bash
./mvnw clean install
```

### Step 3: Run the Application

```bash
./mvnw spring-boot:run
```

- The application will start on **http://localhost:8080** by default.
- Go to **http://localhost:8080/swagger-ui/index.html** for Swagger UI.

## 📚 API Documentation

Once the application is running, you can access the interactive API documentation:

- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

## 🔐 Default Admin Access

The application automatically creates a default admin user on startup:

- **Username**: `admin`
- **Password**: `password`
- **Role**: `ADMIN`

### First-Time Setup

1. Start the application
2. Use the admin credentials to authenticate via `/api/v1/auth/authenticate`
3. Use the returned JWT token for subsequent API calls

---
**Note**: This application includes an `AdminCustomerInitializer` component that automatically creates the admin user
and role on startup. This is intended for development and testing purposes only and should be removed or modified for
production deployment.

## 🔒 Security

### JWT Authentication

- Access tokens expire in 60 minutes
- Refresh tokens expire in 48 hours (2880 minutes)
- All API endpoints require authentication except registration and login

### Role-Based Authorization

- **ADMIN**: Full access to all endpoints
- **USER**: Limited access to their own data and order creation
- Other roles can also be created on demand

### API Security Headers

All protected endpoints require:

```
Authorization: Bearer <jwt-token>
```
