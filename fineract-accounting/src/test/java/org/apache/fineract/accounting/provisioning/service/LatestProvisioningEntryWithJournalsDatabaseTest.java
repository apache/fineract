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

import static org.apache.fineract.query.QMProvisioningHistory.mProvisioningHistory;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import com.querydsl.sql.SQLQueryFactory;
import java.sql.Date;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.provisioning.data.ProvisioningEntryData;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.test.querydsl.QuerydslDatabaseTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.annotation.Import;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestConstructor.AutowireMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@TestConstructor(autowireMode = AutowireMode.ALL)
@Import(ProvisioningEntriesReadPlatformServiceImpl.class)
@MockitoBean(types = { PaginationHelper.class, DatabaseSpecificSQLGenerator.class })
@Transactional
class LatestProvisioningEntryWithJournalsDatabaseTest extends QuerydslDatabaseTest {

    private static final LocalDate POSTING_DATE = LocalDate.of(2025, 6, 15);

    private final ProvisioningEntriesReadPlatformService service;
    private final SQLQueryFactory queryFactory;

    @Test
    void returnsNullWhenHistoryIsEmpty() {
        assertThat(service.retrieveExistingProvisioningIdDateWithJournals()).isNull();
    }

    @Test
    void returnsNullWhenNoRowsHaveJournals() {
        insertEntry(10L, POSTING_DATE, false);
        insertEntry(20L, POSTING_DATE.plusDays(1), null);

        assertThat(service.retrieveExistingProvisioningIdDateWithJournals()).isNull();
    }

    @Test
    void returnsNullWhenTheOnlyPostedDatesAreNull() {
        insertEntry(10L, null, true);
        insertEntry(20L, POSTING_DATE, false);
        insertEntry(30L, POSTING_DATE.plusDays(1), null);

        assertThat(service.retrieveExistingProvisioningIdDateWithJournals()).isNull();
    }

    @Test
    void mapsTheIdDateAndJournalFlagWithOtherFieldsNull() {
        queryFactory.insert(mProvisioningHistory).set(mProvisioningHistory.id, 10L)
                .set(mProvisioningHistory.createdDate, Date.valueOf(POSTING_DATE)).set(mProvisioningHistory.journalEntryCreated, true)
                .set(mProvisioningHistory.createdbyId, 1L).set(mProvisioningHistory.lastmodifiedbyId, 2L).execute();

        final ProvisioningEntryData expected = new ProvisioningEntryData().setId(10L).setCreatedDate(POSTING_DATE).setJournalEntry(true);
        assertThat(service.retrieveExistingProvisioningIdDateWithJournals()).isEqualTo(expected);
    }

    @Test
    void selectsTheMaximumPostedDateAmongMixedRows() {
        insertEntry(10L, POSTING_DATE, true);
        insertEntry(20L, POSTING_DATE.minusDays(1), true);
        insertEntry(30L, POSTING_DATE.minusDays(1), false);
        insertEntry(40L, POSTING_DATE.plusDays(1), false);
        insertEntry(50L, POSTING_DATE.plusDays(2), null);
        insertEntry(60L, null, true);

        final ProvisioningEntryData expected = new ProvisioningEntryData().setId(10L).setCreatedDate(POSTING_DATE).setJournalEntry(true);
        assertThat(service.retrieveExistingProvisioningIdDateWithJournals()).isEqualTo(expected);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(booleans = { true, false })
    void rejectsTwoRowsOnTheMaximumPostedDate(final Boolean secondJournalFlag) {
        insertEntry(10L, POSTING_DATE, true);
        insertEntry(20L, POSTING_DATE, secondJournalFlag);

        final IncorrectResultSizeDataAccessException exception = assertThrowsExactly(IncorrectResultSizeDataAccessException.class,
                service::retrieveExistingProvisioningIdDateWithJournals);

        assertThat(exception.getExpectedSize()).isEqualTo(1);
        assertThat(exception.getActualSize()).isEqualTo(2);
    }

    @Test
    void reportsAllThreeRowsOnTheMaximumPostedDate() {
        insertEntry(10L, POSTING_DATE, true);
        insertEntry(20L, POSTING_DATE, false);
        insertEntry(30L, POSTING_DATE, null);

        final IncorrectResultSizeDataAccessException exception = assertThrowsExactly(IncorrectResultSizeDataAccessException.class,
                service::retrieveExistingProvisioningIdDateWithJournals);

        assertThat(exception.getExpectedSize()).isEqualTo(1);
        assertThat(exception.getActualSize()).isEqualTo(3);
    }

    private void insertEntry(final long id, final LocalDate date, final Boolean journalEntryCreated) {
        queryFactory.insert(mProvisioningHistory).set(mProvisioningHistory.id, id)
                .set(mProvisioningHistory.createdDate, date == null ? null : Date.valueOf(date))
                .set(mProvisioningHistory.journalEntryCreated, journalEntryCreated).execute();
    }
}
