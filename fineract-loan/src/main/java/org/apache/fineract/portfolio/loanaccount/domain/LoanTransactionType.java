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
package org.apache.fineract.portfolio.loanaccount.domain;

public enum LoanTransactionType {

    INVALID(0, "loanTransactionType.invalid"), //
    DISBURSEMENT(1, "loanTransactionType.disbursement"), //
    REPAYMENT(2, "loanTransactionType.repayment"), //
    CONTRA(3, "loanTransactionType.contra"), //
    WAIVE_INTEREST(4, "loanTransactionType.waiver"), //
    REPAYMENT_AT_DISBURSEMENT(5, "loanTransactionType.repaymentAtDisbursement"), //
    WRITEOFF(6, "loanTransactionType.writeOff"), //
    MARKED_FOR_RESCHEDULING(7, "loanTransactionType.marked.for.rescheduling"), //
    /**
     * This type of transactions is allowed on written-off loans where mfi still attempts to recover payments from
     * applicant after writing-off.
     */
    RECOVERY_REPAYMENT(8, "loanTransactionType.recoveryRepayment"), //
    WAIVE_CHARGES(9, "loanTransactionType.waiveCharges"), //
    // UNDO_WAIVE_CHARGE(20, "loanTransactionType.undoWaiveCharge"),
    /**
     * Transaction represents an Accrual (For either interest, charge or a penalty
     **/
    ACCRUAL(10, "loanTransactionType.accrual"), //

    /***
     * A Loan Transfer involves two steps, first a "initiate" Loan transfer transaction done by the Source branch
     * followed by a "complete" loan transaction initiated by the destination branch
     **/
    INITIATE_TRANSFER(12, "loanTransactionType.initiateTransfer"), //
    APPROVE_TRANSFER(13, "loanTransactionType.approveTransfer"), //
    WITHDRAW_TRANSFER(14, "loanTransactionType.withdrawTransfer"), //
    REJECT_TRANSFER(15, "loanTransactionType.rejectTransfer"), //
    REFUND(16, "loanTransactionType.refund"), //
    CHARGE_PAYMENT(17, "loanTransactionType.chargePayment"), //
    REFUND_FOR_ACTIVE_LOAN(18, "loanTransactionType.refund"), //
    INCOME_POSTING(19, "loanTransactionType.incomePosting"), //
    CREDIT_BALANCE_REFUND(20, "loanTransactionType.creditBalanceRefund"), //
    MERCHANT_ISSUED_REFUND(21, "loanTransactionType.merchantIssuedRefund"), //
    PAYOUT_REFUND(22, "loanTransactionType.payoutRefund"), //
    GOODWILL_CREDIT(23, "loanTransactionType.goodwillCredit"), //
    CHARGE_REFUND(24, "loanTransactionType.chargeRefund"), //
    CHARGEBACK(25, "loanTransactionType.chargeback"), //
    CHARGE_ADJUSTMENT(26, "loanTransactionType.chargeAdjustment"), //
    CHARGE_OFF(27, "loanTransactionType.chargeOff"), //
    DOWN_PAYMENT(28, "loanTransactionType.downPayment"), //
    REAGE(29, "loanTransactionType.reAge"), REAMORTIZE(30, "loanTransactionType.reAmortize");

    private final Integer value;
    private final String code;

    LoanTransactionType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public static LoanTransactionType fromInt(final Integer transactionType) {

        if (transactionType == null) {
            return LoanTransactionType.INVALID;
        }

        return switch (transactionType) {
            case 1 -> LoanTransactionType.DISBURSEMENT;
            case 2 -> LoanTransactionType.REPAYMENT;
            case 3 -> LoanTransactionType.CONTRA;
            case 4 -> LoanTransactionType.WAIVE_INTEREST;
            case 5 -> LoanTransactionType.REPAYMENT_AT_DISBURSEMENT;
            case 6 -> LoanTransactionType.WRITEOFF;
            case 7 -> LoanTransactionType.MARKED_FOR_RESCHEDULING;
            case 8 -> LoanTransactionType.RECOVERY_REPAYMENT;
            case 9 -> LoanTransactionType.WAIVE_CHARGES;
            case 10 -> LoanTransactionType.ACCRUAL;
            case 12 -> LoanTransactionType.INITIATE_TRANSFER;
            case 13 -> LoanTransactionType.APPROVE_TRANSFER;
            case 14 -> LoanTransactionType.WITHDRAW_TRANSFER;
            case 15 -> LoanTransactionType.REJECT_TRANSFER;
            case 16 -> LoanTransactionType.REFUND;
            case 17 -> LoanTransactionType.CHARGE_PAYMENT;
            case 18 -> LoanTransactionType.REFUND_FOR_ACTIVE_LOAN;
            case 19 -> LoanTransactionType.INCOME_POSTING;
            case 20 -> LoanTransactionType.CREDIT_BALANCE_REFUND;
            case 21 -> LoanTransactionType.MERCHANT_ISSUED_REFUND;
            case 22 -> LoanTransactionType.PAYOUT_REFUND;
            case 23 -> LoanTransactionType.GOODWILL_CREDIT;
            case 24 -> LoanTransactionType.CHARGE_REFUND;
            case 25 -> LoanTransactionType.CHARGEBACK;
            case 26 -> LoanTransactionType.CHARGE_ADJUSTMENT;
            case 27 -> LoanTransactionType.CHARGE_OFF;
            case 28 -> LoanTransactionType.DOWN_PAYMENT;
            case 29 -> LoanTransactionType.REAGE;
            case 30 -> LoanTransactionType.REAMORTIZE;
            default -> LoanTransactionType.INVALID;
        };
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public boolean isDisbursement() {
        return this.equals(LoanTransactionType.DISBURSEMENT);
    }

    public boolean isRepaymentAtDisbursement() {
        return this.equals(LoanTransactionType.REPAYMENT_AT_DISBURSEMENT);
    }

    public boolean isRepayment() {
        return this.equals(LoanTransactionType.REPAYMENT);
    }

    public boolean isMerchantIssuedRefund() {
        return this.equals(LoanTransactionType.MERCHANT_ISSUED_REFUND);
    }

    public boolean isPayoutRefund() {
        return this.equals(LoanTransactionType.PAYOUT_REFUND);
    }

    public boolean isGoodwillCredit() {
        return this.equals(LoanTransactionType.GOODWILL_CREDIT);
    }

    public boolean isChargeRefund() {
        return this.equals(LoanTransactionType.CHARGE_REFUND);
    }

    public boolean isRepaymentType() {
        return (isRepayment() || isMerchantIssuedRefund() || isPayoutRefund() || isGoodwillCredit() || isChargeRefund() || isDownPayment());
    }

    public boolean isRecoveryRepayment() {
        return this.equals(LoanTransactionType.RECOVERY_REPAYMENT);
    }

    public boolean isWaiveInterest() {
        return this.equals(LoanTransactionType.WAIVE_INTEREST);
    }

    public boolean isWaiveCharges() {
        return this.equals(LoanTransactionType.WAIVE_CHARGES);
    }

    public boolean isAccrual() {
        return this.equals(LoanTransactionType.ACCRUAL);
    }

    public boolean isWriteOff() {
        return this.equals(LoanTransactionType.WRITEOFF);
    }

    public boolean isChargePayment() {
        return this.equals(LoanTransactionType.CHARGE_PAYMENT);
    }

    public boolean isRefundForActiveLoan() {
        return this.equals(LoanTransactionType.REFUND_FOR_ACTIVE_LOAN);
    }

    public boolean isIncomePosting() {
        return this.equals(LoanTransactionType.INCOME_POSTING);
    }

    public boolean isChargeback() {
        return this.equals(LoanTransactionType.CHARGEBACK);
    }

    public boolean isChargeAdjustment() {
        return this.equals(LoanTransactionType.CHARGE_ADJUSTMENT);
    }

    public boolean isChargeOff() {
        return this.equals(LoanTransactionType.CHARGE_OFF);
    }

    public boolean isReAge() {
        return this.equals(LoanTransactionType.REAGE);
    }

    public boolean isReAmortize() {
        return this.equals(LoanTransactionType.REAMORTIZE);
    }

    public boolean isDownPayment() {
        return this.equals(LoanTransactionType.DOWN_PAYMENT);
    }
}
