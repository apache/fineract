/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savingsdepositproduct.domain;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;
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
	
	public Map<String, Object> update(final JsonCommand command){
		
		final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(20);

        final String localeAsInput = command.locale();

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
        
        final String externalIdParamName = "externalId";
        if (command.isChangeInStringParameterNamed(externalIdParamName, this.externalId)) {
            final String newValue = command.stringValueOfParameterNamed(externalIdParamName);
            actualChanges.put(externalIdParamName, newValue);
            this.externalId = newValue;
        }
        
        String currencyCode = this.currency.getCode();
        Integer digitsAfterDecimal = this.currency.getDigitsAfterDecimal();
        
        final String digitsAfterDecimalParamName = "digitsAfterDecimal";
        if (command.isChangeInIntegerParameterNamed(digitsAfterDecimalParamName, digitsAfterDecimal)) {
            final Integer newValue = command.integerValueOfParameterNamed(digitsAfterDecimalParamName);
            actualChanges.put(digitsAfterDecimalParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            digitsAfterDecimal = newValue;
            this.currency = new MonetaryCurrency(currencyCode, digitsAfterDecimal);
        }

        final String currencyCodeParamName = "currencyCode";
        if (command.isChangeInStringParameterNamed(currencyCodeParamName, currencyCode)) {
            final String newValue = command.stringValueOfParameterNamed(currencyCodeParamName);
            actualChanges.put(currencyCodeParamName, newValue);
            currencyCode = newValue;
            this.currency = new MonetaryCurrency(currencyCode, digitsAfterDecimal);
        }
        
        final String minimumBalanceParamName = "minimumBalance";
        if (command.isChangeInBigDecimalParameterNamed(minimumBalanceParamName, this.minimumBalance)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(minimumBalanceParamName);
            actualChanges.put(minimumBalanceParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.minimumBalance = newValue;
        }
        
        final String maximumBalanceParamName = "maximumBalance";
        if (command.isChangeInBigDecimalParameterNamed(maximumBalanceParamName, this.maximumBalance)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(maximumBalanceParamName);
            actualChanges.put(maximumBalanceParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.maximumBalance = newValue;
        }
        
        final String tenureInMonthsParamName = "tenureInMonths";
        if (command.isChangeInIntegerParameterNamed(tenureInMonthsParamName, this.tenureInMonths)) {
            final Integer newValue = command.integerValueOfParameterNamed(tenureInMonthsParamName);
            actualChanges.put(tenureInMonthsParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.tenureInMonths = newValue;
        }
        
        final String defaultInterestRateParamName = "maturityDefaultInterestRate";
        if (command.isChangeInBigDecimalParameterNamed(defaultInterestRateParamName, this.maturityDefaultInterestRate)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(defaultInterestRateParamName);
            actualChanges.put(defaultInterestRateParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.maturityDefaultInterestRate = newValue;
        }
        
        final String minInterestRateParamName = "maturityMinInterestRate";
        if (command.isChangeInBigDecimalParameterNamed(minInterestRateParamName, this.maturityMinInterestRate)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(minInterestRateParamName);
            actualChanges.put(minInterestRateParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.maturityMinInterestRate = newValue;
        }
        
        final String maxInterestRateParamName = "maturityMaxInterestRate";
        if (command.isChangeInBigDecimalParameterNamed(maxInterestRateParamName, this.maturityMaxInterestRate)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(maxInterestRateParamName);
            actualChanges.put(maxInterestRateParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.maturityMaxInterestRate = newValue;
        }
        
        final String interestCompoundedEveryParamName = "interestCompoundedEvery";
        if (command.isChangeInIntegerParameterNamed(interestCompoundedEveryParamName, this.interestCompoundedEvery)) {
            final Integer newValue = command.integerValueOfParameterNamed(interestCompoundedEveryParamName);
            actualChanges.put(interestCompoundedEveryParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.interestCompoundedEvery = newValue;
        }
        
        final String interestCompoundedEveryPeriodTypeParamName = "interestCompoundedEveryPeriodType";
        if (command.isChangeInIntegerParameterNamed(interestCompoundedEveryPeriodTypeParamName, this.interestCompoundedEveryPeriodType.getValue())) {
            final Integer newValue = command.integerValueOfParameterNamed(interestCompoundedEveryPeriodTypeParamName);
            actualChanges.put(interestCompoundedEveryPeriodTypeParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.interestCompoundedEveryPeriodType = PeriodFrequencyType.fromInt(newValue);
        }
		
        final String renewalAllowedParamName = "renewalAllowed";
        if (command.isChangeInBooleanParameterNamed(renewalAllowedParamName, this.renewalAllowed)) {
        	final Boolean newValue = command.booleanObjectValueOfParameterNamed(renewalAllowedParamName);
            actualChanges.put(renewalAllowedParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.renewalAllowed = newValue;
        }
        
        final String preClosureAllowedParamName = "preClosureAllowed";
        if (command.isChangeInBooleanParameterNamed(preClosureAllowedParamName, this.preClosureAllowed)) {
        	final Boolean newValue = command.booleanObjectValueOfParameterNamed(preClosureAllowedParamName);
            actualChanges.put(preClosureAllowedParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.preClosureAllowed = newValue;
        }
		
        final String preClosureInterestRateParamName = "preClosureInterestRate";
        if (command.isChangeInBigDecimalParameterNamed(preClosureInterestRateParamName, this.preClosureInterestRate)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(preClosureInterestRateParamName);
            actualChanges.put(preClosureInterestRateParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.preClosureInterestRate = newValue;
        }

		final String interestCompoundingAllowedParamName = "interestCompoundingAllowed";
        if (command.isChangeInBooleanParameterNamed(interestCompoundingAllowedParamName, this.interestCompoundingAllowed)) {
        	final Boolean newValue = command.booleanObjectValueOfParameterNamed(interestCompoundingAllowedParamName);
            actualChanges.put(interestCompoundingAllowedParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.interestCompoundingAllowed = newValue;
        }
		
        final String isLockinPeriodAllowedParamName = "isLockinPeriodAllowed";
        if (command.isChangeInBooleanParameterNamed(isLockinPeriodAllowedParamName, this.isLockinPeriodAllowed)) {
        	final Boolean newValue = command.booleanObjectValueOfParameterNamed(isLockinPeriodAllowedParamName);
            actualChanges.put(isLockinPeriodAllowedParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.isLockinPeriodAllowed = newValue;
        }
		
		final String lockinPeriodParamName = "lockinPeriod";
        if (command.isChangeInIntegerParameterNamed(lockinPeriodParamName, this.lockinPeriod)) {
            final Integer newValue = command.integerValueOfParameterNamed(lockinPeriodParamName);
            actualChanges.put(lockinPeriodParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.lockinPeriod = newValue;
        }
		
		
		final String lockinPeriodTypeParamName = "lockinPeriodType";
        if (command.isChangeInIntegerParameterNamed(lockinPeriodTypeParamName, this.lockinPeriodType.getValue())) {
            final Integer newValue = command.integerValueOfParameterNamed(lockinPeriodTypeParamName);
            actualChanges.put(lockinPeriodTypeParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.lockinPeriodType = PeriodFrequencyType.fromInt(newValue);
        }
        
        return actualChanges;
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

	public static DepositProduct assembleFromJson(JsonCommand command, PeriodFrequencyType interestCompoundingPeriodType,
			PeriodFrequencyType lockinPeriodType) {
		
		final String name = command.stringValueOfParameterNamed("name");
		final String externalId = command.stringValueOfParameterNamed("externalId");
		final String description = command.stringValueOfParameterNamed("description");
        final String currencyCode = command.stringValueOfParameterNamed("currencyCode");
        final Integer digitsAfterDecimal = command.integerValueOfParameterNamed("digitsAfterDecimal");
        
        final MonetaryCurrency currency = new MonetaryCurrency(currencyCode, digitsAfterDecimal);
        final BigDecimal minimumBalance = command.bigDecimalValueOfParameterNamed("minimumBalance");
        final BigDecimal maximumBalance = command.bigDecimalValueOfParameterNamed("maximumBalance");
        
        final Integer tenureMonths = command.integerValueOfParameterNamed("tenureInMonths");
        final BigDecimal maturityDefaultInterestRate = command.bigDecimalValueOfParameterNamed("maturityDefaultInterestRate");
        final BigDecimal maturityMinInterestRate = command.bigDecimalValueOfParameterNamed("maturityMinInterestRate");
        final BigDecimal maturityMaxInterestRate = command.bigDecimalValueOfParameterNamed("maturityMaxInterestRate");
        final Integer interestCompoundedEvery = command.integerValueOfParameterNamed("interestCompoundedEvery");
        
        final boolean canRenew = command.booleanPrimitiveValueOfParameterNamed("canRenew");
        final boolean canPreClose = command.booleanPrimitiveValueOfParameterNamed("canPreClose");
        final BigDecimal preClosureInterestRate = command.bigDecimalValueOfParameterNamed("preClosureInterestRate");
        final boolean isInterestCompoundingAllowed = command.booleanPrimitiveValueOfParameterNamed("isInterestCompoundingAllowed");
        final boolean isLockinPeriodAllowed = command.booleanPrimitiveValueOfParameterNamed("isLockinPeriodAllowed");
        final Integer lockinPeriod = command.integerValueOfParameterNamed("lockinPeriod");
		
		return new DepositProduct(name, externalId, description, currency, minimumBalance, maximumBalance, tenureMonths, maturityDefaultInterestRate,
				maturityMinInterestRate, maturityMaxInterestRate, interestCompoundedEvery, interestCompoundingPeriodType, canRenew, canPreClose,
				preClosureInterestRate, isInterestCompoundingAllowed, isLockinPeriodAllowed, lockinPeriod, lockinPeriodType);
	}
}