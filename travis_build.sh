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

cd fineract-provider
# NOTE: The --info, while quite a bit more verbose, is VERY useful to understand failures on Travis,
# where you do not have access to any files like build/reports/tests/index.html, only the Console. 
# @see http://mrhaki.blogspot.ch/2013/05/gradle-goodness-show-more-information.html
# @see http://forums.gradle.org/gradle/topics/whats_new_in_gradle_1_1_test_logging for alternative 
./gradlew --info clean licenseMain licenseTest licenseIntegrationTest test
