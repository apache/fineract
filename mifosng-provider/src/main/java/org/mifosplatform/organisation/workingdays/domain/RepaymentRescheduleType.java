/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.workingdays.domain;

import java.util.HashMap;
import java.util.Map;

public enum RepaymentRescheduleType {

    INVALID(0, "RepaymentRescheduleType.invalid"), SAME_DAY(1, "RepaymentRescheduleType.same.day"), MOVE_TO_NEXT_WORKING_DAY(2,
            "RepaymentRescheduleType.move.to.next.working.day"), MOVE_TO_NEXT_REPAYMENT_MEETING_DAY(3,
            "RepaymentRescheduleType.move.to.next.repayment.meeting.day"), MOVE_TO_PREVIOUS_WORKING_DAY(4,
            "RepaymentRescheduleType.move.to.previous.working.day");

    private final Integer value;
    private final String code;

    private RepaymentRescheduleType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    private static final Map<Integer, RepaymentRescheduleType> intToEnumMap = new HashMap<>();
    private static int minValue;
    private static int maxValue;
    static {
        int i = 0;
        for (final RepaymentRescheduleType entityType : RepaymentRescheduleType.values()) {
            if (i == 0) {
                minValue = entityType.value;
            }
            intToEnumMap.put(entityType.value, entityType);
            if (minValue >= entityType.value) {
                minValue = entityType.value;
            }
            if (maxValue < entityType.value) {
                maxValue = entityType.value;
            }
            i = i + 1;
        }
    }

    public static RepaymentRescheduleType fromInt(final int i) {
        final RepaymentRescheduleType RepaymentRescheduleType = intToEnumMap.get(Integer.valueOf(i));
        return RepaymentRescheduleType;
    }

    public static int getMinValue() {
        return minValue;
    }

    public static int getMaxValue() {
        return maxValue;
    }

    @Override
    public String toString() {
        return name().toString();
    }
}
