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

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.portfolio.loanaccount.data.LoanScheduleDelinquencyData;
import org.apache.fineract.portfolio.loanaccount.data.UnpaidChargeData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoanTransactionRepository extends JpaRepository<LoanTransaction, Long>, JpaSpecificationExecutor<LoanTransaction> {

    String FIND_ID_BY_EXTERNAL_ID = "SELECT lt.id FROM LoanTransaction lt WHERE lt.externalId = :externalId";
    String FIND_LOAN_ID_BY_ID = "SELECT lt.loan.id FROM LoanTransaction lt WHERE lt.id = :id";

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
    Collection<LoanScheduleDelinquencyData> fetchLoanTransactionsByTypeAndLessOrEqualDate(@Param("transactionType") Integer transactionType,
            @Param("businessDate") LocalDate businessDate);

    @Query(FIND_ID_BY_EXTERNAL_ID)
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

    @Query(FIND_LOAN_ID_BY_ID)
    Optional<Long> findLoanIdById(@Param("id") Long id);
}
