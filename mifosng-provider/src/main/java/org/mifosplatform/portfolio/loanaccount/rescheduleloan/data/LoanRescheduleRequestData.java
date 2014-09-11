/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.rescheduleloan.data;

import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.codes.data.CodeValueData;

/** 
 * Immutable data object representing loan reschedule request data. 
 **/
public class LoanRescheduleRequestData {
	
	private final Long id;
	private final Long loanId;
	private final Long clientId;
	private final String clientName;
	private final String loanAccountNumber;
	private final LoanRescheduleRequestStatusEnumData statusEnum;
	private final Integer rescheduleFromInstallment;
	private final Integer graceOnPrincipal;
	private final Integer graceOnInterest;
	private final LocalDate rescheduleFromDate;
	private final Integer extraTerms;
	private final BigDecimal interestRate;
	private final Boolean recalculateInterest;
	private final CodeValueData rescheduleReasonCodeValue;
	private final LoanRescheduleRequestTimelineData timeline;
	private final String rescheduleReasonComment;
	private final LocalDate adjustedDueDate;
	
	/** 
	 * LoanRescheduleRequestData constructor
	 **/
	private LoanRescheduleRequestData(Long id, Long loanId, LoanRescheduleRequestStatusEnumData statusEnum, 
			Integer rescheduleFromInstallment, Integer graceOnPrincipal, Integer graceOnInterest, 
			LocalDate rescheduleFromDate, LocalDate adjustedDueDate, Integer extraTerms, BigDecimal interestRate, 
			CodeValueData rescheduleReasonCodeValue, String rescheduleReasonComment, LoanRescheduleRequestTimelineData timeline, 
			final String clientName, final String loanAccountNumber, final Long clientId, final Boolean recalculateInterest) {
		
		this.id = id;
		this.loanId = loanId;
		this.statusEnum = statusEnum;
		this.rescheduleFromInstallment = rescheduleFromInstallment;
		this.graceOnPrincipal = graceOnPrincipal;
		this.graceOnInterest = graceOnInterest;
		this.rescheduleFromDate = rescheduleFromDate;
		this.extraTerms = extraTerms;
		this.interestRate = interestRate;
		this.rescheduleReasonCodeValue = rescheduleReasonCodeValue;
		this.rescheduleReasonComment = rescheduleReasonComment;
		this.adjustedDueDate = adjustedDueDate;
		this.timeline = timeline;
		this.clientName = clientName;
		this.loanAccountNumber = loanAccountNumber;
		this.clientId = clientId;
		this.recalculateInterest = recalculateInterest;
	}
	
	/** 
	 * @return an instance of the LoanRescheduleRequestData class 
	 **/
	public static LoanRescheduleRequestData instance(Long id, Long loanId, LoanRescheduleRequestStatusEnumData statusEnum, 
			Integer rescheduleFromInstallment, Integer graceOnPrincipal, Integer graceOnInterest, 
			LocalDate rescheduleFromDate, LocalDate adjustedDueDate, Integer extraTerms, BigDecimal interestRate, 
			CodeValueData rescheduleReasonCodeValue, String rescheduleReasonComment, LoanRescheduleRequestTimelineData timeline, 
			final String clientName, final String loanAccountNumber, final Long clientId, final Boolean recalculateInterest) {
		
		return new LoanRescheduleRequestData(id, loanId, statusEnum, rescheduleFromInstallment, graceOnPrincipal, graceOnInterest, 
				rescheduleFromDate, adjustedDueDate, extraTerms, interestRate, rescheduleReasonCodeValue, rescheduleReasonComment, 
				timeline, clientName, loanAccountNumber, clientId, recalculateInterest);
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @return the loanId
	 */
	public Long getLoanId() {
		return loanId;
	}

	/**
	 * @return the statusEnum
	 */
	public LoanRescheduleRequestStatusEnumData getStatusEnum() {
		return statusEnum;
	}

	/**
	 * @return the reschedule from installment number
	 */
	public Integer getRescheduleFromInstallment() {
		return rescheduleFromInstallment;
	}

	/**
	 * @return the graceOnPrincipal
	 */
	public Integer getGraceOnPrincipal() {
		return graceOnPrincipal;
	}

	/**
	 * @return the graceOnInterest
	 */
	public Integer getGraceOnInterest() {
		return graceOnInterest;
	}

	/**
	 * @return the reschedule from date
	 */
	public LocalDate getRescheduleFromDate() {
		return rescheduleFromDate;
	}

	/**
	 * @return the extraTerms
	 */
	public Integer getExtraTerms() {
		return extraTerms;
	}

	/**
	 * @return the interestRate
	 */
	public BigDecimal getInterestRate() {
		return interestRate;
	}

	/**
	 * @return the rescheduleReasonCodeValueId
	 */
	public CodeValueData getRescheduleReasonCodeValueId() {
		return rescheduleReasonCodeValue;
	}

	/**
	 * @return the rescheduleReasonText
	 */
	public String getRescheduleReasonComment() {
		return rescheduleReasonComment;
	}
	
	/** 
	 * @return the timeline 
	 **/
	public LoanRescheduleRequestTimelineData getTimeline() {
		return this.timeline;
	}

	/**
	 * @return the adjustedDueDate
	 */
	public LocalDate getAdjustedDueDate() {
		return adjustedDueDate;
	}

	/**
	 * @return the clientName
	 */
	public String getClientName() {
		return clientName;
	}

	/**
	 * @return the loanAccountNumber
	 */
	public String getLoanAccountNumber() {
		return loanAccountNumber;
	}

	/**
	 * @return the clientId
	 */
	public Long getClientId() {
		return clientId;
	}

	/**
	 * @return the recalculateInterest
	 */
	public Boolean getRecalculateInterest() {
		boolean value = false;
		
		if(recalculateInterest != null) {
			value = recalculateInterest;
		}
		
		return value;
	}
}
