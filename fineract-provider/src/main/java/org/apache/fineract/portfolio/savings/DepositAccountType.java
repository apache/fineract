/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.portfolio.savings.domain.SavingsAccount;

/**
 * An enumeration of different transactions that can occur on a
 * {@link SavingsAccount}.
 */
public enum DepositAccountType {

    INVALID(0, "depositAccountType.invalid"), //
    SAVINGS_DEPOSIT(100, "depositAccountType.savingsDeposit"), //
    FIXED_DEPOSIT(200, "depositAccountType.fixedDeposit"), //
    RECURRING_DEPOSIT(300, "depositAccountType.recurringDeposit"), //
    CURRENT_DEPOSIT(400, "depositAccountType.currentDeposit");

    private final Integer value;
    private final String code;
    private static final String SPACE = " ";

    private DepositAccountType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static DepositAccountType fromInt(final Integer transactionType) {

        if (transactionType == null) { return DepositAccountType.INVALID; }

        DepositAccountType depositAccountType = DepositAccountType.INVALID;
        switch (transactionType) {
            case 100:
                depositAccountType = DepositAccountType.SAVINGS_DEPOSIT;
            break;
            case 200:
                depositAccountType = DepositAccountType.FIXED_DEPOSIT;
            break;
            case 300:
                depositAccountType = DepositAccountType.RECURRING_DEPOSIT;
            break;
            case 400:
                depositAccountType = DepositAccountType.CURRENT_DEPOSIT;
            break;
        }
        return depositAccountType;
    }

    public boolean isSavingsDeposit() {
        return this.value.equals(DepositAccountType.SAVINGS_DEPOSIT.getValue());
    }

    public boolean isFixedDeposit() {
        return this.value.equals(DepositAccountType.FIXED_DEPOSIT.getValue());
    }

    public boolean isRecurringDeposit() {
        return this.value.equals(DepositAccountType.RECURRING_DEPOSIT.getValue());
    }

    public boolean isCurrentDeposit() {
        return this.value.equals(DepositAccountType.CURRENT_DEPOSIT.getValue());
    }

    @Override
    public String toString() {
        return StringUtils.replace(code, "_", SPACE);
    }

    public String resourceName() {

        String resourceName = "INVALID";

        switch (this) {
            case FIXED_DEPOSIT:
                resourceName = DepositsApiConstants.FIXED_DEPOSIT_ACCOUNT_RESOURCE_NAME;
            break;
            case RECURRING_DEPOSIT:
                resourceName = DepositsApiConstants.RECURRING_DEPOSIT_ACCOUNT_RESOURCE_NAME;
            break;
            case SAVINGS_DEPOSIT:
                resourceName = DepositsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME;
            break;
            default:
                resourceName = "INVALID";
            break;
        }

        return resourceName;
    }

    public boolean isInvalid() {
        return this.value.equals(DepositAccountType.INVALID.value);
    }
}