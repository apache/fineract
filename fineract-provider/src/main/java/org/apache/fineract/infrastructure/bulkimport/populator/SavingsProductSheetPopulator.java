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

import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.savings.data.SavingsProductData;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;

public class SavingsProductSheetPopulator extends AbstractWorkbookPopulator {
    private List<SavingsProductData> savingsProducts;

    private static final int ID_COL = 0;
    private static final int NAME_COL = 1;
    private static final int NOMINAL_ANNUAL_INTEREST_RATE_COL = 2;
    private static final int INTEREST_COMPOUNDING_PERIOD_COL = 3;
    private static final int INTEREST_POSTING_PERIOD_COL = 4;
    private static final int INTEREST_CALCULATION_COL = 5;
    private static final int INTEREST_CALCULATION_DAYS_IN_YEAR_COL = 6;
    private static final int MIN_OPENING_BALANCE_COL = 7;
    private static final int LOCKIN_PERIOD_COL = 8;
    private static final int LOCKIN_PERIOD_FREQUENCY_COL = 9;
    private static final int CURRENCY_COL = 10;
    private static final int DECIMAL_PLACES_COL = 11;
    private static final int IN_MULTIPLES_OF_COL = 12;
    private static final int WITHDRAWAL_FEE_COL = 13;
    private static final int ALLOW_OVERDRAFT_COL = 14;
    private static final int OVERDRAFT_LIMIT_COL = 15;

    public SavingsProductSheetPopulator(List<SavingsProductData> savingsProducts) {
    this.savingsProducts=savingsProducts;
    }

    @Override
    public void populate(Workbook workbook,String dateFormat) {
        int rowIndex = 1;
        Sheet productSheet = workbook.createSheet(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME);
        setLayout(productSheet);
        CellStyle dateCellStyle = workbook.createCellStyle();
        short df = workbook.createDataFormat().getFormat(dateFormat);
        dateCellStyle.setDataFormat(df);
        for(SavingsProductData product : savingsProducts) {
            Row row = productSheet.createRow(rowIndex++);
            writeLong(ID_COL, row, product.getId());
            writeString(NAME_COL, row, product.getName().trim().replaceAll("[ )(]", "_"));
            writeBigDecimal(NOMINAL_ANNUAL_INTEREST_RATE_COL, row, product.getNominalAnnualInterestRate());
            writeString(INTEREST_COMPOUNDING_PERIOD_COL, row, product.getInterestCompoundingPeriodType().getValue());
            writeString(INTEREST_POSTING_PERIOD_COL, row, product.getInterestPostingPeriodType().getValue());
            writeString(INTEREST_CALCULATION_COL, row, product.getInterestCalculationType().getValue());
            writeString(INTEREST_CALCULATION_DAYS_IN_YEAR_COL, row, product.getInterestCalculationDaysInYearType().getValue());
            if(product.getMinRequiredOpeningBalance() != null)
                writeBigDecimal(MIN_OPENING_BALANCE_COL, row, product.getMinRequiredOpeningBalance());
            if(product.getLockinPeriodFrequency() != null)
                writeInt(LOCKIN_PERIOD_COL, row, product.getLockinPeriodFrequency());
            if(product.getLockinPeriodFrequencyType() != null)
                writeString(LOCKIN_PERIOD_FREQUENCY_COL, row, product.getLockinPeriodFrequencyType().getValue());
            CurrencyData currency = product.getCurrency();
            writeString(CURRENCY_COL, row, currency.code());
            writeInt(DECIMAL_PLACES_COL, row, currency.decimalPlaces());
            if(currency.currencyInMultiplesOf() != null)
                writeInt(IN_MULTIPLES_OF_COL, row, currency.currencyInMultiplesOf());
            writeBoolean(WITHDRAWAL_FEE_COL, row, product.isWithdrawalFeeForTransfers());
            writeBoolean(ALLOW_OVERDRAFT_COL, row,product.isAllowOverdraft());
            if(product.getOverdraftLimit() != null)
                writeBigDecimal(OVERDRAFT_LIMIT_COL, row, product.getOverdraftLimit());
        }
        productSheet.protectSheet("");
    }

    private void setLayout(Sheet worksheet) {
        Row rowHeader = worksheet.createRow(TemplatePopulateImportConstants.ROWHEADER_INDEX);
        rowHeader.setHeight(TemplatePopulateImportConstants.ROW_HEADER_HEIGHT);
        worksheet.setColumnWidth(ID_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(NAME_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(NOMINAL_ANNUAL_INTEREST_RATE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(INTEREST_COMPOUNDING_PERIOD_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(INTEREST_POSTING_PERIOD_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(INTEREST_CALCULATION_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(INTEREST_CALCULATION_DAYS_IN_YEAR_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(MIN_OPENING_BALANCE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(LOCKIN_PERIOD_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(LOCKIN_PERIOD_FREQUENCY_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(CURRENCY_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(DECIMAL_PLACES_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(IN_MULTIPLES_OF_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(WITHDRAWAL_FEE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ALLOW_OVERDRAFT_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(OVERDRAFT_LIMIT_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);

        writeString(ID_COL, rowHeader, "ID");
        writeString(NAME_COL, rowHeader, "Name");
        writeString(NOMINAL_ANNUAL_INTEREST_RATE_COL, rowHeader, "Interest");
        writeString(INTEREST_COMPOUNDING_PERIOD_COL, rowHeader, "Interest Compounding Period");
        writeString(INTEREST_POSTING_PERIOD_COL, rowHeader, "Interest Posting Period");
        writeString(INTEREST_CALCULATION_COL, rowHeader, "Interest Calculated Using");
        writeString(INTEREST_CALCULATION_DAYS_IN_YEAR_COL, rowHeader, "# Days In Year");
        writeString(MIN_OPENING_BALANCE_COL, rowHeader, "Min Opening Balance");
        writeString(LOCKIN_PERIOD_COL, rowHeader, "Locked In For");
        writeString(LOCKIN_PERIOD_FREQUENCY_COL, rowHeader, "Frequency");
        writeString(CURRENCY_COL, rowHeader, "Currency");
        writeString(DECIMAL_PLACES_COL, rowHeader, "Decimal Places");
        writeString(IN_MULTIPLES_OF_COL, rowHeader, "In Multiples Of");
        writeString(WITHDRAWAL_FEE_COL, rowHeader, "Withdrawal Fee for Transfers?");
        writeString(ALLOW_OVERDRAFT_COL, rowHeader, "Apply Overdraft");
        writeString(OVERDRAFT_LIMIT_COL, rowHeader, "Overdraft Limit");
    }

    public List<SavingsProductData> getProducts() {
        return savingsProducts;
    }

    public Integer getProductsSize() {
        return savingsProducts.size();
    }
}
