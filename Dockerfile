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

RUN apt-get update -qq && apt-get install -y wget && apt-get clean

COPY . fineract

WORKDIR /fineract

#RUN gsutil cp gs://fineract-404214-cred/fineract-404214-1eefd4b3e75f.json . && \
#            mv fineract-404214-1eefd4b3e75f.json fineract.json

RUN ./gradlew --no-daemon -q -x compileTestJava -x test -x spotlessJavaCheck -x spotlessJava bootJar

WORKDIR /fineract
RUN jar -xf fineract-provider/build/libs/fineract-provider-0.0.1-SNAPSHOT.jar

# We download separately a JDBC driver (which not allowed to be included in Apache binary distribution)
WORKDIR /fineract/BOOT-INF/lib
#RUN wget -q https://downloads.mariadb.com/Connectors/java/connector-java-2.7.3/mariadb-java-client-2.7.3.jar
RUN wget -q https://storage.cloud.google.com/fineract-404214-java-lib/mysql-connector-j-8.2.0/mysql-connector-j-8.2.0.jar
RUN wget -q https://storage.googleapis.com/cloud-sql-connectors-java/v1.13.1/mysql-socket-factory-1.13.1-jar-with-dependencies.jar

WORKDIR /root

RUN wget https://dl.google.com/cloudsql/cloud_sql_proxy.linux.amd64 && \
                                                      mv cloud_sql_proxy.linux.amd64 cloud_sql_proxy && \
                                                      chmod +x cloud_sql_proxy

# =========================================

FROM azul/zulu-openjdk-alpine:17 AS fineract

COPY --from=builder /fineract/BOOT-INF/lib /app/lib
COPY --from=builder /fineract/META-INF /app/META-INF
COPY --from=builder /fineract/BOOT-INF/classes /app
COPY --from=builder /fineract/fineract-provider/build/libs/ /app
COPY --from=builder /root/cloud_sql_proxy /app
COPY --from=builder /fineract/fineract.json /app

#COPY entrypoint.sh /entrypoint.sh

#RUN chmod 775 /entrypoint.sh

ENV CLOUD_SQL_INSTANCE=fineract-404214:europe-west2:fineract-instance
ENV CLOUD_SQL_USER=root
ENV CLOUD_SQL_PASSWORD=mysql
ENV CLOUD_SQL_SOCKET=/cloudsql/$CLOUD_SQL_INSTANCE

ENV fineract_tenants_driver=com.mysql.cj.jdbc.Driver
ENV fineract_tenants_url=jdbc:mysql://127.0.0.1:3306/fineract_tenants
ENV fineract_tenants_uid=root
ENV fineract_tenants_pwd=mysql

# NOTE: node aware scheduler
ENV FINERACT_NODE_ID=1
# NOTE: env vars prefixed "FINERACT_HIKARI_*" are used to configure the database connection pool
ENV FINERACT_HIKARI_DRIVER_SOURCE_CLASS_NAME=com.mysql.cj.jdbc.Driver
ENV FINERACT_HIKARI_JDBC_URL=jdbc:mysql://127.0.0.1:3306/fineract_tenants
ENV FINERACT_HIKARI_USERNAME=root
ENV FINERACT_HIKARI_PASSWORD=mysql
# ... following variables are optional; "application.properties" contains reasonable defaults (same as here)
ENV FINERACT_HIKARI_MINIMUM_IDLE=3
ENV FINERACT_HIKARI_MAXIMUM_POOL_SIZE=10
ENV FINERACT_HIKARI_IDLE_TIMEOUT=60000
ENV FINERACT_HIKARI_CONNECTION_TIMEOUT=20000
ENV FINERACT_HIKARI_TEST_QUERY='SELECT 1'
ENV FINERACT_HIKARI_AUTO_COMMIT=true
ENV FINERACT_HIKARI_DS_PROPERTIES_CACHE_PREP_STMTS=true
ENV FINERACT_HIKARI_DS_PROPERTIES_PREP_STMT_CACHE_SIZE=250
ENV FINERACT_HIKARI_DS_PROPERTIES_PREP_STMT_CACHE_SQL_LIMIT=2048
ENV FINERACT_HIKARI_DS_PROPERTIES_USE_SERVER_PREP_STMTS=true
ENV FINERACT_HIKARI_DS_PROPERTIES_USE_LOCAL_SESSION_STATE=true
ENV FINERACT_HIKARI_DS_PROPERTIES_REWRITE_BATCHED_STATEMENTS=true
ENV FINERACT_HIKARI_DS_PROPERTIES_CACHE_RESULT_SET_METADATA=true
ENV FINERACT_HIKARI_DS_PROPERTIES_CACHE_SERVER_CONFIGURATION=true
ENV FINERACT_HIKARI_DS_PROPERTIES_ELIDE_SET_AUTO_COMMITS=true
ENV FINERACT_HIKARI_DS_PROPERTIES_MAINTAIN_TIME_STATS=false
ENV FINERACT_HIKARI_DS_PROPERTIES_LOG_SLOW_QUERIES=true
ENV FINERACT_HIKARI_DS_PROPERTIES_DUMP_QUERIES_IN_EXCEPTION=true
ENV FINERACT_HIKARI_DS_PROPERTIES_INSTANCE_CONNECTION_NAME=fineract-404214:europe-west2:fineract-instance

# NOTE: env vars prefixed "FINERACT_DEFAULT_TENANTDB_*" are used to create the default tenant database
ENV FINERACT_DEFAULT_TENANTDB_HOSTNAME=127.0.0.1
ENV FINERACT_DEFAULT_TENANTDB_PORT=3306
ENV FINERACT_DEFAULT_TENANTDB_UID=root
ENV FINERACT_DEFAULT_TENANTDB_PWD=mysql
ENV FINERACT_DEFAULT_TENANTDB_CONN_PARAMS=cloudSqlInstance=fineract-404214:europe-west2:fineract-instance&socketFactory=com.google.cloud.sql.mysql.SocketFactory&allowPublicKeyRetrieval=true&serverTimezone=UTC&useLegacyDatetimeCode=false&sessionVariables=time_zone=`-00:00`
ENV FINERACT_DEFAULT_TENANTDB_TIMEZONE=Africa/Nairobi
ENV FINERACT_DEFAULT_TENANTDB_IDENTIFIER=default
ENV FINERACT_DEFAULT_TENANTDB_NAME=fineract_default
ENV FINERACT_DEFAULT_TENANTDB_DESCRIPTION='Default Demo Tenant'
ENV JAVA_TOOL_OPTIONS="-Xmx4G"

WORKDIR /app

CMD ["./cloud_sql_proxy", "-instances=$CLOUD_SQL_INSTANCE=tcp:0.0.0.0:3306", "-credential_file=fineract.json"]

WORKDIR /fineract

ENTRYPOINT ["java", "-Dloader.path=.", "-jar", "/app/fineract-provider-0.0.1-SNAPSHOT.jar"]

EXPOSE 3306
EXPOSE 8080

#ENTRYPOINT ["/entrypoint.sh"]
