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
import org.apache.fineract.portfolio.fund.data.FundData;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;

public class ExtrasSheetPopulator extends AbstractWorkbookPopulator {

	private List<FundData> funds;
	private List<PaymentTypeData> paymentTypes;
	private List<CurrencyData> currencies;

	private static final int FUND_ID_COL = 0;
	private static final int FUND_NAME_COL = 1;
	private static final int PAYMENT_TYPE_ID_COL = 2;
	private static final int PAYMENT_TYPE_NAME_COL = 3;
	private static final int CURRENCY_CODE_COL = 4;
	private static final int CURRENCY_NAME_COL = 5;

	
	public ExtrasSheetPopulator(List<FundData> funds, List<PaymentTypeData> paymentTypes,
			List<CurrencyData> currencies) {
		this.funds = funds;
		this.paymentTypes = paymentTypes;
		this.currencies = currencies;
	}

	@Override
	public void populate(Workbook workbook,String dateFormat) {
		int fundRowIndex = 1;
		Sheet extrasSheet = workbook.createSheet(TemplatePopulateImportConstants.EXTRAS_SHEET_NAME);
		setLayout(extrasSheet);
		for (FundData fund : funds) {
			Row row = extrasSheet.createRow(fundRowIndex++);
			writeLong(FUND_ID_COL, row, fund.getId());
			writeString(FUND_NAME_COL, row, fund.getName());
		}
		int paymentTypeRowIndex = 1;
		for (PaymentTypeData paymentType : paymentTypes) {
			Row row;
			if (paymentTypeRowIndex < fundRowIndex)
				row = extrasSheet.getRow(paymentTypeRowIndex++);
			else
				row = extrasSheet.createRow(paymentTypeRowIndex++);
			writeLong(PAYMENT_TYPE_ID_COL, row, paymentType.getId());
			writeString(PAYMENT_TYPE_NAME_COL, row, paymentType.getName().trim().replaceAll("[ )(]", "_"));
		}
		int currencyCodeRowIndex = 1;
		for (CurrencyData currencies : currencies) {
			Row row;
			if (currencyCodeRowIndex < paymentTypeRowIndex)
				row = extrasSheet.getRow(currencyCodeRowIndex++);
			else
				row = extrasSheet.createRow(currencyCodeRowIndex++);

			writeString(CURRENCY_NAME_COL, row, currencies.getName().trim().replaceAll("[ )(]", "_"));
			writeString(CURRENCY_CODE_COL, row, currencies.code());
		}
		extrasSheet.protectSheet("");

	}

	private void setLayout(Sheet worksheet) {
		worksheet.setColumnWidth(FUND_ID_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
		worksheet.setColumnWidth(FUND_NAME_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
		worksheet.setColumnWidth(PAYMENT_TYPE_ID_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
		worksheet.setColumnWidth(PAYMENT_TYPE_NAME_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
		worksheet.setColumnWidth(CURRENCY_NAME_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
		worksheet.setColumnWidth(CURRENCY_CODE_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
		Row rowHeader = worksheet.createRow(TemplatePopulateImportConstants.ROWHEADER_INDEX);
		rowHeader.setHeight(TemplatePopulateImportConstants.ROW_HEADER_HEIGHT);
		writeString(FUND_ID_COL, rowHeader, "Fund ID");
		writeString(FUND_NAME_COL, rowHeader, "Name");
		writeString(PAYMENT_TYPE_ID_COL, rowHeader, "Payment Type ID");
		writeString(PAYMENT_TYPE_NAME_COL, rowHeader, "Payment Type Name");
		writeString(CURRENCY_NAME_COL, rowHeader, "Currency Type ");
		writeString(CURRENCY_CODE_COL, rowHeader, "Currency Code ");
	}
	public Integer getFundsSize() {
		return funds.size();
	}
	public Integer getPaymentTypesSize() {
		return paymentTypes.size();
	}

	public Integer getCurrenciesSize() {
		return currencies.size();
	}

	
	
}