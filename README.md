# Apache Fineract

<!-- TODO Reactivate when there is a working CI-CD instance: [![Swagger Validation](https://validator.swagger.io/validator?url=https://sandbox.mifos.community/fineract-provider/swagger-ui/fineract.yaml)](https://validator.swagger.io/validator/debug?url=https://sandbox.mifos.community/fineract-provider/swagger-ui/fineract.yaml) -->
[![Build](https://github.com/apache/fineract/actions/workflows/build-postgresql.yml/badge.svg?branch=develop)](https://github.com/apache/fineract/actions/workflows/build-postgresql.yml)
[![Docker Hub](https://img.shields.io/docker/pulls/apache/fineract.svg?logo=Docker)](https://hub.docker.com/r/apache/fineract)
[![Docker Build](https://github.com/apache/fineract/actions/workflows/publish-dockerhub.yml/badge.svg)](https://github.com/apache/fineract/actions/workflows/publish-dockerhub.yml)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=apache_fineract&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=apache_fineract)
[![Verified DPG](https://img.shields.io/badge/Verified-DPG-3333AB)](https://www.digitalpublicgoods.net/r/apache-fineract "Apache Fineract is a verified Digital Public Good under the Digital Public Goods Alliance.")

Apache Fineract is an open-source core banking platform providing a
flexible, extensible foundation for a wide range of financial services. By
making robust banking technology openly available, it lowers barriers for
institutions and innovators to reach underserved and unbanked populations.

Have a look at the [documentation](https://fineract.apache.org/docs/stable), the [wiki](https://cwiki.apache.org/confluence/display/FINERACT) or at the [FAQ](https://cwiki.apache.org/confluence/pages/viewpage.action?pageId=91554327), if this README does not answer what you are looking for.


COMMUNITY
=========

If you are interested in contributing to this project, but perhaps don't quite know how and where to get started, please [join our developer mailing list](http://fineract.apache.org/#contribute) and [chat server](https://app.element.io/#/room/#apache-fineract-home:matrix.org), listen into our conversations, chime into threads, or just send us a "Hello!" introduction message; we're a friendly bunch, and look forward to hearing from you.

For the developer wiki, see [Contributor's Zone](https://cwiki.apache.org/confluence/display/FINERACT/Contributor%27s+Zone). Maybe [these how-to articles](https://cwiki.apache.org/confluence/display/FINERACT/How-to+articles) help you to get started.

In any case visit [our JIRA Dashboard](https://issues.apache.org/jira/secure/Dashboard.jspa?selectPageId=12335824) to find issues to work on, see what others are doing, or open new issues.

In the moment you get started writing code, please consult our [CONTRIBUTING](CONTRIBUTING.md) guidelines, where you will find more information on subjects like coding style, testing and pull requests.


REQUIREMENTS
============

See [Fineract Development Environment](https://fineract.apache.org/docs/develop/#_fineract_development_environment) in the docs for hardware, software, and tool prerequisites and recommendations.


SECURITY
============
For a list of known vulnerabilities, see [Apache Fineract Security Reports](https://fineract.apache.org/security.html).

If you believe you have found a new vulnerability, [let us know privately](https://fineract.apache.org/#contribute).

For details about security during development and deployment, see the documentation [here](https://fineract.apache.org/docs/stable/#_security).

PRIVATE FORKS
============

If you’re running Apache Fineract on a private fork, you might want to consider **disabling GitHub Actions CI**. 
This is because the usage minutes and artifact costs (including the built workspace and logs generated during execution) could incur additional expenses, that you should be aware!

INSTRUCTIONS
============

Quick Start
---

Follow these steps to quickly set up and run Apache Fineract locally.

### Prerequisites

- [Java JDK](https://fineract.apache.org/docs/develop/#_fineract_development_environment)
- PostgreSQL running locally, listening on port 5432 with proper permissions (see [below](#database-and-tables) for how to run PostgreSQL in Docker)

```bash
# get code
git clone https://github.com/apache/fineract.git
cd fineract

# create dbs
./gradlew createPGDB -PdbName=fineract_tenants
./gradlew createPGDB -PdbName=fineract_default

# start backend
./gradlew devRun
```

After a minute or two, Fineract will be listening for API requests on port 8443.

> [!TIP]
> Java properties or environment variables can be used to override default settings. See `fineract-provider/src/main/resources/application.properties`.

### Verify the application is running

Confirm Fineract is ready with, for example:

```bash
curl --insecure https://localhost:8443/fineract-provider/actuator/health
```

Expected response for fresh instance:

```json
{"status":"UP","groups":["liveness","readiness"]}
```

To test authenticated endpoints, include credentials in your request:

```bash
# basic auth header uses colon-delimited and base64-encoded default "mifos:password" login
curl --location --insecure \
  https://localhost:8443/fineract-provider/api/v1/clients \
  --header 'Content-Type: application/json' \
  --header 'Fineract-Platform-TenantId: default' \
  --header 'Authorization: Basic bWlmb3M6cGFzc3dvcmQ='
```

Expected response for fresh instance:

```json
{"totalFilteredRecords":0,"pageItems":[]}
```

How to run for production
---

Running Fineract _just to try it out_ is relatively easy. If you intend to use it _in a production environment_, be aware that a proper deployment can be complex, costly, and time-consuming. Considerations include: Security, privacy, compliance, performance, service availability, backups, and more. **The Fineract project does not provide a comprehensive guide for deploying Fineract in production.** You might need skills in enterprise Java applications and more. Alternatively, you could pay a vendor for Fineract deployment and maintenance. You will find tips and tricks for [deploying](https://fineract.apache.org/docs/stable/#_deployment) and [securing](https://fineract.apache.org/docs/stable/#_securing_fineract) Fineract in our official documentation.


How to build the JAR file
---

Build a modern, cloud native, fully self contained JAR file:

```bash
./gradlew clean bootJar
```

The JAR will be created in the `fineract-provider/build/libs` directory.
If you intend to use MariaDB or MySQL (warning: [both are deprecated](https://cwiki.apache.org/confluence/display/FINERACT/FSIP-9%3A+Standardize+on+PostgreSQL)), you must download the appropriate JDBC driver yourself and override default database settings. When you start the JAR, specify the directory containing the JDBC driver.

MariaDB example:

```bash
# Assumes mariadb-java-client.jar exists in current dir.
# For MariaDB or MySQL, override default settings such as these:
export FINERACT_HIKARI_DRIVER_SOURCE_CLASS_NAME=org.mariadb.jdbc.Driver
export FINERACT_DEFAULT_TENANTDB_PORT=3306
export FINERACT_HIKARI_JDBC_URL=jdbc:mariadb://localhost:3306/fineract_tenants
java -Dloader.path=. -jar fineract-provider/build/libs/fineract-provider.jar
```

This does not require an external Tomcat.

> [!NOTE]
> Versions of mariadb driver and Fineract JARs are omitted from filenames above for brevity.

How to build the WAR file
---
Build a traditional WAR file:
```bash
./gradlew :fineract-war:clean :fineract-war:war
```
The WAR will be created in the `fineract-war/build/libs` directory. Afterwards deploy the WAR to your Tomcat Servlet Container.

We recommend using the JAR instead of the WAR file deployment, because it's much easier.


How to run using Docker or Podman
---

It is possible to do a 'one-touch' installation of Fineract using containers (AKA "Docker") for dev/test convenience and local/offline demos.
This includes the database running in the container.

As prerequisites, you must have `docker` and `docker-compose` installed on your machine; see
[Docker Install](https://docs.docker.com/install/) and [Docker Compose Install](https://docs.docker.com/compose/install/).

Alternatively, you can also use [Podman](https://github.com/containers/libpod)
(e.g. via `dnf install podman-docker`), and [Podman Compose](https://github.com/containers/podman-compose/)
(e.g. via `pip3 install podman-compose`) instead of Docker.

> [!CAUTION]
> Our images are NOT production-ready. For example, notice how the `test` Spring profile is enabled via `fineract-common.env`, included by `docker-compose.yml`, but `test` must not be enabled in production. YOU are responsible for securing your production instances. See [How to run for production](#how-to-run-for-production).

To run a new Fineract instance on Linux you can simply:
```bash
git clone https://github.com/apache/fineract.git
cd fineract
./gradlew :fineract-provider:jibDockerBuild -x test
```
On Windows, do this instead:
```cmd
git clone https://github.com/apache/fineract.git --config core.autocrlf=input
cd fineract
gradlew :fineract-provider:jibDockerBuild -x test
```
Install the Loki log driver and start:
```bash
docker plugin install grafana/loki-docker-driver:latest \
  --alias loki --grant-all-permissions
docker compose -f docker-compose-development.yml up -d
```
The Fineract (back-end) should be running at https://localhost:8443/fineract-provider/ now.
Wait for https://localhost:8443/fineract-provider/actuator/health to return `{"status":"UP"}`.
You must go to https://localhost:8443 and remember to accept the self-signed SSL certificate of the API once in your browser.

[Docker Hub](https://hub.docker.com/r/apache/fineract) has a pre-built container image of this project, built continuously.

The official Fineract Docker image includes the PostgreSQL JDBC driver only. MySQL and MariaDB JDBC drivers are
**not** included due to license incompatibility (see LICENSE section below). To use Fineract with MySQL or MariaDB,
download the driver from the vendor and either build your own Docker image on top of the official one, mount it
via a Docker volume, or install it from the OS package manager inside a custom image.

You must specify the tenants database JDBC URL by passing it to the `fineract` container via environment
variables; please consult the [`docker-compose.yml`](docker-compose.yml) for exact details how to specify those.

The logfiles and the Java Flight Recorder output are available in `PROJECT_ROOT/build/fineract/logs`. If you use IntelliJ then you can double-click on the `.jfr` file and open it with the IDE. You can also download [Azul Mission Control](https://www.azul.com/products/components/azul-mission-control/) to analyze the Java Flight Recorder file.

NOTE: If you have issues with the file permissions and Docker Compose then you might need to change the variable values for `FINERACT_USER` and `FINERACT_GROUP` in `PROJECT_ROOT/config/docker/env/fineract-common.env`. You can find out what values you need to put there with the following commands:

```bash
id -u ${USER}
id -g ${GROUP}
```

Please make sure that you are not checking in your changed values. The defaults should work for most people.


How to run on Kubernetes
---

### General Clusters

You can also run Fineract using containers on a Kubernetes cluster.
Make sure you set up and connect to your Kubernetes cluster.
You can follow [this](https://cwiki.apache.org/confluence/display/FINERACT/Install+and+configure+kubectl+and+Google+Cloud+SDK+on+ubuntu+16.04) guide to set up a Kubernetes cluster on GKE. Make sure to replace `apache-fineract-cn` with `apache-fineract`

Now e.g. from your Google Cloud shell, run the following commands:

```bash
git clone https://github.com/apache/fineract.git
cd fineract/kubernetes
./kubectl-startup.sh
```

To shutdown and reset your Cluster, run:
```bash
./kubectl-shutdown.sh
```

### Using Minikube

Alternatively, you can run fineract on a local kubernetes cluster using [minikube](https://minikube.sigs.k8s.io/docs/).
As prerequisite you must have `minikube` and `kubectl` installed on your machine; see
[Minikube & Kubectl install](https://kubernetes.io/docs/tasks/tools/install-minikube/).

To run a new Fineract instance on Minikube you can simply:

```bash
git clone https://github.com/apache/fineract.git
cd fineract/kubernetes
minikube start
./kubectl-startup.sh
```

Wait for all pods to be ready:
```bash
kubectl get pods -w
```

Once all pods are running, access the Mifos web application:
```bash
minikube service mifos-community
```

This opens the Mifos X web application in your browser. The nginx reverse proxy in the mifos-community pod forwards API requests to the fineract-server backend.

**Default credentials:**
- Username: `mifos`
- Password: `password`

You can also access the Fineract API directly:
```bash
minikube service fineract-server --url --https
```

Fineract is now running at the printed URL, which you can check e.g. using:
```bash
http --verify=no --timeout 240 --check-status get $(minikube service fineract-server --url --https)/fineract-provider/actuator/health
```

To check the status of your containers on your local minikube Kubernetes cluster, run:
```bash
minikube dashboard
```

You can check Fineract logs using:
```bash
kubectl logs deployment/fineract-server
```

You can check Mifos web app logs using:
```bash
kubectl logs deployment/mifos-community
```

To shutdown and reset your cluster, run:
```bash
./kubectl-shutdown.sh
```


How to enable External Message Broker (ActiveMQ or Apache Kafka)
---

There are two use-cases where external message broker is needed:
 - External Business Events / Reliable Event Framework
 - Executing Partitioned Spring Batch Jobs

External Events are business events, e.g.: `ClientCreated`, which might be important for third party systems. Apache Fineract supports ActiveMQ (or other JMS compliant brokers) and Apache Kafka endpoints for sending out Business Events. By default, they are not emitted.

In case of a large deployment with millions of accounts, the Close of Business Day Spring Batch job may run several hours. In order to speed up this task, remote partitioning of the job is supported. The Manager node partitions breaks up the COB job into smaller pieces (sub tasks), which then can be executed on multiple Worker nodes in parallel. The worker nodes are notified either by ActiveMQ or Kafka regarding their new sub tasks.

### ActiveMQ

JMS based messaging is disabled by default. In `docker-compose-postgresql-activemq.yml` an example is shown, where ActiveMQ is enabled. In that configuration one Spring Batch Manager instance and two Spring Batch Worker instances are created.
Spring based events should be disabled and jms based event handling should be enabled. Furthermore, proper broker JMS URL should be configured.

```bash
FINERACT_REMOTE_JOB_MESSAGE_HANDLER_JMS_ENABLED=true
FINERACT_REMOTE_JOB_MESSAGE_HANDLER_SPRING_EVENTS_ENABLED=false
FINERACT_REMOTE_JOB_MESSAGE_HANDLER_JMS_BROKER_URL=tcp://activemq:61616
```

For additional ActiveMQ related configuration please take a look to the `application.properties` where the supported configuration parameters are listed with their default values.

### Kafka

Kafka support is also disabled by default. In `docker-compose-postgresql-kafka.yml` an example is shown, where self-hosted Kafka is enabled for both External Events and Spring Batch Remote Job execution.

During the development Fineract was tested with PLAINTEXT Kafka brokers without authentication and with AWS MSK using IAM authentication. The extra [JAR file](https://github.com/aws/aws-msk-iam-auth/releases) required for IAM authentication is already added to the classpath.
An example MSK setup can be found in `docker-compose-postgresql-kafka-msk.yml`.

The full list of supported Kafka related properties is documented in the [Fineract Platform documentation](https://fineract.apache.org/docs/stable/).


DATABASE AND TABLES
===================

You can run the required version of the database server in a container, instead of having to install it, like this:

```bash
# start postgresql in background
docker run --name postgres -p 5432:5432 -e POSTGRES_USER=root -e POSTGRES_PASSWORD=postgres -u nobody:nogroup -d postgres:18.3
# stop and destroy container
docker rm -f postgres
```

Similarly, for one of the [deprecated](https://cwiki.apache.org/confluence/display/FINERACT/FSIP-9%3A+Standardize+on+PostgreSQL) database backends:

```bash
# start mariadb in background
docker run --name mariadb-12.2 -p 3306:3306 -e MARIADB_ROOT_PASSWORD=mysql -d mariadb:12.2.2 --innodb-snapshot-isolation=OFF
# stop and destroy container
docker rm -f mariadb-12.2
```

Beware that this container database keeps its state inside the container and not on the host filesystem.  It is lost when you destroy (rm) this container.  This is typically fine for development.  See [Caveats: Where to Store Data on the database container documentation](https://hub.docker.com/_/mariadb) regarding how to make it persistent instead of ephemeral.


MySQL/MariaDB and UTC timezone
---
With release `1.8.0` we introduced improved date time handling in Fineract. Date time is stored in UTC, and UTC timezone enforced even on the JDBC driver, e. g. for MySQL:

```
serverTimezone=UTC&useLegacyDatetimeCode=false&sessionVariables=time_zone='-00:00'
```

If you use MySQL as Fineract database, the following configuration is highly recommended:

* Run the application in UTC (the default command line in our Docker image has the necessary parameters already set)
* Run the MySQL database server in UTC (if you use managed services like AWS RDS, then this should be the default anyway, but it would be good to double-check)

In case Fineract and MySQL do not run in UTC, MySQL might save date time values differently from PostgreSQL

Example scenario: If the Fineract instance runs in timezone: GMT+2, and the local date time is 2022-08-11 17:15 ...
* ... then PostgreSQL saves the LocalDateTime as is: 2022-08-11 17:15
* ... and MySQL saves the LocalDateTime in UTC: 2022-08-11 15:15
* ... but when we read the date time from PostgreSQL or from MySQL, both systems give us the same value: 2022-08-11 17:15 GMT+2

If a previously used Fineract instance didn't run in UTC (backward compatibility), all prior dates will be read wrongly by MySQL. This can cause issues, when you run the database migration scripts.

Recommendation: Shift all dates in your database by the timezone offset that your Fineract instance used.


CONNECTION POOL CONFIGURATION
=======

Please check `application.properties` to see which connection pool settings can be tweaked. The associated environment variables are prefixed with `FINERACT_HIKARI_*`. You can find more information about specific connection pool settings at the [HikariCP Github repository](https://github.com/brettwooldridge/HikariCP?tab=readme-ov-file#gear-configuration-knobs-baby).

NOTE: We keep backwards compatibility until one of the next releases to ensure that things are working as expected. Environment variables prefixed `fineract_tenants_*` can still be used to configure the database connection, but we strongly encourage using `FINERACT_HIKARI_*` with more options.


RELEASES
============

[Official releases](https://fineract.apache.org/#downloads) are created quarterly, at the end of each quarter. Our [documented release procedure](https://fineract.apache.org/docs/develop/#_releases) follows the [ASF release policy](https://www.apache.org/legal/release-policy.html).

See <https://cwiki.apache.org/confluence/display/FINERACT/Fineract+Releases> for an archive of historical release notes along with JIRA issues relevant to each release.

See <https://github.com/apache/fineract/releases> for a list of PRs and contributors for each release.

EOL/unsupported releases are [archived](https://www.apache.org/legal/release-policy.html#archived).

Versioned build products created from the `develop` branch will include `-SNAPSHOT`. See [related settings](https://github.com/qoomon/gradle-git-versioning-plugin) near `version` and `gitVersioning` in `build.gradle` for details.


LICENSE
============

This project is licensed under [Apache License Version 2.0](https://github.com/apache/fineract/blob/develop/APACHE_LICENSETEXT.md).

The following libraries are **not** included in Fineract binary distribution artifacts (binary tarball, WAR, bootJar,
or Docker image) because their licenses are [Category X](https://www.apache.org/legal/resolved.html#category-x)
under the Apache Software Foundation third-party license policy:

- **MariaDB Connector/J** (`org.mariadb.jdbc:mariadb-java-client`) — LGPL
- **MySQL Connector/J** (`com.mysql:mysql-connector-j`) — GPL with FOSS exception
- **SpotBugs Annotations** (`com.github.spotbugs:spotbugs-annotations`) — LGPL

These libraries may be present on the compile classpath during development and testing but are excluded from
all distributed artifacts. If you need MySQL or MariaDB support (both are deprecated; PostgreSQL is the
recommended database), download the appropriate JDBC driver from the vendor's website and provide it at
runtime via `-Dloader.path` as shown in the [How to build the JAR file](#how-to-build-the-jar-file) section above.

As discussed in [LEGAL-462](https://issues.apache.org/jira/browse/LEGAL-462) and
[LEGAL-726](https://issues.apache.org/jira/browse/LEGAL-726), this project therefore
complies with the [Apache Software Foundation third-party license policy](https://www.apache.org/legal/resolved.html).


PLATFORM API
============

Fineract does not provide a UI, but provides an API. Running Fineract locally, the Swagger documentation can be accessed under `https://localhost:8443/fineract-provider/swagger-ui/index.html`. A live version can be accessed via [this Sandbox](https://sandbox.mifos.community/fineract-provider/swagger-ui/index.html) (not hosted by us).

Apache Fineract supports client code generation using [Swagger Codegen](https://github.com/swagger-api/swagger-codegen) based on the [OpenAPI Specification](https://swagger.io/specification/). For more instructions on how to generate client code, check [this section](https://fineract.apache.org/docs/develop/#_generate_api_client) of the Fineract documentation. [This video](https://www.youtube.com/watch?v=FlVd-0YAo6c) documents the use of the Swagger-UI.
