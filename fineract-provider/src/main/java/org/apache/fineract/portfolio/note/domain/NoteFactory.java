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

import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.apache.fineract.portfolio.shareaccounts.domain.ShareAccount;

public final class NoteFactory {

    private NoteFactory() {}

    public static Note createClientNote(Client client, String note) {
        return Note.builder() //
                .client(client) //
                .noteTypeId(NoteType.CLIENT.getValue()) //
                .note(note) //
                .build();
    }

    public static Note createGroupNote(Group group, String note) {
        return Note.builder() //
                .group(group) //
                .noteTypeId(NoteType.GROUP.getValue()) //
                .note(note) //
                .build();
    }

    public static Note createLoanNote(Loan loan, String note) {
        return Note.builder() //
                .loan(loan) //
                .client(loan.client()) //
                .noteTypeId(NoteType.LOAN.getValue()) //
                .note(note) //
                .build();
    }

    public static Note createLoanTransactionNote(Loan loan, LoanTransaction loanTransaction, String note) {
        return Note.builder() //
                .loan(loan) //
                .client(loan.client()) //
                .loanTransaction(loanTransaction) //
                .noteTypeId(NoteType.LOAN_TRANSACTION.getValue()) //
                .note(note) //
                .build();
    }

    public static Note createSavingAccountNote(SavingsAccount account, String note) {
        return Note.builder() //
                .savingsAccount(account) //
                .client(account.getClient()) //
                .noteTypeId(NoteType.SAVING_ACCOUNT.getValue()) //
                .note(note) //
                .build();
    }

    public static Note createSavingsTransactionNote(SavingsAccount savingsAccount, SavingsAccountTransaction savingsTransaction,
            String note) {
        return Note.builder() //
                .savingsAccount(savingsAccount) //
                .client(savingsAccount.getClient()) //
                .savingsTransaction(savingsTransaction) //
                .noteTypeId(NoteType.SAVINGS_TRANSACTION.getValue()) //
                .note(note) //
                .build();
    }

    public static Note createShareAccountNote(ShareAccount account, String note) {
        return Note.builder() //
                .shareAccount(account) //
                .client(account.getClient()) //
                .noteTypeId(NoteType.SHARE_ACCOUNT.getValue()) //
                .note(note) //
                .build();
    }
}
