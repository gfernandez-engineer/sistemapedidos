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

COPY common/src common/src
RUN mvn -N install -DskipTests -B -q && \
    mvn -pl common install -DskipTests -B -q

COPY catalog-service/src catalog-service/src
RUN mvn -pl catalog-service package -DskipTests -B -q

FROM eclipse-temurin:25-jre
WORKDIR /app

RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser

COPY --from=builder /app/catalog-service/target/*.jar app.jar

RUN chown -R appuser:appgroup /app
USER appuser

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]
