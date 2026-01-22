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

import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.accounting.reconciliation.domain.BankStatementTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BankStatementTransactionRepository
        extends JpaRepository<BankStatementTransaction, Long>, JpaSpecificationExecutor<BankStatementTransaction> {

    @Query("SELECT t FROM BankStatementTransaction t WHERE t.statementImport.id = :importId ORDER BY t.transactionDate ASC")
    List<BankStatementTransaction> findByImportId(@Param("importId") Long importId);

    @Query("SELECT t FROM BankStatementTransaction t WHERE t.statementImport.id = :importId AND t.matched = :matched ORDER BY t.transactionDate ASC")
    List<BankStatementTransaction> findByImportIdAndMatchStatus(@Param("importId") Long importId, @Param("matched") boolean matched);

    @Query("SELECT t FROM BankStatementTransaction t WHERE t.statementImport.id = :importId AND t.transactionDate BETWEEN :fromDate AND :toDate ORDER BY t.transactionDate ASC")
    List<BankStatementTransaction> findByImportIdAndDateRange(@Param("importId") Long importId, @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    @Query("SELECT t FROM BankStatementTransaction t WHERE t.referenceNumber = :referenceNumber AND t.matched = false")
    List<BankStatementTransaction> findUnmatchedByReferenceNumber(@Param("referenceNumber") String referenceNumber);
}
