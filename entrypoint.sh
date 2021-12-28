#!/bin/sh
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements. See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership. The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License. You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.
#

# NOTE that we are limited to busybox's sh, and cannot use bash here, because
# this is used in a container image based on azul/zulu-openjdk-alpine:17.

# see e.g. https://explainshell.com/explain?cmd=set+-eux
set -ex

while ! nc -zvw3 "${FINERACT_DEFAULT_TENANTDB_HOSTNAME:-fineractmysql}" "${FINERACT_DEFAULT_TENANTDB_PORT:-3306}" ; do
    >&2 echo "DB Server is unavailable - sleeping"
    sleep 5
done
>&2 echo "DB Server is up - executing command"

java -cp "app:app/lib/*" org.apache.fineract.ServerApplication
