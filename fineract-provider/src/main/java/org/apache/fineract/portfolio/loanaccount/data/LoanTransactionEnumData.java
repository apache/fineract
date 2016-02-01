/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.data;

/**
 * Immutable data object represent loan status enumerations.
 */
@SuppressWarnings("unused")
public class LoanTransactionEnumData {

    private final Long id;
    private final String code;
    private final String value;

    private final boolean disbursement;
    private final boolean repaymentAtDisbursement;
    private final boolean repayment;
    private final boolean contra;
    private final boolean waiveInterest;
    private final boolean waiveCharges;
    private final boolean accrual;
    private final boolean writeOff;
    private final boolean recoveryRepayment;
    private final boolean initiateTransfer;
    private final boolean approveTransfer;
    private final boolean withdrawTransfer;
    private final boolean rejectTransfer;
    private final boolean chargePayment;
    private final boolean refund;
    private final boolean refundForActiveLoans;

    public LoanTransactionEnumData(final Long id, final String code, final String value) {
        this.id = id;
        this.code = code;
        this.value = value;
        this.disbursement = Long.valueOf(1).equals(this.id);
        this.repaymentAtDisbursement = Long.valueOf(5).equals(this.id);
        this.repayment = Long.valueOf(2).equals(this.id);
        this.contra = Long.valueOf(3).equals(this.id);
        this.waiveInterest = Long.valueOf(4).equals(this.id);
        this.waiveCharges = Long.valueOf(9).equals(this.id);
        this.accrual = Long.valueOf(10).equals(this.id);
        this.writeOff = Long.valueOf(6).equals(this.id);
        this.recoveryRepayment = Long.valueOf(8).equals(this.id);
        this.initiateTransfer = Long.valueOf(12).equals(this.id);
        this.approveTransfer = Long.valueOf(13).equals(this.id);
        this.withdrawTransfer = Long.valueOf(14).equals(this.id);
        this.rejectTransfer = Long.valueOf(15).equals(this.id);
        this.refund = Long.valueOf(16).equals(this.id);
        this.chargePayment = Long.valueOf(17).equals(this.id);
        this.refundForActiveLoans = Long.valueOf(18).equals(this.id);
    }

    public Long id() {
        return this.id;
    }

    public String getCode() {
        return this.code;
    }

    public String getValue() {
        return this.value;
    }

    /**
     * Returns true if the transaction involves either a payment (disbursement)
     * or a receipt (repayments, repayments during disbursement etc)
     * 
     * @return
     */
    public boolean isPaymentOrReceipt() {
        if (isDisbursement() || isRepayment() || isRepaymentAtDisbursement() || isRecoveryRepayment()) { return true; }
        return false;
    }

    public boolean isDisbursement() {
        return this.disbursement;
    }

    public boolean isRepaymentAtDisbursement() {
        return this.repaymentAtDisbursement;
    }

    public boolean isRepayment() {
        return this.repayment;
    }

    public boolean isWaiveInterest() {
        return this.waiveInterest;
    }

    public boolean isWaiveCharges() {
        return this.waiveCharges;
    }

    public boolean isWriteOff() {
        return this.writeOff;
    }

    public boolean isRecoveryRepayment() {
        return this.recoveryRepayment;
    }

    public boolean isAccrual() {
        return this.accrual;
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

    public boolean isChargePayment() {
        return this.chargePayment;
    }

    public boolean isRefund() {
        return this.refund;
    }
    
    public boolean isRefundForActiveLoans() {
        return this.refundForActiveLoans;
    }

}