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
package org.apache.fineract.accounting.journalentry.domain;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long>, JpaSpecificationExecutor<JournalEntry>,
        JournalEntryRepositoryCustom {

    @Query("select journalEntry from JournalEntry journalEntry where journalEntry.transactionId= :transactionId and journalEntry.reversed=false and journalEntry.manualEntry=true")
    List<JournalEntry> findUnReversedManualJournalEntriesByTransactionId(@Param("transactionId") String transactionId);

    @Query("select DISTINCT j.transactionId from JournalEntry j where j.transactionId not in (select DISTINCT je.transactionId from JournalEntry je where je.glAccount.id = :contraId)")
    List<String> findNonContraTansactionIds(@Param("contraId") Long contraId);

    @Query("select DISTINCT j.transactionId from JournalEntry j where j.office.id = :officeId and j.glAccount.id = :contraId and j.reversed=false and j.transactionId not in (select DISTINCT je.reversalJournalEntry.transactionId from JournalEntry je where je.reversed=true)")
    List<String> findNonReversedContraTansactionIds(@Param("contraId") Long contraId, @Param("officeId") Long officeId);
    
    @Query("select journalEntry from JournalEntry journalEntry where journalEntry.entityId= :entityId and journalEntry.entityType = :entityType")    
    List<JournalEntry> findProvisioningJournalEntriesByEntityId(@Param("entityId") Long entityId, @Param("entityType") Integer entityType) ;

    @Query("select journalEntry from JournalEntry journalEntry where journalEntry.transactionId= :transactionId and journalEntry.reversed=false and journalEntry.entityType = :entityType")
    List<JournalEntry> findJournalEntries(@Param("transactionId") String transactionId, @Param("entityType") Integer entityType);

    /*@Query("select journalEntry from JournalEntry journalEntry where glAccount.id= :accountId and transactionId= :transactionId and transactionDate= :transactionDate"
            + " and type= :journalEntryType and entityType=1 and entityId= :loanId and loanTransaction.id= :loanTransactionId")
    JournalEntry findLOANJournalEntryWith(@Param("accountId") Long accountId, @Param("transactionId") String transactionId,
            @Param("transactionDate") Date transactionDate, @Param("journalEntryType") Integer journalEntryType,
            @Param("loanId") Long loanId, @Param("loanTransactionId") Long loanTransactionId);*/
}
