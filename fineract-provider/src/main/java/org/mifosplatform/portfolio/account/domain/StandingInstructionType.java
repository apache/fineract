/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.account.domain;

/**
 * Enum representation of loan status states.
 */
public enum StandingInstructionType {

    INVALID(0, "standingInstructionType.invalid"), //
    FIXED(1, "standingInstructionType.fixed"), //
    DUES(2, "standingInstructionType.dues"); //

    private final Integer value;
    private final String code;

    public static StandingInstructionType fromInt(final Integer statusValue) {

        StandingInstructionType enumeration = StandingInstructionType.INVALID;
        switch (statusValue) {
            case 1:
                enumeration = StandingInstructionType.FIXED;
            break;
            case 2:
                enumeration = StandingInstructionType.DUES;
            break;
        }
        return enumeration;
    }

    private StandingInstructionType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public boolean hasStateOf(final StandingInstructionType state) {
        return this.value.equals(state.getValue());
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public boolean isFixedAmoutTransfer() {
        return this.value.equals(StandingInstructionType.FIXED.getValue());
    }

    public boolean isDuesAmoutTransfer() {
        return this.value.equals(StandingInstructionType.DUES.getValue());
    }

}