package org.mifosng.platform.saving.domain;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.joda.time.LocalDate;
import org.mifosng.platform.client.domain.Client;
import org.mifosng.platform.infrastructure.AbstractAuditableCustom;
import org.mifosng.platform.loan.domain.PeriodFrequencyType;
import org.mifosng.platform.savingproduct.domain.SavingFrequencyType;
import org.mifosng.platform.savingproduct.domain.SavingInterestCalculationMethod;
import org.mifosng.platform.savingproduct.domain.SavingProduct;
import org.mifosng.platform.savingproduct.domain.SavingProductType;
import org.mifosng.platform.savingproduct.domain.SavingsInterestType;
import org.mifosng.platform.savingproduct.domain.TenureTypeEnum;
import org.mifosplatform.infrastructure.configuration.domain.MonetaryCurrency;
import org.mifosplatform.infrastructure.configuration.domain.Money;
import org.mifosplatform.infrastructure.user.domain.AppUser;

@Entity
@Table(name = "m_saving_account", uniqueConstraints = @UniqueConstraint(name="saving_acc_external_id", columnNames = { "external_id" }))
public class SavingAccount extends AbstractAuditableCustom<AppUser, Long> {
	
	@ManyToOne
	@JoinColumn(name = "client_id", nullable = false)
	private Client client;

	@ManyToOne
	@JoinColumn(name = "product_id")
	private SavingProduct product;
	
	// FIXME - MADHUKAR - Do not use the actual enum type here but instead use Integer e.g. private Integer savingProductType;
	@Column(name = "savings_product_type", nullable=false)
	private SavingProductType savingProductType;
	
	@Column(name = "external_id")
	private String externalId;
	
	@SuppressWarnings("unused")
	@Embedded
	private MonetaryCurrency currency;
	
	@Column(name = "deposit_amount_per_period", scale = 6, precision = 19, nullable = false)
	private BigDecimal savingsDepositAmountPerPeriod;
	
	@SuppressWarnings("unused")
	@Column(name = "total_deposit_amount", scale = 6, precision = 19, nullable = false)
	private BigDecimal totalSavingsAmount;
	
	@Column(name = "reccuring_nominal_interest_rate", scale = 6, precision = 19, nullable = false)
	private BigDecimal reccuringInterestRate;
	
	@Column(name = "regular_saving_nominal_interest_rate", scale = 6, precision = 19, nullable = false)
	private BigDecimal savingInterestRate;
	
	@Column(name = "tenure", nullable=false)
	private Integer tenure;
	
	@Column(name = "tenure_type", nullable=false)
	private TenureTypeEnum tenureType;
	
	// FIXME - MADHUKAR - Do not use the actual enum type here but instead use Integer e.g. private Integer savingProductType;
	@Column(name = "frequency", nullable=false)
	private SavingFrequencyType frequency;
	
	// FIXME - MADHUKAR - Do not use the actual enum type here but instead use Integer e.g. private Integer savingProductType;
	@Column(name = "interest_type", nullable=false)
	private SavingsInterestType interestType;
	
	// FIXME - MADHUKAR - Do not use the actual enum type here but instead use Integer e.g. private Integer savingProductType;
	@Column(name = "interest_calculation_method")
	private SavingInterestCalculationMethod interestCalculationMethod;
	
	@Temporal(TemporalType.DATE)
	@Column(name = "projected_commencement_date")
	private Date projectedCommencementDate;
	
	@SuppressWarnings("unused")
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
	
	@Column(name = "pre_closure_interest_rate", scale = 6, precision = 19, nullable = false)
	private BigDecimal preClosureInterestRate;
	
	@Column(name = "is_preclosure_allowed", nullable = false)
	private boolean preClosureAllowed = false;
	
    @Column(name = "is_deleted", nullable=false)
	private boolean deleted = false;
    
	@Column(name = "status_enum", nullable = false)
	private Integer accountStatus;
	
	@SuppressWarnings("unused")
	@Temporal(TemporalType.DATE)
	@Column(name = "closedon_date")
	private Date closedOnDate;
	
	@SuppressWarnings("unused")
	@Temporal(TemporalType.DATE)
	@Column(name = "rejectedon_date")
	private Date rejectedOnDate;
	
	@Temporal(TemporalType.DATE)
	@Column(name = "withdrawnon_date")
	private Date withdrawnOnDate;	

	@Column(name = "is_lock_in_period_allowed", nullable=false)
	private boolean isLockinPeriodAllowed = false;
	
	@Column(name = "lock_in_period", nullable=false)
	private Integer lockinPeriod;
	
	@Column(name = "lock_in_period_type", nullable=false)
	private PeriodFrequencyType lockinPeriodType;

	public static SavingAccount openNew(
			Client client,
			SavingProduct product,
			String externalId,
			Money savingsDeposit,
			BigDecimal reccuringInterestRate,
			BigDecimal savingInterestRate,
			Integer tenure,
			LocalDate commencementDate,
			TenureTypeEnum tenureTypeEnum,
			SavingProductType savingProductType,
			SavingFrequencyType savingFrequencyType,
			SavingsInterestType interestType,
			SavingInterestCalculationMethod savingInterestCalculationMethod,
			boolean isLockinPeriodAllowed,
			Integer lockinPeriod,
			PeriodFrequencyType lockinPeriodType,
			ReccuringDepositInterestCalculator reccuringDepositInterestCalculator,
			final DepositLifecycleStateMachine lifecycleStateMachine) {
		
		Money futureValueOnMaturity = null;
		if(savingProductType.isReccuring()){
			futureValueOnMaturity = reccuringDepositInterestCalculator.calculateInterestOnMaturityFor(savingsDeposit, tenure, reccuringInterestRate,
					commencementDate, tenureTypeEnum, savingFrequencyType, savingInterestCalculationMethod);
		} else if(savingProductType.isRegular()){
			futureValueOnMaturity = reccuringDepositInterestCalculator.calculateInterestOnMaturityFor(savingsDeposit, tenure, savingInterestRate,
					commencementDate, tenureTypeEnum, savingFrequencyType, savingInterestCalculationMethod);
		}
		
		return new SavingAccount(client, externalId, product, savingsDeposit, reccuringInterestRate, savingInterestRate, tenure,
				commencementDate, tenureTypeEnum, savingProductType, savingFrequencyType, interestType, savingInterestCalculationMethod,
				isLockinPeriodAllowed, lockinPeriod, lockinPeriodType, futureValueOnMaturity, lifecycleStateMachine);
	}

	public SavingAccount(Client client, String externalId,
			SavingProduct product, Money savingsDeposit,
			BigDecimal reccuringInterestRate, BigDecimal savingInterestRate,
			Integer tenure, LocalDate commencementDate,
			TenureTypeEnum tenureTypeEnum,
			SavingProductType savingProductType,
			SavingFrequencyType savingFrequencyType, SavingsInterestType interestType,
			SavingInterestCalculationMethod savingInterestCalculationMethod,
			boolean isLockinPeriodAllowed, Integer lockinPeriod,
			PeriodFrequencyType lockinPeriodType, Money futureValueOnMaturity,
			final DepositLifecycleStateMachine lifecycleStateMachine) {
		
		DepositAccountStatus from = null;
		if (accountStatus != null) {
			from = DepositAccountStatus.fromInt(accountStatus);
		}

		DepositAccountStatus statusEnum = lifecycleStateMachine.transition(DepositAccountEvent.DEPOSIT_CREATED, from);
		accountStatus = statusEnum.getValue();
		
		this.total=BigDecimal.ZERO;
		this.preClosureInterestRate=BigDecimal.ZERO;
		this.totalSavingsAmount = BigDecimal.ZERO;
		this.interestAccrued = BigDecimal.ZERO;
		
		this.client = client;
		this.externalId = externalId;
		this.product = product;
		this.savingsDepositAmountPerPeriod = savingsDeposit.getAmount();
		this.currency = savingsDeposit.getCurrency();
		this.reccuringInterestRate = reccuringInterestRate;
		this.savingInterestRate = savingInterestRate;
		this.tenure =tenure;
		this.projectedCommencementDate=commencementDate.toDate();
		this.tenureType = tenureTypeEnum;
		this.savingProductType = savingProductType;
		this.frequency = savingFrequencyType;
		this.interestType = interestType;
		this.interestCalculationMethod= savingInterestCalculationMethod;
		this.isLockinPeriodAllowed = isLockinPeriodAllowed;
		this.lockinPeriod = lockinPeriod;
		this.lockinPeriodType = lockinPeriodType;
		this.projectedTotalOnMaturity = futureValueOnMaturity.getAmount();
		this.projectedInterestAccruedOnMaturity =futureValueOnMaturity.getAmount().subtract(BigDecimal.valueOf(savingsDeposit.getAmount().doubleValue()*tenure.doubleValue()));
	}
	
	public void modifyAccount(
			SavingProduct product,
			String externalId,
			Money savingsDeposit,
			BigDecimal reccuringInterestRate,
			BigDecimal savingInterestRate,
			Integer tenure,
			LocalDate commencementDate,
			TenureTypeEnum tenureTypeEnum,
			SavingProductType savingProductType,
			SavingFrequencyType savingFrequencyType,
			SavingInterestCalculationMethod savingInterestCalculationMethod,
			boolean isLockinPeriodAllowed,
			Integer lockinPeriod,
			PeriodFrequencyType lockinPeriodType,
			ReccuringDepositInterestCalculator reccuringDepositInterestCalculator) {
		
		
		Money futureValueOnMaturity = null;
		if(savingProductType.isReccuring()){
			futureValueOnMaturity = reccuringDepositInterestCalculator.calculateInterestOnMaturityFor(savingsDeposit, tenure, reccuringInterestRate,
					commencementDate, tenureTypeEnum, savingFrequencyType, savingInterestCalculationMethod);
		} else if(savingProductType.isRegular()){
			futureValueOnMaturity = reccuringDepositInterestCalculator.calculateInterestOnMaturityFor(savingsDeposit, tenure, savingInterestRate,
					commencementDate, tenureTypeEnum, savingFrequencyType, savingInterestCalculationMethod);
		}
		
		this.product = product;
		this.externalId=externalId;
		this.savingsDepositAmountPerPeriod=savingsDeposit.getAmount();
		this.reccuringInterestRate=reccuringInterestRate;
		this.savingInterestRate = savingInterestRate;
		this.tenure = tenure;
		this.projectedCommencementDate = commencementDate.toDate();
		this.tenureType = tenureTypeEnum;
		this.savingProductType = savingProductType;	
		this.frequency =savingFrequencyType;
		this.interestCalculationMethod = savingInterestCalculationMethod;
		this.isLockinPeriodAllowed = isLockinPeriodAllowed;
		this.lockinPeriod = lockinPeriod;
		this.lockinPeriodType = lockinPeriodType;
		
		// FIXME - MADHUKAR - futureValueOnMaturity is possibly null
		this.projectedTotalOnMaturity = futureValueOnMaturity.getAmount();
		this.projectedInterestAccruedOnMaturity =futureValueOnMaturity.getAmount().subtract(BigDecimal.valueOf(savingsDeposit.getAmount().doubleValue()*tenure.doubleValue()));
	}
	
	public boolean isDeleted() {
		return this.deleted;
	}

	public boolean isPendingApproval() {
		return this.accountStatus.equals(100);
	}

	public Client getClient() {
		return client;
	}

	public SavingProduct getProduct() {
		return product;
	}

	public SavingProductType getSavingProductType() {
		return savingProductType;
	}

	public String getExternalId() {
		return externalId;
	}

	public BigDecimal getSavingsDepositAmountPerPeriod() {
		return savingsDepositAmountPerPeriod;
	}

	public BigDecimal getReccuringInterestRate() {
		return reccuringInterestRate;
	}

	public BigDecimal getSavingInterestRate() {
		return savingInterestRate;
	}

	public Integer getTenure() {
		return tenure;
	}

	public TenureTypeEnum getTenureType() {
		return tenureType;
	}

	public SavingFrequencyType getFrequency() {
		return frequency;
	}

	public SavingsInterestType getInterestType() {
		return interestType;
	}

	public SavingInterestCalculationMethod getInterestCalculationMethod() {
		return interestCalculationMethod;
	}

	public Date getProjectedCommencementDate() {
		return projectedCommencementDate;
	}

	public BigDecimal getPreClosureInterestRate() {
		return preClosureInterestRate;
	}

	public boolean isPreClosureAllowed() {
		return preClosureAllowed;
	}

	public Date getWithdrawnOnDate() {
		return withdrawnOnDate;
	}

	public boolean isLockinPeriodAllowed() {
		return isLockinPeriodAllowed;
	}

	public Integer getLockinPeriod() {
		return lockinPeriod;
	}

	public PeriodFrequencyType getLockinPeriodType() {
		return lockinPeriodType;
	}
}
