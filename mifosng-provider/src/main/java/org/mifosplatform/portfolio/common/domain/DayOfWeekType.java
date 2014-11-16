/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.common.domain;

import org.joda.time.DateTimeConstants;

public enum DayOfWeekType {

	MONDAY(DateTimeConstants.MONDAY, "weekDayType.monday"),
	TUESDAY(DateTimeConstants.TUESDAY, "weekDayType.tuesday"),
	WEDNESDAY(DateTimeConstants.WEDNESDAY, "weekDayType.wednesday"),
	THURSDAY(DateTimeConstants.THURSDAY, "weekDayType.thursday"),
	FRIDAY(DateTimeConstants.FRIDAY, "weekDayType.friday"),
	SATURDAY(DateTimeConstants.SATURDAY, "weekDayType.saturday"),
	SUNDAY(DateTimeConstants.SUNDAY, "weekDayType.sunday"),
	INVALID(0, "weekDayType.invalid");
	
	private final Integer value;
    private final String code;
	
    private DayOfWeekType(Integer value, String code) {
		this.value = value;
		this.code = code;
	}

	public Integer getValue() {
		return this.value;
	}

	public String getCode() {
		return this.code;
	}
	
	public static DayOfWeekType fromInt(final Integer dayOfWeek) {
		DayOfWeekType weekDayType = INVALID;
        if (dayOfWeek != null) {
            switch (dayOfWeek) {
                case 1:
                	weekDayType = MONDAY;
                break;
                case 2:
                	weekDayType = TUESDAY;
                break;
                case 3:
                	weekDayType = WEDNESDAY;
                break;
                case 4:
                	weekDayType = THURSDAY;
                break;
                case 5:
                	weekDayType = FRIDAY;
                break;
                case 6:
                	weekDayType = SATURDAY;
                break;
                case 7:
                	weekDayType = SUNDAY;
                break;
                default:
                break;
            }
        }
        return weekDayType;
    }
    
}
