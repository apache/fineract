package org.mifosplatform.portfolio.loanproduct.service;

import java.util.Collection;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.loanproduct.data.TransactionProcessingStrategyData;

public interface LoanDropdownReadPlatformService {

	List<EnumOptionData> retrieveLoanAmortizationTypeOptions();

	List<EnumOptionData> retrieveLoanInterestTypeOptions();
	
	List<EnumOptionData> retrieveLoanInterestRateCalculatedInPeriodOptions();

	List<EnumOptionData> retrieveLoanTermFrequencyTypeOptions();
	
	List<EnumOptionData> retrieveRepaymentFrequencyTypeOptions();

	List<EnumOptionData> retrieveInterestRateFrequencyTypeOptions();

	Collection<TransactionProcessingStrategyData> retreiveTransactionProcessingStrategies();
	
	List<EnumOptionData> retrieveAccountingRuleTypeOptions();
}