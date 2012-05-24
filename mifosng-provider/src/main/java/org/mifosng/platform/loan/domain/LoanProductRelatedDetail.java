package org.mifosng.platform.loan.domain;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.mifosng.data.command.LoanProductCommand;
import org.mifosng.platform.currency.domain.MonetaryCurrency;
import org.mifosng.platform.currency.domain.Money;

/**
 * LoanRepaymentScheduleDetail encapsulates all the details of a
 * {@link LoanProduct} that are also used and persisted by a {@link Loan}.
 */
@Embeddable
public class LoanProductRelatedDetail implements
		LoanProductMinimumRepaymentScheduleRelatedDetail {

	@Embedded
	private MonetaryCurrency currency;
	
	@Column(name = "principal_amount", scale = 6, precision = 19, nullable = false)
	private BigDecimal principal;

	@Column(name = "nominal_interest_rate_per_period", scale = 6, precision = 19, nullable = false)
	private BigDecimal nominalInterestRatePerPeriod;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "interest_period_frequency_enum", nullable = false)
	private PeriodFrequencyType interestPeriodFrequencyType;

	@Column(name = "annual_nominal_interest_rate", scale = 6, precision = 19, nullable = false)
	private BigDecimal annualNominalInterestRate;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "interest_method_enum", nullable = false)
	private InterestMethod interestMethod;
	
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "interest_calculated_in_period_enum", nullable = false)
	private InterestCalculationPeriodMethod interestCalculationPeriodMethod;

	@Column(name = "repay_every", nullable = false)
	private Integer repayEvery;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "repayment_period_frequency_enum", nullable = false)
	private PeriodFrequencyType repaymentPeriodFrequencyType;

	@Column(name = "number_of_repayments", nullable = false)
	private Integer numberOfRepayments;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "amortization_method_enum", nullable = false)
	private AmortizationMethod amortizationMethod;

	@Column(name = "flexible_repayment_schedule", nullable = false)
	private boolean flexibleRepaymentSchedule;

	@Column(name = "interest_rebate", nullable = false)
	private boolean interestRebateAllowed;

	@Column(name = "arrearstolerance_amount", scale = 6, precision = 19, nullable = false)
	private BigDecimal inArrearsTolerance;

	protected LoanProductRelatedDetail() {
		this.principal = null;
		this.nominalInterestRatePerPeriod = null;
		this.interestPeriodFrequencyType = null;
		this.annualNominalInterestRate = null;
		this.interestMethod = null;

		this.repayEvery = null;
		this.repaymentPeriodFrequencyType = null;
		this.numberOfRepayments = null;
		this.amortizationMethod = null;
		this.inArrearsTolerance = null;
		
		this.flexibleRepaymentSchedule = false;
		this.interestRebateAllowed = false;
	}

	public LoanProductRelatedDetail(final MonetaryCurrency currency,
			final BigDecimal defaultPrincipal,
			final BigDecimal defaultNominalInterestRatePerPeriod,
			final PeriodFrequencyType interestPeriodFrequencyType,
			final BigDecimal defaultAnnualNominalInterestRate,
			final InterestMethod interestMethod, 
			final InterestCalculationPeriodMethod interestCalculationPeriodMethod,
			final Integer repayEvery,
			final PeriodFrequencyType repaymentFrequencyType,
			final Integer defaultNumberOfRepayments,
			final AmortizationMethod amortizationMethod,
			final BigDecimal inArrearsTolerance) {
		this.currency = currency;
		this.principal = defaultPrincipal;
		this.nominalInterestRatePerPeriod = defaultNominalInterestRatePerPeriod;
		this.interestPeriodFrequencyType = interestPeriodFrequencyType;
		this.annualNominalInterestRate = defaultAnnualNominalInterestRate;
		this.interestMethod = interestMethod;
		this.interestCalculationPeriodMethod = interestCalculationPeriodMethod;
		this.repayEvery = repayEvery;
		this.repaymentPeriodFrequencyType = repaymentFrequencyType;
		this.numberOfRepayments = defaultNumberOfRepayments;
		this.amortizationMethod = amortizationMethod;
		this.inArrearsTolerance = inArrearsTolerance;
		this.flexibleRepaymentSchedule = false;
		this.interestRebateAllowed = false;
	}
	
	public MonetaryCurrency getCurrency() {
		return this.currency.copy();
	}

	public Money getPrincipal() {
		return Money.of(this.currency, this.principal);
	}

	public Money getInArrearsTolerance() {
		return Money.of(this.currency, this.inArrearsTolerance);
	}

	public BigDecimal getNominalInterestRatePerPeriod() {
		return BigDecimal.valueOf(Double.valueOf(this.nominalInterestRatePerPeriod.stripTrailingZeros().toString())) ;
	}

	public PeriodFrequencyType getInterestPeriodFrequencyType() {
		return interestPeriodFrequencyType;
	}

	public BigDecimal getAnnualNominalInterestRate() {
		return BigDecimal.valueOf(Double.valueOf(this.annualNominalInterestRate.stripTrailingZeros().toString())) ;
	}

	public InterestMethod getInterestMethod() {
		return interestMethod;
	}

	public InterestCalculationPeriodMethod getInterestCalculationPeriodMethod() {
		return interestCalculationPeriodMethod;
	}

	@Override
	public Integer getRepayEvery() {
		return repayEvery;
	}

	@Override
	public PeriodFrequencyType getRepaymentPeriodFrequencyType() {
		return repaymentPeriodFrequencyType;
	}

	@Override
	public Integer getNumberOfRepayments() {
		return numberOfRepayments;
	}

	public AmortizationMethod getAmortizationMethod() {
		return amortizationMethod;
	}

	public boolean isFlexibleRepaymentSchedule() {
		return flexibleRepaymentSchedule;
	}

	public boolean isInterestRebateAllowed() {
		return interestRebateAllowed;
	}

	public void update(LoanProductCommand command) {
		
		if (command.getCurrencyCode() != null && command.getDigitsAfterDecimal() != null) {
			this.currency = new MonetaryCurrency(command.getCurrencyCode(), command.getDigitsAfterDecimal());
		}
		if (command.getPrincipal() != null) {
			this.principal = command.getPrincipal();
		}
		if (command.getRepaymentEvery() != null) {
			this.repayEvery = command.getRepaymentEvery();
		}
		if (command.getRepaymentFrequency() != null) {
			this.repaymentPeriodFrequencyType = PeriodFrequencyType.fromInt(command.getRepaymentFrequency());
		}
		if (command.getNumberOfRepayments() != null) {
			this.numberOfRepayments = command.getNumberOfRepayments();
		}
		if (command.getAmortizationMethod() != null) {
			this.amortizationMethod = AmortizationMethod.fromInt(command.getAmortizationMethod());
		}
		if (command.getInArrearsToleranceAmount() != null) {
			this.inArrearsTolerance = command.getInArrearsToleranceAmount();
		}
		if (command.getInterestRatePerPeriod() != null) {
			this.nominalInterestRatePerPeriod = command.getInterestRatePerPeriod();
		}
		if (command.getInterestRateFrequencyMethod() != null) {
			this.interestPeriodFrequencyType = PeriodFrequencyType.fromInt(command.getInterestRateFrequencyMethod());
		}
		if (command.getInterestMethod() != null) {
			this.interestMethod = InterestMethod.fromInt(command.getInterestMethod());
		}
		if (command.getInterestCalculationPeriodMethod() != null) {
			this.interestCalculationPeriodMethod = InterestCalculationPeriodMethod.fromInt(command.getInterestCalculationPeriodMethod());
		}
	}
}