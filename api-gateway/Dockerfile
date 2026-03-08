FROM maven:3.9-eclipse-temurin-25 AS builder
WORKDIR /app

COPY pom.xml .
COPY common/pom.xml common/
COPY users-service/pom.xml users-service/
COPY orders-service/pom.xml orders-service/
COPY catalog-service/pom.xml catalog-service/
COPY payments-service/pom.xml payments-service/
COPY deliveries-service/pom.xml deliveries-service/
COPY api-gateway/pom.xml api-gateway/

# Install root POM (api-gateway does not depend on common)
RUN mvn -N install -DskipTests -B -q

COPY api-gateway/src api-gateway/src
RUN mvn -pl api-gateway package -DskipTests -B -q

FROM eclipse-temurin:25-jre
WORKDIR /app

RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser

COPY --from=builder /app/api-gateway/target/*.jar app.jar

RUN chown -R appuser:appgroup /app
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
