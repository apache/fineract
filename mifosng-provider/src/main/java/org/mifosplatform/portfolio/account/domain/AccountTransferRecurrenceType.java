/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.account.domain;

/**
 * Enum representation of loan status states.
 */
public enum AccountTransferRecurrenceType {

    INVALID(0, "accountTransferRecurrenceType.invalid"), //
    PERIODIC(1, "accountTransferRecurrenceType.periodic"), //
    AS_PER_DUES(2, "accountTransferRecurrenceType.as.per.dues"); //

    private final Integer value;
    private final String code;

    public static AccountTransferRecurrenceType fromInt(final Integer statusValue) {

        AccountTransferRecurrenceType enumeration = AccountTransferRecurrenceType.INVALID;
        switch (statusValue) {
            case 1:
                enumeration = AccountTransferRecurrenceType.PERIODIC;
            break;
            case 2:
                enumeration = AccountTransferRecurrenceType.AS_PER_DUES;
            break;
        }
        return enumeration;
    }

    private AccountTransferRecurrenceType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public boolean hasStateOf(final AccountTransferRecurrenceType state) {
        return this.value.equals(state.getValue());
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public boolean isPeriodicRecurrence() {
        return this.value.equals(AccountTransferRecurrenceType.PERIODIC.getValue());
    }

    public boolean isDuesRecurrence() {
        return this.value.equals(AccountTransferRecurrenceType.AS_PER_DUES.getValue());
    }

}