/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.journalentry.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long>, JpaSpecificationExecutor<JournalEntry>,
        JournalEntryRepositoryCustom {

    @Query("from JournalEntry journalEntry where journalEntry.transactionId= :transactionId and journalEntry.reversed is false and journalEntry.manualEntry is true")
    List<JournalEntry> findUnReversedManualJournalEntriesByTransactionId(@Param("transactionId") String transactionId);

    @Query("select DISTINCT j.transactionId from JournalEntry j where j.transactionId not in (select DISTINCT je.transactionId from JournalEntry je where je.glAccount.id = :contraId)")
    List<String> findNonContraTansactionIds(@Param("contraId") Long contraId);

    @Query("select DISTINCT j.transactionId from JournalEntry j where j.office.id = :officeId and j.glAccount.id = :contraId and j.reversed is false and j.transactionId not in (select DISTINCT je.reversalJournalEntry.transactionId from JournalEntry je where je.reversed is true)")
    List<String> findNonReversedContraTansactionIds(@Param("contraId") Long contraId, @Param("officeId") Long officeId);
}
