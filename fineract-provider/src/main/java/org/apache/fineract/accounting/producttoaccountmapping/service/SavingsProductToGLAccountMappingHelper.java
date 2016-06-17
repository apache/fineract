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
package org.apache.fineract.accounting.producttoaccountmapping.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.fineract.accounting.common.AccountingRuleType;
import org.apache.fineract.accounting.common.AccountingConstants.CASH_ACCOUNTS_FOR_SAVINGS;
import org.apache.fineract.accounting.common.AccountingConstants.SAVINGS_PRODUCT_ACCOUNTING_PARAMS;
import org.apache.fineract.accounting.glaccount.domain.GLAccountRepository;
import org.apache.fineract.accounting.glaccount.domain.GLAccountRepositoryWrapper;
import org.apache.fineract.accounting.glaccount.domain.GLAccountType;
import org.apache.fineract.accounting.producttoaccountmapping.domain.PortfolioProductType;
import org.apache.fineract.accounting.producttoaccountmapping.domain.ProductToGLAccountMappingRepository;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentTypeRepositoryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;

@Component
public class SavingsProductToGLAccountMappingHelper extends ProductToGLAccountMappingHelper {

    @Autowired
    public SavingsProductToGLAccountMappingHelper(final GLAccountRepository glAccountRepository,
            final ProductToGLAccountMappingRepository glAccountMappingRepository, final FromJsonHelper fromApiJsonHelper,
            final ChargeRepositoryWrapper chargeRepositoryWrapper, final GLAccountRepositoryWrapper accountRepositoryWrapper,
            final PaymentTypeRepositoryWrapper paymentTypeRepositoryWrapper) {
        super(glAccountRepository, glAccountMappingRepository, fromApiJsonHelper, chargeRepositoryWrapper, accountRepositoryWrapper,
                paymentTypeRepositoryWrapper);
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

    public void createOrmergeSavingsToLiabilityAccountMappingChanges(final JsonElement element, final String paramName, final Long productId,
            final int accountTypeId, final Map<String, Object> changes) {
        createOrmergeProductToAccountMappingChanges(element, paramName, productId, accountTypeId, changes,
                GLAccountType.LIABILITY, PortfolioProductType.SAVING);
    }
    
    /*** Abstractions for payments channel related to savings products ***/

    public void savePaymentChannelToFundSourceMappings(final JsonCommand command, final JsonElement element, final Long productId,
            final Map<String, Object> changes) {
        savePaymentChannelToFundSourceMappings(command, element, productId, changes, PortfolioProductType.SAVING);
    }

    public void updatePaymentChannelToFundSourceMappings(final JsonCommand command, final JsonElement element, final Long productId,
            final Map<String, Object> changes) {
        updatePaymentChannelToFundSourceMappings(command, element, productId, changes, PortfolioProductType.SAVING);
    }

    public void saveChargesToIncomeAccountMappings(final JsonCommand command, final JsonElement element, final Long productId,
            final Map<String, Object> changes) {
        // save both fee and penalty charges
        saveChargesToIncomeOrLiabilityAccountMappings(command, element, productId, changes, PortfolioProductType.SAVING, true);
        saveChargesToIncomeOrLiabilityAccountMappings(command, element, productId, changes, PortfolioProductType.SAVING, false);
    }

    public void updateChargesToIncomeAccountMappings(final JsonCommand command, final JsonElement element, final Long productId,
            final Map<String, Object> changes) {
        // update both fee and penalty charges
        updateChargeToIncomeAccountMappings(command, element, productId, changes, PortfolioProductType.SAVING, true);
        updateChargeToIncomeAccountMappings(command, element, productId, changes, PortfolioProductType.SAVING, false);
    }

    public Map<String, Object> populateChangesForNewSavingsProductToGLAccountMappingCreation(final JsonElement element,
            final AccountingRuleType accountingRuleType) {
        final Map<String, Object> changes = new HashMap<>();

        final Long savingsReferenceId = this.fromApiJsonHelper.extractLongNamed(
                SAVINGS_PRODUCT_ACCOUNTING_PARAMS.SAVINGS_REFERENCE.getValue(), element);
        final Long incomeFromFeesId = this.fromApiJsonHelper.extractLongNamed(
                SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue(), element);
        final Long incomeFromPenaltiesId = this.fromApiJsonHelper.extractLongNamed(
                SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_PENALTIES.getValue(), element);
        final Long interestOnSavingsId = this.fromApiJsonHelper.extractLongNamed(
                SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_SAVINGS.getValue(), element);
        final Long savingsControlId = this.fromApiJsonHelper.extractLongNamed(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.SAVINGS_CONTROL.getValue(),
                element);
        final Long transfersInSuspenseAccountId = this.fromApiJsonHelper.extractLongNamed(
                SAVINGS_PRODUCT_ACCOUNTING_PARAMS.TRANSFERS_SUSPENSE.getValue(), element);
        final Long overdraftControlId = this.fromApiJsonHelper.extractLongNamed(
                SAVINGS_PRODUCT_ACCOUNTING_PARAMS.OVERDRAFT_PORTFOLIO_CONTROL.getValue(), element);
        final Long incomeFromInterest = this.fromApiJsonHelper.extractLongNamed(
                SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_INTEREST.getValue(), element);
        final Long writeOffId = this.fromApiJsonHelper.extractLongNamed(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.LOSSES_WRITTEN_OFF.getValue(),
                element);
        switch (accountingRuleType) {
            case NONE:
            break;
            case CASH_BASED:
                changes.put(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.SAVINGS_CONTROL.getValue(), savingsControlId);
                changes.put(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.SAVINGS_REFERENCE.getValue(), savingsReferenceId);
                changes.put(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_SAVINGS.getValue(), interestOnSavingsId);
                changes.put(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue(), incomeFromFeesId);
                changes.put(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_PENALTIES.getValue(), incomeFromPenaltiesId);
                changes.put(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.TRANSFERS_SUSPENSE.getValue(), transfersInSuspenseAccountId);
                changes.put(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.OVERDRAFT_PORTFOLIO_CONTROL.getValue(), overdraftControlId);
                changes.put(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_INTEREST.getValue(), incomeFromInterest);
                changes.put(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.LOSSES_WRITTEN_OFF.getValue(), writeOffId);
            break;
            case ACCRUAL_PERIODIC:
            break;
            case ACCRUAL_UPFRONT:
            break;
            default:
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

                mergeSavingsToAssetAccountMappingChanges(element, SAVINGS_PRODUCT_ACCOUNTING_PARAMS.OVERDRAFT_PORTFOLIO_CONTROL.getValue(),
                        savingsProductId, CASH_ACCOUNTS_FOR_SAVINGS.OVERDRAFT_PORTFOLIO_CONTROL.getValue(),
                        CASH_ACCOUNTS_FOR_SAVINGS.OVERDRAFT_PORTFOLIO_CONTROL.toString(), changes);

                // income
                mergeSavingsToIncomeAccountMappingChanges(element, SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue(),
                        savingsProductId, CASH_ACCOUNTS_FOR_SAVINGS.INCOME_FROM_FEES.getValue(),
                        CASH_ACCOUNTS_FOR_SAVINGS.INCOME_FROM_FEES.toString(), changes);

                mergeSavingsToIncomeAccountMappingChanges(element, SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_PENALTIES.getValue(),
                        savingsProductId, CASH_ACCOUNTS_FOR_SAVINGS.INCOME_FROM_PENALTIES.getValue(),
                        CASH_ACCOUNTS_FOR_SAVINGS.INCOME_FROM_PENALTIES.toString(), changes);

                mergeSavingsToIncomeAccountMappingChanges(element, SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_INTEREST.getValue(),
                        savingsProductId, CASH_ACCOUNTS_FOR_SAVINGS.INCOME_FROM_INTEREST.getValue(),
                        CASH_ACCOUNTS_FOR_SAVINGS.INCOME_FROM_INTEREST.toString(), changes);

                // expenses
                mergeSavingsToExpenseAccountMappingChanges(element, SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_SAVINGS.getValue(),
                        savingsProductId, CASH_ACCOUNTS_FOR_SAVINGS.INTEREST_ON_SAVINGS.getValue(),
                        CASH_ACCOUNTS_FOR_SAVINGS.INTEREST_ON_SAVINGS.toString(), changes);
                mergeSavingsToExpenseAccountMappingChanges(element, SAVINGS_PRODUCT_ACCOUNTING_PARAMS.LOSSES_WRITTEN_OFF.getValue(),
                        savingsProductId, CASH_ACCOUNTS_FOR_SAVINGS.LOSSES_WRITTEN_OFF.getValue(),
                        CASH_ACCOUNTS_FOR_SAVINGS.LOSSES_WRITTEN_OFF.toString(), changes);

                // liability
                mergeSavingsToLiabilityAccountMappingChanges(element, SAVINGS_PRODUCT_ACCOUNTING_PARAMS.SAVINGS_CONTROL.getValue(),
                        savingsProductId, CASH_ACCOUNTS_FOR_SAVINGS.SAVINGS_CONTROL.getValue(),
                        CASH_ACCOUNTS_FOR_SAVINGS.SAVINGS_CONTROL.toString(), changes);
                mergeSavingsToLiabilityAccountMappingChanges(element, SAVINGS_PRODUCT_ACCOUNTING_PARAMS.TRANSFERS_SUSPENSE.getValue(),
                        savingsProductId, CASH_ACCOUNTS_FOR_SAVINGS.TRANSFERS_SUSPENSE.getValue(),
                        CASH_ACCOUNTS_FOR_SAVINGS.TRANSFERS_SUSPENSE.toString(), changes);
                createOrmergeSavingsToLiabilityAccountMappingChanges(element, SAVINGS_PRODUCT_ACCOUNTING_PARAMS.ESCHEAT_LIABILITY.getValue(),
                        savingsProductId, CASH_ACCOUNTS_FOR_SAVINGS.ESCHEAT_LIABILITY.getValue(),
                         changes);
            break;
            case ACCRUAL_PERIODIC:
            break;
            case ACCRUAL_UPFRONT:
            break;
            default:
            break;
        }
    }

    public void deleteSavingsProductToGLAccountMapping(final Long savingsProductId) {
        deleteProductToGLAccountMapping(savingsProductId, PortfolioProductType.SAVING);
    }
}
