#  🛒 Order Service

## 📖 Overview

 This project is developed as part of an E-commerce Order Processing System.
The goal is to design, implement, and test a backend service in Java that efficiently handles order processing — including order creation, retrieval, status updates, listing, and cancellation.

It simulates a real-world scenario where customers can place and track their orders, with background jobs automatically updating order statuses at regular intervals.

# 📘 Assumptions

## Before developing this service, the following assumptions were made:

### 1.Customer Management
    1.Customer-related operations are managed by a third-party Customer Service.
    2.Therefore, this service does not include customer management functionality. 

### 2.Authentication and Authorization
    1.Authentication is handled by a dedicated Authentication Service.
    2.It is assumed that all requests to this service are made by authenticated users.

### 3.API Gateway Responsibility
    1.All incoming requests are routed through an API Gateway.
    2.The gateway will include the header X-Customer-Id in every request, representing the authenticated customer.

### 4.Inventory Management
    1.Product inventory and stock management are handled by a separate Inventory Service.
    2.It is assumed that inventory operations are managed externally.

### 5.Product Data Handling
    1.When an order is placed, the service will store raw product details as part of the order record for simplicity.
    2.In a real-world scenario, product data would be fetched dynamically from the Inventory Service to maintain accuracy and consistency.

# 📂 How to Clone the Repository

To clone this repository locally, follow these steps:
```shell
# Clone the repository using HTTPS
git clone https://code.techverito.com/kpatel/order-service.git

# Navigate into the project directory
cd order-service

# Check the branch (optional)
git branch -a
```
# 📂 How to Run the Project
## 1. Prerequisites
    Make sure the following tools are installed:

    1.Java 17 or higher
    2.Maven 3.8+
    3.Git
    4.Optional: Docker

    If you don’t have Docker installed, follow these steps:
    Windows / Mac: Download Docker Desktop
    Linux: Install via your package manager:

##  2. Set up PostgreSQL using Docker
    We recommend running the database in Docker for simplicity.
    Step 1: Pull the PostgreSQL Image
    docker pull postgres:15

    Step 2: Run the PostgreSQL Container
    h
    docker run -d \
    --name order-service-db \
    -e POSTGRES_USER=postgres \
    -e POSTGRES_PASSWORD=postgres \
    -e POSTGRES_DB=order_processing_db \
    -p 5432:5432 \
    postgres:15```


## 3. Build and Run the Application
### Build the project
mvn clean install

### Run using Maven
mvn spring-boot:run

The service will start at http://localhost:8080
.

## 4. Access Swagger UI and OpenAPI

Swagger UI: http://localhost:8080/swagger-ui.html

OpenAPI JSON: http://localhost:8080/v3/api-docs



