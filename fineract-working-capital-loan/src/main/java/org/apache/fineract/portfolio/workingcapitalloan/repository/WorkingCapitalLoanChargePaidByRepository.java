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

import java.util.List;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanChargePaidBy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkingCapitalLoanChargePaidByRepository extends JpaRepository<WorkingCapitalLoanChargePaidBy, Long> {

    /**
     * Drops every paid-by row of a loan in one statement, for the reprocessing reset that rebuilds them.
     *
     * <p>
     * Deliberately a bulk delete rather than a derived one. A derived delete loads each row and removes it through the
     * persistence context, which defers the deletes to the next flush - and Hibernate runs inserts before deletes
     * within a flush, so the rebuilt rows would be written while the rows they replace are still present. This executes
     * immediately instead, so the reset is complete before anything is rebuilt, whatever constraints the table carries.
     *
     * <p>
     * {@code flushAutomatically} pushes pending changes out before the delete; the context is deliberately
     * <strong>not</strong> cleared afterwards, because the caller is mid-reprocess and still holds managed balance,
     * charge and allocation entities it goes on to mutate and save.
     */
    @Modifying(flushAutomatically = true)
    @Query("""
            delete from WorkingCapitalLoanChargePaidBy paidBy
            where paidBy.wcLoanTransaction.id in (
                select transaction.id from WorkingCapitalLoanTransaction transaction where transaction.wcLoan.id = :loanId)
            """)
    void deleteByLoanId(@Param("loanId") Long loanId);

    @Query("""
            select paidBy from WorkingCapitalLoanChargePaidBy paidBy
            join fetch paidBy.wcLoanTransaction transaction
            join fetch paidBy.wcLoanCharge loanCharge
            join fetch loanCharge.charge
            where transaction.id in :transactionIds
            order by paidBy.id asc
            """)
    List<WorkingCapitalLoanChargePaidBy> findByTransactionIdIn(@Param("transactionIds") List<Long> transactionIds);
}
