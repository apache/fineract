/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.holiday.service;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.organisation.holiday.domain.HolidayStatusType;

public class HolidayEnumerations {

    public static EnumOptionData holidayStatusType(final int id) {
        return holidayStatusType(HolidayStatusType.fromInt(id));
    }

    public static EnumOptionData holidayStatusType(final HolidayStatusType type) {
        EnumOptionData optionData = null;
        switch (type) {
            case INVALID:
                optionData = new EnumOptionData(HolidayStatusType.INVALID.getValue().longValue(), HolidayStatusType.INVALID.getCode(),
                        "Invalid");
            break;
            case PENDING_FOR_ACTIVATION:
                optionData = new EnumOptionData(HolidayStatusType.PENDING_FOR_ACTIVATION.getValue().longValue(),
                        HolidayStatusType.PENDING_FOR_ACTIVATION.getCode(), "Pending for activation");
            break;
            case ACTIVE:
                optionData = new EnumOptionData(HolidayStatusType.ACTIVE.getValue().longValue(), HolidayStatusType.ACTIVE.getCode(),
                        "Active");
            break;
            case DELETED:
                optionData = new EnumOptionData(HolidayStatusType.DELETED.getValue().longValue(), HolidayStatusType.DELETED.getCode(),
                        "Deleted");
            break;
        }
        return optionData;
    }
}
