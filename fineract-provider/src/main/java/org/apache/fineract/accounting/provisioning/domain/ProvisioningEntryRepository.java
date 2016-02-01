/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.provisioning.domain;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProvisioningEntryRepository extends JpaRepository<ProvisioningEntry, Long>, JpaSpecificationExecutor<ProvisioningEntry> {

    @Query("select entry from ProvisioningEntry entry where entry.createdDate = :createdDate")
    ProvisioningEntry findByProvisioningEntryDate(@Param("createdDate") Date createdDate);
    
    @Query("select entry from ProvisioningEntry entry where entry.createdDate = (select max(entry1.createdDate) from ProvisioningEntry entry1 where entry1.isJournalEntryCreated='1')")
    ProvisioningEntry findExistingProvisioningEntryWithJournalEntries() ;
}
