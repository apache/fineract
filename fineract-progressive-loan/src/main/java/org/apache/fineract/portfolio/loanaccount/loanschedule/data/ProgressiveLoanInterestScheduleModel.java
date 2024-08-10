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
package org.apache.fineract.portfolio.loanaccount.loanschedule.data;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;

public record ProgressiveLoanInterestScheduleModel(List<ProgressiveLoanInterestRepaymentModel> repayments, //
        List<ProgressiveLoanInterestRate> interestRates, //
        LoanProductRelatedDetail loanProductRelatedDetail, //
        Integer installmentAmountInMultiplesOf, //
        MathContext mc) {

    public ProgressiveLoanInterestScheduleModel(List<ProgressiveLoanInterestRepaymentModel> repayments,
            LoanProductRelatedDetail loanProductRelatedDetail, Integer installmentAmountInMultiplesOf, MathContext mc) {
        this(repayments, new ArrayList<>(1), loanProductRelatedDetail, installmentAmountInMultiplesOf, mc);
    }

    public void addInterestRate(final LocalDate newInterestDueDate, final BigDecimal newInterestRate) {
        interestRates.add(new ProgressiveLoanInterestRate(newInterestDueDate, newInterestDueDate.plusDays(1), newInterestRate));
        interestRates.sort(Collections.reverseOrder());
    }

    public BigDecimal getInterestRate(final LocalDate effectiveDate) {
        return interestRates.isEmpty() ? loanProductRelatedDetail.getNominalInterestRatePerPeriod() : findInterestRate(effectiveDate);
    }

    private BigDecimal findInterestRate(final LocalDate effectiveDate) {
        return interestRates.stream().filter(ir -> !ir.effectiveFrom().isAfter(effectiveDate))
                .map(ProgressiveLoanInterestRate::interestRate).findFirst()
                .orElse(loanProductRelatedDetail.getNominalInterestRatePerPeriod());
    }

    public int getLoanTermInDays() {
        if (repayments.isEmpty()) {
            return 0;
        }
        final var firstPeriod = repayments.get(0);
        final var lastPeriod = repayments.size() > 1 ? repayments.get(repayments.size() - 1) : firstPeriod;
        return Math.toIntExact(ChronoUnit.DAYS.between(firstPeriod.getFromDate(), lastPeriod.getDueDate()));
    }
}
