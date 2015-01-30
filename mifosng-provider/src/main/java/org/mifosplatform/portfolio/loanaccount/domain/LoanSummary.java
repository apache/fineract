/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.domain;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;

/**
 * Encapsulates all the summary details of a {@link Loan}.
 * 
 * {@link LoanSummary} fields are updated through a scheduled job. see -
 * {@link UpdateLoanSummariesScheduledJob}.
 */
@Embeddable
public final class LoanSummary {

    // derived totals fields
    @Column(name = "principal_disbursed_derived", scale = 6, precision = 19)
    private BigDecimal totalPrincipalDisbursed;

    @Column(name = "principal_repaid_derived", scale = 6, precision = 19)
    private BigDecimal totalPrincipalRepaid;

    @Column(name = "principal_writtenoff_derived", scale = 6, precision = 19)
    private BigDecimal totalPrincipalWrittenOff;

    @Column(name = "principal_outstanding_derived", scale = 6, precision = 19)
    private BigDecimal totalPrincipalOutstanding;

    @Column(name = "interest_charged_derived", scale = 6, precision = 19)
    private BigDecimal totalInterestCharged;

    @Column(name = "interest_repaid_derived", scale = 6, precision = 19)
    private BigDecimal totalInterestRepaid;

    @Column(name = "interest_waived_derived", scale = 6, precision = 19)
    private BigDecimal totalInterestWaived;

    @Column(name = "interest_writtenoff_derived", scale = 6, precision = 19)
    private BigDecimal totalInterestWrittenOff;

    @Column(name = "interest_outstanding_derived", scale = 6, precision = 19)
    private BigDecimal totalInterestOutstanding;

    @Column(name = "fee_charges_charged_derived", scale = 6, precision = 19)
    private BigDecimal totalFeeChargesCharged;

    @Column(name = "total_charges_due_at_disbursement_derived", scale = 6, precision = 19)
    private BigDecimal totalFeeChargesDueAtDisbursement;

    @Column(name = "fee_charges_repaid_derived", scale = 6, precision = 19)
    private BigDecimal totalFeeChargesRepaid;

    @Column(name = "fee_charges_waived_derived", scale = 6, precision = 19)
    private BigDecimal totalFeeChargesWaived;

    @Column(name = "fee_charges_writtenoff_derived", scale = 6, precision = 19)
    private BigDecimal totalFeeChargesWrittenOff;

    @Column(name = "fee_charges_outstanding_derived", scale = 6, precision = 19)
    private BigDecimal totalFeeChargesOutstanding;

    @Column(name = "penalty_charges_charged_derived", scale = 6, precision = 19)
    private BigDecimal totalPenaltyChargesCharged;

    @Column(name = "penalty_charges_repaid_derived", scale = 6, precision = 19)
    private BigDecimal totalPenaltyChargesRepaid;

    @Column(name = "penalty_charges_waived_derived", scale = 6, precision = 19)
    private BigDecimal totalPenaltyChargesWaived;

    @Column(name = "penalty_charges_writtenoff_derived", scale = 6, precision = 19)
    private BigDecimal totalPenaltyChargesWrittenOff;

    @Column(name = "penalty_charges_outstanding_derived", scale = 6, precision = 19)
    private BigDecimal totalPenaltyChargesOutstanding;

    @Column(name = "total_expected_repayment_derived", scale = 6, precision = 19)
    private BigDecimal totalExpectedRepayment;

    @Column(name = "total_repayment_derived", scale = 6, precision = 19)
    private BigDecimal totalRepayment;

    @Column(name = "total_expected_costofloan_derived", scale = 6, precision = 19)
    private BigDecimal totalExpectedCostOfLoan;

    @Column(name = "total_costofloan_derived", scale = 6, precision = 19)
    private BigDecimal totalCostOfLoan;

    @Column(name = "total_waived_derived", scale = 6, precision = 19)
    private BigDecimal totalWaived;

    @Column(name = "total_writtenoff_derived", scale = 6, precision = 19)
    private BigDecimal totalWrittenOff;

    @Column(name = "total_outstanding_derived", scale = 6, precision = 19)
    private BigDecimal totalOutstanding;

    public static LoanSummary create(final BigDecimal totalFeeChargesDueAtDisbursement) {
        return new LoanSummary(totalFeeChargesDueAtDisbursement);
    }

    protected LoanSummary() {
        //
    }

    private LoanSummary(final BigDecimal totalFeeChargesDueAtDisbursement) {
        this.totalFeeChargesDueAtDisbursement = totalFeeChargesDueAtDisbursement;
    }

    public void updateTotalFeeChargesDueAtDisbursement(final BigDecimal totalFeeChargesDueAtDisbursement) {
        this.totalFeeChargesDueAtDisbursement = totalFeeChargesDueAtDisbursement;
    }

    public Money getTotalFeeChargesDueAtDisbursement(final MonetaryCurrency currency) {
        return Money.of(currency, this.totalFeeChargesDueAtDisbursement);
    }

    public Money getTotalOutstanding(final MonetaryCurrency currency) {
        return Money.of(currency, this.totalOutstanding);
    }

    public boolean isRepaidInFull(final MonetaryCurrency currency) {
        return getTotalOutstanding(currency).isZero();
    }

    public BigDecimal getTotalInterestCharged() {
        return this.totalInterestCharged;
    }

    public BigDecimal getTotalPrincipalOutstanding() {
        return this.totalPrincipalOutstanding;
    }

    public BigDecimal getTotalInterestOutstanding() {
        return this.totalInterestOutstanding;
    }

    public BigDecimal getTotalFeeChargesOutstanding() {
        return this.totalFeeChargesOutstanding;
    }

    public BigDecimal getTotalPenaltyChargesOutstanding() {
        return this.totalPenaltyChargesOutstanding;
    }

    public BigDecimal getTotalOutstanding() {
        return this.totalOutstanding;
    }

    /**
     * All fields but <code>totalFeeChargesDueAtDisbursement</code> should be
     * reset.
     */
    public void zeroFields() {
        this.totalPrincipalDisbursed = BigDecimal.ZERO;
        this.totalPrincipalRepaid = BigDecimal.ZERO;
        this.totalPrincipalWrittenOff = BigDecimal.ZERO;
        this.totalPrincipalOutstanding = BigDecimal.ZERO;
        this.totalInterestCharged = BigDecimal.ZERO;
        this.totalInterestRepaid = BigDecimal.ZERO;
        this.totalInterestWaived = BigDecimal.ZERO;
        this.totalInterestWrittenOff = BigDecimal.ZERO;
        this.totalInterestOutstanding = BigDecimal.ZERO;
        this.totalFeeChargesCharged = BigDecimal.ZERO;
        this.totalFeeChargesRepaid = BigDecimal.ZERO;
        this.totalFeeChargesWaived = BigDecimal.ZERO;
        this.totalFeeChargesWrittenOff = BigDecimal.ZERO;
        this.totalFeeChargesOutstanding = BigDecimal.ZERO;
        this.totalPenaltyChargesCharged = BigDecimal.ZERO;
        this.totalPenaltyChargesRepaid = BigDecimal.ZERO;
        this.totalPenaltyChargesWaived = BigDecimal.ZERO;
        this.totalPenaltyChargesWrittenOff = BigDecimal.ZERO;
        this.totalPenaltyChargesOutstanding = BigDecimal.ZERO;
        this.totalExpectedRepayment = BigDecimal.ZERO;
        this.totalRepayment = BigDecimal.ZERO;
        this.totalExpectedCostOfLoan = BigDecimal.ZERO;
        this.totalCostOfLoan = BigDecimal.ZERO;
        this.totalWaived = BigDecimal.ZERO;
        this.totalWrittenOff = BigDecimal.ZERO;
        this.totalOutstanding = BigDecimal.ZERO;
    }

    public void updateSummary(final MonetaryCurrency currency, final Money principal,
            final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments, final LoanSummaryWrapper summaryWrapper,
            final Boolean disbursed) {

        this.totalPrincipalDisbursed = principal.getAmount();
        this.totalPrincipalRepaid = summaryWrapper.calculateTotalPrincipalRepaid(repaymentScheduleInstallments, currency).getAmount();
        this.totalPrincipalWrittenOff = summaryWrapper.calculateTotalPrincipalWrittenOff(repaymentScheduleInstallments, currency)
                .getAmount();

        this.totalPrincipalOutstanding = principal.minus(this.totalPrincipalRepaid).minus(this.totalPrincipalWrittenOff).getAmount();

        final Money totalInterestCharged = summaryWrapper.calculateTotalInterestCharged(repaymentScheduleInstallments, currency);
        this.totalInterestCharged = totalInterestCharged.getAmount();
        this.totalInterestRepaid = summaryWrapper.calculateTotalInterestRepaid(repaymentScheduleInstallments, currency).getAmount();
        this.totalInterestWaived = summaryWrapper.calculateTotalInterestWaived(repaymentScheduleInstallments, currency).getAmount();
        this.totalInterestWrittenOff = summaryWrapper.calculateTotalInterestWrittenOff(repaymentScheduleInstallments, currency).getAmount();

        if (totalInterestCharged.isGreaterThanZero()) {
            this.totalInterestOutstanding = totalInterestCharged.minus(this.totalInterestRepaid).minus(this.totalInterestWaived)
                    .minus(this.totalInterestWrittenOff).getAmount();
        }

        final Money totalFeeChargesCharged = summaryWrapper.calculateTotalFeeChargesCharged(repaymentScheduleInstallments, currency).plus(
                this.totalFeeChargesDueAtDisbursement);
        this.totalFeeChargesCharged = totalFeeChargesCharged.getAmount();

        Money totalFeeChargesRepaid = summaryWrapper.calculateTotalFeeChargesRepaid(repaymentScheduleInstallments, currency);
        if (disbursed) {
            totalFeeChargesRepaid = totalFeeChargesRepaid.plus(this.totalFeeChargesDueAtDisbursement);
        }
        this.totalFeeChargesRepaid = totalFeeChargesRepaid.getAmount();

        this.totalFeeChargesWaived = summaryWrapper.calculateTotalFeeChargesWaived(repaymentScheduleInstallments, currency).getAmount();
        this.totalFeeChargesWrittenOff = summaryWrapper.calculateTotalFeeChargesWrittenOff(repaymentScheduleInstallments, currency)
                .getAmount();

        if (totalFeeChargesCharged.isGreaterThanZero()) {
            this.totalFeeChargesOutstanding = totalFeeChargesCharged.minus(this.totalFeeChargesRepaid).minus(this.totalFeeChargesWaived)
                    .minus(this.totalFeeChargesWrittenOff).getAmount();
        }

        final Money totalPenaltyChargesCharged = summaryWrapper
                .calculateTotalPenaltyChargesCharged(repaymentScheduleInstallments, currency);
        this.totalPenaltyChargesCharged = totalPenaltyChargesCharged.getAmount();
        this.totalPenaltyChargesRepaid = summaryWrapper.calculateTotalPenaltyChargesRepaid(repaymentScheduleInstallments, currency)
                .getAmount();
        this.totalPenaltyChargesWaived = summaryWrapper.calculateTotalPenaltyChargesWaived(repaymentScheduleInstallments, currency)
                .getAmount();
        this.totalPenaltyChargesWrittenOff = summaryWrapper.calculateTotalPenaltyChargesWrittenOff(repaymentScheduleInstallments, currency)
                .getAmount();

        if (totalPenaltyChargesCharged.isGreaterThanZero()) {
            this.totalPenaltyChargesOutstanding = totalPenaltyChargesCharged.minus(this.totalPenaltyChargesRepaid)
                    .minus(this.totalPenaltyChargesWaived).minus(this.totalPenaltyChargesWrittenOff).getAmount();
        }

        final Money totalExpectedRepayment = Money.of(currency, this.totalPrincipalDisbursed).plus(this.totalInterestCharged)
                .plus(this.totalFeeChargesCharged).plus(this.totalPenaltyChargesCharged);
        this.totalExpectedRepayment = totalExpectedRepayment.getAmount();

        final Money totalRepayment = Money.of(currency, this.totalPrincipalRepaid).plus(this.totalInterestRepaid)
                .plus(this.totalFeeChargesRepaid).plus(this.totalPenaltyChargesRepaid);
        this.totalRepayment = totalRepayment.getAmount();

        final Money totalExpectedCostOfLoan = Money.of(currency, this.totalInterestCharged).plus(this.totalFeeChargesCharged)
                .plus(this.totalPenaltyChargesCharged);
        this.totalExpectedCostOfLoan = totalExpectedCostOfLoan.getAmount();

        final Money totalCostOfLoan = Money.of(currency, this.totalInterestRepaid).plus(this.totalFeeChargesRepaid)
                .plus(this.totalPenaltyChargesRepaid);
        this.totalCostOfLoan = totalCostOfLoan.getAmount();

        final Money totalWaived = Money.of(currency, this.totalInterestWaived).plus(this.totalFeeChargesWaived)
                .plus(this.totalPenaltyChargesWaived);
        this.totalWaived = totalWaived.getAmount();

        final Money totalWrittenOff = Money.of(currency, this.totalPrincipalWrittenOff).plus(this.totalInterestWrittenOff)
                .plus(this.totalFeeChargesWrittenOff).plus(this.totalPenaltyChargesWrittenOff);
        this.totalWrittenOff = totalWrittenOff.getAmount();

        final Money totalOutstanding = Money.of(currency, this.totalPrincipalOutstanding).plus(this.totalInterestOutstanding)
                .plus(this.totalFeeChargesOutstanding).plus(this.totalPenaltyChargesOutstanding);
        this.totalOutstanding = totalOutstanding.getAmount();
    }

    public BigDecimal getTotalPrincipalDisbursed() {
        return this.totalPrincipalDisbursed;
    }

    public BigDecimal getTotalPrincipalRepaid() {
        return this.totalPrincipalRepaid;
    }

    public BigDecimal getTotalWrittenOff() {
        return this.totalWrittenOff;
    }
    
    /** 
     * @return total interest repaid 
     **/
    public BigDecimal getTotalInterestRepaid() {
    	return this.totalInterestRepaid;
    }
    
    public BigDecimal getTotalFeeChargesCharged() {
    	return this.totalFeeChargesCharged;
    }
    
    public BigDecimal getTotalPenaltyChargesCharged() {
    	return this.totalPenaltyChargesCharged;
    }

    
    public BigDecimal getTotalPrincipalWrittenOff() {
        return this.totalPrincipalWrittenOff;
    }

    
    public BigDecimal getTotalInterestWaived() {
        return this.totalInterestWaived;
    }

    
    public BigDecimal getTotalFeeChargesRepaid() {
        return this.totalFeeChargesRepaid;
    }

    
    public BigDecimal getTotalFeeChargesWaived() {
        return this.totalFeeChargesWaived;
    }

    
    public BigDecimal getTotalPenaltyChargesRepaid() {
        return this.totalPenaltyChargesRepaid;
    }

    
    public BigDecimal getTotalPenaltyChargesWaived() {
        return this.totalPenaltyChargesWaived;
    }
}