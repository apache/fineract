FROM bitnami/tomcat:7.0.94 as fineract

USER root
RUN apt-get update -qq && apt-get install -y git openjdk-8-jdk wget
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64/jre

RUN git clone https://github.com/apache/fineract.git
COPY build.gradle fineract/fineract-provider

# To use for local fineract development (instead of installing from the latest code from Github)
#   1) Copy Dockerfile, docker-compose.yml, and the initdb directory to the root of your project (/fineract)
#   2) Edit Dockerfile in /fineract - comment out the 2 lines above (RUN git clone and COPY build.gradle)
#   3) Uncomment the 2 lines below
#   4) Update your local copy of build.gradle and replace all references to localhost with fineractmysql

#RUN mkdir fineract
#COPY . fineract

WORKDIR fineract
RUN ./gradlew clean war
RUN mv build/libs/fineract-provider.war /opt/bitnami/tomcat/webapps

RUN keytool -genkey -keyalg RSA -alias tomcat -keystore /opt/bitnami/tomcat/tomcat.keystore -keypass xyz123 -storepass xyz123 -noprompt -dname "CN=Fineract, OU=Fineract, O=Fineract, L=Unknown, ST=Unknown, C=Unknown"
COPY server.xml /opt/bitnami/tomcat/conf
RUN chmod 664 /opt/bitnami/tomcat/conf/server.xml
WORKDIR /opt/bitnami/tomcat/lib
RUN wget http://central.maven.org/maven2/org/drizzle/jdbc/drizzle-jdbc/1.3/drizzle-jdbc-1.3.jar

