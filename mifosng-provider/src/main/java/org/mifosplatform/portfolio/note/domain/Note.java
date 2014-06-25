/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.note.domain;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.group.domain.Group;
import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransaction;
import org.mifosplatform.portfolio.savings.domain.SavingsAccount;
import org.mifosplatform.useradministration.domain.AppUser;

@Entity
@Table(name = "m_note")
public class Note extends AbstractAuditableCustom<AppUser, Long> {

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = true)
    private final Client client;

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
    private final Integer noteTypeId;

    @ManyToOne
    @JoinColumn(name = "savings_account_id", nullable = true)
    private SavingsAccount savingsAccount;

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