/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.accounting.provisioning.domain;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_provisioning_history")
public class ProvisioningEntry extends AbstractPersistableCustom<Long> {

    @Column(name = "journal_entry_created")
    private Boolean isJournalEntryCreated;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "entry", orphanRemoval = true, fetch=FetchType.EAGER)
    private Set<LoanProductProvisioningEntry> provisioningEntries = new HashSet<>();
    
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
    
    public ProvisioningEntry(AppUser createdBy, Date createdDate, AppUser lastModifiedBy, Date lastModifiedDate, Set<LoanProductProvisioningEntry> provisioningEntries ) {
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
