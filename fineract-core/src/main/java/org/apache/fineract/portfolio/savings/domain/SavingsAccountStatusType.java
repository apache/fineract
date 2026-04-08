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
package org.apache.fineract.portfolio.savings.domain;

import lombok.Getter;

/**
 * Enum representation of SavingsAccount status states.
 */
@Getter
public enum SavingsAccountStatusType {

    INVALID(0, "savingsAccountStatusType.invalid"), //
    SUBMITTED_AND_PENDING_APPROVAL(100, "savingsAccountStatusType.submitted.and.pending.approval"), //
    APPROVED(200, "savingsAccountStatusType.approved"), //
    ACTIVE(300, "savingsAccountStatusType.active"), //
    TRANSFER_IN_PROGRESS(303, "savingsAccountStatusType.transfer.in.progress"), //
    TRANSFER_ON_HOLD(304, "savingsAccountStatusType.transfer.on.hold"), //
    WITHDRAWN_BY_APPLICANT(400, "savingsAccountStatusType.withdrawn.by.applicant"), //
    REJECTED(500, "savingsAccountStatusType.rejected"), //
    CLOSED(600, "savingsAccountStatusType.closed"), //
    PRE_MATURE_CLOSURE(700, "savingsAccountStatusType.pre.mature.closure"), //
    MATURED(800, "savingsAccountStatusType.matured"); //

    private final Integer value;
    private final String code;

    public static SavingsAccountStatusType fromInt(final Integer type) {
        return switch (type) {
            case 100 -> SavingsAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL;
            case 200 -> SavingsAccountStatusType.APPROVED;
            case 300 -> SavingsAccountStatusType.ACTIVE;
            case 303 -> SavingsAccountStatusType.TRANSFER_IN_PROGRESS;
            case 304 -> SavingsAccountStatusType.TRANSFER_ON_HOLD;
            case 400 -> SavingsAccountStatusType.WITHDRAWN_BY_APPLICANT;
            case 500 -> SavingsAccountStatusType.REJECTED;
            case 600 -> SavingsAccountStatusType.CLOSED;
            case 700 -> SavingsAccountStatusType.PRE_MATURE_CLOSURE;
            case 800 -> SavingsAccountStatusType.MATURED;
            default -> SavingsAccountStatusType.INVALID;
        };
    }

    SavingsAccountStatusType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public boolean hasStateOf(final SavingsAccountStatusType state) {
        return this.value.equals(state.getValue());
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
