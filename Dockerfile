# === STAGE 1: Build using an official Java 25 Image ===
FROM eclipse-temurin:25-jdk-jammy AS build
WORKDIR /app

# Install Maven manually on top of Java 25
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Copy configuration files and download project dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy application source code and compile
COPY src ./src
RUN mvn clean package -DskipTests=true -Dmaven.test.skip=true

# === STAGE 2: Lightweight Production Runtime ===
FROM eclipse-temurin:25-jre-jammy
WORKDIR /app

# Safely extract the compiled fat JAR from Stage 1
COPY --from=build /app/target/*.jar app.jar

# Open standard backend communications port
EXPOSE 8080

# Spin up the compiled Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
