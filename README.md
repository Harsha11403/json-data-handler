# JSON Data Handler Application

## Overview

This Spring Boot application provides a backend service for handling and manipulating JSON data. It allows users to store JSON records associated with specific datasets, and then perform operations like grouping and sorting these records based on their content.

The project follows a Test-Driven Development (TDD) approach, ensuring robust and well-tested functionality for core business logic.

## Features

* **JSON Record Storage**: Store arbitrary JSON documents in a PostgreSQL database, linked to a specific `datasetName`.
* **Dynamic Grouping**: Group JSON records within a dataset by a specified field. Supports grouping by string, number, and boolean fields.
* **Dynamic Sorting**: Sort JSON records within a dataset by a specified field in either ascending or descending order. Supports sorting by string, number, and boolean fields.
* **RESTful API**: Exposes endpoints for data insertion, grouping, and sorting.
* **Swagger UI Integration**: Provides interactive API documentation for easy testing and understanding of the endpoints.

## Technology Stack

* **Spring Boot**: 3.5.3 (or 3.3.13 as observed in logs, but pom.xml specifies 3.5.3)
* **Java**: 21
* **Maven**: Build automation tool
* **PostgreSQL**: Relational database for storing JSON records (using `JSONB` column type).
* **Hibernate / Spring Data JPA**: ORM for database interaction.
* **Hypersistence Utils Hibernate**: For mapping `JsonNode` directly to PostgreSQL `JSONB`.
* **Jackson**: For JSON processing (`JsonNode`).
* **Lombok**: Reduces boilerplate code (getters, setters, constructors).
* **Springdoc OpenAPI**: For generating interactive API documentation (Swagger UI).
* **JUnit 5 & Mockito**: For unit and integration testing following TDD principles.

## Getting Started

### Prerequisites

* Java Development Kit (JDK) 21 installed.
* Maven installed.
* PostgreSQL database server running.
* A database created for the application (e.g., `jsondata_manager`).
* A PostgreSQL user with permissions to access this database (e.g., `postgres` with password `Harsha114` as per `application.properties`).

### Setup and Running the Application

1.  **Clone the Repository **
    ```bash
    git clone https://github.com/Harsha11403/json-data-handler.git
    cd json-data-handler-app
    ```

2.  **Configure Database:**
    * Ensure your PostgreSQL server is running.
    * Create a database named `jsondata_manager`.
    * Verify the `spring.datasource.username` and `spring.datasource.password` in `src/main/resources/application.properties` match your PostgreSQL setup.
        ```properties
        spring.datasource.url=jdbc:postgresql://localhost:5432/jsondata_manager
        spring.datasource.username=postgres
        spring.datasource.password=Harsha114
        spring.jpa.hibernate.ddl-auto=update # This will create/update tables automatically
        ```

3.  **Build the Project:**
    Navigate to the project root directory in your terminal and run the Maven clean install command:
    ```bash
    mvn clean install
    ```
    This will compile the code, run tests, and package the application into a JAR file.

4.  **Run the Application:**
    ```bash
    mvn spring-boot:run
    ```
    The application will start on `http://localhost:8080`.

## API Documentation (Swagger UI)

Once the application is running, you can access the interactive API documentation at:
[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

Here you can explore the available endpoints, their request/response schemas, and even try them out directly.

## API Endpoints

### 1. Insert Record

* **URL:** `/api/dataset/{datasetName}/record`
* **Method:** `POST`
* **Path Variable:** `datasetName` (string) - The name of the dataset to which the record belongs.
* **Request Body:** `application/json` - The JSON record to be inserted.
    ```json
    {
        "id": 101,
        "product": "Laptop",
        "price": 1200.00,
        "details": {
            "brand": "TechCo",
            "model": "XPS-15"
        },
        "available": true
    }
    ```
* **Responses:**
    * `201 Created`: Record added successfully.
    * `400 Bad Request`: Invalid `datasetName` or empty `recordContent`.
    * `500 Internal Server Error`: An unexpected error occurred on the server.

### 2. Query Records (Group or Sort)

* **URL:** `/api/dataset/{datasetName}/query`
* **Method:** `GET`
* **Path Variable:** `datasetName` (string) - The name of the dataset to query.
* **Query Parameters (mutually exclusive):**
    * `groupBy` (string, optional): The field name in the JSON records by which to group. Example: `groupBy=product`
    * `sortBy` (string, optional): The field name in the JSON records by which to sort. Example: `sortBy=price`
    * `order` (string, optional, default: `asc`): Sort order. Valid values: `asc` (ascending) or `desc` (descending). Required if `sortBy` is used. Example: `order=desc`

* **Responses:**
    * `200 OK`: Returns grouped or sorted JSON records.
        * **For `groupBy` query:**
            ```json
            {
                "groupedRecords": {
                    "Laptop": [
                        {"id": 101, "product": "Laptop", "price": 1200.00},
                        {"id": 105, "product": "Laptop", "price": 1300.00}
                    ],
                    "Mouse": [
                        {"id": 102, "product": "Mouse", "price": 25.00}
                    ]
                }
            }
            ```
        * **For `sortBy` query:**
            ```json
            {
                "sortedRecords": [
                    {"id": 102, "product": "Mouse", "price": 25.00},
                    {"id": 101, "product": "Laptop", "price": 1200.00},
                    {"id": 105, "product": "Laptop", "price": 1300.00}
                ]
            }
            ```
    * `400 Bad Request`: Invalid parameters (e.g., both `groupBy` and `sortBy` provided, invalid `order` value, or no operation specified).
    * `500 Internal Server Error`: An unexpected error occurred on the server.

## Assumptions Taken

1.  **Database Availability**: It's assumed that a PostgreSQL instance is running and accessible at `localhost:5432` with the credentials provided in `application.properties`.
2.  **`ddl-auto=update`**: The `spring.jpa.hibernate.ddl-auto=update` setting is used for development purposes, which automatically creates/updates database tables based on JPA entities. **This setting is generally not recommended for production environments** as it can lead to data loss or unexpected schema changes. For production, manual schema migrations (e.g., Flyway, Liquibase) are preferred.
3.  **JSON Structure Consistency (for sorting/grouping)**:
    * For sorting/grouping operations, it's assumed that the specified `sortBy` or `groupBy` field will consistently hold the *same primitive data type* (string, number, or boolean) across all relevant JSON records in the dataset. While the service attempts basic type comparison, mixed types for the same field might lead to undefined or inconsistent sorting/grouping behavior (`toString()` comparison is used as a fallback for mixed types or complex objects).
    * Records missing the `sortBy` field will be treated as `null` for sorting purposes and will typically appear at the beginning when sorting in ascending order.
    * Records missing the `groupBy` field will be excluded from the grouping operation.
4.  **Error Handling**: Basic exception handling is in place within the controller, returning 500 status codes for unhandled exceptions. More fine-grained exception handling (e.g., custom exceptions for specific business rules) could be implemented for a production system.
5.  **Performance**: The current `sortRecords` and `groupRecords` implementations retrieve *all* records for a given `datasetName` into memory before processing. For very large datasets, this could lead to performance issues or `OutOfMemoryError`. For production-grade applications with massive data, server-side sorting/grouping using database queries would be more efficient.
6.  **Security**: Basic security measures (like input validation for path/query params) are present. However, advanced security considerations (authentication, authorization, SQL injection prevention beyond JPA's capabilities, robust input sanitization for JSON content) are not explicitly detailed or implemented in this basic setup.
7.  **`ObjectMapper` Usage**: An `ObjectMapper` instance is used directly in the `RecordService`. For complex scenarios, it might be beneficial to configure it with specific serialization/deserialization features via a Spring `@Bean`.
8.  **Spring Boot Version**: While `pom.xml` indicates 3.5.3, build logs sometimes show 3.3.13 due to dependency resolution. The application is expected to work correctly with compatible versions within the Spring Boot 3.x line.

---
