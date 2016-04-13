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
import org.apache.fineract.accounting.common.AccountingConstants.CASH_ACCOUNTS_FOR_SHARES;
import org.apache.fineract.accounting.common.AccountingConstants.SHARES_PRODUCT_ACCOUNTING_PARAMS;
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
public class ShareProductToGLAccountMappingHelper extends ProductToGLAccountMappingHelper {

    @Autowired
    public ShareProductToGLAccountMappingHelper(final GLAccountRepository glAccountRepository,
            final ProductToGLAccountMappingRepository glAccountMappingRepository, final FromJsonHelper fromApiJsonHelper,
            final ChargeRepositoryWrapper chargeRepositoryWrapper, final GLAccountRepositoryWrapper accountRepositoryWrapper,
            final PaymentTypeRepositoryWrapper paymentTypeRepositoryWrapper) {
        super(glAccountRepository, glAccountMappingRepository, fromApiJsonHelper, chargeRepositoryWrapper, accountRepositoryWrapper,
                paymentTypeRepositoryWrapper);
    }

    /*** Set of abstractions for saving Share Products to GL Account Mappings ***/

    public void saveSharesToAssetAccountMapping(final JsonElement element, final String paramName, final Long productId,
            final int placeHolderTypeId) {
        saveProductToAccountMapping(element, paramName, productId, placeHolderTypeId, GLAccountType.ASSET, PortfolioProductType.SHARES);
    }

    public void saveSharesToIncomeAccountMapping(final JsonElement element, final String paramName, final Long productId,
            final int placeHolderTypeId) {
        saveProductToAccountMapping(element, paramName, productId, placeHolderTypeId, GLAccountType.INCOME, PortfolioProductType.SHARES);
    }

    public void saveSharesToEquityAccountMapping(final JsonElement element, final String paramName, final Long productId,
            final int placeHolderTypeId) {
        saveProductToAccountMapping(element, paramName, productId, placeHolderTypeId, GLAccountType.EQUITY, PortfolioProductType.SHARES);
    }

    public void saveSharesToLiabilityAccountMapping(final JsonElement element, final String paramName, final Long productId,
            final int placeHolderTypeId) {
        saveProductToAccountMapping(element, paramName, productId, placeHolderTypeId, GLAccountType.LIABILITY, PortfolioProductType.SHARES);
    }

    /*** Set of abstractions for merging Shares Products to GL Account Mappings ***/

    public void mergeSharesToAssetAccountMappingChanges(final JsonElement element, final String paramName, final Long productId,
            final int accountTypeId, final String accountTypeName, final Map<String, Object> changes) {
        mergeProductToAccountMappingChanges(element, paramName, productId, accountTypeId, accountTypeName, changes, GLAccountType.ASSET,
                PortfolioProductType.SHARES);
    }

    public void mergeSharesToIncomeAccountMappingChanges(final JsonElement element, final String paramName, final Long productId,
            final int accountTypeId, final String accountTypeName, final Map<String, Object> changes) {
        mergeProductToAccountMappingChanges(element, paramName, productId, accountTypeId, accountTypeName, changes, GLAccountType.INCOME,
                PortfolioProductType.SHARES);
    }

    public void mergeSharesToEquityAccountMappingChanges(final JsonElement element, final String paramName, final Long productId,
            final int accountTypeId, final String accountTypeName, final Map<String, Object> changes) {
        mergeProductToAccountMappingChanges(element, paramName, productId, accountTypeId, accountTypeName, changes, GLAccountType.EQUITY,
                PortfolioProductType.SHARES);
    }

    public void mergeSharesToLiabilityAccountMappingChanges(final JsonElement element, final String paramName, final Long productId,
            final int accountTypeId, final String accountTypeName, final Map<String, Object> changes) {
        mergeProductToAccountMappingChanges(element, paramName, productId, accountTypeId, accountTypeName, changes,
                GLAccountType.LIABILITY, PortfolioProductType.SHARES);
    }

    /*** Abstractions for payments channel related to Shares products ***/

    public void savePaymentChannelToFundSourceMappings(final JsonCommand command, final JsonElement element, final Long productId,
            final Map<String, Object> changes) {
        savePaymentChannelToFundSourceMappings(command, element, productId, changes, PortfolioProductType.SHARES);
    }

    public void updatePaymentChannelToFundSourceMappings(final JsonCommand command, final JsonElement element, final Long productId,
            final Map<String, Object> changes) {
        updatePaymentChannelToFundSourceMappings(command, element, productId, changes, PortfolioProductType.SHARES);
    }

    public void saveChargesToIncomeAccountMappings(final JsonCommand command, final JsonElement element, final Long productId,
            final Map<String, Object> changes) {
        saveChargesToIncomeOrLiabilityAccountMappings(command, element, productId, changes, PortfolioProductType.SHARES, true);
        saveChargesToIncomeOrLiabilityAccountMappings(command, element, productId, changes, PortfolioProductType.SHARES, false);
    }

    public void updateChargesToIncomeAccountMappings(final JsonCommand command, final JsonElement element, final Long productId,
            final Map<String, Object> changes) {
        updateChargeToIncomeAccountMappings(command, element, productId, changes, PortfolioProductType.SHARES, true);
        updateChargeToIncomeAccountMappings(command, element, productId, changes, PortfolioProductType.SHARES, false);
    }

    public Map<String, Object> populateChangesForNewSharesProductToGLAccountMappingCreation(final JsonElement element,
            final AccountingRuleType accountingRuleType) {
        final Map<String, Object> changes = new HashMap<>();

        final Long shareReferenceId = this.fromApiJsonHelper.extractLongNamed(SHARES_PRODUCT_ACCOUNTING_PARAMS.SHARES_REFERENCE.getValue(),
                element);
        final Long incomeFromFeeAccountId = this.fromApiJsonHelper.extractLongNamed(
                SHARES_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue(), element);
        final Long shareSuspenseId = this.fromApiJsonHelper.extractLongNamed(SHARES_PRODUCT_ACCOUNTING_PARAMS.SHARES_SUSPENSE.getValue(),
                element);
        final Long shareEquityId = this.fromApiJsonHelper.extractLongNamed(SHARES_PRODUCT_ACCOUNTING_PARAMS.SHARES_EQUITY.getValue(),
                element);

        switch (accountingRuleType) {
            case NONE:
            break;
            case CASH_BASED:
                changes.put(SHARES_PRODUCT_ACCOUNTING_PARAMS.SHARES_REFERENCE.getValue(), shareReferenceId);
                changes.put(SHARES_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue(), incomeFromFeeAccountId);
                changes.put(SHARES_PRODUCT_ACCOUNTING_PARAMS.SHARES_SUSPENSE.getValue(), shareSuspenseId);
                changes.put(SHARES_PRODUCT_ACCOUNTING_PARAMS.SHARES_EQUITY.getValue(), shareEquityId);
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
     * @param sharesProductId
     * @param changes
     * @param element
     * @param accountingRuleType
     */
    public void handleChangesToSharesProductToGLAccountMappings(final Long sharesProductId, final Map<String, Object> changes,
            final JsonElement element, final AccountingRuleType accountingRuleType) {
        switch (accountingRuleType) {
            case NONE:
            break;
            case CASH_BASED:
                // asset
                mergeSharesToAssetAccountMappingChanges(element, SHARES_PRODUCT_ACCOUNTING_PARAMS.SHARES_REFERENCE.getValue(),
                        sharesProductId, CASH_ACCOUNTS_FOR_SHARES.SHARES_REFERENCE.getValue(),
                        CASH_ACCOUNTS_FOR_SHARES.SHARES_REFERENCE.toString(), changes);

                // income
                mergeSharesToIncomeAccountMappingChanges(element, SHARES_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue(),
                        sharesProductId, CASH_ACCOUNTS_FOR_SHARES.INCOME_FROM_FEES.getValue(),
                        CASH_ACCOUNTS_FOR_SHARES.INCOME_FROM_FEES.toString(), changes);

                // liability
                mergeSharesToLiabilityAccountMappingChanges(element, SHARES_PRODUCT_ACCOUNTING_PARAMS.SHARES_SUSPENSE.getValue(),
                        sharesProductId, CASH_ACCOUNTS_FOR_SHARES.SHARES_SUSPENSE.getValue(),
                        CASH_ACCOUNTS_FOR_SHARES.SHARES_SUSPENSE.toString(), changes);

                // equity
                mergeSharesToEquityAccountMappingChanges(element, SHARES_PRODUCT_ACCOUNTING_PARAMS.SHARES_EQUITY.getValue(),
                        sharesProductId, CASH_ACCOUNTS_FOR_SHARES.SHARES_EQUITY.getValue(),
                        CASH_ACCOUNTS_FOR_SHARES.SHARES_EQUITY.toString(), changes);
            break;
            case ACCRUAL_PERIODIC:
            break;
            case ACCRUAL_UPFRONT:
            break;
            default:
            break;
        }
    }

    public void deleteSharesProductToGLAccountMapping(final Long sharesProductId) {
        deleteProductToGLAccountMapping(sharesProductId, PortfolioProductType.SHARES);
    }
}
