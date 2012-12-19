package org.mifosplatform.portfolio.savingsaccountproduct.domain;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;
import org.mifosplatform.portfolio.savingsaccountproduct.command.SavingProductCommand;
import org.mifosplatform.portfolio.savingsdepositproduct.domain.TenureTypeEnum;

@Embeddable
public class SavingProductRelatedDetail {

    @Embedded
    private MonetaryCurrency currency;

    @Column(name = "interest_rate", scale = 6, precision = 19, nullable = false)
    private BigDecimal interestRate;

    @SuppressWarnings("unused")
    @Column(name = "min_interest_rate", scale = 6, precision = 19, nullable = false)
    private BigDecimal minInterestRate;

    @SuppressWarnings("unused")
    @Column(name = "max_interest_rate", scale = 6, precision = 19, nullable = false)
    private BigDecimal maxInterestRate;

    @Column(name = "savings_deposit_amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal savingsDepositAmount;
    
    @Column(name = "deposit_every")
    private Integer depositEvery;

    @Column(name = "savings_product_type", nullable = false)
    private Integer savingProductType;

    @Column(name = "tenure_type", nullable = false)
    private Integer tenureType;

    @Column(name = "tenure", nullable = false)
    private Integer tenure;

    @Column(name = "frequency", nullable = false)
    private Integer frequency;

    @Column(name = "interest_type", nullable = false)
    private Integer interestType;

    @Column(name = "interest_calculation_method")
    private Integer interestCalculationMethod;

    @Column(name = "min_bal_for_withdrawal", scale = 6, precision = 19, nullable = false)
    private BigDecimal minimumBalanceForWithdrawal;

    @Column(name = "is_partial_deposit_allowed", nullable = false)
    private boolean isPartialDepositAllowed;

    @Column(name = "is_lock_in_period_allowed", nullable = false)
    private boolean isLockinPeriodAllowed;

    @Column(name = "lock_in_period", nullable = false)
    private Integer lockinPeriod;

    @Column(name = "lock_in_period_type", nullable = false)
    private Integer lockinPeriodType;

    public SavingProductRelatedDetail() {
        this.interestRate = null;
        this.savingsDepositAmount = null;
    }

    public SavingProductRelatedDetail(final MonetaryCurrency currency, final BigDecimal interestRate, final BigDecimal minInterestRate,
            final BigDecimal maxInterestRate, final BigDecimal savingsDepositAmount,final Integer depositEvery, final SavingProductType savingProductType,
            final TenureTypeEnum tenureType, final Integer tenure, final SavingFrequencyType frequency,
            final SavingsInterestType interestType, final SavingInterestCalculationMethod interestCalculationMethod,
            final BigDecimal minimumBalanceForWithdrawal, final boolean isPartialDepositAllowed, final boolean isLockinPeriodAllowed,
            final Integer lockinPeriod, final PeriodFrequencyType lockinPeriodType) {
        this.currency = currency;
        this.interestRate = interestRate;
        this.minInterestRate = minInterestRate;
        this.maxInterestRate = maxInterestRate;
        this.savingsDepositAmount = savingsDepositAmount;
        this.depositEvery=depositEvery;
        this.savingProductType = savingProductType.getValue();
        this.tenureType = tenureType.getValue();
        this.tenure = tenure;
        this.frequency = frequency.getValue();
        this.interestType = interestType.getValue();
        this.interestCalculationMethod = interestCalculationMethod.getValue();
        this.minimumBalanceForWithdrawal = minimumBalanceForWithdrawal;
        this.isPartialDepositAllowed = isPartialDepositAllowed;
        this.isLockinPeriodAllowed = isLockinPeriodAllowed;
        this.lockinPeriod = lockinPeriod;
        this.lockinPeriodType = lockinPeriodType.getValue();
    }

    public MonetaryCurrency getCurrency() {
        return this.currency.copy();
    }

    public BigDecimal getInterestRate() {
        return BigDecimal.valueOf(Double.valueOf(this.interestRate.stripTrailingZeros().toString()));
    }

    public BigDecimal getMinimumBalance() {
        return BigDecimal.valueOf(Double.valueOf(this.savingsDepositAmount.stripTrailingZeros().toString()));
    }

    public void update(SavingProductCommand command) {
        Integer digitsAfterDecimal = this.currency.getDigitsAfterDecimal();
        if (command.isDigitsAfterDecimalChanged()) {
            digitsAfterDecimal = command.getDigitsAfterDecimal();
        }

        String currencyCodeChanged = this.currency.getCode();
        if (command.isCurrencyCodeChanged()) {
            currencyCodeChanged = command.getCurrencyCode();
        }

        if (command.isDigitsAfterDecimalChanged() || command.isCurrencyCodeChanged()) {
            this.currency = new MonetaryCurrency(currencyCodeChanged, digitsAfterDecimal);
        }

        if (command.isInterestRateChanged()) {
            this.interestRate = command.getInterestRate();
        }

        if (command.isSavingsDepositAmountChanged()) {
            this.savingsDepositAmount = command.getSavingsDepositAmount();
        }
        
        if (command.isDepositEveryChanged()) {
			this.depositEvery=command.getDepositEvery();
		}

        if (command.isSavingProductTypeChanged()) {
            this.savingProductType = SavingProductType.fromInt(command.getSavingProductType()).getValue();
        }

        if (command.isTenureTypeChanged()) {
            this.tenureType = TenureTypeEnum.fromInt(command.getTenureType()).getValue();
        }

        if (command.isTenureChanged()) {
            this.tenure = command.getTenure();
        }

        if (command.isFrequencyChanged()) {
            this.frequency = SavingFrequencyType.fromInt(command.getFrequency()).getValue();
        }

        if (command.isInterestTypeChanged()) {
            this.interestType = SavingsInterestType.fromInt(command.getInterestType()).getValue();
        }

        if (command.isInterestCalculationMethodChanged()) {
            this.interestCalculationMethod = SavingInterestCalculationMethod.fromInt(command.getInterestCalculationMethod()).getValue();
        }

        if (command.isMinimumBalanceForWithdrawalChanged()) {
            this.minimumBalanceForWithdrawal = command.getMinimumBalanceForWithdrawal();
        }

        if (command.isPartialDepositAllowedChanged()) {
            this.isPartialDepositAllowed = command.isPartialDepositAllowed();
        }

        if (command.isLockinPeriodAllowedChanged()) {
            this.isLockinPeriodAllowed = command.isLockinPeriodAllowed();
        }

        if (command.isLockinPeriodChanged()) {
            this.lockinPeriod = command.getLockinPeriod();
        }

        if (command.isLockinPeriodTypeChanged()) {
            this.lockinPeriodType = PeriodFrequencyType.fromInt(command.getLockinPeriodType()).getValue();
        }
    }

    public BigDecimal getSavingsDepositAmount() {
        return savingsDepositAmount;
    }

    public Integer getDepositEvery() {
		return this.depositEvery;
	}

	public Integer getSavingProductType() {
        return savingProductType;
    }

    public Integer getTenureType() {
        return tenureType;
    }

    public Integer getTenure() {
        return tenure;
    }

    public Integer getFrequency() {
        return frequency;
    }

    public Integer getInterestType() {
        return interestType;
    }

    public Integer getInterestCalculationMethod() {
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

    public Integer getLockinPeriodType() {
        return lockinPeriodType;
    }

}
