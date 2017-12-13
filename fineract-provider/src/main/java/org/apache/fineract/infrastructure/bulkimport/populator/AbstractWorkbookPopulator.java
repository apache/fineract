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

import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.group.data.GroupGeneralData;
import org.apache.fineract.portfolio.loanaccount.data.LoanAccountData;
import org.apache.poi.ss.usermodel.*;
import org.joda.time.LocalDate;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

  protected void writeBoolean(int colIndex,Row row,Boolean value){
      row.createCell(colIndex).setCellValue(value);
  }
  protected void writeDouble(int colIndex, Row row, double value) {
      row.createCell(colIndex).setCellValue(value);
  }

  protected void writeFormula(int colIndex, Row row, String formula) {
          row.createCell(colIndex).setCellType(Cell.CELL_TYPE_FORMULA);
          row.createCell(colIndex).setCellFormula(formula);
  }

  protected void writeDate(int colIndex, Row row, String value, CellStyle dateCellStyle,String dateFormat) {
    try {
        SimpleDateFormat formatinDB=null;
        if (value.matches("\\d{4}-\\d{1,2}-\\d{1,2}")){
           formatinDB=new SimpleDateFormat("yyyy-mm-dd");
        }else if (value.matches("\\d{1,2}/\\d{1,2}/\\d{4}")){
            formatinDB=new SimpleDateFormat("dd/mm/yyyy");
        }else if(value.matches("\\d{1,2} \\w{3,12} \\d{4}")){
            formatinDB=new SimpleDateFormat("dd MMMM yyyy");
        }
        Date date1=formatinDB.parse(value);
        SimpleDateFormat expectedFormat=new SimpleDateFormat(dateFormat);
        Date date2=expectedFormat.parse(expectedFormat.format(date1));
        Calendar cal = Calendar.getInstance();
        cal.setTime(date2);
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
  
  protected void writeBigDecimal(int colIndex, Row row, BigDecimal value) {
          row.createCell(colIndex).setCellValue(value.doubleValue());
	}
 
  protected void setOfficeDateLookupTable(Sheet sheet, List<OfficeData> offices, int officeNameCol,
      int activationDateCol,String dateFormat) {
      if(offices!=null){
        Workbook workbook = sheet.getWorkbook();
        CellStyle dateCellStyle = workbook.createCellStyle();
        short df = workbook.createDataFormat().getFormat(dateFormat);
        dateCellStyle.setDataFormat(df);
        int rowIndex = 0;
        for (OfficeData office : offices) {
            Row row = sheet.createRow(++rowIndex);
            writeString(officeNameCol, row, office.name().trim().replaceAll("[ )(]", "_"));
            writeDate(activationDateCol, row,
                    "" + office.getOpeningDate().getDayOfMonth() + "/"
                            + office.getOpeningDate().getMonthOfYear() + "/" + office.getOpeningDate().getYear(),
                    dateCellStyle,dateFormat);

            }
        }
  }

  protected void setClientAndGroupDateLookupTable(Sheet sheet, List<ClientData> clients,
            List<GroupGeneralData> groups, int nameCol, int activationDateCol,boolean containsClientExtId,
          String dateFormat) {
            Workbook workbook = sheet.getWorkbook();
            CellStyle dateCellStyle = workbook.createCellStyle();
            short df = workbook.createDataFormat().getFormat(dateFormat);
            dateCellStyle.setDataFormat(df);
            int rowIndex = 0;
            SimpleDateFormat outputFormat = new SimpleDateFormat(dateFormat);
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = null;
            try {
                if (clients != null){
                    for (ClientData client : clients) {
                        Row row = sheet.getRow(++rowIndex);
                        if (row == null)
                            row = sheet.createRow(rowIndex);
                        writeString(nameCol, row, client.displayName().replaceAll("[ )(] ", "_") + "(" + client.id() + ")");

                        if (client.getActivationDate() != null) {
                            date = inputFormat.parse(client.getActivationDate().toString());
                            writeDate(activationDateCol, row, outputFormat.format(date), dateCellStyle,dateFormat);
                        }
                        if (containsClientExtId){
                            if (client.getExternalId()!=null){
                                writeString(nameCol+1,row,client.getExternalId());
                            }
                        }

                    }
            }
            if (groups!=null){
                for (GroupGeneralData group : groups) {
                    Row row = sheet.getRow(++rowIndex);
                    if (row == null)
                        row = sheet.createRow(rowIndex);
                    writeString(nameCol, row, group.getName().replaceAll("[ )(] ", "_"));

                    date = inputFormat.parse(group.getActivationDate().toString());
                    writeDate(activationDateCol, row, outputFormat.format(date), dateCellStyle,dateFormat);
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
	}

}