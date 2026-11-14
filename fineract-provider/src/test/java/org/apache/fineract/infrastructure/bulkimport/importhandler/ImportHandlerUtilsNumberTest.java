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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ImportHandlerUtilsNumberTest {

    private final Workbook workbook = new HSSFWorkbook();
    private final Sheet sheet = workbook.createSheet("Sheet");
    private Row row;

    @BeforeEach
    void setUp() {
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Parent OfficeId*");
        header.createCell(1).setCellValue("Amount *");
        header.createCell(2).setCellValue("Mobile number");
        row = sheet.createRow(1);
    }

    @AfterEach
    void close() throws IOException {
        workbook.close();
    }

    @Test
    void textInALongColumnNamesTheColumnHeader() {
        row.createCell(0).setCellValue("abc");
        assertThatThrownBy(() -> ImportHandlerUtils.readAsLong(0, row)).isInstanceOf(NumberFormatException.class)
                .hasMessage("Parent OfficeId must be a number");
    }

    @Test
    void textInADoubleColumnNamesTheColumnHeader() {
        row.createCell(1).setCellValue("abc");
        assertThatThrownBy(() -> ImportHandlerUtils.readAsDouble(1, row)).hasMessage("Amount must be a number");
    }

    @Test
    void textInAnIntColumnWithoutHeaderNamesTheColumnLetter() {
        row.createCell(3).setCellValue("x1");
        assertThatThrownBy(() -> ImportHandlerUtils.readAsInt(3, row)).hasMessage("Column D must be a number");
    }

    @Test
    void numbersTypedAsTextAreRead() {
        row.createCell(0).setCellValue(" 12 ");
        row.createCell(1).setCellValue("10.5");
        row.createCell(3).setCellValue("7");
        assertThat(ImportHandlerUtils.readAsLong(0, row)).isEqualTo(12L);
        assertThat(ImportHandlerUtils.readAsDouble(1, row)).isEqualTo(10.5);
        assertThat(ImportHandlerUtils.readAsInt(3, row)).isEqualTo(7);
    }

    @Test
    void emptyTextReadsLikeAnEmptyCell() {
        row.createCell(0).setCellValue("");
        row.createCell(1).setCellValue(" ");
        row.createCell(3).setCellValue("");
        assertThat(ImportHandlerUtils.readAsLong(0, row)).isNull();
        assertThat(ImportHandlerUtils.readAsDouble(1, row)).isZero();
        assertThat(ImportHandlerUtils.readAsInt(3, row)).isNull();
    }

    @Test
    void booleanInANumberColumnIsNotANumber() {
        row.createCell(0).setCellValue(true);
        assertThatThrownBy(() -> ImportHandlerUtils.readAsLong(0, row)).hasMessage("Parent OfficeId must be a number");
    }

    @Test
    void digitsAreKeptAsTyped() {
        row.createCell(2).setCellValue("0791234");
        assertThat(ImportHandlerUtils.readAsDigits(2, row, "Mobile number")).isEqualTo("0791234");
        row.createCell(2).setCellValue(96170123456d);
        assertThat(ImportHandlerUtils.readAsDigits(2, row, "Mobile number")).isEqualTo("96170123456");
        row.createCell(2).setCellValue(" ");
        assertThat(ImportHandlerUtils.readAsDigits(2, row, "Mobile number")).isNull();
    }

    @Test
    void unknownOfficeFailsNamingTheOffice() {
        assertThat(ImportHandlerUtils.requireLookupId(4L, "Office", "Branch A")).isEqualTo(4L);
        assertThatThrownBy(() -> ImportHandlerUtils.requireLookupId(0L, "Office", "Branch X")).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Office `Branch X` not found");
        assertThatThrownBy(() -> ImportHandlerUtils.requireLookupId(null, "Office", null)).hasMessage("Office is required");
    }

    @Test
    void rowCountOverAColumnRangeDoesNotStopAtOneEmptyColumn() {
        row.createCell(0).setCellValue("Deposit");
        Row second = sheet.createRow(2);
        second.createCell(0).setCellValue("Deposit");
        second.createCell(1).setCellValue(" ");
        Row third = sheet.createRow(3);
        third.createCell(2).setCellValue("note");
        sheet.createRow(4);
        Row afterGap = sheet.createRow(5);
        afterGap.createCell(0).setCellValue("Deposit");
        assertThat(ImportHandlerUtils.getNumberOfRows(sheet, 1)).isZero();
        assertThat(ImportHandlerUtils.getNumberOfRows(sheet, 0, 2)).isEqualTo(3);
    }

    @Test
    void anythingButDigitsFailsNamingTheColumn() {
        row.createCell(2).setCellValue("abcdef");
        assertThatThrownBy(() -> ImportHandlerUtils.readAsDigits(2, row, "Mobile number")).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Mobile number must contain digits only");
        row.createCell(2).setCellValue("+961 70");
        assertThatThrownBy(() -> ImportHandlerUtils.readAsDigits(2, row, "Mobile number"))
                .hasMessage("Mobile number must contain digits only");
    }

    @Test
    void jdkExceptionTextIsNotShownAsRowStatus() {
        assertThat(ImportHandlerUtils
                .getErrorMessage(new NullPointerException("Cannot invoke \"java.lang.Long.longValue()\" because \"officeId\" is null")))
                .isEqualTo("A value in this row is missing or invalid");
        assertThat(ImportHandlerUtils.getErrorMessage(new NullPointerException())).isEqualTo("A value in this row is missing or invalid");
        assertThat(ImportHandlerUtils.getErrorMessage(new NullPointerException("Group ID is required to resume the meeting")))
                .isEqualTo("Group ID is required to resume the meeting");
        assertThat(ImportHandlerUtils.getErrorMessage(new NumberFormatException("For input string: \"abc\"")))
                .isEqualTo("A number in this row is empty or not a number");
        assertThat(ImportHandlerUtils.getErrorMessage(new NumberFormatException("empty String")))
                .isEqualTo("A number in this row is empty or not a number");
        assertThat(ImportHandlerUtils.getErrorMessage(new NumberFormatException("Amount must be a number")))
                .isEqualTo("Amount must be a number");
        assertThat(ImportHandlerUtils.getErrorMessage(new IllegalArgumentException("Amount is mandatory")))
                .isEqualTo("Amount is mandatory");
    }
}
