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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.apache.fineract.client.feign.services.SavingsAccountTransactionsApi.SearchSavingsAccountTransactionsQueryParams;
import org.apache.fineract.client.models.GetSavingsAccountTransactionsPageItem;
import org.apache.fineract.client.models.PostSavingsProductsResponse;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.client.models.SavingsAccountTransactionsSearchResponse;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.integrationtests.client.feign.FeignSavingsTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestValidators;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.portfolio.savings.SavingsAccountTransactionType;
import org.apache.fineract.portfolio.search.data.TransactionSearchRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public class SavingsAccountTransactionsSearchIntegrationTest extends FeignSavingsTestBase {

    private static final String DEFAULT_DATE_FORMAT = SavingsTestData.DATETIME_PATTERN;
    private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
    private static final int BAD_REQUEST = 400;
    private static final int NOT_FOUND = 404;

    private final String startDate = "01 May 2023";
    private final String firstDepositDate = "05 May 2023";
    private final String secondDepositDate = "09 May 2023";
    private final String thirdDepositDate = "12 May 2023";
    private final String fourthDepositDate = "01 Jun 2023";
    private final String withdrawDate = "10 May 2023";

    @Test
    public void testSavingsTransactionsSearchAmountFrom() {
        Long savingsId = createClientWithSavingsAccount();

        deposit(savingsId, "100", startDate);
        deposit(savingsId, "300", startDate);

        TransactionSearchRequest searchParameters = new TransactionSearchRequest().fromAmount(BigDecimal.valueOf(100));
        SearchSavingsAccountTransactionsQueryParams queryParams = buildTransactionsSearchQuery(searchParameters, null, null);
        SavingsAccountTransactionsSearchResponse transactionsResponse = savingsTransactionHelper.searchTransactions(savingsId, queryParams);

        assertNotNull(transactionsResponse);
        assertEquals(2, transactionsResponse.getTotal());
        assertNotNull(transactionsResponse.getContent());
        List<GetSavingsAccountTransactionsPageItem> pageItemsList = List.copyOf(transactionsResponse.getContent());
        assertEquals(2, pageItemsList.size());
        assertTrue(MathUtil.isEqualTo(BigDecimal.valueOf(100), pageItemsList.get(1).getAmount()));
    }

    @Test
    public void testSavingsTransactionsSearchAmountFromTo() {
        Long savingsId = createClientWithSavingsAccount();

        deposit(savingsId, "100", startDate);
        deposit(savingsId, "300", startDate);

        TransactionSearchRequest searchParameters = new TransactionSearchRequest().fromAmount(BigDecimal.valueOf(100))
                .toAmount(BigDecimal.valueOf(200));
        SearchSavingsAccountTransactionsQueryParams queryParams = buildTransactionsSearchQuery(searchParameters, null, null);
        SavingsAccountTransactionsSearchResponse transactionsResponse = savingsTransactionHelper.searchTransactions(savingsId, queryParams);

        assertNotNull(transactionsResponse);
        assertEquals(1, transactionsResponse.getTotal());
        assertNotNull(transactionsResponse.getContent());
        List<GetSavingsAccountTransactionsPageItem> pageItemsList = List.copyOf(transactionsResponse.getContent());
        assertEquals(1, pageItemsList.size());
        assertTrue(MathUtil.isEqualTo(BigDecimal.valueOf(100), pageItemsList.get(0).getAmount()));
    }

    @Test
    public void testSavingsTransactionsSearchDateFromTo() {
        Long savingsId = createClientWithSavingsAccount();

        deposit(savingsId, "100", firstDepositDate);
        deposit(savingsId, "300", secondDepositDate);
        withdraw(savingsId, "100", withdrawDate);

        TransactionSearchRequest searchParameters = new TransactionSearchRequest()
                .fromDate(firstDepositDate, DEFAULT_DATE_FORMAT, DEFAULT_LOCALE).toDate(withdrawDate, DEFAULT_DATE_FORMAT, DEFAULT_LOCALE);
        SearchSavingsAccountTransactionsQueryParams queryParams = buildTransactionsSearchQuery(searchParameters, DEFAULT_DATE_FORMAT,
                DEFAULT_LOCALE);
        SavingsAccountTransactionsSearchResponse transactionsResponse = savingsTransactionHelper.searchTransactions(savingsId, queryParams);

        assertNotNull(transactionsResponse);
        assertEquals(3, transactionsResponse.getTotal());
        assertNotNull(transactionsResponse.getContent());
        List<GetSavingsAccountTransactionsPageItem> pageItemsList = List.copyOf(transactionsResponse.getContent());
        assertEquals(3, pageItemsList.size());
        assertEquals(parseLocalDate(withdrawDate, DEFAULT_DATE_FORMAT), pageItemsList.get(0).getDate());
        assertEquals(parseLocalDate(secondDepositDate, DEFAULT_DATE_FORMAT), pageItemsList.get(1).getDate());
    }

    @Test
    public void testSavingsTransactionsSearchSubmittedDateFromTo() {
        LocalDate businessDate = Utils.getLocalDateOfTenant();
        Long savingsId = createClientWithSavingsAccount();

        try {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            businessDateHelper.updateBusinessDate(BusinessDateType.BUSINESS_DATE.name(), businessDate.toString());

            deposit(savingsId, "100", firstDepositDate);
            deposit(savingsId, "300", secondDepositDate);
            withdraw(savingsId, "100", withdrawDate);
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }

        String submittedDate = DateUtils.format(businessDate, DEFAULT_DATE_FORMAT, DEFAULT_LOCALE);
        TransactionSearchRequest searchParameters = new TransactionSearchRequest()
                .fromSubmittedDate(submittedDate, DEFAULT_DATE_FORMAT, DEFAULT_LOCALE)
                .toSubmittedDate(submittedDate, DEFAULT_DATE_FORMAT, DEFAULT_LOCALE);
        SearchSavingsAccountTransactionsQueryParams queryParams = buildTransactionsSearchQuery(searchParameters, DEFAULT_DATE_FORMAT,
                DEFAULT_LOCALE);
        SavingsAccountTransactionsSearchResponse transactionsResponse = savingsTransactionHelper.searchTransactions(savingsId, queryParams);

        assertNotNull(transactionsResponse);
        assertEquals(3, transactionsResponse.getTotal());
        assertNotNull(transactionsResponse.getContent());
        List<GetSavingsAccountTransactionsPageItem> pageItemsList = List.copyOf(transactionsResponse.getContent());
        assertEquals(3, pageItemsList.size());
        assertEquals(businessDate, pageItemsList.get(0).getSubmittedOnDate());
        assertEquals(businessDate, pageItemsList.get(1).getSubmittedOnDate());
        assertEquals(businessDate, pageItemsList.get(2).getSubmittedOnDate());
    }

    @Test
    public void testSavingsTransactionsSearchTransactionTypeDepositAndDefaultSort() {
        Long savingsId = createClientWithSavingsAccount();

        deposit(savingsId, "100", firstDepositDate);
        deposit(savingsId, "300", secondDepositDate);
        withdraw(savingsId, "100", withdrawDate);

        int typeD = SavingsAccountTransactionType.DEPOSIT.getId();
        TransactionSearchRequest searchParameters = new TransactionSearchRequest().types(String.valueOf(typeD));
        SearchSavingsAccountTransactionsQueryParams queryParams = buildTransactionsSearchQuery(searchParameters, DEFAULT_DATE_FORMAT,
                DEFAULT_LOCALE);
        SavingsAccountTransactionsSearchResponse transactionsResponse = savingsTransactionHelper.searchTransactions(savingsId, queryParams);

        assertNotNull(transactionsResponse);
        assertEquals(2, transactionsResponse.getTotal());
        assertNotNull(transactionsResponse.getContent());
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
    public void testSavingsTransactionsSearchTransactionTypesWithdrawAndDeposit() {
        Long savingsId = createClientWithSavingsAccount();

        deposit(savingsId, "100", firstDepositDate);
        deposit(savingsId, "300", secondDepositDate);
        withdraw(savingsId, "100", withdrawDate);

        int typeD = SavingsAccountTransactionType.DEPOSIT.getId();
        int typeW = SavingsAccountTransactionType.WITHDRAWAL.getId();
        TransactionSearchRequest searchParameters = new TransactionSearchRequest().types(String.valueOf(typeD) + ',' + typeW);
        SearchSavingsAccountTransactionsQueryParams queryParams = buildTransactionsSearchQuery(searchParameters, DEFAULT_DATE_FORMAT,
                DEFAULT_LOCALE);
        SavingsAccountTransactionsSearchResponse transactionsResponse = savingsTransactionHelper.searchTransactions(savingsId, queryParams);

        assertNotNull(transactionsResponse);
        assertEquals(3, transactionsResponse.getTotal());
        assertNotNull(transactionsResponse.getContent());
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
        Long savingsId = createClientWithSavingsAccount();

        deposit(savingsId, "100", firstDepositDate);
        deposit(savingsId, "300", secondDepositDate);
        withdraw(savingsId, "100", withdrawDate);

        TransactionSearchRequest searchParameters = new TransactionSearchRequest().pageable(0, 2, null, null);
        SearchSavingsAccountTransactionsQueryParams queryParams = buildTransactionsSearchQuery(searchParameters, null, null);
        SavingsAccountTransactionsSearchResponse transactionsResponse = savingsTransactionHelper.searchTransactions(savingsId, queryParams);

        assertNotNull(transactionsResponse);
        assertEquals(3, transactionsResponse.getTotal());
        assertNotNull(transactionsResponse.getContent());
        assertEquals(2, transactionsResponse.getContent().size());
    }

    @Test
    public void testSavingsTransactionsSearchTransactionTypeDepositAndSortByAmountAsc() {
        Long savingsId = createClientWithSavingsAccount();

        deposit(savingsId, "100", firstDepositDate);
        deposit(savingsId, "300", secondDepositDate);
        withdraw(savingsId, "200", withdrawDate);

        int typeD = SavingsAccountTransactionType.DEPOSIT.getId();
        TransactionSearchRequest searchParameters = new TransactionSearchRequest().types(String.valueOf(typeD)).pageable(null, null,
                "amount", Sort.Direction.ASC);
        SearchSavingsAccountTransactionsQueryParams queryParams = buildTransactionsSearchQuery(searchParameters, DEFAULT_DATE_FORMAT,
                DEFAULT_LOCALE);
        SavingsAccountTransactionsSearchResponse transactionsResponse = savingsTransactionHelper.searchTransactions(savingsId, queryParams);

        assertNotNull(transactionsResponse);
        assertEquals(2, transactionsResponse.getTotal());
        assertNotNull(transactionsResponse.getContent());
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
        Long savingsId = createClientWithSavingsAccount();

        deposit(savingsId, "100", firstDepositDate);
        deposit(savingsId, "300", secondDepositDate);
        withdraw(savingsId, "50", withdrawDate);
        deposit(savingsId, "400", thirdDepositDate);
        deposit(savingsId, "200", fourthDepositDate);
        savingsHelper.postInterest(savingsId);

        int typeD = SavingsAccountTransactionType.DEPOSIT.getId();
        TransactionSearchRequest searchParameters = new TransactionSearchRequest().fromAmount(BigDecimal.valueOf(100))
                .toAmount(BigDecimal.valueOf(500)).fromDate("2023-05-06", DateUtils.DEFAULT_DATE_FORMAT, DEFAULT_LOCALE)
                .toDate("2023-06-01", DateUtils.DEFAULT_DATE_FORMAT, DEFAULT_LOCALE).types(String.valueOf(typeD))
                .pageable(0, 2, "amount", Sort.Direction.DESC);
        SearchSavingsAccountTransactionsQueryParams queryParams = buildTransactionsSearchQuery(searchParameters,
                DateUtils.DEFAULT_DATE_FORMAT, DEFAULT_LOCALE);
        SavingsAccountTransactionsSearchResponse transactionsResponse = savingsTransactionHelper.searchTransactions(savingsId, queryParams);

        assertNotNull(transactionsResponse);
        assertEquals(3, transactionsResponse.getTotal());
        assertNotNull(transactionsResponse.getContent());
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
        Long savingsId = createClientWithSavingsAccount();

        deposit(savingsId, "100", firstDepositDate);
        deposit(savingsId, "300", secondDepositDate);
        withdraw(savingsId, "50", withdrawDate);
        deposit(savingsId, "400", thirdDepositDate);
        deposit(savingsId, "200", fourthDepositDate);

        TransactionSearchRequest searchParameters = new TransactionSearchRequest().fromAmount(BigDecimal.valueOf(100))
                .toAmount(BigDecimal.valueOf(500));
        SearchSavingsAccountTransactionsQueryParams queryParams = buildTransactionsSearchQuery(searchParameters, null, null);
        queryParams.put("fromDate", firstDepositDate); // wrong date format, no dateFormat parameter was sent

        assertEquals(BAD_REQUEST, savingsTransactionHelper.searchTransactionsExpectingErrorStatus(savingsId, queryParams));
    }

    @Test
    public void testSavingsTransactionsSearchTransactionAmountValidationError() {
        Long savingsId = createClientWithSavingsAccount();

        deposit(savingsId, "100", firstDepositDate);
        deposit(savingsId, "300", secondDepositDate);
        withdraw(savingsId, "50", withdrawDate);
        deposit(savingsId, "400", thirdDepositDate);
        deposit(savingsId, "200", fourthDepositDate);

        SearchSavingsAccountTransactionsQueryParams queryParams = buildTransactionsSearchQuery(new TransactionSearchRequest(), null, null);
        queryParams.put("fromAmount", "test"); // not a number

        assertEquals(NOT_FOUND, savingsTransactionHelper.searchTransactionsExpectingErrorStatus(savingsId, queryParams));
    }

    private Long createClientWithSavingsAccount() {
        Long clientId = createClient(startDate);
        assertNotNull(clientId);
        return createSavingsAccountDailyPosting(clientId, startDate);
    }

    private Long createSavingsAccountDailyPosting(final Long clientId, final String startDate) {
        PostSavingsProductsResponse savingsProduct = createSavingsProduct(
                SavingsRequestBuilders.savingsProduct(SavingsTestData.InterestCompoundingPeriodType.DAILY,
                        SavingsTestData.InterestPostingPeriodType.DAILY, SavingsTestData.InterestCalculationType.DAILY_BALANCE));
        assertNotNull(savingsProduct.getResourceId());

        Long savingsId = submitSavingsApplication(clientId, savingsProduct.getResourceId(), startDate).getSavingsId();
        assertNotNull(savingsId);

        approveSavings(savingsId, startDate);
        SavingsTestValidators.verifySavingsIsApproved(savingsHelper.getSavingsStatus(savingsId));

        activateSavings(savingsId, startDate);
        SavingsTestValidators.verifySavingsIsActive(savingsHelper.getSavingsStatus(savingsId));
        return savingsId;
    }

    private SearchSavingsAccountTransactionsQueryParams buildTransactionsSearchQuery(TransactionSearchRequest searchParams,
            String dateFormat, Locale locale) {
        SearchSavingsAccountTransactionsQueryParams params = new SearchSavingsAccountTransactionsQueryParams();
        if (searchParams.getFromDate() != null) {
            params.fromDate(DateUtils.format(searchParams.getFromDate(), dateFormat, locale));
        }
        if (searchParams.getToDate() != null) {
            params.toDate(DateUtils.format(searchParams.getToDate(), dateFormat, locale));
        }
        if (searchParams.getFromSubmittedDate() != null) {
            params.fromSubmittedDate(DateUtils.format(searchParams.getFromSubmittedDate(), dateFormat, locale));
        }
        if (searchParams.getToSubmittedDate() != null) {
            params.toSubmittedDate(DateUtils.format(searchParams.getToSubmittedDate(), dateFormat, locale));
        }
        if (searchParams.getFromAmount() != null) {
            params.fromAmount(searchParams.getFromAmount());
        }
        if (searchParams.getToAmount() != null) {
            params.toAmount(searchParams.getToAmount());
        }
        if (searchParams.getTypes() != null) {
            params.types(String.join(",", searchParams.getTypes()));
        }
        if (searchParams.getCredit() != null) {
            params.credit(searchParams.getCredit());
        }
        if (searchParams.getDebit() != null) {
            params.debit(searchParams.getDebit());
        }
        PageRequest pageable = searchParams.getPageable();
        if (pageable != null) {
            params.offset(pageable.getPageNumber()).limit(pageable.getPageSize());
            Sort sort = pageable.getSort();
            if (sort.isSorted()) {
                List<Sort.Order> orders = sort.toList();
                params.sortOrder(orders.get(0).getDirection().name())
                        .orderBy(orders.stream().map(Sort.Order::getProperty).collect(Collectors.joining(",")));
            }
        }
        if (dateFormat != null) {
            params.dateFormat(dateFormat);
        }
        if (locale != null) {
            params.locale(locale.toString());
        }
        return params;
    }

    @AfterEach
    public void tearDown() {
        globalConfigurationHelper.resetAllDefaultGlobalConfigurations();
        globalConfigurationHelper.verifyAllDefaultGlobalConfigurations();
    }
}
