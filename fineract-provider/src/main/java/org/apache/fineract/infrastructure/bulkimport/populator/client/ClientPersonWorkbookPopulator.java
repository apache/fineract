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
package org.apache.fineract.infrastructure.bulkimport.populator.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.bulkimport.constants.ClientPersonConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.populator.AbstractWorkbookPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.OfficeSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.PersonnelSheetPopulator;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.dataqueries.data.DatatableData;
import org.apache.fineract.infrastructure.dataqueries.data.ResultsetColumnHeaderData;
import org.apache.fineract.infrastructure.dataqueries.data.ResultsetColumnValueData;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.poi.hssf.usermodel.HSSFDataValidationHelper;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.SheetVisibility;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.CellReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientPersonWorkbookPopulator extends AbstractWorkbookPopulator {

    private static final Logger log = LoggerFactory.getLogger(ClientPersonWorkbookPopulator.class);

    private final OfficeSheetPopulator officeSheetPopulator;
    private final PersonnelSheetPopulator personnelSheetPopulator;
    private final List<CodeValueData> clientTypeCodeValues;
    private final List<CodeValueData> genderCodeValues;
    private final List<CodeValueData> clientClassificationCodeValues;
    private final List<CodeValueData> addressTypesCodeValues;
    private final List<CodeValueData> stateProvinceCodeValues;
    private final List<CodeValueData> countryCodeValues;
    private final List<DatatableData> requiredDatatables;

    // Track datatable dropdown columns: namedRangeName -> (clientSheetColumnIndex, lookupSheetColumnIndex, values)
    private final Map<String, DatatableDropdownInfo> datatableDropdowns = new HashMap<>();

    // Track datatable date columns: columnIndex -> columnInfo
    private final Map<Integer, DatatableDateInfo> datatableDateColumns = new HashMap<>();

    // Track datatable boolean columns: columnIndex -> columnInfo
    private final Map<Integer, DatatableBooleanInfo> datatableBooleanColumns = new HashMap<>();

    // Inner class to track datatable dropdown information
    private static class DatatableDropdownInfo {

        final int clientSheetColumnIndex;
        final int lookupSheetColumnIndex;
        final List<ResultsetColumnValueData> values;

        DatatableDropdownInfo(int clientSheetColumnIndex, int lookupSheetColumnIndex, List<ResultsetColumnValueData> values) {
            this.clientSheetColumnIndex = clientSheetColumnIndex;
            this.lookupSheetColumnIndex = lookupSheetColumnIndex;
            this.values = values;
        }
    }

    // Inner class to track datatable date column information
    private static class DatatableDateInfo {

        final int clientSheetColumnIndex;
        final boolean isDateTime;

        DatatableDateInfo(int clientSheetColumnIndex, boolean isDateTime) {
            this.clientSheetColumnIndex = clientSheetColumnIndex;
            this.isDateTime = isDateTime;
        }
    }

    // Inner class to track datatable boolean column information
    private static class DatatableBooleanInfo {

        final int clientSheetColumnIndex;

        DatatableBooleanInfo(int clientSheetColumnIndex) {
            this.clientSheetColumnIndex = clientSheetColumnIndex;
        }
    }

    public ClientPersonWorkbookPopulator(OfficeSheetPopulator officeSheetPopulator, PersonnelSheetPopulator personnelSheetPopulator,
            List<CodeValueData> clientTypeCodeValues, List<CodeValueData> genderCodeValues, List<CodeValueData> clientClassification,
            List<CodeValueData> addressTypesCodeValues, List<CodeValueData> stateProvinceCodeValues, List<CodeValueData> countryCodeValues,
            List<DatatableData> requiredDatatables) {
        this.officeSheetPopulator = officeSheetPopulator;
        this.personnelSheetPopulator = personnelSheetPopulator;
        this.clientTypeCodeValues = clientTypeCodeValues;
        this.genderCodeValues = genderCodeValues;
        this.clientClassificationCodeValues = clientClassification;
        this.addressTypesCodeValues = addressTypesCodeValues;
        this.stateProvinceCodeValues = stateProvinceCodeValues;
        this.countryCodeValues = countryCodeValues;
        this.requiredDatatables = requiredDatatables != null ? requiredDatatables : new ArrayList<>();
    }

    @Override
    public void populate(Workbook workbook, String dateFormat) {
        Sheet clientSheet = workbook.createSheet(TemplatePopulateImportConstants.CLIENT_PERSON_SHEET_NAME);
        personnelSheetPopulator.populate(workbook, dateFormat);
        officeSheetPopulator.populate(workbook, dateFormat);
        // Create hidden lookup sheet for lookup data
        Sheet lookupSheet = workbook.createSheet(TemplatePopulateImportConstants.CLIENT_LOOKUPS_SHEET_NAME);
        // TODO: For debugging, temporarily set to VISIBLE. Revert to VERY_HIDDEN once validated.
        workbook.setSheetVisibility(workbook.getSheetIndex(lookupSheet), SheetVisibility.VERY_HIDDEN);

        setLayout(clientSheet);
        setClientDataLookupTable(lookupSheet);
        setFormatStyle(workbook, clientSheet);
        setRules(clientSheet, dateFormat);

        // Complete datatable handling (lookup population, named ranges, validation)
        // This is called after setLayout to complete datatable setup
        Row rowHeader = clientSheet.getRow(TemplatePopulateImportConstants.ROWHEADER_INDEX);
        if (rowHeader != null) {
            handleDatatableColumnsComplete(clientSheet, rowHeader, lookupSheet, workbook, dateFormat);
        }
    }

    private void setFormatStyle(Workbook workbook, Sheet worksheet) {
        CellStyle dateCellStyle = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd"));

        for (int rowIndex = 1; rowIndex < SpreadsheetVersion.EXCEL97.getMaxRows(); rowIndex++) {
            Row row = worksheet.getRow(rowIndex);
            if (row == null) {
                row = worksheet.createRow(rowIndex);
            }

            setFormatActivationAndSubmittedDate(row, ClientPersonConstants.ACTIVATION_DATE_COL, dateCellStyle);
            setFormatActivationAndSubmittedDate(row, ClientPersonConstants.SUBMITTED_ON_COL, dateCellStyle);

            // Apply date formatting to datatable date columns
            for (DatatableDateInfo dateInfo : datatableDateColumns.values()) {
                setFormatActivationAndSubmittedDate(row, dateInfo.clientSheetColumnIndex, dateCellStyle);
            }
        }
    }

    private void setFormatActivationAndSubmittedDate(Row row, int columnIndex, CellStyle cellStyle) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            cell = row.createCell(columnIndex);
        }
        cell.setCellStyle(cellStyle);
    }

    private void setClientDataLookupTable(Sheet lookupSheet) {
        // Data starts at row 2 (row index 1) to match named range references
        // Column 0 (A): Client Types
        int rowIndex = 1; // Start at row 2 (Excel row 2, POI index 1)
        for (CodeValueData clientTypeCodeValue : clientTypeCodeValues) {
            Row row = lookupSheet.getRow(rowIndex);
            if (row == null) {
                row = lookupSheet.createRow(rowIndex);
            }
            writeString(0, row, clientTypeCodeValue.getName() + " (" + clientTypeCodeValue.getId() + ")");
            rowIndex++;
        }

        // Column 1 (B): Client Classification
        rowIndex = 1;
        for (CodeValueData clientClassificationCodeValue : clientClassificationCodeValues) {
            Row row = lookupSheet.getRow(rowIndex);
            if (row == null) {
                row = lookupSheet.createRow(rowIndex);
            }
            writeString(1, row, clientClassificationCodeValue.getName() + " (" + clientClassificationCodeValue.getId() + ")");
            rowIndex++;
        }

        // Column 2 (C): Gender
        rowIndex = 1;
        for (CodeValueData genderCodeValue : genderCodeValues) {
            Row row = lookupSheet.getRow(rowIndex);
            if (row == null) {
                row = lookupSheet.createRow(rowIndex);
            }
            writeString(2, row, genderCodeValue.getName() + " (" + genderCodeValue.getId() + ")");
            rowIndex++;
        }

        // Column 3 (D): Address Type
        rowIndex = 1;
        for (CodeValueData addressTypeCodeValue : addressTypesCodeValues) {
            Row row = lookupSheet.getRow(rowIndex);
            if (row == null) {
                row = lookupSheet.createRow(rowIndex);
            }
            writeString(3, row, addressTypeCodeValue.getName() + " (" + addressTypeCodeValue.getId() + ")");
            rowIndex++;
        }

        // Column 4 (E): State/Province
        rowIndex = 1;
        for (CodeValueData stateCodeValue : stateProvinceCodeValues) {
            Row row = lookupSheet.getRow(rowIndex);
            if (row == null) {
                row = lookupSheet.createRow(rowIndex);
            }
            writeString(4, row, stateCodeValue.getName() + " (" + stateCodeValue.getId() + ")");
            rowIndex++;
        }

        // Column 5 (F): Country
        rowIndex = 1;
        for (CodeValueData countryCodeValue : countryCodeValues) {
            Row row = lookupSheet.getRow(rowIndex);
            if (row == null) {
                row = lookupSheet.createRow(rowIndex);
            }
            writeString(5, row, countryCodeValue.getName() + " (" + countryCodeValue.getId() + ")");
            rowIndex++;
        }

    }

    private String sanitizeNamedRangeName(String name) {
        // Excel named ranges cannot contain certain characters
        // Replace invalid characters with underscore
        return name.replaceAll("[ @#&()<>,;.:$£€§°\\\\/=!\\?\\-\\+\\*\"\\[\\]]", "_");
    }

    private void setLayout(Sheet worksheet) {
        Row rowHeader = worksheet.createRow(TemplatePopulateImportConstants.ROWHEADER_INDEX);
        rowHeader.setHeight(TemplatePopulateImportConstants.ROW_HEADER_HEIGHT);
        worksheet.setColumnWidth(ClientPersonConstants.FIRST_NAME_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(ClientPersonConstants.LAST_NAME_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(ClientPersonConstants.MIDDLE_NAME_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        writeString(ClientPersonConstants.FIRST_NAME_COL, rowHeader, "First Name*");
        writeString(ClientPersonConstants.LAST_NAME_COL, rowHeader, "Last Name*");
        writeString(ClientPersonConstants.MIDDLE_NAME_COL, rowHeader, "Middle Name");
        worksheet.setColumnWidth(ClientPersonConstants.OFFICE_NAME_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(ClientPersonConstants.STAFF_NAME_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(ClientPersonConstants.EXTERNAL_ID_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientPersonConstants.SUBMITTED_ON_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientPersonConstants.ACTIVATION_DATE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientPersonConstants.ACTIVE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientPersonConstants.MOBILE_NO_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(ClientPersonConstants.DOB_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientPersonConstants.CLIENT_TYPE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientPersonConstants.GENDER_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientPersonConstants.CLIENT_CLASSIFICATION_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(ClientPersonConstants.IS_STAFF_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientPersonConstants.ADDRESS_ENABLED_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientPersonConstants.ADDRESS_TYPE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientPersonConstants.STREET_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientPersonConstants.ADDRESS_LINE_1_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(ClientPersonConstants.ADDRESS_LINE_2_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(ClientPersonConstants.ADDRESS_LINE_3_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(ClientPersonConstants.CITY_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientPersonConstants.STATE_PROVINCE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientPersonConstants.COUNTRY_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientPersonConstants.POSTAL_CODE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientPersonConstants.IS_ACTIVE_ADDRESS_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientPersonConstants.STATUS_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        writeString(ClientPersonConstants.OFFICE_NAME_COL, rowHeader, "Office Name*");
        writeString(ClientPersonConstants.STAFF_NAME_COL, rowHeader, "Staff Name");
        writeString(ClientPersonConstants.EXTERNAL_ID_COL, rowHeader, "External ID ");
        writeString(ClientPersonConstants.SUBMITTED_ON_COL, rowHeader, "Submitted On Date");
        writeString(ClientPersonConstants.ACTIVATION_DATE_COL, rowHeader, "Activation date");
        writeString(ClientPersonConstants.ACTIVE_COL, rowHeader, "Active*");
        writeString(ClientPersonConstants.MOBILE_NO_COL, rowHeader, "Mobile number");
        writeString(ClientPersonConstants.DOB_COL, rowHeader, "Date of Birth ");
        writeString(ClientPersonConstants.CLIENT_TYPE_COL, rowHeader, "Client Type ");
        writeString(ClientPersonConstants.IS_STAFF_COL, rowHeader, "Is a staff memeber ");
        writeString(ClientPersonConstants.GENDER_COL, rowHeader, "Gender ");
        writeString(ClientPersonConstants.ADDRESS_ENABLED_COL, rowHeader, "Address Enabled *");
        writeString(ClientPersonConstants.CLIENT_CLASSIFICATION_COL, rowHeader, "Client Classification ");
        writeString(ClientPersonConstants.ADDRESS_TYPE_COL, rowHeader, "Address Type ");
        writeString(ClientPersonConstants.STREET_COL, rowHeader, "Street  ");
        writeString(ClientPersonConstants.ADDRESS_LINE_1_COL, rowHeader, "Address Line 1");
        writeString(ClientPersonConstants.ADDRESS_LINE_2_COL, rowHeader, "Address Line 2");
        writeString(ClientPersonConstants.ADDRESS_LINE_3_COL, rowHeader, "Address Line 3 ");
        writeString(ClientPersonConstants.CITY_COL, rowHeader, "City ");
        writeString(ClientPersonConstants.STATE_PROVINCE_COL, rowHeader, "State/ Province ");
        writeString(ClientPersonConstants.COUNTRY_COL, rowHeader, "Country ");
        writeString(ClientPersonConstants.POSTAL_CODE_COL, rowHeader, "Postal Code ");
        writeString(ClientPersonConstants.IS_ACTIVE_ADDRESS_COL, rowHeader, "Is active Address ? ");
        writeString(ClientPersonConstants.STATUS_COL, rowHeader, TemplatePopulateImportConstants.STATUS_COLUMN_HEADER);

        // Handle datatable columns (headers will be inserted here)
        // This is called from within setLayout to ensure proper column positioning
        int currentCol = handleDatatableColumnHeaders(worksheet, rowHeader);

        // Add warning message after all dynamic columns
        writeString(currentCol, rowHeader, "All * marked fields are compulsory.");
        worksheet.setColumnWidth(currentCol, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        currentCol++;

        // Insert 8 empty columns as a gap before lookup headers
        currentCol += 8;

        // Add lookup columns dynamically after datatable columns
        // These columns are for user reference and data validation
        // Data values remain on the hidden lookup sheet
        worksheet.setColumnWidth(currentCol, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        writeString(currentCol, rowHeader, "Lookup office Name  ");
        int lookupOfficeNameCol = currentCol++;

        worksheet.setColumnWidth(currentCol, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        writeString(currentCol, rowHeader, "Lookup Office Opened Date ");
        int lookupOfficeDateCol = currentCol++;

        worksheet.setColumnWidth(currentCol, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        writeString(currentCol, rowHeader, "Lookup Gender ");
        int lookupGenderCol = currentCol++;

        worksheet.setColumnWidth(currentCol, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        writeString(currentCol, rowHeader, "Lookup Client Types ");
        int lookupClientTypesCol = currentCol++;

        worksheet.setColumnWidth(currentCol, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        writeString(currentCol, rowHeader, "Lookup Client Classification ");
        int lookupClientClassificationCol = currentCol++;

        worksheet.setColumnWidth(currentCol, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        writeString(currentCol, rowHeader, "Lookup AddressType ");
        int lookupAddressTypeCol = currentCol++;

        worksheet.setColumnWidth(currentCol, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        writeString(currentCol, rowHeader, "Lookup State/Province ");
        int lookupStateProvinceCol = currentCol++;

        worksheet.setColumnWidth(currentCol, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        writeString(currentCol, rowHeader, "Lookup Country ");
        int lookupCountryCol = currentCol++;

        // Populate visible lookup values under lookup headers (for user reference)
        // These are read-only reference values; validations still use the hidden lookup sheet
        populateLookupValues(worksheet, lookupOfficeNameCol, lookupOfficeDateCol, lookupGenderCol, lookupClientTypesCol,
                lookupClientClassificationCol, lookupAddressTypeCol, lookupStateProvinceCol, lookupCountryCol);
    }

    private void populateLookupValues(Sheet worksheet, int lookupOfficeNameCol, int lookupOfficeDateCol, int lookupGenderCol,
            int lookupClientTypesCol, int lookupClientClassificationCol, int lookupAddressTypeCol, int lookupStateProvinceCol,
            int lookupCountryCol) {

        // Populate Office Name lookup values
        int rowIndex = 1; // Start at Excel row 2 (POI index 1)
        List<OfficeData> offices = officeSheetPopulator.getOffices();
        for (OfficeData office : offices) {
            Row row = worksheet.getRow(rowIndex);
            if (row == null) {
                row = worksheet.createRow(rowIndex);
            }
            writeString(lookupOfficeNameCol, row, office.getName() + " (" + office.getId() + ")");
            if (office.getOpeningDate() != null) {
                writeString(lookupOfficeDateCol, row, office.getOpeningDate().toString());
            }
            rowIndex++;
        }

        // Populate Gender lookup values
        rowIndex = 1;
        for (CodeValueData genderCodeValue : genderCodeValues) {
            Row row = worksheet.getRow(rowIndex);
            if (row == null) {
                row = worksheet.createRow(rowIndex);
            }
            writeString(lookupGenderCol, row, genderCodeValue.getName() + " (" + genderCodeValue.getId() + ")");
            rowIndex++;
        }

        // Populate Client Types lookup values
        rowIndex = 1;
        for (CodeValueData clientTypeCodeValue : clientTypeCodeValues) {
            Row row = worksheet.getRow(rowIndex);
            if (row == null) {
                row = worksheet.createRow(rowIndex);
            }
            writeString(lookupClientTypesCol, row, clientTypeCodeValue.getName() + " (" + clientTypeCodeValue.getId() + ")");
            rowIndex++;
        }

        // Populate Client Classification lookup values
        rowIndex = 1;
        for (CodeValueData clientClassificationCodeValue : clientClassificationCodeValues) {
            Row row = worksheet.getRow(rowIndex);
            if (row == null) {
                row = worksheet.createRow(rowIndex);
            }
            writeString(lookupClientClassificationCol, row,
                    clientClassificationCodeValue.getName() + " (" + clientClassificationCodeValue.getId() + ")");
            rowIndex++;
        }

        // Populate Address Type lookup values
        rowIndex = 1;
        for (CodeValueData addressTypeCodeValue : addressTypesCodeValues) {
            Row row = worksheet.getRow(rowIndex);
            if (row == null) {
                row = worksheet.createRow(rowIndex);
            }
            writeString(lookupAddressTypeCol, row, addressTypeCodeValue.getName() + " (" + addressTypeCodeValue.getId() + ")");
            rowIndex++;
        }

        // Populate State/Province lookup values
        rowIndex = 1;
        for (CodeValueData stateCodeValue : stateProvinceCodeValues) {
            Row row = worksheet.getRow(rowIndex);
            if (row == null) {
                row = worksheet.createRow(rowIndex);
            }
            writeString(lookupStateProvinceCol, row, stateCodeValue.getName() + " (" + stateCodeValue.getId() + ")");
            rowIndex++;
        }

        // Populate Country lookup values
        rowIndex = 1;
        for (CodeValueData countryCodeValue : countryCodeValues) {
            Row row = worksheet.getRow(rowIndex);
            if (row == null) {
                row = worksheet.createRow(rowIndex);
            }
            writeString(lookupCountryCol, row, countryCodeValue.getName() + " (" + countryCodeValue.getId() + ")");
            rowIndex++;
        }
    }

    /**
     * Handles datatable column header creation and dropdown tracking. This is called from within setLayout() to insert
     * headers at the correct position.
     *
     * @param worksheet
     *            The client sheet
     * @param rowHeader
     *            The header row
     * @return The next column index after datatable columns (or STATUS_COL + 1 if no datatables)
     */
    private int handleDatatableColumnHeaders(Sheet worksheet, Row rowHeader) {
        int startCol = ClientPersonConstants.STATUS_COL + 1;
        try {
            int currentCol = startCol;
            int lookupSheetCol = 6; // Start after Country (column F = index 5), so next available is G = index 6

            for (DatatableData datatable : requiredDatatables) {
                if (datatable == null) {
                    continue;
                }
                String datatableName = datatable.getRegisteredTableName();
                List<ResultsetColumnHeaderData> columns = datatable.getColumnHeaderData();
                if (columns != null) {
                    for (ResultsetColumnHeaderData column : columns) {
                        try {
                            // Skip system columns (id, client_id, etc.)
                            String columnName = column.getColumnName();
                            if (columnName == null || columnName.equalsIgnoreCase("id") || columnName.equalsIgnoreCase("client_id")
                                    || columnName.equalsIgnoreCase("created_at") || columnName.equalsIgnoreCase("updated_at")) {
                                continue;
                            }

                            worksheet.setColumnWidth(currentCol, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
                            // Use dot notation: registeredTableName.columnName for unambiguous parsing
                            // For single-row datatables: treat as mandatory inline data (append "*" if column is not
                            // nullable)
                            // For multi-row datatables: treat as optional/repeatable child data (no "*" even if not
                            // nullable)
                            String headerLabel = datatableName + "." + columnName;
                            if (!datatable.isMultiRow() && !column.getIsColumnNullable()) {
                                // Single-row datatables: mark mandatory columns with "*"
                                headerLabel += "*";
                            }
                            // Multi-row datatables are always optional (no "*" marker)
                            writeString(currentCol, rowHeader, headerLabel);

                            // Check if this is a dropdown column (CODELOOKUP) with values
                            if (column.isCodeLookupDisplayType() && column.hasColumnValues()) {
                                String namedRangeName = sanitizeNamedRangeName(datatableName + "_" + columnName);
                                List<ResultsetColumnValueData> columnValues = column.getColumnValues();
                                if (columnValues != null && !columnValues.isEmpty()) {
                                    datatableDropdowns.put(namedRangeName,
                                            new DatatableDropdownInfo(currentCol, lookupSheetCol, columnValues));
                                    lookupSheetCol++;
                                }
                            }
                            // Check if this is a date or datetime column
                            else if (column.isDateDisplayType() || column.isDateTimeDisplayType()) {
                                datatableDateColumns.put(currentCol, new DatatableDateInfo(currentCol, column.isDateTimeDisplayType()));
                            }
                            // Check if this is a boolean column
                            else if (column.isBooleanDisplayType()) {
                                datatableBooleanColumns.put(currentCol, new DatatableBooleanInfo(currentCol));
                            }

                            currentCol++;
                        } catch (Exception e) {
                            String datatableNameForLog = datatableName != null ? datatableName : "unknown";
                            String columnNameForLog = column != null && column.getColumnName() != null ? column.getColumnName() : "unknown";
                            log.warn("Failed to process datatable column '{}' in datatable '{}': {}", columnNameForLog, datatableNameForLog,
                                    e.getMessage());
                            // Continue with next column
                        }
                    }
                }
            }
            return currentCol;
        } catch (Exception e) {
            log.warn(
                    "Failed to handle datatable column headers in client bulk import template: {}. Template generation will continue without datatable columns.",
                    e.getMessage());
            return startCol; // Return start position if datatables failed
        }
    }

    /**
     * Completes datatable setup: lookup sheet population, named ranges, and validation. This is called after
     * setLayout() to complete datatable enhancements. All operations are wrapped in try/catch to ensure datatable
     * failures never break template generation.
     *
     * @param worksheet
     *            The client sheet
     * @param rowHeader
     *            The header row
     * @param lookupSheet
     *            The hidden lookup sheet
     * @param workbook
     *            The workbook
     * @param dateFormat
     *            The date format
     */
    private void handleDatatableColumnsComplete(Sheet worksheet, Row rowHeader, Sheet lookupSheet, Workbook workbook, String dateFormat) {
        try {
            // Step 1: Populate datatable dropdown values into lookup sheet
            for (Map.Entry<String, DatatableDropdownInfo> entry : datatableDropdowns.entrySet()) {
                try {
                    DatatableDropdownInfo dropdownInfo = entry.getValue();
                    int rowIndex = 1; // Start at row 2 (Excel row 2, POI index 1)
                    for (ResultsetColumnValueData valueData : dropdownInfo.values) {
                        Row row = lookupSheet.getRow(rowIndex);
                        if (row == null) {
                            row = lookupSheet.createRow(rowIndex);
                        }
                        writeString(dropdownInfo.lookupSheetColumnIndex, row, valueData.getValue() + " (" + valueData.getId() + ")");
                        rowIndex++;
                    }
                } catch (Exception e) {
                    String namedRangeName = entry.getKey();
                    log.warn("Failed to populate dropdown values for datatable named range '{}': {}", namedRangeName, e.getMessage());
                    // Continue with next dropdown
                }
            }

            // Step 3: Create named ranges for datatable dropdown columns
            String lookupSheetName = TemplatePopulateImportConstants.CLIENT_LOOKUPS_SHEET_NAME;
            Workbook clientWorkbook = worksheet.getWorkbook();
            for (Map.Entry<String, DatatableDropdownInfo> entry : datatableDropdowns.entrySet()) {
                try {
                    String namedRangeName = entry.getKey();
                    DatatableDropdownInfo dropdownInfo = entry.getValue();
                    Name datatableDropdownGroup = clientWorkbook.createName();
                    setSanitized(datatableDropdownGroup, namedRangeName);
                    int dropdownLastRow = dropdownInfo.values.size() + 1; // +1 because data starts at row 2
                    String dropdownCol = CellReference.convertNumToColString(dropdownInfo.lookupSheetColumnIndex);
                    datatableDropdownGroup
                            .setRefersToFormula("'" + lookupSheetName + "'!$" + dropdownCol + "$2:$" + dropdownCol + "$" + dropdownLastRow);
                } catch (Exception e) {
                    String namedRangeName = entry.getKey();
                    log.warn("Failed to create named range '{}' for datatable dropdown: {}", namedRangeName, e.getMessage());
                    // Continue with next named range
                }
            }

            // Step 4: Add data validation for datatable dropdown columns
            DataValidationHelper validationHelper = new HSSFDataValidationHelper((HSSFSheet) worksheet);
            for (Map.Entry<String, DatatableDropdownInfo> entry : datatableDropdowns.entrySet()) {
                try {
                    String namedRangeName = entry.getKey();
                    DatatableDropdownInfo dropdownInfo = entry.getValue();
                    CellRangeAddressList datatableDropdownRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                            dropdownInfo.clientSheetColumnIndex, dropdownInfo.clientSheetColumnIndex);
                    DataValidationConstraint datatableDropdownConstraint = validationHelper.createFormulaListConstraint(namedRangeName);
                    DataValidation datatableDropdownValidation = validationHelper.createValidation(datatableDropdownConstraint,
                            datatableDropdownRange);
                    worksheet.addValidationData(datatableDropdownValidation);
                } catch (Exception e) {
                    String namedRangeName = entry.getKey();
                    log.warn("Failed to add data validation for datatable dropdown '{}': {}", namedRangeName, e.getMessage());
                    // Continue with next validation
                }
            }
        } catch (Exception e) {
            log.warn(
                    "Failed to complete datatable setup in client bulk import template: {}. Template generation will continue without datatable enhancements.",
                    e.getMessage());
            // Do not throw - allow template generation to continue
        }
    }

    private void setRules(Sheet worksheet, String dateformat) {
        CellRangeAddressList officeNameRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                ClientPersonConstants.OFFICE_NAME_COL, ClientPersonConstants.OFFICE_NAME_COL);
        CellRangeAddressList staffNameRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                ClientPersonConstants.STAFF_NAME_COL, ClientPersonConstants.STAFF_NAME_COL);
        CellRangeAddressList submittedOnDateRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                ClientPersonConstants.SUBMITTED_ON_COL, ClientPersonConstants.SUBMITTED_ON_COL);
        CellRangeAddressList activationDateRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                ClientPersonConstants.ACTIVATION_DATE_COL, ClientPersonConstants.ACTIVATION_DATE_COL);
        CellRangeAddressList activeRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                ClientPersonConstants.ACTIVE_COL, ClientPersonConstants.ACTIVE_COL);
        CellRangeAddressList clientTypeRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                ClientPersonConstants.CLIENT_TYPE_COL, ClientPersonConstants.CLIENT_TYPE_COL);
        CellRangeAddressList dobRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                ClientPersonConstants.DOB_COL, ClientPersonConstants.DOB_COL);
        CellRangeAddressList isStaffRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                ClientPersonConstants.IS_STAFF_COL, ClientPersonConstants.IS_STAFF_COL);
        CellRangeAddressList genderRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                ClientPersonConstants.GENDER_COL, ClientPersonConstants.GENDER_COL);
        CellRangeAddressList clientClassificationRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                ClientPersonConstants.CLIENT_CLASSIFICATION_COL, ClientPersonConstants.CLIENT_CLASSIFICATION_COL);
        CellRangeAddressList enabledAddressRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                ClientPersonConstants.ADDRESS_ENABLED_COL, ClientPersonConstants.ADDRESS_ENABLED_COL);
        CellRangeAddressList addressTypeRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                ClientPersonConstants.ADDRESS_TYPE_COL, ClientPersonConstants.ADDRESS_TYPE_COL);
        CellRangeAddressList stateProvinceRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                ClientPersonConstants.STATE_PROVINCE_COL, ClientPersonConstants.STATE_PROVINCE_COL);
        CellRangeAddressList countryRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                ClientPersonConstants.COUNTRY_COL, ClientPersonConstants.COUNTRY_COL);
        CellRangeAddressList activeAddressRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                ClientPersonConstants.IS_ACTIVE_ADDRESS_COL, ClientPersonConstants.IS_ACTIVE_ADDRESS_COL);

        DataValidationHelper validationHelper = new HSSFDataValidationHelper((HSSFSheet) worksheet);

        List<OfficeData> offices = officeSheetPopulator.getOffices();
        setNames(worksheet, offices);

        DataValidationConstraint officeNameConstraint = validationHelper.createFormulaListConstraint("Office");
        DataValidationConstraint staffNameConstraint = validationHelper
                .createFormulaListConstraint("INDIRECT(CONCATENATE(\"Staff_\",$D1))");
        DataValidationConstraint submittedOnDateConstraint = validationHelper
                .createDateConstraint(DataValidationConstraint.OperatorType.LESS_OR_EQUAL, "=TODAY()", null, dateformat);
        DataValidationConstraint activationDateConstraint = validationHelper
                .createDateConstraint(DataValidationConstraint.OperatorType.GREATER_OR_EQUAL, "=$H1", null, dateformat);
        DataValidationConstraint dobDateConstraint = validationHelper
                .createDateConstraint(DataValidationConstraint.OperatorType.LESS_OR_EQUAL, "=TODAY()", null, dateformat);
        DataValidationConstraint activeConstraint = validationHelper.createExplicitListConstraint(new String[] { "True", "False" });
        DataValidationConstraint clientTypesConstraint = validationHelper.createFormulaListConstraint("ClientTypes");
        DataValidationConstraint isStaffConstraint = validationHelper.createExplicitListConstraint(new String[] { "True", "False" });
        DataValidationConstraint genderConstraint = validationHelper.createFormulaListConstraint("Gender");
        DataValidationConstraint clientClassificationConstraint = validationHelper.createFormulaListConstraint("ClientClassification");
        DataValidationConstraint enabledAddressConstraint = validationHelper.createExplicitListConstraint(new String[] { "True", "False" });
        DataValidationConstraint addressTypeConstraint = validationHelper.createFormulaListConstraint("AddressType");
        DataValidationConstraint stateProvinceConstraint = validationHelper.createFormulaListConstraint("StateProvince");
        DataValidationConstraint countryConstraint = validationHelper.createFormulaListConstraint("Country");
        DataValidationConstraint activeAddressConstraint = validationHelper.createExplicitListConstraint(new String[] { "True", "False" });

        DataValidation officeValidation = validationHelper.createValidation(officeNameConstraint, officeNameRange);
        DataValidation staffValidation = validationHelper.createValidation(staffNameConstraint, staffNameRange);
        DataValidation submittedOnDateValidation = validationHelper.createValidation(submittedOnDateConstraint, submittedOnDateRange);
        DataValidation activationDateValidation = validationHelper.createValidation(activationDateConstraint, activationDateRange);
        DataValidation dobDateValidation = validationHelper.createValidation(dobDateConstraint, dobRange);
        DataValidation activeValidation = validationHelper.createValidation(activeConstraint, activeRange);
        DataValidation clientTypeValidation = validationHelper.createValidation(clientTypesConstraint, clientTypeRange);
        DataValidation isStaffValidation = validationHelper.createValidation(isStaffConstraint, isStaffRange);
        DataValidation genderValidation = validationHelper.createValidation(genderConstraint, genderRange);
        DataValidation clientClassificationValidation = validationHelper.createValidation(clientClassificationConstraint,
                clientClassificationRange);
        DataValidation enabledAddressValidation = validationHelper.createValidation(enabledAddressConstraint, enabledAddressRange);
        DataValidation addressTypeValidation = validationHelper.createValidation(addressTypeConstraint, addressTypeRange);
        DataValidation stateProvinceValidation = validationHelper.createValidation(stateProvinceConstraint, stateProvinceRange);
        DataValidation countryValidation = validationHelper.createValidation(countryConstraint, countryRange);
        DataValidation activeAddressValidation = validationHelper.createValidation(activeAddressConstraint, activeAddressRange);

        worksheet.addValidationData(activeValidation);
        worksheet.addValidationData(officeValidation);
        worksheet.addValidationData(staffValidation);
        worksheet.addValidationData(activationDateValidation);
        worksheet.addValidationData(submittedOnDateValidation);
        worksheet.addValidationData(dobDateValidation);
        worksheet.addValidationData(clientTypeValidation);
        worksheet.addValidationData(isStaffValidation);
        worksheet.addValidationData(genderValidation);
        worksheet.addValidationData(clientClassificationValidation);
        worksheet.addValidationData(enabledAddressValidation);
        worksheet.addValidationData(addressTypeValidation);
        worksheet.addValidationData(stateProvinceValidation);
        worksheet.addValidationData(countryValidation);
        worksheet.addValidationData(activeAddressValidation);

        // Add date validation for datatable date columns
        for (DatatableDateInfo dateInfo : datatableDateColumns.values()) {
            try {
                CellRangeAddressList datatableDateRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                        dateInfo.clientSheetColumnIndex, dateInfo.clientSheetColumnIndex);
                // Use same date constraint as submittedOnDate (less than or equal to today)
                DataValidationConstraint datatableDateConstraint = validationHelper
                        .createDateConstraint(DataValidationConstraint.OperatorType.LESS_OR_EQUAL, "=TODAY()", null, dateformat);
                DataValidation datatableDateValidation = validationHelper.createValidation(datatableDateConstraint, datatableDateRange);
                worksheet.addValidationData(datatableDateValidation);
            } catch (Exception e) {
                log.warn("Failed to add date validation for datatable date column at index {}: {}", dateInfo.clientSheetColumnIndex,
                        e.getMessage());
            }
        }

        // Add boolean validation for datatable boolean columns
        for (DatatableBooleanInfo booleanInfo : datatableBooleanColumns.values()) {
            try {
                CellRangeAddressList datatableBooleanRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                        booleanInfo.clientSheetColumnIndex, booleanInfo.clientSheetColumnIndex);
                // Use same boolean constraint as active field (True/False)
                DataValidationConstraint datatableBooleanConstraint = validationHelper
                        .createExplicitListConstraint(new String[] { "True", "False" });
                DataValidation datatableBooleanValidation = validationHelper.createValidation(datatableBooleanConstraint,
                        datatableBooleanRange);
                worksheet.addValidationData(datatableBooleanValidation);
            } catch (Exception e) {
                log.warn("Failed to add boolean validation for datatable boolean column at index {}: {}",
                        booleanInfo.clientSheetColumnIndex, e.getMessage());
            }
        }
    }

    private void setNames(Sheet worksheet, List<OfficeData> offices) {
        Workbook clientWorkbook = worksheet.getWorkbook();
        String lookupSheetName = TemplatePopulateImportConstants.CLIENT_LOOKUPS_SHEET_NAME;

        Name officeGroup = clientWorkbook.createName();
        officeGroup.setNameName("Office");
        officeGroup.setRefersToFormula(TemplatePopulateImportConstants.OFFICE_SHEET_NAME + "!$B$2:$B$" + (offices.size() + 1));

        // All lookup named ranges explicitly reference the hidden lookup sheet
        // Column 0 (A): Client Types
        Name clientTypeGroup = clientWorkbook.createName();
        clientTypeGroup.setNameName("ClientTypes");
        int clientTypesLastRow = clientTypeCodeValues.size() + 1; // +1 because data starts at row 2
        String clientTypesCol = CellReference.convertNumToColString(0);
        clientTypeGroup
                .setRefersToFormula("'" + lookupSheetName + "'!$" + clientTypesCol + "$2:$" + clientTypesCol + "$" + clientTypesLastRow);

        // Column 2 (C): Gender
        Name genderGroup = clientWorkbook.createName();
        genderGroup.setNameName("Gender");
        int genderLastRow = genderCodeValues.size() + 1;
        String genderCol = CellReference.convertNumToColString(2);
        genderGroup.setRefersToFormula("'" + lookupSheetName + "'!$" + genderCol + "$2:$" + genderCol + "$" + genderLastRow);

        // Column 1 (B): Client Classification
        Name clientClassficationGroup = clientWorkbook.createName();
        clientClassficationGroup.setNameName("ClientClassification");
        int clientClassificationLastRow = clientClassificationCodeValues.size() + 1;
        String clientClassificationCol = CellReference.convertNumToColString(1);
        clientClassficationGroup.setRefersToFormula("'" + lookupSheetName + "'!$" + clientClassificationCol + "$2:$"
                + clientClassificationCol + "$" + clientClassificationLastRow);

        // Column 3 (D): Address Type
        Name addressTypeGroup = clientWorkbook.createName();
        addressTypeGroup.setNameName("AddressType");
        int addressTypeLastRow = addressTypesCodeValues.size() + 1;
        String addressTypeCol = CellReference.convertNumToColString(3);
        addressTypeGroup
                .setRefersToFormula("'" + lookupSheetName + "'!$" + addressTypeCol + "$2:$" + addressTypeCol + "$" + addressTypeLastRow);

        // Column 4 (E): State/Province
        Name stateProvinceGroup = clientWorkbook.createName();
        stateProvinceGroup.setNameName("StateProvince");
        int stateProvinceLastRow = stateProvinceCodeValues.size() + 1;
        String stateProvinceCol = CellReference.convertNumToColString(4);
        stateProvinceGroup.setRefersToFormula(
                "'" + lookupSheetName + "'!$" + stateProvinceCol + "$2:$" + stateProvinceCol + "$" + stateProvinceLastRow);

        // Column 5 (F): Country
        Name countryGroup = clientWorkbook.createName();
        countryGroup.setNameName("Country");
        int countryLastRow = countryCodeValues.size() + 1;
        String countryCol = CellReference.convertNumToColString(5);
        countryGroup.setRefersToFormula("'" + lookupSheetName + "'!$" + countryCol + "$2:$" + countryCol + "$" + countryLastRow);

        for (Integer i = 0; i < offices.size(); i++) {
            Integer[] officeNameToBeginEndIndexesOfStaff = personnelSheetPopulator.getOfficeNameToBeginEndIndexesOfStaff().get(i);
            if (officeNameToBeginEndIndexesOfStaff != null) {
                Name name = clientWorkbook.createName();
                setSanitized(name, "Staff_" + offices.get(i).getName());
                name.setRefersToFormula(TemplatePopulateImportConstants.STAFF_SHEET_NAME + "!$B$" + officeNameToBeginEndIndexesOfStaff[0]
                        + ":$B$" + officeNameToBeginEndIndexesOfStaff[1]);
            }
        }
    }
}
