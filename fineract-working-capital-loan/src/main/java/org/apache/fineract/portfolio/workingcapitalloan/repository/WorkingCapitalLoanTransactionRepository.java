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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.workingcapitalloan.data.TransactionDateAndAmountHolder;
import org.apache.fineract.portfolio.workingcapitalloan.data.TransactionTypeTotalHolder;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkingCapitalLoanTransactionRepository extends JpaRepository<WorkingCapitalLoanTransaction, Long> {

    List<WorkingCapitalLoanTransaction> findByWcLoan_IdOrderByTransactionDateAscIdAsc(Long wcLoanId);

    Page<WorkingCapitalLoanTransaction> findByWcLoan_IdOrderByTransactionDateAscIdAsc(Long wcLoanId, Pageable pageable);

    Optional<WorkingCapitalLoanTransaction> findByIdAndWcLoan_Id(Long id, Long wcLoanId);

    @Query("""
            select t from WorkingCapitalLoanTransaction t
            where t.wcLoan.id = :wcLoanId and t.transactionType = :transactionType and t.reversed = false
            order by t.id desc
            """)
    List<WorkingCapitalLoanTransaction> findActiveByTypeOrderByIdDesc(@Param("wcLoanId") Long wcLoanId,
            @Param("transactionType") LoanTransactionType transactionType);

    /** Net amortized discount fee income from non-reversed transactions: sum(amortization) - sum(adjustment). */
    // 'else 0' is required by EclipseLink's CASE grammar; EclipseLink also rejects unary negation in JPQL CASE, so the
    // adjustment branch subtracts via (0 - amount) rather than -amount.
    @Query("""
            select coalesce(sum(case when t.transactionType = :amortizationType then t.transactionAmount
                                     when t.transactionType = :adjustmentType then (0 - t.transactionAmount)
                                     else 0 end), 0)
            from WorkingCapitalLoanTransaction t
            where t.wcLoan.id = :wcLoanId and t.reversed = false
              and t.transactionType in (:amortizationType, :adjustmentType)
            """)
    BigDecimal sumNetAmortization(@Param("wcLoanId") Long wcLoanId, @Param("amortizationType") LoanTransactionType amortizationType,
            @Param("adjustmentType") LoanTransactionType adjustmentType);

    /**
     * Total amount of non-reversed discount fee adjustments dated strictly after {@code date} (not yet effective then).
     */
    @Query("""
            select coalesce(sum(t.transactionAmount), 0)
            from WorkingCapitalLoanTransaction t
            where t.wcLoan.id = :wcLoanId and t.reversed = false
              and t.transactionType = :transactionType and t.transactionDate > :date
            """)
    BigDecimal sumDiscountFeeAdjustmentsAfter(@Param("wcLoanId") Long wcLoanId,
            @Param("transactionType") LoanTransactionType transactionType, @Param("date") LocalDate date);

    Optional<WorkingCapitalLoanTransaction> findByWcLoan_IdAndExternalId(Long wcLoanId, ExternalId externalId);

    boolean existsByExternalId(ExternalId externalId);

    List<WorkingCapitalLoanTransaction> findByWcLoan_IdAndTransactionDateGreaterThanEqualOrderByTransactionDateAscIdAsc(Long wcLoanId,
            LocalDate boundaryDate);

    @Query("""
            SELECT t.transactionDate, SUM(t.transactionAmount)
            FROM WorkingCapitalLoanTransaction t
            WHERE t.reversed = FALSE
            AND t.wcLoan.id = :wcLoanId
            AND t.transactionType in :transactionTypes
            GROUP BY t.transactionDate
            """)
    List<TransactionDateAndAmountHolder> fetchTransactionDateAndAmount(@Param("wcLoanId") Long wcLoanId,
            @Param("transactionTypes") List<LoanTransactionType> transactionTypes);

    /**
     * Whether a non-reversed transaction sorts strictly after the given one, in the same (transaction date, id) order
     * the replay uses. The given transaction is excluded by the strict comparison itself, and reversed transactions no
     * longer move money so they cannot make an earlier allocation order-dependent.
     */
    @Query("""
            SELECT CASE WHEN COUNT(transaction) > 0 THEN TRUE ELSE FALSE END
            FROM WorkingCapitalLoanTransaction transaction
            WHERE transaction.wcLoan.id = :loanId
            AND transaction.reversed = FALSE
            AND (transaction.transactionDate > :transactionDate
                OR (transaction.transactionDate = :transactionDate AND transaction.createdDate > :createdDateTime))
            """)
    boolean existsLaterTransaction(@Param("loanId") Long loanId, @Param("transactionDate") LocalDate transactionDate,
            @Param("createdDateTime") OffsetDateTime createdDateTime);

    @Query("""
            SELECT t.transactionType, t.reversed, SUM(t.transactionAmount)
            FROM WorkingCapitalLoanTransaction t
            WHERE t.wcLoan.id = :wcLoanId
            AND t.transactionType in :transactionTypes
            GROUP BY t.transactionType, t.reversed
            """)
    List<TransactionTypeTotalHolder> fetchTotalsPerTypeAndReversed(@Param("wcLoanId") Long wcLoanId,
            @Param("transactionTypes") List<LoanTransactionType> transactionTypes);

    @Query("""
            SELECT t.transactionDate, t.transactionAmount FROM WorkingCapitalLoanTransaction t
            WHERE t.wcLoan.id = :wcLoanId
            AND t.reversed = FALSE
            AND t.transactionType in :transactionTypes
            ORDER BY t.transactionDate DESC, t.id DESC
            """)
    List<TransactionDateAndAmountHolder> findActiveByTypesOrderByDateDesc(@Param("wcLoanId") Long wcLoanId,
            @Param("transactionTypes") List<LoanTransactionType> transactionTypes, Pageable pageable);

    /**
     * Non-reversed transactions of the loan whose type is none of {@code excludedTypes}, latest first in the
     * (transaction date, id) order the replay uses. Used to find the last user transaction without loading the loan's
     * whole transaction history.
     */
    @Query("""
            SELECT t FROM WorkingCapitalLoanTransaction t
            WHERE t.wcLoan.id = :wcLoanId
            AND t.reversed = FALSE
            AND t.transactionType not in :excludedTypes
            ORDER BY t.transactionDate DESC, t.id DESC
            """)
    List<WorkingCapitalLoanTransaction> findActiveExcludingTypesOrderByDateDesc(@Param("wcLoanId") Long wcLoanId,
            @Param("excludedTypes") List<LoanTransactionType> excludedTypes, Pageable pageable);
}
