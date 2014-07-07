/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.rescheduleloan.domain;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_loan_repayment_schedule_history")
public class LoanRepaymentScheduleHistory extends AbstractPersistable<Long> {
	
	@ManyToOne(optional = false)
    @JoinColumn(name = "loan_id")
    private Loan loan;
	
	@OneToOne(optional = true)
    @JoinColumn(name = "loan_reschedule_request_id")
	private LoanRescheduleRequest loanRescheduleRequest;

    @Column(name = "installment", nullable = false)
    private Integer installmentNumber;

    @Temporal(TemporalType.DATE)
    @Column(name = "fromdate", nullable = true)
    private Date fromDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "duedate", nullable = false)
    private Date dueDate;

    @Column(name = "principal_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal principal;

    @Column(name = "principal_completed_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal principalCompleted;

    @Column(name = "principal_writtenoff_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal principalWrittenOff;

    @Column(name = "interest_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal interestCharged;

    @Column(name = "interest_completed_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal interestPaid;

    @Column(name = "interest_waived_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal interestWaived;

    @Column(name = "interest_writtenoff_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal interestWrittenOff;

    @Column(name = "fee_charges_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal feeChargesCharged;

    @Column(name = "fee_charges_completed_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal feeChargesPaid;

    @Column(name = "fee_charges_writtenoff_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal feeChargesWrittenOff;

    @Column(name = "fee_charges_waived_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal feeChargesWaived;

    @Column(name = "penalty_charges_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal penaltyCharges;

    @Column(name = "penalty_charges_completed_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal penaltyChargesPaid;

    @Column(name = "penalty_charges_writtenoff_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal penaltyChargesWrittenOff;

    @Column(name = "penalty_charges_waived_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal penaltyChargesWaived;

    @Column(name = "total_paid_in_advance_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal totalPaidInAdvance;

    @Column(name = "total_paid_late_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal totalPaidLate;

    @Column(name = "completed_derived", nullable = false)
    private boolean obligationsMet;

    @Temporal(TemporalType.DATE)
    @Column(name = "obligations_met_on_date")
    private Date obligationsMetOnDate;
    
    @Temporal(TemporalType.DATE)
    @Column(name = "created_date")
    private Date createdOnDate;
    
    @ManyToOne
    @JoinColumn(name = "createdby_id")
    private AppUser createdByUser;
    
    @ManyToOne
    @JoinColumn(name = "lastmodifiedby_id")
    private AppUser lastModifiedByUser;
    
    @Temporal(TemporalType.DATE)
    @Column(name = "lastmodified_date")
    private Date lastModifiedOnDate;
    
    /** 
     * LoanRepaymentScheduleHistory constructor
     **/
    protected LoanRepaymentScheduleHistory() {}
    
    /** 
     * LoanRepaymentScheduleHistory constructor 
     **/
    private LoanRepaymentScheduleHistory(final Loan loan, final LoanRescheduleRequest loanRescheduleRequest, 
    		final Integer installmentNumber, final Date fromDate, final Date dueDate, final BigDecimal principal, 
    		final BigDecimal principalCompleted, final BigDecimal principalWrittenOff, final BigDecimal interestCharged,
    		final BigDecimal interestPaid, final BigDecimal interestWaived, final BigDecimal interestWrittenOff, 
    		final BigDecimal feeChargesCharged, final BigDecimal feeChargesPaid, final BigDecimal feeChargesWrittenOff, 
    		final BigDecimal feeChargesWaived, final BigDecimal penaltyCharges, final BigDecimal penaltyChargesPaid, 
    		final BigDecimal penaltyChargesWrittenOff, final BigDecimal penaltyChargesWaived, 
    		final BigDecimal totalPaidInAdvance, final BigDecimal totalPaidLate, final boolean obligationsMet, 
    		final Date obligationsMetOnDate, final Date createdOnDate, final AppUser createdByUser, 
    		final AppUser lastModifiedByUser, final Date lastModifiedOnDate) {
    	
    	this.loan = loan;
    	this.loanRescheduleRequest = loanRescheduleRequest;
    	this.installmentNumber = installmentNumber;
    	this.fromDate = fromDate;
    	this.dueDate = dueDate;
    	this.principal = principal;
    	this.principalCompleted = principalCompleted;
    	this.principalWrittenOff = principalWrittenOff;
    	this.interestCharged = interestCharged;
    	this.interestPaid = interestPaid;
    	this.interestWaived = interestWaived;
    	this.interestWrittenOff = interestWrittenOff;
    	this.feeChargesCharged = feeChargesCharged;
    	this.feeChargesPaid = feeChargesPaid;
    	this.feeChargesWrittenOff = feeChargesWrittenOff;
    	this.feeChargesWaived = feeChargesWaived;
    	this.penaltyCharges = penaltyCharges;
    	this.penaltyChargesPaid = penaltyChargesPaid;
    	this.penaltyChargesWrittenOff = penaltyChargesWrittenOff;
    	this.penaltyChargesWaived = penaltyChargesWaived;
    	this.totalPaidInAdvance = totalPaidInAdvance;
    	this.totalPaidLate = totalPaidLate;
    	this.obligationsMet = obligationsMet;
    	this.obligationsMetOnDate = obligationsMetOnDate;
    	this.createdOnDate = createdOnDate;
    	this.createdByUser = createdByUser;
    	this.lastModifiedByUser = lastModifiedByUser;
    	this.lastModifiedOnDate = lastModifiedOnDate;
    }
    
    /** 
     * @return an instance of the LoanRepaymentScheduleHistory class
     **/
    public static LoanRepaymentScheduleHistory instance(final Loan loan, final LoanRescheduleRequest loanRescheduleRequest, 
    		final Integer installmentNumber, final Date fromDate, final Date dueDate, final BigDecimal principal, 
    		final BigDecimal principalCompleted, final BigDecimal principalWrittenOff, final BigDecimal interestCharged,
    		final BigDecimal interestPaid, final BigDecimal interestWaived, final BigDecimal interestWrittenOff, 
    		final BigDecimal feeChargesCharged, final BigDecimal feeChargesPaid, final BigDecimal feeChargesWrittenOff, 
    		final BigDecimal feeChargesWaived, final BigDecimal penaltyCharges, final BigDecimal penaltyChargesPaid, 
    		final BigDecimal penaltyChargesWrittenOff, final BigDecimal penaltyChargesWaived, 
    		final BigDecimal totalPaidInAdvance, final BigDecimal totalPaidLate, final boolean obligationsMet, 
    		final Date obligationsMetOnDate, final Date createdOnDate, final AppUser createdByUser, 
    		final AppUser lastModifiedByUser, final Date lastModifiedOnDate) {
    	
    	return new LoanRepaymentScheduleHistory(loan, loanRescheduleRequest, installmentNumber, fromDate, dueDate, principal, 
    			principalCompleted, principalWrittenOff, interestCharged, interestPaid, interestWaived, interestWrittenOff, 
    			feeChargesCharged, feeChargesPaid, feeChargesWrittenOff, feeChargesWaived, penaltyCharges, penaltyChargesPaid, 
    			penaltyChargesWrittenOff, penaltyChargesWaived, totalPaidInAdvance, totalPaidLate, obligationsMet, 
    			obligationsMetOnDate, createdOnDate, createdByUser, lastModifiedByUser, lastModifiedOnDate);
    	
    }
    
    @SuppressWarnings("unused")
	public static void create(final Loan loan, final LoanRescheduleRequest loanRescheduleRequest, 
    		final Integer installmentNumber, final Date fromDate, final Date dueDate, final BigDecimal principal, 
    		final BigDecimal principalCompleted, final BigDecimal principalWrittenOff, final BigDecimal interestCharged,
    		final BigDecimal interestPaid, final BigDecimal interestWaived, final BigDecimal interestWrittenOff, 
    		final BigDecimal feeChargesCharged, final BigDecimal feeChargesPaid, final BigDecimal feeChargesWrittenOff, 
    		final BigDecimal feeChargesWaived, final BigDecimal penaltyCharges, final BigDecimal penaltyChargesPaid, 
    		final BigDecimal penaltyChargesWrittenOff, final BigDecimal penaltyChargesWaived, 
    		final BigDecimal totalPaidInAdvance, final BigDecimal totalPaidLate, final boolean obligationsMet, 
    		final Date obligationsMetOnDate, final Date createdOnDate, final AppUser createdByUser, 
    		final AppUser lastModifiedByUser, final Date lastModifiedOnDate) {
    	
    	new LoanRepaymentScheduleHistory(loan, loanRescheduleRequest, installmentNumber, fromDate, dueDate, principal, 
    			principalCompleted, principalWrittenOff, interestCharged, interestPaid, interestWaived, interestWrittenOff, 
    			feeChargesCharged, feeChargesPaid, feeChargesWrittenOff, feeChargesWaived, penaltyCharges, penaltyChargesPaid, 
    			penaltyChargesWrittenOff, penaltyChargesWaived, totalPaidInAdvance, totalPaidLate, obligationsMet, 
    			obligationsMetOnDate, createdOnDate, createdByUser, lastModifiedByUser, lastModifiedOnDate);
    }
}
