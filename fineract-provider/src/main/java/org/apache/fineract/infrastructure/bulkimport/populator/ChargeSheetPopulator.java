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

import java.util.List;
import org.apache.fineract.infrastructure.bulkimport.constants.ChargeConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class ChargeSheetPopulator extends AbstractWorkbookPopulator {

    private List<ChargeData> charges;

    private static final int ID_COL = 0;

    public ChargeSheetPopulator(final List<ChargeData> charges) {
        this.charges = charges;
    }

    @Override
    public void populate(final Workbook workbook, String dateFormat) {
        int rowIndex = 1;
        Sheet chargeSheet = workbook.createSheet(TemplatePopulateImportConstants.CHARGE_SHEET_NAME);
        setLayout(chargeSheet);

        populateCharges(chargeSheet, rowIndex);
        chargeSheet.protectSheet("");
    }

    private void populateCharges(Sheet chargeSheet, int rowIndex) {
        for (ChargeData charge : charges) {
            Row row = chargeSheet.createRow(rowIndex);
            writeLong(ID_COL, row, charge.getId());
            writeString(ChargeConstants.CHARGE_NAME_COL, row, charge.getName().trim().replaceAll("[ )(]", "_"));
            writeBigDecimal(ChargeConstants.CHARGE_AMOUNT_COL, row, charge.getAmount());
            writeString(ChargeConstants.CHARGE_CALCULATION_TYPE_COL, row, charge.getChargeCalculationType().getValue());
            writeString(ChargeConstants.CHARGE_TIME_TYPE_COL, row, charge.getChargeTimeType().getValue());
            rowIndex++;
        }
    }

    private void setLayout(Sheet worksheet) {
        worksheet.setColumnWidth(ID_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ChargeConstants.CHARGE_NAME_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(ChargeConstants.CHARGE_AMOUNT_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ChargeConstants.CHARGE_CALCULATION_TYPE_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(ChargeConstants.CHARGE_TIME_TYPE_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);

        Row rowHeader = worksheet.createRow(TemplatePopulateImportConstants.ROWHEADER_INDEX);
        rowHeader.setHeight(TemplatePopulateImportConstants.ROW_HEADER_HEIGHT);

        writeString(ID_COL, rowHeader, "ID");
        writeString(ChargeConstants.CHARGE_NAME_COL, rowHeader, "Name");
        writeString(ChargeConstants.CHARGE_AMOUNT_COL, rowHeader, "Charge Amount");
        writeString(ChargeConstants.CHARGE_CALCULATION_TYPE_COL, rowHeader, "Charge Calculation Type");
        writeString(ChargeConstants.CHARGE_TIME_TYPE_COL, rowHeader, "Charge Time Type");
    }

    public Integer getChargesSize() {
        return charges.size();
    }

    public List<ChargeData> getCharges() {
        return charges;
    }

}
