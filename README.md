Apache Fineract: A Platform for Microfinance [![Build Status](https://travis-ci.org/apache/fineract.svg?branch=develop)](https://travis-ci.org/apache/fineract)
============
Fineract is a mature platform with open APIs that provides a reliable, robust, and affordable core banking solution for financial institutions offering services to the world’s 2 billion underbanked and unbanked. 

Requirements
============
* Java >= 1.8 (Oracle JVMs have been tested)
* gradle-wrapper.jar version 2.10
* MySQL 5.5

Instructions to download gradle wrapper
============
If the file fineract-provider/gradle/wrapper/gradle-wrapper.jar doesn't already exist in your copy of the Fineract codebase, the same needs to be downloaded using the commands below

wget --no-check-certificate -P fineract-provider/gradle/wrapper https://github.com/apache/fineract/raw/develop/fineract-provider/gradle/wrapper/gradle-wrapper.jar

(or)

curl --insecure -L https://github.com/apache/fineract/raw/develop/fineract-provider/gradle/wrapper/gradle-wrapper.jar > fineract-provider/gradle/wrapper/gradle-wrapper.jar

Instructions to run Apache RAT (Release Audit Tool)
============
1. Extract the archive file to your local directory.
2. Download gradle-wrapper.jar version 2.10 and place it in the fineract-provider/gradle/wrapper folder. See 'Instructions to download gradle wrapper' above.
3. Run `./gradlew rat`. A report will be generated under build/reports/rat/rat-report.txt

Instructions to build a war file
============
1. Extract the archive file to your local directory.
2. Ensure gradle-wrapper.jar version 2.10 is present in the fineract-provider/gradle/wrapper folder. See 'Instructions to download gradle wrapper' above.
3. Run `./gradlew clean war` or `./gradlew build` to build deployable war file which will be created at build/libs directory.


Instructions to execute Integration tests
============
1. Login to mysql DB using `mysql -u root -p mysql`
> Note that if this is the first time to access MySQL DB, then you may need to reset your password. 
2. Create the mifosplatform-tenants database using `CREATE DATABASE mifosplatform-tenants`.
3. Create the default tenant database using `CREATE DATABASE mifostenant-default`.
4. Download gradle-wrapper.jar version 2.10 and place it in the fineract-provider/gradle/wrapper folder. See 'Instructions to download gradle wrapper' above.
5. Run the following commands:
    1. `./gradlew migrateTenantListDB -PdbName=mifosplatform-tenants`
    2. `./gradlew migrateTenantDB -PdbName=mifostenant-default`
6. Run `./gradlew clean integrationTest`
7. Run `./gradlew tomcatRunWar`

Version
============

The latest stable release can be viewed on the develop branch: [Latest Release on Develop](https://github.com/apache/fineract/tree/develop "Latest Release").

The progress of this project can be viewed here: [View change log](https://github.com/apache/fineract/blob/develop/CHANGELOG.md "Latest release change log")

License
============

This project is licensed under Apache License Version 2.0. See <https://github.com/apache/incubator-fineract/blob/develop/LICENSE.md> for referece.

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

More Information
============
More details of the project can be found at <https://cwiki.apache.org/confluence/display/FINERACT>.
