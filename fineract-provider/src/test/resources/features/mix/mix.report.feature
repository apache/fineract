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

Feature: MIX XBRL

  @mix
  Scenario Outline: Verify that GL codes resolve
    Given A XBRL template <template>
    When The user resolves GL codes
    Then The result should contain <values>

    Examples:
      | template        | values      |
      | {12000}+{11000} | 12000,11000 |

  @mix
  Scenario Outline: Verify XBRL builder is working
    Given The XBRL input parameters start date <start>, end date <end>, currency <currency>, taxonomy <taxonomy> and sample <sample>
    When The user builds the XBRL report
    Then The XBRL result should match <result>

    Examples:
      | start      | end        | currency | taxonomy | sample | result   |
      | 2005-11-11 | 2013-07-17 | USD      | Assets   | 10000  | xbrl.xml |
