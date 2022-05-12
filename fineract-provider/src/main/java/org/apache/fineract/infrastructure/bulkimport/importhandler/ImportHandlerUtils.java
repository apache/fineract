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
import java.util.List;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.exception.AbstractPlatformException;
import org.apache.fineract.infrastructure.core.exception.UnsupportedParameterException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
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

public final class ImportHandlerUtils {

    private ImportHandlerUtils() {

    }

    public static Integer getNumberOfRows(Sheet sheet, int primaryColumn) {
        Integer noOfEntries = 0;
        // getLastRowNum and getPhysicalNumberOfRows showing false values
        // sometimes
        while (sheet.getRow(noOfEntries + 1) != null && sheet.getRow(noOfEntries + 1).getCell(primaryColumn) != null) {
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
                CellValue val = null;
                try {
                    val = eval.evaluate(c);
                } catch (NullPointerException npe) {
                    return null;
                }
                return ((Double) val.getNumberValue()).longValue();
            }
        } else if (c.getCellType() == CellType.NUMERIC) {
            return ((Double) c.getNumericCellValue()).longValue();
        } else {
            return Long.parseLong(row.getCell(colIndex).getStringCellValue());
        }
        return null;
    }

    public static String readAsString(int colIndex, Row row) {

        Cell c = row.getCell(colIndex);
        if (c == null || c.getCellType() == CellType.BLANK) {
            return null;
        }
        FormulaEvaluator eval = row.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
        if (c.getCellType() == CellType.FORMULA) {
            if (eval != null) {
                CellValue val = null;
                try {
                    val = eval.evaluate(c);
                } catch (NullPointerException npe) {
                    return null;
                }

                String res = trimEmptyDecimalPortion(val.getStringValue());
                if (res != null) {
                    if (!res.equals("")) {
                        return res.trim();
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }
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

        LocalDate localDate = LocalDate.ofInstant(c.getDateCellValue().toInstant(), DateUtils.getDateTimeZoneOfTenant());
        return localDate;
    }

    public static Boolean readAsBoolean(int colIndex, Row row) {
        Cell c = row.getCell(colIndex);
        if (c == null || c.getCellType() == CellType.BLANK) {
            return false;
        }
        FormulaEvaluator eval = row.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
        if (c.getCellType() == CellType.FORMULA) {
            if (eval != null) {
                CellValue val = null;
                try {
                    val = eval.evaluate(c);
                } catch (NullPointerException npe) {
                    return false;
                }
                return val.getBooleanValue();
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
                CellValue val = null;
                try {
                    val = eval.evaluate(c);
                } catch (NullPointerException npe) {
                    return null;
                }
                return ((Double) val.getNumberValue()).intValue();
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
                CellValue val = null;
                try {
                    val = eval.evaluate(c);
                } catch (NullPointerException npe) {
                    return 0.0;
                }
                return val.getNumberValue();
            } else {
                return 0.0;
            }
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
        if (re instanceof AbstractPlatformException) {
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
                        return 0L;
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
}
