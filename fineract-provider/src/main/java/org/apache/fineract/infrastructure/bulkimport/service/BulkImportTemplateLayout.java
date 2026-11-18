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

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.bulkimport.constants.GuarantorConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.JournalEntryConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.LoanRepaymentConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TransactionConstants;
import org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType;
import org.apache.fineract.infrastructure.bulkimport.importhandler.ImportHandlerUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * Where an import handler finds its rows: the sheet it reads and the column it counts them on, which must stay the same
 * as in the handler so that Total Records matches the rows it processes. The header of that column tells the real sheet
 * from a lookup sheet of the same name in another template (the Clients template has an "Offices" lookup sheet, for
 * example). Recurring deposit and savings transactions share one layout, so those two templates cannot be told apart.
 */
record BulkImportTemplateLayout(String templateName, String sheetName, int primaryColumn, String primaryHeader) {

    private static final int FIRST_COLUMN = TemplatePopulateImportConstants.FIRST_COLUMN_INDEX;
    private static final String OFFICE_NAME = "Office Name";
    private static final String AMOUNT = "Amount";

    private static final Map<GlobalEntityType, BulkImportTemplateLayout> LAYOUTS = Map.ofEntries(
            layout(GlobalEntityType.CLIENTS_PERSON, "Clients (Person)", TemplatePopulateImportConstants.CLIENT_PERSON_SHEET_NAME,
                    FIRST_COLUMN, "First Name"),
            layout(GlobalEntityType.CLIENTS_ENTITY, "Clients (Entity)", TemplatePopulateImportConstants.CLIENT_ENTITY_SHEET_NAME,
                    FIRST_COLUMN, "Name"),
            layout(GlobalEntityType.CENTERS, "Centers", TemplatePopulateImportConstants.CENTER_SHEET_NAME, FIRST_COLUMN, "Center Name"),
            layout(GlobalEntityType.GROUPS, "Groups", TemplatePopulateImportConstants.GROUP_SHEET_NAME, FIRST_COLUMN, "Group Name"),
            layout(GlobalEntityType.LOANS, "Loans", TemplatePopulateImportConstants.LOANS_SHEET_NAME, FIRST_COLUMN, OFFICE_NAME),
            layout(GlobalEntityType.LOAN_TRANSACTIONS, "Loan Repayments", TemplatePopulateImportConstants.LOAN_REPAYMENT_SHEET_NAME,
                    LoanRepaymentConstants.AMOUNT_COL, "Amount Repaid"),
            layout(GlobalEntityType.GUARANTORS, "Guarantors", TemplatePopulateImportConstants.GUARANTOR_SHEET_NAME,
                    GuarantorConstants.LOAN_ACCOUNT_NO_COL, "Loan Account No"),
            layout(GlobalEntityType.OFFICES, "Offices", TemplatePopulateImportConstants.OFFICE_SHEET_NAME, FIRST_COLUMN, OFFICE_NAME),
            layout(GlobalEntityType.CHART_OF_ACCOUNTS, "Chart of Accounts", TemplatePopulateImportConstants.CHART_OF_ACCOUNTS_SHEET_NAME,
                    FIRST_COLUMN, "Account Type"),
            layout(GlobalEntityType.GL_JOURNAL_ENTRIES, "Journal Entries", TemplatePopulateImportConstants.JOURNAL_ENTRY_SHEET_NAME,
                    JournalEntryConstants.TRANSACTION_ID_COL, "Transaction Id"),
            layout(GlobalEntityType.STAFF, "Staff", TemplatePopulateImportConstants.EMPLOYEE_SHEET_NAME, FIRST_COLUMN, OFFICE_NAME),
            layout(GlobalEntityType.SHARE_ACCOUNTS, "Share Accounts", TemplatePopulateImportConstants.SHARED_ACCOUNTS_SHEET_NAME,
                    FIRST_COLUMN, "Client Name"),
            layout(GlobalEntityType.SAVINGS_ACCOUNT, "Savings Accounts", TemplatePopulateImportConstants.SAVINGS_ACCOUNTS_SHEET_NAME,
                    FIRST_COLUMN, OFFICE_NAME),
            layout(GlobalEntityType.SAVINGS_TRANSACTIONS, "Savings Transactions",
                    TemplatePopulateImportConstants.SAVINGS_TRANSACTION_SHEET_NAME, TransactionConstants.AMOUNT_COL, AMOUNT),
            layout(GlobalEntityType.RECURRING_DEPOSIT_ACCOUNTS, "Recurring Deposit Accounts",
                    TemplatePopulateImportConstants.RECURRING_DEPOSIT_SHEET_NAME, FIRST_COLUMN, OFFICE_NAME),
            layout(GlobalEntityType.RECURRING_DEPOSIT_ACCOUNTS_TRANSACTIONS, "Recurring Deposit Transactions",
                    TemplatePopulateImportConstants.SAVINGS_TRANSACTION_SHEET_NAME, TransactionConstants.AMOUNT_COL, AMOUNT),
            layout(GlobalEntityType.FIXED_DEPOSIT_ACCOUNTS, "Fixed Deposit Accounts",
                    TemplatePopulateImportConstants.FIXED_DEPOSIT_SHEET_NAME, FIRST_COLUMN, OFFICE_NAME),
            layout(GlobalEntityType.FIXED_DEPOSIT_TRANSACTIONS, "Fixed Deposit Transactions",
                    TemplatePopulateImportConstants.FIXED_DEPOSIT_TRANSACTION_SHEET_NAME, TransactionConstants.AMOUNT_COL, AMOUNT),
            layout(GlobalEntityType.USERS, "Users", TemplatePopulateImportConstants.USER_SHEET_NAME, FIRST_COLUMN, OFFICE_NAME));

    private static Map.Entry<GlobalEntityType, BulkImportTemplateLayout> layout(final GlobalEntityType entityType,
            final String templateName, final String sheetName, final int primaryColumn, final String primaryHeader) {
        return Map.entry(entityType, new BulkImportTemplateLayout(templateName, sheetName, primaryColumn, primaryHeader));
    }

    static Optional<GlobalEntityType> findEntityType(final String entity) {
        return LAYOUTS.keySet().stream().filter(entityType -> entity.trim().equalsIgnoreCase(entityType.toString())).findFirst();
    }

    static BulkImportTemplateLayout of(final GlobalEntityType entityType) {
        return LAYOUTS.get(entityType);
    }

    void validate(final Workbook workbook) {
        if (!matches(workbook)) {
            final String article = "AEIOU".indexOf(this.templateName.charAt(0)) >= 0 ? "an" : "a";
            final String message = "This file is not " + article + " " + this.templateName + " template (sheet '" + this.sheetName
                    + "' with the " + this.templateName + " columns not found)";
            final String code = "error.msg.bulk.import.file.not.expected.template";
            throw new PlatformApiDataValidationException(code, message,
                    List.of(ApiParameterError.parameterError(code, message, message, "file", this.templateName, this.sheetName)));
        }
    }

    int countRecords(final Workbook workbook) {
        return ImportHandlerUtils.getNumberOfRows(workbook.getSheet(this.sheetName), this.primaryColumn);
    }

    private boolean matches(final Workbook workbook) {
        final Sheet sheet = workbook.getSheet(this.sheetName);
        final Row header = sheet == null ? null : sheet.getRow(TemplatePopulateImportConstants.ROWHEADER_INDEX);
        if (header == null) {
            return false;
        }
        final String actualHeader = new DataFormatter().formatCellValue(header.getCell(this.primaryColumn));
        return normalize(this.primaryHeader).equals(normalize(actualHeader));
    }

    // Templates mark mandatory columns as "Name*" or "Name *", and a column can be mandatory in one version only
    private static String normalize(final String header) {
        return StringUtils.normalizeSpace(header.replace("*", "")).toLowerCase(Locale.ROOT);
    }
}
