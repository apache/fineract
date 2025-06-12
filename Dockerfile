# Dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy the built jar from Gradle output
COPY fineract-provider/build/libs/fineract-provider-0.0.0-360d2288.jar app.jar
EXPOSE 8443

ENTRYPOINT ["java", "-jar", "app.jar"]
