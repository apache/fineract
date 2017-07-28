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

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProvisioningEntryRepository extends JpaRepository<ProvisioningEntry, Long>, JpaSpecificationExecutor<ProvisioningEntry> {

	//OPENJPA throws error if we use entry 
    @Query("select entry1 from ProvisioningEntry entry1 where entry1.createdDate = :createdDate")
    ProvisioningEntry findByProvisioningEntryDate(@Param("createdDate") Date createdDate);
    
    @Query("select entry1 from ProvisioningEntry entry1 where entry1.createdDate = (select max(entry2.createdDate) from ProvisioningEntry entry2 where entry2.isJournalEntryCreated=true)")
    ProvisioningEntry findExistingProvisioningEntryWithJournalEntries() ;
}
