/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.loanaccount.rescheduleloan.domain;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.useradministration.domain.AppUser;
import org.joda.time.LocalDate;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_loan_reschedule_request")
public class LoanRescheduleRequest extends AbstractPersistable<Long> {
	
	@ManyToOne
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;
	
	@Column(name = "status_enum", nullable = false)
	private Integer statusEnum;
	
	@Column(name = "reschedule_from_installment")
	private Integer rescheduleFromInstallment;
	
	@Column(name = "grace_on_principal")
	private Integer graceOnPrincipal;
	
	@Column(name = "grace_on_interest")
	private Integer graceOnInterest;
	
	@Temporal(TemporalType.DATE)
    @Column(name = "reschedule_from_date")
	private Date rescheduleFromDate;
	
	@Temporal(TemporalType.DATE)
    @Column(name = "adjusted_due_date")
	private Date adjustedDueDate;
	
	@Column(name = "extra_terms")
	private Integer extraTerms;
	
	@Column(name = "recalculate_interest")
	private Boolean recalculateInterest;
	
	@Column(name = "interest_rate", scale = 6, precision = 19)
	private BigDecimal interestRate;
	
	@ManyToOne
    @JoinColumn(name = "reschedule_reason_cv_id")
	private CodeValue rescheduleReasonCodeValue;
	
	@Column(name = "reschedule_reason_comment")
	private String rescheduleReasonComment;
	
	@Temporal(TemporalType.DATE)
    @Column(name = "submitted_on_date")
	private Date submittedOnDate;
	
	@ManyToOne
    @JoinColumn(name = "submitted_by_user_id")
	private AppUser submittedByUser;
	
	@Temporal(TemporalType.DATE)
    @Column(name = "approved_on_date")
	private Date approvedOnDate;
	
	@ManyToOne
    @JoinColumn(name = "approved_by_user_id")
	private AppUser approvedByUser;
	
	@Temporal(TemporalType.DATE)
    @Column(name = "rejected_on_date")
	private Date rejectedOnDate;
	
	@ManyToOne
    @JoinColumn(name = "rejected_by_user_id")
	private AppUser rejectedByUser;
	
	/** 
	 * LoanRescheduleRequest constructor
	 **/
	protected LoanRescheduleRequest() {}
	
	/** 
	 * LoanRescheduleRequest constructor
	 **/
	private LoanRescheduleRequest(final Loan loan, final Integer statusEnum, final Integer rescheduleFromInstallment, 
			final Integer graceOnPrincipal, final Integer graceOnInterest, final Date rescheduleFromDate, final Date adjustedDueDate,
			final Integer extraTerms, final Boolean recalculateInterest, final BigDecimal interestRate, final CodeValue rescheduleReasonCodeValue, 
			final String rescheduleReasonComment, final Date submittedOnDate, final AppUser submittedByUser, 
			final Date approvedOnDate, final AppUser approvedByUser, final Date rejectedOnDate, AppUser rejectedByUser) {
		this.loan = loan;
		this.statusEnum = statusEnum;
		this.rescheduleFromInstallment = rescheduleFromInstallment;
		this.graceOnPrincipal = graceOnPrincipal;
		this.graceOnInterest = graceOnInterest;
		this.rescheduleFromDate = rescheduleFromDate;
		this.extraTerms = extraTerms;
		this.interestRate = interestRate;
		this.rescheduleReasonCodeValue = rescheduleReasonCodeValue;
		this.rescheduleReasonComment = rescheduleReasonComment;
		this.submittedOnDate = submittedOnDate;
		this.submittedByUser = submittedByUser;
		this.approvedOnDate = approvedOnDate;
		this.approvedByUser = approvedByUser;
		this.rejectedOnDate = rejectedOnDate;
		this.rejectedByUser = rejectedByUser; 
		this.adjustedDueDate = adjustedDueDate;
		this.recalculateInterest = recalculateInterest;
	}
	
	/** 
	 * @return a new instance of the LoanRescheduleRequest class
	 **/
	public static LoanRescheduleRequest instance(final Loan loan, final Integer statusEnum, final Integer rescheduleFromInstallment, 
			final Integer graceOnPrincipal, final Integer graceOnInterest, final Date rescheduleFromDate, final Date adjustedDueDate,
			final Integer extraTerms, final Boolean recalculateInterest, final BigDecimal interestRate, final CodeValue rescheduleReasonCodeValue, 
			final String rescheduleReasonComment, final Date submittedOnDate, final AppUser submittedByUser, 
			final Date approvedOnDate, final AppUser approvedByUser, final Date rejectedOnDate, AppUser rejectedByUser) {
		
		return new LoanRescheduleRequest(loan, statusEnum, rescheduleFromInstallment, graceOnPrincipal, graceOnInterest, 
				rescheduleFromDate, adjustedDueDate, extraTerms, recalculateInterest, interestRate, rescheduleReasonCodeValue, rescheduleReasonComment, submittedOnDate,
				submittedByUser, approvedOnDate, approvedByUser, rejectedOnDate, rejectedByUser);
	}
	
	/** 
	 * @return the reschedule request loan object 
	 **/
	public Loan getLoan() {
		return this.loan;
	}
	
	/** 
	 * @return the status enum 
	 **/
	public Integer getStatusEnum() {
		return this.statusEnum;
	}
	
	/** 
	 * @return installment number of the rescheduling start point
	 **/
	public Integer getRescheduleFromInstallment() {
		return this.rescheduleFromInstallment;
	}
	
	/** 
	 * @return the grace on principal 
	 **/
	public Integer getGraceOnPrincipal() {
		return this.graceOnPrincipal;
	}
	
	/** 
	 * @return the grace on interest 
	 **/
	public Integer getGraceOnInterest() {
		return this.graceOnInterest;
	}
	
	/** 
	 * @return due date of the rescheduling start point 
	 **/
	public LocalDate getRescheduleFromDate() {
		
		LocalDate localDate = null;
		
		if(this.rescheduleFromDate != null) {
			localDate = new LocalDate(this.rescheduleFromDate);
		}
		
		return localDate;
	}
	
	/** 
	 * @return due date of the first rescheduled installment
	 **/
	public LocalDate getAdjustedDueDate() {
		
		LocalDate localDate = null;
		
		if(this.adjustedDueDate != null) {
			localDate = new LocalDate(this.adjustedDueDate);
		}
		
		return localDate;
	}
	
	/** 
	 * @return extra terms to be added after the last loan installment 
	 **/
	public Integer getExtraTerms() {
		return this.extraTerms;
	}
	
	/** 
	 * @return the new interest rate to be applied to unpaid installments 
	 **/
	public BigDecimal getInterestRate() {
		return this.interestRate;
	}
	
	/** 
	 * @return the reschedule reason code value object 
	 **/
	public CodeValue getRescheduleReasonCodeValue() {
		return this.rescheduleReasonCodeValue;
	}
	
	/** 
	 * @return the reschedule reason comment added by the "submittedByUser" 
	 **/
	public String getRescheduleReasonComment() {
		return this.rescheduleReasonComment;
	}
	
	/** 
	 * @return the date the request was submitted 
	 **/
	public LocalDate getSubmittedOnDate() {
		LocalDate localDate = null;
		
		if(this.submittedOnDate != null) {
			localDate = new LocalDate(this.submittedOnDate);
		}
		
		return localDate;
	}
	
	/** 
	 * @return the user that submitted the request 
	 **/
	public AppUser getSubmittedByUser() {
		return this.submittedByUser;
	}
	
	/** 
	 * @return the date the request was approved 
	 **/
	public LocalDate getApprovedOnDate() {
		LocalDate localDate = null;
		
		if(this.approvedOnDate != null) {
			localDate = new LocalDate(this.approvedOnDate);
		}
		
		return localDate;
	}
	
	/** 
	 * @return the user that approved the request 
	 **/
	public AppUser getApprovedByUser() {
		return this.approvedByUser;
	}
	
	/** 
	 * @return the date the request was rejected 
	 **/
	public LocalDate getRejectedOnDate() {
		LocalDate localDate = null;
		
		if(this.rejectedOnDate != null) {
			localDate = new LocalDate(this.rejectedOnDate);
		}
		
		return localDate;
	}
	
	/** 
	 * @return the recalculate interest option (true/false) 
	 **/
	public Boolean getRecalculateInterest() {
		boolean recalculateInterest = false;
		
		if(this.recalculateInterest != null) {
			recalculateInterest = this.recalculateInterest;
		}
		
		return recalculateInterest;
	}
	
	/** 
	 * @return the user that rejected the request 
	 **/
	public AppUser getRejectedByUser() {
		return this.approvedByUser;
	}
	
	/** 
	 * change the status of the loan reschedule request to approved, also updating the 
	 * approvedByUser and approvedOnDate properties 
	 * 
	 * @param approvedByUser the user who approved the request
	 * @param approvedOnDate the date of the approval
	 * @return void
	 **/
	public void approve(final AppUser approvedByUser, final LocalDate approvedOnDate) {
		
		if(approvedOnDate != null) {
			this.approvedByUser = approvedByUser;
			this.approvedOnDate = approvedOnDate.toDate();
			this.statusEnum = LoanStatus.APPROVED.getValue();
		}
	}
	
	/** 
	 * change the status of the loan reschedule request to rejected, also updating the 
	 * approvedByUser and approvedOnDate properties 
	 * 
	 * @param approvedByUser the user who approved the request
	 * @param approvedOnDate the date of the approval
	 * @return void
	 **/
	public void reject(final AppUser approvedByUser, final LocalDate approvedOnDate) {
		
		if(approvedOnDate != null) {
			this.rejectedByUser = approvedByUser;
			this.rejectedOnDate = approvedOnDate.toDate();
			this.statusEnum = LoanStatus.REJECTED.getValue();
		}
	}
}
