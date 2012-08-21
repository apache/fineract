package org.mifosng.platform.saving.domain;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.joda.time.LocalDate;
import org.mifosng.platform.api.commands.DepositAccountCommand;
import org.mifosng.platform.client.domain.Client;
import org.mifosng.platform.currency.domain.MonetaryCurrency;
import org.mifosng.platform.currency.domain.Money;
import org.mifosng.platform.loan.domain.PeriodFrequencyType;
import org.mifosng.platform.savingproduct.domain.DepositProduct;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_deposit_account", uniqueConstraints = @UniqueConstraint(name="deposit_acc_external_id", columnNames = { "external_id" }))
public class DepositAccount extends AbstractPersistable<Long> {
	
	@SuppressWarnings("unused")
	@ManyToOne
	@JoinColumn(name = "client_id", nullable = false)
	private Client client;

	@SuppressWarnings("unused")
	@ManyToOne
	@JoinColumn(name = "product_id")
	private final DepositProduct product;
	
	@Column(name = "external_id")
	private String externalId;
	
	@SuppressWarnings("unused")
	@Embedded
	private MonetaryCurrency currency;
	
	@Column(name = "deposit_amount", scale = 6, precision = 19, nullable = false)
	private BigDecimal depositAmount;
	
	@SuppressWarnings("unused")
	@Column(name = "maturity_nominal_interest_rate", scale = 6, precision = 19, nullable = false)
	private BigDecimal interestRate;
	
	@Column(name = "tenure_months", nullable=false)
	private Integer tenureInMonths;
	
	@SuppressWarnings("unused")
	@Column(name = "interest_compounded_every", nullable = false)
	private Integer interestCompoundedEvery;

	@SuppressWarnings("unused")
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "interest_compounded_every_period_enum", nullable = false)
	private PeriodFrequencyType interestCompoundedFrequencyType;
	
	@SuppressWarnings("unused")
	@Temporal(TemporalType.DATE)
	@Column(name = "projected_commencement_date")
	private Date projectedCommencementDate;
	
	@SuppressWarnings("unused")
	@Temporal(TemporalType.DATE)
	@Column(name = "actual_commencement_date")
	private Date actualCommencementDate;
	
	@SuppressWarnings("unused")
	@Temporal(TemporalType.DATE)
	@Column(name = "projected_maturity_date")
	private Date projectedMaturityDate;
	
	@SuppressWarnings("unused")
	@Temporal(TemporalType.DATE)
	@Column(name = "actual_maturity_date")
	private Date actualMaturityDate;
	
	@SuppressWarnings("unused")
	@Column(name = "projected_interest_accrued_on_maturity", scale = 6, precision = 19, nullable = false)
	private BigDecimal projectedInterestAccruedOnMaturity;
	
	@SuppressWarnings("unused")
	@Column(name = "actual_interest_accrued", scale = 6, precision = 19, nullable = false)
	private BigDecimal interestAccrued;
	
	@SuppressWarnings("unused")
	@Column(name = "projected_total_maturity_amount", scale = 6, precision = 19, nullable = false)
	private BigDecimal projectedTotalOnMaturity;
	
	@SuppressWarnings("unused")
	@Column(name = "actual_total_amount", scale = 6, precision = 19, nullable = false)
	private BigDecimal total;
	
	@SuppressWarnings("unused")
	@Column(name = "pre_closure_interest_rate", scale = 6, precision = 19, nullable = false)
	private BigDecimal preClosureInterestRate;
	
	@SuppressWarnings("unused")
	@Column(name = "is_renewal_allowed", nullable = false)
	private boolean renewalAllowed = false;
	
	@SuppressWarnings("unused")
	@Column(name = "is_preclosure_allowed", nullable = false)
	private boolean preClosureAllowed = false;
	
    @Column(name = "is_deleted", nullable=false)
	private boolean deleted = false;
    
	public static DepositAccount openNew(
			final Client client, final DepositProduct product,
			final String externalId, final Money deposit, final BigDecimal maturityInterestRate, final Integer tenureInMonths, 
			final Integer interestCompoundedEvery, 
			final PeriodFrequencyType interestCompoundedFrequencyPeriodType, 
			final LocalDate commencementDate, 
			final boolean renewalAllowed, 
			final boolean preClosureAllowed, 
			final FixedTermDepositInterestCalculator fixedTermDepositInterestCalculator) {
		
		Money futureValueOnMaturity = fixedTermDepositInterestCalculator.calculateInterestOnMaturityFor(deposit, tenureInMonths, 
				maturityInterestRate, interestCompoundedEvery, interestCompoundedFrequencyPeriodType);
		
		return new DepositAccount(client, product, externalId, deposit, maturityInterestRate, tenureInMonths, 
				interestCompoundedEvery, interestCompoundedFrequencyPeriodType, commencementDate, renewalAllowed, preClosureAllowed, futureValueOnMaturity);
	}
	
	protected DepositAccount() {
		this.product = null;
	}

	private DepositAccount(
			final Client client, final DepositProduct product,
			final String externalId, final Money deposit, final BigDecimal interestRate, final Integer termInMonths, 
			final Integer interestCompoundedEvery, 
			final PeriodFrequencyType interestCompoundedFrequencyPeriodType, 
			final LocalDate commencementDate,
			final boolean renewalAllowed, 
			final boolean preClosureAllowed, 
			final Money futureValueOnMaturity) {
		this.client = client;
		this.product = product;
		this.externalId = externalId;
		this.currency = deposit.getCurrency();
		this.depositAmount = deposit.getAmount();
		product.validateDepositInRange(this.depositAmount);
		this.interestRate = interestRate;
		product.validateInterestRateInRange(interestRate);
		this.tenureInMonths = termInMonths;
		
		this.interestCompoundedEvery = interestCompoundedEvery;
		this.interestCompoundedFrequencyType = interestCompoundedFrequencyPeriodType;
		if (commencementDate != null) {
			this.projectedCommencementDate = commencementDate.toDate();
			this.projectedMaturityDate = commencementDate.plusMonths(this.tenureInMonths).toDate();
		}
		
		this.renewalAllowed = renewalAllowed;
		this.preClosureAllowed = preClosureAllowed;
		
		this.preClosureInterestRate = BigDecimal.ZERO;
		
		// derived fields
		this.projectedInterestAccruedOnMaturity = futureValueOnMaturity.minus(deposit).getAmount();
		this.projectedTotalOnMaturity = futureValueOnMaturity.getAmount();
	}
	
	public boolean isDeleted() {
		return deleted;
	}
	
	/**
	 * Delete is a <i>soft delete</i>. Updates flag on account so it wont appear in query/report results.
	 * 
	 * Any fields with unique constraints and prepended with id of record.
	 */
	public void delete() {
		this.deleted = true;
		this.externalId = this.getId() + "_" + this.externalId;
	}

	public void update(final DepositAccountCommand command) {

	}
}