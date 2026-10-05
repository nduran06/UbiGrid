# Build stage: compile the Spring Boot fat jar with the project's own Maven
# wrapper, so the image always builds with the exact Maven/plugin versions
# committed to the repo.
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

COPY . .
RUN chmod +x mvnw && ./mvnw -q -B clean package -DskipTests

# Runtime stage: JRE only, plus curl for the container HEALTHCHECK against
# Spring Boot Actuator.
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=5s --start-period=40s --retries=6 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
