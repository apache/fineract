package org.mifosplatform.portfolio.savingsaccount.command;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

public class SavingAccountApprovalCommand {
	
	private final Long accountId;
    private final LocalDate approvalDate;
    private final BigDecimal depositAmountPerPeriod;
    private final BigDecimal minimumBalanceForWithdrawal;
    private final BigDecimal recurringInterestRate;
    private final BigDecimal savingInterestRate;
    private final Integer interestType;
    private final Integer tenure;
    private final Integer tenureType;
    private final Integer depositFrequencyType;
    private final Integer depositEvery;
    
    private final Integer interestPostEvery;
    private final Integer interestPostFrequency;
    
    private final String note;
    
    public SavingAccountApprovalCommand(final Long accountId, final LocalDate approvalDate,
    		final BigDecimal depositAmountPerPeriod, final BigDecimal minimumBalanceForWithdrawal, final BigDecimal recurringInterestRate,
    		final BigDecimal savingInterestRate, final Integer interestType, final Integer tenure, final Integer tenureType,
    		final Integer depositFrequencyType, final Integer depositEvery, final String note, 
    		final Integer interestPostEvery, final Integer interestPostFrequency) {
    	this.accountId = accountId;
    	this.approvalDate = approvalDate;
    	this.depositAmountPerPeriod = depositAmountPerPeriod;
    	this.minimumBalanceForWithdrawal = minimumBalanceForWithdrawal;
    	this.recurringInterestRate = recurringInterestRate;
    	this.savingInterestRate = savingInterestRate;
    	this.interestType = interestType;
    	this.tenure = tenure;
    	this.tenureType = tenureType;
    	this.depositFrequencyType = depositFrequencyType;
    	this.depositEvery = depositEvery;
    	this.note = note;
    	
    	this.interestPostEvery = interestPostEvery;
    	this.interestPostFrequency = interestPostFrequency;
	}

	public Long getAccountId() {
		return this.accountId;
	}

	public LocalDate getApprovalDate() {
		return this.approvalDate;
	}

	public BigDecimal getDepositAmountPerPeriod() {
		return this.depositAmountPerPeriod;
	}

	public BigDecimal getMinimumBalanceForWithdrawal() {
		return this.minimumBalanceForWithdrawal;
	}

	public BigDecimal getRecurringInterestRate() {
		return this.recurringInterestRate;
	}

	public BigDecimal getSavingInterestRate() {
		return this.savingInterestRate;
	}

	public Integer getInterestType() {
		return this.interestType;
	}

	public Integer getTenure() {
		return this.tenure;
	}

	public Integer getTenureType() {
		return this.tenureType;
	}

	public Integer getDepositFrequencyType() {
		return this.depositFrequencyType;
	}

	public Integer getDepositEvery() {
		return this.depositEvery;
	}

	public String getNote() {
		return this.note;
	}

	public Integer getInterestPostEvery() {
		return this.interestPostEvery;
	}

	public Integer getInterestPostFrequency() {
		return this.interestPostFrequency;
	}

}
