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

import com.google.gson.JsonElement;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.common.AccountingConstants.AccrualAccountsForLoan;
import org.apache.fineract.accounting.common.AccountingConstants.CashAccountsForLoan;
import org.apache.fineract.accounting.common.AccountingConstants.CashAccountsForSavings;
import org.apache.fineract.accounting.common.AccountingConstants.CashAccountsForShares;
import org.apache.fineract.accounting.common.AccountingConstants.LoanProductAccountingParams;
import org.apache.fineract.accounting.common.AccountingConstants.SavingProductAccountingParams;
import org.apache.fineract.accounting.common.AccountingConstants.SharesProductAccountingParams;
import org.apache.fineract.accounting.common.AccountingRuleType;
import org.apache.fineract.accounting.producttoaccountmapping.serialization.ProductToGLAccountMappingFromApiJsonDeserializer;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductToGLAccountMappingWritePlatformServiceImpl implements ProductToGLAccountMappingWritePlatformService {

    private final FromJsonHelper fromApiJsonHelper;
    private final ProductToGLAccountMappingFromApiJsonDeserializer deserializer;
    private final LoanProductToGLAccountMappingHelper loanProductToGLAccountMappingHelper;
    private final SavingsProductToGLAccountMappingHelper savingsProductToGLAccountMappingHelper;
    private final ShareProductToGLAccountMappingHelper shareProductToGLAccountMappingHelper;

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
                this.loanProductToGLAccountMappingHelper.saveLoanToAssetAccountMapping(element,
                        LoanProductAccountingParams.FUND_SOURCE.getValue(), loanProductId, CashAccountsForLoan.FUND_SOURCE.getValue());
                this.loanProductToGLAccountMappingHelper.saveLoanToAssetAccountMapping(element,
                        LoanProductAccountingParams.LOAN_PORTFOLIO.getValue(), loanProductId,
                        CashAccountsForLoan.LOAN_PORTFOLIO.getValue());
                this.loanProductToGLAccountMappingHelper.saveLoanToAssetAccountMapping(element,
                        LoanProductAccountingParams.TRANSFERS_SUSPENSE.getValue(), loanProductId,
                        CashAccountsForLoan.TRANSFERS_SUSPENSE.getValue());

                // income
                this.loanProductToGLAccountMappingHelper.saveLoanToIncomeAccountMapping(element,
                        LoanProductAccountingParams.INTEREST_ON_LOANS.getValue(), loanProductId,
                        CashAccountsForLoan.INTEREST_ON_LOANS.getValue());
                this.loanProductToGLAccountMappingHelper.saveLoanToIncomeAccountMapping(element,
                        LoanProductAccountingParams.INCOME_FROM_FEES.getValue(), loanProductId,
                        CashAccountsForLoan.INCOME_FROM_FEES.getValue());
                this.loanProductToGLAccountMappingHelper.saveLoanToIncomeAccountMapping(element,
                        LoanProductAccountingParams.INCOME_FROM_PENALTIES.getValue(), loanProductId,
                        CashAccountsForLoan.INCOME_FROM_PENALTIES.getValue());
                this.loanProductToGLAccountMappingHelper.saveLoanToIncomeAccountMapping(element,
                        LoanProductAccountingParams.INCOME_FROM_RECOVERY.getValue(), loanProductId,
                        CashAccountsForLoan.INCOME_FROM_RECOVERY.getValue());

                // expenses
                this.loanProductToGLAccountMappingHelper.saveLoanToExpenseAccountMapping(element,
                        LoanProductAccountingParams.LOSSES_WRITTEN_OFF.getValue(), loanProductId,
                        CashAccountsForLoan.LOSSES_WRITTEN_OFF.getValue());

                // liabilities
                this.loanProductToGLAccountMappingHelper.saveLoanToLiabilityAccountMapping(element,
                        LoanProductAccountingParams.OVERPAYMENT.getValue(), loanProductId, CashAccountsForLoan.OVERPAYMENT.getValue());

                // advanced accounting mappings
                this.loanProductToGLAccountMappingHelper.savePaymentChannelToFundSourceMappings(command, element, loanProductId, null);
                this.loanProductToGLAccountMappingHelper.saveChargesToIncomeAccountMappings(command, element, loanProductId, null);
            break;
            case ACCRUAL_UPFRONT:
                // Fall Through
            case ACCRUAL_PERIODIC:
                // assets (including receivables)
                this.loanProductToGLAccountMappingHelper.saveLoanToAssetAccountMapping(element,
                        LoanProductAccountingParams.FUND_SOURCE.getValue(), loanProductId, AccrualAccountsForLoan.FUND_SOURCE.getValue());
                this.loanProductToGLAccountMappingHelper.saveLoanToAssetAccountMapping(element,
                        LoanProductAccountingParams.LOAN_PORTFOLIO.getValue(), loanProductId,
                        AccrualAccountsForLoan.LOAN_PORTFOLIO.getValue());
                this.loanProductToGLAccountMappingHelper.saveLoanToAssetAccountMapping(element,
                        LoanProductAccountingParams.TRANSFERS_SUSPENSE.getValue(), loanProductId,
                        AccrualAccountsForLoan.TRANSFERS_SUSPENSE.getValue());
                this.loanProductToGLAccountMappingHelper.saveLoanToAssetAccountMapping(element,
                        LoanProductAccountingParams.INTEREST_RECEIVABLE.getValue(), loanProductId,
                        AccrualAccountsForLoan.INTEREST_RECEIVABLE.getValue());
                this.loanProductToGLAccountMappingHelper.saveLoanToAssetAccountMapping(element,
                        LoanProductAccountingParams.FEES_RECEIVABLE.getValue(), loanProductId,
                        AccrualAccountsForLoan.FEES_RECEIVABLE.getValue());
                this.loanProductToGLAccountMappingHelper.saveLoanToAssetAccountMapping(element,
                        LoanProductAccountingParams.PENALTIES_RECEIVABLE.getValue(), loanProductId,
                        AccrualAccountsForLoan.PENALTIES_RECEIVABLE.getValue());

                // income
                this.loanProductToGLAccountMappingHelper.saveLoanToIncomeAccountMapping(element,
                        LoanProductAccountingParams.INTEREST_ON_LOANS.getValue(), loanProductId,
                        AccrualAccountsForLoan.INTEREST_ON_LOANS.getValue());
                this.loanProductToGLAccountMappingHelper.saveLoanToIncomeAccountMapping(element,
                        LoanProductAccountingParams.INCOME_FROM_FEES.getValue(), loanProductId,
                        AccrualAccountsForLoan.INCOME_FROM_FEES.getValue());
                this.loanProductToGLAccountMappingHelper.saveLoanToIncomeAccountMapping(element,
                        LoanProductAccountingParams.INCOME_FROM_PENALTIES.getValue(), loanProductId,
                        AccrualAccountsForLoan.INCOME_FROM_PENALTIES.getValue());
                this.loanProductToGLAccountMappingHelper.saveLoanToIncomeAccountMapping(element,
                        LoanProductAccountingParams.INCOME_FROM_RECOVERY.getValue(), loanProductId,
                        AccrualAccountsForLoan.INCOME_FROM_RECOVERY.getValue());

                // expenses
                this.loanProductToGLAccountMappingHelper.saveLoanToExpenseAccountMapping(element,
                        LoanProductAccountingParams.LOSSES_WRITTEN_OFF.getValue(), loanProductId,
                        AccrualAccountsForLoan.LOSSES_WRITTEN_OFF.getValue());

                // liabilities
                this.loanProductToGLAccountMappingHelper.saveLoanToLiabilityAccountMapping(element,
                        LoanProductAccountingParams.OVERPAYMENT.getValue(), loanProductId, AccrualAccountsForLoan.OVERPAYMENT.getValue());

                // advanced accounting mappings
                this.loanProductToGLAccountMappingHelper.savePaymentChannelToFundSourceMappings(command, element, loanProductId, null);
                this.loanProductToGLAccountMappingHelper.saveChargesToIncomeAccountMappings(command, element, loanProductId, null);
            break;
        }
    }

    @Override
    @Transactional
    public void createSavingProductToGLAccountMapping(final Long savingProductId, final JsonCommand command,
            DepositAccountType accountType) {
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
                        SavingProductAccountingParams.SAVINGS_REFERENCE.getValue(), savingProductId,
                        CashAccountsForSavings.SAVINGS_REFERENCE.getValue());

                if (!accountType.equals(DepositAccountType.RECURRING_DEPOSIT) && !accountType.equals(DepositAccountType.FIXED_DEPOSIT)) {
                    this.savingsProductToGLAccountMappingHelper.saveSavingsToAssetAccountMapping(element,
                            SavingProductAccountingParams.OVERDRAFT_PORTFOLIO_CONTROL.getValue(), savingProductId,
                            CashAccountsForSavings.OVERDRAFT_PORTFOLIO_CONTROL.getValue());
                }

                // income
                this.savingsProductToGLAccountMappingHelper.saveSavingsToIncomeAccountMapping(element,
                        SavingProductAccountingParams.INCOME_FROM_FEES.getValue(), savingProductId,
                        CashAccountsForSavings.INCOME_FROM_FEES.getValue());

                this.savingsProductToGLAccountMappingHelper.saveSavingsToIncomeAccountMapping(element,
                        SavingProductAccountingParams.INCOME_FROM_PENALTIES.getValue(), savingProductId,
                        CashAccountsForSavings.INCOME_FROM_PENALTIES.getValue());

                if (!accountType.equals(DepositAccountType.RECURRING_DEPOSIT) && !accountType.equals(DepositAccountType.FIXED_DEPOSIT)) {
                    this.savingsProductToGLAccountMappingHelper.saveSavingsToIncomeAccountMapping(element,
                            SavingProductAccountingParams.INCOME_FROM_INTEREST.getValue(), savingProductId,
                            CashAccountsForSavings.INCOME_FROM_INTEREST.getValue());
                }

                // expenses
                this.savingsProductToGLAccountMappingHelper.saveSavingsToExpenseAccountMapping(element,
                        SavingProductAccountingParams.INTEREST_ON_SAVINGS.getValue(), savingProductId,
                        CashAccountsForSavings.INTEREST_ON_SAVINGS.getValue());

                if (!accountType.equals(DepositAccountType.RECURRING_DEPOSIT) && !accountType.equals(DepositAccountType.FIXED_DEPOSIT)) {
                    this.savingsProductToGLAccountMappingHelper.saveSavingsToExpenseAccountMapping(element,
                            SavingProductAccountingParams.LOSSES_WRITTEN_OFF.getValue(), savingProductId,
                            CashAccountsForSavings.LOSSES_WRITTEN_OFF.getValue());
                }

                // liability
                this.savingsProductToGLAccountMappingHelper.saveSavingsToLiabilityAccountMapping(element,
                        SavingProductAccountingParams.SAVINGS_CONTROL.getValue(), savingProductId,
                        CashAccountsForSavings.SAVINGS_CONTROL.getValue());
                this.savingsProductToGLAccountMappingHelper.saveSavingsToLiabilityAccountMapping(element,
                        SavingProductAccountingParams.TRANSFERS_SUSPENSE.getValue(), savingProductId,
                        CashAccountsForSavings.TRANSFERS_SUSPENSE.getValue());

                final Boolean isDormancyTrackingActive = this.fromApiJsonHelper.extractBooleanNamed(isDormancyTrackingActiveParamName,
                        element);
                if (null != isDormancyTrackingActive && isDormancyTrackingActive) {
                    this.savingsProductToGLAccountMappingHelper.saveSavingsToLiabilityAccountMapping(element,
                            SavingProductAccountingParams.ESCHEAT_LIABILITY.getValue(), savingProductId,
                            CashAccountsForSavings.ESCHEAT_LIABILITY.getValue());
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
                        SharesProductAccountingParams.SHARES_REFERENCE.getValue(), shareProductId,
                        CashAccountsForShares.SHARES_REFERENCE.getValue());

                // income
                this.shareProductToGLAccountMappingHelper.saveSharesToIncomeAccountMapping(element,
                        SharesProductAccountingParams.INCOME_FROM_FEES.getValue(), shareProductId,
                        CashAccountsForShares.INCOME_FROM_FEES.getValue());

                // expenses
                this.shareProductToGLAccountMappingHelper.saveSharesToEquityAccountMapping(element,
                        SharesProductAccountingParams.SHARES_EQUITY.getValue(), shareProductId,
                        CashAccountsForShares.SHARES_EQUITY.getValue());

                // liability
                this.shareProductToGLAccountMappingHelper.saveSharesToLiabilityAccountMapping(element,
                        SharesProductAccountingParams.SHARES_SUSPENSE.getValue(), shareProductId,
                        CashAccountsForShares.SHARES_SUSPENSE.getValue());

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
         * Variable tracks all accounting mapping properties that have been updated
         ***/
        Map<String, Object> changes = new HashMap<>();
        final JsonElement element = this.fromApiJsonHelper.parse(command.json());
        final AccountingRuleType accountingRuleType = AccountingRuleType.fromInt(accountingRuleTypeId);

        /***
         * If the accounting rule has been changed, delete all existing mapping for the product and recreate a new set
         * of mappings
         ***/
        if (accountingRuleChanged) {
            this.deserializer.validateForLoanProductCreate(command.json());
            this.loanProductToGLAccountMappingHelper.deleteLoanProductToGLAccountMapping(loanProductId);
            createLoanProductToGLAccountMapping(loanProductId, command);
            changes = this.loanProductToGLAccountMappingHelper.populateChangesForNewLoanProductToGLAccountMappingCreation(element,
                    accountingRuleType);
        } /*** else examine and update individual changes ***/
        else {
            this.loanProductToGLAccountMappingHelper.handleChangesToLoanProductToGLAccountMappings(loanProductId, changes, element,
                    accountingRuleType);
            this.loanProductToGLAccountMappingHelper.updatePaymentChannelToFundSourceMappings(command, element, loanProductId, changes);
            this.loanProductToGLAccountMappingHelper.updateChargesToIncomeAccountMappings(command, element, loanProductId, changes);
        }
        return changes;
    }

    @Override
    @Transactional
    public Map<String, Object> updateSavingsProductToGLAccountMapping(final Long savingsProductId, final JsonCommand command,
            final boolean accountingRuleChanged, final int accountingRuleTypeId, final DepositAccountType accountType) {
        /***
         * Variable tracks all accounting mapping properties that have been updated
         ***/
        Map<String, Object> changes = new HashMap<>();
        final JsonElement element = this.fromApiJsonHelper.parse(command.json());
        final AccountingRuleType accountingRuleType = AccountingRuleType.fromInt(accountingRuleTypeId);

        /***
         * If the accounting rule has been changed, delete all existing mapping for the product and recreate a new set
         * of mappings
         ***/
        if (accountingRuleChanged) {
            this.deserializer.validateForSavingsProductCreate(command.json(), accountType);
            this.savingsProductToGLAccountMappingHelper.deleteSavingsProductToGLAccountMapping(savingsProductId);
            createSavingProductToGLAccountMapping(savingsProductId, command, accountType);
            changes = this.savingsProductToGLAccountMappingHelper.populateChangesForNewSavingsProductToGLAccountMappingCreation(element,
                    accountingRuleType);
        } /*** else examine and update individual changes ***/
        else {
            this.savingsProductToGLAccountMappingHelper.handleChangesToSavingsProductToGLAccountMappings(savingsProductId, changes, element,
                    accountingRuleType);
            this.savingsProductToGLAccountMappingHelper.updatePaymentChannelToFundSourceMappings(command, element, savingsProductId,
                    changes);
            this.savingsProductToGLAccountMappingHelper.updateChargesToIncomeAccountMappings(command, element, savingsProductId, changes);
        }
        return changes;
    }

    @Override
    @Transactional
    public Map<String, Object> updateShareProductToGLAccountMapping(final Long shareProductId, final JsonCommand command,
            final boolean accountingRuleChanged, final int accountingRuleTypeId) {
        /***
         * Variable tracks all accounting mapping properties that have been updated
         ***/
        Map<String, Object> changes = new HashMap<>();
        final JsonElement element = this.fromApiJsonHelper.parse(command.json());
        final AccountingRuleType accountingRuleType = AccountingRuleType.fromInt(accountingRuleTypeId);

        /***
         * If the accounting rule has been changed, delete all existing mapping for the product and recreate a new set
         * of mappings
         ***/
        if (accountingRuleChanged) {
            this.shareProductToGLAccountMappingHelper.deleteSharesProductToGLAccountMapping(shareProductId);
            createShareProductToGLAccountMapping(shareProductId, command);
            changes = this.shareProductToGLAccountMappingHelper.populateChangesForNewSharesProductToGLAccountMappingCreation(element,
                    accountingRuleType);
        } /*** else examine and update individual changes ***/
        else {
            this.shareProductToGLAccountMappingHelper.handleChangesToSharesProductToGLAccountMappings(shareProductId, changes, element,
                    accountingRuleType);
            this.shareProductToGLAccountMappingHelper.updatePaymentChannelToFundSourceMappings(command, element, shareProductId, changes);
            this.shareProductToGLAccountMappingHelper.updateChargesToIncomeAccountMappings(command, element, shareProductId, changes);
        }
        return changes;
    }
}
