# === STAGE 1: Build the application ===
# Pull a generic modern Maven image
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy the pom.xml and download dependencies 
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code and build the fat JAR
# Note: Maven 3.9+ can cross-compile to Java 25 if specified in pom.xml
COPY src ./src
RUN mvn clean package -DskipTests

# === STAGE 2: Run the application ===
# Pull the actual native Java 25 runtime environment for execution
FROM eclipse-temurin:25-jre-jammy
WORKDIR /app

# Copy the compiled JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port 8080
EXPOSE 8080

# Execute the application under Java 25
ENTRYPOINT ["java", "-jar", "app.jar"]
