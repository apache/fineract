/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GLJournalEntryRepository extends JpaRepository<GLJournalEntry, Long>, JpaSpecificationExecutor<GLJournalEntry>,
        GLJournalEntryRepositoryCustom {

    @Query("from GLJournalEntry journalEntry where journalEntry.transactionId= :transactionId and journalEntry.reversed is false and journalEntry.portfolioGenerated is false")
    List<GLJournalEntry> findUnReversedManualJournalEntriesByTransactionId(@Param("transactionId") String transactionId);
}
