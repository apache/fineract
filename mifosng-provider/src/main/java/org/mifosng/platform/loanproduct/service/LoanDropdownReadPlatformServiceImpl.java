package org.mifosng.platform.loanproduct.service;

import static org.mifosng.platform.loanproduct.service.LoanEnumerations.amortizationType;
import static org.mifosng.platform.loanproduct.service.LoanEnumerations.interestCalculationPeriodType;
import static org.mifosng.platform.loanproduct.service.LoanEnumerations.interestRateFrequencyType;
import static org.mifosng.platform.loanproduct.service.LoanEnumerations.interestType;
import static org.mifosng.platform.loanproduct.service.LoanEnumerations.loanTermFrequencyType;
import static org.mifosng.platform.loanproduct.service.LoanEnumerations.repaymentFrequencyType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.mifosng.platform.api.data.EnumOptionData;
import org.mifosng.platform.api.data.TransactionProcessingStrategyData;
import org.mifosng.platform.loan.domain.AmortizationMethod;
import org.mifosng.platform.loan.domain.InterestCalculationPeriodMethod;
import org.mifosng.platform.loan.domain.InterestMethod;
import org.mifosng.platform.loan.domain.LoanTransactionProcessingStrategy;
import org.mifosng.platform.loan.domain.LoanTransactionProcessingStrategyRepository;
import org.mifosng.platform.loan.domain.PeriodFrequencyType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoanDropdownReadPlatformServiceImpl implements LoanDropdownReadPlatformService {

	private final LoanTransactionProcessingStrategyRepository loanTransactionProcessingStrategyRepository;

	@Autowired
	public LoanDropdownReadPlatformServiceImpl(final LoanTransactionProcessingStrategyRepository loanTransactionProcessingStrategyRepository) {
		this.loanTransactionProcessingStrategyRepository = loanTransactionProcessingStrategyRepository;
	}
	
	@Override
	public List<EnumOptionData> retrieveLoanAmortizationTypeOptions() {

		List<EnumOptionData> allowedAmortizationMethods = Arrays.asList(
				amortizationType(AmortizationMethod.EQUAL_INSTALLMENTS),
				amortizationType(AmortizationMethod.EQUAL_PRINCIPAL));

		return allowedAmortizationMethods;
	}

	@Override
	public List<EnumOptionData> retrieveLoanInterestTypeOptions() {
		List<EnumOptionData> allowedRepaymentScheduleCalculationMethods = Arrays
				.asList(interestType(InterestMethod.FLAT),
						interestType(InterestMethod.DECLINING_BALANCE));

		return allowedRepaymentScheduleCalculationMethods;
	}

	@Override
	public List<EnumOptionData> retrieveLoanInterestRateCalculatedInPeriodOptions() {

		List<EnumOptionData> allowedOptions = Arrays
				.asList(interestCalculationPeriodType(InterestCalculationPeriodMethod.DAILY),
						interestCalculationPeriodType(InterestCalculationPeriodMethod.SAME_AS_REPAYMENT_PERIOD));

		return allowedOptions;
	}
	
	@Override
	public List<EnumOptionData> retrieveLoanTermFrequencyTypeOptions() {
		List<EnumOptionData> loanTermFrequencyOptions = Arrays.asList(
				loanTermFrequencyType(PeriodFrequencyType.DAYS),
				loanTermFrequencyType(PeriodFrequencyType.WEEKS),
				loanTermFrequencyType(PeriodFrequencyType.MONTHS),
				loanTermFrequencyType(PeriodFrequencyType.YEARS));
		return loanTermFrequencyOptions;
	}

	@Override
	public List<EnumOptionData> retrieveRepaymentFrequencyTypeOptions() {

		List<EnumOptionData> repaymentFrequencyOptions = Arrays.asList(
				repaymentFrequencyType(PeriodFrequencyType.DAYS),
				repaymentFrequencyType(PeriodFrequencyType.WEEKS),
				repaymentFrequencyType(PeriodFrequencyType.MONTHS));
		return repaymentFrequencyOptions;
	}

	@Override
	public List<EnumOptionData> retrieveInterestRateFrequencyTypeOptions() {
		// support for monthly and annual percentage rate (MPR) and (APR)
		List<EnumOptionData> interestRateFrequencyTypeOptions = Arrays.asList(
				interestRateFrequencyType(PeriodFrequencyType.MONTHS),
				interestRateFrequencyType(PeriodFrequencyType.YEARS));
		return interestRateFrequencyTypeOptions;
	}

	@Override
	public Collection<TransactionProcessingStrategyData> retreiveTransactionProcessingStrategies() {
		
		Collection<TransactionProcessingStrategyData> strategyOptions = new ArrayList<TransactionProcessingStrategyData>();
		
		List<LoanTransactionProcessingStrategy> strategies = this.loanTransactionProcessingStrategyRepository.findAll();
		for (LoanTransactionProcessingStrategy strategy : strategies) {
			strategyOptions.add(strategy.toData());
		}
		
		return strategyOptions;
	}
}