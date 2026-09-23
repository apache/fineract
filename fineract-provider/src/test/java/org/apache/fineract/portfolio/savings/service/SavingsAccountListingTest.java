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
package org.apache.fineract.portfolio.savings.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.core.service.database.DatabaseTypeResolver;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

@ExtendWith(MockitoExtension.class)
class SavingsAccountListingTest {

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private DatabaseTypeResolver databaseTypeResolver;
    @Mock
    private AppUser user;
    @Mock
    private Office office;

    @ParameterizedTest
    @CsvSource({ ",,", "300,,", "100,,", ",reference,", "300,reference,", "100,reference,7", ",,7", ",reference,7" })
    void bindsOptionalFiltersInOrderAndPreservesHierarchyAndFilteredCount(String status, String externalId, Long officeId) {
        SearchParameters parameters = SearchParameters.builder().status(status).externalId(externalId).officeId(officeId).limit(1).offset(1)
                .build();
        verifyListing(parameters, status, externalId, officeId);
    }

    @Test
    void supportsExistingNullSearchParameters() {
        verifyListing(null, null, null, null);
    }

    private void verifyListing(SearchParameters parameters, String status, String externalId, Long officeId) {
        when(context.authenticatedUser()).thenReturn(user);
        when(user.getOffice()).thenReturn(office);
        when(office.getHierarchy()).thenReturn(".1.2.");
        if (parameters != null) {
            when(databaseTypeResolver.isPostgreSQL()).thenReturn(true);
        }
        DatabaseSpecificSQLGenerator generator = new DatabaseSpecificSQLGenerator(databaseTypeResolver, null);
        PaginationHelper paginationHelper = new PaginationHelper(generator, databaseTypeResolver);
        SavingsAccountReadPlatformServiceImpl service = new SavingsAccountReadPlatformServiceImpl(context, jdbcTemplate, null,
                paginationHelper, null, generator, null, null);
        SavingsAccountData item = mock(SavingsAccountData.class);
        doReturn(List.of(item)).when(jdbcTemplate).query(anyString(), any(RowMapper.class), any(Object[].class));
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any(Object[].class))).thenReturn(3);

        Page<SavingsAccountData> result = service.retrieveAll(parameters);

        assertThat(result.getPageItems()).containsExactly(item);
        assertThat(result.getTotalFilteredRecords()).isEqualTo(3);
        ArgumentCaptor<String> rowsSql = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> rowsArgs = ArgumentCaptor.forClass(Object[].class);
        verify(jdbcTemplate).query(rowsSql.capture(), any(RowMapper.class), rowsArgs.capture());
        ArgumentCaptor<String> countSql = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> countArgs = ArgumentCaptor.forClass(Object[].class);
        verify(jdbcTemplate).queryForObject(countSql.capture(), eq(Integer.class), countArgs.capture());
        List<Object> expected = new ArrayList<>();
        expected.add(".1.2.%");
        assertThat(rowsSql.getValue()).contains("join m_office o on o.id = c.office_id", "where o.hierarchy like ?");
        if (status != null) {
            assertThat(rowsSql.getValue()).contains("and sa.status_enum = ?");
            expected.add(Integer.parseInt(status));
        } else {
            assertThat(rowsSql.getValue()).doesNotContain("and sa.status_enum = ?");
        }
        if (externalId != null) {
            assertThat(rowsSql.getValue()).contains("and sa.external_id = ?");
            expected.add(externalId);
        } else {
            assertThat(rowsSql.getValue()).doesNotContain("and sa.external_id = ?");
        }
        if (officeId != null) {
            assertThat(rowsSql.getValue()).contains("and c.office_id = ?");
            expected.add(officeId);
        }
        assertThat(rowsArgs.getValue()).containsExactlyElementsOf(expected);
        assertThat(countArgs.getValue()).containsExactlyElementsOf(expected);
        assertThat(countSql.getValue()).isEqualTo(generator.countQueryResult(rowsSql.getValue()));
        assertThat(countSql.getValue()).doesNotContain("LIMIT", "OFFSET");
        if (parameters != null) {
            assertThat(rowsSql.getValue()).contains("LIMIT 1 OFFSET 1");
        }
    }
}
