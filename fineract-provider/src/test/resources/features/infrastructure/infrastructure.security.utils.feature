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

Feature: Security Utils Infrastructure

  @security
  Scenario: Verify that log parameter escape util does not change strings that does not contain any special character
    Given A simple log message without any special character
    When Log parameter escape util escaping the special characters
    Then The log message stays as it is

  @security
  Scenario: Verify that log parameter escape util changes special characters in string
    Given A log message with new line, carriage return and tab characters
    When Log parameter escape util escaping the special characters
    Then The escape util changes the special characters to `_`
