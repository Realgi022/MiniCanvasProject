# --- Stage 1: Build ---
# Using JDK 21 on Alpine Linux for a smaller footprint
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# 1. Copy the Gradle wrapper and configuration files first
# This allows Docker to cache the 'dependencies' layer
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# 2. Grant execution permission and download dependencies
# The --no-daemon flag is best for Docker environments
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon

# 3. Copy the actual source code and build the application
COPY src src
RUN ./gradlew bootJar --no-daemon

# --- Stage 2: Runtime ---
# We switch to the JRE (Runtime) which is much smaller than the SDK
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 4. Copy the JAR from the build stage
# Note: Spring Boot/Gradle usually puts the JAR in build/libs/
COPY --from=build /app/build/libs/*.jar app.jar

# 5. Expose the default Spring Boot port
EXPOSE 8081

# 6. Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]