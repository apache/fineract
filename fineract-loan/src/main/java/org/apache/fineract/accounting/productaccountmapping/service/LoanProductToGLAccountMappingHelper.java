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
package org.apache.fineract.accounting.productaccountmapping.service;

import com.google.gson.JsonElement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.accounting.common.AccountingConstants.AccrualAccountsForLoan;
import org.apache.fineract.accounting.common.AccountingConstants.CashAccountsForLoan;
import org.apache.fineract.accounting.common.AccountingConstants.LoanProductAccountingParams;
import org.apache.fineract.accounting.common.AccountingRuleType;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.glaccount.domain.GLAccountRepository;
import org.apache.fineract.accounting.glaccount.domain.GLAccountRepositoryWrapper;
import org.apache.fineract.accounting.glaccount.domain.GLAccountType;
import org.apache.fineract.accounting.producttoaccountmapping.domain.ProductToGLAccountMappingRepository;
import org.apache.fineract.accounting.producttoaccountmapping.exception.ProductToGLAccountMappingInvalidException;
import org.apache.fineract.accounting.producttoaccountmapping.service.ProductToGLAccountMappingHelper;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.PortfolioProductType;
import org.apache.fineract.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentTypeRepositoryWrapper;
import org.springframework.stereotype.Component;

@Component
public class LoanProductToGLAccountMappingHelper extends ProductToGLAccountMappingHelper {

    public LoanProductToGLAccountMappingHelper(final GLAccountRepository glAccountRepository,
            final ProductToGLAccountMappingRepository glAccountMappingRepository, final FromJsonHelper fromApiJsonHelper,
            final ChargeRepositoryWrapper chargeRepositoryWrapper, final GLAccountRepositoryWrapper accountRepositoryWrapper,
            final PaymentTypeRepositoryWrapper paymentTypeRepositoryWrapper) {
        super(glAccountRepository, glAccountMappingRepository, fromApiJsonHelper, chargeRepositoryWrapper, accountRepositoryWrapper,
                paymentTypeRepositoryWrapper);
    }

    /***
     * Set of abstractions for saving Loan Products to GL Account Mappings
     ***/

    public void saveLoanToAssetAccountMapping(final JsonElement element, final String paramName, final Long productId,
            final int placeHolderTypeId) {
        saveProductToAccountMapping(element, paramName, productId, placeHolderTypeId, GLAccountType.ASSET, PortfolioProductType.LOAN);
    }

    public void saveLoanToAssetOrLiabilityAccountMapping(final JsonElement element, final String paramName, final Long productId,
            final int placeHolderTypeId) {
        GLAccountType glAccountType = getGLAccountType(element, paramName, ASSET_LIABILITY_TYPES);
        if (glAccountType != null) {
            saveProductToAccountMapping(element, paramName, productId, placeHolderTypeId, glAccountType, PortfolioProductType.LOAN);
        }
    }

    public void saveLoanToIncomeAccountMapping(final JsonElement element, final String paramName, final Long productId,
            final int placeHolderTypeId) {
        saveProductToAccountMapping(element, paramName, productId, placeHolderTypeId, GLAccountType.INCOME, PortfolioProductType.LOAN);
    }

    public void saveLoanToExpenseAccountMapping(final JsonElement element, final String paramName, final Long productId,
            final int placeHolderTypeId) {
        saveProductToAccountMapping(element, paramName, productId, placeHolderTypeId, GLAccountType.EXPENSE, PortfolioProductType.LOAN);
    }

    public void saveLoanToLiabilityAccountMapping(final JsonElement element, final String paramName, final Long productId,
            final int placeHolderTypeId) {
        saveProductToAccountMapping(element, paramName, productId, placeHolderTypeId, GLAccountType.LIABILITY, PortfolioProductType.LOAN);
    }

    /***
     * Set of abstractions for merging Savings Products to GL Account Mappings
     ***/
    public void mergeLoanToAssetAccountMappingChanges(final JsonElement element, final String paramName, final Long productId,
            final int accountTypeId, final String accountTypeName, final Map<String, Object> changes) {
        mergeProductToAccountMappingChanges(element, paramName, productId, accountTypeId, accountTypeName, changes, GLAccountType.ASSET,
                PortfolioProductType.LOAN);
    }

    public void mergeLoanToAssetOrLiabilityAccountMappingChanges(final JsonElement element, final String paramName, final Long productId,
            final int accountTypeId, final String accountTypeName, final Map<String, Object> changes) {
        GLAccountType glAccountType = getGLAccountType(element, paramName, ASSET_LIABILITY_TYPES);
        if (glAccountType != null) {
            mergeProductToAccountMappingChanges(element, paramName, productId, accountTypeId, accountTypeName, changes, glAccountType,
                    PortfolioProductType.LOAN);
        }
    }

    public void mergeLoanToIncomeAccountMappingChanges(final JsonElement element, final String paramName, final Long productId,
            final int accountTypeId, final String accountTypeName, final Map<String, Object> changes) {
        mergeProductToAccountMappingChanges(element, paramName, productId, accountTypeId, accountTypeName, changes, GLAccountType.INCOME,
                PortfolioProductType.LOAN);
    }

    public void mergeLoanToExpenseAccountMappingChanges(final JsonElement element, final String paramName, final Long productId,
            final int accountTypeId, final String accountTypeName, final Map<String, Object> changes) {
        mergeProductToAccountMappingChanges(element, paramName, productId, accountTypeId, accountTypeName, changes, GLAccountType.EXPENSE,
                PortfolioProductType.LOAN);
    }

    public void mergeLoanToLiabilityAccountMappingChanges(final JsonElement element, final String paramName, final Long productId,
            final int accountTypeId, final String accountTypeName, final Map<String, Object> changes) {
        mergeProductToAccountMappingChanges(element, paramName, productId, accountTypeId, accountTypeName, changes, GLAccountType.LIABILITY,
                PortfolioProductType.LOAN);
    }

    /*** Abstractions for payments channel related to loan products ***/

    public void savePaymentChannelToFundSourceMappings(final JsonCommand command, final JsonElement element, final Long productId,
            final Map<String, Object> changes) {
        savePaymentChannelToFundSourceMappings(command, element, productId, changes, PortfolioProductType.LOAN);
    }

    public void updatePaymentChannelToFundSourceMappings(final JsonCommand command, final JsonElement element, final Long productId,
            final Map<String, Object> changes) {
        updatePaymentChannelToFundSourceMappings(command, element, productId, changes, PortfolioProductType.LOAN);
    }

    public void saveChargesToIncomeAccountMappings(final JsonCommand command, final JsonElement element, final Long productId,
            final Map<String, Object> changes) {
        // save both fee and penalty charges
        saveChargesToGLAccountMappings(command, element, productId, changes, PortfolioProductType.LOAN, true);
        saveChargesToGLAccountMappings(command, element, productId, changes, PortfolioProductType.LOAN, false);
    }

    public void updateChargesToIncomeAccountMappings(final JsonCommand command, final JsonElement element, final Long productId,
            final Map<String, Object> changes) {
        // update both fee and penalty charges
        updateChargeToIncomeAccountMappings(command, element, productId, changes, PortfolioProductType.LOAN, true);
        updateChargeToIncomeAccountMappings(command, element, productId, changes, PortfolioProductType.LOAN, false);
    }

    public Map<String, Object> populateChangesForNewLoanProductToGLAccountMappingCreation(final JsonElement element,
            final AccountingRuleType accountingRuleType) {
        final Map<String, Object> changes = new HashMap<>();

        final Long fundAccountId = this.fromApiJsonHelper.extractLongNamed(LoanProductAccountingParams.FUND_SOURCE.getValue(), element);
        final Long loanPortfolioAccountId = this.fromApiJsonHelper.extractLongNamed(LoanProductAccountingParams.LOAN_PORTFOLIO.getValue(),
                element);
        final Long incomeFromInterestId = this.fromApiJsonHelper.extractLongNamed(LoanProductAccountingParams.INTEREST_ON_LOANS.getValue(),
                element);
        final Long incomeFromFeeId = this.fromApiJsonHelper.extractLongNamed(LoanProductAccountingParams.INCOME_FROM_FEES.getValue(),
                element);
        final Long incomeFromPenaltyId = this.fromApiJsonHelper
                .extractLongNamed(LoanProductAccountingParams.INCOME_FROM_PENALTIES.getValue(), element);

        final Long incomeFromRecoveryAccountId = this.fromApiJsonHelper
                .extractLongNamed(LoanProductAccountingParams.INCOME_FROM_RECOVERY.getValue(), element);

        final Long writeOffAccountId = this.fromApiJsonHelper.extractLongNamed(LoanProductAccountingParams.LOSSES_WRITTEN_OFF.getValue(),
                element);
        final Long overPaymentAccountId = this.fromApiJsonHelper.extractLongNamed(LoanProductAccountingParams.OVERPAYMENT.getValue(),
                element);
        final Long transfersInSuspenseAccountId = this.fromApiJsonHelper
                .extractLongNamed(LoanProductAccountingParams.TRANSFERS_SUSPENSE.getValue(), element);

        final Long receivableInterestAccountId = this.fromApiJsonHelper
                .extractLongNamed(LoanProductAccountingParams.INTEREST_RECEIVABLE.getValue(), element);
        final Long receivableFeeAccountId = this.fromApiJsonHelper.extractLongNamed(LoanProductAccountingParams.FEES_RECEIVABLE.getValue(),
                element);
        final Long receivablePenaltyAccountId = this.fromApiJsonHelper
                .extractLongNamed(LoanProductAccountingParams.PENALTIES_RECEIVABLE.getValue(), element);

        switch (accountingRuleType) {
            case NONE:
            break;
            case CASH_BASED:
                populateChangesForCashBasedAccounting(changes, fundAccountId, loanPortfolioAccountId, incomeFromInterestId, incomeFromFeeId,
                        incomeFromPenaltyId, writeOffAccountId, overPaymentAccountId, transfersInSuspenseAccountId,
                        incomeFromRecoveryAccountId);
            break;
            case ACCRUAL_PERIODIC:
                populateChangesForAccrualBasedAccounting(changes, fundAccountId, loanPortfolioAccountId, incomeFromInterestId,
                        incomeFromFeeId, incomeFromPenaltyId, writeOffAccountId, overPaymentAccountId, transfersInSuspenseAccountId,
                        incomeFromRecoveryAccountId, receivableInterestAccountId, receivableFeeAccountId, receivablePenaltyAccountId);
            break;
            case ACCRUAL_UPFRONT:
                populateChangesForAccrualBasedAccounting(changes, fundAccountId, loanPortfolioAccountId, incomeFromInterestId,
                        incomeFromFeeId, incomeFromPenaltyId, writeOffAccountId, overPaymentAccountId, transfersInSuspenseAccountId,
                        incomeFromRecoveryAccountId, receivableInterestAccountId, receivableFeeAccountId, receivablePenaltyAccountId);
            break;
        }

        return changes;
    }

    private void populateChangesForAccrualBasedAccounting(final Map<String, Object> changes, final Long fundAccountId,
            final Long loanPortfolioAccountId, final Long incomeFromInterestId, final Long incomeFromFeeId, final Long incomeFromPenaltyId,
            final Long writeOffAccountId, final Long overPaymentAccountId, final Long transfersInSuspenseAccountId,
            final Long incomeFromRecoveryAccountId, final Long receivableInterestAccountId, final Long receivableFeeAccountId,
            final Long receivablePenaltyAccountId) {

        changes.put(LoanProductAccountingParams.INTEREST_RECEIVABLE.getValue(), receivableInterestAccountId);
        changes.put(LoanProductAccountingParams.FEES_RECEIVABLE.getValue(), receivableFeeAccountId);
        changes.put(LoanProductAccountingParams.PENALTIES_RECEIVABLE.getValue(), receivablePenaltyAccountId);

        populateChangesForCashBasedAccounting(changes, fundAccountId, loanPortfolioAccountId, incomeFromInterestId, incomeFromFeeId,
                incomeFromPenaltyId, writeOffAccountId, overPaymentAccountId, transfersInSuspenseAccountId, incomeFromRecoveryAccountId);

    }

    private void populateChangesForCashBasedAccounting(final Map<String, Object> changes, final Long fundAccountId,
            final Long loanPortfolioAccountId, final Long incomeFromInterestId, final Long incomeFromFeeId, final Long incomeFromPenaltyId,
            final Long writeOffAccountId, final Long overPaymentAccountId, final Long transfersInSuspenseAccountId,
            final Long incomeFromRecoveryAccountId) {
        changes.put(LoanProductAccountingParams.FUND_SOURCE.getValue(), fundAccountId);
        changes.put(LoanProductAccountingParams.LOAN_PORTFOLIO.getValue(), loanPortfolioAccountId);
        changes.put(LoanProductAccountingParams.INTEREST_ON_LOANS.getValue(), incomeFromInterestId);
        changes.put(LoanProductAccountingParams.INCOME_FROM_FEES.getValue(), incomeFromFeeId);
        changes.put(LoanProductAccountingParams.INCOME_FROM_PENALTIES.getValue(), incomeFromPenaltyId);
        changes.put(LoanProductAccountingParams.LOSSES_WRITTEN_OFF.getValue(), writeOffAccountId);
        changes.put(LoanProductAccountingParams.OVERPAYMENT.getValue(), overPaymentAccountId);
        changes.put(LoanProductAccountingParams.TRANSFERS_SUSPENSE.getValue(), transfersInSuspenseAccountId);
        changes.put(LoanProductAccountingParams.INCOME_FROM_RECOVERY.getValue(), incomeFromRecoveryAccountId);

    }

    /**
     * Examines and updates each account mapping for given loan product with changes passed in from the Json element
     *
     * @param loanProductId
     * @param changes
     * @param element
     * @param accountingRuleType
     */
    public void handleChangesToLoanProductToGLAccountMappings(final Long loanProductId, final Map<String, Object> changes,
            final JsonElement element, final AccountingRuleType accountingRuleType) {
        switch (accountingRuleType) {
            case NONE:
            break;
            case CASH_BASED:
                // asset or liabilities
                mergeLoanToAssetOrLiabilityAccountMappingChanges(element, LoanProductAccountingParams.FUND_SOURCE.getValue(), loanProductId,
                        CashAccountsForLoan.FUND_SOURCE.getValue(), CashAccountsForLoan.FUND_SOURCE.toString(), changes);

                // asset
                mergeLoanToAssetAccountMappingChanges(element, LoanProductAccountingParams.LOAN_PORTFOLIO.getValue(), loanProductId,
                        CashAccountsForLoan.LOAN_PORTFOLIO.getValue(), CashAccountsForLoan.LOAN_PORTFOLIO.toString(), changes);
                mergeLoanToAssetAccountMappingChanges(element, LoanProductAccountingParams.TRANSFERS_SUSPENSE.getValue(), loanProductId,
                        CashAccountsForLoan.TRANSFERS_SUSPENSE.getValue(), CashAccountsForLoan.TRANSFERS_SUSPENSE.toString(), changes);

                // income
                mergeLoanToIncomeAccountMappingChanges(element, LoanProductAccountingParams.INTEREST_ON_LOANS.getValue(), loanProductId,
                        CashAccountsForLoan.INTEREST_ON_LOANS.getValue(), CashAccountsForLoan.INTEREST_ON_LOANS.toString(), changes);
                mergeLoanToIncomeAccountMappingChanges(element, LoanProductAccountingParams.INCOME_FROM_FEES.getValue(), loanProductId,
                        CashAccountsForLoan.INCOME_FROM_FEES.getValue(), CashAccountsForLoan.INCOME_FROM_FEES.toString(), changes);
                mergeLoanToIncomeAccountMappingChanges(element, LoanProductAccountingParams.INCOME_FROM_PENALTIES.getValue(), loanProductId,
                        CashAccountsForLoan.INCOME_FROM_PENALTIES.getValue(), CashAccountsForLoan.INCOME_FROM_PENALTIES.toString(),
                        changes);
                mergeLoanToIncomeAccountMappingChanges(element, LoanProductAccountingParams.INCOME_FROM_RECOVERY.getValue(), loanProductId,
                        CashAccountsForLoan.INCOME_FROM_RECOVERY.getValue(), CashAccountsForLoan.INCOME_FROM_RECOVERY.toString(), changes);
                mergeLoanToIncomeAccountMappingChanges(element, LoanProductAccountingParams.INCOME_FROM_CHARGE_OFF_FEES.getValue(),
                        loanProductId, CashAccountsForLoan.INCOME_FROM_CHARGE_OFF_FEES.getValue(),
                        CashAccountsForLoan.INCOME_FROM_CHARGE_OFF_FEES.toString(), changes);
                mergeLoanToIncomeAccountMappingChanges(element, LoanProductAccountingParams.INCOME_FROM_CHARGE_OFF_INTEREST.getValue(),
                        loanProductId, CashAccountsForLoan.INCOME_FROM_CHARGE_OFF_INTEREST.getValue(),
                        CashAccountsForLoan.INCOME_FROM_CHARGE_OFF_INTEREST.toString(), changes);
                mergeLoanToIncomeAccountMappingChanges(element, LoanProductAccountingParams.INCOME_FROM_CHARGE_OFF_PENALTY.getValue(),
                        loanProductId, CashAccountsForLoan.INCOME_FROM_CHARGE_OFF_PENALTY.getValue(),
                        CashAccountsForLoan.INCOME_FROM_CHARGE_OFF_PENALTY.toString(), changes);
                mergeLoanToIncomeAccountMappingChanges(element, LoanProductAccountingParams.INCOME_FROM_GOODWILL_CREDIT_INTEREST.getValue(),
                        loanProductId, CashAccountsForLoan.INCOME_FROM_GOODWILL_CREDIT_INTEREST.getValue(),
                        CashAccountsForLoan.INCOME_FROM_GOODWILL_CREDIT_INTEREST.toString(), changes);
                mergeLoanToIncomeAccountMappingChanges(element, LoanProductAccountingParams.INCOME_FROM_GOODWILL_CREDIT_FEES.getValue(),
                        loanProductId, CashAccountsForLoan.INCOME_FROM_GOODWILL_CREDIT_FEES.getValue(),
                        CashAccountsForLoan.INCOME_FROM_GOODWILL_CREDIT_FEES.toString(), changes);
                mergeLoanToIncomeAccountMappingChanges(element, LoanProductAccountingParams.INCOME_FROM_GOODWILL_CREDIT_PENALTY.getValue(),
                        loanProductId, CashAccountsForLoan.INCOME_FROM_GOODWILL_CREDIT_PENALTY.getValue(),
                        CashAccountsForLoan.INCOME_FROM_GOODWILL_CREDIT_PENALTY.toString(), changes);

                // expenses
                mergeLoanToExpenseAccountMappingChanges(element, LoanProductAccountingParams.LOSSES_WRITTEN_OFF.getValue(), loanProductId,
                        CashAccountsForLoan.LOSSES_WRITTEN_OFF.getValue(), CashAccountsForLoan.LOSSES_WRITTEN_OFF.toString(), changes);
                mergeLoanToExpenseAccountMappingChanges(element, LoanProductAccountingParams.GOODWILL_CREDIT.getValue(), loanProductId,
                        CashAccountsForLoan.GOODWILL_CREDIT.getValue(), CashAccountsForLoan.GOODWILL_CREDIT.toString(), changes);
                mergeLoanToExpenseAccountMappingChanges(element, LoanProductAccountingParams.CHARGE_OFF_EXPENSE.getValue(), loanProductId,
                        CashAccountsForLoan.CHARGE_OFF_EXPENSE.getValue(), CashAccountsForLoan.CHARGE_OFF_EXPENSE.toString(), changes);
                mergeLoanToExpenseAccountMappingChanges(element, LoanProductAccountingParams.CHARGE_OFF_FRAUD_EXPENSE.getValue(),
                        loanProductId, CashAccountsForLoan.CHARGE_OFF_FRAUD_EXPENSE.getValue(),
                        CashAccountsForLoan.CHARGE_OFF_FRAUD_EXPENSE.toString(), changes);

                // liabilities
                mergeLoanToLiabilityAccountMappingChanges(element, LoanProductAccountingParams.OVERPAYMENT.getValue(), loanProductId,
                        CashAccountsForLoan.OVERPAYMENT.getValue(), CashAccountsForLoan.OVERPAYMENT.toString(), changes);
            break;
            case ACCRUAL_UPFRONT:
                // fall through to periodic accrual
            case ACCRUAL_PERIODIC:
                // asset or liabilities
                mergeLoanToAssetOrLiabilityAccountMappingChanges(element, LoanProductAccountingParams.FUND_SOURCE.getValue(), loanProductId,
                        CashAccountsForLoan.FUND_SOURCE.getValue(), CashAccountsForLoan.FUND_SOURCE.toString(), changes);

                // assets (including receivables)
                mergeLoanToAssetAccountMappingChanges(element, LoanProductAccountingParams.LOAN_PORTFOLIO.getValue(), loanProductId,
                        AccrualAccountsForLoan.LOAN_PORTFOLIO.getValue(), AccrualAccountsForLoan.LOAN_PORTFOLIO.toString(), changes);
                mergeLoanToAssetAccountMappingChanges(element, LoanProductAccountingParams.TRANSFERS_SUSPENSE.getValue(), loanProductId,
                        AccrualAccountsForLoan.TRANSFERS_SUSPENSE.getValue(), AccrualAccountsForLoan.TRANSFERS_SUSPENSE.toString(),
                        changes);
                mergeLoanToAssetAccountMappingChanges(element, LoanProductAccountingParams.INTEREST_RECEIVABLE.getValue(), loanProductId,
                        AccrualAccountsForLoan.INTEREST_RECEIVABLE.getValue(), AccrualAccountsForLoan.INTEREST_RECEIVABLE.toString(),
                        changes);
                mergeLoanToAssetAccountMappingChanges(element, LoanProductAccountingParams.FEES_RECEIVABLE.getValue(), loanProductId,
                        AccrualAccountsForLoan.FEES_RECEIVABLE.getValue(), AccrualAccountsForLoan.FEES_RECEIVABLE.toString(), changes);
                mergeLoanToAssetAccountMappingChanges(element, LoanProductAccountingParams.PENALTIES_RECEIVABLE.getValue(), loanProductId,
                        AccrualAccountsForLoan.PENALTIES_RECEIVABLE.getValue(), AccrualAccountsForLoan.PENALTIES_RECEIVABLE.toString(),
                        changes);

                // income
                mergeLoanToIncomeAccountMappingChanges(element, LoanProductAccountingParams.INTEREST_ON_LOANS.getValue(), loanProductId,
                        AccrualAccountsForLoan.INTEREST_ON_LOANS.getValue(), AccrualAccountsForLoan.INTEREST_ON_LOANS.toString(), changes);
                mergeLoanToIncomeAccountMappingChanges(element, LoanProductAccountingParams.INCOME_FROM_FEES.getValue(), loanProductId,
                        AccrualAccountsForLoan.INCOME_FROM_FEES.getValue(), AccrualAccountsForLoan.INCOME_FROM_FEES.toString(), changes);
                mergeLoanToIncomeAccountMappingChanges(element, LoanProductAccountingParams.INCOME_FROM_PENALTIES.getValue(), loanProductId,
                        AccrualAccountsForLoan.INCOME_FROM_PENALTIES.getValue(), AccrualAccountsForLoan.INCOME_FROM_PENALTIES.toString(),
                        changes);
                mergeLoanToIncomeAccountMappingChanges(element, LoanProductAccountingParams.INCOME_FROM_RECOVERY.getValue(), loanProductId,
                        AccrualAccountsForLoan.INCOME_FROM_RECOVERY.getValue(), AccrualAccountsForLoan.INCOME_FROM_RECOVERY.toString(),
                        changes);
                mergeLoanToIncomeAccountMappingChanges(element, LoanProductAccountingParams.INCOME_FROM_CHARGE_OFF_FEES.getValue(),
                        loanProductId, AccrualAccountsForLoan.INCOME_FROM_CHARGE_OFF_FEES.getValue(),
                        AccrualAccountsForLoan.INCOME_FROM_CHARGE_OFF_FEES.toString(), changes);
                mergeLoanToIncomeAccountMappingChanges(element, LoanProductAccountingParams.INCOME_FROM_CHARGE_OFF_INTEREST.getValue(),
                        loanProductId, AccrualAccountsForLoan.INCOME_FROM_CHARGE_OFF_INTEREST.getValue(),
                        AccrualAccountsForLoan.INCOME_FROM_CHARGE_OFF_INTEREST.toString(), changes);
                mergeLoanToIncomeAccountMappingChanges(element, LoanProductAccountingParams.INCOME_FROM_CHARGE_OFF_PENALTY.getValue(),
                        loanProductId, AccrualAccountsForLoan.INCOME_FROM_CHARGE_OFF_PENALTY.getValue(),
                        AccrualAccountsForLoan.INCOME_FROM_CHARGE_OFF_PENALTY.toString(), changes);
                mergeLoanToIncomeAccountMappingChanges(element, LoanProductAccountingParams.INCOME_FROM_GOODWILL_CREDIT_INTEREST.getValue(),
                        loanProductId, AccrualAccountsForLoan.INCOME_FROM_GOODWILL_CREDIT_INTEREST.getValue(),
                        AccrualAccountsForLoan.INCOME_FROM_GOODWILL_CREDIT_INTEREST.toString(), changes);
                mergeLoanToIncomeAccountMappingChanges(element, LoanProductAccountingParams.INCOME_FROM_GOODWILL_CREDIT_FEES.getValue(),
                        loanProductId, AccrualAccountsForLoan.INCOME_FROM_GOODWILL_CREDIT_FEES.getValue(),
                        AccrualAccountsForLoan.INCOME_FROM_GOODWILL_CREDIT_FEES.toString(), changes);
                mergeLoanToIncomeAccountMappingChanges(element, LoanProductAccountingParams.INCOME_FROM_GOODWILL_CREDIT_PENALTY.getValue(),
                        loanProductId, AccrualAccountsForLoan.INCOME_FROM_GOODWILL_CREDIT_PENALTY.getValue(),
                        AccrualAccountsForLoan.INCOME_FROM_GOODWILL_CREDIT_PENALTY.toString(), changes);

                // expenses
                mergeLoanToExpenseAccountMappingChanges(element, LoanProductAccountingParams.LOSSES_WRITTEN_OFF.getValue(), loanProductId,
                        AccrualAccountsForLoan.LOSSES_WRITTEN_OFF.getValue(), AccrualAccountsForLoan.LOSSES_WRITTEN_OFF.toString(),
                        changes);
                mergeLoanToExpenseAccountMappingChanges(element, LoanProductAccountingParams.GOODWILL_CREDIT.getValue(), loanProductId,
                        AccrualAccountsForLoan.GOODWILL_CREDIT.getValue(), AccrualAccountsForLoan.GOODWILL_CREDIT.toString(), changes);
                mergeLoanToExpenseAccountMappingChanges(element, LoanProductAccountingParams.CHARGE_OFF_EXPENSE.getValue(), loanProductId,
                        AccrualAccountsForLoan.CHARGE_OFF_EXPENSE.getValue(), AccrualAccountsForLoan.CHARGE_OFF_EXPENSE.toString(),
                        changes);
                mergeLoanToExpenseAccountMappingChanges(element, LoanProductAccountingParams.CHARGE_OFF_FRAUD_EXPENSE.getValue(),
                        loanProductId, AccrualAccountsForLoan.CHARGE_OFF_FRAUD_EXPENSE.getValue(),
                        AccrualAccountsForLoan.CHARGE_OFF_FRAUD_EXPENSE.toString(), changes);

                // liabilities
                mergeLoanToLiabilityAccountMappingChanges(element, LoanProductAccountingParams.OVERPAYMENT.getValue(), loanProductId,
                        CashAccountsForLoan.OVERPAYMENT.getValue(), CashAccountsForLoan.OVERPAYMENT.toString(), changes);
            break;
        }
    }

    public void deleteLoanProductToGLAccountMapping(final Long loanProductId) {
        deleteProductToGLAccountMapping(loanProductId, PortfolioProductType.LOAN);
    }

    private GLAccountType getGLAccountType(final JsonElement element, final String paramName, final List<GLAccountType> allowedTypes) {
        GLAccountType gLAccountType = null;
        final Long accountId = this.fromApiJsonHelper.extractLongNamed(paramName, element);
        if (accountId != null) {
            final GLAccount glAccount = getAccountById(paramName, accountId);
            gLAccountType = GLAccountType.fromInt(glAccount.getType());
            if (!allowedTypes.contains(gLAccountType)) {
                throw new ProductToGLAccountMappingInvalidException(paramName, glAccount.getName(), accountId, gLAccountType.toString(),
                        GLAccountType.ASSET.getCode() + " or " + GLAccountType.LIABILITY.getCode());
            }
        }
        return gLAccountType;
    }

}
