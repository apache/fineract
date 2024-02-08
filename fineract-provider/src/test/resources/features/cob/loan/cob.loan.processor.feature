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

Feature: COB Process

  @cob
  Scenario Outline: LoanItemProcessor - run test
    Given The LoanItemProcessor.process method with item <item>
    When LoanItemProcessor.process method executed
    Then LoanItemProcessor.process result should match

    Examples:
      |item|
      |1   |

  @cob
  Scenario Outline: LoanItemProcessor - run test: exception
    Given The LoanItemProcessor.process method with item <item>
    Then throw exception LoanItemProcessor.process method

    Examples:
      |item|
      ||