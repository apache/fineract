/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.workingdays.domain;


import org.mifosplatform.infrastructure.core.data.EnumOptionData;

public class WorkingDaysEnumerations {

    public static EnumOptionData workingDaysStatusType(final int id) {
        return repaymentRescheduleType(RepaymentRescheduleType.fromInt(id));
    }

    public static EnumOptionData repaymentRescheduleType(final RepaymentRescheduleType type){
        EnumOptionData optionData = null;
        switch(type){
            case INVALID:
                optionData = new EnumOptionData(RepaymentRescheduleType.INVALID.getValue().longValue(),RepaymentRescheduleType.INVALID.getCode(),
                        "invalid");
            break;

            case SAME_DAY:
                optionData = new EnumOptionData(RepaymentRescheduleType.SAME_DAY.getValue().longValue(),RepaymentRescheduleType.SAME_DAY.getCode(),
                        "same day");

                break;
            case MOVE_TO_NEXT_WORKING_DAY:
                optionData = new EnumOptionData(RepaymentRescheduleType.MOVE_TO_NEXT_WORKING_DAY.getValue().longValue(),RepaymentRescheduleType.MOVE_TO_NEXT_WORKING_DAY.getCode(),
                        "move to next working day");
                break;

            case MOVE_TO_NEXT_REPAYMENT_MEETING_DAY:
                optionData = new EnumOptionData(RepaymentRescheduleType.MOVE_TO_NEXT_REPAYMENT_MEETING_DAY.getValue().longValue(),RepaymentRescheduleType.MOVE_TO_NEXT_REPAYMENT_MEETING_DAY.getCode(),
                        "move to next repayment meeting day");
                break;
            case MOVE_TO_PREVIOUS_WORKING_DAY:
                optionData = new EnumOptionData(RepaymentRescheduleType.MOVE_TO_PREVIOUS_WORKING_DAY.getValue().longValue(),RepaymentRescheduleType.MOVE_TO_PREVIOUS_WORKING_DAY.getCode(),
                        "move to previous working day");
                break;
        }

        return optionData;
    }
}
