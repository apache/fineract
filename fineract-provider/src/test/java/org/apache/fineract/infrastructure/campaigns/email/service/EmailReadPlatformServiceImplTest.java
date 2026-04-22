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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.core.service.database.DatabaseTypeResolver;
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

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private DatabaseTypeResolver databaseTypeResolver; // Mock ONLY the resolver

    @Mock
    private PaginationHelper paginationHelper;

    private EmailReadPlatformServiceImpl emailReadPlatformService;

    @BeforeEach
    void setUp() {
        // Use the REAL DatabaseSpecificSQLGenerator, not a mock
        // Pass null for RoutingDataSource as it's not needed for limit()
        DatabaseSpecificSQLGenerator sqlGenerator = new DatabaseSpecificSQLGenerator(databaseTypeResolver, null);
        emailReadPlatformService = new EmailReadPlatformServiceImpl(jdbcTemplate, sqlGenerator, paginationHelper);
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
}
