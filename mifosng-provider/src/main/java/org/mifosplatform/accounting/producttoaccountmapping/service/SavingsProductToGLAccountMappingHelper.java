/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.producttoaccountmapping.service;

import java.util.HashMap;
import java.util.Map;

import org.mifosplatform.accounting.common.AccountingConstants.CASH_ACCOUNTS_FOR_SAVINGS;
import org.mifosplatform.accounting.common.AccountingConstants.SAVINGS_PRODUCT_ACCOUNTING_PARAMS;
import org.mifosplatform.accounting.common.AccountingRuleType;
import org.mifosplatform.accounting.glaccount.domain.GLAccountRepository;
import org.mifosplatform.accounting.glaccount.domain.GLAccountType;
import org.mifosplatform.accounting.producttoaccountmapping.domain.PortfolioProductType;
import org.mifosplatform.accounting.producttoaccountmapping.domain.ProductToGLAccountMappingRepository;
import org.mifosplatform.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;

@Component
public class SavingsProductToGLAccountMappingHelper extends ProductToGLAccountMappingHelper {

    @Autowired
    public SavingsProductToGLAccountMappingHelper(GLAccountRepository glAccountRepository,
            ProductToGLAccountMappingRepository glAccountMappingRepository, FromJsonHelper fromApiJsonHelper,
            CodeValueRepositoryWrapper codeValueRepositoryWrapper, ChargeRepositoryWrapper chargeRepositoryWrapper) {
        super(glAccountRepository, glAccountMappingRepository, fromApiJsonHelper, codeValueRepositoryWrapper, chargeRepositoryWrapper);
    }

    /*** Set of abstractions for saving Saving Products to GL Account Mappings ***/

    public void saveSavingsToAssetAccountMapping(final JsonElement element, final String paramName, final Long productId,
            final int placeHolderTypeId) {
        saveProductToAccountMapping(element, paramName, productId, placeHolderTypeId, GLAccountType.ASSET, PortfolioProductType.SAVING);
    }

    public void saveSavingsToIncomeAccountMapping(final JsonElement element, final String paramName, final Long productId,
            final int placeHolderTypeId) {
        saveProductToAccountMapping(element, paramName, productId, placeHolderTypeId, GLAccountType.INCOME, PortfolioProductType.SAVING);
    }

    public void saveSavingsToExpenseAccountMapping(final JsonElement element, final String paramName, final Long productId,
            final int placeHolderTypeId) {
        saveProductToAccountMapping(element, paramName, productId, placeHolderTypeId, GLAccountType.EXPENSE, PortfolioProductType.SAVING);
    }

    public void saveSavingsToLiabilityAccountMapping(final JsonElement element, final String paramName, final Long productId,
            final int placeHolderTypeId) {
        saveProductToAccountMapping(element, paramName, productId, placeHolderTypeId, GLAccountType.LIABILITY, PortfolioProductType.SAVING);
    }

    /*** Set of abstractions for merging Savings Products to GL Account Mappings ***/

    public void mergeSavingsToAssetAccountMappingChanges(final JsonElement element, final String paramName, final Long productId,
            final int accountTypeId, final String accountTypeName, final Map<String, Object> changes) {
        mergeProductToAccountMappingChanges(element, paramName, productId, accountTypeId, accountTypeName, changes, GLAccountType.ASSET,
                PortfolioProductType.SAVING);
    }

    public void mergeSavingsToIncomeAccountMappingChanges(final JsonElement element, final String paramName, final Long productId,
            final int accountTypeId, final String accountTypeName, final Map<String, Object> changes) {
        mergeProductToAccountMappingChanges(element, paramName, productId, accountTypeId, accountTypeName, changes, GLAccountType.INCOME,
                PortfolioProductType.SAVING);
    }

    public void mergeSavingsToExpenseAccountMappingChanges(final JsonElement element, final String paramName, final Long productId,
            final int accountTypeId, final String accountTypeName, final Map<String, Object> changes) {
        mergeProductToAccountMappingChanges(element, paramName, productId, accountTypeId, accountTypeName, changes, GLAccountType.EXPENSE,
                PortfolioProductType.SAVING);
    }

    public void mergeSavingsToLiabilityAccountMappingChanges(final JsonElement element, final String paramName, final Long productId,
            final int accountTypeId, final String accountTypeName, final Map<String, Object> changes) {
        mergeProductToAccountMappingChanges(element, paramName, productId, accountTypeId, accountTypeName, changes,
                GLAccountType.LIABILITY, PortfolioProductType.SAVING);
    }

    /*** Abstractions for payments channel related to savings products ***/

    public void savePaymentChannelToFundSourceMappings(final JsonCommand command, final JsonElement element, final Long productId,
            Map<String, Object> changes) {
        savePaymentChannelToFundSourceMappings(command, element, productId, changes, PortfolioProductType.SAVING);
    }

    public void updatePaymentChannelToFundSourceMappings(final JsonCommand command, final JsonElement element, final Long productId,
            Map<String, Object> changes) {
        updatePaymentChannelToFundSourceMappings(command, element, productId, changes, PortfolioProductType.SAVING);
    }

    public Map<String, Object> populateChangesForNewSavingsProductToGLAccountMappingCreation(final JsonElement element,
            final AccountingRuleType accountingRuleType) {
        final Map<String, Object> changes = new HashMap<String, Object>();

        final Long savingsReferenceId = this.fromApiJsonHelper.extractLongNamed(
                SAVINGS_PRODUCT_ACCOUNTING_PARAMS.SAVINGS_REFERENCE.getValue(), element);
        final Long incomeFromFeesId = this.fromApiJsonHelper.extractLongNamed(
                SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue(), element);
        final Long interestOnSavingsId = this.fromApiJsonHelper.extractLongNamed(
                SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_SAVINGS.getValue(), element);
        final Long savingsControlId = this.fromApiJsonHelper.extractLongNamed(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.SAVINGS_CONTROL.getValue(),
                element);
        final Long transfersInSuspenseAccountId = this.fromApiJsonHelper.extractLongNamed(
                SAVINGS_PRODUCT_ACCOUNTING_PARAMS.TRANSFERS_SUSPENSE.getValue(), element);

        switch (accountingRuleType) {
            case NONE:
            break;
            case CASH_BASED:
                changes.put(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.SAVINGS_CONTROL.getValue(), savingsControlId);
                changes.put(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.SAVINGS_REFERENCE.getValue(), savingsReferenceId);
                changes.put(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_SAVINGS.getValue(), interestOnSavingsId);
                changes.put(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue(), incomeFromFeesId);
                changes.put(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.TRANSFERS_SUSPENSE.getValue(), transfersInSuspenseAccountId);
            break;
            case ACCRUAL_BASED:
            break;
        }
        return changes;
    }

    /**
     * Examines and updates each account mapping for given loan product with
     * changes passed in from the Json element
     * 
     * @param savingsProductId
     * @param changes
     * @param element
     * @param accountingRuleType
     */
    public void handleChangesToSavingsProductToGLAccountMappings(final Long savingsProductId, final Map<String, Object> changes,
            final JsonElement element, final AccountingRuleType accountingRuleType) {
        switch (accountingRuleType) {
            case NONE:
            break;
            case CASH_BASED:
                // asset
                mergeSavingsToAssetAccountMappingChanges(element, SAVINGS_PRODUCT_ACCOUNTING_PARAMS.SAVINGS_REFERENCE.getValue(),
                        savingsProductId, CASH_ACCOUNTS_FOR_SAVINGS.SAVINGS_REFERENCE.getValue(),
                        CASH_ACCOUNTS_FOR_SAVINGS.SAVINGS_REFERENCE.toString(), changes);

                // income
                mergeSavingsToIncomeAccountMappingChanges(element, SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue(),
                        savingsProductId, CASH_ACCOUNTS_FOR_SAVINGS.INCOME_FROM_FEES.getValue(),
                        CASH_ACCOUNTS_FOR_SAVINGS.INCOME_FROM_FEES.toString(), changes);

                // expenses
                mergeSavingsToExpenseAccountMappingChanges(element, SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_SAVINGS.getValue(),
                        savingsProductId, CASH_ACCOUNTS_FOR_SAVINGS.INTEREST_ON_SAVINGS.getValue(),
                        CASH_ACCOUNTS_FOR_SAVINGS.INTEREST_ON_SAVINGS.toString(), changes);

                // liability
                mergeSavingsToLiabilityAccountMappingChanges(element, SAVINGS_PRODUCT_ACCOUNTING_PARAMS.SAVINGS_CONTROL.getValue(),
                        savingsProductId, CASH_ACCOUNTS_FOR_SAVINGS.SAVINGS_CONTROL.getValue(),
                        CASH_ACCOUNTS_FOR_SAVINGS.SAVINGS_CONTROL.toString(), changes);
                mergeSavingsToLiabilityAccountMappingChanges(element, SAVINGS_PRODUCT_ACCOUNTING_PARAMS.TRANSFERS_SUSPENSE.getValue(),
                        savingsProductId, CASH_ACCOUNTS_FOR_SAVINGS.TRANSFERS_SUSPENSE.getValue(),
                        CASH_ACCOUNTS_FOR_SAVINGS.TRANSFERS_SUSPENSE.toString(), changes);
            break;
            case ACCRUAL_BASED:
            break;
        }
    }

    public void deleteSavingsProductToGLAccountMapping(final Long savingsProductId) {
        deleteProductToGLAccountMapping(savingsProductId, PortfolioProductType.SAVING);
    }
}
