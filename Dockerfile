FROM openjdk:21-jdk-slim

# Install required tools
RUN apt-get update && apt-get install -y git unzip curl

# Set working directory
WORKDIR /app

# Copy your forked repo files into the container
COPY . .

# Build the project (skip tests)
RUN ./gradlew build -x test

EXPOSE 8443

CMD ["./gradlew", "bootRun"]
