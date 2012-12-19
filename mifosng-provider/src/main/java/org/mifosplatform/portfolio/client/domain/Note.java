package org.mifosplatform.portfolio.client.domain;

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
import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransaction;
import org.mifosplatform.portfolio.savingsaccount.domain.SavingAccount;
import org.mifosplatform.portfolio.savingsdepositaccount.domain.DepositAccount;
import org.mifosplatform.useradministration.domain.AppUser;

@Entity
@Table(name = "m_note")
public class Note extends AbstractAuditableCustom<AppUser, Long> {

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private final Client client;

    @SuppressWarnings("unused")
    @ManyToOne
    @JoinColumn(name = "loan_id", nullable = true)
    private Loan loan;

    @SuppressWarnings("unused")
    @ManyToOne
    @JoinColumn(name = "loan_transaction_id", nullable = true)
    private LoanTransaction loanTransaction;

    @Column(name = "note", length = 1000)
    private String note;

    @SuppressWarnings("unused")
    @Column(name = "note_type_enum")
    private Integer noteTypeId;

    @SuppressWarnings("unused")
    @ManyToOne
    @JoinColumn(name = "deposit_account_id", nullable = true)
    private DepositAccount depositAccount;
    
    @SuppressWarnings("unused")
    @ManyToOne
    @JoinColumn(name = "saving_account_id", nullable = true)
    private SavingAccount savingAccount;

    public enum NoteType {
        CLIENT(100, "noteType.client"), LOAN(200, "noteType.loan"), LOAN_TRANSACTION(300, "noteType.loan.transaction"), DEPOSIT(400,
                "noteType.deposit"), SAVING(500,"noteType.saving");

        private Integer value;
        private String code;

        NoteType(final Integer value, final String code) {
            this.value = value;
            this.code = code;
        }

        public Integer getValue() {
            return value;
        }

        public String getCode() {
            return code;
        }

        public static NoteType parse(Integer id) {
            NoteType right = null; // Default
            for (NoteType item : NoteType.values()) {
                if (item.getValue().intValue() == id.intValue()) {
                    right = item;
                    break;
                }
            }
            return right;
        }
    }

    public static Note clientNoteFromJson(final Client client, final JsonCommand command) {
        final String note = command.stringValueOfParameterNamed("note");
        return new Note(client, note);
    }

    public static Note loanNote(final Loan loan, final String note) {
        return new Note(loan, note);
    }

    public static Note loanTransactionNote(final Loan loan, final LoanTransaction loanTransaction, final String note) {
        return new Note(loan, loanTransaction, note);
    }

    public static Note depositNote(final DepositAccount account, final String noteText) {
        return new Note(account, noteText);
    }

    private Note(Client client, String note) {
        this.client = client;
        this.note = note;
        this.noteTypeId = NoteType.CLIENT.getValue();
    }

    private Note(Loan loan, String note) {
        this.loan = loan;
        this.client = loan.client();
        this.note = note;
        this.noteTypeId = NoteType.LOAN.getValue();
    }

    private Note(Loan loan, LoanTransaction loanTransaction, String note) {
        this.loan = loan;
        this.loanTransaction = loanTransaction;
        this.client = loan.client();
        this.note = note;
        this.noteTypeId = NoteType.LOAN_TRANSACTION.getValue();
    }

    protected Note() {
        this.client = null;
        this.loan = null;
        this.loanTransaction = null;
        this.note = null;
        this.noteTypeId = null;
    }

    public Note(final DepositAccount account, final String note) {
        this.depositAccount = account;
        this.client = account.client();
        this.note = note;
        this.noteTypeId = NoteType.DEPOSIT.getValue();
    }

    public Note(SavingAccount account, String note) {
    	this.savingAccount = account;
		this.client = account.getClient();
		this.note = note;
		this.noteTypeId = NoteType.SAVING.getValue();
	}

	public Map<String, Object> update(final JsonCommand command) {
        final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(7);

        final String noteParamName = "note";
        if (command.isChangeInStringParameterNamed(noteParamName, this.note)) {
            final String newValue = command.stringValueOfParameterNamed(noteParamName);
            actualChanges.put(noteParamName, newValue);
            this.note = StringUtils.defaultIfEmpty(newValue, null);
        }
        return actualChanges;
    }

    public boolean isNotAgainstClientWithIdOf(Long clientId) {
        return !this.client.identifiedBy(clientId);
    }
    
    public static Note savingNote(final SavingAccount account, final String noteText) {
        return new Note(account, noteText);
    }
}