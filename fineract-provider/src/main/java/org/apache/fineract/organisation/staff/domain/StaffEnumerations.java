/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.organisation.staff.domain;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;

public class StaffEnumerations {

    public static EnumOptionData organisationalRole(final Integer id) {
        return organisationalRole(StaffOrganisationalRoleType.fromInt(id));
    }

    public static EnumOptionData organisationalRole(final StaffOrganisationalRoleType type) {
        EnumOptionData optionData = new EnumOptionData(StaffOrganisationalRoleType.INVALID.getValue().longValue(),
                StaffOrganisationalRoleType.INVALID.getCode(), "Invalid");
        switch (type) {
            case PROGRAM_DIRECTOR:
                optionData = new EnumOptionData(type.getValue().longValue(), type.getCode(), "Program Director");
            break;
            case BRANCH_MANAGER:
                optionData = new EnumOptionData(type.getValue().longValue(), type.getCode(), "Branch Manager");
            break;
            case FIELD_OFFICER_COORDINATOR:
                optionData = new EnumOptionData(type.getValue().longValue(), type.getCode(), "Field Officer Coordinator");
            break;
            case FIELD_OFFICER:
                optionData = new EnumOptionData(type.getValue().longValue(), type.getCode(), "Field Officer");
            break;
            case INVALID:
            break;
        }
        return optionData;
    }
}