/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.data;

/**
 * Immutable data object represent savings account status enumerations.
 */
public class SavingsAccountStatusEnumData {

    private final Long id;
    @SuppressWarnings("unused")
    private final String code;
    @SuppressWarnings("unused")
    private final String value;
    @SuppressWarnings("unused")
    private final boolean submittedAndPendingApproval;
    @SuppressWarnings("unused")
    private final boolean approved;
    @SuppressWarnings("unused")
    private final boolean rejected;
    @SuppressWarnings("unused")
    private final boolean withdrawnByApplicant;
    @SuppressWarnings("unused")
    private final boolean active;
    @SuppressWarnings("unused")
    private final boolean closed;
    @SuppressWarnings("unused")
    private final boolean prematureClosed;
    @SuppressWarnings("unused")
    private final boolean transferInProgress;
    @SuppressWarnings("unused")
    private final boolean transferOnHold;

    public SavingsAccountStatusEnumData(final Long id, final String code, final String value, final boolean submittedAndPendingApproval,
            final boolean approved, final boolean rejected, final boolean withdrawnByApplicant, final boolean active, final boolean closed,
            final boolean prematureClosed, final boolean transferInProgress, final boolean transferOnHold) {
        this.id = id;
        this.code = code;
        this.value = value;
        this.submittedAndPendingApproval = submittedAndPendingApproval;
        this.approved = approved;
        this.rejected = rejected;
        this.withdrawnByApplicant = withdrawnByApplicant;
        this.active = active;
        this.closed = closed;
        this.prematureClosed = prematureClosed;
        this.transferInProgress = transferInProgress;
        this.transferOnHold = transferOnHold;
    }

    public Long id() {
        return this.id;
    }
}