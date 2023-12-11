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
package org.apache.fineract.portfolio.note.domain;

import java.util.List;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoteRepository extends JpaRepository<Note, Long>, JpaSpecificationExecutor<Note> {

    List<Note> findByLoanId(Long id);

    List<Note> findByClient(Client id);

    List<Note> findByGroup(Group group);

    Note findByLoanAndId(Loan loanId, Long id);

    Note findByClientAndId(Client client, Long id);

    Note findByGroupAndId(Group group, Long id);

    Note findByLoanTransactionAndId(LoanTransaction loanTransaction, Long id);

    List<Note> findBySavingsAccount(SavingsAccount savingAccount);

    Note findBySavingsAccountAndId(SavingsAccount savingAccount, Long id);

    @Query("select note from Note note where note.savingsTransaction.id = :savingsTransactionId")
    List<Note> findBySavingsTransactionId(@Param("savingsTransactionId") Long savingsTransactionId);

}
