package org.mifosng.platform.loanproduct.service;

import java.util.List;

import org.mifosng.data.EnumOptionData;

public interface LoanDropdownReadPlatformService {

	List<EnumOptionData> retrieveLoanAmortizationTypeOptions();

	List<EnumOptionData> retrieveLoanInterestTypeOptions();
	
	List<EnumOptionData> retrieveLoanInterestRateCalculatedInPeriodOptions();

	List<EnumOptionData> retrieveRepaymentFrequencyTypeOptions();

	List<EnumOptionData> retrieveInterestRateFrequencyTypeOptions();
}