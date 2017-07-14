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
package org.apache.fineract.portfolio.savings.data;

import org.apache.fineract.portfolio.savings.SavingsAccountTransactionType;

/**
 * Immutable data object represent savings account transaction type
 * enumerations.
 */
public class SavingsAccountTransactionEnumData {

    private final Long id;
    private final String code;
    private final String value;

    private final boolean deposit;
    private final boolean dividendPayout;
    private final boolean withdrawal;
    private final boolean interestPosting;
    private final boolean feeDeduction;
    private final boolean initiateTransfer;
    private final boolean approveTransfer;
    private final boolean withdrawTransfer;
    private final boolean rejectTransfer;
    private final boolean overdraftInterest;
    private final boolean writtenoff;
    private final boolean overdraftFee = true;
    private final boolean withholdTax;
    private final boolean escheat;
    private final boolean amountHold;
    private final boolean amountRelease;

    public SavingsAccountTransactionEnumData(final Long id, final String code, final String value) {
        this.id = id;
        this.code = code;
        this.value = value;
        this.deposit = Long.valueOf(SavingsAccountTransactionType.DEPOSIT.getValue()).equals(this.id);
        this.dividendPayout = Long.valueOf(SavingsAccountTransactionType.DIVIDEND_PAYOUT.getValue()).equals(this.id);
        this.withdrawal = Long.valueOf(SavingsAccountTransactionType.WITHDRAWAL.getValue()).equals(this.id);
        this.interestPosting = Long.valueOf(SavingsAccountTransactionType.INTEREST_POSTING.getValue()).equals(this.id);
        this.feeDeduction = Long.valueOf(SavingsAccountTransactionType.ANNUAL_FEE.getValue()).equals(this.id)
                || Long.valueOf(SavingsAccountTransactionType.WITHDRAWAL_FEE.getValue()).equals(this.id)
                || Long.valueOf(SavingsAccountTransactionType.PAY_CHARGE.getValue()).equals(this.id);
        this.initiateTransfer = Long.valueOf(SavingsAccountTransactionType.INITIATE_TRANSFER.getValue()).equals(this.id);
        this.approveTransfer = Long.valueOf(SavingsAccountTransactionType.APPROVE_TRANSFER.getValue()).equals(this.id);
        this.withdrawTransfer = Long.valueOf(SavingsAccountTransactionType.WITHDRAW_TRANSFER.getValue()).equals(this.id);
        this.rejectTransfer = Long.valueOf(SavingsAccountTransactionType.REJECT_TRANSFER.getValue()).equals(this.id);
        this.writtenoff = Long.valueOf(SavingsAccountTransactionType.WRITTEN_OFF.getValue()).equals(this.id);
        this.overdraftInterest = Long.valueOf(SavingsAccountTransactionType.OVERDRAFT_INTEREST.getValue()).equals(this.id);
        this.withholdTax = Long.valueOf(SavingsAccountTransactionType.WITHHOLD_TAX.getValue()).equals(this.id);
        this.escheat = Long.valueOf(SavingsAccountTransactionType.ESCHEAT.getValue()).equals(this.id);
        this.amountHold = Long.valueOf(SavingsAccountTransactionType.AMOUNT_HOLD.getValue()).equals(this.id);
        this.amountRelease = Long.valueOf(SavingsAccountTransactionType.AMOUNT_RELEASE.getValue()).equals(this.id);
        // this.overdraftFee =
        // Long.valueOf(SavingsAccountTransactionType.OVERDRAFT_INTEREST.getValue()).equals(this.id);
    }

    public Long getId() {
        return this.id;
    }

    public String getCode() {
        return this.code;
    }

    public String getValue() {
        return this.value;
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

    public boolean isInitiateTransfer() {
        return this.initiateTransfer;
    }

    public boolean isApproveTransfer() {
        return this.approveTransfer;
    }

    public boolean isWithdrawTransfer() {
        return this.withdrawTransfer;
    }

    public boolean isRejectTransfer() {
        return this.rejectTransfer;
    }

    public boolean isOverdraftInterest() {
        return this.overdraftInterest;
    }

    public boolean isWrittenoff() {
        return this.writtenoff;
    }

    public boolean isOverdraftFee() {
        return this.overdraftFee;
    }

    public boolean isWithholdTax() {
        return this.withholdTax;
    }
    
    public boolean isDividendPayout() {
        return this.dividendPayout;
    }
    
    public boolean isEscheat() {
    	return this.escheat;
    }
    
    public boolean isAmountOnHold() {
        return this.amountHold;
    }

    public boolean isAmountRelease() {
        return this.amountRelease;
    }

}