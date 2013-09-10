/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings;

import org.mifosplatform.portfolio.savings.domain.SavingsAccount;

/**
 * An enumeration of different transactions that can occur on a
 * {@link SavingsAccount}.
 */
public enum SavingsAccountTransactionType {

    INVALID(0, "savingsAccountTransactionType.invalid"), //
    DEPOSIT(1, "savingsAccountTransactionType.deposit"), //
    WITHDRAWAL(2, "savingsAccountTransactionType.withdrawal"), //
    INTEREST_POSTING(3, "savingsAccountTransactionType.interestPosting"), //
    WITHDRAWAL_FEE(4, "savingsAccountTransactionType.withdrawalFee"), //
    ANNUAL_FEE(5, "savingsAccountTransactionType.annualFee"), //
    INITIATE_TRANSFER(12, "savingsAccountTransactionType.initiateTransfer"), //
    APPROVE_TRANSFER(13, "savingsAccountTransactionType.approveTransfer"), //
    WITHDRAW_TRANSFER(14, "savingsAccountTransactionType.withdrawTransfer"), //
    REJECT_TRANSFER(15, "savingsAccountTransactionType.rejectTransfer");

    private final Integer value;
    private final String code;

    private SavingsAccountTransactionType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return code;
    }

    public static SavingsAccountTransactionType fromInt(final Integer transactionType) {

        if (transactionType == null) { return SavingsAccountTransactionType.INVALID; }

        SavingsAccountTransactionType loanTransactionType = SavingsAccountTransactionType.INVALID;
        switch (transactionType) {
            case 1:
                loanTransactionType = SavingsAccountTransactionType.DEPOSIT;
            break;
            case 2:
                loanTransactionType = SavingsAccountTransactionType.WITHDRAWAL;
            break;
            case 3:
                loanTransactionType = SavingsAccountTransactionType.INTEREST_POSTING;
            break;
            case 4:
                loanTransactionType = SavingsAccountTransactionType.WITHDRAWAL_FEE;
            break;
            case 5:
                loanTransactionType = SavingsAccountTransactionType.ANNUAL_FEE;
            break;
            case 12:
                loanTransactionType = SavingsAccountTransactionType.INITIATE_TRANSFER;
            break;
            case 13:
                loanTransactionType = SavingsAccountTransactionType.APPROVE_TRANSFER;
            break;
            case 14:
                loanTransactionType = SavingsAccountTransactionType.WITHDRAW_TRANSFER;
            break;
            case 15:
                loanTransactionType = SavingsAccountTransactionType.REJECT_TRANSFER;
            break;
        }
        return loanTransactionType;
    }

    public boolean isDeposit() {
        return this.value.equals(SavingsAccountTransactionType.DEPOSIT.getValue());
    }

    public boolean isWithdrawal() {
        return this.value.equals(SavingsAccountTransactionType.WITHDRAWAL.getValue());
    }

    public boolean isInterestPosting() {
        return this.value.equals(SavingsAccountTransactionType.INTEREST_POSTING.getValue());
    }

    public boolean isWithdrawalFee() {
        return this.value.equals(SavingsAccountTransactionType.WITHDRAWAL_FEE.getValue());
    }

    public boolean isAnnualFee() {
        return this.value.equals(SavingsAccountTransactionType.ANNUAL_FEE.getValue());
    }

    public boolean isDebit() {
        return isWithdrawal() || isWithdrawalFee() || isAnnualFee();
    }

    public boolean isCredit() {
        return isDeposit() || isInterestPosting();
    }
}