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
package org.apache.fineract.infrastructure.bulkimport.populator.group;

import org.apache.fineract.infrastructure.bulkimport.constants.GroupConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.populator.*;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.poi.hssf.usermodel.HSSFDataValidationHelper;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;

import java.util.List;

public class GroupsWorkbookPopulator extends AbstractWorkbookPopulator {

	private OfficeSheetPopulator officeSheetPopulator;
	private PersonnelSheetPopulator personnelSheetPopulator;
	private CenterSheetPopulator centerSheetPopulator;
	private ClientSheetPopulator clientSheetPopulator;

	public GroupsWorkbookPopulator(OfficeSheetPopulator officeSheetPopulator,
			PersonnelSheetPopulator personnelSheetPopulator, CenterSheetPopulator centerSheetPopulator,
			ClientSheetPopulator clientSheetPopulator) {
		this.officeSheetPopulator = officeSheetPopulator;
		this.personnelSheetPopulator = personnelSheetPopulator;
		this.centerSheetPopulator = centerSheetPopulator;
		this.clientSheetPopulator = clientSheetPopulator;
	}

	@Override
	public void populate(Workbook workbook,String dateFormat) {
		Sheet groupSheet = workbook.createSheet(TemplatePopulateImportConstants.GROUP_SHEET_NAME);
		personnelSheetPopulator.populate(workbook,dateFormat);
		officeSheetPopulator.populate(workbook,dateFormat);
		centerSheetPopulator.populate(workbook,dateFormat);
		clientSheetPopulator.populate(workbook,dateFormat);
		setLayout(groupSheet);
		setLookupTable(groupSheet,dateFormat);
		setRules(groupSheet,dateFormat);

	}

	private void setLayout(Sheet worksheet) {
		Row rowHeader = worksheet.createRow(TemplatePopulateImportConstants.ROWHEADER_INDEX);
		rowHeader.setHeight(TemplatePopulateImportConstants.ROW_HEADER_HEIGHT);
		worksheet.setColumnWidth(GroupConstants.NAME_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(GroupConstants.OFFICE_NAME_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
		worksheet.setColumnWidth(GroupConstants.STAFF_NAME_COL,  TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
		worksheet.setColumnWidth(GroupConstants.CENTER_NAME_COL,  TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
		worksheet.setColumnWidth(GroupConstants.EXTERNAL_ID_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(GroupConstants.ACTIVE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(GroupConstants.ACTIVATION_DATE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(GroupConstants.SUBMITTED_ON_DATE_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(GroupConstants.MEETING_START_DATE_COL,  TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
		worksheet.setColumnWidth(GroupConstants.IS_REPEATING_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(GroupConstants.FREQUENCY_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(GroupConstants.INTERVAL_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(GroupConstants.REPEATS_ON_DAY_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(GroupConstants.STATUS_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(GroupConstants.GROUP_ID_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(GroupConstants.FAILURE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(GroupConstants.CLIENT_NAMES_STARTING_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(GroupConstants.LOOKUP_OFFICE_NAME_COL,  TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
		worksheet.setColumnWidth(GroupConstants.LOOKUP_OFFICE_OPENING_DATE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(GroupConstants.LOOKUP_REPEAT_NORMAL_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(GroupConstants.LOOKUP_REPEAT_MONTHLY_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(GroupConstants.LOOKUP_IF_REPEAT_WEEKLY_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);

		writeString(GroupConstants.NAME_COL, rowHeader, "Group Name*");
		writeString(GroupConstants.OFFICE_NAME_COL, rowHeader, "Office Name*");
		writeString(GroupConstants.STAFF_NAME_COL, rowHeader, "Staff Name*");
		writeString(GroupConstants.CENTER_NAME_COL, rowHeader, "Center Name");
		writeString(GroupConstants.EXTERNAL_ID_COL, rowHeader, "External ID");
		writeString(GroupConstants.ACTIVE_COL, rowHeader, "Active*");
		writeString(GroupConstants.ACTIVATION_DATE_COL, rowHeader, "Activation Date*");
		writeString(GroupConstants.SUBMITTED_ON_DATE_COL,rowHeader,"Submitted On Date *");
		writeString(GroupConstants.MEETING_START_DATE_COL, rowHeader, "Meeting Start Date* (On or After)");
		writeString(GroupConstants.IS_REPEATING_COL, rowHeader, "Repeat*");
		writeString(GroupConstants.FREQUENCY_COL, rowHeader, "Frequency*");
		writeString(GroupConstants.INTERVAL_COL, rowHeader, "Interval*");
		writeString(GroupConstants.REPEATS_ON_DAY_COL, rowHeader, "Repeats On*");
		writeString(GroupConstants.CLIENT_NAMES_STARTING_COL, rowHeader, "Client Names* (Enter in consecutive cells horizontally)");
		writeString(GroupConstants.LOOKUP_OFFICE_NAME_COL, rowHeader, "Office Name");
		writeString(GroupConstants.LOOKUP_OFFICE_OPENING_DATE_COL, rowHeader, "Opening Date");
		writeString(GroupConstants.LOOKUP_REPEAT_NORMAL_COL, rowHeader, "Repeat Normal Range");
		writeString(GroupConstants.LOOKUP_REPEAT_MONTHLY_COL, rowHeader, "Repeat Monthly Range");
		writeString(GroupConstants.LOOKUP_IF_REPEAT_WEEKLY_COL, rowHeader, "If Repeat Weekly Range");

	}
    private void setLookupTable(Sheet groupSheet,String dateFormat) {
    	setOfficeDateLookupTable(groupSheet, officeSheetPopulator.getOffices(),GroupConstants.LOOKUP_OFFICE_NAME_COL,
				GroupConstants. LOOKUP_OFFICE_OPENING_DATE_COL,dateFormat);
    	int rowIndex;
    	for(rowIndex = 1; rowIndex <= 11; rowIndex++) {
    		Row row = groupSheet.getRow(rowIndex);
    		if(row == null)
    			row = groupSheet.createRow(rowIndex);
    		writeInt(GroupConstants.LOOKUP_REPEAT_MONTHLY_COL, row, rowIndex);
    	}
    	for(rowIndex = 1; rowIndex <= 3; rowIndex++) 
    		writeInt(GroupConstants.LOOKUP_REPEAT_NORMAL_COL, groupSheet.getRow(rowIndex), rowIndex);
    	String[] days = new String[]{
    			TemplatePopulateImportConstants.MONDAY,
				TemplatePopulateImportConstants.TUESDAY,
				TemplatePopulateImportConstants.WEDNESDAY,
				TemplatePopulateImportConstants.THURSDAY,
				TemplatePopulateImportConstants.FRIDAY,
				TemplatePopulateImportConstants.SATURDAY,
				TemplatePopulateImportConstants.SUNDAY};
    	for(rowIndex = 1; rowIndex <= 7; rowIndex++) 
    		writeString(GroupConstants.LOOKUP_IF_REPEAT_WEEKLY_COL, groupSheet.getRow(rowIndex), days[rowIndex-1]);
    }
    
    private void setRules(Sheet worksheet,String dateFormat){
    	CellRangeAddressList officeNameRange = new  CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
				GroupConstants.OFFICE_NAME_COL, GroupConstants.OFFICE_NAME_COL);
    	CellRangeAddressList staffNameRange = new  CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
				GroupConstants.STAFF_NAME_COL, GroupConstants.STAFF_NAME_COL);
    	CellRangeAddressList centerNameRange = new  CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
				GroupConstants.CENTER_NAME_COL, GroupConstants.CENTER_NAME_COL);
    	CellRangeAddressList activeRange = new  CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
				GroupConstants.ACTIVE_COL, GroupConstants.ACTIVE_COL);
    	CellRangeAddressList activationDateRange = new  CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
				GroupConstants.ACTIVATION_DATE_COL,GroupConstants.ACTIVATION_DATE_COL);
		CellRangeAddressList submittedOnDateRange = new  CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
				GroupConstants.SUBMITTED_ON_DATE_COL,GroupConstants.SUBMITTED_ON_DATE_COL);
    	CellRangeAddressList meetingStartDateRange = new  CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
				GroupConstants.MEETING_START_DATE_COL,GroupConstants.MEETING_START_DATE_COL);
    	CellRangeAddressList isRepeatRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
				GroupConstants.IS_REPEATING_COL, GroupConstants.IS_REPEATING_COL);
    	CellRangeAddressList repeatsRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
				GroupConstants. FREQUENCY_COL, GroupConstants.FREQUENCY_COL);
    	CellRangeAddressList repeatsEveryRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
				GroupConstants.INTERVAL_COL,GroupConstants. INTERVAL_COL);
    	CellRangeAddressList repeatsOnRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
				GroupConstants.REPEATS_ON_DAY_COL, GroupConstants.REPEATS_ON_DAY_COL);
    	
    	DataValidationHelper validationHelper = new HSSFDataValidationHelper((HSSFSheet)worksheet);
    	List<OfficeData> offices = officeSheetPopulator.getOffices();
    	setNames(worksheet, offices);
    	
    	DataValidationConstraint centerNameConstraint = validationHelper.createFormulaListConstraint("INDIRECT(CONCATENATE(\"Center_\",$B1))");
    	DataValidationConstraint officeNameConstraint = validationHelper.createFormulaListConstraint("Office");
    	DataValidationConstraint staffNameConstraint = validationHelper.createFormulaListConstraint("INDIRECT(CONCATENATE(\"Staff_\",$B1))");
    	DataValidationConstraint booleanConstraint = validationHelper.createExplicitListConstraint(new String[]{"True", "False"});
    	DataValidationConstraint activationDateConstraint = validationHelper.createDateConstraint
				(DataValidationConstraint.OperatorType.BETWEEN,
						"=VLOOKUP($B1,$IR$2:$IS" + (offices.size() + 1)+",2,FALSE)", "=TODAY()", dateFormat);
		DataValidationConstraint submittedOnDateConstraint =
				validationHelper.createDateConstraint(DataValidationConstraint.OperatorType.LESS_OR_EQUAL,
						"=$G1" ,null,dateFormat);
		DataValidationConstraint meetingStartDateConstraint = validationHelper.
				createDateConstraint(DataValidationConstraint.OperatorType.BETWEEN,
						"=$G1", "=TODAY()", dateFormat);
    	DataValidationConstraint repeatsConstraint = validationHelper.createExplicitListConstraint(new String[]{
    			TemplatePopulateImportConstants.FREQUENCY_DAILY,
				TemplatePopulateImportConstants.FREQUENCY_WEEKLY,
				TemplatePopulateImportConstants.FREQUENCY_MONTHLY,
				TemplatePopulateImportConstants.FREQUENCY_YEARLY});
    	DataValidationConstraint repeatsEveryConstraint = validationHelper.createFormulaListConstraint("INDIRECT($K1)");
    	DataValidationConstraint repeatsOnConstraint = validationHelper.createFormulaListConstraint("INDIRECT(CONCATENATE($K1,\"_DAYS\"))");
    	
    	DataValidation centerValidation=validationHelper.createValidation(centerNameConstraint, centerNameRange);
    	DataValidation officeValidation = validationHelper.createValidation(officeNameConstraint, officeNameRange);
    	DataValidation staffValidation = validationHelper.createValidation(staffNameConstraint, staffNameRange);
    	DataValidation activationDateValidation = validationHelper.createValidation(activationDateConstraint, activationDateRange);
    	DataValidation activeValidation = validationHelper.createValidation(booleanConstraint, activeRange);
    	DataValidation submittedOnDateValidation=validationHelper.createValidation(submittedOnDateConstraint,submittedOnDateRange);
    	DataValidation meetingStartDateValidation = validationHelper.createValidation(meetingStartDateConstraint, meetingStartDateRange);
    	DataValidation isRepeatValidation = validationHelper.createValidation(booleanConstraint, isRepeatRange);
    	DataValidation repeatsValidation = validationHelper.createValidation(repeatsConstraint, repeatsRange);
    	DataValidation repeatsEveryValidation = validationHelper.createValidation(repeatsEveryConstraint, repeatsEveryRange);
    	DataValidation repeatsOnValidation = validationHelper.createValidation(repeatsOnConstraint, repeatsOnRange);
    	
    	worksheet.addValidationData(centerValidation);
    	worksheet.addValidationData(activeValidation);
        worksheet.addValidationData(officeValidation);
        worksheet.addValidationData(staffValidation);
        worksheet.addValidationData(activationDateValidation);
        worksheet.addValidationData(submittedOnDateValidation);
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
    	officeCenter.setRefersToFormula(TemplatePopulateImportConstants.OFFICE_SHEET_NAME+"!$B$2:$B$" + (offices.size() + 1));
    	
    	
    	//Repeat constraint names
    	Name repeatsDaily = centerWorkbook.createName();
    	repeatsDaily.setNameName("Daily");
    	repeatsDaily.setRefersToFormula(TemplatePopulateImportConstants.GROUP_SHEET_NAME+"!$IT$2:$IT$4");
    	Name repeatsWeekly = centerWorkbook.createName();
    	repeatsWeekly.setNameName("Weekly");
    	repeatsWeekly.setRefersToFormula(TemplatePopulateImportConstants.GROUP_SHEET_NAME+"!$IT$2:$IT$4");
    	Name repeatYearly = centerWorkbook.createName();
    	repeatYearly.setNameName("Yearly");
    	repeatYearly.setRefersToFormula(TemplatePopulateImportConstants.GROUP_SHEET_NAME+"!$IT$2:$IT$4");
    	Name repeatsMonthly = centerWorkbook.createName();
    	repeatsMonthly.setNameName("Monthly");
    	repeatsMonthly.setRefersToFormula(TemplatePopulateImportConstants.GROUP_SHEET_NAME+"!$IU$2:$IU$12");
    	Name repeatsOnWeekly = centerWorkbook.createName();
    	repeatsOnWeekly.setNameName("Weekly_Days");
    	repeatsOnWeekly.setRefersToFormula(TemplatePopulateImportConstants.GROUP_SHEET_NAME+"!$IV$2:$IV$8");
    	
    	
    	//Staff Names for each office & center Names for each office 
    	for(Integer i = 0; i < offices.size(); i++) {
    		Integer[] officeNameToBeginEndIndexesOfCenters =centerSheetPopulator.getOfficeNameToBeginEndIndexesOfCenters().get(i);  		
    		Integer[] officeNameToBeginEndIndexesOfStaff = personnelSheetPopulator.getOfficeNameToBeginEndIndexesOfStaff().get(i);

    		Name loanOfficerName = centerWorkbook.createName();
    		Name centerName=centerWorkbook.createName();

    		 if(officeNameToBeginEndIndexesOfStaff != null) {
    	        loanOfficerName.setNameName("Staff_" + offices.get(i).name().trim().replaceAll("[ )(]", "_"));
    	        loanOfficerName.setRefersToFormula(TemplatePopulateImportConstants.STAFF_SHEET_NAME+
						"!$B$" + officeNameToBeginEndIndexesOfStaff[0] + ":$B$" + officeNameToBeginEndIndexesOfStaff[1]);
    		 }
    		 if (officeNameToBeginEndIndexesOfCenters!=null) {
    			 centerName.setNameName("Center_" + offices.get(i).name().trim().replaceAll("[ )(]", "_"));
    			 centerName.setRefersToFormula(TemplatePopulateImportConstants.CENTER_SHEET_NAME+
						 "!$B$" + officeNameToBeginEndIndexesOfCenters[0] + ":$B$" + officeNameToBeginEndIndexesOfCenters[1]);
			}
    	}
		
	}
    
}