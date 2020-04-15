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

echo "Setting Up Fineract service configuration..."
kubectl apply -f secrets.yml
kubectl apply -f fineractmysql-configmap.yml

echo
echo "Starting fineractmysql..."
kubectl apply -f fineractmysql-deployment.yml

fineractmysql_pod=""
while [[ ${#fineractmysql_pod} -eq 0 ]]; do
    fineractmysql_pod=$(kubectl get pods -l tier=fineractmysql --template '{{range .items}}{{.metadata.name}}{{"\n"}}{{end}}')
done
#sleep 5
fineractmysql_status=$(kubectl get pods ${fineractmysql_pod} --no-headers -o custom-columns=":status.phase")
while [[ ${fineractmysql_status} -ne 'Running' ]]; do
    sleep 1
    fineractmysql_status=$(kubectl get pods ${fineractmysql_pod} --no-headers -o custom-columns=":status.phase")
done

echo
echo "Starting fineract server..."
kubectl apply -f fineract-server-deployment.yml
