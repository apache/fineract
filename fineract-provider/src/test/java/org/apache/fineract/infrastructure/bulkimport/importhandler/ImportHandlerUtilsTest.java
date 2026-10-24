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

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;

class ImportHandlerUtilsTest {

    @Test
    void testReadAsDateWithNumericDate() throws IOException {
        try (Workbook workbook = new HSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Test");
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);

            LocalDate expectedDate = LocalDate.of(2023, 5, 15);
            Date date = Date.from(expectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            cell.setCellValue(date);

            LocalDate actualDate = ImportHandlerUtils.readAsDate(0, row);
            assertThat(actualDate).isEqualTo(expectedDate);
        }
    }

    @Test
    void testReadAsDateWithStringDateCell() throws IOException {
        try (Workbook workbook = new HSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Test");
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);

            cell.setCellValue("2023-05-15");

            LocalDate actualDate = ImportHandlerUtils.readAsDate(0, row);
            assertThat(actualDate).isEqualTo(LocalDate.of(2023, 5, 15));
        }
    }

    @Test
    void testReadAsDateWithStringFormulaCell() throws IOException {
        try (Workbook workbook = new HSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Test");
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);

            // String formula cell that previously caused "Cannot get a NUMERIC value from a STRING formula cell"
            cell.setCellFormula("\"2023-05-15\"");

            LocalDate actualDate = ImportHandlerUtils.readAsDate(0, row);
            assertThat(actualDate).isEqualTo(LocalDate.of(2023, 5, 15));
        }
    }

    @Test
    void testReadAsDateWithEmptyStringFormulaCell() throws IOException {
        try (Workbook workbook = new HSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Test");
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);

            cell.setCellFormula("\"\"");

            LocalDate actualDate = ImportHandlerUtils.readAsDate(0, row);
            assertThat(actualDate).isNull();
        }
    }

    @Test
    void testReadAsDateWithCustomFormat() throws IOException {
        try (Workbook workbook = new HSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Test");
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);

            cell.setCellValue("15/05/2023");

            LocalDate actualDate = ImportHandlerUtils.readAsDate(0, row, "dd/MM/yyyy", "en");
            assertThat(actualDate).isEqualTo(LocalDate.of(2023, 5, 15));
        }
    }

    @Test
    void testReadAsDateWithBlankCell() throws IOException {
        try (Workbook workbook = new HSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Test");
            Row row = sheet.createRow(0);
            row.createCell(0);

            LocalDate actualDate = ImportHandlerUtils.readAsDate(0, row);
            assertThat(actualDate).isNull();

            // Null cell
            assertThat(ImportHandlerUtils.readAsDate(1, row)).isNull();
        }
    }

    @Test
    void testReadAsStringWithFormulas() throws IOException {
        try (Workbook workbook = new HSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Test");
            Row row = sheet.createRow(0);

            Cell strFormulaCell = row.createCell(0);
            strFormulaCell.setCellFormula("\"Hello World\"");
            assertThat(ImportHandlerUtils.readAsString(0, row)).isEqualTo("Hello World");

            Cell numFormulaCell = row.createCell(1);
            numFormulaCell.setCellFormula("10 + 20");
            assertThat(ImportHandlerUtils.readAsString(1, row)).isEqualTo("30");

            Cell boolFormulaCell = row.createCell(2);
            boolFormulaCell.setCellFormula("1 = 1");
            assertThat(ImportHandlerUtils.readAsString(2, row)).isEqualTo("true");
        }
    }

    @Test
    void testReadAsLongWithFormulas() throws IOException {
        try (Workbook workbook = new HSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Test");
            Row row = sheet.createRow(0);

            Cell numFormulaCell = row.createCell(0);
            numFormulaCell.setCellFormula("100 + 50");
            assertThat(ImportHandlerUtils.readAsLong(0, row)).isEqualTo(150L);

            Cell strFormulaCell = row.createCell(1);
            strFormulaCell.setCellFormula("\"200\"");
            assertThat(ImportHandlerUtils.readAsLong(1, row)).isEqualTo(200L);

            Cell emptyStrFormulaCell = row.createCell(2);
            emptyStrFormulaCell.setCellFormula("\"\"");
            assertThat(ImportHandlerUtils.readAsLong(2, row)).isNull();
        }
    }

    @Test
    void testReadAsIntWithFormulas() throws IOException {
        try (Workbook workbook = new HSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Test");
            Row row = sheet.createRow(0);

            Cell numFormulaCell = row.createCell(0);
            numFormulaCell.setCellFormula("40 + 2");
            assertThat(ImportHandlerUtils.readAsInt(0, row)).isEqualTo(42);

            Cell strFormulaCell = row.createCell(1);
            strFormulaCell.setCellFormula("\"42\"");
            assertThat(ImportHandlerUtils.readAsInt(1, row)).isEqualTo(42);

            Cell emptyStrFormulaCell = row.createCell(2);
            emptyStrFormulaCell.setCellFormula("\"\"");
            assertThat(ImportHandlerUtils.readAsInt(2, row)).isNull();
        }
    }

    @Test
    void testReadAsDoubleWithFormulas() throws IOException {
        try (Workbook workbook = new HSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Test");
            Row row = sheet.createRow(0);

            Cell numFormulaCell = row.createCell(0);
            numFormulaCell.setCellFormula("10.5 + 4.5");
            assertThat(ImportHandlerUtils.readAsDouble(0, row)).isEqualTo(15.0);

            Cell strFormulaCell = row.createCell(1);
            strFormulaCell.setCellFormula("\"15.5\"");
            assertThat(ImportHandlerUtils.readAsDouble(1, row)).isEqualTo(15.5);

            Cell emptyStrFormulaCell = row.createCell(2);
            emptyStrFormulaCell.setCellFormula("\"\"");
            assertThat(ImportHandlerUtils.readAsDouble(2, row)).isEqualTo(0.0);
        }
    }

    @Test
    void testReadAsBooleanWithFormulas() throws IOException {
        try (Workbook workbook = new HSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Test");
            Row row = sheet.createRow(0);

            Cell boolFormulaCell = row.createCell(0);
            boolFormulaCell.setCellFormula("5 > 2");
            assertThat(ImportHandlerUtils.readAsBoolean(0, row)).isTrue();

            Cell strFormulaCell = row.createCell(1);
            strFormulaCell.setCellFormula("\"true\"");
            assertThat(ImportHandlerUtils.readAsBoolean(1, row)).isTrue();

            Cell falseFormulaCell = row.createCell(2);
            falseFormulaCell.setCellFormula("\"false\"");
            assertThat(ImportHandlerUtils.readAsBoolean(2, row)).isFalse();
        }
    }
}
