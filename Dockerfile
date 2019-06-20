FROM bitnami/tomcat:7.0.94 as fineract

USER root
RUN apt-get update -qq && apt-get install -y git openjdk-8-jdk wget
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64/jre

RUN mkdir fineract
COPY . fineract

WORKDIR fineract
RUN sed -i 's/localhost/fineractmysql/' ./fineract-provider/build.gradle
RUN ./gradlew clean war
RUN mv build/libs/fineract-provider.war /opt/bitnami/tomcat/webapps

RUN keytool -genkey -keyalg RSA -alias tomcat -keystore /opt/bitnami/tomcat/tomcat.keystore -keypass xyz123 -storepass xyz123 -noprompt -dname "CN=Fineract, OU=Fineract, O=Fineract, L=Unknown, ST=Unknown, C=Unknown"
COPY ./docker/server.xml /opt/bitnami/tomcat/conf
RUN chmod 664 /opt/bitnami/tomcat/conf/server.xml
WORKDIR /opt/bitnami/tomcat/lib
RUN wget http://central.maven.org/maven2/org/drizzle/jdbc/drizzle-jdbc/1.3/drizzle-jdbc-1.3.jar

