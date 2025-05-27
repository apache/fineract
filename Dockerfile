FROM openjdk:11-jdk-slim

WORKDIR /fineract

RUN apt-get update && apt-get install -y git maven

RUN git clone https://github.com/apache/fineract.git . \
  && ./gradlew build -x test

EXPOSE 8443

CMD ["./gradlew", "bootRun"]
