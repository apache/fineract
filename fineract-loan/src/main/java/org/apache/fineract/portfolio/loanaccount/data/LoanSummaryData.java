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

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.fineract.organisation.monetary.data.CurrencyData;

/**
 * Immutable data object representing loan summary information.
 */
@Data
@Builder
@Accessors(chain = true)
public class LoanSummaryData {

    private final CurrencyData currency;
    private final BigDecimal principalDisbursed;
    private final BigDecimal principalAdjustments;
    private final BigDecimal principalPaid;
    private final BigDecimal principalWrittenOff;
    private final BigDecimal principalOutstanding;
    private final BigDecimal principalOverdue;
    private final BigDecimal interestCharged;
    private final BigDecimal interestPaid;
    private final BigDecimal interestWaived;
    private final BigDecimal interestWrittenOff;
    private final BigDecimal interestOutstanding;
    private final BigDecimal interestOverdue;
    private final BigDecimal feeChargesCharged;
    private final BigDecimal feeAdjustments;
    private final BigDecimal feeChargesDueAtDisbursementCharged;
    private final BigDecimal feeChargesPaid;
    private final BigDecimal feeChargesWaived;
    private final BigDecimal feeChargesWrittenOff;
    private final BigDecimal feeChargesOutstanding;
    private final BigDecimal feeChargesOverdue;
    private final BigDecimal penaltyChargesCharged;
    private final BigDecimal penaltyAdjustments;
    private final BigDecimal penaltyChargesPaid;
    private final BigDecimal penaltyChargesWaived;
    private final BigDecimal penaltyChargesWrittenOff;
    private final BigDecimal penaltyChargesOutstanding;
    private final BigDecimal penaltyChargesOverdue;
    private final BigDecimal totalExpectedRepayment;
    private final BigDecimal totalRepayment;
    private final BigDecimal totalExpectedCostOfLoan;
    private final BigDecimal totalCostOfLoan;
    private final BigDecimal totalWaived;
    private final BigDecimal totalWrittenOff;
    private final BigDecimal totalOutstanding;
    private final BigDecimal totalOverdue;
    private final BigDecimal totalRecovered;
    private final LocalDate overdueSinceDate;
    private final Long writeoffReasonId;
    private final String writeoffReason;

    // Adding fields for transaction summary
    private BigDecimal totalMerchantRefund;
    private BigDecimal totalMerchantRefundReversed;
    private BigDecimal totalPayoutRefund;
    private BigDecimal totalPayoutRefundReversed;
    private BigDecimal totalGoodwillCredit;
    private BigDecimal totalGoodwillCreditReversed;
    private BigDecimal totalChargeAdjustment;
    private BigDecimal totalChargeAdjustmentReversed;
    private BigDecimal totalChargeback;
    private BigDecimal totalCreditBalanceRefund;
    private BigDecimal totalCreditBalanceRefundReversed;
    private BigDecimal totalRepaymentTransaction;
    private BigDecimal totalRepaymentTransactionReversed;
    private BigDecimal totalInterestPaymentWaiver;
    private BigDecimal totalInterestRefund;
    private final Long chargeOffReasonId;
    private final String chargeOffReason;

    private BigDecimal totalUnpaidPayableDueInterest;
    private BigDecimal totalUnpaidPayableNotDueInterest;

}
