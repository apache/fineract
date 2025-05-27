# Use OpenJDK 21 as base
FROM openjdk:21-jdk-slim

# Set working directory
WORKDIR /app

# Install required packages: curl, unzip, git
RUN apt-get update && \
    apt-get install -y curl unzip git wget gnupg && \
    rm -rf /var/lib/apt/lists/*

# Install Gradle 8.7
ENV GRADLE_VERSION=8.7
RUN wget https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip && \
    unzip gradle-${GRADLE_VERSION}-bin.zip -d /opt && \
    ln -s /opt/gradle-${GRADLE_VERSION}/bin/gradle /usr/bin/gradle && \
    rm gradle-${GRADLE_VERSION}-bin.zip

# Copy your forked repo source code
COPY . .

# Make gradlew executable (fallback in case it exists)
RUN chmod +x gradlew || true

# Build the application (skip tests)
RUN ./gradlew bootJar -x test

# Expose the application port
EXPOSE 8443

# Optional healthcheck
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8443/fineract/actuator/health || exit 1

# Run the application
CMD ["java", "-jar", "build/libs/fineract-provider.jar"]
