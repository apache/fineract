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

import jakarta.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.fineract.portfolio.TransactionEntryType;

/**
 * An enumeration of different transactions that can occur on a {@link SavingsAccount}.
 */
public enum SavingsAccountTransactionType {

    INVALID(0, "savingsAccountTransactionType.invalid"), //
    DEPOSIT(1, "savingsAccountTransactionType.deposit", TransactionEntryType.CREDIT), //
    WITHDRAWAL(2, "savingsAccountTransactionType.withdrawal", TransactionEntryType.DEBIT), //
    INTEREST_POSTING(3, "savingsAccountTransactionType.interestPosting", TransactionEntryType.CREDIT), //
    WITHDRAWAL_FEE(4, "savingsAccountTransactionType.withdrawalFee", TransactionEntryType.DEBIT), //
    ANNUAL_FEE(5, "savingsAccountTransactionType.annualFee", TransactionEntryType.DEBIT), //
    WAIVE_CHARGES(6, "savingsAccountTransactionType.waiveCharge"), //
    PAY_CHARGE(7, "savingsAccountTransactionType.payCharge", TransactionEntryType.DEBIT), //
    DIVIDEND_PAYOUT(8, "savingsAccountTransactionType.dividendPayout", TransactionEntryType.CREDIT), //
    ACCRUAL(10, "savingsAccountTransactionType.accrual"), //
    INITIATE_TRANSFER(12, "savingsAccountTransactionType.initiateTransfer"), //
    APPROVE_TRANSFER(13, "savingsAccountTransactionType.approveTransfer"), //
    WITHDRAW_TRANSFER(14, "savingsAccountTransactionType.withdrawTransfer"), //
    REJECT_TRANSFER(15, "savingsAccountTransactionType.rejectTransfer"), //
    WRITTEN_OFF(16, "savingsAccountTransactionType.writtenoff"), //
    OVERDRAFT_INTEREST(17, "savingsAccountTransactionType.overdraftInterest", TransactionEntryType.DEBIT), //
    WITHHOLD_TAX(18, "savingsAccountTransactionType.withholdTax", TransactionEntryType.DEBIT), //
    ESCHEAT(19, "savingsAccountTransactionType.escheat", TransactionEntryType.DEBIT), //
    AMOUNT_HOLD(20, "savingsAccountTransactionType.onHold", TransactionEntryType.DEBIT), //
    AMOUNT_RELEASE(21, "savingsAccountTransactionType.release", TransactionEntryType.CREDIT); //

    private static final Map<Integer, SavingsAccountTransactionType> BY_ID = Arrays.stream(values())
            .collect(Collectors.toMap(SavingsAccountTransactionType::getValue, v -> v));

    private final int value;
    private final String code;
    private final TransactionEntryType entryType;

    SavingsAccountTransactionType(final Integer value, final String code, TransactionEntryType entryType) {
        this.value = value;
        this.code = code;
        this.entryType = entryType;
    }

    SavingsAccountTransactionType(final Integer value, final String code) {
        this(value, code, null);
    }

    public int getId() {
        return this.value;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public TransactionEntryType getEntryType() {
        return entryType;
    }

    public boolean isCreditEntryType() {
        return entryType != null && entryType.isCredit();
    }

    public boolean isDebitEntryType() {
        return entryType != null && entryType.isDebit();
    }

    public static SavingsAccountTransactionType fromInt(final Integer value) {
        SavingsAccountTransactionType transactionType = BY_ID.get(value);
        return transactionType == null ? INVALID : transactionType;
    }

    public boolean isValid() {
        return this != INVALID;
    }

    public boolean isDeposit() {
        return this == DEPOSIT;
    }

    public boolean isWithdrawal() {
        return this == WITHDRAWAL;
    }

    public boolean isInterestPosting() {
        return this == INTEREST_POSTING;
    }

    public boolean isOverDraftInterestPosting() {
        return this == OVERDRAFT_INTEREST;
    }

    public boolean isWithHoldTax() {
        return this == WITHHOLD_TAX;
    }

    public boolean isWithdrawalFee() {
        return this == WITHDRAWAL_FEE;
    }

    public boolean isAnnualFee() {
        return this == ANNUAL_FEE;
    }

    public boolean isPayCharge() {
        return this == PAY_CHARGE;
    }

    public boolean isChargeTransaction() {
        return isPayCharge() || isWithdrawalFee() || isAnnualFee();
    }

    public boolean isWaiveCharge() {
        return this == WAIVE_CHARGES;
    }

    public boolean isTransferInitiation() {
        return this == INITIATE_TRANSFER;
    }

    public boolean isTransferApproval() {
        return this == APPROVE_TRANSFER;
    }

    public boolean isTransferRejection() {
        return this == REJECT_TRANSFER;
    }

    public boolean isTransferWithdrawal() {
        return this == WITHDRAW_TRANSFER;
    }

    public boolean isWrittenoff() {
        return this == WRITTEN_OFF;
    }

    public boolean isDividendPayout() {
        return this == DIVIDEND_PAYOUT;
    }

    public boolean isIncomeFromInterest() {
        return this == OVERDRAFT_INTEREST;
    }

    public boolean isEscheat() {
        return this == ESCHEAT;
    }

    public boolean isAmountOnHold() {
        return this == AMOUNT_HOLD;
    }

    public boolean isAmountRelease() {
        return this == AMOUNT_RELEASE;
    }

    public boolean isAccrual() {
        return this == ACCRUAL;
    }

    public boolean isCredit() {
        // AMOUNT_RELEASE is not credit, because the account balance is not changed
        return isCreditEntryType() && !isAmountRelease();
    }

    public boolean isDebit() {
        // AMOUNT_HOLD, ESCHEAT are not debit, because the account balance is not changed
        return isDebitEntryType() && !isAmountOnHold() && !isEscheat();
    }

    @NotNull
    public static List<SavingsAccountTransactionType> getFiltered(Predicate<SavingsAccountTransactionType> filter) {
        return Arrays.stream(values()).filter(filter).toList();
    }
}
