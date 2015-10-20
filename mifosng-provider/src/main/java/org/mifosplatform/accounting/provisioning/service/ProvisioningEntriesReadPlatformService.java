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


public interface ProvisioningEntriesReadPlatformService {

    public Collection<LoanProductProvisioningEntryData> retrieveLoanProductsProvisioningData(Date date) ;
    
    public ProvisioningEntryData retrieveProvisioningEntryData(Long entryId) ;
    
    public Collection<ProvisioningEntryData> retrieveAllProvisioningEntries() ;
    
    public ProvisioningEntryData retrieveProvisioningEntryData(String date) ;
    
    public ProvisioningEntryData retrieveProvisioningEntryDataByCriteriaId(Long criteriaId) ;
    
    public ProvisioningEntryData retrieveExistingProvisioningIdDateWithJournals() ;
}
