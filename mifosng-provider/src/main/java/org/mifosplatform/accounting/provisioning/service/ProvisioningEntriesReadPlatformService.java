/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.provisioning.service;

import java.util.Collection;
import java.util.Date;

import org.mifosplatform.accounting.provisioning.data.LoanProductProvisioningEntryData;
import org.mifosplatform.accounting.provisioning.data.ProvisioningEntryData;
import org.mifosplatform.infrastructure.core.service.Page;
import org.mifosplatform.infrastructure.core.service.SearchParameters;


public interface ProvisioningEntriesReadPlatformService {

    public Collection<LoanProductProvisioningEntryData> retrieveLoanProductsProvisioningData(Date date) ;
    
    public ProvisioningEntryData retrieveProvisioningEntryData(Long entryId) ;
    
    public Page<ProvisioningEntryData> retrieveAllProvisioningEntries(Integer offset, Integer limit) ;
    
    public ProvisioningEntryData retrieveProvisioningEntryData(String date) ;
    
    public ProvisioningEntryData retrieveProvisioningEntryDataByCriteriaId(Long criteriaId) ;
    
    public ProvisioningEntryData retrieveExistingProvisioningIdDateWithJournals() ;
    
    public Page<LoanProductProvisioningEntryData> retrieveProvisioningEntries(SearchParameters searchParams) ;
}
