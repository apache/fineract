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


# Exit the script if any command returns a non-true return value (http://www.davidpashley.com/articles/writing-robust-shell-scripts/)
set -e

git --version
# TODO UNCOMMENT THIS once https://github.com/openMF/mifosx/pull/1291 is merged!
# git pull
# This does NOT get the most recent revision from the submodule repository.
# Instead, it only gets the revision of the submodule that is recorded in the revision of the main repository.
## NOT git submodule update --remote
# The following is very important, because even if the main (API) repo is latest,
# the sub-module initially will be a fixed old rev. and only this grabs real latest:
git submodule foreach 'git checkout develop && git pull --ff-only origin develop'

cd apps/community-app
./build.sh
cd ../../fineract-provider/
./gradlew -Penv=dev clean dist
