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
package org.apache.fineract.accounting.provisioning.service;

import static org.apache.fineract.query.QAccGlAccount.accGlAccount;
import static org.apache.fineract.query.QMLoanproductProvisioningEntry.mLoanproductProvisioningEntry;
import static org.apache.fineract.query.QMOffice.mOffice;
import static org.apache.fineract.query.QMProductLoan.mProductLoan;
import static org.apache.fineract.query.QMProvisionCategory.mProvisionCategory;
import static org.apache.fineract.query.QMProvisioningCriteria.mProvisioningCriteria;
import static org.apache.fineract.query.QMProvisioningHistory.mProvisioningHistory;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.querydsl.sql.SQLQueryFactory;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.provisioning.data.LoanProductProvisioningEntryData;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.core.service.SearchParameters.SearchParametersBuilder;
import org.apache.fineract.test.querydsl.QuerydslDatabaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestConstructor.AutowireMode;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@TestConstructor(autowireMode = AutowireMode.ALL)
@Import(ProvisioningEntriesReadPlatformServiceImpl.class)
@Transactional
class ProvisioningEntriesDatabaseTest extends QuerydslDatabaseTest {

    private static final long HISTORY = 9101L;
    private static final long OTHER_HISTORY = 9102L;
    private static final long OFFICE = 9201L;
    private static final long OTHER_OFFICE = 9202L;
    private static final long PRODUCT = 9301L;
    private static final long OTHER_PRODUCT = 9302L;
    private static final long CATEGORY = 9401L;
    private static final long OTHER_CATEGORY = 9402L;
    private static final long CRITERIA = 9501L;
    private static final long LIABILITY = 9601L;
    private static final long EXPENSE = 9602L;
    private static final BigDecimal RESERVE = new BigDecimal("13.125000");

    private final ProvisioningEntriesReadPlatformService service;
    private final SQLQueryFactory queryFactory;

    @BeforeEach
    void insertFixtures() {
        queryFactory.insert(mProvisioningHistory).set(mProvisioningHistory.id, HISTORY).execute();
        queryFactory.insert(mProvisioningHistory).set(mProvisioningHistory.id, OTHER_HISTORY).execute();
        for (final long office : List.of(OFFICE, OTHER_OFFICE)) {
            queryFactory.insert(mOffice).set(mOffice.id, office).set(mOffice.name, "Office " + office)
                    .set(mOffice.openingDate, Date.valueOf(LocalDate.of(2025, 1, 1))).execute();
        }
        insertProduct(PRODUCT, "SP01");
        insertProduct(OTHER_PRODUCT, "SP02");
        for (final long category : List.of(CATEGORY, OTHER_CATEGORY)) {
            queryFactory.insert(mProvisionCategory).set(mProvisionCategory.id, category)
                    .set(mProvisionCategory.categoryName, "Category " + category).execute();
        }
        queryFactory.insert(mProvisioningCriteria).set(mProvisioningCriteria.id, CRITERIA)
                .set(mProvisioningCriteria.criteriaName, "Test provisioning criteria").execute();
        insertAccount(LIABILITY, "Provision liability", "SQL016-L", 2);
        insertAccount(EXPENSE, "Provision expense", "SQL016-E", 5);

        insertEntry(9706L, HISTORY, OFFICE, PRODUCT, CATEGORY, 7L, RESERVE, LIABILITY, EXPENSE);
        insertEntry(9705L, HISTORY, OFFICE, PRODUCT, OTHER_CATEGORY, 55L, RESERVE, LIABILITY, EXPENSE);
        insertEntry(9704L, HISTORY, OFFICE, OTHER_PRODUCT, CATEGORY, 44L, RESERVE, LIABILITY, EXPENSE);
        insertEntry(9703L, HISTORY, OTHER_OFFICE, PRODUCT, CATEGORY, 33L, RESERVE, LIABILITY, EXPENSE);
        insertEntry(9702L, OTHER_HISTORY, OFFICE, PRODUCT, CATEGORY, 22L, RESERVE, LIABILITY, EXPENSE);
        insertEntry(9701L, HISTORY, OFFICE, PRODUCT, CATEGORY, null, null, null, null);
    }

    @Test
    void opensTransactionForStandaloneRead() {
        TestTransaction.flagForRollback();
        TestTransaction.end();

        assertDays(filtered(), List.of(), 0);
    }

    @Test
    void ordersByEntryIdAndIgnoresZeroFilters() {
        final List<Long> expected = List.of(0L, 22L, 33L, 44L, 55L, 7L);
        assertDays(SearchParameters.builder(), expected, 6);
        assertDays(SearchParameters.builder().provisioningEntryId(0L).officeId(0L).productId(0L).categoryId(0L), expected, 6);
    }

    @Test
    void appliesEachFilterAndIntersectsCombinedFilters() {
        assertDays(SearchParameters.builder().provisioningEntryId(HISTORY), List.of(0L, 33L, 44L, 55L, 7L), 5);
        assertDays(SearchParameters.builder().officeId(OFFICE), List.of(0L, 22L, 44L, 55L, 7L), 5);
        assertDays(SearchParameters.builder().productId(PRODUCT), List.of(0L, 22L, 33L, 55L, 7L), 5);
        assertDays(SearchParameters.builder().categoryId(CATEGORY), List.of(0L, 22L, 33L, 44L, 7L), 5);
        assertDays(filtered(), List.of(0L, 7L), 2);
        assertDays(filtered().officeId(OTHER_OFFICE).productId(OTHER_PRODUCT).limit(1), List.of(), 0);
    }

    @Test
    void mapsJoinedValuesAndJdbcNullDefaults() {
        final LoanProductProvisioningEntryData missing = expectedEntry().setOverdueInDays(0L).setLiablityAccount(0L).setExpenseAccount(0L);
        final LoanProductProvisioningEntryData populated = expectedEntry().setOverdueInDays(7L).setAmountreserved(RESERVE)
                .setLiablityAccount(LIABILITY).setLiabilityAccountName("Provision liability").setLiabilityAccountCode("SQL016-L")
                .setExpenseAccount(EXPENSE).setExpenseAccountName("Provision expense").setExpenseAccountCode("SQL016-E");

        assertThat(service.retrieveProvisioningEntries(filtered().build()).getPageItems()).containsExactly(missing, populated);
    }

    @Test
    void keepsTheUnpagedTotalAcrossPageBoundaries() {
        assertDays(filtered().limit(1), List.of(0L), 2);
        assertDays(filtered().limit(1).offset(0), List.of(0L), 2);
        assertDays(filtered().limit(1).offset(1), List.of(7L), 2);
        assertDays(filtered().limit(1).offset(2), List.of(), 2);
        assertDays(filtered().limit(1).offset(3), List.of(), 2);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = { 0, -1 })
    void ignoresOffsetWithoutAPositiveLimit(final Integer limit) {
        assertDays(filtered().limit(limit).offset(1), List.of(0L, 7L), 2);
        assertDays(filtered().limit(limit).offset(-1), List.of(0L, 7L), 2);
    }

    @Test
    void rejectsNegativeOffsetWithAPositiveLimit() {
        final SearchParameters params = filtered().limit(1).offset(-1).build();
        assertThrows(DataAccessException.class, () -> service.retrieveProvisioningEntries(params));
    }

    private static SearchParametersBuilder filtered() {
        return SearchParameters.builder().provisioningEntryId(HISTORY).officeId(OFFICE).productId(PRODUCT).categoryId(CATEGORY);
    }

    private static LoanProductProvisioningEntryData expectedEntry() {
        return new LoanProductProvisioningEntryData().setHistoryId(HISTORY).setCriteriaId(CRITERIA).setOfficeId(OFFICE)
                .setOfficeName("Office " + OFFICE).setProductId(PRODUCT).setProductName("Product " + PRODUCT).setCategoryId(CATEGORY)
                .setCategoryName("Category " + CATEGORY).setCurrencyCode("USD");
    }

    private void assertDays(final SearchParametersBuilder filters, final List<Long> expected, final int total) {
        final Page<LoanProductProvisioningEntryData> page = service.retrieveProvisioningEntries(filters.build());
        assertThat(page.getPageItems()).extracting(LoanProductProvisioningEntryData::getOverdueInDays).containsExactlyElementsOf(expected);
        assertThat(page.getTotalFilteredRecords()).isEqualTo(total);
    }

    private void insertProduct(final long id, final String shortName) {
        queryFactory.insert(mProductLoan).set(mProductLoan.id, id).set(mProductLoan.shortName, shortName)
                .set(mProductLoan.name, "Product " + id).set(mProductLoan.currencyCode, "USD").set(mProductLoan.currencyDigits, 2)
                .set(mProductLoan.interestMethodEnum, 0).set(mProductLoan.repayEvery, 1).set(mProductLoan.repaymentPeriodFrequencyEnum, 2)
                .set(mProductLoan.numberOfRepayments, 1).set(mProductLoan.amortizationMethodEnum, 1).set(mProductLoan.accountingType, 1)
                .execute();
    }

    private void insertAccount(final long id, final String name, final String code, final int classification) {
        queryFactory.insert(accGlAccount).set(accGlAccount.id, id).set(accGlAccount.name, name).set(accGlAccount.glCode, code)
                .set(accGlAccount.accountUsage, 1).set(accGlAccount.classificationEnum, classification).set(accGlAccount.disabled, false)
                .set(accGlAccount.manualJournalEntriesAllowed, true).execute();
    }

    private void insertEntry(final long id, final long history, final long office, final long product, final long category,
            final Long overdue, final BigDecimal amount, final Long liability, final Long expense) {
        queryFactory.insert(mLoanproductProvisioningEntry).set(mLoanproductProvisioningEntry.id, id)
                .set(mLoanproductProvisioningEntry.historyId, history).set(mLoanproductProvisioningEntry.criteriaId, CRITERIA)
                .set(mLoanproductProvisioningEntry.officeId, office).set(mLoanproductProvisioningEntry.productId, product)
                .set(mLoanproductProvisioningEntry.categoryId, category).set(mLoanproductProvisioningEntry.currencyCode, "USD")
                .set(mLoanproductProvisioningEntry.overdueInDays, overdue).set(mLoanproductProvisioningEntry.reseveAmount, amount)
                .set(mLoanproductProvisioningEntry.liabilityAccount, liability).set(mLoanproductProvisioningEntry.expenseAccount, expense)
                .execute();
    }
}
