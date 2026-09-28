# Fineract image aligned with the epara microservice pattern.
# Pin both images by digest in practice (Renovate/Dependabot keep them fresh).
FROM eclipse-temurin:17-jdk-alpine-3.23 AS builder
# The pipeline stages the boot jar as app.jar (skips the -plain jar).
ARG JAR_FILE=app.jar
WORKDIR /build
COPY ${JAR_FILE} application.jar
COPY agent/applicationinsights-agent-3.4.14.jar applicationinsights-agent.jar

# Spring Boot 3.3+ syntax. On older Boot versions use:
#   java -Djarmode=layertools -jar application.jar extract --destination extracted
RUN java -Djarmode=tools -jar application.jar extract --layers --launcher --destination extracted

FROM eclipse-temurin:17-jre-alpine-3.23

ENV TZ=Etc/UTC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone \
 && addgroup -S -g 10001 fineract \
 && adduser -S -D -H -u 10001 -G fineract fineract

# See notes: check whether the JVM actually uses this bundle.
ADD ca-bundle.crt /etc/pki/tls/certs/ca-bundle.crt

WORKDIR /app
COPY --from=builder /build/extracted/dependencies/ ./
COPY --from=builder /build/extracted/spring-boot-loader/ ./
COPY --from=builder /build/extracted/snapshot-dependencies/ ./
COPY --from=builder /build/extracted/application/ ./
COPY --from=builder /build/applicationinsights-agent.jar ./

ENV APPLICATIONINSIGHTS_ROLE_NAME="fineract" \
    JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

EXPOSE 8080
USER 10001

# exec makes java PID 1 so SIGTERM from Kubernetes reaches it.
# Debugging (JDWP) is added only in non-prod, via JAVA_TOOL_OPTIONS in the deployment.
ENTRYPOINT ["sh", "-c", "exec java -javaagent:/app/applicationinsights-agent.jar $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
