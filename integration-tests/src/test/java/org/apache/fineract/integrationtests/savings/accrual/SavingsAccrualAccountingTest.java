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
package org.apache.fineract.integrationtests.savings.accrual;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.fineract.client.models.GetSavingsAccountsAccountIdResponse;
import org.apache.fineract.client.models.GetSavingsAccountsTransaction;
import org.apache.fineract.client.models.PostSavingsAccountTransactionsRequest;
import org.apache.fineract.client.models.PostSavingsAccountTransactionsResponse;
import org.apache.fineract.client.models.PostSavingsAccountsRequest;
import org.apache.fineract.client.models.PostSavingsAccountsResponse;
import org.apache.fineract.client.models.PostSavingsProductsRequest;
import org.apache.fineract.client.models.PostSavingsProductsResponse;
import org.apache.fineract.integrationtests.BaseSavingsIntegrationTest;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.accounting.JournalEntryHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsProductHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SavingsAccrualAccountingTest extends BaseSavingsIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(SavingsAccrualAccountingTest.class);
    private static ResponseSpecification responseSpec;
    private static RequestSpecification requestSpec;
    private static AccountHelper accountHelper;
    private static BusinessDateHelper businessDateHelper;
    private static ClientHelper clientHelper;
    private static SavingsProductHelper savingsProductHelper;
    private static SavingsAccountHelper savingsAccountHelper;
    private static SchedulerJobHelper schedulerJobHelper;
    private static JournalEntryHelper journalEntryHelper;

    @BeforeAll
    public static void setup() {
        Utils.initializeRESTAssured();
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        requestSpec.header("Fineract-Platform-TenantId", "default");
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        businessDateHelper = new BusinessDateHelper();
        accountHelper = new AccountHelper(requestSpec, responseSpec);
        clientHelper = new ClientHelper(requestSpec, responseSpec);
        savingsProductHelper = new SavingsProductHelper();
        savingsAccountHelper = new SavingsAccountHelper(requestSpec, responseSpec);
        schedulerJobHelper = new SchedulerJobHelper(requestSpec);
        journalEntryHelper = new JournalEntryHelper(requestSpec, responseSpec);
    }

    // UC1: Simple Savings account creation with Accrual Accounting enabled
    // 1. Create a client account
    // 2. Create a Savings product with Accrual accounting enabled
    // 3. Create a Savings account
    // 4. Approve and Activate the Savings account
    // 5. Add a Deposit and validate the account balance
    // ------ Using a second business date
    // 6. Add a second Deposit transaction to have other balance
    // 7. Run the new batch job to Add the Accrual transactions in the Savings account
    // 8. Get the Savings details to:
    // a) Validate the accrued till date
    // b) Validate the total amount of the accrual transactions generated
    // c) Validate the Journal Entry of the first Accrual transaction generated
    @Test
    public void uc1() {
        AtomicLong savingsAccountId = new AtomicLong();
        runAt("1 May 2024", () -> {
            final Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            final Account assetAccount = accountHelper.createAssetAccount();
            final Account incomeAccount = accountHelper.createIncomeAccount();
            final Account expenseAccount = accountHelper.createExpenseAccount();
            final Account overpaymentAccount = accountHelper.createLiabilityAccount();

            PostSavingsProductsRequest savingsProductRequest = createSavingsProductWithAccountMappingForAccrualBased(1.0, assetAccount,
                    incomeAccount, expenseAccount, overpaymentAccount);
            LOG.info("------------------------------CREATE SAVINGS PRODUCT------------------------------------\n");
            PostSavingsProductsResponse savingsProductResponse = savingsProductHelper.createSavingsProduct(savingsProductRequest);
            assertNotNull(savingsProductResponse);
            assertNotNull(savingsProductResponse.getResourceId());
            final Integer savingsProductId = savingsProductResponse.getResourceId();

            PostSavingsAccountsRequest savingsAccountRequest = createSavingsAccountRequest(clientId.intValue(), savingsProductId)
                    .submittedOnDate("1 May 2024").nominalAnnualInterestRate(2.0);
            PostSavingsAccountsResponse savingsAccountResponse = savingsAccountHelper.createSavingsAccount(savingsAccountRequest);
            assertNotNull(savingsAccountResponse);
            assertNotNull(savingsAccountResponse.getResourceId());
            savingsAccountId.set(savingsAccountResponse.getResourceId().longValue());

            savingsAccountHelper.approveSavingsAccount(savingsAccountId.get(), "1 May 2024");
            savingsAccountHelper.activateSavingsAccount(savingsAccountId.get(), "1 May 2024");

            GetSavingsAccountsAccountIdResponse savingsAccountDetails = savingsAccountHelper.getSavingsAccount(savingsAccountId.get());
            LOG.info("Savings account created {}", savingsAccountDetails.getAccountNo());
            assertNotNull(savingsAccountDetails);
            assertNotNull(savingsAccountDetails.getId());
            assertTrue(BigDecimal.ZERO.compareTo(savingsAccountDetails.getSummary().getAvailableBalance()) == 0);

            final BigDecimal transactionAmount = BigDecimal.valueOf(10000.0);
            PostSavingsAccountTransactionsRequest depositTransactionRequest = new PostSavingsAccountTransactionsRequest()
                    .transactionDate("1 May 2024").transactionAmount(transactionAmount).paymentTypeId(1).dateFormat(DATETIME_PATTERN)
                    .locale(LOCALE);
            PostSavingsAccountTransactionsResponse transactionResponse = savingsAccountHelper
                    .applySavingsAccountTransaction(savingsAccountId.get(), depositTransactionRequest, "deposit");
            assertNotNull(transactionResponse);
            assertNotNull(transactionResponse.getResourceId());

            savingsAccountDetails = savingsAccountHelper.getSavingsAccount(savingsAccountId.get());
            LOG.info("Savings account {} with balance {}".formatted(savingsAccountDetails.getAccountNo(),
                    savingsAccountDetails.getSummary().getAvailableBalance().stripTrailingZeros()));
            assertEquals(transactionAmount.stripTrailingZeros(),
                    savingsAccountDetails.getSummary().getAvailableBalance().stripTrailingZeros());
        });

        runAt("3 May 2024", () -> {
            final HashMap<String, Object> queryParams = new HashMap<>();
            queryParams.put("associations", "all");

            final BigDecimal transactionAmount = BigDecimal.valueOf(20000.0);
            GetSavingsAccountsAccountIdResponse savingsAccountDetails = savingsAccountHelper.getSavingsAccount(savingsAccountId.get());
            PostSavingsAccountTransactionsRequest depositTransactionRequest = new PostSavingsAccountTransactionsRequest()
                    .transactionDate("3 May 2024").transactionAmount(transactionAmount).paymentTypeId(1).dateFormat(DATETIME_PATTERN)
                    .locale(LOCALE);
            PostSavingsAccountTransactionsResponse transactionResponse = savingsAccountHelper
                    .applySavingsAccountTransaction(savingsAccountId.get(), depositTransactionRequest, "deposit");
            assertNotNull(transactionResponse);
            assertNotNull(transactionResponse.getResourceId());

            final String jobName = "Add Periodic Accrual Transactions for Savings";
            schedulerJobHelper.executeAndAwaitJob(jobName);

            savingsAccountDetails = savingsAccountHelper.getSavingsAccount(savingsAccountId.get(), queryParams);
            LOG.info("Savings account {} {}", savingsAccountDetails.getSummary().getAccruedTillDate(),
                    savingsAccountDetails.getSummary().getTotalInterestAccrued());
            assertEquals(LocalDate.of(2024, 05, 03), savingsAccountDetails.getSummary().getAccruedTillDate());

            // Validate Accrual Transactions
            final List<GetSavingsAccountsTransaction> transactions = savingsAccountDetails.getTransactions();
            checkSavingsAccrualTransactions(transactions, 2.74);

            // Valudate Journal Entries for the Accrual Transactions
            final Optional<GetSavingsAccountsTransaction> optTransaction = transactions.stream()
                    .filter(transaction -> transaction.getTransactionType().getAccrual() == true).findFirst();
            assertTrue(optTransaction.isPresent(), "Required Accrual transaction not found");
            final GetSavingsAccountsTransaction accrualTransaction = optTransaction.get();
            final List<HashMap> journalEntries = journalEntryHelper.getJournalEntriesByTransactionId("S" + accrualTransaction.getId());
            assertEquals(2, journalEntries.size());
            assertEquals(accrualTransaction.getAmount().floatValue(), journalEntries.get(0).get("amount"));
            assertEquals(accrualTransaction.getAmount().floatValue(), journalEntries.get(1).get("amount"));
        });
    }

}
