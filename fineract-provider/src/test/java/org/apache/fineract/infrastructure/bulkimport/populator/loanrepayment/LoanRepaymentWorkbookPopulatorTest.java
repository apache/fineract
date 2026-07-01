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
package org.apache.fineract.infrastructure.bulkimport.populator.loanrepayment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.infrastructure.bulkimport.constants.LoanRepaymentConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.populator.ClientSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.ExtrasSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.OfficeSheetPopulator;
import org.apache.fineract.portfolio.loanaccount.data.LoanAccountData;
import org.apache.fineract.portfolio.loanaccount.data.LoanStatusEnumData;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;

class LoanRepaymentWorkbookPopulatorTest {

    private static final LoanStatusEnumData ACTIVE_STATUS = new LoanStatusEnumData(300L, "loanStatusType.active", "Active");
    private static final String DATE_FORMAT = "dd MMMM yyyy";

    private LoanAccountData loanWithAccountNo(String accountNo) {
        return new LoanAccountData().setAccountNo(accountNo).setStatus(ACTIVE_STATUS).setClientName("John Doe").setClientId(1L)
                .setLoanProductName("TestProduct").setPrincipal(BigDecimal.valueOf(10000));
    }

    private LoanRepaymentWorkbookPopulator populatorFor(LoanAccountData loan) {
        return LoanRepaymentWorkbookPopulator.full(new ArrayList<>(List.of(loan)), new OfficeSheetPopulator(List.of()),
                new ClientSheetPopulator(List.of(), List.of()), new ExtrasSheetPopulator(List.of(), List.of(), List.of()));
    }

    private LoanRepaymentWorkbookPopulator leanPopulator() {
        // FINERACT-2668: lean mode embeds no clients/offices/loans.
        return LoanRepaymentWorkbookPopulator.lean(new ExtrasSheetPopulator(List.of(), List.of(), List.of()));
    }

    @Test
    void populate_withAlphanumericAccountNumber() throws Exception {
        try (Workbook workbook = new HSSFWorkbook()) {
            populatorFor(loanWithAccountNo("ABC-001")).populate(workbook, DATE_FORMAT);

            Sheet sheet = workbook.getSheet(TemplatePopulateImportConstants.LOAN_REPAYMENT_SHEET_NAME);
            String cellValue = sheet.getRow(1).getCell(LoanRepaymentConstants.LOOKUP_ACCOUNT_NO_COL).getStringCellValue();
            assertEquals("ABC-001-Active", cellValue);
        }
    }

    @Test
    void populate_withNumericAccountNumber() throws Exception {
        try (Workbook workbook = new HSSFWorkbook()) {
            populatorFor(loanWithAccountNo("00000001")).populate(workbook, DATE_FORMAT);

            Sheet sheet = workbook.getSheet(TemplatePopulateImportConstants.LOAN_REPAYMENT_SHEET_NAME);
            String cellValue = sheet.getRow(1).getCell(LoanRepaymentConstants.LOOKUP_ACCOUNT_NO_COL).getStringCellValue();
            assertEquals("00000001-Active", cellValue);
        }
    }

    @Test
    void leanMode_omitsClientAndOfficeSheets() throws Exception {
        try (Workbook workbook = new HSSFWorkbook()) {
            leanPopulator().populate(workbook, DATE_FORMAT);

            assertNotNull(workbook.getSheet(TemplatePopulateImportConstants.LOAN_REPAYMENT_SHEET_NAME));
            assertNotNull(workbook.getSheet(TemplatePopulateImportConstants.EXTRAS_SHEET_NAME));
            // the tenant-wide lookup sheets must not be present in lean mode
            assertNull(workbook.getSheet(TemplatePopulateImportConstants.CLIENT_SHEET_NAME));
            assertNull(workbook.getSheet(TemplatePopulateImportConstants.OFFICE_SHEET_NAME));
        }
    }

    @Test
    void leanMode_keepsDataColumnLayoutForImportHandler() throws Exception {
        try (Workbook workbook = new HSSFWorkbook()) {
            leanPopulator().populate(workbook, DATE_FORMAT);

            Sheet sheet = workbook.getSheet(TemplatePopulateImportConstants.LOAN_REPAYMENT_SHEET_NAME);
            // import handler parses by fixed column index, so the data-entry headers must stay in place
            assertEquals("Loan Account No.*", sheet.getRow(0).getCell(LoanRepaymentConstants.LOAN_ACCOUNT_NO_COL).getStringCellValue());
            assertEquals("Amount Repaid*", sheet.getRow(0).getCell(LoanRepaymentConstants.AMOUNT_COL).getStringCellValue());
            // no per-loan lookup rows are embedded
            assertNull(sheet.getRow(1));
        }
    }
}
