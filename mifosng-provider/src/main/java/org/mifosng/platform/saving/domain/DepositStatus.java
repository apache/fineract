package org.mifosng.platform.saving.domain;

public enum DepositStatus {
	
	INVALID(0, "depositStatusType.invalid"), // 
	SUBMITED_AND_PENDING_APPROVAL(100, "depositStatusType.submitted.and.pending.approval"), //
	APPROVED(200, "depositStatusType.approved"), //
	ACTIVE(300, "depositStatusType.active"), //
	WITHDRAWN_BY_CLIENT(400, "depositStatusType.withdrawn.by.client"), //
	REJECTED(500, "depositStatusType.rejected"), //
	CLOSED(600, "depositStatusType.closed"), //
	MATURED(700,"depositStatusType.matured");
	
    private final Integer value;
	private final String code;
	
	private DepositStatus(final Integer value, final String code) {
		this.value = value;
		this.code = code;
	}
	
    public boolean hasStateOf(final DepositStatus state) {
		return this.value.equals(state.getValue());
	}

	public Integer getValue() {
		return value;
	}

	public String getCode() {
		return code;
	}
	
	public boolean isSubmittedAndPendingApproval() {
		return this.value.equals(DepositStatus.SUBMITED_AND_PENDING_APPROVAL.getValue());
	}
	
	public boolean isApproved() {
		return this.value.equals(DepositStatus.APPROVED.getValue());
	}
	
	public boolean isActive() {
		return this.value.equals(DepositStatus.ACTIVE.getValue());
	}

	public boolean isClosed() {
		return this.value.equals(DepositStatus.CLOSED.getValue());
	}

	public boolean isWithdrawnByClient() {
		return this.value.equals(DepositStatus.WITHDRAWN_BY_CLIENT.getValue());
	}

	public boolean isRejected() {
		return this.value.equals(DepositStatus.REJECTED.getValue());
	}
	
	public static DepositStatus fromInt(final Integer statusValue) {

		DepositStatus enumeration = DepositStatus.INVALID;
		switch (statusValue) {
		case 100:
			enumeration = DepositStatus.SUBMITED_AND_PENDING_APPROVAL;
			break;
		case 200:
			enumeration = DepositStatus.APPROVED;
			break;
		case 300:
			enumeration = DepositStatus.ACTIVE;
			break;
		case 400:
			enumeration = DepositStatus.WITHDRAWN_BY_CLIENT;
			break;
		case 500:
			enumeration = DepositStatus.REJECTED;
			break;
		case 600:
			enumeration = DepositStatus.CLOSED;
			break;
		case 700:
			enumeration = DepositStatus.MATURED;
			break;
		}
		return enumeration;
    }   
}