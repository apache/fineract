/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.charge.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.charge.exception.ChargeDueAtDisbursementCannotBePenaltyException;
import org.apache.fineract.portfolio.charge.exception.ChargeMustBePenaltyException;
import org.apache.fineract.portfolio.charge.service.ChargeEnumerations;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentType;
import org.apache.fineract.portfolio.tax.data.TaxGroupData;
import org.apache.fineract.portfolio.tax.domain.TaxGroup;

@Entity
@Getter
@Setter
@Table(name = "m_charge", uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }, name = "name") })
public class Charge extends AbstractPersistableCustom<Long> {

    public static final String CHARGE_TIME_PARAM_NAME = "chargeTimeType";
    public static final String CHARGE_CALCULATION_TYPE_PARAM_NAME = "chargeCalculationType";
    public static final String FEE_ON_MONTH_DAY_PARAM_NAME = "feeOnMonthDay";
    public static final String FEE_INTERVAL_PARAM_NAME = "feeInterval";
    public static final String LOCALE_PARAM_NAME = "locale";
    public static final String FEE_FREQUENCY_PARAM_NAME = "feeFrequency";

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal amount;

    @Column(name = "currency_code", length = 3)
    private String currencyCode;

    // AA: chargeAppliesTo is immutable, an update of it is rejected by the write service
    @Setter(AccessLevel.NONE)
    @Column(name = "charge_applies_to_enum", nullable = false)
    private Integer chargeAppliesTo;

    @Column(name = "charge_time_enum", nullable = false)
    private Integer chargeTimeType;

    @Column(name = "charge_calculation_enum")
    private Integer chargeCalculation;

    @Column(name = "charge_payment_mode_enum")
    private Integer chargePaymentMode;

    @Column(name = "fee_on_day")
    private Integer feeOnDay;

    @Column(name = "fee_interval")
    private Integer feeInterval;

    @Column(name = "fee_on_month")
    private Integer feeOnMonth;

    @Column(name = "is_penalty", nullable = false)
    private boolean penalty;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    // soft delete, only ever set through delete()
    @Setter(AccessLevel.NONE)
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "min_cap", scale = 6, precision = 19)
    private BigDecimal minCap;

    @Column(name = "max_cap", scale = 6, precision = 19)
    private BigDecimal maxCap;

    @Column(name = "fee_frequency", nullable = true)
    private Integer feeFrequency;

    @Column(name = "is_free_withdrawal", nullable = false)
    private boolean enableFreeWithdrawal;

    @Column(name = "free_withdrawal_charge_frequency")
    private Integer freeWithdrawalFrequency;

    @Column(name = "restart_frequency")
    private Integer restartFrequency;

    @Column(name = "restart_frequency_enum")
    private Integer restartFrequencyEnum;

    @Column(name = "is_payment_type", nullable = false)
    private boolean enablePaymentType;

    @ManyToOne
    @JoinColumn(name = "payment_type_id", nullable = false)
    private PaymentType paymentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "income_or_liability_account_id")
    private GLAccount account;

    @ManyToOne
    @JoinColumn(name = "tax_group_id")
    private TaxGroup taxGroup;

    protected Charge() {}

    public Charge(final String name, final BigDecimal amount, final String currencyCode, final ChargeAppliesTo chargeAppliesTo,
            final ChargeTimeType chargeTime, final ChargeCalculationType chargeCalculationType, final boolean penalty, final boolean active,
            final ChargePaymentMode paymentMode, final MonthDay feeOnMonthDay, final Integer feeInterval, final BigDecimal minCap,
            final BigDecimal maxCap, final Integer feeFrequency, final boolean enableFreeWithdrawalCharge,
            final Integer freeWithdrawalFrequency, final Integer restartFrequency, final PeriodFrequencyType restartFrequencyEnum,
            final GLAccount account, final TaxGroup taxGroup, final boolean enablePaymentType, final PaymentType paymentType) {
        this.name = name;
        this.amount = amount;
        this.currencyCode = currencyCode;
        this.chargeAppliesTo = chargeAppliesTo.getValue();
        this.chargeTimeType = chargeTime.getValue();
        this.chargeCalculation = chargeCalculationType.getValue();
        this.penalty = penalty;
        this.active = active;
        this.account = account;
        this.taxGroup = taxGroup;
        this.chargePaymentMode = paymentMode == null ? null : paymentMode.getValue();

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("charges");

        if (isMonthlyFee() || isAnnualFee()) {
            this.feeOnMonth = feeOnMonthDay.getMonthValue();
            this.feeOnDay = feeOnMonthDay.getDayOfMonth();
        }
        this.feeInterval = feeInterval;
        this.feeFrequency = feeFrequency;

        if (isSavingsCharge()) {
            // TODO vishwas, this validation seems unnecessary as identical
            // validation is performed in the write service
            if (!isAllowedSavingsChargeTime()) {
                baseDataValidator.reset().parameter(CHARGE_TIME_PARAM_NAME).value(this.chargeTimeType)
                        .failWithCodeNoParameterAddedToErrorCode("not.allowed.charge.time.for.savings");
            }
            // TODO vishwas, this validation seems unnecessary as identical
            // validation is performed in the writeservice
            if (!isAllowedSavingsChargeCalculationType()) {
                baseDataValidator.reset().parameter(CHARGE_CALCULATION_TYPE_PARAM_NAME).value(this.chargeCalculation)
                        .failWithCodeNoParameterAddedToErrorCode("not.allowed.charge.calculation.type.for.savings");
            }

            if (!(ChargeTimeType.fromInt(getChargeTimeType()).isWithdrawalFee()
                    || ChargeTimeType.fromInt(getChargeTimeType()).isSavingsNoActivityFee())
                    && ChargeCalculationType.fromInt(getChargeCalculation()).isPercentageOfAmount()) {
                baseDataValidator.reset().parameter(CHARGE_CALCULATION_TYPE_PARAM_NAME).value(this.chargeCalculation)
                        .failWithCodeNoParameterAddedToErrorCode(
                                "savings.charge.calculation.type.percentage.allowed.only.for.withdrawal.or.NoActivity");
            }

            if (enableFreeWithdrawalCharge) {
                this.enableFreeWithdrawal = true;
                this.freeWithdrawalFrequency = freeWithdrawalFrequency;
                this.restartFrequency = restartFrequency;
                this.restartFrequencyEnum = restartFrequencyEnum.getValue();
            }

            if (enablePaymentType && paymentType != null) {
                this.enablePaymentType = true;
                this.paymentType = paymentType;
            }

        } else if (isLoanCharge()) {

            if (penalty && (chargeTime.isTimeOfDisbursement() || chargeTime.isTrancheDisbursement())) {
                throw new ChargeDueAtDisbursementCannotBePenaltyException(name);
            }
            if (!penalty && chargeTime.isOverdueInstallment()) {
                throw new ChargeMustBePenaltyException(name);
            }
            // TODO vishwas, this validation seems unnecessary as identical
            // validation is performed in the write service
            if (!isAllowedLoanChargeTime()) {
                baseDataValidator.reset().parameter(CHARGE_TIME_PARAM_NAME).value(this.chargeTimeType)
                        .failWithCodeNoParameterAddedToErrorCode("not.allowed.charge.time.for.loan");
            }
        }

        if (isPercentageOfDisbursementAmount() || isPercentageOfApprovedAmount()) {
            this.minCap = minCap;
            this.maxCap = maxCap;
        }

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    public boolean isLoanCharge() {
        return ChargeAppliesTo.fromInt(this.chargeAppliesTo).isLoanCharge();
    }

    public boolean isAllowedLoanChargeTime() {
        return ChargeTimeType.fromInt(this.chargeTimeType).isAllowedLoanChargeTime();
    }

    public boolean isAllowedClientChargeTime() {
        return ChargeTimeType.fromInt(this.chargeTimeType).isAllowedClientChargeTime();
    }

    public boolean isSavingsCharge() {
        return ChargeAppliesTo.fromInt(this.chargeAppliesTo).isSavingsCharge();
    }

    public boolean isClientCharge() {
        return ChargeAppliesTo.fromInt(this.chargeAppliesTo).isClientCharge();
    }

    public boolean isAllowedSavingsChargeTime() {
        return ChargeTimeType.fromInt(this.chargeTimeType).isAllowedSavingsChargeTime();
    }

    public boolean isAllowedSavingsChargeCalculationType() {
        return ChargeCalculationType.fromInt(this.chargeCalculation).isAllowedSavingsChargeCalculationType();
    }

    public boolean isAllowedClientChargeCalculationType() {
        return ChargeCalculationType.fromInt(this.chargeCalculation).isAllowedClientChargeCalculationType();
    }

    public boolean isPercentageOfApprovedAmount() {
        return ChargeCalculationType.fromInt(this.chargeCalculation).isPercentageOfAmount();
    }

    public boolean isPercentageOfDisbursementAmount() {
        return ChargeCalculationType.fromInt(this.chargeCalculation).isPercentageOfDisbursementAmount();
    }

    public Integer getFrequencyFreeWithdrawalCharge() {
        return this.freeWithdrawalFrequency;
    }

    public Long getPaymentTypeId() {
        Long paymentTypeId = null;
        if (this.paymentType != null) {
            paymentTypeId = this.paymentType.getId();
        }
        return paymentTypeId;
    }

    /**
     * Delete is a <i>soft delete</i>. Updates flag on charge so it won't appear in query/report results.
     *
     * Any fields with unique constraints and prepended with id of record.
     */
    public void delete() {
        this.deleted = true;
        this.name = getId() + "_" + this.name;
    }

    public ChargeData toData() {
        final EnumOptionData chargeTimeType = ChargeEnumerations.chargeTimeType(this.chargeTimeType);
        final EnumOptionData chargeAppliesTo = ChargeEnumerations.chargeAppliesTo(this.chargeAppliesTo);
        final EnumOptionData chargeCalculationType = ChargeEnumerations.chargeCalculationType(this.chargeCalculation);
        final EnumOptionData chargePaymentMode = ChargeEnumerations.chargePaymentMode(this.chargePaymentMode);
        EnumOptionData feeFrequencyType = null;
        if (this.feeFrequency != null) {
            feeFrequencyType = ChargeEnumerations.feeFrequencyType(this.feeFrequency);
        }
        GLAccountData accountData = null;
        if (account != null) {
            accountData = new GLAccountData().setId(account.getId()).setName(account.getName()).setGlCode(account.getGlCode());
        }
        TaxGroupData taxGroupData = null;
        if (this.taxGroup != null) {
            taxGroupData = TaxGroupData.lookup(taxGroup.getId(), taxGroup.getName());
        }

        PaymentTypeData paymentTypeData = null;
        if (this.paymentType != null) {
            paymentTypeData = PaymentTypeData.builder().id(paymentType.getId()).name(paymentType.getName()).build();
        }

        final CurrencyData currency = new CurrencyData(this.currencyCode, null, 0, 0, null, null);
        return ChargeData.builder().id(getId()).name(this.name).amount(this.amount).currency(currency).chargeTimeType(chargeTimeType)
                .chargeAppliesTo(chargeAppliesTo).chargeCalculationType(chargeCalculationType).chargePaymentMode(chargePaymentMode)
                .feeOnMonthDay(getFeeOnMonthDay()).feeInterval(this.feeInterval).penalty(this.penalty).active(this.active)
                .freeWithdrawal(this.enableFreeWithdrawal).freeWithdrawalChargeFrequency(this.freeWithdrawalFrequency)
                .restartFrequency(this.restartFrequency).restartFrequencyEnum(this.restartFrequencyEnum)
                .isPaymentType(this.enablePaymentType).paymentTypeOptions(paymentTypeData).minCap(this.minCap).maxCap(this.maxCap)
                .feeFrequency(feeFrequencyType).incomeOrLiabilityAccount(accountData).taxGroup(taxGroupData).build();

    }

    public boolean isMonthlyFee() {
        return ChargeTimeType.fromInt(this.chargeTimeType).isMonthlyFee();
    }

    public boolean isAnnualFee() {
        return ChargeTimeType.fromInt(this.chargeTimeType).isAnnualFee();
    }

    public boolean isOverdueInstallment() {
        return ChargeTimeType.fromInt(this.chargeTimeType).isOverdueInstallment();
    }

    public MonthDay getFeeOnMonthDay() {
        MonthDay feeOnMonthDay = null;
        if (this.feeOnDay != null && this.feeOnMonth != null) {
            feeOnMonthDay = MonthDay.now(DateUtils.getDateTimeZoneOfTenant()).withMonth(this.feeOnMonth).withDayOfMonth(this.feeOnDay);
        }
        return feeOnMonthDay;
    }

    public Integer feeInterval() {
        return this.feeInterval;
    }

    public Integer feeFrequency() {
        return this.feeFrequency;
    }

    public Long getIncomeAccountId() {
        Long incomeAccountId = null;
        if (this.account != null) {
            incomeAccountId = this.account.getId();
        }
        return incomeAccountId;
    }

    public Long getTaxGroupId() {
        Long taxGroupId = null;
        if (this.taxGroup != null) {
            taxGroupId = this.taxGroup.getId();
        }
        return taxGroupId;
    }

    public boolean isDisbursementCharge() {
        return ChargeTimeType.fromInt(this.chargeTimeType).equals(ChargeTimeType.DISBURSEMENT)
                || ChargeTimeType.fromInt(this.chargeTimeType).equals(ChargeTimeType.TRANCHE_DISBURSEMENT);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Charge)) {
            return false;
        }
        Charge other = (Charge) o;
        return Objects.equals(name, other.name) && Objects.equals(amount, other.amount) && Objects.equals(currencyCode, other.currencyCode)
                && Objects.equals(chargeAppliesTo, other.chargeAppliesTo) && Objects.equals(chargeTimeType, other.chargeTimeType)
                && Objects.equals(chargeCalculation, other.chargeCalculation) && Objects.equals(chargePaymentMode, other.chargePaymentMode)
                && Objects.equals(feeOnDay, other.feeOnDay) && Objects.equals(feeInterval, other.feeInterval)
                && Objects.equals(feeOnMonth, other.feeOnMonth) && penalty == other.penalty && active == other.active
                && deleted == other.deleted && Objects.equals(minCap, other.minCap) && Objects.equals(maxCap, other.maxCap)
                && Objects.equals(feeFrequency, other.feeFrequency) && Objects.equals(account, other.account)
                && Objects.equals(taxGroup, other.taxGroup);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, amount, currencyCode, chargeAppliesTo, chargeTimeType, chargeCalculation, chargePaymentMode, feeOnDay,
                feeInterval, feeOnMonth, penalty, active, deleted, minCap, maxCap, feeFrequency, account, taxGroup);
    }

}
