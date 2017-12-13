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
package org.apache.fineract.infrastructure.bulkimport.populator.client;

import org.apache.fineract.infrastructure.bulkimport.constants.ClientPersonConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.populator.AbstractWorkbookPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.OfficeSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.PersonnelSheetPopulator;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.poi.hssf.usermodel.HSSFDataValidationHelper;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;

import java.util.List;

public class ClientPersonWorkbookPopulator extends AbstractWorkbookPopulator {

  private OfficeSheetPopulator officeSheetPopulator;
  private PersonnelSheetPopulator personnelSheetPopulator;
  private List<CodeValueData>clientTypeCodeValues;
  private List<CodeValueData>genderCodeValues;
  private List<CodeValueData>clientClassificationCodeValues;
  private List<CodeValueData>addressTypesCodeValues;
  private List<CodeValueData>stateProvinceCodeValues;
  private List<CodeValueData>countryCodeValues;


  public ClientPersonWorkbookPopulator(OfficeSheetPopulator officeSheetPopulator,
      PersonnelSheetPopulator personnelSheetPopulator,List<CodeValueData>clientTypeCodeValues,
          List<CodeValueData>genderCodeValues, List<CodeValueData>clientClassification,List<CodeValueData>addressTypesCodeValues,
          List<CodeValueData>stateProvinceCodeValues,List<CodeValueData>countryCodeValues ) {
    this.officeSheetPopulator = officeSheetPopulator;
    this.personnelSheetPopulator = personnelSheetPopulator;
    this.clientTypeCodeValues=clientTypeCodeValues;
    this.genderCodeValues=genderCodeValues;
    this.clientClassificationCodeValues=clientClassification;
    this.addressTypesCodeValues=addressTypesCodeValues;
    this.stateProvinceCodeValues=stateProvinceCodeValues;
    this.countryCodeValues=countryCodeValues;
  }


  @Override
  public void populate(Workbook workbook,String dateFormat) {
    Sheet clientSheet = workbook.createSheet(TemplatePopulateImportConstants.CLIENT_PERSON_SHEET_NAME);
    personnelSheetPopulator.populate(workbook,dateFormat);
    officeSheetPopulator.populate(workbook,dateFormat);
    setLayout(clientSheet);
    setOfficeDateLookupTable(clientSheet, officeSheetPopulator.getOffices(),
            ClientPersonConstants.RELATIONAL_OFFICE_NAME_COL, ClientPersonConstants.RELATIONAL_OFFICE_OPENING_DATE_COL,dateFormat);
    setClientDataLookupTable(clientSheet);
    setRules(clientSheet,dateFormat);
  }

  private void setClientDataLookupTable(Sheet clientSheet) {
    int rowIndex=0;
    for (CodeValueData clientTypeCodeValue:clientTypeCodeValues) {
      Row row =clientSheet.getRow(++rowIndex);
      if(row==null)
        row=clientSheet.createRow(rowIndex);
      writeString(ClientPersonConstants.LOOKUP_CLIENT_TYPES_COL,row,clientTypeCodeValue.getName()+
              "-"+clientTypeCodeValue.getId());
    }
    rowIndex=0;
    for (CodeValueData clientClassificationCodeValue:clientClassificationCodeValues) {
      Row row =clientSheet.getRow(++rowIndex);
      if(row==null)
        row=clientSheet.createRow(rowIndex);
      writeString(ClientPersonConstants.LOOKUP_CLIENT_CLASSIFICATION_COL,row,
              clientClassificationCodeValue.getName()+"-"+clientClassificationCodeValue.getId());
    }
    rowIndex=0;
    for (CodeValueData genderCodeValue:genderCodeValues) {
      Row row =clientSheet.getRow(++rowIndex);
      if(row==null)
        row=clientSheet.createRow(rowIndex);
      writeString(ClientPersonConstants.LOOKUP_GENDER_COL,row,genderCodeValue.getName()+"-"+genderCodeValue.getId());
    }
    rowIndex=0;
    for (CodeValueData addressTypeCodeValue:addressTypesCodeValues) {
      Row row =clientSheet.getRow(++rowIndex);
      if(row==null)
        row=clientSheet.createRow(rowIndex);
      writeString(ClientPersonConstants.LOOKUP_ADDRESS_TYPE_COL,row,
              addressTypeCodeValue.getName()+"-"+addressTypeCodeValue.getId());
    }
    rowIndex=0;
    for (CodeValueData stateCodeValue:stateProvinceCodeValues) {
      Row row =clientSheet.getRow(++rowIndex);
      if(row==null)
        row=clientSheet.createRow(rowIndex);
      writeString(ClientPersonConstants.LOOKUP_STATE_PROVINCE_COL,row,stateCodeValue.getName()+"-"+stateCodeValue.getId());
    }
    rowIndex=0;
    for (CodeValueData countryCodeValue: countryCodeValues) {
      Row row =clientSheet.getRow(++rowIndex);
      if(row==null)
        row=clientSheet.createRow(rowIndex);
      writeString(ClientPersonConstants.LOOKUP_COUNTRY_COL,row,countryCodeValue.getName()+"-"+countryCodeValue.getId());
    }

  }

  private void setLayout(Sheet worksheet) {
    Row rowHeader = worksheet.createRow(TemplatePopulateImportConstants.ROWHEADER_INDEX);
    rowHeader.setHeight(TemplatePopulateImportConstants.ROW_HEADER_HEIGHT);
    worksheet.setColumnWidth(ClientPersonConstants.FIRST_NAME_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
    worksheet.setColumnWidth(ClientPersonConstants.LAST_NAME_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
    worksheet.setColumnWidth(ClientPersonConstants.MIDDLE_NAME_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
    writeString(ClientPersonConstants.FIRST_NAME_COL, rowHeader, "First Name*");
    writeString(ClientPersonConstants.LAST_NAME_COL, rowHeader, "Last Name*");
    writeString(ClientPersonConstants.MIDDLE_NAME_COL, rowHeader, "Middle Name");
    worksheet.setColumnWidth(ClientPersonConstants.OFFICE_NAME_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
    worksheet.setColumnWidth(ClientPersonConstants.STAFF_NAME_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
    worksheet.setColumnWidth(ClientPersonConstants.EXTERNAL_ID_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
    worksheet.setColumnWidth(ClientPersonConstants.SUBMITTED_ON_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
    worksheet.setColumnWidth(ClientPersonConstants.ACTIVATION_DATE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
    worksheet.setColumnWidth(ClientPersonConstants.ACTIVE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
    worksheet.setColumnWidth(ClientPersonConstants.MOBILE_NO_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
    worksheet.setColumnWidth(ClientPersonConstants.DOB_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
    worksheet.setColumnWidth(ClientPersonConstants.CLIENT_TYPE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
    worksheet.setColumnWidth(ClientPersonConstants.GENDER_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
    worksheet.setColumnWidth(ClientPersonConstants.CLIENT_CLASSIFICATION_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
    worksheet.setColumnWidth(ClientPersonConstants.IS_STAFF_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
    worksheet.setColumnWidth(ClientPersonConstants.ADDRESS_ENABLED_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
    worksheet.setColumnWidth(ClientPersonConstants.ADDRESS_TYPE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
    worksheet.setColumnWidth(ClientPersonConstants.STREET_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
    worksheet.setColumnWidth(ClientPersonConstants.ADDRESS_LINE_1_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
    worksheet.setColumnWidth(ClientPersonConstants.ADDRESS_LINE_2_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
    worksheet.setColumnWidth(ClientPersonConstants.ADDRESS_LINE_3_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
    worksheet.setColumnWidth(ClientPersonConstants.CITY_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
    worksheet.setColumnWidth(ClientPersonConstants.STATE_PROVINCE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
    worksheet.setColumnWidth(ClientPersonConstants.COUNTRY_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
    worksheet.setColumnWidth(ClientPersonConstants.POSTAL_CODE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
    worksheet.setColumnWidth(ClientPersonConstants.IS_ACTIVE_ADDRESS_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
    worksheet.setColumnWidth(ClientPersonConstants.WARNING_COL,TemplatePopulateImportConstants.MEDIUM_COL_SIZE);

    worksheet.setColumnWidth(ClientPersonConstants.RELATIONAL_OFFICE_NAME_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
    worksheet.setColumnWidth(ClientPersonConstants.RELATIONAL_OFFICE_OPENING_DATE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
    worksheet.setColumnWidth(ClientPersonConstants.LOOKUP_GENDER_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
    worksheet.setColumnWidth(ClientPersonConstants.LOOKUP_CLIENT_TYPES_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
    worksheet.setColumnWidth(ClientPersonConstants.LOOKUP_CLIENT_CLASSIFICATION_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
    worksheet.setColumnWidth(ClientPersonConstants.LOOKUP_ADDRESS_TYPE_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
    worksheet.setColumnWidth(ClientPersonConstants.LOOKUP_STATE_PROVINCE_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
    worksheet.setColumnWidth(ClientPersonConstants.LOOKUP_COUNTRY_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
    writeString(ClientPersonConstants.OFFICE_NAME_COL, rowHeader, "Office Name*");
    writeString(ClientPersonConstants.STAFF_NAME_COL, rowHeader, "Staff Name");
    writeString(ClientPersonConstants.EXTERNAL_ID_COL, rowHeader, "External ID ");
    writeString(ClientPersonConstants.SUBMITTED_ON_COL,rowHeader,"Submitted On Date");
    writeString(ClientPersonConstants.ACTIVATION_DATE_COL, rowHeader, "Activation date");
    writeString(ClientPersonConstants.ACTIVE_COL, rowHeader, "Active*");
    writeString(ClientPersonConstants.MOBILE_NO_COL, rowHeader, "Mobile number");
    writeString(ClientPersonConstants.DOB_COL, rowHeader, "Date of Birth ");
    writeString(ClientPersonConstants.CLIENT_TYPE_COL, rowHeader, "Client Type ");
    writeString(ClientPersonConstants.IS_STAFF_COL, rowHeader, "Is a staff memeber ");
    writeString(ClientPersonConstants.GENDER_COL, rowHeader, "Gender ");
    writeString(ClientPersonConstants.ADDRESS_ENABLED_COL,rowHeader,"Address Enabled *");
    writeString(ClientPersonConstants.CLIENT_CLASSIFICATION_COL, rowHeader, "Client Classification ");
    writeString(ClientPersonConstants.ADDRESS_TYPE_COL, rowHeader, "Address Type ");
    writeString(ClientPersonConstants.STREET_COL, rowHeader, "Street  ");
    writeString(ClientPersonConstants.ADDRESS_LINE_1_COL, rowHeader, "Address Line 1");
    writeString(ClientPersonConstants.ADDRESS_LINE_2_COL, rowHeader, "Address Line 2");
    writeString(ClientPersonConstants.ADDRESS_LINE_3_COL, rowHeader, "Address Line 3 ");
    writeString(ClientPersonConstants.CITY_COL, rowHeader, "City ");
    writeString(ClientPersonConstants.STATE_PROVINCE_COL, rowHeader, "State/ Province ");
    writeString(ClientPersonConstants.COUNTRY_COL, rowHeader, "Country ");
    writeString(ClientPersonConstants.POSTAL_CODE_COL, rowHeader, "Postal Code ");
    writeString(ClientPersonConstants.IS_ACTIVE_ADDRESS_COL, rowHeader, "Is active Address ? ");
    writeString(ClientPersonConstants.WARNING_COL, rowHeader, "All * marked fields are compulsory.");

    writeString(ClientPersonConstants.RELATIONAL_OFFICE_NAME_COL, rowHeader, "Lookup office Name  ");
    writeString(ClientPersonConstants.RELATIONAL_OFFICE_OPENING_DATE_COL, rowHeader, "Lookup Office Opened Date ");
    writeString(ClientPersonConstants.LOOKUP_GENDER_COL, rowHeader, "Lookup Gender ");
    writeString(ClientPersonConstants.LOOKUP_CLIENT_TYPES_COL, rowHeader, "Lookup Client Types ");
    writeString(ClientPersonConstants.LOOKUP_CLIENT_CLASSIFICATION_COL, rowHeader, "Lookup Client Classification ");
    writeString(ClientPersonConstants.LOOKUP_ADDRESS_TYPE_COL, rowHeader, "Lookup AddressType ");
    writeString(ClientPersonConstants.LOOKUP_STATE_PROVINCE_COL, rowHeader, "Lookup State/Province ");
    writeString(ClientPersonConstants.LOOKUP_COUNTRY_COL, rowHeader, "Lookup Country ");


  }

  private void setRules(Sheet worksheet,String dateformat) {
    CellRangeAddressList officeNameRange = new CellRangeAddressList(1,
        SpreadsheetVersion.EXCEL97.getLastRowIndex(), ClientPersonConstants.OFFICE_NAME_COL, ClientPersonConstants.OFFICE_NAME_COL);
    CellRangeAddressList staffNameRange = new CellRangeAddressList(1,
        SpreadsheetVersion.EXCEL97.getLastRowIndex(),ClientPersonConstants. STAFF_NAME_COL,ClientPersonConstants. STAFF_NAME_COL);
    CellRangeAddressList submittedOnDateRange = new CellRangeAddressList(1,
            SpreadsheetVersion.EXCEL97.getLastRowIndex(),ClientPersonConstants. SUBMITTED_ON_COL, ClientPersonConstants.SUBMITTED_ON_COL);
    CellRangeAddressList activationDateRange = new CellRangeAddressList(1,
        SpreadsheetVersion.EXCEL97.getLastRowIndex(), ClientPersonConstants.ACTIVATION_DATE_COL, ClientPersonConstants.ACTIVATION_DATE_COL);
    CellRangeAddressList activeRange = new CellRangeAddressList(1,
        SpreadsheetVersion.EXCEL97.getLastRowIndex(), ClientPersonConstants.ACTIVE_COL,ClientPersonConstants. ACTIVE_COL);
    CellRangeAddressList clientTypeRange=new CellRangeAddressList(1,
            SpreadsheetVersion.EXCEL97.getLastRowIndex(),ClientPersonConstants. CLIENT_TYPE_COL,ClientPersonConstants. CLIENT_TYPE_COL);
    CellRangeAddressList dobRange=new CellRangeAddressList(1,
            SpreadsheetVersion.EXCEL97.getLastRowIndex(),ClientPersonConstants. DOB_COL,ClientPersonConstants. DOB_COL);
    CellRangeAddressList isStaffRange=new CellRangeAddressList(1,
            SpreadsheetVersion.EXCEL97.getLastRowIndex(),ClientPersonConstants. IS_STAFF_COL,ClientPersonConstants. IS_STAFF_COL);
    CellRangeAddressList genderRange=new CellRangeAddressList(1,
            SpreadsheetVersion.EXCEL97.getLastRowIndex(), ClientPersonConstants.GENDER_COL,ClientPersonConstants. GENDER_COL);
    CellRangeAddressList clientClassificationRange=new CellRangeAddressList(1,
            SpreadsheetVersion.EXCEL97.getLastRowIndex(), ClientPersonConstants.CLIENT_CLASSIFICATION_COL, ClientPersonConstants.CLIENT_CLASSIFICATION_COL);
    CellRangeAddressList enabledAddressRange=new CellRangeAddressList(1,
            SpreadsheetVersion.EXCEL97.getLastRowIndex(), ClientPersonConstants.ADDRESS_ENABLED_COL, ClientPersonConstants.ADDRESS_ENABLED_COL);
    CellRangeAddressList addressTypeRange=new CellRangeAddressList(1,
            SpreadsheetVersion.EXCEL97.getLastRowIndex(),ClientPersonConstants. ADDRESS_TYPE_COL, ClientPersonConstants.ADDRESS_TYPE_COL);
    CellRangeAddressList stateProvinceRange=new CellRangeAddressList(1,
            SpreadsheetVersion.EXCEL97.getLastRowIndex(),ClientPersonConstants. STATE_PROVINCE_COL, ClientPersonConstants.STATE_PROVINCE_COL);
    CellRangeAddressList countryRange=new CellRangeAddressList(1,
            SpreadsheetVersion.EXCEL97.getLastRowIndex(), ClientPersonConstants.COUNTRY_COL, ClientPersonConstants.COUNTRY_COL);
    CellRangeAddressList activeAddressRange=new CellRangeAddressList(1,
            SpreadsheetVersion.EXCEL97.getLastRowIndex(),ClientPersonConstants. IS_ACTIVE_ADDRESS_COL,ClientPersonConstants. IS_ACTIVE_ADDRESS_COL);


    DataValidationHelper validationHelper = new HSSFDataValidationHelper((HSSFSheet) worksheet);

    List<OfficeData> offices = officeSheetPopulator.getOffices();
    setNames(worksheet, offices);

    DataValidationConstraint officeNameConstraint =
        validationHelper.createFormulaListConstraint("Office");
    DataValidationConstraint staffNameConstraint =
        validationHelper.createFormulaListConstraint("INDIRECT(CONCATENATE(\"Staff_\",$D1))");
    DataValidationConstraint submittedOnDateConstraint =
            validationHelper.createDateConstraint(DataValidationConstraint.OperatorType.LESS_OR_EQUAL,
                     "=$I1" ,null,dateformat);
    DataValidationConstraint activationDateConstraint =
        validationHelper.createDateConstraint(DataValidationConstraint.OperatorType.BETWEEN,
            "=VLOOKUP($D1,$AJ$2:$AK" + (offices.size() + 1) + ",2,FALSE)", "=TODAY()", dateformat);
    DataValidationConstraint dobDateConstraint =
            validationHelper.createDateConstraint(DataValidationConstraint.OperatorType.LESS_OR_EQUAL,
                    "=TODAY()",null, dateformat);
    DataValidationConstraint activeConstraint =
        validationHelper.createExplicitListConstraint(new String[] {"True", "False"});
    DataValidationConstraint clientTypesConstraint =
            validationHelper.createFormulaListConstraint("ClientTypes");
    DataValidationConstraint isStaffConstraint =
            validationHelper.createExplicitListConstraint(new String[] {"True", "False"});
    DataValidationConstraint genderConstraint =
            validationHelper.createFormulaListConstraint("Gender");
    DataValidationConstraint clientClassificationConstraint =
            validationHelper.createFormulaListConstraint("ClientClassification");
    DataValidationConstraint enabledAddressConstraint =
            validationHelper.createExplicitListConstraint(new String[] {"True", "False"});
    DataValidationConstraint addressTypeConstraint =
            validationHelper.createFormulaListConstraint("AddressType");
    DataValidationConstraint stateProvinceConstraint =
            validationHelper.createFormulaListConstraint("StateProvince");
    DataValidationConstraint countryConstraint =
            validationHelper.createFormulaListConstraint("Country");
    DataValidationConstraint activeAddressConstraint =
            validationHelper.createExplicitListConstraint(new String[] {"True", "False"});

    DataValidation officeValidation =
        validationHelper.createValidation(officeNameConstraint, officeNameRange);
    DataValidation staffValidation =
        validationHelper.createValidation(staffNameConstraint, staffNameRange);
    DataValidation submittedOnDateValidation =
            validationHelper.createValidation(submittedOnDateConstraint, submittedOnDateRange);
    DataValidation activationDateValidation =
        validationHelper.createValidation(activationDateConstraint, activationDateRange);
    DataValidation dobDateValidation =
            validationHelper.createValidation(dobDateConstraint, dobRange);
    DataValidation activeValidation =
        validationHelper.createValidation(activeConstraint, activeRange);
    DataValidation clientTypeValidation =
            validationHelper.createValidation(clientTypesConstraint, clientTypeRange);
    DataValidation isStaffValidation =
            validationHelper.createValidation(isStaffConstraint, isStaffRange);
    DataValidation genderValidation =
            validationHelper.createValidation(genderConstraint, genderRange);
    DataValidation clientClassificationValidation =
            validationHelper.createValidation(clientClassificationConstraint, clientClassificationRange);
    DataValidation enabledAddressValidation=
            validationHelper.createValidation(enabledAddressConstraint,enabledAddressRange);
    DataValidation addressTypeValidation =
            validationHelper.createValidation(addressTypeConstraint, addressTypeRange);
    DataValidation stateProvinceValidation =
            validationHelper.createValidation(stateProvinceConstraint, stateProvinceRange);
    DataValidation countryValidation =
            validationHelper.createValidation(countryConstraint, countryRange);
    DataValidation activeAddressValidation =
            validationHelper.createValidation(activeAddressConstraint,activeAddressRange);

    worksheet.addValidationData(activeValidation);
    worksheet.addValidationData(officeValidation);
    worksheet.addValidationData(staffValidation);
    worksheet.addValidationData(activationDateValidation);
    worksheet.addValidationData(submittedOnDateValidation);
    worksheet.addValidationData(dobDateValidation);
    worksheet.addValidationData(clientTypeValidation);
    worksheet.addValidationData(isStaffValidation);
    worksheet.addValidationData(genderValidation);
    worksheet.addValidationData(clientClassificationValidation);
    worksheet.addValidationData(enabledAddressValidation);
    worksheet.addValidationData(addressTypeValidation);
    worksheet.addValidationData(stateProvinceValidation);
    worksheet.addValidationData(countryValidation);
    worksheet.addValidationData(activeAddressValidation);
  }

  private void setNames(Sheet worksheet, List<OfficeData> offices) {
    Workbook clientWorkbook = worksheet.getWorkbook();
    Name officeGroup = clientWorkbook.createName();
    officeGroup.setNameName("Office");
    officeGroup.setRefersToFormula(TemplatePopulateImportConstants.OFFICE_SHEET_NAME+"!$B$2:$B$" + (offices.size() + 1));

    Name clientTypeGroup = clientWorkbook.createName();
    clientTypeGroup.setNameName("ClientTypes");
    clientTypeGroup.setRefersToFormula(TemplatePopulateImportConstants.CLIENT_PERSON_SHEET_NAME+"!$AN$2:$AN$" +
            (clientTypeCodeValues.size() + 1));

    Name genderGroup = clientWorkbook.createName();
    genderGroup.setNameName("Gender");
    genderGroup.setRefersToFormula(TemplatePopulateImportConstants.CLIENT_PERSON_SHEET_NAME+"!$AL$2:$AL$" + (genderCodeValues.size() + 1));

    Name clientClassficationGroup = clientWorkbook.createName();
    clientClassficationGroup.setNameName("ClientClassification");
    clientClassficationGroup.setRefersToFormula(TemplatePopulateImportConstants.CLIENT_PERSON_SHEET_NAME+"!$AM$2:$AM$" +
            (clientClassificationCodeValues.size() + 1));

    Name addressTypeGroup = clientWorkbook.createName();
    addressTypeGroup.setNameName("AddressType");
    addressTypeGroup.setRefersToFormula(TemplatePopulateImportConstants.CLIENT_PERSON_SHEET_NAME+"!$AO$2:$AO$" +
            (addressTypesCodeValues.size() + 1));

    Name stateProvinceGroup = clientWorkbook.createName();
    stateProvinceGroup.setNameName("StateProvince");
    stateProvinceGroup.setRefersToFormula(TemplatePopulateImportConstants.CLIENT_PERSON_SHEET_NAME+"!$AP$2:$AP$" +
            (stateProvinceCodeValues.size() + 1));

    Name countryGroup = clientWorkbook.createName();
    countryGroup.setNameName("Country");
    countryGroup.setRefersToFormula(TemplatePopulateImportConstants.CLIENT_PERSON_SHEET_NAME+"!$AQ$2:$AQ$" +
            (countryCodeValues.size() + 1));
    
    for (Integer i = 0; i < offices.size(); i++) {
      Integer[] officeNameToBeginEndIndexesOfStaff =
          personnelSheetPopulator.getOfficeNameToBeginEndIndexesOfStaff().get(i);
      if (officeNameToBeginEndIndexesOfStaff != null) {
        Name name = clientWorkbook.createName();
        name.setNameName("Staff_" + offices.get(i).name().trim().replaceAll("[ )(]", "_"));
        name.setRefersToFormula(TemplatePopulateImportConstants.STAFF_SHEET_NAME+"!$B$" +
                officeNameToBeginEndIndexesOfStaff[0] + ":$B$" + officeNameToBeginEndIndexesOfStaff[1]);
      }
    }
  }

}