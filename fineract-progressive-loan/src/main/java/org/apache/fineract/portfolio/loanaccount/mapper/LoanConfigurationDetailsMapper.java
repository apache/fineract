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
package org.apache.fineract.portfolio.loanaccount.mapper;

import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.common.domain.DaysInMonthType;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanproduct.data.LoanConfigurationDetails;
import org.apache.fineract.portfolio.loanproduct.domain.ILoanConfigurationDetails;
import org.apache.fineract.portfolio.loanproduct.domain.LoanPreCloseInterestCalculationStrategy;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.apache.fineract.portfolio.loanproduct.domain.RecalculationFrequencyType;

public final class LoanConfigurationDetailsMapper {

    private LoanConfigurationDetailsMapper() {}

    public static ILoanConfigurationDetails map(Loan loan) {
        if (loan == null) {
            return null;
        }

        LoanProductRelatedDetail loanProductRelatedDetail = loan.getLoanProductRelatedDetail();
        if (loanProductRelatedDetail == null) {
            return null;
        }

        MonetaryCurrency currency = loan.getCurrency();
        CurrencyData currencyData = currency.toData();

        return new LoanConfigurationDetails(currencyData, loanProductRelatedDetail.getNominalInterestRatePerPeriod(),
                loanProductRelatedDetail.getAnnualNominalInterestRate(), loanProductRelatedDetail.getGraceOnInterestCharged(),
                loanProductRelatedDetail.getGraceOnPrincipalPayment(), loanProductRelatedDetail.getGraceOnPrincipalPayment(),
                loanProductRelatedDetail.getRecurringMoratoriumOnPrincipalPeriods(), loanProductRelatedDetail.getInterestMethod(),
                loanProductRelatedDetail.getInterestCalculationPeriodMethod(),
                DaysInYearType.fromInt(loanProductRelatedDetail.getDaysInYearType()),
                DaysInMonthType.fromInt(loanProductRelatedDetail.getDaysInMonthType()), loanProductRelatedDetail.getAmortizationMethod(),
                loanProductRelatedDetail.getRepaymentPeriodFrequencyType(), loanProductRelatedDetail.getRepayEvery(),
                loanProductRelatedDetail.getNumberOfRepayments(), loanProductRelatedDetail.isInterestRecognitionOnDisbursementDate(),
                loanProductRelatedDetail.getDaysInYearCustomStrategy(), loanProductRelatedDetail.isAllowPartialPeriodInterestCalculation(),
                loan.isInterestRecalculationEnabled(), getRestFrequencyType(loan), getPreCloseInterestCalculationStrategy(loan));
    }

    private static RecalculationFrequencyType getRestFrequencyType(Loan loan) {
        if (loan.getLoanInterestRecalculationDetails() != null) {
            return loan.getLoanInterestRecalculationDetails().getRestFrequencyType();
        } else {
            return RecalculationFrequencyType.INVALID;
        }
    }

    private static LoanPreCloseInterestCalculationStrategy getPreCloseInterestCalculationStrategy(Loan loan) {
        if (loan.getLoanInterestRecalculationDetails() != null) {
            return loan.getLoanInterestRecalculationDetails().getPreCloseInterestCalculationStrategy();
        } else {
            return LoanPreCloseInterestCalculationStrategy.NONE;
        }
    }
}
