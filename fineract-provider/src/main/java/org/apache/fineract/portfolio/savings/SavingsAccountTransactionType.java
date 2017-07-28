/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.savings;

import org.apache.fineract.portfolio.savings.domain.SavingsAccount;

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
    WAIVE_CHARGES(6, "savingsAccountTransactionType.waiveCharge"), //
    PAY_CHARGE(7, "savingsAccountTransactionType.payCharge"), //
    DIVIDEND_PAYOUT(8, "savingsAccountTransactionType.dividendPayout"), //
    INITIATE_TRANSFER(12, "savingsAccountTransactionType.initiateTransfer"), //
    APPROVE_TRANSFER(13, "savingsAccountTransactionType.approveTransfer"), //
    WITHDRAW_TRANSFER(14, "savingsAccountTransactionType.withdrawTransfer"), //
    REJECT_TRANSFER(15, "savingsAccountTransactionType.rejectTransfer"), WRITTEN_OFF(16, "savingsAccountTransactionType.writtenoff"), //
    OVERDRAFT_INTEREST(17, "savingsAccountTransactionType.overdraftInterest"), //
    WITHHOLD_TAX(18, "savingsAccountTransactionType.withholdTax"),
    ESCHEAT(19, "savingsAccountTransactionType.escheat"),
    AMOUNT_HOLD(20, "savingsAccountTransactionType.onHold"),
    AMOUNT_RELEASE(21, "savingsAccountTransactionType.release");

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
        return this.code;
    }

    public static SavingsAccountTransactionType fromInt(final Integer transactionType) {

        if (transactionType == null) { return SavingsAccountTransactionType.INVALID; }

        SavingsAccountTransactionType savingsAccountTransactionType = SavingsAccountTransactionType.INVALID;
        switch (transactionType) {
            case 1:
                savingsAccountTransactionType = SavingsAccountTransactionType.DEPOSIT;
            break;
            case 2:
                savingsAccountTransactionType = SavingsAccountTransactionType.WITHDRAWAL;
            break;
            case 3:
                savingsAccountTransactionType = SavingsAccountTransactionType.INTEREST_POSTING;
            break;
            case 4:
                savingsAccountTransactionType = SavingsAccountTransactionType.WITHDRAWAL_FEE;
            break;
            case 5:
                savingsAccountTransactionType = SavingsAccountTransactionType.ANNUAL_FEE;
            break;
            case 6:
                savingsAccountTransactionType = SavingsAccountTransactionType.WAIVE_CHARGES;
            break;
            case 7:
                savingsAccountTransactionType = SavingsAccountTransactionType.PAY_CHARGE;
            break;
            case 8:
                savingsAccountTransactionType = SavingsAccountTransactionType.DIVIDEND_PAYOUT;
            break;
            case 12:
                savingsAccountTransactionType = SavingsAccountTransactionType.INITIATE_TRANSFER;
            break;
            case 13:
                savingsAccountTransactionType = SavingsAccountTransactionType.APPROVE_TRANSFER;
            break;
            case 14:
                savingsAccountTransactionType = SavingsAccountTransactionType.WITHDRAW_TRANSFER;
            break;
            case 15:
                savingsAccountTransactionType = SavingsAccountTransactionType.REJECT_TRANSFER;
            break;
            case 16:
                savingsAccountTransactionType = SavingsAccountTransactionType.WRITTEN_OFF;
            break;
            case 17:
                savingsAccountTransactionType = SavingsAccountTransactionType.OVERDRAFT_INTEREST;
            break;
            case 18:
                savingsAccountTransactionType = SavingsAccountTransactionType.WITHHOLD_TAX;
            break;
            case 19:
            	savingsAccountTransactionType = SavingsAccountTransactionType.ESCHEAT;
            break;
            case 20:
                savingsAccountTransactionType = SavingsAccountTransactionType.AMOUNT_HOLD;
            break;
            case 21:
                savingsAccountTransactionType = SavingsAccountTransactionType.AMOUNT_RELEASE;
            break;
        }
        return savingsAccountTransactionType;
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

	public boolean isOverDraftInterestPosting() {
		return this.value.equals(SavingsAccountTransactionType.OVERDRAFT_INTEREST.getValue());
	}
	
    public boolean isWithHoldTax() {
        return this.value.equals(SavingsAccountTransactionType.WITHHOLD_TAX.getValue());
    }

    public boolean isWithdrawalFee() {
        return this.value.equals(SavingsAccountTransactionType.WITHDRAWAL_FEE.getValue());
    }

    public boolean isAnnualFee() {
        return this.value.equals(SavingsAccountTransactionType.ANNUAL_FEE.getValue());
    }

    public boolean isPayCharge() {
        return this.value.equals(SavingsAccountTransactionType.PAY_CHARGE.getValue());
    }

    public boolean isChargeTransaction() {
        return isPayCharge() || isWithdrawalFee() || isAnnualFee();
    }

    public boolean isWaiveCharge() {
        return this.value.equals(SavingsAccountTransactionType.WAIVE_CHARGES.getValue());
    }

    public boolean isTransferInitiation() {
        return this.value.equals(SavingsAccountTransactionType.INITIATE_TRANSFER.getValue());
    }

    public boolean isTransferApproval() {
        return this.value.equals(SavingsAccountTransactionType.APPROVE_TRANSFER.getValue());
    }

    public boolean isTransferRejection() {
        return this.value.equals(SavingsAccountTransactionType.REJECT_TRANSFER.getValue());
    }

    public boolean isTransferWithdrawal() {
        return this.value.equals(SavingsAccountTransactionType.WITHDRAW_TRANSFER.getValue());
    }

    public boolean isWrittenoff() {
        return this.value.equals(SavingsAccountTransactionType.WRITTEN_OFF.getValue());
    }

    public boolean isDividendPayout() {
        return this.value.equals(SavingsAccountTransactionType.DIVIDEND_PAYOUT.getValue());
    }

    public boolean isIncomeFromInterest() {
        return this.value.equals(SavingsAccountTransactionType.OVERDRAFT_INTEREST.getValue());
    }

    public boolean isEscheat() {
        return this.value.equals(SavingsAccountTransactionType.ESCHEAT.getValue());
    }
    
    public boolean isAmountOnHold() {
        return this.value.equals(SavingsAccountTransactionType.AMOUNT_HOLD.getValue());
    }

    public boolean isAmountRelease() {
        return this.value.equals(SavingsAccountTransactionType.AMOUNT_RELEASE.getValue());
    }

    public boolean isDebit() {
        return isWithdrawal() || isWithdrawalFee() || isAnnualFee() || isPayCharge() || isIncomeFromInterest() || isWithHoldTax() || isEscheat() || isAmountOnHold();
    }

    public boolean isCredit() {
        return isDeposit() || isInterestPosting() || isDividendPayout() || isAmountRelease();
    }
}