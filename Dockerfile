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
FROM azul/zulu-openjdk-debian:17 AS builder

RUN apt-get update -qq && apt-get install -y wget cloud-sql-proxy

ENV CLOUD_SQL_INSTANCE=fineract-404214:europe-west2:fineract-instance
ENV CLOUD_SQL_USER=root
ENV CLOUD_SQL_PASSWORD=mysql
ENV CLOUD_SQL_SOCKET=/cloudsql/$CLOUD_SQL_INSTANCE

COPY . fineract
WORKDIR /fineract

RUN ./gradlew --no-daemon -q -x compileTestJava -x test -x spotlessJavaCheck -x spotlessJava bootJar

WORKDIR /fineract
RUN jar -xf fineract-provider/build/libs/fineract-provider-0.0.1-SNAPSHOT.jar

# We download separately a JDBC driver (which not allowed to be included in Apache binary distribution)
WORKDIR /fineract/BOOT-INF/lib
#RUN wget -q https://downloads.mariadb.com/Connectors/java/connector-java-2.7.3/mariadb-java-client-2.7.3.jar
RUN wget -q https://storage.cloud.google.com/fineract-404214-java-lib/mysql-connector-j-8.2.0/mysql-connector-j-8.2.0.jar
RUN wget -q https://storage.googleapis.com/cloud-sql-connectors-java/v1.13.1/mysql-socket-factory-1.13.1-jar-with-dependencies.jar

RUN cloud_sql_proxy -instances=$CLOUD_SQL_INSTANCE=$CLOUD_SQL_SOCKET

# =========================================

FROM azul/zulu-openjdk-alpine:17 AS fineract

COPY --from=builder /fineract/BOOT-INF/lib /app/lib
COPY --from=builder /fineract/META-INF /app/META-INF
COPY --from=builder /fineract/BOOT-INF/classes /app
COPY --from=builder /fineract/fineract-provider/build/libs/ /app

#WORKDIR /fineract

#COPY entrypoint.sh /entrypoint.sh

#RUN chmod 775 /entrypoint.sh

EXPOSE 3306
EXPOSE 8443

WORKDIR /fineract

CMD ["java", "-Dloader.path=.", "-jar", "/app/fineract-provider-0.0.1-SNAPSHOT.jar"]

#ENTRYPOINT ["/entrypoint.sh"]
