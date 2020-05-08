Apache Fineract: A Platform for Microfinance  [![Build Status](https://travis-ci.org/apache/fineract.svg?branch=develop)](https://travis-ci.org/apache/fineract)  [![Docker Hub](https://img.shields.io/docker/pulls/apache/fineract.svg)](https://hub.docker.com/r/apache/fineract)  [![Docker Build](https://img.shields.io/docker/cloud/build/apache/fineract.svg)](https://hub.docker.com/r/apache/fineract/builds)
============

Fineract is a mature platform with open APIs that provides a reliable, robust, and affordable core banking solution for financial institutions offering services to the world’s 2 billion underbanked and unbanked.

[Have a look at the FAQ on our Wiki at apache.org](https://cwiki.apache.org/confluence/display/FINERACT/FAQ) if this README does not answer what you are looking for.

[![Code Now! (Gitpod)](https://gitpod.io/button/open-in-gitpod.svg)](https://gitpod.io/#https://github.com/apache/fineract)
to start contributing to this project in the online web-based IDE GitPod.io right away!
(You may initially have to press F1 to Find Command and run "Java: Start Language Server".)
It's of course also possible to contribute with a "traditional" local development environment (see below).

Community
=========

If you are interested in contributing to this project, but perhaps don't quite know how and where to get started, please [join our developer mailing list](http://fineract.apache.org/#contribute), listen into our conversations, chime into threads, and just send us a friendly "Hello!" introduction email; we're a friendly bunch, and look forward to hearing from you.


Requirements
============
* Java >= 11 (OpenJDK JVM is tested by our CI on Travis)
* MySQL 5.5

You can run the required version of the database server in a container, instead of having to install it, like this:

    docker run --name mysql-5.5 -p 3306:3306 -e MYSQL_ROOT_PASSWORD=mysql -d mysql:5.5

and stop and destroy it like this:

    docker rm -f mysql-5.5

Beware that this database container database keeps its state inside the container and not on the host filesystem.  It is lost when you destroy (rm) this container.  This is typically fine for development.  See [Caveats: Where to Store Data on the database container documentation](https://hub.docker.com/_/mysql) re. how to make it persistent instead of ephemeral.

Tomcat v9 is only required if you wish to deploy the Fineract WAR to a separate external servlet container.  Note that you do not require to install Tomcat to develop Fineract, or to run it in production if you use the self-contained JAR, which transparently embeds a servlet container using Spring Boot.  (Until FINERACT-730, Tomcat 7/8 were also supported, but now Tomcat 9 is required.)


Instructions how to run for local development
============

Run the following commands:
1. `./gradlew createDB -PdbName=fineract_tenants`
1. `./gradlew createDB -PdbName=fineract_default`
1. `./gradlew bootRun`


Instructions to download Gradle wrapper
============
The file fineract-provider/gradle/wrapper/gradle-wrapper.jar binary is checked into this projects Git source repository,
but won't exist in your copy of the Fineract codebase if you downloaded a released source archive from apache.org.
In that case, you need to download it using the commands below:

    wget --no-check-certificate -P fineract-provider/gradle/wrapper https://github.com/apache/fineract/raw/develop/fineract-provider/gradle/wrapper/gradle-wrapper.jar

(or)

    curl --insecure -L https://github.com/apache/fineract/raw/develop/fineract-provider/gradle/wrapper/gradle-wrapper.jar > fineract-provider/gradle/wrapper/gradle-wrapper.jar


Instructions to run Apache RAT (Release Audit Tool)
============
1. Extract the archive file to your local directory.
2. Run `./gradlew rat`. A report will be generated under build/reports/rat/rat-report.txt


Instructions to build the JAR file
============
1. Extract the archive file to your local directory.
2. Run `./gradlew clean bootJar` to build a modern cloud native fully self contained JAR file which will be created at `build/libs` directory.
3. Start it using `java -jar build/libs/fineract-provider.jar` (does not require external Tomcat)


Instructions to build a WAR file
============
1. Extract the archive file to your local directory.
2. Run `./gradlew clean bootWar` to build a traditional WAR file which will be created at `build/libs` directory.  
3. Deploy this WAR to your Tomcat v9 Servlet Container.

We recommend using the JAR instead of the WAR file deployment, because it's much easier.


Instructions to execute Integration Tests
============
> Note that if this is the first time to access MySQL DB, then you may need to reset your password.

Run the following commands, very similarly to how [.travis.yml](.travis.yml) does:
1. `./gradlew createDB -PdbName=fineract_tenants`
1. `./gradlew createDB -PdbName=fineract_default`
1. `./gradlew clean integrationTest`


Instructions to run using Docker and docker-compose
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
1. `docker-compose build`
1. `docker-compose up -d`
1. fineract (back-end) is running at https://localhost:8443/fineract-provider/
1. wait for https://localhost:8443/fineract-provider/actuator/health to return `{"status":"UP"}`
1. you must go to https://localhost:8443 and remember to accept the self-signed SSL certificate of the API once in your browser, otherwise  you get a message that is rather misleading from the UI.
1. community-app (UI) is running at http://localhost:9090/?baseApiUrl=https://localhost:8443/fineract-provider&tenantIdentifier=default
1. login using default _username_ `mifos` and _password_ `password`

The [`docker-compose.yml`](docker-compose.yml) will build the `fineract` container from the source based on the [`Dockerfile`](Dockerfile).  You could change that to use the pre-built container image instead of having to re-build it.

https://hub.docker.com/r/apache/fineract has a pre-built container image of this project, built continuously.

You must specify the MySQL tenants database JDBC URL by passing it to the `fineract` container via environment
variables; please consult the [`docker-compose.yml`](docker-compose.yml) for exact details how to specify those.
_(Note that in previous versions, the `mysqlserver` environment variable used at `docker build` time instead of at
`docker run` time did something similar; this has changed in [FINERACT-773](https://issues.apache.org/jira/browse/FINERACT-773)),
and the `mysqlserver` environment variable is now no longer supported.)_


Instructions to run on Kubernetes
=================================

General Clusters
----------------

You can also run Fineract using containers on a Kubernetes cluster.
Make sure you set up and connect to your Kubernetes cluster.
You can follow [this](https://cwiki.apache.org/confluence/display/FINERACT/Install+and+configure+kubectl+and+Google+Cloud+SDK+on+ubuntu+16.04) guide to set up a Kubernetes cluster on GKE. Make sure to replace `apache-fineract-cn` with `apache-fineract`

Now e.g. from your Google Cloud shell, run the following commands:

1. `git clone https://github.com/apache/fineract.git ; cd fineract/kubernetes`
1. `./kubectl-startup`

To shutdown and reset your Cluster, run:

    ./kubectl-shutdown

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

    ./kubectl-shutdown

We have [some open issues in JIRA with Kubernetes related enhancement ideas](https://jira.apache.org/jira/browse/FINERACT-783?jql=labels%20%3D%20kubernetes%20AND%20project%20%3D%20%22Apache%20Fineract%22%20) which you are welcome to contribute to.


Checkstyle
============

This project enforces its code conventions using [checkstyle.xml](fineract-provider/config/checkstyle/checkstyle.xml).  It is configured to run automatically during the normal Gradle build, and fail if there are any style violations detected.
We recommend that you configure your favourite Java IDE to match those conventions.  For Eclipse, you can
File > Import > General > Preferences our [config/fineractdev-eclipse-preferences.epf](config/fineractdev-eclipse-preferences.epf).
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

This project is licensed under Apache License Version 2.0. See <https://github.com/apache/incubator-fineract/blob/develop/LICENSE.md> for reference.

The Connector/J JDBC Driver client library from MariaDB.org, which is licensed under the LGPL,
is used in development when running integration tests that use the Flyway library.  That JDBC
driver is however not included in and distributed with the Fineract product and is not
required to use the product.
If you are developer and object to using the LGPL licensed Connector/J JDBC driver,
simply do not run the integration tests that use the Flyway library.
As discussed in [LEGAL-462](https://issues.apache.org/jira/browse/LEGAL-462), this project therefore
complies with the [Apache Software Foundation third-party license policy](https://www.apache.org/legal/resolved.html).


Apache Fineract Platform API
============

The API for the Fineract-platform (project named 'Apache Fineract') is documented in the API-docs under <b><i>Full API Matrix</i></b> and can be viewed [here](https://demo.mifos.io/api-docs/apiLive.htm "API Documentation").


API clients (Web UIs, Mobile, etc.)
============

* https://github.com/openMF/community-app/ is the "traditional" Reference Client App Web UI for the API offered by this project
* https://github.com/openMF/web-app is the next generation UI rewrite also using this project's API
* https://github.com/openMF/android-client is an Android Mobile App client for this project's API
* https://github.com/openMF has more related proejcts


Online Demos
============

* [fineract.dev](https://www.fineract.dev) always runs the latest version of this code
* [demo.mifos.io](https://demo.mifos.io) A demo account is provided for users to experience the functionality of the Community App.  Users can use "mifos" for USERNAME and "password" for PASSWORD (without quotation marks).


Developers
============
Please see <https://cwiki.apache.org/confluence/display/FINERACT/Contributor%27s+Zone> for the developers wiki page.

Please refer to <https://cwiki.apache.org/confluence/display/FINERACT/Fineract+101> for the first-time contribution to this project.

Please see <https://cwiki.apache.org/confluence/display/FINERACT/How-to+articles> for technical details to get started.

Please visit <https://issues.apache.org/jira/projects/FINERACT/> to open or find issues.

Roadmap
============

[Project Release Roadmap on JIRA (Detailed View)](https://issues.apache.org/jira/browse/FINERACT-268?jql=project%20%3D%20FINERACT "Project Release Roadmap on JIRA (Detailed View)")


Video Demonstration
============

Apache Fineract / Mifos X Demo (November 2016) - <https://www.youtube.com/watch?v=h61g9TptMBo>


Governance and Policies
=======================

[Becoming a Committer](https://cwiki.apache.org/confluence/display/FINERACT/Becoming+a+Committer)
documents the process through which you can become a committer in this project.


Pull Requests
-------------

If your PR is failing to pass our CI build due to a test failure, then:

1. Understand if the failure is due to your PR or an unrelated unstable test.
1. If you suspect it is because of a "flaky" test, and not due to a change in your PR, then please do not simply wait for an active maintainer to come and help you, but instead be a proactive contributor to the project - see next steps.
1. Search for the name of the failed test on https://issues.apache.org/jira/, e.g. for `AccountingScenarioIntegrationTest` you would find [FINERACT-899](https://issues.apache.org/jira/browse/FINERACT-899).
1. If you happen to read in such bugs that tests were just recently fixed, or ignored, then rebase your PR to pick up that change.
1. If you find previous comments "proving" that the same test has arbitrarily failed in at least 3 past PRs, then please do yourself raise a small separate new PR proposing to add an `@Ignore // TODO FINERACT-123` to the respective unstable test (e.g. [#774](https://github.com/apache/fineract/pull/774)) with the commit message mentioning said JIRA, as always.  (Please do NOT just `@Ignore` any existing tests mixed in as part of your larger PR.)
1. If there is no existing JIRA for the test, then first please evaluate whether the failure couldn't be a (perhaps strange) impact of the change you are proposing after all.  If it's not, then please raise a new JIRA to document the suspected Flaky Test, and link it to [FINERACT-850](https://issues.apache.org/jira/browse/FINERACT-850).  This will allow the next person coming along hitting the same test failure to easily find it, and eventually propose to ignore the unstable test.
1. Then (only) Close and Reopen your PR, which will cause a new build, to see if it passes.
1. Of course, we very much appreciate you then jumping onto any such bugs and helping us figure out how to fix all ignored tests!

[Pull Request Size Limit](https://cwiki.apache.org/confluence/display/FINERACT/Pull+Request+Size+Limit)
documents that we cannot accept huge "code dump" Pull Requests, with some related suggestions.

Guideline for new Feature commits involving Refactoring: If you are submitting PR for a new Feature,
and it involves refactoring, try to differentiate "new Feature code" with "Refactored" by placing
them in different commits. This helps review to review your code faster.


Releasing
---------

[How to Release Apache Fineract](https://cwiki.apache.org/confluence/x/DRwIB) documents the process how we make the source code that is available here in this Git repository into a binary release ZIP available on http://fineract.apache.org.


More Information
============
More details of the project can be found at <https://cwiki.apache.org/confluence/display/FINERACT>.
