/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.teller.data;

import java.io.Serializable;
import java.util.Date;

/**
 * {@code TellerJournalData} represents an immutable journal data object.
 *
 * @version 1.0.0
<<<<<<< HEAD
 * @since 2.0.0
 * @see java.io.Serializable
 * @since 2.0.0
 */
public final class TellerJournalData implements Serializable {

    private final Long officeId;
    private final Long tellerId;
    private final Date day;
    private final Double openingBalance;
    private final Double settledBalance;
    private final Double closingBalance;
    private final Double sumReceipts;
    private final Double sumPayments;

    /*
     * Sole private CTOR to create a new instance.
     */
    private TellerJournalData(final Long officeId, final Long tellerId, final Date day, final Double openingBalance,
                              final Double settledBalance, final Double closingBalance, final Double sumReceipts,
                              final Double sumPayments) {
        this.officeId = officeId;
        this.tellerId = tellerId;
        this.day = day;
        this.openingBalance = openingBalance;
        this.settledBalance = settledBalance;
        this.closingBalance = closingBalance;
        this.sumReceipts = sumReceipts;
        this.sumPayments = sumPayments;
    }

    /**
     * Create a new teller journal data object.
     *
     * @param officeId       - id of related office
     * @param tellerId       - id of related teller
     * @param day            - day of this journals data
     * @param openingBalance - balance at the time of opening the teller
     * @param settledBalance - balance at the time od settling the teller
     * @param closingBalance - balance at the time of closing the teller
     * @param sumReceipts    - sum of all posted receipts
     * @param sumPayments    - sum of all posted payments
     * @return the new created {@code TellerJournalData}
     */
    public static TellerJournalData instance(final Long officeId, final Long tellerId, final Date day,
                                             final Double openingBalance, final Double settledBalance,
                                             final Double closingBalance, final Double sumReceipts,
                                             final Double sumPayments) {
        return new TellerJournalData(officeId, tellerId, day, openingBalance, settledBalance, closingBalance, sumReceipts,
                sumPayments);
    }

    public Long getOfficeId() {
        return officeId;
    }

    public Long getTellerId() {
        return tellerId;
    }

    public Date getDay() {
        return day;
    }

    public Double getOpeningBalance() {
        return openingBalance;
    }

    public Double getSettledBalance() {
        return settledBalance;
    }

    public Double getClosingBalance() {
        return closingBalance;
    }

    public Double getSumReceipts() {
        return sumReceipts;
    }

    public Double getSumPayments() {
        return sumPayments;
    }
}
