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

import com.google.gson.JsonElement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.common.AccountingConstants.CashAccountsForLoan;
import org.apache.fineract.accounting.common.AccountingConstants.LoanProductAccountingParams;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.glaccount.domain.GLAccountRepositoryWrapper;
import org.apache.fineract.accounting.glaccount.domain.GLAccountType;
import org.apache.fineract.accounting.producttoaccountmapping.domain.ProductToGLAccountMapping;
import org.apache.fineract.accounting.producttoaccountmapping.domain.ProductToGLAccountMappingRepository;
import org.apache.fineract.accounting.producttoaccountmapping.exception.ProductToGLAccountMappingInvalidException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.PortfolioProductType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkingCapitalLoanProductToGLAccountMappingHelper {

    private static final PortfolioProductType PRODUCT_TYPE = PortfolioProductType.WORKING_CAPITAL_LOAN;
    private static final List<GLAccountType> ASSET_LIABILITY_TYPES = List.of(GLAccountType.ASSET, GLAccountType.LIABILITY);

    private final ProductToGLAccountMappingRepository accountMappingRepository;
    private final GLAccountRepositoryWrapper accountRepositoryWrapper;
    private final FromJsonHelper fromApiJsonHelper;

    public void saveCashBasedAccountMapping(final JsonElement element, final Long productId) {
        // assets / liabilities (fund source can be either asset or liability)
        saveAccountMapping(element, LoanProductAccountingParams.FUND_SOURCE.getValue(), productId,
                CashAccountsForLoan.FUND_SOURCE.getValue(), ASSET_LIABILITY_TYPES);

        // assets
        saveAccountMapping(element, LoanProductAccountingParams.LOAN_PORTFOLIO.getValue(), productId,
                CashAccountsForLoan.LOAN_PORTFOLIO.getValue(), GLAccountType.ASSET);
        saveAccountMapping(element, LoanProductAccountingParams.TRANSFERS_SUSPENSE.getValue(), productId,
                CashAccountsForLoan.TRANSFERS_SUSPENSE.getValue(), GLAccountType.ASSET);

        // income (required)
        saveAccountMapping(element, LoanProductAccountingParams.INCOME_FROM_DISCOUNT_FEE.getValue(), productId,
                CashAccountsForLoan.INCOME_FROM_DISCOUNT_FEE.getValue(), GLAccountType.INCOME);
        saveAccountMapping(element, LoanProductAccountingParams.INCOME_FROM_FEES.getValue(), productId,
                CashAccountsForLoan.INCOME_FROM_FEES.getValue(), GLAccountType.INCOME);
        saveAccountMapping(element, LoanProductAccountingParams.INCOME_FROM_PENALTIES.getValue(), productId,
                CashAccountsForLoan.INCOME_FROM_PENALTIES.getValue(), GLAccountType.INCOME);
        saveAccountMapping(element, LoanProductAccountingParams.INCOME_FROM_RECOVERY.getValue(), productId,
                CashAccountsForLoan.INCOME_FROM_RECOVERY.getValue(), GLAccountType.INCOME);

        // income (optional)
        saveOptionalAccountMapping(element, LoanProductAccountingParams.INCOME_FROM_CHARGE_OFF_INTEREST.getValue(), productId,
                CashAccountsForLoan.INCOME_FROM_CHARGE_OFF_INTEREST.getValue(), GLAccountType.INCOME);
        saveOptionalAccountMapping(element, LoanProductAccountingParams.INCOME_FROM_CHARGE_OFF_FEES.getValue(), productId,
                CashAccountsForLoan.INCOME_FROM_CHARGE_OFF_FEES.getValue(), GLAccountType.INCOME);
        saveOptionalAccountMapping(element, LoanProductAccountingParams.INCOME_FROM_CHARGE_OFF_PENALTY.getValue(), productId,
                CashAccountsForLoan.INCOME_FROM_CHARGE_OFF_PENALTY.getValue(), GLAccountType.INCOME);
        saveOptionalAccountMapping(element, LoanProductAccountingParams.INCOME_FROM_GOODWILL_CREDIT_INTEREST.getValue(), productId,
                CashAccountsForLoan.INCOME_FROM_GOODWILL_CREDIT_INTEREST.getValue(), GLAccountType.INCOME);
        saveOptionalAccountMapping(element, LoanProductAccountingParams.INCOME_FROM_GOODWILL_CREDIT_FEES.getValue(), productId,
                CashAccountsForLoan.INCOME_FROM_GOODWILL_CREDIT_FEES.getValue(), GLAccountType.INCOME);
        saveOptionalAccountMapping(element, LoanProductAccountingParams.INCOME_FROM_GOODWILL_CREDIT_PENALTY.getValue(), productId,
                CashAccountsForLoan.INCOME_FROM_GOODWILL_CREDIT_PENALTY.getValue(), GLAccountType.INCOME);

        // expenses (required)
        saveAccountMapping(element, LoanProductAccountingParams.LOSSES_WRITTEN_OFF.getValue(), productId,
                CashAccountsForLoan.LOSSES_WRITTEN_OFF.getValue(), GLAccountType.EXPENSE);

        // expenses (optional)
        saveOptionalAccountMapping(element, LoanProductAccountingParams.GOODWILL_CREDIT.getValue(), productId,
                CashAccountsForLoan.GOODWILL_CREDIT.getValue(), GLAccountType.EXPENSE);
        saveOptionalAccountMapping(element, LoanProductAccountingParams.CHARGE_OFF_EXPENSE.getValue(), productId,
                CashAccountsForLoan.CHARGE_OFF_EXPENSE.getValue(), GLAccountType.EXPENSE);
        saveOptionalAccountMapping(element, LoanProductAccountingParams.CHARGE_OFF_FRAUD_EXPENSE.getValue(), productId,
                CashAccountsForLoan.CHARGE_OFF_FRAUD_EXPENSE.getValue(), GLAccountType.EXPENSE);

        // liabilities
        saveAccountMapping(element, LoanProductAccountingParams.OVERPAYMENT.getValue(), productId,
                CashAccountsForLoan.OVERPAYMENT.getValue(), GLAccountType.LIABILITY);
        saveAccountMapping(element, LoanProductAccountingParams.DEFERRED_INCOME_LIABILITY.getValue(), productId,
                CashAccountsForLoan.DEFERRED_INCOME_LIABILITY.getValue(), GLAccountType.LIABILITY);
    }

    public void handleChangesToCashBasedAccountMapping(final Long productId, final Map<String, Object> changes, final JsonElement element) {
        // assets / liabilities (fund source can be either asset or liability)
        mergeAccountMappingChanges(element, LoanProductAccountingParams.FUND_SOURCE.getValue(), productId,
                CashAccountsForLoan.FUND_SOURCE.getValue(), changes, ASSET_LIABILITY_TYPES);

        // assets
        mergeAccountMappingChanges(element, LoanProductAccountingParams.LOAN_PORTFOLIO.getValue(), productId,
                CashAccountsForLoan.LOAN_PORTFOLIO.getValue(), changes, GLAccountType.ASSET);
        mergeAccountMappingChanges(element, LoanProductAccountingParams.TRANSFERS_SUSPENSE.getValue(), productId,
                CashAccountsForLoan.TRANSFERS_SUSPENSE.getValue(), changes, GLAccountType.ASSET);

        // income
        mergeAccountMappingChanges(element, LoanProductAccountingParams.INCOME_FROM_DISCOUNT_FEE.getValue(), productId,
                CashAccountsForLoan.INCOME_FROM_DISCOUNT_FEE.getValue(), changes, GLAccountType.INCOME);
        mergeAccountMappingChanges(element, LoanProductAccountingParams.INCOME_FROM_FEES.getValue(), productId,
                CashAccountsForLoan.INCOME_FROM_FEES.getValue(), changes, GLAccountType.INCOME);
        mergeAccountMappingChanges(element, LoanProductAccountingParams.INCOME_FROM_PENALTIES.getValue(), productId,
                CashAccountsForLoan.INCOME_FROM_PENALTIES.getValue(), changes, GLAccountType.INCOME);
        mergeAccountMappingChanges(element, LoanProductAccountingParams.INCOME_FROM_RECOVERY.getValue(), productId,
                CashAccountsForLoan.INCOME_FROM_RECOVERY.getValue(), changes, GLAccountType.INCOME);
        mergeAccountMappingChanges(element, LoanProductAccountingParams.INCOME_FROM_CHARGE_OFF_INTEREST.getValue(), productId,
                CashAccountsForLoan.INCOME_FROM_CHARGE_OFF_INTEREST.getValue(), changes, GLAccountType.INCOME);
        mergeAccountMappingChanges(element, LoanProductAccountingParams.INCOME_FROM_CHARGE_OFF_FEES.getValue(), productId,
                CashAccountsForLoan.INCOME_FROM_CHARGE_OFF_FEES.getValue(), changes, GLAccountType.INCOME);
        mergeAccountMappingChanges(element, LoanProductAccountingParams.INCOME_FROM_CHARGE_OFF_PENALTY.getValue(), productId,
                CashAccountsForLoan.INCOME_FROM_CHARGE_OFF_PENALTY.getValue(), changes, GLAccountType.INCOME);
        mergeAccountMappingChanges(element, LoanProductAccountingParams.INCOME_FROM_GOODWILL_CREDIT_INTEREST.getValue(), productId,
                CashAccountsForLoan.INCOME_FROM_GOODWILL_CREDIT_INTEREST.getValue(), changes, GLAccountType.INCOME);
        mergeAccountMappingChanges(element, LoanProductAccountingParams.INCOME_FROM_GOODWILL_CREDIT_FEES.getValue(), productId,
                CashAccountsForLoan.INCOME_FROM_GOODWILL_CREDIT_FEES.getValue(), changes, GLAccountType.INCOME);
        mergeAccountMappingChanges(element, LoanProductAccountingParams.INCOME_FROM_GOODWILL_CREDIT_PENALTY.getValue(), productId,
                CashAccountsForLoan.INCOME_FROM_GOODWILL_CREDIT_PENALTY.getValue(), changes, GLAccountType.INCOME);

        // expenses
        mergeAccountMappingChanges(element, LoanProductAccountingParams.LOSSES_WRITTEN_OFF.getValue(), productId,
                CashAccountsForLoan.LOSSES_WRITTEN_OFF.getValue(), changes, GLAccountType.EXPENSE);
        mergeAccountMappingChanges(element, LoanProductAccountingParams.GOODWILL_CREDIT.getValue(), productId,
                CashAccountsForLoan.GOODWILL_CREDIT.getValue(), changes, GLAccountType.EXPENSE);
        mergeAccountMappingChanges(element, LoanProductAccountingParams.CHARGE_OFF_EXPENSE.getValue(), productId,
                CashAccountsForLoan.CHARGE_OFF_EXPENSE.getValue(), changes, GLAccountType.EXPENSE);
        mergeAccountMappingChanges(element, LoanProductAccountingParams.CHARGE_OFF_FRAUD_EXPENSE.getValue(), productId,
                CashAccountsForLoan.CHARGE_OFF_FRAUD_EXPENSE.getValue(), changes, GLAccountType.EXPENSE);

        // liabilities
        mergeAccountMappingChanges(element, LoanProductAccountingParams.OVERPAYMENT.getValue(), productId,
                CashAccountsForLoan.OVERPAYMENT.getValue(), changes, GLAccountType.LIABILITY);
        mergeAccountMappingChanges(element, LoanProductAccountingParams.DEFERRED_INCOME_LIABILITY.getValue(), productId,
                CashAccountsForLoan.DEFERRED_INCOME_LIABILITY.getValue(), changes, GLAccountType.LIABILITY);
    }

    public Map<String, Object> populateChangesForNewCashBasedMappingCreation(final JsonElement element) {
        final Map<String, Object> changes = new HashMap<>();
        // required accounts
        putChange(changes, element, LoanProductAccountingParams.FUND_SOURCE);
        putChange(changes, element, LoanProductAccountingParams.LOAN_PORTFOLIO);
        putChange(changes, element, LoanProductAccountingParams.TRANSFERS_SUSPENSE);
        putChange(changes, element, LoanProductAccountingParams.INCOME_FROM_DISCOUNT_FEE);
        putChange(changes, element, LoanProductAccountingParams.INCOME_FROM_FEES);
        putChange(changes, element, LoanProductAccountingParams.INCOME_FROM_PENALTIES);
        putChange(changes, element, LoanProductAccountingParams.INCOME_FROM_RECOVERY);
        putChange(changes, element, LoanProductAccountingParams.LOSSES_WRITTEN_OFF);
        putChange(changes, element, LoanProductAccountingParams.OVERPAYMENT);
        putChange(changes, element, LoanProductAccountingParams.DEFERRED_INCOME_LIABILITY);
        // optional accounts
        putChangeIfPresent(changes, element, LoanProductAccountingParams.INCOME_FROM_CHARGE_OFF_INTEREST);
        putChangeIfPresent(changes, element, LoanProductAccountingParams.INCOME_FROM_CHARGE_OFF_FEES);
        putChangeIfPresent(changes, element, LoanProductAccountingParams.INCOME_FROM_CHARGE_OFF_PENALTY);
        putChangeIfPresent(changes, element, LoanProductAccountingParams.INCOME_FROM_GOODWILL_CREDIT_INTEREST);
        putChangeIfPresent(changes, element, LoanProductAccountingParams.INCOME_FROM_GOODWILL_CREDIT_FEES);
        putChangeIfPresent(changes, element, LoanProductAccountingParams.INCOME_FROM_GOODWILL_CREDIT_PENALTY);
        putChangeIfPresent(changes, element, LoanProductAccountingParams.GOODWILL_CREDIT);
        putChangeIfPresent(changes, element, LoanProductAccountingParams.CHARGE_OFF_EXPENSE);
        putChangeIfPresent(changes, element, LoanProductAccountingParams.CHARGE_OFF_FRAUD_EXPENSE);
        return changes;
    }

    private void putChange(final Map<String, Object> changes, final JsonElement element, final LoanProductAccountingParams param) {
        changes.put(param.getValue(), this.fromApiJsonHelper.extractLongNamed(param.getValue(), element));
    }

    private void putChangeIfPresent(final Map<String, Object> changes, final JsonElement element, final LoanProductAccountingParams param) {
        final Long value = this.fromApiJsonHelper.extractLongNamed(param.getValue(), element);
        if (value != null) {
            changes.put(param.getValue(), value);
        }
    }

    public void deleteProductToGLAccountMapping(final Long productId) {
        final List<ProductToGLAccountMapping> mappings = this.accountMappingRepository.findByProductIdAndProductType(productId,
                PRODUCT_TYPE.getValue());
        if (mappings != null && !mappings.isEmpty()) {
            this.accountMappingRepository.deleteAll(mappings);
        }
    }

    private void saveAccountMapping(final JsonElement element, final String paramName, final Long productId, final int financialAccountType,
            final GLAccountType expectedAccountType) {
        saveAccountMapping(element, paramName, productId, financialAccountType, List.of(expectedAccountType));
    }

    private void saveAccountMapping(final JsonElement element, final String paramName, final Long productId, final int financialAccountType,
            final List<GLAccountType> expectedAccountTypes) {
        final Long accountId = this.fromApiJsonHelper.extractLongNamed(paramName, element);
        if (accountId != null) {
            final GLAccount glAccount = getAccountByIdAndType(paramName, expectedAccountTypes, accountId);
            final ProductToGLAccountMapping accountMapping = new ProductToGLAccountMapping().setGlAccount(glAccount).setProductId(productId)
                    .setProductType(PRODUCT_TYPE.getValue()).setFinancialAccountType(financialAccountType);
            this.accountMappingRepository.saveAndFlush(accountMapping);
        }
    }

    private void saveOptionalAccountMapping(final JsonElement element, final String paramName, final Long productId,
            final int financialAccountType, final GLAccountType expectedAccountType) {
        final Long accountId = this.fromApiJsonHelper.extractLongNamed(paramName, element);
        if (accountId != null) {
            saveAccountMapping(element, paramName, productId, financialAccountType, expectedAccountType);
        }
    }

    private void mergeAccountMappingChanges(final JsonElement element, final String paramName, final Long productId,
            final int accountTypeId, final Map<String, Object> changes, final GLAccountType expectedAccountType) {
        mergeAccountMappingChanges(element, paramName, productId, accountTypeId, changes, List.of(expectedAccountType));
    }

    private void mergeAccountMappingChanges(final JsonElement element, final String paramName, final Long productId,
            final int accountTypeId, final Map<String, Object> changes, final List<GLAccountType> expectedAccountTypes) {
        final Long accountId = this.fromApiJsonHelper.extractLongNamed(paramName, element);
        if (accountId != null) {
            final ProductToGLAccountMapping existingMapping = this.accountMappingRepository.findCoreProductToFinAccountMapping(productId,
                    PRODUCT_TYPE.getValue(), accountTypeId);
            if (existingMapping == null) {
                saveAccountMapping(element, paramName, productId, accountTypeId, expectedAccountTypes);
                changes.put(paramName, accountId);
            } else if (existingMapping.getGlAccount() != null && !Objects.equals(existingMapping.getGlAccount().getId(), accountId)) {
                final GLAccount glAccount = getAccountByIdAndType(paramName, expectedAccountTypes, accountId);
                changes.put(paramName, accountId);
                existingMapping.setGlAccount(glAccount);
                this.accountMappingRepository.saveAndFlush(existingMapping);
            }
        }
    }

    private GLAccount getAccountByIdAndType(final String paramName, final List<GLAccountType> expectedAccountTypes, final Long accountId) {
        final GLAccount glAccount = this.accountRepositoryWrapper.findOneWithNotFoundDetection(accountId);
        final List<Integer> expectedValues = expectedAccountTypes.stream().map(GLAccountType::getValue).toList();
        if (!expectedValues.contains(glAccount.getType())) {
            throw new ProductToGLAccountMappingInvalidException(paramName, glAccount.getName(), accountId,
                    GLAccountType.fromInt(glAccount.getType()).toString(), expectedValues.toString());
        }
        return glAccount;
    }
}
