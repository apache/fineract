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
package org.apache.fineract.organisation.holiday.service;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.organisation.holiday.domain.HolidayStatusType;
import org.apache.fineract.organisation.holiday.domain.RescheduleType;

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
    
    public static EnumOptionData rescheduleType(final int id) {
        return rescheduleType(RescheduleType.fromInt(id));
    }

    
    public static EnumOptionData rescheduleType(final RescheduleType type) {
        EnumOptionData optionData = null;
        switch (type) {
            case RESCHEDULETONEXTREPAYMENTDATE:
                optionData = new EnumOptionData(RescheduleType.RESCHEDULETONEXTREPAYMENTDATE.getValue().longValue(),
                        RescheduleType.RESCHEDULETONEXTREPAYMENTDATE.getCode(), "Reschedule to next repayment date");
            break;
            case RESCHEDULETOSPECIFICDATE:
                optionData = new EnumOptionData(RescheduleType.RESCHEDULETOSPECIFICDATE.getValue().longValue(),
                        RescheduleType.RESCHEDULETOSPECIFICDATE.getCode(), "Reschedule to specified date");
            break;
           
            default:
                optionData = new EnumOptionData(RescheduleType.INVALID.getValue().longValue(),
                        RescheduleType.INVALID.getCode(), "Invalid");
            break;
        }
        return optionData;
    }
}
