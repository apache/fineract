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
package org.apache.fineract.integrationtests.savings.base;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.BusinessDateUpdateRequest;
import org.apache.fineract.client.models.PostSavingsAccountTransactionsRequest;
import org.apache.fineract.client.models.PostSavingsAccountTransactionsResponse;
import org.apache.fineract.client.models.PostSavingsAccountsAccountIdRequest;
import org.apache.fineract.client.models.PostSavingsAccountsAccountIdResponse;
import org.apache.fineract.client.models.PostSavingsAccountsRequest;
import org.apache.fineract.client.models.PostSavingsAccountsResponse;
import org.apache.fineract.client.models.PostSavingsProductsRequest;
import org.apache.fineract.client.models.PostSavingsProductsResponse;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.client.models.SavingsAccountData;
import org.apache.fineract.client.models.SavingsAccountTransactionData;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.IntegrationTest;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.JournalEntryHelper;
import org.apache.fineract.integrationtests.common.system.CodeHelper;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;

@Slf4j
public class BaseSavingsIntegrationTest extends IntegrationTest {

    protected static final String DATETIME_PATTERN = "dd MMMM yyyy";

    static {
        Utils.initializeRESTAssured();
    }

    protected final ResponseSpecification responseSpec = createResponseSpecification(Matchers.is(200));
    private final String fullAdminAuthKey = getFullAdminAuthKey();
    protected final RequestSpecification requestSpec = createRequestSpecification(fullAdminAuthKey);

    protected BusinessDateHelper businessDateHelper = new BusinessDateHelper();
    protected DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATETIME_PATTERN);
    protected GlobalConfigurationHelper globalConfigurationHelper = new GlobalConfigurationHelper();
    protected final CodeHelper codeHelper = new CodeHelper();
    protected JournalEntryHelper journalEntryHelper = new JournalEntryHelper(requestSpec, responseSpec);
    protected ClientHelper clientHelper = new ClientHelper(requestSpec, responseSpec);
    protected SchedulerJobHelper schedulerJobHelper = new SchedulerJobHelper(requestSpec);

    protected void runFromToInclusive(String fromDate, String toDate, Consumer<String> runnable) {
        DateTimeFormatter format = DateTimeFormatter.ofPattern(DATETIME_PATTERN);
        LocalDate startDate = LocalDate.parse(fromDate, format);
        LocalDate endDate = LocalDate.parse(toDate, format);

        LocalDate currentDate = startDate;
        while (currentDate.isBefore(endDate) || currentDate.isEqual(endDate)) {
            runAt(format.format(currentDate), runnable);
            currentDate = currentDate.plusDays(1);
        }
    }

    protected void runAt(String date, Runnable runnable) {
        runAt(date, (d) -> runnable.run());
    }

    protected void runAt(String date, Consumer<String> runnable) {
        try {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            businessDateHelper.updateBusinessDate(new BusinessDateUpdateRequest().type(BusinessDateUpdateRequest.TypeEnum.BUSINESS_DATE)
                    .date(date).dateFormat(DATETIME_PATTERN).locale("en"));
            runnable.accept(date);
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    protected PostSavingsProductsRequest dailyInterestPostingProduct() {
        return new PostSavingsProductsRequest().locale("en").name(Utils.uniqueRandomStringGenerator("DAILY_INTEREST", 6))//
                .shortName(Utils.uniqueRandomStringGenerator("", 4))//
                .description("Daily interest posting product") //
                .nominalAnnualInterestRate(10.0) //
                .digitsAfterDecimal(0) //
                .currencyCode("EUR") //
                .accountingRule(1) // none
                .interestCalculationDaysInYearType(DaysInYearType.DAYS_365).interestCompoundingPeriodType(InterestPeriodType.DAILY)
                .interestCalculationType(InterestCalculationType.AVERAGE_DAILY_BALANCE) //
                .interestPostingPeriodType(InterestPeriodType.DAILY);//
    }

    protected PostSavingsProductsResponse createProduct(PostSavingsProductsRequest productsRequest) {
        return ok(fineractClient().savingsProducts.create13(productsRequest));
    }

    protected PostSavingsAccountsRequest applySavingsRequest(Long clientId, Long productId, String submittedDate) {
        return new PostSavingsAccountsRequest().clientId(clientId).productId(productId).dateFormat(DATETIME_PATTERN).locale("en")
                .submittedOnDate(submittedDate);
    }

    protected PostSavingsAccountsResponse applySavingsAccount(PostSavingsAccountsRequest request) {
        return ok(fineractClient().savingsAccounts.submitApplication2(request));
    }

    protected PostSavingsAccountsAccountIdResponse approveSavingsAccount(Long savingsId, String date) {
        PostSavingsAccountsAccountIdRequest request = new PostSavingsAccountsAccountIdRequest().dateFormat(DATETIME_PATTERN).locale("en")
                .approvedOnDate(date);
        return ok(fineractClient().savingsAccounts.handleCommands6(savingsId, request, "approve"));
    }

    protected PostSavingsAccountsAccountIdResponse activateSavingsAccount(Long savingsId, String date) {
        PostSavingsAccountsAccountIdRequest request = new PostSavingsAccountsAccountIdRequest().dateFormat(DATETIME_PATTERN).locale("en")
                .activatedOnDate(date);
        return ok(fineractClient().savingsAccounts.handleCommands6(savingsId, request, "activate"));
    }

    protected PostSavingsAccountTransactionsResponse deposit(Long savingsId, String date, BigDecimal amount) {
        PostSavingsAccountTransactionsRequest request = new PostSavingsAccountTransactionsRequest() //
                .dateFormat(DATETIME_PATTERN) //
                .locale("en") //
                .paymentTypeId(1).transactionAmount(amount) //
                .transactionDate(date); //
        return ok(fineractClient().savingsTransactions.transaction2(savingsId, request, "deposit"));
    }

    protected SavingsAccountData getSavingsAccount(Long savingsId) {
        return ok(fineractClient().savingsAccounts.retrieveOne25(savingsId, false, null, "transactions"));
    }

    protected List<SavingsAccountTransactionData> getTransactions(Long savingsId) {
        return ok(fineractClient().savingsAccounts.retrieveOne25(savingsId, false, null, "transactions")).getTransactions();
    }

    protected void verifyNoTransactions(Long savingsId) {
        verifyTransactions(savingsId, (Transaction[]) null);
    }

    protected void verifyTransactions(Long savingsId, Transaction... transactions) {
        SavingsAccountData savingsDetails = ok(fineractClient().savingsAccounts.retrieveOne25(savingsId, false, null, "transactions"));
        if (transactions == null || transactions.length == 0) {
            Assertions.assertTrue(savingsDetails.getTransactions().isEmpty(), "No transaction is expected on savings account " + savingsId);
        } else {
            Assertions.assertEquals(transactions.length, savingsDetails.getTransactions().size());
            Arrays.stream(transactions).forEach(tr -> {
                Optional<SavingsAccountTransactionData> optTx = savingsDetails.getTransactions().stream()
                        .filter(item -> Objects.requireNonNull(item.getAmount()).compareTo(BigDecimal.valueOf(tr.amount)) == 0 //
                                && Objects.equals(item.getTransactionType().getValue(), tr.type) //
                                && Objects.equals(item.getDate(), LocalDate.parse(tr.date, dateTimeFormatter)))
                        .findFirst();
                Assertions.assertTrue(optTx.isPresent(), "Required transaction  not found: " + tr + " on savings account " + savingsId);

                SavingsAccountTransactionData tx = optTx.get();

                if (tr.reversed != null) {
                    Assertions.assertEquals(tr.reversed, tx.getReversed(),
                            "Transaction is not reversed: " + tr + " on savings account " + savingsId);
                }
            });
        }
    }

    private RequestSpecification createRequestSpecification(String authKey) {
        RequestSpecification requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + authKey);
        requestSpec.header("Fineract-Platform-TenantId", "default");
        return requestSpec;
    }

    protected ResponseSpecification createResponseSpecification(Matcher<Integer> statusCodeMatcher) {
        return new ResponseSpecBuilder().expectStatusCode(statusCodeMatcher).build();
    }

    private String getFullAdminAuthKey() {
        return Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey();
    }

    @ToString
    @AllArgsConstructor
    public static class Transaction {

        Double amount;
        String type;
        String date;
        Boolean reversed;
    }

    public static class InterestPeriodType {

        public static final int DAILY = 1;
        public static final int MONTHLY = 4;
        public static final int QUATERLY = 5;
        public static final int BIANNUAL = 6;
    }

    public static class InterestCalculationType {

        public static final int DAILY_BALANCE = 1;
        public static final int AVERAGE_DAILY_BALANCE = 2;
    }

    public static class InterestRecalculationCompoundingMethod {

        public static final Integer NONE = 0;
    }

    public static class DaysInYearType {

        public static final Integer INVALID = 0;
        public static final Integer ACTUAL = 1;
        public static final Integer DAYS_360 = 360;
        public static final Integer DAYS_364 = 364;
        public static final Integer DAYS_365 = 365;
    }
}
