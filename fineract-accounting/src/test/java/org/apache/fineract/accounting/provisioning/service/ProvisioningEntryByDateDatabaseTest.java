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
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.querydsl.sql.SQLQueryFactory;
import java.sql.Date;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.provisioning.data.ProvisioningEntryData;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.test.querydsl.QuerydslDatabaseTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.annotation.Import;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
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
class ProvisioningEntryByDateDatabaseTest extends QuerydslDatabaseTest {

    private final ProvisioningEntriesReadPlatformService service;
    private final SQLQueryFactory queryFactory;
    private final PlatformTransactionManager transactionManager;

    @BeforeAll
    void insertFixtures() {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            insertEntry(10, LocalDate.of(2025, 6, 15), 1L, 2L, true);
            insertEntry(20, LocalDate.of(2025, 7, 15), 1L, 2L, true);
        });
    }

    @AfterAll
    void deleteFixtures() {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            queryFactory.delete(mProvisioningHistory).where(mProvisioningHistory.id.in(10L, 20L)).execute();
        });
    }

    @Test
    void mapsTheEntryForTheRequestedDate() {
        final ProvisioningEntryData expected = new ProvisioningEntryData().setId(10L).setJournalEntry(true)
                .setCreatedDate(LocalDate.of(2025, 6, 15)).setCreatedById(1L).setCreatedUser("mifos").setModifiedById(2L)
                .setModifiedUser("system");

        assertThat(service.retrieveProvisioningEntryData("2025-06-15")).isEqualTo(expected);
    }

    @Test
    void findsOneEntryByMonthPrefix() {
        assertThat(service.retrieveProvisioningEntryData("2025-07")).extracting(ProvisioningEntryData::getId).isEqualTo(20L);
    }

    @Test
    void returnsNullWhenNoDateMatches() {
        assertThat(service.retrieveProvisioningEntryData("2025-08")).isNull();
    }

    @Transactional
    @ParameterizedTest
    @ValueSource(strings = { "2025", "2025-0_-15", "2025%-15", "" })
    void rejectsAPatternMatchingMultipleDatedEntries(final String pattern) {
        insertEntry(30, null, 1L, 2L, true);

        final IncorrectResultSizeDataAccessException exception = assertThrows(IncorrectResultSizeDataAccessException.class,
                () -> service.retrieveProvisioningEntryData(pattern));

        assertThat(exception.getActualSize()).isEqualTo(2);
    }

    @Test
    @Transactional
    void keepsEntriesWithMissingAuditUsersAndMapsJdbcDefaults() {
        final LocalDate createdDate = LocalDate.of(2025, 8, 15);
        final LocalDate modifiedDate = LocalDate.of(2025, 9, 15);
        insertEntry(30, createdDate, null, 2L, null);
        insertEntry(40, modifiedDate, 1L, null, true);

        assertThat(service.retrieveProvisioningEntryData("2025-08")).isEqualTo(new ProvisioningEntryData().setId(30L).setJournalEntry(false)
                .setCreatedDate(createdDate).setCreatedById(0L).setModifiedById(2L).setModifiedUser("system"));
        assertThat(service.retrieveProvisioningEntryData("2025-09")).isEqualTo(new ProvisioningEntryData().setId(40L).setJournalEntry(true)
                .setCreatedDate(modifiedDate).setCreatedById(1L).setCreatedUser("mifos").setModifiedById(0L));
    }

    @Test
    void returnsNullForANullDatePrefix() {
        assertThat(service.retrieveProvisioningEntryData((String) null)).isNull();
    }

    @Test
    void matchesTheEscapedDateSeparator() {
        assertThat(service.retrieveProvisioningEntryData("2025\\-06")).extracting(ProvisioningEntryData::getId).isEqualTo(10L);
    }

    @ParameterizedTest
    @ValueSource(strings = { "2025\\_%", "2025-06\\" })
    void treatsEscapedWildcardsAsLiteralCharacters(final String prefix) {
        assertThat(service.retrieveProvisioningEntryData(prefix)).isNull();
    }

    private void insertEntry(final long id, final LocalDate createdDate, final Long createdBy, final Long modifiedBy,
            final Boolean journalEntryCreated) {
        queryFactory.insert(mProvisioningHistory).set(mProvisioningHistory.id, id)
                .set(mProvisioningHistory.journalEntryCreated, journalEntryCreated)
                .set(mProvisioningHistory.createdDate, createdDate == null ? null : Date.valueOf(createdDate))
                .set(mProvisioningHistory.createdbyId, createdBy).set(mProvisioningHistory.lastmodifiedbyId, modifiedBy).execute();
    }
}
