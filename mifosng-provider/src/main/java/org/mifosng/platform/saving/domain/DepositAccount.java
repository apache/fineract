package org.mifosng.platform.saving.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.joda.time.LocalDate;
import org.mifosng.platform.api.commands.DepositStateTransitionApprovalCommand;
import org.mifosng.platform.client.domain.Client;
import org.mifosng.platform.currency.domain.MonetaryCurrency;
import org.mifosng.platform.currency.domain.Money;
import org.mifosng.platform.exceptions.InvalidDepositStateTransitionException;
import org.mifosng.platform.infrastructure.AbstractAuditableCustom;
import org.mifosng.platform.loan.domain.PeriodFrequencyType;
import org.mifosng.platform.savingproduct.domain.DepositProduct;
import org.mifosng.platform.user.domain.AppUser;

@Entity
@Table(name = "m_deposit_account", uniqueConstraints = @UniqueConstraint(name="deposit_acc_external_id", columnNames = { "external_id" }))
public class DepositAccount extends AbstractAuditableCustom<AppUser, Long>  {
	
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
	
	@Embedded
	private MonetaryCurrency currency;
	
	@Column(name = "deposit_amount", scale = 6, precision = 19, nullable = false)
	private BigDecimal depositAmount;
	
	@Column(name = "maturity_nominal_interest_rate", scale = 6, precision = 19, nullable = false)
	private BigDecimal interestRate;
	
	@Column(name = "tenure_months", nullable=false)
	private Integer tenureInMonths;
	
	@Column(name = "interest_compounded_every", nullable = false)
	private Integer interestCompoundedEvery;

	@Column(name = "interest_compounded_every_period_enum", nullable = false)
	private Integer interestCompoundedFrequencyType;
	
	@Temporal(TemporalType.DATE)
	@Column(name = "projected_commencement_date")
	private Date projectedCommencementDate;
	
	@Temporal(TemporalType.DATE)
	@Column(name = "actual_commencement_date")
	private Date actualCommencementDate;
	
	@SuppressWarnings("unused")
	@Temporal(TemporalType.DATE)
	@Column(name = "matures_on_date")
	private Date maturesOnDate;
	
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
    
	@Column(name = "status_enum", nullable = false)
	private Integer depositStatus;
	
	@SuppressWarnings("unused")
	@Temporal(TemporalType.DATE)
	@Column(name = "closedon_date")
	private Date closedOnDate;
	
	@SuppressWarnings("unused")
	@Temporal(TemporalType.DATE)
	@Column(name = "rejectedon_date")
	private Date rejectedOnDate;
	
	@SuppressWarnings("unused")
	@Temporal(TemporalType.DATE)
	@Column(name = "withdrawnon_date")
	private Date withdrawnOnDate;	
	
	
	// see
	// http://stackoverflow.com/questions/4334970/hibernate-cannot-simultaneously-fetch-multiple-bags
	@OrderBy(value = "id")
	@LazyCollection(LazyCollectionOption.FALSE)
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "depositAccount", orphanRemoval = true)
	private final List<DepositAccountTransaction> depositaccountTransactions = new ArrayList<DepositAccountTransaction>();
    
	public DepositAccount openNew(
			final Client client, final DepositProduct product,
			final String externalId, final Money deposit, final BigDecimal maturityInterestRate, final Integer tenureInMonths, 
			final Integer interestCompoundedEvery, 
			final PeriodFrequencyType interestCompoundedFrequencyPeriodType, 
			final LocalDate commencementDate, 
			final boolean renewalAllowed, 
			final boolean preClosureAllowed, 
			final FixedTermDepositInterestCalculator fixedTermDepositInterestCalculator, 
			final DepositLifecycleStateMachine depositLifecycleStateMachine) {
		
		Money futureValueOnMaturity = fixedTermDepositInterestCalculator.calculateInterestOnMaturityFor(deposit, tenureInMonths, 
				maturityInterestRate, interestCompoundedEvery, interestCompoundedFrequencyPeriodType);
		
		
		DepositAccountStatus from = null;
		if (depositStatus != null) {
			from = DepositAccountStatus.fromInt(depositStatus);
		}

		DepositAccountStatus statusEnum = depositLifecycleStateMachine.transition(DepositAccountEvent.DEPOSIT_CREATED, from);
		depositStatus = statusEnum.getValue();
		
		return new DepositAccount(client, product, externalId, deposit, maturityInterestRate, tenureInMonths, 
				interestCompoundedEvery, interestCompoundedFrequencyPeriodType, commencementDate, renewalAllowed, preClosureAllowed, futureValueOnMaturity,depositStatus);
	}
	
	public DepositAccount() {
		this.product = null;
	}

	public DepositAccount(
			final Client client, final DepositProduct product,
			final String externalId, final Money deposit, final BigDecimal interestRate, final Integer termInMonths, 
			final Integer interestCompoundedEvery, 
			final PeriodFrequencyType interestCompoundedFrequencyPeriodType, 
			final LocalDate commencementDate,
			final boolean renewalAllowed, 
			final boolean preClosureAllowed, 
			final Money futureValueOnMaturity,
			final Integer depositStatus) {
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
		this.interestCompoundedFrequencyType = interestCompoundedFrequencyPeriodType.getValue();
		if (commencementDate != null) {
			this.projectedCommencementDate = commencementDate.toDate();
			this.maturesOnDate = commencementDate.plusMonths(this.tenureInMonths).toDate();
		}
		
		this.renewalAllowed = renewalAllowed;
		this.preClosureAllowed = preClosureAllowed;
		
		this.preClosureInterestRate = BigDecimal.ZERO;
		
		// derived fields
		this.projectedInterestAccruedOnMaturity = futureValueOnMaturity.minus(deposit).getAmount();
		this.projectedTotalOnMaturity = futureValueOnMaturity.getAmount();
		this.depositStatus=depositStatus;
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

	public void approve(
			final LocalDate actualCommencementDate, 
			final DepositLifecycleStateMachine depositLifecycleStateMachine, 
			final DepositStateTransitionApprovalCommand command,
			final FixedTermDepositInterestCalculator calculator) {

		DepositAccountStatus statusEnum = depositLifecycleStateMachine.transition(DepositAccountEvent.DEPOSIT_APPROVED, DepositAccountStatus.fromInt(this.depositStatus));
		this.depositStatus = statusEnum.getValue();
		
		if (command.getTenureInMonths() != null) {
			this.tenureInMonths = command.getTenureInMonths();
		}
		
		if (command.getDepositAmount() != null) {
			this.depositAmount = command.getDepositAmount();
		}
		
		if (command.getInterestCompoundedEveryPeriodType() != null) {
			this.interestCompoundedFrequencyType = PeriodFrequencyType.fromInt(command.getInterestCompoundedEveryPeriodType()).getValue();
		}

		this.actualCommencementDate = actualCommencementDate.toDate();
		this.maturesOnDate = getActualCommencementDate().plusMonths(this.tenureInMonths).toDate();
		
		Money futureValueOnMaturity = calculator.calculateInterestOnMaturityFor(getDeposit(), this.tenureInMonths, 
				this.interestRate, this.interestCompoundedEvery, getInterestCompoundedFrequencyType());
		
		this.interestAccrued = futureValueOnMaturity.minus(getDeposit()).getAmount();
		this.total = futureValueOnMaturity.getAmount();
		
		DepositAccountTransaction depositaccountTransaction = DepositAccountTransaction.deposit(getDeposit(), getActualCommencementDate());
		depositaccountTransaction.updateAccount(this);
		this.depositaccountTransactions.add(depositaccountTransaction);
		
		LocalDate submittalDate = new LocalDate(this.projectedCommencementDate);
		if (actualCommencementDate.isBefore(submittalDate)) {
			final String errorMessage = "The date on which a deposit is approved cannot be before its submittal date: "	+ submittalDate.toString();
			throw new InvalidDepositStateTransitionException("approval", "cannot.be.before.submittal.date", errorMessage, getActualCommencementDate(), submittalDate);
		}
		
		if (actualCommencementDate.isAfter(new LocalDate())) {
			final String errorMessage = "The date on which a deposit is approved cannot be in the future.";
			throw new InvalidDepositStateTransitionException("approval", "cannot.be.a.future.date", errorMessage, getActualCommencementDate());
		}
	}

	public LocalDate getActualCommencementDate() {
		LocalDate date = null;
		if (this.actualCommencementDate != null) {
			date = new LocalDate(this.actualCommencementDate);
		}
		return date;
	}

	public LocalDate getProjectedCommencementDate() {
		LocalDate date = null;
		if (this.projectedCommencementDate != null) {
			date = new LocalDate(this.projectedCommencementDate);
		}
		return date;
	}

	public void reject(final LocalDate rejectedOn,
			final DepositLifecycleStateMachine depositLifecycleStateMachine) {

		DepositAccountStatus statusEnum = depositLifecycleStateMachine.transition(
				DepositAccountEvent.DEPOSIT_REJECTED,
				DepositAccountStatus.fromInt(this.depositStatus));
		this.depositStatus = statusEnum.getValue();

		this.rejectedOnDate = rejectedOn.toDateTimeAtCurrentTime().toDate();
		this.closedOnDate = rejectedOn.toDateTimeAtCurrentTime().toDate();

		if (rejectedOn.isBefore(getProjectedCommencementDate())) {

			final String errorMessage = "The date on which a deposit is rejected cannot be before its submittal date: "
					+ getProjectedCommencementDate().toString();
			throw new InvalidDepositStateTransitionException("reject",
					"cannot.be.before.submittal.date", errorMessage,
					rejectedOn, getProjectedCommencementDate());

		}
		if (rejectedOn.isAfter(new LocalDate())) {

			final String errorMessage = "The date on which a deposit is rejected cannot be in the future.";
			throw new InvalidDepositStateTransitionException("reject",
					"cannot.be.a.future.date", errorMessage, rejectedOn);

		}

	}

	public void withdrawnByApplicant(final LocalDate withdrawnOn,
			final DepositLifecycleStateMachine depositLifecycleStateMachine) {

		DepositAccountStatus statusEnum = depositLifecycleStateMachine.transition(
				DepositAccountEvent.DEPOSIT_WITHDRAWN,
				DepositAccountStatus.fromInt(this.depositStatus));
		this.depositStatus = statusEnum.getValue();

		this.withdrawnOnDate = withdrawnOn.toDateTimeAtCurrentTime().toDate();
		this.closedOnDate = withdrawnOn.toDateTimeAtCurrentTime().toDate();

		if (withdrawnOn.isBefore(getProjectedCommencementDate())) {

			final String errorMessage = "The date on which a deposit is rejected cannot be before its submittal date: "
					+ getProjectedCommencementDate().toString();
			throw new InvalidDepositStateTransitionException("reject",
					"cannot.be.before.submittal.date", errorMessage,
					withdrawnOn, getProjectedCommencementDate());

		}
		
		if (withdrawnOn.isAfter(new LocalDate())) {
			final String errorMessage = "The date on which a deposit is rejected cannot be in the future.";
			throw new InvalidDepositStateTransitionException("reject",
					"cannot.be.a.future.date", errorMessage, withdrawnOn);
		}
	}

	public void undoDepositApproval(final DepositLifecycleStateMachine depositLifecycleStateMachine) {

		DepositAccountStatus statusEnum = depositLifecycleStateMachine.transition(
				DepositAccountEvent.DEPOSIT_APPROVAL_UNDO,
				DepositAccountStatus.fromInt(this.depositStatus));
		this.depositStatus = statusEnum.getValue();

		this.actualCommencementDate = null;
		this.maturesOnDate = getProjectedCommencementDate().plusMonths(this.tenureInMonths).toDate();
		this.total=null;
		this.interestAccrued=null;
		this.depositaccountTransactions.clear();
	}

	public PeriodFrequencyType getInterestCompoundedFrequencyType() {
		return PeriodFrequencyType.fromInt(this.interestCompoundedFrequencyType);
	}

	public Money getDeposit() {
		return Money.of(this.currency, this.depositAmount);
	}
}