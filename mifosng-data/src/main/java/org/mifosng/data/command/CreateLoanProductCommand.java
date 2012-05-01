package org.mifosng.data.command;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CreateLoanProductCommand implements LoanProductCommandData {

	private String name;
	private String description;
	
	private CommonLoanProperties commonLoanProperties;
	
	public CreateLoanProductCommand() {
		this.commonLoanProperties = new CommonLoanProperties();
	}

	public CreateLoanProductCommand(final String currencyCode,
			final Integer digitsAfterDecimal, final Number principal,
			final String name, final String description,
			final Number interestRatePerPeriod, Integer interestRateFrequencyMethod, 
			final Integer interestMethod, final Integer interestCalculationPeriodMethod,
			final Integer repaymentEvery, final Integer repaymentFrequency, final Integer numberOfRepayments, Integer amortizationMethod, final Number toleranceAmount,
			final boolean flexibleRepaymentSchedule,
			final boolean interestRebateAllowed) {
		this.name = name;
		this.description = description;
		
		commonLoanProperties = new CommonLoanProperties(currencyCode, digitsAfterDecimal, principal, interestRatePerPeriod, interestRateFrequencyMethod, 
				interestMethod, interestCalculationPeriodMethod,
				repaymentEvery, repaymentFrequency, numberOfRepayments, amortizationMethod, toleranceAmount, flexibleRepaymentSchedule, interestRebateAllowed);
	}
	
	/* (non-Javadoc)
	 * @see org.mifosng.data.command.LoanProductCommandData#getName()
	 */
	@Override
	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see org.mifosng.data.command.LoanProductCommandData#getDescription()
	 */
	@Override
	public String getDescription() {
		return this.description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	/* (non-Javadoc)
	 * @see org.mifosng.data.command.LoanProductCommandData#getCurrencyCode()
	 */
	@Override
	public String getCurrencyCode() {
		return this.commonLoanProperties.getCurrencyCode();
	}

	public void setCurrencyCode(final String currencyCode) {
		this.commonLoanProperties.setCurrencyCode(currencyCode);
	}

	/* (non-Javadoc)
	 * @see org.mifosng.data.command.LoanProductCommandData#getDigitsAfterDecimal()
	 */
	@Override
	public Integer getDigitsAfterDecimal() {
		return this.commonLoanProperties.getDigitsAfterDecimal();
	}

	public void setDigitsAfterDecimal(final Integer digitsAfterDecimal) {
		this.commonLoanProperties.setDigitsAfterDecimal(digitsAfterDecimal);
	}

	/* (non-Javadoc)
	 * @see org.mifosng.data.command.LoanProductCommandData#getRepaymentEvery()
	 */
	@Override
	public Integer getRepaymentEvery() {
		return this.commonLoanProperties.getRepaymentEvery();
	}

	public void setRepaymentEvery(final Integer repaymentEvery) {
		this.commonLoanProperties.setRepaymentEvery(repaymentEvery);
	}

	/* (non-Javadoc)
	 * @see org.mifosng.data.command.LoanProductCommandData#getRepaymentFrequency()
	 */
	@Override
	public Integer getRepaymentFrequency() {
		return this.commonLoanProperties.getRepaymentFrequency();
	}

	public void setRepaymentFrequency(final Integer repaymentFrequency) {
		this.commonLoanProperties.setRepaymentFrequency(repaymentFrequency);
	}

	/* (non-Javadoc)
	 * @see org.mifosng.data.command.LoanProductCommandData#isFlexibleRepaymentSchedule()
	 */
	@Override
	public Boolean isFlexibleRepaymentSchedule() {
		return this.commonLoanProperties.isFlexibleRepaymentSchedule();
	}

	public void setFlexibleRepaymentSchedule(
			final Boolean flexibleRepaymentSchedule) {
		this.commonLoanProperties.setFlexibleRepaymentSchedule(flexibleRepaymentSchedule);
	}

	/* (non-Javadoc)
	 * @see org.mifosng.data.command.LoanProductCommandData#isInterestRebateAllowed()
	 */
	@Override
	public Boolean isInterestRebateAllowed() {
		return this.commonLoanProperties.isInterestRebateAllowed();
	}

	public void setInterestRebateAllowed(final Boolean interestRebateAllowed) {
		this.commonLoanProperties.setInterestRebateAllowed(interestRebateAllowed);
	}

	/* (non-Javadoc)
	 * @see org.mifosng.data.command.LoanProductCommandData#getPrincipal()
	 */
	@Override
	public BigDecimal getPrincipal() {
		return this.commonLoanProperties.getPrincipal();
	}

	public void setPrincipal(final BigDecimal principal) {
		this.commonLoanProperties.setPrincipal(principal);
	}

	/* (non-Javadoc)
	 * @see org.mifosng.data.command.LoanProductCommandData#getFlexibleRepaymentSchedule()
	 */
	@Override
	public Boolean getFlexibleRepaymentSchedule() {
		return this.commonLoanProperties.getFlexibleRepaymentSchedule();
	}

	/* (non-Javadoc)
	 * @see org.mifosng.data.command.LoanProductCommandData#getInterestRebateAllowed()
	 */
	@Override
	public Boolean getInterestRebateAllowed() {
		return this.commonLoanProperties.getInterestRebateAllowed();
	}

	/* (non-Javadoc)
	 * @see org.mifosng.data.command.LoanProductCommandData#getInterestRateFrequencyMethod()
	 */
	@Override
	public Integer getInterestRateFrequencyMethod() {
		return commonLoanProperties.getInterestRateFrequencyMethod();
	}

	public void setInterestRateFrequencyMethod(Integer interestRateFrequencyMethod) {
		this.commonLoanProperties.setInterestRateFrequencyMethod(interestRateFrequencyMethod);
	}

	/* (non-Javadoc)
	 * @see org.mifosng.data.command.LoanProductCommandData#getInterestMethod()
	 */
	@Override
	public Integer getInterestMethod() {
		return commonLoanProperties.getInterestMethod();
	}

	public void setInterestMethod(Integer interestMethod) {
		this.commonLoanProperties.setInterestMethod(interestMethod);
	}

	/* (non-Javadoc)
	 * @see org.mifosng.data.command.LoanProductCommandData#getAmortizationMethod()
	 */
	@Override
	public Integer getAmortizationMethod() {
		return commonLoanProperties.getAmortizationMethod();
	}

	public void setAmortizationMethod(Integer amortizationMethod) {
		this.commonLoanProperties.setAmortizationMethod(amortizationMethod);
	}

	/* (non-Javadoc)
	 * @see org.mifosng.data.command.LoanProductCommandData#getNumberOfRepayments()
	 */
	@Override
	public Integer getNumberOfRepayments() {
		return commonLoanProperties.getNumberOfRepayments();
	}

	public void setNumberOfRepayments(Integer numberOfRepayments) {
		this.commonLoanProperties.setNumberOfRepayments(numberOfRepayments);
	}

	/* (non-Javadoc)
	 * @see org.mifosng.data.command.LoanProductCommandData#getInterestRatePerPeriod()
	 */
	@Override
	public BigDecimal getInterestRatePerPeriod() {
		return commonLoanProperties.getInterestRatePerPeriod();
	}

	public void setInterestRatePerPeriod(BigDecimal interestRatePerPeriod) {
		this.commonLoanProperties.setInterestRatePerPeriod(interestRatePerPeriod);
	}

	/* (non-Javadoc)
	 * @see org.mifosng.data.command.LoanProductCommandData#getCommonLoanProperties()
	 */
	@Override
	public CommonLoanProperties getCommonLoanProperties() {
		return commonLoanProperties;
	}

	public void setCommonLoanProperties(CommonLoanProperties commonLoanProperties) {
		this.commonLoanProperties = commonLoanProperties;
	}
	
	/* (non-Javadoc)
	 * @see org.mifosng.data.command.LoanProductCommandData#getInArrearsToleranceAmount()
	 */
	@Override
	public BigDecimal getInArrearsToleranceAmount() {
		return this.commonLoanProperties.getInArrearsToleranceAmount();
	}

	public void setInArrearsToleranceAmount(BigDecimal inArrearsToleranceAmount) {
		this.commonLoanProperties.setInArrearsToleranceAmount(inArrearsToleranceAmount);
	}
	
	@Override
	public Integer getInterestCalculationPeriodMethod() {
		return this.commonLoanProperties.getInterestCalculationPeriodMethod();
	}

	public void setInterestCalculationPeriodMethod(final Integer interestCalculationPeriodMethod) {
		this.commonLoanProperties.setInterestCalculationPeriodMethod(interestCalculationPeriodMethod);
	}
}