Generate Apache Fineract API Client
============

Apache Fineract supports client code generation using [OpenAPI Generator](https://openapi-generator.tech). It uses [OpenAPI Specification Version 3.0.3](https://swagger.io/specification/).

## Generate API Client

The API client is built as part of the overall Fineract Gradle build. If you want to save (maybe) some time you can try to execute just the following line in root directory of the project:

- Run `./gradlew :fineract-client:build`

The client JAR can be found in `fineract-client/build/libs`.

Note: Build only `fineract-client` may or may not actually save you some build time. There are still project module dependencies that might trigger a complete build.

## Validate OpenAPI Spec File

The `resolve` task in [build.gradle](https://github.com/apache/fineract/blob/develop/fineract-provider/build.gradle#L80) file will generate the OpenAPI Spec File for the project. To make sure Swagger Codegen generates a correct library, it is important for the OpenAPI Spec file to be valid. Validation is done automatically by the OpenAPI code generator Gradle plugin. If you still have problems during code generation please use [Swagger OpenAPI Validator](https://validator.swagger.io/) to validate the spec file.
