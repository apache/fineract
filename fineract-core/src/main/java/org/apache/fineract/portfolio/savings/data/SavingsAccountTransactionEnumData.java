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

import java.io.Serializable;
import lombok.Getter;
import org.apache.fineract.portfolio.TransactionEntryType;
import org.apache.fineract.portfolio.savings.SavingsAccountTransactionType;

/**
 * Immutable data object represent savings account transaction type enumerations.
 */
@Getter
public class SavingsAccountTransactionEnumData implements Serializable {

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
    private final boolean overdraftFee;
    private final boolean withholdTax;
    private final boolean escheat;
    private final boolean amountHold;
    private final boolean amountRelease;
    private final boolean accrual;

    public SavingsAccountTransactionEnumData(final Long id, final String code, final String value) {
        this.id = id;
        this.code = code;
        this.value = value;
        SavingsAccountTransactionType transactionType = id == null ? null : SavingsAccountTransactionType.fromInt(id.intValue());
        this.deposit = transactionType == SavingsAccountTransactionType.DEPOSIT;
        this.dividendPayout = transactionType == SavingsAccountTransactionType.DIVIDEND_PAYOUT;
        this.accrual = transactionType == SavingsAccountTransactionType.ACCRUAL;
        this.withdrawal = transactionType == SavingsAccountTransactionType.WITHDRAWAL;
        this.interestPosting = transactionType == SavingsAccountTransactionType.INTEREST_POSTING;
        this.feeDeduction = transactionType == SavingsAccountTransactionType.ANNUAL_FEE
                || transactionType == SavingsAccountTransactionType.WITHDRAWAL_FEE
                || transactionType == SavingsAccountTransactionType.PAY_CHARGE;
        this.initiateTransfer = transactionType == SavingsAccountTransactionType.INITIATE_TRANSFER;
        this.approveTransfer = transactionType == SavingsAccountTransactionType.APPROVE_TRANSFER;
        this.withdrawTransfer = transactionType == SavingsAccountTransactionType.WITHDRAW_TRANSFER;
        this.rejectTransfer = transactionType == SavingsAccountTransactionType.REJECT_TRANSFER;
        this.writtenoff = transactionType == SavingsAccountTransactionType.WRITTEN_OFF;
        this.overdraftFee = false;
        this.overdraftInterest = transactionType == SavingsAccountTransactionType.OVERDRAFT_INTEREST;
        this.withholdTax = transactionType == SavingsAccountTransactionType.WITHHOLD_TAX;
        this.escheat = transactionType == SavingsAccountTransactionType.ESCHEAT;
        this.amountHold = transactionType == SavingsAccountTransactionType.AMOUNT_HOLD;
        this.amountRelease = transactionType == SavingsAccountTransactionType.AMOUNT_RELEASE;
    }

    public boolean isIncomeFromInterest() {
        return this.overdraftInterest;
    }

    public boolean isOverDraftInterestPosting() {
        return this.overdraftInterest;
    }

    public boolean isDepositOrWithdrawal() {
        return this.deposit || this.withdrawal;
    }

    public boolean isChargeTransaction() {
        return feeDeduction;
    }

    public boolean isAnnualFee() {
        return Long.valueOf(SavingsAccountTransactionType.ANNUAL_FEE.getValue()).equals(this.id);
    }

    public boolean isPayCharge() {
        return Long.valueOf(SavingsAccountTransactionType.PAY_CHARGE.getValue()).equals(this.id);
    }

    public boolean isWithdrawalFee() {
        return Long.valueOf(SavingsAccountTransactionType.WITHDRAWAL_FEE.getValue()).equals(this.id);
    }

    public boolean isCredit() {
        SavingsAccountTransactionType transactionType = getTransactionTypeEnum();
        return transactionType != null && transactionType.isCredit();
    }

    public boolean isDebit() {
        SavingsAccountTransactionType transactionType = getTransactionTypeEnum();
        return transactionType != null && transactionType.isDebit();
    }

    public TransactionEntryType getEntryType() {
        SavingsAccountTransactionType transactionType = getTransactionTypeEnum();
        return transactionType == null ? null : transactionType.getEntryType();
    }

    public SavingsAccountTransactionType getTransactionTypeEnum() {
        return id == null ? null : SavingsAccountTransactionType.fromInt(id.intValue());
    }
}
