/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.producttoaccountmapping.service;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.mifosplatform.accounting.common.AccountingRuleType;
import org.mifosplatform.accounting.common.AccountingConstants.ACCRUAL_ACCOUNTS_FOR_LOAN;
import org.mifosplatform.accounting.common.AccountingConstants.CASH_ACCOUNTS_FOR_LOAN;
import org.mifosplatform.accounting.common.AccountingConstants.CASH_ACCOUNTS_FOR_SAVINGS;
import org.mifosplatform.accounting.common.AccountingConstants.LOAN_PRODUCT_ACCOUNTING_PARAMS;
import org.mifosplatform.accounting.common.AccountingConstants.SAVINGS_PRODUCT_ACCOUNTING_PARAMS;
import org.mifosplatform.accounting.producttoaccountmapping.serialization.ProductToGLAccountMappingFromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.mifosplatform.portfolio.savings.SavingsApiConstants.*;

import com.google.gson.JsonElement;

@Service
public class ProductToGLAccountMappingWritePlatformServiceImpl implements ProductToGLAccountMappingWritePlatformService {

    private final FromJsonHelper fromApiJsonHelper;
    private final ProductToGLAccountMappingFromApiJsonDeserializer deserializer;
    private final LoanProductToGLAccountMappingHelper loanProductToGLAccountMappingHelper;
    private final SavingsProductToGLAccountMappingHelper savingsProductToGLAccountMappingHelper;

    @Autowired
    public ProductToGLAccountMappingWritePlatformServiceImpl(final FromJsonHelper fromApiJsonHelper,
            final ProductToGLAccountMappingFromApiJsonDeserializer deserializer,
            LoanProductToGLAccountMappingHelper loanProductToGLAccountMappingHelper,
            SavingsProductToGLAccountMappingHelper savingsProductToGLAccountMappingHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.deserializer = deserializer;
        this.loanProductToGLAccountMappingHelper = loanProductToGLAccountMappingHelper;
        this.savingsProductToGLAccountMappingHelper = savingsProductToGLAccountMappingHelper;
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
                loanProductToGLAccountMappingHelper
                        .saveLoanToAssetAccountMapping(element, LOAN_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue(), loanProductId,
                                CASH_ACCOUNTS_FOR_LOAN.FUND_SOURCE.getValue());
                loanProductToGLAccountMappingHelper.saveLoanToAssetAccountMapping(element,
                        LOAN_PRODUCT_ACCOUNTING_PARAMS.LOAN_PORTFOLIO.getValue(), loanProductId,
                        CASH_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO.getValue());

                // income
                loanProductToGLAccountMappingHelper.saveLoanToIncomeAccountMapping(element,
                        LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_LOANS.getValue(), loanProductId,
                        CASH_ACCOUNTS_FOR_LOAN.INTEREST_ON_LOANS.getValue());
                loanProductToGLAccountMappingHelper.saveLoanToIncomeAccountMapping(element,
                        LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue(), loanProductId,
                        CASH_ACCOUNTS_FOR_LOAN.INCOME_FROM_FEES.getValue());
                loanProductToGLAccountMappingHelper.saveLoanToIncomeAccountMapping(element,
                        LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_PENALTIES.getValue(), loanProductId,
                        CASH_ACCOUNTS_FOR_LOAN.INCOME_FROM_PENALTIES.getValue());

                // expenses
                loanProductToGLAccountMappingHelper.saveLoanToExpenseAccountMapping(element,
                        LOAN_PRODUCT_ACCOUNTING_PARAMS.LOSSES_WRITTEN_OFF.getValue(), loanProductId,
                        CASH_ACCOUNTS_FOR_LOAN.LOSSES_WRITTEN_OFF.getValue());

                // advanced accounting mappings
                loanProductToGLAccountMappingHelper.savePaymentChannelToFundSourceMappings(command, element, loanProductId, null);
                loanProductToGLAccountMappingHelper.saveChargesToIncomeAccountMappings(command, element, loanProductId, null);
            break;
            case ACCRUAL_BASED:

                // assets (including receivables)
                loanProductToGLAccountMappingHelper.saveLoanToAssetAccountMapping(element,
                        LOAN_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue(), loanProductId,
                        ACCRUAL_ACCOUNTS_FOR_LOAN.FUND_SOURCE.getValue());
                loanProductToGLAccountMappingHelper.saveLoanToAssetAccountMapping(element,
                        LOAN_PRODUCT_ACCOUNTING_PARAMS.LOAN_PORTFOLIO.getValue(), loanProductId,
                        ACCRUAL_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO.getValue());
                loanProductToGLAccountMappingHelper.saveLoanToAssetAccountMapping(element,
                        LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_RECEIVABLE.getValue(), loanProductId,
                        ACCRUAL_ACCOUNTS_FOR_LOAN.INTEREST_RECEIVABLE.getValue());
                loanProductToGLAccountMappingHelper.saveLoanToAssetAccountMapping(element,
                        LOAN_PRODUCT_ACCOUNTING_PARAMS.FEES_RECEIVABLE.getValue(), loanProductId,
                        ACCRUAL_ACCOUNTS_FOR_LOAN.FEES_RECEIVABLE.getValue());
                loanProductToGLAccountMappingHelper.saveLoanToAssetAccountMapping(element,
                        LOAN_PRODUCT_ACCOUNTING_PARAMS.PENALTIES_RECEIVABLE.getValue(), loanProductId,
                        ACCRUAL_ACCOUNTS_FOR_LOAN.PENALTIES_RECEIVABLE.getValue());

                // income
                loanProductToGLAccountMappingHelper.saveLoanToIncomeAccountMapping(element,
                        LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_LOANS.getValue(), loanProductId,
                        ACCRUAL_ACCOUNTS_FOR_LOAN.INTEREST_ON_LOANS.getValue());
                loanProductToGLAccountMappingHelper.saveLoanToIncomeAccountMapping(element,
                        LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue(), loanProductId,
                        ACCRUAL_ACCOUNTS_FOR_LOAN.INCOME_FROM_FEES.getValue());
                loanProductToGLAccountMappingHelper.saveLoanToIncomeAccountMapping(element,
                        LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_PENALTIES.getValue(), loanProductId,
                        ACCRUAL_ACCOUNTS_FOR_LOAN.INCOME_FROM_PENALTIES.getValue());

                // expenses
                loanProductToGLAccountMappingHelper.saveLoanToExpenseAccountMapping(element,
                        LOAN_PRODUCT_ACCOUNTING_PARAMS.LOSSES_WRITTEN_OFF.getValue(), loanProductId,
                        ACCRUAL_ACCOUNTS_FOR_LOAN.LOSSES_WRITTEN_OFF.getValue());

                // advanced accounting mappings
                loanProductToGLAccountMappingHelper.savePaymentChannelToFundSourceMappings(command, element, loanProductId, null);
                loanProductToGLAccountMappingHelper.saveChargesToIncomeAccountMappings(command, element, loanProductId, null);
            break;
        }
    }

    @Override
    @Transactional
    public void createSavingProductToGLAccountMapping(Long savingProductId, JsonCommand command) {
        final JsonElement element = this.fromApiJsonHelper.parse(command.json());
        final Integer accountingRuleTypeId = this.fromApiJsonHelper.extractIntegerNamed(accountingRuleParamName, element,
                Locale.getDefault());
        final AccountingRuleType accountingRuleType = AccountingRuleType.fromInt(accountingRuleTypeId);

        switch (accountingRuleType) {
            case NONE:
            break;
            case CASH_BASED:
                // asset
                savingsProductToGLAccountMappingHelper.saveSavingsToAssetAccountMapping(element,
                        SAVINGS_PRODUCT_ACCOUNTING_PARAMS.SAVINGS_REFERENCE.getValue(), savingProductId,
                        CASH_ACCOUNTS_FOR_SAVINGS.SAVINGS_REFERENCE.getValue());

                // income
                savingsProductToGLAccountMappingHelper.saveSavingsToIncomeAccountMapping(element,
                        SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue(), savingProductId,
                        CASH_ACCOUNTS_FOR_SAVINGS.INCOME_FROM_FEES.getValue());

                // expenses
                savingsProductToGLAccountMappingHelper.saveSavingsToExpenseAccountMapping(element,
                        SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_SAVINGS.getValue(), savingProductId,
                        CASH_ACCOUNTS_FOR_SAVINGS.INTEREST_ON_SAVINGS.getValue());

                // liability
                savingsProductToGLAccountMappingHelper.saveSavingsToLiabilityAccountMapping(element,
                        SAVINGS_PRODUCT_ACCOUNTING_PARAMS.SAVINGS_CONTROL.getValue(), savingProductId,
                        CASH_ACCOUNTS_FOR_SAVINGS.SAVINGS_CONTROL.getValue());

                // advanced accounting mappings
                savingsProductToGLAccountMappingHelper.savePaymentChannelToFundSourceMappings(command, element, savingProductId, null);
            break;
            /*** Do not have accrual based accounting for now ***/
            case ACCRUAL_BASED:
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
        Map<String, Object> changes = new HashMap<String, Object>();
        final JsonElement element = this.fromApiJsonHelper.parse(command.json());
        final AccountingRuleType accountingRuleType = AccountingRuleType.fromInt(accountingRuleTypeId);

        /***
         * If the accounting rule has been changed, delete all existing mapping
         * for the product and recreate a new set of mappings
         ***/
        if (accountingRuleChanged) {
            this.deserializer.validateForLoanProductCreate(command.json());
            loanProductToGLAccountMappingHelper.deleteLoanProductToGLAccountMapping(loanProductId);
            createLoanProductToGLAccountMapping(loanProductId, command);
            changes = loanProductToGLAccountMappingHelper.populateChangesForNewLoanProductToGLAccountMappingCreation(element,
                    accountingRuleType);
        }/*** else examine and update individual changes ***/
        else {
            loanProductToGLAccountMappingHelper.handleChangesToLoanProductToGLAccountMappings(loanProductId, changes, element,
                    accountingRuleType);
            loanProductToGLAccountMappingHelper.updatePaymentChannelToFundSourceMappings(command, element, loanProductId, changes);
            loanProductToGLAccountMappingHelper.updateChargesToIncomeAccountMappings(command, element, loanProductId, changes);
        }
        return changes;
    }

    @Override
    public Map<String, Object> updateSavingsProductToGLAccountMapping(Long savingsProductId, JsonCommand command,
            boolean accountingRuleChanged, int accountingRuleTypeId) {
        /***
         * Variable tracks all accounting mapping properties that have been
         * updated
         ***/
        Map<String, Object> changes = new HashMap<String, Object>();
        final JsonElement element = this.fromApiJsonHelper.parse(command.json());
        final AccountingRuleType accountingRuleType = AccountingRuleType.fromInt(accountingRuleTypeId);

        /***
         * If the accounting rule has been changed, delete all existing mapping
         * for the product and recreate a new set of mappings
         ***/
        if (accountingRuleChanged) {
            this.deserializer.validateForSavingsProductCreate(command.json());
            savingsProductToGLAccountMappingHelper.deleteSavingsProductToGLAccountMapping(savingsProductId);
            createSavingProductToGLAccountMapping(savingsProductId, command);
            changes = savingsProductToGLAccountMappingHelper.populateChangesForNewSavingsProductToGLAccountMappingCreation(element,
                    accountingRuleType);
        }/*** else examine and update individual changes ***/
        else {
            savingsProductToGLAccountMappingHelper.handleChangesToSavingsProductToGLAccountMappings(savingsProductId, changes, element,
                    accountingRuleType);
            savingsProductToGLAccountMappingHelper.updatePaymentChannelToFundSourceMappings(command, element, savingsProductId, changes);
        }
        return changes;
    }
}
