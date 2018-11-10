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
package org.apache.fineract.infrastructure.bulkimport.importhandler.recurringdeposit;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.constants.RecurringDepositConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.data.Count;
import org.apache.fineract.infrastructure.bulkimport.importhandler.ImportHandler;
import org.apache.fineract.infrastructure.bulkimport.importhandler.ImportHandlerUtils;
import org.apache.fineract.infrastructure.bulkimport.importhandler.helper.DateSerializer;
import org.apache.fineract.infrastructure.bulkimport.importhandler.helper.EnumOptionDataIdSerializer;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.exception.*;
import org.apache.fineract.portfolio.savings.data.RecurringDepositAccountData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountChargeData;
import org.apache.fineract.portfolio.savings.data.SavingsActivation;
import org.apache.fineract.portfolio.savings.data.SavingsApproval;
import org.apache.poi.ss.usermodel.*;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
@Service
public class RecurringDepositImportHandler implements ImportHandler {

    private Workbook workbook;
    private List<RecurringDepositAccountData> savings;
    private List<SavingsApproval> approvalDates;
    private List<SavingsActivation> activationDates;
    private List<String> statuses;

    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public RecurringDepositImportHandler(final PortfolioCommandSourceWritePlatformService
            commandsSourceWritePlatformService) {
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;

    }
    @Override
    public Count process(Workbook workbook, String locale, String dateFormat) {
        this.workbook=workbook;
        savings=new ArrayList<>();
        approvalDates=new ArrayList<>();
        activationDates=new ArrayList<>();
        statuses=new ArrayList<>();
        readExcelFile(locale,dateFormat);
        return importEntity(dateFormat);
    }

    public void readExcelFile(String locale, String dateFormat) {
        Sheet savingsSheet = workbook.getSheet(TemplatePopulateImportConstants.RECURRING_DEPOSIT_SHEET_NAME);
        Integer noOfEntries = ImportHandlerUtils.getNumberOfRows(savingsSheet, TemplatePopulateImportConstants.ROWHEADER_INDEX);
        for (int rowIndex = 1; rowIndex <= noOfEntries; rowIndex++) {
            Row row;
                row = savingsSheet.getRow(rowIndex);
                if (ImportHandlerUtils.isNotImported(row, RecurringDepositConstants.STATUS_COL)) {
                    savings.add(readSavings(row,locale,dateFormat));
                    approvalDates.add(readSavingsApproval(row,locale,dateFormat));
                    activationDates.add(readSavingsActivation(row,locale,dateFormat));
                }
        }

    }

    private SavingsActivation readSavingsActivation(Row row,String locale, String dateFormat) {
        LocalDate activationDate = ImportHandlerUtils.readAsDate(RecurringDepositConstants.ACTIVATION_DATE_COL, row);
        if (activationDate!=null)
            return SavingsActivation.importInstance(activationDate, row.getRowNum(),locale,dateFormat);
        else
            return null;
    }

    private SavingsApproval readSavingsApproval(Row row,String locale, String dateFormat) {
        LocalDate approvalDate = ImportHandlerUtils.readAsDate(RecurringDepositConstants.APPROVED_DATE_COL, row);
        if (approvalDate!=null)
            return SavingsApproval.importInstance(approvalDate, row.getRowNum(),locale,dateFormat);
        else
            return null;
    }

    private RecurringDepositAccountData readSavings(Row row,String locale, String dateFormat) {

        String productName = ImportHandlerUtils.readAsString(RecurringDepositConstants.PRODUCT_COL, row);
        Long productId = ImportHandlerUtils.getIdByName(workbook.getSheet(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME), productName);
        String fieldOfficerName = ImportHandlerUtils.readAsString(RecurringDepositConstants.FIELD_OFFICER_NAME_COL, row);
        Long fieldOfficerId = ImportHandlerUtils.getIdByName(workbook.getSheet(TemplatePopulateImportConstants.STAFF_SHEET_NAME), fieldOfficerName);
        LocalDate submittedOnDate = ImportHandlerUtils.readAsDate(RecurringDepositConstants.SUBMITTED_ON_DATE_COL, row);
        String interestCompoundingPeriodType = ImportHandlerUtils.readAsString(RecurringDepositConstants.INTEREST_COMPOUNDING_PERIOD_COL, row);
        Long interestCompoundingPeriodTypeId = null;
        EnumOptionData interestCompoundingPeriodTypeEnum=null;
        if (interestCompoundingPeriodType!=null) {
            if (interestCompoundingPeriodType.equalsIgnoreCase("Daily"))
                interestCompoundingPeriodTypeId = 1L;
            else if (interestCompoundingPeriodType.equalsIgnoreCase("Monthly"))
                interestCompoundingPeriodTypeId = 4L;
            else if (interestCompoundingPeriodType.equalsIgnoreCase("Quarterly"))
                interestCompoundingPeriodTypeId = 5L;
            else if (interestCompoundingPeriodType.equalsIgnoreCase("Semi-Annual"))
                interestCompoundingPeriodTypeId = 6L;
            else if (interestCompoundingPeriodType.equalsIgnoreCase("Annually"))
                interestCompoundingPeriodTypeId = 7L;
            interestCompoundingPeriodTypeEnum = new EnumOptionData(interestCompoundingPeriodTypeId, null, null);
        }
        String interestPostingPeriodType = ImportHandlerUtils.readAsString(RecurringDepositConstants.INTEREST_POSTING_PERIOD_COL, row);
        Long interestPostingPeriodTypeId = null;
        EnumOptionData interestPostingPeriodTypeEnum=null;
        if (interestPostingPeriodType!=null) {
            if (interestPostingPeriodType.equalsIgnoreCase("Monthly"))
                interestPostingPeriodTypeId = 4L;
            else if (interestPostingPeriodType.equalsIgnoreCase("Quarterly"))
                interestPostingPeriodTypeId = 5L;
            else if (interestPostingPeriodType.equalsIgnoreCase("Annually"))
                interestPostingPeriodTypeId = 7L;
            else if (interestPostingPeriodType.equalsIgnoreCase("BiAnnual"))
                interestPostingPeriodTypeId = 6L;
                interestPostingPeriodTypeEnum = new EnumOptionData(interestPostingPeriodTypeId, null, null);
        }

        String interestCalculationType = ImportHandlerUtils.readAsString(RecurringDepositConstants.INTEREST_CALCULATION_COL, row);
        Long interestCalculationTypeId = null;
        EnumOptionData interestCalculationTypeEnum=null;
        if (interestCalculationType!=null) {
            if (interestCalculationType.equalsIgnoreCase("Daily Balance"))
                interestCalculationTypeId = 1L;
            else if (interestCalculationType.equalsIgnoreCase("Average Daily Balance"))
                interestCalculationTypeId = 2L;
             interestCalculationTypeEnum = new EnumOptionData(interestCalculationTypeId, null, null);
        }
        String interestCalculationDaysInYearType = ImportHandlerUtils.readAsString(RecurringDepositConstants.INTEREST_CALCULATION_DAYS_IN_YEAR_COL, row);
        EnumOptionData interestCalculationDaysInYearTypeEnum=null;
        Long interestCalculationDaysInYearTypeId = null;
        if (interestCalculationDaysInYearType!=null) {
            if (interestCalculationDaysInYearType.equalsIgnoreCase("360 Days"))
                interestCalculationDaysInYearTypeId = 360L;
            else if (interestCalculationDaysInYearType.equalsIgnoreCase("365 Days"))
                interestCalculationDaysInYearTypeId = 365L;
             interestCalculationDaysInYearTypeEnum = new EnumOptionData(interestCalculationDaysInYearTypeId, null, null);
        }
        Integer lockinPeriodFrequency = ImportHandlerUtils.readAsInt(RecurringDepositConstants.LOCKIN_PERIOD_COL, row);
        String lockinPeriodFrequencyType = ImportHandlerUtils.readAsString(RecurringDepositConstants.LOCKIN_PERIOD_FREQUENCY_COL, row);
        Long lockinPeriodFrequencyTypeId = null;
        EnumOptionData lockinPeriodFrequencyTypeEnum=null;
        if (lockinPeriodFrequencyType!=null) {
            if (lockinPeriodFrequencyType.equalsIgnoreCase("Days"))
                lockinPeriodFrequencyTypeId = 0L;
            else if (lockinPeriodFrequencyType.equalsIgnoreCase("Weeks"))
                lockinPeriodFrequencyTypeId = 1L;
            else if (lockinPeriodFrequencyType.equalsIgnoreCase("Months"))
                lockinPeriodFrequencyTypeId = 2L;
            else if (lockinPeriodFrequencyType.equalsIgnoreCase("Years"))
                lockinPeriodFrequencyTypeId = 3L;
             lockinPeriodFrequencyTypeEnum = new EnumOptionData(lockinPeriodFrequencyTypeId, null, null);
        }
        BigDecimal depositAmount=null;
        if (ImportHandlerUtils.readAsDouble(RecurringDepositConstants.RECURRING_DEPOSIT_AMOUNT_COL, row)!=null)
         depositAmount = BigDecimal.valueOf(ImportHandlerUtils.readAsDouble(RecurringDepositConstants.RECURRING_DEPOSIT_AMOUNT_COL, row));
        Integer depositPeriod = ImportHandlerUtils.readAsInt(RecurringDepositConstants.DEPOSIT_PERIOD_COL, row);
        String depositPeriodFrequency = ImportHandlerUtils.readAsString(RecurringDepositConstants.DEPOSIT_PERIOD_FREQUENCY_COL, row);
        Long depositPeriodFrequencyId = null;
        if (depositPeriodFrequency!=null) {
            if (depositPeriodFrequency.equalsIgnoreCase("Days"))
                depositPeriodFrequencyId = 0L;
            else if (depositPeriodFrequency.equalsIgnoreCase("Weeks"))
                depositPeriodFrequencyId = 1L;
            else if (depositPeriodFrequency.equalsIgnoreCase("Months"))
                depositPeriodFrequencyId = 2L;
            else if (depositPeriodFrequency.equalsIgnoreCase("Years"))
                depositPeriodFrequencyId = 3L;
        }
        Integer recurringFrequency = ImportHandlerUtils.readAsInt(RecurringDepositConstants.DEPOSIT_FREQUENCY_COL, row);
        String recurringFrequencyType = ImportHandlerUtils.readAsString(RecurringDepositConstants.DEPOSIT_FREQUENCY_TYPE_COL, row);
        Long recurringFrequencyTypeId = null;
        EnumOptionData recurringFrequencyTypeEnum=null;
        if (recurringFrequencyType!=null) {
            if (recurringFrequencyType.equalsIgnoreCase("Days"))
                recurringFrequencyTypeId = 0L;
            else if (recurringFrequencyType.equalsIgnoreCase("Weeks"))
                recurringFrequencyTypeId = 1L;
            else if (recurringFrequencyType.equalsIgnoreCase("Months"))
                recurringFrequencyTypeId = 2L;
            else if (recurringFrequencyType.equalsIgnoreCase("Years"))
                recurringFrequencyTypeId = 3L;
             recurringFrequencyTypeEnum = new EnumOptionData(recurringFrequencyTypeId, null, null);
        }
        LocalDate depositStartDate = ImportHandlerUtils.readAsDate(RecurringDepositConstants.DEPOSIT_START_DATE_COL, row);
        Boolean allowWithdrawal = ImportHandlerUtils.readAsBoolean(RecurringDepositConstants.ALLOW_WITHDRAWAL_COL, row);
        Boolean isMandatoryDeposit = ImportHandlerUtils.readAsBoolean(RecurringDepositConstants.IS_MANDATORY_DEPOSIT_COL, row);
        Boolean inheritCalendar = ImportHandlerUtils.readAsBoolean(RecurringDepositConstants.FREQ_SAME_AS_GROUP_CENTER_COL, row);
        Boolean adjustAdvancePayments = ImportHandlerUtils.readAsBoolean(RecurringDepositConstants.ADJUST_ADVANCE_PAYMENTS_COL, row);
        String clientName = ImportHandlerUtils.readAsString(RecurringDepositConstants.CLIENT_NAME_COL, row);
        String externalId = ImportHandlerUtils.readAsString(RecurringDepositConstants.EXTERNAL_ID_COL, row);
        List<SavingsAccountChargeData> charges = new ArrayList<>();

        String charge1 = ImportHandlerUtils.readAsString(RecurringDepositConstants.CHARGE_ID_1, row);
        String charge2 = ImportHandlerUtils.readAsString(RecurringDepositConstants.CHARGE_ID_2, row);

        if (charge1!=null) {
            if (ImportHandlerUtils.readAsDouble(RecurringDepositConstants.CHARGE_AMOUNT_1, row)!=null) {
                charges.add(new SavingsAccountChargeData(ImportHandlerUtils.readAsLong(RecurringDepositConstants.CHARGE_ID_1, row),
                        BigDecimal.valueOf(ImportHandlerUtils.readAsDouble(RecurringDepositConstants.CHARGE_AMOUNT_1, row)),
                        ImportHandlerUtils.readAsDate(RecurringDepositConstants.CHARGE_DUE_DATE_1, row)));
            }else {
                charges.add(new SavingsAccountChargeData(ImportHandlerUtils.readAsLong(RecurringDepositConstants.CHARGE_ID_1, row),
                       null,
                        ImportHandlerUtils.readAsDate(RecurringDepositConstants.CHARGE_DUE_DATE_1, row)));
            }
        }

        if (charge2!=null) {
            if (ImportHandlerUtils.readAsDouble(RecurringDepositConstants.CHARGE_AMOUNT_2, row)!=null) {
                charges.add(new SavingsAccountChargeData(ImportHandlerUtils.readAsLong(RecurringDepositConstants.CHARGE_ID_2, row),
                        BigDecimal.valueOf(ImportHandlerUtils.readAsDouble(RecurringDepositConstants.CHARGE_AMOUNT_2, row)),
                        ImportHandlerUtils.readAsDate(RecurringDepositConstants.CHARGE_DUE_DATE_2, row)));
            }else {
                charges.add(new SavingsAccountChargeData(ImportHandlerUtils.readAsLong(RecurringDepositConstants.CHARGE_ID_2, row),
                       null,
                        ImportHandlerUtils.readAsDate(RecurringDepositConstants.CHARGE_DUE_DATE_2, row)));
            }
        }
        String status = ImportHandlerUtils.readAsString(RecurringDepositConstants.STATUS_COL, row);
        statuses.add(status);
        Long clientId = ImportHandlerUtils.getIdByName(workbook.getSheet(TemplatePopulateImportConstants.CLIENT_SHEET_NAME), clientName);
        return RecurringDepositAccountData.importInstance(clientId, productId, fieldOfficerId, submittedOnDate,
                interestCompoundingPeriodTypeEnum,interestPostingPeriodTypeEnum,interestCalculationTypeEnum,
                interestCalculationDaysInYearTypeEnum, lockinPeriodFrequency, lockinPeriodFrequencyTypeEnum,
                depositAmount, depositPeriod, depositPeriodFrequencyId, depositStartDate,
                recurringFrequency, recurringFrequencyTypeEnum, inheritCalendar, isMandatoryDeposit,
                allowWithdrawal, adjustAdvancePayments, externalId,charges, row.getRowNum(),locale,dateFormat);
    }

    public Count importEntity(String dateFormat) {
        Sheet savingsSheet = workbook.getSheet(TemplatePopulateImportConstants.RECURRING_DEPOSIT_SHEET_NAME);
        int successCount=0;
        int errorCount=0;
        int progressLevel = 0;
        Long savingsId=null;
        String errorMessage="";
        for (int i = 0; i < savings.size(); i++) {
            Row row = savingsSheet.getRow(savings.get(i).getRowIndex());
            Cell statusCell = row.createCell(RecurringDepositConstants.STATUS_COL);
            Cell errorReportCell = row.createCell(RecurringDepositConstants.FAILURE_REPORT_COL);
            try {
                String status = statuses.get(i);
                progressLevel = getProgressLevel(status);

                if (progressLevel == 0) {
                    CommandProcessingResult result= importSavings(i,dateFormat);
                    savingsId = result.getSavingsId();
                    progressLevel = 1;
                } else
                    savingsId = ImportHandlerUtils.readAsLong(RecurringDepositConstants.SAVINGS_ID_COL, savingsSheet.getRow(savings.get(i).getRowIndex()));

                if (progressLevel <= 1) progressLevel = importSavingsApproval(savingsId, i,dateFormat);

                if (progressLevel <= 2) progressLevel = importSavingsActivation(savingsId, i,dateFormat);
                successCount++;
                statusCell.setCellValue(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
                statusCell.setCellStyle(ImportHandlerUtils.getCellStyle(workbook, IndexedColors.LIGHT_GREEN));
            }catch (RuntimeException ex){
                errorCount++;
                ex.printStackTrace();
                errorMessage=ImportHandlerUtils.getErrorMessage(ex);
                writeRecurringDepositErrorMessage(savingsId,errorMessage,progressLevel,statusCell,errorReportCell,row);
            }
        }
        setReportHeaders(savingsSheet);
        return Count.instance(successCount,errorCount);
    }

    private void writeRecurringDepositErrorMessage(Long savingsId,String errorMessage,int progressLevel,Cell statusCell,Cell errorReportCell,Row row){
        String status = "";
        if (progressLevel == 0)
            status = TemplatePopulateImportConstants.STATUS_CREATION_FAILED;
        else if (progressLevel == 1)
            status = TemplatePopulateImportConstants.STATUS_APPROVAL_FAILED;
        else if (progressLevel == 2) status = TemplatePopulateImportConstants.STATUS_ACTIVATION_FAILED;
        statusCell.setCellValue(status);
        statusCell.setCellStyle(ImportHandlerUtils.getCellStyle(workbook, IndexedColors.RED));

        if (progressLevel > 0) row.createCell(RecurringDepositConstants.SAVINGS_ID_COL).setCellValue(savingsId);

        errorReportCell.setCellValue(errorMessage);
    }

    private void setReportHeaders(Sheet savingsSheet) {
        savingsSheet.setColumnWidth(RecurringDepositConstants.STATUS_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        Row rowHeader = savingsSheet.getRow(TemplatePopulateImportConstants.ROWHEADER_INDEX);
        ImportHandlerUtils.writeString(RecurringDepositConstants.STATUS_COL, rowHeader, "Status");
        ImportHandlerUtils.writeString(RecurringDepositConstants.SAVINGS_ID_COL, rowHeader, "Savings ID");
        ImportHandlerUtils.writeString(RecurringDepositConstants.FAILURE_REPORT_COL, rowHeader, "Report");
    }

    private int importSavingsActivation(Long savingsId, int i,String dateFormat) {
        if(activationDates.get(i)!=null) {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(LocalDate.class, new DateSerializer(dateFormat));
            String payload = gsonBuilder.create().toJson(activationDates.get(i));
            final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                    .recurringDepositAccountActivation(savingsId)//
                    .withJson(payload) //
                    .build(); //
            final CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        }
        return 3;
    }

    private int importSavingsApproval(Long savingsId, int i, String dateFormat) {
        if(approvalDates.get(i)!=null) {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(LocalDate.class, new DateSerializer(dateFormat));
            String payload = gsonBuilder.create().toJson(approvalDates.get(i));
            final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                    .approveRecurringDepositAccountApplication(savingsId)//
                    .withJson(payload) //
                    .build(); //
            final CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        }
        return 2;
    }

    private CommandProcessingResult importSavings(int i, String dateFormat) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDate.class, new DateSerializer(dateFormat));
        gsonBuilder.registerTypeAdapter(EnumOptionData.class,new EnumOptionDataIdSerializer());
        JsonObject savingsJsonob=gsonBuilder.create().toJsonTree(savings.get(i)).getAsJsonObject();
        savingsJsonob.remove("withdrawalFeeForTransfers");
        String payload=savingsJsonob.toString();
        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .createRecurringDepositAccount() //
                .withJson(payload) //
                .build(); //
        final CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return result;
    }

    private int getProgressLevel(String status) {
        if (status==null || status.equals(TemplatePopulateImportConstants.STATUS_CREATION_FAILED))
            return 0;
        else if (status.equals(TemplatePopulateImportConstants.STATUS_APPROVAL_FAILED))
            return 1;
        else if (status.equals(TemplatePopulateImportConstants.STATUS_ACTIVATION_FAILED)) return 2;
        return 0;
    }


}
