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

import static org.apache.fineract.infrastructure.core.service.DateUtils.parseLocalDate;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.fineract.client.models.GetSavingsAccountTransactionsPageItem;
import org.apache.fineract.client.models.SavingsAccountTransactionsSearchResponse;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsProductHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsStatusChecker;
import org.apache.fineract.portfolio.savings.SavingsAccountTransactionType;
import org.apache.fineract.portfolio.search.data.TransactionSearchRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@SuppressWarnings({ "rawtypes" })
public class SavingsAccountTransactionsSearchIntegrationTest {

    public static final String ACCOUNT_TYPE_INDIVIDUAL = "INDIVIDUAL";
    public static final String DEFAULT_DATE_FORMAT = "dd MMMM yyyy";
    public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
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
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.responseSpecForValidationError = new ResponseSpecBuilder().expectStatusCode(400).build();
        this.savingsAccountHelperValidationError = new SavingsAccountHelper(this.requestSpec, this.responseSpecForValidationError);
        this.savingsProductHelper = new SavingsProductHelper();
    }

    @Test
    public void testSavingsTransactionsSearchAmountFrom() throws JsonProcessingException {
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, startDate);
        Assertions.assertNotNull(clientID);
        final Integer savingsId = createSavingsAccountDailyPosting(clientID, startDate);

        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "100", startDate, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "300", startDate, CommonConstants.RESPONSE_RESOURCE_ID);

        TransactionSearchRequest searchParameters = new TransactionSearchRequest().fromAmount(BigDecimal.valueOf(100));
        Map<String, Object> queryParams = buildTransactionsSearchQuery(searchParameters, null, null);
        SavingsAccountTransactionsSearchResponse transactionsResponse = this.savingsAccountHelper.searchSavingsTransactions(savingsId,
                queryParams);

        Assertions.assertNotNull(transactionsResponse);
        assertEquals(2, transactionsResponse.getTotal());
        Assertions.assertNotNull(transactionsResponse.getContent());
        List<GetSavingsAccountTransactionsPageItem> pageItemsList = List.copyOf(transactionsResponse.getContent());
        assertEquals(2, pageItemsList.size());
        assertTrue(MathUtil.isEqualTo(BigDecimal.valueOf(100), pageItemsList.get(1).getAmount()));
    }

    @Test
    public void testSavingsTransactionsSearchAmountFromTo() throws JsonProcessingException {
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, startDate);
        Assertions.assertNotNull(clientID);
        final Integer savingsId = createSavingsAccountDailyPosting(clientID, startDate);

        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "100", startDate, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "300", startDate, CommonConstants.RESPONSE_RESOURCE_ID);

        TransactionSearchRequest searchParameters = new TransactionSearchRequest().fromAmount(BigDecimal.valueOf(100))
                .toAmount(BigDecimal.valueOf(200));
        Map<String, Object> queryParams = buildTransactionsSearchQuery(searchParameters, null, null);
        SavingsAccountTransactionsSearchResponse transactionsResponse = this.savingsAccountHelper.searchSavingsTransactions(savingsId,
                queryParams);

        Assertions.assertNotNull(transactionsResponse);
        assertEquals(1, transactionsResponse.getTotal());
        Assertions.assertNotNull(transactionsResponse.getContent());
        List<GetSavingsAccountTransactionsPageItem> pageItemsList = List.copyOf(transactionsResponse.getContent());
        assertEquals(1, pageItemsList.size());
        assertTrue(MathUtil.isEqualTo(BigDecimal.valueOf(100), pageItemsList.get(0).getAmount()));
    }

    @Test
    public void testSavingsTransactionsSearchDateFromTo() throws JsonProcessingException {
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, startDate);
        Assertions.assertNotNull(clientID);
        final Integer savingsId = createSavingsAccountDailyPosting(clientID, startDate);

        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "100", firstDepositDate, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "300", secondDepositDate, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, "100", withdrawDate, CommonConstants.RESPONSE_RESOURCE_ID);

        TransactionSearchRequest searchParameters = new TransactionSearchRequest()
                .fromDate(firstDepositDate, DEFAULT_DATE_FORMAT, DEFAULT_LOCALE).toDate(withdrawDate, DEFAULT_DATE_FORMAT, DEFAULT_LOCALE);
        Map<String, Object> queryParams = buildTransactionsSearchQuery(searchParameters, DEFAULT_DATE_FORMAT, DEFAULT_LOCALE);
        SavingsAccountTransactionsSearchResponse transactionsResponse = this.savingsAccountHelper.searchSavingsTransactions(savingsId,
                queryParams);

        Assertions.assertNotNull(transactionsResponse);
        assertEquals(3, transactionsResponse.getTotal());
        Assertions.assertNotNull(transactionsResponse.getContent());
        List<GetSavingsAccountTransactionsPageItem> pageItemsList = List.copyOf(transactionsResponse.getContent());
        assertEquals(3, pageItemsList.size());
        assertEquals(parseLocalDate(withdrawDate, DEFAULT_DATE_FORMAT), pageItemsList.get(0).getDate());
        assertEquals(parseLocalDate(secondDepositDate, DEFAULT_DATE_FORMAT), pageItemsList.get(1).getDate());
    }

    @Test
    public void testSavingsTransactionsSearchSubmittedDateFromTo() throws JsonProcessingException {
        LocalDate businessDate = Utils.getLocalDateOfTenant();
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, startDate);
        Assertions.assertNotNull(clientID);
        final Integer savingsId = createSavingsAccountDailyPosting(clientID, startDate);

        try {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, true);
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, businessDate);

            this.savingsAccountHelper.depositToSavingsAccount(savingsId, "100", firstDepositDate, CommonConstants.RESPONSE_RESOURCE_ID);
            this.savingsAccountHelper.depositToSavingsAccount(savingsId, "300", secondDepositDate, CommonConstants.RESPONSE_RESOURCE_ID);
            this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, "100", withdrawDate, CommonConstants.RESPONSE_RESOURCE_ID);
        } finally {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, false);
        }
        String submittedDate = DateUtils.format(businessDate, DEFAULT_DATE_FORMAT, DEFAULT_LOCALE);
        TransactionSearchRequest searchParameters = new TransactionSearchRequest()
                .fromSubmittedDate(submittedDate, DEFAULT_DATE_FORMAT, DEFAULT_LOCALE)
                .toSubmittedDate(submittedDate, DEFAULT_DATE_FORMAT, DEFAULT_LOCALE);
        Map<String, Object> queryParams = buildTransactionsSearchQuery(searchParameters, DEFAULT_DATE_FORMAT, DEFAULT_LOCALE);
        SavingsAccountTransactionsSearchResponse transactionsResponse = this.savingsAccountHelper.searchSavingsTransactions(savingsId,
                queryParams);

        Assertions.assertNotNull(transactionsResponse);
        assertEquals(3, transactionsResponse.getTotal());
        Assertions.assertNotNull(transactionsResponse.getContent());
        List<GetSavingsAccountTransactionsPageItem> pageItemsList = List.copyOf(transactionsResponse.getContent());
        assertEquals(3, pageItemsList.size());
        assertEquals(businessDate, pageItemsList.get(0).getSubmittedOnDate());
        assertEquals(businessDate, pageItemsList.get(1).getSubmittedOnDate());
        assertEquals(businessDate, pageItemsList.get(2).getSubmittedOnDate());
    }

    @Test
    public void testSavingsTransactionsSearchTransactionTypeDepositAndDefaultSort() {
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, startDate);
        Assertions.assertNotNull(clientID);
        final Integer savingsId = createSavingsAccountDailyPosting(clientID, startDate);

        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "100", firstDepositDate, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "300", secondDepositDate, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, "100", withdrawDate, CommonConstants.RESPONSE_RESOURCE_ID);

        int typeD = SavingsAccountTransactionType.DEPOSIT.getId();
        TransactionSearchRequest searchParameters = new TransactionSearchRequest().types(String.valueOf(typeD));
        Map<String, Object> queryParams = buildTransactionsSearchQuery(searchParameters, DEFAULT_DATE_FORMAT, DEFAULT_LOCALE);
        SavingsAccountTransactionsSearchResponse transactionsResponse = this.savingsAccountHelper.searchSavingsTransactions(savingsId,
                queryParams);

        Assertions.assertNotNull(transactionsResponse);
        assertEquals(2, transactionsResponse.getTotal());
        Assertions.assertNotNull(transactionsResponse.getContent());
        List<GetSavingsAccountTransactionsPageItem> pageItemsList = List.copyOf(transactionsResponse.getContent());
        assertEquals(2, pageItemsList.size());
        GetSavingsAccountTransactionsPageItem first = pageItemsList.get(0);
        assertEquals(Long.valueOf(typeD), first.getTransactionType().getId());
        assertTrue(MathUtil.isEqualTo(BigDecimal.valueOf(300), first.getAmount()));
        assertEquals(parseLocalDate(secondDepositDate, DEFAULT_DATE_FORMAT), first.getDate());
        GetSavingsAccountTransactionsPageItem second = pageItemsList.get(1);
        assertEquals(Long.valueOf(typeD), second.getTransactionType().getId());
        assertTrue(MathUtil.isEqualTo(BigDecimal.valueOf(100), second.getAmount()));
        assertEquals(parseLocalDate(firstDepositDate, DEFAULT_DATE_FORMAT), second.getDate());
    }

    @Test
    public void testSavingsTransactionsSearchTransactionTypesWithdrawAndDeposit() throws JsonProcessingException {
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, startDate);
        Assertions.assertNotNull(clientID);
        final Integer savingsId = createSavingsAccountDailyPosting(clientID, startDate);

        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "100", firstDepositDate, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "300", secondDepositDate, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, "100", withdrawDate, CommonConstants.RESPONSE_RESOURCE_ID);

        int typeD = SavingsAccountTransactionType.DEPOSIT.getId();
        int typeW = SavingsAccountTransactionType.WITHDRAWAL.getId();
        TransactionSearchRequest searchParameters = new TransactionSearchRequest().types(String.valueOf(typeD) + ',' + typeW);
        Map<String, Object> queryParams = buildTransactionsSearchQuery(searchParameters, DEFAULT_DATE_FORMAT, DEFAULT_LOCALE);
        SavingsAccountTransactionsSearchResponse transactionsResponse = this.savingsAccountHelper.searchSavingsTransactions(savingsId,
                queryParams);

        Assertions.assertNotNull(transactionsResponse);
        assertEquals(3, transactionsResponse.getTotal());
        Assertions.assertNotNull(transactionsResponse.getContent());
        List<GetSavingsAccountTransactionsPageItem> pageItemsList = List.copyOf(transactionsResponse.getContent());
        assertEquals(3, pageItemsList.size());
        assertEquals(Long.valueOf(typeW), pageItemsList.get(0).getTransactionType().getId());
        assertTrue(MathUtil.isEqualTo(BigDecimal.valueOf(100), pageItemsList.get(0).getAmount()));
        assertEquals(Long.valueOf(typeD), pageItemsList.get(1).getTransactionType().getId());
        assertTrue(MathUtil.isEqualTo(BigDecimal.valueOf(300), pageItemsList.get(1).getAmount()));
        assertEquals(Long.valueOf(typeD), pageItemsList.get(2).getTransactionType().getId());
        assertTrue(MathUtil.isEqualTo(BigDecimal.valueOf(100), pageItemsList.get(2).getAmount()));
    }

    @Test
    public void testSavingsTransactionsSearchPaginationAndNoFilter() {
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, startDate);
        Assertions.assertNotNull(clientID);
        final Integer savingsId = createSavingsAccountDailyPosting(clientID, startDate);

        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "100", firstDepositDate, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "300", secondDepositDate, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, "100", withdrawDate, CommonConstants.RESPONSE_RESOURCE_ID);

        TransactionSearchRequest searchParameters = new TransactionSearchRequest().pageable(0, 2, null, null);
        Map<String, Object> queryParams = buildTransactionsSearchQuery(searchParameters, null, null);
        SavingsAccountTransactionsSearchResponse transactionsResponse = this.savingsAccountHelper.searchSavingsTransactions(savingsId,
                queryParams);

        Assertions.assertNotNull(transactionsResponse);
        assertEquals(3, transactionsResponse.getTotal());
        Assertions.assertNotNull(transactionsResponse.getContent());
        assertEquals(2, transactionsResponse.getContent().size());
    }

    @Test
    public void testSavingsTransactionsSearchTransactionTypeDepositAndSortByAmountAsc() {
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, startDate);
        Assertions.assertNotNull(clientID);
        final Integer savingsId = createSavingsAccountDailyPosting(clientID, startDate);

        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "100", firstDepositDate, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "300", secondDepositDate, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, "200", withdrawDate, CommonConstants.RESPONSE_RESOURCE_ID);

        int typeD = SavingsAccountTransactionType.DEPOSIT.getId();
        TransactionSearchRequest searchParameters = new TransactionSearchRequest().types(String.valueOf(typeD)).pageable(null, null,
                "amount", Sort.Direction.ASC);
        Map<String, Object> queryParams = buildTransactionsSearchQuery(searchParameters, DEFAULT_DATE_FORMAT, DEFAULT_LOCALE);
        SavingsAccountTransactionsSearchResponse transactionsResponse = this.savingsAccountHelper.searchSavingsTransactions(savingsId,
                queryParams);

        Assertions.assertNotNull(transactionsResponse);
        assertEquals(2, transactionsResponse.getTotal());
        Assertions.assertNotNull(transactionsResponse.getContent());
        List<GetSavingsAccountTransactionsPageItem> pageItemsList = List.copyOf(transactionsResponse.getContent());
        assertEquals(2, pageItemsList.size());
        GetSavingsAccountTransactionsPageItem first = pageItemsList.get(0);
        assertEquals(Long.valueOf(typeD), first.getTransactionType().getId());
        assertTrue(MathUtil.isEqualTo(BigDecimal.valueOf(100), first.getAmount()));
        assertEquals(parseLocalDate(firstDepositDate, DEFAULT_DATE_FORMAT), first.getDate());
        GetSavingsAccountTransactionsPageItem second = pageItemsList.get(1);
        assertEquals(Long.valueOf(typeD), second.getTransactionType().getId());
        assertTrue(MathUtil.isEqualTo(BigDecimal.valueOf(300), second.getAmount()));
        assertEquals(parseLocalDate(secondDepositDate, DEFAULT_DATE_FORMAT), second.getDate());
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

        int typeD = SavingsAccountTransactionType.DEPOSIT.getId();
        TransactionSearchRequest searchParameters = new TransactionSearchRequest().fromAmount(BigDecimal.valueOf(100))
                .toAmount(BigDecimal.valueOf(500)).fromDate("2023-05-06", DateUtils.DEFAULT_DATE_FORMAT, DEFAULT_LOCALE)
                .toDate("2023-06-01", DateUtils.DEFAULT_DATE_FORMAT, DEFAULT_LOCALE).types(String.valueOf(typeD))
                .pageable(0, 2, "amount", Sort.Direction.DESC);
        Map<String, Object> queryParams = buildTransactionsSearchQuery(searchParameters, DateUtils.DEFAULT_DATE_FORMAT, DEFAULT_LOCALE);
        SavingsAccountTransactionsSearchResponse transactionsResponse = this.savingsAccountHelper.searchSavingsTransactions(savingsId,
                queryParams);

        Assertions.assertNotNull(transactionsResponse);
        assertEquals(3, transactionsResponse.getTotal());
        Assertions.assertNotNull(transactionsResponse.getContent());
        List<GetSavingsAccountTransactionsPageItem> pageItemsList = List.copyOf(transactionsResponse.getContent());
        assertEquals(2, pageItemsList.size());
        GetSavingsAccountTransactionsPageItem first = pageItemsList.get(0);
        assertEquals(Long.valueOf(typeD), first.getTransactionType().getId());
        assertTrue(MathUtil.isEqualTo(BigDecimal.valueOf(400), first.getAmount()));
        assertEquals(parseLocalDate(thirdDepositDate, DEFAULT_DATE_FORMAT), first.getDate());
        GetSavingsAccountTransactionsPageItem second = pageItemsList.get(1);
        assertEquals(Long.valueOf(typeD), second.getTransactionType().getId());
        assertTrue(MathUtil.isEqualTo(BigDecimal.valueOf(300), second.getAmount()));
        assertEquals(parseLocalDate(secondDepositDate, DEFAULT_DATE_FORMAT), second.getDate());
    }

    @Test
    public void testSavingsTransactionsSearchDateValidationError() {
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, startDate);
        Assertions.assertNotNull(clientID);
        final Integer savingsId = createSavingsAccountDailyPosting(clientID, startDate);

        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "100", firstDepositDate, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "300", secondDepositDate, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, "50", withdrawDate, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "400", thirdDepositDate, CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "200", fourthDepositDate, CommonConstants.RESPONSE_RESOURCE_ID);

        int typeD = SavingsAccountTransactionType.DEPOSIT.getId();
        TransactionSearchRequest searchParameters = new TransactionSearchRequest().fromAmount(BigDecimal.valueOf(100))
                .toAmount(BigDecimal.valueOf(500));
        Map<String, Object> queryParams = buildTransactionsSearchQuery(searchParameters, null, null);
        queryParams.put("fromDate", "05 May 2023"); // wrong date format
        this.savingsAccountHelperValidationError.searchSavingsTransactions(savingsId, queryParams);
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

        TransactionSearchRequest searchParameters = new TransactionSearchRequest();
        Map<String, Object> queryParams = buildTransactionsSearchQuery(searchParameters, null, null);
        queryParams.put("fromAmount", "test"); // not number
        responseSpecForValidationError.statusCode(404);
        this.savingsAccountHelperValidationError.searchSavingsTransactions(savingsId, queryParams);
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

    private Map<String, Object> buildTransactionsSearchQuery(TransactionSearchRequest searchParams, String dateFormat, Locale locale) {
        HashMap<String, Object> params = new HashMap<>();
        if (searchParams.getFromDate() != null) {
            params.put("fromDate", DateUtils.format(searchParams.getFromDate(), dateFormat, locale));
        }
        if (searchParams.getToDate() != null) {
            params.put("toDate", DateUtils.format(searchParams.getToDate(), dateFormat, locale));
        }
        if (searchParams.getFromSubmittedDate() != null) {
            params.put("fromSubmittedDate", DateUtils.format(searchParams.getFromSubmittedDate(), dateFormat, locale));
        }
        if (searchParams.getToSubmittedDate() != null) {
            params.put("toSubmittedDate", DateUtils.format(searchParams.getToSubmittedDate(), dateFormat, locale));
        }
        if (searchParams.getFromAmount() != null) {
            params.put("fromAmount", searchParams.getFromAmount());
        }
        if (searchParams.getToAmount() != null) {
            params.put("toAmount", searchParams.getToAmount());
        }
        if (searchParams.getTypes() != null) {
            params.put("types", String.join(",", searchParams.getTypes()));
        }
        if (searchParams.getCredit() != null) {
            params.put("credit", searchParams.getCredit());
        }
        if (searchParams.getDebit() != null) {
            params.put("debit", searchParams.getDebit());
        }
        PageRequest pageable = searchParams.getPageable();
        if (pageable != null) {
            params.put("offset", pageable.getPageNumber());
            params.put("limit", pageable.getPageSize());
            Sort sort = pageable.getSort();
            if (sort.isSorted()) {
                List<Sort.Order> orders = sort.toList();
                params.put("sortOrder", orders.get(0).getDirection());
                params.put("orderBy", orders.stream().map(Sort.Order::getProperty).collect(Collectors.joining(",")));
            }
        }
        if (dateFormat != null) {
            params.put("dateFormat", dateFormat);
        }
        if (locale != null) {
            params.put("locale", locale.toString());
        }
        return params;
    }

    // Reset configuration fields
    @AfterEach
    public void tearDown() {
        GlobalConfigurationHelper.resetAllDefaultGlobalConfigurations(this.requestSpec, this.responseSpec);
        GlobalConfigurationHelper.verifyAllDefaultGlobalConfigurations(this.requestSpec, this.responseSpec);
    }

}
