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
package org.apache.fineract.infrastructure.bulkimport.populator.loan;

import org.apache.fineract.infrastructure.bulkimport.constants.LoanConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.populator.*;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;
import org.apache.poi.hssf.usermodel.HSSFDataValidationHelper;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;

import java.util.List;

public class LoanWorkbookPopulator extends AbstractWorkbookPopulator {

	private OfficeSheetPopulator officeSheetPopulator;
	private ClientSheetPopulator clientSheetPopulator;
	private GroupSheetPopulator groupSheetPopulator;
	private PersonnelSheetPopulator personnelSheetPopulator;
	private LoanProductSheetPopulator productSheetPopulator;
	private ExtrasSheetPopulator extrasSheetPopulator;


	public LoanWorkbookPopulator(OfficeSheetPopulator officeSheetPopulator, ClientSheetPopulator clientSheetPopulator,
			GroupSheetPopulator groupSheetPopulator, PersonnelSheetPopulator personnelSheetPopulator,
			LoanProductSheetPopulator productSheetPopulator, ExtrasSheetPopulator extrasSheetPopulator) {
		this.officeSheetPopulator = officeSheetPopulator;
		this.clientSheetPopulator = clientSheetPopulator;
		this.groupSheetPopulator = groupSheetPopulator;
		this.personnelSheetPopulator = personnelSheetPopulator;
		this.productSheetPopulator = productSheetPopulator;
		this.extrasSheetPopulator = extrasSheetPopulator;
	}

	@Override
	public void populate(Workbook workbook,String dateFormat) {
		Sheet loanSheet = workbook.createSheet(TemplatePopulateImportConstants.LOANS_SHEET_NAME);
		officeSheetPopulator.populate(workbook,dateFormat);
		clientSheetPopulator.populate(workbook,dateFormat);
		groupSheetPopulator.populate(workbook,dateFormat);
		personnelSheetPopulator.populate(workbook,dateFormat);
		productSheetPopulator.populate(workbook,dateFormat);
		extrasSheetPopulator.populate(workbook,dateFormat);
		setLayout(loanSheet);
		setRules(loanSheet,dateFormat);
		setDefaults(loanSheet);
		setClientAndGroupDateLookupTable(loanSheet, clientSheetPopulator.getClients(), groupSheetPopulator.getGroups(),
				LoanConstants.LOOKUP_CLIENT_NAME_COL, LoanConstants.LOOKUP_ACTIVATION_DATE_COL,
				TemplatePopulateImportConstants.CONTAINS_CLIENT_EXTERNAL_ID,dateFormat);
	}

	private void setRules(Sheet worksheet,String dateFormat) {
		CellRangeAddressList officeNameRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
				LoanConstants.OFFICE_NAME_COL, LoanConstants.OFFICE_NAME_COL);
		CellRangeAddressList loanTypeRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
				LoanConstants.LOAN_TYPE_COL, LoanConstants.LOAN_TYPE_COL);
		CellRangeAddressList clientNameRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
				LoanConstants.CLIENT_NAME_COL, LoanConstants.CLIENT_NAME_COL);
		CellRangeAddressList productNameRange = new CellRangeAddressList(1,
				SpreadsheetVersion.EXCEL97.getLastRowIndex(), LoanConstants.PRODUCT_COL, LoanConstants.PRODUCT_COL);
		CellRangeAddressList loanOfficerRange = new CellRangeAddressList(1,
				SpreadsheetVersion.EXCEL97.getLastRowIndex(), LoanConstants.LOAN_OFFICER_NAME_COL, LoanConstants.LOAN_OFFICER_NAME_COL);
		CellRangeAddressList submittedDateRange = new CellRangeAddressList(1,
				SpreadsheetVersion.EXCEL97.getLastRowIndex(), LoanConstants.SUBMITTED_ON_DATE_COL,LoanConstants. SUBMITTED_ON_DATE_COL);
		CellRangeAddressList fundNameRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
				LoanConstants.FUND_NAME_COL, LoanConstants.FUND_NAME_COL);
		CellRangeAddressList principalRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
				LoanConstants.PRINCIPAL_COL,LoanConstants.PRINCIPAL_COL);
		CellRangeAddressList noOfRepaymentsRange = new CellRangeAddressList(1,
				SpreadsheetVersion.EXCEL97.getLastRowIndex(), LoanConstants.NO_OF_REPAYMENTS_COL, LoanConstants.NO_OF_REPAYMENTS_COL);
		CellRangeAddressList repaidFrequencyRange = new CellRangeAddressList(1,
				SpreadsheetVersion.EXCEL97.getLastRowIndex(), LoanConstants.REPAID_EVERY_FREQUENCY_COL, LoanConstants.REPAID_EVERY_FREQUENCY_COL);
		CellRangeAddressList loanTermRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
				LoanConstants.LOAN_TERM_COL, LoanConstants.LOAN_TERM_COL);
		CellRangeAddressList loanTermFrequencyRange = new CellRangeAddressList(1,
				SpreadsheetVersion.EXCEL97.getLastRowIndex(), LoanConstants.LOAN_TERM_FREQUENCY_COL, LoanConstants.LOAN_TERM_FREQUENCY_COL);
		CellRangeAddressList interestFrequencyRange = new CellRangeAddressList(1,
				SpreadsheetVersion.EXCEL97.getLastRowIndex(),LoanConstants.NOMINAL_INTEREST_RATE_FREQUENCY_COL,
				LoanConstants.NOMINAL_INTEREST_RATE_FREQUENCY_COL);
		CellRangeAddressList interestRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
				LoanConstants.NOMINAL_INTEREST_RATE_COL, LoanConstants.NOMINAL_INTEREST_RATE_COL);
		CellRangeAddressList amortizationRange = new CellRangeAddressList(1,
				SpreadsheetVersion.EXCEL97.getLastRowIndex(), LoanConstants.AMORTIZATION_COL, LoanConstants.AMORTIZATION_COL);
		CellRangeAddressList interestMethodRange = new CellRangeAddressList(1,
				SpreadsheetVersion.EXCEL97.getLastRowIndex(), LoanConstants.INTEREST_METHOD_COL, LoanConstants.INTEREST_METHOD_COL);
		CellRangeAddressList intrestCalculationPeriodRange = new CellRangeAddressList(1,
				SpreadsheetVersion.EXCEL97.getLastRowIndex(), LoanConstants.INTEREST_CALCULATION_PERIOD_COL,
				LoanConstants.INTEREST_CALCULATION_PERIOD_COL);
		CellRangeAddressList repaymentStrategyRange = new CellRangeAddressList(1,
				SpreadsheetVersion.EXCEL97.getLastRowIndex(), LoanConstants.REPAYMENT_STRATEGY_COL,LoanConstants. REPAYMENT_STRATEGY_COL);
		CellRangeAddressList arrearsToleranceRange = new CellRangeAddressList(1,
				SpreadsheetVersion.EXCEL97.getLastRowIndex(), LoanConstants.ARREARS_TOLERANCE_COL,LoanConstants. ARREARS_TOLERANCE_COL);
		CellRangeAddressList graceOnPrincipalPaymentRange = new CellRangeAddressList(1,
				SpreadsheetVersion.EXCEL97.getLastRowIndex(), LoanConstants.GRACE_ON_PRINCIPAL_PAYMENT_COL,
				LoanConstants.GRACE_ON_PRINCIPAL_PAYMENT_COL);
		CellRangeAddressList graceOnInterestPaymentRange = new CellRangeAddressList(1,
				SpreadsheetVersion.EXCEL97.getLastRowIndex(), LoanConstants.GRACE_ON_INTEREST_PAYMENT_COL,
				LoanConstants.GRACE_ON_INTEREST_PAYMENT_COL);
		CellRangeAddressList graceOnInterestChargedRange = new CellRangeAddressList(1,
				SpreadsheetVersion.EXCEL97.getLastRowIndex(), LoanConstants.GRACE_ON_INTEREST_CHARGED_COL,
				LoanConstants.GRACE_ON_INTEREST_CHARGED_COL);
		CellRangeAddressList approvedDateRange = new CellRangeAddressList(1,
				SpreadsheetVersion.EXCEL97.getLastRowIndex(), LoanConstants.APPROVED_DATE_COL, LoanConstants.APPROVED_DATE_COL);
		CellRangeAddressList disbursedDateRange = new CellRangeAddressList(1,
				SpreadsheetVersion.EXCEL97.getLastRowIndex(), LoanConstants.DISBURSED_DATE_COL, LoanConstants.DISBURSED_DATE_COL);
		CellRangeAddressList paymentTypeRange = new CellRangeAddressList(1,
				SpreadsheetVersion.EXCEL97.getLastRowIndex(), LoanConstants.DISBURSED_PAYMENT_TYPE_COL, LoanConstants.DISBURSED_PAYMENT_TYPE_COL);
		CellRangeAddressList repaymentTypeRange = new CellRangeAddressList(1,
				SpreadsheetVersion.EXCEL97.getLastRowIndex(), LoanConstants.REPAYMENT_TYPE_COL,LoanConstants. REPAYMENT_TYPE_COL);
		CellRangeAddressList lastrepaymentDateRange = new CellRangeAddressList(1,
				SpreadsheetVersion.EXCEL97.getLastRowIndex(), LoanConstants.LAST_REPAYMENT_DATE_COL, LoanConstants.LAST_REPAYMENT_DATE_COL);
		DataValidationHelper validationHelper = new HSSFDataValidationHelper((HSSFSheet) worksheet);

		setNames(worksheet);

		DataValidationConstraint officeNameConstraint = validationHelper.createFormulaListConstraint("Office");
		DataValidationConstraint loanTypeConstraint = validationHelper
				.createExplicitListConstraint(new String[] {
						LoanConstants.LOAN_TYPE_INDIVIDUAL,
						LoanConstants.LOAN_TYPE_GROUP,
						LoanConstants.LOAN_TYPE_JLG});
		DataValidationConstraint clientNameConstraint = validationHelper.createFormulaListConstraint(
				"IF($B1=\"Group\",INDIRECT(CONCATENATE(\"Group_\",$A1)),INDIRECT(CONCATENATE(\"Client_\",$A1)))");
		DataValidationConstraint productNameConstraint = validationHelper.createFormulaListConstraint("Products");
		DataValidationConstraint loanOfficerNameConstraint = validationHelper
				.createFormulaListConstraint("INDIRECT(CONCATENATE(\"Staff_\",$A1))");
		DataValidationConstraint submittedDateConstraint = validationHelper.createDateConstraint(
				DataValidationConstraint.OperatorType.BETWEEN,
				"=IF(INDIRECT(CONCATENATE(\"START_DATE_\",$E1))>VLOOKUP($C1,$AR$2:$AT$"
						+ (clientSheetPopulator.getClientsSize() + groupSheetPopulator.getGroupsSize() + 1)
						+ ",3,FALSE),INDIRECT(CONCATENATE(\"START_DATE_\",$E1)),VLOOKUP($C1,$AR$2:$AT$"
						+ (clientSheetPopulator.getClientsSize() + groupSheetPopulator.getGroupsSize() + 1)
						+ ",3,FALSE))",
				"=TODAY()", dateFormat);
		DataValidationConstraint approvalDateConstraint = validationHelper
				.createDateConstraint(DataValidationConstraint.OperatorType.BETWEEN, "=$G1", "=TODAY()", dateFormat);
		DataValidationConstraint disbursedDateConstraint = validationHelper
				.createDateConstraint(DataValidationConstraint.OperatorType.BETWEEN, "=$H1", "=TODAY()", dateFormat);
		DataValidationConstraint paymentTypeConstraint = validationHelper.createFormulaListConstraint("PaymentTypes");
		DataValidationConstraint fundNameConstraint = validationHelper.createFormulaListConstraint("Funds");
		DataValidationConstraint principalConstraint = validationHelper.createDecimalConstraint(
				DataValidationConstraint.OperatorType.BETWEEN, "=INDIRECT(CONCATENATE(\"MIN_PRINCIPAL_\",$E1))",
				"=INDIRECT(CONCATENATE(\"MAX_PRINCIPAL_\",$E1))");
		DataValidationConstraint noOfRepaymentsConstraint = validationHelper.createIntegerConstraint(
				DataValidationConstraint.OperatorType.BETWEEN, "=INDIRECT(CONCATENATE(\"MIN_REPAYMENT_\",$E1))",
				"=INDIRECT(CONCATENATE(\"MAX_REPAYMENT_\",$E1))");
		DataValidationConstraint frequencyConstraint = validationHelper
				.createExplicitListConstraint(new String[] { "Days", "Weeks", "Months" });
		DataValidationConstraint loanTermConstraint = validationHelper
				.createIntegerConstraint(DataValidationConstraint.OperatorType.GREATER_OR_EQUAL, "=$M1*$N1", null);
		DataValidationConstraint interestFrequencyConstraint = validationHelper
				.createFormulaListConstraint("INDIRECT(CONCATENATE(\"INTEREST_FREQUENCY_\",$E1))");
		DataValidationConstraint interestConstraint = validationHelper.createDecimalConstraint(
				DataValidationConstraint.OperatorType.BETWEEN, "=INDIRECT(CONCATENATE(\"MIN_INTEREST_\",$E1))",
				"=INDIRECT(CONCATENATE(\"MAX_INTEREST_\",$E1))");
		DataValidationConstraint amortizationConstraint = validationHelper
				.createExplicitListConstraint(new String[] { "Equal principal payments", "Equal installments" });
		DataValidationConstraint interestMethodConstraint = validationHelper
				.createExplicitListConstraint(new String[] { "Flat", "Declining Balance" });
		DataValidationConstraint interestCalculationPeriodConstraint = validationHelper
				.createExplicitListConstraint(new String[] { "Daily", "Same as repayment period" });
		DataValidationConstraint repaymentStrategyConstraint = validationHelper.createExplicitListConstraint(
				new String[] { "Penalties, Fees, Interest, Principal order", "HeavensFamily Unique", "Creocore Unique",
						"Overdue/Due Fee/Int,Principal", "Principal, Interest, Penalties, Fees Order",
						"Interest, Principal, Penalties, Fees Order", "Early Repayment Strategy" });
		DataValidationConstraint arrearsToleranceConstraint = validationHelper
				.createIntegerConstraint(DataValidationConstraint.OperatorType.GREATER_OR_EQUAL, "0", null);
		DataValidationConstraint graceOnPrincipalPaymentConstraint = validationHelper
				.createIntegerConstraint(DataValidationConstraint.OperatorType.GREATER_OR_EQUAL, "0", null);
		DataValidationConstraint graceOnInterestPaymentConstraint = validationHelper
				.createIntegerConstraint(DataValidationConstraint.OperatorType.GREATER_OR_EQUAL, "0", null);
		DataValidationConstraint graceOnInterestChargedConstraint = validationHelper
				.createIntegerConstraint(DataValidationConstraint.OperatorType.GREATER_OR_EQUAL, "0", null);
		DataValidationConstraint lastRepaymentDateConstraint = validationHelper
				.createDateConstraint(DataValidationConstraint.OperatorType.BETWEEN, "=$I1", "=TODAY()", dateFormat);

		DataValidation officeValidation = validationHelper.createValidation(officeNameConstraint, officeNameRange);
		DataValidation loanTypeValidation = validationHelper.createValidation(loanTypeConstraint, loanTypeRange);
		DataValidation clientValidation = validationHelper.createValidation(clientNameConstraint, clientNameRange);
		DataValidation productNameValidation = validationHelper.createValidation(productNameConstraint,
				productNameRange);
		DataValidation loanOfficerValidation = validationHelper.createValidation(loanOfficerNameConstraint,
				loanOfficerRange);
		DataValidation fundNameValidation = validationHelper.createValidation(fundNameConstraint, fundNameRange);
		DataValidation repaidFrequencyValidation = validationHelper.createValidation(frequencyConstraint,
				repaidFrequencyRange);
		DataValidation loanTermFrequencyValidation = validationHelper.createValidation(frequencyConstraint,
				loanTermFrequencyRange);
		DataValidation amortizationValidation = validationHelper.createValidation(amortizationConstraint,
				amortizationRange);
		DataValidation interestMethodValidation = validationHelper.createValidation(interestMethodConstraint,
				interestMethodRange);
		DataValidation interestCalculationPeriodValidation = validationHelper
				.createValidation(interestCalculationPeriodConstraint, intrestCalculationPeriodRange);
		DataValidation repaymentStrategyValidation = validationHelper.createValidation(repaymentStrategyConstraint,
				repaymentStrategyRange);
		DataValidation paymentTypeValidation = validationHelper.createValidation(paymentTypeConstraint,
				paymentTypeRange);
		DataValidation repaymentTypeValidation = validationHelper.createValidation(paymentTypeConstraint,
				repaymentTypeRange);
		DataValidation submittedDateValidation = validationHelper.createValidation(submittedDateConstraint,
				submittedDateRange);
		DataValidation approvalDateValidation = validationHelper.createValidation(approvalDateConstraint,
				approvedDateRange);
		DataValidation disbursedDateValidation = validationHelper.createValidation(disbursedDateConstraint,
				disbursedDateRange);
		DataValidation lastRepaymentDateValidation = validationHelper.createValidation(lastRepaymentDateConstraint,
				lastrepaymentDateRange);
		DataValidation principalValidation = validationHelper.createValidation(principalConstraint, principalRange);
		DataValidation loanTermValidation = validationHelper.createValidation(loanTermConstraint, loanTermRange);
		DataValidation noOfRepaymentsValidation = validationHelper.createValidation(noOfRepaymentsConstraint,
				noOfRepaymentsRange);
		DataValidation interestValidation = validationHelper.createValidation(interestConstraint, interestRange);
		DataValidation arrearsToleranceValidation = validationHelper.createValidation(arrearsToleranceConstraint,
				arrearsToleranceRange);
		DataValidation graceOnPrincipalPaymentValidation = validationHelper
				.createValidation(graceOnPrincipalPaymentConstraint, graceOnPrincipalPaymentRange);
		DataValidation graceOnInterestPaymentValidation = validationHelper
				.createValidation(graceOnInterestPaymentConstraint, graceOnInterestPaymentRange);
		DataValidation graceOnInterestChargedValidation = validationHelper
				.createValidation(graceOnInterestChargedConstraint, graceOnInterestChargedRange);
		DataValidation interestFrequencyValidation = validationHelper.createValidation(interestFrequencyConstraint,
				interestFrequencyRange);

		interestFrequencyValidation.setSuppressDropDownArrow(true);

		worksheet.addValidationData(officeValidation);
		worksheet.addValidationData(loanTypeValidation);
		worksheet.addValidationData(clientValidation);
		worksheet.addValidationData(productNameValidation);
		worksheet.addValidationData(loanOfficerValidation);
		worksheet.addValidationData(submittedDateValidation);
		worksheet.addValidationData(approvalDateValidation);
		worksheet.addValidationData(disbursedDateValidation);
		worksheet.addValidationData(paymentTypeValidation);
		worksheet.addValidationData(fundNameValidation);
		worksheet.addValidationData(principalValidation);
		worksheet.addValidationData(repaidFrequencyValidation);
		worksheet.addValidationData(loanTermFrequencyValidation);
		worksheet.addValidationData(noOfRepaymentsValidation);
		worksheet.addValidationData(loanTermValidation);
		worksheet.addValidationData(interestValidation);
		worksheet.addValidationData(interestFrequencyValidation);
		worksheet.addValidationData(amortizationValidation);
		worksheet.addValidationData(interestMethodValidation);
		worksheet.addValidationData(interestCalculationPeriodValidation);
		worksheet.addValidationData(repaymentStrategyValidation);
		worksheet.addValidationData(arrearsToleranceValidation);
		worksheet.addValidationData(graceOnPrincipalPaymentValidation);
		worksheet.addValidationData(graceOnInterestPaymentValidation);
		worksheet.addValidationData(graceOnInterestChargedValidation);
		worksheet.addValidationData(lastRepaymentDateValidation);
		worksheet.addValidationData(repaymentTypeValidation);

	}

	private void setLayout(Sheet worksheet) {
		Row rowHeader = worksheet.createRow(TemplatePopulateImportConstants.ROWHEADER_INDEX);
		rowHeader.setHeight(TemplatePopulateImportConstants.ROW_HEADER_HEIGHT);
		worksheet.setColumnWidth(LoanConstants.OFFICE_NAME_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(LoanConstants.LOAN_TYPE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(LoanConstants.CLIENT_NAME_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(LoanConstants.CLIENT_EXTERNAL_ID,TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
		worksheet.setColumnWidth(LoanConstants.PRODUCT_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(LoanConstants.LOAN_OFFICER_NAME_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(LoanConstants.SUBMITTED_ON_DATE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(LoanConstants.APPROVED_DATE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(LoanConstants.DISBURSED_DATE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(LoanConstants.DISBURSED_PAYMENT_TYPE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(LoanConstants.FUND_NAME_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(LoanConstants.PRINCIPAL_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(LoanConstants.LOAN_TERM_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(LoanConstants.LOAN_TERM_FREQUENCY_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(LoanConstants.NO_OF_REPAYMENTS_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(LoanConstants.REPAID_EVERY_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(LoanConstants.REPAID_EVERY_FREQUENCY_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(LoanConstants.NOMINAL_INTEREST_RATE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(LoanConstants.NOMINAL_INTEREST_RATE_FREQUENCY_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(LoanConstants.AMORTIZATION_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
		worksheet.setColumnWidth(LoanConstants.INTEREST_METHOD_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(LoanConstants.INTEREST_CALCULATION_PERIOD_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(LoanConstants.ARREARS_TOLERANCE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(LoanConstants.REPAYMENT_STRATEGY_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(LoanConstants.GRACE_ON_PRINCIPAL_PAYMENT_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(LoanConstants.GRACE_ON_INTEREST_PAYMENT_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(LoanConstants.GRACE_ON_INTEREST_CHARGED_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(LoanConstants.INTEREST_CHARGED_FROM_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(LoanConstants.FIRST_REPAYMENT_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(LoanConstants.TOTAL_AMOUNT_REPAID_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(LoanConstants.LAST_REPAYMENT_DATE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(LoanConstants.REPAYMENT_TYPE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(LoanConstants.LOOKUP_CLIENT_NAME_COL,  TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
		worksheet.setColumnWidth(LoanConstants.LOOKUP_CLIENT_EXTERNAL_ID,TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
		worksheet.setColumnWidth(LoanConstants.LOOKUP_ACTIVATION_DATE_COL,  TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
		worksheet.setColumnWidth(LoanConstants.EXTERNAL_ID_COL,  TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
		worksheet.setColumnWidth(LoanConstants.CHARGE_ID_1,  TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
		worksheet.setColumnWidth(LoanConstants.CHARGE_AMOUNT_1,  TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
		worksheet.setColumnWidth(LoanConstants.CHARGE_DUE_DATE_1,  TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
		worksheet.setColumnWidth(LoanConstants.CHARGE_ID_2,  TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
		worksheet.setColumnWidth(LoanConstants.CHARGE_AMOUNT_2,  TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
		worksheet.setColumnWidth(LoanConstants.CHARGE_DUE_DATE_2,  TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
		worksheet.setColumnWidth(LoanConstants.GROUP_ID,  TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
		worksheet.setColumnWidth(LoanConstants.LINK_ACCOUNT_ID,  TemplatePopulateImportConstants.MEDIUM_COL_SIZE);

		writeString(LoanConstants.OFFICE_NAME_COL, rowHeader, "Office Name*");
		writeString(LoanConstants.LOAN_TYPE_COL, rowHeader, "Loan Type*");
		writeString(LoanConstants.CLIENT_NAME_COL, rowHeader, "Client/Group Name*");
		writeString(LoanConstants.CLIENT_EXTERNAL_ID,rowHeader,"Client ExternalID");
		writeString(LoanConstants.PRODUCT_COL, rowHeader, "Product*");
		writeString(LoanConstants.LOAN_OFFICER_NAME_COL, rowHeader, "Loan Officer*");
		writeString(LoanConstants.SUBMITTED_ON_DATE_COL, rowHeader, "Submitted On*");
		writeString(LoanConstants.APPROVED_DATE_COL, rowHeader, "Approved On");
		writeString(LoanConstants.DISBURSED_DATE_COL, rowHeader, "Disbursed Date");
		writeString(LoanConstants.DISBURSED_PAYMENT_TYPE_COL, rowHeader, "Payment Type*");
		writeString(LoanConstants.FUND_NAME_COL, rowHeader, "Fund Name");
		writeString(LoanConstants.PRINCIPAL_COL, rowHeader, "Principal*");
		writeString(LoanConstants.LOAN_TERM_COL, rowHeader, "Loan Term*");
		writeString(LoanConstants.NO_OF_REPAYMENTS_COL, rowHeader, "# of Repayments*");
		writeString(LoanConstants.REPAID_EVERY_COL, rowHeader, "Repaid Every*");
		writeString(LoanConstants.NOMINAL_INTEREST_RATE_COL, rowHeader, "Nominal Interest %*");
		writeString(LoanConstants.AMORTIZATION_COL, rowHeader, "Amortization*");
		writeString(LoanConstants.INTEREST_METHOD_COL, rowHeader, "Interest Method*");
		writeString(LoanConstants.INTEREST_CALCULATION_PERIOD_COL, rowHeader, "Interest Calculation Period*");
		writeString(LoanConstants.ARREARS_TOLERANCE_COL, rowHeader, "Arrears Tolerance");
		writeString(LoanConstants.REPAYMENT_STRATEGY_COL, rowHeader, "Repayment Strategy*");
		writeString(LoanConstants.GRACE_ON_PRINCIPAL_PAYMENT_COL, rowHeader, "Grace-Principal Payment");
		writeString(LoanConstants.GRACE_ON_INTEREST_PAYMENT_COL, rowHeader, "Grace-Interest Payment");
		writeString(LoanConstants.GRACE_ON_INTEREST_CHARGED_COL, rowHeader, "Interest-Free Period(s)");
		writeString(LoanConstants.INTEREST_CHARGED_FROM_COL, rowHeader, "Interest Charged From");
		writeString(LoanConstants.FIRST_REPAYMENT_COL, rowHeader, "First Repayment On");
		writeString(LoanConstants.TOTAL_AMOUNT_REPAID_COL, rowHeader, "Amount Repaid");
		writeString(LoanConstants.LAST_REPAYMENT_DATE_COL, rowHeader, "Date-Last Repayment");
		writeString(LoanConstants.REPAYMENT_TYPE_COL, rowHeader, "Repayment Type");
		writeString(LoanConstants.LOOKUP_CLIENT_NAME_COL, rowHeader, "Client Name");
		writeString(LoanConstants.LOOKUP_CLIENT_EXTERNAL_ID,rowHeader,"Lookup Client ExternalID");
		writeString(LoanConstants.LOOKUP_ACTIVATION_DATE_COL, rowHeader, "Client Activation Date");
		writeString(LoanConstants.EXTERNAL_ID_COL, rowHeader, "External Id");
		writeString(LoanConstants.CHARGE_ID_1, rowHeader, "Charge Id");
		writeString(LoanConstants.CHARGE_AMOUNT_1, rowHeader, "Charged Amount");
		writeString(LoanConstants.CHARGE_DUE_DATE_1, rowHeader, "Charged On Date");
		writeString(LoanConstants.CHARGE_ID_2, rowHeader, "Charge Id");
		writeString(LoanConstants.CHARGE_AMOUNT_2, rowHeader, "Charged Amount");
		writeString(LoanConstants.CHARGE_DUE_DATE_2, rowHeader, "Charged On Date");
		writeString(LoanConstants.GROUP_ID, rowHeader, "GROUP ID");
		writeString(LoanConstants.LINK_ACCOUNT_ID, rowHeader, "Linked Account No.");

		CellStyle borderStyle = worksheet.getWorkbook().createCellStyle();
		CellStyle doubleBorderStyle = worksheet.getWorkbook().createCellStyle();
		borderStyle.setBorderBottom(CellStyle.BORDER_THIN);
		doubleBorderStyle.setBorderBottom(CellStyle.BORDER_THIN);
		doubleBorderStyle.setBorderRight(CellStyle.BORDER_THICK);
		for (int colNo = 0; colNo < 35; colNo++) {
			Cell cell = rowHeader.getCell(colNo);
			if (cell == null)
				rowHeader.createCell(colNo);
			rowHeader.getCell(colNo).setCellStyle(borderStyle);
		}
		rowHeader.getCell(LoanConstants.FIRST_REPAYMENT_COL).setCellStyle(doubleBorderStyle);
		rowHeader.getCell(LoanConstants.REPAYMENT_TYPE_COL).setCellStyle(doubleBorderStyle);
	}

	private void setDefaults(Sheet worksheet) {

		for (Integer rowNo = 1; rowNo < 1000; rowNo++) {
			Row row = worksheet.createRow(rowNo);
			writeFormula(LoanConstants.CLIENT_EXTERNAL_ID, row,
					"IF(ISERROR(VLOOKUP($C"+(rowNo+1)+",$AR$2:$AS$"+(clientSheetPopulator.getClients().size()+1)+",2,FALSE))," +
							"\"\",(VLOOKUP($C"+(rowNo+1)+",$AR$2:$AS$"+(clientSheetPopulator.getClients().size()+1)+",2,FALSE)))");
			writeFormula(LoanConstants.FUND_NAME_COL, row, "IF(ISERROR(INDIRECT(CONCATENATE(\"FUND_\",$E" + (rowNo + 1)
					+ "))),\"\",INDIRECT(CONCATENATE(\"FUND_\",$E" + (rowNo + 1) + ")))");
			writeFormula(LoanConstants.PRINCIPAL_COL, row, "IF(ISERROR(INDIRECT(CONCATENATE(\"PRINCIPAL_\",$E" + (rowNo + 1)
					+ "))),\"\",INDIRECT(CONCATENATE(\"PRINCIPAL_\",$E" + (rowNo + 1) + ")))");
			writeFormula(LoanConstants.REPAID_EVERY_COL, row, "IF(ISERROR(INDIRECT(CONCATENATE(\"REPAYMENT_EVERY_\",$E" + (rowNo + 1)
					+ "))),\"\",INDIRECT(CONCATENATE(\"REPAYMENT_EVERY_\",$E" + (rowNo + 1) + ")))");
			writeFormula(LoanConstants.REPAID_EVERY_FREQUENCY_COL, row, "IF(ISERROR(INDIRECT(CONCATENATE(\"REPAYMENT_FREQUENCY_\",$E"
					+ (rowNo + 1) + "))),\"\",INDIRECT(CONCATENATE(\"REPAYMENT_FREQUENCY_\",$E" + (rowNo + 1) + ")))");
			writeFormula(LoanConstants.NO_OF_REPAYMENTS_COL, row, "IF(ISERROR(INDIRECT(CONCATENATE(\"NO_REPAYMENT_\",$E" + (rowNo + 1)
					+ "))),\"\",INDIRECT(CONCATENATE(\"NO_REPAYMENT_\",$E" + (rowNo + 1) + ")))");
			writeFormula(LoanConstants.LOAN_TERM_COL, row, "IF(ISERROR($M" + (rowNo + 1) + "*$N" + (rowNo + 1) + "),\"\",$M"
					+ (rowNo + 1) + "*$N" + (rowNo + 1) + ")");
			writeFormula(LoanConstants.LOAN_TERM_FREQUENCY_COL, row, "$O" + (rowNo + 1));
			writeFormula(LoanConstants.NOMINAL_INTEREST_RATE_FREQUENCY_COL, row,
					"IF(ISERROR(INDIRECT(CONCATENATE(\"INTEREST_FREQUENCY_\",$E" + (rowNo + 1)
							+ "))),\"\",INDIRECT(CONCATENATE(\"INTEREST_FREQUENCY_\",$E" + (rowNo + 1) + ")))");
			writeFormula(LoanConstants.NOMINAL_INTEREST_RATE_COL, row, "IF(ISERROR(INDIRECT(CONCATENATE(\"INTEREST_\",$E"
					+ (rowNo + 1) + "))),\"\",INDIRECT(CONCATENATE(\"INTEREST_\",$E" + (rowNo + 1) + ")))");
			writeFormula(LoanConstants.AMORTIZATION_COL, row, "IF(ISERROR(INDIRECT(CONCATENATE(\"AMORTIZATION_\",$E" + (rowNo + 1)
					+ "))),\"\",INDIRECT(CONCATENATE(\"AMORTIZATION_\",$E" + (rowNo + 1) + ")))");
			writeFormula(LoanConstants.INTEREST_METHOD_COL, row, "IF(ISERROR(INDIRECT(CONCATENATE(\"INTEREST_TYPE_\",$E" + (rowNo + 1)
					+ "))),\"\",INDIRECT(CONCATENATE(\"INTEREST_TYPE_\",$E" + (rowNo + 1) + ")))");
			writeFormula(LoanConstants.INTEREST_CALCULATION_PERIOD_COL, row,
					"IF(ISERROR(INDIRECT(CONCATENATE(\"INTEREST_CALCULATION_\",$E" + (rowNo + 1)
							+ "))),\"\",INDIRECT(CONCATENATE(\"INTEREST_CALCULATION_\",$E" + (rowNo + 1) + ")))");
			writeFormula(LoanConstants.ARREARS_TOLERANCE_COL, row, "IF(ISERROR(INDIRECT(CONCATENATE(\"ARREARS_TOLERANCE_\",$E"
					+ (rowNo + 1) + "))),\"\",INDIRECT(CONCATENATE(\"ARREARS_TOLERANCE_\",$E" + (rowNo + 1) + ")))");
			writeFormula(LoanConstants.REPAYMENT_STRATEGY_COL, row, "IF(ISERROR(INDIRECT(CONCATENATE(\"STRATEGY_\",$E" + (rowNo + 1)
					+ "))),\"\",INDIRECT(CONCATENATE(\"STRATEGY_\",$E" + (rowNo + 1) + ")))");
			writeFormula(LoanConstants.GRACE_ON_PRINCIPAL_PAYMENT_COL, row, "IF(ISERROR(INDIRECT(CONCATENATE(\"GRACE_PRINCIPAL_\",$E"
					+ (rowNo + 1) + "))),\"\",INDIRECT(CONCATENATE(\"GRACE_PRINCIPAL_\",$E" + (rowNo + 1) + ")))");
			writeFormula(LoanConstants.GRACE_ON_INTEREST_PAYMENT_COL, row,
					"IF(ISERROR(INDIRECT(CONCATENATE(\"GRACE_INTEREST_PAYMENT_\",$E" + (rowNo + 1)
							+ "))),\"\",INDIRECT(CONCATENATE(\"GRACE_INTEREST_PAYMENT_\",$E" + (rowNo + 1) + ")))");
			writeFormula(LoanConstants.GRACE_ON_INTEREST_CHARGED_COL, row,
					"IF(ISERROR(INDIRECT(CONCATENATE(\"GRACE_INTEREST_CHARGED_\",$E" + (rowNo + 1)
							+ "))),\"\",INDIRECT(CONCATENATE(\"GRACE_INTEREST_CHARGED_\",$E" + (rowNo + 1) + ")))");

		}
	}

	private void setNames(Sheet worksheet) {
		Workbook loanWorkbook = worksheet.getWorkbook();
		List<String> officeNames = officeSheetPopulator.getOfficeNames();
		List<LoanProductData> products = productSheetPopulator.getProducts();

		// Office Names
		Name officeGroup = loanWorkbook.createName();
		officeGroup.setNameName("Office");
		officeGroup.setRefersToFormula(TemplatePopulateImportConstants.OFFICE_SHEET_NAME+"!$B$2:$B$" + (officeNames.size() + 1));

		// Client and Loan Officer Names for each office
		for (Integer i = 0; i < officeNames.size(); i++) {
			Integer[] officeNameToBeginEndIndexesOfClients = clientSheetPopulator
					.getOfficeNameToBeginEndIndexesOfClients().get(i);
			Integer[] officeNameToBeginEndIndexesOfStaff = personnelSheetPopulator
					.getOfficeNameToBeginEndIndexesOfStaff().get(i);
			Integer[] officeNameToBeginEndIndexesOfGroups = groupSheetPopulator.getOfficeNameToBeginEndIndexesOfGroups()
					.get(i);
			Name clientName = loanWorkbook.createName();
			Name loanOfficerName = loanWorkbook.createName();
			Name groupName = loanWorkbook.createName();

			if (officeNameToBeginEndIndexesOfStaff != null) {
				loanOfficerName.setNameName("Staff_" + officeNames.get(i).trim().replaceAll("[ )(]", "_"));
				loanOfficerName.setRefersToFormula(TemplatePopulateImportConstants.STAFF_SHEET_NAME+"!$B$" + officeNameToBeginEndIndexesOfStaff[0] + ":$B$"
						+ officeNameToBeginEndIndexesOfStaff[1]);
			}
			if (officeNameToBeginEndIndexesOfClients != null) {
				clientName.setNameName("Client_" + officeNames.get(i).trim().replaceAll("[ )(]", "_"));
				clientName.setRefersToFormula(TemplatePopulateImportConstants.CLIENT_SHEET_NAME+"!$B$" + officeNameToBeginEndIndexesOfClients[0] + ":$B$"
						+ officeNameToBeginEndIndexesOfClients[1]);
			}
			if (officeNameToBeginEndIndexesOfGroups != null) {
				groupName.setNameName("Group_" + officeNames.get(i).trim().replaceAll("[ )(]", "_"));
				groupName.setRefersToFormula(TemplatePopulateImportConstants.GROUP_SHEET_NAME+"!$B$" + officeNameToBeginEndIndexesOfGroups[0] + ":$B$"
						+ officeNameToBeginEndIndexesOfGroups[1]);
			}

		}

		// Product Name
		Name productGroup = loanWorkbook.createName();
		productGroup.setNameName("Products");
		productGroup.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$B$2:$B$" + (productSheetPopulator.getProductsSize() + 1));

		// Fund Name
		Name fundGroup = loanWorkbook.createName();
		fundGroup.setNameName("Funds");
		fundGroup.setRefersToFormula(TemplatePopulateImportConstants.EXTRAS_SHEET_NAME+"!$B$2:$B$" + (extrasSheetPopulator.getFundsSize() + 1));

		// Payment Type Name
		Name paymentTypeGroup = loanWorkbook.createName();
		paymentTypeGroup.setNameName("PaymentTypes");
		paymentTypeGroup.setRefersToFormula(TemplatePopulateImportConstants.EXTRAS_SHEET_NAME+"!$D$2:$D$" + (extrasSheetPopulator.getPaymentTypesSize() + 1));

		// Default Fund, Default Principal, Min Principal, Max Principal,
		// Default No. of Repayments, Min Repayments, Max Repayments, Repayment
		// Every,
		// Repayment Every Frequency, Interest Rate, Min Interest Rate, Max
		// Interest Rate, Interest Frequency, Amortization, Interest Type,
		// Interest Calculation Period, Transaction Processing Strategy, Arrears
		// Tolerance, GraceOnPrincipalPayment, GraceOnInterestPayment,
		// GraceOnInterestCharged, StartDate Names for each loan product
		for (Integer i = 0; i < products.size(); i++) {
			Name fundName = loanWorkbook.createName();
			Name principalName = loanWorkbook.createName();
			Name minPrincipalName = loanWorkbook.createName();
			Name maxPrincipalName = loanWorkbook.createName();
			Name noOfRepaymentName = loanWorkbook.createName();
			Name minNoOfRepayment = loanWorkbook.createName();
			Name maxNoOfRepaymentName = loanWorkbook.createName();
			Name repaymentEveryName = loanWorkbook.createName();
			Name repaymentFrequencyName = loanWorkbook.createName();
			Name interestName = loanWorkbook.createName();
			Name minInterestName = loanWorkbook.createName();
			Name maxInterestName = loanWorkbook.createName();
			Name interestFrequencyName = loanWorkbook.createName();
			Name amortizationName = loanWorkbook.createName();
			Name interestTypeName = loanWorkbook.createName();
			Name interestCalculationPeriodName = loanWorkbook.createName();
			Name transactionProcessingStrategyName = loanWorkbook.createName();
			Name arrearsToleranceName = loanWorkbook.createName();
			Name graceOnPrincipalPaymentName = loanWorkbook.createName();
			Name graceOnInterestPaymentName = loanWorkbook.createName();
			Name graceOnInterestChargedName = loanWorkbook.createName();
			Name startDateName = loanWorkbook.createName();
			String productName = products.get(i).getName().replaceAll("[ ]", "_");
			fundName.setNameName("FUND_" + productName);
			principalName.setNameName("PRINCIPAL_" + productName);
			minPrincipalName.setNameName("MIN_PRINCIPAL_" + productName);
			maxPrincipalName.setNameName("MAX_PRINCIPAL_" + productName);
			noOfRepaymentName.setNameName("NO_REPAYMENT_" + productName);
			minNoOfRepayment.setNameName("MIN_REPAYMENT_" + productName);
			maxNoOfRepaymentName.setNameName("MAX_REPAYMENT_" + productName);
			repaymentEveryName.setNameName("REPAYMENT_EVERY_" + productName);
			repaymentFrequencyName.setNameName("REPAYMENT_FREQUENCY_" + productName);
			interestName.setNameName("INTEREST_" + productName);
			minInterestName.setNameName("MIN_INTEREST_" + productName);
			maxInterestName.setNameName("MAX_INTEREST_" + productName);
			interestFrequencyName.setNameName("INTEREST_FREQUENCY_" + productName);
			amortizationName.setNameName("AMORTIZATION_" + productName);
			interestTypeName.setNameName("INTEREST_TYPE_" + productName);
			interestCalculationPeriodName.setNameName("INTEREST_CALCULATION_" + productName);
			transactionProcessingStrategyName.setNameName("STRATEGY_" + productName);
			arrearsToleranceName.setNameName("ARREARS_TOLERANCE_" + productName);
			graceOnPrincipalPaymentName.setNameName("GRACE_PRINCIPAL_" + productName);
			graceOnInterestPaymentName.setNameName("GRACE_INTEREST_PAYMENT_" + productName);
			graceOnInterestChargedName.setNameName("GRACE_INTEREST_CHARGED_" + productName);
			startDateName.setNameName("START_DATE_" + productName);
			if (products.get(i).getFundName() != null)
				fundName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$C$" + (i + 2));
			principalName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$D$" + (i + 2));
			minPrincipalName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$E$" + (i + 2));
			maxPrincipalName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$F$" + (i + 2));
			noOfRepaymentName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$G$" + (i + 2));
			minNoOfRepayment.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$H$" + (i + 2));
			maxNoOfRepaymentName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$I$" + (i + 2));
			repaymentEveryName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$J$" + (i + 2));
			repaymentFrequencyName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$K$" + (i + 2));
			interestName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$L$" + (i + 2));
			minInterestName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$M$" + (i + 2));
			maxInterestName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$N$" + (i + 2));
			interestFrequencyName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$O$" + (i + 2));
			amortizationName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$P$" + (i + 2));
			interestTypeName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$Q$" + (i + 2));
			interestCalculationPeriodName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$R$" + (i + 2));
			transactionProcessingStrategyName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$T$" + (i + 2));
			arrearsToleranceName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$S$" + (i + 2));
			graceOnPrincipalPaymentName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$U$" + (i + 2));
			graceOnInterestPaymentName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$V$" + (i + 2));
			graceOnInterestChargedName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$W$" + (i + 2));
			startDateName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$X$" + (i + 2));
		}
	}

}
