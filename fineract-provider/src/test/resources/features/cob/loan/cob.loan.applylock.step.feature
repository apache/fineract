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

Feature: COB Apply Loan Lock Step

  @cob
  Scenario Outline: ApplyLoanLockTasklet - run test
    Given The ApplyLoanLockTasklet.execute method with action <action>
    When ApplyLoanLockTasklet.execute method executed
    Then ApplyLoanLockTasklet.execute result should match

    Examples:
      |action|
      |good|

  @cob
  Scenario Outline: ApplyLoanLockTasklet - run test: exception
    Given The ApplyLoanLockTasklet.execute method with action <action>
    Then throw exception ApplyLoanLockTasklet.execute method

    Examples:
      |action|
      |error|

  @cob
  Scenario Outline: ApplyLoanLockTasklet - run test: database exception not recoverable after retries
    Given The ApplyLoanLockTasklet.execute method with action <action>
    Then throw LoanLockCannotBeAppliedException exception ApplyLoanLockTasklet.execute method

    Examples:
      |action|
      |db-error-not-recoverable|

  @cob
  Scenario Outline: ApplyLoanLockTasklet - run test: database exception first try
    Given The ApplyLoanLockTasklet.execute method with action <action>
    When ApplyLoanLockTasklet.execute method executed
    Then ApplyLoanLockTasklet.execute result should be retry

    Examples:
      |action|
      |db-error-first-try|