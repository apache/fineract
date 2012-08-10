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
    private DepositProductRelatedDetail depositProductRelatedDetail;
    
    protected DepositProduct(){
    	this.name = null;
        this.description = null;
        depositProductRelatedDetail=null;
    }
    
    public DepositProduct(final String name, final String description, final MonetaryCurrency currency, final BigDecimal minimumBalance,final BigDecimal maximumBalance,
    		final Integer tenureMonths,final BigDecimal maturityDefaultInterestRate, final BigDecimal maturityMinInterestRate, BigDecimal maturityMaxInterestRate, Boolean canRenew, 
    		Boolean canPreClose, BigDecimal preClosureInterestRate ){
    	this.name = name.trim();
		if (StringUtils.isNotBlank(description)) {
			this.description = description.trim();
		} else {
			this.description = null;
		}
		this.depositProductRelatedDetail= new DepositProductRelatedDetail(currency, minimumBalance, maximumBalance, tenureMonths, maturityDefaultInterestRate, maturityMinInterestRate, maturityMaxInterestRate, canRenew, canPreClose, preClosureInterestRate);
    }
    
    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }	
	
	public MonetaryCurrency getCurrency() {
    	return this.depositProductRelatedDetail.getCurrency();
    }
	
	public BigDecimal getMinimumBalance(){
		return this.depositProductRelatedDetail.getMinimumBalance();
	}
	
	public BigDecimal getMaximumBalance(){
		return this.depositProductRelatedDetail.getMaximumBalance();
	}
	
	public Integer getTenureMonths(){
		return this.depositProductRelatedDetail.getTenureMonths();
	}
	
	public BigDecimal getMaturityDefaultInterestRate() {
		return this.depositProductRelatedDetail.getMaturityDefaultInterestRate();
	}
	
	public BigDecimal getMaturityMinInterestRate() {
		return this.depositProductRelatedDetail.getMaturityMinInterestRate();
	}
	
	public BigDecimal getMaturityMaxInterestRate() {
		return this.depositProductRelatedDetail.getMaturityMaxInterestRate();
	}
	
	public Boolean getCanRenew() {
		return this.depositProductRelatedDetail.getCanRenew();
	}
	
	public Boolean getCanPreClose() {
		return this.depositProductRelatedDetail.getCanPreClose();
	}
	
	public BigDecimal getPreClosureInterestRate() {
		return this.depositProductRelatedDetail.getPreClosureInterestRate();
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
		
		this.depositProductRelatedDetail.update(command);
	}

	public void validateInterestRateInRange(final BigDecimal interestRate) {
		this.depositProductRelatedDetail.validateInterestRateInRange(interestRate);
	}
}
