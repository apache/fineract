package org.mifosng.platform.deposit.domain;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.mifosng.platform.api.commands.DepositProductCommand;
import org.mifosng.platform.currency.domain.MonetaryCurrency;
import org.mifosng.platform.exceptions.ValueOutsideRangeException;
import org.mifosng.platform.infrastructure.AbstractAuditableCustom;
import org.mifosng.platform.user.domain.AppUser;

@Entity
@Table(name = "portfolio_product_deposit")
public class DepositProduct extends AbstractAuditableCustom<AppUser, Long> {
	
	@SuppressWarnings("unused")
	@Column(name = "name", nullable = false)
	private String name;

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
	
	@SuppressWarnings("unused")
	@Column(name = "tenure_months", nullable=false)
	private Integer tenureInMonths;
	
	@SuppressWarnings("unused")
	@Column(name = "maturity_default_interest_rate", scale = 6, precision = 19, nullable = false)
	private BigDecimal maturityDefaultInterestRate;
	
	@Column(name = "maturity_min_interest_rate", scale = 6, precision = 19, nullable = false)
	private BigDecimal maturityMinInterestRate;
	
	@Column(name = "maturity_max_interest_rate", scale = 6, precision = 19, nullable = false)
	private BigDecimal maturityMaxInterestRate;
	
	@Column(name = "can_renew", nullable=false)
	private boolean renewalAllowed = false;
	
	@Column(name = "can_pre_close", nullable=false)
	private boolean preClosureAllowed = false;
	
	@SuppressWarnings("unused")
	@Column(name = "pre_closure_interest_rate", scale = 6, precision = 19, nullable = false)
	private BigDecimal preClosureInterestRate;
	
	protected DepositProduct() {
		//
	}
    
	public DepositProduct(final String name, final String description,
			final MonetaryCurrency currency, final BigDecimal minimumBalance,
			final BigDecimal maximumBalance, final Integer tenureMonths,
			final BigDecimal maturityDefaultInterestRate,
			final BigDecimal maturityMinInterestRate,
			BigDecimal maturityMaxInterestRate, boolean canRenew,
			boolean canPreClose, BigDecimal preClosureInterestRate) {
		this.name = name.trim();
		if (StringUtils.isNotBlank(description)) {
			this.description = description.trim();
		} else {
			this.description = null;
		}
		
		this.currency = currency;
		this.minimumBalance = minimumBalance;
		this.maximumBalance = maximumBalance;
		this.tenureInMonths = tenureMonths;
		this.maturityDefaultInterestRate = maturityDefaultInterestRate;
		this.maturityMinInterestRate = maturityMinInterestRate;
		this.maturityMaxInterestRate = maturityMaxInterestRate;
		this.renewalAllowed = canRenew;
		this.preClosureAllowed = canPreClose;
		this.preClosureInterestRate = preClosureInterestRate;
	}
	
	public MonetaryCurrency getCurrency() {
		return currency;
	}

	public boolean isRenewalAllowed() {
		return this.renewalAllowed;
	}

	public boolean isPreClosureAllowed() {
		return this.preClosureAllowed;
	}
	
	public boolean isDeleted() {
		return deleted;
	}
	
	public void delete() {
		this.deleted = true;
	}
	
	public void update(final DepositProductCommand command){
		
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
		
		if (command.isRenewalAllowedChanged()) {
			this.renewalAllowed=command.isRenewalAllowed();
		}
		
		if (command.isPreClosureAllowed()) {
			this.preClosureAllowed=command.isPreClosureAllowed();
		}
		
		if (command.isPreClosureInterestRateChanged()) {
			this.preClosureInterestRate=command.getPreClosureInterestRate();
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
			throw new ValueOutsideRangeException(actualValue, minValue, maxValue, "deposit.account.maturityInterestRate");
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
			final String maxValue = (this.minimumBalance == null) ? "" : this.minimumBalance.toPlainString();
			throw new ValueOutsideRangeException(actualValue, minValue, maxValue, "deposit.account.deposit.amount");
		}
	}
}