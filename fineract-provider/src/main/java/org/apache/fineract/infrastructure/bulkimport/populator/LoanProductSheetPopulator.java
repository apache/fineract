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
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;


public class LoanProductSheetPopulator extends AbstractWorkbookPopulator {

	private List<LoanProductData> products;
	
	private static final int ID_COL = 0;
	private static final int NAME_COL = 1;
	private static final int FUND_NAME_COL = 2;
	private static final int PRINCIPAL_COL = 3;
	private static final int MIN_PRINCIPAL_COL = 4;
	private static final int MAX_PRINCIPAL_COL = 5;
	private static final int NO_OF_REPAYMENTS_COL = 6;
	private static final int MIN_REPAYMENTS_COL = 7;
	private static final int MAX_REPAYMENTS_COL = 8;
	private static final int REPAYMENT_EVERY_COL = 9;
	private static final int REPAYMENT_FREQUENCY_COL = 10;
	private static final int INTEREST_RATE_COL = 11;
	private static final int MIN_INTEREST_RATE_COL = 12;
	private static final int MAX_INTEREST_RATE_COL = 13;
	private static final int INTEREST_RATE_FREQUENCY_COL = 14;
	private static final int AMORTIZATION_TYPE_COL = 15;
	private static final int INTEREST_TYPE_COL = 16;
	private static final int INTEREST_CALCULATION_PERIOD_TYPE_COL = 17;
	private static final int IN_ARREARS_TOLERANCE_COL = 18;
	private static final int TRANSACTION_PROCESSING_STRATEGY_NAME_COL = 19;
	private static final int GRACE_ON_PRINCIPAL_PAYMENT_COL = 20;
	private static final int GRACE_ON_INTEREST_PAYMENT_COL = 21;
	private static final int GRACE_ON_INTEREST_CHARGED_COL = 22;
	private static final int START_DATE_COL = 23;
	private static final int CLOSE_DATE_COL = 24;

	
	
	public LoanProductSheetPopulator(List<LoanProductData> products) {
		this.products = products;
	}

	@Override
	public void populate(Workbook workbook,String dateFormat) {
		int rowIndex = 1;
		Sheet productSheet = workbook.createSheet(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME);
		setLayout(productSheet);
		CellStyle dateCellStyle = workbook.createCellStyle();
		short df = workbook.createDataFormat().getFormat(dateFormat);
		dateCellStyle.setDataFormat(df);
		for (LoanProductData product : products) {
			Row row = productSheet.createRow(rowIndex++);
			writeLong(ID_COL, row, product.getId());
			writeString(NAME_COL, row, product.getName().trim().replaceAll("[ )(]", "_"));
			if (product.getFundName() != null)
				writeString(FUND_NAME_COL, row, product.getFundName());
			writeBigDecimal(PRINCIPAL_COL, row, product.getPrincipal());
			if (product.getMinPrincipal() != null)
				writeBigDecimal(MIN_PRINCIPAL_COL, row, product.getMinPrincipal());
			else
				writeInt(MIN_PRINCIPAL_COL, row, 1);
			if (product.getMaxPrincipal() != null)
				writeBigDecimal(MAX_PRINCIPAL_COL, row, product.getMaxPrincipal());
			else
				writeInt(MAX_PRINCIPAL_COL, row, 999999999);
			writeInt(NO_OF_REPAYMENTS_COL, row, product.getNumberOfRepayments());
			if (product.getMinNumberOfRepayments() != null)
				writeInt(MIN_REPAYMENTS_COL, row, product.getMinNumberOfRepayments());
			else
				writeInt(MIN_REPAYMENTS_COL, row, 1);
			if (product.getMaxNumberOfRepayments() != null)
				writeInt(MAX_REPAYMENTS_COL, row, product.getMaxNumberOfRepayments());
			else
				writeInt(MAX_REPAYMENTS_COL, row, 999999999);
			writeInt(REPAYMENT_EVERY_COL, row, product.getRepaymentEvery());
			writeString(REPAYMENT_FREQUENCY_COL, row, product.getRepaymentFrequencyType().getValue());
			writeBigDecimal(INTEREST_RATE_COL, row, product.getInterestRatePerPeriod());
			if (product.getMinInterestRatePerPeriod() != null)
				writeBigDecimal(MIN_INTEREST_RATE_COL, row, product.getMinInterestRatePerPeriod());
			else
				writeInt(MIN_INTEREST_RATE_COL, row, 1);
			if (product.getMaxInterestRatePerPeriod() != null)
				writeBigDecimal(MAX_INTEREST_RATE_COL, row, product.getMaxInterestRatePerPeriod());
			else
				writeInt(MAX_INTEREST_RATE_COL, row, 999999999);
			writeString(INTEREST_RATE_FREQUENCY_COL, row, product.getInterestRateFrequencyType().getValue());
			writeString(AMORTIZATION_TYPE_COL, row, product.getAmortizationType().getValue());
			writeString(INTEREST_TYPE_COL, row, product.getInterestType().getValue());
			writeString(INTEREST_CALCULATION_PERIOD_TYPE_COL, row,
					product.getInterestCalculationPeriodType().getValue());
			if (product.getInArrearsTolerance() != null)
				writeBigDecimal(IN_ARREARS_TOLERANCE_COL, row, product.getInArrearsTolerance());
			writeString(TRANSACTION_PROCESSING_STRATEGY_NAME_COL, row, product.getTransactionProcessingStrategyName());
			if (product.getGraceOnPrincipalPayment() != null)
				writeInt(GRACE_ON_PRINCIPAL_PAYMENT_COL, row, product.getGraceOnPrincipalPayment());
			if (product.getGraceOnInterestPayment() != null)
				writeInt(GRACE_ON_INTEREST_PAYMENT_COL, row, product.getGraceOnInterestPayment());
			if (product.getGraceOnInterestCharged() != null)
				writeInt(GRACE_ON_INTEREST_CHARGED_COL, row, product.getGraceOnInterestCharged());
			if (product.getStartDate() != null)
				writeDate(START_DATE_COL, row, product.getStartDate().toString(), dateCellStyle,dateFormat);
			else
				writeDate(START_DATE_COL, row, "1/1/1970", dateCellStyle,dateFormat);
			if (product.getCloseDate() != null)
				writeDate(CLOSE_DATE_COL, row, product.getCloseDate().toString(), dateCellStyle,dateFormat);
			else
				writeDate(CLOSE_DATE_COL, row, "1/1/2040", dateCellStyle,dateFormat);
			productSheet.protectSheet("");
		}

	}

	private void setLayout(Sheet worksheet) {
		worksheet.setColumnWidth(ID_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(NAME_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(FUND_NAME_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(PRINCIPAL_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(MIN_PRINCIPAL_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(MAX_PRINCIPAL_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(NO_OF_REPAYMENTS_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(MIN_REPAYMENTS_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(MAX_REPAYMENTS_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(REPAYMENT_EVERY_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(REPAYMENT_FREQUENCY_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(INTEREST_RATE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(MIN_INTEREST_RATE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(MAX_INTEREST_RATE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(INTEREST_RATE_FREQUENCY_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(AMORTIZATION_TYPE_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
		worksheet.setColumnWidth(INTEREST_TYPE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(INTEREST_CALCULATION_PERIOD_TYPE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(IN_ARREARS_TOLERANCE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(TRANSACTION_PROCESSING_STRATEGY_NAME_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(GRACE_ON_PRINCIPAL_PAYMENT_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(GRACE_ON_INTEREST_PAYMENT_COL,  TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
		worksheet.setColumnWidth(GRACE_ON_INTEREST_CHARGED_COL,  TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
		worksheet.setColumnWidth(START_DATE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(CLOSE_DATE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);

		Row rowHeader = worksheet.createRow(TemplatePopulateImportConstants.ROWHEADER_INDEX);
		rowHeader.setHeight(TemplatePopulateImportConstants.ROW_HEADER_HEIGHT);
		writeString(ID_COL, rowHeader, "ID");
		writeString(NAME_COL, rowHeader, "Name");
		writeString(FUND_NAME_COL, rowHeader, "Fund");
		writeString(PRINCIPAL_COL, rowHeader, "Principal");
		writeString(MIN_PRINCIPAL_COL, rowHeader, "Min Principal");
		writeString(MAX_PRINCIPAL_COL, rowHeader, "Max Principal");
		writeString(NO_OF_REPAYMENTS_COL, rowHeader, "# of Repayments");
		writeString(MIN_REPAYMENTS_COL, rowHeader, "Min Repayments");
		writeString(MAX_REPAYMENTS_COL, rowHeader, "Max Repayments");
		writeString(REPAYMENT_EVERY_COL, rowHeader, "Repayment Every");
		writeString(REPAYMENT_FREQUENCY_COL, rowHeader, "Frequency");
		writeString(INTEREST_RATE_COL, rowHeader, "Interest");
		writeString(MIN_INTEREST_RATE_COL, rowHeader, "Min Interest");
		writeString(MAX_INTEREST_RATE_COL, rowHeader, "Max Interest");
		writeString(INTEREST_RATE_FREQUENCY_COL, rowHeader, "Frequency");
		writeString(AMORTIZATION_TYPE_COL, rowHeader, "Amortization Type");
		writeString(INTEREST_TYPE_COL, rowHeader, "Interest Type");
		writeString(INTEREST_CALCULATION_PERIOD_TYPE_COL, rowHeader, "Interest Calculation Period");
		writeString(IN_ARREARS_TOLERANCE_COL, rowHeader, "In Arrears Tolerance");
		writeString(TRANSACTION_PROCESSING_STRATEGY_NAME_COL, rowHeader, "Transaction Processing Strategy");
		writeString(GRACE_ON_PRINCIPAL_PAYMENT_COL, rowHeader, "Grace On Principal Payment");
		writeString(GRACE_ON_INTEREST_PAYMENT_COL, rowHeader, "Grace on Interest Payment");
		writeString(GRACE_ON_INTEREST_CHARGED_COL, rowHeader, "Grace on Interest Charged");
		writeString(START_DATE_COL, rowHeader, "Start Date");
		writeString(CLOSE_DATE_COL, rowHeader, "End Date");
	}
	public List<LoanProductData> getProducts(){
		return products;
		
	}
	public Integer getProductsSize() {
		return products.size();
	}
}
