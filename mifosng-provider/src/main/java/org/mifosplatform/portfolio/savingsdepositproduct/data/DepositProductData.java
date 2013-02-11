package org.mifosplatform.portfolio.savingsdepositproduct.data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.joda.time.DateTime;
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
    private final BigDecimal minimumBalance;
    private final BigDecimal maximumBalance;
    private final Integer tenureInMonths;
    private final BigDecimal maturityDefaultInterestRate;
    private final BigDecimal maturityMinInterestRate;
    private final BigDecimal maturityMaxInterestRate;
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

    private final DateTime createdOn;
    private final DateTime lastModifedOn;

    private final Collection<CurrencyData> currencyOptions;
    private final List<EnumOptionData> interestCompoundedEveryPeriodTypeOptions;

    public DepositProductData(final DateTime createdOn, final DateTime lastModifedOn, final Long id, final String externalId,
            final String name, final String description, final String currencyCode, final Integer digitsAfterDecimal,
            final BigDecimal minimumBalance, final BigDecimal maximumBalance, final Integer tenureMonths,
            final BigDecimal maturityDefaultInterestRate, final BigDecimal maturityMinInterestRate,
            final BigDecimal maturityMaxInterestRate, final Integer interestCompoundedEvery,
            final EnumOptionData interestCompoundedEveryPeriodType, final boolean renewalAllowed, final boolean preClosureAllowed,
            final BigDecimal preClosureInterestRate, final boolean interestCompoundingAllowed, final boolean isLockinPeriodAllowed,
            final Integer lockinPeriod, final EnumOptionData lockinPeriodType,final CurrencyData currencyData) {

        this.createdOn = createdOn;
        this.lastModifedOn = lastModifedOn;
        this.id = id;
        this.externalId = externalId;
        this.name = name;
        this.description = description;
        this.currencyCode = currencyCode;
        this.digitsAfterDecimal = digitsAfterDecimal;
        this.minimumBalance = minimumBalance;
        this.maximumBalance = maximumBalance;

        this.tenureInMonths = tenureMonths;
        this.maturityDefaultInterestRate = maturityDefaultInterestRate;
        this.maturityMinInterestRate = maturityMinInterestRate;
        this.maturityMaxInterestRate = maturityMaxInterestRate;
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
        this.currencyOptions = new ArrayList<CurrencyData>();
        this.interestCompoundedEveryPeriodTypeOptions = new ArrayList<EnumOptionData>();
    }

    public DepositProductData(final Collection<CurrencyData> currencyOptions,
            final EnumOptionData defaultInterestCompoundedEveryPeriodType,
            final List<EnumOptionData> interestCompoundedEveryPeriodTypeOptions) {
        this.createdOn = null;
        this.lastModifedOn = null;
        this.id = null;
        this.externalId = null;
        this.name = null;
        this.description = null;
        this.currencyCode = null;
        this.digitsAfterDecimal = null;
        this.minimumBalance = null;
        this.maximumBalance = null;

        this.tenureInMonths = null;
        this.maturityDefaultInterestRate = null;
        this.maturityMinInterestRate = null;
        this.maturityMaxInterestRate = null;
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
        this.createdOn = product.getCreatedOn();
        this.lastModifedOn = product.getLastModifedOn();
        this.id = product.getId();
        this.externalId = product.getExternalId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.currencyCode = product.getCurrencyCode();
        this.digitsAfterDecimal = product.getDigitsAfterDecimal();
        this.minimumBalance = product.getMinimumBalance();
        this.maximumBalance = product.getMaximumBalance();

        this.tenureInMonths = product.getTenureInMonths();
        this.maturityDefaultInterestRate = product.getMaturityDefaultInterestRate();
        this.maturityMinInterestRate = product.getMaturityMinInterestRate();
        this.maturityMaxInterestRate = product.getMaturityMaxInterestRate();
        this.interestCompoundedEvery = product.getInterestCompoundedEvery();
        this.interestCompoundedEveryPeriodType = product.getInterestCompoundedEveryPeriodType();

        this.renewalAllowed = product.isRenewalAllowed();
        this.preClosureAllowed = product.isPreClosureAllowed();
        this.preClosureInterestRate = product.getPreClosureInterestRate();
        this.interestCompoundingAllowed = product.isInterestCompoundingAllowed();

        this.lockinPeriod = product.getLockinPeriod();
        this.lockinPeriodType = product.getLockinPeriodType();
        this.isLockinPeriodAllowed = product.isLockinPeriodAllowed();
        
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
        return minimumBalance;
    }

    public BigDecimal getMaximumBalance() {
        return maximumBalance;
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

    public BigDecimal getMaturityMaxInterestRate() {
        return maturityMaxInterestRate;
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

    public DateTime getCreatedOn() {
        return createdOn;
    }

    public DateTime getLastModifedOn() {
        return lastModifedOn;
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