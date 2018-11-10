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
package org.apache.fineract.infrastructure.bulkimport.populator.guarantor;

import org.apache.fineract.infrastructure.bulkimport.constants.GuarantorConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.populator.AbstractWorkbookPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.ClientSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.OfficeSheetPopulator;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.portfolio.loanaccount.data.LoanAccountData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;
import org.apache.poi.hssf.usermodel.HSSFDataValidationHelper;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class GuarantorWorkbookPopulator extends AbstractWorkbookPopulator {
	private OfficeSheetPopulator officeSheetPopulator;
	private ClientSheetPopulator clientSheetPopulator;
	private List<LoanAccountData> loans;
	private List<SavingsAccountData> savings;
	private List<CodeValueData>guarantorRelationshipTypes;

	public GuarantorWorkbookPopulator(OfficeSheetPopulator officeSheetPopulator,
									  ClientSheetPopulator clientSheetPopulator,
									  List<LoanAccountData> loans, List<SavingsAccountData> savings,
									  List<CodeValueData> guarantorRelationshipTypes) {
		this.officeSheetPopulator = officeSheetPopulator;
		this.clientSheetPopulator = clientSheetPopulator;
		this.loans = loans;
		this.savings = savings;
		this.guarantorRelationshipTypes=guarantorRelationshipTypes;

	}

	@Override
	public void populate(Workbook workbook,String dateFormat) {
		Sheet addGuarantorSheet = workbook.createSheet(TemplatePopulateImportConstants.GUARANTOR_SHEET_NAME);
    	setLayout(addGuarantorSheet);
    	officeSheetPopulator.populate(workbook,dateFormat);
    	clientSheetPopulator.populate(workbook,dateFormat);
    	populateLoansTable(addGuarantorSheet,dateFormat);
    	populateSavingsTable(addGuarantorSheet,dateFormat);
    	populateGuarantorRelationshipTypes(addGuarantorSheet,dateFormat);
    	setRules(addGuarantorSheet);

	}



	private void setLayout(Sheet worksheet) {
		Row rowHeader = worksheet.createRow(0);
		 worksheet.setColumnWidth(GuarantorConstants.OFFICE_NAME_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
	        worksheet.setColumnWidth(GuarantorConstants.CLIENT_NAME_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
	        worksheet.setColumnWidth(GuarantorConstants.LOAN_ACCOUNT_NO_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
	        worksheet.setColumnWidth(GuarantorConstants.GUARANTO_TYPE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
	        worksheet.setColumnWidth(GuarantorConstants.CLIENT_RELATIONSHIP_TYPE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
	        worksheet.setColumnWidth(GuarantorConstants.ENTITY_OFFICE_NAME_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
	        worksheet.setColumnWidth(GuarantorConstants.ENTITY_ID_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
	        worksheet.setColumnWidth(GuarantorConstants.FIRST_NAME_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
	        worksheet.setColumnWidth(GuarantorConstants.LAST_NAME_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
	        worksheet.setColumnWidth(GuarantorConstants.ADDRESS_LINE_1_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
	        worksheet.setColumnWidth(GuarantorConstants.ADDRESS_LINE_2_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
	        worksheet.setColumnWidth(GuarantorConstants.CITY_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
	        worksheet.setColumnWidth(GuarantorConstants.DOB_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
	        worksheet.setColumnWidth(GuarantorConstants.ZIP_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
	        worksheet.setColumnWidth(GuarantorConstants.SAVINGS_ID_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
	        worksheet.setColumnWidth(GuarantorConstants.AMOUNT, TemplatePopulateImportConstants.SMALL_COL_SIZE);
	        worksheet.setColumnWidth(GuarantorConstants.LOOKUP_CLIENT_NAME_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
	        worksheet.setColumnWidth(GuarantorConstants.LOOKUP_ACCOUNT_NO_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
	        worksheet.setColumnWidth(GuarantorConstants.LOOKUP_SAVINGS_CLIENT_NAME_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
	        worksheet.setColumnWidth(GuarantorConstants.LOOKUP_SAVINGS_ACCOUNT_NO_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
	        writeString(GuarantorConstants.OFFICE_NAME_COL, rowHeader, "Office Name*");
	        writeString(GuarantorConstants.CLIENT_NAME_COL, rowHeader, "Client Name*");
	        writeString(GuarantorConstants.LOAN_ACCOUNT_NO_COL, rowHeader, " Loan Account NO");
	        writeString(GuarantorConstants.GUARANTO_TYPE_COL, rowHeader, "Guranter_type*");
	        writeString(GuarantorConstants.CLIENT_RELATIONSHIP_TYPE_COL, rowHeader, "Client Relationship type*");
	        writeString(GuarantorConstants.ENTITY_OFFICE_NAME_COL, rowHeader, "Guranter office");
	        writeString(GuarantorConstants.ENTITY_ID_COL, rowHeader, "Gurantor client id*");
	        writeString(GuarantorConstants.FIRST_NAME_COL, rowHeader, "First Name*");
	        writeString(GuarantorConstants.LAST_NAME_COL, rowHeader, "Last Name");
	        writeString(GuarantorConstants.ADDRESS_LINE_1_COL, rowHeader, "ADDRESS LINE 1");
	        writeString(GuarantorConstants.ADDRESS_LINE_2_COL, rowHeader, "ADDRESS LINE 2");
	        writeString(GuarantorConstants.CITY_COL, rowHeader, "City");
	        writeString(GuarantorConstants.DOB_COL, rowHeader, "Date of Birth");
	        writeString(GuarantorConstants.ZIP_COL, rowHeader, "Zip*");
	        writeString(GuarantorConstants.SAVINGS_ID_COL, rowHeader, "Savings Account Id");
	        writeString(GuarantorConstants.AMOUNT, rowHeader, "Amount");
	        writeString(GuarantorConstants.LOOKUP_CLIENT_NAME_COL, rowHeader, "Lookup Client");
	        writeString(GuarantorConstants.LOOKUP_ACCOUNT_NO_COL, rowHeader, "Lookup Loan Account");
	        writeString(GuarantorConstants.LOOKUP_SAVINGS_CLIENT_NAME_COL, rowHeader, "Savings Lookup Client");
	        writeString(GuarantorConstants.LOOKUP_SAVINGS_ACCOUNT_NO_COL, rowHeader, "Savings Lookup Account");
	
	}
    private void populateSavingsTable(Sheet addGuarantorSheet,String dateFormat) {
        Workbook workbook = addGuarantorSheet.getWorkbook();
        CellStyle dateCellStyle = workbook.createCellStyle();
        short df = workbook.createDataFormat().getFormat(dateFormat);
        dateCellStyle.setDataFormat(df);
        int rowIndex = 1;
        Row row;
        Collections.sort(savings, SavingsAccountData.ClientNameComparator);
            for(SavingsAccountData savingsAccount : savings) {
                if(addGuarantorSheet.getRow(rowIndex)==null) {
                    row = addGuarantorSheet.createRow(rowIndex++);
                }
                else {
                 row=addGuarantorSheet.getRow(rowIndex++);
                }
                writeString(GuarantorConstants.LOOKUP_SAVINGS_CLIENT_NAME_COL, row, savingsAccount.getClientName()  + "(" + savingsAccount.getClientId() + ")");
                writeLong(GuarantorConstants.LOOKUP_SAVINGS_ACCOUNT_NO_COL, row, Long.parseLong(savingsAccount.getAccountNo()));
            }

    }
    private void populateLoansTable(Sheet addGuarantorSheet,String dateFormat) {
        Workbook workbook = addGuarantorSheet.getWorkbook();
        CellStyle dateCellStyle = workbook.createCellStyle();
        short df = workbook.createDataFormat().getFormat(dateFormat);
        dateCellStyle.setDataFormat(df);
        int rowIndex = 1;
        Row row;
        Collections.sort(loans, LoanAccountData.ClientNameComparator);
            for(LoanAccountData loan : loans) {
                if(addGuarantorSheet.getRow(rowIndex)==null){
                    row = addGuarantorSheet.createRow(rowIndex++);
                }
                else{
                    row= addGuarantorSheet.getRow(rowIndex++);
                }
                writeString(GuarantorConstants.LOOKUP_CLIENT_NAME_COL, row, loan.getClientName() + "(" + loan.getClientId() + ")");
                writeString(GuarantorConstants.LOOKUP_ACCOUNT_NO_COL, row, Long.parseLong(loan.getAccountNo())+"-"+loan.getStatusStringValue());
            }
    }
	private void populateGuarantorRelationshipTypes(Sheet addGuarantorSheet, String dateFormat) {
		Workbook workbook = addGuarantorSheet.getWorkbook();
		CellStyle dateCellStyle = workbook.createCellStyle();
		short df = workbook.createDataFormat().getFormat(dateFormat);
		dateCellStyle.setDataFormat(df);
		int rowIndex = 1;
		Row row;
		for (CodeValueData relationshipType:guarantorRelationshipTypes) {
			if (addGuarantorSheet.getRow(rowIndex)==null){
				row=addGuarantorSheet.createRow(rowIndex++);
			}else {
				row=addGuarantorSheet.getRow(rowIndex++);
			}
			writeString(GuarantorConstants.LOOKUP_GUARANTOR_RELATIONSHIPS,row,relationshipType.getName()+"-"+relationshipType.getId());
		}

	}
	private void setRules(Sheet worksheet) {

    		CellRangeAddressList officeNameRange = new  CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
					GuarantorConstants.OFFICE_NAME_COL, GuarantorConstants.OFFICE_NAME_COL);
        	CellRangeAddressList clientNameRange = new  CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
					GuarantorConstants.CLIENT_NAME_COL, GuarantorConstants.CLIENT_NAME_COL);
        	CellRangeAddressList entityofficeNameRange = new  CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
					GuarantorConstants.ENTITY_OFFICE_NAME_COL, GuarantorConstants.ENTITY_OFFICE_NAME_COL);
        	CellRangeAddressList entityclientNameRange = new  CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
					GuarantorConstants.ENTITY_ID_COL, GuarantorConstants.ENTITY_ID_COL);
        	CellRangeAddressList accountNumberRange = new  CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
					GuarantorConstants.LOAN_ACCOUNT_NO_COL, GuarantorConstants.LOAN_ACCOUNT_NO_COL);
        	CellRangeAddressList savingsaccountNumberRange = new  CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
					GuarantorConstants.SAVINGS_ID_COL, GuarantorConstants.SAVINGS_ID_COL);
        	CellRangeAddressList guranterTypeRange = new  CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
					GuarantorConstants.GUARANTO_TYPE_COL, GuarantorConstants.GUARANTO_TYPE_COL);
			CellRangeAddressList guranterRelationshipTypeRange = new  CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
				GuarantorConstants.CLIENT_RELATIONSHIP_TYPE_COL, GuarantorConstants.CLIENT_RELATIONSHIP_TYPE_COL);
        	
        	DataValidationHelper validationHelper = new HSSFDataValidationHelper((HSSFSheet)worksheet);
        	
        	setNames(worksheet);
        	
        	DataValidationConstraint officeNameConstraint = validationHelper.createFormulaListConstraint("Office");
        	DataValidationConstraint clientNameConstraint = validationHelper.createFormulaListConstraint("INDIRECT(CONCATENATE(\"Client_\",$A1))");
        	DataValidationConstraint accountNumberConstraint = validationHelper.createFormulaListConstraint("INDIRECT(CONCATENATE(\"Account_\",SUBSTITUTE(SUBSTITUTE(SUBSTITUTE($B1,\" \",\"_\"),\"(\",\"_\"),\")\",\"_\")))");
        	DataValidationConstraint savingsaccountNumberConstraint = validationHelper.createFormulaListConstraint("INDIRECT(CONCATENATE(\"SavingsAccount_\",SUBSTITUTE(SUBSTITUTE(SUBSTITUTE($G1,\" \",\"_\"),\"(\",\"_\"),\")\",\"_\")))");
        	DataValidationConstraint guranterTypeConstraint = validationHelper.createExplicitListConstraint(new String[] {
        			TemplatePopulateImportConstants.GUARANTOR_INTERNAL,
					TemplatePopulateImportConstants.GUARANTOR_EXTERNAL});
			DataValidationConstraint guarantorRelationshipConstraint = validationHelper.createFormulaListConstraint("GuarantorRelationship");
        	DataValidationConstraint entityofficeNameConstraint = validationHelper.createFormulaListConstraint("Office");
        	DataValidationConstraint entityclientNameConstraint = validationHelper.createFormulaListConstraint("INDIRECT(CONCATENATE(\"Client_\",$F1))");
    	
        	DataValidation officeValidation = validationHelper.createValidation(officeNameConstraint, officeNameRange);
        	DataValidation clientValidation = validationHelper.createValidation(clientNameConstraint, clientNameRange);
        	DataValidation accountNumberValidation = validationHelper.createValidation(accountNumberConstraint, accountNumberRange);
        	DataValidation savingsaccountNumberValidation = validationHelper.createValidation(savingsaccountNumberConstraint, savingsaccountNumberRange);
        	DataValidation guranterTypeValidation = validationHelper.createValidation(guranterTypeConstraint, guranterTypeRange);
        	DataValidation guarantorRelationshipValidation=validationHelper.createValidation(guarantorRelationshipConstraint,guranterRelationshipTypeRange);
        	DataValidation entityofficeValidation = validationHelper.createValidation(entityofficeNameConstraint, entityofficeNameRange);
        	DataValidation entityclientValidation = validationHelper.createValidation(entityclientNameConstraint, entityclientNameRange);
    	
        	
        	worksheet.addValidationData(officeValidation);
            worksheet.addValidationData(clientValidation);
            worksheet.addValidationData(accountNumberValidation);
            worksheet.addValidationData(guranterTypeValidation);
            worksheet.addValidationData(guarantorRelationshipValidation);
            worksheet.addValidationData(entityofficeValidation);
            worksheet.addValidationData(entityclientValidation);
            worksheet.addValidationData(savingsaccountNumberValidation);
    	
	}
	private void setNames(Sheet worksheet) {
    	Workbook addGurarantorWorkbook = worksheet.getWorkbook();
    	ArrayList<String> officeNames = new ArrayList<String>(officeSheetPopulator.getOfficeNames());
    	
    	//Office Names
		Name officeGroup = addGurarantorWorkbook.createName();
		officeGroup.setNameName("Office");
		officeGroup.setRefersToFormula(TemplatePopulateImportConstants.OFFICE_SHEET_NAME+"!$B$2:$B$" + (officeNames.size() + 1));

		//GurantorRelationshipTypes Names
		Name guarantorRelationshipsGroup = addGurarantorWorkbook.createName();
		guarantorRelationshipsGroup.setNameName("GuarantorRelationship");
		guarantorRelationshipsGroup.setRefersToFormula(TemplatePopulateImportConstants.GUARANTOR_SHEET_NAME+"!$CH$2:$CH$" + (guarantorRelationshipTypes.size() + 1));
    	
    	//Clients Named after Offices
    	for(Integer i = 0; i < officeNames.size(); i++) {
    		Integer[] officeNameToBeginEndIndexesOfClients = clientSheetPopulator.getOfficeNameToBeginEndIndexesOfClients().get(i);
    		Name name = addGurarantorWorkbook.createName();
    		if(officeNameToBeginEndIndexesOfClients != null) {
    	       name.setNameName("Client_" + officeNames.get(i).trim().replaceAll("[ )(]", "_"));
    	       name.setRefersToFormula(TemplatePopulateImportConstants.CLIENT_SHEET_NAME+"!$B$" + officeNameToBeginEndIndexesOfClients[0] +
					   ":$B$" + officeNameToBeginEndIndexesOfClients[1]);
    		}
    	}
    	
    	//Counting clients with active loans and starting and end addresses of cells
    	HashMap<String, Integer[]> clientNameToBeginEndIndexes = new HashMap<String, Integer[]>();
    	ArrayList<String> clientsWithActiveLoans = new ArrayList<String>();
    	ArrayList<String> clientIdsWithActiveLoans = new ArrayList<String>();
    	int startIndex = 1, endIndex = 1;
    	String clientName = "";
    	String clientId = "";
    	for(int i = 0; i < loans.size(); i++){
    		if(!clientName.equals(loans.get(i).getClientName())) {
    			endIndex = i + 1;
    			clientNameToBeginEndIndexes.put(clientName, new Integer[]{startIndex, endIndex});
    			startIndex = i + 2;
    			clientName = loans.get(i).getClientName();
    			clientId = loans.get(i).getClientId().toString();
    			clientsWithActiveLoans.add(clientName);
    			clientIdsWithActiveLoans.add(clientId);
    		}
    		if(i == loans.size()-1) {
    			endIndex = i + 2;
    			clientNameToBeginEndIndexes.put(clientName, new Integer[]{startIndex, endIndex});
    		}
    	}
    	
    	//Account Number Named  after Clients
    	for(int j = 0; j < clientsWithActiveLoans.size(); j++) {
    		Name name = addGurarantorWorkbook.createName();
    		name.setNameName("Account_" + clientsWithActiveLoans.get(j).replaceAll(" ", "_") + "_" + clientIdsWithActiveLoans.get(j) + "_");
    		name.setRefersToFormula(TemplatePopulateImportConstants.GUARANTOR_SHEET_NAME+"!$CE$" + clientNameToBeginEndIndexes.get(clientsWithActiveLoans.get(j))[0] +
					":$CE$" + clientNameToBeginEndIndexes.get(clientsWithActiveLoans.get(j))[1]);
    	}
    	///savings
    	//Counting clients with active savings and starting and end addresses of cells for naming
    	ArrayList<String> clientsWithActiveSavings = new ArrayList<String>();
    	ArrayList<String> clientIdsWithActiveSavings = new ArrayList<String>();
    	clientName="";
    	clientId="";
    	for(int i = 0; i < savings.size(); i++){
    		if(!clientName.equals(savings.get(i).getClientName())) {
    			endIndex = i + 1;
    			clientNameToBeginEndIndexes.put(clientName, new Integer[]{startIndex, endIndex});
    			startIndex = i + 2;
    			clientName = savings.get(i).getClientName();
    			clientId = savings.get(i).getClientId().toString();
    			clientsWithActiveSavings.add(clientName);
    			clientIdsWithActiveSavings.add(clientId);
    		}
    		if(i == savings.size()-1) {
    			endIndex = i + 2;
    			clientNameToBeginEndIndexes.put(clientName, new Integer[]{startIndex, endIndex});
    		}
    	}
    	//Account Number Named  after Clients
    	for(int j = 0; j < clientsWithActiveSavings.size(); j++) {
    		Name name = addGurarantorWorkbook.createName();
    		name.setNameName("SavingsAccount_" + clientsWithActiveSavings.get(j).replaceAll(" ", "_") + "_" + clientIdsWithActiveSavings.get(j) + "_");
    		name.setRefersToFormula(TemplatePopulateImportConstants.GUARANTOR_SHEET_NAME+"!$CG$" + clientNameToBeginEndIndexes.get(clientsWithActiveSavings.get(j))[0] +
					":$CG$" + clientNameToBeginEndIndexes.get(clientsWithActiveSavings.get(j))[1]);
    	}
  	
	}

}