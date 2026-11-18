# Builds the backend in two stages: first compile with a full JDK, then run with a small JRE only.

# ---- Stage 1: build the jar ----
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copy the Maven wrapper and pom.xml first and download all dependencies.
# Docker caches this step, so it only runs again when pom.xml changes.
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

# Now copy the source code and build the jar (the tests already run in CI, so skip them here)
COPY src src
RUN ./mvnw -B -q package -DskipTests

# ---- Stage 2: run the jar ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Run as a normal user instead of root, so a bug in the app can't take over the whole container
RUN addgroup -S app && adduser -S app -G app
USER app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
