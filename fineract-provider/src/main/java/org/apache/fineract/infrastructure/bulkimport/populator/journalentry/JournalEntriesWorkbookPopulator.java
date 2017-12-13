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
package org.apache.fineract.infrastructure.bulkimport.populator.journalentry;

import org.apache.fineract.infrastructure.bulkimport.constants.JournalEntryConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.populator.AbstractWorkbookPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.ExtrasSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.GlAccountSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.OfficeSheetPopulator;
import org.apache.poi.hssf.usermodel.HSSFDataValidationHelper;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;

import java.util.ArrayList;


public class JournalEntriesWorkbookPopulator extends AbstractWorkbookPopulator {
	
	private OfficeSheetPopulator officeSheetPopulator;
	private GlAccountSheetPopulator glAccountSheetPopulator;
	private ExtrasSheetPopulator extrasSheetPopulator;

	public JournalEntriesWorkbookPopulator(OfficeSheetPopulator officeSheetPopulator,
			GlAccountSheetPopulator glAccountSheetPopulator, ExtrasSheetPopulator extrasSheetPopulator) {
		this.officeSheetPopulator = officeSheetPopulator;
		this.glAccountSheetPopulator = glAccountSheetPopulator;
		this.extrasSheetPopulator = extrasSheetPopulator;
	}

	@Override
	public void populate(Workbook workbook,String dateFormat) {
		Sheet addJournalEntriesSheet = workbook.createSheet(TemplatePopulateImportConstants.JOURNAL_ENTRY_SHEET_NAME);
		officeSheetPopulator.populate(workbook,dateFormat);
		glAccountSheetPopulator.populate(workbook,dateFormat);
		extrasSheetPopulator.populate(workbook,dateFormat);
		setRules(addJournalEntriesSheet);
		setDefaults(addJournalEntriesSheet);
		setLayout(addJournalEntriesSheet);
	}
	
	private void setLayout(Sheet worksheet) {
		Row rowHeader = worksheet.createRow(TemplatePopulateImportConstants.ROWHEADER_INDEX);
		rowHeader.setHeight(TemplatePopulateImportConstants.ROW_HEADER_HEIGHT);
		worksheet.setColumnWidth(JournalEntryConstants.OFFICE_NAME_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(JournalEntryConstants.TRANSACION_ON_DATE_COL,  TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(JournalEntryConstants.CURRENCY_NAME_COL,  TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(JournalEntryConstants.PAYMENT_TYPE_ID_COL,  TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(JournalEntryConstants.TRANSACTION_ID_COL,  TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(JournalEntryConstants.GL_ACCOUNT_ID_CREDIT_COL,  TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(JournalEntryConstants.AMOUNT_CREDIT_COL,  TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(JournalEntryConstants.GL_ACCOUNT_ID_DEBIT_COL,  TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(JournalEntryConstants.AMOUNT_DEBIT_COL,  TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(JournalEntryConstants.ACCOUNT_NO_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(JournalEntryConstants.CHECK_NO_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(JournalEntryConstants.ROUTING_CODE_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(JournalEntryConstants.RECEIPT_NO_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(JournalEntryConstants.BANK_NO_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(JournalEntryConstants.COMMENTS_COL,TemplatePopulateImportConstants.EXTRALARGE_COL_SIZE);

		writeString(JournalEntryConstants.OFFICE_NAME_COL, rowHeader, "Office Name*");
		writeString(JournalEntryConstants.TRANSACION_ON_DATE_COL, rowHeader, "Transaction On *");
		writeString(JournalEntryConstants.CURRENCY_NAME_COL, rowHeader, "Currecy Type*");
		writeString(JournalEntryConstants.PAYMENT_TYPE_ID_COL, rowHeader, "Payment Type*");
		writeString(JournalEntryConstants.TRANSACTION_ID_COL, rowHeader, "Transaction Id*");
		writeString(JournalEntryConstants.GL_ACCOUNT_ID_CREDIT_COL, rowHeader, "Credit Account Type*");
		writeString(JournalEntryConstants.AMOUNT_CREDIT_COL, rowHeader, "Amount*");
		writeString(JournalEntryConstants.GL_ACCOUNT_ID_DEBIT_COL, rowHeader, "Debit Account Type*");
		writeString(JournalEntryConstants.AMOUNT_DEBIT_COL, rowHeader, "Amount*");
		writeString(JournalEntryConstants.ACCOUNT_NO_COL,rowHeader,"Account#");
		writeString(JournalEntryConstants.CHECK_NO_COL,rowHeader,"Cheque#");
		writeString(JournalEntryConstants.ROUTING_CODE_COL,rowHeader,"Routing code");
		writeString(JournalEntryConstants.RECEIPT_NO_COL,rowHeader,"Receipt#");
		writeString(JournalEntryConstants.BANK_NO_COL,rowHeader,"Bank#");
		writeString(JournalEntryConstants.COMMENTS_COL,rowHeader,"Comments");

		// TODO Auto-generated method stub

	}
	
	private void setRules(Sheet worksheet) {
	
			CellRangeAddressList officeNameRange = new CellRangeAddressList(1,
					SpreadsheetVersion.EXCEL97.getLastRowIndex(),
					JournalEntryConstants.OFFICE_NAME_COL,JournalEntryConstants. OFFICE_NAME_COL);

			CellRangeAddressList currencyCodeRange = new CellRangeAddressList(
					1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
					JournalEntryConstants.CURRENCY_NAME_COL, JournalEntryConstants.CURRENCY_NAME_COL);

			CellRangeAddressList paymenttypeRange = new CellRangeAddressList(1,
					SpreadsheetVersion.EXCEL97.getLastRowIndex(),
					JournalEntryConstants.PAYMENT_TYPE_ID_COL, JournalEntryConstants.PAYMENT_TYPE_ID_COL);

			CellRangeAddressList glaccountCreditRange = new CellRangeAddressList(
					1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
					JournalEntryConstants.GL_ACCOUNT_ID_CREDIT_COL, JournalEntryConstants.GL_ACCOUNT_ID_CREDIT_COL);

			CellRangeAddressList glaccountDebitRange = new CellRangeAddressList(
					1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
					JournalEntryConstants.GL_ACCOUNT_ID_DEBIT_COL, JournalEntryConstants.GL_ACCOUNT_ID_DEBIT_COL);

			DataValidationHelper validationHelper = new HSSFDataValidationHelper(
					(HSSFSheet) worksheet);

			setNames(worksheet);

			DataValidationConstraint officeNameConstraint = validationHelper
					.createFormulaListConstraint("Office");
			DataValidationConstraint currencyCodeConstraint = validationHelper
					.createFormulaListConstraint("Currency");
			DataValidationConstraint paymentTypeConstraint = validationHelper
					.createFormulaListConstraint("PaymentType");

			DataValidationConstraint glaccountConstraint = validationHelper
					.createFormulaListConstraint("GlAccounts");

			DataValidation officeValidation = validationHelper
					.createValidation(officeNameConstraint, officeNameRange);
			DataValidation currencyCodeValidation = validationHelper
					.createValidation(currencyCodeConstraint, currencyCodeRange);
			DataValidation paymentTypeValidation = validationHelper
					.createValidation(paymentTypeConstraint, paymenttypeRange);

			DataValidation glaccountCreditValidation = validationHelper
					.createValidation(glaccountConstraint, glaccountCreditRange);
			DataValidation glaccountDebitValidation = validationHelper
					.createValidation(glaccountConstraint, glaccountDebitRange);

			worksheet.addValidationData(officeValidation);
			worksheet.addValidationData(currencyCodeValidation);
			worksheet.addValidationData(paymentTypeValidation);

			worksheet.addValidationData(glaccountCreditValidation);
			worksheet.addValidationData(glaccountDebitValidation);
		}
	
	private void setNames(Sheet worksheet) {
		Workbook addJournalEntriesWorkbook = worksheet.getWorkbook();
		ArrayList<String> officeNames = new ArrayList<>(officeSheetPopulator.getOfficeNames());
		// Office Names
		Name officeGroup = addJournalEntriesWorkbook.createName();
		officeGroup.setNameName("Office");
		officeGroup.setRefersToFormula(TemplatePopulateImportConstants.OFFICE_SHEET_NAME+"!$B$2:$B$"
				+ (officeNames.size() + 1));
		// Payment Type Name
		Name paymentTypeGroup = addJournalEntriesWorkbook.createName();
		paymentTypeGroup.setNameName("PaymentType");
		paymentTypeGroup.setRefersToFormula(TemplatePopulateImportConstants.EXTRAS_SHEET_NAME+"!$D$2:$D$"
				+ (extrasSheetPopulator.getPaymentTypesSize() + 1));
		// Currency Type Name
		Name currencyGroup = addJournalEntriesWorkbook.createName();
		currencyGroup.setNameName("Currency");
		currencyGroup.setRefersToFormula(TemplatePopulateImportConstants.EXTRAS_SHEET_NAME+"!$F$2:$F$"
				+ (extrasSheetPopulator.getCurrenciesSize() + 1));

		// Account Name
		Name glaccountGroup = addJournalEntriesWorkbook.createName();
		glaccountGroup.setNameName("GlAccounts");
		glaccountGroup.setRefersToFormula(TemplatePopulateImportConstants.GL_ACCOUNTS_SHEET_NAME+"!$B$2:$B$"
				+ (glAccountSheetPopulator.getGlAccountNamesSize() + 1));
	}
	
	private void setDefaults(Sheet worksheet) {
		for (Integer rowNo = 1; rowNo < 1000; rowNo++) {
			Row row = worksheet.createRow(rowNo);
		}

	}

	
	
}
