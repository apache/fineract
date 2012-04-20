package org.mifosng.platform.client.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.mifosng.platform.infrastructure.AbstractAuditableCustom;
import org.mifosng.platform.loan.domain.Loan;
import org.mifosng.platform.loan.domain.LoanTransaction;
import org.mifosng.platform.organisation.domain.Organisation;
import org.mifosng.platform.user.domain.AppUser;

@Entity
@Table(name = "portfolio_note")
public class Note extends AbstractAuditableCustom<AppUser, Long> {

    @SuppressWarnings("unused")
	@ManyToOne
    @JoinColumn(name = "org_id", nullable = false)
    private final Organisation organisation;

    @SuppressWarnings("unused")
	@ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private final Client       client;
    
    @SuppressWarnings("unused")
	@ManyToOne
    @JoinColumn(name = "loan_id", nullable = true)
    private Loan       loan;
    
    @SuppressWarnings("unused")
	@ManyToOne
    @JoinColumn(name = "loan_transaction_id", nullable = true)
    private LoanTransaction       loanTransaction;

    @SuppressWarnings("unused")
	@Column(name = "note", length=1000)
    private String       note;
    
    @SuppressWarnings("unused")
	@Column(name = "note_type_enum")
    private Integer       noteTypeId;
    
    public enum NoteType {
        CLIENT(100), LOAN(200), LOAN_TRANSACTION(300);

        private int value;

        NoteType(int value) { this.value = value; }    

        public int getValue() { return value; }

        public static NoteType parse(int id) {
        	NoteType right = null; // Default
            for (NoteType item : NoteType.values()) {
                if (item.getValue()==id) {
                    right = item;
                    break;
                }
            }
            return right;
        }
    }

    public static Note clientNote(Organisation organisation, Client client, String note) {
		return new Note(organisation, client, note);
	}

	public static Note loanNote(Organisation organisation, Loan loan,  String note) {
		return new Note(organisation, loan, note);
	}
	
	public static Note loanTransactionNote(Organisation organisation, Loan loan, LoanTransaction loanTransaction, String note) {
		return new Note(organisation, loan, loanTransaction, note);
	}
    
    private Note(Organisation organisation, Client client, String note) {
    	this.organisation = organisation;
		this.client = client;
		this.note = note;
		this.noteTypeId = NoteType.CLIENT.getValue();
	}
    
    private Note(Organisation organisation, Loan loan, String note) {
    	this.organisation = organisation;
    	this.loan = loan;
		this.client = loan.getClient();
		this.note = note;
		this.noteTypeId = NoteType.LOAN.getValue();
	}
    
    private Note(Organisation organisation, Loan loan, LoanTransaction loanTransaction, String note) {
    	this.organisation = organisation;
    	this.loan = loan;
    	this.loanTransaction = loanTransaction;
		this.client = loan.getClient();
		this.note = note;
		this.noteTypeId = NoteType.LOAN_TRANSACTION.getValue();
	}
    
    protected Note() {
        this.organisation = null;
        this.client = null;
        this.loan = null;
        this.loanTransaction = null;
        this.note = null;
        this.noteTypeId = null;
    }

	public void update(final String note) {
		this.note = note;
	}
}