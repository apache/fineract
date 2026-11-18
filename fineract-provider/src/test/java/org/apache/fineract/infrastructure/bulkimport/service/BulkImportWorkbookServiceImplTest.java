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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType;
import org.apache.fineract.infrastructure.bulkimport.domain.ImportDocumentRepository;
import org.apache.fineract.infrastructure.bulkimport.mapping.ImportDocumentMapper;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.contentstore.util.ContentPipe;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.core.service.TransactionBoundApplicationEventPublisher;
import org.apache.fineract.infrastructure.documentmanagement.data.DocumentCreateResponse;
import org.apache.fineract.infrastructure.documentmanagement.service.DocumentWritePlatformService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BulkImportWorkbookServiceImplTest {

    private static final String OFFICES = GlobalEntityType.OFFICES.toString();
    private static final String LOCALE = "en";
    private static final String DATE_FORMAT = "dd MMMM yyyy";

    @Mock
    private TransactionBoundApplicationEventPublisher eventPublisher;
    @Mock
    private PlatformSecurityContext securityContext;
    @Mock
    private ImportDocumentRepository importDocumentRepository;
    @Mock
    private DocumentWritePlatformService writePlatformService;
    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private ContentPipe pipe;
    @Mock
    private ImportDocumentMapper mapper;
    @Mock
    private AppUser user;

    private BulkImportWorkbookServiceImpl service;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "UTC", null));
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.of(2026, 11, 6))));
        service = new BulkImportWorkbookServiceImpl(eventPublisher, securityContext, importDocumentRepository, writePlatformService,
                jdbcTemplate, pipe, mapper);
        when(securityContext.authenticatedUser()).thenReturn(user);
        when(user.getId()).thenReturn(1L);
        when(writePlatformService.createDocument(any())).thenReturn(DocumentCreateResponse.builder().resourceId(10L).build());
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @ParameterizedTest
    @ValueSource(strings = { "offices.xls", "OFFICES.XLS", "Offices.Xls" })
    void acceptsXlsInAnyCase(final String fileName) throws IOException {
        upload(fileName, workbookBytes(new HSSFWorkbook(), "Offices"));

        verify(writePlatformService).createDocument(any());
        verify(eventPublisher).publishEvent(any());
    }

    @ParameterizedTest
    @ValueSource(strings = { "offices.xlsx", "OFFICES.XLSX" })
    void acceptsXlsxInAnyCase(final String fileName) throws IOException {
        upload(fileName, workbookBytes(new XSSFWorkbook(), "Offices"));

        verify(writePlatformService).createDocument(any());
    }

    @Test
    void rejectsFileWithoutExtension() throws IOException {
        assertRejected(upload("offices_template", workbookBytes(new HSSFWorkbook(), "Offices")),
                "error.msg.bulk.import.file.extension.missing", "File has no extension - only .xls and .xlsx are accepted");
    }

    @Test
    void rejectsUnsupportedExtension() throws IOException {
        assertRejected(upload("offices.csv", workbookBytes(new HSSFWorkbook(), "Offices")), "error.msg.bulk.import.file.extension.invalid",
                "File extension .csv is not supported - only .xls and .xlsx are accepted");
    }

    @Test
    void rejectsFileNameLongerThanTheDocumentColumn() throws IOException {
        final String fileName = "x".repeat(BulkImportWorkbookServiceImpl.MAX_FILE_NAME_LENGTH - 3) + ".xls";

        assertRejected(upload(fileName, workbookBytes(new HSSFWorkbook(), "Offices")), "error.msg.bulk.import.file.name.too.long",
                "File name is too long (max 250 characters)");
    }

    @Test
    void acceptsFileNameAtTheDocumentColumnLimit() throws IOException {
        final String fileName = "x".repeat(BulkImportWorkbookServiceImpl.MAX_FILE_NAME_LENGTH - 4) + ".xls";

        upload(fileName, workbookBytes(new HSSFWorkbook(), "Offices"));

        verify(writePlatformService).createDocument(any());
    }

    @Test
    void rejectsWorkbookWithoutTheEntitySheet() throws IOException {
        assertRejected(upload("blank_workbook.xlsx", workbookBytes(new XSSFWorkbook(), "Sheet1")), "error.msg.bulk.import.sheet.not.found",
                "Sheet 'Offices' not found in the file");
    }

    @Test
    void rejectsContentThatIsNotAWorkbook() {
        assertRejected(upload("offices.xls", "Office Name,Parent Office\nA,Head Office\n".getBytes(StandardCharsets.UTF_8)),
                "error.msg.bulk.import.file.unreadable", "File could not be read as an Excel workbook");
    }

    @Test
    void rejectsCorruptWorkbook() throws IOException {
        final byte[] valid = workbookBytes(new XSSFWorkbook(), "Offices");
        final byte[] truncated = Arrays.copyOf(valid, valid.length / 3);

        assertRejected(upload("offices.xlsx", truncated), "error.msg.bulk.import.file.unreadable",
                "File could not be read as an Excel workbook");
    }

    @ParameterizedTest
    @EnumSource(value = GlobalEntityType.class, names = { "OFFICES", "CENTERS", "CHART_OF_ACCOUNTS", "CLIENTS_ENTITY", "CLIENTS_PERSON",
            "FIXED_DEPOSIT_ACCOUNTS", "FIXED_DEPOSIT_TRANSACTIONS", "GROUPS", "GUARANTORS", "GL_JOURNAL_ENTRIES", "LOANS",
            "LOAN_TRANSACTIONS", "RECURRING_DEPOSIT_ACCOUNTS", "RECURRING_DEPOSIT_ACCOUNTS_TRANSACTIONS", "SAVINGS_ACCOUNT",
            "SAVINGS_TRANSACTIONS", "SHARE_ACCOUNTS", "STAFF", "USERS" })
    void everyImportableEntityHasADataSheet(final GlobalEntityType entityType) {
        assertThat(BulkImportWorkbookServiceImpl.dataSheetName(entityType)).isNotBlank();
    }

    private PlatformApiDataValidationException upload(final String fileName, final byte[] content) {
        final FormDataContentDisposition fileDetail = FormDataContentDisposition.name("file").fileName(fileName).size(content.length)
                .build();
        try {
            service.importWorkbook(OFFICES, new ByteArrayInputStream(content), fileDetail, LOCALE, DATE_FORMAT);
            return null;
        } catch (PlatformApiDataValidationException e) {
            return e;
        }
    }

    private void assertRejected(final PlatformApiDataValidationException exception, final String code, final String message) {
        assertThat(exception).isNotNull();
        assertThat(exception.getErrors()).hasSize(1);
        final ApiParameterError error = exception.getErrors().get(0);
        assertThat(error.getUserMessageGlobalisationCode()).isEqualTo(code);
        assertThat(error.getDefaultUserMessage()).isEqualTo(message);
        assertThat(error.getParameterName()).isEqualTo("file");
        verify(writePlatformService, never()).createDocument(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    private static byte[] workbookBytes(final Workbook workbook, final String sheetName) throws IOException {
        try (workbook) {
            final Sheet sheet = workbook.createSheet(sheetName);
            sheet.createRow(0).createCell(0).setCellValue("Office Name");
            sheet.createRow(1).createCell(0).setCellValue("Branch");
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }
}
