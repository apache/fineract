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
package org.apache.fineract.portfolio.workingcapitalloan.calc;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.springframework.lang.NonNull;

/**
 * Calculator service for Working Capital loan projected amortization schedule. Analogous to {@code EMICalculator} for
 * progressive loans, but with a minimal API: generation, disbursement and payment.
 */
public interface ProjectedAmortizationScheduleCalculator {

    /**
     * Creates an initial projected amortization schedule model (at loan creation).
     *
     * @return model with no payments applied
     */
    @NonNull
    ProjectedAmortizationScheduleModel generateModel(@NonNull BigDecimal originationFeeAmount, @NonNull BigDecimal netDisbursementAmount,
            @NonNull BigDecimal totalPaymentValue, @NonNull BigDecimal periodPaymentRate, int npvDayCount,
            @NonNull LocalDate expectedDisbursementDate, @NonNull MathContext mc, @NonNull MonetaryCurrency currency);

    /**
     * Recalculates the model with updated financial parameters (at approval or disbursement). Preserves already applied
     * payments on the model.
     *
     * @param model
     *            current model (may have payments)
     * @param newDiscountAmount
     *            approved or disbursed discount
     * @param newNetAmount
     *            approved or actual net amount
     * @param newStartDate
     *            actual start date
     * @return new model with recalculated schedule
     */
    @NonNull
    ProjectedAmortizationScheduleModel addDisbursement(@NonNull ProjectedAmortizationScheduleModel model,
            @NonNull BigDecimal newDiscountAmount, @NonNull BigDecimal newNetAmount, @NonNull LocalDate newStartDate);

    /**
     * Applies a payment to the model. The model is mutated in place; callers can read the updated payment directly from
     * the model.
     *
     * @param model
     *            current model (mutated in place)
     * @param paymentDate
     *            the date when payment was made
     * @param paymentAmount
     *            actual payment amount
     */
    void applyPayment(@NonNull ProjectedAmortizationScheduleModel model, @NonNull LocalDate paymentDate, @NonNull BigDecimal paymentAmount);
}
