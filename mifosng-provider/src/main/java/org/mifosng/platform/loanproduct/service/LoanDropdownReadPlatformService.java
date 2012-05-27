package org.mifosng.platform.loanproduct.service;

import java.util.List;

import org.mifosng.data.EnumOptionData;

public interface LoanDropdownReadPlatformService {

	List<EnumOptionData> retrieveLoanAmortizationMethodOptions();

	List<EnumOptionData> retrieveLoanInterestMethodOptions();
	
	List<EnumOptionData> retrieveLoanInterestRateCalculatedInPeriodOptions();

	List<EnumOptionData> retrieveRepaymentFrequencyOptions();

	List<EnumOptionData> retrieveInterestFrequencyOptions();
}