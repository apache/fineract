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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

public abstract class AbstractWorkbookPopulator implements WorkbookPopulator {

  protected void writeLong(int colIndex, Row row, long value) {
    row.createCell(colIndex).setCellValue(value);
  }

  protected void writeString(int colIndex, Row row, String value) {
    row.createCell(colIndex).setCellValue(value);
  }

  protected void writeFormula(int colIndex, Row row, String formula) {
    row.createCell(colIndex).setCellType(Cell.CELL_TYPE_FORMULA);
    row.createCell(colIndex).setCellFormula(formula);
  }



}