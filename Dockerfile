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
FROM azul/zulu-openjdk-alpine:17-latest AS builder

COPY . fineract

WORKDIR /fineract

RUN ./gradlew --no-build-cache --no-daemon -q -x rat -x compileTestJava -x test -x spotlessJavaCheck -x spotlessJava bootJar

# =========================================

FROM azul/zulu-openjdk-alpine:17-latest AS fineract

#Copy Apache Fineract binary
COPY --from=builder /fineract/fineract-provider/build/libs/ /app

WORKDIR /app

COPY entrypoint.sh /entrypoint.sh

RUN chmod 775 /entrypoint.sh

EXPOSE 8080 8443

ENTRYPOINT ["/entrypoint.sh"]