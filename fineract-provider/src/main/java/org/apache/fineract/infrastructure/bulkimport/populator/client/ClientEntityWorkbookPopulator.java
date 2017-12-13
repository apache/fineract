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

import org.apache.fineract.infrastructure.bulkimport.constants.ClientEntityConstants;
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

public class ClientEntityWorkbookPopulator extends AbstractWorkbookPopulator {

    private OfficeSheetPopulator officeSheetPopulator;
    private PersonnelSheetPopulator personnelSheetPopulator;
    private List<CodeValueData> clientTypeCodeValues;
    private List<CodeValueData> constitutionCodeValues;
    private List<CodeValueData> clientClassificationCodeValues;
    private List<CodeValueData> addressTypesCodeValues;
    private List<CodeValueData> stateProvinceCodeValues;
    private List<CodeValueData> countryCodeValues;
    private List<CodeValueData> mainBusinesslineCodeValues;


    public ClientEntityWorkbookPopulator(OfficeSheetPopulator officeSheetPopulator,
            PersonnelSheetPopulator personnelSheetPopulator,List<CodeValueData>clientTypeCodeValues,
            List<CodeValueData>constitutionCodeValues,List<CodeValueData>mainBusinessline ,
            List<CodeValueData>clientClassification,List<CodeValueData>addressTypesCodeValues,
            List<CodeValueData>stateProvinceCodeValues,List<CodeValueData>countryCodeValues ) {
        this.officeSheetPopulator = officeSheetPopulator;
        this.personnelSheetPopulator = personnelSheetPopulator;
        this.clientTypeCodeValues=clientTypeCodeValues;
        this.constitutionCodeValues=constitutionCodeValues;
        this.clientClassificationCodeValues=clientClassification;
        this.addressTypesCodeValues=addressTypesCodeValues;
        this.stateProvinceCodeValues=stateProvinceCodeValues;
        this.countryCodeValues=countryCodeValues;
        this.mainBusinesslineCodeValues=mainBusinessline;
    }


    @Override
    public void populate(Workbook workbook,String dateFormat) {
        Sheet clientSheet = workbook.createSheet(TemplatePopulateImportConstants.CLIENT_ENTITY_SHEET_NAME);
        personnelSheetPopulator.populate(workbook,dateFormat);
        officeSheetPopulator.populate(workbook,dateFormat);
        setLayout(clientSheet);
        setOfficeDateLookupTable(clientSheet, officeSheetPopulator.getOffices(),
                ClientEntityConstants.RELATIONAL_OFFICE_NAME_COL, ClientEntityConstants.RELATIONAL_OFFICE_OPENING_DATE_COL,dateFormat);
        setClientDataLookupTable(clientSheet);
        setRules(clientSheet,dateFormat);
    }

    private void setClientDataLookupTable(Sheet clientSheet) {
        int rowIndex=0;
        for (CodeValueData clientTypeCodeValue:clientTypeCodeValues) {
            Row row =clientSheet.getRow(++rowIndex);
            if(row==null)
                row=clientSheet.createRow(rowIndex);
            writeString(ClientEntityConstants.LOOKUP_CLIENT_TYPES,row,clientTypeCodeValue.getName()+"-"+clientTypeCodeValue.getId());
        }
        rowIndex=0;
        for (CodeValueData clientClassificationCodeValue:clientClassificationCodeValues) {
            Row row =clientSheet.getRow(++rowIndex);
            if(row==null)
                row=clientSheet.createRow(rowIndex);
            writeString(ClientEntityConstants.LOOKUP_CLIENT_CLASSIFICATION,row,
                    clientClassificationCodeValue.getName()+"-"+clientClassificationCodeValue.getId());
        }
        rowIndex=0;
        for (CodeValueData constitutionCodeValue:constitutionCodeValues) {
            Row row =clientSheet.getRow(++rowIndex);
            if(row==null)
                row=clientSheet.createRow(rowIndex);
            writeString(ClientEntityConstants.LOOKUP_CONSTITUTION_COL,row,
                    constitutionCodeValue.getName()+"-"+constitutionCodeValue.getId());
        }
        rowIndex=0;
        for (CodeValueData mainBusinessCodeValue:mainBusinesslineCodeValues) {
            Row row =clientSheet.getRow(++rowIndex);
            if(row==null)
                row=clientSheet.createRow(rowIndex);
            writeString(ClientEntityConstants.LOOKUP_MAIN_BUSINESS_LINE,row,
                    mainBusinessCodeValue.getName()+"-"+mainBusinessCodeValue.getId());
        }
        rowIndex=0;
        for (CodeValueData addressTypeCodeValue:addressTypesCodeValues) {
            Row row =clientSheet.getRow(++rowIndex);
            if(row==null)
                row=clientSheet.createRow(rowIndex);
            writeString(ClientEntityConstants.LOOKUP_ADDRESS_TYPE,row,
                    addressTypeCodeValue.getName()+"-"+addressTypeCodeValue.getId());
        }
        rowIndex=0;
        for (CodeValueData stateCodeValue:stateProvinceCodeValues) {
            Row row =clientSheet.getRow(++rowIndex);
            if(row==null)
                row=clientSheet.createRow(rowIndex);
            writeString(ClientEntityConstants.LOOKUP_STATE_PROVINCE,row,
                    stateCodeValue.getName()+"-"+stateCodeValue.getId());
        }
        rowIndex=0;
        for (CodeValueData countryCodeValue: countryCodeValues) {
            Row row =clientSheet.getRow(++rowIndex);
            if(row==null)
                row=clientSheet.createRow(rowIndex);
            writeString(ClientEntityConstants.LOOKUP_COUNTRY,row,
                    countryCodeValue.getName()+"-"+countryCodeValue.getId());
        }

    }

    private void setLayout(Sheet worksheet) {
        Row rowHeader = worksheet.createRow(TemplatePopulateImportConstants.ROWHEADER_INDEX);
        rowHeader.setHeight(TemplatePopulateImportConstants.ROW_HEADER_HEIGHT);
        worksheet.setColumnWidth(ClientEntityConstants.NAME_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        writeString(ClientEntityConstants.NAME_COL, rowHeader, "Name*");
        worksheet.setColumnWidth(ClientEntityConstants.OFFICE_NAME_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.STAFF_NAME_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.INCOPORATION_DATE_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.INCOPORATION_VALID_TILL_COL,TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.MOBILE_NO_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.CLIENT_TYPE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.CLIENT_CLASSIFICATION_COL,TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.INCOPORATION_NUMBER_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.MAIN_BUSINESS_LINE,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.CONSTITUTION_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.REMARKS_COL,TemplatePopulateImportConstants.LARGE_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.EXTERNAL_ID_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.SUBMITTED_ON_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.ACTIVE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.ACTIVATION_DATE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.ADDRESS_ENABLED,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.ADDRESS_TYPE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.STREET_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.ADDRESS_LINE_1_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.ADDRESS_LINE_2_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.ADDRESS_LINE_3_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.CITY_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.STATE_PROVINCE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.COUNTRY_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.POSTAL_CODE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.IS_ACTIVE_ADDRESS_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.WARNING_COL,TemplatePopulateImportConstants.MEDIUM_COL_SIZE);

        worksheet.setColumnWidth(ClientEntityConstants.RELATIONAL_OFFICE_NAME_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.RELATIONAL_OFFICE_OPENING_DATE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.LOOKUP_CONSTITUTION_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.LOOKUP_CLIENT_TYPES,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.LOOKUP_CLIENT_CLASSIFICATION,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.LOOKUP_ADDRESS_TYPE,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.LOOKUP_STATE_PROVINCE,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.LOOKUP_COUNTRY,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ClientEntityConstants.LOOKUP_MAIN_BUSINESS_LINE,TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        writeString(ClientEntityConstants.NAME_COL,rowHeader,"Name");
        writeString(ClientEntityConstants.OFFICE_NAME_COL, rowHeader, "Office Name*");
        writeString(ClientEntityConstants.STAFF_NAME_COL, rowHeader, "Staff Name");
        writeString(ClientEntityConstants.INCOPORATION_DATE_COL,rowHeader,"Incorporation Date");
        writeString(ClientEntityConstants.INCOPORATION_VALID_TILL_COL,rowHeader,"Incorporation Validity Till Date");
        writeString(ClientEntityConstants.MOBILE_NO_COL, rowHeader, "Mobile number");
        writeString(ClientEntityConstants.CLIENT_TYPE_COL, rowHeader, "Client Type ");
        writeString(ClientEntityConstants.CLIENT_CLASSIFICATION_COL, rowHeader, "Client Classification ");
        writeString(ClientEntityConstants.INCOPORATION_NUMBER_COL,rowHeader,"Incorporation Number");
        writeString(ClientEntityConstants.MAIN_BUSINESS_LINE,rowHeader,"Main Business Line");
        writeString(ClientEntityConstants.CONSTITUTION_COL,rowHeader,"Constitution");
        writeString(ClientEntityConstants.REMARKS_COL,rowHeader,"Remarks");
        writeString(ClientEntityConstants.EXTERNAL_ID_COL, rowHeader, "External ID ");
        writeString(ClientEntityConstants.SUBMITTED_ON_COL,rowHeader,"Submitted On Date");
        writeString(ClientEntityConstants.ACTIVE_COL, rowHeader, "Active*");
        writeString(ClientEntityConstants.ACTIVATION_DATE_COL, rowHeader, "Activation Date ");
        writeString(ClientEntityConstants.ADDRESS_ENABLED,rowHeader,"Address Enabled ");
        writeString(ClientEntityConstants.ADDRESS_TYPE_COL, rowHeader, "Address Type ");
        writeString(ClientEntityConstants.STREET_COL, rowHeader, "Street  ");
        writeString(ClientEntityConstants.ADDRESS_LINE_1_COL, rowHeader, "Address Line 1");
        writeString(ClientEntityConstants.ADDRESS_LINE_2_COL, rowHeader, "Address Line 2");
        writeString(ClientEntityConstants.ADDRESS_LINE_3_COL, rowHeader, "Address Line 3");
        writeString(ClientEntityConstants.CITY_COL, rowHeader, "City");
        writeString(ClientEntityConstants.STATE_PROVINCE_COL, rowHeader, "State/ Province");
        writeString(ClientEntityConstants.COUNTRY_COL, rowHeader, "Country");
        writeString(ClientEntityConstants.POSTAL_CODE_COL, rowHeader, "Postal Code");
        writeString(ClientEntityConstants.IS_ACTIVE_ADDRESS_COL, rowHeader, "Is active Address ? ");
        writeString(ClientEntityConstants.WARNING_COL, rowHeader, "All * marked fields are compulsory.");

        writeString(ClientEntityConstants.RELATIONAL_OFFICE_NAME_COL, rowHeader, "Lookup office Name  ");
        writeString(ClientEntityConstants.RELATIONAL_OFFICE_OPENING_DATE_COL, rowHeader, "Lookup Office Opened Date ");
        writeString(ClientEntityConstants.LOOKUP_CONSTITUTION_COL, rowHeader, "Lookup Constitution ");
        writeString(ClientEntityConstants.LOOKUP_CLIENT_TYPES, rowHeader, "Lookup Client Types ");
        writeString(ClientEntityConstants.LOOKUP_CLIENT_CLASSIFICATION, rowHeader, "Lookup Client Classification ");
        writeString(ClientEntityConstants.LOOKUP_ADDRESS_TYPE, rowHeader, "Lookup AddressType ");
        writeString(ClientEntityConstants.LOOKUP_STATE_PROVINCE, rowHeader, "Lookup State/Province ");
        writeString(ClientEntityConstants.LOOKUP_COUNTRY, rowHeader, "Lookup Country ");
        writeString(ClientEntityConstants.LOOKUP_MAIN_BUSINESS_LINE,rowHeader,"Lookup Business Line");


    }

    private void setRules(Sheet worksheet,String dateFormat) {
        CellRangeAddressList officeNameRange = new CellRangeAddressList(1,
                SpreadsheetVersion.EXCEL97.getLastRowIndex(), ClientEntityConstants.OFFICE_NAME_COL,
                ClientEntityConstants.OFFICE_NAME_COL);
        CellRangeAddressList staffNameRange = new CellRangeAddressList(1,
                SpreadsheetVersion.EXCEL97.getLastRowIndex(), ClientEntityConstants.STAFF_NAME_COL, ClientEntityConstants.STAFF_NAME_COL);
        CellRangeAddressList submittedOnDateRange = new CellRangeAddressList(1,
                SpreadsheetVersion.EXCEL97.getLastRowIndex(), ClientEntityConstants.SUBMITTED_ON_COL,ClientEntityConstants. SUBMITTED_ON_COL);
        CellRangeAddressList dateRange = new CellRangeAddressList(1,
                SpreadsheetVersion.EXCEL97.getLastRowIndex(),ClientEntityConstants. ACTIVATION_DATE_COL,ClientEntityConstants. ACTIVATION_DATE_COL);
        CellRangeAddressList activeRange = new CellRangeAddressList(1,
                SpreadsheetVersion.EXCEL97.getLastRowIndex(),ClientEntityConstants. ACTIVE_COL,ClientEntityConstants. ACTIVE_COL);
        CellRangeAddressList clientTypeRange=new CellRangeAddressList(1,
                SpreadsheetVersion.EXCEL97.getLastRowIndex(),ClientEntityConstants. CLIENT_TYPE_COL,ClientEntityConstants. CLIENT_TYPE_COL);
        CellRangeAddressList constitutionRange=new CellRangeAddressList(1,
                SpreadsheetVersion.EXCEL97.getLastRowIndex(), ClientEntityConstants.CONSTITUTION_COL,ClientEntityConstants. CONSTITUTION_COL);
        CellRangeAddressList mainBusinessLineRange=new CellRangeAddressList(1,
                SpreadsheetVersion.EXCEL97.getLastRowIndex(),ClientEntityConstants. MAIN_BUSINESS_LINE,ClientEntityConstants. MAIN_BUSINESS_LINE);
        CellRangeAddressList clientClassificationRange=new CellRangeAddressList(1,
                SpreadsheetVersion.EXCEL97.getLastRowIndex(), ClientEntityConstants.CLIENT_CLASSIFICATION_COL,
                ClientEntityConstants.CLIENT_CLASSIFICATION_COL);
        CellRangeAddressList enabledAddressRange=new CellRangeAddressList(1,
                SpreadsheetVersion.EXCEL97.getLastRowIndex(), ClientEntityConstants.ADDRESS_ENABLED, ClientEntityConstants.ADDRESS_ENABLED);
        CellRangeAddressList addressTypeRange=new CellRangeAddressList(1,
                SpreadsheetVersion.EXCEL97.getLastRowIndex(), ClientEntityConstants.ADDRESS_TYPE_COL,ClientEntityConstants. ADDRESS_TYPE_COL);
        CellRangeAddressList stateProvinceRange=new CellRangeAddressList(1,
                SpreadsheetVersion.EXCEL97.getLastRowIndex(), ClientEntityConstants.STATE_PROVINCE_COL,ClientEntityConstants. STATE_PROVINCE_COL);
        CellRangeAddressList countryRange=new CellRangeAddressList(1,
                SpreadsheetVersion.EXCEL97.getLastRowIndex(),ClientEntityConstants. COUNTRY_COL,ClientEntityConstants. COUNTRY_COL);
        CellRangeAddressList activeAddressRange=new CellRangeAddressList(1,
                SpreadsheetVersion.EXCEL97.getLastRowIndex(),ClientEntityConstants. IS_ACTIVE_ADDRESS_COL,ClientEntityConstants. IS_ACTIVE_ADDRESS_COL);
        CellRangeAddressList incorporateDateRange=new CellRangeAddressList(1,
                SpreadsheetVersion.EXCEL97.getLastRowIndex(), ClientEntityConstants.INCOPORATION_DATE_COL,ClientEntityConstants.INCOPORATION_DATE_COL);
        CellRangeAddressList incorporateDateTillRange=new CellRangeAddressList(1,
                SpreadsheetVersion.EXCEL97.getLastRowIndex(), ClientEntityConstants.INCOPORATION_VALID_TILL_COL,
                ClientEntityConstants.INCOPORATION_VALID_TILL_COL);


        DataValidationHelper validationHelper = new HSSFDataValidationHelper((HSSFSheet) worksheet);

        List<OfficeData> offices = officeSheetPopulator.getOffices();
        setNames(worksheet, offices);

        DataValidationConstraint officeNameConstraint =
                validationHelper.createFormulaListConstraint("Office");
        DataValidationConstraint staffNameConstraint =
                validationHelper.createFormulaListConstraint("INDIRECT(CONCATENATE(\"Staff_\",$B1))");
        DataValidationConstraint submittedOnDateConstraint =
                validationHelper.createDateConstraint(DataValidationConstraint.OperatorType.LESS_OR_EQUAL,
                        "=$O1" ,null, dateFormat);
        DataValidationConstraint activationDateConstraint =
                validationHelper.createDateConstraint(DataValidationConstraint.OperatorType.BETWEEN,
                        "=VLOOKUP($B1,$AJ$2:$AK" + (offices.size() + 1) + ",2,FALSE)", "=TODAY()", dateFormat);
        DataValidationConstraint activeConstraint =
                validationHelper.createExplicitListConstraint(new String[] {"True", "False"});
        DataValidationConstraint clientTypesConstraint =
                validationHelper.createFormulaListConstraint("ClientTypes");
        DataValidationConstraint constitutionConstraint =
                validationHelper.createFormulaListConstraint("Constitution");
        DataValidationConstraint mainBusinessLineConstraint =
                validationHelper.createFormulaListConstraint("MainBusinessLine");
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
        DataValidationConstraint incorpDateConstraint=
                validationHelper.createDateConstraint(DataValidationConstraint.OperatorType.LESS_OR_EQUAL,
                        "=TODAY()",null,dateFormat);
        DataValidationConstraint incorpDateTillConstraint=
                validationHelper.createDateConstraint(DataValidationConstraint.OperatorType.GREATER_OR_EQUAL,
                        "=TODAY()",null,dateFormat);


        DataValidation officeValidation =
                validationHelper.createValidation(officeNameConstraint, officeNameRange);
        DataValidation staffValidation =
                validationHelper.createValidation(staffNameConstraint, staffNameRange);
        DataValidation submittedOnDateValidation =
                validationHelper.createValidation(submittedOnDateConstraint, submittedOnDateRange);
        DataValidation activationDateValidation =
                validationHelper.createValidation(activationDateConstraint, dateRange);
        DataValidation activeValidation =
                validationHelper.createValidation(activeConstraint, activeRange);
        DataValidation clientTypeValidation =
                validationHelper.createValidation(clientTypesConstraint, clientTypeRange);
        DataValidation constitutionValidation =
                validationHelper.createValidation(constitutionConstraint,constitutionRange);
        DataValidation mainBusinessLineValidation =
                validationHelper.createValidation(mainBusinessLineConstraint,mainBusinessLineRange);
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
        DataValidation incorporateDateValidation=validationHelper.createValidation(incorpDateConstraint,incorporateDateRange);
        DataValidation incorporateDateTillValidation=validationHelper.createValidation(incorpDateTillConstraint,incorporateDateTillRange);

        worksheet.addValidationData(activeValidation);
        worksheet.addValidationData(officeValidation);
        worksheet.addValidationData(staffValidation);
        worksheet.addValidationData(activationDateValidation);
        worksheet.addValidationData(submittedOnDateValidation);
        worksheet.addValidationData(clientTypeValidation);
        worksheet.addValidationData(constitutionValidation);
        worksheet.addValidationData(mainBusinessLineValidation);
        worksheet.addValidationData(clientClassificationValidation);
        worksheet.addValidationData(enabledAddressValidation);
        worksheet.addValidationData(addressTypeValidation);
        worksheet.addValidationData(stateProvinceValidation);
        worksheet.addValidationData(countryValidation);
        worksheet.addValidationData(activeAddressValidation);
        worksheet.addValidationData(incorporateDateValidation);
        worksheet.addValidationData(incorporateDateTillValidation);
    }

    private void setNames(Sheet worksheet, List<OfficeData> offices) {
        Workbook clientWorkbook = worksheet.getWorkbook();
        Name officeGroup = clientWorkbook.createName();
        officeGroup.setNameName("Office");
        officeGroup.setRefersToFormula(TemplatePopulateImportConstants.OFFICE_SHEET_NAME+"!$B$2:$B$" + (offices.size() + 1));

        Name clientTypeGroup = clientWorkbook.createName();
        clientTypeGroup.setNameName("ClientTypes");
        clientTypeGroup.setRefersToFormula(TemplatePopulateImportConstants.CLIENT_ENTITY_SHEET_NAME+"!$AN$2:$AN$" +
                (clientTypeCodeValues.size() + 1));

        Name constitutionGroup = clientWorkbook.createName();
        constitutionGroup.setNameName("Constitution");
        constitutionGroup.setRefersToFormula(TemplatePopulateImportConstants.CLIENT_ENTITY_SHEET_NAME+"!$AL$2:$AL$" +
                (constitutionCodeValues.size() + 1));

        Name mainBusinessLineGroup = clientWorkbook.createName();
        mainBusinessLineGroup.setNameName("MainBusinessLine");
        mainBusinessLineGroup.setRefersToFormula(TemplatePopulateImportConstants.CLIENT_ENTITY_SHEET_NAME+"!$AR$2:$AR$" +
                (mainBusinesslineCodeValues.size() + 1));

        Name clientClassficationGroup = clientWorkbook.createName();
        clientClassficationGroup.setNameName("ClientClassification");
        clientClassficationGroup.setRefersToFormula(TemplatePopulateImportConstants.CLIENT_ENTITY_SHEET_NAME+"!$AM$2:$AM$" +
                (clientClassificationCodeValues.size() + 1));

        Name addressTypeGroup = clientWorkbook.createName();
        addressTypeGroup.setNameName("AddressType");
        addressTypeGroup.setRefersToFormula(TemplatePopulateImportConstants.CLIENT_ENTITY_SHEET_NAME+
                "!$AO$2:$AO$" + (addressTypesCodeValues.size() + 1));

        Name stateProvinceGroup = clientWorkbook.createName();
        stateProvinceGroup.setNameName("StateProvince");
        stateProvinceGroup.setRefersToFormula(TemplatePopulateImportConstants.CLIENT_ENTITY_SHEET_NAME+
                "!$AP$2:$AP$" + (stateProvinceCodeValues.size() + 1));

        Name countryGroup = clientWorkbook.createName();
        countryGroup.setNameName("Country");
        countryGroup.setRefersToFormula(TemplatePopulateImportConstants.CLIENT_ENTITY_SHEET_NAME+"!$AQ$2:$AQ$" +
                (countryCodeValues.size() + 1));

        for (Integer i = 0; i < offices.size(); i++) {
            Integer[] officeNameToBeginEndIndexesOfStaff =
                    personnelSheetPopulator.getOfficeNameToBeginEndIndexesOfStaff().get(i);
            if (officeNameToBeginEndIndexesOfStaff != null) {
                Name name = clientWorkbook.createName();
                name.setNameName("Staff_" + offices.get(i).name().trim().replaceAll("[ )(]", "_"));
                name.setRefersToFormula(TemplatePopulateImportConstants.STAFF_SHEET_NAME+"!$B$" +
                        officeNameToBeginEndIndexesOfStaff[0] + ":$B$"
                        + officeNameToBeginEndIndexesOfStaff[1]);
            }
        }
    }

}
