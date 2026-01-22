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
import org.apache.fineract.accounting.reconciliation.domain.BankStatementImport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BankStatementImportRepository
        extends JpaRepository<BankStatementImport, Long>, JpaSpecificationExecutor<BankStatementImport> {

    @Query("SELECT b FROM BankStatementImport b WHERE b.glAccount.id = :glAccountId AND b.status IN :statuses ORDER BY b.statementDate DESC")
    List<BankStatementImport> findByGlAccountAndStatuses(@Param("glAccountId") Long glAccountId, @Param("statuses") List<String> statuses);

    @Query("SELECT b FROM BankStatementImport b WHERE b.glAccount.id = :glAccountId AND b.statementDate BETWEEN :fromDate AND :toDate ORDER BY b.statementDate DESC")
    List<BankStatementImport> findByGlAccountAndDateRange(@Param("glAccountId") Long glAccountId, @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    @Query("SELECT b FROM BankStatementImport b WHERE b.status = :status ORDER BY b.statementDate DESC")
    List<BankStatementImport> findByStatus(@Param("status") String status);

    @Query("SELECT b FROM BankStatementImport b WHERE b.office.id = :officeId ORDER BY b.statementDate DESC")
    List<BankStatementImport> findByOffice(@Param("officeId") Long officeId);
}
