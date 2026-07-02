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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.common.AccountingConstants;
import org.apache.fineract.accounting.common.AccountingConstants.CashAccountsForLoan;
import org.apache.fineract.accounting.common.AccountingConstants.LoanProductAccountingDataParams;
import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.accounting.producttoaccountmapping.domain.ProductToGLAccountMapping;
import org.apache.fineract.accounting.producttoaccountmapping.domain.ProductToGLAccountMappingRepository;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.PortfolioProductType;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalAccountingRuleType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkingCapitalProductAccountingMappingServiceImpl implements WorkingCapitalProductAccountingMappingService {

    private final WorkingCapitalLoanProductToGLAccountMappingHelper mappingHelper;
    private final ProductToGLAccountMappingRepository accountMappingRepository;
    private final FromJsonHelper fromApiJsonHelper;

    private void validateCreateAccountMapping(final JsonElement element, String arrayName, String key) {
        final JsonArray array = this.fromApiJsonHelper.extractJsonArrayNamed(arrayName, element);
        if (array != null) {
            ArrayList<String> values = new ArrayList<>(array.size());
            for (int i = 0; i < array.size(); i++) {
                final JsonObject jsonObject = array.get(i).getAsJsonObject();
                if (jsonObject.get(key) != null) {
                    String value = jsonObject.get(key).getAsString();
                    if (!values.contains(value)) {
                        values.add(value);
                    } else {
                        String e = arrayName + "." + key;
                        throw new PlatformApiDataValidationException("duplicated.enrty.for." + e, "Duplicated entry for " + e, e);
                    }
                }
            }
        }
    }

    @Override
    @Transactional
    public void createAccountMapping(final Long wcLoanProductId, final JsonCommand command) {
        final JsonElement element = this.fromApiJsonHelper.parse(command.json());
        final String accountingRuleValue = this.fromApiJsonHelper.extractStringNamed("accountingRule", element);
        final WorkingCapitalAccountingRuleType accountingRuleType = WorkingCapitalAccountingRuleType.valueOf(accountingRuleValue);

        validateCreateAccountMapping(element,
                AccountingConstants.LoanProductAccountingParams.PAYMENT_CHANNEL_FUND_SOURCE_MAPPING.getValue(),
                AccountingConstants.LoanProductAccountingParams.PAYMENT_TYPE.getValue());
        validateCreateAccountMapping(element, AccountingConstants.LoanProductAccountingParams.PENALTY_INCOME_ACCOUNT_MAPPING.getValue(),
                AccountingConstants.LoanProductAccountingParams.CHARGE_ID.getValue());
        validateCreateAccountMapping(element, AccountingConstants.LoanProductAccountingParams.FEE_INCOME_ACCOUNT_MAPPING.getValue(),
                AccountingConstants.LoanProductAccountingParams.CHARGE_ID.getValue());
        validateCreateAccountMapping(element,
                AccountingConstants.LoanProductAccountingParams.CHARGE_OFF_REASON_TO_EXPENSE_ACCOUNT_MAPPINGS.getValue(),
                AccountingConstants.LoanProductAccountingParams.CHARGE_OFF_REASON_CODE_VALUE_ID.getValue());
        validateCreateAccountMapping(element,
                AccountingConstants.LoanProductAccountingParams.WRITE_OFF_REASON_TO_EXPENSE_ACCOUNT_MAPPINGS.getValue(),
                AccountingConstants.LoanProductAccountingParams.WRITE_OFF_REASON_CODE_VALUE_ID.getValue());

        if (accountingRuleType.isAccrualWithDeferredRevenueAmortization()) {
            this.mappingHelper.saveAccrualWithDeferredRevenueAmortizationAccountMapping(element, wcLoanProductId);
            this.mappingHelper.saveAdvancedMappings(command, element, wcLoanProductId);
        }
    }

    @Override
    @Transactional
    public Map<String, Object> updateAccountMapping(final Long wcLoanProductId, final JsonCommand command,
            final boolean accountingRuleChanged, final WorkingCapitalAccountingRuleType accountingRuleType) {
        Map<String, Object> changes = new HashMap<>();
        final JsonElement element = this.fromApiJsonHelper.parse(command.json());

        if (accountingRuleChanged) {
            this.mappingHelper.deleteProductToGLAccountMapping(wcLoanProductId);
            if (accountingRuleType.isAccrualWithDeferredRevenueAmortization()) {
                this.mappingHelper.saveAccrualWithDeferredRevenueAmortizationAccountMapping(element, wcLoanProductId);
                changes = this.mappingHelper.populateChangesForNewAccrualWithDeferredRevenueAmortizationMappingCreation(element);
            }
        } else {
            if (accountingRuleType.isAccrualWithDeferredRevenueAmortization()) {
                this.mappingHelper.handleChangesToAccrualWithDeferredRevenueAmortizationAccountMapping(wcLoanProductId, changes, element);
                this.mappingHelper.updateAdvancedMappings(command, element, wcLoanProductId, changes);
            }
        }
        return changes;
    }

    @Override
    @Transactional
    public void deleteAccountMapping(final Long wcLoanProductId) {
        this.mappingHelper.deleteProductToGLAccountMapping(wcLoanProductId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, GLAccountData> fetchAccountMappingDetails(final Long wcLoanProductId,
            final WorkingCapitalAccountingRuleType accountingRuleType) {
        final Map<String, GLAccountData> accountMappingDetails = new LinkedHashMap<>(8);

        if (!accountingRuleType.isAccrualWithDeferredRevenueAmortization()) {
            return accountMappingDetails;
        }

        final List<ProductToGLAccountMapping> mappings = this.accountMappingRepository.findAllRegularMappings(wcLoanProductId,
                PortfolioProductType.WORKING_CAPITAL_LOAN.getValue());

        for (final ProductToGLAccountMapping mapping : mappings) {
            final CashAccountsForLoan glAccountForLoan = CashAccountsForLoan.fromInt(mapping.getFinancialAccountType());
            if (glAccountForLoan == null) {
                continue;
            }

            final GLAccountData glAccountData = new GLAccountData().setId(mapping.getGlAccount().getId())
                    .setName(mapping.getGlAccount().getName()).setGlCode(mapping.getGlAccount().getGlCode());

            switch (glAccountForLoan) {
                case FUND_SOURCE -> accountMappingDetails.put(LoanProductAccountingDataParams.FUND_SOURCE.getValue(), glAccountData);
                case LOAN_PORTFOLIO -> accountMappingDetails.put(LoanProductAccountingDataParams.LOAN_PORTFOLIO.getValue(), glAccountData);
                case TRANSFERS_SUSPENSE ->
                    accountMappingDetails.put(LoanProductAccountingDataParams.TRANSFERS_SUSPENSE.getValue(), glAccountData);
                case INCOME_FROM_DISCOUNT_FEE ->
                    accountMappingDetails.put(LoanProductAccountingDataParams.INCOME_FROM_DISCOUNT_FEE.getValue(), glAccountData);
                case DEFERRED_INCOME_LIABILITY ->
                    accountMappingDetails.put(LoanProductAccountingDataParams.DEFERRED_INCOME_LIABILITY.getValue(), glAccountData);
                case FEES_RECEIVABLE ->
                    accountMappingDetails.put(LoanProductAccountingDataParams.FEES_RECEIVABLE.getValue(), glAccountData);
                case PENALTIES_RECEIVABLE ->
                    accountMappingDetails.put(LoanProductAccountingDataParams.PENALTIES_RECEIVABLE.getValue(), glAccountData);
                case INCOME_FROM_FEES ->
                    accountMappingDetails.put(LoanProductAccountingDataParams.INCOME_FROM_FEES.getValue(), glAccountData);
                case INCOME_FROM_PENALTIES ->
                    accountMappingDetails.put(LoanProductAccountingDataParams.INCOME_FROM_PENALTIES.getValue(), glAccountData);
                case INCOME_FROM_RECOVERY ->
                    accountMappingDetails.put(LoanProductAccountingDataParams.INCOME_FROM_RECOVERY.getValue(), glAccountData);
                case LOSSES_WRITTEN_OFF ->
                    accountMappingDetails.put(LoanProductAccountingDataParams.LOSSES_WRITTEN_OFF.getValue(), glAccountData);
                case GOODWILL_CREDIT ->
                    accountMappingDetails.put(LoanProductAccountingDataParams.GOODWILL_CREDIT.getValue(), glAccountData);
                case OVERPAYMENT -> accountMappingDetails.put(LoanProductAccountingDataParams.OVERPAYMENT.getValue(), glAccountData);
                case INCOME_FROM_CHARGE_OFF_INTEREST ->
                    accountMappingDetails.put(LoanProductAccountingDataParams.INCOME_FROM_CHARGE_OFF_INTEREST.getValue(), glAccountData);
                case INCOME_FROM_CHARGE_OFF_FEES ->
                    accountMappingDetails.put(LoanProductAccountingDataParams.INCOME_FROM_CHARGE_OFF_FEES.getValue(), glAccountData);
                case INCOME_FROM_CHARGE_OFF_PENALTY ->
                    accountMappingDetails.put(LoanProductAccountingDataParams.INCOME_FROM_CHARGE_OFF_PENALTY.getValue(), glAccountData);
                case CHARGE_OFF_EXPENSE ->
                    accountMappingDetails.put(LoanProductAccountingDataParams.CHARGE_OFF_EXPENSE.getValue(), glAccountData);
                case CHARGE_OFF_FRAUD_EXPENSE ->
                    accountMappingDetails.put(LoanProductAccountingDataParams.CHARGE_OFF_FRAUD_EXPENSE.getValue(), glAccountData);
                case INCOME_FROM_GOODWILL_CREDIT_INTEREST -> accountMappingDetails
                        .put(LoanProductAccountingDataParams.INCOME_FROM_GOODWILL_CREDIT_INTEREST.getValue(), glAccountData);
                case INCOME_FROM_GOODWILL_CREDIT_FEES ->
                    accountMappingDetails.put(LoanProductAccountingDataParams.INCOME_FROM_GOODWILL_CREDIT_FEES.getValue(), glAccountData);
                case INCOME_FROM_GOODWILL_CREDIT_PENALTY -> accountMappingDetails
                        .put(LoanProductAccountingDataParams.INCOME_FROM_GOODWILL_CREDIT_PENALTY.getValue(), glAccountData);
                default -> {
                }
            }
        }

        return accountMappingDetails;
    }
}
