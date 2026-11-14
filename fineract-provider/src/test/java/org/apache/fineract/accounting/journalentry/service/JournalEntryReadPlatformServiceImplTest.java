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
package org.apache.fineract.accounting.journalentry.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import org.apache.fineract.accounting.financialactivityaccount.domain.FinancialActivityAccountRepositoryWrapper;
import org.apache.fineract.accounting.glaccount.service.GLAccountReadPlatformService;
import org.apache.fineract.accounting.journalentry.data.JournalEntryAssociationParametersData;
import org.apache.fineract.accounting.journalentry.data.JournalEntryData;
import org.apache.fineract.accounting.journalentry.exception.JournalEntriesNotFoundException;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

@ExtendWith(MockitoExtension.class)
class JournalEntryReadPlatformServiceImplTest {

    private static final String HIERARCHY = ".1.7.";
    private static final String HIERARCHY_PATTERN = ".1.7.%";
    private static final long ENTRY_ID = 5L;

    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private GLAccountReadPlatformService glAccountReadPlatformService;
    @Mock
    private OfficeReadPlatformService officeReadPlatformService;
    @Mock
    private ColumnValidator columnValidator;
    @Mock
    private FinancialActivityAccountRepositoryWrapper financialActivityAccountRepositoryWrapper;
    @Mock
    private PaginationHelper paginationHelper;
    @Mock
    private DatabaseSpecificSQLGenerator sqlGenerator;

    private JournalEntryReadPlatformServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new JournalEntryReadPlatformServiceImpl(jdbcTemplate, glAccountReadPlatformService, officeReadPlatformService,
                columnValidator, financialActivityAccountRepositoryWrapper, paginationHelper, sqlGenerator);
    }

    @Test
    void retrieveAllWithHierarchyLimitsToOfficeSubtree() {
        final ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<Object[]> argsCaptor = ArgumentCaptor.forClass(Object[].class);
        when(paginationHelper.fetchPage(eq(jdbcTemplate), sqlCaptor.capture(), argsCaptor.capture(), any())).thenReturn(null);

        service.retrieveAll(SearchParameters.builder().hierarchy(HIERARCHY).build(), null, null, null, null, null, null, null, null,
                new JournalEntryAssociationParametersData());

        assertTrue(sqlCaptor.getValue().contains("office.hierarchy like ?"),
                "expected an office hierarchy clause when the caller's hierarchy is supplied");
        assertTrue(Arrays.asList(argsCaptor.getValue()).contains(HIERARCHY_PATTERN),
                "expected the hierarchy prefix to be bound as a like pattern");
    }

    @Test
    void retrieveAllWithoutHierarchyDoesNotFilterOnOfficeHierarchy() {
        final ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        when(paginationHelper.fetchPage(eq(jdbcTemplate), sqlCaptor.capture(), any(), any())).thenReturn(null);

        service.retrieveAll(SearchParameters.builder().build(), null, null, null, null, null, null, null, null,
                new JournalEntryAssociationParametersData());

        assertFalse(sqlCaptor.getValue().contains("office.hierarchy"), "callers without a hierarchy must stay unscoped");
    }

    @Test
    void retrieveGLJournalEntryByIdWithHierarchyLimitsToOfficeSubtree() {
        final ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        when(jdbcTemplate.queryForObject(sqlCaptor.capture(), ArgumentMatchers.<RowMapper<JournalEntryData>>any(), eq(ENTRY_ID),
                eq(HIERARCHY_PATTERN))).thenReturn(null);

        service.retrieveGLJournalEntryById(ENTRY_ID, new JournalEntryAssociationParametersData(), HIERARCHY);

        assertTrue(sqlCaptor.getValue().endsWith("where journalEntry.id = ? and office.hierarchy like ?"));
    }

    @Test
    void retrieveGLJournalEntryByIdOutsideHierarchyIsNotFound() {
        when(jdbcTemplate.queryForObject(anyString(), ArgumentMatchers.<RowMapper<JournalEntryData>>any(), eq(ENTRY_ID),
                eq(HIERARCHY_PATTERN))).thenThrow(new EmptyResultDataAccessException(1));

        assertThrows(JournalEntriesNotFoundException.class,
                () -> service.retrieveGLJournalEntryById(ENTRY_ID, new JournalEntryAssociationParametersData(), HIERARCHY));
    }

    @Test
    void retrieveGLJournalEntryByIdWithoutHierarchyDoesNotFilterOnOfficeHierarchy() {
        final ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        when(jdbcTemplate.queryForObject(sqlCaptor.capture(), ArgumentMatchers.<RowMapper<JournalEntryData>>any(), eq(ENTRY_ID)))
                .thenReturn(null);

        service.retrieveGLJournalEntryById(ENTRY_ID, new JournalEntryAssociationParametersData());

        assertFalse(sqlCaptor.getValue().contains("office.hierarchy"));
    }
}
