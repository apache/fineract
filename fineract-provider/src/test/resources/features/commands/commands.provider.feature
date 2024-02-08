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

Feature: Commands Provider

  @template
  Scenario Outline: Verify that command providers are injected
    Given A command handler for entity <entity> and action <action>
    When The user processes the command with ID <id>
    Then The command ID matches <id>

    Examples:
      | id  | entity | action |
      | 815 | HUMAN  | UPDATE |

  @template
  Scenario Outline: Verify that command no command handler is provided
    Given A missing command handler for entity <entity> and action <action>
    Then The system should throw an exception

    Examples:
      | entity   | action      |
      | WHATEVER | DOSOMETHING |
