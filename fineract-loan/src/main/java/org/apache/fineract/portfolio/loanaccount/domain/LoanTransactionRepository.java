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
package org.apache.fineract.portfolio.loanaccount.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.portfolio.loanaccount.data.CumulativeIncomeFromIncomePosting;
import org.apache.fineract.portfolio.loanaccount.data.LoanScheduleDelinquencyData;
import org.apache.fineract.portfolio.loanaccount.data.TransactionPortionsForForeclosure;
import org.apache.fineract.portfolio.loanaccount.data.UnpaidChargeData;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoanTransactionRepository extends JpaRepository<LoanTransaction, Long>, JpaSpecificationExecutor<LoanTransaction> {

    Optional<LoanTransaction> findByIdAndLoanId(Long transactionId, Long loanId);

    @Query("""
            SELECT new org.apache.fineract.portfolio.loanaccount.data.LoanScheduleDelinquencyData(
                lt.loan.id,
                min(lt.dateOf),
                0L,
                lt.loan
            ) FROM LoanTransaction lt
            WHERE lt.typeOf = :transactionType and
            lt.dateOf <= :businessDate and
            lt.loan.loanProduct.delinquencyBucket is not null
            GROUP BY lt.loan
            """)
    Collection<LoanScheduleDelinquencyData> fetchLoanTransactionsByTypeAndLessOrEqualDate(
            @Param("transactionType") LoanTransactionType transactionType, @Param("businessDate") LocalDate businessDate);

    @Query("SELECT lt.id FROM LoanTransaction lt WHERE lt.externalId = :externalId")
    Long findIdByExternalId(@Param("externalId") ExternalId externalId);

    @Query("""
                    SELECT new org.apache.fineract.portfolio.loanaccount.data.UnpaidChargeData(
                        lc.charge.id,
                        lc.charge.name,
                        SUM(lc.amountOutstanding)
                    ) FROM LoanCharge lc
                    WHERE lc.loan = :loan
                    AND lc.active = true
                    AND lc.amountOutstanding > 0
                    GROUP BY lc.charge.id, lc.charge.name
            """)
    List<UnpaidChargeData> fetchTotalUnpaidChargesForLoan(@Param("loan") Loan loan);

    @Query("SELECT lt.loan.id FROM LoanTransaction lt WHERE lt.id = :id")
    Optional<Long> findLoanIdById(@Param("id") Long id);

    @Query("""
                SELECT COALESCE(SUM(lt.unrecognizedIncomePortion), 0)
                FROM LoanTransaction lt
                WHERE lt.loan = :loan
                AND lt.typeOf = org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.WAIVE_INTEREST
                AND lt.reversed = false
                AND lt.dateOf <= :toDate
            """)
    BigDecimal findTotalUnrecognizedIncomeFromInterestWaiverByLoanAndDate(@Param("loan") Loan loan, @Param("toDate") LocalDate toDate);

    @Query("""
            SELECT COALESCE(SUM(CASE WHEN lt.typeOf = org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.ACCRUAL THEN lt.interestPortion
                 WHEN lt.typeOf = org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.ACCRUAL_ADJUSTMENT THEN -lt.interestPortion
                 ELSE 0 END), 0)
            FROM LoanTransaction lt
            WHERE lt.loan = :loan
            AND lt.reversed = false
            """)
    BigDecimal findTotalInterestAccruedAmount(@Param("loan") Loan loan);

    @Query("""
            SELECT COALESCE(SUM(lt.interestPortion), 0)
            FROM LoanTransaction lt
            WHERE lt.loan = :loan
                AND lt.reversed = false
                AND lt.typeOf = org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.ACCRUAL
                AND lt.dateOf > :fromDate
                AND lt.dateOf <= :dueDate
            """)
    BigDecimal findAccrualInterestInPeriod(@Param("loan") Loan loan, @Param("fromDate") LocalDate fromDate,
            @Param("dueDate") LocalDate dueDate);

    @Query("""
            SELECT CASE WHEN COUNT(lt) > 0 THEN false ELSE true END
            FROM LoanTransaction lt
            WHERE lt.loan = :loan
            AND lt.reversed = false
            AND lt.dateOf > :transactionDate
            """)
    boolean isChronologicallyLatest(@Param("transactionDate") LocalDate transactionDate, @Param("loan") Loan loan);

    @Query("""
            SELECT MAX(lt.dateOf) FROM LoanTransaction lt
            WHERE lt.loan = :loan
            AND lt.reversed = false
            AND lt.typeOf NOT IN (
                    org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.CONTRA,
                    org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.MARKED_FOR_RESCHEDULING,
                    org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.ACCRUAL,
                    org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.ACCRUAL_ADJUSTMENT,
                    org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.APPROVE_TRANSFER,
                    org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.INITIATE_TRANSFER,
                    org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.REJECT_TRANSFER,
                    org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.WITHDRAW_TRANSFER,
                    org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.CAPITALIZED_INCOME_AMORTIZATION,
                    org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.CAPITALIZED_INCOME_AMORTIZATION_ADJUSTMENT
            )
            """)
    Optional<LocalDate> findLastTransactionDateForReprocessing(@Param("loan") Loan loan);

    @Query("""
            SELECT lt.id FROM LoanTransaction lt
            WHERE lt.loan = :loan
            AND lt.id IS NOT NULL
            """)
    List<Long> findTransactionIdsByLoan(@Param("loan") Loan loan);

    @Query("""
            SELECT lt.id FROM LoanTransaction lt
            WHERE lt.loan = :loan
            AND lt.id IS NOT NULL
            AND lt.reversed = true
            """)
    List<Long> findReversedTransactionIdsByLoan(@Param("loan") Loan loan);

    @Query("""
            SELECT
                lt.typeOf AS transactionType,
                lt.interestPortion AS interestPortion,
                lt.feeChargesPortion AS feeChargesPortion,
                lt.penaltyChargesPortion AS penaltyChargesPortion
            FROM LoanTransaction lt
            WHERE lt.loan = :loan
                AND lt.reversed = false
                AND lt.dateOf <= :tillDate
                AND lt.typeOf NOT IN (
                    org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.DISBURSEMENT,
                    org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.REPAYMENT_AT_DISBURSEMENT
            )
            ORDER BY lt.dateOf
            """)
    List<TransactionPortionsForForeclosure> findTransactionDataForForeclosureIncome(@Param("loan") Loan loan,
            @Param("tillDate") LocalDate tillDate);

    @Query("""
            SELECT lt
            FROM LoanTransaction lt
            WHERE lt.loan = :loan
                AND lt.reversed = false
                AND lt.typeOf IN :types
                AND lt.dateOf = :transactionDate
            """)
    Optional<LoanTransaction> findNonReversedByLoanAndTypesAndDate(@Param("loan") Loan loan, @Param("types") Set<LoanTransactionType> types,
            @Param("transactionDate") LocalDate transactionDate);

    @Query("""
            SELECT lt
            FROM LoanTransaction lt
            WHERE lt.loan = :loan
                AND lt.reversed = false
                AND lt.typeOf IN :types
                AND lt.dateOf > :transactionDate
            ORDER BY lt.dateOf, lt.createdDate, lt.id
            """)
    List<LoanTransaction> findNonReversedByLoanAndTypesAndAfterDate(@Param("loan") Loan loan,
            @Param("types") Set<LoanTransactionType> types, @Param("transactionDate") LocalDate transactionDate);

    @Query("""
            SELECT lt
            FROM LoanTransaction lt
            WHERE lt.loan = :loan
                AND lt.reversed = false
                AND lt.typeOf = :type
                AND lt.dateOf > :transactionDate
            ORDER BY lt.dateOf, lt.createdDate, lt.id
            """)
    List<LoanTransaction> findNonReversedByLoanAndTypeAndAfterDate(@Param("loan") Loan loan, @Param("type") LoanTransactionType type,
            @Param("transactionDate") LocalDate transactionDate);

    @Query("""
            SELECT CASE WHEN COUNT(lt) > 0 THEN true ELSE false END
            FROM LoanTransaction lt
            WHERE lt.loan = :loan
                AND lt.reversed = false
                AND lt.typeOf IN :types
                AND lt.dateOf >= :accrualDate
            """)
    boolean existsNonReversedByLoanAndTypesAndOnOrAfterDate(@Param("loan") Loan loan, @Param("types") Set<LoanTransactionType> types,
            @Param("accrualDate") LocalDate accrualDate);

    @Query("""
            SELECT lt
            FROM LoanTransaction lt
            WHERE lt.loan = :loan
                AND lt.reversed = false
                AND lt.typeOf IN :types
                AND lt.id NOT IN :existingTransactionIds
            ORDER BY lt.dateOf, lt.createdDate, lt.id
            """)
    List<LoanTransaction> findNonReversedByLoanAndTypesAndNotInIds(@Param("loan") Loan loan, @Param("types") Set<LoanTransactionType> types,
            @Param("existingTransactionIds") List<Long> existingTransactionIds);

    @Query("""
            SELECT lt
            FROM LoanTransaction lt
            WHERE lt.loan = :loan
                AND lt.reversed = false
                AND lt.typeOf IN :types
            ORDER BY lt.dateOf, lt.createdDate, lt.id
            """)
    List<LoanTransaction> findNonReversedByLoanAndTypes(@Param("loan") Loan loan, @Param("types") Set<LoanTransactionType> types);

    @Query("""
            SELECT lt
            FROM LoanTransaction lt
            WHERE lt.loan = :loan
                AND lt.reversed = false
                AND lt.typeOf = :type
            ORDER BY lt.dateOf, lt.createdDate, lt.id
            """)
    List<LoanTransaction> findNonReversedByLoanAndType(@Param("loan") Loan loan, @Param("type") LoanTransactionType type);

    @Query("""
            SELECT lt
            FROM LoanTransaction lt
            WHERE lt.loan = :loan
                AND lt.reversed = false
                AND lt.dateOf >= :date
                AND lt.typeOf IN :types
            ORDER BY lt.dateOf, lt.createdDate, lt.id
            """)
    List<LoanTransaction> findNonReversedByLoanAndTypesAndOnOrAfterDate(@Param("loan") Loan loan,
            @Param("types") Set<LoanTransactionType> types, @Param("date") LocalDate date);

    @Query("""
            SELECT new org.apache.fineract.portfolio.loanaccount.data.CumulativeIncomeFromIncomePosting(
                COALESCE(SUM(lt.interestPortion), 0),
                COALESCE(SUM(lt.feeChargesPortion), 0),
                COALESCE(SUM(lt.penaltyChargesPortion), 0)
            )
            FROM LoanTransaction lt
            WHERE lt.loan = :loan
                AND lt.reversed = false
                AND lt.typeOf = org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.INCOME_POSTING
            """)
    CumulativeIncomeFromIncomePosting findCumulativeIncomeByLoanAndType(@Param("loan") Loan loan);

    @Query("""
            SELECT COALESCE(SUM(CASE WHEN lt.typeOf = org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.ACCRUAL THEN lcpb.amount
                 WHEN lt.typeOf = org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.ACCRUAL_ADJUSTMENT THEN -lcpb.amount
                 ELSE 0 END), 0)
            FROM LoanChargePaidBy lcpb
            JOIN lcpb.loanTransaction lt
            WHERE lcpb.loanCharge = :loanCharge
                AND lt.reversed = false
            """)
    BigDecimal findChargeAccrualAmount(@Param("loanCharge") LoanCharge loanCharge);

    @Query("""
            SELECT COALESCE(SUM(CASE WHEN lt.typeOf = org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.ACCRUAL THEN lcpb.amount
                 WHEN lt.typeOf = org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.ACCRUAL_ADJUSTMENT THEN -lcpb.amount
                 ELSE 0 END), 0)
            FROM LoanChargePaidBy lcpb
            JOIN lcpb.loanTransaction lt
            WHERE lcpb.loanCharge = :loanCharge
                AND lcpb.installmentNumber = :installmentNumber
                AND lt.reversed = false
            """)
    BigDecimal findChargeAccrualAmountByInstallment(@Param("loanCharge") LoanCharge loanCharge,
            @Param("installmentNumber") Integer installmentNumber);

    @Query("""
            SELECT COALESCE(SUM(lt.unrecognizedIncomePortion), 0)
            FROM LoanChargePaidBy lcpb
            JOIN lcpb.loanTransaction lt
            WHERE lcpb.loanCharge = :loanCharge
                AND lt.reversed = false
                AND lt.typeOf = org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.WAIVE_CHARGES
                AND lt.dateOf <= :tillDate
            """)
    BigDecimal findChargeUnrecognizedWaivedAmount(@Param("loanCharge") LoanCharge loanCharge, @Param("tillDate") LocalDate tillDate);

    @Query("""
            SELECT MAX(lt.dateOf) FROM LoanTransaction lt
            WHERE lt.loan = :loan
                AND lt.reversed = false
                AND lt.typeOf IN :types
            """)
    Optional<LocalDate> findLastNonReversedTransactionDateByLoanAndTypes(@Param("loan") Loan loan,
            @Param("types") Set<LoanTransactionType> types);

    @Query("""
            SELECT lt FROM LoanTransaction lt
            WHERE lt.loan = :loan
                AND (
                    (lt.reversed = true AND lt.id IN :existingTransactionIds AND lt.id NOT IN :existingReversedTransactionIds)
                    OR (lt.id NOT IN :existingTransactionIds)
                )
            """)
    List<LoanTransaction> findTransactionsForAccountingBridge(@Param("loan") Loan loan,
            @Param("existingTransactionIds") List<Long> existingTransactionIds,
            @Param("existingReversedTransactionIds") List<Long> existingReversedTransactionIds);

    @Query("""
            SELECT lt FROM LoanTransaction lt
            WHERE lt.loan = :loan
                AND (
                    (lt.reversed = true AND lt.id IN :existingTransactionIds)
                    OR (lt.id NOT IN :existingTransactionIds)
                )
            """)
    List<LoanTransaction> findTransactionsForAccountingBridge(@Param("loan") Loan loan,
            @Param("existingTransactionIds") List<Long> existingTransactionIds);

    @Query("""
            SELECT lt FROM LoanTransaction lt
            WHERE lt.loan = :loan
                AND lt.reversed = false
            """)
    List<LoanTransaction> findNonReversedByLoan(@Param("loan") Loan loan);

    @Query("""
            SELECT lt FROM LoanTransaction lt
            WHERE lt.loan = :loan
                AND lt.reversed = false
                AND lt.typeOf = :type
                AND lt.dateOf IN :transactionDates
            ORDER BY lt.dateOf, lt.createdDate, lt.id
            """)
    List<LoanTransaction> findNonReversedLoanAndTypeAndDates(@Param("loan") Loan loan, @Param("type") LoanTransactionType type,
            @Param("transactionDates") Set<LocalDate> transactionDates);

    @Query("""
            SELECT lt FROM LoanTransaction lt
            WHERE lt.loan = :loan
                AND lt.reversed = false
                AND lt.typeOf = :type
            ORDER BY lt.dateOf DESC
            """)
    List<LoanTransaction> findNonReversedByLoanAndType(@Param("loan") Loan loan, @Param("type") LoanTransactionType type,
            Pageable pageable);

    @Query("""
            SELECT COALESCE(SUM(CASE WHEN lt.typeOf = org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.CAPITALIZED_INCOME_AMORTIZATION THEN lt.amount
              WHEN lt.typeOf = org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.CAPITALIZED_INCOME_AMORTIZATION_ADJUSTMENT THEN -lt.amount
              ELSE 0 END), 0) FROM LoanTransaction lt
            WHERE lt.loan = :loan
            AND lt.reversed = false
            AND (lt.typeOf = org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.CAPITALIZED_INCOME_AMORTIZATION
              OR lt.typeOf = org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.CAPITALIZED_INCOME_AMORTIZATION_ADJUSTMENT)
            """)
    BigDecimal getAmortizedAmountCapitalizedIncome(@Param("loan") Loan loan);

    @Query("""
            SELECT COALESCE(SUM(CASE WHEN lt.typeOf = org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.BUY_DOWN_FEE_AMORTIZATION THEN lt.amount
              WHEN lt.typeOf = org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.BUY_DOWN_FEE_AMORTIZATION_ADJUSTMENT THEN -lt.amount
              ELSE 0 END), 0) FROM LoanTransaction lt
            WHERE lt.loan = :loan
            AND lt.reversed = false
            AND (lt.typeOf = org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.BUY_DOWN_FEE_AMORTIZATION
              OR lt.typeOf = org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.BUY_DOWN_FEE_AMORTIZATION_ADJUSTMENT)
            """)
    BigDecimal getAmortizedAmountBuyDownFee(@Param("loan") Loan loan);

    @Query("""
            SELECT lt FROM LoanTransaction lt, LoanTransactionRelation ltr
            WHERE lt.reversed = false
            AND lt = ltr.fromTransaction
            AND ltr.toTransaction = :transaction
            AND ltr.relationType = org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelationTypeEnum.ADJUSTMENT
            """)
    List<LoanTransaction> findAdjustments(@Param("transaction") LoanTransaction transaction);

    @Query("""
            SELECT lt FROM LoanTransaction lt
            WHERE lt.loan = :loan
                AND lt.reversed = false
                AND lt.typeOf NOT IN (
                        org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.CONTRA,
                        org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.MARKED_FOR_RESCHEDULING,
                        org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.ACCRUAL,
                        org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.ACCRUAL_ADJUSTMENT,
                        org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.APPROVE_TRANSFER,
                        org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.INITIATE_TRANSFER,
                        org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.REJECT_TRANSFER,
                        org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.WITHDRAW_TRANSFER,
                        org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.CAPITALIZED_INCOME_AMORTIZATION,
                        org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.CAPITALIZED_INCOME_AMORTIZATION_ADJUSTMENT
                )
            ORDER BY lt.dateOf, lt.createdDate, lt.id
            """)
    List<LoanTransaction> findNonReversedTransactionsForReprocessingByLoan(@Param("loan") Loan loan);

    @Query("""
            SELECT MAX(lt.dateOf) FROM LoanTransaction lt
            WHERE lt.loan = :loan
                AND lt.reversed = false
                AND lt.amount > 0
                AND lt.typeOf IN (
                    org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.REPAYMENT,
                    org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.MERCHANT_ISSUED_REFUND,
                    org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.PAYOUT_REFUND,
                    org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.GOODWILL_CREDIT,
                    org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.CHARGE_REFUND,
                    org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.CHARGE_ADJUSTMENT,
                    org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.DOWN_PAYMENT,
                    org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.INTEREST_PAYMENT_WAIVER,
                    org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.INTEREST_REFUND,
                    org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.CAPITALIZED_INCOME_ADJUSTMENT
                )
            """)
    Optional<LocalDate> findLastRepaymentLikeTransactionDate(@Param("loan") Loan loan);

    @Query("""
            SELECT CASE WHEN COUNT(lt) > 0 THEN true ELSE false END
            FROM LoanTransaction lt
            WHERE lt.loan = :loan
                AND lt.reversed = false
                AND lt.typeOf = :type
                AND lt.dateOf > :transactionDate
            """)
    boolean existsNonReversedByLoanAndTypeAndAfterDate(@Param("loan") Loan loan, @Param("type") LoanTransactionType type,
            @Param("transactionDate") LocalDate transactionDate);

    @Query("""
            SELECT lt FROM LoanTransaction lt
            WHERE lt.loan = :loan
                AND lt.reversed = false
                AND lt.typeOf NOT IN (
                    org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.CONTRA,
                    org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.MARKED_FOR_RESCHEDULING,
                    org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.ACCRUAL,
                    org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.ACCRUAL_ADJUSTMENT,
                    org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.ACCRUAL_ACTIVITY,
                    org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.APPROVE_TRANSFER,
                    org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.INITIATE_TRANSFER,
                    org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.REJECT_TRANSFER,
                    org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.WITHDRAW_TRANSFER,
                    org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.CHARGE_OFF,
                    org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.REAMORTIZE,
                    org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.REAGE,
                    org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.CAPITALIZED_INCOME_AMORTIZATION,
                    org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.CONTRACT_TERMINATION,
                    org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.CAPITALIZED_INCOME_AMORTIZATION_ADJUSTMENT
                )
            ORDER BY lt.dateOf, lt.createdDate, lt.id
            """)
    List<LoanTransaction> findNonReversedMonetaryTransactionsByLoan(@Param("loan") Loan loan);

    @Query("""
            SELECT COALESCE(SUM(lt.amount), 0)
            FROM LoanTransaction lt
            WHERE lt.loan = :loan
                AND lt.reversed = false
                AND lt.typeOf = org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.RECOVERY_REPAYMENT
            """)
    BigDecimal calculateTotalRecoveryPaymentAmount(@Param("loan") Loan loan);

    @Query("""
            SELECT lt FROM LoanTransaction lt
            WHERE lt.loan = :loan
                AND (
                    (:dateComparison = 'BEFORE' AND lt.dateOf < :chargeOffDate) OR
                    (:dateComparison = 'EQUAL' AND lt.dateOf = :chargeOffDate) OR
                    (:dateComparison = 'AFTER' AND lt.dateOf > :chargeOffDate)
                )
                AND (
                    (lt.reversed = true AND lt.id IN :existingTransactionIds AND lt.id NOT IN :existingReversedTransactionIds)
                    OR (lt.id NOT IN :existingTransactionIds)
                )
            ORDER BY lt.dateOf, lt.createdDate, lt.id
            """)
    List<LoanTransaction> findTransactionsForChargeOffClassification(@Param("loan") Loan loan,
            @Param("chargeOffDate") LocalDate chargeOffDate, @Param("dateComparison") String dateComparison,
            @Param("existingTransactionIds") List<Long> existingTransactionIds,
            @Param("existingReversedTransactionIds") List<Long> existingReversedTransactionIds);

    @Query("""
            SELECT lt FROM LoanTransaction lt
            WHERE lt.loan = :loan
                AND (
                    (:dateComparison = 'BEFORE' AND lt.dateOf < :chargeOffDate) OR
                    (:dateComparison = 'EQUAL' AND lt.dateOf = :chargeOffDate) OR
                    (:dateComparison = 'AFTER' AND lt.dateOf > :chargeOffDate)
                )
                AND (
                    (lt.reversed = true AND lt.id IN :existingTransactionIds)
                    OR (lt.id NOT IN :existingTransactionIds)
                )
            ORDER BY lt.dateOf, lt.createdDate, lt.id
            """)
    List<LoanTransaction> findTransactionsForChargeOffClassification(@Param("loan") Loan loan,
            @Param("chargeOffDate") LocalDate chargeOffDate, @Param("dateComparison") String dateComparison,
            @Param("existingTransactionIds") List<Long> existingTransactionIds);

    @Query("""
            SELECT lt FROM LoanTransaction lt
            WHERE lt.loan = :loan
                AND (
                    (:dateComparison = 'BEFORE' AND lt.dateOf < :chargeOffDate) OR
                    (:dateComparison = 'EQUAL' AND lt.dateOf = :chargeOffDate) OR
                    (:dateComparison = 'AFTER' AND lt.dateOf > :chargeOffDate)
                )
                AND lt.reversed = false
            ORDER BY lt.dateOf, lt.createdDate, lt.id
            """)
    List<LoanTransaction> findTransactionsForChargeOffClassification(@Param("loan") Loan loan,
            @Param("chargeOffDate") LocalDate chargeOffDate, @Param("dateComparison") String dateComparison);

}
