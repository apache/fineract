/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.provisioning.domain;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_provisioning_history")
public class ProvisioningEntry extends AbstractPersistable<Long> {

    @Column(name = "journal_entry_created")
    private Boolean isJournalEntryCreated;
    
    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "entry", orphanRemoval = true)
    Collection<LoanProductProvisioningEntry> provisioningEntries = new HashSet<>();
    
    @OneToOne
    @JoinColumn(name = "createdby_id")
    private AppUser createdBy;

    @Column(name = "created_date")
    @Temporal(TemporalType.DATE)
    private Date createdDate;

    @OneToOne
    @JoinColumn(name = "lastmodifiedby_id")
    private AppUser lastModifiedBy;

    @Column(name = "lastmodified_date")
    @Temporal(TemporalType.DATE)
    private Date lastModifiedDate;

    protected ProvisioningEntry() {
        
    }
    
    public ProvisioningEntry(AppUser createdBy, Date createdDate, AppUser lastModifiedBy, Date lastModifiedDate, Collection<LoanProductProvisioningEntry> provisioningEntries ) {
        this.provisioningEntries = provisioningEntries ;
        this.createdBy = createdBy ;
        this.createdDate = createdDate ;
        this.lastModifiedBy = lastModifiedBy ;
        this.lastModifiedDate = lastModifiedDate ;
    }
    
    public void setProvisioningEntries(Collection<LoanProductProvisioningEntry> provisioningEntries) {
        if(this.provisioningEntries == null) this.provisioningEntries = new HashSet<>(); 
        this.provisioningEntries.addAll(provisioningEntries) ;
    }
    
    public Collection<LoanProductProvisioningEntry> getLoanProductProvisioningEntries() {
        return this.provisioningEntries ;
    }
    
    public void setJournalEntryCreated(Boolean bool) {
        this.isJournalEntryCreated = bool ;
    }
    
    public Date getCreatedDate() {
        return this.createdDate ;
    }
    
}
