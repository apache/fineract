/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

/**
 * Enum representation of {@link SavingsAccount} status states.
 */
public enum SavingsAccountStatusType {

    INVALID(0, "savingsAccountStatusType.invalid"), //
    SUBMITTED_AND_PENDING_APPROVAL(100, "savingsAccountStatusType.submitted.and.pending.approval"), //
    APPROVED(200, "savingsAccountStatusType.approved"), //
    ACTIVE(300, "savingsAccountStatusType.active"), //
    TRANSFER_IN_PROGRESS(303, "savingsAccountStatusType.transfer.in.progress"), //
    TRANSFER_ON_HOLD(304, "savingsAccountStatusType.transfer.on.hold"), //
    WITHDRAWN_BY_APPLICANT(400, "savingsAccountStatusType.withdrawn.by.applicant"), //
    REJECTED(500, "savingsAccountStatusType.rejected"), //
    CLOSED(600, "savingsAccountStatusType.closed"), PRE_MATURE_CLOSURE(700, "savingsAccountStatusType.pre.mature.closure"), MATURED(800,
            "savingsAccountStatusType.matured");

    private final Integer value;
    private final String code;

    public static SavingsAccountStatusType fromInt(final Integer type) {

        SavingsAccountStatusType enumeration = SavingsAccountStatusType.INVALID;
        switch (type) {
            case 100:
                enumeration = SavingsAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL;
            break;
            case 200:
                enumeration = SavingsAccountStatusType.APPROVED;
            break;
            case 300:
                enumeration = SavingsAccountStatusType.ACTIVE;
            break;
            case 303:
                enumeration = SavingsAccountStatusType.TRANSFER_IN_PROGRESS;
            break;
            case 304:
                enumeration = SavingsAccountStatusType.TRANSFER_ON_HOLD;
            break;
            case 400:
                enumeration = SavingsAccountStatusType.WITHDRAWN_BY_APPLICANT;
            break;
            case 500:
                enumeration = SavingsAccountStatusType.REJECTED;
            break;
            case 600:
                enumeration = SavingsAccountStatusType.CLOSED;
            break;
            case 700:
                enumeration = SavingsAccountStatusType.PRE_MATURE_CLOSURE;
            break;
            case 800:
                enumeration = SavingsAccountStatusType.MATURED;
            break;
        }
        return enumeration;
    }

    private SavingsAccountStatusType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public boolean hasStateOf(final SavingsAccountStatusType state) {
        return this.value.equals(state.getValue());
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public boolean isSubmittedAndPendingApproval() {
        return this.value.equals(SavingsAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL.getValue());
    }

    public boolean isApproved() {
        return this.value.equals(SavingsAccountStatusType.APPROVED.getValue());
    }

    public boolean isRejected() {
        return this.value.equals(SavingsAccountStatusType.REJECTED.getValue());
    }

    public boolean isApplicationWithdrawnByApplicant() {
        return this.value.equals(SavingsAccountStatusType.WITHDRAWN_BY_APPLICANT.getValue());
    }

    public boolean isActive() {
        return this.value.equals(SavingsAccountStatusType.ACTIVE.getValue());
    }

    public boolean isActiveOrAwaitingApprovalOrDisbursal() {
        return isApproved() || isSubmittedAndPendingApproval() || isActive();
    }

    public boolean isClosed() {
        return this.value.equals(SavingsAccountStatusType.CLOSED.getValue()) || isRejected() || isApplicationWithdrawnByApplicant();
    }

    public boolean isTransferInProgress() {
        return this.value.equals(SavingsAccountStatusType.TRANSFER_IN_PROGRESS.getValue());
    }

    public boolean isTransferOnHold() {
        return this.value.equals(SavingsAccountStatusType.TRANSFER_ON_HOLD.getValue());
    }

    public boolean isUnderTransfer() {
        return isTransferInProgress() || isTransferOnHold();
    }

    public boolean isMatured() {
        return this.value.equals(SavingsAccountStatusType.MATURED.getValue());
    }

    public boolean isPreMatureClosure() {
        return this.value.equals(SavingsAccountStatusType.PRE_MATURE_CLOSURE.getValue());
    }
}