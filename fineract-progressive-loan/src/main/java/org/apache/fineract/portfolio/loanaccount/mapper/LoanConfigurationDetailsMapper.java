package org.apache.fineract.portfolio.loanaccount.mapper;

import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.common.domain.DaysInMonthType;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanproduct.data.LoanConfigurationDetails;
import org.apache.fineract.portfolio.loanproduct.domain.ILoanConfigurationDetails;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;

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
                loan.isInterestRecalculationEnabled(), loan.getLoanInterestRecalculationDetails().getRestFrequencyType(),
                loan.getLoanInterestRecalculationDetails().getPreCloseInterestCalculationStrategy());
    }
}
