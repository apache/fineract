package org.mifosng.platform.organisation.service;

import java.util.Collection;
import java.util.List;

import org.mifosng.platform.api.data.OfficeData;
import org.mifosng.platform.api.data.OfficeLookup;
import org.mifosng.platform.api.data.OfficeTransactionData;

public interface OfficeReadPlatformService {

	Collection<OfficeData> retrieveAllOffices();

	Collection<OfficeLookup> retrieveAllOfficesForLookup();

	OfficeData retrieveOffice(Long officeId);

	OfficeData retrieveNewOfficeTemplate();

	List<OfficeLookup> retrieveAllowedParents(Long officeId);

	Collection<OfficeTransactionData> retrieveAllOfficeTransactions();

	OfficeTransactionData retrieveNewOfficeTransactionDetails();
}