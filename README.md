Apache Fineract: A Platform for Microfinance [![Build Status](https://travis-ci.org/apache/fineract.svg?branch=develop)](https://travis-ci.org/apache/fineract)
============
Fineract is a mature platform with open APIs that provides a reliable, robust, and affordable core banking solution for financial institutions offering services to the world’s 2 billion underbanked and unbanked. 

Requirements
============
* Java >= 1.8 (Oracle JVMs have been tested)
* MySQL 5.5

You can run the required version of the database server in a container, instead of having to install it, like this:

    docker run --name mysql-5.5 -p 3306:3306 -e MYSQL_ROOT_PASSWORD=mysql -d mysql:5.5
    
and stop and destroy it like this:

    docker rm -f mysql-5.5

Beware that this database container database keeps its state inside the container and not on the host filesystem.  It is lost when you destroy (rm) this container.  This is typically fine for development.  See [Caveats: Where to Store Data on the database container documentation](https://hub.docker.com/_/mysql) re. how to make it persistant instead of ephemeral.

Instructions to download gradle wrapper
============
The file fineract-provider/gradle/wrapper/gradle-wrapper.jar binary is checked into this projects's Git source repository,
but won't exist in your copy of the Fineract codebase if you downloaded a released source archive from apache.org.
In that case, you need to downloaded it using the commands below:

wget --no-check-certificate -P fineract-provider/gradle/wrapper https://github.com/apache/fineract/raw/develop/fineract-provider/gradle/wrapper/gradle-wrapper.jar

(or)

curl --insecure -L https://github.com/apache/fineract/raw/develop/fineract-provider/gradle/wrapper/gradle-wrapper.jar > fineract-provider/gradle/wrapper/gradle-wrapper.jar

Instructions to run Apache RAT (Release Audit Tool)
============
1. Extract the archive file to your local directory.
2. Run `./gradlew rat`. A report will be generated under build/reports/rat/rat-report.txt

Instructions to build a war file
============
1. Extract the archive file to your local directory.
2. Run `./gradlew clean war` or `./gradlew build` to build deployable war file which will be created at build/libs directory.


Instructions to execute Integration tests
============
> Note that if this is the first time to access MySQL DB, then you may need to reset your password. 

Run the following commands, very similarly to how [.travis.yml](.travis.yml) does:
1. `./gradlew createDB -PdbName=mifosplatform-tenants`
1. `./gradlew createDB -PdbName=mifostenant-default`
1. `./gradlew clean integrationTest`


Instructions to run using Docker and docker-compose
===================================================

It is possible to do a 'one-touch' installation of Fineract using docker-compose

  Prerequisites:
  * docker and docker-compose installed on your machine


  Installing a new Fineract instance:

  * Clone the Fineract Github repository
  * Navigate to the docker directory
  * Run the following commands:
      * docker-compose build
      * docker-compose up -d
  * Fineract will run at https://localhost:8443/fineract-provider


Version
============

The latest stable release can be viewed on the develop branch: [Latest Release on Develop](https://github.com/apache/fineract/tree/develop "Latest Release").

The progress of this project can be viewed here: [View change log](https://github.com/apache/fineract/blob/develop/CHANGELOG.md "Latest release change log")

License
============

This project is licensed under Apache License Version 2.0. See <https://github.com/apache/incubator-fineract/blob/develop/LICENSE.md> for referece.

The Connector/J JDBC Driver client library from MariaDB.org, which is licensed under the LGPL,
is used in development when running integration tests that use the Flyway library.  That JDBC
driver is however not included in and distributed with the Fineract product, and is not
required to use the product.
If you are developer and object to using the LGPL licensed Connector/J JDBC driver,
simply do not run the integration tests that use the Flyway library.
As discussed in [LEGAL-462](https://issues.apache.org/jira/browse/LEGAL-462), this project therefore
complies with the [Apache Software Foundation third-party license policy](https://www.apache.org/legal/resolved.html).


Apache Fineract Platform API
============

The API for the Fineract-platform (project named 'Apache Fineract') is documented in the API-docs under <b><i>Full API Matrix</i></b> and can be viewed [here](https://demo.openmf.org/api-docs/apiLive.htm "API Documentation").

Online Demos
============

* [Community App](https://demo.openmf.org "Reference Client App")
> For this demo, a demo account is also provided for users to experience the functionality of this Community App. Users can use "mifos" for USERNAME and "password" for PASSWORD(without quotation marks). 

Developers
============
Please see <https://cwiki.apache.org/confluence/display/FINERACT/Contributor%27s+Zone> for the developers wiki page.

Please refer to <https://cwiki.apache.org/confluence/display/FINERACT/Fineract+101> for first time contribution for this project.

Please see <https://cwiki.apache.org/confluence/display/FINERACT/How-to+articles> for technical details to get started.


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

[Pull Request Size Limit](https://cwiki.apache.org/confluence/display/FINERACT/Pull+Request+Size+Limit)
documents that we cannot accept huge "code dump" Pull Requests, with some related suggestions.

More Information
============
More details of the project can be found at <https://cwiki.apache.org/confluence/display/FINERACT>.
