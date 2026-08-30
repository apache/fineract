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
package org.apache.fineract.portfolio.loanaccount.data;

import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import org.apache.fineract.accounting.journalentry.data.LoanTransactionTypeDTO;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;

/**
 * Immutable data object represent loan status enumerations.
 */
@Getter
public class LoanTransactionEnumData implements Serializable, LoanTransactionTypeDTO {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Long id;
    private final String code;
    private final String value;

    private final boolean disbursement;
    private final boolean repaymentAtDisbursement;
    private final boolean repayment;
    private final boolean merchantIssuedRefund;
    private final boolean payoutRefund;
    private final boolean goodwillCredit;
    private final boolean interestPaymentWaiver;
    private final boolean chargeRefund;
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
    private final boolean creditBalanceRefund;
    private final boolean chargeAdjustment;
    private final boolean chargeback;
    private final boolean chargeoff;
    private final boolean downPayment;
    private final boolean reAge;
    private final boolean reAmortize;
    private final boolean accrualActivity;
    private final boolean interestRefund;
    private final boolean accrualAdjustment;
    private final boolean capitalizedIncome;
    private final boolean capitalizedIncomeAmortization;
    private final boolean capitalizedIncomeAdjustment;
    private final boolean capitalizedIncomeAmortizationAdjustment;
    private final boolean contractTermination;
    private final boolean buyDownFee;
    private final boolean buyDownFeeAdjustment;
    private final boolean buyDownFeeAmortization;
    private final boolean buyDownFeeAmortizationAdjustment;

    public LoanTransactionEnumData(final Long id, final String code, final String value) {
        this.id = id;
        this.code = code;
        this.value = value;
        this.disbursement = Long.valueOf(LoanTransactionType.DISBURSEMENT.getValue()).equals(this.id);
        this.repaymentAtDisbursement = Long.valueOf(LoanTransactionType.REPAYMENT_AT_DISBURSEMENT.getValue()).equals(this.id);
        this.repayment = Long.valueOf(LoanTransactionType.REPAYMENT.getValue()).equals(this.id);
        this.merchantIssuedRefund = Long.valueOf(LoanTransactionType.MERCHANT_ISSUED_REFUND.getValue()).equals(this.id);
        this.payoutRefund = Long.valueOf(LoanTransactionType.PAYOUT_REFUND.getValue()).equals(this.id);
        this.goodwillCredit = Long.valueOf(LoanTransactionType.GOODWILL_CREDIT.getValue()).equals(this.id);
        this.chargeRefund = Long.valueOf(LoanTransactionType.CHARGE_REFUND.getValue()).equals(this.id);
        this.contra = Long.valueOf(LoanTransactionType.CONTRA.getValue()).equals(this.id);
        this.waiveInterest = Long.valueOf(LoanTransactionType.WAIVE_INTEREST.getValue()).equals(this.id);
        this.waiveCharges = Long.valueOf(LoanTransactionType.WAIVE_CHARGES.getValue()).equals(this.id);
        this.accrual = Long.valueOf(LoanTransactionType.ACCRUAL.getValue()).equals(this.id);
        this.writeOff = Long.valueOf(LoanTransactionType.WRITEOFF.getValue()).equals(this.id);
        this.recoveryRepayment = Long.valueOf(LoanTransactionType.RECOVERY_REPAYMENT.getValue()).equals(this.id);
        this.initiateTransfer = Long.valueOf(LoanTransactionType.INITIATE_TRANSFER.getValue()).equals(this.id);
        this.approveTransfer = Long.valueOf(LoanTransactionType.APPROVE_TRANSFER.getValue()).equals(this.id);
        this.withdrawTransfer = Long.valueOf(LoanTransactionType.WITHDRAW_TRANSFER.getValue()).equals(this.id);
        this.rejectTransfer = Long.valueOf(LoanTransactionType.REJECT_TRANSFER.getValue()).equals(this.id);
        this.refund = Long.valueOf(LoanTransactionType.REFUND.getValue()).equals(this.id);
        this.chargePayment = Long.valueOf(LoanTransactionType.CHARGE_PAYMENT.getValue()).equals(this.id);
        this.refundForActiveLoans = Long.valueOf(LoanTransactionType.REFUND_FOR_ACTIVE_LOAN.getValue()).equals(this.id);
        this.creditBalanceRefund = Long.valueOf(LoanTransactionType.CREDIT_BALANCE_REFUND.getValue()).equals(this.id);
        this.chargeback = Long.valueOf(LoanTransactionType.CHARGEBACK.getValue()).equals(this.id);
        this.chargeAdjustment = Long.valueOf(LoanTransactionType.CHARGE_ADJUSTMENT.getValue()).equals(this.id);
        this.chargeoff = Long.valueOf(LoanTransactionType.CHARGE_OFF.getValue()).equals(this.id);
        this.downPayment = Long.valueOf(LoanTransactionType.DOWN_PAYMENT.getValue()).equals(this.id);
        this.interestPaymentWaiver = Long.valueOf(LoanTransactionType.INTEREST_PAYMENT_WAIVER.getValue()).equals(this.id);
        this.accrualActivity = Long.valueOf(LoanTransactionType.ACCRUAL_ACTIVITY.getValue()).equals(this.id);
        this.reAge = Long.valueOf(LoanTransactionType.REAGE.getValue()).equals(this.id);
        this.reAmortize = Long.valueOf(LoanTransactionType.REAMORTIZE.getValue()).equals(this.id);
        this.interestRefund = Long.valueOf(LoanTransactionType.INTEREST_REFUND.getValue()).equals(this.id);
        this.accrualAdjustment = Long.valueOf(LoanTransactionType.ACCRUAL_ADJUSTMENT.getValue()).equals(this.id);
        this.capitalizedIncome = Long.valueOf(LoanTransactionType.CAPITALIZED_INCOME.getValue()).equals(this.id);
        this.capitalizedIncomeAmortization = Long.valueOf(LoanTransactionType.CAPITALIZED_INCOME_AMORTIZATION.getValue()).equals(this.id);
        this.capitalizedIncomeAdjustment = Long.valueOf(LoanTransactionType.CAPITALIZED_INCOME_ADJUSTMENT.getValue()).equals(this.id);
        this.capitalizedIncomeAmortizationAdjustment = Long
                .valueOf(LoanTransactionType.CAPITALIZED_INCOME_AMORTIZATION_ADJUSTMENT.getValue()).equals(this.id);
        this.contractTermination = Long.valueOf(LoanTransactionType.CONTRACT_TERMINATION.getValue()).equals(this.id);
        this.buyDownFee = Long.valueOf(LoanTransactionType.BUY_DOWN_FEE.getValue()).equals(this.id);
        this.buyDownFeeAdjustment = Long.valueOf(LoanTransactionType.BUY_DOWN_FEE_ADJUSTMENT.getValue()).equals(this.id);
        this.buyDownFeeAmortization = Long.valueOf(LoanTransactionType.BUY_DOWN_FEE_AMORTIZATION.getValue()).equals(this.id);
        this.buyDownFeeAmortizationAdjustment = Long.valueOf(LoanTransactionType.BUY_DOWN_FEE_AMORTIZATION_ADJUSTMENT.getValue())
                .equals(this.id);
    }

    @Override
    public boolean isRepaymentType() {
        return isRepayment() || isMerchantIssuedRefund() || isPayoutRefund() || isGoodwillCredit() || isChargeRefund()
                || isChargeAdjustment() || isDownPayment();
    }
}
