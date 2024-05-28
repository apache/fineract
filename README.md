Apache Fineract: A Platform for Microfinance
============
[![Swagger Validation](https://validator.swagger.io/validator?url=https://sandbox.mifos.community/fineract-provider/swagger-ui/fineract.yaml)](https://validator.swagger.io/validator/debug?url=https://sandbox.mifos.community/fineract-provider/swagger-ui/fineract.yaml) [![build](https://github.com/apache/fineract/actions/workflows/build.yml/badge.svg)](https://github.com/apache/fineract/actions/workflows/build.yml) [![Docker Hub](https://img.shields.io/docker/pulls/apache/fineract.svg?logo=Docker)](https://hub.docker.com/r/apache/fineract)  [![Docker Build](https://img.shields.io/docker/cloud/build/apache/fineract.svg?logo=Docker)](https://hub.docker.com/r/apache/fineract/builds) [![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=apache_fineract&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=apache_fineract)

</b>

Fineract is a mature platform with open APIs that provides a reliable, robust, and affordable core banking solution for financial institutions offering services to the world’s 3 billion underbanked and unbanked.

[Have a look at the FAQ on our Wiki at apache.org](https://cwiki.apache.org/confluence/display/FINERACT/FAQ) if this README does not answer what you are looking for.  [Visit our JIRA Dashboard](https://issues.apache.org/jira/secure/Dashboard.jspa?selectPageId=12335824) to find issues to work on, see what others are working on, or open new issues.

[![Code Now! (Gitpod)](https://gitpod.io/button/open-in-gitpod.svg)](https://gitpod.io/#https://github.com/apache/fineract)
to start contributing to this project in the online web-based IDE GitPod.io right away!
(You may initially have to press F1 to Find Command and run "Java: Start Language Server".)
It's of course also possible to contribute with a "traditional" local development environment (see below).

COMMUNITY
=========

If you are interested in contributing to this project, but perhaps don't quite know how and where to get started, please [join our developer mailing list](http://fineract.apache.org/#contribute), listen into our conversations, chime into threads, and just send us a "Hello!" introduction email; we're a friendly bunch, and look forward to hearing from you.


REQUIREMENTS
============
* `Java >= 17` (Azul Zulu JVM is tested by our CI on GitHub Actions)
* MariaDB `11.2`

You can run the required version of the database server in a container, instead of having to install it, like this:

    docker run --name mariadb-11.2 -p 3306:3306 -e MARIADB_ROOT_PASSWORD=mysql -d mariadb:11.2

and stop and destroy it like this:

    docker rm -f mariadb-11.2

<br>Beware that this database container database keeps its state inside the container and not on the host filesystem.  It is lost when you destroy (rm) this container.  This is typically fine for development.  See [Caveats: Where to Store Data on the database container documentation](https://hub.docker.com/_/mariadb) re. how to make it persistent instead of ephemeral.<br>

Tomcat v9 is only required if you wish to deploy the Fineract WAR to a separate external servlet container.  Note that you do not require to install Tomcat to develop Fineract, or to run it in production if you use the self-contained JAR, which transparently embeds a servlet container using Spring Boot.  (Until FINERACT-730, Tomcat 7/8 were also supported, but now Tomcat 9 is required.)

<br>IMPORTANT: If you use MySQL or MariaDB
============

Recently (after release `1.7.0`), we introduced improved date time handling in Fineract. Date time is from now on stored in UTC and we are enforcing UTC timezone even on the JDBC driver, e. g. for MySQL:

```
serverTimezone=UTC&useLegacyDatetimeCode=false&sessionVariables=time_zone=‘-00:00’
```

__DO__: If you do use MySQL as your Fineract database then the following configuration is highly recommended:

* Run the application in UTC (the default command line in our Docker image has the necessary parameters already set)
* Run the MySQL database server in UTC (if you use managed services like AWS RDS then this should be the default anyway, but it would be good to double-check)

__DON'T__: In case the Fineract instance and the MySQL server are __not__ running in UTC then the following could happen:

* MySQL is saving date time values differently from PostgreSQL
* Example scenario: if the Fineract instance runs in timezone: GMT+2, and the local date time is 2022-08-11 17:15 ...
* ... then __PostgreSQL saves__ the LocalDateTime as is: __2022-08-11 17:15__
* ... and __MySQL saves__ the LocalDateTime in UTC: __2022-08-11 15:15__
* ... but when we __read__ the date time from PostgreSQL __or__ from MySQL, then both systems give us the same values: __2022-08-11 17:15 GMT+2__

If a previously used Fineract instance didn't run in UTC (backward compatibility), then all prior dates will be read wrongly by MySQL/MariaDB. This can cause issues when you run the database migration scripts.

__RECOMMENDATION__: you need to shift all dates in your database by the timezone offset that your Fineract instance used.

<br>INSTRUCTIONS: How to run for local development
============

Run the following commands:
1. `./gradlew createDB -PdbName=fineract_tenants`
1. `./gradlew createDB -PdbName=fineract_default`
1. `./gradlew bootRun`


<br>INSTRUCTIONS: How to build the JAR file
============
1. Clone the repository or download and extract the archive file to your local directory.
2. Run `./gradlew clean bootJar` to build a modern cloud native fully self contained JAR file which will be created at `fineract-provider/build/libs` directory.
3. As we are not allowed to include a JDBC driver in the built JAR, download a JDBC driver of your choice. For example: `wget https://downloads.mariadb.com/Connectors/java/connector-java-3.3.2/mariadb-java-client-3.3.2.jar`
4. Start the jar and pass the directory where you have downloaded the JDBC driver as loader.path, for example: `java -Dloader.path=. -jar fineract-provider/build/libs/fineract-provider.jar` (does not require external Tomcat)

NOTE: we cannot upgrade to version 3.0.x of the MariaDB driver just yet; have to wait until 3.0.4 is out for a bug fix.

The tenants database connection details are configured [via environment variables (as with Docker container)](#instructions-to-run-using-docker-and-docker-compose), e.g. like this:

    export FINERACT_HIKARI_PASSWORD=verysecret
    ...
    java -jar fineract-provider.jar


<br>SECURITY
============
NOTE: The HTTP Basic and OAuth2 authentication schemes are mutually exclusive. You can't enable them both at the same time. Fineract checks these settings on startup and will fail if more than one authentication scheme is enabled.

HTTP Basic Authentication
------------
By default Fineract is configured with a HTTP Basic Authentication scheme, so you actually don't have to do anything if you want to use it. But if you would like to explicitly choose this authentication scheme then there are two ways to enable it:
1. Use environment variables (best choice if you run with Docker Compose):
```
FINERACT_SECURITY_BASICAUTH_ENABLED=true
FINERACT_SECURITY_OAUTH_ENABLED=false
```
2. Use JVM parameters (best choice if you run the Spring Boot JAR):
```
java -Dfineract.security.basicauth.enabled=true -Dfineract.security.oauth.enabled=false -jar fineract-provider.jar
```

<br>OAuth2 AUTHENTICATION
------------
There is also an OAuth2 authentication scheme available. Again, two ways to enable it:
1. Use environment variables (best choice if you run with Docker Compose):
```
FINERACT_SECURITY_BASICAUTH_ENABLED=false
FINERACT_SECURITY_OAUTH_ENABLED=true
```
2. Use JVM parameters (best choice if you run the Spring Boot JAR):
```
java -Dfineract.security.basicauth.enabled=false -Dfineract.security.oauth.enabled=true -jar fineract-provider.jar
```

TWO FACTOR AUTHENTICATION (2FA)
------------
You can also enable 2FA authentication. Depending on how you start Fineract add the following:

1. Use environment variable (best choice if you run with Docker Compose):
```
FINERACT_SECURITY_2FA_ENABLED=true
```
2. Use JVM parameter (best choice if you run the Spring Boot JAR):
```
-Dfineract.security.2fa.enabled=true
```


<br>INSTRUCTIONS: How to build a WAR file
============
1. Clone the repository or download and extract the archive file to your local directory.
2. Run `./gradlew :fineract-war:clean :fineract-war:war` to build a traditional WAR file which will be created at `fineract-war/build/libs` directory.
3. Deploy this WAR to your Tomcat v9 Servlet Container.

We recommend using the JAR instead of the WAR file deployment, because it's much easier.

Note that with the 1.4 release the tenants database pool configuration changed from Tomcat DBCP in XML to an embedded Hikari, configured by environment variables, see above.


INSTRUCTIONS: How to execute Integration Tests
============
> Note that if this is the first time to access MySQL DB, then you may need to reset your password.

Run the following commands:
1. `./gradlew createDB -PdbName=fineract_tenants`
1. `./gradlew createDB -PdbName=fineract_default`
1. `./gradlew clean test`


INSTRUCTIONS: How to run and debug in Eclipse IDE
============

It is possible to run Fineract in Eclipse IDE and also to debug Fineract using Eclipse's debugging facilities.
To do this, you need to create the Eclipse project files and import the project into an Eclipse workspace:

1. Create Eclipse project files into the Fineract project by running `./gradlew cleanEclipse eclipse`
2. Import the fineract-provider project into your Eclipse workspace (File->Import->General->Existing Projects into Workspace, choose root directory fineract/fineract-provider)
3. Do a clean build of the project in Eclipse (Project->Clean...)
3. Run / debug Fineract by right clicking on org.apache.fineract.ServerApplication class and choosing Run As / Debug As -> Java Application. All normal Eclipse debugging features (breakpoints, watchpoints etc) should work as expected.

If you change the project settings (dependencies etc) in Gradle, you should redo step 1 and refresh the project in Eclipse.

You can also use Eclipse Junit support to run tests in Eclipse (Run As->Junit Test)

Finally, modifying source code in Eclipse automatically triggers hot code replace to a running instance, allowing you to immediately test your changes


INSTRUCTIONS: How to run using Docker and docker-compose
===================================================

It is possible to do a 'one-touch' installation of Fineract using containers (AKA "Docker").
Fineract now packs the mifos community-app web UI in it's docker deploy.
You can now run and test fineract with a GUI directly from the combined docker builds.
This includes the database running in a container.

As Prerequisites, you must have `docker` and `docker-compose` installed on your machine; see
[Docker Install](https://docs.docker.com/install/) and
[Docker Compose Install](https://docs.docker.com/compose/install/).

Alternatively, you can also use [Podman](https://github.com/containers/libpod)
(e.g. via `dnf install podman-docker`), and [Podman Compose](https://github.com/containers/podman-compose/)
(e.g. via `pip3 install podman-compose`) instead of Docker.

Now to run a new Fineract instance you can simply:

1. `git clone https://github.com/apache/fineract.git ; cd fineract`
1. for windows, use `git clone https://github.com/apache/fineract.git --config core.autocrlf=input ; cd fineract`
1. `./gradlew :fineract-provider:jibDockerBuild -x test`
1. install the Loki log driver with `docker plugin install grafana/loki-docker-driver:latest --alias loki --grant-all-permissions`
1. `docker compose -f docker-compose-development.yml up -d`
1. fineract (back-end) is running at https://localhost:8443/fineract-provider/
1. wait for https://localhost:8443/fineract-provider/actuator/health to return `{"status":"UP"}`
1. you must go to https://localhost:8443 and remember to accept the self-signed SSL certificate of the API once in your browser, otherwise  you get a message that is rather misleading from the UI.
1. community-app (UI) is running at http://localhost:9090/?baseApiUrl=https://localhost:8443/fineract-provider&tenantIdentifier=default
1. login using default _username_ `mifos` and _password_ `password`

https://hub.docker.com/r/apache/fineract has a pre-built container image of this project, built continuously.

You must specify the MySQL tenants database JDBC URL by passing it to the `fineract` container via environment
variables; please consult the [`docker-compose.yml`](docker-compose.yml) for exact details how to specify those.
_(Note that in previous versions, the `mysqlserver` environment variable used at `docker build` time instead of at
`docker run` time did something similar; this has changed in [FINERACT-773](https://issues.apache.org/jira/browse/FINERACT-773)),
and the `mysqlserver` environment variable is now no longer supported.)_

The logfiles and the Java Flight Recorder output are available in `PROJECT_ROOT/build/fineract/logs`. If you use IntelliJ then you can double-click on the `.jfr` file and open it with the IDE. You can also download Azul Mission Control from here https://www.azul.com/products/components/azul-mission-control/ to analyze the Java Flight Recorder file.

NOTE: If you have issues with the file permissions and Docker Compose then you might need to change the variable values for `FINERACT_USER` and `FINERACT_GROUP` in `PROJECT_ROOT/config/docker/env/fineract-common.env`. You can find out what values you need to put there with the following commands:

```
id -u ${USER}
id -u ${GROUP}
```

Please make sure that you are not checking in your changed values. The defaults should normally work for most people.

Connection pool configuration
=============================

Please check `application.properties` to see which connection pool settings can be tweaked. The associated environment variables are prefixed with `FINERACT_HIKARI_*`. You can find more information about specific connection pool settings (Hikari) at https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby

NOTE: we'll keep backwards compatibility until one of the next releases to ensure that things are working as expected. Environment variables prefixed `fineract_tenants_*` can still be used to configure the database connection, but we strongly encourage using `FINERACT_HIKARI_*` with more options.

<br>SSL CONFIGURATION
=================

Read also [the HTTPS related doc](fineract-doc/src/docs/en/chapters/deployment/https.adoc).

By default SSL is enabled, but all SSL related properties are now tunable. SSL can be turned off by setting the environment variable `FINERACT_SERVER_SSL_ENABLED` to false. If you do that then please make sure to also change the server port to `8080` via the variable `FINERACT_SERVER_PORT`, just for the sake of keeping the conventions.
You can choose now easily a different SSL keystore by setting `FINERACT_SERVER_SSL_KEY_STORE` with a path to a different (not embedded) keystore. The password can be set via `FINERACT_SERVER_SSL_KEY_STORE_PASSWORD`. See the `application.properties` file and the latest Spring Boot documentation (https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html) for more details.


<br>TOMCAT CONFIGURATION
====================

Please refer to the `application.properties` and the official Spring Boot documentation (https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html) on how to do performance tuning for Tomcat. Note: you can set now the acceptable form POST size (default is 2MB) via environment variable `FINERACT_SERVER_TOMCAT_MAX_HTTP_FORM_POST_SIZE`.


<br>INSTRUCTIONS: How to run on Kubernetes
=================================

<br>General Clusters
----------------

You can also run Fineract using containers on a Kubernetes cluster.
Make sure you set up and connect to your Kubernetes cluster.
You can follow [this](https://cwiki.apache.org/confluence/display/FINERACT/Install+and+configure+kubectl+and+Google+Cloud+SDK+on+ubuntu+16.04) guide to set up a Kubernetes cluster on GKE. Make sure to replace `apache-fineract-cn` with `apache-fineract`

Now e.g. from your Google Cloud shell, run the following commands:

1. `git clone https://github.com/apache/fineract.git ; cd fineract/kubernetes`
1. `./kubectl-startup.sh`

To shutdown and reset your Cluster, run:

    ./kubectl-shutdown.sh

Using Minikube
--------------

Alternatively, you can run fineract on a local kubernetes cluster using [minikube](https://minikube.sigs.k8s.io/docs/).
As Prerequisites, you must have `minikube` and `kubectl` installed on your machine; see
[Minikube & Kubectl install](https://kubernetes.io/docs/tasks/tools/install-minikube/).

Now to run a new Fineract instance on Minikube you can simply:

1. `git clone https://github.com/apache/fineract.git ; cd fineract/kubernetes`
1. `minikube start`
1. `./kubectl-startup.sh`
1. `minikube service fineract-server --url --https`
1. Fineract is now running at the printed URL (note HTTP), which you can check e.g. using:

   http --verify=no --timeout 240 --check-status get $(minikube service fineract-server --url --https)/fineract-provider/actuator/health

To check the status of your containers on your local minikube Kubernetes cluster, run:

    minikube dashboard

You can check Fineract logs using:

    kubectl logs deployment/fineract-server

To shutdown and reset your cluster, run:

    ./kubectl-shutdown.sh

To shutdown and reset your cluster, run:

    minikube ssh

    sudo rm -rf /mnt/data/

We have [some open issues in JIRA with Kubernetes related enhancement ideas](https://jira.apache.org/jira/browse/FINERACT-783?jql=labels%20%3D%20kubernetes%20AND%20project%20%3D%20%22Apache%20Fineract%22%20) which you are welcome to contribute to.


INSTRUCTIONS: How to download Gradle wrapper
============
The file gradle/wrapper/gradle-wrapper.jar binary is checked into this projects Git source repository,
but won't exist in your copy of the Fineract codebase if you downloaded a released source archive from apache.org.
In that case, you need to download it using the commands below:

    wget --no-check-certificate -P gradle/wrapper https://github.com/apache/fineract/raw/develop/gradle/wrapper/gradle-wrapper.jar

(or)

    curl --insecure -L https://github.com/apache/fineract/raw/develop/gradle/wrapper/gradle-wrapper.jar > gradle/wrapper/gradle-wrapper.jar


INSTRUCTIONS: How to run Apache RAT (Release Audit Tool)
============
1. Extract the archive file to your local directory.
2. Run `./gradlew rat`. A report will be generated under build/reports/rat/rat-report.txt


INSTRUCTIONS: How to enable External Message Broker (ActiveMQ or Apache Kafka)
============

There are two use-cases where external message broker is needed:
 - External Business Events / Reliable Event Framework
 - Executing Partitioned Spring Batch Jobs

External Events are business events, e.g.: `ClientCreated`, which might be important for third party systems. Apache Fineract supports ActiveMQ (or other JMS compliant brokers) and Apache Kafka endpoints for sending out Business Events. By default, they are not emitted.

In case of a large deployment with millions of accounts, the Close of Business Day Spring Batch job may run several hours. In order to speed up this task, remote partitioning of the job is supported. The Manager node partitions (breaks up) the COB job into smaller pieces (sub tasks) which then can be executed on multiple Worker nodes in parallel. The worker nodes are notified either by ActiveMQ or Kafka regarding their new sub tasks.
### Active MQ

JMS based messaging is disabled by default. In `docker-compose-postgresql-activemq.yml` an example is shown where ActiveMQ is enabled. In that configuration one Spring Batch Manager instance and two Spring Batch Worker instances are created.
Spring based events should be disabled and jms based event handling should be enabled. Furthermore, proper broker JMS URL should be configured.

```
      FINERACT_REMOTE_JOB_MESSAGE_HANDLER_JMS_ENABLED=true
      FINERACT_REMOTE_JOB_MESSAGE_HANDLER_SPRING_EVENTS_ENABLED=false
      FINERACT_REMOTE_JOB_MESSAGE_HANDLER_JMS_BROKER_URL=tcp://activemq:61616
```

For additional ActiveMQ related configuration please take a look to the `application.properties` where the supported configuration parameters are listed with their default values.

### Kafka

Kafka support also disabled by default. In `docker-compose-postgresql-kafka.yml` an example is shown where self-hosted Kafka is enabled for both External Events and Spring Batch Remote Job execution.

During the development Fineract was tested with PLAINTEXT Kafka brokers without authentication and with AWS MSK using IAM authentication. The extra [jar file](https://github.com/aws/aws-msk-iam-auth/releases) required for IAM authentication is already added to the classpath.
An example MSK setup can be found in `docker-compose-postgresql-kafka-msk.yml`.

The full list of supported Kafka related properties are documented here: https://fineract.apache.org/docs/current/

Checkstyle and Spotless
============

This project enforces its code conventions using [checkstyle.xml](config/checkstyle/checkstyle.xml) through Checkstyle and [fineract-formatting-preferences.xml](config/fineract-formatting-preferences.xml) through Spotless. They are configured to run automatically during the normal Gradle build, and fail if there are any violations detected. You can run the following command to automatically fix spotless violations:

    `./gradlew spotlessApply`

Since some checks are present in both Checkstyle and Spotless, the same command can help you fix some of the Checkstyle violations (but not all, other Checkstyle violations need to fixed manually).

You can also check for Spotless violations (only; but normally don't have to, because the regular build full already includes this anyway):

    `./gradlew spotlessCheck`

We recommend that you configure your favourite Java IDE to match those conventions. For Eclipse, you can go to
Window > Java > Code Style and import our [config/fineractdev-formatter.xml](config/fineractdev-formatter.xml) under formatter section and [config/fineractdev-cleanup.xml](config/fineractdev-cleanup.xml) under Clean up section. The same fineractdev-formatter.xml configuration file (that can be used in Eclipse IDE) is also used by Spotless to both check for violations and autoformat code on the CLI.
You could also use Checkstyle directly in your IDE (but you don't neccesarily have to, it may just be more convenient for you).  For Eclipse, use https://checkstyle.org/eclipse-cs/ and load our checkstyle.xml into it, for IntelliJ you can use [CheckStyle-IDEA](https://plugins.jetbrains.com/plugin/1065-checkstyle-idea).


Code Coverage Reports
============

The project uses Jacoco to measure unit tests code coverage, to generate a report run the following command:

    `./gradlew clean build jacocoTestReport`

Generated reports can be found in build/code-coverage directory.


Versions
============

The latest stable release can be viewed on the develop branch: [Latest Release on Develop](https://github.com/apache/fineract/tree/develop "Latest Release").

The progress of this project can be viewed here: [View change log](https://github.com/apache/fineract/blob/develop/CHANGELOG.md "Latest release change log")


License
============

This project is licensed under Apache License Version 2.0. See <https://github.com/apache/fineract/blob/develop/APACHE_LICENSETEXT.md> for reference.

The Connector/J JDBC Driver client library from MariaDB.org, which is licensed under the LGPL,
is used in development when running integration tests that use the Liquibase library.  That JDBC
driver is however not included in and distributed with the Fineract product and is not
required to use the product.
If you are developer and object to using the LGPL licensed Connector/J JDBC driver,
simply do not run the integration tests that use the Liquibase library and/or use another JDBC driver.
As discussed in [LEGAL-462](https://issues.apache.org/jira/browse/LEGAL-462), this project therefore
complies with the [Apache Software Foundation third-party license policy](https://www.apache.org/legal/resolved.html).


<br><br>APACHE FINERACT PLATFORM API
============

The API for Fineract is documented in [apiLive.htm](fineract-provider/src/main/resources/static/legacy-docs/apiLive.htm), and the [apiLive.htm can be viewed on fineract.apache.org](https://fineract.apache.org/docs/legacy/ "API Documentation").  If you have your own Fineract instance running, you can find this documentation under [/fineract-provider/legacy-docs/apiLive.htm](https://localhost:8443/fineract-provider/legacy-docs/apiLive.htm).

The Swagger documentation (work in progress; see [FINERACT-733](https://issues.apache.org/jira/browse/FINERACT-733)) can be accessed under [/fineract-provider/swagger-ui/index.html](https://localhost:8443/fineract-provider/swagger-ui/index.html) and [live Swagger UI here on Fineract.dev](https://sandbox.mifos.community/fineract-provider/swagger-ui/index.html).

Apache Fineract supports client code generation using [Swagger Codegen](https://github.com/swagger-api/swagger-codegen) based on the [OpenAPI Specification](https://swagger.io/specification/).  For more instructions on how to generate the client code, check [fineract-doc/src/docs/en/chapters/sdk/client.adoc](fineract-doc/src/docs/en/chapters/sdk/client.adoc).


<br>API CLIENTS (Web UIs, Mobile, etc.)
============

* https://github.com/openMF/community-app/ is the "traditional" Reference Client App Web UI for the API offered by this project
* https://github.com/openMF/web-app is the next generation UI rewrite also using this project's API
* https://github.com/openMF/android-client is an Android Mobile App client for this project's API
* https://github.com/openMF has more related proejcts


<br>ONLINE DEMOS
============

* [sandbox.mifos.community](https://sandbox.mifos.community) always runs the latest version of this code
* [demo.mifos.io](https://demo.mifos.io) A demo account is provided for users to experience the functionality of the Community App.  Users can use "mifos" for USERNAME and "password" for PASSWORD (without quotation marks).
* [Swagger-UI Demo video](https://www.youtube.com/watch?v=FlVd-0YAo6c) This is a demo video for Swagger-UI documentation, more information [here](https://github.com/apache/fineract#swagger-ui-documentation).


<br>DEVELOPERS
============
Please see <https://cwiki.apache.org/confluence/display/FINERACT/Contributor%27s+Zone> for the developers wiki page.

Please refer to <https://cwiki.apache.org/confluence/display/FINERACT/Fineract+101> for the first-time contribution to this project.

Please see <https://cwiki.apache.org/confluence/display/FINERACT/How-to+articles> for technical details to get started.

Please visit [our JIRA Dashboard](https://issues.apache.org/jira/secure/Dashboard.jspa?selectPageId=12335824) to find issues to work on, see what others are working on, or open new issues.


<br>VIDEO DEMONSTRATION
============

Apache Fineract / Mifos X Demo (November 2016) - <https://www.youtube.com/watch?v=h61g9TptMBo>

<br>SWAGGER UI DEMONSTRATION
============

We use Swagger-UI to generate and maintain our API documentation, you can see the demo video [here](https://www.youtube.com/watch?v=FlVd-0YAo6c) or a live version
[here](https://sandbox.mifos.community/fineract-provider/swagger-ui/index.html). If you interested to know more about Swagger-UI you can check their [website](https://swagger.io/).

<br>GORVENANCE AND POLICIES
=======================

[Becoming a Committer](https://cwiki.apache.org/confluence/display/FINERACT/Becoming+a+Committer)
documents the process through which you can become a committer in this project.


<br>ERROR HANDLING GUIDELINES
------------------
* When catching exceptions, either rethrow them, or log them.  Either way, include the root cause by using `catch (SomeException e)` and then either `throw AnotherException("..details..", e)` or `LOG.error("...context...", e)`.
* Completely empty catch blocks are VERY suspicous!  Are you sure that you want to just "swallow" an exception?  Really, 100% totally absolutely sure?? ;-) Such "normal exceptions which just happen sometimes but are actually not really errors" are almost always a bad idea, can be a performance issue, and typically are an indication of another problem - e.g. the use of a wrong API which throws an Exception for an expected condition, when really you would want to use another API that instead returns something empty or optional.
* In tests, you'll typically never catch exceptions, but just propagate them, with `@Test void testXYZ() throws SomeException, AnotherException`..., so that the test fails if the exception happens.  Unless you actually really want to test for the occurence of a problem - in that case, use [JUnit's Assert.assertThrows()](https://github.com/junit-team/junit4/wiki/Exception-testing) (but not `@Test(expected = SomeException.class)`).
* Never catch `NullPointerException` & Co.

<br>LOGGING GUIDELINES
------------------
* We use [SLF4J](http://www.slf4j.org) as our logging API.
* Never, ever, use `System.out` and `System.err` or `printStackTrace()` anywhere, but always `LOG.info()` or `LOG.error()` instead.
* Use placeholder (`LOG.error("Could not... details: {}", something, exception)`) and never String concatenation (`LOG.error("Could not... details: " + something, exception)`)
* Which Log Level is appropriate?
    * `LOG.error()` should be used to inform an "operator" running Fineract who supervises error logs of an unexpected condition.  This includes technical problems with an external "environment" (e.g. can't reach a database), and situations which are likely bugs which need to be fixed in the code.  They do NOT include e.g. validation errors for incoming API requests - that is signaled through the API response - and does (should) not be logged as an error.  (Note that there is no _FATAL_ level in SLF4J; a "FATAL" event should just be logged as an _ERROR_.)
    * `LOG.warn()` should be using sparingly.  Make up your mind if it's an error (above) - or not!
    * `LOG.info()` can be used notably for one-time actions taken during start-up.  It should typically NOT be used to print out "regular" application usage information.  The default logging configuration always outputs the application INFO logs, and in production under load, there's really no point to constantly spew out lots of information from frequently traversed paths in the code about what's going on.  (Metrics are a better way.)  `LOG.info()` *can* be used freely in tests though.
    * `LOG.debug()` can be used anywhere in the code to log things that may be useful during investigations of specific problems.  They are not shown in the default logging configuration, but can be enabled for troubleshooting.  Developers should typically "turn down" most `LOG.info()` which they used while writing a new feature to "follow along what happens during local testing" to `LOG.debug()` for production before we merge their PRs.
    * `LOG.trace()` is not used in Fineract.

Pull Requests
-------------

We request that your commit message include a FINERACT JIRA issue, recommended to be put in parentheses at the end of the first line.  Start with an upper case imperative verb (not past form), and a short but concise clear description. (E.g. _Add enforced HideUtilityClassConstructor checkstyle (FINERACT-821)_ or _Fix inability to reschedule when interest accrued larger than EMI (FINERACT-1109)_ etc.).

If your PR is failing to pass our CI build due to a test failure, then:

1. Understand if the failure is due to your PR or an unrelated unstable test.
1. If you suspect it is because of a "flaky" test, and not due to a change in your PR, then please do not simply wait for an active maintainer to come and help you, but instead be a proactive contributor to the project - see next steps.  Do understand that we may not review PRs that are not green - it is the contributor's (that's you!) responsability to get a proposed PR to pass the build, not primarily the maintainers.
1. Search for the name of the failed test on https://issues.apache.org/jira/, e.g. for `AccountingScenarioIntegrationTest` you would find [FINERACT-899](https://issues.apache.org/jira/browse/FINERACT-899).
1. If you happen to read in such bugs that tests were just recently fixed, or ignored, then rebase your PR to pick up that change.
1. If you find previous comments "proving" that the same test has arbitrarily failed in at least 3 past PRs, then please do yourself raise a small separate new PR proposing to add an `@Disabled // TODO FINERACT-123` to the respective unstable test (e.g. [#774](https://github.com/apache/fineract/pull/774)) with the commit message mentioning said JIRA, as always.  (Please do NOT just `@Disabled` any existing tests mixed in as part of your larger PR.)
1. If there is no existing JIRA for the test, then first please evaluate whether the failure couldn't be a (perhaps strange) impact of the change you are proposing after all.  If it's not, then please raise a new JIRA to document the suspected Flaky Test, and link it to [FINERACT-850](https://issues.apache.org/jira/browse/FINERACT-850).  This will allow the next person coming along hitting the same test failure to easily find it, and eventually propose to ignore the unstable test.
1. Then (only) Close and Reopen your PR, which will cause a new build, to see if it passes.
1. Of course, we very much appreciate you then jumping onto any such bugs and helping us figure out how to fix all ignored tests!

[Pull Request Size Limit](https://cwiki.apache.org/confluence/display/FINERACT/Pull+Request+Size+Limit)
documents that we cannot accept huge "code dump" Pull Requests, with some related suggestions.

Guideline for new Feature commits involving Refactoring: If you are submitting PR for a new Feature,
and it involves refactoring, try to differentiate "new Feature code" with "Refactored" by placing
them in different commits. This helps review to review your code faster.

We have an automated Bot which marks pull requests as "stale" after a while, and ultimately automatically closes them.


Merge Strategy
--------------

This project's committers typically prefer to bring your Pull Requests in through _Rebase and Merge_ instead of _Create a Merge Commit_. (If you are unfamiliar with GitHub's UI re. this, note the somewhat hidden little triangle drop-down at the bottom of PR, visible only to committers, not contributors.)  This avoids the "merge commits" which we consider to be somewhat "polluting" the projects commits log history view.  We understand this doesn't give an easy automatic reference to the original PR (which GitHub automatically adds to the Merge Commit message it generates), but we consider this an only very minor inconvenience; it's typically relatively easy to find the original PR even just from the commit message, and JIRA.

We expect most proposed PRs to typically consist of a single commit.  Committers may use _Squash and merge_ to combine your commits at merge time, and if they do so will rewrite your commit message as they see fit.

Neither of these two are hard absolute rules, but mere conventions.  Multiple commits in single PR make sense in certain cases (e.g. branch backports).


Dependency Upgrades
-------------------

This project uses a number of 3rd-party libraries, and this section provides some guidance for their updates. We have set-up [Renovate's bot](https://renovate.whitesourcesoftware.com) to automatically raise Pull Requests for our review when new dependencies are available [FINERACT-962](https://issues.apache.org/jira/browse/FINERACT-962).

Upgrades sometimes require package name changes.  Changed code should ideally have test coverage.

Our `ClasspathHellDuplicatesCheckRuleTest` detects classes that appear in more than 1 JAR.  If a version bump in [`build.gradle`](https://github.com/search?q=repo%3Aapache%2Ffineract+filename%3Abuild.gradle&type=Code&ref=advsearch&l=&l=) causes changes in transitives dependencies, then you may have to add related `exclude` to our [`dependencies.gradle`](https://github.com/apache/fineract/search?q=dependencies.gradle).  Running `./gradlew dependencies` helps to understand what is required.


More Information
============
More details of the project can be found at <https://cwiki.apache.org/confluence/display/FINERACT>.
