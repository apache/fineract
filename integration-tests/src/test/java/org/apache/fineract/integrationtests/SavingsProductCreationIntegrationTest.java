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
package org.apache.fineract.integrationtests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import org.apache.fineract.client.models.GetSavingsProductsAccountingMappings;
import org.apache.fineract.client.models.GetSavingsProductsProductIdResponse;
import org.apache.fineract.client.models.PostSavingsProductsRequest;
import org.apache.fineract.client.models.PutSavingsProductsProductIdRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignSavingsTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.junit.jupiter.api.Test;

public class SavingsProductCreationIntegrationTest extends FeignSavingsTestBase {

    private static final BigDecimal MINIMUM_OPENING_BALANCE = new BigDecimal("1000.0");
    private static final BigDecimal OVERDRAFT_LIMIT = new BigDecimal("100000");
    private static final String INTEREST_RECEIVABLE_ACCOUNT = "interestReceivableAccount";

    @Test
    public void testStandardSavingsProductCreation_DoesNotAllowOverdraft() {
        final Account assetAccount = accountHelper.createAssetAccount("assetAccount");
        final Account incomeAccount = accountHelper.createIncomeAccount("incomeAccount");
        final Account expenseAccount = accountHelper.createExpenseAccount("expenseAccount");
        final Account liabilityAccount = accountHelper.createLiabilityAccount("liabilityAccount");

        final Long productId = savingsProductHelper.createSavingsProduct(SavingsRequestBuilders
                .withAccrualAccountingMappings(accrualProduct(), assetAccount, liabilityAccount, incomeAccount, expenseAccount))
                .getResourceId();
        assertNotNull(productId);

        assertNull(getAccountingMappings(productId).getInterestReceivableAccount(),
                "A product without an overdraft should not carry an interest receivable account");
    }

    @Test
    public void testSavingsProductWithOverdraftCreation_AllowsOverdraft() {
        final Account assetAccount = accountHelper.createAssetAccount("assetAccount");
        final Account interestReceivableAccount = accountHelper.createAssetAccount(INTEREST_RECEIVABLE_ACCOUNT);
        final Account incomeAccount = accountHelper.createIncomeAccount("incomeAccount");
        final Account expenseAccount = accountHelper.createExpenseAccount("expenseAccount");
        final Account liabilityAccount = accountHelper.createLiabilityAccount("liabilityAccount");

        final Long productId = savingsProductHelper
                .createSavingsProduct(SavingsRequestBuilders.withAccrualAccountingMappings(overdraftAccrualProduct(), assetAccount,
                        liabilityAccount, incomeAccount, expenseAccount, interestReceivableAccount))
                .getResourceId();
        assertNotNull(productId);

        verifyInterestReceivableAccount(productId, interestReceivableAccount);
    }

    @Test
    public void testSavingsProductWithOverdraftUpdate_AllowsOverdraft() {
        final Account assetAccount = accountHelper.createAssetAccount("assetAccount");
        final Account interestReceivableAccount = accountHelper.createAssetAccount(INTEREST_RECEIVABLE_ACCOUNT);
        final Account incomeAccount = accountHelper.createIncomeAccount("incomeAccount");
        final Account expenseAccount = accountHelper.createExpenseAccount("expenseAccount");
        final Account liabilityAccount = accountHelper.createLiabilityAccount("liabilityAccount");

        final PostSavingsProductsRequest createdProduct = SavingsRequestBuilders.withAccrualAccountingMappings(overdraftAccrualProduct(),
                assetAccount, liabilityAccount, incomeAccount, expenseAccount, interestReceivableAccount);
        final Long productId = savingsProductHelper.createSavingsProduct(createdProduct).getResourceId();
        assertNotNull(productId);
        verifyInterestReceivableAccount(productId, interestReceivableAccount);

        final Account newInterestReceivableAccount = accountHelper.createAssetAccount(INTEREST_RECEIVABLE_ACCOUNT);
        final Long updatedProductId = savingsProductHelper
                .updateSavingsProduct(productId, SavingsRequestBuilders.withAccrualAccountingMappings(updateOf(createdProduct),
                        assetAccount, liabilityAccount, incomeAccount, expenseAccount, newInterestReceivableAccount))
                .getResourceId();
        assertEquals(productId, updatedProductId, "The update should have been applied to the same product");

        verifyInterestReceivableAccount(updatedProductId, newInterestReceivableAccount);
        assertNotEquals(interestReceivableAccount.getAccountID(),
                getAccountingMappings(updatedProductId).getInterestReceivableAccount().getId().intValue(),
                "The interest receivable account should no longer be the one the product was created with");
    }

    @Test
    public void testRetrieveSavingsProductsWithOfficeSpecificRestrictionEnabledAndNoEntityAccessMapping() {
        try {
            globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.OFFICE_SPECIFIC_PRODUCTS_ENABLED, true);
            globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.RESTRICT_PRODUCTS_TO_USER_OFFICE, true);

            assertNotNull(savingsProductHelper.getAllSavingsProducts());
        } finally {
            globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.RESTRICT_PRODUCTS_TO_USER_OFFICE, false);
            globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.OFFICE_SPECIFIC_PRODUCTS_ENABLED, false);
        }
    }

    private void verifyInterestReceivableAccount(final Long productId, final Account expectedAccount) {
        final GetSavingsProductsAccountingMappings mappings = getAccountingMappings(productId);
        assertNotNull(mappings.getInterestReceivableAccount(), "Interest receivable account is missing");
        assertEquals(expectedAccount.getAccountID(), mappings.getInterestReceivableAccount().getId().intValue());
    }

    private GetSavingsProductsAccountingMappings getAccountingMappings(final Long productId) {
        final GetSavingsProductsProductIdResponse product = getSavingsProduct(productId);
        assertNotNull(product);
        assertNotNull(product.getAccountingMappings(), "Accounting mappings are missing");
        return product.getAccountingMappings();
    }

    private PostSavingsProductsRequest overdraftAccrualProduct() {
        return accrualProduct().allowOverdraft(true).overdraftLimit(OVERDRAFT_LIMIT);
    }

    private PostSavingsProductsRequest accrualProduct() {
        return SavingsRequestBuilders
                .savingsProduct(SavingsTestData.InterestCompoundingPeriodType.DAILY, SavingsTestData.InterestPostingPeriodType.QUARTERLY,
                        SavingsTestData.InterestCalculationType.DAILY_BALANCE)
                .minRequiredOpeningBalance(MINIMUM_OPENING_BALANCE)//
                .accountingRule(SavingsTestData.AccountingRule.ACCRUAL_PERIODIC);
    }

    /** Carries the created product's own values, so the update changes only the accounting mappings under test. */
    private PutSavingsProductsProductIdRequest updateOf(final PostSavingsProductsRequest product) {
        return new PutSavingsProductsProductIdRequest()//
                .name(product.getName())//
                .shortName(product.getShortName())//
                .description(product.getDescription())//
                .currencyCode(product.getCurrencyCode())//
                .digitsAfterDecimal(product.getDigitsAfterDecimal())//
                .inMultiplesOf(product.getInMultiplesOf())//
                .nominalAnnualInterestRate(product.getNominalAnnualInterestRate())//
                .minRequiredOpeningBalance(product.getMinRequiredOpeningBalance())//
                .interestCompoundingPeriodType(product.getInterestCompoundingPeriodType())//
                .interestPostingPeriodType(product.getInterestPostingPeriodType())//
                .interestCalculationType(product.getInterestCalculationType())//
                .interestCalculationDaysInYearType(product.getInterestCalculationDaysInYearType())//
                .accountingRule(product.getAccountingRule())//
                .allowOverdraft(product.getAllowOverdraft())//
                .overdraftLimit(product.getOverdraftLimit())//
                .locale(SavingsTestData.LOCALE);
    }
}
