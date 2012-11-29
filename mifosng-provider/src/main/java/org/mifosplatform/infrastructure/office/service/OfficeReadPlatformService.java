package org.mifosplatform.infrastructure.office.service;

import java.util.Collection;
import java.util.List;

import org.mifosplatform.infrastructure.office.data.OfficeData;
import org.mifosplatform.infrastructure.office.data.OfficeLookup;
import org.mifosplatform.infrastructure.office.data.OfficeTransactionData;

public interface OfficeReadPlatformService {

    Collection<OfficeData> retrieveAllOffices();

    Collection<OfficeLookup> retrieveAllOfficesForLookup();

    OfficeData retrieveOffice(Long officeId);

    OfficeData retrieveNewOfficeTemplate();

    List<OfficeLookup> retrieveAllowedParents(Long officeId);

    Collection<OfficeTransactionData> retrieveAllOfficeTransactions();

    OfficeTransactionData retrieveNewOfficeTransactionDetails();
}