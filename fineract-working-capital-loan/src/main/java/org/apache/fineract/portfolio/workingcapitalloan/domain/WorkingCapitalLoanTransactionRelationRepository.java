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

package org.apache.fineract.portfolio.workingcapitalloan.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.workingcapitalloan.data.ChargeIdAndAmountHolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkingCapitalLoanTransactionRelationRepository
        extends JpaRepository<WorkingCapitalLoanTransactionRelation, Long>, CrudRepository<WorkingCapitalLoanTransactionRelation, Long> {

    Optional<WorkingCapitalLoanTransactionRelation> findByToTransactionAndFromTransactionReversedAndFromTransactionTransactionType(
            WorkingCapitalLoanTransaction relatedDisbursementTransaction, boolean reversed, LoanTransactionType transactionType);

    List<WorkingCapitalLoanTransactionRelation> findAllByToTransactionAndFromTransactionReversedAndFromTransactionTransactionType(
            WorkingCapitalLoanTransaction relatedDisbursementTransaction, boolean reversed, LoanTransactionType transactionType);

    /**
     * Unfiltered by reversed state, unlike the sibling lookup above: used where a previously-reversed link must still
     * be found again (e.g. reviving a final discount-fee amortization the discount pool no longer zeroes out).
     */
    List<WorkingCapitalLoanTransactionRelation> findAllByToTransactionAndFromTransactionTransactionType(
            WorkingCapitalLoanTransaction toTransaction, LoanTransactionType transactionType);

    List<WorkingCapitalLoanTransactionRelation> findAllByToChargeAndFromTransactionReversedAndFromTransactionTransactionType(
            WorkingCapitalLoanCharge toCharge, boolean reversed, LoanTransactionType transactionType);

    @Query("""
            SELECT r.toCharge.id, SUM(r.fromTransaction.transactionAmount)
            FROM WorkingCapitalLoanTransactionRelation r
            WHERE r.fromTransaction.wcLoan.id = :wcLoanId
            AND r.fromTransaction.reversed = FALSE
            AND r.fromTransaction.transactionType = :transactionType
            AND r.toCharge IS NOT NULL
            GROUP BY r.toCharge.id
            """)
    List<ChargeIdAndAmountHolder> fetchTransactionAmountPerCharge(@Param("wcLoanId") Long wcLoanId,
            @Param("transactionType") LoanTransactionType transactionType);

    @Query("""
            SELECT COALESCE(SUM(r.fromTransaction.transactionAmount), 0)
            FROM WorkingCapitalLoanTransactionRelation r
            WHERE r.toCharge = :charge
            AND r.fromTransaction.reversed = FALSE
            AND r.fromTransaction.transactionType = :transactionType
            """)
    BigDecimal fetchTransactionAmountForCharge(@Param("charge") WorkingCapitalLoanCharge charge,
            @Param("transactionType") LoanTransactionType transactionType);

    /**
     * Charge-linked transactions that sort after the given one, split by whether the charge they address is a penalty.
     * The order is the replay order of {@link WorkingCapitalLoanTransactionComparator}: the transaction date decides,
     * and same-date transactions fall back to the id. The id stands in for the comparator's submitted-on and creation
     * keys, which it agrees with as long as the business date does not run backwards: ids come from an identity column,
     * and the submitted-on date is the business date the transaction was booked on.
     */
    @Query("""
            SELECT COALESCE(SUM(r.fromTransaction.transactionAmount), 0)
            FROM WorkingCapitalLoanTransactionRelation r
            WHERE r.fromTransaction.wcLoan.id = :wcLoanId
            AND r.fromTransaction.transactionType = :transactionType
            AND r.fromTransaction.reversed = FALSE
            AND r.toCharge.penaltyCharge = :penalty
            AND (r.fromTransaction.transactionDate > :afterTransactionDate
                 OR (r.fromTransaction.transactionDate = :afterTransactionDate AND r.fromTransaction.id > :afterTransactionId))
            """)
    BigDecimal sumAmountForChargesSortingAfter(@Param("wcLoanId") Long wcLoanId,
            @Param("transactionType") LoanTransactionType transactionType, @Param("afterTransactionDate") LocalDate afterTransactionDate,
            @Param("afterTransactionId") Long afterTransactionId, @Param("penalty") boolean penalty);
}
