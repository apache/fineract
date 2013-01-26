package org.mifosplatform.organisation.office.service;

import java.util.Collection;
import java.util.List;

import org.mifosplatform.organisation.office.data.OfficeData;
import org.mifosplatform.organisation.office.data.OfficeLookup;
import org.mifosplatform.organisation.office.data.OfficeTransactionData;

public interface OfficeReadPlatformService {

    Collection<OfficeData> retrieveAllOffices();

    Collection<OfficeData> retrieveAllOfficesForDropdown();
    
    Collection<OfficeLookup> retrieveAllOfficesForLookup();

    OfficeData retrieveOffice(Long officeId);

    OfficeData retrieveNewOfficeTemplate();

    List<OfficeLookup> retrieveAllowedParents(Long officeId);

    Collection<OfficeTransactionData> retrieveAllOfficeTransactions();

    OfficeTransactionData retrieveNewOfficeTransactionDetails();
}