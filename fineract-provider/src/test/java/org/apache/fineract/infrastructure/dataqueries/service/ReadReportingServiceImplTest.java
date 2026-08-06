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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.report.service.ReportParameterTypeResolver;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.service.SqlInjectionPreventerService;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

@ExtendWith(MockitoExtension.class)
public class ReadReportingServiceImplTest {

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

}
