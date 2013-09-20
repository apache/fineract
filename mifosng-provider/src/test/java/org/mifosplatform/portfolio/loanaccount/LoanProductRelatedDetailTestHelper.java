package org.mifosplatform.portfolio.loanaccount;

import java.math.BigDecimal;

import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanScheduleGenerator;
import org.mifosplatform.portfolio.loanproduct.domain.AmortizationMethod;
import org.mifosplatform.portfolio.loanproduct.domain.InterestCalculationPeriodMethod;
import org.mifosplatform.portfolio.loanproduct.domain.InterestMethod;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;

/**
 * This class is used to keep in one place configurations for setting up
 * {@link LoanProductRelatedDetail} object used in {@link LoanScheduleGenerator}
 * 's
 */
public class LoanProductRelatedDetailTestHelper {

    public static LoanProductRelatedDetail createSettingsForEqualPrincipalAmortizationQuarterly() {

        final MonetaryCurrency currency = new MonetaryCurrencyBuilder().withCode("USD").withDigitsAfterDecimal(2).build();
        final BigDecimal defaultPrincipal = BigDecimal.valueOf(Double.valueOf("200000"));

        // 2% per month, 24% per year
        final BigDecimal defaultNominalInterestRatePerPeriod = BigDecimal.valueOf(Double.valueOf("2"));
        final PeriodFrequencyType interestPeriodFrequencyType = PeriodFrequencyType.MONTHS;
        final BigDecimal defaultAnnualNominalInterestRate = BigDecimal.valueOf(Double.valueOf("24"));

        final InterestMethod interestMethod = InterestMethod.DECLINING_BALANCE;
        final InterestCalculationPeriodMethod interestCalculationPeriodMethod = InterestCalculationPeriodMethod.SAME_AS_REPAYMENT_PERIOD;
        final Integer repayEvery = Integer.valueOf(3);
        final PeriodFrequencyType repaymentFrequencyType = PeriodFrequencyType.MONTHS;

        final Integer defaultNumberOfRepayments = Integer.valueOf(4);
        final AmortizationMethod amortizationMethod = AmortizationMethod.EQUAL_PRINCIPAL;

        final BigDecimal inArrearsTolerance = BigDecimal.ZERO;

        return createLoanProductRelatedDetail(currency, defaultPrincipal, defaultNominalInterestRatePerPeriod, interestPeriodFrequencyType,
                defaultAnnualNominalInterestRate, interestMethod, interestCalculationPeriodMethod, repayEvery, repaymentFrequencyType,
                defaultNumberOfRepayments, amortizationMethod, inArrearsTolerance);
    }

    public static LoanProductRelatedDetail createSettingsForEqualInstallmentAmortizationQuarterly() {
        final MonetaryCurrency currency = new MonetaryCurrencyBuilder().withCode("USD").withDigitsAfterDecimal(2).build();
        final BigDecimal defaultPrincipal = BigDecimal.valueOf(Double.valueOf("200000"));

        // 2% per month, 24% per year
        final BigDecimal defaultNominalInterestRatePerPeriod = BigDecimal.valueOf(Double.valueOf("2"));
        final PeriodFrequencyType interestPeriodFrequencyType = PeriodFrequencyType.MONTHS;
        final BigDecimal defaultAnnualNominalInterestRate = BigDecimal.valueOf(Double.valueOf("24"));

        final InterestMethod interestMethod = InterestMethod.DECLINING_BALANCE;
        final InterestCalculationPeriodMethod interestCalculationPeriodMethod = InterestCalculationPeriodMethod.SAME_AS_REPAYMENT_PERIOD;
        final Integer repayEvery = Integer.valueOf(3);
        final PeriodFrequencyType repaymentFrequencyType = PeriodFrequencyType.MONTHS;

        final Integer defaultNumberOfRepayments = Integer.valueOf(4);
        final AmortizationMethod amortizationMethod = AmortizationMethod.EQUAL_INSTALLMENTS;

        final BigDecimal inArrearsTolerance = BigDecimal.ZERO;

        return createLoanProductRelatedDetail(currency, defaultPrincipal, defaultNominalInterestRatePerPeriod, interestPeriodFrequencyType,
                defaultAnnualNominalInterestRate, interestMethod, interestCalculationPeriodMethod, repayEvery, repaymentFrequencyType,
                defaultNumberOfRepayments, amortizationMethod, inArrearsTolerance);
    }

    public static LoanProductRelatedDetail createSettingsForFlatQuarterly(final AmortizationMethod amortizationMethod) {

        final MonetaryCurrency currency = new MonetaryCurrencyBuilder().withCode("USD").withDigitsAfterDecimal(2).build();
        final BigDecimal defaultPrincipal = BigDecimal.valueOf(Double.valueOf("200000"));

        // 2% per month, 24% per year
        final BigDecimal defaultNominalInterestRatePerPeriod = BigDecimal.valueOf(Double.valueOf("2"));
        final PeriodFrequencyType interestPeriodFrequencyType = PeriodFrequencyType.MONTHS;
        final BigDecimal defaultAnnualNominalInterestRate = BigDecimal.valueOf(Double.valueOf("24"));

        final InterestMethod interestMethod = InterestMethod.FLAT;
        final InterestCalculationPeriodMethod interestCalculationPeriodMethod = InterestCalculationPeriodMethod.SAME_AS_REPAYMENT_PERIOD;
        final Integer repayEvery = Integer.valueOf(3);
        final PeriodFrequencyType repaymentFrequencyType = PeriodFrequencyType.MONTHS;

        final Integer defaultNumberOfRepayments = Integer.valueOf(4);

        final BigDecimal inArrearsTolerance = BigDecimal.ZERO;

        return createLoanProductRelatedDetail(currency, defaultPrincipal, defaultNominalInterestRatePerPeriod, interestPeriodFrequencyType,
                defaultAnnualNominalInterestRate, interestMethod, interestCalculationPeriodMethod, repayEvery, repaymentFrequencyType,
                defaultNumberOfRepayments, amortizationMethod, inArrearsTolerance);
    }

    public static LoanProductRelatedDetail createSettingsForIrregularFlatEveryFourMonths() {

        final MonetaryCurrency currency = new MonetaryCurrencyBuilder().withCode("KSH").withDigitsAfterDecimal(0).build();
        final BigDecimal defaultPrincipal = BigDecimal.valueOf(Double.valueOf("15000"));

        // 2% per month, 24% per year
        final BigDecimal defaultNominalInterestRatePerPeriod = BigDecimal.valueOf(Double.valueOf("2"));
        final PeriodFrequencyType interestPeriodFrequencyType = PeriodFrequencyType.MONTHS;
        final BigDecimal defaultAnnualNominalInterestRate = BigDecimal.valueOf(Double.valueOf("24"));

        final InterestMethod interestMethod = InterestMethod.FLAT;
        final InterestCalculationPeriodMethod interestCalculationPeriodMethod = InterestCalculationPeriodMethod.SAME_AS_REPAYMENT_PERIOD;
        final Integer repayEvery = Integer.valueOf(3);
        final PeriodFrequencyType repaymentFrequencyType = PeriodFrequencyType.MONTHS;

        final Integer defaultNumberOfRepayments = Integer.valueOf(2);

        final BigDecimal inArrearsTolerance = BigDecimal.ZERO;

        final AmortizationMethod amortizationMethod = AmortizationMethod.EQUAL_PRINCIPAL;

        return createLoanProductRelatedDetail(currency, defaultPrincipal, defaultNominalInterestRatePerPeriod, interestPeriodFrequencyType,
                defaultAnnualNominalInterestRate, interestMethod, interestCalculationPeriodMethod, repayEvery, repaymentFrequencyType,
                defaultNumberOfRepayments, amortizationMethod, inArrearsTolerance);
    }

    private static LoanProductRelatedDetail createLoanProductRelatedDetail(final MonetaryCurrency currency,
            final BigDecimal defaultPrincipal, final BigDecimal defaultNominalInterestRatePerPeriod,
            final PeriodFrequencyType interestPeriodFrequencyType, final BigDecimal defaultAnnualNominalInterestRate,
            final InterestMethod interestMethod, final InterestCalculationPeriodMethod interestCalculationPeriodMethod,
            final Integer repayEvery, final PeriodFrequencyType repaymentFrequencyType, final Integer defaultNumberOfRepayments,
            final AmortizationMethod amortizationMethod, final BigDecimal inArrearsTolerance) {

        final Integer graceOnPrincipalPayment = Integer.valueOf(0);
        final Integer graceOnInterestPayment = Integer.valueOf(0);
        final Integer graceOnInterestCharged = Integer.valueOf(0);

        return new LoanProductRelatedDetail(currency, defaultPrincipal, defaultNominalInterestRatePerPeriod, interestPeriodFrequencyType,
                defaultAnnualNominalInterestRate, interestMethod, interestCalculationPeriodMethod, repayEvery, repaymentFrequencyType,
                defaultNumberOfRepayments, graceOnPrincipalPayment, graceOnInterestPayment, graceOnInterestCharged, amortizationMethod,
                inArrearsTolerance);
    }
}