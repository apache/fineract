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

import java.util.List;

import org.apache.fineract.infrastructure.bulkimport.populator.AbstractWorkbookPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.OfficeSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.PersonnelSheetPopulator;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.portfolio.savings.data.SavingsProductData;
import org.apache.poi.hssf.usermodel.HSSFDataValidationHelper;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;

public class ClientPersonWorkbookPopulator extends AbstractWorkbookPopulator {

  private static final int FIRST_NAME_COL = 0;//A
  private static final int LAST_NAME_COL = 1;//B
  private static final int MIDDLE_NAME_COL = 2;//C
  private static final int OFFICE_NAME_COL = 3;//D
  private static final int STAFF_NAME_COL = 4;//E
  private static final int EXTERNAL_ID_COL = 5;//F
  private static final int ACTIVE_COL = 6;//H
  private static final int ACTIVATION_DATE_COL = 7;//I
  private static final int SUBMITTED_ON_COL=8; //G
  private static final int MOBILE_NO_COL=9;//J
  private static final int DOB_COL=10;//K
  private static final int CLIENT_TYPE_COL=11;//L
  private static final int GENDER_COL=12;//M
  private static final int CLIENT_CLASSIFICATION_COL=13;//N
  private static final int IS_STAFF_COL=14;//O
  private static final int ADDRESS_ENABLED=15;// P
  private static final int ADDRESS_TYPE_COL=16;//Q
  private static final int STREET_COL=17;//R
  private static final int ADDRESS_LINE_1_COL=18;//S
  private static final int ADDRESS_LINE_2_COL=19;//T
  private static final int ADDRESS_LINE_3_COL=20;//U
  private static final int CITY=21;//V
  private static final int STATE_PROVINCE_COL=22;//W
  private static final int COUNTRY_COL=23;//X
  private static final int POSTAL_CODE_COL=24;//Y
  private static final int IS_ACTIVE_ADDRESS_COL=25;//Z
  private static final int WARNING_COL = 26;//AA
  private static final int RELATIONAL_OFFICE_NAME_COL = 35;//AJ
  private static final int RELATIONAL_OFFICE_OPENING_DATE_COL = 36;//AK
  private static final int LOOKUP_GENDER_COL = 37;//AL
  private static final int LOOKUP_CLIENT_CLASSIFICATION = 38;//AM
  private static final int LOOKUP_CLIENT_TYPES = 39;//AN
  private static final int LOOKUP_ADDRESS_TYPE = 40;//AO
  private static final int LOOKUP_STATE_PROVINCE = 41;//AP
  private static final int LOOKUP_COUNTRY = 42;//AQ

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
  public void populate(Workbook workbook) {
    Sheet clientSheet = workbook.createSheet("ClientPerson");
    personnelSheetPopulator.populate(workbook);
    officeSheetPopulator.populate(workbook);
    setLayout(clientSheet);
    setOfficeDateLookupTable(clientSheet, officeSheetPopulator.getOffices(),
            RELATIONAL_OFFICE_NAME_COL, RELATIONAL_OFFICE_OPENING_DATE_COL);
    setClientDataLookupTable(clientSheet);
    setRules(clientSheet);
  }

  private void setClientDataLookupTable(Sheet clientSheet) {
    int rowIndex=0;
    for (CodeValueData clientTypeCodeValue:clientTypeCodeValues) {
      Row row =clientSheet.getRow(++rowIndex);
      if(row==null)
        row=clientSheet.createRow(rowIndex);
      writeString(LOOKUP_CLIENT_TYPES,row,clientTypeCodeValue.getName()+"-"+clientTypeCodeValue.getId());
    }
    rowIndex=0;
    for (CodeValueData clientClassificationCodeValue:clientClassificationCodeValues) {
      Row row =clientSheet.getRow(++rowIndex);
      if(row==null)
        row=clientSheet.createRow(rowIndex);
      writeString(LOOKUP_CLIENT_CLASSIFICATION,row,clientClassificationCodeValue.getName()+"-"+clientClassificationCodeValue.getId());
    }
    rowIndex=0;
    for (CodeValueData genderCodeValue:genderCodeValues) {
      Row row =clientSheet.getRow(++rowIndex);
      if(row==null)
        row=clientSheet.createRow(rowIndex);
      writeString(LOOKUP_GENDER_COL,row,genderCodeValue.getName()+"-"+genderCodeValue.getId());
    }
    rowIndex=0;
    for (CodeValueData addressTypeCodeValue:addressTypesCodeValues) {
      Row row =clientSheet.getRow(++rowIndex);
      if(row==null)
        row=clientSheet.createRow(rowIndex);
      writeString(LOOKUP_ADDRESS_TYPE,row,addressTypeCodeValue.getName()+"-"+addressTypeCodeValue.getId());
    }
    rowIndex=0;
    for (CodeValueData stateCodeValue:stateProvinceCodeValues) {
      Row row =clientSheet.getRow(++rowIndex);
      if(row==null)
        row=clientSheet.createRow(rowIndex);
      writeString(LOOKUP_STATE_PROVINCE,row,stateCodeValue.getName()+"-"+stateCodeValue.getId());
    }
    rowIndex=0;
    for (CodeValueData countryCodeValue: countryCodeValues) {
      Row row =clientSheet.getRow(++rowIndex);
      if(row==null)
        row=clientSheet.createRow(rowIndex);
      writeString(LOOKUP_COUNTRY,row,countryCodeValue.getName()+"-"+countryCodeValue.getId());
    }

  }

  private void setLayout(Sheet worksheet) {
    Row rowHeader = worksheet.createRow(0);
    rowHeader.setHeight((short) 500);
    worksheet.setColumnWidth(FIRST_NAME_COL, 6000);
    worksheet.setColumnWidth(LAST_NAME_COL, 6000);
    worksheet.setColumnWidth(MIDDLE_NAME_COL, 6000);
    writeString(FIRST_NAME_COL, rowHeader, "First Name*");
    writeString(LAST_NAME_COL, rowHeader, "Last Name*");
    writeString(MIDDLE_NAME_COL, rowHeader, "Middle Name");
    worksheet.setColumnWidth(OFFICE_NAME_COL, 5000);
    worksheet.setColumnWidth(STAFF_NAME_COL, 5000);
    worksheet.setColumnWidth(EXTERNAL_ID_COL, 3500);
    worksheet.setColumnWidth(SUBMITTED_ON_COL,4000);
    worksheet.setColumnWidth(ACTIVATION_DATE_COL, 4000);
    worksheet.setColumnWidth(ACTIVE_COL, 2000);
    worksheet.setColumnWidth(MOBILE_NO_COL, 6000);
    worksheet.setColumnWidth(DOB_COL, 4000);
    worksheet.setColumnWidth(CLIENT_TYPE_COL, 4000);
    worksheet.setColumnWidth(GENDER_COL, 2000);
    worksheet.setColumnWidth(CLIENT_CLASSIFICATION_COL, 6000);
    worksheet.setColumnWidth(IS_STAFF_COL, 4000);
    worksheet.setColumnWidth(ADDRESS_ENABLED,4000);
    worksheet.setColumnWidth(ADDRESS_TYPE_COL, 4000);
    worksheet.setColumnWidth(STREET_COL, 4000);
    worksheet.setColumnWidth(ADDRESS_LINE_1_COL, 6000);
    worksheet.setColumnWidth(ADDRESS_LINE_2_COL, 6000);
    worksheet.setColumnWidth(ADDRESS_LINE_3_COL, 6000);
    worksheet.setColumnWidth(CITY, 4000);
    worksheet.setColumnWidth(STATE_PROVINCE_COL, 4000);
    worksheet.setColumnWidth(COUNTRY_COL, 2000);
    worksheet.setColumnWidth(POSTAL_CODE_COL, 4000);
    worksheet.setColumnWidth(IS_ACTIVE_ADDRESS_COL, 4000);
    worksheet.setColumnWidth(WARNING_COL,6000);

    worksheet.setColumnWidth(RELATIONAL_OFFICE_NAME_COL, 6000);
    worksheet.setColumnWidth(RELATIONAL_OFFICE_OPENING_DATE_COL, 4000);
    worksheet.setColumnWidth(LOOKUP_GENDER_COL,2000);
    worksheet.setColumnWidth(LOOKUP_CLIENT_TYPES,3000);
    worksheet.setColumnWidth(LOOKUP_CLIENT_CLASSIFICATION,4000);
    worksheet.setColumnWidth(LOOKUP_ADDRESS_TYPE,4000);
    worksheet.setColumnWidth(LOOKUP_STATE_PROVINCE,4000);
    worksheet.setColumnWidth(LOOKUP_COUNTRY,4000);
    writeString(OFFICE_NAME_COL, rowHeader, "Office Name*");
    writeString(STAFF_NAME_COL, rowHeader, "Staff Name*");
    writeString(EXTERNAL_ID_COL, rowHeader, "External ID *");
    writeString(SUBMITTED_ON_COL,rowHeader,"Submitted On Date");
    writeString(ACTIVATION_DATE_COL, rowHeader, "Activation date *");
    writeString(ACTIVE_COL, rowHeader, "Active*");
    writeString(MOBILE_NO_COL, rowHeader, "Mobile number*");
    writeString(DOB_COL, rowHeader, "Date of Birth *");
    writeString(CLIENT_TYPE_COL, rowHeader, "Client Type *");
    writeString(IS_STAFF_COL, rowHeader, "Is a staff memeber *");
    writeString(GENDER_COL, rowHeader, "Gender *");
    writeString(ADDRESS_ENABLED,rowHeader,"Address Enabled *");
    writeString(CLIENT_CLASSIFICATION_COL, rowHeader, "Client Classification *");
    writeString(ADDRESS_TYPE_COL, rowHeader, "Address Type *");
    writeString(STREET_COL, rowHeader, "Street  *");
    writeString(ADDRESS_LINE_1_COL, rowHeader, "Address Line 1*");
    writeString(ADDRESS_LINE_2_COL, rowHeader, "Address Line 2*");
    writeString(ADDRESS_LINE_3_COL, rowHeader, "Address Line 3 ");
    writeString(CITY, rowHeader, "City *");
    writeString(STATE_PROVINCE_COL, rowHeader, "State/ Province *");
    writeString(COUNTRY_COL, rowHeader, "Country *");
    writeString(POSTAL_CODE_COL, rowHeader, "Postal Code ");
    writeString(IS_ACTIVE_ADDRESS_COL, rowHeader, "Is active Address ? ");
    writeString(WARNING_COL, rowHeader, "All * marked fields are compulsory.");

    writeString(RELATIONAL_OFFICE_NAME_COL, rowHeader, "Lookup office Name  ");
    writeString(RELATIONAL_OFFICE_OPENING_DATE_COL, rowHeader, "Lookup Office Opened Date ");
    writeString(LOOKUP_GENDER_COL, rowHeader, "Lookup Gender ");
    writeString(LOOKUP_CLIENT_TYPES, rowHeader, "Lookup Client Types ");
    writeString(LOOKUP_CLIENT_CLASSIFICATION, rowHeader, "Lookup Client Classification ");
    writeString(LOOKUP_ADDRESS_TYPE, rowHeader, "Lookup AddressType ");
    writeString(LOOKUP_STATE_PROVINCE, rowHeader, "Lookup State/Province ");
    writeString(LOOKUP_COUNTRY, rowHeader, "Lookup Country ");


  }

  private void setRules(Sheet worksheet) {
    CellRangeAddressList officeNameRange = new CellRangeAddressList(1,
            SpreadsheetVersion.EXCEL97.getLastRowIndex(), OFFICE_NAME_COL, OFFICE_NAME_COL);
    CellRangeAddressList staffNameRange = new CellRangeAddressList(1,
            SpreadsheetVersion.EXCEL97.getLastRowIndex(), STAFF_NAME_COL, STAFF_NAME_COL);
    CellRangeAddressList submittedOnDateRange = new CellRangeAddressList(1,
            SpreadsheetVersion.EXCEL97.getLastRowIndex(), SUBMITTED_ON_COL, SUBMITTED_ON_COL);
    CellRangeAddressList activationDateRange = new CellRangeAddressList(1,
            SpreadsheetVersion.EXCEL97.getLastRowIndex(), ACTIVATION_DATE_COL, ACTIVATION_DATE_COL);
    CellRangeAddressList activeRange = new CellRangeAddressList(1,
            SpreadsheetVersion.EXCEL97.getLastRowIndex(), ACTIVE_COL, ACTIVE_COL);
    CellRangeAddressList clientTypeRange=new CellRangeAddressList(1,
            SpreadsheetVersion.EXCEL97.getLastRowIndex(), CLIENT_TYPE_COL, CLIENT_TYPE_COL);
    CellRangeAddressList dobRange=new CellRangeAddressList(1,
            SpreadsheetVersion.EXCEL97.getLastRowIndex(), DOB_COL, DOB_COL);
    CellRangeAddressList isStaffRange=new CellRangeAddressList(1,
            SpreadsheetVersion.EXCEL97.getLastRowIndex(), IS_STAFF_COL, IS_STAFF_COL);
    CellRangeAddressList genderRange=new CellRangeAddressList(1,
            SpreadsheetVersion.EXCEL97.getLastRowIndex(), GENDER_COL, GENDER_COL);
    CellRangeAddressList clientClassificationRange=new CellRangeAddressList(1,
            SpreadsheetVersion.EXCEL97.getLastRowIndex(), CLIENT_CLASSIFICATION_COL, CLIENT_CLASSIFICATION_COL);
    CellRangeAddressList enabledAddressRange=new CellRangeAddressList(1,
            SpreadsheetVersion.EXCEL97.getLastRowIndex(), ADDRESS_ENABLED, ADDRESS_ENABLED);
    CellRangeAddressList addressTypeRange=new CellRangeAddressList(1,
            SpreadsheetVersion.EXCEL97.getLastRowIndex(), ADDRESS_TYPE_COL, ADDRESS_TYPE_COL);
    CellRangeAddressList stateProvinceRange=new CellRangeAddressList(1,
            SpreadsheetVersion.EXCEL97.getLastRowIndex(), STATE_PROVINCE_COL, STATE_PROVINCE_COL);
    CellRangeAddressList countryRange=new CellRangeAddressList(1,
            SpreadsheetVersion.EXCEL97.getLastRowIndex(), COUNTRY_COL, COUNTRY_COL);
    CellRangeAddressList activeAddressRange=new CellRangeAddressList(1,
            SpreadsheetVersion.EXCEL97.getLastRowIndex(), IS_ACTIVE_ADDRESS_COL, IS_ACTIVE_ADDRESS_COL);


    DataValidationHelper validationHelper = new HSSFDataValidationHelper((HSSFSheet) worksheet);

    List<OfficeData> offices = officeSheetPopulator.getOffices();
    setNames(worksheet, offices);

    DataValidationConstraint officeNameConstraint =
            validationHelper.createFormulaListConstraint("Office");
    DataValidationConstraint staffNameConstraint =
            validationHelper.createFormulaListConstraint("INDIRECT(CONCATENATE(\"Staff_\",$D1))");
    DataValidationConstraint submittedOnDateConstraint =
            validationHelper.createDateConstraint(DataValidationConstraint.OperatorType.LESS_OR_EQUAL,
                    "=$I1" ,null,"dd/mm/yy");
    DataValidationConstraint activationDateConstraint =
            validationHelper.createDateConstraint(DataValidationConstraint.OperatorType.BETWEEN,
                    "=VLOOKUP($D1,$AJ$2:$AK" + (offices.size() + 1) + ",2,FALSE)", "=TODAY()", "dd/mm/yy");
    DataValidationConstraint dobDateConstraint =
            validationHelper.createDateConstraint(DataValidationConstraint.OperatorType.LESS_OR_EQUAL,
                    "=TODAY()",null, "dd/mm/yy");
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
    officeGroup.setRefersToFormula("Offices!$B$2:$B$" + (offices.size() + 1));

    Name clientTypeGroup = clientWorkbook.createName();
    clientTypeGroup.setNameName("ClientTypes");
    clientTypeGroup.setRefersToFormula("ClientPerson!$AN$2:$AN$" + (clientTypeCodeValues.size() + 1));

    Name genderGroup = clientWorkbook.createName();
    genderGroup.setNameName("Gender");
    genderGroup.setRefersToFormula("ClientPerson!$AL$2:$AL$" + (genderCodeValues.size() + 1));

    Name clientClassficationGroup = clientWorkbook.createName();
    clientClassficationGroup.setNameName("ClientClassification");
    clientClassficationGroup.setRefersToFormula("ClientPerson!$AM$2:$AM$" + (clientClassificationCodeValues.size() + 1));

    Name addressTypeGroup = clientWorkbook.createName();
    addressTypeGroup.setNameName("AddressType");
    addressTypeGroup.setRefersToFormula("ClientPerson!$AO$2:$AO$" + (addressTypesCodeValues.size() + 1));

    Name stateProvinceGroup = clientWorkbook.createName();
    stateProvinceGroup.setNameName("StateProvince");
    stateProvinceGroup.setRefersToFormula("ClientPerson!$AP$2:$AP$" + (stateProvinceCodeValues.size() + 1));

    Name countryGroup = clientWorkbook.createName();
    countryGroup.setNameName("Country");
    countryGroup.setRefersToFormula("ClientPerson!$AQ$2:$AQ$" + (countryCodeValues.size() + 1));

    for (Integer i = 0; i < offices.size(); i++) {
      Integer[] officeNameToBeginEndIndexesOfStaff =
              personnelSheetPopulator.getOfficeNameToBeginEndIndexesOfStaff().get(i);
      if (officeNameToBeginEndIndexesOfStaff != null) {
        Name name = clientWorkbook.createName();
        name.setNameName("Staff_" + offices.get(i).name().trim().replaceAll("[ )(]", "_"));
        name.setRefersToFormula("Staff!$B$" + officeNameToBeginEndIndexesOfStaff[0] + ":$B$"
                + officeNameToBeginEndIndexesOfStaff[1]);
      }
    }
  }

}