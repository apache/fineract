package org.mifosng.platform.api.data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.joda.time.LocalDate;

/**
 * Immutable data object for deposit accounts.
 */
public class DepositAccountData {

	private final Long id;
	private final String externalId;
	private final EnumOptionData status;	
	private final Long clientId;
	private final String clientName;
	private final Long productId;
	private final String productName;
	
	private final CurrencyData currency;
	private final BigDecimal deposit;
	private final BigDecimal maturityInterestRate;
	
	private final Integer tenureInMonths;
	private final LocalDate projectedCommencementDate;
	private final LocalDate actualCommencementDate;
	private final LocalDate maturedOn;
	private final LocalDate withdrawnonDate;
	private final LocalDate rejectedonDate;
	private final LocalDate closedonDate;
	private final BigDecimal projectedInterestAccrued;
	private final BigDecimal actualInterestAccrued;
	private final BigDecimal projectedMaturityAmount;
	private final BigDecimal actualMaturityAmount;
	
	private final Integer interestCompoundedEvery;
	private final EnumOptionData interestCompoundedEveryPeriodType;
	private final boolean renewalAllowed;
	private final boolean preClosureAllowed;
	private final BigDecimal preClosureInterestRate;
	
	private final List<EnumOptionData> interestCompoundedEveryPeriodTypeOptions;
	private final List<DepositProductLookup> productOptions;
	
	private Collection<DepositAccountTransactionData> transactions;
	
	/*
	 * used when returning account template data but only a clientId is passed, no selected product.
	 */
	public static DepositAccountData createFrom(final Long clientId, final String clientDisplayName) {
		return new DepositAccountData(clientId, clientDisplayName);
	}
	
	private DepositAccountData(
			final Long clientId, 
			final String clientName) {
		this.id=null;
		this.externalId = null;
		this.status = null;
		this.clientId = clientId;
		this.clientName = clientName;
		this.productId = null;
		this.productName = null;
		this.currency = null;
		this.deposit = null;
		this.maturityInterestRate=null;
		this.tenureInMonths = null;
		this.projectedCommencementDate = null;
		this.actualCommencementDate = null;
		this.maturedOn = null;
		this.projectedInterestAccrued = null;
		this.actualInterestAccrued = null;
		this.projectedMaturityAmount = null;
		this.actualMaturityAmount = null;
		this.interestCompoundedEvery = null;
		this.interestCompoundedEveryPeriodType = null;
		this.renewalAllowed = false;
		this.preClosureAllowed = false;
		this.preClosureInterestRate = null;
		
		this.interestCompoundedEveryPeriodTypeOptions = new ArrayList<EnumOptionData>();
		this.productOptions = new ArrayList<DepositProductLookup>();
		
		this.withdrawnonDate=null;
		this.closedonDate=null;
		this.rejectedonDate=null;
		
		this.transactions=null;
	}

	public DepositAccountData(
			final DepositAccountData account, 
			final List<EnumOptionData> interestCompoundedEveryPeriodTypeOptions,
			final Collection<DepositProductLookup> productOptions) {
		this.id = account.getId();
		this.externalId = account.getExternalId();
		this.status = account.getStatus();
		this.clientId = account.getClientId();
		this.clientName = account.getClientName();
		this.productId = account.getProductId();
		this.productName = account.getProductName();
		this.currency = account.getCurrency();
		this.deposit = account.getDeposit();
		this.maturityInterestRate = account.getMaturityInterestRate();
		this.tenureInMonths = account.getTenureInMonths();
		this.projectedCommencementDate = account.getProjectedCommencementDate();
		this.actualCommencementDate = account.getActualCommencementDate();
		this.maturedOn = account.getMaturedOn();
		this.projectedInterestAccrued = account.getProjectedInterestAccrued();
		this.actualInterestAccrued = account.getActualInterestAccrued();
		this.projectedMaturityAmount = account.getProjectedMaturityAmount();
		this.actualMaturityAmount = account.getActualMaturityAmount();
		this.interestCompoundedEvery = account.getInterestCompoundedEvery();
		this.interestCompoundedEveryPeriodType = account.getInterestCompoundedEveryPeriodType();
		this.renewalAllowed = account.isRenewalAllowed();
		this.preClosureAllowed = account.isPreClosureAllowed();
		this.preClosureInterestRate = account.getPreClosureInterestRate();
		
		this.interestCompoundedEveryPeriodTypeOptions = interestCompoundedEveryPeriodTypeOptions;
		this.productOptions = new ArrayList<DepositProductLookup>(productOptions);
		
		this.withdrawnonDate=account.getWithdrawnonDate();
		this.rejectedonDate=account.getRejectedonDate();
		this.closedonDate=account.getClosedonDate();
		
		this.transactions=account.getTransactions();
	}
	
	public DepositAccountData(
			final Long id,
			final String externalId,
			final EnumOptionData status,
			final Long clientId, 
			final String clientName, 
			final Long productId, 
			final String productName, 
			final CurrencyData currency,
			final BigDecimal deposit, final BigDecimal interestRate, 
			final Integer tenureInMonths, 
			final LocalDate projectedCommencementDate, 
			final LocalDate actualCommencementDate, 
			final LocalDate maturedOn, 
			final BigDecimal projectedInterestAccrued, 
			final BigDecimal actualInterestAccrued, 
			final BigDecimal projectedMaturityAmount, 
			final BigDecimal actualMaturityAmount,
			final Integer interestCompoundedEvery, 
			final EnumOptionData interestCompoundedEveryPeriodType, 
			final boolean renewalAllowed, 
			final boolean preClosureAllowed, 
			final BigDecimal preClosureInterestRate,
			final LocalDate withdrawnonDate,
			final LocalDate rejectedonDate,
			final LocalDate closedonDate) {
		this.id=id;
		this.externalId = externalId;
		this.status = status;
		this.clientId = clientId;
		this.clientName = clientName;
		this.productId = productId;
		this.productName = productName;
		this.currency = currency;
		this.deposit = deposit;
		this.maturityInterestRate=interestRate;
		this.tenureInMonths = tenureInMonths;
		this.projectedCommencementDate = projectedCommencementDate;
		this.actualCommencementDate = actualCommencementDate;
		this.maturedOn = maturedOn;
		this.projectedInterestAccrued = projectedInterestAccrued;
		this.actualInterestAccrued = actualInterestAccrued;
		this.projectedMaturityAmount = projectedMaturityAmount;
		this.actualMaturityAmount = actualMaturityAmount;
		this.interestCompoundedEvery = interestCompoundedEvery;
		this.interestCompoundedEveryPeriodType = interestCompoundedEveryPeriodType;
		this.renewalAllowed = renewalAllowed;
		this.preClosureAllowed = preClosureAllowed;
		this.preClosureInterestRate = preClosureInterestRate;
		
		this.interestCompoundedEveryPeriodTypeOptions = new ArrayList<EnumOptionData>();
		this.productOptions = new ArrayList<DepositProductLookup>();
		
		this.withdrawnonDate = withdrawnonDate;
		this.rejectedonDate = rejectedonDate;
		this.closedonDate = closedonDate;
		
		this.transactions = null;
	}
	
	public DepositAccountData(
			final Long clientId, 
			final String clientName, 
			final Long productId, 
			final String productName, 
			final CurrencyData currency,
			final BigDecimal deposit, final BigDecimal interestRate, 
			final Integer tenureInMonths, 
			final Integer interestCompoundedEvery, 
			final EnumOptionData interestCompoundedEveryPeriodType, 
			final boolean renewalAllowed, 
			final boolean preClosureAllowed, 
			final BigDecimal preClosureInterestRate) {
		this.id=null;
		this.externalId = null;
		this.status = null;
		this.clientId = clientId;
		this.clientName = clientName;
		this.productId = productId;
		this.productName = productName;
		this.currency = currency;
		this.deposit = deposit;
		this.maturityInterestRate=interestRate;
		this.tenureInMonths = tenureInMonths;
		this.projectedCommencementDate = null;
		this.actualCommencementDate = null;
		this.maturedOn = null;
		this.projectedInterestAccrued = null;
		this.actualInterestAccrued = null;
		this.projectedMaturityAmount = null;
		this.actualMaturityAmount = null;
		this.interestCompoundedEvery = interestCompoundedEvery;
		this.interestCompoundedEveryPeriodType = interestCompoundedEveryPeriodType;
		this.renewalAllowed = renewalAllowed;
		this.preClosureAllowed = preClosureAllowed;
		this.preClosureInterestRate = preClosureInterestRate;
		
		this.interestCompoundedEveryPeriodTypeOptions = new ArrayList<EnumOptionData>();
		this.productOptions = new ArrayList<DepositProductLookup>();
		
		this.withdrawnonDate=null;
		this.closedonDate=null;
		this.rejectedonDate=null;
		
		this.transactions = null;
	}

	public Long getId() {
		return id;
	}
	
	public String getExternalId() {
		return externalId;
	}
	
	public EnumOptionData getStatus() {
		return status;
	}

	public Long getClientId() {
		return clientId;
	}

	public String getClientName() {
		return clientName;
	}

	public Long getProductId() {
		return productId;
	}

	public String getProductName() {
		return productName;
	}

	public CurrencyData getCurrency() {
		return currency;
	}

	public BigDecimal getDeposit() {
		return deposit;
	}
	
	public BigDecimal getMaturityInterestRate() {
		return maturityInterestRate;
	}

	public Integer getTenureInMonths() {
		return tenureInMonths;
	}

	public LocalDate getProjectedCommencementDate() {
		return projectedCommencementDate;
	}

	public LocalDate getActualCommencementDate() {
		return actualCommencementDate;
	}

	public LocalDate getMaturedOn() {
		return maturedOn;
	}

	public BigDecimal getProjectedInterestAccrued() {
		return projectedInterestAccrued;
	}

	public BigDecimal getActualInterestAccrued() {
		return actualInterestAccrued;
	}

	public BigDecimal getProjectedMaturityAmount() {
		return projectedMaturityAmount;
	}

	public BigDecimal getActualMaturityAmount() {
		return actualMaturityAmount;
	}
	
	public Integer getInterestCompoundedEvery() {
		return interestCompoundedEvery;
	}

	public EnumOptionData getInterestCompoundedEveryPeriodType() {
		return interestCompoundedEveryPeriodType;
	}

	public boolean isRenewalAllowed() {
		return renewalAllowed;
	}

	public boolean isPreClosureAllowed() {
		return preClosureAllowed;
	}

	public BigDecimal getPreClosureInterestRate() {
		return preClosureInterestRate;
	}

	public List<EnumOptionData> getInterestCompoundedEveryPeriodTypeOptions() {
		return interestCompoundedEveryPeriodTypeOptions;
	}

	public List<DepositProductLookup> getProductOptions() {
		return productOptions;
	}

	public LocalDate getWithdrawnonDate() {
		return withdrawnonDate;
	}

	public LocalDate getRejectedonDate() {
		return rejectedonDate;
	}

	public LocalDate getClosedonDate() {
		return closedonDate;
	}

	public Collection<DepositAccountTransactionData> getTransactions() {
		return transactions;
	}
	
	public void setTransactions(Collection<DepositAccountTransactionData> transactions) {
		this.transactions = transactions;
	}
	
}