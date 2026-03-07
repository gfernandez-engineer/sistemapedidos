# Stage 1: Build
FROM eclipse-temurin:25-jdk AS builder

WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline -B || true

COPY src ./src

RUN ./mvnw clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:25-jre

WORKDIR /app

RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser

COPY --from=builder /app/target/*.jar app.jar

RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]
