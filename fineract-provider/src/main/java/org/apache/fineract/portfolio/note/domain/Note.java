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

import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableCustom;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.apache.fineract.portfolio.shareaccounts.domain.ShareAccount;
import org.apache.fineract.useradministration.domain.AppUser;

@Entity
@Table(name = "m_note")
public class Note extends AbstractAuditableCustom<AppUser, Long> {

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = true)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = true)
    private Group group;

    @ManyToOne
    @JoinColumn(name = "loan_id", nullable = true)
    private Loan loan;

    @ManyToOne
    @JoinColumn(name = "loan_transaction_id", nullable = true)
    private LoanTransaction loanTransaction;

    @Column(name = "note", length = 1000)
    private String note;

    @Column(name = "note_type_enum")
    private Integer noteTypeId;

    @ManyToOne
    @JoinColumn(name = "savings_account_id", nullable = true)
    private SavingsAccount savingsAccount;

    @ManyToOne
    @JoinColumn(name = "savings_account_transaction_id", nullable = true)
    private SavingsAccountTransaction savingsTransaction;
    
    @ManyToOne
    @JoinColumn(name = "share_account_id", nullable = true)
    private ShareAccount shareAccount;
    
    
    public static Note clientNoteFromJson(final Client client, final JsonCommand command) {
        final String note = command.stringValueOfParameterNamed("note");
        return new Note(client, note);
    }

    public static Note groupNoteFromJson(final Group group, final JsonCommand command) {
        final String note = command.stringValueOfParameterNamed("note");
        return new Note(group, note);
    }

    public static Note loanNote(final Loan loan, final String note) {
        return new Note(loan, note);
    }

    public static Note loanTransactionNote(final Loan loan, final LoanTransaction loanTransaction, final String note) {
        return new Note(loan, loanTransaction, note);
    }

    public static Note savingNote(final SavingsAccount account, final String note) {
        return new Note(account, note);
    }

    public static Note savingsTransactionNote(final SavingsAccount savingsAccount, final SavingsAccountTransaction savingsTransaction,
            final String note) {
        return new Note(savingsAccount, savingsTransaction, note);
    }
    
    private Note(final SavingsAccount savingsAccount, final SavingsAccountTransaction savingsTransaction, final String note) {
        this.savingsAccount = savingsAccount;
        this.savingsTransaction = savingsTransaction;
        this.client = savingsAccount.getClient();
        this.note = note;
        this.noteTypeId = NoteType.SAVINGS_TRANSACTION.getValue();
    }
    
    public static Note shareNote(final ShareAccount account, final String note) {
        return new Note(account, note);
    }
    
    public Note(final Client client, final String note) {
        this.client = client;
        this.note = note;
        this.noteTypeId = NoteType.CLIENT.getValue();
    }

    private Note(final Group group, final String note) {
        this.group = group;
        this.note = note;
        this.client = null;
        this.noteTypeId = NoteType.GROUP.getValue();
    }

    private Note(final Loan loan, final String note) {
        this.loan = loan;
        this.client = loan.client();
        this.note = note;
        this.noteTypeId = NoteType.LOAN.getValue();
    }

    private Note(final Loan loan, final LoanTransaction loanTransaction, final String note) {
        this.loan = loan;
        this.loanTransaction = loanTransaction;
        this.client = loan.client();
        this.note = note;
        this.noteTypeId = NoteType.LOAN_TRANSACTION.getValue();
    }

    protected Note() {
        this.client = null;
        this.group = null;
        this.loan = null;
        this.loanTransaction = null;
        this.note = null;
        this.noteTypeId = null;
    }

    public Note(final SavingsAccount account, final String note) {
        this.savingsAccount = account;
        this.client = account.getClient();
        this.note = note;
        this.noteTypeId = NoteType.SAVING_ACCOUNT.getValue();
    }

    public Note(final ShareAccount account, final String note) {
        this.shareAccount = account;
        this.client = account.getClient();
        this.note = note;
        this.noteTypeId = NoteType.SHARE_ACCOUNT.getValue();
    }
    
    public Map<String, Object> update(final JsonCommand command) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(7);

        final String noteParamName = "note";
        if (command.isChangeInStringParameterNamed(noteParamName, this.note)) {
            final String newValue = command.stringValueOfParameterNamed(noteParamName);
            actualChanges.put(noteParamName, newValue);
            this.note = StringUtils.defaultIfEmpty(newValue, null);
        }
        return actualChanges;
    }

    public boolean isNotAgainstClientWithIdOf(final Long clientId) {
        return !this.client.identifiedBy(clientId);
    }

}