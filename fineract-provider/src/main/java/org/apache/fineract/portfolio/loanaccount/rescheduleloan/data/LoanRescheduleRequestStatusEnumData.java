/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.rescheduleloan.data;

import org.mifosplatform.portfolio.loanaccount.domain.LoanStatus;

/**
 * Immutable data object represent loan reschedule request status enumerations.
 **/
public class LoanRescheduleRequestStatusEnumData {

    private final Long id;
    private final String code;
    private final String value;
    private final boolean pendingApproval;
    private final boolean approved;
    private final boolean rejected;

    /**
     * LoanRescheduleRequestStatusEnumData constructor
     * 
     * Note: Same status types/states for loan accounts are used in here
     **/
    public LoanRescheduleRequestStatusEnumData(Long id, String code, String value) {
        this.id = id;
        this.code = code;
        this.value = value;
        this.pendingApproval = Long.valueOf(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL.getValue()).equals(this.id);
        this.approved = Long.valueOf(LoanStatus.APPROVED.getValue()).equals(this.id);
        this.rejected = Long.valueOf(LoanStatus.REJECTED.getValue()).equals(this.id);
    }

    public Long id() {
        return this.id;
    }

    public String code() {
        return this.code;
    }

    public String value() {
        return this.value;
    }

    public boolean isPendingApproval() {
        return this.pendingApproval;
    }

    public boolean isApproved() {
        return this.approved;
    }

    public boolean isRejected() {
        return this.rejected;
    }
}
