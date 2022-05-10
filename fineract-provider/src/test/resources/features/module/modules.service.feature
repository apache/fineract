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

Feature: Service Modules

  @modules
  Scenario Outline: Verify that the dummy service returns the correct message
    Given An auto configuration <autoConfigurationClass> and a service configuration <configurationClass>
    When The user retrieves the service of interface class <interfaceClass>
    Then The service class should match <serviceClass>

    Examples:
      | autoConfigurationClass                                           | configurationClass                                           | interfaceClass                                                      | serviceClass                                                                         |
      | org.apache.fineract.portfolio.note.starter.NoteAutoConfiguration | org.apache.fineract.module.service.TestDefaultConfiguration  | org.apache.fineract.portfolio.note.service.NoteReadPlatformService  | org.apache.fineract.portfolio.note.service.NoteReadPlatformServiceImpl               |
      | org.apache.fineract.portfolio.note.starter.NoteAutoConfiguration | org.apache.fineract.module.service.TestDefaultConfiguration  | org.apache.fineract.portfolio.note.service.NoteWritePlatformService | org.apache.fineract.portfolio.note.service.NoteWritePlatformServiceJpaRepositoryImpl |
      | org.apache.fineract.portfolio.note.starter.NoteAutoConfiguration | org.apache.fineract.module.service.TestOverrideConfiguration | org.apache.fineract.portfolio.note.service.NoteReadPlatformService  | org.apache.fineract.module.service.custom.CustomNoteReadPlatformService              |
      | org.apache.fineract.portfolio.note.starter.NoteAutoConfiguration | org.apache.fineract.module.service.TestOverrideConfiguration | org.apache.fineract.portfolio.note.service.NoteWritePlatformService | org.apache.fineract.module.service.custom.CustomNoteWritePlatformService             |