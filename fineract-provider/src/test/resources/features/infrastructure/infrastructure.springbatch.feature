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

Feature: SpringBatch Infrastructure

  @springbatch
  Scenario: Verify that spring batch properties are read correctly for LOAN_COB
    Given Property Service is initialized
    When partition size is fetched for "LOAN_COB"
    When chunk size is fetched for "LOAN_COB"
    When retry limit is fetched for "LOAN_COB"
    Then partition size is 100
    Then chunk size is 100
    Then retry limit is 5

  @springbatch
  Scenario: Verify that spring batch default properties are read correctly for non-existing job
    Given Property Service is initialized
    When partition size is fetched for "INVALID_JOB"
    When chunk size is fetched for "INVALID_JOB"
    When retry limit is fetched for "INVALID_JOB"
    Then partition size is 1
    Then chunk size is 1
    Then retry limit is 1
