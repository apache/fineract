package org.mifosng.platform.loan.domain;

public enum LoanStatusEnum {
	
	INVALID(0, "loanStatusType.invalid"), // 
	SUBMITED_AND_PENDING_APPROVAL(100, "loanStatusType.submitted.and.pending.approval"), //
	APPROVED(200, "loanStatusType.approved"), //
	ACTIVE(300, "loanStatusType.active"), //
	WITHDRAWN_BY_CLIENT(400, "loanStatusType.withdrawn.by.client"), //
	REJECTED(500, "loanStatusType.rejected"), //
	CLOSED(600, "loanStatusType.closed"), //
	OVERPAID(700, "loanStatusType.overpaid"); 

    private final Integer value;
	private final String code;
	
	public static LoanStatusEnum fromInt(final Integer selectedMethod) {

		LoanStatusEnum enumeration = LoanStatusEnum.INVALID;
		switch (selectedMethod) {
		case 100:
			enumeration = LoanStatusEnum.SUBMITED_AND_PENDING_APPROVAL;
			break;
		case 200:
			enumeration = LoanStatusEnum.APPROVED;
			break;
		case 300:
			enumeration = LoanStatusEnum.ACTIVE;
			break;
		case 400:
			enumeration = LoanStatusEnum.WITHDRAWN_BY_CLIENT;
			break;
		case 500:
			enumeration = LoanStatusEnum.REJECTED;
			break;
		case 600:
			enumeration = LoanStatusEnum.CLOSED;
			break;
		case 700:
			enumeration = LoanStatusEnum.OVERPAID;
			break;
		}
		return enumeration;
    }

    private LoanStatusEnum(final Integer value, final String code) {
        this.value = value;
		this.code = code;
    }
    
    public boolean hasStateOf(final LoanStatusEnum state) {
		return this.value.equals(state.getValue());
	}

    public Integer getValue() {
        return this.value;
    }
    
    public String getCode() {
		return code;
	}

	public boolean isSubmittedAndPendingApproval() {
		return this.value.equals(LoanStatusEnum.SUBMITED_AND_PENDING_APPROVAL.getValue());
	}
	
	public boolean isApproved() {
		return this.value.equals(LoanStatusEnum.APPROVED.getValue());
	}
	
	public boolean isActive() {
		return this.value.equals(LoanStatusEnum.ACTIVE.getValue());
	}

	public boolean isClosed() {
		return this.value.equals(LoanStatusEnum.CLOSED.getValue());
	}

	public boolean isWithdrawnByClient() {
		return this.value.equals(LoanStatusEnum.WITHDRAWN_BY_CLIENT.getValue());
	}

	public boolean isRejected() {
		return this.value.equals(LoanStatusEnum.REJECTED.getValue());
	}
}