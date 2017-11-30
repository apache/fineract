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
import org.apache.fineract.infrastructure.bulkimport.constants.OfficeConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.poi.ss.usermodel.*;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class ImportHandlerUtils  {

    public static Integer getNumberOfRows(Sheet sheet, int primaryColumn) {
        Integer noOfEntries = 0;
        // getLastRowNum and getPhysicalNumberOfRows showing false values
        // sometimes
        while (sheet.getRow(noOfEntries+1) !=null && sheet.getRow(noOfEntries+1).getCell(primaryColumn) != null) {
            noOfEntries++;
        }

        return noOfEntries;
    }

    public static Boolean isNotImported(Row row, int statusColumn) {
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

}