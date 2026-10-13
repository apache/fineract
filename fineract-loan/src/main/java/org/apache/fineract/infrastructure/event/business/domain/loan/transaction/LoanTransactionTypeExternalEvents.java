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
package org.apache.fineract.infrastructure.event.business.domain.loan.transaction;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanChargebackTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.charge.LoanWaiveChargeBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.reaging.LoanReAgeTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.reamortization.LoanReAmortizeTransactionBusinessEvent;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;

/**
 * Resolves the external event type that announces a loan transaction of a given type, so an event derived from that
 * transaction (its adjustment, for instance) can follow the same enable/disable switch in
 * {@code m_external_event_configuration}.
 * <p>
 * Only post events are listed because pre events are never posted externally. Transaction types without an event of
 * their own resolve to empty.
 */
public final class LoanTransactionTypeExternalEvents {

    private static final Map<LoanTransactionType, String> EVENT_TYPES = new EnumMap<>(LoanTransactionType.class);

    static {
        register(LoanTransactionType.DISBURSEMENT, LoanDisbursalTransactionBusinessEvent.class);
        register(LoanTransactionType.REPAYMENT, LoanTransactionMakeRepaymentPostBusinessEvent.class);
        register(LoanTransactionType.WAIVE_INTEREST, LoanWaiveInterestBusinessEvent.class);
        register(LoanTransactionType.WRITEOFF, LoanWrittenOffPostBusinessEvent.class);
        register(LoanTransactionType.RECOVERY_REPAYMENT, LoanTransactionRecoveryPaymentPostBusinessEvent.class);
        register(LoanTransactionType.WAIVE_CHARGES, LoanWaiveChargeBusinessEvent.class);
        register(LoanTransactionType.ACCRUAL, LoanAccrualTransactionCreatedBusinessEvent.class);
        register(LoanTransactionType.REFUND, LoanRefundPostBusinessEvent.class);
        register(LoanTransactionType.CHARGE_PAYMENT, LoanChargePaymentPostBusinessEvent.class);
        register(LoanTransactionType.REFUND_FOR_ACTIVE_LOAN, LoanRefundPostBusinessEvent.class);
        register(LoanTransactionType.CREDIT_BALANCE_REFUND, LoanCreditBalanceRefundPostBusinessEvent.class);
        register(LoanTransactionType.MERCHANT_ISSUED_REFUND, LoanTransactionMerchantIssuedRefundPostBusinessEvent.class);
        register(LoanTransactionType.PAYOUT_REFUND, LoanTransactionPayoutRefundPostBusinessEvent.class);
        register(LoanTransactionType.GOODWILL_CREDIT, LoanTransactionGoodwillCreditPostBusinessEvent.class);
        register(LoanTransactionType.CHARGE_REFUND, LoanChargeRefundBusinessEvent.class);
        register(LoanTransactionType.CHARGEBACK, LoanChargebackTransactionBusinessEvent.class);
        register(LoanTransactionType.CHARGE_ADJUSTMENT, LoanChargeAdjustmentPostBusinessEvent.class);
        register(LoanTransactionType.CHARGE_OFF, LoanChargeOffPostBusinessEvent.class);
        register(LoanTransactionType.DOWN_PAYMENT, LoanTransactionDownPaymentPostBusinessEvent.class);
        register(LoanTransactionType.REAGE, LoanReAgeTransactionBusinessEvent.class);
        register(LoanTransactionType.REAMORTIZE, LoanReAmortizeTransactionBusinessEvent.class);
        register(LoanTransactionType.INTEREST_PAYMENT_WAIVER, LoanTransactionInterestPaymentWaiverPostBusinessEvent.class);
        register(LoanTransactionType.ACCRUAL_ACTIVITY, LoanTransactionAccrualActivityPostBusinessEvent.class);
        register(LoanTransactionType.INTEREST_REFUND, LoanTransactionInterestRefundPostBusinessEvent.class);
        register(LoanTransactionType.ACCRUAL_ADJUSTMENT, LoanAccrualAdjustmentTransactionBusinessEvent.class);
        register(LoanTransactionType.CAPITALIZED_INCOME, LoanCapitalizedIncomeTransactionCreatedBusinessEvent.class);
        register(LoanTransactionType.CAPITALIZED_INCOME_AMORTIZATION,
                LoanCapitalizedIncomeAmortizationTransactionCreatedBusinessEvent.class);
        register(LoanTransactionType.CAPITALIZED_INCOME_ADJUSTMENT, LoanCapitalizedIncomeAdjustmentTransactionCreatedBusinessEvent.class);
        register(LoanTransactionType.CONTRACT_TERMINATION, LoanTransactionContractTerminationPostBusinessEvent.class);
        register(LoanTransactionType.CAPITALIZED_INCOME_AMORTIZATION_ADJUSTMENT,
                LoanCapitalizedIncomeAmortizationAdjustmentTransactionCreatedBusinessEvent.class);
        register(LoanTransactionType.BUY_DOWN_FEE, LoanBuyDownFeeTransactionCreatedBusinessEvent.class);
        register(LoanTransactionType.BUY_DOWN_FEE_ADJUSTMENT, LoanBuyDownFeeAdjustmentTransactionCreatedBusinessEvent.class);
        register(LoanTransactionType.BUY_DOWN_FEE_AMORTIZATION, LoanBuyDownFeeAmortizationTransactionCreatedBusinessEvent.class);
        register(LoanTransactionType.BUY_DOWN_FEE_AMORTIZATION_ADJUSTMENT,
                LoanBuyDownFeeAmortizationAdjustmentTransactionCreatedBusinessEvent.class);
    }

    private LoanTransactionTypeExternalEvents() {}

    /**
     * The event type is the class simple name: that is what the configuration table is keyed by and what the startup
     * validation checks against.
     */
    private static void register(final LoanTransactionType transactionType, final Class<?> eventClass) {
        EVENT_TYPES.put(transactionType, eventClass.getSimpleName());
    }

    public static Optional<String> externalEventTypeFor(final LoanTransactionType transactionType) {
        return Optional.ofNullable(transactionType).map(EVENT_TYPES::get);
    }
}
