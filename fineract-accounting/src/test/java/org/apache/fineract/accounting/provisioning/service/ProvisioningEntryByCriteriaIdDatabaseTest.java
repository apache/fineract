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
import static org.apache.fineract.query.QMProductLoan.mProductLoan;
import static org.apache.fineract.query.QMProvisioningCriteria.mProvisioningCriteria;
import static org.apache.fineract.query.QMProvisioningHistory.mProvisioningHistory;
import static org.assertj.core.api.Assertions.assertThat;

import com.querydsl.sql.SQLQueryFactory;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.provisioning.data.LoanProductProvisioningEntryData;
import org.apache.fineract.accounting.provisioning.data.ProvisioningEntryData;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.test.querydsl.QuerydslDatabaseTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestConstructor.AutowireMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@RequiredArgsConstructor
@TestConstructor(autowireMode = AutowireMode.ALL)
@TestInstance(Lifecycle.PER_CLASS)
@Import(ProvisioningEntriesReadPlatformServiceImpl.class)
@MockitoBean(types = { PaginationHelper.class, DatabaseSpecificSQLGenerator.class })
class ProvisioningEntryByCriteriaIdDatabaseTest extends QuerydslDatabaseTest {

    private static final long CRITERIA_ID = 101L;
    private static final long EMPTY_CRITERIA_ID = 102L;
    private static final long HISTORY_ID = 201L;
    private static final long SECOND_HISTORY_ID = 202L;
    private static final long PRODUCT_ID = 301L;
    private static final long LIABILITY_ACCOUNT_ID = 401L;
    private static final long EXPENSE_ACCOUNT_ID = 402L;
    private static final long OFFICE_ID = 1L;
    private static final long CATEGORY_ID = 1L;
    private static final String CURRENCY_CODE = "USD";
    private static final LocalDate CREATED_DATE = LocalDate.of(2025, 6, 15);
    private static final BigDecimal RESERVED_AMOUNT = new BigDecimal("500.250000");
    private static final BigDecimal SECOND_RESERVED_AMOUNT = new BigDecimal("750.500000");

    private final ProvisioningEntriesReadPlatformService service;
    private final SQLQueryFactory queryFactory;
    private final PlatformTransactionManager transactionManager;

    @BeforeAll
    void insertFixtures() {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            queryFactory.insert(mProductLoan).set(mProductLoan.id, PRODUCT_ID).set(mProductLoan.shortName, "PRVS")
                    .set(mProductLoan.name, "Provisioning Loan").set(mProductLoan.currencyCode, CURRENCY_CODE)
                    .set(mProductLoan.currencyDigits, 2).set(mProductLoan.interestMethodEnum, 0).set(mProductLoan.repayEvery, 1)
                    .set(mProductLoan.repaymentPeriodFrequencyEnum, 2).set(mProductLoan.numberOfRepayments, 12)
                    .set(mProductLoan.amortizationMethodEnum, 1).set(mProductLoan.accountingType, 1).execute();
            insertAccount(LIABILITY_ACCOUNT_ID, "PRVS-2000", "Provision Liability", 2);
            insertAccount(EXPENSE_ACCOUNT_ID, "PRVS-5000", "Provision Expense", 5);
            insertCriteria(CRITERIA_ID, "Provisioning criterion with entries");
            insertCriteria(EMPTY_CRITERIA_ID, "Provisioning criterion without entries");
            queryFactory.insert(mProvisioningHistory).set(mProvisioningHistory.id, HISTORY_ID)
                    .set(mProvisioningHistory.journalEntryCreated, true).set(mProvisioningHistory.createdbyId, 1L)
                    .set(mProvisioningHistory.createdDate, Date.valueOf(CREATED_DATE)).set(mProvisioningHistory.lastmodifiedbyId, 2L)
                    .set(mProvisioningHistory.lastmodifiedDate, Date.valueOf(CREATED_DATE.plusDays(1))).execute();
            insertChild(HISTORY_ID, 15L, RESERVED_AMOUNT);
        });
    }

    @AfterAll
    void deleteFixtures() {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            queryFactory.delete(mLoanproductProvisioningEntry).where(mLoanproductProvisioningEntry.criteriaId.eq(CRITERIA_ID)).execute();
            queryFactory.delete(mProvisioningHistory).where(mProvisioningHistory.id.eq(HISTORY_ID)).execute();
            queryFactory.delete(mProvisioningCriteria).where(mProvisioningCriteria.id.in(CRITERIA_ID, EMPTY_CRITERIA_ID)).execute();
            queryFactory.delete(mProductLoan).where(mProductLoan.id.eq(PRODUCT_ID)).execute();
            queryFactory.delete(accGlAccount).where(accGlAccount.id.in(LIABILITY_ACCOUNT_ID, EXPENSE_ACCOUNT_ID)).execute();
        });
    }

    @Test
    void mapsHistoryAndChildUsingTheChildHistoryId() {
        final ProvisioningEntryData actual = new TransactionTemplate(transactionManager)
                .execute(status -> service.retrieveProvisioningEntryDataByCriteriaId(CRITERIA_ID));

        assertThat(actual).isEqualTo(new ProvisioningEntryData().setId(HISTORY_ID).setJournalEntry(true).setCreatedById(1L)
                .setCreatedUser("mifos").setCreatedDate(CREATED_DATE).setModifiedById(2L).setModifiedUser("system")
                .setProvisioningEntries(List.of(expectedChild(HISTORY_ID, 15L, RESERVED_AMOUNT))));
    }

    @Test
    @Transactional
    void returnsNullForACriterionWithoutChildrenEvenWhenItsIdMatchesAHistoryId() {
        insertHistoryWithoutAuditFields(EMPTY_CRITERIA_ID);

        assertThat(service.retrieveProvisioningEntryDataByCriteriaId(EMPTY_CRITERIA_ID)).isNull();
    }

    @Test
    @Transactional
    void retainsHistoryWithMissingAuditFieldsAndUsesJdbcDefaults() {
        insertHistoryWithoutAuditFields(SECOND_HISTORY_ID);
        queryFactory.delete(mLoanproductProvisioningEntry)
                .where(mLoanproductProvisioningEntry.criteriaId.eq(CRITERIA_ID), mLoanproductProvisioningEntry.historyId.eq(HISTORY_ID))
                .execute();
        insertChild(SECOND_HISTORY_ID, 30L, SECOND_RESERVED_AMOUNT);

        assertThat(service.retrieveProvisioningEntryDataByCriteriaId(CRITERIA_ID)).isEqualTo(
                new ProvisioningEntryData().setId(SECOND_HISTORY_ID).setJournalEntry(false).setCreatedById(0L).setModifiedById(0L)
                        .setProvisioningEntries(List.of(expectedChild(SECOND_HISTORY_ID, 30L, SECOND_RESERVED_AMOUNT))));
    }

    @Test
    @Transactional
    void returnsAllChildrenAndUsesTheHistoryOfTheFirstReturnedChild() {
        insertHistoryWithoutAuditFields(SECOND_HISTORY_ID);
        insertChild(SECOND_HISTORY_ID, 30L, SECOND_RESERVED_AMOUNT);

        final ProvisioningEntryData actual = service.retrieveProvisioningEntryDataByCriteriaId(CRITERIA_ID);

        assertThat(actual).isNotNull();
        assertThat(actual.getProvisioningEntries()).containsExactlyInAnyOrder(expectedChild(HISTORY_ID, 15L, RESERVED_AMOUNT),
                expectedChild(SECOND_HISTORY_ID, 30L, SECOND_RESERVED_AMOUNT));
        assertThat(actual.getId()).isEqualTo(actual.getProvisioningEntries().iterator().next().getHistoryId());
    }

    private void insertCriteria(final long id, final String name) {
        queryFactory.insert(mProvisioningCriteria).set(mProvisioningCriteria.id, id).set(mProvisioningCriteria.criteriaName, name)
                .execute();
    }

    private void insertAccount(final long id, final String code, final String name, final int classification) {
        queryFactory.insert(accGlAccount).set(accGlAccount.id, id).set(accGlAccount.glCode, code).set(accGlAccount.name, name)
                .set(accGlAccount.classificationEnum, classification).execute();
    }

    private void insertHistoryWithoutAuditFields(final long id) {
        queryFactory.insert(mProvisioningHistory).set(mProvisioningHistory.id, id).setNull(mProvisioningHistory.journalEntryCreated)
                .execute();
    }

    private void insertChild(final long historyId, final long overdueDays, final BigDecimal amount) {
        queryFactory.insert(mLoanproductProvisioningEntry).set(mLoanproductProvisioningEntry.criteriaId, CRITERIA_ID)
                .set(mLoanproductProvisioningEntry.historyId, historyId).set(mLoanproductProvisioningEntry.currencyCode, CURRENCY_CODE)
                .set(mLoanproductProvisioningEntry.officeId, OFFICE_ID).set(mLoanproductProvisioningEntry.productId, PRODUCT_ID)
                .set(mLoanproductProvisioningEntry.categoryId, CATEGORY_ID).set(mLoanproductProvisioningEntry.overdueInDays, overdueDays)
                .set(mLoanproductProvisioningEntry.reseveAmount, amount)
                .set(mLoanproductProvisioningEntry.liabilityAccount, LIABILITY_ACCOUNT_ID)
                .set(mLoanproductProvisioningEntry.expenseAccount, EXPENSE_ACCOUNT_ID).execute();
    }

    private static LoanProductProvisioningEntryData expectedChild(final long historyId, final long overdueDays, final BigDecimal amount) {
        return new LoanProductProvisioningEntryData().setCriteriaId(CRITERIA_ID).setHistoryId(historyId).setCurrencyCode(CURRENCY_CODE)
                .setOfficeId(OFFICE_ID).setOfficeName("Head Office").setProductId(PRODUCT_ID).setProductName("Provisioning Loan")
                .setCategoryId(CATEGORY_ID).setCategoryName("STANDARD").setOverdueInDays(overdueDays).setAmountreserved(amount)
                .setLiablityAccount(LIABILITY_ACCOUNT_ID).setLiabilityAccountCode("PRVS-2000")
                .setLiabilityAccountName("Provision Liability").setExpenseAccount(EXPENSE_ACCOUNT_ID).setExpenseAccountCode("PRVS-5000")
                .setExpenseAccountName("Provision Expense");
    }
}
