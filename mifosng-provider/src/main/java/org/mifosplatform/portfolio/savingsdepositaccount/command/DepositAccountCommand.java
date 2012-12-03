package org.mifosplatform.portfolio.savingsdepositaccount.command;

import java.math.BigDecimal;
import java.util.Set;

import org.joda.time.LocalDate;

/**
 * Immutable command used when create/renewing deposit accounts
 */
public class DepositAccountCommand {

    private final Long id;
    private final Long clientId;
    private final Long productId;
    private final String externalId;

    private final BigDecimal depositAmount;
    private final BigDecimal maturityInterestRate;
    private final BigDecimal preClosureInterestRate;
    private final Integer tenureInMonths;

    private final boolean isLockinPeriodAllowed;
    private final Integer lockinPeriod;
    private final Integer lockinPeriodType;

    private final Integer interestCompoundedEvery;
    private final Integer interestCompoundedEveryPeriodType;
    private final LocalDate commencementDate;

    private final boolean renewalAllowed;
    private final boolean preClosureAllowed;
    private final boolean isInterestWithdrawable;
    private final boolean interestCompoundingAllowed;

    private final Set<String> modifiedParameters;

    public DepositAccountCommand(final Set<String> modifiedParameters, final Long id, final Long clientId, final Long productId,
            final String externalId, final BigDecimal depositAmount, final BigDecimal interestRate,
            final BigDecimal preClosureInterestRate, final Integer tenureInMonths, final Integer compoundingInterestFrequency,
            final Integer compoundingInterestFrequencyType, final LocalDate commencementDate, final boolean renewalAllowed,
            final boolean preClosureAllowed, final boolean isInterstWithdrawable, final boolean interestCompoundingAllowed,
            final boolean isLockinPeriodAllowed, final Integer lockInPeriod, final Integer lockinPeriodType) {
        this.id = id;
        this.clientId = clientId;
        this.productId = productId;
        this.externalId = externalId;

        this.depositAmount = depositAmount;
        this.maturityInterestRate = interestRate;
        this.preClosureInterestRate = preClosureInterestRate;
        this.tenureInMonths = tenureInMonths;

        this.modifiedParameters = modifiedParameters;
        this.interestCompoundedEvery = compoundingInterestFrequency;
        this.interestCompoundedEveryPeriodType = compoundingInterestFrequencyType;
        this.commencementDate = commencementDate;
        this.renewalAllowed = renewalAllowed;
        this.preClosureAllowed = preClosureAllowed;
        this.isInterestWithdrawable = isInterstWithdrawable;
        this.interestCompoundingAllowed = interestCompoundingAllowed;

        this.isLockinPeriodAllowed = isLockinPeriodAllowed;
        this.lockinPeriod = lockInPeriod;
        this.lockinPeriodType = lockinPeriodType;
    }

    public Long getId() {
        return id;
    }

    public Long getClientId() {
        return clientId;
    }

    public Long getProductId() {
        return productId;
    }

    public String getExternalId() {
        return externalId;
    }

    public BigDecimal getDepositAmount() {
        return depositAmount;
    }

    public BigDecimal getMaturityInterestRate() {
        return maturityInterestRate;
    }

    public BigDecimal getPreClosureInterestRate() {
        return preClosureInterestRate;
    }

    public Integer getTenureInMonths() {
        return tenureInMonths;
    }

    public Integer getInterestCompoundedEvery() {
        return interestCompoundedEvery;
    }

    public Integer getInterestCompoundedEveryPeriodType() {
        return interestCompoundedEveryPeriodType;
    }

    public LocalDate getCommencementDate() {
        return commencementDate;
    }

    public boolean isInterestWithdrawable() {
        return isInterestWithdrawable;
    }

    public boolean isRenewalAllowed() {
        return renewalAllowed;
    }

    public boolean isPreClosureAllowed() {
        return preClosureAllowed;
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

    public boolean isNoFieldChanged() {
        return this.modifiedParameters.isEmpty();
    }

    public boolean isExternalIdChanged() {
        return this.modifiedParameters.contains("externalId");
    }

    public boolean isDepositAmountChanged() {
        return this.modifiedParameters.contains("deposit");
    }

    public boolean isTenureInMonthsChanged() {
        return this.modifiedParameters.contains("tenureInMonths");
    }

    public boolean isLockinPeriodChanged() {
        return this.modifiedParameters.contains("lockinPeriod");
    }

    public boolean isMaturityActualInterestRateChanged() {
        return this.modifiedParameters.contains("maturityInterestRate");
    }

    public boolean isPreClosureInterestRateChanged() {
        return this.modifiedParameters.contains("preClosureInterestRate");
    }

    public boolean isCompoundingInterestEveryChanged() {
        return this.modifiedParameters.contains("interestCompoundedEvery");
    }

    public boolean isInterestCompoundedEveryPeriodTypeChaged() {
        return this.modifiedParameters.contains("interestCompoundedEveryPeriodType");
    }

    public boolean isRenewalAllowedChanged() {
        return this.modifiedParameters.contains("renewalAllowed");
    }

    public boolean isPreClosureAllowedChanged() {
        return this.modifiedParameters.contains("preClosureAllowed");
    }

    public boolean isInterestWithdrawableChanged() {
        return this.modifiedParameters.contains("isInterestWithdrawable");
    }

    public boolean isInterestCompoundingAllowedChanged() {
        return this.modifiedParameters.contains("interestCompoundingAllowed");
    }

    public boolean isCommencementDateChanged() {
        return this.modifiedParameters.contains("commencementDate");
    }
}