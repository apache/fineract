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
package org.apache.fineract.portfolio.account.domain;

import java.util.Collection;

import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StandingInstructionRepository extends JpaRepository<AccountTransferStandingInstruction, Long>,
        JpaSpecificationExecutor<AccountTransferStandingInstruction> {
    public final static String FIND_BY_LOAN_AND_STATUS_QUERY = "select accountTransferStandingInstruction "
            + "from AccountTransferStandingInstruction accountTransferStandingInstruction "
            + "where accountTransferStandingInstruction.status = :status "
            + "and (accountTransferStandingInstruction.accountTransferDetails.toLoanAccount = :loan "
            + "or accountTransferStandingInstruction.accountTransferDetails.fromLoanAccount = :loan)";
    
    public final static String FIND_BY_SAVINGS_AND_STATUS_QUERY = "select accountTransferStandingInstruction "
            + "from AccountTransferStandingInstruction accountTransferStandingInstruction "
            + "where accountTransferStandingInstruction.status = :status "
            + "and (accountTransferStandingInstruction.accountTransferDetails.toSavingsAccount = :savingsAccount "
            + "or accountTransferStandingInstruction.accountTransferDetails.fromSavingsAccount = :savingsAccount)";
    
    @Query(FIND_BY_LOAN_AND_STATUS_QUERY)
    public Collection<AccountTransferStandingInstruction> findByLoanAccountAndStatus(@Param("loan") Loan loan, @Param("status") Integer status);
    
    @Query(FIND_BY_SAVINGS_AND_STATUS_QUERY)
    public Collection<AccountTransferStandingInstruction> findBySavingsAccountAndStatus(@Param("savingsAccount") SavingsAccount savingsAccount, @Param("status") Integer status);    
}
