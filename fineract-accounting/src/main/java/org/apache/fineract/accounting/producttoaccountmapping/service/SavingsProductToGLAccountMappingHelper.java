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

import com.google.gson.JsonElement;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.accounting.common.AccountingConstants.AccrualAccountsForSavings;
import org.apache.fineract.accounting.common.AccountingConstants.CashAccountsForSavings;
import org.apache.fineract.accounting.common.AccountingConstants.SavingProductAccountingParams;
import org.apache.fineract.accounting.common.AccountingRuleType;
import org.apache.fineract.accounting.glaccount.domain.GLAccountRepository;
import org.apache.fineract.accounting.glaccount.domain.GLAccountRepositoryWrapper;
import org.apache.fineract.accounting.glaccount.domain.GLAccountType;
import org.apache.fineract.accounting.producttoaccountmapping.domain.ProductToGLAccountMappingRepository;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.PortfolioProductType;
import org.apache.fineract.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentTypeRepositoryWrapper;
import org.springframework.stereotype.Component;

@Component
public class SavingsProductToGLAccountMappingHelper extends ProductToGLAccountMappingHelper {

    public SavingsProductToGLAccountMappingHelper(final GLAccountRepository glAccountRepository,
            final ProductToGLAccountMappingRepository glAccountMappingRepository, final FromJsonHelper fromApiJsonHelper,
            final ChargeRepositoryWrapper chargeRepositoryWrapper, final GLAccountRepositoryWrapper accountRepositoryWrapper,
            final PaymentTypeRepositoryWrapper paymentTypeRepositoryWrapper) {
        super(glAccountRepository, glAccountMappingRepository, fromApiJsonHelper, chargeRepositoryWrapper, accountRepositoryWrapper,
                paymentTypeRepositoryWrapper);
    }

    /***
     * Set of abstractions for saving Saving Products to GL Account Mappings
     ***/

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

    /***
     * Set of abstractions for merging Savings Products to GL Account Mappings
     ***/

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
        mergeProductToAccountMappingChanges(element, paramName, productId, accountTypeId, accountTypeName, changes, GLAccountType.LIABILITY,
                PortfolioProductType.SAVING);
    }

    public void createOrmergeSavingsToLiabilityAccountMappingChanges(final JsonElement element, final String paramName,
            final Long productId, final int accountTypeId, final Map<String, Object> changes) {
        createOrmergeProductToAccountMappingChanges(element, paramName, productId, accountTypeId, changes, GLAccountType.LIABILITY,
                PortfolioProductType.SAVING);
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
        saveChargesToGLAccountMappings(command, element, productId, changes, PortfolioProductType.SAVING, true);
        saveChargesToGLAccountMappings(command, element, productId, changes, PortfolioProductType.SAVING, false);
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

        final Long savingsReferenceId = this.fromApiJsonHelper.extractLongNamed(SavingProductAccountingParams.SAVINGS_REFERENCE.getValue(),
                element);
        final Long incomeFromFeesId = this.fromApiJsonHelper.extractLongNamed(SavingProductAccountingParams.INCOME_FROM_FEES.getValue(),
                element);
        final Long incomeFromPenaltiesId = this.fromApiJsonHelper
                .extractLongNamed(SavingProductAccountingParams.INCOME_FROM_PENALTIES.getValue(), element);
        final Long interestOnSavingsId = this.fromApiJsonHelper
                .extractLongNamed(SavingProductAccountingParams.INTEREST_ON_SAVINGS.getValue(), element);
        final Long savingsControlId = this.fromApiJsonHelper.extractLongNamed(SavingProductAccountingParams.SAVINGS_CONTROL.getValue(),
                element);
        final Long transfersInSuspenseAccountId = this.fromApiJsonHelper
                .extractLongNamed(SavingProductAccountingParams.TRANSFERS_SUSPENSE.getValue(), element);
        final Long overdraftControlId = this.fromApiJsonHelper
                .extractLongNamed(SavingProductAccountingParams.OVERDRAFT_PORTFOLIO_CONTROL.getValue(), element);
        final Long incomeFromInterest = this.fromApiJsonHelper
                .extractLongNamed(SavingProductAccountingParams.INCOME_FROM_INTEREST.getValue(), element);
        final Long writeOffId = this.fromApiJsonHelper.extractLongNamed(SavingProductAccountingParams.LOSSES_WRITTEN_OFF.getValue(),
                element);
        switch (accountingRuleType) {
            case NONE:
            break;
            case CASH_BASED:
                changes.put(SavingProductAccountingParams.SAVINGS_CONTROL.getValue(), savingsControlId);
                changes.put(SavingProductAccountingParams.SAVINGS_REFERENCE.getValue(), savingsReferenceId);
                changes.put(SavingProductAccountingParams.INTEREST_ON_SAVINGS.getValue(), interestOnSavingsId);
                changes.put(SavingProductAccountingParams.INCOME_FROM_FEES.getValue(), incomeFromFeesId);
                changes.put(SavingProductAccountingParams.INCOME_FROM_PENALTIES.getValue(), incomeFromPenaltiesId);
                changes.put(SavingProductAccountingParams.TRANSFERS_SUSPENSE.getValue(), transfersInSuspenseAccountId);
                changes.put(SavingProductAccountingParams.OVERDRAFT_PORTFOLIO_CONTROL.getValue(), overdraftControlId);
                changes.put(SavingProductAccountingParams.INCOME_FROM_INTEREST.getValue(), incomeFromInterest);
                changes.put(SavingProductAccountingParams.LOSSES_WRITTEN_OFF.getValue(), writeOffId);
            break;
            case ACCRUAL_PERIODIC:
                final Long feeReceivableId = this.fromApiJsonHelper
                        .extractLongNamed(SavingProductAccountingParams.FEES_RECEIVABLE.getValue(), element);
                final Long penaltyReceivableId = this.fromApiJsonHelper
                        .extractLongNamed(SavingProductAccountingParams.PENALTIES_RECEIVABLE.getValue(), element);
                final Long interestPayableId = this.fromApiJsonHelper
                        .extractLongNamed(SavingProductAccountingParams.INTEREST_PAYABLE.getValue(), element);

                changes.put(SavingProductAccountingParams.SAVINGS_CONTROL.getValue(), savingsControlId);
                changes.put(SavingProductAccountingParams.SAVINGS_REFERENCE.getValue(), savingsReferenceId);
                changes.put(SavingProductAccountingParams.INTEREST_ON_SAVINGS.getValue(), interestOnSavingsId);
                changes.put(SavingProductAccountingParams.INCOME_FROM_FEES.getValue(), incomeFromFeesId);
                changes.put(SavingProductAccountingParams.INCOME_FROM_PENALTIES.getValue(), incomeFromPenaltiesId);
                changes.put(SavingProductAccountingParams.TRANSFERS_SUSPENSE.getValue(), transfersInSuspenseAccountId);
                changes.put(SavingProductAccountingParams.OVERDRAFT_PORTFOLIO_CONTROL.getValue(), overdraftControlId);
                changes.put(SavingProductAccountingParams.INCOME_FROM_INTEREST.getValue(), incomeFromInterest);
                changes.put(SavingProductAccountingParams.LOSSES_WRITTEN_OFF.getValue(), writeOffId);
                changes.put(SavingProductAccountingParams.FEES_RECEIVABLE.getValue(), feeReceivableId);
                changes.put(SavingProductAccountingParams.PENALTIES_RECEIVABLE.getValue(), penaltyReceivableId);
                changes.put(SavingProductAccountingParams.INTEREST_PAYABLE.getValue(), interestPayableId);
            break;
            case ACCRUAL_UPFRONT:
            break;
        }
        return changes;
    }

    /**
     * Examines and updates each account mapping for given loan product with changes passed in from the Json element
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
                mergeSavingsToAssetAccountMappingChanges(element, SavingProductAccountingParams.SAVINGS_REFERENCE.getValue(),
                        savingsProductId, CashAccountsForSavings.SAVINGS_REFERENCE.getValue(),
                        CashAccountsForSavings.SAVINGS_REFERENCE.toString(), changes);

                mergeSavingsToAssetAccountMappingChanges(element, SavingProductAccountingParams.OVERDRAFT_PORTFOLIO_CONTROL.getValue(),
                        savingsProductId, CashAccountsForSavings.OVERDRAFT_PORTFOLIO_CONTROL.getValue(),
                        CashAccountsForSavings.OVERDRAFT_PORTFOLIO_CONTROL.toString(), changes);

                // income
                mergeSavingsToIncomeAccountMappingChanges(element, SavingProductAccountingParams.INCOME_FROM_FEES.getValue(),
                        savingsProductId, CashAccountsForSavings.INCOME_FROM_FEES.getValue(),
                        CashAccountsForSavings.INCOME_FROM_FEES.toString(), changes);

                mergeSavingsToIncomeAccountMappingChanges(element, SavingProductAccountingParams.INCOME_FROM_PENALTIES.getValue(),
                        savingsProductId, CashAccountsForSavings.INCOME_FROM_PENALTIES.getValue(),
                        CashAccountsForSavings.INCOME_FROM_PENALTIES.toString(), changes);

                mergeSavingsToIncomeAccountMappingChanges(element, SavingProductAccountingParams.INCOME_FROM_INTEREST.getValue(),
                        savingsProductId, CashAccountsForSavings.INCOME_FROM_INTEREST.getValue(),
                        CashAccountsForSavings.INCOME_FROM_INTEREST.toString(), changes);

                // expenses
                mergeSavingsToExpenseAccountMappingChanges(element, SavingProductAccountingParams.INTEREST_ON_SAVINGS.getValue(),
                        savingsProductId, CashAccountsForSavings.INTEREST_ON_SAVINGS.getValue(),
                        CashAccountsForSavings.INTEREST_ON_SAVINGS.toString(), changes);
                mergeSavingsToExpenseAccountMappingChanges(element, SavingProductAccountingParams.LOSSES_WRITTEN_OFF.getValue(),
                        savingsProductId, CashAccountsForSavings.LOSSES_WRITTEN_OFF.getValue(),
                        CashAccountsForSavings.LOSSES_WRITTEN_OFF.toString(), changes);

                // liability
                mergeSavingsToLiabilityAccountMappingChanges(element, SavingProductAccountingParams.SAVINGS_CONTROL.getValue(),
                        savingsProductId, CashAccountsForSavings.SAVINGS_CONTROL.getValue(),
                        CashAccountsForSavings.SAVINGS_CONTROL.toString(), changes);
                mergeSavingsToLiabilityAccountMappingChanges(element, SavingProductAccountingParams.TRANSFERS_SUSPENSE.getValue(),
                        savingsProductId, CashAccountsForSavings.TRANSFERS_SUSPENSE.getValue(),
                        CashAccountsForSavings.TRANSFERS_SUSPENSE.toString(), changes);
                createOrmergeSavingsToLiabilityAccountMappingChanges(element, SavingProductAccountingParams.ESCHEAT_LIABILITY.getValue(),
                        savingsProductId, CashAccountsForSavings.ESCHEAT_LIABILITY.getValue(), changes);
            break;
            case ACCRUAL_PERIODIC:
                // asset
                mergeSavingsToAssetAccountMappingChanges(element, SavingProductAccountingParams.SAVINGS_REFERENCE.getValue(),
                        savingsProductId, AccrualAccountsForSavings.SAVINGS_REFERENCE.getValue(),
                        AccrualAccountsForSavings.SAVINGS_REFERENCE.toString(), changes);

                mergeSavingsToAssetAccountMappingChanges(element, SavingProductAccountingParams.OVERDRAFT_PORTFOLIO_CONTROL.getValue(),
                        savingsProductId, AccrualAccountsForSavings.OVERDRAFT_PORTFOLIO_CONTROL.getValue(),
                        AccrualAccountsForSavings.OVERDRAFT_PORTFOLIO_CONTROL.toString(), changes);

                mergeSavingsToAssetAccountMappingChanges(element, SavingProductAccountingParams.FEES_RECEIVABLE.getValue(),
                        savingsProductId, AccrualAccountsForSavings.FEES_RECEIVABLE.getValue(),
                        AccrualAccountsForSavings.FEES_RECEIVABLE.toString(), changes);

                mergeSavingsToAssetAccountMappingChanges(element, SavingProductAccountingParams.PENALTIES_RECEIVABLE.getValue(),
                        savingsProductId, AccrualAccountsForSavings.PENALTIES_RECEIVABLE.getValue(),
                        AccrualAccountsForSavings.PENALTIES_RECEIVABLE.toString(), changes);

                // income
                mergeSavingsToIncomeAccountMappingChanges(element, SavingProductAccountingParams.INCOME_FROM_FEES.getValue(),
                        savingsProductId, AccrualAccountsForSavings.INCOME_FROM_FEES.getValue(),
                        AccrualAccountsForSavings.INCOME_FROM_FEES.toString(), changes);

                mergeSavingsToIncomeAccountMappingChanges(element, SavingProductAccountingParams.INCOME_FROM_PENALTIES.getValue(),
                        savingsProductId, AccrualAccountsForSavings.INCOME_FROM_PENALTIES.getValue(),
                        AccrualAccountsForSavings.INCOME_FROM_PENALTIES.toString(), changes);

                mergeSavingsToIncomeAccountMappingChanges(element, SavingProductAccountingParams.INCOME_FROM_INTEREST.getValue(),
                        savingsProductId, AccrualAccountsForSavings.INCOME_FROM_INTEREST.getValue(),
                        AccrualAccountsForSavings.INCOME_FROM_INTEREST.toString(), changes);

                // expenses
                mergeSavingsToExpenseAccountMappingChanges(element, SavingProductAccountingParams.INTEREST_ON_SAVINGS.getValue(),
                        savingsProductId, AccrualAccountsForSavings.INTEREST_ON_SAVINGS.getValue(),
                        AccrualAccountsForSavings.INTEREST_ON_SAVINGS.toString(), changes);

                mergeSavingsToExpenseAccountMappingChanges(element, SavingProductAccountingParams.LOSSES_WRITTEN_OFF.getValue(),
                        savingsProductId, AccrualAccountsForSavings.LOSSES_WRITTEN_OFF.getValue(),
                        AccrualAccountsForSavings.LOSSES_WRITTEN_OFF.toString(), changes);

                // liability
                mergeSavingsToLiabilityAccountMappingChanges(element, SavingProductAccountingParams.SAVINGS_CONTROL.getValue(),
                        savingsProductId, AccrualAccountsForSavings.SAVINGS_CONTROL.getValue(),
                        AccrualAccountsForSavings.SAVINGS_CONTROL.toString(), changes);

                mergeSavingsToLiabilityAccountMappingChanges(element, SavingProductAccountingParams.TRANSFERS_SUSPENSE.getValue(),
                        savingsProductId, AccrualAccountsForSavings.TRANSFERS_SUSPENSE.getValue(),
                        AccrualAccountsForSavings.TRANSFERS_SUSPENSE.toString(), changes);

                mergeSavingsToLiabilityAccountMappingChanges(element, SavingProductAccountingParams.INTEREST_PAYABLE.getValue(),
                        savingsProductId, AccrualAccountsForSavings.INTEREST_PAYABLE.getValue(),
                        AccrualAccountsForSavings.INTEREST_PAYABLE.toString(), changes);

                createOrmergeSavingsToLiabilityAccountMappingChanges(element, SavingProductAccountingParams.ESCHEAT_LIABILITY.getValue(),
                        savingsProductId, AccrualAccountsForSavings.ESCHEAT_LIABILITY.getValue(), changes);

            break;
            case ACCRUAL_UPFRONT:
            break;
        }
    }

    public void deleteSavingsProductToGLAccountMapping(final Long savingsProductId) {
        deleteProductToGLAccountMapping(savingsProductId, PortfolioProductType.SAVING);
    }
}
