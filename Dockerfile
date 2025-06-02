FROM openjdk:21-jdk-slim

# Install required packages including Gradle
RUN apt-get update && apt-get install -y \
    curl \
    wget \
    unzip \
    postgresql-client \
    redis-tools \
    && rm -rf /var/lib/apt/lists/*

# Install Gradle
RUN wget https://services.gradle.org/distributions/gradle-8.10.2-bin.zip -P /tmp \
    && unzip -d /opt/gradle /tmp/gradle-8.10.2-bin.zip \
    && rm /tmp/gradle-8.10.2-bin.zip

ENV GRADLE_HOME=/opt/gradle/gradle-8.10.2
ENV PATH=${GRADLE_HOME}/bin:${PATH}

WORKDIR /app

# Copy build files
COPY build.gradle .
COPY settings.gradle .
COPY gradle.properties .
COPY lombok.config .

# Copy source code
COPY buildSrc/ buildSrc/
COPY config/ config/
COPY custom/ custom/
COPY fineract-accounting/ fineract-accounting/
COPY fineract-avro-schemas/ fineract-avro-schemas/
COPY fineract-branch/ fineract-branch/
COPY fineract-charge/ fineract-charge/
COPY fineract-client/ fineract-client/
COPY fineract-command/ fineract-command/
COPY fineract-core/ fineract-core/
COPY fineract-db/ fineract-db/
COPY fineract-document/ fineract-document/
COPY fineract-investor/ fineract-investor/
COPY fineract-loan/ fineract-loan/
COPY fineract-progressive-loan/ fineract-progressive-loan/
COPY fineract-progressive-loan-embeddable-schedule-generator/ fineract-progressive-loan-embeddable-schedule-generator/
COPY fineract-provider/ fineract-provider/
COPY fineract-rates/ fineract-rates/
COPY fineract-report/ fineract-report/
COPY fineract-savings/ fineract-savings/
COPY fineract-tax/ fineract-tax/
COPY fineract-war/ fineract-war/

# Build the application
RUN gradle clean bootJar -x test --no-daemon

# Expose port
EXPOSE 8080

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=basicauth
ENV JAVA_OPTS="-Xmx2g -Xms1g"

# Run the application
CMD ["java", "-jar", "-Dspring.profiles.active=basicauth", "fineract-provider/build/libs/fineract-provider.jar"]
