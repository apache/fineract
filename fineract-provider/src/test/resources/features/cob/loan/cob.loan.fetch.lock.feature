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

Feature: COB Loan fetch and (soft) lock Step

  @cob
  Scenario Outline: FetchAndLockLoanTasklet - run test
    Given The FetchAndLockLoanTasklet.execute method with action <action>
    When FetchAndLockLoanTasklet.execute method executed
    Then FetchAndLockLoanTasklet.execute result should match

    Examples:
      |action|
      |empty loanIds|
      |good|
      |soft lock|
      |inline cob|
      |chunk processing|