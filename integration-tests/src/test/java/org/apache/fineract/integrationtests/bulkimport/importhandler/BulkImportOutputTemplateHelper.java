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
package org.apache.fineract.integrationtests.bulkimport.importhandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.function.Supplier;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public final class BulkImportOutputTemplateHelper {

    private static final long DEFAULT_MAX_WAIT_MILLIS = 10000;
    private static final long POLL_INTERVAL_MILLIS = 500;

    private BulkImportOutputTemplateHelper() {}

    public static Workbook waitForWorkbook(Supplier<byte[]> downloader, String sheetName, int rowIndex, int statusColumn)
            throws InterruptedException, IOException {
        return waitForWorkbook(downloader, sheetName, rowIndex, statusColumn, DEFAULT_MAX_WAIT_MILLIS);
    }

    public static Workbook waitForWorkbook(Supplier<byte[]> downloader, String sheetName, int rowIndex, int statusColumn,
            long maxWaitMillis) throws InterruptedException, IOException {
        long start = System.currentTimeMillis();
        DataFormatter formatter = new DataFormatter();
        RuntimeException lastException = null;

        while ((System.currentTimeMillis() - start) < maxWaitMillis) {
            try {
                Workbook workbook = new HSSFWorkbook(new ByteArrayInputStream(downloader.get()));
                Sheet sheet = workbook.getSheet(sheetName);
                Row row = sheet == null ? null : sheet.getRow(rowIndex);
                if (row != null && StringUtils.isNotBlank(formatter.formatCellValue(row.getCell(statusColumn)))) {
                    return workbook;
                }
                workbook.close();
            } catch (IOException | RuntimeException e) {
                lastException = new RuntimeException(e);
            }
            Thread.sleep(POLL_INTERVAL_MILLIS);
        }

        RuntimeException exception = new RuntimeException("Cannot find processed bulk import output workbook for sheet: " + sheetName);
        if (lastException != null) {
            exception.addSuppressed(lastException);
        }
        throw exception;
    }
}
