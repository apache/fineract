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
package org.apache.fineract.portfolio.savings.domain;

import java.time.LocalDate;
import java.util.List;
import javax.persistence.LockModeType;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionDTOV2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SavingsAccountTransactionRepository
        extends JpaRepository<SavingsAccountTransaction, Long>, JpaSpecificationExecutor<SavingsAccountTransaction> {

    @Query("select sat from SavingsAccountTransaction sat where sat.id = :transactionId and sat.savingsAccount.id = :savingsId")
    SavingsAccountTransaction findOneByIdAndSavingsAccountId(@Param("transactionId") Long transactionId,
            @Param("savingsId") Long savingsId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select st from SavingsAccountTransaction st where st.savingsAccount = :savingsAccount and st.dateOf >= :transactionDate order by st.dateOf,st.createdDate,st.id")
    List<SavingsAccountTransaction> findTransactionsAfterPivotDate(@Param("savingsAccount") SavingsAccount savingsAccount,
            @Param("transactionDate") LocalDate transactionDate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select st from SavingsAccountTransaction st where st.savingsAccount = :savingsAccount and st.dateOf = :date and st.reversalTransaction <> 1 and st.reversed <> 1 order by st.id")
    List<SavingsAccountTransaction> findTransactionRunningBalanceBeforePivotDate(@Param("savingsAccount") SavingsAccount savingsAccount,
            @Param("date") LocalDate date);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<SavingsAccountTransaction> findBySavingsAccount(@Param("savingsAccount") SavingsAccount savingsAccount);

    List<SavingsAccountTransaction> findByRefNo(@Param("refNo") String refNo);

    @Query("select sat from SavingsAccountTransaction sat where sat.savingsAccount.id = :savingsId and sat.dateOf <= :transactionDate and sat.reversed=false")
    List<SavingsAccountTransaction> findBySavingsAccountIdAndLessThanDateOfAndReversedIsFalse(@Param("savingsId") Long savingsId,
            @Param("transactionDate") LocalDate transactionDate, Pageable pageable);

    @Query("""
                    SELECT NEW org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionDTOV2(tr.id,
                tr.typeOf, tr.dateOf, tr.amount, tr.releaseIdOfHoldAmountTransaction, tr.reasonForBlock,
                tr.createdDate, tr.appUser, nt.note, tr.runningBalance, tr.reversed,
                tr.reversalTransaction, tr.originalTxnId, tr.lienTransaction, tr.isManualTransaction,
                fromTran, toTran, tr.savingsAccount, tr.paymentDetail, currency
                )
                    FROM SavingsAccountTransaction tr
                    JOIN ApplicationCurrency currency ON (currency.code = tr.savingsAccount.currency.code)
                    LEFT JOIN AccountTransferTransaction fromtran ON (fromtran.fromSavingsTransaction = tr)
                    LEFT JOIN AccountTransferTransaction totran ON (totran.toSavingsTransaction = tr)
                    LEFT JOIN tr.notes nt ON (nt.savingsTransaction = tr)
                    WHERE tr.savingsAccount.id = :savingsId
                    AND tr.savingsAccount.depositType = :depositType
                    AND (:transactionType IS NULL OR tr.typeOf = :transactionType )
                    AND (:#{#searchParameters.fromDate} IS NULL OR tr.dateOf >= :#{#searchParameters.fromDate} )
                    AND (:#{#searchParameters.toDate} IS NULL OR tr.dateOf <= :#{#searchParameters.toDate} )
                    AND (:#{#searchParameters.fromAmount} IS NULL OR tr.amount >= :#{#searchParameters.fromAmount} )
                    AND (:#{#searchParameters.toAmount} IS NULL OR tr.amount <= :#{#searchParameters.toAmount} )
            """)
    Page<SavingsAccountTransactionDTOV2> findAll(@Param("savingsId") Long savingsId, @Param("depositType") Integer depositType,
            @Param("transactionType") Integer transactionType, @Param("searchParameters") SearchParameters searchParameters,
            Pageable pageable);
}
