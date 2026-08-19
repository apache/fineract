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
package org.apache.fineract.accounting.journalentry.data;

public interface LoanTransactionTypeDTO {

    Long getId();

    String getCode();

    String getValue();

    boolean isAccrualAdjustment();

    boolean isAccrual();

    boolean isApproveTransfer();

    boolean isBuyDownFee();

    boolean isBuyDownFeeAdjustment();

    boolean isBuyDownFeeAmortization();

    boolean isBuyDownFeeAmortizationAdjustment();

    boolean isCapitalizedIncome();

    boolean isCapitalizedIncomeAdjustment();

    boolean isCapitalizedIncomeAmortization();

    boolean isCapitalizedIncomeAmortizationAdjustment();

    boolean isChargeAdjustment();

    boolean isChargePayment();

    boolean isChargeRefund();

    boolean isChargeback();

    boolean isChargeoff();

    boolean isCreditBalanceRefund();

    boolean isDisbursement();

    boolean isGoodwillCredit();

    boolean isInitiateTransfer();

    boolean isInterestPaymentWaiver();

    boolean isInterestRefund();

    boolean isMerchantIssuedRefund();

    boolean isPayoutRefund();

    boolean isRecoveryRepayment();

    boolean isRefund();

    boolean isRefundForActiveLoans();

    boolean isRepayment();

    boolean isRepaymentAtDisbursement();

    boolean isRepaymentType();

    boolean isWaiveCharges();

    boolean isWaiveInterest();

    boolean isWithdrawTransfer();

    boolean isWriteOff();
}
