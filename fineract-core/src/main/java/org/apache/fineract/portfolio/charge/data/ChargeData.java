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
package org.apache.fineract.portfolio.charge.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.MonthDay;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.fineract.portfolio.tax.data.TaxGroupData;

/**
 * Immutable data object for charge data.
 */
@Getter
@EqualsAndHashCode(of = "id")
@Builder(toBuilder = true)
public final class ChargeData implements Comparable<ChargeData>, Serializable {

    private final Long id;
    private final String name;

    @Builder.Default
    private final boolean active = Boolean.FALSE;
    @Builder.Default
    private final boolean penalty = Boolean.FALSE;
    @Builder.Default
    private final boolean freeWithdrawal = Boolean.FALSE;
    @Builder.Default
    private final boolean isPaymentType = Boolean.FALSE;

    private final Integer freeWithdrawalChargeFrequency;
    private final Integer restartFrequency;
    private final Integer restartFrequencyEnum;
    private final PaymentTypeData paymentTypeOptions;
    private final CurrencyData currency;
    private final BigDecimal amount;
    private final EnumOptionData chargeTimeType;
    private final EnumOptionData chargeAppliesTo;
    private final EnumOptionData chargeCalculationType;
    private final EnumOptionData chargePaymentMode;
    private final MonthDay feeOnMonthDay;
    private final Integer feeInterval;
    private final BigDecimal minCap;
    private final BigDecimal maxCap;
    private final EnumOptionData feeFrequency;
    private final GLAccountData incomeOrLiabilityAccount;
    private final TaxGroupData taxGroup;

    // template attributes
    private final Collection<CurrencyData> currencyOptions;
    private final List<EnumOptionData> chargeCalculationTypeOptions;//
    private final List<EnumOptionData> chargeAppliesToOptions;//
    private final List<EnumOptionData> chargeTimeTypeOptions;//
    private final List<EnumOptionData> chargePaymetModeOptions;//

    private final List<EnumOptionData> loanChargeCalculationTypeOptions;
    private final List<EnumOptionData> loanChargeTimeTypeOptions;
    private final List<EnumOptionData> savingsChargeCalculationTypeOptions;
    private final List<EnumOptionData> savingsChargeTimeTypeOptions;
    private final List<EnumOptionData> clientChargeCalculationTypeOptions;
    private final List<EnumOptionData> clientChargeTimeTypeOptions;
    private final List<EnumOptionData> shareChargeCalculationTypeOptions;
    private final List<EnumOptionData> shareChargeTimeTypeOptions;

    private final List<EnumOptionData> feeFrequencyOptions;

    private final Map<String, List<GLAccountData>> incomeOrLiabilityAccountOptions;
    private final Collection<TaxGroupData> taxGroupOptions;

    private final String accountMappingForChargeConfig;
    private final List<GLAccountData> expenseAccountOptions;
    private final List<GLAccountData> assetAccountOptions;

    public static ChargeData withTemplate(final ChargeData charge, final ChargeData template) {
        return charge.toBuilder().currencyOptions(template.getCurrencyOptions())
                .chargeCalculationTypeOptions(template.getChargeCalculationTypeOptions())
                .chargeAppliesToOptions(template.getChargeAppliesToOptions()).chargeTimeTypeOptions(template.getChargeTimeTypeOptions())
                .chargePaymetModeOptions(template.getChargePaymetModeOptions())
                .loanChargeCalculationTypeOptions(template.getLoanChargeCalculationTypeOptions())
                .loanChargeTimeTypeOptions(template.getLoanChargeTimeTypeOptions())
                .savingsChargeCalculationTypeOptions(template.getSavingsChargeCalculationTypeOptions())
                .savingsChargeTimeTypeOptions(template.getSavingsChargeTimeTypeOptions())
                .clientChargeCalculationTypeOptions(template.getClientChargeCalculationTypeOptions())
                .clientChargeTimeTypeOptions(template.getClientChargeTimeTypeOptions())
                .feeFrequencyOptions(template.getFeeFrequencyOptions())
                .incomeOrLiabilityAccountOptions(template.getIncomeOrLiabilityAccountOptions())
                .taxGroupOptions(template.getTaxGroupOptions())
                .shareChargeCalculationTypeOptions(template.getShareChargeCalculationTypeOptions())
                .shareChargeTimeTypeOptions(template.getShareChargeTimeTypeOptions())
                .accountMappingForChargeConfig(template.getAccountMappingForChargeConfig())
                .expenseAccountOptions(template.getExpenseAccountOptions()).assetAccountOptions(template.getAssetAccountOptions()).build();
    }

    @Override
    public int compareTo(final ChargeData obj) {
        if (obj == null) {
            return -1;
        }

        return obj.id.compareTo(this.id);
    }

    public boolean isOverdueInstallmentCharge() {
        boolean isOverdueInstallmentCharge = false;
        if (this.chargeTimeType != null) {
            isOverdueInstallmentCharge = ChargeTimeType.fromInt(this.chargeTimeType.getId().intValue()).isOverdueInstallment();
        }
        return isOverdueInstallmentCharge;
    }

    public boolean isIsPaymentType() {
        return this.isPaymentType;
    }
}
