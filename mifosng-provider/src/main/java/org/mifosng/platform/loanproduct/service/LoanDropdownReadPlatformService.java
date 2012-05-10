package org.mifosng.platform.loanproduct.service;

import java.util.List;

import org.mifosng.data.EnumOptionReadModel;

public interface LoanDropdownReadPlatformService {

	List<EnumOptionReadModel> retrieveLoanAmortizationMethodOptions();

	List<EnumOptionReadModel> retrieveLoanInterestMethodOptions();
	
	List<EnumOptionReadModel> retrieveLoanInterestRateCalculatedInPeriodOptions();

	List<EnumOptionReadModel> retrieveRepaymentFrequencyOptions();

	List<EnumOptionReadModel> retrieveInterestFrequencyOptions();
}