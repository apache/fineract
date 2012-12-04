package org.mifosplatform.portfolio.savingsdepositaccount.domain;

public enum DepositAccountStatus {

    INVALID(0, "depositStatusType.invalid"), //
    SUBMITED_AND_PENDING_APPROVAL(100, "depositStatusType.submitted.and.pending.approval"), //
    // APPROVED(200, "depositStatusType.approved"), //
    ACTIVE(300, "depositStatusType.active"), //
    WITHDRAWN_BY_CLIENT(400, "depositStatusType.withdrawn.by.client"), //
    REJECTED(500, "depositStatusType.rejected"), //
    CLOSED(600, "depositStatusType.closed"), //
    MATURED(700, "depositStatusType.matured"), PRECLOSED(800, "depositStatusType.preclosed");

    private final Integer value;
    private final String code;

    private DepositAccountStatus(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public boolean hasStateOf(final DepositAccountStatus state) {
        return this.value.equals(state.getValue());
    }

    public Integer getValue() {
        return value;
    }

    public String getCode() {
        return code;
    }

    public boolean isSubmittedAndPendingApproval() {
        return this.value.equals(DepositAccountStatus.SUBMITED_AND_PENDING_APPROVAL.getValue());
    }

    /*
     * public boolean isApproved() { return
     * this.value.equals(DepositAccountStatus.APPROVED.getValue()); }
     */

    public boolean isActive() {
        return this.value.equals(DepositAccountStatus.ACTIVE.getValue());
    }

    public boolean isClosed() {
        return this.value.equals(DepositAccountStatus.CLOSED.getValue());
    }

    public boolean isWithdrawnByClient() {
        return this.value.equals(DepositAccountStatus.WITHDRAWN_BY_CLIENT.getValue());
    }

    public boolean isRejected() {
        return this.value.equals(DepositAccountStatus.REJECTED.getValue());
    }

    public boolean isPreClosed() {
        return this.value.equals(DepositAccountStatus.PRECLOSED.getValue());
    }

    public static DepositAccountStatus fromInt(final Integer statusValue) {

        DepositAccountStatus enumeration = DepositAccountStatus.INVALID;
        switch (statusValue) {
            case 100:
                enumeration = DepositAccountStatus.SUBMITED_AND_PENDING_APPROVAL;
            break;
            /*
             * case 200: enumeration = DepositAccountStatus.APPROVED; break;
             */
            case 300:
                enumeration = DepositAccountStatus.ACTIVE;
            break;
            case 400:
                enumeration = DepositAccountStatus.WITHDRAWN_BY_CLIENT;
            break;
            case 500:
                enumeration = DepositAccountStatus.REJECTED;
            break;
            case 600:
                enumeration = DepositAccountStatus.CLOSED;
            break;
            case 700:
                enumeration = DepositAccountStatus.MATURED;
            break;
            case 800:
                enumeration = DepositAccountStatus.PRECLOSED;
            break;
        }
        return enumeration;
    }
}