/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.account.domain;

/**
 * Enum representation of loan status states.
 */
public enum AccountTransferType {

    INVALID(0, "accountTransferType.invalid"), //
    ACCOUNT_TRANSFER(1, "accountTransferType.account.transfer"), //
    LOAN_REPAYMENT(2, "accountTransferType.loan.repayment"), //
    CHARGE_PAYMENT(3, "accountTransferType.charge.payment"), //
    INTEREST_TRANSFER(4, "accountTransferType.interest.transfer"); //

    private final Integer value;
    private final String code;

    public static AccountTransferType fromInt(final Integer statusValue) {

        AccountTransferType enumeration = AccountTransferType.INVALID;
        switch (statusValue) {
            case 1:
                enumeration = AccountTransferType.ACCOUNT_TRANSFER;
            break;
            case 2:
                enumeration = AccountTransferType.LOAN_REPAYMENT;
            break;
            case 3:
                enumeration = AccountTransferType.CHARGE_PAYMENT;
            break;
            case 4:
                enumeration = AccountTransferType.INTEREST_TRANSFER;
            break;
        }
        return enumeration;
    }

    private AccountTransferType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public boolean hasStateOf(final AccountTransferType state) {
        return this.value.equals(state.getValue());
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public boolean isAccountTransfer() {
        return this.value.equals(AccountTransferType.ACCOUNT_TRANSFER.getValue());
    }

    public boolean isLoanRepayment() {
        return this.value.equals(AccountTransferType.LOAN_REPAYMENT.getValue());
    }

    public boolean isChargePayment() {
        return this.value.equals(AccountTransferType.CHARGE_PAYMENT.getValue());
    }

    public boolean isInterestTransfer() {
        return this.value.equals(AccountTransferType.INTEREST_TRANSFER.getValue());
    }
}