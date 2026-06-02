# Project CNPM - Backend

This repository contains the backend service for the CNPM project, built with a modern Java Spring Boot stack. It provides robust RESTful APIs, secure authentication, media management, and advanced AI capabilities using Spring AI.

## 🚀 Technologies Used

- **Core Framework**: Java 17, Spring Boot 3.5
- **Database & ORM**: MySQL, Spring Data JPA
- **Security**: Spring Security, JWT (JSON Web Tokens)
- **AI Integration**: Spring AI (Google GenAI, Qdrant Vector Store for embeddings)
- **Media Storage**: Cloudinary integration
- **Utilities**: Lombok (boilerplate reduction), Spring Boot Mail (email services)
- **Build Tool**: Maven

## 📁 Project Structure

The main application source code is located within the `demo` directory. 
- `src/main/java`: Contains the core application logic (Controllers, Services, Repositories, Security Configs).
- `src/main/resources`: Contains application configuration files (`application.properties` / `application.yml`).

## 🛠️ Getting Started

### Prerequisites
- **JDK 17** installed on your machine.
- **Maven** (or use the provided Maven wrapper `mvnw`).
- **MySQL Server** running locally or remotely.
- API Keys for **Cloudinary**, **Google GenAI**, and a **Qdrant** instance.

### Installation & Setup

1. Navigate to the project directory:
   ```bash
   cd demo
   ```
2. Configure your environment variables. Update your `src/main/resources/application.properties` with the necessary database credentials, JWT secret, Cloudinary config, and API keys.
3. Clean and build the project:
   ```bash
   ./mvnw clean install
   ```
4. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

The server will start (default port is usually `8080`).

## 🛡️ Security
The application uses stateless session management with Spring Security and JWT. Ensure that your JWT secret is kept secure and not exposed in public repositories.
