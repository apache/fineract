/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.data;

import org.joda.time.LocalDate;

/**
 * Immutable data object represent the important time-line events of a savings
 * account application.
 */
@SuppressWarnings("unused")
public class SavingsAccountApplicationTimelineData {

    private final LocalDate submittedOnDate;
    private final String submittedByUsername;
    private final String submittedByFirstname;
    private final String submittedByLastname;
    private final LocalDate rejectedOnDate;
    private final String rejectedByUsername;
    private final String rejectedByFirstname;
    private final String rejectedByLastname;
    private final LocalDate withdrawnOnDate;
    private final String withdrawnByUsername;
    private final String withdrawnByFirstname;
    private final String withdrawnByLastname;
    private final LocalDate approvedOnDate;
    private final String approvedByUsername;
    private final String approvedByFirstname;
    private final String approvedByLastname;
    private final LocalDate activatedOnDate;
    private final String activatedByUsername;
    private final String activatedByFirstname;
    private final String activatedByLastname;
    private final LocalDate closedOnDate;
    private final String closedByUsername;
    private final String closedByFirstname;
    private final String closedByLastname;

    public static SavingsAccountApplicationTimelineData templateDefault() {

        final LocalDate submittedOnDate = null;
        final String submittedByUsername = null;
        final String submittedByFirstname = null;
        final String submittedByLastname = null;
        final LocalDate rejectedOnDate = null;
        final String rejectedByUsername = null;
        final String rejectedByFirstname = null;
        final String rejectedByLastname = null;
        final LocalDate withdrawnOnDate = null;
        final String withdrawnByUsername = null;
        final String withdrawnByFirstname = null;
        final String withdrawnByLastname = null;
        final LocalDate approvedOnDate = null;
        final String approvedByUsername = null;
        final String approvedByFirstname = null;
        final String approvedByLastname = null;
        final LocalDate activatedOnDate = null;
        final String activatedByUsername = null;
        final String activatedByFirstname = null;
        final String activatedByLastname = null;
        final LocalDate closedOnDate = null;
        final String closedByUsername = null;
        final String closedByFirstname = null;
        final String closedByLastname = null;

        return new SavingsAccountApplicationTimelineData(submittedOnDate, submittedByUsername, submittedByFirstname, submittedByLastname,
                rejectedOnDate, rejectedByUsername, rejectedByFirstname, rejectedByLastname, withdrawnOnDate, withdrawnByUsername,
                withdrawnByFirstname, withdrawnByLastname, approvedOnDate, approvedByUsername, approvedByFirstname, approvedByLastname,
                activatedOnDate, activatedByUsername, activatedByFirstname, activatedByLastname, closedOnDate, closedByUsername,
                closedByFirstname, closedByLastname);
    }

    public SavingsAccountApplicationTimelineData(final LocalDate submittedOnDate, final String submittedByUsername,
            final String submittedByFirstname, final String submittedByLastname, final LocalDate rejectedOnDate,
            final String rejectedByUsername, final String rejectedByFirstname, final String rejectedByLastname,
            final LocalDate withdrawnOnDate, final String withdrawnByUsername, final String withdrawnByFirstname,
            final String withdrawnByLastname, final LocalDate approvedOnDate, final String approvedByUsername,
            final String approvedByFirstname, final String approvedByLastname, final LocalDate activatedOnDate,
            final String activatedByUsername, final String activatedByFirstname, final String activatedByLastname,
            final LocalDate closedOnDate, final String closedByUsername, final String closedByFirstname, final String closedByLastname) {
        this.submittedOnDate = submittedOnDate;
        this.submittedByUsername = submittedByUsername;
        this.submittedByFirstname = submittedByFirstname;
        this.submittedByLastname = submittedByLastname;
        this.rejectedOnDate = rejectedOnDate;
        this.rejectedByUsername = rejectedByUsername;
        this.rejectedByFirstname = rejectedByFirstname;
        this.rejectedByLastname = rejectedByLastname;
        this.withdrawnOnDate = withdrawnOnDate;
        this.withdrawnByUsername = withdrawnByUsername;
        this.withdrawnByFirstname = withdrawnByFirstname;
        this.withdrawnByLastname = withdrawnByLastname;
        this.approvedOnDate = approvedOnDate;
        this.approvedByUsername = approvedByUsername;
        this.approvedByFirstname = approvedByFirstname;
        this.approvedByLastname = approvedByLastname;
        this.activatedOnDate = activatedOnDate;
        this.activatedByUsername = activatedByUsername;
        this.activatedByFirstname = activatedByFirstname;
        this.activatedByLastname = activatedByLastname;
        this.closedOnDate = closedOnDate;
        this.closedByUsername = closedByUsername;
        this.closedByFirstname = closedByFirstname;
        this.closedByLastname = closedByLastname;
    }
}