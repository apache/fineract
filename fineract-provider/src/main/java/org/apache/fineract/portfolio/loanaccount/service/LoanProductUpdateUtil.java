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
package org.apache.fineract.portfolio.loanaccount.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.accounting.common.AccountingRuleType;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.portfolio.floatingrates.domain.FloatingRate;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.AprCalculator;
import org.apache.fineract.portfolio.loanproduct.LoanProductConstants;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductBorrowerCycleVariations;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductConfigurableAttributes;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductParamType;
import org.apache.fineract.portfolio.loanproduct.domain.RepaymentStartDateType;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoanProductUpdateUtil {

    private final LoanProductRelatedDetailUpdateUtil detailUpdateUtil;

    private final LoanProductMinMaxConstraintsUpdateUtil minMaxConstraintsUpdateUtil;

    private final LoanProductFloatingRatesUpdateUtil floatingRatesUpdateUtil;

    private final LoanProductVariableInstallmentConfigUpdateUtil variableInstallmentConfigUpdateUtil;

    private final LoanProductTrancheDetailsUpdateUtil trancheDetailsUpdateUtil;

    private final LoanProductInterestRecalculationDetailsAssembler interestRecalculationDetailsAssembler;

    private final LoanProductInterestRecalculationDetailsUpdateUtil interestRecalculationDetailsUpdateUtil;

    private final LoanProductGuaranteeDetailsAssembler guaranteeDetailsAssembler;

    private final LoanProductGuaranteeDetailsUpdateUtil guaranteeDetailsUpdateUtil;

    public Map<String, Object> update(final LoanProduct loanProduct, final JsonCommand command, final AprCalculator aprCalculator,
            FloatingRate floatingRate) {
        final Map<String, Object> actualChanges = detailUpdateUtil.updateLoanRepaymentSchedule(loanProduct.getLoanProductRelatedDetail(),
                command, aprCalculator);
        actualChanges.putAll(minMaxConstraintsUpdateUtil.update(loanProduct.loanProductMinMaxConstraints(), command));

        final String isLinkedToFloatingInterestRates = "isLinkedToFloatingInterestRates";
        if (command.isChangeInBooleanParameterNamed(isLinkedToFloatingInterestRates, loanProduct.isLinkedToFloatingInterestRate())) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(isLinkedToFloatingInterestRates);
            actualChanges.put(isLinkedToFloatingInterestRates, newValue);
            loanProduct.setLinkedToFloatingInterestRate(newValue);
        }

        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.ENABLE_ACCRUAL_ACTIVITY_POSTING,
                loanProduct.getLoanProductRelatedDetail().isEnableAccrualActivityPosting())) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.ENABLE_ACCRUAL_ACTIVITY_POSTING);
            actualChanges.put(LoanProductConstants.ENABLE_ACCRUAL_ACTIVITY_POSTING, newValue);
            loanProduct.getLoanProductRelatedDetail().setEnableAccrualActivityPosting(newValue);
        }

        if (loanProduct.isLinkedToFloatingInterestRate()) {
            actualChanges.putAll(floatingRatesUpdateUtil.update(loanProduct.loanProductFloatingRates(), command, floatingRate));
            loanProduct.getLoanProductRelatedDetail().updateForFloatingInterestRates();
            loanProduct.getLoanProductMinMaxConstraints().updateForFloatingInterestRates();
        } else {
            loanProduct.setFloatingRates(null);
        }

        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.allowVariableInstallmentsParamName,
                loanProduct.isAllowVariabeInstallments())) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.allowVariableInstallmentsParamName);
            actualChanges.put(LoanProductConstants.allowVariableInstallmentsParamName, newValue);
            loanProduct.setAllowVariabeInstallments(newValue);
        }

        if (loanProduct.isAllowVariabeInstallments()) {
            actualChanges.putAll(variableInstallmentConfigUpdateUtil.update(loanProduct.loanProductVariableInstallmentConfig(), command));
        } else {
            loanProduct.setVariableInstallmentConfig(null);
        }

        final String accountingTypeParamName = "accountingRule";
        Integer currentValue = loanProduct.getAccountingRule() == null ? null : loanProduct.getAccountingRule().getValue();
        if (command.isChangeInIntegerParameterNamed(accountingTypeParamName, currentValue)) {
            final Integer newValue = command.integerValueOfParameterNamed(accountingTypeParamName);
            actualChanges.put(accountingTypeParamName, newValue);
            loanProduct.setAccountingRule(AccountingRuleType.fromInt(newValue));
        }

        final String nameParamName = "name";
        if (command.isChangeInStringParameterNamed(nameParamName, loanProduct.getName())) {
            final String newValue = command.stringValueOfParameterNamed(nameParamName);
            actualChanges.put(nameParamName, newValue);
            loanProduct.setName(newValue);
        }

        final String shortNameParamName = LoanProductConstants.SHORT_NAME;
        if (command.isChangeInStringParameterNamed(shortNameParamName, loanProduct.getShortName())) {
            final String newValue = command.stringValueOfParameterNamed(shortNameParamName);
            actualChanges.put(shortNameParamName, newValue);
            loanProduct.setShortName(newValue);
        }

        final String descriptionParamName = "description";
        if (command.isChangeInStringParameterNamed(descriptionParamName, loanProduct.getDescription())) {
            final String newValue = command.stringValueOfParameterNamed(descriptionParamName);
            actualChanges.put(descriptionParamName, newValue);
            loanProduct.setDescription(newValue);
        }

        Long existingFundId = null;
        if (loanProduct.getFund() != null) {
            existingFundId = loanProduct.getFund().getId();
        }
        final String fundIdParamName = "fundId";
        if (command.isChangeInLongParameterNamed(fundIdParamName, existingFundId)) {
            final Long newValue = command.longValueOfParameterNamed(fundIdParamName);
            actualChanges.put(fundIdParamName, newValue);
        }

        final String transactionProcessingStrategyCodeParamName = "transactionProcessingStrategyCode";
        if (command.isChangeInStringParameterNamed(transactionProcessingStrategyCodeParamName,
                loanProduct.getTransactionProcessingStrategyCode())) {
            final String newValue = command.stringValueOfParameterNamed(transactionProcessingStrategyCodeParamName);
            actualChanges.put(transactionProcessingStrategyCodeParamName, newValue);
        }

        final String paymentAllocationParamName = "paymentAllocation";
        if (command.hasParameter(paymentAllocationParamName)) {
            final JsonArray jsonArray = command.arrayOfParameterNamed(paymentAllocationParamName);
            if (jsonArray != null) {
                actualChanges.put(paymentAllocationParamName, command.jsonFragment(paymentAllocationParamName));
            }
        }

        final String creditAllocationParamName = "creditAllocation";
        if (command.hasParameter(creditAllocationParamName)) {
            final JsonArray jsonArray = command.arrayOfParameterNamed(creditAllocationParamName);
            if (jsonArray != null) {
                actualChanges.put(creditAllocationParamName, command.jsonFragment(creditAllocationParamName));
            }
        }

        final String chargesParamName = "charges";
        if (command.hasParameter(chargesParamName)) {
            final JsonArray jsonArray = command.arrayOfParameterNamed(chargesParamName);
            if (jsonArray != null) {
                actualChanges.put(chargesParamName, command.jsonFragment(chargesParamName));
            }
        }

        final String includeInBorrowerCycleParamName = "includeInBorrowerCycle";
        if (command.isChangeInBooleanParameterNamed(includeInBorrowerCycleParamName, loanProduct.isIncludeInBorrowerCycle())) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(includeInBorrowerCycleParamName);
            actualChanges.put(includeInBorrowerCycleParamName, newValue);
            loanProduct.setIncludeInBorrowerCycle(newValue);
        }

        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.USE_BORROWER_CYCLE_PARAMETER_NAME,
                loanProduct.isUseBorrowerCycle())) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.USE_BORROWER_CYCLE_PARAMETER_NAME);
            actualChanges.put(LoanProductConstants.USE_BORROWER_CYCLE_PARAMETER_NAME, newValue);
            loanProduct.setUseBorrowerCycle(newValue);
        }

        if (loanProduct.isUseBorrowerCycle()) {
            actualChanges.putAll(updateBorrowerCycleVariations(loanProduct, command));
        } else {
            clearVariations(loanProduct);
        }
        final String dateFormatAsInput = command.dateFormat();
        final String localeAsInput = command.locale();

        final String localeParamName = "locale";
        final String dateFormatParamName = "dateFormat";

        final String startDateParamName = "startDate";
        if (command.isChangeInLocalDateParameterNamed(startDateParamName, loanProduct.getStartDate())) {
            final String valueAsInput = command.stringValueOfParameterNamed(startDateParamName);
            actualChanges.put(startDateParamName, valueAsInput);
            actualChanges.put(dateFormatParamName, dateFormatAsInput);
            actualChanges.put(localeParamName, localeAsInput);

            loanProduct.setStartDate(command.localDateValueOfParameterNamed(startDateParamName));
        }

        final String closeDateParamName = "closeDate";
        if (command.isChangeInLocalDateParameterNamed(closeDateParamName, loanProduct.getCloseDate())) {
            final String valueAsInput = command.stringValueOfParameterNamed(closeDateParamName);
            actualChanges.put(closeDateParamName, valueAsInput);
            actualChanges.put(dateFormatParamName, dateFormatAsInput);
            actualChanges.put(localeParamName, localeAsInput);

            loanProduct.setCloseDate(command.localDateValueOfParameterNamed(closeDateParamName));
        }

        final String externalIdTypeParamName = "externalId";
        if (command.isChangeInExternalIdParameterNamed(externalIdTypeParamName, loanProduct.getExternalId())) {
            final ExternalId newValue = ExternalIdFactory.produce(command.stringValueOfParameterNamed(externalIdTypeParamName));
            actualChanges.put(accountingTypeParamName, newValue);
            loanProduct.setExternalId(newValue);
        }
        trancheDetailsUpdateUtil.update(loanProduct.getLoanProductTrancheDetails(), command, actualChanges);

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.OVERDUE_DAYS_FOR_NPA_PARAMETER_NAME,
                loanProduct.getOverdueDaysForNPA())) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.OVERDUE_DAYS_FOR_NPA_PARAMETER_NAME);
            actualChanges.put(LoanProductConstants.OVERDUE_DAYS_FOR_NPA_PARAMETER_NAME, newValue);
            actualChanges.put("locale", localeAsInput);
            loanProduct.setOverdueDaysForNPA(newValue);
        }

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.MINIMUM_DAYS_BETWEEN_DISBURSAL_AND_FIRST_REPAYMENT,
                loanProduct.getMinimumDaysBetweenDisbursalAndFirstRepayment())) {
            final Integer newValue = command
                    .integerValueOfParameterNamed(LoanProductConstants.MINIMUM_DAYS_BETWEEN_DISBURSAL_AND_FIRST_REPAYMENT);
            actualChanges.put(LoanProductConstants.MINIMUM_DAYS_BETWEEN_DISBURSAL_AND_FIRST_REPAYMENT, newValue);
            actualChanges.put("locale", localeAsInput);
            loanProduct.setMinimumDaysBetweenDisbursalAndFirstRepayment(newValue);
        }

        if (command.isChangeInBooleanParameterNamed("syncExpectedWithDisbursementDate", loanProduct.isSyncExpectedWithDisbursementDate())) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed("syncExpectedWithDisbursementDate");
            actualChanges.put("syncExpectedWithDisbursementDate", newValue);
            loanProduct.setSyncExpectedWithDisbursementDate(newValue);
        }

        Long delinquencyBucketId = null;
        if (loanProduct.getDelinquencyBucket() != null) {
            delinquencyBucketId = loanProduct.getDelinquencyBucket().getId();
        }
        if (command.isChangeInLongParameterNamed(LoanProductConstants.DELINQUENCY_BUCKET_PARAM_NAME, delinquencyBucketId)) {
            final Long newValue = command.longValueOfParameterNamed(LoanProductConstants.DELINQUENCY_BUCKET_PARAM_NAME);
            actualChanges.put(LoanProductConstants.DELINQUENCY_BUCKET_PARAM_NAME, newValue);
        }

        // Update interest recalculation settings
        final boolean isInterestRecalculationEnabledChanged = actualChanges
                .containsKey(LoanProductConstants.IS_INTEREST_RECALCULATION_ENABLED_PARAMETER_NAME);

        if (isInterestRecalculationEnabledChanged) {
            if (loanProduct.isInterestRecalculationEnabled()) {
                loanProduct.setProductInterestRecalculationDetails(interestRecalculationDetailsAssembler.createFrom(command));
                loanProduct.getProductInterestRecalculationDetails().updateProduct(loanProduct);
                actualChanges.put(LoanProductConstants.interestRecalculationCompoundingMethodParameterName,
                        command.integerValueOfParameterNamed(LoanProductConstants.interestRecalculationCompoundingMethodParameterName));
                actualChanges.put(LoanProductConstants.rescheduleStrategyMethodParameterName,
                        command.integerValueOfParameterNamed(LoanProductConstants.rescheduleStrategyMethodParameterName));
            } else {
                loanProduct.setProductInterestRecalculationDetails(null);
            }
        }

        if (!isInterestRecalculationEnabledChanged && loanProduct.isInterestRecalculationEnabled()) {
            interestRecalculationDetailsUpdateUtil.update(loanProduct.getProductInterestRecalculationDetails(), command, actualChanges,
                    localeAsInput);
        }

        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.holdGuaranteeFundsParamName, loanProduct.isHoldGuaranteeFunds())) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.holdGuaranteeFundsParamName);
            actualChanges.put(LoanProductConstants.holdGuaranteeFundsParamName, newValue);
            loanProduct.setHoldGuaranteeFunds(newValue);
        }

        final String configurableAttributesChanges = LoanProductConstants.allowAttributeOverridesParamName;
        if (command.hasParameter(configurableAttributesChanges)) {
            if (!command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                    .isJsonNull()) {
                actualChanges.put(configurableAttributesChanges, command.jsonFragment(configurableAttributesChanges));

                if (command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                        .getAsJsonPrimitive(LoanProductConstants.amortizationTypeParamName)
                        .getAsBoolean() != loanProduct.getLoanConfigurableAttributes().getAmortizationBoolean()) {
                    loanProduct.getLoanConfigurableAttributes()
                            .setAmortizationType(command.parsedJson().getAsJsonObject()
                                    .getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                                    .getAsJsonPrimitive(LoanProductConstants.amortizationTypeParamName).getAsBoolean());
                }

                if (command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                        .getAsJsonPrimitive(LoanProductConstants.interestTypeParamName)
                        .getAsBoolean() != loanProduct.getLoanConfigurableAttributes().getInterestMethodBoolean()) {
                    loanProduct.getLoanConfigurableAttributes()
                            .setInterestType(command.parsedJson().getAsJsonObject()
                                    .getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                                    .getAsJsonPrimitive(LoanProductConstants.interestTypeParamName).getAsBoolean());
                }

                if (command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                        .getAsJsonPrimitive(LoanProductConstants.transactionProcessingStrategyCodeParamName)
                        .getAsBoolean() != loanProduct.getLoanConfigurableAttributes().getTransactionProcessingStrategyBoolean()) {
                    loanProduct.getLoanConfigurableAttributes()
                            .setTransactionProcessingStrategyCode(command.parsedJson().getAsJsonObject()
                                    .getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                                    .getAsJsonPrimitive(LoanProductConstants.transactionProcessingStrategyCodeParamName).getAsBoolean());
                }

                if (command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                        .getAsJsonPrimitive(LoanProductConstants.interestCalculationPeriodTypeParamName)
                        .getAsBoolean() != loanProduct.getLoanConfigurableAttributes().getInterestCalcPeriodBoolean()) {
                    loanProduct.getLoanConfigurableAttributes()
                            .setInterestCalculationPeriodType(command.parsedJson().getAsJsonObject()
                                    .getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                                    .getAsJsonPrimitive(LoanProductConstants.interestCalculationPeriodTypeParamName).getAsBoolean());
                }

                if (command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                        .getAsJsonPrimitive(LoanProductConstants.inArrearsToleranceParamName)
                        .getAsBoolean() != loanProduct.getLoanConfigurableAttributes().getArrearsToleranceBoolean()) {
                    loanProduct.getLoanConfigurableAttributes()
                            .setInArrearsTolerance(command.parsedJson().getAsJsonObject()
                                    .getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                                    .getAsJsonPrimitive(LoanProductConstants.inArrearsToleranceParamName).getAsBoolean());
                }

                if (command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                        .getAsJsonPrimitive(LoanProductConstants.repaymentEveryParamName)
                        .getAsBoolean() != loanProduct.getLoanConfigurableAttributes().getRepaymentEveryBoolean()) {
                    loanProduct.getLoanConfigurableAttributes()
                            .setRepaymentEvery(command.parsedJson().getAsJsonObject()
                                    .getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                                    .getAsJsonPrimitive(LoanProductConstants.repaymentEveryParamName).getAsBoolean());
                }

                if (command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                        .getAsJsonPrimitive(LoanProductConstants.graceOnPrincipalAndInterestPaymentParamName)
                        .getAsBoolean() != loanProduct.getLoanConfigurableAttributes().getGraceOnPrincipalAndInterestPaymentBoolean()) {
                    loanProduct.getLoanConfigurableAttributes()
                            .setGraceOnPrincipalAndInterestPayment(command.parsedJson().getAsJsonObject()
                                    .getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                                    .getAsJsonPrimitive(LoanProductConstants.graceOnPrincipalAndInterestPaymentParamName).getAsBoolean());
                }

                if (command.parsedJson().getAsJsonObject().getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                        .getAsJsonPrimitive(LoanProductConstants.GRACE_ON_ARREARS_AGEING_PARAMETER_NAME)
                        .getAsBoolean() != loanProduct.getLoanConfigurableAttributes().getGraceOnArrearsAgingBoolean()) {
                    loanProduct.getLoanConfigurableAttributes()
                            .setGraceOnArrearsAgeing(command.parsedJson().getAsJsonObject()
                                    .getAsJsonObject(LoanProductConstants.allowAttributeOverridesParamName)
                                    .getAsJsonPrimitive(LoanProductConstants.GRACE_ON_ARREARS_AGEING_PARAMETER_NAME).getAsBoolean());
                }
            } else {
                loanProduct.setLoanConfigurableAttributes(LoanProductConfigurableAttributes.populateDefaultsForConfigurableAttributes());
                loanProduct.getLoanConfigurableAttributes().updateLoanProduct(loanProduct);
            }
        }

        if (actualChanges.containsKey(LoanProductConstants.holdGuaranteeFundsParamName)) {
            if (loanProduct.isHoldGuaranteeFunds()) {
                loanProduct.setLoanProductGuaranteeDetails(guaranteeDetailsAssembler.createFrom(command));
                loanProduct.getLoanProductGuaranteeDetails().updateProduct(loanProduct);
                actualChanges.put(LoanProductConstants.mandatoryGuaranteeParamName,
                        loanProduct.getLoanProductGuaranteeDetails().getMandatoryGuarantee());
                actualChanges.put(LoanProductConstants.minimumGuaranteeFromGuarantorParamName,
                        loanProduct.getLoanProductGuaranteeDetails().getMinimumGuaranteeFromGuarantor());
                actualChanges.put(LoanProductConstants.minimumGuaranteeFromOwnFundsParamName,
                        loanProduct.getLoanProductGuaranteeDetails().getMinimumGuaranteeFromOwnFunds());
            } else {
                loanProduct.setLoanProductRelatedDetail(null);
            }

        } else if (loanProduct.isHoldGuaranteeFunds()) {
            guaranteeDetailsUpdateUtil.update(loanProduct.getLoanProductGuaranteeDetails(), command, actualChanges);
        }

        if (command.isChangeInBigDecimalParameterNamed(LoanProductConstants.principalThresholdForLastInstallmentParamName,
                loanProduct.getPrincipalThresholdForLastInstallment())) {
            BigDecimal newValue = command
                    .bigDecimalValueOfParameterNamed(LoanProductConstants.principalThresholdForLastInstallmentParamName);
            actualChanges.put(LoanProductConstants.principalThresholdForLastInstallmentParamName, newValue);
            loanProduct.setPrincipalThresholdForLastInstallment(newValue);
        }
        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.ACCOUNT_MOVES_OUT_OF_NPA_ONLY_ON_ARREARS_COMPLETION_PARAM_NAME,
                loanProduct.isAccountMovesOutOfNPAOnlyOnArrearsCompletion())) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(
                    LoanProductConstants.ACCOUNT_MOVES_OUT_OF_NPA_ONLY_ON_ARREARS_COMPLETION_PARAM_NAME);
            actualChanges.put(LoanProductConstants.ACCOUNT_MOVES_OUT_OF_NPA_ONLY_ON_ARREARS_COMPLETION_PARAM_NAME, newValue);
            loanProduct.setAccountMovesOutOfNPAOnlyOnArrearsCompletion(newValue);
        }
        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.canDefineEmiAmountParamName,
                loanProduct.isCanDefineInstallmentAmount())) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.canDefineEmiAmountParamName);
            actualChanges.put(LoanProductConstants.canDefineEmiAmountParamName, newValue);
            loanProduct.setCanDefineInstallmentAmount(newValue);
        }

        if (command.isChangeInIntegerParameterNamedWithNullCheck(LoanProductConstants.installmentAmountInMultiplesOfParamName,
                loanProduct.getLoanProductRelatedDetail().getInstallmentAmountInMultiplesOf())) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.installmentAmountInMultiplesOfParamName);
            actualChanges.put(LoanProductConstants.installmentAmountInMultiplesOfParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            loanProduct.getLoanProductRelatedDetail().setInstallmentAmountInMultiplesOf(newValue);
        }

        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.CAN_USE_FOR_TOPUP, loanProduct.isCanUseForTopup())) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.CAN_USE_FOR_TOPUP);
            actualChanges.put(LoanProductConstants.CAN_USE_FOR_TOPUP, newValue);
            loanProduct.setCanUseForTopup(newValue);
        }

        if (command.hasParameter(LoanProductConstants.RATES_PARAM_NAME)) {
            final JsonArray jsonArray = command.arrayOfParameterNamed(LoanProductConstants.RATES_PARAM_NAME);
            if (jsonArray != null) {
                actualChanges.put(LoanProductConstants.RATES_PARAM_NAME, command.jsonFragment(LoanProductConstants.RATES_PARAM_NAME));
            }
        }

        if (command.isChangeInBigDecimalParameterNamed(LoanProductConstants.fixedPrincipalPercentagePerInstallmentParamName,
                loanProduct.getFixedPrincipalPercentagePerInstallment())) {
            BigDecimal newValue = command
                    .bigDecimalValueOfParameterNamed(LoanProductConstants.fixedPrincipalPercentagePerInstallmentParamName);
            actualChanges.put(LoanProductConstants.fixedPrincipalPercentagePerInstallmentParamName, newValue);
            loanProduct.setFixedPrincipalPercentagePerInstallment(newValue);
        }

        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.DISALLOW_EXPECTED_DISBURSEMENTS,
                loanProduct.isDisallowExpectedDisbursements())) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.DISALLOW_EXPECTED_DISBURSEMENTS);
            actualChanges.put(LoanProductConstants.DISALLOW_EXPECTED_DISBURSEMENTS, newValue);
            loanProduct.setDisallowExpectedDisbursements(newValue);
        }

        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.ALLOW_APPROVED_DISBURSED_AMOUNTS_OVER_APPLIED,
                loanProduct.isAllowApprovedDisbursedAmountsOverApplied())) {
            final boolean newValue = command
                    .booleanPrimitiveValueOfParameterNamed(LoanProductConstants.ALLOW_APPROVED_DISBURSED_AMOUNTS_OVER_APPLIED);
            actualChanges.put(LoanProductConstants.ALLOW_APPROVED_DISBURSED_AMOUNTS_OVER_APPLIED, newValue);
            loanProduct.setAllowApprovedDisbursedAmountsOverApplied(newValue);
        }

        if (command.isChangeInStringParameterNamed(LoanProductConstants.OVER_APPLIED_CALCULATION_TYPE,
                loanProduct.getOverAppliedCalculationType())) {
            final String newValue = command.stringValueOfParameterNamed(LoanProductConstants.OVER_APPLIED_CALCULATION_TYPE);
            actualChanges.put(LoanProductConstants.OVER_APPLIED_CALCULATION_TYPE, newValue);
            loanProduct.setOverAppliedCalculationType(newValue);
        }

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.OVER_APPLIED_NUMBER, loanProduct.getOverAppliedNumber())) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.OVER_APPLIED_NUMBER);
            actualChanges.put(LoanProductConstants.OVER_APPLIED_NUMBER, newValue);
            actualChanges.put("locale", localeAsInput);
            loanProduct.setOverAppliedNumber(newValue);
        }

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.DUE_DAYS_FOR_REPAYMENT_EVENT,
                loanProduct.getDueDaysForRepaymentEvent())) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.DUE_DAYS_FOR_REPAYMENT_EVENT);
            actualChanges.put(LoanProductConstants.DUE_DAYS_FOR_REPAYMENT_EVENT, newValue);
            actualChanges.put("locale", localeAsInput);
            loanProduct.setDueDaysForRepaymentEvent(newValue);
        }

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.OVER_DUE_DAYS_FOR_REPAYMENT_EVENT,
                loanProduct.getOverDueDaysForRepaymentEvent())) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.OVER_DUE_DAYS_FOR_REPAYMENT_EVENT);
            actualChanges.put(LoanProductConstants.OVER_DUE_DAYS_FOR_REPAYMENT_EVENT, newValue);
            actualChanges.put("locale", localeAsInput);
            loanProduct.setOverDueDaysForRepaymentEvent(newValue);
        }

        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.ENABLE_DOWN_PAYMENT,
                loanProduct.getLoanProductRelatedDetail().isEnableDownPayment())) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.ENABLE_DOWN_PAYMENT);
            actualChanges.put(LoanProductConstants.ENABLE_DOWN_PAYMENT, newValue);
            loanProduct.getLoanProductRelatedDetail().setEnableDownPayment(newValue);
        }

        if (command.isChangeInBigDecimalParameterNamed(LoanProductConstants.DISBURSED_AMOUNT_PERCENTAGE_DOWN_PAYMENT,
                loanProduct.getLoanProductRelatedDetail().getDisbursedAmountPercentageForDownPayment())) {
            BigDecimal newValue = command.bigDecimalValueOfParameterNamed(LoanProductConstants.DISBURSED_AMOUNT_PERCENTAGE_DOWN_PAYMENT);
            actualChanges.put(LoanProductConstants.DISBURSED_AMOUNT_PERCENTAGE_DOWN_PAYMENT, newValue);
            loanProduct.getLoanProductRelatedDetail().setDisbursedAmountPercentageForDownPayment(newValue);
        }

        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.ENABLE_AUTO_REPAYMENT_DOWN_PAYMENT,
                loanProduct.getLoanProductRelatedDetail().isEnableAutoRepaymentForDownPayment())) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.ENABLE_AUTO_REPAYMENT_DOWN_PAYMENT);
            actualChanges.put(LoanProductConstants.ENABLE_AUTO_REPAYMENT_DOWN_PAYMENT, newValue);
            loanProduct.getLoanProductRelatedDetail().setEnableAutoRepaymentForDownPayment(newValue);
        }

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.REPAYMENT_START_DATE_TYPE,
                loanProduct.getRepaymentStartDateType().getValue())) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.REPAYMENT_START_DATE_TYPE);
            actualChanges.put(LoanProductConstants.REPAYMENT_START_DATE_TYPE, newValue);
            loanProduct.setRepaymentStartDateType(RepaymentStartDateType.fromInt(newValue));
        }

        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.ENABLE_INSTALLMENT_LEVEL_DELINQUENCY,
                loanProduct.isEnableInstallmentLevelDelinquency())) {
            final boolean newValue = command
                    .booleanPrimitiveValueOfParameterNamed(LoanProductConstants.ENABLE_INSTALLMENT_LEVEL_DELINQUENCY);
            actualChanges.put(LoanProductConstants.ENABLE_INSTALLMENT_LEVEL_DELINQUENCY, newValue);
            loanProduct.updateEnableInstallmentLevelDelinquency(newValue);
        }

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.FIXED_LENGTH,
                loanProduct.getLoanProductRelatedDetail().getFixedLength())) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.FIXED_LENGTH);
            actualChanges.put(LoanProductConstants.FIXED_LENGTH, newValue);
            loanProduct.getLoanProductRelatedDetail().setFixedLength(newValue);
        }

        return actualChanges;
    }

    private Map<String, Object> updateBorrowerCycleVariations(final LoanProduct loanProduct, final JsonCommand command) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(20);
        List<Long> variationIds = fetchAllVariationIds(loanProduct);
        updateBorrowerCycleVariations(loanProduct, command, LoanProductParamType.PRINCIPAL.getValue(),
                LoanProductConstants.PRINCIPAL_VARIATIONS_FOR_BORROWER_CYCLE_PARAMETER_NAME, actualChanges, variationIds);
        updateBorrowerCycleVariations(loanProduct, command, LoanProductParamType.INTERESTRATE.getValue(),
                LoanProductConstants.INTEREST_RATE_VARIATIONS_FOR_BORROWER_CYCLE_PARAMETER_NAME, actualChanges, variationIds);
        updateBorrowerCycleVariations(loanProduct, command, LoanProductParamType.REPAYMENT.getValue(),
                LoanProductConstants.NUMBER_OF_REPAYMENT_VARIATIONS_FOR_BORROWER_CYCLE_PARAMETER_NAME, actualChanges, variationIds);
        for (Long id : variationIds) {
            loanProduct.getBorrowerCycleVariations().remove(fetchLoanProductBorrowerCycleVariationById(loanProduct, id));
        }
        return actualChanges;
    }

    public LoanProductBorrowerCycleVariations fetchLoanProductBorrowerCycleVariationById(LoanProduct loanProduct, Long id) {
        LoanProductBorrowerCycleVariations borrowerCycleVariation = null;
        for (LoanProductBorrowerCycleVariations cycleVariation : loanProduct.getBorrowerCycleVariations()) {
            if (id.equals(cycleVariation.getId())) {
                borrowerCycleVariation = cycleVariation;
                break;
            }
        }
        return borrowerCycleVariation;
    }

    private List<Long> fetchAllVariationIds(LoanProduct loanProduct) {
        List<Long> list = new ArrayList<>();
        for (LoanProductBorrowerCycleVariations cycleVariation : loanProduct.getBorrowerCycleVariations()) {
            list.add(cycleVariation.getId());
        }
        return list;
    }

    private void updateBorrowerCycleVariations(final LoanProduct loanProduct, final JsonCommand command, Integer paramType,
            String variationParameterName, final Map<String, Object> actualChanges, List<Long> variationIds) {
        if (command.parameterExists(variationParameterName)) {
            final JsonArray variationArray = command.arrayOfParameterNamed(variationParameterName);
            if (variationArray != null && variationArray.size() > 0) {
                int i = 0;
                do {
                    final JsonObject jsonObject = variationArray.get(i).getAsJsonObject();
                    BigDecimal defaultValue = null;
                    BigDecimal minValue = null;
                    BigDecimal maxValue = null;
                    Integer cycleNumber = null;
                    Integer valueUsageCondition = null;
                    Long id = null;
                    if (jsonObject.has(LoanProductConstants.DEFAULT_VALUE_PARAMETER_NAME)
                            && jsonObject.get(LoanProductConstants.DEFAULT_VALUE_PARAMETER_NAME).isJsonPrimitive()) {
                        defaultValue = jsonObject.getAsJsonPrimitive(LoanProductConstants.DEFAULT_VALUE_PARAMETER_NAME).getAsBigDecimal();
                    }
                    if (jsonObject.has(LoanProductConstants.MIN_VALUE_PARAMETER_NAME)
                            && jsonObject.get(LoanProductConstants.MIN_VALUE_PARAMETER_NAME).isJsonPrimitive()
                            && StringUtils.isNotBlank(jsonObject.get(LoanProductConstants.MIN_VALUE_PARAMETER_NAME).getAsString())) {
                        minValue = jsonObject.getAsJsonPrimitive(LoanProductConstants.MIN_VALUE_PARAMETER_NAME).getAsBigDecimal();
                    }
                    if (jsonObject.has(LoanProductConstants.MAX_VALUE_PARAMETER_NAME)
                            && jsonObject.get(LoanProductConstants.MAX_VALUE_PARAMETER_NAME).isJsonPrimitive()
                            && StringUtils.isNotBlank(jsonObject.get(LoanProductConstants.MAX_VALUE_PARAMETER_NAME).getAsString())) {
                        maxValue = jsonObject.getAsJsonPrimitive(LoanProductConstants.MAX_VALUE_PARAMETER_NAME).getAsBigDecimal();
                    }
                    if (jsonObject.has(LoanProductConstants.BORROWER_CYCLE_NUMBER_PARAM_NAME)
                            && jsonObject.get(LoanProductConstants.BORROWER_CYCLE_NUMBER_PARAM_NAME).isJsonPrimitive()) {
                        cycleNumber = jsonObject.getAsJsonPrimitive(LoanProductConstants.BORROWER_CYCLE_NUMBER_PARAM_NAME).getAsInt();
                    }
                    if (jsonObject.has(LoanProductConstants.VALUE_CONDITION_TYPE_PARAM_NAME)
                            && jsonObject.get(LoanProductConstants.VALUE_CONDITION_TYPE_PARAM_NAME).isJsonPrimitive()) {
                        valueUsageCondition = jsonObject.getAsJsonPrimitive(LoanProductConstants.VALUE_CONDITION_TYPE_PARAM_NAME)
                                .getAsInt();
                    }
                    if (jsonObject.has(LoanProductConstants.BORROWER_CYCLE_ID_PARAMETER_NAME)
                            && jsonObject.get(LoanProductConstants.BORROWER_CYCLE_ID_PARAMETER_NAME).isJsonPrimitive() && StringUtils
                                    .isNotBlank(jsonObject.get(LoanProductConstants.BORROWER_CYCLE_ID_PARAMETER_NAME).getAsString())) {
                        id = jsonObject.getAsJsonPrimitive(LoanProductConstants.BORROWER_CYCLE_ID_PARAMETER_NAME).getAsLong();
                    }
                    LoanProductBorrowerCycleVariations borrowerCycleVariations = new LoanProductBorrowerCycleVariations(cycleNumber,
                            paramType, valueUsageCondition, minValue, maxValue, defaultValue);
                    if (id == null) {
                        borrowerCycleVariations.updateLoanProduct(loanProduct);
                        loanProduct.getBorrowerCycleVariations().add(borrowerCycleVariations);
                        actualChanges.put("borrowerCycleParamType", paramType);
                    } else {
                        variationIds.remove(id);
                        LoanProductBorrowerCycleVariations existingCycleVariation = fetchLoanProductBorrowerCycleVariationById(loanProduct,
                                id);
                        if (!existingCycleVariation.equals(borrowerCycleVariations)) {
                            existingCycleVariation.copy(borrowerCycleVariations);
                            actualChanges.put("borrowerCycleId", id);
                        }
                    }
                    i++;
                } while (i < variationArray.size());
            }
        }
    }

    private void clearVariations(LoanProduct loanProduct) {
        loanProduct.getBorrowerCycleVariations().clear();
    }
}
