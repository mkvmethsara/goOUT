# 🟢 Stage 1: Build the Spring Boot App using Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy your project files into the Docker container
COPY pom.xml .
COPY src ./src

# Run the build command (Skipping tests to make deployment faster)
RUN mvn clean package -DskipTests

# 🔵 Stage 2: Run the App using a lightweight Java environment
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the compiled .jar file from Stage 1
COPY --from=build /app/target/*.jar app.jar

# Expose port 8080 so the internet can reach it
EXPOSE 8080

# The command to start the server
ENTRYPOINT ["java", "-jar", "app.jar"]