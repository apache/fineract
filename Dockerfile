# 🧱 Builder Stage: Build Fineract JAR
FROM openjdk:17-jdk-slim as builder

# Set working directory
WORKDIR /app

# Install build dependencies
RUN apt-get update && \
    apt-get install -y git curl unzip wget gnupg && \
    rm -rf /var/lib/apt/lists/*

# Install Gradle manually
ENV GRADLE_VERSION=7.6.2
RUN wget https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip && \
    unzip gradle-${GRADLE_VERSION}-bin.zip -d /opt && \
    ln -s /opt/gradle-${GRADLE_VERSION}/bin/gradle /usr/bin/gradle && \
    rm gradle-${GRADLE_VERSION}-bin.zip

# Copy the source code
COPY . .

# Make Gradle wrapper executable
RUN chmod +x gradlew

# 🏗️ Build Fineract
RUN ./gradlew :fineract-provider:bootJar -x test -x check -x asciidoctor -x asciidoctorPdf

# 🚀 Runtime Stage: Run the built JAR
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy built JAR from builder
COPY --from=builder /app/fineract-provider/build/libs/fineract-provider.jar .

EXPOSE 8443

# Start Fineract
CMD ["java", "-jar", "fineract-provider.jar"]
