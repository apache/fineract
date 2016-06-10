package org.apache.fineract.portfolio.loanaccount.service;

import java.util.Collection;


import org.apache.fineract.portfolio.loanaccount.data.GroupLoanIndividualMonitoringData;

public interface GroupLoanIndividualMonitoringReadPlatformService {
	
	Collection<GroupLoanIndividualMonitoringData> retrieveAll();
	
	GroupLoanIndividualMonitoringData retrieveOne(final Long id);
	
	Collection<GroupLoanIndividualMonitoringData> retrieveAllByLoanId(final Long loanId);
	
	GroupLoanIndividualMonitoringData retrieveByLoanAndClientId(final Long loanId, final Long clientId);
}
