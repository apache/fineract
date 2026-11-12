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
package org.apache.fineract.infrastructure.bulkimport.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.command.core.CommandDispatcher;
import org.apache.fineract.infrastructure.bulkimport.data.BulkImportEvent;
import org.apache.fineract.infrastructure.bulkimport.data.Count;
import org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType;
import org.apache.fineract.infrastructure.bulkimport.domain.ImportDocument;
import org.apache.fineract.infrastructure.bulkimport.domain.ImportDocumentRepository;
import org.apache.fineract.infrastructure.bulkimport.importhandler.ImportHandler;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.contentstore.util.ContentPipe;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationContext;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BulkImportEventListenerTest {

    private static final String DATE_FORMAT = "dd MMMM yyyy";
    private static final int TOTAL_RECORDS = 3;

    @Mock
    private ApplicationContext applicationContext;
    @Mock
    private ImportDocumentRepository importRepository;
    @Mock
    private ContentPipe pipe;
    @Mock
    private CommandDispatcher dispatcher;
    @Mock
    private ImportHandler importHandler;

    private Workbook workbook;
    private ImportDocument importDocument;
    private BulkImportEvent event;
    private BulkImportEventListener listener;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "UTC", null));
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.of(2026, 11, 5))));
        when(applicationContext.getBean(anyString(), eq(ImportHandler.class))).thenReturn(importHandler);
        workbook = new XSSFWorkbook();
        importDocument = ImportDocument.instance(1L, LocalDateTime.of(2026, 11, 5, 9, 55), GlobalEntityType.CENTERS.getValue(),
                mock(AppUser.class), TOTAL_RECORDS);
        event = new BulkImportEvent(this, workbook, "CLIENTS_ENTITY_TEMPLATE.xls", "application/vnd.ms-excel", importDocument, "en",
                DATE_FORMAT, ThreadLocalContextUtil.getContext(), 1L);
        listener = new BulkImportEventListener(applicationContext, importRepository, pipe, dispatcher);
    }

    @AfterEach
    void tearDown() throws Exception {
        workbook.close();
        ThreadLocalContextUtil.reset();
    }

    @Test
    void importThatStopsBeforeCountingCountsEveryRecordAsFailed() {
        when(importHandler.process(any(), any(), any())).thenThrow(new NullPointerException("sheet is null"));

        listener.onApplicationEvent(event);

        assertThat(importDocument.getCompleted()).isTrue();
        assertThat(importDocument.getSuccessCount()).isZero();
        assertThat(importDocument.getFailureCount()).isEqualTo(TOTAL_RECORDS);
        verify(importRepository).saveAndFlush(importDocument);
    }

    @Test
    void importThatStopsAfterCountingKeepsItsCounts() {
        when(importHandler.process(any(), any(), any())).thenReturn(Count.instance(2, 1));
        when(dispatcher.dispatch(any())).thenThrow(new IllegalStateException("result file not stored"));

        listener.onApplicationEvent(event);

        assertThat(importDocument.getCompleted()).isTrue();
        assertThat(importDocument.getSuccessCount()).isEqualTo(2);
        assertThat(importDocument.getFailureCount()).isEqualTo(1);
        verify(importRepository).saveAndFlush(importDocument);
    }

    @Test
    void finishedImportIsSavedOnceWithItsCounts() {
        when(importHandler.process(any(), any(), any())).thenReturn(Count.instance(2, 1));
        when(dispatcher.dispatch(any())).thenReturn(() -> null);

        listener.onApplicationEvent(event);

        assertThat(importDocument.getSuccessCount()).isEqualTo(2);
        assertThat(importDocument.getFailureCount()).isEqualTo(1);
        verify(importRepository).saveAndFlush(importDocument);
    }
}
