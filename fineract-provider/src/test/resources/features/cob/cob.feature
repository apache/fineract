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

Feature: COB

  @cob
  Scenario Outline: COB Business Step Service - run test
    Given The COBBusinessStepService.run method with executeMap <executionMap>
    When COBBusinessStepService.run method executed
    Then The COBBusinessStepService.run result should match

    Examples:
      |executionMap|
      |1,test|

  @cob
  Scenario Outline: COB Business Step Service - run test failure
    Given The COBBusinessStepService.run method with executeMap <executionMap>
    Then throw exception COBBusinessStepService.run method

    Examples:
      |executionMap|
      |null|

  @cob
  Scenario Outline: COB Business Step Service - run test failure
    Given The COBBusinessStepService.run method with executeMap <executionMap>
    Then throw exception COBBusinessStepService.run method with verification

    Examples:
      |executionMap|
      |2,notExist|
      |3,|

  @cob
  Scenario Outline: COB Business Step Service - getCOBBusinessStepMap test found bean
    Given The COBBusinessStepService.getCOBBusinessStepMap method with businessStepClass <className>, and jobName <jobName>
    When COBBusinessStepService.getCOBBusinessStepMap method executed
    Then The COBBusinessStepService.getCOBBusinessStepMap result should match

    Examples:
      |className|jobName|
      |LoanCOBBusinessStep|exist|

  @cob
  Scenario Outline: COB Business Step Service - getCOBBusinessStepMap empty
    Given The COBBusinessStepService.getCOBBusinessStepMap method with businessStepClass <className>, and jobName <jobName>
    When COBBusinessStepService.getCOBBusinessStepMap method executed
    Then The COBBusinessStepService.getCOBBusinessStepMap result empty

    Examples:
      |className|jobName|
      |LoanCOBBusinessStep|notExist|
      |empty|exist|
      |random|exist|

  @cob
  Scenario Outline: COB Business Step Service - getCOBBusinessStepMap no bean
    Given The COBBusinessStepService.getCOBBusinessStepMap method with businessStepClass <className>, and jobName <jobName>
    Then The COBBusinessStepService.getCOBBusinessStepMap result exception

    Examples:
      |className|jobName|
      |null|notExist|