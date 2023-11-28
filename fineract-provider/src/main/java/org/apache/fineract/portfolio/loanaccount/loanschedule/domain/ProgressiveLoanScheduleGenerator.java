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
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProgressiveLoanScheduleGenerator extends AbstractProgressiveLoanScheduleGenerator {

    private final ScheduledDateGenerator scheduledDateGenerator;
    private final PaymentPeriodsInOneYearCalculator paymentPeriodsInOneYearCalculator;

    @Override
    public ScheduledDateGenerator getScheduledDateGenerator() {
        return scheduledDateGenerator;
    }

    @Override
    public PaymentPeriodsInOneYearCalculator getPaymentPeriodsInOneYearCalculator() {
        return paymentPeriodsInOneYearCalculator;
    }

    @Override
    public PrincipalInterest calculatePrincipalInterestComponentsForPeriod(LoanApplicationTerms loanApplicationTerms,
            LoanScheduleParams loanScheduleParams, MathContext mc) {
        // TODO: handle interest calculation
        int periodNumber = loanScheduleParams.getPeriodNumber();
        BigDecimal fixedEMIAmount = loanApplicationTerms.getFixedEmiAmount();
        Money calculatedPrincipal;
        if (fixedEMIAmount == null) {
            int noRemainingPeriod = loanApplicationTerms.getActualNoOfRepaymnets() - periodNumber + 1;
            calculatedPrincipal = loanScheduleParams.getOutstandingBalance().dividedBy(noRemainingPeriod, mc.getRoundingMode());
            if (loanApplicationTerms.getInstallmentAmountInMultiplesOf() != null) {
                calculatedPrincipal = Money.roundToMultiplesOf(calculatedPrincipal,
                        loanApplicationTerms.getInstallmentAmountInMultiplesOf());
            }
            loanApplicationTerms.setFixedEmiAmount(calculatedPrincipal.getAmount());
        } else {
            calculatedPrincipal = Money.of(loanApplicationTerms.getCurrency(), fixedEMIAmount);
        }
        // adjust if needed
        if (periodNumber == loanApplicationTerms.getActualNoOfRepaymnets()) {
            Money remainingAmount = loanScheduleParams.getOutstandingBalance().minus(calculatedPrincipal);
            calculatedPrincipal = calculatedPrincipal.plus(remainingAmount);
        }

        return new PrincipalInterest(calculatedPrincipal, Money.zero(loanApplicationTerms.getCurrency()),
                Money.zero(loanApplicationTerms.getCurrency()));
    }
}
