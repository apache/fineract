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
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.util.ZipArchiveFakeEntry;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

class BulkImportFileSizeCheckTest {

    @Test
    void rejectsXlsxWhoseUnpackedSheetIsAboveTheLimit() throws IOException {
        final byte[] bytes = workbookBytes(new XSSFWorkbook(), 2000);

        final PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> BulkImportFileSizeCheck.requireReadablePartSizes(bytes, 50_000));

        final ApiParameterError error = exception.getErrors().get(0);
        assertThat(error.getUserMessageGlobalisationCode()).isEqualTo("error.msg.bulk.import.file.too.large");
        assertThat(error.getParameterName()).isEqualTo("file");
        assertThat(error.getDefaultUserMessage()).matches(
                "The file is too large to import: one of its sheets holds [0-9.]+ MB of data once unpacked, above the 0.1 MB limit. "
                        + "Split the rows into at least [0-9]+ files and upload them one at a time.");
    }

    @Test
    void minimumFileCountIsRoundedUp() throws IOException {
        final byte[] bytes = workbookBytes(new XSSFWorkbook(), 2000);
        final long largestPart = largestPartSize(bytes);

        final PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> BulkImportFileSizeCheck.requireReadablePartSizes(bytes, largestPart - 1));

        assertThat(exception.getErrors().get(0).getDefaultUserMessage()).contains("at least 2 files");
        assertDoesNotThrow(() -> BulkImportFileSizeCheck.requireReadablePartSizes(bytes, largestPart));
    }

    @Test
    void xlsxWithinPoiLimitPasses() throws IOException {
        final byte[] bytes = workbookBytes(new XSSFWorkbook(), 10);

        assertDoesNotThrow(() -> BulkImportFileSizeCheck.requireReadablePartSizes(bytes, ZipArchiveFakeEntry.getMaxEntrySize()));
    }

    @Test
    void xlsIsNotAZipAndSkipsTheCheck() throws IOException {
        final byte[] bytes = workbookBytes(new HSSFWorkbook(), 10);

        assertDoesNotThrow(() -> BulkImportFileSizeCheck.requireReadablePartSizes(bytes, 1));
    }

    @Test
    void unreadableZipIsLeftToTheWorkbookReader() {
        final byte[] bytes = { 'P', 'K', 3, 4, 0, 0, 0, 0 };

        assertDoesNotThrow(() -> BulkImportFileSizeCheck.requireReadablePartSizes(bytes, 1));
    }

    private static byte[] workbookBytes(final Workbook workbook, final int rows) throws IOException {
        try (workbook) {
            final Sheet sheet = workbook.createSheet("Loans");
            for (int i = 0; i < rows; i++) {
                final Row row = sheet.createRow(i);
                for (int c = 0; c < 10; c++) {
                    row.createCell(c).setCellValue("value " + i + "-" + c);
                }
            }
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private static long largestPartSize(final byte[] bytes) throws IOException {
        long largest = 0;
        try (ZipFile zip = ZipFile.builder().setSeekableByteChannel(new SeekableInMemoryByteChannel(bytes)).get()) {
            for (Enumeration<ZipArchiveEntry> entries = zip.getEntries(); entries.hasMoreElements();) {
                largest = Math.max(largest, entries.nextElement().getSize());
            }
        }
        return largest;
    }
}
