# ---------- Stage 1: build the JAR ----------
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app

# Download dependencies first so they are cached between builds
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Compile and package (tests run in CI / locally with ./mvnw test)
COPY src ./src
RUN mvn -B -q package -DskipTests

# ---------- Stage 2: run the application ----------
FROM eclipse-temurin:25-jre-noble
WORKDIR /app

# Run as a non-root user
RUN groupadd --system spring && useradd --system --gid spring spring \
    && mkdir -p /app/uploads && chown -R spring:spring /app
USER spring

COPY --from=build --chown=spring:spring /app/target/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod \
    PORT=8080 \
    JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
