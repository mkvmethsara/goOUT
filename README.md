🌍 GoOUT - Backend API (Spring Boot)

Welcome to the backend repository for GoOUT, a collaborative travel planning and expense settlement platform. This RESTful API serves as the core engine for the application, handling everything from secure user authentication to complex debt-settlement mathematics.

👥 Team & Contributions (Pod 2 - Backend)

This API was built collaboratively using a strictly organized branch-and-merge Git workflow. Below is the breakdown of individual contributions:

👑 Methsara (Tech Lead / Core Architecture)

Engineered the overall Spring Boot architecture and MongoDB database schemas.

Implemented Spring Security, BCrypt password hashing, and the JWT (JSON Web Token) authentication flow.

Built the mock OTP Email Verification system.

Programmed the complex Settlement Math Engine (ExpenseService) to automatically calculate group debts.

Designed the GlobalExceptionHandler (RFC 7807 Standard) for unified frontend error handling.

Conducted code reviews, branch resets, and final merge conflict resolutions.

💻 Hashen (Trip Core API)

Built the core Trip CRUD operations (TripController).

Engineered the MongoDB custom queries (TripRepository) for the Public Discovery Feed.

Implemented the "My Trips" dashboard logic, filtering trips by ownerId and visibility.

💻 Udai (Expense API Routing)

Developed the RESTful endpoints for the Expense module (ExpenseController).

Integrated the Tech Lead's Math Engine to expose the Dashboard and Ledger calculations to the React frontend.

Handled JSON payload mappings for receipt creation.

💻 Dewnaka (Trip Participant Management)

Developed the logic for users joining existing trips (TripService).

Managed MongoDB array operations to safely push User IDs into the Trip participantIds array.

Built the endpoints to fetch and display active trip members.

🏗️ Architecture & Best Practices

To ensure enterprise-level code quality, our team adhered to the following standards:

Service-Controller-Repository Pattern: Strict separation of concerns. Controllers handle HTTP, Services handle business logic, and Repositories handle MongoDB.

Data Transfer Objects (DTOs): Prevented over-posting and secured sensitive data (like removing passwords from JSON responses) using custom DTOs.

Stateless Authentication: Session management is strictly stateless, relying entirely on Bearer Tokens for route protection.

CORS Configuration: Explicitly configured WebMvcConfigurer to allow preflight OPTIONS requests and seamless integration with the Vite/React frontend.

🚀 How to Run Locally

1. Prerequisites

Java Development Kit (JDK) 17 or higher.

Maven installed.

MongoDB (Local instance running on port 27017, or a MongoDB Atlas URI).

2. Environment Configuration

Navigate to src/main/resources/ and ensure your application.properties contains the correct database connection:

# MongoDB Configuration
spring.data.mongodb.uri=mongodb://localhost:27017
spring.data.mongodb.database=goout_db

# Multipart Form Data (For Image Uploads)
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB


3. Build and Run

Open your terminal in the root directory (where the pom.xml is located) and run:

mvn clean install
mvn spring-boot:run


The server will initialize on http://localhost:8080.

🧪 API Testing (Postman)

The complete suite of API endpoints has been fully tested and documented.
To evaluate the endpoints:

Locate the GoOUT_API_Final_Project.json file included in the root directory (or docs folder) of this repository.

Open Postman and click Import.

Upload the .json file to view all configured requests, including saved example responses for 200 OK and 404 Not Found scenarios.

Note: You must run the 1. Register User and Verify OTP requests first to generate a Bearer Token for the protected routes!