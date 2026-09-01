# ============================================================================
# Dockerfile - Fineract Quida
# SoW 4.0 - Proyecto 3 - Build P3.1
# Contexto: preparado por P3.1 workflow (JAR ya copiado como app.jar)
# ============================================================================
FROM eclipse-temurin:25-jre-alpine

LABEL org.opencontainers.image.title="fineract"
LABEL org.opencontainers.image.vendor="Quida CF"
LABEL org.opencontainers.image.description="Apache Fineract build para Quida"
LABEL org.opencontainers.image.source="https://github.com/jgaitan-quida/quida-core-fineract"

RUN addgroup -g 1000 fineract && \
    adduser -D -u 1000 -G fineract fineract

WORKDIR /app

COPY --chown=fineract:fineract app.jar app.jar

USER fineract

EXPOSE 8080

ENV JAVA_TOOL_OPTIONS="-Xmx1g -Xms512m -XX:+UseContainerSupport"

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
