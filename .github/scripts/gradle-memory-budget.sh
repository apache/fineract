#!/usr/bin/env bash
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
# Cap the Gradle build JVM heap and worker concurrency for the current CI job.
#
# The override is appended to GRADLE_USER_HOME/gradle.properties, which outranks the
# project's own gradle.properties, so it applies to every Gradle invocation in the job
# without editing tracked files. Appending is safe even if the key is already present:
# java.util.Properties keeps the last occurrence.
#
# gradle/actions/setup-gradle exports GRADLE_USER_HOME; jobs that do not use it fall back to
# Gradle's own default of ~/.gradle.
#
# CAUTION on MAX_HEAP: the OpenAPI generator (org.openapi.generator GenerateTask) runs through
# the worker API with classloader isolation, i.e. inside the build JVM, and
# ':fineract-client:buildJavaSdk' alone needs 7g (6g fails with Java heap space). Serializing
# the generators does not help -- see openApiGeneratorLock in build.gradle, which already does
# that. So a job may only go below 8g if it never executes a GenerateTask, which in practice
# means either it passes '-x buildJavaSdk' or the generated sources arrive already built in the
# workspace artifact.
#
# Usage: MAX_HEAP=4g WORKERS_MAX=2 gradle-memory-budget.sh
#        (run from the directory containing the gradle.properties to derive args from)

set -euo pipefail

MAX_HEAP="${MAX_HEAP:-4g}"
WORKERS_MAX="${WORKERS_MAX:-2}"

GUH="${GRADLE_USER_HOME:-$HOME/.gradle}"

if [ ! -f gradle.properties ]; then
  echo "::error::gradle.properties not found in $(pwd)"
  exit 1
fi

GRADLE_JVMARGS=$(sed -n 's/^org\.gradle\.jvmargs=//p' gradle.properties)
if [ -z "$GRADLE_JVMARGS" ]; then
  echo "::error::org.gradle.jvmargs is missing from gradle.properties"
  exit 1
fi

case "$GRADLE_JVMARGS" in
  *-Xmx*) ;;
  *)
    echo "::error::org.gradle.jvmargs has no -Xmx to override — the budget would be a no-op."
    exit 1
    ;;
esac

GRADLE_JVMARGS=$(printf '%s' "$GRADLE_JVMARGS" | sed -E "s/-Xmx[^ ]+/-Xmx${MAX_HEAP}/")

mkdir -p "$GUH"
{
  echo "org.gradle.jvmargs=$GRADLE_JVMARGS"
  echo "org.gradle.workers.max=$WORKERS_MAX"
} >> "$GUH/gradle.properties"

echo "Gradle build JVM capped at -Xmx${MAX_HEAP}, org.gradle.workers.max=${WORKERS_MAX}"
echo "(written to $GUH/gradle.properties)."
