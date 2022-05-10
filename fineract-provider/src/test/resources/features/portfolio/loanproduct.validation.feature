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

Feature: Loan Product Validation

  @loanproduct
  Scenario Outline: Verify that loan product settings are throwing expected exceptions
    Given A loan product with values `allowMultipleDisbursal` is <allowMultipleDisbursal>, `disallowExpectedDisbursements` is <disallowExpectedDisbursements>, `allowApprovedDisbursedAmountsOverApplied` is <allowApprovedDisbursedAmountsOverApplied>, `overAppliedCalculationType` is <overAppliedCalculationType> and `overAppliedNumber` is <overAppliedNumber>
    When The user validates the loan product before saving
    Then An exception <exception> with message template  <template> should be thrown

    Examples:
      | allowMultipleDisbursal | disallowExpectedDisbursements | allowApprovedDisbursedAmountsOverApplied | overAppliedCalculationType | overAppliedNumber | exception                                                                           | template                                                                                                 |
      | false                  | true                          | false                                    | -                          | -1                | org.apache.fineract.portfolio.loanproduct.exception.LoanProductGeneralRuleException | error.msg.allowMultipleDisbursals.not.set.disallowExpectedDisbursements.cant.be.set                      |
      | true                   | false                         | true                                     | -                          | -1                | org.apache.fineract.portfolio.loanproduct.exception.LoanProductGeneralRuleException | error.msg.disallowExpectedDisbursements.not.set.allowApprovedDisbursedAmountsOverApplied.cant.be.set     |
      | true                   | true                          | true                                     | -                          | -1                | org.apache.fineract.portfolio.loanproduct.exception.LoanProductGeneralRuleException | error.msg.allowApprovedDisbursedAmountsOverApplied.is.set.overAppliedCalculationType.is.mandatory        |
      | true                   | true                          | true                                     | -                          | -1                | org.apache.fineract.portfolio.loanproduct.exception.LoanProductGeneralRuleException | error.msg.allowApprovedDisbursedAmountsOverApplied.is.set.overAppliedCalculationType.is.mandatory        |
      | true                   | false                         | true                                     | -                          | -1                | org.apache.fineract.portfolio.loanproduct.exception.LoanProductGeneralRuleException | error.msg.disallowExpectedDisbursements.not.set.allowApprovedDisbursedAmountsOverApplied.cant.be.set     |
      | true                   | true                          | true                                     | notflat                    | -1                | org.apache.fineract.portfolio.loanproduct.exception.LoanProductGeneralRuleException | error.msg.overAppliedCalculationType.must.be.percentage.or.flat                                          |
      | true                   | true                          | false                                    | flat                       | -1                | org.apache.fineract.portfolio.loanproduct.exception.LoanProductGeneralRuleException | error.msg.allowApprovedDisbursedAmountsOverApplied.is.not.set.overAppliedCalculationType.cant.be.entered |
      | true                   | true                          | true                                     | flat                       | -1                | org.apache.fineract.portfolio.loanproduct.exception.LoanProductGeneralRuleException | error.msg.allowApprovedDisbursedAmountsOverApplied.is.set.overAppliedNumber.is.mandatory                 |
      | true                   | true                          | false                                    | -                          | 80                | org.apache.fineract.portfolio.loanproduct.exception.LoanProductGeneralRuleException | error.msg.allowApprovedDisbursedAmountsOverApplied.is.not.set.overAppliedNumber.cant.be.entered          |
