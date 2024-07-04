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
package org.apache.fineract.portfolio.loanaccount.loanschedule.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanScheduleParams;
import org.apache.fineract.portfolio.loanproduct.calc.EMICalculationResult;
import org.apache.fineract.portfolio.loanproduct.calc.EMICalculator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProgressiveLoanScheduleGenerator extends AbstractProgressiveLoanScheduleGenerator {

    private final ScheduledDateGenerator scheduledDateGenerator;
    private final PaymentPeriodsInOneYearCalculator paymentPeriodsInOneYearCalculator;
    private final EMICalculator emiCalculator;

    @Override
    public ScheduledDateGenerator getScheduledDateGenerator() {
        return scheduledDateGenerator;
    }

    @Override
    public PaymentPeriodsInOneYearCalculator getPaymentPeriodsInOneYearCalculator() {
        return paymentPeriodsInOneYearCalculator;
    }

    @Override
    protected EMICalculator getEMICalculator() {
        return emiCalculator;
    }

    @Override
    public PrincipalInterest calculatePrincipalInterestComponentsForPeriod(final LoanApplicationTerms loanApplicationTerms,
            final LoanScheduleParams loanScheduleParams, final EMICalculationResult emiCalculationResult, final MathContext mc) {

        final Money equalMonthlyInstallmentValue = loanApplicationTerms.getInstallmentAmountInMultiplesOf() != null
                ? Money.roundToMultiplesOf(emiCalculationResult.getEqualMonthlyInstallmentValue(),
                        loanApplicationTerms.getInstallmentAmountInMultiplesOf())
                : emiCalculationResult.getEqualMonthlyInstallmentValue();
        final BigDecimal rateFactorMinus1 = emiCalculationResult.getNextRepaymentPeriodRateFactorMinus1();
        final Money calculatedInterest = loanScheduleParams.getOutstandingBalanceAsPerRest().multipliedBy(rateFactorMinus1);
        final Money calculatedPrincipal = equalMonthlyInstallmentValue.minus(calculatedInterest);

        return new PrincipalInterest(
                adjustCalculatedPrincipalWithRemainingBalanceInLastPeriod(calculatedPrincipal, loanApplicationTerms, loanScheduleParams),
                calculatedInterest, Money.zero(loanApplicationTerms.getCurrency()));
    }

    private Money adjustCalculatedPrincipalWithRemainingBalanceInLastPeriod(final Money calculatedPrincipal,
            final LoanApplicationTerms loanApplicationTerms, final LoanScheduleParams loanScheduleParams) {
        final boolean isLastRepaymentPeriod = loanScheduleParams.getPeriodNumber() == loanApplicationTerms.getActualNoOfRepaymnets();
        if (isLastRepaymentPeriod) {
            final Money remainingAmount = loanScheduleParams.getOutstandingBalanceAsPerRest().minus(calculatedPrincipal);
            return calculatedPrincipal.plus(remainingAmount);
        }
        return calculatedPrincipal;
    }
}
