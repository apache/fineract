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
FROM openjdk:11 AS builder

# COPY ./build/libs/fineract-provider.jar fineract/build/libs/
RUN apt-get update -qq && apt-get install -y wget

COPY . fineract
WORKDIR /fineract

RUN ./gradlew -PautomatedBuild=true -Psecurity=oauth --no-daemon -q -x rat -x test bootJar

WORKDIR /fineract/target
RUN jar -xf /fineract/fineract-provider/build/libs/fineract-provider*.jar

FROM gcr.io/distroless/java:11 as fineract

COPY --from=builder /fineract/target/BOOT-INF/lib /app/lib
COPY --from=builder /fineract/target/META-INF /app/META-INF
COPY --from=builder /fineract/target/BOOT-INF/classes /app

EXPOSE 8443

ENV DRIVERCLASS_NAME=com.mysql.cj.jdbc.Driver
ENV PROTOCOL=jdbc
ENV SUB_PROTOCOL=mysql
ENV fineract_tenants_driver=com.mysql.cj.jdbc.Driver
ENV fineract_tenants_url=xxx
ENV fineract_tenants_uid=xxx
ENV fineract_tenants_pwd=xxx
ENV FINERACT_DEFAULT_TENANTDB_HOSTNAME=xxx
ENV FINERACT_DEFAULT_TENANTDB_PORT=3306
ENV FINERACT_DEFAULT_TENANTDB_UID=xxx
ENV FINERACT_DEFAULT_TENANTDB_PWD=xxx

ENTRYPOINT ["java", "-cp", "app:app/lib/*", "org.apache.fineract.ServerApplication"]
