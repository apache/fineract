package org.mifosng.platform.organisation.service;

import java.util.Collection;

import org.mifosng.data.OfficeData;

public interface OfficeReadPlatformService {

	Collection<OfficeData> retrieveAllOffices();

	OfficeData retrieveOffice(Long officeId);
}