# === STAGE 1: Build the application ===
# Using Maven with the Eclipse Temurin JDK 25 image
FROM maven:3.9.6-eclipse-temurin-25 AS build
WORKDIR /app

# Copy the pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code and build the fat JAR
COPY src ./src
RUN mvn clean package -DskipTests

# === STAGE 2: Run the application ===
# Using the matching lightweight Eclipse Temurin Java 25 JRE image
FROM eclipse-temurin:25-jre-jammy
WORKDIR /app

# Copy the compiled JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port 8080
EXPOSE 8080

# Execute the application
ENTRYPOINT ["java", "-jar", "app.jar"]
