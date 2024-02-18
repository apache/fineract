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
package org.apache.fineract.portfolio.savings.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.common.AccountingConstants.SavingProductAccountingParams;
import org.apache.fineract.accounting.common.AccountingValidations;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SavingsProductAccountingDataValidator {

    private final FromJsonHelper fromApiJsonHelper;

    public void evaluateProductAccountingData(final Integer accountingRuleType, final boolean isDormancyActive, final JsonElement element,
            DataValidatorBuilder baseDataValidator, final DepositAccountType accountType) {
        // GL Accounts for Cash or Accrual Periodic
        if (AccountingValidations.isCashBasedAccounting(accountingRuleType)
                || AccountingValidations.isAccrualPeriodicBasedAccounting(accountingRuleType)) {

            final Long savingsControlAccountId = fromApiJsonHelper
                    .extractLongNamed(SavingProductAccountingParams.SAVINGS_CONTROL.getValue(), element);
            baseDataValidator.reset().parameter(SavingProductAccountingParams.SAVINGS_CONTROL.getValue()).value(savingsControlAccountId)
                    .notNull().integerGreaterThanZero();

            final Long savingsReferenceAccountId = fromApiJsonHelper
                    .extractLongNamed(SavingProductAccountingParams.SAVINGS_REFERENCE.getValue(), element);
            baseDataValidator.reset().parameter(SavingProductAccountingParams.SAVINGS_REFERENCE.getValue()).value(savingsReferenceAccountId)
                    .notNull().integerGreaterThanZero();

            final Long transfersInSuspenseAccountId = fromApiJsonHelper
                    .extractLongNamed(SavingProductAccountingParams.TRANSFERS_SUSPENSE.getValue(), element);
            baseDataValidator.reset().parameter(SavingProductAccountingParams.TRANSFERS_SUSPENSE.getValue())
                    .value(transfersInSuspenseAccountId).notNull().integerGreaterThanZero();

            final Long interestOnSavingsAccountId = fromApiJsonHelper
                    .extractLongNamed(SavingProductAccountingParams.INTEREST_ON_SAVINGS.getValue(), element);
            baseDataValidator.reset().parameter(SavingProductAccountingParams.INTEREST_ON_SAVINGS.getValue())
                    .value(interestOnSavingsAccountId).notNull().integerGreaterThanZero();

            final Long incomeFromFeeId = fromApiJsonHelper.extractLongNamed(SavingProductAccountingParams.INCOME_FROM_FEES.getValue(),
                    element);
            baseDataValidator.reset().parameter(SavingProductAccountingParams.INCOME_FROM_FEES.getValue()).value(incomeFromFeeId).notNull()
                    .integerGreaterThanZero();

            final Long incomeFromPenaltyId = fromApiJsonHelper
                    .extractLongNamed(SavingProductAccountingParams.INCOME_FROM_PENALTIES.getValue(), element);
            baseDataValidator.reset().parameter(SavingProductAccountingParams.INCOME_FROM_PENALTIES.getValue()).value(incomeFromPenaltyId)
                    .notNull().integerGreaterThanZero();

            if (!accountType.equals(DepositAccountType.RECURRING_DEPOSIT) && !accountType.equals(DepositAccountType.FIXED_DEPOSIT)) {
                final Long overdraftControlAccountId = fromApiJsonHelper
                        .extractLongNamed(SavingProductAccountingParams.OVERDRAFT_PORTFOLIO_CONTROL.getValue(), element);
                baseDataValidator.reset().parameter(SavingProductAccountingParams.OVERDRAFT_PORTFOLIO_CONTROL.getValue())
                        .value(overdraftControlAccountId).notNull().integerGreaterThanZero();

                final Long incomeFromInterest = fromApiJsonHelper
                        .extractLongNamed(SavingProductAccountingParams.INCOME_FROM_INTEREST.getValue(), element);
                baseDataValidator.reset().parameter(SavingProductAccountingParams.INCOME_FROM_INTEREST.getValue()).value(incomeFromInterest)
                        .notNull().integerGreaterThanZero();

                final Long writtenoff = fromApiJsonHelper.extractLongNamed(SavingProductAccountingParams.LOSSES_WRITTEN_OFF.getValue(),
                        element);
                baseDataValidator.reset().parameter(SavingProductAccountingParams.LOSSES_WRITTEN_OFF.getValue()).value(writtenoff).notNull()
                        .integerGreaterThanZero();
            }

            if (isDormancyActive) {
                final Long escheatLiabilityAccountId = fromApiJsonHelper
                        .extractLongNamed(SavingProductAccountingParams.ESCHEAT_LIABILITY.getValue(), element);
                baseDataValidator.reset().parameter(SavingProductAccountingParams.ESCHEAT_LIABILITY.getValue())
                        .value(escheatLiabilityAccountId).notNull().integerGreaterThanZero();
            }
        }

        // GL Accounts for Accrual Period only
        if (AccountingValidations.isAccrualPeriodicBasedAccounting(accountingRuleType)) {
            final Long feeReceivableAccountId = fromApiJsonHelper.extractLongNamed(SavingProductAccountingParams.FEES_RECEIVABLE.getValue(),
                    element);
            baseDataValidator.reset().parameter(SavingProductAccountingParams.FEES_RECEIVABLE.getValue()).value(feeReceivableAccountId)
                    .notNull().integerGreaterThanZero();

            final Long penaltyReceivableAccountId = fromApiJsonHelper
                    .extractLongNamed(SavingProductAccountingParams.PENALTIES_RECEIVABLE.getValue(), element);
            baseDataValidator.reset().parameter(SavingProductAccountingParams.PENALTIES_RECEIVABLE.getValue())
                    .value(penaltyReceivableAccountId).notNull().integerGreaterThanZero();

            final Long interestPayableAccountId = fromApiJsonHelper
                    .extractLongNamed(SavingProductAccountingParams.INTEREST_PAYABLE.getValue(), element);
            baseDataValidator.reset().parameter(SavingProductAccountingParams.INTEREST_PAYABLE.getValue()).value(interestPayableAccountId)
                    .notNull().integerGreaterThanZero();
        }

        validatePaymentChannelFundSourceMappings(baseDataValidator, element);
        validateChargeToIncomeAccountMappings(baseDataValidator, element);
    }

    /**
     * Validation for advanced accounting options
     */
    public void validatePaymentChannelFundSourceMappings(final DataValidatorBuilder baseDataValidator, final JsonElement element) {
        if (fromApiJsonHelper.parameterExists(SavingProductAccountingParams.PAYMENT_CHANNEL_FUND_SOURCE_MAPPING.getValue(), element)) {
            final JsonArray paymentChannelMappingArray = fromApiJsonHelper
                    .extractJsonArrayNamed(SavingProductAccountingParams.PAYMENT_CHANNEL_FUND_SOURCE_MAPPING.getValue(), element);
            if (paymentChannelMappingArray != null && paymentChannelMappingArray.size() > 0) {
                int i = 0;
                do {
                    final JsonObject jsonObject = paymentChannelMappingArray.get(i).getAsJsonObject();
                    final Long paymentTypeId = jsonObject.get(SavingProductAccountingParams.PAYMENT_TYPE.getValue()).getAsLong();
                    final Long paymentSpecificFundAccountId = jsonObject.get(SavingProductAccountingParams.FUND_SOURCE.getValue())
                            .getAsLong();
                    baseDataValidator.reset()
                            .parameter(SavingProductAccountingParams.PAYMENT_CHANNEL_FUND_SOURCE_MAPPING.getValue() + "[" + i + "]."
                                    + SavingProductAccountingParams.PAYMENT_TYPE.toString())
                            .value(paymentTypeId).notNull().integerGreaterThanZero();
                    baseDataValidator.reset()
                            .parameter(SavingProductAccountingParams.PAYMENT_CHANNEL_FUND_SOURCE_MAPPING.getValue() + "[" + i + "]."
                                    + SavingProductAccountingParams.FUND_SOURCE.getValue())
                            .value(paymentSpecificFundAccountId).notNull().integerGreaterThanZero();
                    i++;
                } while (i < paymentChannelMappingArray.size());
            }
        }
    }

    public void validateChargeToIncomeAccountMappings(final DataValidatorBuilder baseDataValidator, final JsonElement element) {
        // validate for both fee and penalty charges
        validateChargeToIncomeAccountMappings(baseDataValidator, element, true);
        validateChargeToIncomeAccountMappings(baseDataValidator, element, true);
    }

    private void validateChargeToIncomeAccountMappings(final DataValidatorBuilder baseDataValidator, final JsonElement element,
            final boolean isPenalty) {
        String parameterName;
        if (isPenalty) {
            parameterName = SavingProductAccountingParams.PENALTY_INCOME_ACCOUNT_MAPPING.getValue();
        } else {
            parameterName = SavingProductAccountingParams.FEE_INCOME_ACCOUNT_MAPPING.getValue();
        }

        if (this.fromApiJsonHelper.parameterExists(parameterName, element)) {
            final JsonArray chargeToIncomeAccountMappingArray = this.fromApiJsonHelper.extractJsonArrayNamed(parameterName, element);
            if (chargeToIncomeAccountMappingArray != null && chargeToIncomeAccountMappingArray.size() > 0) {
                int i = 0;
                do {
                    final JsonObject jsonObject = chargeToIncomeAccountMappingArray.get(i).getAsJsonObject();
                    final Long chargeId = this.fromApiJsonHelper.extractLongNamed(SavingProductAccountingParams.CHARGE_ID.getValue(),
                            jsonObject);
                    final Long incomeAccountId = this.fromApiJsonHelper
                            .extractLongNamed(SavingProductAccountingParams.INCOME_ACCOUNT_ID.getValue(), jsonObject);
                    baseDataValidator.reset().parameter(parameterName + "[" + i + "]." + SavingProductAccountingParams.CHARGE_ID.getValue())
                            .value(chargeId).notNull().integerGreaterThanZero();
                    baseDataValidator.reset()
                            .parameter(parameterName + "[" + i + "]." + SavingProductAccountingParams.INCOME_ACCOUNT_ID.getValue())
                            .value(incomeAccountId).notNull().integerGreaterThanZero();
                    i++;
                } while (i < chargeToIncomeAccountMappingArray.size());
            }
        }
    }

}
