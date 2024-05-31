# Apache Fineract: A Platform for Microfinance

[![Swagger Validation](https://validator.swagger.io/validator?url=https://demo.fineract.dev/fineract-provider/swagger-ui/fineract.yaml)](https://validator.swagger.io/validator/debug?url=https://demo.fineract.dev/fineract-provider/swagger-ui/fineract.yaml) [![build](https://github.com/apache/fineract/actions/workflows/build.yml/badge.svg)](https://github.com/apache/fineract/actions/workflows/build.yml) [![Docker Hub](https://img.shields.io/docker/pulls/apache/fineract.svg?logo=Docker)](https://hub.docker.com/r/apache/fineract)  [![Docker Build](https://img.shields.io/docker/cloud/build/apache/fineract.svg?logo=Docker)](https://hub.docker.com/r/apache/fineract/builds) [![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=apache_fineract&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=apache_fineract)

Fineract is a mature platform with open APIs that provides a reliable, robust, and affordable core banking solution for financial institutions offering services to the world’s 3 billion underbanked and unbanked.

[Have a look at the FAQ on our Wiki at apache.org](https://cwiki.apache.org/confluence/display/FINERACT/FAQ) if this README does not answer what you are looking for.  [Visit our JIRA Dashboard](https://issues.apache.org/jira/secure/Dashboard.jspa?selectPageId=12335824) to find issues to work on, see what others are working on, or open new issues.

[![Code Now! (Gitpod)](https://gitpod.io/button/open-in-gitpod.svg)](https://gitpod.io/#https://github.com/apache/fineract)
to start contributing to this project in the online web-based IDE GitPod.io right away!
(You may initially have to press F1 to Find Command and run "Java: Start Language Server".)
It's of course also possible to contribute with a "traditional" local development environment (see below).

## Contributing

If you are interested in contributing to this project, but perhaps don't quite know how and where to get started, please [join our developer mailing list](http://fineract.apache.org/#contribute), listen into our conversations, chime into threads, and just send us a "Hello!" introduction email; we're a friendly bunch, and look forward to hearing from you.

[Here](https://fineract.apache.org/docs/current/#_contributing) is a more complete guide to help you with your first contribution to Fineract.

## Development Environment Setup

Please refer to our [dev guide](https://fineract.apache.org/docs/current/#_fineract_development_guide) for a comprehensive set up guide.


## Versions

The latest stable release can be viewed on the develop branch: [Latest Release on Develop](https://github.com/apache/fineract/tree/develop "Latest Release").

The progress of this project can be viewed here: [View change log](https://github.com/apache/fineract/blob/develop/CHANGELOG.md "Latest release change log")


## License

This project is licensed under Apache License Version 2.0. See <https://github.com/apache/incubator-fineract/blob/develop/LICENSE.md> for reference.

The Connector/J JDBC Driver client library from MariaDB.org, which is licensed under the LGPL,
is used in development when running integration tests that use the Liquibase library.  That JDBC
driver is however not included in and distributed with the Fineract product and is not
required to use the product.
If you are developer and object to using the LGPL licensed Connector/J JDBC driver,
simply do not run the integration tests that use the Liquibase library and/or use another JDBC driver.
As discussed in [LEGAL-462](https://issues.apache.org/jira/browse/LEGAL-462), this project therefore
complies with the [Apache Software Foundation third-party license policy](https://www.apache.org/legal/resolved.html).

## Online Demos

* [sandbox.mifos.community](https://sandbox.mifos.community) always runs the latest version of this code
* [demo.mifos.io](https://demo.mifos.io) A demo account is provided for users to experience the functionality of the Community App.  Users can use "mifos" for USERNAME and "password" for PASSWORD (without quotation marks).
* [Swagger-UI Demo video](https://www.youtube.com/watch?v=FlVd-0YAo6c) This is a demo video for Swagger-UI documentation, more information [here](https://github.com/apache/fineract#swagger-ui-documentation).

    
Apache Fineract / Mifos X Demo (November 2016) - <https://www.youtube.com/watch?v=h61g9TptMBo>

## More Information

More details of the project can be found on our official documentation [here](https://fineract.apache.org/docs/current/) and on our wiki [here](https://cwiki.apache.org/confluence/display/FINERACT).
