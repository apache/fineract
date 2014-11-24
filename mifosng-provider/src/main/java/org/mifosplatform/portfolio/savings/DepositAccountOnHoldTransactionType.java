/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings;

import org.mifosplatform.portfolio.savings.domain.SavingsAccount;

/**
 * An enumeration of on hold transactions that can occur on a
 * {@link SavingsAccount}.
 */
public enum DepositAccountOnHoldTransactionType {

    INVALID(0, "deposutAccountOnHoldTransactionType.invalid"), //
    HOLD(1, "deposutAccountOnHoldTransactionType.hold"), //
    RELEASE(2, "deposutAccountOnHoldTransactionType.release");

    private final Integer value;
    private final String code;

    private DepositAccountOnHoldTransactionType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static DepositAccountOnHoldTransactionType fromInt(final Integer transactionType) {

        if (transactionType == null) { return DepositAccountOnHoldTransactionType.INVALID; }

        DepositAccountOnHoldTransactionType savingsAccountTransactionType = DepositAccountOnHoldTransactionType.INVALID;
        switch (transactionType) {
            case 1:
                savingsAccountTransactionType = DepositAccountOnHoldTransactionType.HOLD;
            break;
            case 2:
                savingsAccountTransactionType = DepositAccountOnHoldTransactionType.RELEASE;
            break;

        }
        return savingsAccountTransactionType;
    }

    public boolean isHold() {
        return this.value.equals(DepositAccountOnHoldTransactionType.HOLD.getValue());
    }

    public boolean isRelease() {
        return this.value.equals(DepositAccountOnHoldTransactionType.RELEASE.getValue());
    }

}