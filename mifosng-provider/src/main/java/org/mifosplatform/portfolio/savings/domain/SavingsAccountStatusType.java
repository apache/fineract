/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

/**
 * Enum representation of {@link SavingsAccount} status states.
 */
public enum SavingsAccountStatusType {

    INVALID(0, "savingsAccountStatusType.invalid"), //
    UNACTIVATED(100, "savingsAccountStatusType.unactivated"), //
    ACTIVE(300, "savingsAccountStatusType.active"), //
    CLOSED(600, "savingsAccountStatusType.closed");

    private final Integer value;
    private final String code;

    public static SavingsAccountStatusType fromInt(final Integer type) {

        SavingsAccountStatusType enumeration = SavingsAccountStatusType.INVALID;
        switch (type) {
            case 100:
                enumeration = SavingsAccountStatusType.UNACTIVATED;
            break;
            case 300:
                enumeration = SavingsAccountStatusType.ACTIVE;
            break;
            case 600:
                enumeration = SavingsAccountStatusType.CLOSED;
            break;
        }
        return enumeration;
    }

    private SavingsAccountStatusType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public boolean hasStateOf(final SavingsAccountStatusType state) {
        return this.value.equals(state.getValue());
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return code;
    }

    public boolean isUnactivated() {
        return this.value.equals(SavingsAccountStatusType.UNACTIVATED.getValue());
    }

    public boolean isActive() {
        return this.value.equals(SavingsAccountStatusType.ACTIVE.getValue());
    }

    public boolean isClosed() {
        return this.value.equals(SavingsAccountStatusType.CLOSED.getValue());
    }
}