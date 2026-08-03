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
package org.apache.fineract.integrationtests.client.feign;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Function;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.DeleteSavingsAccountsAccountIdResponse;
import org.apache.fineract.client.models.GetSavingsAccountsSavingsAccountIdChargesResponse;
import org.apache.fineract.client.models.GetSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdResponse;
import org.apache.fineract.client.models.GetSavingsProductsProductIdResponse;
import org.apache.fineract.client.models.PostSavingsAccountTransactionsResponse;
import org.apache.fineract.client.models.PostSavingsAccountsAccountIdResponse;
import org.apache.fineract.client.models.PostSavingsAccountsResponse;
import org.apache.fineract.client.models.PostSavingsAccountsSavingsAccountIdChargesRequest;
import org.apache.fineract.client.models.PostSavingsAccountsSavingsAccountIdChargesResponse;
import org.apache.fineract.client.models.PostSavingsProductsRequest;
import org.apache.fineract.client.models.PostSavingsProductsResponse;
import org.apache.fineract.client.models.SavingsAccountData;
import org.apache.fineract.client.models.SavingsAccountStatusEnumData;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignAccountHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignAccountTransferHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBusinessDateHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignChargesHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignDatatableHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignGlobalConfigurationHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignJournalEntryHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignPaymentTypeHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsChargeHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsLifecycleExtension;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsProductHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsTransactionHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSchedulerHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(FeignSavingsLifecycleExtension.class)
public abstract class FeignSavingsTestBase extends FeignIntegrationTest {

    protected static FeignSavingsHelper savingsHelper;
    protected static FeignSavingsTransactionHelper savingsTransactionHelper;
    protected static FeignSavingsProductHelper savingsProductHelper;
    protected static FeignClientHelper clientHelper;
    protected static FeignChargesHelper chargesHelper;
    protected static FeignGlobalConfigurationHelper globalConfigurationHelper;
    protected static FeignBusinessDateHelper businessDateHelper;
    protected static FeignAccountHelper accountHelper;
    protected static FeignJournalEntryHelper journalEntryHelper;
    protected static FeignSchedulerHelper schedulerHelper;
    protected static FeignDatatableHelper datatableHelper;
    protected static FeignSavingsChargeHelper savingsChargeHelper;
    protected static FeignAccountTransferHelper accountTransferHelper;
    protected static FeignPaymentTypeHelper paymentTypeHelper;

    @BeforeAll
    public static void setupSavingsHelpers() {
        FineractFeignClient client = FineractFeignClientHelper.getFineractFeignClient();
        savingsHelper = new FeignSavingsHelper(client);
        savingsTransactionHelper = new FeignSavingsTransactionHelper(client);
        savingsProductHelper = new FeignSavingsProductHelper(client);
        clientHelper = new FeignClientHelper(client);
        chargesHelper = new FeignChargesHelper(client);
        globalConfigurationHelper = new FeignGlobalConfigurationHelper(client);
        businessDateHelper = new FeignBusinessDateHelper(client);
        accountHelper = new FeignAccountHelper(client);
        journalEntryHelper = new FeignJournalEntryHelper(client);
        schedulerHelper = new FeignSchedulerHelper(client);
        datatableHelper = new FeignDatatableHelper(client);
        savingsChargeHelper = new FeignSavingsChargeHelper(client);
        accountTransferHelper = new FeignAccountTransferHelper(client);
        paymentTypeHelper = new FeignPaymentTypeHelper(client);
    }

    protected Long createClient() {
        return clientHelper.createClient();
    }

    protected Long createClient(String activationDate) {
        return clientHelper.createClient(activationDate);
    }

    protected PostSavingsProductsResponse createDefaultSavingsProduct() {
        return savingsProductHelper.createDefaultSavingsProduct();
    }

    protected PostSavingsProductsResponse createSavingsProduct(PostSavingsProductsRequest request) {
        return savingsProductHelper.createSavingsProduct(request);
    }

    protected GetSavingsProductsProductIdResponse getSavingsProduct(Long productId) {
        return savingsProductHelper.getSavingsProduct(productId);
    }

    protected PostSavingsAccountsResponse submitSavingsApplication(Long clientId, Long productId, String date) {
        return savingsHelper.submitApplication(clientId, productId, date);
    }

    protected PostSavingsAccountsAccountIdResponse approveSavings(Long savingsId, String date) {
        return savingsHelper.approveSavings(savingsId, date);
    }

    protected PostSavingsAccountsAccountIdResponse activateSavings(Long savingsId, String date) {
        return savingsHelper.activateSavings(savingsId, date);
    }

    protected Long createApproveActivateSavings(Long clientId, Long productId, String date) {
        return savingsHelper.createApproveActivateSavings(clientId, productId, date);
    }

    protected PostSavingsAccountsAccountIdResponse closeSavings(Long savingsId, String date, boolean withdrawBalance) {
        return savingsHelper.closeSavings(savingsId, date, withdrawBalance);
    }

    protected DeleteSavingsAccountsAccountIdResponse deleteSavingsApplication(Long savingsId) {
        return savingsHelper.deleteSavingsApplication(savingsId);
    }

    protected SavingsAccountData getSavingsDetails(Long savingsId) {
        return savingsHelper.getSavingsDetails(savingsId);
    }

    protected PostSavingsAccountTransactionsResponse deposit(Long savingsId, String amount, String date) {
        return savingsTransactionHelper.deposit(savingsId, amount, date);
    }

    protected PostSavingsAccountTransactionsResponse withdraw(Long savingsId, String amount, String date) {
        return savingsTransactionHelper.withdraw(savingsId, amount, date);
    }

    protected void verifySavingsStatus(Long savingsId, Function<SavingsAccountStatusEnumData, Boolean> statusExtractor) {
        SavingsAccountData savings = getSavingsDetails(savingsId);
        assertNotNull(savings.getStatus(), "Savings status should not be null");
        assertTrue(statusExtractor.apply(savings.getStatus()), "Savings status check failed for account " + savingsId);
    }

    protected void runAt(String date, Runnable action) {
        businessDateHelper.runAt(date, detectDateFormat(date), action);
    }

    private static String detectDateFormat(String date) {
        return date.matches("\\d{4}-\\d{2}-\\d{2}") ? LoanTestData.ISO_DATE_PATTERN : LoanTestData.DATETIME_PATTERN;
    }

    protected PostSavingsAccountsSavingsAccountIdChargesResponse addSavingsAccountCharge(Long savingsId,
            PostSavingsAccountsSavingsAccountIdChargesRequest request) {
        return savingsHelper.addSavingsAccountCharge(savingsId, request);
    }

    protected GetSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdResponse getSavingsAccountCharge(Long savingsId,
            Long savingsAccountChargeId) {
        return savingsHelper.getSavingsAccountCharge(savingsId, savingsAccountChargeId);
    }

    protected List<GetSavingsAccountsSavingsAccountIdChargesResponse> getSavingsCharges(Long savingsId) {
        return savingsHelper.getSavingsCharges(savingsId);
    }
}
