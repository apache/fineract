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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.*;
import org.apache.poi.ss.usermodel.*;
import org.joda.time.LocalDate;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class ImportHandlerUtils {

    public static Integer getNumberOfRows(Sheet sheet, int primaryColumn) {
        Integer noOfEntries = 0;
        // getLastRowNum and getPhysicalNumberOfRows showing false values
        // sometimes
        while (sheet.getRow(noOfEntries+1) !=null && sheet.getRow(noOfEntries+1).getCell(primaryColumn) != null) {
            noOfEntries++;
        }

        return noOfEntries;
    }

    public static boolean isNotImported(Row row, int statusColumn) {
        if (readAsString(statusColumn,row)!=null) {
            return !readAsString(statusColumn, row).equals(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
        }else {
            return true;
        }
    }

    public static Long readAsLong(int colIndex, Row row) {
        Cell c = row.getCell(colIndex);
        if (c == null || c.getCellType() == Cell.CELL_TYPE_BLANK)
            return null;
        FormulaEvaluator eval = row.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
        if(c.getCellType() == Cell.CELL_TYPE_FORMULA) {
            if(eval!=null) {
                CellValue val = eval.evaluate(c);
                return ((Double) val.getNumberValue()).longValue();
            }
        }
        else if (c.getCellType()==Cell.CELL_TYPE_NUMERIC){
            return ((Double) c.getNumericCellValue()).longValue();
        }
        else {
            return Long.parseLong(row.getCell(colIndex).getStringCellValue());
        }
        return null;
    }


    public static String readAsString(int colIndex, Row row) {

        Cell c = row.getCell(colIndex);
        if (c == null || c.getCellType() == Cell.CELL_TYPE_BLANK)
            return null;
        FormulaEvaluator eval = row.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
        if(c.getCellType() == Cell.CELL_TYPE_FORMULA) {
            if (eval!=null) {
                CellValue val = eval.evaluate(c);
                String res = trimEmptyDecimalPortion(val.getStringValue());
                if (res!=null) {
                    if (!res.equals("")) {
                        return res.trim();
                    } else {
                        return null;
                    }
                }else {
                    return null;
                }
            }else {
                return null;
            }
        }else if(c.getCellType()==Cell.CELL_TYPE_STRING) {
            String res = trimEmptyDecimalPortion(c.getStringCellValue().trim());
            return res.trim();

        }else if(c.getCellType()==Cell.CELL_TYPE_NUMERIC) {
            return ((Double) row.getCell(colIndex).getNumericCellValue()).intValue() + "";
        }else if (c.getCellType()==Cell.CELL_TYPE_BOOLEAN){
            return c.getBooleanCellValue()+"";
        }else {
            return null;
        }
    }


    public static String trimEmptyDecimalPortion(String result) {
        if(result != null && result.endsWith(".0"))
            return	result.split("\\.")[0];
        else
            return result;
    }

    public static LocalDate readAsDate(int colIndex, Row row) {
        Cell c = row.getCell(colIndex);
        if(c == null || c.getCellType() == Cell.CELL_TYPE_BLANK)
            return null;

        LocalDate localDate=new LocalDate(c.getDateCellValue());
        return localDate;
    }

    public static Boolean readAsBoolean(int colIndex, Row row) {
            Cell c = row.getCell(colIndex);
            if(c == null || c.getCellType() == Cell.CELL_TYPE_BLANK)
                return false;
            FormulaEvaluator eval = row.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
            if(c.getCellType() == Cell.CELL_TYPE_FORMULA) {
                if(eval!=null) {
                    CellValue val = eval.evaluate(c);
                    return val.getBooleanValue();
                }
                return false;
            }else if(c.getCellType()==Cell.CELL_TYPE_BOOLEAN)
                return c.getBooleanCellValue();
            else {
                String booleanString = row.getCell(colIndex).getStringCellValue().trim();
                if (booleanString.equalsIgnoreCase("TRUE"))
                    return true;
                else
                    return false;
            }
        }

    public static Integer readAsInt(int colIndex, Row row) {
            Cell c = row.getCell(colIndex);
            if (c == null || c.getCellType() == Cell.CELL_TYPE_BLANK)
                return null;
            FormulaEvaluator eval = row.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
            if(c.getCellType() == Cell.CELL_TYPE_FORMULA) {
                if(eval!=null) {
                   CellValue val = eval.evaluate(c);
                    return ((Double) val.getNumberValue()).intValue();
                }
                return null;
            }else if (c.getCellType()==Cell.CELL_TYPE_NUMERIC) {
                return ((Double) c.getNumericCellValue()).intValue();
            }else {
                return Integer.parseInt(row.getCell(colIndex).getStringCellValue());
            }
    }

    public static Double readAsDouble(int colIndex, Row row) {
        Cell c = row.getCell(colIndex);
        if (c == null || c.getCellType() == Cell.CELL_TYPE_BLANK)
            return 0.0;
        FormulaEvaluator eval = row.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
        if(c.getCellType() == Cell.CELL_TYPE_FORMULA) {
                if (eval!=null) {
                    CellValue val = eval.evaluate(c);
                    return val.getNumberValue();
                }else {
                    return 0.0;
                }
        } else if (c.getCellType()==Cell.CELL_TYPE_NUMERIC) {
            return row.getCell(colIndex).getNumericCellValue();
        }else {
            return Double.parseDouble(row.getCell(colIndex).getStringCellValue());
        }
    }

    public static void writeString(int colIndex, Row row, String value) {
        if(value!=null)
            row.createCell(colIndex).setCellValue(value);
    }

    public static CellStyle getCellStyle(Workbook workbook, IndexedColors color) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(color.getIndex());
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        return style;
    }

    public static String getDefaultUserMessages(List<ApiParameterError> ApiParameterErrorList){
        StringBuffer defaultUserMessages=new StringBuffer();
        for (ApiParameterError error:ApiParameterErrorList) {
            defaultUserMessages=defaultUserMessages.append(error.getDefaultUserMessage()+'\t');
        }
        return defaultUserMessages.toString();
    }
    public static String getErrorList(List<String> errorList){
        StringBuffer errors=new StringBuffer();
        for (String error: errorList) {
                errors=errors.append(error);
        }
        return errors.toString();
    }

    public static void writeErrorMessage(Sheet sheet,Integer rowIndex,String errorMessage,int statusColumn){
        Cell statusCell = sheet.getRow(rowIndex).createCell(statusColumn);
        statusCell.setCellValue(errorMessage);
        statusCell.setCellStyle(getCellStyle(sheet.getWorkbook(), IndexedColors.RED));
    }

    public static String getErrorMessage(RuntimeException re) {
        if (re instanceof AbstractPlatformDomainRuleException){
            AbstractPlatformDomainRuleException abstractPlatformDomainRuleException= (AbstractPlatformDomainRuleException) re;
            return abstractPlatformDomainRuleException.getDefaultUserMessage();
        }else if (re instanceof AbstractPlatformResourceNotFoundException){
            AbstractPlatformResourceNotFoundException abstractPlatformResourceNotFoundException= (AbstractPlatformResourceNotFoundException) re;
            return  abstractPlatformResourceNotFoundException.getDefaultUserMessage();
        }else if (re instanceof AbstractPlatformServiceUnavailableException) {
            AbstractPlatformServiceUnavailableException abstractPlatformServiceUnavailableException = (AbstractPlatformServiceUnavailableException) re;
            return abstractPlatformServiceUnavailableException.getDefaultUserMessage();
        }else if (re instanceof PlatformDataIntegrityException){
            PlatformDataIntegrityException platformDataIntegrityException= (PlatformDataIntegrityException) re;
            return platformDataIntegrityException.getDefaultUserMessage();
        }else if (re instanceof PlatformApiDataValidationException){
            PlatformApiDataValidationException platformApiDataValidationException=(PlatformApiDataValidationException) re;
            return getDefaultUserMessages(platformApiDataValidationException.getErrors());
        }else if (re instanceof UnsupportedParameterException ){
            UnsupportedParameterException unsupportedParameterException= (UnsupportedParameterException) re;
            return  getErrorList(unsupportedParameterException.getUnsupportedParameters());
        }else {
            if (re.getMessage()!=null) {
                return re.getMessage();
            }else {
                return re.getClass().getCanonicalName();
            }
        }
    }

    public static Long getIdByName (Sheet sheet, String name) {
        String sheetName = sheet.getSheetName();
        if(!sheetName.equals(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME)) {
            for (Row row : sheet) {
                for (Cell cell : row) {
                    if(name!=null) {
                        if (cell.getCellType() == Cell.CELL_TYPE_STRING && cell.getRichStringCellValue().getString().trim().equals(name)) {
                            if (sheetName.equals(TemplatePopulateImportConstants.OFFICE_SHEET_NAME) ||
                                    sheetName.equals(TemplatePopulateImportConstants.GL_ACCOUNTS_SHEET_NAME) ||
                                    sheetName.equals(TemplatePopulateImportConstants.EXTRAS_SHEET_NAME) ||
                                    sheetName.equals(TemplatePopulateImportConstants.SHARED_PRODUCTS_SHEET_NAME)||
                                    sheetName.equals(TemplatePopulateImportConstants.ROLES_SHEET_NAME)) {
                                if (row.getCell(cell.getColumnIndex() - 1).getCellType() == Cell.CELL_TYPE_NUMERIC)
                                    return ((Double) row.getCell(cell.getColumnIndex() - 1).getNumericCellValue()).longValue();
                                return 0L;
                            } else if (sheetName.equals(TemplatePopulateImportConstants.CLIENT_SHEET_NAME) ||
                                    sheetName.equals(TemplatePopulateImportConstants.CENTER_SHEET_NAME) ||
                                    sheetName.equals(TemplatePopulateImportConstants.GROUP_SHEET_NAME) ||
                                    sheetName.equals(TemplatePopulateImportConstants.STAFF_SHEET_NAME))
                                if (row.getCell(cell.getColumnIndex() + 1).getCellType() == Cell.CELL_TYPE_NUMERIC)
                                    return ((Double) row.getCell(cell.getColumnIndex() + 1).getNumericCellValue()).longValue();
                            return 0L;
                        }
                    }else {
                        return 0L;
                    }
                }
            }
        } else if (sheetName.equals(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME)) {
            for(Row row : sheet) {
                for(int i = 0; i < 2; i++) {
                    if (name != null) {
                        Cell cell = row.getCell(i);
                        if (cell.getCellType() == Cell.CELL_TYPE_STRING && cell.getRichStringCellValue().getString().trim().equals(name)) {
                            return ((Double) row.getCell(cell.getColumnIndex() - 1).getNumericCellValue()).longValue();
                        }
                    }else {
                        return 0L;
                    }
                }
            }
        }
        return 0L;
    }
    public static String getCodeByName(Sheet sheet, String name) {
        String sheetName = sheet.getSheetName();
        sheetName.equals(TemplatePopulateImportConstants.EXTRAS_SHEET_NAME);
        {
            for (Row row : sheet) {
                for (Cell cell : row) {
                    if (name!=null) {
                        if (cell.getCellType() == Cell.CELL_TYPE_STRING
                                && cell.getRichStringCellValue().getString().trim()
                                .equals(name)) {
                            return row.getCell(cell.getColumnIndex() - 1)
                                    .getStringCellValue().toString();

                        }
                    }
                }
            }
        }
        return "";
    }

    public static String getFrequencyId(String frequency) {
        if (frequency!=null) {
            if (frequency.equalsIgnoreCase(TemplatePopulateImportConstants.FREQUENCY_DAILY))
                frequency = "1";
            else if (frequency.equalsIgnoreCase(TemplatePopulateImportConstants.FREQUENCY_WEEKLY))
                frequency = "2";
            else if (frequency.equalsIgnoreCase(TemplatePopulateImportConstants.FREQUENCY_MONTHLY))
                frequency = "3";
            else if (frequency.equalsIgnoreCase(TemplatePopulateImportConstants.FREQUENCY_YEARLY))
                frequency = "4";
            return frequency;
        }else {
            return null;
        }
    }

    public static String getRepeatsOnDayId(String repeatsOnDay) {
        if (repeatsOnDay!=null) {
            if (repeatsOnDay.equalsIgnoreCase(TemplatePopulateImportConstants.MONDAY))
                repeatsOnDay = "1";
            else if (repeatsOnDay.equalsIgnoreCase(TemplatePopulateImportConstants.TUESDAY))
                repeatsOnDay = "2";
            else if (repeatsOnDay.equalsIgnoreCase(TemplatePopulateImportConstants.WEDNESDAY))
                repeatsOnDay = "3";
            else if (repeatsOnDay.equalsIgnoreCase(TemplatePopulateImportConstants.THURSDAY))
                repeatsOnDay = "4";
            else if (repeatsOnDay.equalsIgnoreCase(TemplatePopulateImportConstants.FRIDAY))
                repeatsOnDay = "5";

            else if (repeatsOnDay.equalsIgnoreCase(TemplatePopulateImportConstants.SATURDAY))
                repeatsOnDay = "6";
            else if (repeatsOnDay.equalsIgnoreCase(TemplatePopulateImportConstants.SUNDAY))
                repeatsOnDay = "7";
            return repeatsOnDay;
        }else {
            return null;
        }
    }
}