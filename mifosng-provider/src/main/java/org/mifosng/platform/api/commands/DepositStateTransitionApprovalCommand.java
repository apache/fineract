package org.mifosng.platform.api.commands;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

public class DepositStateTransitionApprovalCommand {
	
	private final Long accountId;
	private final Long productId;
	private final LocalDate eventDate;
	private final BigDecimal depositAmount;
	private final Integer tenureInMonths;
	private final Integer interestCompoundedEveryPeriodType;
	private final Integer interestCompoundedEvery;
	
	private final String note;

	public DepositStateTransitionApprovalCommand(Long resourceIdentifier, Long productId, LocalDate eventDate, Integer tenureInMonths, BigDecimal depositAmount, Integer interestCompoundedEveryPeriodType, Integer interestCompoundedEvery, String note) {
		
		this.accountId=resourceIdentifier;
		this.eventDate=eventDate;
		this.depositAmount=depositAmount;
		this.tenureInMonths=tenureInMonths;
		this.interestCompoundedEveryPeriodType=interestCompoundedEveryPeriodType;
		this.productId=productId;
		this.interestCompoundedEvery=interestCompoundedEvery;
		
		this.note = note;
		
	}

	public Long getAccountId() {
		return accountId;
	}

	public LocalDate getEventDate() {
		return eventDate;
	}

	public BigDecimal getDepositAmount() {
		return depositAmount;
	}

	public Integer getTenureInMonths() {
		return tenureInMonths;
	}

	public Integer getInterestCompoundedEveryPeriodType() {
		return interestCompoundedEveryPeriodType;
	}

	public Long getProductId() {
		return productId;
	}

	public Integer getInterestCompoundedEvery() {
		return interestCompoundedEvery;
	}

	public String getNote() {
		return note;
	}
	
}
