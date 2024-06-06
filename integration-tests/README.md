# Run integration tests

## Prerequisites
- A running Fineract instance is required

## Default configuration
- `BACKEND_PROTOCOL=https`
- `BACKEND_HOST=localhost`
- `BACKEND_PORT=8443`
- `BACKEND_USERNAME=mifos`
- `BACKEND_PASSWORD=password`
- `BACKEND_TENANT=default`

## To override default values
- To override any of the default value, just add new value with the same key as environment variable
- Example:
  - `BACKEND_PROTOCOL=http`
