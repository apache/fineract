/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings;

import java.util.ArrayList;
import java.util.List;

import org.mifosplatform.portfolio.savings.domain.FixedDepositAccount;
import org.mifosplatform.portfolio.savings.domain.RecurringDepositAccount;

/**
 * An enumeration of different options available on account closure
 * {@link FixedDepositAccount} & {@link RecurringDepositAccount}.
 */
public enum DepositAccountOnClosureType {

    INVALID(0, "depositAccountClosureType.invalid"), //
    WITHDRAW_DEPOSIT(100, "depositAccountClosureType.withdrawDeposit"), //
    TRANSFER_TO_SAVINGS(200, "depositAccountClosureType.transferToSavings"), //
    REINVEST(300, "depositAccountClosureType.reinvest"); //

    private final Integer value;
    private final String code;

    private DepositAccountOnClosureType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static DepositAccountOnClosureType fromInt(final Integer closureTypeValue) {

        if (closureTypeValue == null) { return DepositAccountOnClosureType.INVALID; }

        DepositAccountOnClosureType accountOnClosureType = DepositAccountOnClosureType.INVALID;
        switch (closureTypeValue) {
            case 100:
                accountOnClosureType = DepositAccountOnClosureType.WITHDRAW_DEPOSIT;
            break;
            case 200:
                accountOnClosureType = DepositAccountOnClosureType.TRANSFER_TO_SAVINGS;
            break;
            case 300:
                accountOnClosureType = DepositAccountOnClosureType.REINVEST;
            break;
        }
        return accountOnClosureType;
    }

    public boolean isWithdarwDeposit() {
        return this.value.equals(DepositAccountOnClosureType.WITHDRAW_DEPOSIT.getValue());
    }

    public boolean isTransferToSavings() {
        return this.value.equals(DepositAccountOnClosureType.TRANSFER_TO_SAVINGS.getValue());
    }

    public boolean isReinvest() {
        return this.value.equals(DepositAccountOnClosureType.REINVEST.getValue());
    }

    public boolean isInvalid() {
        return this.value.equals(DepositAccountOnClosureType.INVALID.getValue());
    }

    public static Object[] integerValues() {
        final List<Integer> values = new ArrayList<>();
        for (final DepositAccountOnClosureType enumType : values()) {
            if (!enumType.isInvalid()) {
                values.add(enumType.getValue());
            }
        }

        return values.toArray();
    }
}