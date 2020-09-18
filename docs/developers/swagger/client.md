Generate Apache Fineract API Client
============

Apache Fineract supports client code generation using [Swagger Codegen](https://github.com/swagger-api/swagger-codegen). Project supports [all clients](https://github.com/swagger-api/swagger-codegen#overview) supported by Swagger Codegen. It uses [OpenAPI Specification Version 3.0.3](https://swagger.io/specification/).

## Generate API Client

In root directory of the project:

- Run `./gradlew resolve`
- Run `./gradlew generateSwaggerCode`

The client code will be generated under `build/swagger-code-fineract`

## Build API Client

- Run `cd build/swagger-code-fineract`
- Run `./gradlew build`

Alternatively, if you have Maven installed on your system, you can also do:

- Run `cd build/swagger-code-fineract`
- Run `mvn clean package`

## Customize Generated Code

Swagger Codegen provides several options to customize the generated code. [Here](https://openapi-generator.tech/docs/generators/java/) are the options available for customization.

- Open the [config.json.template](https://github.com/apache/fineract/blob/develop/fineract-provider/config/swagger/config.json.template) file
- Customize options
- Build the project again as mentioned in **Generate API Client Code** section
  
## Customize using Mustache Templates

Swagger Codegen uses Mustache Templates for generating the client library. For additional customizations you can add/edit custom templates inside the `fineract-provider/config/swagger/templates` folder. 

Make sure you are following the supported templates. Otherwise, the generated code will not build correctly.

- [Java Mustache Templates](https://github.com/swagger-api/swagger-codegen/tree/master/modules/swagger-codegen/src/main/resources/Java)
- [Retrofit2 Mustache Templates](https://github.com/swagger-api/swagger-codegen/tree/master/modules/swagger-codegen/src/main/resources/Java/libraries/retrofit2)

If you need to add templates for a specific library, you need to follow the same directory structure as present in the links above.

## Validate OpenAPI Spec File

The `resolve` task in [build.gradle](https://github.com/apache/fineract/blob/develop/fineract-provider/build.gradle#L212) file will generate the OpenAPI Spec File for the project. To make sure Swagger Codegen generates a correct library, it is important for the OpenAPI Spec file to be valid. Use [Swagger OpenAPI Validator](https://validator.swagger.io/) to validate the spec file.
