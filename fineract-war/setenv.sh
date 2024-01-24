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

# NOTE: drop this file into your Tomcat bin folder and use it to tweak your configuration in a WAR deployment
export FINERACT_NODE_ID="1"
# NOTE: env vars prefixed "FINERACT_HIKARI_*" are used to configure the database connection pool
export FINERACT_HIKARI_DRIVER_SOURCE_CLASS_NAME="org.mariadb.jdbc.Driver"
export FINERACT_HIKARI_JDBC_URL="jdbc:mariadb://fineractmysql:3306/fineract_tenants"
export FINERACT_HIKARI_USERNAME="root"
export FINERACT_HIKARI_PASSWORD="skdcnwauicn2ucnaecasdsajdnizucawencascdca"
# ... following variables are optional; "application.properties" contains reasonable defaults (same as here)
export FINERACT_HIKARI_MINIMUM_IDLE="3"
export FINERACT_HIKARI_MAXIMUM_POOL_SIZE="10"
export FINERACT_HIKARI_IDLE_TIMEOUT="60000"
export FINERACT_HIKARI_CONNECTION_TIMEOUT="20000"
export FINERACT_HIKARI_TEST_QUERY="SELECT 1"
export FINERACT_HIKARI_AUTO_COMMIT="true"
export FINERACT_HIKARI_DS_PROPERTIES_CACHE_PREP_STMTS="true"
export FINERACT_HIKARI_DS_PROPERTIES_PREP_STMT_CACHE_SIZE="250"
export FINERACT_HIKARI_DS_PROPERTIES_PREP_STMT_CACHE_SQL_LIMIT="2048"
export FINERACT_HIKARI_DS_PROPERTIES_USE_SERVER_PREP_STMTS="true"
export FINERACT_HIKARI_DS_PROPERTIES_USE_LOCAL_SESSION_STATE="true"
export FINERACT_HIKARI_DS_PROPERTIES_REWRITE_BATCHED_STATEMENTS="true"
export FINERACT_HIKARI_DS_PROPERTIES_CACHE_RESULT_SET_METADATA="true"
export FINERACT_HIKARI_DS_PROPERTIES_CACHE_SERVER_CONFIGURATION="true"
export FINERACT_HIKARI_DS_PROPERTIES_ELIDE_SET_AUTO_COMMITS="true"
export FINERACT_HIKARI_DS_PROPERTIES_MAINTAIN_TIME_STATS="false"
export FINERACT_HIKARI_DS_PROPERTIES_LOG_SLOW_QUERIES="true"
export FINERACT_HIKARI_DS_PROPERTIES_DUMP_QUERIES_IN_EXCEPTION="true"
# NOTE: env vars prefixed "FINERACT_DEFAULT_TENANTDB_*" are used to create the default tenant database
export FINERACT_DEFAULT_TENANTDB_HOSTNAME="fineractmysql"
export FINERACT_DEFAULT_TENANTDB_PORT="3306"
export FINERACT_DEFAULT_TENANTDB_UID="root"
export FINERACT_DEFAULT_TENANTDB_PWD="skdcnwauicn2ucnaecasdsajdnizucawencascdca"
export FINERACT_DEFAULT_TENANTDB_CONN_PARAMS=""
export FINERACT_DEFAULT_TENANTDB_TIMEZONE="Asia/Kolkata"
export FINERACT_DEFAULT_TENANTDB_IDENTIFIER="default"
export FINERACT_DEFAULT_TENANTDB_NAME="fineract_default"
export FINERACT_DEFAULT_TENANTDB_DESCRIPTION="Default Demo Tenant"
export FINERACT_INSECURE_HTTP_CLIENT="true"
