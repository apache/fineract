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
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.apache.fineract.client.models.Filters;
import org.apache.fineract.client.models.GetSavingsAccountTransactionsPageItem;
import org.apache.fineract.client.models.PagedRequestSavingsTransactionSearch;
import org.apache.fineract.client.models.RangeFilterBigDecimal;
import org.apache.fineract.client.models.RangeFilterLocalDate;
import org.apache.fineract.client.models.SavingsAccountTransactionsSearchResponse;
import org.apache.fineract.client.models.SavingsTransactionSearch;
import org.apache.fineract.client.models.SortOrder;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsProductHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsStatusChecker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings({ "rawtypes" })
public class SavingsAccountTransactionsSearchIntegrationTest {

    public static final String ACCOUNT_TYPE_INDIVIDUAL = "INDIVIDUAL";
    public static final String DEFAULT_DATE_FORMAT = "dd MMM yyyy";
    final String startDate = "01 May 2023";
    final String firstDepositDate = "05 May 2023";
    final String secondDepositDate = "09 May 2023";
    final String thirdDepositDate = "12 May 2023";
    final String fourthDepositDate = "01 Jun 2023";
    final String withdrawDate = "10 May 2023";

    private ResponseSpecification responseSpec;
    private ResponseSpecification responseSpecForValidationError;
    private RequestSpecification requestSpec;
    private SavingsProductHelper savingsProductHelper;
    private SavingsAccountHelper savingsAccountHelper;
    private SavingsAccountHelper savingsAccountHelperValidationError;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.responseSpecForValidationError = new ResponseSpecBuilder().expectStatusCode(400).build();
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.savingsAccountHelperValidationError = new SavingsAccountHelper(this.requestSpec, this.responseSpecForValidationError);
        this.savingsProductHelper = new SavingsProductHelper();
    }

    @Test
    public void testSavingsTransactionsSearchWithAmountFilterLteGte() throws JsonProcessingException {
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, startDate);
        Assertions.assertNotNull(clientID);

        final Integer savingsId = createSavingsAccountDailyPosting(clientID, startDate);

        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "100", startDate, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "300", startDate, CommonConstants.RESPONSE_RESOURCE_ID);

        Filters filters = new Filters();
        filters.addTransactionAmountItem(buildTransactionAmountRange(RangeFilterBigDecimal.OperatorEnum.GTE, BigDecimal.valueOf(100)));
        filters.addTransactionAmountItem(buildTransactionAmountRange(RangeFilterBigDecimal.OperatorEnum.LTE, BigDecimal.valueOf(200)));
        PagedRequestSavingsTransactionSearch searchRequest = buildTransactionsSearchReqeust(filters, null, null, null);
        SavingsAccountTransactionsSearchResponse transactionsResponse = this.savingsAccountHelper.searchTransactions(savingsId,
                searchRequest);
        Assertions.assertNotNull(transactionsResponse);
        assertEquals(1, transactionsResponse.getTotalFilteredRecords());
        Assertions.assertNotNull(transactionsResponse.getPageItems());
        List<GetSavingsAccountTransactionsPageItem> pageItemsList = List.copyOf(transactionsResponse.getPageItems());
        BigDecimal expectedAmount = BigDecimal.valueOf(100);
        assertEquals(0, expectedAmount.compareTo(pageItemsList.get(0).getAmount()));
    }

    @Test
    public void testSavingsTransactionsSearchWithAmountFilterLtGt() throws JsonProcessingException {
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, startDate);
        Assertions.assertNotNull(clientID);

        final Integer savingsId = createSavingsAccountDailyPosting(clientID, startDate);

        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "100", startDate, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "300", startDate, CommonConstants.RESPONSE_RESOURCE_ID);

        Filters filters = new Filters();
        filters.addTransactionAmountItem(buildTransactionAmountRange(RangeFilterBigDecimal.OperatorEnum.GT, BigDecimal.valueOf(100)));
        filters.addTransactionAmountItem(buildTransactionAmountRange(RangeFilterBigDecimal.OperatorEnum.LT, BigDecimal.valueOf(400)));
        PagedRequestSavingsTransactionSearch searchRequest = buildTransactionsSearchReqeust(filters, null, null, null);
        SavingsAccountTransactionsSearchResponse transactionsResponse = this.savingsAccountHelper.searchTransactions(savingsId,
                searchRequest);
        Assertions.assertNotNull(transactionsResponse);
        assertEquals(1, transactionsResponse.getTotalFilteredRecords());
        Assertions.assertNotNull(transactionsResponse.getPageItems());
        List<GetSavingsAccountTransactionsPageItem> pageItemsList = List.copyOf(transactionsResponse.getPageItems());
        BigDecimal expectedAmount = BigDecimal.valueOf(300);
        assertEquals(0, expectedAmount.compareTo(pageItemsList.get(0).getAmount()));
    }

    @Test
    public void testSavingsTransactionsSearchWithDateFilterLteGte() throws JsonProcessingException {
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, startDate);
        Assertions.assertNotNull(clientID);

        final Integer savingsId = createSavingsAccountDailyPosting(clientID, startDate);

        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "100", firstDepositDate, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "300", secondDepositDate, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, "100", withdrawDate, CommonConstants.RESPONSE_RESOURCE_ID);

        Filters filters = new Filters();
        filters.addTransactionDateItem(buildTransactionDateRange(RangeFilterLocalDate.OperatorEnum.GTE, LocalDate.of(2023, 05, 06)));
        filters.addTransactionDateItem(buildTransactionDateRange(RangeFilterLocalDate.OperatorEnum.LTE, LocalDate.of(2023, 05, 10)));
        PagedRequestSavingsTransactionSearch searchRequest = buildTransactionsSearchReqeust(filters, null, null, null);
        SavingsAccountTransactionsSearchResponse transactionsResponse = this.savingsAccountHelper.searchTransactions(savingsId,
                searchRequest);
        Assertions.assertNotNull(transactionsResponse);
        assertEquals(2, transactionsResponse.getTotalFilteredRecords());
        Assertions.assertNotNull(transactionsResponse.getPageItems());
        assertEquals(2, transactionsResponse.getPageItems().size());
        List<GetSavingsAccountTransactionsPageItem> pageItemsList = List.copyOf(transactionsResponse.getPageItems());
        assertEquals(0, parseDate(withdrawDate, DEFAULT_DATE_FORMAT).compareTo(pageItemsList.get(0).getDate()));
        assertEquals(0, parseDate(secondDepositDate, DEFAULT_DATE_FORMAT).compareTo(pageItemsList.get(1).getDate()));
    }

    @Test
    public void testSavingsTransactionsSearchWithTransactionTypeDepositAndDefaultSort() {
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, startDate);
        Assertions.assertNotNull(clientID);

        final Integer savingsId = createSavingsAccountDailyPosting(clientID, startDate);

        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "100", firstDepositDate, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "300", secondDepositDate, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, "100", withdrawDate, CommonConstants.RESPONSE_RESOURCE_ID);

        Filters filters = new Filters();
        filters.setTransactionType(List.of(Filters.TransactionTypeEnum.DEPOSIT));
        PagedRequestSavingsTransactionSearch searchRequest = buildTransactionsSearchReqeust(filters, null, null, null);
        SavingsAccountTransactionsSearchResponse transactionsResponse = this.savingsAccountHelper.searchTransactions(savingsId,
                searchRequest);
        Assertions.assertNotNull(transactionsResponse);
        assertEquals(2, transactionsResponse.getTotalFilteredRecords());
        Assertions.assertNotNull(transactionsResponse.getPageItems());
        assertEquals(2, transactionsResponse.getPageItems().size());
        List<GetSavingsAccountTransactionsPageItem> pageItemsList = List.copyOf(transactionsResponse.getPageItems());
        assertTrue(Filters.TransactionTypeEnum.DEPOSIT.getValue().equalsIgnoreCase(pageItemsList.get(0).getTransactionType().getValue()));
        assertEquals(0, BigDecimal.valueOf(300).compareTo(pageItemsList.get(0).getAmount()));
        assertEquals(0, parseDate(secondDepositDate, DEFAULT_DATE_FORMAT).compareTo(pageItemsList.get(0).getDate()));
        assertTrue(Filters.TransactionTypeEnum.DEPOSIT.getValue().equalsIgnoreCase(pageItemsList.get(1).getTransactionType().getValue()));
        assertEquals(0, BigDecimal.valueOf(100).compareTo(pageItemsList.get(1).getAmount()));
        assertEquals(0, parseDate(firstDepositDate, DEFAULT_DATE_FORMAT).compareTo(pageItemsList.get(1).getDate()));
    }

    @Test
    public void testSavingsTransactionsSearchWithTransactionTypeWithdrawAndDeposit() throws JsonProcessingException {
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, startDate);
        Assertions.assertNotNull(clientID);

        final Integer savingsId = createSavingsAccountDailyPosting(clientID, startDate);

        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "100", firstDepositDate, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "300", secondDepositDate, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, "100", withdrawDate, CommonConstants.RESPONSE_RESOURCE_ID);

        Filters filters = new Filters();
        filters.setTransactionType(List.of(Filters.TransactionTypeEnum.DEPOSIT, Filters.TransactionTypeEnum.WITHDRAWAL));
        PagedRequestSavingsTransactionSearch searchRequest = buildTransactionsSearchReqeust(filters, null, null, null);
        SavingsAccountTransactionsSearchResponse transactionsResponse = this.savingsAccountHelper.searchTransactions(savingsId,
                searchRequest);
        Assertions.assertNotNull(transactionsResponse);
        assertEquals(3, transactionsResponse.getTotalFilteredRecords());
        Assertions.assertNotNull(transactionsResponse.getPageItems());
        assertEquals(3, transactionsResponse.getPageItems().size());
        List<GetSavingsAccountTransactionsPageItem> pageItemsList = List.copyOf(transactionsResponse.getPageItems());
        assertTrue(
                Filters.TransactionTypeEnum.WITHDRAWAL.getValue().equalsIgnoreCase(pageItemsList.get(0).getTransactionType().getValue()));
        assertEquals(0, BigDecimal.valueOf(100).compareTo(pageItemsList.get(0).getAmount()));
        assertTrue(Filters.TransactionTypeEnum.DEPOSIT.getValue().equalsIgnoreCase(pageItemsList.get(1).getTransactionType().getValue()));
        assertEquals(0, BigDecimal.valueOf(300).compareTo(pageItemsList.get(1).getAmount()));
        assertTrue(Filters.TransactionTypeEnum.DEPOSIT.getValue().equalsIgnoreCase(pageItemsList.get(2).getTransactionType().getValue()));
        assertEquals(0, BigDecimal.valueOf(100).compareTo(pageItemsList.get(2).getAmount()));
    }

    @Test
    public void testSavingsTransactionsSearchWithPaginationAndNoFilter() {
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, startDate);
        Assertions.assertNotNull(clientID);
        final Integer savingsId = createSavingsAccountDailyPosting(clientID, startDate);
        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "100", firstDepositDate, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "300", secondDepositDate, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, "100", withdrawDate, CommonConstants.RESPONSE_RESOURCE_ID);
        Filters filters = new Filters();
        int page = 0;
        int size = 2;
        PagedRequestSavingsTransactionSearch searchRequest = buildTransactionsSearchReqeust(filters, page, size, null);
        SavingsAccountTransactionsSearchResponse transactionsResponse = this.savingsAccountHelper.searchTransactions(savingsId,
                searchRequest);
        Assertions.assertNotNull(transactionsResponse);
        assertEquals(3, transactionsResponse.getTotalFilteredRecords());
        Assertions.assertNotNull(transactionsResponse.getPageItems());
        assertEquals(2, transactionsResponse.getPageItems().size());
    }

    @Test
    public void testSavingsTransactionsSearchWithTransactionTypeDepositAndSortByAmountAsc() {
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, startDate);
        Assertions.assertNotNull(clientID);
        final Integer savingsId = createSavingsAccountDailyPosting(clientID, startDate);
        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "100", firstDepositDate, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "300", secondDepositDate, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, "200", withdrawDate, CommonConstants.RESPONSE_RESOURCE_ID);
        Filters filters = new Filters();
        filters.setTransactionType(List.of(Filters.TransactionTypeEnum.DEPOSIT));
        SortOrder sortOrder = new SortOrder();
        sortOrder.setProperty("amount");
        sortOrder.setDirection(SortOrder.DirectionEnum.ASC);
        List<SortOrder> sortOrders = List.of(sortOrder);
        PagedRequestSavingsTransactionSearch searchRequest = buildTransactionsSearchReqeust(filters, null, null, sortOrders);
        SavingsAccountTransactionsSearchResponse transactionsResponse = this.savingsAccountHelper.searchTransactions(savingsId,
                searchRequest);
        Assertions.assertNotNull(transactionsResponse);
        assertEquals(2, transactionsResponse.getTotalFilteredRecords());
        Assertions.assertNotNull(transactionsResponse.getPageItems());
        assertEquals(2, transactionsResponse.getPageItems().size());
        List<GetSavingsAccountTransactionsPageItem> pageItemsList = List.copyOf(transactionsResponse.getPageItems());
        assertTrue(Filters.TransactionTypeEnum.DEPOSIT.getValue().equalsIgnoreCase(pageItemsList.get(0).getTransactionType().getValue()));
        assertEquals(0, BigDecimal.valueOf(100).compareTo(pageItemsList.get(0).getAmount()));
        assertTrue(Filters.TransactionTypeEnum.DEPOSIT.getValue().equalsIgnoreCase(pageItemsList.get(1).getTransactionType().getValue()));
        assertEquals(0, BigDecimal.valueOf(300).compareTo(pageItemsList.get(1).getAmount()));
    }

    @Test
    public void testSavingsTransactionsSearchWithFiltersSortingAndPagination() {
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, startDate);
        Assertions.assertNotNull(clientID);
        final Integer savingsId = createSavingsAccountDailyPosting(clientID, startDate);
        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "100", firstDepositDate, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "300", secondDepositDate, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, "50", withdrawDate, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "400", thirdDepositDate, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "200", fourthDepositDate, CommonConstants.RESPONSE_RESOURCE_ID);

        this.savingsAccountHelper.postInterestForSavings(savingsId);
        Filters filters = new Filters();
        filters.addTransactionAmountItem(buildTransactionAmountRange(RangeFilterBigDecimal.OperatorEnum.GTE, BigDecimal.valueOf(100)));
        filters.addTransactionAmountItem(buildTransactionAmountRange(RangeFilterBigDecimal.OperatorEnum.LT, BigDecimal.valueOf(500)));
        filters.addTransactionDateItem(buildTransactionDateRange(RangeFilterLocalDate.OperatorEnum.GT, LocalDate.of(2023, 05, 06)));
        filters.addTransactionDateItem(buildTransactionDateRange(RangeFilterLocalDate.OperatorEnum.LTE, LocalDate.of(2023, 06, 01)));
        filters.setTransactionType(List.of(Filters.TransactionTypeEnum.DEPOSIT));
        SortOrder sortOrder = new SortOrder();
        sortOrder.setProperty("amount");
        sortOrder.setDirection(SortOrder.DirectionEnum.DESC);
        List<SortOrder> sortOrders = List.of(sortOrder);
        int page = 0;
        int size = 2;
        PagedRequestSavingsTransactionSearch searchRequest = buildTransactionsSearchReqeust(filters, page, size, sortOrders);
        SavingsAccountTransactionsSearchResponse transactionsResponse = this.savingsAccountHelper.searchTransactions(savingsId,
                searchRequest);
        Assertions.assertNotNull(transactionsResponse);
        assertEquals(3, transactionsResponse.getTotalFilteredRecords());
        Assertions.assertNotNull(transactionsResponse.getPageItems());
        assertEquals(2, transactionsResponse.getPageItems().size());
        List<GetSavingsAccountTransactionsPageItem> pageItemsList = List.copyOf(transactionsResponse.getPageItems());
        assertTrue(Filters.TransactionTypeEnum.DEPOSIT.getValue().equalsIgnoreCase(pageItemsList.get(0).getTransactionType().getValue()));
        assertEquals(0, BigDecimal.valueOf(400).compareTo(pageItemsList.get(0).getAmount()));
        assertEquals(0, parseDate(thirdDepositDate, DEFAULT_DATE_FORMAT).compareTo(pageItemsList.get(0).getDate()));
        assertTrue(Filters.TransactionTypeEnum.DEPOSIT.getValue().equalsIgnoreCase(pageItemsList.get(1).getTransactionType().getValue()));
        assertEquals(0, BigDecimal.valueOf(300).compareTo(pageItemsList.get(1).getAmount()));
        assertEquals(0, parseDate(secondDepositDate, DEFAULT_DATE_FORMAT).compareTo(pageItemsList.get(1).getDate()));

    }

    @Test
    public void testSavingsTransactionsSearchFilterRangeValidationError() {
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, startDate);
        Assertions.assertNotNull(clientID);
        final Integer savingsId = createSavingsAccountDailyPosting(clientID, startDate);
        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "100", firstDepositDate, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "300", secondDepositDate, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, "50", withdrawDate, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "400", thirdDepositDate, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "200", fourthDepositDate, CommonConstants.RESPONSE_RESOURCE_ID);

        Filters filters = new Filters();
        filters.addTransactionAmountItem(buildTransactionAmountRange(RangeFilterBigDecimal.OperatorEnum.GTE, BigDecimal.valueOf(100)));
        filters.addTransactionAmountItem(buildTransactionAmountRange(RangeFilterBigDecimal.OperatorEnum.LT, BigDecimal.valueOf(500)));
        filters.addTransactionAmountItem(buildTransactionAmountRange(RangeFilterBigDecimal.OperatorEnum.LT, BigDecimal.valueOf(1000)));
        filters.addTransactionDateItem(buildTransactionDateRange(RangeFilterLocalDate.OperatorEnum.GT, LocalDate.of(2023, 05, 6)));
        filters.addTransactionDateItem(buildTransactionDateRange(RangeFilterLocalDate.OperatorEnum.LTE, LocalDate.of(2023, 06, 1)));
        filters.addTransactionDateItem(buildTransactionDateRange(RangeFilterLocalDate.OperatorEnum.LTE, LocalDate.of(2023, 06, 8)));
        filters.setTransactionType(List.of(Filters.TransactionTypeEnum.DEPOSIT));
        SortOrder sortOrder = new SortOrder();
        sortOrder.setProperty("amount");
        sortOrder.setDirection(SortOrder.DirectionEnum.DESC);
        List<SortOrder> sortOrders = List.of(sortOrder);
        int page = 0;
        int size = 2;
        PagedRequestSavingsTransactionSearch searchRequest = buildTransactionsSearchReqeust(filters, page, size, sortOrders);
        this.savingsAccountHelperValidationError.searchTransactions(savingsId, searchRequest);
    }

    @Test
    public void testSavingsTransactionsSearchTransactionAmountValidationError() {
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, startDate);
        Assertions.assertNotNull(clientID);
        final Integer savingsId = createSavingsAccountDailyPosting(clientID, startDate);
        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "100", firstDepositDate, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "300", secondDepositDate, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, "50", withdrawDate, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "400", thirdDepositDate, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "200", fourthDepositDate, CommonConstants.RESPONSE_RESOURCE_ID);

        Filters filters = new Filters();
        filters.addTransactionAmountItem(buildTransactionAmountRange(RangeFilterBigDecimal.OperatorEnum.GTE, BigDecimal.valueOf(-100)));
        filters.setTransactionType(List.of(Filters.TransactionTypeEnum.DEPOSIT));
        PagedRequestSavingsTransactionSearch searchRequest = buildTransactionsSearchReqeust(filters, null, null, null);
        this.savingsAccountHelperValidationError.searchTransactions(savingsId, searchRequest);
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

    private PagedRequestSavingsTransactionSearch buildTransactionsSearchReqeust(Filters filters, Integer page, Integer size,
            List<SortOrder> sorts) {
        final Integer DEFAULT_PAGE_SIZE = 50;
        SavingsTransactionSearch savingsTransactionSearch = new SavingsTransactionSearch();
        savingsTransactionSearch.setFilters(filters);
        PagedRequestSavingsTransactionSearch pagedRequest = new PagedRequestSavingsTransactionSearch();
        pagedRequest.setRequest(savingsTransactionSearch);
        pagedRequest.setSorts(sorts != null ? sorts : new ArrayList<>());
        pagedRequest.setPage(page != null ? page : 0);
        pagedRequest.setSize(size != null ? size : DEFAULT_PAGE_SIZE);
        return pagedRequest;
    }

    private RangeFilterBigDecimal buildTransactionAmountRange(RangeFilterBigDecimal.OperatorEnum operator, BigDecimal value) {
        RangeFilterBigDecimal transactionAmountFilter = new RangeFilterBigDecimal();
        transactionAmountFilter.setOperator(operator);
        transactionAmountFilter.setValue(value);
        return transactionAmountFilter;
    }

    private RangeFilterLocalDate buildTransactionDateRange(RangeFilterLocalDate.OperatorEnum operator, LocalDate value) {
        RangeFilterLocalDate transactionDateFilter = new RangeFilterLocalDate();
        transactionDateFilter.setOperator(operator);
        transactionDateFilter.setValue(value);
        return transactionDateFilter;
    }

    public static LocalDate parseDate(String dateStr, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return LocalDate.parse(dateStr, formatter);
    }

    // Reset configuration fields
    @AfterEach
    public void tearDown() {
        GlobalConfigurationHelper.resetAllDefaultGlobalConfigurations(this.requestSpec, this.responseSpec);
        GlobalConfigurationHelper.verifyAllDefaultGlobalConfigurations(this.requestSpec, this.responseSpec);
    }

}
