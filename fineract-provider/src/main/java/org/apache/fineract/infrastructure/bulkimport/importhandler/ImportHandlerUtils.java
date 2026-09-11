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

import com.google.common.base.Splitter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.exception.AbstractPlatformException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.UnsupportedParameterException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.dataqueries.data.DatatableData;
import org.apache.fineract.infrastructure.dataqueries.data.EntityTables;
import org.apache.fineract.infrastructure.dataqueries.data.ResultsetColumnHeaderData;
import org.apache.fineract.infrastructure.dataqueries.data.StatusEnum;
import org.apache.fineract.infrastructure.dataqueries.domain.EntityDatatableChecks;
import org.apache.fineract.infrastructure.dataqueries.domain.EntityDatatableChecksRepository;
import org.apache.fineract.infrastructure.dataqueries.service.DatatableReadService;
import org.apache.fineract.portfolio.search.service.SearchUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.SheetVisibility;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;

@Slf4j
public final class ImportHandlerUtils {

    private ImportHandlerUtils() {

    }

    public static Integer getNumberOfRows(Sheet sheet, int primaryColumn) {
        Integer noOfEntries = 0;
        // getLastRowNum and getPhysicalNumberOfRows showing false values
        // sometimes
        int maxRows = sheet.getLastRowNum();
        while (noOfEntries < maxRows) {
            Row row = sheet.getRow(noOfEntries + 1);
            if (row == null) {
                break;
            }
            Cell cell = row.getCell(primaryColumn);
            if (cell == null || cell.getCellType() == CellType.BLANK) {
                break;
            }
            noOfEntries++;
        }
        return noOfEntries;
    }

    public static boolean isNotImported(Row row, int statusColumn) {
        if (readAsString(statusColumn, row) != null) {
            return !readAsString(statusColumn, row).equals(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
        } else {
            return true;
        }
    }

    public static Long readAsLong(int colIndex, Row row) {
        Cell c = row.getCell(colIndex);
        if (c == null || c.getCellType() == CellType.BLANK) {
            return null;
        }
        FormulaEvaluator eval = row.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
        if (c.getCellType() == CellType.FORMULA) {
            if (eval != null) {
                CellValue value;
                try {
                    value = eval.evaluate(c);
                    return ((Double) value.getNumberValue()).longValue();
                } catch (Exception e) {
                    log.error("Cell evaluation error: ", e);
                }
            }
            return null;
        } else if (c.getCellType() == CellType.NUMERIC) {
            return ((Double) c.getNumericCellValue()).longValue();
        } else {
            return Long.parseLong(row.getCell(colIndex).getStringCellValue());
        }
    }

    public static String readAsString(int colIndex, Row row) {

        Cell c = row.getCell(colIndex);
        if (c == null || c.getCellType() == CellType.BLANK) {
            return null;
        }
        FormulaEvaluator eval = row.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
        if (c.getCellType() == CellType.FORMULA) {
            if (eval != null) {
                CellValue value;
                try {
                    value = eval.evaluate(c);

                    String res = trimEmptyDecimalPortion(value.getStringValue());

                    if (StringUtils.isNotEmpty(res)) {
                        return res.trim();
                    }
                } catch (Exception e) {
                    log.error("Cell evaluation error: ", e);
                }
            }
            return null;
        } else if (c.getCellType() == CellType.STRING) {
            String res = trimEmptyDecimalPortion(c.getStringCellValue().trim());
            return res.trim();

        } else if (c.getCellType() == CellType.NUMERIC) {
            return ((Double) row.getCell(colIndex).getNumericCellValue()).intValue() + "";
        } else if (c.getCellType() == CellType.BOOLEAN) {
            return c.getBooleanCellValue() + "";
        } else {
            return null;
        }
    }

    public static String trimEmptyDecimalPortion(String result) {
        if (result != null && result.endsWith(".0")) {
            return Splitter.on("\\.").split(result).iterator().next();
        } else {
            return result;
        }
    }

    public static LocalDate readAsDate(int colIndex, Row row) {
        Cell c = row.getCell(colIndex);
        if (c == null || c.getCellType() == CellType.BLANK) {
            return null;
        }

        return LocalDate.ofInstant(c.getDateCellValue().toInstant(), DateUtils.getDateTimeZoneOfTenant());
    }

    public static Boolean readAsBoolean(int colIndex, Row row) {
        Cell c = row.getCell(colIndex);
        if (c == null || c.getCellType() == CellType.BLANK) {
            return false;
        }
        FormulaEvaluator eval = row.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
        if (c.getCellType() == CellType.FORMULA) {
            if (eval != null) {
                CellValue value;
                try {
                    value = eval.evaluate(c);
                    return value.getBooleanValue();
                } catch (Exception e) {
                    log.error("Cell evaluation error: ", e);
                }
            }
            return false;
        } else if (c.getCellType() == CellType.BOOLEAN) {
            return c.getBooleanCellValue();
        } else {
            String booleanString = row.getCell(colIndex).getStringCellValue().trim();
            if (booleanString.equalsIgnoreCase("TRUE")) {
                return true;
            } else {
                return false;
            }
        }
    }

    public static Integer readAsInt(int colIndex, Row row) {
        Cell c = row.getCell(colIndex);
        if (c == null || c.getCellType() == CellType.BLANK) {
            return null;
        }
        FormulaEvaluator eval = row.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
        if (c.getCellType() == CellType.FORMULA) {
            if (eval != null) {
                CellValue value;
                try {
                    value = eval.evaluate(c);
                    return ((Double) value.getNumberValue()).intValue();
                } catch (Exception e) {
                    log.error("Cell evaluation error: ", e);
                }
            }
            return null;
        } else if (c.getCellType() == CellType.NUMERIC) {
            return ((Double) c.getNumericCellValue()).intValue();
        } else {
            return Integer.parseInt(row.getCell(colIndex).getStringCellValue());
        }
    }

    public static Double readAsDouble(int colIndex, Row row) {
        Cell c = row.getCell(colIndex);
        if (c == null || c.getCellType() == CellType.BLANK) {
            return 0.0;
        }
        FormulaEvaluator eval = row.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
        if (c.getCellType() == CellType.FORMULA) {
            if (eval != null) {
                CellValue value;
                try {
                    value = eval.evaluate(c);
                    return value.getNumberValue();
                } catch (Exception e) {
                    log.error("Cell evaluation error: ", e);
                }
            }
            return 0.0;
        } else if (c.getCellType() == CellType.NUMERIC) {
            return row.getCell(colIndex).getNumericCellValue();
        } else {
            return Double.parseDouble(row.getCell(colIndex).getStringCellValue());
        }
    }

    public static void writeString(int colIndex, Row row, String value) {
        if (value != null) {
            row.createCell(colIndex).setCellValue(value);
        }
    }

    public static CellStyle getCellStyle(Workbook workbook, IndexedColors color) {
        CellReference cellReference = new CellReference("A1");
        Sheet predefined = workbook.getSheet(color.toString());
        // if we have already defined this style, return it and don't create
        // another one
        if (predefined != null) {
            Row row = predefined.getRow(cellReference.getRow());
            Cell cell = row.getCell(cellReference.getCol());
            return cell.getCellStyle();
        }
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(color.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Sheet cache = workbook.createSheet(color.toString());
        workbook.setSheetVisibility(workbook.getSheetIndex(cache), SheetVisibility.VERY_HIDDEN);
        Row row = cache.createRow(cellReference.getRow());
        Cell cell = row.createCell(cellReference.getCol());
        cell.setCellStyle(style);

        return style;
    }

    public static String getDefaultUserMessages(List<ApiParameterError> ApiParameterErrorList) {
        StringBuilder defaultUserMessages = new StringBuilder();
        for (ApiParameterError error : ApiParameterErrorList) {
            defaultUserMessages = defaultUserMessages.append(error.getDefaultUserMessage() + '\t');
        }
        return defaultUserMessages.toString();
    }

    public static String getErrorList(List<String> errorList) {
        StringBuilder errors = new StringBuilder();
        for (String error : errorList) {
            errors = errors.append(error);
        }
        return errors.toString();
    }

    public static void writeErrorMessage(Sheet sheet, Integer rowIndex, String errorMessage, int statusColumn) {
        Cell statusCell = sheet.getRow(rowIndex).createCell(statusColumn);
        statusCell.setCellValue(errorMessage);
        statusCell.setCellStyle(getCellStyle(sheet.getWorkbook(), IndexedColors.RED));
    }

    public static String getErrorMessage(RuntimeException re) {
        if (re instanceof PlatformApiDataValidationException validationException && !validationException.getErrors().isEmpty()) {
            return getDefaultUserMessages(validationException.getErrors());
        } else if (re instanceof AbstractPlatformException) {
            AbstractPlatformException abstractPlatformException = (AbstractPlatformException) re;
            return abstractPlatformException.getDefaultUserMessage();
        } else if (re instanceof UnsupportedParameterException) {
            UnsupportedParameterException unsupportedParameterException = (UnsupportedParameterException) re;
            return getErrorList(unsupportedParameterException.getUnsupportedParameters());
        } else {
            if (re.getMessage() != null) {
                return re.getMessage();
            } else {
                return re.getClass().getCanonicalName();
            }
        }
    }

    public static Long getIdByName(Sheet sheet, String name) {
        String sheetName = sheet.getSheetName();
        if (!sheetName.equals(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME)) {
            for (Row row : sheet) {
                for (Cell cell : row) {
                    if (name != null) {
                        if (cell.getCellType() == CellType.STRING && cell.getRichStringCellValue().getString().trim().equals(name)) {
                            if (sheetName.equals(TemplatePopulateImportConstants.OFFICE_SHEET_NAME)
                                    || sheetName.equals(TemplatePopulateImportConstants.GL_ACCOUNTS_SHEET_NAME)
                                    || sheetName.equals(TemplatePopulateImportConstants.EXTRAS_SHEET_NAME)
                                    || sheetName.equals(TemplatePopulateImportConstants.CHARGE_SHEET_NAME)
                                    || sheetName.equals(TemplatePopulateImportConstants.SHARED_PRODUCTS_SHEET_NAME)
                                    || sheetName.equals(TemplatePopulateImportConstants.ROLES_SHEET_NAME)) {
                                if (row.getCell(cell.getColumnIndex() - 1).getCellType() == CellType.NUMERIC) {
                                    return ((Double) row.getCell(cell.getColumnIndex() - 1).getNumericCellValue()).longValue();
                                }
                                return 0L;
                            } else if (sheetName.equals(TemplatePopulateImportConstants.CLIENT_SHEET_NAME)
                                    || sheetName.equals(TemplatePopulateImportConstants.CENTER_SHEET_NAME)
                                    || sheetName.equals(TemplatePopulateImportConstants.GROUP_SHEET_NAME)
                                    || sheetName.equals(TemplatePopulateImportConstants.STAFF_SHEET_NAME)) {
                                if (row.getCell(cell.getColumnIndex() + 1).getCellType() == CellType.NUMERIC) {
                                    return ((Double) row.getCell(cell.getColumnIndex() + 1).getNumericCellValue()).longValue();
                                }
                            }
                            return 0L;
                        }
                    } else {
                        return null;
                    }
                }
            }
        } else {
            for (Row row : sheet) {
                for (int i = 0; i < 2; i++) {
                    if (name != null) {
                        Cell cell = row.getCell(i);
                        if (cell.getCellType() == CellType.STRING && cell.getRichStringCellValue().getString().trim().equals(name)) {
                            return ((Double) row.getCell(cell.getColumnIndex() - 1).getNumericCellValue()).longValue();
                        }
                    } else {
                        return 0L;
                    }
                }
            }
        }
        return 0L;
    }

    public static EnumOptionData getChargeTimeTypeEmun(Sheet sheet, String name) {
        String sheetName = sheet.getSheetName();
        String chargeTimeType = "";
        EnumOptionData chargeTimeTypeEnum = null;
        if (sheetName.equals(TemplatePopulateImportConstants.CHARGE_SHEET_NAME)) {
            for (Row row : sheet) {
                for (Cell cell : row) {
                    if (name != null) {
                        if (cell.getCellType() == CellType.STRING && cell.getRichStringCellValue().getString().trim().equals(name)) {
                            chargeTimeType = row.getCell(cell.getColumnIndex() + 3).getStringCellValue().toString();

                        }
                    }
                }
            }
        }
        if (!chargeTimeType.equals("")) {
            String chargeTimeTypeId = "";
            if (chargeTimeType.equalsIgnoreCase("Disbursement")) {
                chargeTimeTypeId = "1";
            }
            chargeTimeTypeEnum = new EnumOptionData(null, null, chargeTimeTypeId);
        }
        return chargeTimeTypeEnum;
    }

    public static EnumOptionData getChargeAmountTypeEnum(final String chargeAmountType) {
        EnumOptionData chargeAmountTypeEnum = null;
        if (chargeAmountType != null) {
            String chargeAmountTypeId = "";
            if (chargeAmountType.equalsIgnoreCase("Flat")) {
                chargeAmountTypeId = "1";
            } else if (chargeAmountType.equalsIgnoreCase("% Amount")) {
                chargeAmountTypeId = "2";
            } else {
                chargeAmountTypeId = chargeAmountType;
            }
            chargeAmountTypeEnum = new EnumOptionData(null, null, chargeAmountTypeId);
        }
        return chargeAmountTypeEnum;
    }

    public static String getCodeByName(Sheet sheet, String name) {
        String sheetName = sheet.getSheetName();
        if (sheetName.equals(TemplatePopulateImportConstants.EXTRAS_SHEET_NAME)) {
            for (Row row : sheet) {
                for (Cell cell : row) {
                    if (name != null) {
                        if (cell.getCellType() == CellType.STRING && cell.getRichStringCellValue().getString().trim().equals(name)) {
                            return row.getCell(cell.getColumnIndex() - 1).getStringCellValue().toString();

                        }
                    }
                }
            }
        }
        return "";
    }

    public static String getFrequencyId(String frequency) {
        if (frequency != null) {
            if (frequency.equalsIgnoreCase(TemplatePopulateImportConstants.FREQUENCY_DAILY)) {
                frequency = "1";
            } else if (frequency.equalsIgnoreCase(TemplatePopulateImportConstants.FREQUENCY_WEEKLY)) {
                frequency = "2";
            } else if (frequency.equalsIgnoreCase(TemplatePopulateImportConstants.FREQUENCY_MONTHLY)) {
                frequency = "3";
            } else if (frequency.equalsIgnoreCase(TemplatePopulateImportConstants.FREQUENCY_YEARLY)) {
                frequency = "4";
            }
            return frequency;
        } else {
            return null;
        }
    }

    public static String getRepeatsOnDayId(String repeatsOnDay) {
        if (repeatsOnDay != null) {
            if (repeatsOnDay.equalsIgnoreCase(TemplatePopulateImportConstants.MONDAY)) {
                repeatsOnDay = "1";
            } else if (repeatsOnDay.equalsIgnoreCase(TemplatePopulateImportConstants.TUESDAY)) {
                repeatsOnDay = "2";
            } else if (repeatsOnDay.equalsIgnoreCase(TemplatePopulateImportConstants.WEDNESDAY)) {
                repeatsOnDay = "3";
            } else if (repeatsOnDay.equalsIgnoreCase(TemplatePopulateImportConstants.THURSDAY)) {
                repeatsOnDay = "4";
            } else if (repeatsOnDay.equalsIgnoreCase(TemplatePopulateImportConstants.FRIDAY)) {
                repeatsOnDay = "5";
            } else if (repeatsOnDay.equalsIgnoreCase(TemplatePopulateImportConstants.SATURDAY)) {
                repeatsOnDay = "6";
            } else if (repeatsOnDay.equalsIgnoreCase(TemplatePopulateImportConstants.SUNDAY)) {
                repeatsOnDay = "7";
            }
            return repeatsOnDay;
        } else {
            return null;
        }
    }

    /**
     * Validates that all required datatables are present in the Excel workbook for the given row.
     *
     * Validation logic: 1. Multi-row datatables (1:N with entity) are always skipped - they are optional/repeatable
     * child data. 2. If NO column header exists for a required datatable, validation is skipped entirely. This allows
     * required datatables to be optional in the Excel sheet if no columns are present. 3. If column headers EXIST for a
     * required datatable: a. Retrieve the datatable schema to identify mandatory (non-nullable) columns. b. If the
     * datatable has ZERO mandatory columns (all columns nullable), validation passes even if all values are blank. c.
     * If there are mandatory columns, validate that each mandatory column either: - Exists in the Excel header AND has
     * a non-blank value, OR - Is missing from the Excel header (treated as missing field). d. System columns (id,
     * client_id, created_at, updated_at) and locale/dateFormat are ignored.
     *
     * This ensures that: - Optional datatables (not present in Excel) → no validation error - Required datatables with
     * all nullable columns (present but empty) → no validation error - Required datatables with mandatory columns
     * (present but missing mandatory values) → validation error - Multi-row datatables → always skipped
     *
     * @param workbook
     *            The Excel workbook
     * @param sheet
     *            The sheet containing the client data
     * @param row
     *            The data row to validate
     * @param entitySubtype
     *            The entity subtype (PERSON or ENTITY)
     * @param entityDatatableChecksRepository
     *            Repository to fetch required datatables
     * @param datatableReadService
     *            Service to retrieve datatable metadata (can be null, will skip multi-row and mandatory field checks if
     *            null)
     * @return Error message if validation fails (format: "Missing required datatable fields in <datatable>: <field1>,
     *         <field2>"), null if validation passes
     */
    public static String validateRequiredDatatables(Workbook workbook, Sheet sheet, Row row, String entitySubtype,
            EntityDatatableChecksRepository entityDatatableChecksRepository, DatatableReadService datatableReadService) {
        if (entityDatatableChecksRepository == null) {
            return null;
        }

        // Get required datatables for CLIENT entity with CREATE status
        List<EntityDatatableChecks> requiredChecks;
        if (entitySubtype != null) {
            requiredChecks = entityDatatableChecksRepository.findByEntityAndStatusAndSubtype(EntityTables.CLIENT.getName(),
                    StatusEnum.CREATE.getValue(), entitySubtype);
        } else {
            requiredChecks = entityDatatableChecksRepository.findByEntityAndStatus(EntityTables.CLIENT.getName(),
                    StatusEnum.CREATE.getValue());
        }

        if (requiredChecks == null || requiredChecks.isEmpty()) {
            return null; // No required datatables
        }

        // Get header row (row 0)
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            return "Missing required datatable(s): " + getDatatableNames(requiredChecks);
        }

        // Build a map of column headers to column indices
        Map<String, Integer> headerColumnMap = new HashMap<>();
        for (int colIndex = 0; colIndex <= headerRow.getLastCellNum(); colIndex++) {
            Cell headerCell = headerRow.getCell(colIndex);
            if (headerCell != null && headerCell.getCellType() == CellType.STRING) {
                String headerValue = headerCell.getStringCellValue();
                if (headerValue != null) {
                    headerColumnMap.put(headerValue.trim(), colIndex);
                }
            }
        }

        // Check each required datatable
        List<String> missingDatatables = new ArrayList<>();
        for (EntityDatatableChecks check : requiredChecks) {
            String datatableName = check.getDatatableName();

            // Skip validation for multi-row datatables - they are optional/repeatable child data
            if (datatableReadService != null) {
                try {
                    DatatableData datatableData = datatableReadService.retrieveDatatable(datatableName);
                    if (datatableData != null && datatableData.isMultiRow()) {
                        // Multi-row datatables are optional, skip validation
                        log.debug("Skipping validation for multi-row datatable '{}' - treated as optional/repeatable child data",
                                datatableName);
                        continue;
                    }
                } catch (Exception e) {
                    // If we can't retrieve datatable metadata, continue with validation
                    // This preserves backward compatibility
                    log.debug("Could not retrieve datatable metadata for '{}' to check multi-row status: {}", datatableName,
                            e.getMessage());
                }
            }

            // First, check if ANY column header exists for this datatable (presence check)
            boolean headerExists = false;
            List<Integer> matchingColumnIndices = new ArrayList<>();

            // Check if any column header matches the datatable name pattern
            // Prefer dot notation (new format), fallback to underscore notation (legacy)
            for (Map.Entry<String, Integer> entry : headerColumnMap.entrySet()) {
                String header = entry.getKey();
                int colIndex = entry.getValue();

                // Remove trailing * if present
                String headerWithoutStar = header.endsWith("*") ? header.substring(0, header.length() - 1) : header;

                boolean matches = false;

                // Check dot notation first (preferred format): registeredTableName.columnName
                if (headerWithoutStar.contains(".")) {
                    int dotIndex = headerWithoutStar.indexOf(".");
                    if (dotIndex > 0) {
                        String headerDatatableName = headerWithoutStar.substring(0, dotIndex);
                        if (headerDatatableName.equals(datatableName)) {
                            matches = true;
                        }
                    }
                }
                // Fallback to underscore notation (legacy format) for backward compatibility
                else if (headerWithoutStar.contains("_")) {
                    int lastUnderscoreIndex = headerWithoutStar.lastIndexOf("_");
                    if (lastUnderscoreIndex > 0) {
                        String headerDatatableName = headerWithoutStar.substring(0, lastUnderscoreIndex);
                        if (headerDatatableName.equals(datatableName)) {
                            matches = true;
                        }
                    }
                }

                if (matches) {
                    headerExists = true;
                    matchingColumnIndices.add(colIndex);
                }
            }

            // If no header exists for this datatable, skip validation entirely
            // This handles the case where a required datatable is configured but not present in the Excel sheet
            if (!headerExists) {
                log.debug("Skipping validation for required datatable '{}' - no column headers found in Excel sheet. "
                        + "Validation only applies when at least one column header for the datatable is present.", datatableName);
                continue;
            }

            // Header exists, so now validate mandatory columns
            // Build a map of Excel column names (without datatable prefix) to column indices
            Map<String, Integer> excelColumnMap = new HashMap<>();
            for (Map.Entry<String, Integer> entry : headerColumnMap.entrySet()) {
                String header = entry.getKey();
                int colIndex = entry.getValue();

                // Remove trailing * if present
                String headerWithoutStar = header.endsWith("*") ? header.substring(0, header.length() - 1) : header;

                String columnName = null;
                // Check dot notation first (preferred format): registeredTableName.columnName
                if (headerWithoutStar.contains(".")) {
                    int dotIndex = headerWithoutStar.indexOf(".");
                    if (dotIndex > 0 && dotIndex < headerWithoutStar.length() - 1) {
                        String headerDatatableName = headerWithoutStar.substring(0, dotIndex);
                        if (headerDatatableName.equals(datatableName)) {
                            columnName = headerWithoutStar.substring(dotIndex + 1);
                        }
                    }
                }
                // Fallback to underscore notation (legacy format) for backward compatibility
                else if (headerWithoutStar.contains("_")) {
                    int lastUnderscoreIndex = headerWithoutStar.lastIndexOf("_");
                    if (lastUnderscoreIndex > 0 && lastUnderscoreIndex < headerWithoutStar.length() - 1) {
                        String headerDatatableName = headerWithoutStar.substring(0, lastUnderscoreIndex);
                        if (headerDatatableName.equals(datatableName)) {
                            columnName = headerWithoutStar.substring(lastUnderscoreIndex + 1);
                        }
                    }
                }

                if (columnName != null) {
                    excelColumnMap.put(columnName, colIndex);
                }
            }

            // Retrieve datatable schema to check for mandatory columns
            List<String> missingMandatoryFields = new ArrayList<>();
            if (datatableReadService != null) {
                try {
                    DatatableData datatableData = datatableReadService.retrieveDatatable(datatableName);
                    if (datatableData != null && datatableData.getColumnHeaderData() != null) {
                        // Find all mandatory columns (non-nullable, excluding system columns)
                        List<String> mandatoryColumns = new ArrayList<>();
                        for (ResultsetColumnHeaderData column : datatableData.getColumnHeaderData()) {
                            String columnName = column.getColumnName();

                            // Ignore system columns
                            if (columnName.equalsIgnoreCase("id") || columnName.equalsIgnoreCase("client_id")
                                    || columnName.equalsIgnoreCase("created_at") || columnName.equalsIgnoreCase("updated_at")
                                    || columnName.equalsIgnoreCase("locale") || columnName.equalsIgnoreCase("dateFormat")) {
                                continue;
                            }

                            // Check if column is mandatory (not nullable)
                            if (!column.getIsColumnNullable()) {
                                mandatoryColumns.add(columnName);
                            }
                        }

                        // If there are no mandatory columns, skip validation (all columns are nullable)
                        if (mandatoryColumns.isEmpty()) {
                            log.debug("Skipping validation for required datatable '{}' - no mandatory fields found. "
                                    + "All columns are nullable, so blank values are allowed.", datatableName);
                            continue;
                        }

                        // Check each mandatory column
                        for (String mandatoryColumn : mandatoryColumns) {
                            Integer colIndex = excelColumnMap.get(mandatoryColumn);

                            if (colIndex == null) {
                                // Mandatory column is missing from Excel header
                                missingMandatoryFields.add(mandatoryColumn);
                            } else {
                                // Check if the cell has a non-blank value
                                Cell dataCell = row.getCell(colIndex);
                                boolean hasValue = false;
                                if (dataCell != null && dataCell.getCellType() != CellType.BLANK) {
                                    String cellValue = readAsString(colIndex, row);
                                    if (cellValue != null && !cellValue.trim().isEmpty()) {
                                        hasValue = true;
                                    }
                                }

                                if (!hasValue) {
                                    missingMandatoryFields.add(mandatoryColumn);
                                }
                            }
                        }

                        // If mandatory fields are missing, add to error list
                        if (!missingMandatoryFields.isEmpty()) {
                            String friendlyName = getFriendlyDatatableName(datatableName);
                            String fieldsList = String.join(", ", missingMandatoryFields);
                            missingDatatables.add(friendlyName + ": " + fieldsList);
                            log.debug("Failing validation for required datatable '{}' - missing mandatory fields: {}", datatableName,
                                    fieldsList);
                        }
                    } else {
                        // If we can't retrieve datatable schema, fall back to old behavior
                        // Check if at least one column has a non-empty value
                        boolean hasValue = false;
                        for (Integer colIndex : matchingColumnIndices) {
                            Cell dataCell = row.getCell(colIndex);
                            if (dataCell != null && dataCell.getCellType() != CellType.BLANK) {
                                String cellValue = readAsString(colIndex, row);
                                if (cellValue != null && !cellValue.trim().isEmpty()) {
                                    hasValue = true;
                                    break;
                                }
                            }
                        }

                        if (!hasValue) {
                            String friendlyName = getFriendlyDatatableName(datatableName);
                            missingDatatables.add(friendlyName);
                            log.debug("Required datatable '{}' has column headers in Excel but no values provided "
                                    + "(datatable schema unavailable for mandatory field check)", datatableName);
                        }
                    }
                } catch (Exception e) {
                    // If we can't retrieve datatable schema, fall back to old behavior
                    // Check if at least one column has a non-empty value
                    boolean hasValue = false;
                    for (Integer colIndex : matchingColumnIndices) {
                        Cell dataCell = row.getCell(colIndex);
                        if (dataCell != null && dataCell.getCellType() != CellType.BLANK) {
                            String cellValue = readAsString(colIndex, row);
                            if (cellValue != null && !cellValue.trim().isEmpty()) {
                                hasValue = true;
                                break;
                            }
                        }
                    }

                    if (!hasValue) {
                        String friendlyName = getFriendlyDatatableName(datatableName);
                        missingDatatables.add(friendlyName);
                        log.debug("Required datatable '{}' has column headers in Excel but no values provided "
                                + "(error retrieving datatable schema: {})", datatableName, e.getMessage());
                    }
                }
            } else {
                // If datatableReadService is null, fall back to old behavior
                // Check if at least one column has a non-empty value
                boolean hasValue = false;
                for (Integer colIndex : matchingColumnIndices) {
                    Cell dataCell = row.getCell(colIndex);
                    if (dataCell != null && dataCell.getCellType() != CellType.BLANK) {
                        String cellValue = readAsString(colIndex, row);
                        if (cellValue != null && !cellValue.trim().isEmpty()) {
                            hasValue = true;
                            break;
                        }
                    }
                }

                if (!hasValue) {
                    String friendlyName = getFriendlyDatatableName(datatableName);
                    missingDatatables.add(friendlyName);
                    log.debug("Required datatable '{}' has column headers in Excel but no values provided "
                            + "(datatableReadService unavailable for mandatory field check)", datatableName);
                }
            }
        }

        if (!missingDatatables.isEmpty()) {
            // Format: "Missing required datatable fields in <datatable>: <field1>, <field2>"
            // or "Missing required datatable(s): <datatable>" if no field details available
            List<String> errorMessages = new ArrayList<>();
            for (String missing : missingDatatables) {
                if (missing.contains(": ")) {
                    // Has field details: "Missing required datatable fields in <datatable>: <fields>"
                    errorMessages.add("Missing required datatable fields in " + missing);
                } else {
                    // No field details: fallback to old format
                    errorMessages.add("Missing required datatable(s): " + missing);
                }
            }
            return String.join("; ", errorMessages);
        }

        return null;
    }

    private static String getDatatableNames(List<EntityDatatableChecks> checks) {
        List<String> names = new ArrayList<>();
        for (EntityDatatableChecks check : checks) {
            names.add(getFriendlyDatatableName(check.getDatatableName()));
        }
        return String.join(", ", names);
    }

    private static String getFriendlyDatatableName(String datatableName) {
        // Convert datatable name to a more friendly format
        // e.g., "extra_client_details" -> "Extra Client Details"
        if (datatableName == null || datatableName.isEmpty()) {
            return datatableName;
        }
        // Replace underscores with spaces and capitalize words
        List<String> parts = Splitter.on('_').splitToList(datatableName);
        StringBuilder friendly = new StringBuilder();
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) {
                friendly.append(" ");
            }
            String part = parts.get(i);
            if (!part.isEmpty()) {
                friendly.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) {
                    friendly.append(part.substring(1));
                }
            }
        }
        return friendly.toString();
    }

    /**
     * Reads datatable columns from the Excel row and groups them by registered table name.
     *
     * Preferred format (new): <registeredTableName>.<columnName> or <registeredTableName>.<columnName>* Legacy format
     * (backward compatibility): <registeredTableName>_<columnName> or <registeredTableName>_<columnName>*
     *
     * For dot notation: splits at the first dot (only one is expected). For legacy underscore notation: splits at the
     * last underscore.
     *
     * @param sheet
     *            The sheet containing the client data
     * @param row
     *            The data row to read from
     * @param locale
     *            The locale for datatable data
     * @param dateFormat
     *            The date format for datatable data
     * @return List of maps representing datatables, each with "registeredTableName" and "data" keys. Only includes
     *         datatables with at least one non-empty value.
     */
    public static List<Map<String, Object>> readDatatablesFromRow(Sheet sheet, Row row, String locale, String dateFormat) {
        List<Map<String, Object>> datatablesList = new ArrayList<>();

        if (sheet == null || row == null) {
            return datatablesList;
        }

        // Get header row (row 0)
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            return datatablesList;
        }

        // Build a map of column headers to column indices
        Map<String, Integer> headerColumnMap = new HashMap<>();
        for (int colIndex = 0; colIndex <= headerRow.getLastCellNum(); colIndex++) {
            Cell headerCell = headerRow.getCell(colIndex);
            if (headerCell != null && headerCell.getCellType() == CellType.STRING) {
                String headerValue = headerCell.getStringCellValue();
                if (headerValue != null) {
                    headerColumnMap.put(headerValue.trim(), colIndex);
                }
            }
        }

        // Group datatable columns by datatable name
        Map<String, Map<String, Object>> datatablesMap = new HashMap<>();
        boolean legacyHeaderDetected = false;

        for (Map.Entry<String, Integer> entry : headerColumnMap.entrySet()) {
            String header = entry.getKey();
            int colIndex = entry.getValue();

            // Remove trailing * if present
            String headerWithoutStar = header.endsWith("*") ? header.substring(0, header.length() - 1) : header;

            String registeredTableName;
            String columnName = null;

            // Prefer dot notation (new format): registeredTableName.columnName
            if (headerWithoutStar.contains(".")) {
                int dotIndex = headerWithoutStar.indexOf(".");
                if (dotIndex > 0 && dotIndex < headerWithoutStar.length() - 1) {
                    registeredTableName = headerWithoutStar.substring(0, dotIndex);
                    columnName = headerWithoutStar.substring(dotIndex + 1);
                } else {
                    registeredTableName = null;
                }
            }
            // Fallback to underscore notation (legacy format) for backward compatibility
            else if (headerWithoutStar.contains("_")) {
                legacyHeaderDetected = true;
                // Use lastIndexOf to split at the last underscore
                // Everything before the last _ is the registeredTableName
                // Everything after the last _ is the columnName
                int lastUnderscoreIndex = headerWithoutStar.lastIndexOf("_");
                if (lastUnderscoreIndex > 0 && lastUnderscoreIndex < headerWithoutStar.length() - 1) {
                    registeredTableName = headerWithoutStar.substring(0, lastUnderscoreIndex);
                    columnName = headerWithoutStar.substring(lastUnderscoreIndex + 1);
                } else {
                    registeredTableName = null;
                }
            } else {
                registeredTableName = null;
            }

            // Process if we have a valid datatable column
            if (registeredTableName != null && columnName != null) {
                // Skip system columns
                if (columnName.equalsIgnoreCase("id") || columnName.equalsIgnoreCase("client_id")
                        || columnName.equalsIgnoreCase("created_at") || columnName.equalsIgnoreCase("updated_at")) {
                    continue;
                }

                // Read cell value
                Cell dataCell = row.getCell(colIndex);
                Object cellValue = null;
                if (dataCell != null && dataCell.getCellType() != CellType.BLANK) {
                    // Try to read as different types
                    if (dataCell.getCellType() == CellType.NUMERIC) {
                        if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(dataCell)) {
                            // Date value - format as string using dateFormat
                            LocalDate dateValue = readAsDate(colIndex, row);
                            if (dateValue != null && dateFormat != null) {
                                try {
                                    DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern(dateFormat).toFormatter();
                                    cellValue = dateValue.format(formatter);
                                } catch (Exception e) {
                                    // Fallback to ISO format if dateFormat is invalid
                                    cellValue = dateValue.toString();
                                }
                            } else if (dateValue != null) {
                                cellValue = dateValue.toString();
                            }
                        } else {
                            // Numeric value - check if it's a whole number
                            double numValue = dataCell.getNumericCellValue();
                            if (numValue == Math.floor(numValue)) {
                                cellValue = (long) numValue;
                            } else {
                                cellValue = numValue;
                            }
                        }
                    } else if (dataCell.getCellType() == CellType.BOOLEAN) {
                        cellValue = dataCell.getBooleanCellValue();
                    } else {
                        // String value
                        String stringValue = readAsString(colIndex, row);
                        if (stringValue != null && !stringValue.trim().isEmpty()) {
                            // Try to extract ID from display value format: "Label (ID)"
                            // This handles CODELOOKUP and FK/INTEGER columns that show human-readable values
                            String extractedId = SearchUtil.extractIdFromDisplayValue(stringValue.trim());
                            // If extraction succeeded (returned a different value), try to parse as integer
                            if (!extractedId.equals(stringValue.trim())) {
                                try {
                                    cellValue = Integer.parseInt(extractedId);
                                } catch (NumberFormatException e) {
                                    // If parsing fails, use the original string value
                                    cellValue = stringValue.trim();
                                }
                            } else {
                                // No ID pattern found, use original value
                                cellValue = stringValue.trim();
                            }
                        }
                    }
                }

                // Only add if value is not null/empty
                if (cellValue != null) {
                    // Get or create datatable entry using registeredTableName
                    Map<String, Object> datatableData = datatablesMap.computeIfAbsent(registeredTableName, k -> {
                        Map<String, Object> newDatatable = new HashMap<>();
                        newDatatable.put("registeredTableName", registeredTableName);
                        Map<String, Object> data = new HashMap<>();
                        data.put("locale", locale);
                        data.put("dateFormat", dateFormat);
                        newDatatable.put("data", data);
                        return newDatatable;
                    });

                    // Add column value to data
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = (Map<String, Object>) datatableData.get("data");
                    data.put(columnName, cellValue);
                }
            }
        }

        // Log warning if legacy underscore-based headers were detected
        if (legacyHeaderDetected) {
            log.warn(
                    "Legacy underscore-based datatable column headers detected. Please update template to use dot notation (registeredTableName.columnName) for unambiguous parsing.");
        }

        // Convert map values to list (only include datatables with at least one data field beyond locale/dateFormat)
        for (Map<String, Object> datatable : datatablesMap.values()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) datatable.get("data");
            // Check if there are any fields beyond locale and dateFormat
            if (data.size() > 2) {
                datatablesList.add(datatable);
            }
        }

        return datatablesList;
    }

    /**
     * Ensures all configured client datatables are included in the payload, even if they have no values. This is
     * required for bulk import to match Fineract's contract where all entity-linked datatables must be present. Only
     * includes datatables that are explicitly linked to Client via EntityDatatableChecks.
     *
     * Construction rules: - If datatable has at least one required column (non-nullable): existing behavior applies
     * (must be filled by user) - If datatable has NO required columns: always construct the datatable in payload, even
     * if multi-row - Multi-row datatables with required columns are skipped (user must provide data) - Multi-row
     * datatables with no required columns are included with empty entry
     *
     * @param sheet
     *            The sheet containing the client data
     * @param row
     *            The data row to read from
     * @param locale
     *            The locale for datatable data
     * @param dateFormat
     *            The date format for datatable data
     * @param entityDatatableChecksRepository
     *            Repository to fetch datatables linked to Client entity (can be null, will skip if null)
     * @param datatableReadService
     *            Service to retrieve datatable metadata (can be null, will skip multi-row check if null)
     * @param entitySubtype
     *            The entity subtype (PERSON or ENTITY), can be null
     * @return List of maps representing datatables, each with "registeredTableName" and "data" keys. Includes only
     *         datatables linked via EntityDatatableChecks, with empty entries for those without values.
     */
    public static List<Map<String, Object>> readDatatablesFromRowWithAllConfigured(Sheet sheet, Row row, String locale, String dateFormat,
            EntityDatatableChecksRepository entityDatatableChecksRepository, DatatableReadService datatableReadService,
            String entitySubtype) {
        // First, read datatables that have values from Excel
        List<Map<String, Object>> datatablesWithValues = readDatatablesFromRow(sheet, row, locale, dateFormat);

        // Create a map of registeredTableName -> datatable for quick lookup
        Map<String, Map<String, Object>> datatablesMap = new HashMap<>();
        for (Map<String, Object> datatable : datatablesWithValues) {
            String registeredTableName = (String) datatable.get("registeredTableName");
            if (registeredTableName != null) {
                datatablesMap.put(registeredTableName, datatable);
            }
        }

        // If entityDatatableChecksRepository is available, ensure all linked client datatables are included
        if (entityDatatableChecksRepository != null) {
            try {
                // Retrieve only datatables linked to Client via EntityDatatableChecks with CREATE status
                List<EntityDatatableChecks> linkedChecks;
                if (entitySubtype != null) {
                    linkedChecks = entityDatatableChecksRepository.findByEntityAndStatusAndSubtype(EntityTables.CLIENT.getName(),
                            StatusEnum.CREATE.getValue(), entitySubtype);
                } else {
                    linkedChecks = entityDatatableChecksRepository.findByEntityAndStatus(EntityTables.CLIENT.getName(),
                            StatusEnum.CREATE.getValue());
                }

                // For each linked datatable, ensure it's in the result
                for (EntityDatatableChecks check : linkedChecks) {
                    String registeredTableName = check.getDatatableName();

                    // Skip if already included (has values)
                    if (datatablesMap.containsKey(registeredTableName)) {
                        continue;
                    }

                    // Retrieve datatable schema to check multi-row status and required columns
                    boolean isMultiRow = false;
                    boolean hasRequiredColumns = false;
                    org.apache.fineract.infrastructure.dataqueries.data.DatatableData datatableData = null;

                    if (datatableReadService != null) {
                        try {
                            datatableData = datatableReadService.retrieveDatatable(registeredTableName);
                            if (datatableData != null) {
                                isMultiRow = datatableData.isMultiRow();

                                // Check if datatable has at least one required column
                                if (datatableData.getColumnHeaderData() != null) {
                                    for (ResultsetColumnHeaderData column : datatableData.getColumnHeaderData()) {
                                        String columnName = column.getColumnName();

                                        // Skip system columns
                                        if (columnName.equalsIgnoreCase("id") || columnName.equalsIgnoreCase("client_id")
                                                || columnName.equalsIgnoreCase("created_at") || columnName.equalsIgnoreCase("updated_at")
                                                || columnName.equalsIgnoreCase("locale") || columnName.equalsIgnoreCase("dateFormat")) {
                                            continue;
                                        }

                                        // Check if column is required (not nullable)
                                        if (!column.getIsColumnNullable()) {
                                            hasRequiredColumns = true;
                                            break;
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            // If we can't retrieve datatable metadata, continue (assume not multi-row, no required
                            // columns)
                            log.debug("Could not retrieve datatable metadata for '{}': {}", registeredTableName, e.getMessage());
                        }
                    }

                    // Skip multi-row datatables ONLY if they have required columns
                    // Multi-row datatables with NO required columns should be included (empty entry)
                    if (isMultiRow && hasRequiredColumns) {
                        // Multi-row with required columns: skip (user must provide data)
                        continue;
                    }

                    // Create empty datatable entry with locale and dateFormat
                    Map<String, Object> emptyDatatable = new HashMap<>();
                    emptyDatatable.put("registeredTableName", registeredTableName);
                    Map<String, Object> data = new HashMap<>();
                    data.put("locale", locale);
                    data.put("dateFormat", dateFormat);

                    // Add empty strings for known columns if schema is available
                    if (datatableData != null && datatableData.getColumnHeaderData() != null) {
                        for (ResultsetColumnHeaderData column : datatableData.getColumnHeaderData()) {
                            String columnName = column.getColumnName();
                            // Skip system columns
                            if (columnName.equalsIgnoreCase("id") || columnName.equalsIgnoreCase("client_id")
                                    || columnName.equalsIgnoreCase("created_at") || columnName.equalsIgnoreCase("updated_at")
                                    || columnName.equalsIgnoreCase("locale") || columnName.equalsIgnoreCase("dateFormat")) {
                                continue;
                            }
                            // Add empty string for known columns
                            data.put(columnName, "");
                        }
                    }

                    emptyDatatable.put("data", data);
                    datatablesMap.put(registeredTableName, emptyDatatable);
                }
            } catch (Exception e) {
                // If we can't retrieve linked datatables, log and continue with what we have
                log.debug("Could not retrieve linked client datatables for bulk import payload: {}", e.getMessage());
            }
        }

        // Return all datatables (with values and empty ones)
        return new ArrayList<>(datatablesMap.values());
    }

}
