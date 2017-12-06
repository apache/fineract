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
package org.apache.fineract.infrastructure.bulkimport.importhandler.fixeddeposits;

import com.google.gson.*;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.constants.FixedDepositConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.data.Count;
import org.apache.fineract.infrastructure.bulkimport.importhandler.ImportHandler;
import org.apache.fineract.infrastructure.bulkimport.importhandler.ImportHandlerUtils;
import org.apache.fineract.infrastructure.bulkimport.importhandler.helper.DateSerializer;
import org.apache.fineract.infrastructure.bulkimport.importhandler.helper.EnumOptionDataIdSerializer;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.exception.*;
import org.apache.fineract.portfolio.savings.data.*;
import org.apache.poi.ss.usermodel.*;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class FixedDepositImportHandler implements ImportHandler {

    private Workbook workbook;

    private List<FixedDepositAccountData> savings;
    private List<SavingsApproval> approvalDates;
    private List<SavingsActivation> activationDates;
    private List<ClosingOfSavingsAccounts> closedOnDate;
    private List<String> statuses;

    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public FixedDepositImportHandler(final PortfolioCommandSourceWritePlatformService
            commandsSourceWritePlatformService) {
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }


    @Override
    public Count process(Workbook workbook, String locale, String dateFormat) {
        this.workbook = workbook;
        savings=new ArrayList<>();
        approvalDates=new ArrayList<>();
        activationDates=new ArrayList<>();
        closedOnDate=new ArrayList<>();
        statuses=new ArrayList<>();
        readExcelFile(locale,dateFormat);
        return importEntity(dateFormat);
    }

    public void readExcelFile(final String locale, final String dateFormat) {
        Sheet savingsSheet = workbook.getSheet(TemplatePopulateImportConstants.FIXED_DEPOSIT_SHEET_NAME);
        Integer noOfEntries = ImportHandlerUtils.getNumberOfRows(savingsSheet, TemplatePopulateImportConstants.FIRST_COLUMN_INDEX);
        for (int rowIndex = 1; rowIndex <= noOfEntries; rowIndex++) {
            Row row;
                row = savingsSheet.getRow(rowIndex);
                if (ImportHandlerUtils.isNotImported(row, FixedDepositConstants.STATUS_COL)) {
                    savings.add(readSavings(row,locale,dateFormat));
                    approvalDates.add(readSavingsApproval(row,locale,dateFormat));
                    activationDates.add(readSavingsActivation(row,locale,dateFormat));
                    closedOnDate.add(readSavingsClosed(row,locale,dateFormat));
                }
        }

    }

    private ClosingOfSavingsAccounts readSavingsClosed(Row row,String locale,String dateFormat) {
        LocalDate closedOnDate = ImportHandlerUtils.readAsDate(FixedDepositConstants.CLOSED_ON_DATE, row);
        Long onAccountClosureId = ImportHandlerUtils.readAsLong(FixedDepositConstants.ON_ACCOUNT_CLOSURE_ID, row);
        Long toSavingsAccountId = ImportHandlerUtils.readAsLong(FixedDepositConstants.TO_SAVINGS_ACCOUNT_ID, row);
        if (closedOnDate!=null)
            return ClosingOfSavingsAccounts.importInstance(null, closedOnDate,onAccountClosureId,toSavingsAccountId, null,
                    row.getRowNum(),locale,dateFormat);
        else
            return null;
    }

    private SavingsActivation readSavingsActivation(Row row,String locale,String dateFormat) {
        LocalDate activationDate = ImportHandlerUtils.readAsDate(FixedDepositConstants.ACTIVATION_DATE_COL, row);
        if (activationDate!=null)
            return SavingsActivation.importInstance(activationDate, row.getRowNum(),locale,dateFormat);
        else
            return null;
    }

    private SavingsApproval readSavingsApproval(Row row,String locale,String dateFormat) {
        LocalDate approvalDate = ImportHandlerUtils.readAsDate(FixedDepositConstants.APPROVED_DATE_COL, row);
        if (approvalDate!=null)
            return SavingsApproval.importInstance(approvalDate, row.getRowNum(),locale,dateFormat);
        else
            return null;
    }

    private FixedDepositAccountData readSavings(Row row,String locale,String dateFormat) {

        String productName = ImportHandlerUtils.readAsString(FixedDepositConstants.PRODUCT_COL, row);
        Long productId = ImportHandlerUtils.getIdByName(workbook.getSheet(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME), productName);
        String fieldOfficerName = ImportHandlerUtils.readAsString(FixedDepositConstants.FIELD_OFFICER_NAME_COL, row);
        Long fieldOfficerId = ImportHandlerUtils.getIdByName(workbook.getSheet(TemplatePopulateImportConstants.STAFF_SHEET_NAME), fieldOfficerName);
        LocalDate submittedOnDate = ImportHandlerUtils.readAsDate(FixedDepositConstants.SUBMITTED_ON_DATE_COL, row);
        String interestCompoundingPeriodType = ImportHandlerUtils.readAsString(FixedDepositConstants.INTEREST_COMPOUNDING_PERIOD_COL, row);
        Long interestCompoundingPeriodTypeId = null;
        EnumOptionData interestCompoundingPeriodTypeEnum=null;
        if (interestCompoundingPeriodType!=null) {
            if (interestCompoundingPeriodType.equalsIgnoreCase(TemplatePopulateImportConstants.INTEREST_COMPOUNDING_PERIOD_DAILY))
                interestCompoundingPeriodTypeId = 1L;
            else if (interestCompoundingPeriodType.equalsIgnoreCase(TemplatePopulateImportConstants.INTEREST_COMPOUNDING_PERIOD_MONTHLY))
                interestCompoundingPeriodTypeId = 4L;
            else if (interestCompoundingPeriodType.equalsIgnoreCase(TemplatePopulateImportConstants.INTEREST_COMPOUNDING_PERIOD_QUARTERLY))
                interestCompoundingPeriodTypeId = 5L;
            else if (interestCompoundingPeriodType.equalsIgnoreCase(TemplatePopulateImportConstants.INTEREST_COMPOUNDING_PERIOD_SEMI_ANNUALLY))
                interestCompoundingPeriodTypeId = 6L;
            else if (interestCompoundingPeriodType.equalsIgnoreCase(TemplatePopulateImportConstants.INTEREST_COMPOUNDING_PERIOD_ANNUALLY))
                interestCompoundingPeriodTypeId = 7L;
             interestCompoundingPeriodTypeEnum = new EnumOptionData(interestCompoundingPeriodTypeId, null, null);
        }
        String interestPostingPeriodType = ImportHandlerUtils.readAsString(FixedDepositConstants.INTEREST_POSTING_PERIOD_COL, row);
        Long interestPostingPeriodTypeId = null;

        EnumOptionData interestPostingPeriodTypeEnum=null;
        if (interestCompoundingPeriodType!=null) {
            if (interestPostingPeriodType.equalsIgnoreCase(TemplatePopulateImportConstants.INTEREST_POSTING_PERIOD_MONTHLY))
                interestPostingPeriodTypeId = 4L;
            else if (interestPostingPeriodType.equalsIgnoreCase(TemplatePopulateImportConstants.INTEREST_POSTING_PERIOD_QUARTERLY))
                interestPostingPeriodTypeId = 5L;
            else if (interestPostingPeriodType.equalsIgnoreCase(TemplatePopulateImportConstants.INTEREST_COMPOUNDING_PERIOD_ANNUALLY))
                interestPostingPeriodTypeId = 7L;
            else if (interestPostingPeriodType.equalsIgnoreCase(TemplatePopulateImportConstants.INTEREST_POSTING_PERIOD_BIANUALLY))
                interestPostingPeriodTypeId = 6L;
                interestPostingPeriodTypeEnum = new EnumOptionData(interestPostingPeriodTypeId, null, null);
        }
        String interestCalculationType = ImportHandlerUtils.readAsString(FixedDepositConstants.INTEREST_CALCULATION_COL, row);
        EnumOptionData interestCalculationTypeEnum=null;
        if (interestCalculationType!=null) {
            Long interestCalculationTypeId = null;
            if (interestCalculationType.equalsIgnoreCase(TemplatePopulateImportConstants.INTEREST_CAL_DAILY_BALANCE))
                interestCalculationTypeId = 1L;
            else if (interestCalculationType.equalsIgnoreCase(TemplatePopulateImportConstants.INTEREST_CAL_AVG_BALANCE))
                interestCalculationTypeId = 2L;
             interestCalculationTypeEnum = new EnumOptionData(interestCalculationTypeId, null, null);
        }
        String interestCalculationDaysInYearType = ImportHandlerUtils.readAsString(FixedDepositConstants.INTEREST_CALCULATION_DAYS_IN_YEAR_COL, row);
        Long interestCalculationDaysInYearTypeId = null;
        EnumOptionData interestCalculationDaysInYearTypeEnum=null;
        if (interestCalculationDaysInYearType!=null) {
            if (interestCalculationDaysInYearType.equalsIgnoreCase(TemplatePopulateImportConstants.INTEREST_CAL_DAYS_IN_YEAR_360))
                interestCalculationDaysInYearTypeId = 360L;
            else if (interestCalculationDaysInYearType.equalsIgnoreCase(TemplatePopulateImportConstants.INTEREST_CAL_DAYS_IN_YEAR_365))
                interestCalculationDaysInYearTypeId = 365L;
             interestCalculationDaysInYearTypeEnum = new EnumOptionData(interestCalculationDaysInYearTypeId, null, null);
        }
        Integer lockinPeriodFrequency = ImportHandlerUtils.readAsInt(FixedDepositConstants.LOCKIN_PERIOD_COL, row);
        String lockinPeriodFrequencyType = ImportHandlerUtils.readAsString(FixedDepositConstants.LOCKIN_PERIOD_FREQUENCY_COL, row);

        Long lockinPeriodFrequencyTypeId =null;
        EnumOptionData lockinPeriodFrequencyTypeEnum=null;
        if (lockinPeriodFrequencyType!=null) {
            if (lockinPeriodFrequencyType.equalsIgnoreCase(TemplatePopulateImportConstants.FREQUENCY_DAYS))
                lockinPeriodFrequencyTypeId = 0L;
            else if (lockinPeriodFrequencyType.equalsIgnoreCase(TemplatePopulateImportConstants.FREQUENCY_WEEKS))
                lockinPeriodFrequencyTypeId = 1L;
            else if (lockinPeriodFrequencyType.equalsIgnoreCase(TemplatePopulateImportConstants.FREQUENCY_MONTHS))
                lockinPeriodFrequencyTypeId = 2L;
            else if (lockinPeriodFrequencyType.equalsIgnoreCase(TemplatePopulateImportConstants.FREQUENCY_YEARS))
                lockinPeriodFrequencyTypeId = 3L;
             lockinPeriodFrequencyTypeEnum = new EnumOptionData(lockinPeriodFrequencyTypeId, null, null);
        }
        BigDecimal depositAmount=null;
        if (ImportHandlerUtils.readAsDouble(FixedDepositConstants.DEPOSIT_AMOUNT_COL, row)!=null) {
            depositAmount = BigDecimal.valueOf(ImportHandlerUtils.readAsDouble(FixedDepositConstants.DEPOSIT_AMOUNT_COL, row));
        }
        Integer depositPeriod = ImportHandlerUtils.readAsInt(FixedDepositConstants.DEPOSIT_PERIOD_COL, row);

        String depositPeriodFrequency = ImportHandlerUtils.readAsString(FixedDepositConstants.DEPOSIT_PERIOD_FREQUENCY_COL, row);
        Long depositPeriodFrequencyId = null;
        if (depositPeriodFrequency!=null) {
            if (depositPeriodFrequency.equalsIgnoreCase(TemplatePopulateImportConstants.FREQUENCY_DAYS))
                depositPeriodFrequencyId = 0L;
            else if (depositPeriodFrequency.equalsIgnoreCase(TemplatePopulateImportConstants.FREQUENCY_WEEKS))
                depositPeriodFrequencyId = 1L;
            else if (depositPeriodFrequency.equalsIgnoreCase(TemplatePopulateImportConstants.FREQUENCY_MONTHS))
                depositPeriodFrequencyId = 2L;
            else if (depositPeriodFrequency.equalsIgnoreCase(TemplatePopulateImportConstants.FREQUENCY_YEARS))
                depositPeriodFrequencyId = 3L;
        }
        String externalId = ImportHandlerUtils.readAsString(FixedDepositConstants.EXTERNAL_ID_COL, row);
        String clientName = ImportHandlerUtils.readAsString(FixedDepositConstants.CLIENT_NAME_COL, row);

        List<SavingsAccountChargeData> charges = new ArrayList<SavingsAccountChargeData>();

        String charge1 = ImportHandlerUtils.readAsString(FixedDepositConstants.CHARGE_ID_1, row);
        String charge2 = ImportHandlerUtils.readAsString(FixedDepositConstants.CHARGE_ID_2, row);

        if (charge1!=null) {
            if (ImportHandlerUtils.readAsDouble(FixedDepositConstants.CHARGE_AMOUNT_1, row)!=null)
            charges.add(new SavingsAccountChargeData(ImportHandlerUtils.readAsLong(FixedDepositConstants.CHARGE_ID_1, row),
                    BigDecimal.valueOf(ImportHandlerUtils.readAsDouble(FixedDepositConstants.CHARGE_AMOUNT_1, row)),
                    ImportHandlerUtils.readAsDate(FixedDepositConstants.CHARGE_DUE_DATE_1, row)));
        }else {
            charges.add(new SavingsAccountChargeData(ImportHandlerUtils.readAsLong(FixedDepositConstants.CHARGE_ID_1, row),
                    null,
                    ImportHandlerUtils.readAsDate(FixedDepositConstants.CHARGE_DUE_DATE_1, row)));
        }

        if (charge2!=null) {
            if (ImportHandlerUtils.readAsDouble(FixedDepositConstants.CHARGE_AMOUNT_2, row)!=null) {
                charges.add(new SavingsAccountChargeData(ImportHandlerUtils.readAsLong(FixedDepositConstants.CHARGE_ID_2, row),
                        BigDecimal.valueOf(ImportHandlerUtils.readAsDouble(FixedDepositConstants.CHARGE_AMOUNT_2, row)),
                        ImportHandlerUtils.readAsDate(FixedDepositConstants.CHARGE_DUE_DATE_2, row)));
            }else {
                charges.add(new SavingsAccountChargeData(ImportHandlerUtils.readAsLong(FixedDepositConstants.CHARGE_ID_2, row),
                       null,
                        ImportHandlerUtils.readAsDate(FixedDepositConstants.CHARGE_DUE_DATE_2, row)));
            }
        }
        String status = ImportHandlerUtils.readAsString(FixedDepositConstants.STATUS_COL, row);
        statuses.add(status);
        Long clientId = ImportHandlerUtils.getIdByName(workbook.getSheet(TemplatePopulateImportConstants.CLIENT_SHEET_NAME), clientName);
        return FixedDepositAccountData.importInstance (clientId, productId, fieldOfficerId, submittedOnDate,
                interestCompoundingPeriodTypeEnum, interestPostingPeriodTypeEnum, interestCalculationTypeEnum,
                interestCalculationDaysInYearTypeEnum, lockinPeriodFrequency, lockinPeriodFrequencyTypeEnum,
                depositAmount, depositPeriod, depositPeriodFrequencyId, externalId, charges,row.getRowNum(),locale,dateFormat);
    }

    public Count importEntity(String dateFormat) {
        Sheet savingsSheet = workbook.getSheet(TemplatePopulateImportConstants.FIXED_DEPOSIT_SHEET_NAME);
        int successCount=0;
        int errorCount=0;
        String errorMessage="";
        int progressLevel = 0;
        Long savingsId=null;
        for (int i = 0; i < savings.size(); i++) {
            Row row = savingsSheet.getRow(savings.get(i).getRowIndex());
            Cell statusCell = row.createCell(FixedDepositConstants.STATUS_COL);
            Cell errorReportCell = row.createCell(FixedDepositConstants.FAILURE_REPORT_COL);
            try {
                String status = statuses.get(i);
                progressLevel = getProgressLevel(status);

                if (progressLevel == 0) {
                    CommandProcessingResult result= importSavings(i,dateFormat);
                    savingsId = result.getSavingsId();
                    progressLevel = 1;
                } else
                    savingsId = ImportHandlerUtils.readAsLong(FixedDepositConstants.SAVINGS_ID_COL, savingsSheet.getRow(savings.get(i).getRowIndex()));

                if (progressLevel <= 1) progressLevel = importSavingsApproval(savingsId, i,dateFormat);

                if (progressLevel <= 2) progressLevel = importSavingsActivation(savingsId, i,dateFormat);

                if (progressLevel <= 3) progressLevel = importSavingsClosing(savingsId, i,dateFormat);

                successCount++;
                statusCell.setCellValue(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
                statusCell.setCellStyle(ImportHandlerUtils.getCellStyle(workbook, IndexedColors.LIGHT_GREEN));
            }catch (RuntimeException ex){
                errorCount++;
                ex.printStackTrace();
                errorMessage=ImportHandlerUtils.getErrorMessage(ex);
                writeFixedDepositErrorMessage(savingsId,errorMessage,progressLevel,statusCell,errorReportCell,row);
            }

        }
        setReportHeaders(savingsSheet);
        return Count.instance(successCount,errorCount);
    }
    private void writeFixedDepositErrorMessage(Long savingsId,String errorMessage,int progressLevel,Cell statusCell,Cell errorReportCell,Row row){
        String status = "";
        if (progressLevel == 0)
            status = TemplatePopulateImportConstants.STATUS_CREATION_FAILED;
        else if (progressLevel == 1)
            status = TemplatePopulateImportConstants.STATUS_APPROVAL_FAILED;
        else if (progressLevel == 2) status = TemplatePopulateImportConstants.STATUS_ACTIVATION_FAILED;
        statusCell.setCellValue(status);
        statusCell.setCellStyle(ImportHandlerUtils.getCellStyle(workbook, IndexedColors.RED));

        if (progressLevel > 0) row.createCell(FixedDepositConstants.SAVINGS_ID_COL).setCellValue(savingsId);
        errorReportCell.setCellValue(errorMessage);
    }

    private int importSavingsClosing(Long savingsId, int i,String dateFormat) {
        if(closedOnDate.get(i)!=null) {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(LocalDate.class, new DateSerializer(dateFormat));
            String payload = gsonBuilder.create().toJson(closedOnDate.get(i));
            final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                    .closeFixedDepositAccount(savingsId)//
                    .withJson(payload) //
                    .build(); //
            final CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        }
        return 4;
    }

    private CommandProcessingResult importSavings(int i,String dateFormat) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDate.class, new DateSerializer(dateFormat));
        gsonBuilder.registerTypeAdapter(EnumOptionData.class,new EnumOptionDataIdSerializer());
        JsonObject savingsJsonob=gsonBuilder.create().toJsonTree(savings.get(i)).getAsJsonObject();
        savingsJsonob.remove("withdrawalFeeForTransfers");
        JsonArray chargesJsonAr=savingsJsonob.getAsJsonArray("charges");
        for (int j=0;j<chargesJsonAr.size();j++){
            JsonElement chargesJsonElement=chargesJsonAr.get(j);
            JsonObject chargeJsonOb =chargesJsonElement.getAsJsonObject();
            chargeJsonOb.remove("penalty");
        }
        if (chargesJsonAr.get(0).getAsJsonObject().toString().equals("{}"))
            savingsJsonob.remove("charges");
        String payload=savingsJsonob.toString();
        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .createFixedDepositAccount() //
                .withJson(payload) //
                .build(); //
        final CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return result;
    }

    private int importSavingsApproval(Long savingsId, int i,String dateFormat) {
        if(approvalDates.get(i)!=null) {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(LocalDate.class, new DateSerializer(dateFormat));
            String payload = gsonBuilder.create().toJson(approvalDates.get(i));
            final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                    .approveFixedDepositAccountApplication(savingsId)//
                    .withJson(payload) //
                    .build(); //
            final CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        }
        return 2;
    }

    private int importSavingsActivation(Long savingsId, int i, String dateFormat) {
        if(activationDates.get(i)!=null) {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(LocalDate.class, new DateSerializer(dateFormat));
            String payload = gsonBuilder.create().toJson(activationDates.get(i));
            final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                    .fixedDepositAccountActivation(savingsId)//
                    .withJson(payload) //
                    .build(); //
            final CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        }
        return 3;
    }

    private int getProgressLevel(String status) {
        if (status==null || status.equals(TemplatePopulateImportConstants.STATUS_CREATION_FAILED))
            return 0;
        else if (status.equals(TemplatePopulateImportConstants.STATUS_APPROVAL_FAILED))
            return 1;
        else if (status.equals(TemplatePopulateImportConstants.STATUS_ACTIVATION_FAILED)) return 2;
        return 0;
    }

    private void setReportHeaders(Sheet savingsSheet) {
        savingsSheet.setColumnWidth(FixedDepositConstants.STATUS_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        Row rowHeader = savingsSheet.getRow(0);
        ImportHandlerUtils.writeString(FixedDepositConstants.STATUS_COL, rowHeader, TemplatePopulateImportConstants.STATUS_COL_REPORT_HEADER);
        ImportHandlerUtils.writeString(FixedDepositConstants.SAVINGS_ID_COL, rowHeader, TemplatePopulateImportConstants.SAVINGS_ID_COL_REPORT_HEADER);
        ImportHandlerUtils.writeString(FixedDepositConstants.FAILURE_REPORT_COL, rowHeader, TemplatePopulateImportConstants.FAILURE_COL_REPORT_HEADER);
    }


}
