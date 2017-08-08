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

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public abstract class AbstractWorkbookPopulator implements WorkbookPopulator {

  protected void writeString(int colIndex, Row row, String value) {
    row.createCell(colIndex).setCellValue(value);
  }

  protected void writeFormula(int colIndex, Row row, String formula) {
    row.createCell(colIndex).setCellType(Cell.CELL_TYPE_FORMULA);
    row.createCell(colIndex).setCellFormula(formula);
  }

  protected void writeDate(int colIndex, Row row, String value, CellStyle dateCellStyle) {
    try {
    	//yyyy-MM-dd
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
  
  protected void writeBigDecimal(int colIndex, Row row, BigDecimal value) {
		row.createCell(colIndex).setCellValue(value.doubleValue());
		;
	}

}