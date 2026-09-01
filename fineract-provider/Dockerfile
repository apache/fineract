# ============================================================================
# Dockerfile - Fineract Qüida
# SoW 4.0 - Proyecto 3 - Build P3.1
#
# Base: eclipse-temurin JRE 25 (Alpine)
# Usuario: no-root (uid 1000)
# Puerto: 8080 (Spring Boot embedded)
# ============================================================================
FROM eclipse-temurin:25-jre-alpine

# Metadata SCARE
LABEL org.opencontainers.image.title="fineract"
LABEL org.opencontainers.image.vendor="Quida CF"
LABEL org.opencontainers.image.description="Apache Fineract build para Quida"
LABEL org.opencontainers.image.source="https://github.com/jgaitan-quida/quida-core-fineract"

# Crear usuario no-root
RUN addgroup -g 1000 fineract && \
    adduser -D -u 1000 -G fineract fineract

WORKDIR /app

# Copiar JAR generado por Gradle
COPY --chown=fineract:fineract fineract-provider/build/libs/fineract-provider-*.jar app.jar

USER fineract

EXPOSE 8080

# Java startup con JVM tunning
ENV JAVA_TOOL_OPTIONS="-Xmx1g -Xms512m -XX:+UseContainerSupport"

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
