package org.mifosng.platform.loan.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "ref_loan_status")
public class LoanStatus implements Persistable<Integer> {

	public static final Integer SUBMITED_AND_PENDING_APPROVAL = 100;
	public static final Integer APPROVED = 200;
	public static final Integer ACTIVE = 300;
	public static final Integer WITHDRAWN_BY_CLIENT = 400;
	public static final Integer REJECTED = 500;
	public static final Integer CLOSED = 600;
	
	@Id
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "display_name")
	private String displayName;
	
	public LoanStatus() {
	}

	public String getDisplayName() {
		return this.displayName;
	}

	@Override
	public Integer getId() {
		return null;
	}

	@Override
	public boolean isNew() {
		return false;
	}

	public boolean hasStateOf(Integer state) {
		return this.id.equals(state);
	}

	public boolean isSubmittedAndPendingApproval() {
		return hasStateOf(LoanStatus.SUBMITED_AND_PENDING_APPROVAL);
	}

	public boolean isApproved() {
		return hasStateOf(LoanStatus.APPROVED);
	}

	public boolean isClosed() {
		return hasStateOf(LoanStatus.CLOSED);
	}

	public boolean isWithdrawnByClient() {
		return hasStateOf(LoanStatus.WITHDRAWN_BY_CLIENT);
	}

	public boolean isRejected() {
		return hasStateOf(LoanStatus.REJECTED);
	}

	public boolean isActive() {
		return hasStateOf(LoanStatus.ACTIVE);
	}
}