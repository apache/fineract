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

import java.math.BigDecimal;
import java.util.Set;
import org.apache.fineract.client.models.GetSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdResponse;
import org.apache.fineract.client.models.PostSavingsAccountsSavingsAccountIdChargesRequest;
import org.apache.fineract.client.models.PostSavingsProductsRequest;
import org.apache.fineract.client.models.PostTaxesComponentsRequest;
import org.apache.fineract.client.models.PostTaxesGroupRequest;
import org.apache.fineract.client.models.PostTaxesGroupTaxComponents;
import org.apache.fineract.client.models.SavingsAccountTransactionData;
import org.apache.fineract.integrationtests.client.feign.FeignSavingsTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignTaxComponentHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignTaxGroupHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SavingsChargeTaxIntegrationTest extends FeignSavingsTestBase {

    private static final String DATE = "01 January 2023";
    private static final String TAX_START_DATE = "01 January 2013";

    private final FeignTaxComponentHelper taxComponentHelper = new FeignTaxComponentHelper(
            FineractFeignClientHelper.getFineractFeignClient());
    private final FeignTaxGroupHelper taxGroupHelper = new FeignTaxGroupHelper(FineractFeignClientHelper.getFineractFeignClient());

    private Account assetAccount;
    private Account controlAccount;
    private Account incomeAccount;
    private Account expenseAccount;

    @BeforeEach
    void createAccounts() {
        assetAccount = accountHelper.createAssetAccount();
        controlAccount = accountHelper.createLiabilityAccount();
        incomeAccount = accountHelper.createIncomeAccount();
        expenseAccount = accountHelper.createExpenseAccount();
    }

    @Test
    void cashAccounting_chargePayment_postsTaxAndNetIncome_andUndoReversesThem() {
        runAt(DATE, () -> {
            final Account taxAccount = accountHelper.createLiabilityAccount("vatPayable");
            final Long savingsId = activeSavings(cashProduct(), "500");
            final Long savingsChargeId = addCharge(savingsId, taxedCharge(taxAccount.getAccountID().longValue()));

            savingsChargeHelper.payCharge(savingsId, savingsChargeId, "100", DATE);

            assertFullyPaid(savingsId, savingsChargeId);
            journalEntryHelper.checkJournalEntryForLiabilityAccount(controlAccount, DATE, debit(controlAccount, 100.0));
            journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, DATE, credit(incomeAccount, 90.0));
            journalEntryHelper.checkJournalEntryForLiabilityAccount(taxAccount, DATE, credit(taxAccount, 10.0));

            savingsTransactionHelper.undoTransaction(savingsId, payChargeTransactionId(savingsId));

            journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, DATE, credit(incomeAccount, 90.0),
                    debit(incomeAccount, 90.0));
            journalEntryHelper.checkJournalEntryForLiabilityAccount(taxAccount, DATE, credit(taxAccount, 10.0), debit(taxAccount, 10.0));
        });
    }

    @Test
    void cashAccounting_chargePaidIntoOverdraft_splitsTaxAcrossBothLegsOnce() {
        runAt(DATE, () -> {
            final Account taxAccount = accountHelper.createLiabilityAccount("vatPayable");
            final Long productId = createSavingsProduct(SavingsRequestBuilders.withCashBasedAccounting(
                    productRequest().allowOverdraft(true).overdraftLimit(BigDecimal.valueOf(1000)), assetAccount, controlAccount,
                    incomeAccount, expenseAccount)).getResourceId();
            final Long savingsId = activeSavings(productId, "40");
            final Long savingsChargeId = addCharge(savingsId, taxedCharge(taxAccount.getAccountID().longValue()));

            savingsChargeHelper.payCharge(savingsId, savingsChargeId, "100", DATE);

            journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, DATE, debit(assetAccount, 60.0));
            journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, DATE, credit(incomeAccount, 54.0),
                    credit(incomeAccount, 36.0));
            journalEntryHelper.checkJournalEntryForLiabilityAccount(taxAccount, DATE, credit(taxAccount, 6.0), credit(taxAccount, 4.0));
        });
    }

    @Test
    void cashAccounting_taxComponentWithoutCreditAccount_keepsTheFullAmountInIncome() {
        runAt(DATE, () -> {
            final Long savingsId = activeSavings(cashProduct(), "500");
            final Long savingsChargeId = addCharge(savingsId, taxedCharge(null));

            savingsChargeHelper.payCharge(savingsId, savingsChargeId, "100", DATE);

            assertFullyPaid(savingsId, savingsChargeId);
            journalEntryHelper.checkJournalEntryForLiabilityAccount(controlAccount, DATE, debit(controlAccount, 100.0));
            journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, DATE, credit(incomeAccount, 100.0));
        });
    }

    @Test
    void accrualAccounting_chargePayment_postsTaxAndNetIncome() {
        runAt(DATE, () -> {
            final Account taxAccount = accountHelper.createLiabilityAccount("vatPayable");
            final Long productId = createSavingsProduct(SavingsRequestBuilders
                    .withAccrualAccountingMappings(productRequest(), assetAccount, controlAccount, incomeAccount, expenseAccount)
                    .accountingRule(SavingsTestData.AccountingRule.ACCRUAL_PERIODIC)).getResourceId();
            final Long savingsId = activeSavings(productId, "500");
            final Long savingsChargeId = addCharge(savingsId, taxedCharge(taxAccount.getAccountID().longValue()));

            savingsChargeHelper.payCharge(savingsId, savingsChargeId, "100", DATE);

            assertFullyPaid(savingsId, savingsChargeId);
            journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, DATE, credit(incomeAccount, 90.0));
            journalEntryHelper.checkJournalEntryForLiabilityAccount(taxAccount, DATE, credit(taxAccount, 10.0));
        });
    }

    private void assertFullyPaid(final Long savingsId, final Long savingsChargeId) {
        final GetSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdResponse charge = getSavingsAccountCharge(savingsId,
                savingsChargeId);
        assertEquals(100.0f, charge.getAmountPaid());
        assertEquals(0.0f, charge.getAmountOutstanding());
    }

    private Long cashProduct() {
        return createSavingsProduct(SavingsRequestBuilders.withCashBasedAccounting(productRequest(), assetAccount, controlAccount,
                incomeAccount, expenseAccount)).getResourceId();
    }

    private static PostSavingsProductsRequest productRequest() {
        return SavingsRequestBuilders.savingsProduct(SavingsTestData.InterestCompoundingPeriodType.DAILY,
                SavingsTestData.InterestPostingPeriodType.QUARTERLY, SavingsTestData.InterestCalculationType.DAILY_BALANCE);
    }

    private Long activeSavings(final Long productId, final String deposit) {
        final Long savingsId = createApproveActivateSavings(createClient(DATE), productId, DATE);
        deposit(savingsId, deposit, DATE);
        return savingsId;
    }

    private Long taxedCharge(final Long taxCreditAccountId) {
        final Long taxComponentId = taxComponentHelper
                .createTaxComponent(
                        new PostTaxesComponentsRequest().name(Utils.uniqueRandomStringGenerator("TAX_COMP_", 6)).percentage(10.0f)
                                .startDate(TAX_START_DATE).dateFormat(SavingsTestData.DATETIME_PATTERN).locale(SavingsTestData.LOCALE)
                                .creditAccountId(taxCreditAccountId).creditAccountType(taxCreditAccountId == null ? null : 2))
                .getResourceId();
        final Long taxGroupId = taxGroupHelper
                .createTaxGroup(new PostTaxesGroupRequest().name(Utils.uniqueRandomStringGenerator("TAX_GRP_", 6))
                        .taxComponents(Set.of(new PostTaxesGroupTaxComponents().taxComponentId(taxComponentId).startDate(TAX_START_DATE)))
                        .dateFormat(SavingsTestData.DATETIME_PATTERN).locale(SavingsTestData.LOCALE))
                .getResourceId();
        return savingsChargeHelper
                .createCharge(
                        SavingsRequestBuilders.savingsSpecifiedDueDateCharge(100.0, SavingsTestData.CURRENCY_CODE).taxGroupId(taxGroupId))
                .getResourceId();
    }

    private Long addCharge(final Long savingsId, final Long chargeId) {
        return savingsChargeHelper.addChargeToSavings(savingsId, new PostSavingsAccountsSavingsAccountIdChargesRequest().chargeId(chargeId)
                .amount(100.0f).dueDate(DATE).dateFormat(SavingsTestData.DATETIME_PATTERN).locale(SavingsTestData.LOCALE)).getResourceId();
    }

    private Long payChargeTransactionId(final Long savingsId) {
        return savingsHelper.getSavingsDetails(savingsId, "transactions").getTransactions().stream()
                .filter(transaction -> Boolean.TRUE.equals(transaction.getTransactionType().getPayCharge()))
                .map(SavingsAccountTransactionData::getId).findFirst().orElseThrow();
    }

    private static LoanTestData.Journal debit(final Account account, final double amount) {
        return new LoanTestData.Journal(amount, account, "DEBIT");
    }

    private static LoanTestData.Journal credit(final Account account, final double amount) {
        return new LoanTestData.Journal(amount, account, "CREDIT");
    }
}
