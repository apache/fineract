FROM openjdk:21-jdk-slim

WORKDIR /app

# Install dependencies
RUN apt-get update && \
    apt-get install -y curl unzip git wget gnupg && \
    rm -rf /var/lib/apt/lists/*

# Install Gradle
ENV GRADLE_VERSION=8.7
RUN wget https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip && \
    unzip gradle-${GRADLE_VERSION}-bin.zip -d /opt && \
    ln -s /opt/gradle-${GRADLE_VERSION}/bin/gradle /usr/bin/gradle && \
    rm gradle-${GRADLE_VERSION}-bin.zip

# Copy source code
COPY . .

# Make gradlew executable
RUN chmod +x gradlew || true

# ✅ Force rebuild to bypass Docker cache
ENV FORCE_REBUILD=1

# 🔧 Build the app (this creates the JAR in fineract-provider module)
RUN ./gradlew bootJar -x test

# Expose port
EXPOSE 8443

# ✅ Run the app (corrected JAR path)
CMD ["java", "-jar", "fineract-provider/build/libs/fineract-provider.jar"]
