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
import org.apache.fineract.infrastructure.bulkimport.constants.ClientEntityConstants;
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

public class ClientEntityWorkbookPopulator extends AbstractWorkbookPopulator {

    private static final Logger log = LoggerFactory.getLogger(ClientEntityWorkbookPopulator.class);

    private final OfficeSheetPopulator officeSheetPopulator;
    private final PersonnelSheetPopulator personnelSheetPopulator;
    private final List<CodeValueData> clientTypeCodeValues;
    private final List<CodeValueData> constitutionCodeValues;
    private final List<CodeValueData> clientClassificationCodeValues;
    private final List<CodeValueData> addressTypesCodeValues;
    private final List<CodeValueData> stateProvinceCodeValues;
    private final List<CodeValueData> countryCodeValues;
    private final List<CodeValueData> mainBusinesslineCodeValues;
    private final List<DatatableData> requiredDatatables;
    // Track datatable dropdown columns: namedRangeName -> (clientSheetColumnIndex, lookupSheetColumnIndex, values)
    private final Map<String, ClientEntityWorkbookPopulator.DatatableDropdownInfo> datatableDropdowns = new HashMap<>();

    // Track datatable date columns: columnIndex -> columnInfo
    private final Map<Integer, ClientEntityWorkbookPopulator.DatatableDateInfo> datatableDateColumns = new HashMap<>();

    // Track datatable boolean columns: columnIndex -> columnInfo
    private final Map<Integer, ClientEntityWorkbookPopulator.DatatableBooleanInfo> datatableBooleanColumns = new HashMap<>();

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

    public ClientEntityWorkbookPopulator(OfficeSheetPopulator officeSheetPopulator, PersonnelSheetPopulator personnelSheetPopulator,
            List<CodeValueData> clientTypeCodeValues, List<CodeValueData> constitutionCodeValues, List<CodeValueData> mainBusinessline,
            List<CodeValueData> clientClassification, List<CodeValueData> addressTypesCodeValues,
            List<CodeValueData> stateProvinceCodeValues, List<CodeValueData> countryCodeValues, List<DatatableData> requiredDatatables) {
        this.officeSheetPopulator = officeSheetPopulator;
        this.personnelSheetPopulator = personnelSheetPopulator;
        this.clientTypeCodeValues = clientTypeCodeValues;
        this.constitutionCodeValues = constitutionCodeValues;
        this.clientClassificationCodeValues = clientClassification;
        this.addressTypesCodeValues = addressTypesCodeValues;
        this.stateProvinceCodeValues = stateProvinceCodeValues;
        this.countryCodeValues = countryCodeValues;
        this.mainBusinesslineCodeValues = mainBusinessline;
        this.requiredDatatables = requiredDatatables != null ? requiredDatatables : new ArrayList<>();
    }

    @Override
    public void populate(Workbook workbook, String dateFormat) {
        Sheet clientSheet = workbook.createSheet(TemplatePopulateImportConstants.CLIENT_ENTITY_SHEET_NAME);
        personnelSheetPopulator.populate(workbook, dateFormat);
        officeSheetPopulator.populate(workbook, dateFormat);

        // Create hidden lookup sheet for lookup data
        Sheet lookupSheet = workbook.createSheet(TemplatePopulateImportConstants.CLIENT_LOOKUPS_SHEET_NAME);
        // TODO: For debugging, temporarily set to VISIBLE. Revert to VERY_HIDDEN once validated.
        workbook.setSheetVisibility(workbook.getSheetIndex(lookupSheet), SheetVisibility.VERY_HIDDEN);

        setLayout(clientSheet); // fills datatableDropdowns
        setClientDataLookupTable(lookupSheet); // now values exist
        setFormatStyle(workbook, clientSheet);
        setRules(clientSheet, dateFormat);
        handleDatatableColumnsComplete(clientSheet, lookupSheet);
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

            setFormatActivationAndSubmittedDate(row, ClientEntityConstants.ACTIVATION_DATE_COL, dateCellStyle);
            setFormatActivationAndSubmittedDate(row, ClientEntityConstants.SUBMITTED_ON_COL, dateCellStyle);
            setFormatActivationAndSubmittedDate(row, ClientEntityConstants.INCOPORATION_VALID_TILL_COL, dateCellStyle);
            setFormatActivationAndSubmittedDate(row, ClientEntityConstants.INCOPORATION_DATE_COL, dateCellStyle);
            // Apply date formatting to datatable date columns
            for (ClientEntityWorkbookPopulator.DatatableDateInfo dateInfo : datatableDateColumns.values()) {
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

        // Column 2 (C): Constitution
        rowIndex = 1;
        for (CodeValueData constitutionCodeValue : constitutionCodeValues) {
            Row row = lookupSheet.getRow(rowIndex);
            if (row == null) {
                row = lookupSheet.createRow(rowIndex);
            }
            writeString(2, row, constitutionCodeValue.getName() + " (" + constitutionCodeValue.getId() + ")");
            rowIndex++;
        }

        // Column 3 (D): Main Business Line
        rowIndex = 1;
        for (CodeValueData mainBusinessCodeValue : mainBusinesslineCodeValues) {
            Row row = lookupSheet.getRow(rowIndex);
            if (row == null) {
                row = lookupSheet.createRow(rowIndex);
            }
            writeString(3, row, mainBusinessCodeValue.getName() + " (" + mainBusinessCodeValue.getId() + ")");
            rowIndex++;
        }

        // Column 4 (E): Address Type
        rowIndex = 1;
        for (CodeValueData addressTypeCodeValue : addressTypesCodeValues) {
            Row row = lookupSheet.getRow(rowIndex);
            if (row == null) {
                row = lookupSheet.createRow(rowIndex);
            }
            writeString(4, row, addressTypeCodeValue.getName() + " (" + addressTypeCodeValue.getId() + ")");
            rowIndex++;
        }

        // Column 5 (F): State/Province
        rowIndex = 1;
        for (CodeValueData stateCodeValue : stateProvinceCodeValues) {
            Row row = lookupSheet.getRow(rowIndex);
            if (row == null) {
                row = lookupSheet.createRow(rowIndex);
            }
            writeString(5, row, stateCodeValue.getName() + " (" + stateCodeValue.getId() + ")");
            rowIndex++;
        }

        // Column 6 (G): Country
        rowIndex = 1;
        for (CodeValueData countryCodeValue : countryCodeValues) {
            Row row = lookupSheet.getRow(rowIndex);
            if (row == null) {
                row = lookupSheet.createRow(rowIndex);
            }
            writeString(6, row, countryCodeValue.getName() + " (" + countryCodeValue.getId() + ")");
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
        worksheet.setColumnWidth(ClientEntityConstants.NAME_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        writeString(ClientEntityConstants.NAME_COL, rowHeader, "Name*");
        worksheet.setColumnWidth(ClientEntityConstants.OFFICE_NAME_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.STAFF_NAME_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.INCOPORATION_DATE_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.INCOPORATION_VALID_TILL_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.MOBILE_NO_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.CLIENT_TYPE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.CLIENT_CLASSIFICATION_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.INCOPORATION_NUMBER_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.MAIN_BUSINESS_LINE, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.CONSTITUTION_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.REMARKS_COL, TemplatePopulateImportConstants.LARGE_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.EXTERNAL_ID_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.SUBMITTED_ON_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.ACTIVE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.ACTIVATION_DATE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.ADDRESS_ENABLED, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.ADDRESS_TYPE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.STREET_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.ADDRESS_LINE_1_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.ADDRESS_LINE_2_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.ADDRESS_LINE_3_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.CITY_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.STATE_PROVINCE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.COUNTRY_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.POSTAL_CODE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.IS_ACTIVE_ADDRESS_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.STATUS_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        writeString(ClientEntityConstants.NAME_COL, rowHeader, "Name");
        writeString(ClientEntityConstants.OFFICE_NAME_COL, rowHeader, "Office Name*");
        writeString(ClientEntityConstants.STAFF_NAME_COL, rowHeader, "Staff Name");
        writeString(ClientEntityConstants.INCOPORATION_DATE_COL, rowHeader, "Incorporation Date");
        writeString(ClientEntityConstants.INCOPORATION_VALID_TILL_COL, rowHeader, "Incorporation Validity Till Date");
        writeString(ClientEntityConstants.MOBILE_NO_COL, rowHeader, "Mobile number");
        writeString(ClientEntityConstants.CLIENT_TYPE_COL, rowHeader, "Client Type ");
        writeString(ClientEntityConstants.CLIENT_CLASSIFICATION_COL, rowHeader, "Client Classification ");
        writeString(ClientEntityConstants.INCOPORATION_NUMBER_COL, rowHeader, "Incorporation Number");
        writeString(ClientEntityConstants.MAIN_BUSINESS_LINE, rowHeader, "Main Business Line");
        writeString(ClientEntityConstants.CONSTITUTION_COL, rowHeader, "Constitution*");
        writeString(ClientEntityConstants.REMARKS_COL, rowHeader, "Remarks");
        writeString(ClientEntityConstants.EXTERNAL_ID_COL, rowHeader, "External ID ");
        writeString(ClientEntityConstants.SUBMITTED_ON_COL, rowHeader, "Submitted On Date");
        writeString(ClientEntityConstants.ACTIVE_COL, rowHeader, "Active*");
        writeString(ClientEntityConstants.ACTIVATION_DATE_COL, rowHeader, "Activation Date* ");
        writeString(ClientEntityConstants.ADDRESS_ENABLED, rowHeader, "Address Enabled ");
        writeString(ClientEntityConstants.ADDRESS_TYPE_COL, rowHeader, "Address Type ");
        writeString(ClientEntityConstants.STREET_COL, rowHeader, "Street  ");
        writeString(ClientEntityConstants.ADDRESS_LINE_1_COL, rowHeader, "Address Line 1");
        writeString(ClientEntityConstants.ADDRESS_LINE_2_COL, rowHeader, "Address Line 2");
        writeString(ClientEntityConstants.ADDRESS_LINE_3_COL, rowHeader, "Address Line 3");
        writeString(ClientEntityConstants.CITY_COL, rowHeader, "City");
        writeString(ClientEntityConstants.STATE_PROVINCE_COL, rowHeader, "State/ Province");
        writeString(ClientEntityConstants.COUNTRY_COL, rowHeader, "Country");
        writeString(ClientEntityConstants.POSTAL_CODE_COL, rowHeader, "Postal Code");
        writeString(ClientEntityConstants.IS_ACTIVE_ADDRESS_COL, rowHeader, "Is active Address ? ");
        writeString(ClientEntityConstants.STATUS_COL, rowHeader, TemplatePopulateImportConstants.STATUS_COLUMN_HEADER);

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
        writeString(currentCol, rowHeader, "Lookup Constitution ");
        int lookupConstitutionCol = currentCol++;

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

        worksheet.setColumnWidth(currentCol, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        writeString(currentCol, rowHeader, "Lookup Business Line");
        int lookupBusinessLineCol = currentCol++;

        // Populate visible lookup values under lookup headers (for user reference)
        // These are read-only reference values; validations still use the hidden lookup sheet
        populateLookupValues(worksheet, lookupOfficeNameCol, lookupOfficeDateCol, lookupConstitutionCol, lookupClientTypesCol,
                lookupClientClassificationCol, lookupAddressTypeCol, lookupStateProvinceCol, lookupCountryCol, lookupBusinessLineCol);
    }

    private void populateLookupValues(Sheet worksheet, int lookupOfficeNameCol, int lookupOfficeDateCol, int lookupConstitutionCol,
            int lookupClientTypesCol, int lookupClientClassificationCol, int lookupAddressTypeCol, int lookupStateProvinceCol,
            int lookupCountryCol, int lookupBusinessLineCol) {

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

        // Populate Constitution lookup values
        rowIndex = 1;
        for (CodeValueData constitutionCodeValue : constitutionCodeValues) {
            Row row = worksheet.getRow(rowIndex);
            if (row == null) {
                row = worksheet.createRow(rowIndex);
            }
            writeString(lookupConstitutionCol, row, constitutionCodeValue.getName() + " (" + constitutionCodeValue.getId() + ")");
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

        // Populate Business Line lookup values
        rowIndex = 1;
        for (CodeValueData mainBusinessCodeValue : mainBusinesslineCodeValues) {
            Row row = worksheet.getRow(rowIndex);
            if (row == null) {
                row = worksheet.createRow(rowIndex);
            }
            writeString(lookupBusinessLineCol, row, mainBusinessCodeValue.getName() + " (" + mainBusinessCodeValue.getId() + ")");
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
        int startCol = ClientEntityConstants.STATUS_COL + 1;
        try {
            int currentCol = startCol;
            int lookupSheetCol = 7; // Start after Country (column G = index 6), so next available is H = index 7

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
                                datatableDateColumns.put(currentCol,
                                        new ClientEntityWorkbookPopulator.DatatableDateInfo(currentCol, column.isDateTimeDisplayType()));
                            }
                            // Check if this is a boolean column
                            else if (column.isBooleanDisplayType()) {
                                datatableBooleanColumns.put(currentCol, new ClientEntityWorkbookPopulator.DatatableBooleanInfo(currentCol));
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
     * Populates datatable dropdown values into the hidden lookup sheet and adds data validation on the client sheet's
     * dropdown columns. Named ranges for these columns are already created in {@link #setNames(Sheet, List)}; this only
     * fills in the values they refer to and wires up validation. All operations are wrapped in try/catch to ensure
     * datatable failures never break template generation.
     *
     * @param worksheet
     *            The client sheet
     * @param lookupSheet
     *            The hidden lookup sheet
     */
    private void handleDatatableColumnsComplete(Sheet worksheet, Sheet lookupSheet) {
        try {
            // Populate datatable dropdown values into lookup sheet
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

            // Add data validation for datatable dropdown columns
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

    private void setRules(Sheet worksheet, String dateFormat) {
        CellRangeAddressList officeNameRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                ClientEntityConstants.OFFICE_NAME_COL, ClientEntityConstants.OFFICE_NAME_COL);
        CellRangeAddressList staffNameRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                ClientEntityConstants.STAFF_NAME_COL, ClientEntityConstants.STAFF_NAME_COL);
        CellRangeAddressList submittedOnDateRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                ClientEntityConstants.SUBMITTED_ON_COL, ClientEntityConstants.SUBMITTED_ON_COL);
        CellRangeAddressList dateRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                ClientEntityConstants.ACTIVATION_DATE_COL, ClientEntityConstants.ACTIVATION_DATE_COL);
        CellRangeAddressList activeRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                ClientEntityConstants.ACTIVE_COL, ClientEntityConstants.ACTIVE_COL);
        CellRangeAddressList clientTypeRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                ClientEntityConstants.CLIENT_TYPE_COL, ClientEntityConstants.CLIENT_TYPE_COL);
        CellRangeAddressList constitutionRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                ClientEntityConstants.CONSTITUTION_COL, ClientEntityConstants.CONSTITUTION_COL);
        CellRangeAddressList mainBusinessLineRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                ClientEntityConstants.MAIN_BUSINESS_LINE, ClientEntityConstants.MAIN_BUSINESS_LINE);
        CellRangeAddressList clientClassificationRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                ClientEntityConstants.CLIENT_CLASSIFICATION_COL, ClientEntityConstants.CLIENT_CLASSIFICATION_COL);
        CellRangeAddressList enabledAddressRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                ClientEntityConstants.ADDRESS_ENABLED, ClientEntityConstants.ADDRESS_ENABLED);
        CellRangeAddressList addressTypeRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                ClientEntityConstants.ADDRESS_TYPE_COL, ClientEntityConstants.ADDRESS_TYPE_COL);
        CellRangeAddressList stateProvinceRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                ClientEntityConstants.STATE_PROVINCE_COL, ClientEntityConstants.STATE_PROVINCE_COL);
        CellRangeAddressList countryRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                ClientEntityConstants.COUNTRY_COL, ClientEntityConstants.COUNTRY_COL);
        CellRangeAddressList activeAddressRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                ClientEntityConstants.IS_ACTIVE_ADDRESS_COL, ClientEntityConstants.IS_ACTIVE_ADDRESS_COL);
        CellRangeAddressList incorporateDateRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                ClientEntityConstants.INCOPORATION_DATE_COL, ClientEntityConstants.INCOPORATION_DATE_COL);
        CellRangeAddressList incorporateDateTillRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                ClientEntityConstants.INCOPORATION_VALID_TILL_COL, ClientEntityConstants.INCOPORATION_VALID_TILL_COL);

        DataValidationHelper validationHelper = new HSSFDataValidationHelper((HSSFSheet) worksheet);

        List<OfficeData> offices = officeSheetPopulator.getOffices();
        setNames(worksheet, offices);

        DataValidationConstraint officeNameConstraint = validationHelper.createFormulaListConstraint("Office");
        DataValidationConstraint staffNameConstraint = validationHelper
                .createFormulaListConstraint("INDIRECT(CONCATENATE(\"Staff_\",$B1))");
        DataValidationConstraint submittedOnDateConstraint = validationHelper
                .createDateConstraint(DataValidationConstraint.OperatorType.LESS_OR_EQUAL, "=TODAY()", null, dateFormat);
        DataValidationConstraint activationDateConstraint = validationHelper
                .createDateConstraint(DataValidationConstraint.OperatorType.GREATER_OR_EQUAL, "=$O1", null, dateFormat);
        DataValidationConstraint activeConstraint = validationHelper.createExplicitListConstraint(new String[] { "True", "False" });
        DataValidationConstraint clientTypesConstraint = validationHelper.createFormulaListConstraint("ClientTypes");
        DataValidationConstraint constitutionConstraint = validationHelper.createFormulaListConstraint("Constitution");
        DataValidationConstraint mainBusinessLineConstraint = validationHelper.createFormulaListConstraint("MainBusinessLine");
        DataValidationConstraint clientClassificationConstraint = validationHelper.createFormulaListConstraint("ClientClassification");
        DataValidationConstraint enabledAddressConstraint = validationHelper.createExplicitListConstraint(new String[] { "True", "False" });
        DataValidationConstraint addressTypeConstraint = validationHelper.createFormulaListConstraint("AddressType");
        DataValidationConstraint stateProvinceConstraint = validationHelper.createFormulaListConstraint("StateProvince");
        DataValidationConstraint countryConstraint = validationHelper.createFormulaListConstraint("Country");
        DataValidationConstraint activeAddressConstraint = validationHelper.createExplicitListConstraint(new String[] { "True", "False" });
        DataValidationConstraint incorpDateConstraint = validationHelper
                .createDateConstraint(DataValidationConstraint.OperatorType.LESS_OR_EQUAL, "=TODAY()", null, dateFormat);
        DataValidationConstraint incorpDateTillConstraint = validationHelper
                .createDateConstraint(DataValidationConstraint.OperatorType.GREATER_OR_EQUAL, "=TODAY()", null, dateFormat);

        DataValidation officeValidation = validationHelper.createValidation(officeNameConstraint, officeNameRange);
        DataValidation staffValidation = validationHelper.createValidation(staffNameConstraint, staffNameRange);
        DataValidation submittedOnDateValidation = validationHelper.createValidation(submittedOnDateConstraint, submittedOnDateRange);
        DataValidation activationDateValidation = validationHelper.createValidation(activationDateConstraint, dateRange);
        DataValidation activeValidation = validationHelper.createValidation(activeConstraint, activeRange);
        DataValidation clientTypeValidation = validationHelper.createValidation(clientTypesConstraint, clientTypeRange);
        DataValidation constitutionValidation = validationHelper.createValidation(constitutionConstraint, constitutionRange);
        DataValidation mainBusinessLineValidation = validationHelper.createValidation(mainBusinessLineConstraint, mainBusinessLineRange);
        DataValidation clientClassificationValidation = validationHelper.createValidation(clientClassificationConstraint,
                clientClassificationRange);
        DataValidation enabledAddressValidation = validationHelper.createValidation(enabledAddressConstraint, enabledAddressRange);
        DataValidation addressTypeValidation = validationHelper.createValidation(addressTypeConstraint, addressTypeRange);
        DataValidation stateProvinceValidation = validationHelper.createValidation(stateProvinceConstraint, stateProvinceRange);
        DataValidation countryValidation = validationHelper.createValidation(countryConstraint, countryRange);
        DataValidation activeAddressValidation = validationHelper.createValidation(activeAddressConstraint, activeAddressRange);
        DataValidation incorporateDateValidation = validationHelper.createValidation(incorpDateConstraint, incorporateDateRange);
        DataValidation incorporateDateTillValidation = validationHelper.createValidation(incorpDateTillConstraint,
                incorporateDateTillRange);

        worksheet.addValidationData(activeValidation);
        worksheet.addValidationData(officeValidation);
        worksheet.addValidationData(staffValidation);
        worksheet.addValidationData(activationDateValidation);
        worksheet.addValidationData(submittedOnDateValidation);
        worksheet.addValidationData(clientTypeValidation);
        worksheet.addValidationData(constitutionValidation);
        worksheet.addValidationData(mainBusinessLineValidation);
        worksheet.addValidationData(clientClassificationValidation);
        worksheet.addValidationData(enabledAddressValidation);
        worksheet.addValidationData(addressTypeValidation);
        worksheet.addValidationData(stateProvinceValidation);
        worksheet.addValidationData(countryValidation);
        worksheet.addValidationData(activeAddressValidation);
        worksheet.addValidationData(incorporateDateValidation);
        worksheet.addValidationData(incorporateDateTillValidation);

        // Add date validation for datatable date columns
        for (ClientEntityWorkbookPopulator.DatatableDateInfo dateInfo : datatableDateColumns.values()) {
            try {
                CellRangeAddressList datatableDateRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                        dateInfo.clientSheetColumnIndex, dateInfo.clientSheetColumnIndex);
                // Use same date constraint as submittedOnDate (less than or equal to today)
                DataValidationConstraint datatableDateConstraint = validationHelper
                        .createDateConstraint(DataValidationConstraint.OperatorType.LESS_OR_EQUAL, "=TODAY()", null, dateFormat);
                DataValidation datatableDateValidation = validationHelper.createValidation(datatableDateConstraint, datatableDateRange);
                worksheet.addValidationData(datatableDateValidation);
            } catch (Exception e) {
                log.warn("Failed to add date validation for datatable date column at index {}: {}", dateInfo.clientSheetColumnIndex,
                        e.getMessage());
            }
        }

        // Add boolean validation for datatable boolean columns
        for (ClientEntityWorkbookPopulator.DatatableBooleanInfo booleanInfo : datatableBooleanColumns.values()) {
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

        // Column 2 (C): Constitution
        Name constitutionGroup = clientWorkbook.createName();
        constitutionGroup.setNameName("Constitution");
        int constitutionLastRow = constitutionCodeValues.size() + 1;
        String constitutionCol = CellReference.convertNumToColString(2);
        constitutionGroup
                .setRefersToFormula("'" + lookupSheetName + "'!$" + constitutionCol + "$2:$" + constitutionCol + "$" + constitutionLastRow);

        // Column 3 (D): Main Business Line
        Name mainBusinessLineGroup = clientWorkbook.createName();
        mainBusinessLineGroup.setNameName("MainBusinessLine");
        int mainBusinessLineLastRow = mainBusinesslineCodeValues.size() + 1;
        String mainBusinessLineCol = CellReference.convertNumToColString(3);
        mainBusinessLineGroup.setRefersToFormula(
                "'" + lookupSheetName + "'!$" + mainBusinessLineCol + "$2:$" + mainBusinessLineCol + "$" + mainBusinessLineLastRow);

        // Column 1 (B): Client Classification
        Name clientClassficationGroup = clientWorkbook.createName();
        clientClassficationGroup.setNameName("ClientClassification");
        int clientClassificationLastRow = clientClassificationCodeValues.size() + 1;
        String clientClassificationCol = CellReference.convertNumToColString(1);
        clientClassficationGroup.setRefersToFormula("'" + lookupSheetName + "'!$" + clientClassificationCol + "$2:$"
                + clientClassificationCol + "$" + clientClassificationLastRow);

        // Column 4 (E): Address Type
        Name addressTypeGroup = clientWorkbook.createName();
        addressTypeGroup.setNameName("AddressType");
        int addressTypeLastRow = addressTypesCodeValues.size() + 1;
        String addressTypeCol = CellReference.convertNumToColString(4);
        addressTypeGroup
                .setRefersToFormula("'" + lookupSheetName + "'!$" + addressTypeCol + "$2:$" + addressTypeCol + "$" + addressTypeLastRow);

        // Column 5 (F): State/Province
        Name stateProvinceGroup = clientWorkbook.createName();
        stateProvinceGroup.setNameName("StateProvince");
        int stateProvinceLastRow = stateProvinceCodeValues.size() + 1;
        String stateProvinceCol = CellReference.convertNumToColString(5);
        stateProvinceGroup.setRefersToFormula(
                "'" + lookupSheetName + "'!$" + stateProvinceCol + "$2:$" + stateProvinceCol + "$" + stateProvinceLastRow);

        // Column 6 (G): Country
        Name countryGroup = clientWorkbook.createName();
        countryGroup.setNameName("Country");
        int countryLastRow = countryCodeValues.size() + 1;
        String countryCol = CellReference.convertNumToColString(6);
        countryGroup.setRefersToFormula("'" + lookupSheetName + "'!$" + countryCol + "$2:$" + countryCol + "$" + countryLastRow);

        // Create named ranges for datatable dropdown columns
        for (Map.Entry<String, DatatableDropdownInfo> entry : datatableDropdowns.entrySet()) {
            String namedRangeName = entry.getKey();
            DatatableDropdownInfo dropdownInfo = entry.getValue();
            Name datatableDropdownGroup = clientWorkbook.createName();
            setSanitized(datatableDropdownGroup, namedRangeName);
            int dropdownLastRow = dropdownInfo.values.size() + 1;
            String dropdownCol = CellReference.convertNumToColString(dropdownInfo.lookupSheetColumnIndex);
            datatableDropdownGroup
                    .setRefersToFormula("'" + lookupSheetName + "'!$" + dropdownCol + "$2:$" + dropdownCol + "$" + dropdownLastRow);
        }

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
