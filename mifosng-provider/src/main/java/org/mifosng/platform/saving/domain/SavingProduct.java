package org.mifosng.platform.saving.domain;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.mifosng.platform.api.commands.SavingProductCommand;
import org.mifosng.platform.currency.domain.MonetaryCurrency;
import org.mifosng.platform.infrastructure.AbstractAuditableCustom;
import org.mifosng.platform.user.domain.AppUser;

@Entity
@Table(name = "portfolio_product_savings")
public class SavingProduct extends AbstractAuditableCustom<AppUser, Long> {
	
	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "description")
	private String description;
	
    @Column(name = "is_deleted", nullable=false)
	private boolean deleted = false;
	
	@Embedded
	SavingProductRelatedDetail savingProductRelatedDetail;

	protected SavingProduct() {
		this.name = null;
        this.description = null;
        this.savingProductRelatedDetail = null;
	}

	public SavingProduct(final String name, final String description, final MonetaryCurrency currency,final BigDecimal interestRate, final BigDecimal minimumBalance,final BigDecimal maximumBalance) {
		this.name = name.trim();
		if (StringUtils.isNotBlank(description)) {
			this.description = description.trim();
		} else {
			this.description = null;
		}
		this.savingProductRelatedDetail=new SavingProductRelatedDetail(currency, interestRate,minimumBalance,maximumBalance);
	}
	
    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }	
	
	public MonetaryCurrency getCurrency() {
    	return this.savingProductRelatedDetail.getCurrency();
    }
	
	public BigDecimal getInterestRate(){
		return this.savingProductRelatedDetail.getInterestRate();
	}
	
	public BigDecimal getMinimumBalance(){
		return this.savingProductRelatedDetail.getMinimumBalance();
	}
	
	public BigDecimal getMaximumBalance(){
		return this.savingProductRelatedDetail.getMaximumBalance();
	}
	
	public boolean isDeleted() {
		return deleted;
	}
	
	public void delete() {
		this.deleted = true;
	}	

	public void update(final SavingProductCommand command) {

		if (command.isNameChanged()) {
			this.name = command.getName();
		}

		if (command.isDescriptionChanged()) {
			this.description = command.getDescription();
		}
		
		this.savingProductRelatedDetail.update(command);
	}
}