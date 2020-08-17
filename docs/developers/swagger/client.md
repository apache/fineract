Generate Apache Fineract API Client
============

Apache Fineract supports client code generation using [Swagger Codegen](https://github.com/swagger-api/swagger-codegen). Project supports [all clients](https://github.com/swagger-api/swagger-codegen#overview) supported by Swagger Codegen. It uses [OpenAPI Specification Version 3.0.3](https://swagger.io/specification/).

## Generate API Client

In root directory of the project:

- Run `./gradlew build`
- Run `./gradlew generateSwaggerCode`

The client code will be generated under `build/swagger-code-fineract`

## Build API Client:

- Run `cd build/swagger-code-fineract`
- Run `./gradlew build`

## Customize Code Generator:

Swagger Codegen provides several options to customize the generated code. [Here](https://openapi-generator.tech/docs/generators/java/) are the options available for customization.

- Open `fineract-provider/config/swagger/config.json`
- Customize options
- Build the project again as mentioned in **Generate API Client Code** section