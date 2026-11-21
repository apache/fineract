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
package org.apache.fineract.cob.domain;

import static org.apache.fineract.query.QMLoanAccountLocks.mLoanAccountLocks;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.PostgreSQLTemplates;
import com.querydsl.sql.SQLBindings;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.SQLTemplates;
import java.sql.Array;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.core.service.database.DatabaseType;
import org.apache.fineract.infrastructure.core.service.database.DatabaseTypeResolver;
import org.apache.fineract.infrastructure.core.service.database.RoutingDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class LockingServiceSqlTest {

    private static final List<Long> LOAN_IDS = List.of(10L, 20L, 30L);
    private static final String TABLE_NAME = "test_locks";

    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private DatabaseTypeResolver databaseTypeResolver;
    @Mock
    private RoutingDataSource dataSource;
    @Mock
    private FineractProperties fineractProperties;
    @Mock
    private SQLQueryFactory queryFactory;

    private AbstractLockingService underTest;

    @BeforeEach
    void setUp() {
        final DatabaseSpecificSQLGenerator sqlGenerator = new DatabaseSpecificSQLGenerator(databaseTypeResolver, dataSource);
        underTest = new AbstractLockingService(jdbcTemplate, sqlGenerator, fineractProperties, queryFactory, mLoanAccountLocks,
                mLoanAccountLocks.loanId) {

            @Override
            protected String getTableName() {
                return TABLE_NAME;
            }

            @Override
            protected String getBatchLoanLockInsert() {
                return "";
            }

            @Override
            protected String getBatchLoanLockUpgrade() {
                return "";
            }
        };
    }

    @Test
    void findLockIdsByLoanIdInOnMySqlUsesInClauseWithIndividualBindParameters() {
        when(databaseTypeResolver.databaseType()).thenReturn(DatabaseType.MYSQL);
        final SQLQuery<Long> query = stubLockLookup(MySQLTemplates.builder().build());

        assertEquals(LOAN_IDS, underTest.findLockIdsByLoanIdIn(LOAN_IDS));

        final SQLBindings bindings = query.getSQL();
        assertTrue(bindings.getSQL().contains("where m_loan_account_locks.loan_id in"));
        assertEquals(LOAN_IDS, bindings.getNullFriendlyBindings());
    }

    @Test
    void findLockIdsByLoanIdInOnPostgresUsesAnyClauseWithArrayBindParameter() {
        when(databaseTypeResolver.databaseType()).thenReturn(DatabaseType.POSTGRESQL);
        final SQLQuery<Long> query = stubLockLookup(PostgreSQLTemplates.builder().build());

        assertEquals(LOAN_IDS, underTest.findLockIdsByLoanIdIn(LOAN_IDS));

        final SQLBindings bindings = query.getSQL();
        assertTrue(bindings.getSQL().contains("where m_loan_account_locks.loan_id = ANY (?)"));
        assertEquals(1, bindings.getNullFriendlyBindings().size());
        assertArrayEquals(LOAN_IDS.toArray(Long[]::new), (Long[]) bindings.getNullFriendlyBindings().getFirst());
    }

    private SQLQuery<Long> stubLockLookup(final SQLTemplates templates) {
        final SQLQuery<Long> query = new SQLQuery<Long>(templates) {

            @Override
            public List<Long> fetch() {
                return LOAN_IDS;
            }
        };
        when(queryFactory.select(mLoanAccountLocks.loanId)).thenReturn(query.select(mLoanAccountLocks.loanId));
        return query;
    }

    @Test
    void findLockIdsByLoanIdInAndLockOwnerOnPostgresUsesAnyClauseWithArrayBindParameter() throws SQLException {
        when(databaseTypeResolver.databaseType()).thenReturn(DatabaseType.POSTGRESQL);
        final Connection connection = mock(Connection.class);
        final Array loanIdArray = mock(Array.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createArrayOf(eq("bigint"), any(Long[].class))).thenReturn(loanIdArray);

        underTest.findLockIdsByLoanIdInAndLockOwner(LOAN_IDS, LockOwner.LOAN_COB_CHUNK_PROCESSING);

        final ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).queryForList(sqlCaptor.capture(), eq(Long.class), eq(loanIdArray),
                eq(LockOwner.LOAN_COB_CHUNK_PROCESSING.name()));
        assertEquals("SELECT loan_id FROM " + TABLE_NAME + " WHERE loan_id = ANY (?) AND lock_owner = ?", sqlCaptor.getValue());
    }

    @Test
    void deleteByLoanIdInAndLockOwnerOnMySqlUsesInClauseWithIndividualBindParameters() {
        when(databaseTypeResolver.databaseType()).thenReturn(DatabaseType.MYSQL);

        underTest.deleteByLoanIdInAndLockOwner(LOAN_IDS, LockOwner.LOAN_COB_CHUNK_PROCESSING);

        final ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).update(sqlCaptor.capture(), eq(10L), eq(20L), eq(30L), eq(LockOwner.LOAN_COB_CHUNK_PROCESSING.name()));
        assertEquals("DELETE FROM " + TABLE_NAME + " WHERE loan_id IN (?,?,?) AND lock_owner = ?", sqlCaptor.getValue());
    }

    @ParameterizedTest
    @MethodSource("emptyLoanIds")
    void findLockIdsByLoanIdInWithNoIdsSkipsQuery(final List<Long> loanIds) {
        assertTrue(underTest.findLockIdsByLoanIdIn(loanIds).isEmpty());
        verifyNoInteractions(jdbcTemplate, queryFactory);
    }

    private static Stream<List<Long>> emptyLoanIds() {
        return Stream.of(Collections.emptyList());
    }
}
