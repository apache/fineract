<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements. See the NOTICE file
distributed with this work for additional information
regarding copyright ownership. The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied. See the License for the
specific language governing permissions and limitations
under the License.
-->

# BBK Fineract proof of concept

This branch provides a reproducible local proof of concept based on Apache
Fineract 1.15.0, Java 21, Gradle 8.14.5, and PostgreSQL 18.3. It works with
Docker Compose or with Podman using a Docker Compose provider.

The configuration is for local development and evaluation only. It binds its
published ports to localhost and must not be used as a production deployment.

## Prerequisites

- JDK 21
- Podman or Docker
- Docker Compose 2.24.4 or newer (`!override` support is required)
- OpenSSL, for generating local credentials

## First run

Build the version-pinned Fineract image:

```bash
env JAVA_HOME=/usr/lib/jvm/java-21-openjdk \
  ./gradlew --no-daemon --max-workers=8 \
  :fineract-provider:jibDockerBuild -x test
```

Create local credentials. Never commit `.env`:

```bash
cp .env.example .env
openssl rand -hex 32
```

Generate a different value for each entry in `.env`, then start the stack:

```bash
podman compose -f docker-compose.yml -f compose.bbk.yml config --quiet
podman compose -f docker-compose.yml -f compose.bbk.yml up -d
```

Verify health:

```bash
curl --insecure --fail-with-body \
  https://localhost:8443/fineract-provider/actuator/health
```

Verify the authenticated API using the upstream development account:

```bash
curl --location --insecure \
  --user mifos:password \
  --header 'Fineract-Platform-TenantId: default' \
  --header 'Content-Type: application/json' \
  https://localhost:8443/fineract-provider/api/v1/clients
```

## Daily operation

```bash
# Stop containers while retaining their state.
podman compose -f docker-compose.yml -f compose.bbk.yml stop

# Start them again.
podman compose -f docker-compose.yml -f compose.bbk.yml start

# Remove containers and networks while retaining PostgreSQL data.
podman compose -f docker-compose.yml -f compose.bbk.yml down
```

PostgreSQL data is stored in the named volume
`bbk-fineract-postgres-18`. Do not add `--volumes` or `-v` to `down` unless
you intentionally want to erase the proof-of-concept database.

## Logs

```bash
podman compose -f docker-compose.yml -f compose.bbk.yml logs --tail=200 db fineract
```
