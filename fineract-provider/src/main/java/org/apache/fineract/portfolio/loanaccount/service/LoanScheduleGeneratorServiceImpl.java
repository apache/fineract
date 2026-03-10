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

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanRepaymentScheduleModelData;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.ProgressiveLoanScheduleGenerator;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoanScheduleGeneratorServiceImpl implements LoanScheduleGeneratorService {

    private final ProgressiveLoanScheduleGenerator scheduleGenerator;

    @Override
    public BigDecimal calculateInteresOnlyWithFirtDisbursement(final Loan loan) {
        if (!loan.isMultiDisburmentLoan() || loan.getDisbursementDetails().isEmpty() || loan.getDisbursementDetails().size() == 1) {
            return loan.getTotalInterest();
        }

        final MathContext mc = MoneyHelper.getMathContext();
        final LoanProductRelatedDetail loanProductRelatedDetail = loan.getLoanProductRelatedDetail();
        final LocalDate disbursementDate = loan.isOpen() ? loan.getDisbursementDate() : loan.getExpectedDisbursedOnLocalDate();
        final BigDecimal disbursedAmount = loan.getDisbursementDetails().isEmpty() ? loan.getPrincipal().getAmount()
                : loan.getDisbursementDetails().get(0).getPrincipal();
        final LoanRepaymentScheduleModelData modelData = new LoanRepaymentScheduleModelData(disbursementDate, loan.getCurrency().toData(),
                disbursedAmount, disbursementDate, loanProductRelatedDetail.getNumberOfRepayments(), //
                loanProductRelatedDetail.getRepayEvery(), loanProductRelatedDetail.getRepaymentPeriodFrequencyType().toString(), //
                loanProductRelatedDetail.getAnnualNominalInterestRate(), //
                loanProductRelatedDetail.isEnableDownPayment(), loanProductRelatedDetail.fetchDaysInMonthType(), //
                loanProductRelatedDetail.fetchDaysInYearType(), //
                loanProductRelatedDetail.getDisbursedAmountPercentageForDownPayment(), //
                loanProductRelatedDetail.getInstallmentAmountInMultiplesOf(), //
                loanProductRelatedDetail.getFixedLength(), //
                loanProductRelatedDetail.isInterestRecognitionOnDisbursementDate(), //
                loanProductRelatedDetail.getDaysInYearCustomStrategy(), //
                loanProductRelatedDetail.getInterestMethod(), //
                loanProductRelatedDetail.isAllowPartialPeriodInterestCalculation(), false);

        return scheduleGenerator.generate(mc, modelData).getTotalInterestAmount();
    }
}
