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
package org.apache.fineract.infrastructure.bulkimport.importhandler.guarantor;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.constants.GuarantorConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.data.Count;
import org.apache.fineract.infrastructure.bulkimport.importhandler.ImportHandler;
import org.apache.fineract.infrastructure.bulkimport.importhandler.ImportHandlerUtils;
import org.apache.fineract.infrastructure.bulkimport.importhandler.helper.DateSerializer;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.*;
import org.apache.fineract.portfolio.loanaccount.guarantor.data.GuarantorData;
import org.apache.poi.ss.usermodel.*;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class GuarantorImportHandler implements ImportHandler {
    private Workbook workbook;
    private List<GuarantorData> guarantors;
    private Long loanAccountId ;

    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

@Autowired
    public GuarantorImportHandler(final PortfolioCommandSourceWritePlatformService
        commandsSourceWritePlatformService) {
    this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @Override
    public Count process(Workbook workbook, String locale, String dateFormat) {
        this.workbook=workbook;
        this.guarantors=new ArrayList<>();
        readExcelFile(locale,dateFormat);
        return importEntity(dateFormat);
    }

    public void readExcelFile(final String locale, final String dateFormat) {
        Sheet addGuarantorSheet = workbook.getSheet(TemplatePopulateImportConstants.GUARANTOR_SHEET_NAME);
        Integer noOfEntries = ImportHandlerUtils.getNumberOfRows(addGuarantorSheet, GuarantorConstants.LOAN_ACCOUNT_NO_COL);
        for (int rowIndex = 1; rowIndex <= noOfEntries; rowIndex++) {
            Row row;
                row = addGuarantorSheet.getRow(rowIndex);
                if (ImportHandlerUtils.isNotImported(row, GuarantorConstants.STATUS_COL))
                    guarantors.add(ReadGuarantor(row,locale,dateFormat));

        }
    }

    private GuarantorData ReadGuarantor(Row row,String locale,String dateFormat) {
        String loanaccountInfo=ImportHandlerUtils.readAsString(GuarantorConstants.LOAN_ACCOUNT_NO_COL, row);
        if (loanaccountInfo!=null){
            String loanAccountAr[]=loanaccountInfo.split("-");
            loanAccountId=Long.parseLong(loanAccountAr[0]);
        }
        String guarantorType = ImportHandlerUtils.readAsString(GuarantorConstants.GUARANTO_TYPE_COL, row);

        Integer guarantorTypeId = null;
        if (guarantorType!=null) {
            if (guarantorType.equalsIgnoreCase(TemplatePopulateImportConstants.GUARANTOR_INTERNAL))
                guarantorTypeId = 1;
            else if (guarantorType.equalsIgnoreCase(TemplatePopulateImportConstants.GUARANTOR_EXTERNAL))
                guarantorTypeId = 3;
        }
        String clientName = ImportHandlerUtils.readAsString(GuarantorConstants.ENTITY_ID_COL, row);
        Long entityId = ImportHandlerUtils.getIdByName(workbook.getSheet(TemplatePopulateImportConstants.CLIENT_SHEET_NAME), clientName);
        String clientRelationshipTypeInfo=ImportHandlerUtils.readAsString(GuarantorConstants.CLIENT_RELATIONSHIP_TYPE_COL, row);
        Integer clientRelationshipTypeId=null;
        if (clientRelationshipTypeInfo!=null){
            String clientRelationshipTypeAr[]=clientRelationshipTypeInfo.split("-");
            clientRelationshipTypeId=Integer.parseInt(clientRelationshipTypeAr[1]);
        }
        String firstname = ImportHandlerUtils.readAsString(GuarantorConstants.FIRST_NAME_COL, row);
        String lastname = ImportHandlerUtils.readAsString(GuarantorConstants.LAST_NAME_COL, row);
        String addressLine1 = ImportHandlerUtils.readAsString(GuarantorConstants.ADDRESS_LINE_1_COL, row);
        String addressLine2 = ImportHandlerUtils.readAsString(GuarantorConstants.ADDRESS_LINE_2_COL, row);
        String city = ImportHandlerUtils.readAsString(GuarantorConstants.CITY_COL, row);
        LocalDate dob = ImportHandlerUtils.readAsDate(GuarantorConstants.DOB_COL, row);
        String zip = ImportHandlerUtils.readAsString(GuarantorConstants.ZIP_COL, row);
        Integer savingsId = ImportHandlerUtils.readAsInt(GuarantorConstants.SAVINGS_ID_COL, row);
        BigDecimal amount = BigDecimal.valueOf(ImportHandlerUtils.readAsDouble(GuarantorConstants.AMOUNT, row));

        return GuarantorData.importInstance(guarantorTypeId,clientRelationshipTypeId,entityId,firstname,
                lastname,addressLine1, addressLine2,city,dob,zip,savingsId,amount, row.getRowNum(), loanAccountId,locale,dateFormat);
    }

    public Count importEntity(String dateFormat) {
        Sheet addGuarantorSheet = workbook.getSheet(TemplatePopulateImportConstants.GUARANTOR_SHEET_NAME);
        int successCount=0;
        int errorCount=0;
        String errorMessage="";
        GsonBuilder gsonBuilder=new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDate.class, new DateSerializer(dateFormat));
        for (GuarantorData guarantor : guarantors) {
            try {
                JsonObject guarantorJsonob=gsonBuilder.create().toJsonTree(guarantor).getAsJsonObject();
                guarantorJsonob.remove("status");
                String payload = guarantorJsonob.toString();
                final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                        .createGuarantor(guarantor.getAccountId()) //
                        .withJson(payload) //
                        .build(); //
                final CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
                successCount++;
                Cell statusCell = addGuarantorSheet.getRow(guarantor.getRowIndex()).createCell(GuarantorConstants.STATUS_COL);
                statusCell.setCellValue(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
                statusCell.setCellStyle(ImportHandlerUtils.getCellStyle(workbook, IndexedColors.LIGHT_GREEN));
            }catch (RuntimeException ex){
                errorCount++;
                ex.printStackTrace();
                errorMessage=ImportHandlerUtils.getErrorMessage(ex);
                ImportHandlerUtils.writeErrorMessage(addGuarantorSheet,guarantor.getRowIndex(),errorMessage,GuarantorConstants.STATUS_COL);
            }

        }
                addGuarantorSheet.setColumnWidth(GuarantorConstants.STATUS_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
                ImportHandlerUtils.writeString(GuarantorConstants.STATUS_COL, addGuarantorSheet.getRow(TemplatePopulateImportConstants.ROWHEADER_INDEX),
                TemplatePopulateImportConstants.STATUS_COL_REPORT_HEADER);
                return  Count.instance(successCount,errorCount);
    }


}
