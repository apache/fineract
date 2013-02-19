/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savingsaccountproduct.domain;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;
import org.mifosplatform.portfolio.savingsdepositproduct.domain.TenureTypeEnum;
import org.mifosplatform.useradministration.domain.AppUser;

@Entity
@Table(name = "m_product_savings")
public class SavingProduct extends AbstractAuditableCustom<AppUser, Long> {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @Embedded
    SavingProductRelatedDetail savingProductRelatedDetail;

    protected SavingProduct() {
        this.name = null;
        this.description = null;
        this.savingProductRelatedDetail = null;
    }

    public SavingProduct(final String name, final String description, final MonetaryCurrency currency, final BigDecimal interestRate,
            final BigDecimal minInterestRate, final BigDecimal maxInterestRate, final BigDecimal savingsDepositAmount,Integer depositEvery,
            final SavingProductType savingProductType, final TenureTypeEnum tenureType, final Integer tenure,
            final SavingFrequencyType savingFrequencyType, final SavingsInterestType savingsInterestType,
            SavingInterestCalculationMethod savingInterestCalculationMethod, final BigDecimal minimumBalanceForWithdrawal,
            final boolean isPartialDepositAllowed, final boolean isLockinPeriodAllowed, final Integer lockinPeriod,
            final PeriodFrequencyType lockinPeriodType) {

        this.name = name.trim();
        if (StringUtils.isNotBlank(description)) {
            this.description = description.trim();
        } else {
            this.description = null;
        }
        this.savingProductRelatedDetail = new SavingProductRelatedDetail(currency, interestRate, minInterestRate, maxInterestRate,
                savingsDepositAmount, depositEvery, savingProductType, tenureType, tenure, savingFrequencyType, savingsInterestType,
                savingInterestCalculationMethod, minimumBalanceForWithdrawal, isPartialDepositAllowed, isLockinPeriodAllowed, lockinPeriod,
                lockinPeriodType);
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

    public BigDecimal getInterestRate() {
        return this.savingProductRelatedDetail.getInterestRate();
    }

    public BigDecimal getMinimumBalance() {
        return this.savingProductRelatedDetail.getMinimumBalance();
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void delete() {
        this.deleted = true;
    }

    public SavingProductRelatedDetail getSavingProductRelatedDetail() {
        return savingProductRelatedDetail;
    }

    public Map<String, Object> update(final JsonCommand command) {

    	final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(20);
    	
    	final String nameParamName = "name";
        if (command.isChangeInStringParameterNamed(nameParamName, this.name)) {
            final String newValue = command.stringValueOfParameterNamed(nameParamName);
            actualChanges.put(nameParamName, newValue);
            this.name = newValue;
        }
    	
        final String descriptionParamName = "description";
        if (command.isChangeInStringParameterNamed(descriptionParamName, this.description)) {
            final String newValue = command.stringValueOfParameterNamed(descriptionParamName);
            actualChanges.put(descriptionParamName, newValue);
            this.description = newValue;
        }

        return this.savingProductRelatedDetail.update(command, actualChanges);
    }

	public static SavingProduct assembleFromJson(JsonCommand command, MonetaryCurrency currency, SavingProductType savingProductType, TenureTypeEnum tenureType, 
			SavingFrequencyType savingFrequencyType, SavingsInterestType interestType, SavingInterestCalculationMethod savingInterestCalculationMethod,
			PeriodFrequencyType lockinPeriodType) {

		final String name = command.stringValueOfParameterNamed("name");
		final String description = command.stringValueOfParameterNamed("description");
		final BigDecimal interestRate = command.bigDecimalValueOfParameterNamed("interestRate");
		final BigDecimal minInterestRate = command.bigDecimalValueOfParameterNamed("minInterestRate");
		final BigDecimal maxInterestRate = command.bigDecimalValueOfParameterNamed("maxInterestRate");
		final BigDecimal savingsDepositAmount = command.bigDecimalValueOfParameterNamed("savingsDepositAmount");
		final Integer tenure = command.integerValueOfParameterNamed("tenure");
		final BigDecimal minimumBalanceForWithdrawal = command.bigDecimalValueOfParameterNamed("minimumBalanceForWithdrawal");
		final boolean isPartialDepositAllowed = command.booleanPrimitiveValueOfParameterNamed("isPartialDepositAllowed");
		final boolean isLockinPeriodAllowed = command.booleanPrimitiveValueOfParameterNamed("isLockinPeriodAllowed");
        final Integer lockinPeriod = command.integerValueOfParameterNamed("lockinPeriod");
        final Integer depositEvery = command.integerValueOfParameterNamed("depositEvery");
        
        return new SavingProduct(name, description, currency, interestRate, minInterestRate, maxInterestRate, savingsDepositAmount, depositEvery, savingProductType,
        		tenureType, tenure, savingFrequencyType, interestType, savingInterestCalculationMethod, minimumBalanceForWithdrawal, isPartialDepositAllowed, 
        		isLockinPeriodAllowed, lockinPeriod, lockinPeriodType);
	}
}