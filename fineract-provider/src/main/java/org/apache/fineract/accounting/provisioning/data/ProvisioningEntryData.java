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
package org.apache.fineract.accounting.provisioning.data;

import java.math.BigDecimal;
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

    private BigDecimal reservedAmount ;
    
    private Collection<LoanProductProvisioningEntryData> provisioningEntries ;
    
    public ProvisioningEntryData(final Long id, final Collection<LoanProductProvisioningEntryData> provisioningEntries) {
        this.provisioningEntries = provisioningEntries ;
        this.id = id ;
    }

    public ProvisioningEntryData(Long id, Boolean journalEntry, Long createdById,
            String createdUser, Date createdDate, Long modifiedById, String modifiedUser, BigDecimal totalReservedAmount) {
        this.id = id ;
        this.journalEntry = journalEntry ;
        this.createdById = createdById ;
        this.createdUser = createdUser ;
        this.modifiedById = modifiedById ;
        this.modifiedUser = modifiedUser ;
        this.createdDate = createdDate ;
        this.reservedAmount = totalReservedAmount ;
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
