# Use the Liberica JDK 17 image as a parent image
FROM bellsoft/liberica-runtime-container:jdk-21-stream-musl

# Set the working directory to /app
WORKDIR /app

# Copy the current directory contents into the container at /app
COPY . /app

# Download MariaDB JDBC driver
RUN apk add --no-cache wget && \
    wget https://downloads.mariadb.com/Connectors/java/connector-java-2.7.5/mariadb-java-client-2.7.5.jar && \
    apk del wget

# Build the JAR file
RUN ./gradlew clean bootJar

# Make port 8080 available to the world outside this container
EXPOSE 8443

# Run the application when the container launches
CMD ["java", "-Dloader.path=.", "-jar", "fineract-provider/build/libs/fineract-provider.jar"]
