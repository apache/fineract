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
package org.apache.fineract.cob.savings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import org.apache.fineract.cob.domain.LockOwner;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;

@ExtendWith(MockitoExtension.class)
class SavingsLockingServiceImplTest {

    private static final LocalDate COB_BUSINESS_DATE = LocalDate.parse("2026-06-09");

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private DatabaseSpecificSQLGenerator sqlGenerator;
    @Mock
    private FineractProperties fineractProperties;
    @Mock
    private FineractProperties.FineractQueryProperties queryProperties;

    private SavingsLockingServiceImpl service;

    @BeforeEach
    void setUp() {
        lenient().when(fineractProperties.getQuery()).thenReturn(queryProperties);
        lenient().when(queryProperties.getInClauseParameterSizeLimit()).thenReturn(1000);
        service = new SavingsLockingServiceImpl(jdbcTemplate, sqlGenerator, fineractProperties);

        HashMap<BusinessDateType, LocalDate> dates = new HashMap<>();
        dates.put(BusinessDateType.COB_DATE, COB_BUSINESS_DATE);
        ThreadLocalContextUtil.setBusinessDates(dates);
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    // ---------- applyLock ----------

    @Test
    void applyLockInsertsRowPerSavingsIdWithBusinessDate() throws Exception {
        List<Long> savingsIds = List.of(10L, 20L);

        service.applyLock(savingsIds, LockOwner.SAVINGS_COB_CHUNK_PROCESSING);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<ParameterizedPreparedStatementSetter<Long>> setterCaptor = ArgumentCaptor
                .forClass(ParameterizedPreparedStatementSetter.class);
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).batchUpdate(sqlCaptor.capture(), eq(savingsIds), eq(savingsIds.size()), setterCaptor.capture());

        assertThat(normalize(sqlCaptor.getValue()))
                .isEqualTo("INSERT INTO m_savings_account_locks (savings_id, version, lock_owner, lock_placed_on, "
                        + "lock_placed_on_cob_business_date) VALUES (?,?,?,?,?)");

        PreparedStatement ps = mock(PreparedStatement.class);
        setterCaptor.getValue().setValues(ps, 42L);
        verify(ps).setLong(1, 42L);
        verify(ps).setLong(2, 1L);
        verify(ps).setString(3, "SAVINGS_COB_CHUNK_PROCESSING");
        verify(ps).setObject(eq(4), any());
        verify(ps).setObject(5, COB_BUSINESS_DATE);
    }

    // ---------- upgradeLock ----------

    @Test
    void upgradeLockBatchUpdatesWithConfiguredChunkSize() throws Exception {
        List<Long> savingsIds = List.of(10L, 20L);
        when(queryProperties.getInClauseParameterSizeLimit()).thenReturn(500);

        service.upgradeLock(savingsIds, LockOwner.SAVINGS_COB_CHUNK_PROCESSING);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<ParameterizedPreparedStatementSetter<Long>> setterCaptor = ArgumentCaptor
                .forClass(ParameterizedPreparedStatementSetter.class);
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).batchUpdate(sqlCaptor.capture(), eq(savingsIds), eq(500), setterCaptor.capture());

        assertThat(normalize(sqlCaptor.getValue())).isEqualTo(
                "UPDATE m_savings_account_locks SET version = version + 1, lock_owner = ?, lock_placed_on = ? WHERE savings_id = ?");

        PreparedStatement ps = mock(PreparedStatement.class);
        setterCaptor.getValue().setValues(ps, 99L);
        verify(ps).setString(1, "SAVINGS_COB_CHUNK_PROCESSING");
        verify(ps).setObject(eq(2), any());
        verify(ps).setLong(3, 99L);
    }

    // ---------- updateLockError ----------

    @Test
    void updateLockErrorRunsParameterisedUpdate() {
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any())).thenReturn(1);

        service.updateLockError(77L, LockOwner.SAVINGS_COB_CHUNK_PROCESSING, "boom", "stack");

        verify(jdbcTemplate).update(
                "UPDATE m_savings_account_locks SET error = ?, stacktrace = ? WHERE savings_id = ? AND lock_owner = ?", "boom", "stack",
                77L, "SAVINGS_COB_CHUNK_PROCESSING");
    }

    @Test
    void updateLockErrorSucceedsSilentlyWhenNoRowMatches() {
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any())).thenReturn(0);

        service.updateLockError(77L, LockOwner.SAVINGS_COB_CHUNK_PROCESSING, "boom", "stack");

        verify(jdbcTemplate).update(anyString(), any(), any(), any(), any());
    }

    // ---------- findLockIdsByLoanIdIn ----------

    @Test
    void findLockIdsByLoanIdInReturnsEmptyWithoutQueryingWhenInputEmpty() {
        List<Long> result = service.findLockIdsByLoanIdIn(List.of());

        assertThat(result).isEmpty();
        verifyNoInteractions(jdbcTemplate, sqlGenerator);
    }

    @Test
    void findLockIdsByLoanIdInQueriesByIdList() {
        List<Long> savingsIds = List.of(10L, 20L, 30L);
        when(sqlGenerator.in("savings_id", savingsIds)).thenReturn("savings_id IN (?,?,?)");
        when(sqlGenerator.inParametersFor(savingsIds)).thenReturn(new Object[] { 10L, 20L, 30L });
        when(jdbcTemplate.queryForList(anyString(), eq(Long.class), any(Object[].class))).thenReturn(List.of(10L, 30L));

        List<Long> result = service.findLockIdsByLoanIdIn(savingsIds);

        assertThat(result).containsExactly(10L, 30L);
        verify(jdbcTemplate).queryForList("SELECT savings_id FROM m_savings_account_locks WHERE savings_id IN (?,?,?)", Long.class,
                new Object[] { 10L, 20L, 30L });
    }

    // ---------- findLockIdsByLoanIdInAndLockOwner ----------

    @Test
    void findLockIdsByLoanIdInAndLockOwnerReturnsEmptyWithoutQueryingWhenInputEmpty() {
        List<Long> result = service.findLockIdsByLoanIdInAndLockOwner(List.of(), LockOwner.SAVINGS_COB_CHUNK_PROCESSING);

        assertThat(result).isEmpty();
        verifyNoInteractions(jdbcTemplate, sqlGenerator);
    }

    @Test
    void findLockIdsByLoanIdInAndLockOwnerQueriesByIdAndOwner() {
        List<Long> savingsIds = List.of(10L, 20L);
        when(sqlGenerator.in("savings_id", savingsIds)).thenReturn("savings_id IN (?,?)");
        when(sqlGenerator.inParametersFor(savingsIds)).thenReturn(new Object[] { 10L, 20L });
        when(jdbcTemplate.queryForList(anyString(), eq(Long.class), any(Object[].class))).thenReturn(List.of(20L));

        List<Long> result = service.findLockIdsByLoanIdInAndLockOwner(savingsIds, LockOwner.SAVINGS_COB_CHUNK_PROCESSING);

        assertThat(result).containsExactly(20L);
        verify(jdbcTemplate).queryForList("SELECT savings_id FROM m_savings_account_locks WHERE savings_id IN (?,?) AND lock_owner = ?",
                Long.class, new Object[] { 10L, 20L, "SAVINGS_COB_CHUNK_PROCESSING" });
    }

    // ---------- deleteByLoanIdInAndLockOwner ----------

    @Test
    void deleteByLoanIdInAndLockOwnerSkipsQueryWhenInputEmpty() {
        service.deleteByLoanIdInAndLockOwner(List.of(), LockOwner.SAVINGS_COB_CHUNK_PROCESSING);

        verifyNoInteractions(jdbcTemplate, sqlGenerator);
    }

    @Test
    void deleteByLoanIdInAndLockOwnerIssuesParameterisedDelete() {
        List<Long> savingsIds = List.of(10L, 20L);
        when(sqlGenerator.in("savings_id", savingsIds)).thenReturn("savings_id IN (?,?)");
        when(sqlGenerator.inParametersFor(savingsIds)).thenReturn(new Object[] { 10L, 20L });

        service.deleteByLoanIdInAndLockOwner(savingsIds, LockOwner.SAVINGS_COB_CHUNK_PROCESSING);

        verify(jdbcTemplate).update("DELETE FROM m_savings_account_locks WHERE savings_id IN (?,?) AND lock_owner = ?",
                new Object[] { 10L, 20L, "SAVINGS_COB_CHUNK_PROCESSING" });
    }

    private String normalize(String sql) {
        return sql.replaceAll("\\s+", " ").trim();
    }
}
