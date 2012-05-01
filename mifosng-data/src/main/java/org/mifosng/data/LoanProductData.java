package org.mifosng.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.DateTime;

@XmlRootElement(name = "loanProduct")
public class LoanProductData implements Serializable {

	private Long id;
	private String name;
	private String description;
	private boolean isFlexible;
	private boolean isInterestRebateAllowed;
	private MoneyData principalMoney;
	private BigDecimal interestRatePerPeriod = BigDecimal.ZERO;
	private int interestRatePeriod;
	private BigDecimal annualInterestRate = BigDecimal.ZERO;
	private int interestMethod;
	private int interestRateCalculatedInPeriod;
	
	private Integer numberOfInterestFreePeriods = Integer.valueOf(0);
	private Integer repaidEvery;
	private int repaymentPeriodFrequency;
	private Integer numberOfRepayments;
	private int amortizationMethod;
	private MoneyData inArrearsTolerance;
	private DateTime createdOn;
	private DateTime lastModifedOn;
	
	private List<CurrencyData> possibleCurrencies = new ArrayList<CurrencyData>();
	private List<EnumOptionReadModel> possibleAmortizationOptions = new ArrayList<EnumOptionReadModel>();
	private List<EnumOptionReadModel> possibleInterestOptions = new ArrayList<EnumOptionReadModel>();
	private List<EnumOptionReadModel> possibleInterestRateCalculatedInPeriodOptions = new ArrayList<EnumOptionReadModel>();
	private List<EnumOptionReadModel> repaymentFrequencyOptions = new ArrayList<EnumOptionReadModel>();
	private List<EnumOptionReadModel> interestFrequencyOptions = new ArrayList<EnumOptionReadModel>();
	
	public LoanProductData() {
		//
	}

	public LoanProductData(final Long id, final String name,
			String description, final boolean isFlexible,
			final boolean isInterestRebateAllowed,
			final MoneyData principalMoney,
			final BigDecimal interestRatePerPeriod,
			final int interestRatePeriod, final BigDecimal annualInterestRate,
			final int interestMethod, final int interestCalculationInPeriodMethod,
			final Integer repaidEvery,
			final int repaymentPeriodFrequency, final Integer numberOfRepayments,
			final int amortizationMethod, MoneyData inArrearsTolerance, DateTime createdOn, DateTime lastModifedOn) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.isFlexible = isFlexible;
		this.isInterestRebateAllowed = isInterestRebateAllowed;
		this.principalMoney = principalMoney;
		this.interestRatePerPeriod = interestRatePerPeriod;
		this.interestRatePeriod = interestRatePeriod;
		this.annualInterestRate = annualInterestRate;
		this.interestMethod = interestMethod;
		this.interestRateCalculatedInPeriod = interestCalculationInPeriodMethod;
		this.numberOfRepayments = numberOfRepayments;
		this.repaidEvery = repaidEvery;
		this.repaymentPeriodFrequency = repaymentPeriodFrequency;
		this.amortizationMethod = amortizationMethod;
		this.inArrearsTolerance = inArrearsTolerance;
		this.createdOn = createdOn;
		this.lastModifedOn = lastModifedOn;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isFlexible() {
		return isFlexible;
	}

	public void setFlexible(boolean isFlexible) {
		this.isFlexible = isFlexible;
	}

	public boolean isInterestRebateAllowed() {
		return isInterestRebateAllowed;
	}

	public void setInterestRebateAllowed(boolean isInterestRebateAllowed) {
		this.isInterestRebateAllowed = isInterestRebateAllowed;
	}

	public MoneyData getPrincipalMoney() {
		return principalMoney;
	}

	public void setPrincipalMoney(MoneyData principalMoney) {
		this.principalMoney = principalMoney;
	}

	public BigDecimal getInterestRatePerPeriod() {
		return interestRatePerPeriod;
	}

	public void setInterestRatePerPeriod(BigDecimal interestRatePerPeriod) {
		this.interestRatePerPeriod = interestRatePerPeriod;
	}

	public int getInterestRatePeriod() {
		return interestRatePeriod;
	}

	public void setInterestRatePeriod(int interestRatePeriod) {
		this.interestRatePeriod = interestRatePeriod;
	}

	public BigDecimal getAnnualInterestRate() {
		return annualInterestRate;
	}

	public void setAnnualInterestRate(BigDecimal annualInterestRate) {
		this.annualInterestRate = annualInterestRate;
	}

	public int getInterestMethod() {
		return interestMethod;
	}

	public void setInterestMethod(int interestMethod) {
		this.interestMethod = interestMethod;
	}

	public Integer getRepaidEvery() {
		return repaidEvery;
	}

	public void setRepaidEvery(Integer repaidEvery) {
		this.repaidEvery = repaidEvery;
	}

	public int getRepaymentPeriodFrequency() {
		return repaymentPeriodFrequency;
	}

	public void setRepaymentPeriodFrequency(int repaymentPeriodFrequency) {
		this.repaymentPeriodFrequency = repaymentPeriodFrequency;
	}

	public Integer getNumberOfRepayments() {
		return numberOfRepayments;
	}

	public void setNumberOfRepayments(Integer numberOfRepayments) {
		this.numberOfRepayments = numberOfRepayments;
	}

	public int getAmortizationMethod() {
		return amortizationMethod;
	}

	public void setAmortizationMethod(int amortizationMethod) {
		this.amortizationMethod = amortizationMethod;
	}

	public MoneyData getInArrearsTolerance() {
		return inArrearsTolerance;
	}

	public void setInArrearsTolerance(MoneyData inArrearsTolerance) {
		this.inArrearsTolerance = inArrearsTolerance;
	}

	public DateTime getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(DateTime createdOn) {
		this.createdOn = createdOn;
	}

	public DateTime getLastModifedOn() {
		return lastModifedOn;
	}

	public void setLastModifedOn(DateTime lastModifedOn) {
		this.lastModifedOn = lastModifedOn;
	}

	public List<CurrencyData> getPossibleCurrencies() {
		return possibleCurrencies;
	}

	public void setPossibleCurrencies(List<CurrencyData> possibleCurrencies) {
		this.possibleCurrencies = possibleCurrencies;
	}

	public List<EnumOptionReadModel> getPossibleAmortizationOptions() {
		return possibleAmortizationOptions;
	}

	public void setPossibleAmortizationOptions(
			List<EnumOptionReadModel> possibleAmortizationOptions) {
		this.possibleAmortizationOptions = possibleAmortizationOptions;
	}

	public List<EnumOptionReadModel> getPossibleInterestOptions() {
		return possibleInterestOptions;
	}

	public void setPossibleInterestOptions(
			List<EnumOptionReadModel> possibleInterestOptions) {
		this.possibleInterestOptions = possibleInterestOptions;
	}

	public List<EnumOptionReadModel> getRepaymentFrequencyOptions() {
		return repaymentFrequencyOptions;
	}

	public void setRepaymentFrequencyOptions(
			List<EnumOptionReadModel> repaymentFrequencyOptions) {
		this.repaymentFrequencyOptions = repaymentFrequencyOptions;
	}

	public List<EnumOptionReadModel> getInterestFrequencyOptions() {
		return interestFrequencyOptions;
	}

	public void setInterestFrequencyOptions(
			List<EnumOptionReadModel> interestFrequencyOptions) {
		this.interestFrequencyOptions = interestFrequencyOptions;
	}

	public Integer getNumberOfInterestFreePeriods() {
		return numberOfInterestFreePeriods;
	}

	public void setNumberOfInterestFreePeriods(Integer numberOfInterestFreePeriods) {
		this.numberOfInterestFreePeriods = numberOfInterestFreePeriods;
	}

	public int getInterestRateCalculatedInPeriod() {
		return interestRateCalculatedInPeriod;
	}

	public void setInterestRateCalculatedInPeriod(int interestRateCalculatedInPeriod) {
		this.interestRateCalculatedInPeriod = interestRateCalculatedInPeriod;
	}

	public List<EnumOptionReadModel> getPossibleInterestRateCalculatedInPeriodOptions() {
		return possibleInterestRateCalculatedInPeriodOptions;
	}

	public void setPossibleInterestRateCalculatedInPeriodOptions(
			List<EnumOptionReadModel> possibleInterestRateCalculatedInPeriodOptions) {
		this.possibleInterestRateCalculatedInPeriodOptions = possibleInterestRateCalculatedInPeriodOptions;
	}
}