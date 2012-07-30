package org.mifosng.platform.loanproduct.service;

import java.util.Collection;
import java.util.List;

import org.mifosng.platform.api.data.EnumOptionData;
import org.mifosng.platform.api.data.TransactionProcessingStrategyData;

public interface LoanDropdownReadPlatformService {

	List<EnumOptionData> retrieveLoanAmortizationTypeOptions();

	List<EnumOptionData> retrieveLoanInterestTypeOptions();
	
	List<EnumOptionData> retrieveLoanInterestRateCalculatedInPeriodOptions();

	List<EnumOptionData> retrieveRepaymentFrequencyTypeOptions();

	List<EnumOptionData> retrieveInterestRateFrequencyTypeOptions();

	Collection<TransactionProcessingStrategyData> retreiveTransactionProcessingStrategies();
}