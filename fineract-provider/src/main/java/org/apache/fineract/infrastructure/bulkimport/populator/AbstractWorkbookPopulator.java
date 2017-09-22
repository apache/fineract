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
package org.apache.fineract.infrastructure.bulkimport.populator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public abstract class AbstractWorkbookPopulator implements WorkbookPopulator {

  protected void writeInt(int colIndex, Row row, int value) {
    row.createCell(colIndex).setCellValue(value);
  }

  protected void writeLong(int colIndex, Row row, long value) {
    row.createCell(colIndex).setCellValue(value);
  }

  protected void writeString(int colIndex, Row row, String value) {
    row.createCell(colIndex).setCellValue(value);
  }

  protected void writeDouble(int colIndex, Row row, double value) {
    row.createCell(colIndex).setCellValue(value);
  }

  protected void writeFormula(int colIndex, Row row, String formula) {
    row.createCell(colIndex).setCellType(Cell.CELL_TYPE_FORMULA);
    row.createCell(colIndex).setCellFormula(formula);
  }

  protected CellStyle getDateCellStyle(Workbook workbook) {
    CellStyle dateCellStyle = workbook.createCellStyle();
    short df = workbook.createDataFormat().getFormat("dd/mm/yy");
    dateCellStyle.setDataFormat(df);
    return dateCellStyle;
  }

  protected void writeDate(int colIndex, Row row, String value, CellStyle dateCellStyle) {
    try {
      // To make validation between functions inclusive.
      Date date = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).parse(value);
      Calendar cal = Calendar.getInstance();
      cal.setTime(date);
      cal.set(Calendar.HOUR_OF_DAY, 0);
      cal.set(Calendar.MINUTE, 0);
      cal.set(Calendar.SECOND, 0);
      cal.set(Calendar.MILLISECOND, 0);
      Date dateWithoutTime = cal.getTime();
      row.createCell(colIndex).setCellValue(dateWithoutTime);
      row.getCell(colIndex).setCellStyle(dateCellStyle);
    } catch (ParseException pe) {
      throw new IllegalArgumentException("ParseException");
    }
  }

  protected void setOfficeDateLookupTable(Sheet sheet, List<OfficeData> offices, int officeNameCol,
      int activationDateCol) {
    Workbook workbook = sheet.getWorkbook();
    CellStyle dateCellStyle = workbook.createCellStyle();
    short df = workbook.createDataFormat().getFormat("dd/mm/yy");
    dateCellStyle.setDataFormat(df);
    int rowIndex = 0;
    for (OfficeData office : offices) {
      Row row = sheet.createRow(++rowIndex);
      writeString(officeNameCol, row, office.name().trim().replaceAll("[ )(]", "_"));
      writeDate(activationDateCol, row,
          "" + office.getOpeningDate().getDayOfMonth() + "/"
              + office.getOpeningDate().getMonthOfYear() + "/" + office.getOpeningDate().getYear(),
          dateCellStyle);
    }
  }

}
