#!/bin/bash
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


set -e

docker rm -f mysql-for-test
docker run --name mysql-for-test -p 3306:3306 -e MYSQL_ROOT_PASSWORD=mysql -d amd64/mysql:5.7
sleep 20

./gradlew createDB -PdbName=fineract_tenants
./gradlew createDB -PdbName=fineract_default

# Hardcoding the time zone is a temporary fix for https://issues.apache.org/jira/browse/FINERACT-723
TZ=Europe/Berlin ./gradlew clean test

docker rm -f mysql-for-test