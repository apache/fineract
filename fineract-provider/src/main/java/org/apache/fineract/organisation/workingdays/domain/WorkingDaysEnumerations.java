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
package org.apache.fineract.organisation.workingdays.domain;


import org.apache.fineract.infrastructure.core.data.EnumOptionData;

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
