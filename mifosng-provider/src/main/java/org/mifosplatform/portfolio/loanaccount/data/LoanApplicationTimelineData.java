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
    private final LocalDate expectedMaturityDate;

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

        return new LoanApplicationTimelineData(submittedOnDate, submittedByUsername, submittedByFirstname, submittedByLastname,
                rejectedOnDate, rejectedByUsername, rejectedByFirstname, rejectedByLastname, withdrawnOnDate, withdrawnByUsername,
                withdrawnByFirstname, withdrawnByLastname, approvedOnDate, approvedByUsername, approvedByFirstname, approvedByLastname,
                expectedDisbursementDate, actualDisbursementDate, disbursedByUsername, disbursedByFirstname, disbursedByLastname,
                closedOnDate, closedByUsername, closedByFirstname, closedByLastname, expectedMaturityDate);
    }

    public LoanApplicationTimelineData(final LocalDate submittedOnDate, final String submittedByUsername,
            final String submittedByFirstname, final String submittedByLastname, final LocalDate rejectedOnDate,
            final String rejectedByUsername, final String rejectedByFirstname, final String rejectedByLastname,
            final LocalDate withdrawnOnDate, final String withdrawnByUsername, final String withdrawnByFirstname,
            final String withdrawnByLastname, final LocalDate approvedOnDate, final String approvedByUsername,
            final String approvedByFirstname, final String approvedByLastname, final LocalDate expectedDisbursementDate,
            final LocalDate actualDisbursementDate, final String disbursedByUsername, final String disbursedByFirstname,
            final String disbursedByLastname, final LocalDate closedOnDate, final String closedByUsername, final String closedByFirstname,
            final String closedByLastname, final LocalDate expectedMaturityDate) {
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
        this.expectedMaturityDate = expectedMaturityDate;
    }

    public RepaymentScheduleRelatedLoanData repaymentScheduleRelatedData(final CurrencyData currency, final BigDecimal principal,
            final BigDecimal inArrearsTolerance, final BigDecimal totalFeeChargesAtDisbursement) {
        return new RepaymentScheduleRelatedLoanData(this.expectedDisbursementDate, this.actualDisbursementDate, currency, principal,
                inArrearsTolerance, totalFeeChargesAtDisbursement);
    }
}