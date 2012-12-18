package org.mifosplatform.portfolio.loanaccount.loanschedule.data;

import java.math.BigDecimal;
import java.util.Collection;

import org.mifosplatform.organisation.monetary.data.CurrencyData;

/**
 * Immutable data object to represent aspects of a loan schedule such as:
 * 
 * <ul>
 * <li>summary information - the totals for each part of schedule monitored</li>
 * <li>repayment schedule - the principal due, outstanding balance and cost of
 * loan items such as interest and charges</li>
 * </ul>
 */
public class LoanScheduleData {

    @SuppressWarnings("unused")
    private final Integer loanTermInDays;

    /**
     * The currency associated with all monetary values in loan schedule.
     */
    @SuppressWarnings("unused")
    private final CurrencyData currency;

    /**
     * The cumulative total of all money (principal) disbursed to the loan
     * applicant.
     */
    @SuppressWarnings("unused")
    private final BigDecimal cumulativePrincipalDisbursed;

    /**
     * The cumulative total of all <code>principalDue</code> for each period of
     * loan schedule. (originalPrincipal)
     */
    @SuppressWarnings("unused")
    private final BigDecimal cumulativePrincipalDue;
    @SuppressWarnings("unused")
    private final BigDecimal cumulativePrincipalPaid;
    @SuppressWarnings("unused")
    private final BigDecimal cumulativePrincipalWrittenOff;
    @SuppressWarnings("unused")
    private final BigDecimal cumulativePrincipalOutstanding;

    /**
     * The cumulative total of interest expected on any principal disbursed.
     * (originalInterest)
     */
    @SuppressWarnings("unused")
    private final BigDecimal cumulativeInterestExpected;
    @SuppressWarnings("unused")
    private final BigDecimal cumulativeInterestPaid;
    @SuppressWarnings("unused")
    private final BigDecimal cumulativeInterestWaived;
    @SuppressWarnings("unused")
    private final BigDecimal cumulativeInterestWrittenOff;
    @SuppressWarnings("unused")
    private final BigDecimal cumulativeInterestOutstanding;

    /**
     * The cumulative total of all fee charges applied on the loan to date.
     */
    @SuppressWarnings("unused")
    private final BigDecimal cumulativeFeeChargesToDate;
    @SuppressWarnings("unused")
    private final BigDecimal cumulativeFeeChargesPaid;
    @SuppressWarnings("unused")
    private final BigDecimal cumulativeFeeChargesWaived;
    @SuppressWarnings("unused")
    private final BigDecimal cumulativeFeeChargesWrittenOff;
    @SuppressWarnings("unused")
    private final BigDecimal cumulativeFeeChargesOutstanding;

    @SuppressWarnings("unused")
    private final BigDecimal cumulativePenaltyChargesToDate;
    @SuppressWarnings("unused")
    private final BigDecimal cumulativePenaltyChargesPaid;
    @SuppressWarnings("unused")
    private final BigDecimal cumulativePenaltyChargesWaived;
    @SuppressWarnings("unused")
    private final BigDecimal cumulativePenaltyChargesWrittenOff;
    @SuppressWarnings("unused")
    private final BigDecimal cumulativePenaltyChargesOutstanding;

    /**
     * The cumulative total of all costs on the loan. Costs tracked are:
     * <ul>
     * <li>Interest</li>
     * <li>Charges (fees and penalties)</li>
     * </ul>
     */
    @SuppressWarnings("unused")
    private final BigDecimal totalCostOfLoan;

    /**
     * The sum of <code>principalDisbursed</code> and
     * <code>totalCostOfLoan</code>. (originalTotal)
     */
    @SuppressWarnings("unused")
    private final BigDecimal totalExpectedRepayment;

    /**
     * The cumulative sum of all repayments to date. (totalPaid)
     */
    @SuppressWarnings("unused")
    private final BigDecimal totalPaidToDate;

    /**
     * The cumulative sum of all waivers to date.
     */
    @SuppressWarnings("unused")
    private final BigDecimal totalWaivedToDate;

    /**
     * The cumulative sum of all waivers to date.
     */
    @SuppressWarnings("unused")
    private final BigDecimal totalWrittenOffToDate;

    /**
     * The cumulative sum of all principal, interest & charges outstanding.
     */
    private final BigDecimal totalOutstanding;

    /**
     * The amount by which this loan is is arrears.
     * 
     * Different to outstanding as arrears is based on whether a portion of the
     * total outstanding is late and above any tolerance setting.
     */
    @SuppressWarnings("unused")
    private final BigDecimal totalInArrears;

    /**
     * <code>periods</code> is collection of data objects containing specific
     * information to each period of the loan schedule including disbursement
     * and repayment information.
     */
    private final Collection<LoanSchedulePeriodData> periods;

    public LoanScheduleData() {
        this.cumulativePrincipalDisbursed = null;
        this.cumulativePrincipalPaid = null;
        this.cumulativePrincipalWrittenOff = null;
        this.cumulativePrincipalDue = null;
        this.cumulativePrincipalOutstanding = null;

        this.cumulativeInterestExpected = null;
        this.cumulativeInterestPaid = null;
        this.cumulativeInterestWaived = null;
        this.cumulativeInterestWrittenOff = null;
        this.cumulativeInterestOutstanding = null;

        this.cumulativeFeeChargesToDate = null;
        this.cumulativeFeeChargesPaid = null;
        this.cumulativeFeeChargesWaived = null;
        this.cumulativeFeeChargesWrittenOff = null;
        this.cumulativeFeeChargesOutstanding = null;

        this.cumulativePenaltyChargesToDate = null;
        this.cumulativePenaltyChargesPaid = null;
        this.cumulativePenaltyChargesWaived = null;
        this.cumulativePenaltyChargesWrittenOff = null;
        this.cumulativePenaltyChargesOutstanding = null;

        this.totalCostOfLoan = null;

        this.totalExpectedRepayment = null;
        this.totalPaidToDate = null;
        this.totalWaivedToDate = null;
        this.totalWrittenOffToDate = null;
        this.totalOutstanding = null;
        this.totalInArrears = null;

        this.currency = null;
        this.periods = null;
        this.loanTermInDays = null;
    }

    public LoanScheduleData(final CurrencyData currency, final Collection<LoanSchedulePeriodData> periods, final Integer loanTermInDays,
            final BigDecimal cumulativePrincipalDisbursed, final BigDecimal cumulativePrincipalDue,
            final BigDecimal cumulativePrincipalOutstanding, final BigDecimal cumulativeInterestExpected,
            final BigDecimal cumulativeFeeChargesToDate, final BigDecimal totalExpectedRepayment) {
        this.currency = currency;
        this.periods = periods;
        this.loanTermInDays = loanTermInDays;

        this.cumulativePrincipalDisbursed = cumulativePrincipalDisbursed;
        this.cumulativePrincipalDue = cumulativePrincipalDue;
        this.cumulativePrincipalPaid = null;
        this.cumulativePrincipalWrittenOff = null;
        this.cumulativePrincipalOutstanding = cumulativePrincipalOutstanding;

        this.cumulativeInterestExpected = cumulativeInterestExpected;
        this.cumulativeInterestPaid = null;
        this.cumulativeInterestWaived = null;
        this.cumulativeInterestWrittenOff = null;
        this.cumulativeInterestOutstanding = null;

        this.cumulativeFeeChargesToDate = cumulativeFeeChargesToDate;
        this.cumulativeFeeChargesPaid = null;
        this.cumulativeFeeChargesWaived = null;
        this.cumulativeFeeChargesWrittenOff = null;
        this.cumulativeFeeChargesOutstanding = null;

        this.cumulativePenaltyChargesToDate = null;
        this.cumulativePenaltyChargesPaid = null;
        this.cumulativePenaltyChargesWaived = null;
        this.cumulativePenaltyChargesWrittenOff = null;
        this.cumulativePenaltyChargesOutstanding = null;

        if (cumulativeFeeChargesToDate != null) {
            this.totalCostOfLoan = cumulativeInterestExpected.add(cumulativeFeeChargesToDate);
        } else if (cumulativeInterestExpected != null) {
            this.totalCostOfLoan = cumulativeInterestExpected;
        } else {
            this.totalCostOfLoan = null;
        }

        this.totalExpectedRepayment = totalExpectedRepayment;
        this.totalPaidToDate = BigDecimal.ZERO;
        this.totalWaivedToDate = BigDecimal.ZERO;
        this.totalWrittenOffToDate = BigDecimal.ZERO;
        this.totalOutstanding = totalExpectedRepayment;
        this.totalInArrears = BigDecimal.ZERO;
    }

    /*
     * Used when fully populating
     */
    public LoanScheduleData(final CurrencyData currency, final Collection<LoanSchedulePeriodData> periods, final Integer loanTermInDays,
            final BigDecimal cumulativePrincipalDisbursed, final BigDecimal cumulativePrincipalDue,
            final BigDecimal cumulativePrincipalPaid, final BigDecimal cumulativePrincipalWrittenOff,
            final BigDecimal cumulativePrincipalOutstanding, final BigDecimal cumulativeInterestExpected,
            final BigDecimal cumulativeInterestPaid, final BigDecimal cumulativeInterestWaived,
            final BigDecimal cumulativeInterestWrittenOff, final BigDecimal cumulativeInterestOutstanding,
            final BigDecimal cumulativeFeeChargesToDate, final BigDecimal cumulativeFeeChargesPaid,
            final BigDecimal cumulativeFeeChargesWaived, final BigDecimal cumulativeFeeChargesWrittenOff,
            final BigDecimal cumulativeFeeChargesOutstanding, final BigDecimal cumulativePenaltyChargesToDate,
            final BigDecimal cumulativePenaltyChargesPaid, final BigDecimal cumulativePenaltyChargesWaived,
            final BigDecimal cumulativePenaltyChargesWrittenOff, final BigDecimal cumulativePenaltyChargesOutstanding,
            final BigDecimal totalCostOfLoan, final BigDecimal totalExpectedRepayment, final BigDecimal totalPaidToDate,
            final BigDecimal totalWaivedToDate, final BigDecimal totalWrittenOffToDate, final BigDecimal totalOutstanding,
            final BigDecimal totalInArrears) {
        this.currency = currency;
        this.periods = periods;
        this.loanTermInDays = loanTermInDays;

        this.cumulativePrincipalDisbursed = cumulativePrincipalDisbursed;
        this.cumulativePrincipalDue = cumulativePrincipalDue;
        this.cumulativePrincipalPaid = cumulativePrincipalPaid;
        this.cumulativePrincipalWrittenOff = cumulativePrincipalWrittenOff;
        this.cumulativePrincipalOutstanding = cumulativePrincipalOutstanding;
        this.cumulativeInterestExpected = cumulativeInterestExpected;
        this.cumulativeInterestPaid = cumulativeInterestPaid;
        this.cumulativeInterestWrittenOff = cumulativeInterestWrittenOff;
        this.cumulativeInterestWaived = cumulativeInterestWaived;
        this.cumulativeInterestOutstanding = cumulativeInterestOutstanding;
        this.cumulativeFeeChargesToDate = cumulativeFeeChargesToDate;
        this.cumulativeFeeChargesPaid = cumulativeFeeChargesPaid;
        this.cumulativeFeeChargesWaived = cumulativeFeeChargesWaived;
        this.cumulativeFeeChargesWrittenOff = cumulativeFeeChargesWrittenOff;
        this.cumulativeFeeChargesOutstanding = cumulativeFeeChargesOutstanding;
        this.cumulativePenaltyChargesToDate = cumulativePenaltyChargesToDate;
        this.cumulativePenaltyChargesPaid = cumulativePenaltyChargesPaid;
        this.cumulativePenaltyChargesWaived = cumulativePenaltyChargesWaived;
        this.cumulativePenaltyChargesWrittenOff = cumulativePenaltyChargesWrittenOff;
        this.cumulativePenaltyChargesOutstanding = cumulativePenaltyChargesOutstanding;
        this.totalCostOfLoan = totalCostOfLoan;
        this.totalExpectedRepayment = totalExpectedRepayment;
        this.totalPaidToDate = totalPaidToDate;
        this.totalWaivedToDate = totalWaivedToDate;
        this.totalWrittenOffToDate = totalWrittenOffToDate;
        this.totalOutstanding = totalOutstanding;
        this.totalInArrears = totalInArrears;
    }

    public Collection<LoanSchedulePeriodData> getPeriods() {
        return periods;
    }

    public BigDecimal totalOutstanding() {
        return this.totalOutstanding;
    }
}