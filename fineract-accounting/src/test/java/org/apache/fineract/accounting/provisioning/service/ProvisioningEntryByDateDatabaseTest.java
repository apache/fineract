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

import static org.apache.fineract.query.QMAppuser.mAppuser;
import static org.apache.fineract.query.QMProvisioningHistory.mProvisioningHistory;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.querydsl.core.types.dsl.DateExpression;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Objects;
import org.apache.fineract.accounting.provisioning.data.ProvisioningEntryData;
import org.apache.fineract.query.QMAppuser;
import org.apache.fineract.query.QMProvisioningHistory;
import org.apache.fineract.test.database.DatabaseTables;
import org.apache.fineract.test.database.JdbcTestcontainersSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.TransactionTemplate;

@DatabaseTables({ QMProvisioningHistory.class, QMAppuser.class })
class ProvisioningEntryByDateDatabaseTest extends JdbcTestcontainersSupport {

    private ProvisioningEntriesReadPlatformServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ProvisioningEntriesReadPlatformServiceImpl(jdbc, null, null, queryFactory);
        insertUser(1, "mifos");
        insertUser(2, "system");
        insertEntry(10, LocalDate.of(2025, 6, 15), 1L, 2L, true);
        insertEntry(20, LocalDate.of(2025, 7, 15), 1L, 2L, true);
    }

    @Test
    void mapsTheEntryForTheRequestedDate() {
        final ProvisioningEntryData expected = new ProvisioningEntryData().setId(10L).setJournalEntry(true)
                .setCreatedDate(LocalDate.of(2025, 6, 15)).setCreatedById(1L).setCreatedUser("mifos").setModifiedById(2L)
                .setModifiedUser("system");

        assertThat(service.retrieveProvisioningEntryData("2025-06-16")).isEqualTo(expected);
    }

    @Test
    void findsOneEntryByMonthPrefix() {
        assertThat(service.retrieveProvisioningEntryData("2025-07")).extracting(ProvisioningEntryData::getId).isEqualTo(20L);
    }

    @Test
    void returnsNullWhenNoDateMatches() {
        assertThat(service.retrieveProvisioningEntryData("2025-08")).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = { "2025", "2025-0_-15", "2025%-15", "" })
    void rejectsAPatternMatchingMultipleDatedEntries(final String pattern) {
        insertEntry(30, null, 1L, 2L, true);

        final IncorrectResultSizeDataAccessException exception = assertThrows(IncorrectResultSizeDataAccessException.class,
                () -> service.retrieveProvisioningEntryData(pattern));

        assertThat(exception.getActualSize()).isEqualTo(2);
    }

    @Test
    void catchingMultipleMatchesKeepsTheCallerTransactionCommittable() {
        final DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(
                Objects.requireNonNull(jdbc.getDataSource()));
        final TransactionInterceptor interceptor = new TransactionInterceptor();
        interceptor.setTransactionManager(transactionManager);
        interceptor.setTransactionAttributeSource(new AnnotationTransactionAttributeSource());
        final ProxyFactory proxyFactory = new ProxyFactory(service);
        proxyFactory.addAdvice(interceptor);
        final ProvisioningEntriesReadPlatformService proxy = (ProvisioningEntriesReadPlatformService) proxyFactory.getProxy();

        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            assertThrows(IncorrectResultSizeDataAccessException.class, () -> proxy.retrieveProvisioningEntryData("2025"));
            assertThat(status.isRollbackOnly()).isFalse();
            status.setRollbackOnly();
        });
    }

    @Test
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

    private void insertUser(final long id, final String username) {
        queryFactory.insert(mAppuser).set(mAppuser.id, id).set(mAppuser.username, username).set(mAppuser.firstname, username)
                .set(mAppuser.lastname, username).set(mAppuser.password, "unused").set(mAppuser.email, username + "@example.org")
                .set(mAppuser.isDeleted, false).set(mAppuser.firsttimeLoginRemaining, false).set(mAppuser.nonexpired, true)
                .set(mAppuser.nonlocked, true).set(mAppuser.nonexpiredCredentials, true).set(mAppuser.enabled, true)
                .set(mAppuser.lastTimePasswordUpdated, DateExpression.currentDate(Date.class)).set(mAppuser.passwordNeverExpires, true)
                .set(mAppuser.passwordResetRequired, false).set(mAppuser.failedLoginAttempts, 0).set(mAppuser.isLoginRetriesEnabled, false)
                .set(mAppuser.isPasswordResetEnabled, false).execute();
    }

    private void insertEntry(final long id, final LocalDate createdDate, final Long createdBy, final Long modifiedBy,
            final Boolean journalEntryCreated) {
        queryFactory.insert(mProvisioningHistory).set(mProvisioningHistory.id, id)
                .set(mProvisioningHistory.journalEntryCreated, journalEntryCreated)
                .set(mProvisioningHistory.createdDate, createdDate == null ? null : Date.valueOf(createdDate))
                .set(mProvisioningHistory.createdbyId, createdBy).set(mProvisioningHistory.lastmodifiedbyId, modifiedBy).execute();
    }
}
