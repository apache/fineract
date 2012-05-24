package org.mifosng.platform.organisation.service;

import java.util.Collection;

import org.mifosng.data.OfficeData;
import org.mifosng.data.OfficeLookup;
import org.mifosng.data.OfficeTemplateData;

public interface OfficeReadPlatformService {

	Collection<OfficeData> retrieveAllOffices();

	Collection<OfficeLookup> retrieveAllOfficesForLookup();

	OfficeData retrieveOffice(Long officeId);

	OfficeTemplateData retrieveNewOfficeTemplate();
	
	OfficeTemplateData retrieveExistingOfficeTemplate(Long officeId);
}