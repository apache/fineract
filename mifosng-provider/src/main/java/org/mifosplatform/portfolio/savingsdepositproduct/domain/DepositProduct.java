package org.mifosplatform.portfolio.savingsdepositproduct.domain;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;
import org.mifosplatform.portfolio.savingsdepositproduct.command.DepositProductCommand;
import org.mifosplatform.portfolio.savingsdepositproduct.exception.DepositProductValueOutsideRangeException;
import org.mifosplatform.useradministration.domain.AppUser;

@Entity
@Table(name = "m_product_deposit", uniqueConstraints={
												@UniqueConstraint(columnNames = {"name"}, name="name_deposit_product"), 
												@UniqueConstraint(columnNames = {"external_id"}, name="externalid_deposit_product")
})
public class DepositProduct extends AbstractAuditableCustom<AppUser, Long> {
	
	@Column(name = "name", nullable = false)
	private String name;
	
	@Column(name = "external_id", length=100)
	private String externalId;

	@SuppressWarnings("unused")
	@Column(name = "description")
	private String description;
	
    @Column(name = "is_deleted", nullable=false)
	private boolean deleted = false;
    
    @Embedded
	private MonetaryCurrency currency;
	
	@Column(name = "minimum_balance", scale = 6, precision = 19, nullable = false)
	private BigDecimal minimumBalance;
	
	@Column(name = "maximum_balance", scale = 6, precision = 19, nullable = false)
	private BigDecimal maximumBalance;
	
	@Column(name = "interest_compounded_every", nullable=false)
	private Integer interestCompoundedEvery;
	
	@Column(name = "interest_compounded_every_period_enum", nullable=false)
	private PeriodFrequencyType interestCompoundedEveryPeriodType;
	
	@Column(name = "maturity_default_interest_rate", scale = 6, precision = 19, nullable = false)
	private BigDecimal maturityDefaultInterestRate;
	
	@Column(name = "maturity_min_interest_rate", scale = 6, precision = 19, nullable = false)
	private BigDecimal maturityMinInterestRate;
	
	@Column(name = "maturity_max_interest_rate", scale = 6, precision = 19, nullable = false)
	private BigDecimal maturityMaxInterestRate;
	
	@Column(name = "tenure_months", nullable=false)
	private Integer tenureInMonths;
	
	@Column(name = "is_renewal_allowed", nullable=false)
	private boolean renewalAllowed = false;
	
	@Column(name = "is_preclosure_allowed", nullable=false)
	private boolean preClosureAllowed = false;
	
	@Column(name = "is_compounding_interest_allowed", nullable=false)
	private boolean interestCompoundingAllowed = false;
	
	@Column(name = "pre_closure_interest_rate", scale = 6, precision = 19, nullable = false)
	private BigDecimal preClosureInterestRate;
	
	@Column(name = "is_lock_in_period_allowed", nullable=false)
	private boolean isLockinPeriodAllowed = false;
	
	@Column(name = "lock_in_period", nullable=false)
	private Integer lockinPeriod;
	
	@Column(name = "lock_in_period_type", nullable=false)
	private PeriodFrequencyType lockinPeriodType;
	
	protected DepositProduct() {
		//
	}
    
	public DepositProduct(final String name, final String externalId, 
			final String description,
			final MonetaryCurrency currency, final BigDecimal minimumBalance,
			final BigDecimal maximumBalance, final Integer tenureMonths,
			final BigDecimal maturityDefaultInterestRate,
			final BigDecimal maturityMinInterestRate,
			final BigDecimal maturityMaxInterestRate, 
			final Integer interestCompoundedEvery, 
			final PeriodFrequencyType interestCompoundedEveryPeriodType, 
			final boolean canRenew,
			final boolean canPreClose, 
			final BigDecimal preClosureInterestRate,
			final boolean isInterestCompoundingAllowed,
			final boolean isLockinPeriodAllowed,
			final Integer lockinPeriod,
			final PeriodFrequencyType lockinPeriodType) {
		this.name = name.trim();
		if (StringUtils.isNotBlank(description)) {
			this.description = description.trim();
		} else {
			this.description = null;
		}
		if (StringUtils.isNotBlank(externalId)) {
			this.externalId = externalId.trim();
		} else {
			this.externalId = null;
		}
		
		this.currency = currency;
		this.minimumBalance = minimumBalance;
		this.maximumBalance = maximumBalance;
		this.tenureInMonths = tenureMonths;
		this.maturityDefaultInterestRate = maturityDefaultInterestRate;
		this.maturityMinInterestRate = maturityMinInterestRate;
		this.maturityMaxInterestRate = maturityMaxInterestRate;
		this.interestCompoundedEvery = interestCompoundedEvery;
		this.interestCompoundedEveryPeriodType = interestCompoundedEveryPeriodType;
		this.renewalAllowed = canRenew;
		this.preClosureAllowed = canPreClose;
		this.preClosureInterestRate = preClosureInterestRate;
		this.interestCompoundingAllowed = isInterestCompoundingAllowed;
		this.isLockinPeriodAllowed = isLockinPeriodAllowed;
		this.lockinPeriod = lockinPeriod;
		this.lockinPeriodType = lockinPeriodType;
	}
	
	public MonetaryCurrency getCurrency() {
		return currency;
	}
	
	public Integer getInterestCompoundedEvery() {
		return interestCompoundedEvery;
	}

	public PeriodFrequencyType getInterestCompoundedEveryPeriodType() {
		return interestCompoundedEveryPeriodType;
	}

	public Integer getTenureInMonths() {
		return tenureInMonths;
	}
	
	public BigDecimal getMaturityDefaultInterestRate() {
		return maturityDefaultInterestRate;
	}

	public BigDecimal getMaturityMinInterestRate() {
		return maturityMinInterestRate;
	}

	public BigDecimal getPreClosureInterestRate() {
		return preClosureInterestRate;
	}

	public boolean isRenewalAllowed() {
		return this.renewalAllowed;
	}

	public boolean isPreClosureAllowed() {
		return this.preClosureAllowed;
	}
	
	public boolean isInterestCompoundingAllowed() {
		return interestCompoundingAllowed;
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

	public boolean isDeleted() {
		return deleted;
	}
	
	/**
	 * Delete is a <i>soft delete</i>. Updates flag on product so it wont appear in query/report results.
	 * 
	 * Any fields with unique constraints and prepended with id of record.
	 */
	public void delete() {
		this.deleted = true;
		this.name = this.getId() + "_DELETED_" + this.name;
		this.externalId = this.getId() + "_DELETED_" + this.externalId;
	}
	
	public void update(final DepositProductCommand command, final PeriodFrequencyType interestCompoundingFrequency, final PeriodFrequencyType lockinPeriodType){
		
		if (command.isExternalIdChanged()) {
			this.externalId = command.getExternalId();
		}
		
		if (command.isNameChanged()) {
			this.name = command.getName();
		}

		if (command.isDescriptionChanged()) {
			this.description = command.getDescription();
		}
		
		Integer digitsAfterDecimalChanged = this.currency.getDigitsAfterDecimal();
		if (command.isDigitsAfterDecimalChanged()) {
			digitsAfterDecimalChanged = command.getDigitsAfterDecimal();
		}
		
		String currencyCodeChanged = this.currency.getCode();
		if (command.isCurrencyCodeChanged()) {
			currencyCodeChanged = command.getCurrencyCode();
		}
		
		if (command.isDigitsAfterDecimalChanged() || command.isCurrencyCodeChanged()) {
			this.currency = new MonetaryCurrency(currencyCodeChanged, digitsAfterDecimalChanged);
		}
				
		if(command.isMinimumBalanceChanged()){
			this.minimumBalance=command.getMinimumBalance();
		}
		
		if(command.isMaximumBalanceChanged()){
			this.maximumBalance=command.getMaximumBalance();
		}
		
		if(command.isTenureMonthsChanged()){
			this.tenureInMonths=command.getTenureInMonths();
		}
		
		if (command.isMaturityDefaultInterestRateChanged()) {
			this.maturityDefaultInterestRate=command.getMaturityDefaultInterestRate();
		}
		
		if(command.isMaturityMaxInterestRateChanged()){
			this.maturityMaxInterestRate=command.getMaturityMaxInterestRate();
		}
		
		if (command.isMaturityMinInterestRateChanged()) {
			this.maturityMinInterestRate=command.getMaturityMinInterestRate();
		}
		
		if (command.isInterestCompoundedEveryChanged()) {
			this.interestCompoundedEvery = command.getInterestCompoundedEvery();
		}
		
		if (command.isInterestCompoundedEveryPeriodTypeChanged()) {
			this.interestCompoundedEveryPeriodType = interestCompoundingFrequency;
		}
		
		if (command.isRenewalAllowedChanged()) {
			this.renewalAllowed=command.isRenewalAllowed();
		}
		
		if (command.isPreClosureAllowed()) {
			this.preClosureAllowed=command.isPreClosureAllowed();
		}
		
		if (command.isPreClosureInterestRateChanged()) {
			this.preClosureInterestRate=command.getPreClosureInterestRate();
		}
		
		if(command.interestCompoundingAllowedChanged()){
			this.interestCompoundingAllowed = command.isInterestCompoundingAllowed();
		}
		
		if(command.isLockinPeriodAllowedChanged()){
			this.isLockinPeriodAllowed = command.isLockinPeriodAllowed();
		}
		
		if(command.isLockinPeriodChanged()){
			this.lockinPeriod = command.getLockinPeriod();
		}
		
		if(command.isLockinPeriodTypeChanged()){
			this.lockinPeriodType = lockinPeriodType;
		}
	}
	
	public void validateInterestRateInRange(final BigDecimal interestRate) {
		boolean inRange = true;
		if (interestRate.compareTo(this.maturityMinInterestRate) < 0) {
			inRange = false;
		}
		
		if (this.maturityMaxInterestRate.compareTo(interestRate) < 0) {
			inRange = false;
		}
		
		if (!inRange) {
			final String actualValue = interestRate.toPlainString();
			final String minValue = (this.maturityMinInterestRate == null) ? "" : this.maturityMinInterestRate.toPlainString();
			final String maxValue = (this.maturityMaxInterestRate == null) ? "" : this.maturityMaxInterestRate.toPlainString();
			throw new DepositProductValueOutsideRangeException(actualValue, minValue, maxValue, "deposit.account.maturityInterestRate");
		}
	}

	public void validateDepositInRange(final BigDecimal depositAmount) {
		boolean inRange = true;
		if (depositAmount.compareTo(this.minimumBalance) < 0) {
			inRange = false;
		}
		
		if (this.maximumBalance != null && this.maximumBalance.compareTo(depositAmount) < 0) {
			inRange = false;
		}
		
		if (!inRange) {
			final String actualValue = depositAmount.toPlainString();
			final String minValue = (this.minimumBalance == null) ? "" : this.minimumBalance.toPlainString();
			final String maxValue = (this.maximumBalance == null) ? "" : this.maximumBalance.toPlainString();
			throw new DepositProductValueOutsideRangeException(actualValue, minValue, maxValue, "deposit.account.deposit.amount");
		}
	}
}