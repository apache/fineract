package org.mifosplatform.portfolio.savingsdepositproduct.command;

import java.math.BigDecimal;
import java.util.Set;

public class DepositProductCommand {

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
    private final Integer interestCompoundedEveryPeriodType;
    private final boolean isLockinPeriodAllowed;
    private final Integer lockinPeriod;
    private final Integer lockinPeriodType;

    private final boolean renewalAllowed;
    private final boolean preClosureAllowed;
    private final BigDecimal preClosureInterestRate;

    private final boolean interestCompoundingAllowed;

    private final Set<String> modifiedParameters;

    public DepositProductCommand(final Set<String> modifiedParameters, final Long id, final String externalId, final String name,
            final String description, final String currencyCode, final Integer digitsAfterDecimal, final BigDecimal minimumBalance,
            final BigDecimal maximumBalance, final Integer tenureInMonths, final BigDecimal maturityDefaultInterestRate,
            final BigDecimal maturityMinInterestRate, final BigDecimal maturityMaxInterestRate, final Integer interestCompoundedEvery,
            final Integer interestCompoundedEveryPeriodType, final boolean renewalAllowed, final boolean preClosureAllowed,
            final BigDecimal preClosureInterestRate, final boolean interestCompoundingAllowed, final boolean isLockinPeriodAllowed,
            final Integer lockinPeriod, final Integer lockinPeriodType) {
        this.id = id;
        this.externalId = externalId;
        this.name = name;
        this.description = description;

        this.currencyCode = currencyCode;
        this.digitsAfterDecimal = digitsAfterDecimal;

        this.minimumBalance = minimumBalance;
        this.maximumBalance = maximumBalance;

        this.tenureInMonths = tenureInMonths;
        this.maturityDefaultInterestRate = maturityDefaultInterestRate;
        this.maturityMinInterestRate = maturityMinInterestRate;
        this.maturityMaxInterestRate = maturityMaxInterestRate;
        this.interestCompoundedEvery = interestCompoundedEvery;
        this.interestCompoundedEveryPeriodType = interestCompoundedEveryPeriodType;
        this.preClosureInterestRate = preClosureInterestRate;

        this.renewalAllowed = renewalAllowed;
        this.preClosureAllowed = preClosureAllowed;
        this.interestCompoundingAllowed = interestCompoundingAllowed;

        this.isLockinPeriodAllowed = isLockinPeriodAllowed;
        this.lockinPeriod = lockinPeriod;
        this.lockinPeriodType = lockinPeriodType;

        this.modifiedParameters = modifiedParameters;

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

    public Integer getInterestCompoundedEveryPeriodType() {
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

    public boolean isInterestCompoundingAllowed() {
        return interestCompoundingAllowed;
    }

    public boolean isLockinPeriodAllowed() {
        return isLockinPeriodAllowed;
    }

    public Integer getLockinPeriod() {
        return lockinPeriod;
    }

    public Integer getLockinPeriodType() {
        return lockinPeriodType;
    }

    public boolean isExternalIdChanged() {
        return this.modifiedParameters.contains("externalId");
    }

    public boolean isNameChanged() {
        return this.modifiedParameters.contains("name");
    }

    public boolean isDescriptionChanged() {
        return this.modifiedParameters.contains("description");
    }

    public boolean isCurrencyCodeChanged() {
        return this.modifiedParameters.contains("currencyCode");
    }

    public boolean isDigitsAfterDecimalChanged() {
        return this.modifiedParameters.contains("digitsAfterDecimal");
    }

    public boolean isInterestRateChanged() {
        return this.modifiedParameters.contains("interestRate");
    }

    public boolean isMinimumBalanceChanged() {
        return this.modifiedParameters.contains("minimumBalance");
    }

    public boolean isMaximumBalanceChanged() {
        return this.modifiedParameters.contains("maximumBalance");
    }

    public boolean isTenureMonthsChanged() {
        return this.modifiedParameters.contains("tenureMonths");
    }

    public boolean isMaturityDefaultInterestRateChanged() {
        return this.modifiedParameters.contains("maturityDefaultInterestRate");
    }

    public boolean isMaturityMinInterestRateChanged() {
        return this.modifiedParameters.contains("maturityMinInterestRate");
    }

    public boolean isMaturityMaxInterestRateChanged() {
        return this.modifiedParameters.contains("maturityMaxInterestRate");
    }

    public boolean isInterestCompoundedEveryChanged() {
        return this.modifiedParameters.contains("interestCompoundedEvery");
    }

    public boolean isInterestCompoundedEveryPeriodTypeChanged() {
        return this.modifiedParameters.contains("interestCompoundedEveryPeriodType");
    }

    public boolean isRenewalAllowedChanged() {
        return this.modifiedParameters.contains("renewalAllowed");
    }

    public boolean isPreClosureAllowedChanged() {
        return this.modifiedParameters.contains("preClosureAllowed");
    }

    public boolean isPreClosureInterestRateChanged() {
        return this.modifiedParameters.contains("preClosureInterestRate");
    }

    public boolean isNoFieldChanged() {
        return this.modifiedParameters.isEmpty();
    }

    public boolean interestCompoundingAllowedChanged() {
        return this.modifiedParameters.contains("interestCompoundingAllowed");
    }

    public boolean isLockinPeriodAllowedChanged() {
        return this.modifiedParameters.contains("isLockinPeriodAllowed");
    }

    public boolean isLockinPeriodChanged() {
        return this.modifiedParameters.contains("lockinPeriod");
    }

    public boolean isLockinPeriodTypeChanged() {
        return this.modifiedParameters.contains("lockinPeriodType");
    }
}