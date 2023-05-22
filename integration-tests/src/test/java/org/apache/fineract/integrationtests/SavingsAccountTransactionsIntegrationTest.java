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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.fineract.client.models.GetSavingsAccountTransactionsPageItem;
import org.apache.fineract.client.models.GetSavingsAccountTransactionsResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsProductHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsStatusChecker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({ "rawtypes" })
public class SavingsAccountTransactionsIntegrationTest {

    public static final String ACCOUNT_TYPE_INDIVIDUAL = "INDIVIDUAL";

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private SavingsProductHelper savingsProductHelper;
    private SavingsAccountHelper savingsAccountHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.savingsProductHelper = new SavingsProductHelper();
    }

    @Test
    public void testSavingsTransactions() {
        final String startDate = "01 May 2023";
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, startDate);
        Assertions.assertNotNull(clientID);

        final Integer savingsId = createSavingsAccountDailyPosting(clientID, startDate);

        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "1000", startDate, CommonConstants.RESPONSE_RESOURCE_ID);

        GetSavingsAccountTransactionsResponse transactionsResponse = this.savingsAccountHelper.getSavingsTransactionsV2(savingsId);
        Assertions.assertNotNull(transactionsResponse);
        assertEquals(1, transactionsResponse.getTotalFilteredRecords());
        Assertions.assertNotNull(transactionsResponse.getPageItems());
    }

    @Test
    public void testSavingsTransactionsPagination() {
        final String clientCreateDate = "25 Apr 2023";
        final String startDate1 = "01 May 2023";
        final String startDate2 = "04 May 2023";
        final String startDate3 = "07 May 2023";
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, clientCreateDate);
        Assertions.assertNotNull(clientID);
        Map<String, String> queryParametersMap = new HashMap<>();
        queryParametersMap.put("offset", "0");
        queryParametersMap.put("limit", "2");
        final Integer savingsId = createSavingsAccountDailyPosting(clientID, clientCreateDate);

        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "1000", startDate1, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "500", startDate2, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "700", startDate3, CommonConstants.RESPONSE_RESOURCE_ID);

        GetSavingsAccountTransactionsResponse transactionsResponse = this.savingsAccountHelper
                .getSavingsTransactionsWithQueryParams(savingsId, queryParametersMap);
        Assertions.assertNotNull(transactionsResponse);
        assertEquals(3, transactionsResponse.getTotalFilteredRecords());
        Assertions.assertNotNull(transactionsResponse.getPageItems());
        assertEquals(2, transactionsResponse.getPageItems().size());
    }

    @Test
    public void testSavingsTransactionsSortByAmountDesc() {
        final String clientCreateDate = "25 Apr 2023";
        final String startDate1 = "01 May 2023";
        final String startDate2 = "04 May 2023";
        final String startDate3 = "07 May 2023";
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, clientCreateDate);
        Assertions.assertNotNull(clientID);
        Map<String, String> queryParametersMap = new HashMap<>();
        queryParametersMap.put("orderBy", "amount");
        queryParametersMap.put("sortOrder", "DESC");
        final Integer savingsId = createSavingsAccountDailyPosting(clientID, clientCreateDate);

        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "1000", startDate1, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "500", startDate2, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "700", startDate3, CommonConstants.RESPONSE_RESOURCE_ID);

        GetSavingsAccountTransactionsResponse transactionsResponse = this.savingsAccountHelper
                .getSavingsTransactionsWithQueryParams(savingsId, queryParametersMap);
        Assertions.assertNotNull(transactionsResponse);
        assertEquals(3, transactionsResponse.getTotalFilteredRecords());
        Assertions.assertNotNull(transactionsResponse.getPageItems());
        assertEquals(3, transactionsResponse.getPageItems().size());
        Set<GetSavingsAccountTransactionsPageItem> pageItems = transactionsResponse.getPageItems();
        List<BigDecimal> actualList = new ArrayList<>();
        pageItems.forEach(data -> {
            actualList.add(data.getAmount());
        });
        boolean isDescending = isListOrdered(actualList, "DESC");

        assertEquals(true, isDescending);
    }

    @Test
    public void testSavingsTransactionsSortByAmountDefault() {
        final String clientCreateDate = "25 Apr 2023";
        final String startDate1 = "01 May 2023";
        final String startDate2 = "04 May 2023";
        final String startDate3 = "07 May 2023";
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, clientCreateDate);
        Assertions.assertNotNull(clientID);
        Map<String, String> queryParametersMap = new HashMap<>();
        queryParametersMap.put("orderBy", "amount");
        final Integer savingsId = createSavingsAccountDailyPosting(clientID, clientCreateDate);

        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "1000", startDate1, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "500", startDate2, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "700", startDate3, CommonConstants.RESPONSE_RESOURCE_ID);

        GetSavingsAccountTransactionsResponse transactionsResponse = this.savingsAccountHelper
                .getSavingsTransactionsWithQueryParams(savingsId, queryParametersMap);
        Assertions.assertNotNull(transactionsResponse);
        assertEquals(3, transactionsResponse.getTotalFilteredRecords());
        Assertions.assertNotNull(transactionsResponse.getPageItems());
        assertEquals(3, transactionsResponse.getPageItems().size());
        Set<GetSavingsAccountTransactionsPageItem> pageItems = transactionsResponse.getPageItems();
        List<BigDecimal> actualList = new ArrayList<>();
        pageItems.forEach(data -> {
            actualList.add(data.getAmount());
        });
        boolean isAscending = isListOrdered(actualList, null);
        // Default Sort is ASC if sortOrder is not provided
        assertEquals(true, isAscending);

    }

    @Test
    public void testSavingsTransactionsSortByTransactionDateAsc() {
        final String clientCreateDate = "25 Apr 2023";
        final String startDate1 = "01 May 2023";
        final String startDate2 = "04 May 2023";
        final String startDate3 = "07 May 2023";
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, clientCreateDate);
        Assertions.assertNotNull(clientID);
        Map<String, String> queryParametersMap = new HashMap<>();
        queryParametersMap.put("orderBy", "dateOf");
        queryParametersMap.put("sortOrder", "ASC");
        final Integer savingsId = createSavingsAccountDailyPosting(clientID, clientCreateDate);

        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "1000", startDate1, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "500", startDate2, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "700", startDate3, CommonConstants.RESPONSE_RESOURCE_ID);

        GetSavingsAccountTransactionsResponse transactionsResponse = this.savingsAccountHelper
                .getSavingsTransactionsWithQueryParams(savingsId, queryParametersMap);
        Assertions.assertNotNull(transactionsResponse);
        assertEquals(3, transactionsResponse.getTotalFilteredRecords());
        Assertions.assertNotNull(transactionsResponse.getPageItems());
        assertEquals(3, transactionsResponse.getPageItems().size());
        Set<GetSavingsAccountTransactionsPageItem> pageItems = transactionsResponse.getPageItems();
        List<LocalDate> actualList = new ArrayList<>();
        pageItems.forEach(data -> {
            actualList.add(data.getDate());
        });
        boolean isAscending = isDateListOrdered(actualList, "ASC");
        assertEquals(true, isAscending);

    }

    @Test
    public void testSavingsTransactionsFilterByTransactionType() {
        final String clientCreateDate = "25 Apr 2023";
        final String startDate1 = "01 May 2023";
        final String startDate2 = "04 May 2023";
        final String startDate3 = "07 May 2023";
        final String withdrawDate = "09 May 2023";
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, clientCreateDate);
        Assertions.assertNotNull(clientID);
        Map<String, String> queryParametersMap = new HashMap<>();
        queryParametersMap.put("transactionType", "deposit");
        final Integer savingsId = createSavingsAccountDailyPosting(clientID, clientCreateDate);

        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "1000", startDate1, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "500", startDate2, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "700", startDate3, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.postInterestForSavings(savingsId);

        this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, "400", withdrawDate, CommonConstants.RESPONSE_RESOURCE_ID);

        GetSavingsAccountTransactionsResponse transactionsResponse = this.savingsAccountHelper
                .getSavingsTransactionsWithQueryParams(savingsId, queryParametersMap);
        Assertions.assertNotNull(transactionsResponse);
        assertEquals(3, transactionsResponse.getTotalFilteredRecords());
        Assertions.assertNotNull(transactionsResponse.getPageItems());
        assertEquals(3, transactionsResponse.getPageItems().size());
        Set<GetSavingsAccountTransactionsPageItem> pageItems = transactionsResponse.getPageItems();
        for (GetSavingsAccountTransactionsPageItem item : pageItems) {
            assertEquals(true, item.getTransactionType().getDeposit(), "Transaction Type is not as expected");
        }
    }

    @Test
    public void testSavingsTransactionsFilterByTransactionTypeAndAmount() {
        final String clientCreateDate = "25 Apr 2023";
        final String startDate1 = "01 May 2023";
        final String startDate2 = "04 May 2023";
        final String startDate3 = "07 May 2023";
        final String withdrawDate = "09 May 2023";
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, clientCreateDate);
        Assertions.assertNotNull(clientID);
        Map<String, String> queryParametersMap = new HashMap<>();
        queryParametersMap.put("transactionType", "deposit");
        queryParametersMap.put("fromAmount", "400");
        queryParametersMap.put("toAmount", "700");
        final Integer savingsId = createSavingsAccountDailyPosting(clientID, clientCreateDate);

        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "1000", startDate1, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "500", startDate2, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "700", startDate3, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.postInterestForSavings(savingsId);

        this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, "400", withdrawDate, CommonConstants.RESPONSE_RESOURCE_ID);

        GetSavingsAccountTransactionsResponse transactionsResponse = this.savingsAccountHelper
                .getSavingsTransactionsWithQueryParams(savingsId, queryParametersMap);
        Assertions.assertNotNull(transactionsResponse);
        assertEquals(2, transactionsResponse.getTotalFilteredRecords());
        Assertions.assertNotNull(transactionsResponse.getPageItems());
        assertEquals(2, transactionsResponse.getPageItems().size());
        Set<GetSavingsAccountTransactionsPageItem> pageItems = transactionsResponse.getPageItems();
        for (GetSavingsAccountTransactionsPageItem item : pageItems) {
            assertEquals(true, item.getTransactionType().getDeposit(), "Transaction Type is not as expected");
            assertEquals(true, item.getAmount().compareTo(BigDecimal.valueOf(700)) <= 0);
        }
    }

    @Test
    public void testSavingsTransactionsFilterByTransactionDate() {
        final String clientCreateDate = "25 Apr 2023";
        final String startDate1 = "01 May 2023";
        final String startDate2 = "04 May 2023";
        final String startDate3 = "07 May 2023";
        final String withdrawDate = "09 May 2023";
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, clientCreateDate);
        Assertions.assertNotNull(clientID);
        Map<String, String> queryParametersMap = new HashMap<>();
        String dateFormat = "dd MMM yyyy";
        queryParametersMap.put("locale", "en");
        queryParametersMap.put("dateFormat", dateFormat);
        queryParametersMap.put("fromDate", startDate1);
        queryParametersMap.put("toDate", startDate2);
        final Integer savingsId = createSavingsAccountDailyPosting(clientID, clientCreateDate);

        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "1000", startDate1, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "500", startDate2, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "700", startDate3, CommonConstants.RESPONSE_RESOURCE_ID);

        this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, "400", withdrawDate, CommonConstants.RESPONSE_RESOURCE_ID);

        GetSavingsAccountTransactionsResponse transactionsResponse = this.savingsAccountHelper
                .getSavingsTransactionsWithQueryParams(savingsId, queryParametersMap);
        Assertions.assertNotNull(transactionsResponse);
        assertEquals(2, transactionsResponse.getTotalFilteredRecords());
        Assertions.assertNotNull(transactionsResponse.getPageItems());
        assertEquals(2, transactionsResponse.getPageItems().size());
        Set<GetSavingsAccountTransactionsPageItem> pageItems = transactionsResponse.getPageItems();
        for (GetSavingsAccountTransactionsPageItem item : pageItems) {
            assertEquals(true, isDateBetweenGivenRange(startDate1, startDate2, dateFormat, item));
        }
    }

    private boolean isDateBetweenGivenRange(final String fromDate, final String toDate, String dateFormat, GetSavingsAccountTransactionsPageItem item) {
        return (item.getDate().compareTo(LocalDate.parse(fromDate, DateTimeFormatter.ofPattern(dateFormat))) >= 0) && (item.getDate().compareTo(LocalDate.parse(toDate, DateTimeFormatter.ofPattern(dateFormat))) <= 0);
    }
    

    private <T extends Comparable<T>> boolean isListOrdered(List<T> list, String order) {
        for (int i = 1; i < list.size(); i++) {
            T currentElement = list.get(i);
            T previousElement = list.get(i - 1);

            int comparisonResult = currentElement.compareTo(previousElement);

            if ((Objects.isNull(order) || order.equalsIgnoreCase("asc")) && comparisonResult > 0) {
                return true; // In ascending order
            } else if (order.equalsIgnoreCase("desc") && comparisonResult < 0) {
                return true; // In descending order
            }
        }

        return false; // List is not ordered
    }

    private boolean isDateListOrdered(List<LocalDate> dates, String order) {
        for (int i = 1; i < dates.size(); i++) {
            LocalDate currentDate = dates.get(i);
            LocalDate previousDate = dates.get(i - 1);

            int comparisonResult = currentDate.compareTo(previousDate);

            if ((Objects.isNull(order) || order.equalsIgnoreCase("asc")) && comparisonResult > 0) {
                return true; // In ascending order
            } else if (order.equalsIgnoreCase("desc") && comparisonResult < 0) {
                return true; // In descending order
            }
        }

        return false; // List is not ordered
    }

    private Integer createSavingsAccountDailyPosting(final Integer clientID, final String startDate) {
        final Integer savingsProductID = createSavingsProductDailyPosting();
        Assertions.assertNotNull(savingsProductID);
        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplicationOnDate(clientID, savingsProductID,
                ACCOUNT_TYPE_INDIVIDUAL, startDate);
        Assertions.assertNotNull(savingsId);
        HashMap savingsStatusHashMap = this.savingsAccountHelper.approveSavingsOnDate(savingsId, startDate);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);
        savingsStatusHashMap = this.savingsAccountHelper.activateSavingsAccount(savingsId, startDate);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);
        return savingsId;
    }

    private Integer createSavingsProductDailyPosting() {
        final String savingsProductJSON = this.savingsProductHelper.withInterestCompoundingPeriodTypeAsDaily()
                .withInterestPostingPeriodTypeAsDaily().withInterestCalculationPeriodTypeAsDailyBalance().build();
        return SavingsProductHelper.createSavingsProduct(savingsProductJSON, requestSpec, responseSpec);
    }

    // Reset configuration fields
    @AfterEach
    public void tearDown() {
        GlobalConfigurationHelper.resetAllDefaultGlobalConfigurations(this.requestSpec, this.responseSpec);
        GlobalConfigurationHelper.verifyAllDefaultGlobalConfigurations(this.requestSpec, this.responseSpec);
    }

}
