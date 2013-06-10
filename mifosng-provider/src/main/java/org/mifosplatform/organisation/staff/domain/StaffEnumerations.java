/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.staff.domain;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;

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