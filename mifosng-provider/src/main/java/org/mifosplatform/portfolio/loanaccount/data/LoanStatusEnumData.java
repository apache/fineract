/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.data;

/**
 * Immutable data object represent loan status enumerations.
 */
@SuppressWarnings("unused")
public class LoanStatusEnumData {

    private final Long id;
    private final String code;
    private final String value;
    private final boolean pendingApproval;
    private final boolean waitingForDisbursal;
    private final boolean active;
    private final boolean closedObligationsMet;
    private final boolean closedWrittenOff;
    private final boolean closedRescheduled;
    private final boolean closed;
    private final boolean overpaid;

    public LoanStatusEnumData(final Long id, final String code, final String value) {
        this.id = id;
        this.code = code;
        this.value = value;
        this.pendingApproval = Long.valueOf(100).equals(this.id);
        this.waitingForDisbursal = Long.valueOf(200).equals(this.id);
        this.active = Long.valueOf(300).equals(this.id);
        this.closedObligationsMet = Long.valueOf(600).equals(this.id);
        this.closedWrittenOff = Long.valueOf(601).equals(this.id);
        this.closedRescheduled = Long.valueOf(602).equals(this.id);
        this.closed = this.closedObligationsMet || this.closedWrittenOff || this.closedRescheduled;
        this.overpaid = Long.valueOf(700).equals(this.id);
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
}