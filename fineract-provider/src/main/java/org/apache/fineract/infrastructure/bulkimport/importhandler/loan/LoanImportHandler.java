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
package org.apache.fineract.infrastructure.bulkimport.importhandler.loan;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.constants.LoanConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.data.Count;
import org.apache.fineract.infrastructure.bulkimport.importhandler.ImportHandler;
import org.apache.fineract.infrastructure.bulkimport.importhandler.ImportHandlerUtils;
import org.apache.fineract.infrastructure.bulkimport.importhandler.helper.DateSerializer;
import org.apache.fineract.infrastructure.bulkimport.importhandler.helper.EnumOptionDataValueSerializer;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.serialization.GoogleGsonSerializerHelper;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.portfolio.loanaccount.data.DisbursementData;
import org.apache.fineract.portfolio.loanaccount.data.LoanAccountData;
import org.apache.fineract.portfolio.loanaccount.data.LoanApprovalData;
import org.apache.fineract.portfolio.loanaccount.data.LoanChargeData;
import org.apache.fineract.portfolio.loanaccount.data.LoanCollateralManagementData;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleTransactionProcessorFactory;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.exception.InvalidAmountOfCollateralQuantity;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class LoanImportHandler implements ImportHandler {

    public static final String EMPTY_STR = "";
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    private final LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory;

    @Override
    public Count process(final Workbook workbook, final String locale, final String dateFormat) {
        List<LoanAccountData> loans = new ArrayList<>();
        List<LoanApprovalData> approvalDates = new ArrayList<>();
        List<LoanTransactionData> loanRepayments = new ArrayList<>();
        List<DisbursementData> disbursalDates = new ArrayList<>();
        List<String> statuses = new ArrayList<>();
        readExcelFile(workbook, loans, approvalDates, loanRepayments, disbursalDates, statuses, locale, dateFormat);
        return importEntity(workbook, loans, approvalDates, loanRepayments, disbursalDates, statuses, dateFormat);
    }

    private void readExcelFile(final Workbook workbook, final List<LoanAccountData> loans, final List<LoanApprovalData> approvalDates,
            final List<LoanTransactionData> loanRepayments, final List<DisbursementData> disbursalDates, List<String> statuses,
            final String locale, final String dateFormat) {
        Sheet loanSheet = workbook.getSheet(TemplatePopulateImportConstants.LOANS_SHEET_NAME);
        Integer noOfEntries = ImportHandlerUtils.getNumberOfRows(loanSheet, TemplatePopulateImportConstants.FIRST_COLUMN_INDEX);
        for (int rowIndex = 1; rowIndex <= noOfEntries; rowIndex++) {
            Row row;
            row = loanSheet.getRow(rowIndex);
            if (ImportHandlerUtils.isNotImported(row, LoanConstants.STATUS_COL)) {
                loans.add(readLoan(workbook, row, statuses, locale, dateFormat));
                approvalDates.add(readLoanApproval(row, locale, dateFormat));
                disbursalDates.add(readDisbursalData(row, locale, dateFormat));
                loanRepayments.add(readLoanRepayment(workbook, row, locale, dateFormat));
            }
        }

    }

    private LoanTransactionData readLoanRepayment(final Workbook workbook, final Row row, final String locale, final String dateFormat) {
        BigDecimal repaymentAmount = null;
        if (ImportHandlerUtils.readAsDouble(LoanConstants.TOTAL_AMOUNT_REPAID_COL, row) != null) {
            repaymentAmount = BigDecimal.valueOf(ImportHandlerUtils.readAsDouble(LoanConstants.TOTAL_AMOUNT_REPAID_COL, row));
        }
        LocalDate lastRepaymentDate = ImportHandlerUtils.readAsDate(LoanConstants.LAST_REPAYMENT_DATE_COL, row);
        String repaymentType = ImportHandlerUtils.readAsString(LoanConstants.REPAYMENT_TYPE_COL, row);
        Long repaymentTypeId = ImportHandlerUtils.getIdByName(workbook.getSheet(TemplatePopulateImportConstants.EXTRAS_SHEET_NAME),
                repaymentType);
        if (repaymentAmount != null && lastRepaymentDate != null && repaymentType != null && repaymentTypeId != null) {
            return LoanTransactionData.importInstance(repaymentAmount, lastRepaymentDate, repaymentTypeId, row.getRowNum(), locale,
                    dateFormat);
        }

        return null;
    }

    private DisbursementData readDisbursalData(final Row row, final String locale, final String dateFormat) {
        LocalDate disbursedDate = ImportHandlerUtils.readAsDate(LoanConstants.DISBURSED_DATE_COL, row);
        String linkAccountId = null;
        if (ImportHandlerUtils.readAsLong(LoanConstants.LINK_ACCOUNT_ID, row) != null) {
            linkAccountId = Objects.requireNonNull(ImportHandlerUtils.readAsLong(LoanConstants.LINK_ACCOUNT_ID, row)).toString();
        }

        if (disbursedDate != null) {
            return DisbursementData.importInstance(disbursedDate, linkAccountId, row.getRowNum(), locale, dateFormat);
        }
        return null;
    }

    private LoanApprovalData readLoanApproval(final Row row, final String locale, final String dateFormat) {
        LocalDate approvedDate = ImportHandlerUtils.readAsDate(LoanConstants.APPROVED_DATE_COL, row);
        if (approvedDate != null) {
            return LoanApprovalData.importInstance(approvedDate, row.getRowNum(), locale, dateFormat);
        }

        return null;
    }

    private LoanAccountData readLoan(final Workbook workbook, final Row row, final List<String> statuses, final String locale,
            final String dateFormat) {
        ExternalId externalId = ExternalIdFactory.produce(ImportHandlerUtils.readAsString(LoanConstants.EXTERNAL_ID_COL, row));
        String status = ImportHandlerUtils.readAsString(LoanConstants.STATUS_COL, row);
        String productName = ImportHandlerUtils.readAsString(LoanConstants.PRODUCT_COL, row);
        Long productId = ImportHandlerUtils.getIdByName(workbook.getSheet(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME), productName);
        String loanOfficerName = ImportHandlerUtils.readAsString(LoanConstants.LOAN_OFFICER_NAME_COL, row);
        Long loanOfficerId = ImportHandlerUtils.getIdByName(workbook.getSheet(TemplatePopulateImportConstants.STAFF_SHEET_NAME),
                loanOfficerName);
        LocalDate submittedOnDate = ImportHandlerUtils.readAsDate(LoanConstants.SUBMITTED_ON_DATE_COL, row);
        String fundName = ImportHandlerUtils.readAsString(LoanConstants.FUND_NAME_COL, row);
        Long fundId;
        if (fundName == null) {
            fundId = null;
        } else {
            fundId = ImportHandlerUtils.getIdByName(workbook.getSheet(TemplatePopulateImportConstants.EXTRAS_SHEET_NAME), fundName);
        }

        BigDecimal principal = null;
        if (ImportHandlerUtils.readAsDouble(LoanConstants.PRINCIPAL_COL, row) != null) {
            principal = BigDecimal.valueOf(ImportHandlerUtils.readAsDouble(LoanConstants.PRINCIPAL_COL, row));
        }
        Integer numberOfRepayments = ImportHandlerUtils.readAsInt(LoanConstants.NO_OF_REPAYMENTS_COL, row);
        Integer repaidEvery = ImportHandlerUtils.readAsInt(LoanConstants.REPAID_EVERY_COL, row);
        String repaidEveryFrequency = ImportHandlerUtils.readAsString(LoanConstants.REPAID_EVERY_FREQUENCY_COL, row);
        String repaidEveryFrequencyId = EMPTY_STR;
        EnumOptionData repaidEveryFrequencyEnums = null;
        if (repaidEveryFrequency != null) {
            if (repaidEveryFrequency.equalsIgnoreCase("Days")) {
                repaidEveryFrequencyId = "0";
            } else if (repaidEveryFrequency.equalsIgnoreCase("Weeks")) {
                repaidEveryFrequencyId = "1";
            } else if (repaidEveryFrequency.equalsIgnoreCase("Months")) {
                repaidEveryFrequencyId = "2";
            } else if (repaidEveryFrequency.equalsIgnoreCase("Semi Month")) {
                repaidEveryFrequencyId = "5";
            }
            repaidEveryFrequencyEnums = new EnumOptionData(null, null, repaidEveryFrequencyId);
        }
        Integer loanTerm = ImportHandlerUtils.readAsInt(LoanConstants.LOAN_TERM_COL, row);
        String loanTermFrequencyType = ImportHandlerUtils.readAsString(LoanConstants.LOAN_TERM_FREQUENCY_COL, row);
        EnumOptionData loanTermFrequencyEnum = null;
        if (loanTermFrequencyType != null) {
            String loanTermFrequencyId = EMPTY_STR;
            if (loanTermFrequencyType.equalsIgnoreCase("Days")) {
                loanTermFrequencyId = "0";
            } else if (loanTermFrequencyType.equalsIgnoreCase("Weeks")) {
                loanTermFrequencyId = "1";
            } else if (loanTermFrequencyType.equalsIgnoreCase("Months")) {
                loanTermFrequencyId = "2";
            }
            loanTermFrequencyEnum = new EnumOptionData(null, null, loanTermFrequencyId);
        }
        BigDecimal nominalInterestRate = null;
        if (ImportHandlerUtils.readAsDouble(LoanConstants.NOMINAL_INTEREST_RATE_COL, row) != null) {
            nominalInterestRate = BigDecimal.valueOf(ImportHandlerUtils.readAsDouble(LoanConstants.NOMINAL_INTEREST_RATE_COL, row));
        }
        String amortization = ImportHandlerUtils.readAsString(LoanConstants.AMORTIZATION_COL, row);
        String amortizationId = EMPTY_STR;
        EnumOptionData amortizationEnumOption = null;
        if (amortization != null) {
            if (amortization.equalsIgnoreCase("Equal principal payments")) {
                amortizationId = "0";
            } else if (amortization.equalsIgnoreCase("Equal installments")) {
                amortizationId = "1";
            }
            amortizationEnumOption = new EnumOptionData(null, null, amortizationId);
        }
        String interestMethod = ImportHandlerUtils.readAsString(LoanConstants.INTEREST_METHOD_COL, row);
        String interestMethodId = EMPTY_STR;
        EnumOptionData interestMethodEnum = null;
        if (interestMethod != null) {
            if (interestMethod.equalsIgnoreCase("Flat")) {
                interestMethodId = "1";
            } else if (interestMethod.equalsIgnoreCase("Declining Balance")) {
                interestMethodId = "0";
            }
            interestMethodEnum = new EnumOptionData(null, null, interestMethodId);
        }
        String interestCalculationPeriod = ImportHandlerUtils.readAsString(LoanConstants.INTEREST_CALCULATION_PERIOD_COL, row);
        String interestCalculationPeriodId = EMPTY_STR;
        EnumOptionData interestCalculationPeriodEnum = null;
        if (interestCalculationPeriod != null) {
            if (interestCalculationPeriod.equalsIgnoreCase("Daily")) {
                interestCalculationPeriodId = "0";
            } else if (interestCalculationPeriod.equalsIgnoreCase("Same as repayment period")) {
                interestCalculationPeriodId = "1";
            }
            interestCalculationPeriodEnum = new EnumOptionData(null, null, interestCalculationPeriodId);

        }
        BigDecimal arrearsTolerance = null;
        if (ImportHandlerUtils.readAsDouble(LoanConstants.ARREARS_TOLERANCE_COL, row) != null) {
            arrearsTolerance = BigDecimal.valueOf(ImportHandlerUtils.readAsDouble(LoanConstants.ARREARS_TOLERANCE_COL, row));
        }

        String loanRepaymentScheduleTransactionProcessorStrategy = ImportHandlerUtils.readAsString(LoanConstants.REPAYMENT_STRATEGY_COL,
                row);

        LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = loanRepaymentScheduleTransactionProcessorFactory
                .determineProcessor(loanRepaymentScheduleTransactionProcessorStrategy);

        String repaymentStrategyCode = "mifos-standard-strategy";

        if (loanRepaymentScheduleTransactionProcessor != null) {
            repaymentStrategyCode = loanRepaymentScheduleTransactionProcessor.getCode();
        }
        Integer graceOnPrincipalPayment = ImportHandlerUtils.readAsInt(LoanConstants.GRACE_ON_PRINCIPAL_PAYMENT_COL, row);
        Integer graceOnInterestPayment = ImportHandlerUtils.readAsInt(LoanConstants.GRACE_ON_INTEREST_PAYMENT_COL, row);
        Integer graceOnInterestCharged = ImportHandlerUtils.readAsInt(LoanConstants.GRACE_ON_INTEREST_CHARGED_COL, row);
        LocalDate interestChargedFromDate = ImportHandlerUtils.readAsDate(LoanConstants.INTEREST_CHARGED_FROM_COL, row);
        LocalDate firstRepaymentOnDate = ImportHandlerUtils.readAsDate(LoanConstants.FIRST_REPAYMENT_COL, row);
        String loanType = null;
        EnumOptionData loanTypeEnumOption = null;
        if (ImportHandlerUtils.readAsString(LoanConstants.LOAN_TYPE_COL, row) != null) {
            loanType = ImportHandlerUtils.readAsString(LoanConstants.LOAN_TYPE_COL, row).toLowerCase(Locale.ENGLISH);

            loanTypeEnumOption = new EnumOptionData(null, null, loanType);
        }

        String clientOrGroupName = ImportHandlerUtils.readAsString(LoanConstants.CLIENT_NAME_COL, row);

        List<LoanChargeData> charges = new ArrayList<>();

        String chargeOneName = ImportHandlerUtils.readAsString(LoanConstants.CHARGE_NAME_1, row);
        String chargeTwoName = ImportHandlerUtils.readAsString(LoanConstants.CHARGE_NAME_2, row);

        Long chargeOneId = null;
        if (chargeOneName != null) {
            chargeOneId = ImportHandlerUtils.getIdByName(workbook.getSheet(TemplatePopulateImportConstants.CHARGE_SHEET_NAME),
                    chargeOneName);
        }
        Long chargeTwoId = null;
        if (chargeTwoName != null) {
            chargeTwoId = ImportHandlerUtils.getIdByName(workbook.getSheet(TemplatePopulateImportConstants.CHARGE_SHEET_NAME),
                    chargeTwoName);
        }

        Long collateralId = ImportHandlerUtils.readAsLong(LoanConstants.LOAN_COLLATERAL_ID, row);

        Long groupId = ImportHandlerUtils.readAsLong(LoanConstants.GROUP_ID, row);

        String linkAccountId = ImportHandlerUtils.readAsString(LoanConstants.LINK_ACCOUNT_ID, row);

        if (chargeOneId != null) {
            if (ImportHandlerUtils.readAsDouble(LoanConstants.CHARGE_AMOUNT_1, row) != null) {
                EnumOptionData chargeOneTimeTypeEnum = ImportHandlerUtils
                        .getChargeTimeTypeEmun(workbook.getSheet(TemplatePopulateImportConstants.CHARGE_SHEET_NAME), chargeOneName);
                EnumOptionData chargeOneAmountTypeEnum = ImportHandlerUtils
                        .getChargeAmountTypeEnum(ImportHandlerUtils.readAsString(LoanConstants.CHARGE_AMOUNT_TYPE_1, row));

                BigDecimal chargeAmount;
                BigDecimal amountOrPercentage = BigDecimal.valueOf(ImportHandlerUtils.readAsDouble(LoanConstants.CHARGE_AMOUNT_1, row));
                if (chargeOneAmountTypeEnum.getValue().equalsIgnoreCase("1")) {
                    chargeAmount = amountOrPercentage;
                } else {
                    chargeAmount = LoanCharge.percentageOf(principal, amountOrPercentage);
                }

                charges.add(new LoanChargeData(chargeOneId, ImportHandlerUtils.readAsDate(LoanConstants.CHARGE_DUE_DATE_1, row),
                        chargeAmount, chargeOneAmountTypeEnum, chargeOneTimeTypeEnum));
            } else {
                charges.add(new LoanChargeData(chargeOneId, ImportHandlerUtils.readAsDate(LoanConstants.CHARGE_DUE_DATE_1, row), null));
            }
        }

        if (chargeTwoId != null) {
            if (ImportHandlerUtils.readAsDouble(LoanConstants.CHARGE_AMOUNT_2, row) != null) {
                EnumOptionData chargeTwoTimeTypeEnum = ImportHandlerUtils
                        .getChargeTimeTypeEmun(workbook.getSheet(TemplatePopulateImportConstants.CHARGE_SHEET_NAME), chargeTwoName);
                EnumOptionData chargeTwoAmountTypeEnum = ImportHandlerUtils
                        .getChargeAmountTypeEnum(ImportHandlerUtils.readAsString(LoanConstants.CHARGE_AMOUNT_TYPE_2, row));

                BigDecimal chargeAmount;
                BigDecimal amountOrPercentage = BigDecimal.valueOf(ImportHandlerUtils.readAsDouble(LoanConstants.CHARGE_AMOUNT_2, row));
                if (chargeTwoTimeTypeEnum.getValue().equalsIgnoreCase("1")) {
                    chargeAmount = amountOrPercentage;
                } else {
                    chargeAmount = LoanCharge.percentageOf(principal, amountOrPercentage);
                }

                charges.add(new LoanChargeData(chargeTwoId, ImportHandlerUtils.readAsDate(LoanConstants.CHARGE_DUE_DATE_2, row),
                        chargeAmount, chargeTwoAmountTypeEnum, chargeTwoTimeTypeEnum));
            } else {
                charges.add(new LoanChargeData(chargeTwoId, ImportHandlerUtils.readAsDate(LoanConstants.CHARGE_DUE_DATE_2, row), null));
            }
        }

        List<LoanCollateralManagementData> loanCollateralManagementData = new ArrayList<>();

        if (collateralId != null) {
            if (ImportHandlerUtils.readAsDouble(LoanConstants.LOAN_COLLATERAL_QUANTITY, row) != null) {
                loanCollateralManagementData.add(new LoanCollateralManagementData(collateralId,
                        BigDecimal.valueOf(ImportHandlerUtils.readAsDouble(LoanConstants.LOAN_COLLATERAL_QUANTITY, row)), null, null,
                        null));
            } else {
                throw new InvalidAmountOfCollateralQuantity(null);
            }
        }

        statuses.add(status);

        if (loanType != null) {
            if (loanType.equals("individual")) {
                Long clientId = ImportHandlerUtils.getIdByName(workbook.getSheet(TemplatePopulateImportConstants.CLIENT_SHEET_NAME),
                        clientOrGroupName);
                return LoanAccountData.importInstanceIndividual(loanTypeEnumOption, clientId, productId, loanOfficerId, submittedOnDate,
                        fundId, principal, numberOfRepayments, repaidEvery, repaidEveryFrequencyEnums, loanTerm, loanTermFrequencyEnum,
                        nominalInterestRate, submittedOnDate, amortizationEnumOption, interestMethodEnum, interestCalculationPeriodEnum,
                        arrearsTolerance, repaymentStrategyCode, graceOnPrincipalPayment, graceOnInterestPayment, graceOnInterestCharged,
                        interestChargedFromDate, firstRepaymentOnDate, row.getRowNum(), externalId, null, charges, linkAccountId, locale,
                        dateFormat, loanCollateralManagementData, null);
            } else if (loanType.equals("jlg")) {
                Long clientId = ImportHandlerUtils.getIdByName(workbook.getSheet(TemplatePopulateImportConstants.CLIENT_SHEET_NAME),
                        clientOrGroupName);
                return LoanAccountData.importInstanceIndividual(loanTypeEnumOption, clientId, productId, loanOfficerId, submittedOnDate,
                        fundId, principal, numberOfRepayments, repaidEvery, repaidEveryFrequencyEnums, loanTerm, loanTermFrequencyEnum,
                        nominalInterestRate, submittedOnDate, amortizationEnumOption, interestMethodEnum, interestCalculationPeriodEnum,
                        arrearsTolerance, repaymentStrategyCode, graceOnPrincipalPayment, graceOnInterestPayment, graceOnInterestCharged,
                        interestChargedFromDate, firstRepaymentOnDate, row.getRowNum(), externalId, groupId, charges, linkAccountId, locale,
                        dateFormat, null, null);
            } else {
                Long groupIdforGroupLoan = ImportHandlerUtils
                        .getIdByName(workbook.getSheet(TemplatePopulateImportConstants.GROUP_SHEET_NAME), clientOrGroupName);
                return LoanAccountData.importInstanceGroup(loanTypeEnumOption, groupIdforGroupLoan, productId, loanOfficerId,
                        submittedOnDate, fundId, principal, numberOfRepayments, repaidEvery, repaidEveryFrequencyEnums, loanTerm,
                        loanTermFrequencyEnum, nominalInterestRate, amortizationEnumOption, interestMethodEnum,
                        interestCalculationPeriodEnum, arrearsTolerance, repaymentStrategyCode, graceOnPrincipalPayment,
                        graceOnInterestPayment, graceOnInterestCharged, interestChargedFromDate, firstRepaymentOnDate, row.getRowNum(),
                        externalId, linkAccountId, locale, dateFormat, null);
            }
        }

        return null;
    }

    private Count importEntity(final Workbook workbook, final List<LoanAccountData> loans, final List<LoanApprovalData> approvalDates,
            final List<LoanTransactionData> loanRepayments, final List<DisbursementData> disbursalDates, final List<String> statuses,
            final String dateFormat) {
        Sheet loanSheet = workbook.getSheet(TemplatePopulateImportConstants.LOANS_SHEET_NAME);
        int successCount = 0;
        int errorCount = 0;
        int progressLevel = 0;
        String loanId;
        String errorMessage;
        for (int i = 0; i < loans.size(); i++) {
            Row row = loanSheet.getRow(loans.get(i).getRowIndex());
            Cell errorReportCell = row.createCell(LoanConstants.FAILURE_REPORT_COL);
            Cell statusCell = row.createCell(LoanConstants.STATUS_COL);
            CommandProcessingResult result = null;
            loanId = EMPTY_STR;
            try {
                String status = statuses.get(i);
                progressLevel = getProgressLevel(status);

                if (progressLevel == 0 && loans.get(i) != null) {
                    result = importLoan(loans, i, dateFormat);
                    loanId = result.getLoanId().toString();
                    progressLevel = 1;
                } else {
                    loanId = ImportHandlerUtils.readAsString(LoanConstants.LOAN_ID_COL, loanSheet.getRow(loans.get(i).getRowIndex()));
                }

                if (progressLevel <= 1 && approvalDates.get(i) != null) {
                    progressLevel = importLoanApproval(approvalDates, result, i, dateFormat);
                }

                if (progressLevel <= 2 && disbursalDates.get(i) != null) {
                    progressLevel = importDisbursalData(approvalDates, disbursalDates, result, i, dateFormat);
                }

                if (loanRepayments.get(i) != null) {
                    progressLevel = importLoanRepayment(loanRepayments, result, i, dateFormat);
                }

                successCount++;
                statusCell.setCellValue(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
                statusCell.setCellStyle(ImportHandlerUtils.getCellStyle(workbook, IndexedColors.LIGHT_GREEN));
            } catch (RuntimeException ex) {
                errorCount++;
                log.error("Problem occurred in importEntity function", ex);
                errorMessage = ImportHandlerUtils.getErrorMessage(ex);
                writeLoanErrorMessage(workbook, loanId, errorMessage, progressLevel, statusCell, errorReportCell, row);
            }

        }
        setReportHeaders(loanSheet);
        return Count.instance(successCount, errorCount);
    }

    private void writeLoanErrorMessage(final Workbook workbook, final String loanId, final String errorMessage, final int progressLevel,
            final Cell statusCell, final Cell errorReportCell, final Row row) {
        String status = EMPTY_STR;
        if (progressLevel == 0) {
            status = TemplatePopulateImportConstants.STATUS_CREATION_FAILED;
        } else if (progressLevel == 1) {
            status = TemplatePopulateImportConstants.STATUS_APPROVAL_FAILED;
        } else if (progressLevel == 2) {
            status = TemplatePopulateImportConstants.STATUS_DISBURSAL_FAILED;
        } else if (progressLevel == 3) {
            status = TemplatePopulateImportConstants.STATUS_DISBURSAL_REPAYMENT_FAILED;
        }
        statusCell.setCellValue(status);
        statusCell.setCellStyle(ImportHandlerUtils.getCellStyle(workbook, IndexedColors.RED));

        if (progressLevel > 0) {
            row.createCell(LoanConstants.LOAN_ID_COL).setCellValue(Integer.parseInt(loanId));
        }
        errorReportCell.setCellValue(errorMessage);
    }

    private void setReportHeaders(Sheet sheet) {
        sheet.setColumnWidth(LoanConstants.STATUS_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        Row rowHeader = sheet.getRow(TemplatePopulateImportConstants.ROWHEADER_INDEX);
        ImportHandlerUtils.writeString(LoanConstants.STATUS_COL, rowHeader, "Status");
        ImportHandlerUtils.writeString(LoanConstants.LOAN_ID_COL, rowHeader, "Loan ID");
        ImportHandlerUtils.writeString(LoanConstants.FAILURE_REPORT_COL, rowHeader, "Report");
    }

    private Integer importLoanRepayment(final List<LoanTransactionData> loanRepayments, final CommandProcessingResult result,
            final int rowIndex, final String dateFormat) {
        GsonBuilder gsonBuilder = GoogleGsonSerializerHelper.createGsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDate.class, new DateSerializer(dateFormat));
        JsonObject loanRepaymentJsonob = gsonBuilder.create().toJsonTree(loanRepayments.get(rowIndex)).getAsJsonObject();
        loanRepaymentJsonob.remove("manuallyReversed");
        loanRepaymentJsonob.remove("numberOfRepayments");
        String payload = loanRepaymentJsonob.toString();
        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .loanRepaymentTransaction(result.getLoanId()) //
                .withJson(payload) //
                .build(); //

        commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return 4;
    }

    private Integer importDisbursalData(final List<LoanApprovalData> approvalDates, final List<DisbursementData> disbursalDates,
            final CommandProcessingResult result, final int rowIndex, final String dateFormat) {
        if (approvalDates.get(rowIndex) != null && disbursalDates.get(rowIndex) != null) {

            DisbursementData disbusalData = disbursalDates.get(rowIndex);
            String linkAccountId = disbusalData.getLinkAccountId();
            GsonBuilder gsonBuilder = GoogleGsonSerializerHelper.createGsonBuilder();
            gsonBuilder.registerTypeAdapter(LocalDate.class, new DateSerializer(dateFormat));
            if (linkAccountId != null && !EMPTY_STR.equals(linkAccountId)) {
                String payload = gsonBuilder.create().toJson(disbusalData);
                final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                        .disburseLoanToSavingsApplication(result.getLoanId()) //
                        .withJson(payload) //
                        .build(); //
                commandsSourceWritePlatformService.logCommandSource(commandRequest);
            } else {
                String payload = gsonBuilder.create().toJson(disbusalData);
                final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                        .disburseLoanApplication(result.getLoanId()) //
                        .withJson(payload) //
                        .build(); //

                commandsSourceWritePlatformService.logCommandSource(commandRequest);
            }
        }
        return 3;
    }

    private Integer importLoanApproval(final List<LoanApprovalData> approvalDates, final CommandProcessingResult result, final int rowIndex,
            final String dateFormat) {
        if (approvalDates.get(rowIndex) != null) {
            GsonBuilder gsonBuilder = GoogleGsonSerializerHelper.createGsonBuilder();
            gsonBuilder.registerTypeAdapter(LocalDate.class, new DateSerializer(dateFormat));
            String payload = gsonBuilder.create().toJson(approvalDates.get(rowIndex));
            final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                    .approveLoanApplication(result.getLoanId()) //
                    .withJson(payload) //
                    .build(); //

            commandsSourceWritePlatformService.logCommandSource(commandRequest);
        }
        return 2;
    }

    private CommandProcessingResult importLoan(final List<LoanAccountData> loans, final int rowIndex, final String dateFormat) {
        GsonBuilder gsonBuilder = GoogleGsonSerializerHelper.createGsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDate.class, new DateSerializer(dateFormat));
        gsonBuilder.registerTypeAdapter(EnumOptionData.class, new EnumOptionDataValueSerializer());
        JsonObject loanJsonOb = gsonBuilder.create().toJsonTree(loans.get(rowIndex)).getAsJsonObject();
        loanJsonOb.remove("isLoanProductLinkedToFloatingRate");
        loanJsonOb.remove("isInterestRecalculationEnabled");
        loanJsonOb.remove("isFloatingInterestRate");
        loanJsonOb.remove("isRatesEnabled");
        JsonArray chargesJsonAr = loanJsonOb.getAsJsonArray("charges");
        if (chargesJsonAr != null) {
            for (int i = 0; i < chargesJsonAr.size(); i++) {
                JsonElement chargesJsonElement = chargesJsonAr.get(i);
                JsonObject chargeJsonOb = chargesJsonElement.getAsJsonObject();
                chargeJsonOb.remove("penalty");
                chargeJsonOb.remove("paid");
                chargeJsonOb.remove("waived");
                chargeJsonOb.remove("chargePayable");
            }
        }
        loanJsonOb.remove("isTopup");
        String payload = loanJsonOb.toString();
        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .createLoanApplication() //
                .withJson(payload) //
                .build(); //
        return commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    private int getProgressLevel(String status) {
        if (status == null || status.equals(TemplatePopulateImportConstants.STATUS_CREATION_FAILED)) {
            return 0;
        } else if (status.equals(TemplatePopulateImportConstants.STATUS_APPROVAL_FAILED)) {
            return 1;
        } else if (status.equals(TemplatePopulateImportConstants.STATUS_DISBURSAL_FAILED)) {
            return 2;
        } else if (status.equals(TemplatePopulateImportConstants.STATUS_DISBURSAL_REPAYMENT_FAILED)) {
            return 3;
        }
        return 0;
    }

}
