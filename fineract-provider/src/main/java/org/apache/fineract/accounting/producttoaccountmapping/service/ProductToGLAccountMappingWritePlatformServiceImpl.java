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

import static org.apache.fineract.portfolio.savings.SavingsApiConstants.accountingRuleParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.isDormancyTrackingActiveParamName;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.fineract.accounting.common.AccountingConstants.SHARES_PRODUCT_ACCOUNTING_PARAMS;
import org.apache.fineract.accounting.common.AccountingRuleType;
import org.apache.fineract.accounting.common.AccountingConstants.ACCRUAL_ACCOUNTS_FOR_LOAN;
import org.apache.fineract.accounting.common.AccountingConstants.CASH_ACCOUNTS_FOR_LOAN;
import org.apache.fineract.accounting.common.AccountingConstants.CASH_ACCOUNTS_FOR_SAVINGS;
import org.apache.fineract.accounting.common.AccountingConstants.CASH_ACCOUNTS_FOR_SHARES;
import org.apache.fineract.accounting.common.AccountingConstants.LOAN_PRODUCT_ACCOUNTING_PARAMS;
import org.apache.fineract.accounting.common.AccountingConstants.SAVINGS_PRODUCT_ACCOUNTING_PARAMS;
import org.apache.fineract.accounting.producttoaccountmapping.serialization.ProductToGLAccountMappingFromApiJsonDeserializer;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonElement;

@Service
public class ProductToGLAccountMappingWritePlatformServiceImpl implements ProductToGLAccountMappingWritePlatformService {

    private final FromJsonHelper fromApiJsonHelper;
    private final ProductToGLAccountMappingFromApiJsonDeserializer deserializer;
    private final LoanProductToGLAccountMappingHelper loanProductToGLAccountMappingHelper;
    private final SavingsProductToGLAccountMappingHelper savingsProductToGLAccountMappingHelper;
    private final ShareProductToGLAccountMappingHelper shareProductToGLAccountMappingHelper;

    @Autowired
    public ProductToGLAccountMappingWritePlatformServiceImpl(final FromJsonHelper fromApiJsonHelper,
            final ProductToGLAccountMappingFromApiJsonDeserializer deserializer,
            final LoanProductToGLAccountMappingHelper loanProductToGLAccountMappingHelper,
            final SavingsProductToGLAccountMappingHelper savingsProductToGLAccountMappingHelper,
            final ShareProductToGLAccountMappingHelper shareProductToGLAccountMappingHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.deserializer = deserializer;
        this.loanProductToGLAccountMappingHelper = loanProductToGLAccountMappingHelper;
        this.savingsProductToGLAccountMappingHelper = savingsProductToGLAccountMappingHelper;
        this.shareProductToGLAccountMappingHelper = shareProductToGLAccountMappingHelper;
    }

    @Override
    @Transactional
    public void createLoanProductToGLAccountMapping(final Long loanProductId, final JsonCommand command) {
        final JsonElement element = this.fromApiJsonHelper.parse(command.json());
        final Integer accountingRuleTypeId = this.fromApiJsonHelper.extractIntegerNamed("accountingRule", element, Locale.getDefault());
        final AccountingRuleType accountingRuleType = AccountingRuleType.fromInt(accountingRuleTypeId);

        switch (accountingRuleType) {
            case NONE:
            break;
            case CASH_BASED:
                // asset
                this.loanProductToGLAccountMappingHelper
                        .saveLoanToAssetAccountMapping(element, LOAN_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue(), loanProductId,
                                CASH_ACCOUNTS_FOR_LOAN.FUND_SOURCE.getValue());
                this.loanProductToGLAccountMappingHelper.saveLoanToAssetAccountMapping(element,
                        LOAN_PRODUCT_ACCOUNTING_PARAMS.LOAN_PORTFOLIO.getValue(), loanProductId,
                        CASH_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO.getValue());
                this.loanProductToGLAccountMappingHelper.saveLoanToAssetAccountMapping(element,
                        LOAN_PRODUCT_ACCOUNTING_PARAMS.TRANSFERS_SUSPENSE.getValue(), loanProductId,
                        CASH_ACCOUNTS_FOR_LOAN.TRANSFERS_SUSPENSE.getValue());

                // income
                this.loanProductToGLAccountMappingHelper.saveLoanToIncomeAccountMapping(element,
                        LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_LOANS.getValue(), loanProductId,
                        CASH_ACCOUNTS_FOR_LOAN.INTEREST_ON_LOANS.getValue());
                this.loanProductToGLAccountMappingHelper.saveLoanToIncomeAccountMapping(element,
                        LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue(), loanProductId,
                        CASH_ACCOUNTS_FOR_LOAN.INCOME_FROM_FEES.getValue());
                this.loanProductToGLAccountMappingHelper.saveLoanToIncomeAccountMapping(element,
                        LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_PENALTIES.getValue(), loanProductId,
                        CASH_ACCOUNTS_FOR_LOAN.INCOME_FROM_PENALTIES.getValue());
                this.loanProductToGLAccountMappingHelper.saveLoanToIncomeAccountMapping(element,
                        LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_RECOVERY.getValue(), loanProductId,
                        CASH_ACCOUNTS_FOR_LOAN.INCOME_FROM_RECOVERY.getValue());

                // expenses
                this.loanProductToGLAccountMappingHelper.saveLoanToExpenseAccountMapping(element,
                        LOAN_PRODUCT_ACCOUNTING_PARAMS.LOSSES_WRITTEN_OFF.getValue(), loanProductId,
                        CASH_ACCOUNTS_FOR_LOAN.LOSSES_WRITTEN_OFF.getValue());

                // liabilities
                this.loanProductToGLAccountMappingHelper
                        .saveLoanToLiabilityAccountMapping(element, LOAN_PRODUCT_ACCOUNTING_PARAMS.OVERPAYMENT.getValue(), loanProductId,
                                CASH_ACCOUNTS_FOR_LOAN.OVERPAYMENT.getValue());

                // advanced accounting mappings
                this.loanProductToGLAccountMappingHelper.savePaymentChannelToFundSourceMappings(command, element, loanProductId, null);
                this.loanProductToGLAccountMappingHelper.saveChargesToIncomeAccountMappings(command, element, loanProductId, null);
            break;
            case ACCRUAL_UPFRONT:
                // Fall Through
            case ACCRUAL_PERIODIC:
                // assets (including receivables)
                this.loanProductToGLAccountMappingHelper.saveLoanToAssetAccountMapping(element,
                        LOAN_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue(), loanProductId,
                        ACCRUAL_ACCOUNTS_FOR_LOAN.FUND_SOURCE.getValue());
                this.loanProductToGLAccountMappingHelper.saveLoanToAssetAccountMapping(element,
                        LOAN_PRODUCT_ACCOUNTING_PARAMS.LOAN_PORTFOLIO.getValue(), loanProductId,
                        ACCRUAL_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO.getValue());
                this.loanProductToGLAccountMappingHelper.saveLoanToAssetAccountMapping(element,
                        LOAN_PRODUCT_ACCOUNTING_PARAMS.TRANSFERS_SUSPENSE.getValue(), loanProductId,
                        ACCRUAL_ACCOUNTS_FOR_LOAN.TRANSFERS_SUSPENSE.getValue());
                this.loanProductToGLAccountMappingHelper.saveLoanToAssetAccountMapping(element,
                        LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_RECEIVABLE.getValue(), loanProductId,
                        ACCRUAL_ACCOUNTS_FOR_LOAN.INTEREST_RECEIVABLE.getValue());
                this.loanProductToGLAccountMappingHelper.saveLoanToAssetAccountMapping(element,
                        LOAN_PRODUCT_ACCOUNTING_PARAMS.FEES_RECEIVABLE.getValue(), loanProductId,
                        ACCRUAL_ACCOUNTS_FOR_LOAN.FEES_RECEIVABLE.getValue());
                this.loanProductToGLAccountMappingHelper.saveLoanToAssetAccountMapping(element,
                        LOAN_PRODUCT_ACCOUNTING_PARAMS.PENALTIES_RECEIVABLE.getValue(), loanProductId,
                        ACCRUAL_ACCOUNTS_FOR_LOAN.PENALTIES_RECEIVABLE.getValue());

                // income
                this.loanProductToGLAccountMappingHelper.saveLoanToIncomeAccountMapping(element,
                        LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_LOANS.getValue(), loanProductId,
                        ACCRUAL_ACCOUNTS_FOR_LOAN.INTEREST_ON_LOANS.getValue());
                this.loanProductToGLAccountMappingHelper.saveLoanToIncomeAccountMapping(element,
                        LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue(), loanProductId,
                        ACCRUAL_ACCOUNTS_FOR_LOAN.INCOME_FROM_FEES.getValue());
                this.loanProductToGLAccountMappingHelper.saveLoanToIncomeAccountMapping(element,
                        LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_PENALTIES.getValue(), loanProductId,
                        ACCRUAL_ACCOUNTS_FOR_LOAN.INCOME_FROM_PENALTIES.getValue());
                this.loanProductToGLAccountMappingHelper.saveLoanToIncomeAccountMapping(element,
                        LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_RECOVERY.getValue(), loanProductId,
                        ACCRUAL_ACCOUNTS_FOR_LOAN.INCOME_FROM_RECOVERY.getValue());

                // expenses
                this.loanProductToGLAccountMappingHelper.saveLoanToExpenseAccountMapping(element,
                        LOAN_PRODUCT_ACCOUNTING_PARAMS.LOSSES_WRITTEN_OFF.getValue(), loanProductId,
                        ACCRUAL_ACCOUNTS_FOR_LOAN.LOSSES_WRITTEN_OFF.getValue());

                // liabilities
                this.loanProductToGLAccountMappingHelper.saveLoanToLiabilityAccountMapping(element,
                        LOAN_PRODUCT_ACCOUNTING_PARAMS.OVERPAYMENT.getValue(), loanProductId,
                        ACCRUAL_ACCOUNTS_FOR_LOAN.OVERPAYMENT.getValue());

                // advanced accounting mappings
                this.loanProductToGLAccountMappingHelper.savePaymentChannelToFundSourceMappings(command, element, loanProductId, null);
                this.loanProductToGLAccountMappingHelper.saveChargesToIncomeAccountMappings(command, element, loanProductId, null);
            break;
        }
    }

    @Override
    @Transactional
    public void createSavingProductToGLAccountMapping(final Long savingProductId, final JsonCommand command, DepositAccountType accountType) {
        final JsonElement element = this.fromApiJsonHelper.parse(command.json());
        final Integer accountingRuleTypeId = this.fromApiJsonHelper.extractIntegerNamed(accountingRuleParamName, element,
                Locale.getDefault());
        final AccountingRuleType accountingRuleType = AccountingRuleType.fromInt(accountingRuleTypeId);

        switch (accountingRuleType) {
            case NONE:
            break;
            case CASH_BASED:
                // asset
                this.savingsProductToGLAccountMappingHelper.saveSavingsToAssetAccountMapping(element,
                        SAVINGS_PRODUCT_ACCOUNTING_PARAMS.SAVINGS_REFERENCE.getValue(), savingProductId,
                        CASH_ACCOUNTS_FOR_SAVINGS.SAVINGS_REFERENCE.getValue());

                if (!accountType.equals(DepositAccountType.RECURRING_DEPOSIT) && !accountType.equals(DepositAccountType.FIXED_DEPOSIT)) {
                    this.savingsProductToGLAccountMappingHelper.saveSavingsToAssetAccountMapping(element,
                            SAVINGS_PRODUCT_ACCOUNTING_PARAMS.OVERDRAFT_PORTFOLIO_CONTROL.getValue(), savingProductId,
                            CASH_ACCOUNTS_FOR_SAVINGS.OVERDRAFT_PORTFOLIO_CONTROL.getValue());
                }

                // income
                this.savingsProductToGLAccountMappingHelper.saveSavingsToIncomeAccountMapping(element,
                        SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue(), savingProductId,
                        CASH_ACCOUNTS_FOR_SAVINGS.INCOME_FROM_FEES.getValue());

                this.savingsProductToGLAccountMappingHelper.saveSavingsToIncomeAccountMapping(element,
                        SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_PENALTIES.getValue(), savingProductId,
                        CASH_ACCOUNTS_FOR_SAVINGS.INCOME_FROM_PENALTIES.getValue());

                if (!accountType.equals(DepositAccountType.RECURRING_DEPOSIT) && !accountType.equals(DepositAccountType.FIXED_DEPOSIT)) {
                    this.savingsProductToGLAccountMappingHelper.saveSavingsToIncomeAccountMapping(element,
                            SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_INTEREST.getValue(), savingProductId,
                            CASH_ACCOUNTS_FOR_SAVINGS.INCOME_FROM_INTEREST.getValue());
                }

                // expenses
                this.savingsProductToGLAccountMappingHelper.saveSavingsToExpenseAccountMapping(element,
                        SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_SAVINGS.getValue(), savingProductId,
                        CASH_ACCOUNTS_FOR_SAVINGS.INTEREST_ON_SAVINGS.getValue());

                if (!accountType.equals(DepositAccountType.RECURRING_DEPOSIT) && !accountType.equals(DepositAccountType.FIXED_DEPOSIT)) {
                    this.savingsProductToGLAccountMappingHelper.saveSavingsToExpenseAccountMapping(element,
                            SAVINGS_PRODUCT_ACCOUNTING_PARAMS.LOSSES_WRITTEN_OFF.getValue(), savingProductId,
                            CASH_ACCOUNTS_FOR_SAVINGS.LOSSES_WRITTEN_OFF.getValue());
                }

                // liability
                this.savingsProductToGLAccountMappingHelper.saveSavingsToLiabilityAccountMapping(element,
                        SAVINGS_PRODUCT_ACCOUNTING_PARAMS.SAVINGS_CONTROL.getValue(), savingProductId,
                        CASH_ACCOUNTS_FOR_SAVINGS.SAVINGS_CONTROL.getValue());
                this.savingsProductToGLAccountMappingHelper.saveSavingsToLiabilityAccountMapping(element,
                        SAVINGS_PRODUCT_ACCOUNTING_PARAMS.TRANSFERS_SUSPENSE.getValue(), savingProductId,
                        CASH_ACCOUNTS_FOR_SAVINGS.TRANSFERS_SUSPENSE.getValue());
                
                final Boolean isDormancyTrackingActive = this.fromApiJsonHelper.extractBooleanNamed(isDormancyTrackingActiveParamName, element);
                if(null != isDormancyTrackingActive && isDormancyTrackingActive){
                    this.savingsProductToGLAccountMappingHelper.saveSavingsToLiabilityAccountMapping(element,
                            SAVINGS_PRODUCT_ACCOUNTING_PARAMS.ESCHEAT_LIABILITY.getValue(), savingProductId,
                            CASH_ACCOUNTS_FOR_SAVINGS.ESCHEAT_LIABILITY.getValue());
                }

                // advanced accounting mappings
                this.savingsProductToGLAccountMappingHelper.savePaymentChannelToFundSourceMappings(command, element, savingProductId, null);
                this.savingsProductToGLAccountMappingHelper.saveChargesToIncomeAccountMappings(command, element, savingProductId, null);
            break;
            default:
            break;
        }

    }

    @Override
    @Transactional
    public void createShareProductToGLAccountMapping(final Long shareProductId, final JsonCommand command) {

        this.deserializer.validateForShareProductCreate(command.json());
        final JsonElement element = this.fromApiJsonHelper.parse(command.json());
        final Integer accountingRuleTypeId = this.fromApiJsonHelper.extractIntegerNamed(accountingRuleParamName, element,
                Locale.getDefault());
        final AccountingRuleType accountingRuleType = AccountingRuleType.fromInt(accountingRuleTypeId);

        switch (accountingRuleType) {
            case NONE:
            break;
            case CASH_BASED:
                // asset
                this.shareProductToGLAccountMappingHelper.saveSharesToAssetAccountMapping(element,
                        SHARES_PRODUCT_ACCOUNTING_PARAMS.SHARES_REFERENCE.getValue(), shareProductId,
                        CASH_ACCOUNTS_FOR_SHARES.SHARES_REFERENCE.getValue());

                // income
                this.shareProductToGLAccountMappingHelper.saveSharesToIncomeAccountMapping(element,
                        SHARES_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue(), shareProductId,
                        CASH_ACCOUNTS_FOR_SHARES.INCOME_FROM_FEES.getValue());

                // expenses
                this.shareProductToGLAccountMappingHelper.saveSharesToEquityAccountMapping(element,
                        SHARES_PRODUCT_ACCOUNTING_PARAMS.SHARES_EQUITY.getValue(), shareProductId,
                        CASH_ACCOUNTS_FOR_SHARES.SHARES_EQUITY.getValue());

                // liability
                this.shareProductToGLAccountMappingHelper.saveSharesToLiabilityAccountMapping(element,
                        SHARES_PRODUCT_ACCOUNTING_PARAMS.SHARES_SUSPENSE.getValue(), shareProductId,
                        CASH_ACCOUNTS_FOR_SHARES.SHARES_SUSPENSE.getValue());

                // advanced accounting mappings
                this.savingsProductToGLAccountMappingHelper.savePaymentChannelToFundSourceMappings(command, element, shareProductId, null);
                this.savingsProductToGLAccountMappingHelper.saveChargesToIncomeAccountMappings(command, element, shareProductId, null);
            break;
            default:
            break;
        }

    }

    @Override
    @Transactional
    public Map<String, Object> updateLoanProductToGLAccountMapping(final Long loanProductId, final JsonCommand command,
            final boolean accountingRuleChanged, final int accountingRuleTypeId) {
        /***
         * Variable tracks all accounting mapping properties that have been
         * updated
         ***/
        Map<String, Object> changes = new HashMap<>();
        final JsonElement element = this.fromApiJsonHelper.parse(command.json());
        final AccountingRuleType accountingRuleType = AccountingRuleType.fromInt(accountingRuleTypeId);

        /***
         * If the accounting rule has been changed, delete all existing mapping
         * for the product and recreate a new set of mappings
         ***/
        if (accountingRuleChanged) {
            this.deserializer.validateForLoanProductCreate(command.json());
            this.loanProductToGLAccountMappingHelper.deleteLoanProductToGLAccountMapping(loanProductId);
            createLoanProductToGLAccountMapping(loanProductId, command);
            changes = this.loanProductToGLAccountMappingHelper.populateChangesForNewLoanProductToGLAccountMappingCreation(element,
                    accountingRuleType);
        }/*** else examine and update individual changes ***/
        else {
            this.loanProductToGLAccountMappingHelper.handleChangesToLoanProductToGLAccountMappings(loanProductId, changes, element,
                    accountingRuleType);
            this.loanProductToGLAccountMappingHelper.updatePaymentChannelToFundSourceMappings(command, element, loanProductId, changes);
            this.loanProductToGLAccountMappingHelper.updateChargesToIncomeAccountMappings(command, element, loanProductId, changes);
        }
        return changes;
    }

    @Override
    public Map<String, Object> updateSavingsProductToGLAccountMapping(final Long savingsProductId, final JsonCommand command,
            final boolean accountingRuleChanged, final int accountingRuleTypeId, final DepositAccountType accountType) {
        /***
         * Variable tracks all accounting mapping properties that have been
         * updated
         ***/
        Map<String, Object> changes = new HashMap<>();
        final JsonElement element = this.fromApiJsonHelper.parse(command.json());
        final AccountingRuleType accountingRuleType = AccountingRuleType.fromInt(accountingRuleTypeId);

        /***
         * If the accounting rule has been changed, delete all existing mapping
         * for the product and recreate a new set of mappings
         ***/
        if (accountingRuleChanged) {
            this.deserializer.validateForSavingsProductCreate(command.json(), accountType);
            this.savingsProductToGLAccountMappingHelper.deleteSavingsProductToGLAccountMapping(savingsProductId);
            createSavingProductToGLAccountMapping(savingsProductId, command, accountType);
            changes = this.savingsProductToGLAccountMappingHelper.populateChangesForNewSavingsProductToGLAccountMappingCreation(element,
                    accountingRuleType);
        }/*** else examine and update individual changes ***/
        else {
            this.savingsProductToGLAccountMappingHelper.handleChangesToSavingsProductToGLAccountMappings(savingsProductId, changes,
                    element, accountingRuleType);
            this.savingsProductToGLAccountMappingHelper.updatePaymentChannelToFundSourceMappings(command, element, savingsProductId,
                    changes);
            this.savingsProductToGLAccountMappingHelper.updateChargesToIncomeAccountMappings(command, element, savingsProductId, changes);
        }
        return changes;
    }

    @Override
    public Map<String, Object> updateShareProductToGLAccountMapping(final Long shareProductId, final JsonCommand command,
            final boolean accountingRuleChanged, final int accountingRuleTypeId) {
        /***
         * Variable tracks all accounting mapping properties that have been
         * updated
         ***/
        Map<String, Object> changes = new HashMap<>();
        final JsonElement element = this.fromApiJsonHelper.parse(command.json());
        final AccountingRuleType accountingRuleType = AccountingRuleType.fromInt(accountingRuleTypeId);

        /***
         * If the accounting rule has been changed, delete all existing mapping
         * for the product and recreate a new set of mappings
         ***/
        if (accountingRuleChanged) {
            this.shareProductToGLAccountMappingHelper.deleteSharesProductToGLAccountMapping(shareProductId);
            createShareProductToGLAccountMapping(shareProductId, command);
            changes = this.shareProductToGLAccountMappingHelper.populateChangesForNewSharesProductToGLAccountMappingCreation(element,
                    accountingRuleType);
        }/*** else examine and update individual changes ***/
        else {
            this.shareProductToGLAccountMappingHelper.handleChangesToSharesProductToGLAccountMappings(shareProductId, changes, element,
                    accountingRuleType);
            this.shareProductToGLAccountMappingHelper.updatePaymentChannelToFundSourceMappings(command, element, shareProductId, changes);
            this.shareProductToGLAccountMappingHelper.updateChargesToIncomeAccountMappings(command, element, shareProductId, changes);
        }
        return changes;
    }
}
