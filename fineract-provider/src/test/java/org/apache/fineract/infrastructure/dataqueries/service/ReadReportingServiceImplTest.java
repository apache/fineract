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
package org.apache.fineract.infrastructure.dataqueries.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.dataqueries.data.GenericResultsetData;
import org.apache.fineract.infrastructure.report.service.ReportParameterTypeResolver;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.service.SqlInjectionPreventerService;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

@ExtendWith(MockitoExtension.class)
public class ReadReportingServiceImplTest {

    /** A cascaded lookup: {@code ${officeId}} comes from the PARENT parameter, so this report does not declare it. */
    private static final String CASCADED_REPORT_NAME = "loanOfficerIdSelectAll";
    private static final String CASCADED_REPORT_SQL = "select lo.id, lo.display_name as name from m_staff lo "
            + "join m_office o on o.id = lo.office_id where lo.is_loan_officer = true and o.id = ${officeId}";

    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private PlatformSecurityContext context;
    @Mock
    private GenericDataService genericDataService;
    @Mock
    private SqlInjectionPreventerService sqlInjectionPreventerService;
    @Mock
    private DatabaseSpecificSQLGenerator sqlGenerator;
    @Mock
    private FineractProperties fineractProperties;
    @Mock
    private ReportParameterTypeResolver reportParameterTypeResolver;

    private ReadReportingServiceImpl readReportingService;

    @BeforeEach
    public void setUp() {
        readReportingService = new ReadReportingServiceImpl(jdbcTemplate, context, genericDataService, sqlInjectionPreventerService,
                sqlGenerator, fineractProperties, reportParameterTypeResolver);
    }

    @Test
    public void testRetrieveGenericResultset_ThrowsValidationExceptionWhenParametersMissing() {
        String reportName = "Test Report";
        String reportType = "report";
        String mockSql = "select * from loans where office_id = ${officeId} and loan_officer_id = ${loanOfficerId}";

        SqlRowSet rs = mock(SqlRowSet.class);
        when(rs.next()).thenReturn(true);
        when(rs.getString("the_sql")).thenReturn(mockSql);
        when(jdbcTemplate.queryForRowSet(anyString(), any(Object[].class))).thenReturn(rs);
        when(genericDataService.wrapSQL(anyString())).thenAnswer(invocation -> invocation.getArgument(0));

        when(sqlInjectionPreventerService.quoteIdentifier(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(reportParameterTypeResolver.loadParamFormatTypes(reportName)).thenReturn(new HashMap<>());

        AppUser appUser = mock(AppUser.class);
        Office office = mock(Office.class);
        when(context.authenticatedUser()).thenReturn(appUser);
        when(appUser.getOffice()).thenReturn(office);
        when(appUser.getId()).thenReturn(1L);
        when(office.getHierarchy()).thenReturn(".1.");

        when(genericDataService.replace(anyString(), anyString(), anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(sqlGenerator.currentBusinessDate()).thenReturn("2024-01-01");
        when(sqlGenerator.currentTenantDateTime()).thenReturn("2024-01-01 00:00:00");

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("officeId", "1");
        // Note: we omit loanOfficerId on purpose to trigger validation error

        PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class, () -> {
            readReportingService.retrieveGenericResultset(reportName, reportType, queryParams);
        });

        assertEquals(1, exception.getErrors().size());
        assertEquals("error.msg.report.missing.parameter", exception.getErrors().get(0).getUserMessageGlobalisationCode());
        assertEquals("loanOfficerId", exception.getErrors().get(0).getParameterName());
    }

    // FINERACT-2713: a cascaded parameter is absent from the running report's own format-type map, so it used to bind
    // as a String. PostgreSQL rejects "bigint = character varying" (MySQL silently coerces it), leaving the dependent
    // dropdown empty. A plain integer with no declared type must bind numerically.
    @Test
    public void untypedIntegerParameterBindsAsNumber() {
        stubCascadedReport(Map.of()); // officeId not declared

        Object[] bound = boundParamsFor(Map.of("officeId", "1"));

        assertEquals(1, bound.length);
        assertEquals(Long.valueOf(1L), bound[0], "an untyped plain integer must bind as a number, not a String");
    }

    // The inference must stay narrow: anything that is not a plain integer keeps its String binding, so currency codes
    // and identifiers carrying leading zeros are not silently mangled into numbers.
    @Test
    public void untypedNonIntegerParameterStaysAString() {
        stubCascadedReport(Map.of());

        assertEquals("USD", boundParamsFor(Map.of("officeId", "USD"))[0]);
    }

    @Test
    public void untypedIntegerWithLeadingZerosStaysAString() {
        stubCascadedReport(Map.of());

        assertEquals("000123", boundParamsFor(Map.of("officeId", "000123"))[0], "leading zeros are significant in identifiers");
    }

    // A declared type still wins - this path is untouched by the fix.
    @Test
    public void declaredNumberParameterStillBindsAsNumber() {
        stubCascadedReport(Map.of("officeId", "number"));

        assertEquals(Long.valueOf(7L), boundParamsFor(Map.of("officeId", "7"))[0]);
    }

    /** Serves {@link #CASCADED_REPORT_SQL} for {@link #CASCADED_REPORT_NAME} with the given declared format types. */
    private void stubCascadedReport(Map<String, String> paramFormatTypes) {
        SqlRowSet rowSet = mock(SqlRowSet.class);
        when(rowSet.next()).thenReturn(true);
        when(rowSet.getString("the_sql")).thenReturn(CASCADED_REPORT_SQL);
        when(jdbcTemplate.queryForRowSet(anyString(), eq(CASCADED_REPORT_NAME))).thenReturn(rowSet);
        when(sqlInjectionPreventerService.quoteIdentifier(anyString())).thenAnswer(call -> call.getArgument(0));
        when(reportParameterTypeResolver.loadParamFormatTypes(CASCADED_REPORT_NAME)).thenReturn(paramFormatTypes);

        AppUser appUser = mock(AppUser.class);
        Office office = mock(Office.class);
        when(context.authenticatedUser()).thenReturn(appUser);
        when(appUser.getOffice()).thenReturn(office);
        when(appUser.getId()).thenReturn(1L);
        when(office.getHierarchy()).thenReturn(".");

        when(sqlGenerator.currentBusinessDate()).thenReturn("'2024-01-01'");
        when(sqlGenerator.currentTenantDateTime()).thenReturn("'2024-01-01 00:00:00'");

        // pass the SQL through untouched so the assertions are about the bound values, not the rewriting
        when(genericDataService.wrapSQL(anyString())).thenAnswer(call -> call.getArgument(0));
        when(genericDataService.replace(anyString(), anyString(), anyString())).thenAnswer(call -> call.getArgument(0));
        when(genericDataService.fillGenericResultSet(anyString(), any())).thenReturn(mock(GenericResultsetData.class));
    }

    /** Runs the cascaded report and returns the values actually bound to the prepared statement. */
    private Object[] boundParamsFor(Map<String, String> queryParams) {
        readReportingService.retrieveGenericResultset(CASCADED_REPORT_NAME, "report", queryParams);
        ArgumentCaptor<Object[]> captor = ArgumentCaptor.forClass(Object[].class);
        verify(genericDataService).fillGenericResultSet(anyString(), captor.capture());
        return captor.getValue();
    }
}
