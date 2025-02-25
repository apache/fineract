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

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.codes.service.CodeReadPlatformService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenantConnection;
import org.apache.fineract.infrastructure.core.serialization.DatatableCommandFromApiJsonDeserializer;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.core.service.database.DatabaseTypeResolver;
import org.apache.fineract.infrastructure.dataqueries.data.DataTableValidator;
import org.apache.fineract.infrastructure.dataqueries.data.ResultsetRowData;
import org.apache.fineract.infrastructure.event.business.domain.BusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.datatable.DatatableEntryBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.infrastructure.security.utils.DefaultSqlValidator;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.portfolio.search.service.SearchUtil;
import org.apache.fineract.useradministration.domain.AppUser;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;

@ExtendWith(MockitoExtension.class)
public class DatatableBusinessEventTest {

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private BusinessEventNotifierService businessEventNotifierService;
    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private GenericDataService genericDataService;
    @Mock
    private SearchUtil searchUtil;
    @Mock
    private DatabaseTypeResolver databaseTypeResolver;
    @Mock
    private DatabaseSpecificSQLGenerator sqlGenerator;
    @Mock
    private FineractPlatformTenantConnection tenantConnection;
    @Mock
    private DefaultSqlValidator sqlValidator;
    @Mock
    private FromJsonHelper fromJsonHelper;
    @Mock
    private DatatableCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    @Mock
    private ConfigurationDomainService configurationDomainService;
    @Mock
    private CodeReadPlatformService codeReadPlatformService;
    @Mock
    private DataTableValidator dataTableValidator;
    @Mock
    private ColumnValidator columnValidator;
    @Mock
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    @Mock
    private DatatableKeywordGenerator datatableKeywordGenerator;

    @InjectMocks
    private ReadWriteNonCoreDataServiceImpl underTest;

    @Captor
    private ArgumentCaptor<BusinessEvent<?>> businessEventArgumentCaptor;

    private static String DATATABLE_NAME = "test_loan_data";

    @BeforeEach
    public void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.parse("2024-01-16"),
                BusinessDateType.COB_DATE, LocalDate.parse("2024-01-15"))));

        SqlRowSet sqlRS = Mockito.mock(SqlRowSet.class);
        SqlRowSet sqlRSData = Mockito.mock(SqlRowSet.class);
        doNothing().when(sqlValidator).validate(anyString());
        when(jdbcTemplate.queryForRowSet(anyString(), anyString())).thenReturn(sqlRS);
        when(jdbcTemplate.queryForRowSet(anyString())).thenReturn(sqlRSData);

        when(sqlRS.next()).thenReturn(true).thenReturn(false);
        when(sqlRS.getString("application_table_name")).thenReturn("m_loan");
        when(sqlRSData.next()).thenReturn(true).thenReturn(false);
        when(sqlRSData.getObject(anyString())).thenReturn(1L);

        AppUser currentUser = Mockito.mock(AppUser.class);
        Office office = Mockito.mock(Office.class);
        when(context.authenticatedUser()).thenReturn(currentUser);
        when(currentUser.getOffice()).thenReturn(office);
        when(office.getHierarchy()).thenReturn(".");
    }

    @AfterEach
    public void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    public void businessEventCreateNewDatatableEntryTest() {
        when(jdbcTemplate.update(any(PreparedStatementCreator.class), any(KeyHolder.class))).thenReturn(1);

        underTest.createNewDatatableEntry(DATATABLE_NAME, 1L, createJsonCommand("{}", 1L));

        ArgumentCaptor<BusinessEvent<?>> businessEventArgumentCaptor = verifyBusinessEvents(1);
        verifyDatatableBusinessEvent(businessEventArgumentCaptor, 0, 1L);
    }

    @Test
    public void businessEventUpdateDatatableEntryTest() {
        List<Object> values = new ArrayList<>();
        values.add(1L);
        List<ResultsetRowData> result = new ArrayList<>();
        result.add(ResultsetRowData.create(values));
        when(genericDataService.fillResultsetRowData(anyString(), any())).thenReturn(result);

        // ResultsetColumnHeaderData resultSet = Mockito.mock(ResultsetColumnHeaderData.class);
        // when(searchUtil.getFiltered(columnHeaders,
        // ResultsetColumnHeaderData::getIsColumnPrimaryKey)).thenReturn(resultSet);

        underTest.updateDatatableEntryOneToMany(DATATABLE_NAME, 1L, 1L, createJsonCommand("{}", 1L));

        ArgumentCaptor<BusinessEvent<?>> businessEventArgumentCaptor = verifyBusinessEvents(1);
        verifyDatatableBusinessEvent(businessEventArgumentCaptor, 0, 1L);
    }

    @Test
    public void businessEventDeleteDatatableEntryTest() {

        underTest.deleteDatatableEntry(DATATABLE_NAME, 1L, 1L, createJsonCommand("{}", 1L));

        ArgumentCaptor<BusinessEvent<?>> businessEventArgumentCaptor = verifyBusinessEvents(1);
        verifyDatatableBusinessEvent(businessEventArgumentCaptor, 0, 1L);
    }

    @NotNull
    private ArgumentCaptor<BusinessEvent<?>> verifyBusinessEvents(int expectedBusinessEvents) {
        @SuppressWarnings("unchecked")
        ArgumentCaptor<BusinessEvent<?>> businessEventArgumentCaptor = ArgumentCaptor.forClass(BusinessEvent.class);
        verify(businessEventNotifierService, times(expectedBusinessEvents)).notifyPostBusinessEvent(businessEventArgumentCaptor.capture());
        return businessEventArgumentCaptor;
    }

    private void verifyDatatableBusinessEvent(ArgumentCaptor<BusinessEvent<?>> businessEventArgumentCaptor, int index, Long entityId) {
        assertTrue(businessEventArgumentCaptor.getAllValues().get(index) instanceof DatatableEntryBusinessEvent);
        assertEquals(DATATABLE_NAME, ((DatatableEntryBusinessEvent) businessEventArgumentCaptor.getAllValues().get(index))
                .getDatatableEntryDetails().getDatatableName());
        assertEquals(entityId, ((DatatableEntryBusinessEvent) businessEventArgumentCaptor.getAllValues().get(index)).getAggregateRootId());
    }

    private JsonCommand createJsonCommand(final String jsonCommand, final Long resourceId) {
        return new JsonCommand(null, jsonCommand, null, null, null, resourceId, null, null, null, null, null, null, null, null, null, null,
                null, null);
    }
}
