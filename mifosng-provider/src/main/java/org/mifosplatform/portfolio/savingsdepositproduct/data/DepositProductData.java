/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savingsdepositproduct.data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.organisation.monetary.data.CurrencyData;

/**
 * Immutable data object representing details of a deposit product.
 */
public class DepositProductData {

    private final Long id;
    private final String externalId;
    private final String name;
    private final String description;
    private final String currencyCode;
    private final Integer digitsAfterDecimal;
    private final BigDecimal minDeposit;
    private final BigDecimal maxDeposit;
    private final BigDecimal defaultDeposit;
    private final Integer tenureInMonths;
    private final BigDecimal minInterestRate;
    private final BigDecimal maxInterestRate;
    private final BigDecimal defaultInterestRate;
    private final Integer interestCompoundedEvery;
    private final EnumOptionData interestCompoundedEveryPeriodType;
    private final CurrencyData currency;

    private final boolean renewalAllowed;
    private final boolean preClosureAllowed;
    private final BigDecimal preClosureInterestRate;
    private final boolean interestCompoundingAllowed;

    private final boolean isLockinPeriodAllowed;
    private final Integer lockinPeriod;
    private final EnumOptionData lockinPeriodType;

    private final Collection<CurrencyData> currencyOptions;
    private final List<EnumOptionData> interestCompoundedEveryPeriodTypeOptions;

    public DepositProductData(final Long id, final String externalId,
            final String name, final String description, final String currencyCode, final Integer digitsAfterDecimal,
            final BigDecimal minDeposit, final BigDecimal defaultDeposit, final BigDecimal maxDeposit, final Integer tenureMonths,
            final BigDecimal defaultInterestRate, final BigDecimal minInterestRate,
            final BigDecimal maxInterestRate, final Integer interestCompoundedEvery,
            final EnumOptionData interestCompoundedEveryPeriodType, final boolean renewalAllowed, final boolean preClosureAllowed,
            final BigDecimal preClosureInterestRate, final boolean interestCompoundingAllowed, final boolean isLockinPeriodAllowed,
            final Integer lockinPeriod, final EnumOptionData lockinPeriodType,final CurrencyData currencyData) {

        this.id = id;
        this.externalId = externalId;
        this.name = name;
        this.description = description;
        this.currencyCode = currencyCode;
        this.digitsAfterDecimal = digitsAfterDecimal;
        this.minDeposit = minDeposit;
        this.defaultDeposit = defaultDeposit;
        this.maxDeposit = maxDeposit;

        this.tenureInMonths = tenureMonths;
        this.defaultInterestRate = defaultInterestRate;
        this.minInterestRate = minInterestRate;
        this.maxInterestRate = maxInterestRate;
        this.interestCompoundedEvery = interestCompoundedEvery;
        this.interestCompoundedEveryPeriodType = interestCompoundedEveryPeriodType;
        this.renewalAllowed = renewalAllowed;
        this.preClosureAllowed = preClosureAllowed;
        this.preClosureInterestRate = preClosureInterestRate;
        this.interestCompoundingAllowed = interestCompoundingAllowed;

        this.lockinPeriod = lockinPeriod;
        this.lockinPeriodType = lockinPeriodType;
        this.isLockinPeriodAllowed = isLockinPeriodAllowed;
        this.currency = currencyData;
        this.currencyOptions = null;
        this.interestCompoundedEveryPeriodTypeOptions = null;
    }

    public DepositProductData(final Collection<CurrencyData> currencyOptions,
            final EnumOptionData defaultInterestCompoundedEveryPeriodType,
            final List<EnumOptionData> interestCompoundedEveryPeriodTypeOptions) {
        this.id = null;
        this.externalId = null;
        this.name = null;
        this.description = null;
        this.currencyCode = null;
        this.digitsAfterDecimal = null;
        this.minDeposit = null;
        this.defaultDeposit = null;
        this.maxDeposit = null;

        this.tenureInMonths = null;
        this.defaultInterestRate = null;
        this.minInterestRate = null;
        this.maxInterestRate = null;
        this.interestCompoundedEvery = null;
        this.interestCompoundedEveryPeriodType = defaultInterestCompoundedEveryPeriodType;

        this.renewalAllowed = false;
        this.preClosureAllowed = false;
        this.preClosureInterestRate = null;
        this.interestCompoundingAllowed = false;

        this.isLockinPeriodAllowed = false;
        this.lockinPeriod = null;
        this.lockinPeriodType = defaultInterestCompoundedEveryPeriodType;
        
        this.currency = null;

        this.currencyOptions = currencyOptions;
        this.interestCompoundedEveryPeriodTypeOptions = interestCompoundedEveryPeriodTypeOptions;
    }

    public DepositProductData(final DepositProductData product, final Collection<CurrencyData> currencyOptions,
            final List<EnumOptionData> interestCompoundedEveryPeriodTypeOptions) {
        this.id = product.id;
        this.externalId = product.externalId;
        this.name = product.name;
        this.description = product.description;
        this.currencyCode = product.currencyCode;
        this.digitsAfterDecimal = product.digitsAfterDecimal;
        this.minDeposit = product.minDeposit;
        this.defaultDeposit = product.defaultDeposit;
        this.maxDeposit = product.maxDeposit;

        this.tenureInMonths = product.tenureInMonths;
        this.defaultInterestRate = product.defaultInterestRate;
        this.minInterestRate = product.minInterestRate;
        this.maxInterestRate = product.maxInterestRate;
        this.interestCompoundedEvery = product.interestCompoundedEvery;
        this.interestCompoundedEveryPeriodType = product.interestCompoundedEveryPeriodType;

        this.renewalAllowed = product.renewalAllowed;
        this.preClosureAllowed = product.preClosureAllowed;
        this.preClosureInterestRate = product.preClosureInterestRate;
        this.interestCompoundingAllowed = product.interestCompoundingAllowed;

        this.lockinPeriod = product.lockinPeriod;
        this.lockinPeriodType = product.lockinPeriodType;
        this.isLockinPeriodAllowed = product.isLockinPeriodAllowed;
        
        this.currencyOptions = currencyOptions;
        this.interestCompoundedEveryPeriodTypeOptions = interestCompoundedEveryPeriodTypeOptions;
        
        if (this.currencyOptions != null && this.currencyOptions.size() == 1) {
            this.currency = new ArrayList<CurrencyData>(this.currencyOptions).get(0);
        } else {
            this.currency = product.currency;
        }
        
    }

    public Long getId() {
        return id;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public Integer getDigitsAfterDecimal() {
        return digitsAfterDecimal;
    }

    public BigDecimal getMinimumBalance() {
        return minDeposit;
    }

    public BigDecimal getMaximumBalance() {
        return maxDeposit;
    }

    public Integer getTenureInMonths() {
        return tenureInMonths;
    }

    public BigDecimal getMaturityDefaultInterestRate() {
        return defaultInterestRate;
    }

    public BigDecimal getMaturityMinInterestRate() {
        return minInterestRate;
    }

    public BigDecimal getMaturityMaxInterestRate() {
        return maxInterestRate;
    }

    public Integer getInterestCompoundedEvery() {
        return interestCompoundedEvery;
    }

    public EnumOptionData getInterestCompoundedEveryPeriodType() {
        return interestCompoundedEveryPeriodType;
    }

    public boolean isRenewalAllowed() {
        return renewalAllowed;
    }

    public boolean isPreClosureAllowed() {
        return preClosureAllowed;
    }

    public BigDecimal getPreClosureInterestRate() {
        return preClosureInterestRate;
    }

    public Collection<CurrencyData> getCurrencyOptions() {
        return currencyOptions;
    }

    public List<EnumOptionData> getInterestCompoundedEveryPeriodTypeOptions() {
        return interestCompoundedEveryPeriodTypeOptions;
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

    public EnumOptionData getLockinPeriodType() {
        return lockinPeriodType;
    }

}