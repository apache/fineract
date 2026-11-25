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
package org.apache.fineract.infrastructure.campaigns.email.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.apache.fineract.infrastructure.campaigns.email.data.EmailData;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.core.service.database.DatabaseTypeResolver;
import org.apache.fineract.query.QScheduledEmailMessagesOutbound;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class EmailReadPlatformServiceImplTest {

    private static final QScheduledEmailMessagesOutbound EMO = new QScheduledEmailMessagesOutbound("emo");

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private DatabaseTypeResolver databaseTypeResolver; // Mock ONLY the resolver

    @Mock
    private PaginationHelper paginationHelper;

    @Mock
    private SQLQueryFactory queryFactory;

    private EmailReadPlatformServiceImpl emailReadPlatformService;

    @BeforeEach
    void setUp() {
        // Use the REAL DatabaseSpecificSQLGenerator, not a mock
        // Pass null for RoutingDataSource as it's not needed for limit()
        DatabaseSpecificSQLGenerator sqlGenerator = new DatabaseSpecificSQLGenerator(databaseTypeResolver, null);
        emailReadPlatformService = new EmailReadPlatformServiceImpl(jdbcTemplate, sqlGenerator, paginationHelper, queryFactory);
    }

    @Test
    void testRetrieveAllPendingWithMySQL() {
        // Given
        SearchParameters searchParameters = SearchParameters.builder().limit(10).build();

        // Simulate MySQL environment
        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        // isPostgreSQL not checked if isMySQL is true

        // When
        emailReadPlatformService.retrieveAllPending(searchParameters);

        // Then
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).query(sqlCaptor.capture(), any(RowMapper.class), any(Object[].class));

        String executedSql = sqlCaptor.getValue();
        // Verify MySQL specific syntax (LIMIT 10)
        assertTrue(executedSql.contains("LIMIT 0,10"), "SQL should contain MySQL LIMIT clause: " + executedSql);
    }

    @Test
    void testRetrieveAllSentWithPostgres() {
        // Given
        SearchParameters searchParameters = SearchParameters.builder().limit(5).build();

        // Simulate Postgres environment
        when(databaseTypeResolver.isMySQL()).thenReturn(false);
        when(databaseTypeResolver.isPostgreSQL()).thenReturn(true);

        // When
        emailReadPlatformService.retrieveAllSent(searchParameters);

        // Then
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).query(sqlCaptor.capture(), any(RowMapper.class), any(Object[].class));

        String executedSql = sqlCaptor.getValue();
        // Verify Postgres specific syntax (LIMIT 5)
        assertTrue(executedSql.contains("LIMIT 5 OFFSET 0"), "SQL should contain Postgres LIMIT clause: " + executedSql);
    }

    @Test
    void testRetrieveAllPendingWithoutLimit() {
        // Given - limit 0 means unlimited (getLimit() returns null)
        SearchParameters searchParameters = SearchParameters.builder().limit(0).build();

        // When
        emailReadPlatformService.retrieveAllPending(searchParameters);

        // Then
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).query(sqlCaptor.capture(), any(RowMapper.class), any(Object[].class));

        String executedSql = sqlCaptor.getValue();
        // Verify NO LIMIT clause
        assertTrue(!executedSql.contains("LIMIT"), "SQL should NOT contain LIMIT when limit is null");
    }

    @Test
    void testRetrieveWithLimitOne() {
        // Given
        SearchParameters searchParameters = SearchParameters.builder().limit(1).build();

        // Simulate MySQL environment
        when(databaseTypeResolver.isMySQL()).thenReturn(true);

        // When
        emailReadPlatformService.retrieveAllPending(searchParameters);

        // Then
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).query(sqlCaptor.capture(), any(RowMapper.class), any(Object[].class));

        String executedSql = sqlCaptor.getValue();
        // Verify MySQL specific syntax (LIMIT 1)
        assertTrue(executedSql.contains("LIMIT 0,1"), "SQL should contain MySQL LIMIT 1 clause");
    }

    @Test
    void testRetrieveWithNegativeLimit() {
        // Given
        SearchParameters searchParameters = SearchParameters.builder().limit(-5).build();

        // When
        emailReadPlatformService.retrieveAllPending(searchParameters);

        // Then
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).query(sqlCaptor.capture(), any(RowMapper.class), any(Object[].class));

        String executedSql = sqlCaptor.getValue();
        // Verify NO LIMIT clause for negative values (as they are invalid for SQL limit)
        assertTrue(!executedSql.contains("LIMIT"), "SQL should NOT contain LIMIT when limit is negative");
    }

    @Test
    void testRetrieveAllReturnsExpectedData() {
        Date submittedDate = Date.valueOf(LocalDate.of(2026, 11, 25));
        givenRows(row(1L, 10L, 20L, 30L, "Welcome Campaign", 100, "user@example.com", submittedDate, "Welcome", "Hello World", "No Error"));

        Collection<EmailData> result = emailReadPlatformService.retrieveAll();

        assertNotNull(result);
        assertEquals(1, result.size());

        EmailData data = result.iterator().next();
        assertEquals(1L, data.getId());
        assertEquals(10L, data.getGroupId());
        assertEquals(20L, data.getClientId());
        assertEquals(30L, data.getStaffId());
        assertEquals("Welcome Campaign", data.getCampaignName());
        assertNotNull(data.getStatus());
        assertEquals(100L, data.getStatus().getId());
        assertEquals("Pending", data.getStatus().getValue());
        assertEquals("user@example.com", data.getEmailAddress());
        assertEquals(LocalDate.of(2026, 11, 25), data.getSentDate());
        assertEquals("Welcome", data.getEmailSubject());
        assertEquals("Hello World", data.getEmailMessage());
        assertEquals("No Error", data.getErrorMessage());
    }

    @Test
    void testRetrieveAllBuildsExpectedQuery() {
        SQLQuery<Tuple> query = givenRows();

        emailReadPlatformService.retrieveAll();

        verify(queryFactory).select(EMO.id, EMO.groupId, EMO.clientId, EMO.staffId, EMO.campaignName, EMO.statusEnum, EMO.emailAddress,
                EMO.submittedonDate, EMO.emailSubject, EMO.message, EMO.errorMessage);
        verify(query).from(EMO);
    }

    @Test
    void testRetrieveAllReturnsEmptyWhenNoRows() {
        givenRows();

        Collection<EmailData> result = emailReadPlatformService.retrieveAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testRetrieveAllHandlesNullFieldsSafely() {
        givenRows(row(null, null, null, null, null, null, null, null, null, null, null));

        Collection<EmailData> result = emailReadPlatformService.retrieveAll();

        assertNotNull(result);
        assertEquals(1, result.size());

        EmailData data = result.iterator().next();
        assertNull(data.getId());
        assertNull(data.getGroupId());
        assertNull(data.getClientId());
        assertNull(data.getStaffId());
        assertNull(data.getCampaignName());
        assertNull(data.getStatus());
        assertNull(data.getEmailAddress());
        assertNull(data.getSentDate());
        assertNull(data.getEmailSubject());
        assertNull(data.getEmailMessage());
        assertNull(data.getErrorMessage());
    }

    @SuppressWarnings("unchecked")
    private SQLQuery<Tuple> givenRows(Tuple... rows) {
        SQLQuery<Tuple> query = mock(SQLQuery.class, RETURNS_SELF);
        when(queryFactory.select(EMO.id, EMO.groupId, EMO.clientId, EMO.staffId, EMO.campaignName, EMO.statusEnum, EMO.emailAddress,
                EMO.submittedonDate, EMO.emailSubject, EMO.message, EMO.errorMessage)).thenReturn(query);
        when(query.fetch()).thenReturn(List.of(rows));
        return query;
    }

    private static Tuple row(Long id, Long groupId, Long clientId, Long staffId, String campaignName, Integer statusEnum,
            String emailAddress, Date submittedonDate, String emailSubject, String message, String errorMessage) {
        return Projections.tuple(EMO.id, EMO.groupId, EMO.clientId, EMO.staffId, EMO.campaignName, EMO.statusEnum, EMO.emailAddress,
                EMO.submittedonDate, EMO.emailSubject, EMO.message, EMO.errorMessage).newInstance(id, groupId, clientId, staffId,
                        campaignName, statusEnum, emailAddress, submittedonDate, emailSubject, message, errorMessage);
    }
}
