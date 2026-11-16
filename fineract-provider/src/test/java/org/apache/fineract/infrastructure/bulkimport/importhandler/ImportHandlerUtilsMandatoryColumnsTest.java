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
package org.apache.fineract.infrastructure.bulkimport.importhandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import org.apache.fineract.infrastructure.bulkimport.importhandler.ImportHandlerUtils.MandatoryColumn;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;

class ImportHandlerUtilsMandatoryColumnsTest {

    @Test
    void requireColumnsNamesEveryEmptyColumn() throws IOException {
        try (Workbook workbook = new HSSFWorkbook()) {
            Row row = workbook.createSheet("Data").createRow(1);
            row.createCell(0).setCellValue("filled");
            row.createCell(1).setCellValue("  ");

            assertThatThrownBy(() -> ImportHandlerUtils.requireColumns(row, new MandatoryColumn(0, "First"),
                    new MandatoryColumn(1, "Second"), new MandatoryColumn(2, "Third"))).isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Second is mandatory, Third is mandatory");
            assertThatNoException().isThrownBy(() -> ImportHandlerUtils.requireColumns(row, new MandatoryColumn(0, "First")));
        }
    }

    @Test
    void readAsMandatoryBooleanAcceptsOnlyTrueOrFalse() throws IOException {
        try (Workbook workbook = new HSSFWorkbook()) {
            Row row = workbook.createSheet("Data").createRow(1);
            row.createCell(0).setCellValue("true");
            row.createCell(1).setCellValue("FALSE");
            row.createCell(2).setCellValue(true);
            row.createCell(3).setCellValue("maybe");
            row.createCell(4).setCellValue(1);

            assertThat(ImportHandlerUtils.readAsMandatoryBoolean(0, row, "Active")).isTrue();
            assertThat(ImportHandlerUtils.readAsMandatoryBoolean(1, row, "Active")).isFalse();
            assertThat(ImportHandlerUtils.readAsMandatoryBoolean(2, row, "Active")).isTrue();
            assertThatThrownBy(() -> ImportHandlerUtils.readAsMandatoryBoolean(3, row, "Active"))
                    .hasMessage("Active must be TRUE or FALSE");
            assertThatThrownBy(() -> ImportHandlerUtils.readAsMandatoryBoolean(4, row, "Active"))
                    .hasMessage("Active must be TRUE or FALSE");
            assertThatThrownBy(() -> ImportHandlerUtils.readAsMandatoryBoolean(5, row, "Active")).hasMessage("Active is mandatory");
        }
    }

    @Test
    void rowsAreCountedPastAnEmptyKeyColumn() throws IOException {
        try (Workbook workbook = new HSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Data");
            sheet.createRow(0).createCell(0).setCellValue("header");
            for (int rowIndex = 1; rowIndex <= 3; rowIndex++) {
                Row row = sheet.createRow(rowIndex);
                row.createCell(0).setCellValue("value " + rowIndex);
                if (rowIndex != 2) {
                    row.createCell(4).setCellValue("key " + rowIndex);
                }
            }
            sheet.createRow(4).createCell(9).setCellValue("outside the data columns");

            assertThat(ImportHandlerUtils.getNumberOfRows(sheet, 4)).isEqualTo(1);
            assertThat(ImportHandlerUtils.getNumberOfRows(sheet, 0, 8)).isEqualTo(3);
        }
    }
}
