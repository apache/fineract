package org.mifosplatform.portfolio.savingsaccountproduct.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.joda.time.DateTime;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingFrequencyType;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingInterestCalculationMethod;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingProductType;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingsInterestType;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingsLockinPeriodEnum;
import org.mifosplatform.portfolio.savingsaccountproduct.service.SavingProductEnumerations;
import org.mifosplatform.portfolio.savingsdepositproduct.domain.TenureTypeEnum;

public class SavingProductData implements Serializable {

    private final Long id;
    private final String name;
    private final String description;
    private final CurrencyData currency;
    private final Integer digitsAfterDecimal;

    private final BigDecimal interestRate;
    private final BigDecimal minInterestRate;
    private final BigDecimal maxInterestRate;
    private final BigDecimal savingsDepositAmount;
    private final EnumOptionData savingProductType;
    private final EnumOptionData tenureType;
    private final Integer tenure;
    private final EnumOptionData savingFrequencyType;
    private final EnumOptionData interestType;
    private final EnumOptionData interestCalculationMethod;
    private final BigDecimal minimumBalanceForWithdrawal;
    private final boolean isPartialDepositAllowed;
    private final boolean isLockinPeriodAllowed;
    private final Integer lockinPeriod;
    private final EnumOptionData lockinPeriodType;

    private DateTime createdOn;
    private DateTime lastModifedOn;
    private final Integer depositEvery;

    @SuppressWarnings("unused")
    private Collection<CurrencyData> currencyOptions = new ArrayList<CurrencyData>();
    private List<EnumOptionData> savingsProductTypeOptions = new ArrayList<EnumOptionData>();
    private List<EnumOptionData> tenureTypeOptions = new ArrayList<EnumOptionData>();
    private List<EnumOptionData> savingFrequencyOptions = new ArrayList<EnumOptionData>();
    private List<EnumOptionData> savingsInterestTypeOptions = new ArrayList<EnumOptionData>();
    private List<EnumOptionData> lockinPeriodTypeOptions = new ArrayList<EnumOptionData>();
    private List<EnumOptionData> interestCalculationOptions = new ArrayList<EnumOptionData>();

    public SavingProductData() {
        this.createdOn = new DateTime();
        this.lastModifedOn = new DateTime();
        this.id = null;
        this.name = null;
        this.description = null;
        this.interestRate = BigDecimal.ZERO;
        this.minInterestRate = BigDecimal.ZERO;
        this.maxInterestRate = BigDecimal.ZERO;
        this.currency = new CurrencyData("USD", "US Dollar", 2, "$", "currency.USD");
        this.digitsAfterDecimal = Integer.valueOf(2);
        this.savingsDepositAmount = BigDecimal.ZERO;
        this.savingProductType = SavingProductEnumerations.savingProductType(SavingProductType.RECURRING);
        this.tenureType = SavingProductEnumerations.tenureTypeEnum(TenureTypeEnum.FIXED_PERIOD);
        this.tenure = Integer.valueOf(12);
        this.savingFrequencyType = SavingProductEnumerations.interestFrequencyType(SavingFrequencyType.MONTHLY);
        this.interestType = SavingProductEnumerations.savingInterestType(SavingsInterestType.COMPOUNDING);
        this.interestCalculationMethod = SavingProductEnumerations
                .savingInterestCalculationMethod(SavingInterestCalculationMethod.AVERAGEBAL);
        this.minimumBalanceForWithdrawal = BigDecimal.valueOf(100);
        this.isPartialDepositAllowed = false;
        this.isLockinPeriodAllowed = false;
        this.lockinPeriod = Integer.valueOf(0);
        this.lockinPeriodType = SavingProductEnumerations.savingsLockinPeriod(SavingsLockinPeriodEnum.MONTHS);
        this.depositEvery = Integer.valueOf(3);
    }

    public SavingProductData(DateTime createdOn, DateTime lastModifedOn, Long id, String name, String description, BigDecimal interestRate,
            BigDecimal minInterestRate, BigDecimal maxInterestRate, CurrencyData currency, Integer digitsAfterDecimal,
            BigDecimal savingsDepositAmount, EnumOptionData savingProductType, EnumOptionData tenureType, Integer tenure,
            EnumOptionData savingFrequencyType, EnumOptionData savingInterestType, EnumOptionData interestCalculationMethod,
            BigDecimal minimumBalanceForWithdrawal, boolean isPartialDepositAllowed, boolean isLockinPeriodAllowed, Integer lockinPeriod,
            EnumOptionData lockinPeriodType, Integer depositEvery) {
        this.createdOn = createdOn;
        this.lastModifedOn = lastModifedOn;
        this.id = id;
        this.name = name;
        this.description = description;
        this.interestRate = interestRate;
        this.minInterestRate = minInterestRate;
        this.maxInterestRate = maxInterestRate;
        this.currency = currency;
        this.digitsAfterDecimal = digitsAfterDecimal;
        this.savingsDepositAmount = savingsDepositAmount;
        this.savingProductType = savingProductType;
        this.tenureType = tenureType;
        this.tenure = tenure;
        this.savingFrequencyType = savingFrequencyType;
        this.interestType = savingInterestType;
        this.interestCalculationMethod = interestCalculationMethod;
        this.minimumBalanceForWithdrawal = minimumBalanceForWithdrawal;
        this.isPartialDepositAllowed = isPartialDepositAllowed;
        this.isLockinPeriodAllowed = isLockinPeriodAllowed;
        this.lockinPeriod = lockinPeriod;
        this.lockinPeriodType = lockinPeriodType;
        this.depositEvery = depositEvery;
    }

    public SavingProductData(SavingProductData product, Collection<CurrencyData> currencyOptions,
            List<EnumOptionData> savingsProductTypeOptions, List<EnumOptionData> tenureTypeOptions,
            List<EnumOptionData> savingFrequencyOptions, List<EnumOptionData> savingsInterestTypeOptions,
            List<EnumOptionData> lockinPeriodTypeOptions, List<EnumOptionData> interestCalculationOptions) {

        this.createdOn = product.getCreatedOn();
        this.lastModifedOn = product.getLastModifedOn();
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.interestRate = product.getInterestRate();
        this.minInterestRate = product.getMinInterestRate();
        this.maxInterestRate = product.getMaxInterestRate();
        this.currency = product.getCurrency();
        this.digitsAfterDecimal = product.getDigitsAfterDecimal();
        this.savingsDepositAmount = product.getSavingsDepositAmount();
        this.savingProductType = product.getSavingProductType();
        this.tenureType = product.getTenureType();
        this.tenure = product.getTenure();
        this.savingFrequencyType = product.getSavingFrequencyType();
        this.interestType = product.getInterestType();
        this.interestCalculationMethod = product.getInterestCalculationMethod();
        this.minimumBalanceForWithdrawal = product.getMinimumBalanceForWithdrawal();
        this.isPartialDepositAllowed = product.isPartialDepositAllowed();
        this.isLockinPeriodAllowed = product.isLockinPeriodAllowed();
        this.lockinPeriod = product.getLockinPeriod();
        this.lockinPeriodType = product.getLockinPeriodType();
        this.depositEvery = product.getDepositEvery();

        this.currencyOptions = currencyOptions;
        this.savingsProductTypeOptions = savingsProductTypeOptions;
        this.tenureTypeOptions = tenureTypeOptions;
        this.savingFrequencyOptions = savingFrequencyOptions;
        this.savingsInterestTypeOptions = savingsInterestTypeOptions;
        this.lockinPeriodTypeOptions = lockinPeriodTypeOptions;
        this.interestCalculationOptions = interestCalculationOptions;

    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public CurrencyData getCurrency() {
        return currency;
    }

    public Integer getDigitsAfterDecimal() {
        return digitsAfterDecimal;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public BigDecimal getMinInterestRate() {
        return minInterestRate;
    }

    public BigDecimal getMaxInterestRate() {
        return maxInterestRate;
    }

    public BigDecimal getSavingsDepositAmount() {
        return savingsDepositAmount;
    }

    public EnumOptionData getSavingProductType() {
        return savingProductType;
    }

    public EnumOptionData getTenureType() {
        return tenureType;
    }

    public Integer getTenure() {
        return tenure;
    }

    public EnumOptionData getInterestType() {
        return interestType;
    }

    public EnumOptionData getInterestCalculationMethod() {
        return interestCalculationMethod;
    }

    public BigDecimal getMinimumBalanceForWithdrawal() {
        return minimumBalanceForWithdrawal;
    }

    public boolean isPartialDepositAllowed() {
        return isPartialDepositAllowed;
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

    public DateTime getCreatedOn() {
        return createdOn;
    }

    public DateTime getLastModifedOn() {
        return lastModifedOn;
    }

    public void setCurrencyOptions(Collection<CurrencyData> currencyOptions) {
        this.currencyOptions = currencyOptions;
    }

    public EnumOptionData getSavingFrequencyType() {
        return savingFrequencyType;
    }

    public List<EnumOptionData> getSavingsProductTypeOptions() {
        return savingsProductTypeOptions;
    }

    public List<EnumOptionData> getTenureTypeOptions() {
        return tenureTypeOptions;
    }

    public List<EnumOptionData> getSavingFrequencyOptions() {
        return savingFrequencyOptions;
    }

    public List<EnumOptionData> getSavingsInterestTypeOptions() {
        return savingsInterestTypeOptions;
    }

    public List<EnumOptionData> getLockinPeriodTypeOptions() {
        return lockinPeriodTypeOptions;
    }

    public List<EnumOptionData> getInterestCalculationOptions() {
        return interestCalculationOptions;
    }

    public Integer getDepositEvery() {
        return this.depositEvery;
    }

}
