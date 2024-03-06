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

import lombok.Getter;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;

/**
 * Immutable data object represent loan status enumerations.
 */
@Getter
public class LoanTransactionEnumData {

    private final Long id;
    private final String code;
    private final String value;

    private final boolean disbursement;
    private final boolean repaymentAtDisbursement;
    private final boolean repayment;
    private final boolean merchantIssuedRefund;
    private final boolean payoutRefund;
    private final boolean goodwillCredit;
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

    public LoanTransactionEnumData(final Long id, final String code, final String value) {
        this.id = id;
        this.code = code;
        this.value = value;
        this.disbursement = Long.valueOf(1).equals(this.id);
        this.repaymentAtDisbursement = Long.valueOf(5).equals(this.id);
        this.repayment = Long.valueOf(2).equals(this.id);
        this.merchantIssuedRefund = Long.valueOf(21).equals(this.id);
        this.payoutRefund = Long.valueOf(22).equals(this.id);
        this.goodwillCredit = Long.valueOf(23).equals(this.id);
        this.chargeRefund = Long.valueOf(24).equals(this.id);
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
        this.creditBalanceRefund = Long.valueOf(20).equals(this.id);
        this.chargeback = Long.valueOf(25).equals(this.id);
        this.chargeAdjustment = Long.valueOf(26).equals(this.id);
        this.chargeoff = Long.valueOf(27).equals(this.id);
        this.downPayment = Long.valueOf(28).equals(this.id);
        this.reAge = Long.valueOf(LoanTransactionType.REAGE.getValue()).equals(this.id);
        this.reAmortize = Long.valueOf(LoanTransactionType.REAMORTIZE.getValue()).equals(this.id);
    }

    public boolean isRepaymentType() {
        if (isRepayment() || isMerchantIssuedRefund() || isPayoutRefund() || isGoodwillCredit() || isChargeRefund() || isChargeAdjustment()
                || isDownPayment()) {
            return true;
        }
        return false;
    }
}
