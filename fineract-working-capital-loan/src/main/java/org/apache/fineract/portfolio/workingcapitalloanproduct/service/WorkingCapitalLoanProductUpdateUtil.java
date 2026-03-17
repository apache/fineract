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
package org.apache.fineract.portfolio.workingcapitalloanproduct.service;

import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.workingcapitalloanproduct.WorkingCapitalLoanProductConstants;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalAmortizationType;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanPeriodFrequencyType;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProduct;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductConfigurableAttributes;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductMinMaxConstraints;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductRelatedDetail;
import org.springframework.stereotype.Component;

/**
 * Utility for applying update (JsonCommand) to Working Capital Loan Product and its embedded parts. Keeps JsonCommand
 * out of domain entities.
 */
@Component
public class WorkingCapitalLoanProductUpdateUtil {

    /**
     * Update currency fields on the product from command.
     */
    public Map<String, Object> updateCurrency(final WorkingCapitalLoanProduct product, final JsonCommand command) {
        final Map<String, Object> changes = new HashMap<>();
        MonetaryCurrency current = product.getCurrency();
        if (current == null) {
            return changes;
        }
        if (command.isChangeInStringParameterNamed(WorkingCapitalLoanProductConstants.currencyCodeParamName, current.getCode())) {
            final String newCurrencyCode = command.stringValueOfParameterNamed(WorkingCapitalLoanProductConstants.currencyCodeParamName);
            changes.put(WorkingCapitalLoanProductConstants.currencyCodeParamName, newCurrencyCode);
            current = new MonetaryCurrency(newCurrencyCode, current.getDigitsAfterDecimal(), current.getInMultiplesOf());
            product.setCurrency(current);
        }
        if (command.isChangeInIntegerParameterNamed(WorkingCapitalLoanProductConstants.digitsAfterDecimalParamName,
                current.getDigitsAfterDecimal())) {
            final Integer newValue = command.integerValueOfParameterNamed(WorkingCapitalLoanProductConstants.digitsAfterDecimalParamName);
            changes.put(WorkingCapitalLoanProductConstants.digitsAfterDecimalParamName, newValue);
            current = new MonetaryCurrency(current.getCode(), newValue, current.getInMultiplesOf());
            product.setCurrency(current);
        }
        if (command.isChangeInIntegerParameterNamed(WorkingCapitalLoanProductConstants.inMultiplesOfParamName,
                current.getInMultiplesOf())) {
            final Integer newValue = command.integerValueOfParameterNamed(WorkingCapitalLoanProductConstants.inMultiplesOfParamName);
            changes.put(WorkingCapitalLoanProductConstants.inMultiplesOfParamName, newValue);
            product.setCurrency(new MonetaryCurrency(current.getCode(), current.getDigitsAfterDecimal(), newValue));
        }
        return changes;
    }

    /**
     * Update related detail (core product parameters) from command.
     */
    public Map<String, Object> updateRelatedDetail(final WorkingCapitalLoanProductRelatedDetail relatedDetail, final JsonCommand command) {
        final Map<String, Object> changes = new HashMap<>();
        if (command.isChangeInStringParameterNamed(WorkingCapitalLoanProductConstants.amortizationTypeParamName,
                relatedDetail.getAmortizationType().name())) {
            final String newValue = command.stringValueOfParameterNamed(WorkingCapitalLoanProductConstants.amortizationTypeParamName);
            changes.put(WorkingCapitalLoanProductConstants.amortizationTypeParamName, newValue);
            relatedDetail.setAmortizationType(WorkingCapitalAmortizationType.fromString(newValue));
        }
        if (command.isChangeInBigDecimalParameterNamed(WorkingCapitalLoanProductConstants.flatPercentageAmountParamName,
                relatedDetail.getFlatPercentageAmount())) {
            final BigDecimal newValue = command
                    .bigDecimalValueOfParameterNamed(WorkingCapitalLoanProductConstants.flatPercentageAmountParamName);
            changes.put(WorkingCapitalLoanProductConstants.flatPercentageAmountParamName, newValue);
            relatedDetail.setFlatPercentageAmount(newValue);
        }
        if (command.isChangeInIntegerParameterNamed(WorkingCapitalLoanProductConstants.npvDayCountParamName,
                relatedDetail.getNpvDayCount())) {
            final Integer newValue = command.integerValueOfParameterNamed(WorkingCapitalLoanProductConstants.npvDayCountParamName);
            changes.put(WorkingCapitalLoanProductConstants.npvDayCountParamName, newValue);
            relatedDetail.setNpvDayCount(newValue);
        }
        if (command.isChangeInBigDecimalParameterNamed(WorkingCapitalLoanProductConstants.principalParamName,
                relatedDetail.getPrincipal())) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(WorkingCapitalLoanProductConstants.principalParamName);
            changes.put(WorkingCapitalLoanProductConstants.principalParamName, newValue);
            relatedDetail.setPrincipal(newValue);
        }
        if (command.isChangeInBigDecimalParameterNamed(WorkingCapitalLoanProductConstants.periodPaymentRateParamName,
                relatedDetail.getPeriodPaymentRate())) {
            final BigDecimal newValue = command
                    .bigDecimalValueOfParameterNamed(WorkingCapitalLoanProductConstants.periodPaymentRateParamName);
            changes.put(WorkingCapitalLoanProductConstants.periodPaymentRateParamName, newValue);
            relatedDetail.setPeriodPaymentRate(newValue);
        }
        if (command.isChangeInIntegerParameterNamed(WorkingCapitalLoanProductConstants.repaymentEveryParamName,
                relatedDetail.getRepaymentEvery())) {
            final Integer newValue = command.integerValueOfParameterNamed(WorkingCapitalLoanProductConstants.repaymentEveryParamName);
            changes.put(WorkingCapitalLoanProductConstants.repaymentEveryParamName, newValue);
            relatedDetail.setRepaymentEvery(newValue);
        }
        if (command.isChangeInStringParameterNamed(WorkingCapitalLoanProductConstants.repaymentFrequencyTypeParamName,
                relatedDetail.getRepaymentFrequencyType().name())) {
            final String newValue = command.stringValueOfParameterNamed(WorkingCapitalLoanProductConstants.repaymentFrequencyTypeParamName);
            changes.put(WorkingCapitalLoanProductConstants.repaymentFrequencyTypeParamName, newValue);
            relatedDetail.setRepaymentFrequencyType(WorkingCapitalLoanPeriodFrequencyType.fromString(newValue));
        }
        if (command.isChangeInBigDecimalParameterNamed(WorkingCapitalLoanProductConstants.discountParamName, relatedDetail.getDiscount())) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(WorkingCapitalLoanProductConstants.discountParamName);
            changes.put(WorkingCapitalLoanProductConstants.discountParamName, newValue);
            relatedDetail.setDiscount(newValue);
        }
        return changes;
    }

    /**
     * Update min/max constraints from command.
     */
    public Map<String, Object> updateMinMaxConstraints(final WorkingCapitalLoanProductMinMaxConstraints minMaxConstraints,
            final JsonCommand command) {
        final Map<String, Object> changes = new HashMap<>();
        if (command.isChangeInBigDecimalParameterNamed(WorkingCapitalLoanProductConstants.minPrincipalParamName,
                minMaxConstraints.getMinPrincipal())) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(WorkingCapitalLoanProductConstants.minPrincipalParamName);
            changes.put(WorkingCapitalLoanProductConstants.minPrincipalParamName, newValue);
            minMaxConstraints.setMinPrincipal(newValue);
        }
        if (command.isChangeInBigDecimalParameterNamed(WorkingCapitalLoanProductConstants.maxPrincipalParamName,
                minMaxConstraints.getMaxPrincipal())) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(WorkingCapitalLoanProductConstants.maxPrincipalParamName);
            changes.put(WorkingCapitalLoanProductConstants.maxPrincipalParamName, newValue);
            minMaxConstraints.setMaxPrincipal(newValue);
        }
        if (command.isChangeInBigDecimalParameterNamed(WorkingCapitalLoanProductConstants.minPeriodPaymentRateParamName,
                minMaxConstraints.getMinPeriodPaymentRate())) {
            final BigDecimal newValue = command
                    .bigDecimalValueOfParameterNamed(WorkingCapitalLoanProductConstants.minPeriodPaymentRateParamName);
            changes.put(WorkingCapitalLoanProductConstants.minPeriodPaymentRateParamName, newValue);
            minMaxConstraints.setMinPeriodPaymentRate(newValue);
        }
        if (command.isChangeInBigDecimalParameterNamed(WorkingCapitalLoanProductConstants.maxPeriodPaymentRateParamName,
                minMaxConstraints.getMaxPeriodPaymentRate())) {
            final BigDecimal newValue = command
                    .bigDecimalValueOfParameterNamed(WorkingCapitalLoanProductConstants.maxPeriodPaymentRateParamName);
            changes.put(WorkingCapitalLoanProductConstants.maxPeriodPaymentRateParamName, newValue);
            minMaxConstraints.setMaxPeriodPaymentRate(newValue);
        }
        return changes;
    }

    /**
     * Update configurable attributes from command.
     */
    public Map<String, Object> updateConfigurableAttributes(final WorkingCapitalLoanProductConfigurableAttributes config,
            final JsonCommand command) {
        final Map<String, Object> changes = new HashMap<>();
        if (command.parameterExists(WorkingCapitalLoanProductConstants.allowAttributeOverridesParamName)) {
            final JsonObject allowOverrides = command.parsedJson().getAsJsonObject()
                    .getAsJsonObject(WorkingCapitalLoanProductConstants.allowAttributeOverridesParamName);
            if (allowOverrides != null && !allowOverrides.isJsonNull()) {
                updateBooleanField(allowOverrides, WorkingCapitalLoanProductConstants.flatPercentageAmountOverridableParamName,
                        config::setFlatPercentageAmount, config::getFlatPercentageAmount, changes);
                updateBooleanField(allowOverrides, WorkingCapitalLoanProductConstants.delinquencyBucketClassificationOverridableParamName,
                        config::setDelinquencyBucketClassification, config::getDelinquencyBucketClassification, changes);
                updateBooleanField(allowOverrides, WorkingCapitalLoanProductConstants.discountDefaultOverridableParamName,
                        config::setDiscountDefault, config::getDiscountDefault, changes);
                updateBooleanField(allowOverrides, WorkingCapitalLoanProductConstants.periodPaymentFrequencyOverridableParamName,
                        config::setPeriodPaymentFrequency, config::getPeriodPaymentFrequency, changes);
                updateBooleanField(allowOverrides, WorkingCapitalLoanProductConstants.periodPaymentFrequencyTypeOverridableParamName,
                        config::setPeriodPaymentFrequencyType, config::getPeriodPaymentFrequencyType, changes);
            }
        }
        return changes;
    }

    private static void updateBooleanField(final JsonObject allowOverrides, final String paramName, final Consumer<Boolean> setter,
            final Supplier<Boolean> getter, final Map<String, Object> changes) {
        if (allowOverrides.has(paramName)) {
            final Boolean newValue = allowOverrides.get(paramName).getAsBoolean();
            if (!Objects.equals(getter.get(), newValue)) {
                changes.put(paramName, newValue);
                setter.accept(newValue);
            }
        }
    }
}
