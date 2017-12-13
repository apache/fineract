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
package org.apache.fineract.infrastructure.bulkimport.populator.centers;

import org.apache.fineract.infrastructure.bulkimport.constants.CenterConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.populator.AbstractWorkbookPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.GroupSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.OfficeSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.PersonnelSheetPopulator;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.poi.hssf.usermodel.HSSFDataValidationHelper;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;

import java.util.List;


public class CentersWorkbookPopulator extends AbstractWorkbookPopulator {


	private OfficeSheetPopulator officeSheetPopulator;
	private PersonnelSheetPopulator personnelSheetPopulator;
	private GroupSheetPopulator groupSheetPopulator;

	public CentersWorkbookPopulator(OfficeSheetPopulator officeSheetPopulator,
			PersonnelSheetPopulator personnelSheetPopulator,GroupSheetPopulator groupSheetPopulator) {
		this.officeSheetPopulator = officeSheetPopulator;
		this.personnelSheetPopulator = personnelSheetPopulator;
		this.groupSheetPopulator=groupSheetPopulator;
	}

	@Override
	public void populate(Workbook workbook,String dateFormat) {
		Sheet centerSheet = workbook.createSheet(TemplatePopulateImportConstants.CENTER_SHEET_NAME);
		personnelSheetPopulator.populate(workbook,dateFormat);
		officeSheetPopulator.populate(workbook,dateFormat);
		groupSheetPopulator.populate(workbook,dateFormat);
		setLayout(centerSheet);
		setLookupTable(centerSheet,dateFormat);
		setRules(centerSheet,dateFormat);
	}
	

	private void setLayout(Sheet worksheet) {
		Row rowHeader = worksheet.createRow(0);
		rowHeader.setHeight(TemplatePopulateImportConstants.ROW_HEADER_HEIGHT);
		worksheet.setColumnWidth(CenterConstants.CENTER_NAME_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(CenterConstants.OFFICE_NAME_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
		worksheet.setColumnWidth(CenterConstants.STAFF_NAME_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
		worksheet.setColumnWidth(CenterConstants.EXTERNAL_ID_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(CenterConstants.ACTIVE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(CenterConstants.ACTIVATION_DATE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(CenterConstants.SUBMITTED_ON_DATE_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(CenterConstants.MEETING_START_DATE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(CenterConstants.IS_REPEATING_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(CenterConstants.FREQUENCY_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(CenterConstants.INTERVAL_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(CenterConstants.REPEATS_ON_DAY_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(CenterConstants.STATUS_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(CenterConstants.CENTER_ID_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(CenterConstants.FAILURE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(CenterConstants.GROUP_NAMES_STARTING_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(CenterConstants.LOOKUP_OFFICE_NAME_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
		worksheet.setColumnWidth(CenterConstants.LOOKUP_OFFICE_OPENING_DATE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(CenterConstants.LOOKUP_REPEAT_NORMAL_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(CenterConstants.LOOKUP_REPEAT_MONTHLY_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(CenterConstants.LOOKUP_IF_REPEAT_WEEKLY_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);

		writeString(CenterConstants.CENTER_NAME_COL, rowHeader, "Center Name*");
		writeString(CenterConstants.OFFICE_NAME_COL, rowHeader, "Office Name*");
		writeString(CenterConstants.STAFF_NAME_COL, rowHeader, "Staff Name*");
		writeString(CenterConstants.EXTERNAL_ID_COL, rowHeader, "External ID");
		writeString(CenterConstants.ACTIVE_COL, rowHeader, "Active*");
		writeString(CenterConstants.ACTIVATION_DATE_COL, rowHeader, "Activation Date*");
		writeString(CenterConstants.SUBMITTED_ON_DATE_COL,rowHeader,"Submitted On Date");
		writeString(CenterConstants.MEETING_START_DATE_COL, rowHeader, "Meeting Start Date* (On or After)");
		writeString(CenterConstants.IS_REPEATING_COL, rowHeader, "Repeat*");
		writeString(CenterConstants.FREQUENCY_COL, rowHeader, "Frequency*");
		writeString(CenterConstants.INTERVAL_COL, rowHeader, "Interval*");
		writeString(CenterConstants.REPEATS_ON_DAY_COL, rowHeader, "Repeats On*");
		writeString(CenterConstants.GROUP_NAMES_STARTING_COL,rowHeader,"Group Names* (Enter in consecutive cells horizontally)");
		writeString(CenterConstants.LOOKUP_OFFICE_NAME_COL, rowHeader, "Office Name");
		writeString(CenterConstants.LOOKUP_OFFICE_OPENING_DATE_COL, rowHeader, "Opening Date");
		writeString(CenterConstants.LOOKUP_REPEAT_NORMAL_COL, rowHeader, "Repeat Normal Range");
		writeString(CenterConstants.LOOKUP_REPEAT_MONTHLY_COL, rowHeader, "Repeat Monthly Range");
		writeString(CenterConstants.LOOKUP_IF_REPEAT_WEEKLY_COL, rowHeader, "If Repeat Weekly Range");
	}
	private void setLookupTable(Sheet centerSheet,String dateFormat) {
		setOfficeDateLookupTable(centerSheet, officeSheetPopulator.getOffices(), CenterConstants.LOOKUP_OFFICE_NAME_COL,CenterConstants.LOOKUP_OFFICE_OPENING_DATE_COL,dateFormat);
    	int rowIndex;
    	for(rowIndex = 1; rowIndex <= 11; rowIndex++) {
    		Row row = centerSheet.getRow(rowIndex);
    		if(row == null)
    			row = centerSheet.createRow(rowIndex);
    		writeInt(CenterConstants.LOOKUP_REPEAT_MONTHLY_COL, row, rowIndex);
    	}
    	for(rowIndex = 1; rowIndex <= 3; rowIndex++) 
    		writeInt(CenterConstants.LOOKUP_REPEAT_NORMAL_COL, centerSheet.getRow(rowIndex), rowIndex);

    	String[] days = new String[]{
    			TemplatePopulateImportConstants.MONDAY,
				TemplatePopulateImportConstants.TUESDAY,
				TemplatePopulateImportConstants.WEDNESDAY,
				TemplatePopulateImportConstants.THURSDAY,
				TemplatePopulateImportConstants.FRIDAY,
				TemplatePopulateImportConstants.SATURDAY,
				TemplatePopulateImportConstants.SUNDAY};

    	for(rowIndex = 1; rowIndex <= 7; rowIndex++)
    		writeString(CenterConstants.LOOKUP_IF_REPEAT_WEEKLY_COL, centerSheet.getRow(rowIndex), days[rowIndex-1]);
		
	}
	private void setRules(Sheet worksheet,String dateFormat) {
    	CellRangeAddressList officeNameRange = new  CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(), CenterConstants.OFFICE_NAME_COL,CenterConstants. OFFICE_NAME_COL);
    	CellRangeAddressList staffNameRange = new  CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(), CenterConstants.STAFF_NAME_COL,CenterConstants. STAFF_NAME_COL);
    	CellRangeAddressList activationDateRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),CenterConstants. ACTIVATION_DATE_COL,CenterConstants. ACTIVATION_DATE_COL);
    	CellRangeAddressList activeRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(), CenterConstants.ACTIVE_COL, CenterConstants.ACTIVE_COL);
		CellRangeAddressList submittedDateRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),CenterConstants. SUBMITTED_ON_DATE_COL,CenterConstants.SUBMITTED_ON_DATE_COL);
    	CellRangeAddressList meetingStartDateRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(), CenterConstants.MEETING_START_DATE_COL,CenterConstants. MEETING_START_DATE_COL);
    	CellRangeAddressList isRepeatRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),CenterConstants. IS_REPEATING_COL,CenterConstants. IS_REPEATING_COL);
    	CellRangeAddressList repeatsRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(), CenterConstants.FREQUENCY_COL, CenterConstants.FREQUENCY_COL);
    	CellRangeAddressList repeatsEveryRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(), CenterConstants.INTERVAL_COL,CenterConstants. INTERVAL_COL);
    	CellRangeAddressList repeatsOnRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(), CenterConstants.REPEATS_ON_DAY_COL,CenterConstants. REPEATS_ON_DAY_COL);
    	
    	
    	DataValidationHelper validationHelper = new HSSFDataValidationHelper((HSSFSheet)worksheet);
    	List<OfficeData> offices = officeSheetPopulator.getOffices();
    	setNames(worksheet, offices);
    	

    	DataValidationConstraint officeNameConstraint = validationHelper.createFormulaListConstraint("Office");
    	DataValidationConstraint staffNameConstraint = validationHelper.
				createFormulaListConstraint("INDIRECT(CONCATENATE(\"Staff_\",$B1))");
    	DataValidationConstraint activationDateConstraint = validationHelper.createDateConstraint
				(DataValidationConstraint.OperatorType.BETWEEN, "=VLOOKUP($B1,$IR$2:$IS" + (offices.size() + 1)+",2,FALSE)",
						"=TODAY()", dateFormat);
    	DataValidationConstraint booleanConstraint = validationHelper.createExplicitListConstraint(new String[]{"True", "False"});
		DataValidationConstraint submittedOnDateConstraint =
				validationHelper.createDateConstraint(DataValidationConstraint.OperatorType.LESS_OR_EQUAL,
						"=$F1", null,dateFormat);
    	DataValidationConstraint meetingStartDateConstraint =
				validationHelper.createDateConstraint(DataValidationConstraint.OperatorType.BETWEEN,
						"=$F1", "=TODAY()", dateFormat);
    	DataValidationConstraint repeatsConstraint =
				validationHelper.createExplicitListConstraint(new String[]{
						TemplatePopulateImportConstants.FREQUENCY_DAILY,
						TemplatePopulateImportConstants.FREQUENCY_WEEKLY,
						TemplatePopulateImportConstants.FREQUENCY_MONTHLY,
						TemplatePopulateImportConstants.FREQUENCY_YEARLY});
    	DataValidationConstraint repeatsEveryConstraint = validationHelper.createFormulaListConstraint("INDIRECT($J1)");
    	DataValidationConstraint repeatsOnConstraint = validationHelper.createFormulaListConstraint("INDIRECT(CONCATENATE($J1,\"_DAYS\"))");


    	DataValidation officeValidation = validationHelper.createValidation(officeNameConstraint, officeNameRange);
    	DataValidation staffValidation = validationHelper.createValidation(staffNameConstraint, staffNameRange);
    	DataValidation activationDateValidation = validationHelper.createValidation(activationDateConstraint, activationDateRange);
    	DataValidation activeValidation = validationHelper.createValidation(booleanConstraint, activeRange);
    	DataValidation submittedOnValidation=validationHelper.createValidation(submittedOnDateConstraint,submittedDateRange);
    	DataValidation meetingStartDateValidation = validationHelper.createValidation(meetingStartDateConstraint, meetingStartDateRange);
    	DataValidation isRepeatValidation = validationHelper.createValidation(booleanConstraint, isRepeatRange);
    	DataValidation repeatsValidation = validationHelper.createValidation(repeatsConstraint, repeatsRange);
    	DataValidation repeatsEveryValidation = validationHelper.createValidation(repeatsEveryConstraint, repeatsEveryRange);
    	DataValidation repeatsOnValidation = validationHelper.createValidation(repeatsOnConstraint, repeatsOnRange);
    	

    	worksheet.addValidationData(activeValidation);
        worksheet.addValidationData(officeValidation);
        worksheet.addValidationData(staffValidation);
        worksheet.addValidationData(activationDateValidation);
        worksheet.addValidationData(submittedOnValidation);
        worksheet.addValidationData(meetingStartDateValidation);
        worksheet.addValidationData(isRepeatValidation);
        worksheet.addValidationData(repeatsValidation);
        worksheet.addValidationData(repeatsEveryValidation);
        worksheet.addValidationData(repeatsOnValidation);
	}
	
	private void setNames(Sheet worksheet, List<OfficeData> offices) {
    	Workbook centerWorkbook = worksheet.getWorkbook();
    	Name officeCenter = centerWorkbook.createName();
    	officeCenter.setNameName("Office");
    	officeCenter.setRefersToFormula(TemplatePopulateImportConstants.OFFICE_SHEET_NAME +"!$B$2:$B$" + (offices.size() + 1));
    	
    	
    	//Repeat constraint names
    	Name repeatsDaily = centerWorkbook.createName();
    	repeatsDaily.setNameName("Daily");
    	repeatsDaily.setRefersToFormula(TemplatePopulateImportConstants.CENTER_SHEET_NAME+"!$IT$2:$IT$4");
    	Name repeatsWeekly = centerWorkbook.createName();
    	repeatsWeekly.setNameName("Weekly");
    	repeatsWeekly.setRefersToFormula(TemplatePopulateImportConstants.CENTER_SHEET_NAME+"!$IT$2:$IT$4");
    	Name repeatYearly = centerWorkbook.createName();
    	repeatYearly.setNameName("Yearly");
    	repeatYearly.setRefersToFormula(TemplatePopulateImportConstants.CENTER_SHEET_NAME+"!$IT$2:$IT$4");
    	Name repeatsMonthly = centerWorkbook.createName();
    	repeatsMonthly.setNameName("Monthly");
    	repeatsMonthly.setRefersToFormula(TemplatePopulateImportConstants.CENTER_SHEET_NAME+"!$IU$2:$IU$12");
    	Name repeatsOnWeekly = centerWorkbook.createName();
    	repeatsOnWeekly.setNameName("Weekly_Days");
    	repeatsOnWeekly.setRefersToFormula(TemplatePopulateImportConstants.CENTER_SHEET_NAME+"!$IV$2:$IV$8");
    	
    	
    	//Staff Names for each office
    	for(Integer i = 0; i < offices.size(); i++) {
    		Integer[] officeNameToBeginEndIndexesOfStaff = personnelSheetPopulator.getOfficeNameToBeginEndIndexesOfStaff().get(i);
    		Name loanOfficerName = centerWorkbook.createName();
    		 if(officeNameToBeginEndIndexesOfStaff != null) {
    	        loanOfficerName.setNameName("Staff_" + offices.get(i).name().trim().replaceAll("[ )(]", "_"));
    	        loanOfficerName.setRefersToFormula(TemplatePopulateImportConstants.STAFF_SHEET_NAME+"!$B$" + officeNameToBeginEndIndexesOfStaff[0] + ":$B$" + officeNameToBeginEndIndexesOfStaff[1]);
    		 }
    	}
		
	}
	
	
}