#!/bin/sh

set -e

while ! nc -zvw3 $FINERACT_DEFAULT_TENANTDB_HOSTNAME $FINERACT_DEFAULT_TENANTDB_PORT ; do
    >&2 echo "DB Server is unavailable - sleeping"
    sleep 5
done
>&2 echo "DB Server is up - executing command"

java -Dloader.path=/app/libs/ -jar /app/fineract-provider.jar