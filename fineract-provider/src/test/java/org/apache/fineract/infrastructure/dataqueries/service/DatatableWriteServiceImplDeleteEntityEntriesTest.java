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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.dataqueries.data.EntityTables;
import org.apache.fineract.infrastructure.event.business.domain.datatable.DatatableEntryDeletedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class DatatableWriteServiceImplDeleteEntityEntriesTest {

    private static final String REGISTERED_TABLES_SQL = "SELECT registered_table_name FROM x_registered_table WHERE application_table_name = ?";
    private static final String DELETE_A_SQL = "DELETE FROM \"dt_wc_a\" WHERE \"wc_loan_id\" = ?";
    private static final String DELETE_B_SQL = "DELETE FROM \"dt_wc_b\" WHERE \"wc_loan_id\" = ?";

    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private DatabaseSpecificSQLGenerator sqlGenerator;
    @Mock
    private DatatableUtil datatableUtil;
    @Mock
    private BusinessEventNotifierService businessEventNotifierService;

    @InjectMocks
    private DatatableWriteServiceImpl underTest;

    @Test
    void deletesTheEntityRowsFromEveryRegisteredDatatable() {
        when(jdbcTemplate.queryForList(REGISTERED_TABLES_SQL, String.class, "m_wc_loan")).thenReturn(List.of("dt_wc_a", "dt_wc_b"));
        when(datatableUtil.getFKField(EntityTables.WC_LOAN)).thenReturn("wc_loan_id");
        when(sqlGenerator.escape(anyString())).thenAnswer(invocation -> "\"" + invocation.getArgument(0) + "\"");
        when(jdbcTemplate.update(DELETE_A_SQL, 7L)).thenReturn(2);
        when(jdbcTemplate.update(DELETE_B_SQL, 7L)).thenReturn(0);

        underTest.deleteDatatableEntriesOfEntity(EntityTables.WC_LOAN, 7L);

        verify(datatableUtil).validateDatatableName("dt_wc_a");
        verify(datatableUtil).validateDatatableName("dt_wc_b");
        verify(jdbcTemplate).update(DELETE_A_SQL, 7L);
        verify(jdbcTemplate).update(DELETE_B_SQL, 7L);
        // the entity datatable check of a single entry delete is not asked
        verify(jdbcTemplate, never()).queryForObject(anyString(), eq(Long.class), anyString());
        // one event, for the datatable that had entries
        ArgumentCaptor<DatatableEntryDeletedBusinessEvent> event = ArgumentCaptor.forClass(DatatableEntryDeletedBusinessEvent.class);
        verify(businessEventNotifierService).notifyPostBusinessEvent(event.capture());
        assertEquals("dt_wc_a", event.getValue().get().getDatatableName());
        assertEquals(EntityTables.WC_LOAN, event.getValue().get().getEntityType());
        assertEquals(7L, event.getValue().get().getAppTableId());
    }

    @Test
    void deletesNothingWhenNoDatatableIsRegistered() {
        when(jdbcTemplate.queryForList(REGISTERED_TABLES_SQL, String.class, "m_wc_loan")).thenReturn(List.of());

        underTest.deleteDatatableEntriesOfEntity(EntityTables.WC_LOAN, 7L);

        verify(jdbcTemplate, never()).update(anyString(), eq(7L));
        verify(businessEventNotifierService, never()).notifyPostBusinessEvent(any());
    }
}
