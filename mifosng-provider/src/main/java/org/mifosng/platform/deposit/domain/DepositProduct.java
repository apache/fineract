package org.mifosng.platform.deposit.domain;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.mifosng.platform.api.commands.DepositProductCommand;
import org.mifosng.platform.currency.domain.MonetaryCurrency;
import org.mifosng.platform.infrastructure.AbstractAuditableCustom;
import org.mifosng.platform.user.domain.AppUser;

@Entity
@Table(name = "portfolio_product_deposit")
public class DepositProduct extends AbstractAuditableCustom<AppUser, Long> {
	
	@Column(name = "name", nullable = false)
	private String name;

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
	
	@Column(name = "tenure_months", nullable=false)
	private Integer tenureMonths;
	
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
	
	@Column(name = "pre_closure_interest_rate", scale = 6, precision = 19, nullable = false)
	private BigDecimal preClosureInterestRate;
	
    protected DepositProduct(){
    	this.name = null;
        this.description = null;
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
		this.tenureMonths = tenureMonths;
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
	
	public void update(DepositProductCommand command){
		
		if (command.isNameChanged()) {
			this.name = command.getName();
		}

		if (command.isDescriptionChanged()) {
			this.description = command.getDescription();
		}
	}

	public void validateInterestRateInRange(final BigDecimal interestRate) {
//		this.depositProductRelatedDetail.validateInterestRateInRange(interestRate);
	}

	public void validateDepositInRange(final BigDecimal depositAmount) {
//		this.depositProductRelatedDetail.validateDepositInRange(depositAmount);
	}
}
