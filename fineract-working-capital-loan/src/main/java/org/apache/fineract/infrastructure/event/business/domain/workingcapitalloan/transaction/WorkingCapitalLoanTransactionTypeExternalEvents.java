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
package org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.transaction;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;

/**
 * Resolves the external event type that announces a Working Capital Loan transaction of a given type, so an event
 * derived from that transaction (its adjustment, for instance) can follow the same enable/disable switch in
 * {@code m_external_event_configuration}. Transaction types without an event of their own resolve to empty.
 */
public final class WorkingCapitalLoanTransactionTypeExternalEvents {

    private static final Map<LoanTransactionType, String> EVENT_TYPES = new EnumMap<>(LoanTransactionType.class);

    static {
        register(LoanTransactionType.DISBURSEMENT, WorkingCapitalLoanDisbursalTransactionBusinessEvent.class);
        register(LoanTransactionType.REPAYMENT, WorkingCapitalLoanRepaymentTransactionBusinessEvent.class);
        register(LoanTransactionType.PAYOUT_REFUND, WorkingCapitalLoanPayoutRefundTransactionBusinessEvent.class);
        register(LoanTransactionType.GOODWILL_CREDIT, WorkingCapitalLoanGoodwillCreditTransactionBusinessEvent.class);
        register(LoanTransactionType.CREDIT_BALANCE_REFUND, WorkingCapitalLoanCreditBalanceRefundTransactionBusinessEvent.class);
        register(LoanTransactionType.RECOVERY_REPAYMENT, WorkingCapitalLoanRecoveryPaymentTransactionBusinessEvent.class);
        register(LoanTransactionType.ACCRUAL, WorkingCapitalLoanAccrualTransactionBusinessEvent.class);
        register(LoanTransactionType.ACCRUAL_ADJUSTMENT, WorkingCapitalLoanAccrualAdjustmentTransactionBusinessEvent.class);
        register(LoanTransactionType.CHARGE_ADJUSTMENT, WorkingCapitalLoanChargeAdjustmentTransactionBusinessEvent.class);
        register(LoanTransactionType.WAIVE_CHARGES, WorkingCapitalLoanChargeWaiverTransactionBusinessEvent.class);
        register(LoanTransactionType.CHARGE_OFF, WorkingCapitalLoanChargeOffTransactionBusinessEvent.class);
        register(LoanTransactionType.WRITEOFF, WorkingCapitalLoanWriteOffTransactionBusinessEvent.class);
        register(LoanTransactionType.DISCOUNT_FEE, WorkingCapitalLoanDiscountFeeTransactionBusinessEvent.class);
        register(LoanTransactionType.DISCOUNT_FEE_AMORTIZATION, WorkingCapitalLoanDiscountFeeAmortizationTransactionBusinessEvent.class);
        register(LoanTransactionType.DISCOUNT_FEE_ADJUSTMENT, WorkingCapitalLoanDiscountFeeAdjustmentTransactionBusinessEvent.class);
        register(LoanTransactionType.DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT,
                WorkingCapitalLoanDiscountFeeAmortizationAdjustmentTransactionBusinessEvent.class);
    }

    private WorkingCapitalLoanTransactionTypeExternalEvents() {}

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
