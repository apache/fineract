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
package org.apache.fineract.portfolio.workingcapitalloan.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.apache.fineract.cob.data.COBIdAndExternalIdAndAccountNo;
import org.apache.fineract.cob.data.COBIdAndLastClosedBusinessDate;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface WorkingCapitalLoanRepository extends JpaRepository<WorkingCapitalLoan, Long>, JpaSpecificationExecutor<WorkingCapitalLoan>,
        CrudRepository<WorkingCapitalLoan, Long> {

    boolean existsByExternalId(ExternalId externalId);

    boolean existsByAccountNumber(String accountNumber);

    Optional<WorkingCapitalLoan> findByExternalId(ExternalId externalId);

    @Query("""
            SELECT DISTINCT wcl FROM WorkingCapitalLoan wcl
            LEFT JOIN FETCH wcl.client
            LEFT JOIN FETCH wcl.fund
            LEFT JOIN FETCH wcl.loanProduct
            LEFT JOIN FETCH wcl.paymentAllocationRules
            LEFT JOIN FETCH wcl.transactions txn
            LEFT JOIN FETCH txn.allocation
            LEFT JOIN FETCH wcl.disbursementDetails detail
            LEFT JOIN FETCH detail.disbursedBy
            WHERE wcl.id = :id
            """)
    Optional<WorkingCapitalLoan> findByIdWithFullDetails(@Param("id") Long id);

    @Query("""
            SELECT wcl FROM WorkingCapitalLoan wcl
            LEFT JOIN FETCH wcl.client
            LEFT JOIN FETCH wcl.fund
            LEFT JOIN FETCH wcl.loanProduct
            LEFT JOIN FETCH wcl.transactions txn
            LEFT JOIN FETCH txn.allocation
            WHERE wcl.externalId = :externalId
            """)
    Optional<WorkingCapitalLoan> findByExternalIdWithDetails(@Param("externalId") ExternalId externalId);

    @Query("""
            SELECT wcl FROM WorkingCapitalLoan wcl
            LEFT JOIN FETCH wcl.client
            LEFT JOIN FETCH wcl.client.office
            LEFT JOIN FETCH wcl.fund
            LEFT JOIN FETCH wcl.loanProduct
            LEFT JOIN FETCH wcl.paymentAllocationRules
            LEFT JOIN FETCH wcl.transactions txn
            LEFT JOIN FETCH txn.allocation
            WHERE wcl.id IN :ids
            """)
    List<WorkingCapitalLoan> findByIdInWithFullDetails(@Param("ids") List<Long> ids);

    @Query("select loan.id from WorkingCapitalLoan loan where loan.id BETWEEN :minAccountId and :maxAccountId and loan.loanStatus in :nonClosedLoanStatuses and :cobBusinessDate = loan.lastClosedBusinessDate")
    List<Long> findAllLoansByLastClosedBusinessDateNotNullAndMinAndMaxLoanIdAndStatuses(Long minAccountId, Long maxAccountId,
            LocalDate cobBusinessDate, Collection<LoanStatus> nonClosedLoanStatuses);

    @Query("select loan.id from WorkingCapitalLoan loan where loan.id BETWEEN :minAccountId and :maxAccountId and loan.loanStatus in :nonClosedLoanStatuses and (:cobBusinessDate = loan.lastClosedBusinessDate or loan.lastClosedBusinessDate is NULL)")
    List<Long> findAllLoansByLastClosedBusinessDateAndMinAndMaxLoanIdAndStatuses(Long minAccountId, Long maxAccountId,
            LocalDate cobBusinessDate, Collection<LoanStatus> nonClosedLoanStatuses);

    @Query("select loan.id, loan.lastClosedBusinessDate from  WorkingCapitalLoan loan where loan.id IN :loanIds and loan.loanStatus in :loanStatuses and (loan.lastClosedBusinessDate < :cobBusinessDate or loan.lastClosedBusinessDate is null)")
    List<COBIdAndLastClosedBusinessDate> findAllLoansBehindOrNullByLoanIdsAndStatuses(@Param("cobBusinessDate") LocalDate cobBusinessDate,
            @Param("loanIds") List<Long> loanIds, @Param("loanStatuses") Collection<LoanStatus> loanStatuses);

    Long findIdByExternalId(ExternalId externalId);

    @Query("select loan.id, loan.lastClosedBusinessDate from WorkingCapitalLoan loan where loan.id IN :loanIds and loan.loanStatus in :loanStatuses and loan.lastClosedBusinessDate < :cobBusinessDate")
    List<COBIdAndLastClosedBusinessDate> findAllLoansBehindByLoanIdsAndStatuses(@Param("cobBusinessDate") LocalDate cobBusinessDate,
            @Param("loanIds") List<Long> loanIds, @Param("loanStatuses") Collection<LoanStatus> loanStatuses);

    @Query("select loan.id, loan.lastClosedBusinessDate from WorkingCapitalLoan loan LEFT JOIN FETCH loan.disbursementDetails detail where loan.id IN :loanIds and loan.loanStatus in :loanStatuses and loan.lastClosedBusinessDate IS NULL and detail.actualDisbursementDate = :cobBusinessDate")
    List<COBIdAndLastClosedBusinessDate> findAllLoansBehindOnDisbursementDate(@Param("cobBusinessDate") LocalDate cobBusinessDate,
            @Param("loanIds") List<Long> loanIds, @Param("loanStatuses") Collection<LoanStatus> loanStatuses);

    @Query("select loan.id, loan.lastClosedBusinessDate from WorkingCapitalLoan loan where loan.loanStatus in :loanStatuses and loan.lastClosedBusinessDate = (select min(l.lastClosedBusinessDate) from WorkingCapitalLoan l where l"
            + ".loanStatus in :loanStatuses and l.lastClosedBusinessDate < :cobBusinessDate)")
    List<COBIdAndLastClosedBusinessDate> findOldestCOBProcessedLoan(@Param("cobBusinessDate") LocalDate cobBusinessDate,
            @Param("loanStatuses") Collection<LoanStatus> loanStatuses);

    @Query("select loan.id, loan.externalId, loan.accountNumber from WorkingCapitalLoanAccountLock lock left join WorkingCapitalLoan loan on lock.loanId = loan.id where lock.lockPlacedOnCobBusinessDate = :cobBusinessDate")
    List<COBIdAndExternalIdAndAccountNo> findAllStayedLockedByCobBusinessDate(@Param("cobBusinessDate") LocalDate cobBusinessDate);

    boolean existsByLoanProduct_Id(Long productId);

    List<WorkingCapitalLoan> findByClient_Id(Long clientId);

    @Query("""
            SELECT max(loan.loanProductCounter)
            FROM WorkingCapitalLoan loan
            WHERE loan.client.id = :clientId AND loan.loanProduct.id = :productId
            """)
    Integer findMaxLoanProductCounterByClientAndProduct(@Param("clientId") Long clientId, @Param("productId") Long productId);
}
