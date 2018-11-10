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
import org.apache.fineract.portfolio.savings.data.RecurringDepositProductData;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;

public class RecurringDepositProductSheetPopulator extends AbstractWorkbookPopulator {

    private List<RecurringDepositProductData> products;

    private static final int ID_COL = 0;
    private static final int NAME_COL = 1;
    private static final int SHORT_NAME_COL = 2;
    private static final int NOMINAL_ANNUAL_INTEREST_RATE_COL = 3;
    private static final int INTEREST_COMPOUNDING_PERIOD_COL = 4;
    private static final int INTEREST_POSTING_PERIOD_COL = 5;
    private static final int INTEREST_CALCULATION_COL = 6;
    private static final int INTEREST_CALCULATION_DAYS_IN_YEAR_COL = 7;
    private static final int LOCKIN_PERIOD_COL = 8;
    private static final int LOCKIN_PERIOD_FREQUENCY_COL = 9;
    private static final int CURRENCY_COL = 10;
    private static final int MIN_DEPOSIT_COL = 11;
    private static final int MAX_DEPOSIT_COL = 12;
    private static final int DEPOSIT_COL = 13;
    private static final int MIN_DEPOSIT_TERM_COL = 14;
    private static final int MIN_DEPOSIT_TERM_TYPE_COL = 15;
    private static final int MAX_DEPOSIT_TERM_COL = 16;
    private static final int MAX_DEPOSIT_TERM_TYPE_COL = 17;
    private static final int PRECLOSURE_PENAL_APPLICABLE_COL = 18;
    private static final int PRECLOSURE_PENAL_INTEREST_COL = 19;
    private static final int PRECLOSURE_INTEREST_TYPE_COL = 20;
    private static final int IN_MULTIPLES_OF_DEPOSIT_TERM_COL = 21;
    private static final int IN_MULTIPLES_OF_DEPOSIT_TERM_TYPE_COL = 22;
    private static final int IS_MANDATORY_DEPOSIT_COL = 23;
    private static final int ALLOW_WITHDRAWAL_COL = 24;
    private static final int ADJUST_ADVANCE_COL = 25;

    public RecurringDepositProductSheetPopulator(List<RecurringDepositProductData> recurringDepositProducts) {
        this.products=recurringDepositProducts;
    }

    @Override
    public void populate(Workbook workbook,String dateFormat) {
            int rowIndex = 1;
            Sheet productSheet = workbook.createSheet(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME);
            setLayout(productSheet);
            CellStyle dateCellStyle = workbook.createCellStyle();
            short df = workbook.createDataFormat().getFormat(dateFormat);
            dateCellStyle.setDataFormat(df);
            for(RecurringDepositProductData product : products) {
                Row row = productSheet.createRow(rowIndex++);
                writeLong(ID_COL, row, product.getId());
                writeString(NAME_COL, row, product.getName().trim().replaceAll("[ )(]", "_"));
                writeString(SHORT_NAME_COL, row, product.getShortName().trim().replaceAll("[ )(]", "_"));
                writeBigDecimal(NOMINAL_ANNUAL_INTEREST_RATE_COL, row, product.getNominalAnnualInterestRate());
                writeString(INTEREST_COMPOUNDING_PERIOD_COL, row, product.getInterestCompoundingPeriodType().getValue());
                writeString(INTEREST_POSTING_PERIOD_COL, row, product.getInterestPostingPeriodType().getValue());
                writeString(INTEREST_CALCULATION_COL, row, product.getInterestCalculationType().getValue());
                writeString(INTEREST_CALCULATION_DAYS_IN_YEAR_COL, row, product.getInterestCalculationDaysInYearType().getValue());
                writeBoolean(PRECLOSURE_PENAL_APPLICABLE_COL, row, product.isPreClosurePenalApplicable());
                writeString(MIN_DEPOSIT_TERM_TYPE_COL, row, product.getMinDepositTermType().getValue());

                if(product.getMinDepositAmount() != null)
                    writeBigDecimal(MIN_DEPOSIT_COL, row, product.getMinDepositAmount());
                if(product.getMaxDepositAmount() != null)
                    writeBigDecimal(MAX_DEPOSIT_COL, row, product.getMaxDepositAmount());
                if(product.getDepositAmount() != null)
                    writeBigDecimal(DEPOSIT_COL, row, product.getDepositAmount());
                if(product.getMaxDepositTerm() != null)
                    writeInt(MAX_DEPOSIT_TERM_COL, row, product.getMaxDepositTerm());

                if(product.getMinDepositTerm() != null)
                    writeInt(MIN_DEPOSIT_TERM_COL, row, product.getMinDepositTerm());


                if(product.getInMultiplesOfDepositTerm() != null)
                    writeInt(IN_MULTIPLES_OF_DEPOSIT_TERM_COL, row, product.getInMultiplesOfDepositTerm());
                if(product.getPreClosurePenalInterest() != null)
                    writeBigDecimal(PRECLOSURE_PENAL_INTEREST_COL, row, product.getPreClosurePenalInterest());
                if(product.getMaxDepositTermType() != null)
                    writeString(MAX_DEPOSIT_TERM_TYPE_COL, row, product.getMaxDepositTermType().getValue());
                if(product.getPreClosurePenalInterestOnType() != null)
                    writeString(PRECLOSURE_INTEREST_TYPE_COL, row, product.getPreClosurePenalInterestOnType().getValue());
                if(product.getInMultiplesOfDepositTermType() != null)
                    writeString(IN_MULTIPLES_OF_DEPOSIT_TERM_TYPE_COL, row, product.getInMultiplesOfDepositTermType().getValue());
                    writeBoolean(ALLOW_WITHDRAWAL_COL, row, product.isAllowWithdrawal());
                    writeBoolean(ADJUST_ADVANCE_COL, row, product.isAdjustAdvanceTowardsFuturePayments());
                    writeBoolean(IS_MANDATORY_DEPOSIT_COL, row, product.isMandatoryDeposit());
                if(product.getLockinPeriodFrequency() != null)
                    writeInt(LOCKIN_PERIOD_COL, row, product.getLockinPeriodFrequency());
                if(product.getLockinPeriodFrequencyType() != null)
                    writeString(LOCKIN_PERIOD_FREQUENCY_COL, row, product.getLockinPeriodFrequencyType().getValue());
                CurrencyData currency = product.getCurrency();
                writeString(CURRENCY_COL, row, currency.code());
            }
            productSheet.protectSheet("");
    }

    private void setLayout(Sheet worksheet) {
        Row rowHeader = worksheet.createRow(TemplatePopulateImportConstants.ROWHEADER_INDEX);
        rowHeader.setHeight(TemplatePopulateImportConstants.ROW_HEADER_HEIGHT);
        worksheet.setColumnWidth(ID_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(NAME_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(SHORT_NAME_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(NOMINAL_ANNUAL_INTEREST_RATE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(INTEREST_COMPOUNDING_PERIOD_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(INTEREST_POSTING_PERIOD_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(INTEREST_CALCULATION_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(INTEREST_CALCULATION_DAYS_IN_YEAR_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(LOCKIN_PERIOD_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(LOCKIN_PERIOD_FREQUENCY_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(CURRENCY_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(MIN_DEPOSIT_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(MAX_DEPOSIT_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(DEPOSIT_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(MIN_DEPOSIT_TERM_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(MAX_DEPOSIT_TERM_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(MIN_DEPOSIT_TERM_TYPE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(MAX_DEPOSIT_TERM_TYPE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(PRECLOSURE_PENAL_APPLICABLE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(PRECLOSURE_PENAL_INTEREST_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(PRECLOSURE_INTEREST_TYPE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(IN_MULTIPLES_OF_DEPOSIT_TERM_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(IN_MULTIPLES_OF_DEPOSIT_TERM_TYPE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(IS_MANDATORY_DEPOSIT_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ALLOW_WITHDRAWAL_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ADJUST_ADVANCE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);

        writeString(ID_COL, rowHeader, "ID");
        writeString(NAME_COL, rowHeader, "Name");
        writeString(SHORT_NAME_COL, rowHeader, "Short Name");
        writeString(NOMINAL_ANNUAL_INTEREST_RATE_COL, rowHeader, "Interest");
        writeString(INTEREST_COMPOUNDING_PERIOD_COL, rowHeader, "Interest Compounding Period");
        writeString(INTEREST_POSTING_PERIOD_COL, rowHeader, "Interest Posting Period");
        writeString(INTEREST_CALCULATION_COL, rowHeader, "Interest Calculated Using");
        writeString(INTEREST_CALCULATION_DAYS_IN_YEAR_COL, rowHeader, "# Days In Year");
        writeString(LOCKIN_PERIOD_COL, rowHeader, "Locked In For");
        writeString(LOCKIN_PERIOD_FREQUENCY_COL, rowHeader, "Frequency");
        writeString(CURRENCY_COL, rowHeader, "Currency");
        writeString(MIN_DEPOSIT_COL, rowHeader, "Min Deposit");
        writeString(MAX_DEPOSIT_COL, rowHeader, "Max Deposit");
        writeString(DEPOSIT_COL, rowHeader, "Deposit");
        writeString(MIN_DEPOSIT_TERM_COL, rowHeader, "Min Deposit Term");
        writeString(MAX_DEPOSIT_TERM_COL, rowHeader, "Max Deposit Term");
        writeString(MIN_DEPOSIT_TERM_TYPE_COL, rowHeader, "Min Deposit Term Type");
        writeString(MAX_DEPOSIT_TERM_TYPE_COL, rowHeader, "Max Deposit Term Type");
        writeString(PRECLOSURE_PENAL_APPLICABLE_COL, rowHeader, "Preclosure Penal Applicable");
        writeString(PRECLOSURE_PENAL_INTEREST_COL, rowHeader, "Penal Interest");
        writeString(PRECLOSURE_INTEREST_TYPE_COL, rowHeader, "Penal Interest Type");
        writeString(IN_MULTIPLES_OF_DEPOSIT_TERM_COL, rowHeader, "Multiples of Deposit Term");
        writeString(IN_MULTIPLES_OF_DEPOSIT_TERM_TYPE_COL, rowHeader, "Multiples of Deposit Term Type");
        writeString(IS_MANDATORY_DEPOSIT_COL, rowHeader, "Is Mandatory Deposit?");
        writeString(ALLOW_WITHDRAWAL_COL, rowHeader, "Allow Withdrawal?");
        writeString(ADJUST_ADVANCE_COL, rowHeader, "Adjust Advance Towards Future Payments?");
    }

    public List<RecurringDepositProductData> getProducts() {
        return products;
    }

    public Integer getProductsSize() {
        return products.size();
    }

}
