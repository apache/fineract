/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.provisioning.data;

import java.util.Collection;
import java.util.Date;

@SuppressWarnings("unused")
public class ProvisioningEntryData {

    private Long id ;
    
    private Boolean journalEntry ;
    
    private Long createdById ;
    
    private String createdUser ;

    Date createdDate ;
    
    Long modifiedById ;
    
    private String modifiedUser ;
    
    private Collection<LoanProductProvisioningEntryData> provisioningEntries ;
    
    public ProvisioningEntryData(final Long id, final Collection<LoanProductProvisioningEntryData> provisioningEntries) {
        this.provisioningEntries = provisioningEntries ;
        this.id = id ;
    }

    public ProvisioningEntryData(Long id, Boolean journalEntry, Long createdById,
            String createdUser, Date createdDate, Long modifiedById, String modifiedUser) {
        this.id = id ;
        this.journalEntry = journalEntry ;
        this.createdById = createdById ;
        this.createdUser = createdUser ;
        this.modifiedById = modifiedById ;
        this.modifiedUser = modifiedUser ;
        this.createdDate = createdDate ;
    }
    
    public void setEntries(Collection<LoanProductProvisioningEntryData> provisioningEntries) {
        this.provisioningEntries = provisioningEntries ;
    }

    public Long getId() {
        return this.id ;
    }
    
    public Date getCreatedDate() {
        return this.createdDate ;
    }
}
