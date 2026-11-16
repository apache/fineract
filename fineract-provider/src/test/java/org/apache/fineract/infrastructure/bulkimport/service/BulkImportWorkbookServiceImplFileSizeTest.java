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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.domain.ImportDocumentRepository;
import org.apache.fineract.infrastructure.bulkimport.mapping.ImportDocumentMapper;
import org.apache.fineract.infrastructure.contentstore.util.ContentPipe;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.TransactionBoundApplicationEventPublisher;
import org.apache.fineract.infrastructure.documentmanagement.service.DocumentWritePlatformService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.poi.openxml4j.util.ZipArchiveFakeEntry;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class BulkImportWorkbookServiceImplFileSizeTest {

    private static final int LOWERED_LIMIT = 50_000;

    private final TransactionBoundApplicationEventPublisher eventPublisher = mock(TransactionBoundApplicationEventPublisher.class);
    private final PlatformSecurityContext securityContext = mock(PlatformSecurityContext.class);
    private final ImportDocumentRepository importDocumentRepository = mock(ImportDocumentRepository.class);
    private final DocumentWritePlatformService documentWritePlatformService = mock(DocumentWritePlatformService.class);
    private final BulkImportWorkbookServiceImpl service = new BulkImportWorkbookServiceImpl(eventPublisher, securityContext,
            importDocumentRepository, documentWritePlatformService, mock(JdbcTemplate.class), mock(ContentPipe.class),
            mock(ImportDocumentMapper.class));

    private int originalLimit;

    @BeforeEach
    void lowerPoiLimit() {
        originalLimit = ZipArchiveFakeEntry.getMaxEntrySize();
        ZipArchiveFakeEntry.setMaxEntrySize(LOWERED_LIMIT);
    }

    @AfterEach
    void restorePoiLimit() {
        ZipArchiveFakeEntry.setMaxEntrySize(originalLimit);
    }

    @Test
    void oversizedXlsxIsRejectedBeforeAnyDocumentOrImportIsCreated() throws IOException {
        final byte[] bytes = officesXlsx(2000);
        final FormDataContentDisposition fileDetail = FormDataContentDisposition.name("file").fileName("offices.xlsx").size(bytes.length)
                .build();

        final PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> service.importWorkbook("offices", new ByteArrayInputStream(bytes), fileDetail, "en", "dd MMMM yyyy"));

        assertThat(exception.getErrors()).singleElement()
                .satisfies(error -> assertThat(error.getUserMessageGlobalisationCode()).isEqualTo("error.msg.bulk.import.file.too.large"));
        verifyNoInteractions(documentWritePlatformService, importDocumentRepository, eventPublisher, securityContext);
    }

    private static byte[] officesXlsx(final int rows) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            final Sheet sheet = workbook.createSheet(TemplatePopulateImportConstants.OFFICE_SHEET_NAME);
            sheet.createRow(0).createCell(0).setCellValue("Office Name*");
            for (int i = 1; i <= rows; i++) {
                final Row row = sheet.createRow(i);
                for (int c = 0; c < 10; c++) {
                    row.createCell(c).setCellValue("value " + i + "-" + c);
                }
            }
            workbook.write(out);
            return out.toByteArray();
        }
    }
}
