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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.serialization.DatatableCommandFromApiJsonDeserializer;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.core.service.database.DatabaseTypeResolver;
import org.apache.fineract.infrastructure.dataqueries.data.DataTableValidator;
import org.apache.fineract.infrastructure.dataqueries.data.EntityTables;
import org.apache.fineract.infrastructure.dataqueries.data.GenericResultsetData;
import org.apache.fineract.infrastructure.dataqueries.data.ResultsetColumnHeaderData;
import org.apache.fineract.infrastructure.dataqueries.data.ResultsetRowData;
import org.apache.fineract.infrastructure.event.business.domain.BusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.datatable.DatatableEntryBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.portfolio.search.service.SearchUtil;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DatatableBusinessEventTest {

    private static final String DATATABLE_NAME = "test_loan_data";
    private static final long APP_TABLE_ID = 1L;
    private static final long DATATABLE_ID = 1L;

    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private DatabaseTypeResolver databaseTypeResolver;
    @Mock
    private DatabaseSpecificSQLGenerator sqlGenerator;
    @Mock
    private PlatformSecurityContext context;
    @Mock
    private FromJsonHelper fromJsonHelper;
    @Mock
    private GenericDataService genericDataService;
    @Mock
    private DatatableCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    @Mock
    private ConfigurationDomainService configurationDomainService;
    @Mock
    private CodeReadPlatformService codeReadPlatformService;
    @Mock
    private DataTableValidator dataTableValidator;
    @Mock
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    @Mock
    private DatatableKeywordGenerator datatableKeywordGenerator;
    @Mock
    private SearchUtil searchUtil;
    @Mock
    private BusinessEventNotifierService businessEventNotifierService;
    @Mock
    private DatatableReadService datatableReadService;
    @Mock
    private DatatableUtil datatableUtil;

    @InjectMocks
    private DatatableWriteServiceImpl underTest;

    @BeforeEach
    void setUp() {
        setupThreadLocalContext();
        setupSecurityContext();
        setupEntityTable();
        setupCommandProcessingResult();
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void shouldNotifyBusinessEventAfterCreatingNewDatatableEntry() {
        setupCreateDatatableEntryMocks();
        JsonCommand command = createJsonCommand("{}", APP_TABLE_ID);

        underTest.createNewDatatableEntry(DATATABLE_NAME, APP_TABLE_ID, command);

        verifyBusinessEvent(APP_TABLE_ID);
    }

    @Test
    void shouldNotifyBusinessEventAfterUpdatingDatatableEntry() {
        setupUpdateDeleteDatatableMocks();
        JsonCommand command = createJsonCommand("{}", APP_TABLE_ID);

        underTest.updateDatatableEntryOneToMany(DATATABLE_NAME, APP_TABLE_ID, DATATABLE_ID, command);

        verifyBusinessEvent(APP_TABLE_ID);
    }

    @Test
    void shouldNotifyBusinessEventAfterDeletingDatatableEntry() {
        setupUpdateDeleteDatatableMocks();
        JsonCommand command = createJsonCommand("{}", APP_TABLE_ID);

        underTest.deleteDatatableEntry(DATATABLE_NAME, APP_TABLE_ID, DATATABLE_ID, command);

        verifyBusinessEvent(APP_TABLE_ID);
    }

    private void setupThreadLocalContext() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        Map<BusinessDateType, LocalDate> businessDates = Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.parse("2024-01-16"),
                BusinessDateType.COB_DATE, LocalDate.parse("2024-01-15"));
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(businessDates));
    }

    private void setupSecurityContext() {
        AppUser currentUser = Mockito.mock(AppUser.class);
        Office office = Mockito.mock(Office.class);
        when(context.authenticatedUser()).thenReturn(currentUser);
        when(currentUser.getOffice()).thenReturn(office);
        when(office.getHierarchy()).thenReturn(".");
    }

    private void setupEntityTable() {
        EntityTables entityTable = Mockito.mock(EntityTables.class);
        when(entityTable.getForeignKeyColumnNameOnDatatable()).thenReturn("loan_id");
        when(datatableUtil.queryForApplicationEntity(anyString())).thenReturn(entityTable);
    }

    private void setupCommandProcessingResult() {
        CommandProcessingResult commandProcessingResult = Mockito.mock(CommandProcessingResult.class);
        when(commandProcessingResult.getOfficeId()).thenReturn(1L);
        when(datatableUtil.checkMainResourceExistsWithinScope(any(EntityTables.class), anyLong())).thenReturn(commandProcessingResult);
    }

    private void setupCreateDatatableEntryMocks() {
        List<ResultsetColumnHeaderData> columnHeaders = new ArrayList<>();
        when(genericDataService.fillResultsetColumnHeaders(anyString())).thenReturn(columnHeaders);

        when(jdbcTemplate.update(any(PreparedStatementCreator.class), any(KeyHolder.class))).thenReturn(1);

        GeneratedKeyHolder keyHolder = Mockito.mock(GeneratedKeyHolder.class);
        when(keyHolder.getKeys()).thenReturn(Map.of("id", 1L));
        when(sqlGenerator.fetchPK(any())).thenReturn(1L);

        when(fromJsonHelper.extractDataMap(any(), anyString())).thenReturn(new HashMap<>());
    }

    private void setupUpdateDeleteDatatableMocks() {
        List<ResultsetColumnHeaderData> columnHeaders = new ArrayList<>();
        List<Object> rowValues = new ArrayList<>();
        rowValues.add(1L);

        List<ResultsetRowData> rows = new ArrayList<>();
        rows.add(ResultsetRowData.create(rowValues));

        GenericResultsetData resultData = new GenericResultsetData(columnHeaders, rows);

        Mockito.doReturn(resultData).when(datatableUtil).retrieveDataTableGenericResultSet(any(EntityTables.class), eq(DATATABLE_NAME),
                eq(APP_TABLE_ID), isNull(), eq(DATATABLE_ID));

        when(genericDataService.fillResultsetRowData(anyString(), any())).thenReturn(rows);
    }

    private void verifyBusinessEvent(Long expectedEntityId) {
        ArgumentCaptor<BusinessEvent<?>> eventCaptor = ArgumentCaptor.forClass(BusinessEvent.class);
        verify(businessEventNotifierService, times(1)).notifyPostBusinessEvent(eventCaptor.capture());

        BusinessEvent<?> capturedEvent = eventCaptor.getValue();
        assertInstanceOf(DatatableEntryBusinessEvent.class, capturedEvent);
        DatatableEntryBusinessEvent event = (DatatableEntryBusinessEvent) capturedEvent;

        assertEquals(DATATABLE_NAME, event.getDatatableEntryDetails().getDatatableName());
        assertEquals(expectedEntityId, event.getAggregateRootId());
    }

    private JsonCommand createJsonCommand(final String jsonCommand, final Long resourceId) {
        return new JsonCommand(null, jsonCommand, null, null, null, resourceId, null, null, null, null, null, null, null, null, null, null,
                null, null);
    }
}
