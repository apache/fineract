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
package org.apache.fineract.accounting.reconciliation.domain.repository;

import java.util.List;
import org.apache.fineract.accounting.reconciliation.domain.ReconciliationMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReconciliationMatchRepository
        extends JpaRepository<ReconciliationMatch, Long>, JpaSpecificationExecutor<ReconciliationMatch> {

    @Query("SELECT m FROM ReconciliationMatch m WHERE m.statementImport.id = :importId ORDER BY m.createdDate DESC")
    List<ReconciliationMatch> findByImportId(@Param("importId") Long importId);

    @Query("SELECT m FROM ReconciliationMatch m WHERE m.bankTransaction.id = :bankTransactionId")
    List<ReconciliationMatch> findByBankTransactionId(@Param("bankTransactionId") Long bankTransactionId);

    @Query("SELECT m FROM ReconciliationMatch m WHERE m.glJournalEntryId = :journalEntryId")
    List<ReconciliationMatch> findByGlJournalEntryId(@Param("journalEntryId") Long journalEntryId);

    @Query("SELECT m FROM ReconciliationMatch m WHERE m.matchType = :matchType AND m.statementImport.id = :importId")
    List<ReconciliationMatch> findByImportIdAndMatchType(@Param("importId") Long importId, @Param("matchType") String matchType);

    @Query("DELETE FROM ReconciliationMatch m WHERE m.statementImport.id = :importId")
    void deleteByImportId(@Param("importId") Long importId);
}
