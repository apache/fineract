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
package org.apache.fineract.infrastructure.bulkimport.populator.loanrepayment;

import org.apache.fineract.infrastructure.bulkimport.constants.LoanRepaymentConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.populator.AbstractWorkbookPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.ClientSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.ExtrasSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.OfficeSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.comparator.LoanComparatorByStatusActive;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.loanaccount.data.LoanAccountData;
import org.apache.poi.hssf.usermodel.HSSFDataValidationHelper;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class LoanRepaymentWorkbookPopulator extends AbstractWorkbookPopulator {
	private OfficeSheetPopulator officeSheetPopulator;
	private ClientSheetPopulator clientSheetPopulator;
	private ExtrasSheetPopulator extrasSheetPopulator;
	private List<LoanAccountData> allloans;
	private Map<Long,String> clientIdToClientExternalId;

	public LoanRepaymentWorkbookPopulator(List<LoanAccountData> loans, OfficeSheetPopulator officeSheetPopulator,
			ClientSheetPopulator clientSheetPopulator, ExtrasSheetPopulator extrasSheetPopulator) {
		this.allloans = loans;
		this.officeSheetPopulator = officeSheetPopulator;
		this.clientSheetPopulator = clientSheetPopulator;
		this.extrasSheetPopulator = extrasSheetPopulator;
	}

	@Override
	public void populate(Workbook workbook,String dateFormat) {
		Sheet loanRepaymentSheet = workbook.createSheet(TemplatePopulateImportConstants.LOAN_REPAYMENT_SHEET_NAME);
		setLayout(loanRepaymentSheet);
		officeSheetPopulator.populate(workbook,dateFormat);
		clientSheetPopulator.populate(workbook,dateFormat);
		extrasSheetPopulator.populate(workbook,dateFormat);
		setClientIdToClientExternalId();
		populateLoansTable(loanRepaymentSheet,dateFormat);
		setRules(loanRepaymentSheet,dateFormat);
		setDefaults(loanRepaymentSheet);
	}

	private void setClientIdToClientExternalId() {
		clientIdToClientExternalId =new HashMap<>();
		List<ClientData>allclients=clientSheetPopulator.getClients();
		for (ClientData client: allclients) {
			if (client.getExternalId()!=null)
			clientIdToClientExternalId.put(client.getId(),client.getExternalId());
		}
	}

	private void setDefaults(Sheet worksheet) {
			for (Integer rowNo = 1; rowNo < 3000; rowNo++) {
				Row row = worksheet.getRow(rowNo);
				if (row == null)
					row = worksheet.createRow(rowNo);
				writeFormula(LoanRepaymentConstants.CLIENT_EXTERNAL_ID, row,
						"IF(ISERROR(VLOOKUP($B"+(rowNo+1)+",$P$2:$Q$"+(allloans.size()+1)+",2,FALSE))," +
								"\"\",(VLOOKUP($B"+(rowNo+1)+",$P$2:$Q$"+(allloans.size()+1)+",2,FALSE)))");
				writeFormula(LoanRepaymentConstants.PRODUCT_COL, row,
						"IF(ISERROR(VLOOKUP($D" + (rowNo + 1) + ",$R$2:$T$" + (allloans.size() + 1)
								+ ",2,FALSE)),\"\",VLOOKUP($D" + (rowNo + 1) + ",$R$2:$T$" + (allloans.size() + 1)
								+ ",2,FALSE))");
				writeFormula(LoanRepaymentConstants.PRINCIPAL_COL, row,
						"IF(ISERROR(VLOOKUP($D" + (rowNo + 1) + ",$R$2:$T$" + (allloans.size() + 1)
								+ ",3,FALSE)),\"\",VLOOKUP($D" + (rowNo + 1) + ",$R$2:$T$" + (allloans.size() + 1)
								+ ",3,FALSE))");
			}
	}

	private void setRules(Sheet worksheet,String dateFormat) {
		CellRangeAddressList officeNameRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
				LoanRepaymentConstants.OFFICE_NAME_COL, LoanRepaymentConstants.OFFICE_NAME_COL);
		CellRangeAddressList clientNameRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
				LoanRepaymentConstants.CLIENT_NAME_COL, LoanRepaymentConstants.CLIENT_NAME_COL);
		CellRangeAddressList accountNumberRange = new CellRangeAddressList(1,
				SpreadsheetVersion.EXCEL97.getLastRowIndex(), LoanRepaymentConstants.LOAN_ACCOUNT_NO_COL, LoanRepaymentConstants.LOAN_ACCOUNT_NO_COL);
		CellRangeAddressList repaymentTypeRange = new CellRangeAddressList(1,
				SpreadsheetVersion.EXCEL97.getLastRowIndex(), LoanRepaymentConstants.REPAYMENT_TYPE_COL, LoanRepaymentConstants.REPAYMENT_TYPE_COL);
		CellRangeAddressList repaymentDateRange = new CellRangeAddressList(1,
				SpreadsheetVersion.EXCEL97.getLastRowIndex(), LoanRepaymentConstants.REPAID_ON_DATE_COL, LoanRepaymentConstants.REPAID_ON_DATE_COL);

		DataValidationHelper validationHelper = new HSSFDataValidationHelper((HSSFSheet) worksheet);

		setNames(worksheet);

		DataValidationConstraint officeNameConstraint = validationHelper.createFormulaListConstraint("Office");
		DataValidationConstraint clientNameConstraint = validationHelper
				.createFormulaListConstraint("INDIRECT(CONCATENATE(\"Client_\",$A1))");
		DataValidationConstraint accountNumberConstraint = validationHelper.createFormulaListConstraint(
				"INDIRECT(CONCATENATE(\"Account_\",SUBSTITUTE(SUBSTITUTE(SUBSTITUTE($B1,\" \",\"_\"),\"(\",\"_\"),\")\",\"_\")))");
		DataValidationConstraint paymentTypeConstraint = validationHelper.createFormulaListConstraint("PaymentTypes");
		DataValidationConstraint repaymentDateConstraint = validationHelper.createDateConstraint(
				DataValidationConstraint.OperatorType.BETWEEN,
				"=VLOOKUP($D1,$R$2:$U$" + (allloans.size() + 1) + ",4,FALSE)", "=TODAY()", dateFormat);

		DataValidation officeValidation = validationHelper.createValidation(officeNameConstraint, officeNameRange);
		DataValidation clientValidation = validationHelper.createValidation(clientNameConstraint, clientNameRange);
		DataValidation accountNumberValidation = validationHelper.createValidation(accountNumberConstraint,
				accountNumberRange);
		DataValidation repaymentTypeValidation = validationHelper.createValidation(paymentTypeConstraint,
				repaymentTypeRange);
		DataValidation repaymentDateValidation = validationHelper.createValidation(repaymentDateConstraint,
				repaymentDateRange);

		worksheet.addValidationData(officeValidation);
		worksheet.addValidationData(clientValidation);
		worksheet.addValidationData(accountNumberValidation);
		worksheet.addValidationData(repaymentTypeValidation);
		worksheet.addValidationData(repaymentDateValidation);

	}

	private void setNames(Sheet worksheet) {
		ArrayList<String> officeNames = new ArrayList<>(officeSheetPopulator.getOfficeNames());
		Workbook loanRepaymentWorkbook = worksheet.getWorkbook();
		// Office Names
		Name officeGroup = loanRepaymentWorkbook.createName();
		officeGroup.setNameName("Office");
		officeGroup.setRefersToFormula(TemplatePopulateImportConstants.OFFICE_SHEET_NAME+"!$B$2:$B$" + (officeNames.size() + 1));

		// Clients Named after Offices
		for (Integer i = 0; i < officeNames.size(); i++) {
			Integer[] officeNameToBeginEndIndexesOfClients = clientSheetPopulator
					.getOfficeNameToBeginEndIndexesOfClients().get(i);
			Name name = loanRepaymentWorkbook.createName();
			if (officeNameToBeginEndIndexesOfClients != null) {
				name.setNameName("Client_" + officeNames.get(i).trim().replaceAll("[ )(]", "_"));
				name.setRefersToFormula(TemplatePopulateImportConstants.CLIENT_SHEET_NAME+"!$B$" + officeNameToBeginEndIndexesOfClients[0] + ":$B$"
						+ officeNameToBeginEndIndexesOfClients[1]);
			}
		}

		// Counting clients with active loans and starting and end addresses of
		// cells
		HashMap<String, Integer[]> clientNameToBeginEndIndexes = new HashMap<String, Integer[]>();
		ArrayList<String> clientsWithActiveLoans = new ArrayList<String>();
		ArrayList<String> clientIdsWithActiveLoans = new ArrayList<String>();
		int startIndex = 1, endIndex = 1;
		String clientName = "";
		String clientId = "";
		for (int i = 0; i < allloans.size(); i++) {
			if (!clientName.equals(allloans.get(i).getClientName())) {
				endIndex = i + 1;
				clientNameToBeginEndIndexes.put(clientName, new Integer[] { startIndex, endIndex });
				startIndex = i + 2;
				clientName = allloans.get(i).getClientName();
				clientId = allloans.get(i).getClientId().toString();
				if (!clientsWithActiveLoans.contains(clientName)) {
					clientsWithActiveLoans.add(clientName);
					clientIdsWithActiveLoans.add(clientId);
				}
			}
			if (i == allloans.size() - 1) {
				endIndex = i + 2;
				clientNameToBeginEndIndexes.put(clientName, new Integer[] { startIndex, endIndex });
			}
		}

			// Account Number Named after Clients
		for (int j = 0; j < clientsWithActiveLoans.size(); j++) {
			Name name = loanRepaymentWorkbook.createName();
			name.setNameName("Account_" + clientsWithActiveLoans.get(j).replaceAll(" ", "_") + "_"
					+ clientIdsWithActiveLoans.get(j) + "_");
			name.setRefersToFormula(
					TemplatePopulateImportConstants.LOAN_REPAYMENT_SHEET_NAME+"!$R$" + clientNameToBeginEndIndexes.get(clientsWithActiveLoans.get(j))[0] + ":$R$"
							+ clientNameToBeginEndIndexes.get(clientsWithActiveLoans.get(j))[1]);
		}

		// Payment Type Name
		Name paymentTypeGroup = loanRepaymentWorkbook.createName();
		paymentTypeGroup.setNameName("PaymentTypes");
		paymentTypeGroup.setRefersToFormula(TemplatePopulateImportConstants.EXTRAS_SHEET_NAME+"!$D$2:$D$" + (extrasSheetPopulator.getPaymentTypesSize() + 1));
	}

	private void populateLoansTable(Sheet loanRepaymentSheet,String dateFormat) {
		int rowIndex = 1;
		Row row;
		Workbook workbook = loanRepaymentSheet.getWorkbook();
		CellStyle dateCellStyle = workbook.createCellStyle();
		short df = workbook.createDataFormat().getFormat(dateFormat);
		dateCellStyle.setDataFormat(df);
		SimpleDateFormat outputFormat = new SimpleDateFormat(dateFormat);
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = null;
		Collections.sort(allloans,new LoanComparatorByStatusActive());
		for (LoanAccountData loan : allloans) {
			row = loanRepaymentSheet.createRow(rowIndex++);
			writeString(LoanRepaymentConstants.LOOKUP_CLIENT_NAME_COL, row, loan.getClientName() + "(" + loan.getClientId() + ")");
			writeString(LoanRepaymentConstants.LOOKUP_CLIENT_EXTERNAL_ID,row, clientIdToClientExternalId.get(loan.getClientId()));
			writeString(LoanRepaymentConstants.LOOKUP_ACCOUNT_NO_COL, row, loan.getAccountNo()+"-"+loan.getStatusStringValue());
			writeString(LoanRepaymentConstants.LOOKUP_PRODUCT_COL, row, loan.getLoanProductName());
			writeDouble(LoanRepaymentConstants.LOOKUP_PRINCIPAL_COL, row, loan.getPrincipal().doubleValue());
			if (loan.getDisbursementDate() != null) {
				try {
					date = inputFormat.parse(loan.getDisbursementDate().toString());
				} catch (ParseException e) {
					e.printStackTrace();
				}
				writeDate(LoanRepaymentConstants.LOOKUP_LOAN_DISBURSEMENT_DATE_COL, row,
						outputFormat.format(date), dateCellStyle,dateFormat);
			}
		}
	}

	private void setLayout(Sheet worksheet) {
		Row rowHeader = worksheet.createRow(TemplatePopulateImportConstants.ROWHEADER_INDEX);
		rowHeader.setHeight(TemplatePopulateImportConstants.ROW_HEADER_HEIGHT);
		worksheet.setColumnWidth(LoanRepaymentConstants.OFFICE_NAME_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(LoanRepaymentConstants.CLIENT_NAME_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
		worksheet.setColumnWidth(LoanRepaymentConstants.CLIENT_EXTERNAL_ID,TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(LoanRepaymentConstants.LOAN_ACCOUNT_NO_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(LoanRepaymentConstants.PRODUCT_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(LoanRepaymentConstants.PRINCIPAL_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(LoanRepaymentConstants.AMOUNT_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(LoanRepaymentConstants.REPAID_ON_DATE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(LoanRepaymentConstants.REPAYMENT_TYPE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(LoanRepaymentConstants.ACCOUNT_NO_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(LoanRepaymentConstants.CHECK_NO_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(LoanRepaymentConstants.RECEIPT_NO_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(LoanRepaymentConstants.ROUTING_CODE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(LoanRepaymentConstants.BANK_NO_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(LoanRepaymentConstants.LOOKUP_CLIENT_NAME_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
		worksheet.setColumnWidth(LoanRepaymentConstants.LOOKUP_CLIENT_EXTERNAL_ID,TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(LoanRepaymentConstants.LOOKUP_ACCOUNT_NO_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(LoanRepaymentConstants.LOOKUP_PRODUCT_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(LoanRepaymentConstants.LOOKUP_PRINCIPAL_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(LoanRepaymentConstants.LOOKUP_LOAN_DISBURSEMENT_DATE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		writeString(LoanRepaymentConstants.OFFICE_NAME_COL, rowHeader, "Office Name*");
		writeString(LoanRepaymentConstants.CLIENT_NAME_COL, rowHeader, "Client Name*");
		writeString(LoanRepaymentConstants.CLIENT_EXTERNAL_ID,rowHeader,"Client Ext.Id");
		writeString(LoanRepaymentConstants.LOAN_ACCOUNT_NO_COL, rowHeader, "Loan Account No.*");
		writeString(LoanRepaymentConstants.PRODUCT_COL, rowHeader, "Product Name");
		writeString(LoanRepaymentConstants.PRINCIPAL_COL, rowHeader, "Principal");
		writeString(LoanRepaymentConstants.AMOUNT_COL, rowHeader, "Amount Repaid*");
		writeString(LoanRepaymentConstants.REPAID_ON_DATE_COL, rowHeader, "Date*");
		writeString(LoanRepaymentConstants.REPAYMENT_TYPE_COL, rowHeader, "Type*");
		writeString(LoanRepaymentConstants.ACCOUNT_NO_COL, rowHeader, "Account No");
		writeString(LoanRepaymentConstants.CHECK_NO_COL, rowHeader, "Check No");
		writeString(LoanRepaymentConstants.RECEIPT_NO_COL, rowHeader, "Receipt No");
		writeString(LoanRepaymentConstants.ROUTING_CODE_COL, rowHeader, "Routing Code");
		writeString(LoanRepaymentConstants.BANK_NO_COL, rowHeader, "Bank No");
		writeString(LoanRepaymentConstants.LOOKUP_CLIENT_NAME_COL, rowHeader, "Lookup Client");
		writeString(LoanRepaymentConstants.LOOKUP_CLIENT_EXTERNAL_ID,rowHeader,"Lookup ClientExtId");
		writeString(LoanRepaymentConstants.LOOKUP_ACCOUNT_NO_COL, rowHeader, "Lookup Account");
		writeString(LoanRepaymentConstants.LOOKUP_PRODUCT_COL, rowHeader, "Lookup Product");
		writeString(LoanRepaymentConstants.LOOKUP_PRINCIPAL_COL, rowHeader, "Lookup Principal");
		writeString(LoanRepaymentConstants.LOOKUP_LOAN_DISBURSEMENT_DATE_COL, rowHeader, "Lookup Loan Disbursement Date");

	}

}