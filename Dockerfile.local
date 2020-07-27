# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements. See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership. The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License. You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.
#
FROM registry.dev.redbee.io/waas-gradle-builder:1.4 AS builder

# COPY . fineract is slow e.g. when using Podman instead of Docker (because it doesn't honor .dockerignore and copies all of .git/** into the container..), so let's explicitly list only what we need:
COPY fineract-provider/src/main fineract/fineract-provider/src/main/
COPY fineract-provider/config fineract/fineract-provider/config/
COPY fineract-provider/gradle fineract/fineract-provider/gradle/
COPY fineract-provider/properties fineract/fineract-provider/properties/
COPY fineract-provider/[bd]*.gradle fineract/fineract-provider/
COPY fineract-provider/gradle.properties fineract/fineract-provider/
COPY fineract-provider/gradlew fineract/fineract-provider/
COPY gradle* fineract/
COPY settings.gradle fineract/
COPY licenses fineract/licenses/
COPY *LICENSE* fineract/
COPY *NOTICE* fineract/

WORKDIR fineract
RUN gradle clean -x rat -x test -Psecurity=oauth war

# =========================================

FROM registry.dev.redbee.io/was-tomcat7.0.94:1.0 as fineract

ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64/jre

COPY --from=builder /home/gradle/fineract/build/libs/fineract-provider.war /opt/bitnami/tomcat/webapps

RUN keytool -genkey -keyalg RSA -alias tomcat -keystore /opt/bitnami/tomcat/tomcat.keystore -keypass xyz123 -storepass xyz123 -noprompt -dname "CN=Fineract, OU=Fineract, O=Fineract, L=Unknown, ST=Unknown, C=Unknown"
COPY ./docker/server.xml /opt/bitnami/tomcat/conf
RUN chmod 664 /opt/bitnami/tomcat/conf/server.xml

