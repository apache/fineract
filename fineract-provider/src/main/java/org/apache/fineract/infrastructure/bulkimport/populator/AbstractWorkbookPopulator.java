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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.group.data.GroupGeneralData;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractWorkbookPopulator implements WorkbookPopulator {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractWorkbookPopulator.class);
    private static final Pattern NAME_REGEX = Pattern.compile("[ @#&()<>,;.:$£€§°\\\\/=!\\?\\-\\+\\*\"\\[\\]]");

    protected void writeInt(int colIndex, Row row, int value) {
        row.createCell(colIndex).setCellValue(value);
    }

    protected void writeLong(int colIndex, Row row, long value) {
        row.createCell(colIndex).setCellValue((double) value);
    }

    protected void writeString(int colIndex, Row row, String value) {
        row.createCell(colIndex).setCellValue(value);
    }

    protected void writeBoolean(int colIndex, Row row, Boolean value) {
        row.createCell(colIndex).setCellValue(value);
    }

    protected void writeDouble(int colIndex, Row row, double value) {
        row.createCell(colIndex).setCellValue(value);
    }

    protected void writeFormula(int colIndex, Row row, String formula) {
        row.createCell(colIndex).setCellFormula(formula);
    }

    protected void writeDate(int colIndex, Row row, String value, CellStyle dateCellStyle, String dateFormat) {
        try {
            DateTimeFormatter formatinDB;
            if (value.matches("\\d{4}-\\d{1,2}-\\d{1,2}")) {
                formatinDB = new DateTimeFormatterBuilder().appendPattern("yyyy-M-d").toFormatter();
            } else if (value.matches("\\d{1,2}/\\d{1,2}/\\d{4}")) {
                formatinDB = new DateTimeFormatterBuilder().appendPattern("d/M/yyyy").toFormatter();
            } else if (value.matches("\\d{1,2} \\w{3,12} \\d{4}")) {
                formatinDB = new DateTimeFormatterBuilder().appendPattern("d MMMM yyyy").toFormatter();
            } else {
                throw new IllegalArgumentException("Unrecognised format of date value: " + value);
            }
            LocalDate date1 = LocalDate.parse(value, formatinDB);
            DateTimeFormatter expectedFormat = new DateTimeFormatterBuilder().appendPattern(dateFormat).toFormatter();
            row.createCell(colIndex).setCellValue(expectedFormat.format(date1));
            row.getCell(colIndex).setCellStyle(dateCellStyle);
        } catch (DateTimeParseException pe) {
            throw new IllegalArgumentException(pe);
        }
    }

    protected void writeBigDecimal(int colIndex, Row row, BigDecimal value) {
        row.createCell(colIndex).setCellValue(((value != null) ? value.doubleValue() : 0));
    }

    protected void setOfficeDateLookupTable(Sheet sheet, List<OfficeData> offices, int officeNameCol, int activationDateCol,
            String dateFormat) {
        if (offices != null) {
            Workbook workbook = sheet.getWorkbook();
            CellStyle dateCellStyle = workbook.createCellStyle();
            short df = workbook.createDataFormat().getFormat(dateFormat);
            dateCellStyle.setDataFormat(df);
            int rowIndex = 0;
            for (OfficeData office : offices) {
                Row row = sheet.createRow(++rowIndex);
                writeString(officeNameCol, row, office.getName().trim().replaceAll("[ )(]", "_"));
                writeDate(activationDateCol, row, "" + office.getOpeningDate().getDayOfMonth() + "/"
                        + office.getOpeningDate().getMonthValue() + "/" + office.getOpeningDate().getYear(), dateCellStyle, dateFormat);

            }
        }
    }

    protected void setClientAndGroupDateLookupTable(Sheet sheet, List<ClientData> clients, List<GroupGeneralData> groups, int nameCol,
            int activationDateCol, boolean containsClientExtId, String dateFormat) {
        Workbook workbook = sheet.getWorkbook();
        CellStyle dateCellStyle = workbook.createCellStyle();
        short df = workbook.createDataFormat().getFormat(dateFormat);
        dateCellStyle.setDataFormat(df);
        int rowIndex = 0;
        DateTimeFormatter outputFormat = new DateTimeFormatterBuilder().appendPattern(dateFormat).toFormatter();
        try {
            if (clients != null) {
                for (ClientData client : clients) {
                    Row row = sheet.getRow(++rowIndex);
                    if (row == null) {
                        row = sheet.createRow(rowIndex);
                    }
                    writeString(nameCol, row, client.getDisplayName().replaceAll("[ )(] ", "_") + "(" + client.getId() + ")");

                    if (client.getActivationDate() != null) {
                        writeDate(activationDateCol, row, outputFormat.format(client.getActivationDate()), dateCellStyle, dateFormat);
                    }
                    if (containsClientExtId) {
                        if (!client.getExternalId().isEmpty()) {
                            writeString(nameCol + 1, row, client.getExternalId().getValue());
                        }
                    }

                }
            }
            if (groups != null) {
                for (GroupGeneralData group : groups) {
                    Row row = sheet.getRow(++rowIndex);
                    if (row == null) {
                        row = sheet.createRow(rowIndex);
                    }
                    writeString(nameCol, row, group.getName().replaceAll("[ )(] ", "_"));

                    if (group.getActivationDate() != null) {
                        writeDate(activationDateCol, row, outputFormat.format(group.getActivationDate()), dateCellStyle, dateFormat);
                    }

                }
            }
        } catch (DateTimeParseException e) {
            LOG.error("Problem occurred in setClientAndGroupDateLookupTable function", e);
        }
    }

    /**
     * See {@link Name#setNameName(String)} and https://issues.apache.org/jira/browse/FINERACT-1256.
     */
    protected void setSanitized(Name poiName, String roughName) {
        String sanitized = NAME_REGEX.matcher(roughName.trim()).replaceAll("_");
        poiName.setNameName(sanitized);
    }
}
