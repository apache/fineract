/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.data;

import org.mifosplatform.portfolio.savings.SavingsAccountTransactionType;

/**
 * Immutable data object represent savings account transaction type
 * enumerations.
 */
@SuppressWarnings("unused")
public class SavingsAccountTransactionEnumData {

    private final Long id;
    private final String code;
    private final String value;

    private final boolean deposit;
    private final boolean withdrawal;
    private final boolean interestPosting;
    private final boolean feeDeduction;

    public SavingsAccountTransactionEnumData(final Long id, final String code, final String value) {
        this.id = id;
        this.code = code;
        this.value = value;
        this.deposit = Long.valueOf(SavingsAccountTransactionType.DEPOSIT.getValue()).equals(this.id);
        this.withdrawal = Long.valueOf(SavingsAccountTransactionType.WITHDRAWAL.getValue()).equals(this.id);
        this.interestPosting = Long.valueOf(SavingsAccountTransactionType.INTEREST_POSTING.getValue()).equals(this.id);
        this.feeDeduction = Long.valueOf(SavingsAccountTransactionType.ANNUAL_FEE.getValue()).equals(this.id)
                || Long.valueOf(SavingsAccountTransactionType.WITHDRAWAL_FEE.getValue()).equals(this.id);
    }

    public boolean isDeposit() {
        return this.deposit;
    }

    public boolean isWithdrawal() {
        return this.withdrawal;
    }

    public boolean isDepositOrWithdrawal() {
        return this.deposit || this.withdrawal;
    }

    public boolean isInterestPosting() {
        return this.interestPosting;
    }

    public boolean isFeeDeduction() {
        return this.feeDeduction;
    }

}