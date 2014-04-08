/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.account.domain;

/**
 * Enum representation of loan status states.
 */
public enum StandingInstructionPriority {

    INVALID(0, "standingInstructionPriority.invalid"), //
    URGENT(1, "standingInstructionPriority.urgent"), //
    HIGH(2, "standingInstructionPriority.high"), //
    MEDIUM(3, "standingInstructionPriority.medium"), //
    LOW(4, "standingInstructionPriority.low");

    private final Integer value;
    private final String code;

    public static StandingInstructionPriority fromInt(final Integer statusValue) {

        StandingInstructionPriority enumeration = StandingInstructionPriority.INVALID;
        switch (statusValue) {
            case 1:
                enumeration = StandingInstructionPriority.URGENT;
            break;
            case 2:
                enumeration = StandingInstructionPriority.HIGH;
            break;
            case 3:
                enumeration = StandingInstructionPriority.MEDIUM;
            break;
            case 4:
                enumeration = StandingInstructionPriority.LOW;
            break;
        }
        return enumeration;
    }

    private StandingInstructionPriority(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public boolean hasStateOf(final StandingInstructionPriority state) {
        return this.value.equals(state.getValue());
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public boolean isHighPriority() {
        return this.value.equals(StandingInstructionPriority.HIGH.getValue());
    }

    public boolean isLowPriority() {
        return this.value.equals(StandingInstructionPriority.LOW.getValue());
    }

    public boolean isUrgentPriority() {
        return this.value.equals(StandingInstructionPriority.URGENT.getValue());
    }

    public boolean isMediumPriority() {
        return this.value.equals(StandingInstructionPriority.MEDIUM.getValue());
    }
}