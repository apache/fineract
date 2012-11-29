package org.mifosng.platform.loanschedule.domain;

import java.math.BigDecimal;

import org.mifosng.platform.loan.domain.AmortizationMethod;
import org.mifosng.platform.loan.domain.InterestCalculationPeriodMethod;
import org.mifosng.platform.loan.domain.InterestMethod;
import org.mifosng.platform.loan.domain.LoanProductRelatedDetail;
import org.mifosng.platform.loan.domain.MonetaryCurrencyBuilder;
import org.mifosng.platform.loan.domain.PeriodFrequencyType;
import org.mifosplatform.infrastructure.configuration.domain.MonetaryCurrency;

/**
 * This class is used to keep in one place configurations for setting up {@link LoanProductRelatedDetail} object used in {@link LoanScheduleGenerator}'s
 */
public class LoanProductRelatedDetailTestHelper {
	
	public static LoanProductRelatedDetail createSettingsForEqualPrincipalAmortizationQuarterly() {
		
		MonetaryCurrency currency = new MonetaryCurrencyBuilder().withCode("USD").withDigitsAfterDecimal(2).build();
		BigDecimal defaultPrincipal = BigDecimal.valueOf(Double.valueOf("200000"));
		
		// 2% per month, 24% per year
		BigDecimal defaultNominalInterestRatePerPeriod = BigDecimal.valueOf(Double.valueOf("2"));  
		PeriodFrequencyType interestPeriodFrequencyType = PeriodFrequencyType.MONTHS;
		BigDecimal defaultAnnualNominalInterestRate = BigDecimal.valueOf(Double.valueOf("24"));
		
		InterestMethod interestMethod = InterestMethod.DECLINING_BALANCE;
		InterestCalculationPeriodMethod interestCalculationPeriodMethod = InterestCalculationPeriodMethod.SAME_AS_REPAYMENT_PERIOD;
		Integer repayEvery = Integer.valueOf(3);
		PeriodFrequencyType repaymentFrequencyType = PeriodFrequencyType.MONTHS;
		
		Integer defaultNumberOfRepayments = Integer.valueOf(4);
		AmortizationMethod amortizationMethod = AmortizationMethod.EQUAL_PRINCIPAL;
		
		BigDecimal inArrearsTolerance = BigDecimal.ZERO;
		
		return createLoanProductRelatedDetail(currency, defaultPrincipal,
				defaultNominalInterestRatePerPeriod,
				interestPeriodFrequencyType, defaultAnnualNominalInterestRate,
				interestMethod, interestCalculationPeriodMethod, repayEvery,
				repaymentFrequencyType, defaultNumberOfRepayments,
				amortizationMethod, inArrearsTolerance);
	}

	public static LoanProductRelatedDetail createSettingsForEqualInstallmentAmortizationQuarterly() {
		MonetaryCurrency currency = new MonetaryCurrencyBuilder().withCode("USD").withDigitsAfterDecimal(2).build();
		BigDecimal defaultPrincipal = BigDecimal.valueOf(Double.valueOf("200000"));
		
		// 2% per month, 24% per year
		BigDecimal defaultNominalInterestRatePerPeriod = BigDecimal.valueOf(Double.valueOf("2"));  
		PeriodFrequencyType interestPeriodFrequencyType = PeriodFrequencyType.MONTHS;
		BigDecimal defaultAnnualNominalInterestRate = BigDecimal.valueOf(Double.valueOf("24"));
		
		InterestMethod interestMethod = InterestMethod.DECLINING_BALANCE;
		InterestCalculationPeriodMethod interestCalculationPeriodMethod = InterestCalculationPeriodMethod.SAME_AS_REPAYMENT_PERIOD;
		Integer repayEvery = Integer.valueOf(3);
		PeriodFrequencyType repaymentFrequencyType = PeriodFrequencyType.MONTHS;
		
		Integer defaultNumberOfRepayments = Integer.valueOf(4);
		AmortizationMethod amortizationMethod = AmortizationMethod.EQUAL_INSTALLMENTS;
		
		BigDecimal inArrearsTolerance = BigDecimal.ZERO;
		
		return createLoanProductRelatedDetail(currency, defaultPrincipal,
				defaultNominalInterestRatePerPeriod,
				interestPeriodFrequencyType, defaultAnnualNominalInterestRate,
				interestMethod, interestCalculationPeriodMethod, repayEvery,
				repaymentFrequencyType, defaultNumberOfRepayments,
				amortizationMethod, inArrearsTolerance);
	}
	
	public static LoanProductRelatedDetail createSettingsForFlatQuarterly(final AmortizationMethod amortizationMethod) {
		
		MonetaryCurrency currency = new MonetaryCurrencyBuilder().withCode("USD").withDigitsAfterDecimal(2).build();
		BigDecimal defaultPrincipal = BigDecimal.valueOf(Double.valueOf("200000"));
		
		// 2% per month, 24% per year
		BigDecimal defaultNominalInterestRatePerPeriod = BigDecimal.valueOf(Double.valueOf("2"));  
		PeriodFrequencyType interestPeriodFrequencyType = PeriodFrequencyType.MONTHS;
		BigDecimal defaultAnnualNominalInterestRate = BigDecimal.valueOf(Double.valueOf("24"));
		
		InterestMethod interestMethod = InterestMethod.FLAT;
		InterestCalculationPeriodMethod interestCalculationPeriodMethod = InterestCalculationPeriodMethod.SAME_AS_REPAYMENT_PERIOD;
		Integer repayEvery = Integer.valueOf(3);
		PeriodFrequencyType repaymentFrequencyType = PeriodFrequencyType.MONTHS;
		
		Integer defaultNumberOfRepayments = Integer.valueOf(4);
		
		BigDecimal inArrearsTolerance = BigDecimal.ZERO;
		
		return createLoanProductRelatedDetail(currency, defaultPrincipal,
				defaultNominalInterestRatePerPeriod,
				interestPeriodFrequencyType, defaultAnnualNominalInterestRate,
				interestMethod, interestCalculationPeriodMethod, repayEvery,
				repaymentFrequencyType, defaultNumberOfRepayments,
				amortizationMethod, inArrearsTolerance);
	}
	
	public static LoanProductRelatedDetail createSettingsForIrregularFlatEveryFourMonths() {
		
		MonetaryCurrency currency = new MonetaryCurrencyBuilder().withCode("KSH").withDigitsAfterDecimal(0).build();
		BigDecimal defaultPrincipal = BigDecimal.valueOf(Double.valueOf("15000"));
		
		// 2% per month, 24% per year
		BigDecimal defaultNominalInterestRatePerPeriod = BigDecimal.valueOf(Double.valueOf("2"));  
		PeriodFrequencyType interestPeriodFrequencyType = PeriodFrequencyType.MONTHS;
		BigDecimal defaultAnnualNominalInterestRate = BigDecimal.valueOf(Double.valueOf("24"));
		
		InterestMethod interestMethod = InterestMethod.FLAT;
		InterestCalculationPeriodMethod interestCalculationPeriodMethod = InterestCalculationPeriodMethod.SAME_AS_REPAYMENT_PERIOD;
		Integer repayEvery = Integer.valueOf(3);
		PeriodFrequencyType repaymentFrequencyType = PeriodFrequencyType.MONTHS;
		
		Integer defaultNumberOfRepayments = Integer.valueOf(2);
		
		BigDecimal inArrearsTolerance = BigDecimal.ZERO;
		
		AmortizationMethod amortizationMethod = AmortizationMethod.EQUAL_PRINCIPAL;
		
		return createLoanProductRelatedDetail(currency, defaultPrincipal,
				defaultNominalInterestRatePerPeriod,
				interestPeriodFrequencyType, defaultAnnualNominalInterestRate,
				interestMethod, interestCalculationPeriodMethod, repayEvery,
				repaymentFrequencyType, defaultNumberOfRepayments,
				amortizationMethod, inArrearsTolerance);
	}
	
	
	private static LoanProductRelatedDetail createLoanProductRelatedDetail(
			MonetaryCurrency currency, BigDecimal defaultPrincipal,
			BigDecimal defaultNominalInterestRatePerPeriod,
			PeriodFrequencyType interestPeriodFrequencyType,
			BigDecimal defaultAnnualNominalInterestRate,
			InterestMethod interestMethod,
			InterestCalculationPeriodMethod interestCalculationPeriodMethod,
			Integer repayEvery, PeriodFrequencyType repaymentFrequencyType,
			Integer defaultNumberOfRepayments,
			AmortizationMethod amortizationMethod, BigDecimal inArrearsTolerance) {
		
		return new LoanProductRelatedDetail(currency, defaultPrincipal, defaultNominalInterestRatePerPeriod, 
				interestPeriodFrequencyType, defaultAnnualNominalInterestRate, interestMethod, 
				interestCalculationPeriodMethod, repayEvery, repaymentFrequencyType, defaultNumberOfRepayments, amortizationMethod, inArrearsTolerance);
	}
}