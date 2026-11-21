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
package org.apache.fineract.portfolio.loanaccount.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanaccount.data.OutstandingAmountsDTO;
import org.apache.fineract.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanInstallmentCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.springframework.stereotype.Component;

/**
 * The amount a foreclosure collects. The foreclosure template, the foreclosure posting and its accruals use it, so they
 * always agree.
 */
@Component
@RequiredArgsConstructor
public class LoanForeclosureDetailService {

    private final LoanBalanceService loanBalanceService;
    private final LoanTransactionProcessingService loanTransactionProcessingService;
    private final LoanUtilService loanUtilService;

    public LoanRepaymentScheduleInstallment fetchForeclosureDetail(final Loan loan, final LocalDate foreclosureDate) {
        if (!loan.isProgressiveSchedule()) {
            return loanBalanceService.fetchLoanForeclosureDetail(loan, foreclosureDate);
        }
        // A progressive foreclosure accelerates the maturity to the foreclosure date like an early termination, so it
        // collects the same amount, except for the charges due after the date: the foreclosure deactivates them.
        final ScheduleGeneratorDTO scheduleGeneratorDTO = loanUtilService.buildScheduleGeneratorDTO(loan, null);
        final OutstandingAmountsDTO outstanding = loanTransactionProcessingService.fetchEarlyTerminationDetail(scheduleGeneratorDTO,
                foreclosureDate, loan);
        final MonetaryCurrency currency = loan.getCurrency();
        Money feeCharges = outstanding.feeCharges().plus(percentageInstallmentFeeChange(loan, foreclosureDate, false));
        Money penaltyCharges = outstanding.penaltyCharges().plus(percentageInstallmentFeeChange(loan, foreclosureDate, true));
        for (final LoanCharge loanCharge : loan.getActiveCharges()) {
            if (loanCharge.getDueLocalDate() != null && DateUtils.isAfter(loanCharge.getDueLocalDate(), foreclosureDate)) {
                if (loanCharge.isPenaltyCharge()) {
                    penaltyCharges = penaltyCharges.minus(loanCharge.getAmountOutstanding(currency));
                } else {
                    feeCharges = feeCharges.minus(loanCharge.getAmountOutstanding(currency));
                }
            }
        }
        final LocalDate businessDate = DateUtils.getBusinessLocalDate();
        return new LoanRepaymentScheduleInstallment(null, 0, businessDate, businessDate, outstanding.principal().getAmount(),
                outstanding.interest().getAmount(), feeCharges.getAmount(), penaltyCharges.getAmount(), false, null);
    }

    /**
     * How much the percentage-based installment fees (or penalties) of the installment the foreclosure lands in change.
     * The processor recalculates them after the acceleration, when the installment holds the principal of the
     * installments after it and its interest is prorated to the foreclosure date.
     */
    private Money percentageInstallmentFeeChange(final Loan loan, final LocalDate foreclosureDate, final boolean penalty) {
        final MonetaryCurrency currency = loan.getCurrency();
        Money change = Money.zero(currency);
        final LoanRepaymentScheduleInstallment currentInstallment = loan.getRelatedRepaymentScheduleInstallment(foreclosureDate);
        if (currentInstallment == null || currentInstallment.isAdditional()
                || !DateUtils.isBefore(foreclosureDate, loan.getMaturityDate())) {
            return change;
        }
        Money principal = currentInstallment.getPrincipal(currency);
        for (final LoanRepaymentScheduleInstallment installment : loan.getRepaymentScheduleInstallments()) {
            if (!installment.equals(currentInstallment) && DateUtils.isBefore(foreclosureDate, installment.getDueDate())) {
                principal = principal.plus(installment.getPrincipal(currency));
            }
        }
        final Money interest = currentInstallment.getInterestOutstanding(currency).isGreaterThanZero()
                ? currentInstallment.getInterestChargedProratedTill(foreclosureDate, currency)
                : currentInstallment.getInterestCharged(currency);
        for (final LoanInstallmentCharge installmentCharge : currentInstallment.getInstallmentCharges()) {
            final LoanCharge loanCharge = installmentCharge.getLoanCharge();
            if (loanCharge.isPenaltyCharge() != penalty || !loanCharge.isActive() || !loanCharge.getChargeCalculation().isPercentageBased()
                    || !DateUtils.isBefore(loanCharge.getSubmittedOnDate(), foreclosureDate)) {
                continue;
            }
            final Money recalculated = LoanChargeService.calculateInstallmentChargeAmount(currency, loanCharge.getChargeCalculation(),
                    loanCharge.getPercentage(), principal, interest);
            final Money outstanding = installmentCharge.getAmountOutstanding(currency);
            final Money recalculatedOutstanding = outstanding.plus(recalculated).minus(installmentCharge.getAmount(currency));
            change = change.plus(recalculatedOutstanding.isGreaterThanZero() ? recalculatedOutstanding : Money.zero(currency))
                    .minus(outstanding);
        }
        return change;
    }
}
