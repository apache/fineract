/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.data;

import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.monetary.data.CurrencyData;

/**
 * Immutable data object represent the important time-line events of a loan
 * application and loan.
 */
@SuppressWarnings("unused")
public class LoanApplicationTimelineData {

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
    private final LocalDate expectedDisbursementDate;
    private final LocalDate actualDisbursementDate;
    private final String disbursedByUsername;
    private final String disbursedByFirstname;
    private final String disbursedByLastname;
    private final LocalDate closedOnDate;
    private final String closedByUsername;
    private final String closedByFirstname;
    private final String closedByLastname;
    private final LocalDate expectedMaturityDate;
    private final LocalDate writeOffOnDate;
    private final String writeOffByUsername;
    private final String writeOffByFirstname;
    private final String writeOffByLastname;

    public static LoanApplicationTimelineData templateDefault(final LocalDate expectedDisbursementDate) {

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
        final LocalDate actualDisbursementDate = null;
        final String disbursedByUsername = null;
        final String disbursedByFirstname = null;
        final String disbursedByLastname = null;
        final LocalDate closedOnDate = null;
        final String closedByUsername = null;
        final String closedByFirstname = null;
        final String closedByLastname = null;
        final LocalDate expectedMaturityDate = null;
        final LocalDate writeOffOnDate = null;
        final String writeOffByUsername = null;
        final String writeOffByFirstname = null;
        final String writeOffByLastname = null;

        return new LoanApplicationTimelineData(submittedOnDate, submittedByUsername, submittedByFirstname, submittedByLastname,
                rejectedOnDate, rejectedByUsername, rejectedByFirstname, rejectedByLastname, withdrawnOnDate, withdrawnByUsername,
                withdrawnByFirstname, withdrawnByLastname, approvedOnDate, approvedByUsername, approvedByFirstname, approvedByLastname,
                expectedDisbursementDate, actualDisbursementDate, disbursedByUsername, disbursedByFirstname, disbursedByLastname,
                closedOnDate, closedByUsername, closedByFirstname, closedByLastname, expectedMaturityDate, writeOffOnDate,
                writeOffByUsername, writeOffByFirstname, writeOffByLastname);
    }

    public LoanApplicationTimelineData(final LocalDate submittedOnDate, final String submittedByUsername,
            final String submittedByFirstname, final String submittedByLastname, final LocalDate rejectedOnDate,
            final String rejectedByUsername, final String rejectedByFirstname, final String rejectedByLastname,
            final LocalDate withdrawnOnDate, final String withdrawnByUsername, final String withdrawnByFirstname,
            final String withdrawnByLastname, final LocalDate approvedOnDate, final String approvedByUsername,
            final String approvedByFirstname, final String approvedByLastname, final LocalDate expectedDisbursementDate,
            final LocalDate actualDisbursementDate, final String disbursedByUsername, final String disbursedByFirstname,
            final String disbursedByLastname, final LocalDate closedOnDate, final String closedByUsername, final String closedByFirstname,
            final String closedByLastname, final LocalDate expectedMaturityDate, final LocalDate writeOffOnDate,
            final String writeOffByUsername, final String writeOffByFirstname, final String writeOffByLastname) {
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
        this.expectedDisbursementDate = expectedDisbursementDate;
        this.actualDisbursementDate = actualDisbursementDate;
        this.disbursedByUsername = disbursedByUsername;
        this.disbursedByFirstname = disbursedByFirstname;
        this.disbursedByLastname = disbursedByLastname;
        this.closedOnDate = closedOnDate;
        this.closedByUsername = closedByUsername;
        this.closedByFirstname = closedByFirstname;
        this.closedByLastname = closedByLastname;
        this.expectedMaturityDate = expectedMaturityDate;
        this.writeOffOnDate = writeOffOnDate;
        this.writeOffByUsername = writeOffByUsername;
        this.writeOffByFirstname = writeOffByFirstname;
        this.writeOffByLastname = writeOffByLastname;
    }

    public RepaymentScheduleRelatedLoanData repaymentScheduleRelatedData(final CurrencyData currency, final BigDecimal principal,
            final BigDecimal approvedPrincipal, final BigDecimal inArrearsTolerance, final BigDecimal totalFeeChargesAtDisbursement) {
        return new RepaymentScheduleRelatedLoanData(this.expectedDisbursementDate, this.actualDisbursementDate, currency, principal,
                 inArrearsTolerance, totalFeeChargesAtDisbursement);
    }
}