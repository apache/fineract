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
package org.apache.fineract.infrastructure.bulkimport.importhandler.office;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import org.apache.fineract.infrastructure.bulkimport.constants.OfficeConstants;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OfficeImportHandlerParentOfficeTest {

    private final Workbook workbook = new HSSFWorkbook();
    private final Sheet sheet = workbook.createSheet("Offices");
    private Row row;

    @BeforeEach
    void setUp() {
        sheet.createRow(0);
        row = sheet.createRow(1);
        lookup(row, "Head Office", 1);
        lookup(sheet.createRow(2), "Branch B", 2);
    }

    @AfterEach
    void close() throws IOException {
        workbook.close();
    }

    @Test
    void typedParentIdOfAnotherListedOfficeIsRejected() {
        parent("Head Office", 2);
        assertThatThrownBy(() -> OfficeImportHandler.readParentOfficeId(row)).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Parent Office 'Head Office' does not match Parent OfficeId 2");
    }

    @Test
    void decimalParentIdIsRejected() {
        parent("Head Office", 1.5);
        assertThatThrownBy(() -> OfficeImportHandler.readParentOfficeId(row)).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Parent OfficeId must be a whole number");
    }

    @Test
    void decimalParentIdTypedAsTextIsRejected() {
        row.createCell(OfficeConstants.PARENT_OFFICE_NAME_COL).setCellValue("Head Office");
        row.createCell(OfficeConstants.PARENT_OFFICE_ID_COL).setCellValue("1.5");
        assertThatThrownBy(() -> OfficeImportHandler.readParentOfficeId(row)).hasMessage("Parent OfficeId must be a whole number");
    }

    @Test
    void typedParentIdMatchingTheNamedOfficeIsAccepted() {
        parent("Branch B", 2);
        assertThat(OfficeImportHandler.readParentOfficeId(row)).isEqualTo(2L);
    }

    @Test
    void typedParentIdThatIsNotListedIsPassedOn() {
        parent("Head Office", 99);
        assertThat(OfficeImportHandler.readParentOfficeId(row)).isEqualTo(99L);
    }

    private void parent(String name, double id) {
        row.createCell(OfficeConstants.PARENT_OFFICE_NAME_COL).setCellValue(name);
        row.createCell(OfficeConstants.PARENT_OFFICE_ID_COL).setCellValue(id);
    }

    private static void lookup(Row lookupRow, String name, long id) {
        lookupRow.createCell(OfficeConstants.LOOKUP_OFFICE_COL).setCellValue(name);
        lookupRow.createCell(OfficeConstants.LOOKUP_OFFICE_ID_COL).setCellValue(id);
    }
}
