# Use the Liberica JDK 17 image as a parent image
FROM bellsoft/liberica-runtime-container:jdk-17-stream-musl

# Set the working directory to /app
WORKDIR /app

# Copy the current directory contents into the container at /app
COPY . /app

RUN apk add --no-cache wget && \
    wget https://jdbc.postgresql.org/download/postgresql-42.7.0.jar &&  \
    apk del wget

# Build the JAR file
RUN ./gradlew clean bootJar

# Make port 8080 available to the world outside this container
EXPOSE 8080

# Run the application when the container launches
CMD ["java", "-Dloader.path=.", "-jar", "fineract-provider/build/libs/fineract-provider-0.1.0-SNAPSHOT.jar"]
