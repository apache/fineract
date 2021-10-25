#!/bin/sh

set -e

while ! nc -zvw3 fineractmysql 3306 ; do
    >&2 echo "DB Server is unavailable - sleeping"
    sleep 5
done
>&2 echo "DB Server is up - executing command"

java -Dloader.path=/app/libs/ -jar /app/fineract-provider.jar

