FROM eclipse-temurin:25-jre
WORKDIR /app

RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser

COPY --chown=appuser:appgroup deliveries-service/target/*.jar app.jar

USER appuser

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]
