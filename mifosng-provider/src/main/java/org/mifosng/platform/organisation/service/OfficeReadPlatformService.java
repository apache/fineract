package org.mifosng.platform.organisation.service;

import java.util.Collection;
import java.util.List;

import org.mifosng.data.OfficeData;
import org.mifosng.data.OfficeLookup;

public interface OfficeReadPlatformService {

	Collection<OfficeData> retrieveAllOffices();

	Collection<OfficeLookup> retrieveAllOfficesForLookup();

	OfficeData retrieveOffice(Long officeId);

	OfficeData retrieveNewOfficeTemplate();

	List<OfficeLookup> retrieveAllowedParents(Long officeId);
}