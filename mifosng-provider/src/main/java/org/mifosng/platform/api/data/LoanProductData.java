package org.mifosng.platform.api.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.map.annotate.JsonFilter;
import org.joda.time.DateTime;

@JsonFilter("myFilter")
public class LoanProductData implements Serializable {

	private Long id;
	private String name;
	private String description;
	
	private MoneyData principal;
	private MoneyData inArrearsTolerance;
	
	private Integer numberOfRepayments;	
	private Integer repaymentEvery;
	private BigDecimal interestRatePerPeriod = BigDecimal.ZERO;
	private BigDecimal annualInterestRate = BigDecimal.ZERO;

	private EnumOptionData repaymentFrequencyType;
	private EnumOptionData interestRateFrequencyType;
	private EnumOptionData amortizationType;
	private EnumOptionData interestType;
	private EnumOptionData interestCalculationPeriodType;
	
	private DateTime createdOn;
	private DateTime lastModifedOn;
	
	private List<CurrencyData> currencyOptions = new ArrayList<CurrencyData>();
	private List<EnumOptionData> amortizationTypeOptions = new ArrayList<EnumOptionData>();
	private List<EnumOptionData> interestTypeOptions = new ArrayList<EnumOptionData>();
	private List<EnumOptionData> interestCalculationPeriodTypeOptions = new ArrayList<EnumOptionData>();
	private List<EnumOptionData> repaymentFrequencyTypeOptions = new ArrayList<EnumOptionData>();
	private List<EnumOptionData> interestRateFrequencyTypeOptions = new ArrayList<EnumOptionData>();
	
	public LoanProductData() {
		//
	}

	public LoanProductData(DateTime createdOn, DateTime lastModifedOn, Long id,
			String name, String description, MoneyData principal,
			MoneyData tolerance, Integer numberOfRepayments,
			Integer repaymentEvery, BigDecimal interestRatePerPeriod,
			BigDecimal annualInterestRate,
			EnumOptionData repaymentFrequencyType,
			EnumOptionData interestRateFrequencyType,
			EnumOptionData amortizationType, 
			EnumOptionData interestType,
			EnumOptionData interestCalculationPeriodType) {
		this.createdOn = createdOn;
		this.lastModifedOn = lastModifedOn;
		this.id = id;
		this.name = name;
		this.description = description;
		this.principal = principal;
		this.inArrearsTolerance = tolerance;
		this.numberOfRepayments = numberOfRepayments;
		this.repaymentEvery = repaymentEvery;
		this.interestRatePerPeriod = interestRatePerPeriod;
		this.annualInterestRate = annualInterestRate;
		this.repaymentFrequencyType = repaymentFrequencyType;
		this.interestRateFrequencyType = interestRateFrequencyType;
		this.amortizationType = amortizationType;
		this.interestType = interestType;
		this.interestCalculationPeriodType = interestCalculationPeriodType;
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

	public MoneyData getPrincipal() {
		return principal;
	}

	public void setPrincipal(MoneyData principal) {
		this.principal = principal;
	}

	public MoneyData getInArrearsTolerance() {
		return inArrearsTolerance;
	}

	public void setInArrearsTolerance(MoneyData inArrearsTolerance) {
		this.inArrearsTolerance = inArrearsTolerance;
	}

	public Integer getNumberOfRepayments() {
		return numberOfRepayments;
	}

	public void setNumberOfRepayments(Integer numberOfRepayments) {
		this.numberOfRepayments = numberOfRepayments;
	}

	public Integer getRepaymentEvery() {
		return repaymentEvery;
	}

	public void setRepaymentEvery(Integer repaymentEvery) {
		this.repaymentEvery = repaymentEvery;
	}

	public BigDecimal getInterestRatePerPeriod() {
		return interestRatePerPeriod;
	}

	public void setInterestRatePerPeriod(BigDecimal interestRatePerPeriod) {
		this.interestRatePerPeriod = interestRatePerPeriod;
	}

	public BigDecimal getAnnualInterestRate() {
		return annualInterestRate;
	}

	public void setAnnualInterestRate(BigDecimal annualInterestRate) {
		this.annualInterestRate = annualInterestRate;
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

	public EnumOptionData getRepaymentFrequencyType() {
		return repaymentFrequencyType;
	}

	public void setRepaymentFrequencyType(EnumOptionData repaymentFrequencyType) {
		this.repaymentFrequencyType = repaymentFrequencyType;
	}

	public EnumOptionData getInterestRateFrequencyType() {
		return interestRateFrequencyType;
	}

	public void setInterestRateFrequencyType(
			EnumOptionData interestRateFrequencyType) {
		this.interestRateFrequencyType = interestRateFrequencyType;
	}

	public EnumOptionData getAmortizationType() {
		return amortizationType;
	}

	public void setAmortizationType(EnumOptionData amortizationType) {
		this.amortizationType = amortizationType;
	}

	public EnumOptionData getInterestType() {
		return interestType;
	}

	public void setInterestType(EnumOptionData interestType) {
		this.interestType = interestType;
	}

	public EnumOptionData getInterestCalculationPeriodType() {
		return interestCalculationPeriodType;
	}

	public void setInterestCalculationPeriodType(
			EnumOptionData interestCalculationPeriodType) {
		this.interestCalculationPeriodType = interestCalculationPeriodType;
	}

	public List<EnumOptionData> getRepaymentFrequencyTypeOptions() {
		return repaymentFrequencyTypeOptions;
	}

	public void setRepaymentFrequencyTypeOptions(
			List<EnumOptionData> repaymentFrequencyTypeOptions) {
		this.repaymentFrequencyTypeOptions = repaymentFrequencyTypeOptions;
	}

	public List<EnumOptionData> getInterestRateFrequencyTypeOptions() {
		return interestRateFrequencyTypeOptions;
	}

	public void setInterestRateFrequencyTypeOptions(
			List<EnumOptionData> interestRateFrequencyTypeOptions) {
		this.interestRateFrequencyTypeOptions = interestRateFrequencyTypeOptions;
	}

	public List<CurrencyData> getCurrencyOptions() {
		return currencyOptions;
	}

	public void setCurrencyOptions(List<CurrencyData> currencyOptions) {
		this.currencyOptions = currencyOptions;
	}

	public List<EnumOptionData> getAmortizationTypeOptions() {
		return amortizationTypeOptions;
	}

	public void setAmortizationTypeOptions(
			List<EnumOptionData> amortizationTypeOptions) {
		this.amortizationTypeOptions = amortizationTypeOptions;
	}

	public List<EnumOptionData> getInterestTypeOptions() {
		return interestTypeOptions;
	}

	public void setInterestTypeOptions(List<EnumOptionData> interestTypeOptions) {
		this.interestTypeOptions = interestTypeOptions;
	}

	public List<EnumOptionData> getInterestCalculationPeriodTypeOptions() {
		return interestCalculationPeriodTypeOptions;
	}

	public void setInterestCalculationPeriodTypeOptions(
			List<EnumOptionData> interestCalculationPeriodTypeOptions) {
		this.interestCalculationPeriodTypeOptions = interestCalculationPeriodTypeOptions;
	}
}