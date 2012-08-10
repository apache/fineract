package org.mifosng.platform.deposit.domain;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

import org.mifosng.platform.api.commands.DepositProductCommand;
import org.mifosng.platform.currency.domain.MonetaryCurrency;
import org.mifosng.platform.exceptions.InterestRateOutsideRangeException;

	@Embeddable
	public class DepositProductRelatedDetail {
	
	@Embedded
	private MonetaryCurrency currency;
	
	@Column(name = "minimum_balance", scale = 6, precision = 19, nullable = false)
	private BigDecimal minimumBalance;
	
	@Column(name = "maximum_balance", scale = 6, precision = 19, nullable = false)
	private BigDecimal maximumBalance;
	
	@Column(name = "tenure_months", nullable=false)
	private Integer tenureMonths;
	
	@Column(name = "maturity_default_interest_rate", scale = 6, precision = 19, nullable = false)
	private BigDecimal maturityDefaultInterestRate;
	
	@Column(name = "maturity_min_interest_rate", scale = 6, precision = 19, nullable = false)
	private BigDecimal maturityMinInterestRate;
	
	@Column(name = "maturity_max_interest_rate", scale = 6, precision = 19, nullable = false)
	private BigDecimal maturityMaxInterestRate;
	
	@Column(name = "can_renew", nullable=false)
	private Boolean canRenew;
	
	@Column(name = "can_pre_close", nullable=false)
	private Boolean canPreClose;
	
	@Column(name = "pre_closure_interest_rate", scale = 6, precision = 19, nullable = false)
	private BigDecimal preClosureInterestRate;
	
	public DepositProductRelatedDetail() {
		this.maximumBalance = null;
		this.minimumBalance = null;
		this.tenureMonths = null;
		this.maturityDefaultInterestRate = null;
		this.maturityMinInterestRate = null;
		this.maturityMaxInterestRate = null;
		this.canRenew = null;
		this.canPreClose = null;
		this.preClosureInterestRate = null;
	}
	
	public DepositProductRelatedDetail(final MonetaryCurrency currency, final BigDecimal minimumBalance,final BigDecimal maximumBalance, final Integer tenureMonths,
			final BigDecimal maturityDefaultInterestRate, final BigDecimal maturityMinInterestRate, BigDecimal maturityMaxInterestRate, Boolean canRenew, Boolean canPreClose, BigDecimal preClosureInterestRate){
		
		this.currency=currency;
		this.minimumBalance=minimumBalance;
		this.maximumBalance=maximumBalance;
		
		this.tenureMonths = tenureMonths;
		this.maturityDefaultInterestRate = maturityDefaultInterestRate;
		this.maturityMinInterestRate = maturityMinInterestRate;
		this.maturityMaxInterestRate = maturityMaxInterestRate;
		this.canRenew = canRenew;
		this.canPreClose = canPreClose;
		this.preClosureInterestRate = preClosureInterestRate;
	}
	
	
	public MonetaryCurrency getCurrency() {
		return this.currency.copy();
	}
	
	public BigDecimal getMinimumBalance(){
		return BigDecimal.valueOf(Double.valueOf(this.minimumBalance.stripTrailingZeros().toString()));
	}
	
	public BigDecimal getMaximumBalance(){
		return BigDecimal.valueOf(Double.valueOf(this.maximumBalance.stripTrailingZeros().toString()));
	}
	
	public Integer getTenureMonths() {
		return this.tenureMonths;
	}

	public BigDecimal getMaturityDefaultInterestRate() {
		return BigDecimal.valueOf(Double.valueOf(this.maturityDefaultInterestRate.stripTrailingZeros().toString()));
	}

	public BigDecimal getMaturityMinInterestRate() {
		return BigDecimal.valueOf(Double.valueOf(this.maturityMinInterestRate.stripTrailingZeros().toString()));
	}

	public BigDecimal getMaturityMaxInterestRate() {
		return BigDecimal.valueOf(Double.valueOf(this.maturityMaxInterestRate.stripTrailingZeros().toString()));
	}

	public Boolean getCanRenew() {
		return this.canRenew;
	}

	public Boolean getCanPreClose() {
		return this.canPreClose;
	}

	public BigDecimal getPreClosureInterestRate() {
		return BigDecimal.valueOf(Double.valueOf(this.preClosureInterestRate.stripTrailingZeros().toString()));
	}

	public void update(DepositProductCommand command){
		
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
			this.tenureMonths=command.getTenureMonths();
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
		
		if (command.isCanRenewChanged()) {
			this.canRenew=command.getCanRenew();
		}
		
		if (command.isCanPreCloseChanged()) {
			this.canPreClose=command.getCanPreClose();
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
			throw new InterestRateOutsideRangeException(interestRate, this.maturityMinInterestRate, this.maturityMaxInterestRate);
		}
	}
}