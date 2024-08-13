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
package org.apache.fineract.portfolio.loanaccount.api;

import static org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations.interestType;

import com.google.gson.JsonElement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookPopulatorService;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookService;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.ApiParameterHelper;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.api.JsonQuery;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.data.UploadRequest;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.CommandParameterUtil;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant;
import org.apache.fineract.infrastructure.dataqueries.data.DatatableData;
import org.apache.fineract.infrastructure.dataqueries.data.EntityTables;
import org.apache.fineract.infrastructure.dataqueries.data.StatusEnum;
import org.apache.fineract.infrastructure.dataqueries.service.EntityDatatableChecksReadService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.service.SqlValidator;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.data.PortfolioAccountDTO;
import org.apache.fineract.portfolio.account.data.PortfolioAccountData;
import org.apache.fineract.portfolio.account.service.AccountAssociationsReadPlatformService;
import org.apache.fineract.portfolio.account.service.PortfolioAccountReadPlatformService;
import org.apache.fineract.portfolio.accountdetails.data.LoanAccountSummaryData;
import org.apache.fineract.portfolio.accountdetails.service.AccountDetailsReadPlatformService;
import org.apache.fineract.portfolio.calendar.data.CalendarData;
import org.apache.fineract.portfolio.calendar.domain.CalendarEntityType;
import org.apache.fineract.portfolio.calendar.service.CalendarReadPlatformService;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.charge.service.ChargeReadPlatformService;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.client.service.ClientReadPlatformService;
import org.apache.fineract.portfolio.collateralmanagement.data.LoanCollateralResponseData;
import org.apache.fineract.portfolio.collateralmanagement.service.LoanCollateralManagementReadPlatformService;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.delinquency.api.DelinquencyApiResourceSwagger;
import org.apache.fineract.portfolio.delinquency.data.LoanDelinquencyTagHistoryData;
import org.apache.fineract.portfolio.delinquency.domain.LoanDelinquencyAction;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyReadPlatformService;
import org.apache.fineract.portfolio.delinquency.validator.LoanDelinquencyActionData;
import org.apache.fineract.portfolio.floatingrates.data.InterestRatePeriodData;
import org.apache.fineract.portfolio.fund.data.FundData;
import org.apache.fineract.portfolio.fund.service.FundReadPlatformService;
import org.apache.fineract.portfolio.group.data.GroupGeneralData;
import org.apache.fineract.portfolio.group.service.GroupReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.data.CollectionData;
import org.apache.fineract.portfolio.loanaccount.data.DisbursementData;
import org.apache.fineract.portfolio.loanaccount.data.GlimRepaymentTemplate;
import org.apache.fineract.portfolio.loanaccount.data.LoanAccountData;
import org.apache.fineract.portfolio.loanaccount.data.LoanApprovalData;
import org.apache.fineract.portfolio.loanaccount.data.LoanChargeData;
import org.apache.fineract.portfolio.loanaccount.data.LoanCollateralManagementData;
import org.apache.fineract.portfolio.loanaccount.data.LoanSummaryData;
import org.apache.fineract.portfolio.loanaccount.data.LoanTermVariationsData;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionData;
import org.apache.fineract.portfolio.loanaccount.data.PaidInAdvanceData;
import org.apache.fineract.portfolio.loanaccount.data.RepaymentScheduleRelatedLoanData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanSummaryBalancesRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTermVariationType;
import org.apache.fineract.portfolio.loanaccount.exception.LoanNotFoundException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanTemplateTypeRequiredException;
import org.apache.fineract.portfolio.loanaccount.exception.NotSupportedLoanTemplateTypeException;
import org.apache.fineract.portfolio.loanaccount.guarantor.data.GuarantorData;
import org.apache.fineract.portfolio.loanaccount.guarantor.service.GuarantorReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleModel;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.service.LoanScheduleCalculationPlatformService;
import org.apache.fineract.portfolio.loanaccount.loanschedule.service.LoanScheduleHistoryReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.GLIMAccountInfoReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanChargeReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.LoanProductConstants;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;
import org.apache.fineract.portfolio.loanproduct.data.TransactionProcessingStrategyData;
import org.apache.fineract.portfolio.loanproduct.domain.InterestMethod;
import org.apache.fineract.portfolio.loanproduct.service.LoanDropdownReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.apache.fineract.portfolio.note.data.NoteData;
import org.apache.fineract.portfolio.note.domain.NoteType;
import org.apache.fineract.portfolio.note.service.NoteReadPlatformService;
import org.apache.fineract.portfolio.rate.data.RateData;
import org.apache.fineract.portfolio.rate.service.RateReadService;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountStatusType;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Path("/v1/loans")
@Component
@Tag(name = "Loans", description = "The API concept of loans models the loan application process and the loan contract/monitoring process.\n"
        + "\n" + "Field Descriptions\n" + "accountNo\n"
        + "The account no. associated with this loan. Is auto generated if not provided at loan application creation time.\n"
        + "externalId\n" + "A place to put an external reference for this loan e.g. The ID another system uses.\n"
        + "If provided, it must be unique.\n" + "fundId\n" + "Optional: For associating a loan with a given fund.\n" + "loanOfficerId\n"
        + "Optional: For associating a loan with a given staff member who is a loan officer.\n" + "loanPurposeId\n"
        + "Optional: For marking a loan with a given loan purpose option. Loan purposes are configurable and can be setup by system admin through code/code values screens.\n"
        + "principal\n" + "The loan amount to be disbursed to through loan.\n" + "loanTermFrequency\n" + "The length of loan term\n"
        + "Used like: loanTermFrequency loanTermFrequencyType\n" + "e.g. 12 Months\n" + "loanTermFrequencyType\n"
        + "The loan term period to use. Used like: loanTermFrequency loanTermFrequencyType\n"
        + "e.g. 12 Months Example Values: 0=Days, 1=Weeks, 2=Months, 3=Years\n" + "numberOfRepayments\n"
        + "Number of installments to repay.\n" + "Used like: numberOfRepayments Every repaymentEvery repaymentFrequencyType\n"
        + "e.g. 10 (repayments) Every 12 Weeks\n" + "repaymentEvery\n"
        + "Used like: numberOfRepayments Every repaymentEvery repaymentFrequencyType\n" + "e.g. 10 (repayments) Every 12 Weeks\n"
        + "repaymentFrequencyType\n" + "Used like: numberOfRepayments Every repaymentEvery repaymentFrequencyType\n"
        + "e.g. 10 (repayments) Every 12 Weeks \n" + "Example Values: 0=Days, 1=Weeks, 2=Months\n" + "interestRatePerPeriod\n"
        + "Interest Rate.\n" + "Used like: interestRatePerPeriod % interestRateFrequencyType - interestType\n"
        + "e.g. 12.0000% Per year - Declining Balance\n" + "interestRateFrequencyType\n"
        + "Used like: interestRatePerPeriod% interestRateFrequencyType - interestType\n" + "e.g. 12.0000% Per year - Declining Balance \n"
        + "Example Values: 2=Per month, 3=Per year\n" + "graceOnPrincipalPayment\n"
        + "Optional: Integer - represents the number of repayment periods that grace should apply to the principal component of a repayment period.\n"
        + "graceOnInterestPayment\n"
        + "Optional: Integer - represents the number of repayment periods that grace should apply to the interest component of a repayment period. Interest is still calculated but offset to later repayment periods.\n"
        + "graceOnInterestCharged\n" + "Optional: Integer - represents the number of repayment periods that should be interest-free.\n"
        + "graceOnArrearsAgeing\n"
        + "Optional: Integer - Used in Arrears calculation to only take into account loans that are more than graceOnArrearsAgeing days overdue.\n"
        + "interestChargedFromDate\n" + "Optional: Date - The date from with interest is to start being charged.\n"
        + "expectedDisbursementDate\n" + "The proposed disbursement date of the loan so a proposed repayment schedule can be provided.\n"
        + "submittedOnDate\n" + "The date the loan application was submitted by applicant.\n" + "linkAccountId\n"
        + "The Savings Account id for linking with loan account for payments.\n" + "amortizationType\n"
        + "Example Values: 0=Equal principle payments, 1=Equal installments\n" + "interestType\n"
        + "Used like: interestRatePerPeriod% interestRateFrequencyType - interestType\n" + "e.g. 12.0000% Per year - Declining Balance \n"
        + "Example Values: 0=Declining Balance, 1=Flat\n" + "interestCalculationPeriodType\n"
        + "Example Values: 0=Daily, 1=Same as repayment period\n" + "allowPartialPeriodInterestCalcualtion\n"
        + "This value will be supported along with interestCalculationPeriodType as Same as repayment period to calculate interest for partial periods. Example: Interest charged from is 5th of April , Principal is 10000 and interest is 1% per month then the interest will be (10000 * 1%)* (25/30) , it calculates for the month first then calculates exact periods between start date and end date(can be a decimal)\n"
        + "inArrearsTolerance\n" + "The amount that can be 'waived' at end of all loan payments because it is too small to worry about.\n"
        + "This is also the tolerance amount assessed when determining if a loan is in arrears.\n" + "transactionProcessingStrategyCode\n"
        + "An enumeration that indicates the type of transaction processing strategy to be used. This relates to functionality that is also known as Payment Application Logic.\n"
        + "A number of out of the box approaches exist, some are custom to specific MFIs, some are more general and indicate the order in which payments are processed.\n"
        + "\n"
        + "Refer to the Payment Application Logic / Transaction Processing Strategy section in the appendix for more detailed overview of each available payment application logic provided out of the box.\n"
        + "\n" + "List of current approaches:\n" + "1 = Mifos style (Similar to Old Mifos)\n" + "2 = Heavensfamily (Custom MFI approach)\n"
        + "3 = Creocore (Custom MFI approach)\n" + "4 = RBI (India)\n" + "5 = Principal Interest Penalties Fees Order\n"
        + "6 = Interest Principal Penalties Fees Order\n" + "7 = Early Payment Strategy\n" + "loanType\n"
        + "To represent different type of loans.\n" + "At present there are three type of loans are supported. \n"
        + "Available loan types:\n" + "individual: Loan given to individual member\n" + "group: Loan given to group as a whole\n"
        + "jlg: Joint liability group loan given to members in a group on individual basis. JLG loan can be given to one or more members in a group.\n"
        + "recalculationRestFrequencyDate\n"
        + "Specifies rest frequency start date for interest recalculation. This date must be before or equal to disbursement date\n"
        + "recalculationCompoundingFrequencyDate\n"
        + "Specifies compounding frequency start date for interest recalculation. This date must be equal to disbursement date")
@RequiredArgsConstructor
public class LoansApiResource {

    private static final Set<String> LOAN_DATA_PARAMETERS = new HashSet<>(Arrays.asList("id", "accountNo", "status", "externalId",
            "clientId", "group", "loanProductId", "loanProductName", "loanProductDescription", "isLoanProductLinkedToFloatingRate",
            "fundId", "fundName", "loanPurposeId", "loanPurposeName", "loanOfficerId", "loanOfficerName", "currency", "principal",
            "totalOverpaid", "inArrearsTolerance", "termFrequency", "termPeriodFrequencyType", "numberOfRepayments", "repaymentEvery",
            "interestRatePerPeriod", "annualInterestRate", "repaymentFrequencyType", "transactionProcessingStrategyCode",
            "transactionProcessingStrategyName", "interestRateFrequencyType", "amortizationType", "interestType",
            "interestCalculationPeriodType", LoanProductConstants.ALLOW_PARTIAL_PERIOD_INTEREST_CALCUALTION_PARAM_NAME,
            "expectedFirstRepaymentOnDate", "graceOnPrincipalPayment", "recurringMoratoriumOnPrincipalPeriods", "graceOnInterestPayment",
            "graceOnInterestCharged", "interestChargedFromDate", "timeline", "totalFeeChargesAtDisbursement", "summary",
            "repaymentSchedule", "transactions", "charges", "collateral", "guarantors", "meeting", "productOptions",
            "amortizationTypeOptions", "interestTypeOptions", "interestCalculationPeriodTypeOptions", "repaymentFrequencyTypeOptions",
            "repaymentFrequencyNthDayTypeOptions", "repaymentFrequencyDaysOfWeekTypeOptions", "termFrequencyTypeOptions",
            "interestRateFrequencyTypeOptions", "fundOptions", "repaymentStrategyOptions", "chargeOptions", "loanOfficerOptions",
            "loanPurposeOptions", "loanCollateralOptions", "chargeTemplate", "calendarOptions", "syncDisbursementWithMeeting",
            "loanCounter", "loanProductCounter", "notes", "accountLinkingOptions", "linkedAccount", "interestRateDifferential",
            "isFloatingInterestRate", "interestRatesPeriods", "lastClosedBusinessDate", LoanApiConstants.canUseForTopup,
            LoanApiConstants.isTopup, LoanApiConstants.loanIdToClose, LoanApiConstants.topupAmount,
            LoanApiConstants.clientActiveLoanOptions, LoanApiConstants.datatables, LoanProductConstants.RATES_PARAM_NAME,
            LoanApiConstants.MULTIDISBURSE_DETAILS_PARAMNAME, LoanApiConstants.EMI_AMOUNT_VARIATIONS_PARAMNAME,
            LoanApiConstants.COLLECTION_PARAMNAME));

    private static final Set<String> LOAN_APPROVAL_DATA_PARAMETERS = new HashSet<>(Arrays.asList("approvalDate", "approvalAmount"));
    private static final Set<String> GLIM_ACCOUNTS_DATA_PARAMETERS = new HashSet<>(Arrays.asList("glimId", "groupId", "clientId",
            "parentLoanAccountNo", "parentPrincipalAmount", "childLoanAccountNo", "childPrincipalAmount", "clientName"));
    private static final String RESOURCE_NAME_FOR_PERMISSIONS = "LOAN";
    private static final String RESOURCE_NAME_FOR_DELINQUENCY_ACTION_PERMISSIONS = "DELINQUENCY_ACTION";

    private final PlatformSecurityContext context;
    private final LoanReadPlatformService loanReadPlatformService;
    private final LoanProductReadPlatformService loanProductReadPlatformService;
    private final LoanDropdownReadPlatformService dropdownReadPlatformService;
    private final FundReadPlatformService fundReadPlatformService;
    private final ChargeReadPlatformService chargeReadPlatformService;
    private final LoanChargeReadPlatformService loanChargeReadPlatformService;
    private final LoanScheduleCalculationPlatformService calculationPlatformService;
    private final GuarantorReadPlatformService guarantorReadPlatformService;
    private final CodeValueReadPlatformService codeValueReadPlatformService;
    private final GroupReadPlatformService groupReadPlatformService;
    private final DefaultToApiJsonSerializer<LoanAccountData> toApiJsonSerializer;
    private final DefaultToApiJsonSerializer<LoanApprovalData> loanApprovalDataToApiJsonSerializer;
    private final DefaultToApiJsonSerializer<LoanScheduleData> loanScheduleToApiJsonSerializer;
    private final DefaultToApiJsonSerializer<LoanDelinquencyActionData> delinquencyActionSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final FromJsonHelper fromJsonHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final CalendarReadPlatformService calendarReadPlatformService;
    private final NoteReadPlatformService noteReadPlatformService;
    private final PortfolioAccountReadPlatformService portfolioAccountReadPlatformService;
    private final AccountAssociationsReadPlatformService accountAssociationsReadPlatformService;
    private final LoanScheduleHistoryReadPlatformService loanScheduleHistoryReadPlatformService;
    private final AccountDetailsReadPlatformService accountDetailsReadPlatformService;
    private final EntityDatatableChecksReadService entityDatatableChecksReadService;
    private final BulkImportWorkbookService bulkImportWorkbookService;
    private final BulkImportWorkbookPopulatorService bulkImportWorkbookPopulatorService;
    private final RateReadService rateReadService;
    private final ConfigurationDomainService configurationDomainService;
    private final DefaultToApiJsonSerializer<GlimRepaymentTemplate> glimTemplateToApiJsonSerializer;
    private final GLIMAccountInfoReadPlatformService glimAccountInfoReadPlatformService;
    private final LoanCollateralManagementReadPlatformService loanCollateralManagementReadPlatformService;
    private final DefaultToApiJsonSerializer<LoanDelinquencyTagHistoryData> jsonSerializerTagHistory;
    private final DelinquencyReadPlatformService delinquencyReadPlatformService;
    private final SqlValidator sqlValidator;
    private final LoanSummaryBalancesRepository loanSummaryBalancesRepository;
    private final ClientReadPlatformService clientReadPlatformService;

    /*
     * This template API is used for loan approval, ideally this should be invoked on loan that are pending for
     * approval. But system does not validate the status of the loan, it returns the template irrespective of loan
     * status
     */

    @GET
    @Path("{loanId}/template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.GetLoansApprovalTemplateResponse.class))) })
    public String retrieveApprovalTemplate(@PathParam("loanId") @Parameter(description = "loanId", required = true) final Long loanId,
            @QueryParam("templateType") @Parameter(description = "templateType") final String templateType,
            @Context final UriInfo uriInfo) {
        return retrieveApprovalTemplate(loanId, null, templateType, uriInfo);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Loan Details Template", description = "This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n"
            + "\n" + "Field Defaults\n" + "Allowed description Lists\n" + "Example Requests:\n" + "\n"
            + "loans/template?templateType=individual&clientId=1\n" + "\n" + "\n"
            + "loans/template?templateType=individual&clientId=1&productId=1")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.GetLoansTemplateResponse.class))) })
    public String template(@QueryParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @QueryParam("groupId") @Parameter(description = "groupId") final Long groupId,
            @QueryParam("productId") @Parameter(description = "productId") final Long productId,
            @QueryParam("templateType") @Parameter(description = "templateType") final String templateType,
            @DefaultValue("false") @QueryParam("staffInSelectedOfficeOnly") @Parameter(description = "staffInSelectedOfficeOnly") final boolean staffInSelectedOfficeOnly,
            @DefaultValue("false") @QueryParam("activeOnly") @Parameter(description = "activeOnly") final boolean onlyActive,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        // template
        final Collection<LoanProductData> productOptions = this.loanProductReadPlatformService.retrieveAllLoanProductsForLookup(onlyActive);

        // options
        Collection<StaffData> allowedLoanOfficers;
        Collection<CodeValueData> loanCollateralOptions;
        Collection<CalendarData> calendarOptions = null;
        LoanAccountData newLoanAccount = new LoanAccountData();
        LocalDate expectedDisbursementDate = DateUtils.getBusinessLocalDate();
        Long officeId = null;
        Collection<PortfolioAccountData> accountLinkingOptions = null;
        boolean isRatesEnabled = this.configurationDomainService.isSubRatesEnabled();

        if (productId != null) {
            newLoanAccount = this.loanReadPlatformService.retrieveLoanProductDetailsTemplate(productId, clientId, groupId);
        }

        if (templateType == null) {
            final String errorMsg = "Loan template type must be provided";
            throw new LoanTemplateTypeRequiredException(errorMsg);
        } else if (templateType.equals("collateral")) {
            loanCollateralOptions = this.codeValueReadPlatformService.retrieveCodeValuesByCode("LoanCollateral");
            newLoanAccount = newLoanAccount.setLoanCollateralOptions(loanCollateralOptions);
        } else {
            // for JLG loan both client and group details are required
            switch (templateType) {
                case "individual", "jlg" -> {
                    if (clientId != null) {
                        final ClientData clientData = this.clientReadPlatformService.retrieveOne(clientId);
                        officeId = clientData.getOfficeId();
                        newLoanAccount = newLoanAccount.withClientData(clientData).withExpectedDisbursementDate(expectedDisbursementDate);
                    }
                    // if it's JLG loan add group details
                    if (templateType.equals("jlg")) {
                        final GroupGeneralData groupData = this.groupReadPlatformService.retrieveOne(groupId);
                        newLoanAccount = newLoanAccount.setGroup(groupData);
                        calendarOptions = this.loanReadPlatformService.retrieveCalendars(groupId);
                    }
                }
                case "group" -> {
                    final GroupGeneralData groupData = this.groupReadPlatformService.retrieveOne(groupId);
                    officeId = groupData.getOfficeId();
                    calendarOptions = this.loanReadPlatformService.retrieveCalendars(groupId);
                    newLoanAccount = newLoanAccount.setGroup(groupData).withExpectedDisbursementDate(expectedDisbursementDate);
                    accountLinkingOptions = getAccountLinkingOptions(newLoanAccount, clientId, groupId);
                }
                case "jlgbulk" -> {
                    // get group details along with members in that group
                    final GroupGeneralData groupData = this.groupReadPlatformService.retrieveGroupAndMembersDetails(groupId);
                    officeId = groupData.getOfficeId();
                    calendarOptions = this.loanReadPlatformService.retrieveCalendars(groupId);
                    newLoanAccount = newLoanAccount.setGroup(groupData).withExpectedDisbursementDate(expectedDisbursementDate);
                    if (productId != null) {
                        Map<Long, Integer> memberLoanCycle = new HashMap<>();
                        Collection<ClientData> members = groupData.clientMembers();
                        accountLinkingOptions = new ArrayList<>();
                        if (members != null) {
                            for (ClientData clientData : members) {
                                Integer loanCounter = this.loanReadPlatformService.retriveLoanCounter(clientData.getId(), productId);
                                memberLoanCycle.put(clientData.getId(), loanCounter);
                                accountLinkingOptions.addAll(getAccountLinkingOptions(newLoanAccount, clientData.getId(), groupId));
                            }
                        }
                        newLoanAccount = newLoanAccount.associateMemberVariations(memberLoanCycle);
                    }
                }
                default -> {
                    final String errorMsg = "Loan template type '" + templateType + "' is not supported";
                    throw new NotSupportedLoanTemplateTypeException(errorMsg, templateType);
                }
            }

            allowedLoanOfficers = this.loanReadPlatformService.retrieveAllowedLoanOfficers(officeId, staffInSelectedOfficeOnly);

            if (clientId != null) {
                accountLinkingOptions = getAccountLinkingOptions(newLoanAccount, clientId, groupId);
            }

            // add product options, allowed loan officers and calendar options
            // (calendar options will be null in individual loan)
            newLoanAccount = newLoanAccount.associationsAndTemplate(productOptions, allowedLoanOfficers, calendarOptions,
                    accountLinkingOptions, isRatesEnabled);
        }
        final List<DatatableData> datatableTemplates = this.entityDatatableChecksReadService.retrieveTemplates(StatusEnum.CREATE.getValue(),
                EntityTables.LOAN.getName(), productId);
        newLoanAccount.setDatatables(datatableTemplates);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, newLoanAccount, LOAN_DATA_PARAMETERS);
    }

    private Collection<PortfolioAccountData> getAccountLinkingOptions(final LoanAccountData newLoanAccount, final Long clientId,
            final Long groupId) {
        final CurrencyData currencyData = newLoanAccount.getCurrency();
        String currencyCode = null;
        if (currencyData != null) {
            currencyCode = currencyData.getCode();
        }
        final long[] accountStatus = { SavingsAccountStatusType.ACTIVE.getValue() };
        final PortfolioAccountDTO portfolioAccountDTO = new PortfolioAccountDTO(PortfolioAccountType.SAVINGS.getValue(), clientId,
                currencyCode, accountStatus, DepositAccountType.SAVINGS_DEPOSIT.getValue());
        if (groupId != null) {
            portfolioAccountDTO.setGroupId(groupId);
        }
        return this.portfolioAccountReadPlatformService.retrieveAllForLookup(portfolioAccountDTO);
    }

    @GET
    @Path("{loanId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Loan", description = "Note: template=true parameter doesn't apply to this resource."
            + "Example Requests:\n" + "\n" + "loans/1\n" + "\n" + "\n" + "loans/1?fields=id,principal,annualInterestRate\n" + "\n" + "\n"
            + "loans/1?associations=all\n" + "\n" + "loans/1?associations=all&exclude=guarantors\n" + "\n" + "\n"
            + "loans/1?fields=id,principal,annualInterestRate&associations=repaymentSchedule,transactions")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.GetLoansLoanIdResponse.class))) })
    public String retrieveLoan(@PathParam("loanId") @Parameter(description = "loanId", required = true) final Long loanId,
            @DefaultValue("false") @QueryParam("staffInSelectedOfficeOnly") @Parameter(description = "staffInSelectedOfficeOnly") final boolean staffInSelectedOfficeOnly,
            @DefaultValue("all") @QueryParam("associations") @Parameter(in = ParameterIn.QUERY, name = "associations", description = "Loan object relations to be included in the response", required = false, examples = {
                    @ExampleObject(value = "all"), @ExampleObject(value = "repaymentSchedule,transactions") }) final String associations,
            @QueryParam("exclude") @Parameter(in = ParameterIn.QUERY, name = "exclude", description = "Optional Loan object relation list to be filtered in the response", required = false, example = "guarantors,futureSchedule") final String exclude,
            @QueryParam("fields") @Parameter(in = ParameterIn.QUERY, name = "fields", description = "Optional Loan attribute list to be in the response", required = false, example = "id,principal,annualInterestRate") final String fields,
            @Context final UriInfo uriInfo) {
        return retrieveLoan(loanId, null, staffInSelectedOfficeOnly, exclude, uriInfo);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Loans", description = "The list capability of loans can support pagination and sorting.\n"
            + "Example Requests:\n" + "\n" + "loans\n" + "\n" + "loans?fields=accountNo\n" + "\n" + "loans?offset=10&limit=50\n" + "\n"
            + "loans?orderBy=accountNo&sortOrder=DESC")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.GetLoansResponse.class))) })
    public String retrieveAll(@Context final UriInfo uriInfo,
            @QueryParam("externalId") @Parameter(description = "externalId") final String externalId,
            // @QueryParam("underHierarchy") final String hierarchy,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder,
            @QueryParam("accountNo") @Parameter(description = "accountNo") final String accountNo,
            @QueryParam("status") @Parameter(description = "status") final String status) {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        sqlValidator.validate(orderBy);
        sqlValidator.validate(sortOrder);
        sqlValidator.validate(accountNo);
        sqlValidator.validate(externalId);
        final SearchParameters searchParameters = SearchParameters.builder().accountNo(accountNo).sortOrder(sortOrder)
                .externalId(externalId).offset(offset).limit(limit).orderBy(orderBy).status(status).build();

        final Page<LoanAccountData> loanBasicDetails = this.loanReadPlatformService.retrieveAll(searchParameters);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, loanBasicDetails, LOAN_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Calculate loan repayment schedule | Submit a new Loan Application", description = "It calculates the loan repayment Schedule\n"
            + "Submits a new loan application\n"
            + "Mandatory Fields: clientId, productId, principal, loanTermFrequency, loanTermFrequencyType, loanType, numberOfRepayments, repaymentEvery, repaymentFrequencyType, interestRatePerPeriod, amortizationType, interestType, interestCalculationPeriodType, transactionProcessingStrategyCode, expectedDisbursementDate, submittedOnDate, loanType\n"
            + "Optional Fields: graceOnPrincipalPayment, graceOnInterestPayment, graceOnInterestCharged, linkAccountId, allowPartialPeriodInterestCalcualtion, fixedEmiAmount, maxOutstandingLoanBalance, disbursementData, graceOnArrearsAgeing, createStandingInstructionAtDisbursement (requires linkedAccountId if set to true)\n"
            + "Additional Mandatory Fields if interest recalculation is enabled for product and Rest frequency not same as repayment period: recalculationRestFrequencyDate\n"
            + "Additional Mandatory Fields if interest recalculation with interest/fee compounding is enabled for product and compounding frequency not same as repayment period: recalculationCompoundingFrequencyDate\n"
            + "Additional Mandatory Field if Entity-Datatable Check is enabled for the entity of type loan: datatables")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.PostLoansRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.PostLoansResponse.class))) })
    public String calculateLoanScheduleOrSubmitLoanApplication(
            @QueryParam("command") @Parameter(description = "command") final String commandParam, @Context final UriInfo uriInfo,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        if (CommandParameterUtil.is(commandParam, "calculateLoanSchedule")) {

            final JsonElement parsedQuery = this.fromJsonHelper.parse(apiRequestBodyAsJson);
            final JsonQuery query = JsonQuery.from(apiRequestBodyAsJson, parsedQuery, this.fromJsonHelper);

            final LoanScheduleModel loanSchedule = this.calculationPlatformService.calculateLoanSchedule(query, true);

            final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
            return this.loanScheduleToApiJsonSerializer.serialize(settings, loanSchedule.toData(), new HashSet<>());
        }

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createLoanApplication().withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{loanId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Modify a loan application", description = "Loan application can only be modified when in 'Submitted and pending approval' state. Once the application is approved, the details cannot be changed using this method.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.PutLoansLoanIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.PutLoansLoanIdResponse.class))) })
    public String modifyLoanApplication(@PathParam("loanId") @Parameter(description = "loanId", required = true) final Long loanId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        return modifyLoanApplication(loanId, null, commandParam, apiRequestBodyAsJson);
    }

    @DELETE
    @Path("{loanId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete a Loan Application", description = "Note: Only loans in \"Submitted and awaiting approval\" status can be deleted.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.DeleteLoansLoanIdResponse.class))) })
    public String deleteLoanApplication(@PathParam("loanId") @Parameter(description = "loanId", required = true) final Long loanId) {
        return deleteLoanApplication(loanId, null);
    }

    @POST
    @Path("{loanId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Approve Loan Application | Recover Loan Guarantee | Undo Loan Application Approval | Assign a Loan Officer | Unassign a Loan Officer | Reject Loan Application | Applicant Withdraws from Loan Application | Disburse Loan Disburse Loan To Savings Account | Undo Loan Disbursal", description = "Approve Loan Application:\n"
            + "Mandatory Fields: approvedOnDate\n" + "Optional Fields: approvedLoanAmount and expectedDisbursementDate\n"
            + "Approves the loan application\n\n" + "Recover Loan Guarantee:\n" + "Recovers the loan guarantee\n\n"
            + "Undo Loan Application Approval:\n" + "Undoes the Loan Application Approval\n\n" + "Assign a Loan Officer:\n"
            + "Allows you to assign Loan Officer for existing Loan.\n\n" + "Unassign a Loan Officer:\n"
            + "Allows you to unassign the Loan Officer.\n\n" + "Reject Loan Application:\n" + "Mandatory Fields: rejectedOnDate\n"
            + "Allows you to reject the loan application\n\n" + "Applicant Withdraws from Loan Application:\n"
            + "Mandatory Fields: withdrawnOnDate\n" + "Allows the applicant to withdraw the loan application\n\n" + "Disburse Loan:\n"
            + "Mandatory Fields: actualDisbursementDate\n" + "Optional Fields: transactionAmount and fixedEmiAmount\n"
            + "Disburses the Loan\n\n" + "Disburse Loan To Savings Account:\n" + "Mandatory Fields: actualDisbursementDate\n"
            + "Optional Fields: transactionAmount and fixedEmiAmount\n" + "Disburses the loan to Saving Account\n\n"
            + "Undo Loan Disbursal:\n" + "Undoes the Loan Disbursal\n" + "Showing request and response for Assign a Loan Officer")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.PostLoansLoanIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.PostLoansLoanIdResponse.class))) })
    public String stateTransitions(@PathParam("loanId") @Parameter(description = "loanId", required = true) final Long loanId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        return stateTransitions(loanId, null, commandParam, apiRequestBodyAsJson);
    }

    @GET
    @Path("downloadtemplate")
    @Produces("application/vnd.ms-excel")
    public Response getLoansTemplate(@QueryParam("officeId") final Long officeId, @QueryParam("staffId") final Long staffId,
            @QueryParam("dateFormat") final String dateFormat) {
        return bulkImportWorkbookPopulatorService.getTemplate(GlobalEntityType.LOANS.toString(), officeId, staffId, dateFormat);
    }

    @GET
    @Path("glimAccount/{glimId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String getGlimRepaymentTemplate(@PathParam("glimId") final Long glimId, @Context final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        Collection<GlimRepaymentTemplate> glimRepaymentTemplate = this.glimAccountInfoReadPlatformService.findglimRepaymentTemplate(glimId);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.glimTemplateToApiJsonSerializer.serialize(settings, glimRepaymentTemplate, GLIM_ACCOUNTS_DATA_PARAMETERS);
    }

    @POST
    @Path("glimAccount/{glimId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Approve GLIM Application | Undo GLIM Application Approval | Reject GLIM Application | Disburse Loan Disburse Loan To Savings Account | Undo Loan Disbursal", description = "Approve GLIM Application:\n"
            + "Mandatory Fields: approvedOnDate\n" + "Optional Fields: approvedLoanAmount and expectedDisbursementDate\n"
            + "Approves the GLIM application\n\n" + "Undo GLIM Application Approval:\n" + "Undoes the GLIM Application Approval\n\n"
            + "Reject GLIM Application:\n" + "Mandatory Fields: rejectedOnDate\n" + "Allows you to reject the GLIM application\n\n"
            + "Disburse Loan:\n" + "Mandatory Fields: actualDisbursementDate\n" + "Optional Fields: transactionAmount and fixedEmiAmount\n"
            + "Disburses the Loan\n\n" + "Disburse Loan To Savings Account:\n" + "Mandatory Fields: actualDisbursementDate\n"
            + "Optional Fields: transactionAmount and fixedEmiAmount\n" + "Disburses the loan to Saving Account\n\n"
            + "Undo Loan Disbursal:\n" + "Undoes the Loan Disbursal\n")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.PostLoansLoanIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.PostLoansLoanIdResponse.class))) })
    public String glimStateTransitions(@PathParam("glimId") final Long glimId, @QueryParam("command") final String commandParam,
            final String apiRequestBodyAsJson) {

        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);

        CommandWrapper commandRequest = null;
        if (CommandParameterUtil.is(commandParam, "reject")) {
            commandRequest = builder.rejectGLIMApplication(glimId).build();
        } else if (CommandParameterUtil.is(commandParam, "approve")) {
            commandRequest = builder.approveGLIMLoanApplication(glimId).build();
        } else if (CommandParameterUtil.is(commandParam, "disburse")) {
            commandRequest = builder.disburseGlimLoanApplication(glimId).build();
        } else if (CommandParameterUtil.is(commandParam, "glimrepayment")) {
            commandRequest = builder.repaymentGlimLoanApplication(glimId).build();
        } else if (CommandParameterUtil.is(commandParam, "undodisbursal")) {
            commandRequest = builder.undoGLIMLoanDisbursal(glimId).build();
        } else if (CommandParameterUtil.is(commandParam, "undoapproval")) {
            commandRequest = builder.undoGLIMLoanApproval(glimId).build();
        }

        if (commandRequest == null) {
            throw new UnrecognizedQueryParamException("command", commandParam);
        }
        CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Path("repayments/downloadtemplate")
    @Produces("application/vnd.ms-excel")
    public Response getLoanRepaymentTemplate(@QueryParam("officeId") final Long officeId,
            @QueryParam("dateFormat") final String dateFormat) {
        return bulkImportWorkbookPopulatorService.getTemplate(GlobalEntityType.LOAN_TRANSACTIONS.toString(), officeId, null, dateFormat);
    }

    @POST
    @Path("uploadtemplate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RequestBody(description = "Upload Loan template", content = {
            @Content(mediaType = MediaType.MULTIPART_FORM_DATA, schema = @Schema(implementation = UploadRequest.class)) })
    public String postLoanTemplate(@FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail, @FormDataParam("locale") final String locale,
            @FormDataParam("dateFormat") final String dateFormat) {
        final Long importDocumentId = this.bulkImportWorkbookService.importWorkbook(GlobalEntityType.LOANS.toString(), uploadedInputStream,
                fileDetail, locale, dateFormat);
        return this.toApiJsonSerializer.serialize(importDocumentId);
    }

    @POST
    @Path("repayments/uploadtemplate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RequestBody(description = "Upload Loan repayments template", content = {
            @Content(mediaType = MediaType.MULTIPART_FORM_DATA, schema = @Schema(implementation = UploadRequest.class)) })
    public String postLoanRepaymentTemplate(@FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail, @FormDataParam("locale") final String locale,
            @FormDataParam("dateFormat") final String dateFormat) {
        final Long importDocumentId = this.bulkImportWorkbookService.importWorkbook(GlobalEntityType.LOAN_TRANSACTIONS.toString(),
                uploadedInputStream, fileDetail, locale, dateFormat);
        return this.toApiJsonSerializer.serialize(importDocumentId);
    }

    @GET
    @Path("{loanId}/delinquencytags")
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve the Loan Delinquency Tag history using the Loan Id", description = "")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = DelinquencyApiResourceSwagger.GetDelinquencyTagHistoryResponse.class)))) })
    public String getDelinquencyTagHistory(@PathParam("loanId") @Parameter(description = "loanId", required = true) final Long loanId,
            @Context final UriInfo uriInfo) {
        return getDelinquencyTagHistory(loanId, null, uriInfo);
    }

    // External id related APIs
    @GET
    @Path("external-id/{loanExternalId}/template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.GetLoansApprovalTemplateResponse.class))) })
    public String retrieveApprovalTemplate(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId", required = true) final String loanExternalId,
            @QueryParam("templateType") @Parameter(description = "templateType") final String templateType,
            @Context final UriInfo uriInfo) {
        return retrieveApprovalTemplate(null, loanExternalId, templateType, uriInfo);
    }

    @GET
    @Path("external-id/{loanExternalId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Loan", description = "Note: template=true parameter doesn't apply to this resource."
            + "Example Requests:\n" + "\n" + "loans/external-id/7dd80a7c-ycba-a446-t378-91eb6f53e854\n" + "\n" + "\n"
            + "loans/external-id/7dd80a7c-ycba-a446-t378-91eb6f53e854?fields=id,principal,annualInterestRate\n" + "\n" + "\n"
            + "loans/external-id/7dd80a7c-ycba-a446-t378-91eb6f53e854?associations=all\n" + "\n"
            + "loans/external-id/7dd80a7c-ycba-a446-t378-91eb6f53e854?associations=all&exclude=guarantors\n" + "\n" + "\n"
            + "loans/external-id/7dd80a7c-ycba-a446-t378-91eb6f53e854?fields=id,principal,annualInterestRate&associations=repaymentSchedule,transactions")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.GetLoansLoanIdResponse.class))) })
    public String retrieveLoan(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId", required = true) final String loanExternalId,
            @DefaultValue("false") @QueryParam("staffInSelectedOfficeOnly") @Parameter(description = "staffInSelectedOfficeOnly") final boolean staffInSelectedOfficeOnly,
            @DefaultValue("all") @QueryParam("associations") @Parameter(in = ParameterIn.QUERY, name = "associations", description = "Loan object relations to be included in the response", required = false, examples = {
                    @ExampleObject(value = "all"), @ExampleObject(value = "repaymentSchedule,transactions") }) final String associations,
            @QueryParam("exclude") @Parameter(in = ParameterIn.QUERY, name = "exclude", description = "Optional Loan object relation list to be filtered in the response", required = false, example = "guarantors,futureSchedule") final String exclude,
            @QueryParam("fields") @Parameter(in = ParameterIn.QUERY, name = "fields", description = "Optional Loan attribute list to be in the response", required = false, example = "id,principal,annualInterestRate") final String fields,
            @Context final UriInfo uriInfo) {
        return retrieveLoan(null, loanExternalId, staffInSelectedOfficeOnly, exclude, uriInfo);
    }

    @PUT
    @Path("external-id/{loanExternalId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Modify a loan application", description = "Loan application can only be modified when in 'Submitted and pending approval' state. Once the application is approved, the details cannot be changed using this method.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.PutLoansLoanIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.PutLoansLoanIdResponse.class))) })
    public String modifyLoanApplication(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId", required = true) final String loanExternalId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        return modifyLoanApplication(null, loanExternalId, commandParam, apiRequestBodyAsJson);
    }

    @DELETE
    @Path("external-id/{loanExternalId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete a Loan Application", description = "Note: Only loans in \"Submitted and awaiting approval\" status can be deleted.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.DeleteLoansLoanIdResponse.class))) })
    public String deleteLoanApplication(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId", required = true) final String loanExternalId) {
        return deleteLoanApplication(null, loanExternalId);
    }

    @POST
    @Path("external-id/{loanExternalId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Approve Loan Application | Recover Loan Guarantee | Undo Loan Application Approval | Assign a Loan Officer | Unassign a Loan Officer | Reject Loan Application | Applicant Withdraws from Loan Application | Disburse Loan Disburse Loan To Savings Account | Undo Loan Disbursal", description = "Approve Loan Application:\n"
            + "Mandatory Fields: approvedOnDate\n" + "Optional Fields: approvedLoanAmount and expectedDisbursementDate\n"
            + "Approves the loan application\n\n" + "Recover Loan Guarantee:\n" + "Recovers the loan guarantee\n\n"
            + "Undo Loan Application Approval:\n" + "Undoes the Loan Application Approval\n\n" + "Assign a Loan Officer:\n"
            + "Allows you to assign Loan Officer for existing Loan.\n\n" + "Unassign a Loan Officer:\n"
            + "Allows you to unassign the Loan Officer.\n\n" + "Reject Loan Application:\n" + "Mandatory Fields: rejectedOnDate\n"
            + "Allows you to reject the loan application\n\n" + "Applicant Withdraws from Loan Application:\n"
            + "Mandatory Fields: withdrawnOnDate\n" + "Allows the applicant to withdraw the loan application\n\n" + "Disburse Loan:\n"
            + "Mandatory Fields: actualDisbursementDate\n" + "Optional Fields: transactionAmount and fixedEmiAmount\n"
            + "Disburses the Loan\n\n" + "Disburse Loan To Savings Account:\n" + "Mandatory Fields: actualDisbursementDate\n"
            + "Optional Fields: transactionAmount and fixedEmiAmount\n" + "Disburses the loan to Saving Account\n\n"
            + "Undo Loan Disbursal:\n" + "Undoes the Loan Disbursal\n" + "Showing request and response for Assign a Loan Officer")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.PostLoansLoanIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.PostLoansLoanIdResponse.class))) })
    public String stateTransitions(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId", required = true) final String loanExternalId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        return stateTransitions(null, loanExternalId, commandParam, apiRequestBodyAsJson);
    }

    @GET
    @Path("external-id/{loanExternalId}/delinquencytags")
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve the Loan Delinquency Tag history using the Loan Id", description = "")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = DelinquencyApiResourceSwagger.GetDelinquencyTagHistoryResponse.class)))) })
    public String getDelinquencyTagHistory(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId", required = true) final String loanExternalId,
            @Context final UriInfo uriInfo) {
        return getDelinquencyTagHistory(null, loanExternalId, uriInfo);
    }

    @GET
    @Path("{loanId}/delinquency-actions")
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve delinquency actions related to the loan", description = "")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = DelinquencyApiResourceSwagger.GetDelinquencyActionsResponse.class)))) })
    public String getLoanDelinquencyActions(@PathParam("loanId") @Parameter(description = "loanId", required = true) final Long loanId,
            @Context final UriInfo uriInfo) {
        return getLoanDelinquencyActions(loanId, null, uriInfo);
    }

    @GET
    @Path("external-id/{loanExternalId}/delinquency-actions")
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve delinquency actions related to the loan", description = "")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = DelinquencyApiResourceSwagger.GetDelinquencyActionsResponse.class)))) })
    public String getLoanDelinquencyActions(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId", required = true) final String loanExternalId,
            @Context final UriInfo uriInfo) {
        return getLoanDelinquencyActions(null, loanExternalId, uriInfo);
    }

    @POST
    @Path("{loanId}/delinquency-actions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Adds a new delinquency action for a loan", description = "")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = DelinquencyApiResourceSwagger.PostLoansDelinquencyActionRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DelinquencyApiResourceSwagger.PostLoansDelinquencyActionResponse.class))) })
    public String createLoanDelinquencyAction(@PathParam("loanId") @Parameter(description = "loanId", required = true) final Long loanId,
            @Context final UriInfo uriInfo, @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        return createLoanDelinquencyAction(loanId, ExternalId.empty(), apiRequestBodyAsJson);
    }

    @POST
    @Path("external-id/{loanExternalId}/delinquency-actions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Adds a new delinquency action for a loan", description = "")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = DelinquencyApiResourceSwagger.PostLoansDelinquencyActionRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DelinquencyApiResourceSwagger.PostLoansDelinquencyActionResponse.class))) })
    public String createLoanDelinquencyAction(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId", required = true) final String loanExternalId,
            @Context final UriInfo uriInfo, @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        return createLoanDelinquencyAction(null, ExternalIdFactory.produce(loanExternalId), apiRequestBodyAsJson);
    }

    private String retrieveApprovalTemplate(final Long loanId, final String loanExternalIdStr, final String templateType,
            final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        LoanApprovalData loanApprovalTemplate = null;
        ExternalId loanExternalId = ExternalIdFactory.produce(loanExternalIdStr);
        Long resolvedLoanId = getResolvedLoanId(loanId, loanExternalId);
        if (templateType == null) {
            final String errorMsg = "Loan template type must be provided";
            throw new LoanTemplateTypeRequiredException(errorMsg);
        } else if (templateType.equals("approval")) {
            loanApprovalTemplate = this.loanReadPlatformService.retrieveApprovalTemplate(resolvedLoanId);
        }

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.loanApprovalDataToApiJsonSerializer.serialize(settings, loanApprovalTemplate, LOAN_APPROVAL_DATA_PARAMETERS);
    }

    private String retrieveLoan(final Long loanId, final String loanExternalIdStr, boolean staffInSelectedOfficeOnly, final String exclude,
            final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        ExternalId loanExternalId = ExternalIdFactory.produce(loanExternalIdStr);
        Long resolvedLoanId = getResolvedLoanId(loanId, loanExternalId);
        LoanAccountData loanBasicDetails = this.loanReadPlatformService.retrieveOne(resolvedLoanId);
        if (loanBasicDetails.isInterestRecalculationEnabled()) {
            Collection<CalendarData> interestRecalculationCalendarDatas = this.calendarReadPlatformService.retrieveCalendarsByEntity(
                    loanBasicDetails.getInterestRecalculationDetailId(), CalendarEntityType.LOAN_RECALCULATION_REST_DETAIL.getValue(),
                    null);
            CalendarData calendarData = null;
            if (!CollectionUtils.isEmpty(interestRecalculationCalendarDatas)) {
                calendarData = interestRecalculationCalendarDatas.iterator().next();
            }

            Collection<CalendarData> interestRecalculationCompoundingCalendarDatas = this.calendarReadPlatformService
                    .retrieveCalendarsByEntity(loanBasicDetails.getInterestRecalculationDetailId(),
                            CalendarEntityType.LOAN_RECALCULATION_COMPOUNDING_DETAIL.getValue(), null);
            CalendarData compoundingCalendarData = null;
            if (!CollectionUtils.isEmpty(interestRecalculationCompoundingCalendarDatas)) {
                compoundingCalendarData = interestRecalculationCompoundingCalendarDatas.iterator().next();
            }
            loanBasicDetails = loanBasicDetails.withInterestRecalculationCalendarData(calendarData, compoundingCalendarData);
        }
        if (loanBasicDetails.getRepaymentFrequencyType() != null
                && loanBasicDetails.getRepaymentFrequencyType().getId().intValue() == PeriodFrequencyType.MONTHS.getValue()) {
            Collection<CalendarData> loanCalendarDatas = this.calendarReadPlatformService.retrieveCalendarsByEntity(resolvedLoanId,
                    CalendarEntityType.LOANS.getValue(), null);
            CalendarData calendarData = null;
            if (!CollectionUtils.isEmpty(loanCalendarDatas)) {
                calendarData = loanCalendarDatas.iterator().next();
            }
            if (calendarData != null) {
                loanBasicDetails = loanBasicDetails.setMeeting(calendarData);
            }
        }
        Collection<InterestRatePeriodData> interestRatesPeriods = this.loanReadPlatformService
                .retrieveLoanInterestRatePeriodData(loanBasicDetails);
        Collection<LoanTransactionData> loanRepayments = null;
        LoanScheduleData repaymentSchedule = null;
        Collection<LoanChargeData> charges = null;
        Collection<GuarantorData> guarantors = null;
        CalendarData meeting = null;
        Collection<NoteData> notes = null;
        PortfolioAccountData linkedAccount = null;
        Collection<DisbursementData> disbursementData = null;
        Collection<LoanTermVariationsData> emiAmountVariations = null;
        Collection<LoanCollateralResponseData> loanCollateralManagements;
        Collection<LoanCollateralManagementData> loanCollateralManagementData = new ArrayList<>();
        CollectionData collectionData = this.delinquencyReadPlatformService.calculateLoanCollectionData(resolvedLoanId);

        final Set<String> mandatoryResponseParameters = new HashSet<>();
        final Set<String> associationParameters = ApiParameterHelper.extractAssociationsForResponseIfProvided(uriInfo.getQueryParameters());
        if (!associationParameters.isEmpty()) {
            if (associationParameters.contains(DataTableApiConstant.allAssociateParamName)) {
                associationParameters.addAll(Arrays.asList(DataTableApiConstant.repaymentScheduleAssociateParamName,
                        DataTableApiConstant.futureScheduleAssociateParamName, DataTableApiConstant.originalScheduleAssociateParamName,
                        DataTableApiConstant.transactionsAssociateParamName, DataTableApiConstant.chargesAssociateParamName,
                        DataTableApiConstant.guarantorsAssociateParamName, DataTableApiConstant.collateralAssociateParamName,
                        DataTableApiConstant.notesAssociateParamName, DataTableApiConstant.linkedAccountAssociateParamName,
                        DataTableApiConstant.multiDisburseDetailsAssociateParamName, DataTableApiConstant.collectionAssociateParamName));
            }

            ApiParameterHelper.excludeAssociationsForResponseIfProvided(exclude, associationParameters);

            if (associationParameters.contains(DataTableApiConstant.guarantorsAssociateParamName)) {
                mandatoryResponseParameters.add(DataTableApiConstant.guarantorsAssociateParamName);
                guarantors = this.guarantorReadPlatformService.retrieveGuarantorsForLoan(resolvedLoanId);
                if (CollectionUtils.isEmpty(guarantors)) {
                    guarantors = null;
                }
            }

            if (associationParameters.contains(DataTableApiConstant.transactionsAssociateParamName)) {
                mandatoryResponseParameters.add(DataTableApiConstant.transactionsAssociateParamName);
                loanRepayments = this.loanReadPlatformService.retrieveLoanTransactions(resolvedLoanId);
            }

            if (associationParameters.contains(DataTableApiConstant.multiDisburseDetailsAssociateParamName)
                    || associationParameters.contains(DataTableApiConstant.repaymentScheduleAssociateParamName)) {
                mandatoryResponseParameters.add(DataTableApiConstant.multiDisburseDetailsAssociateParamName);
                disbursementData = this.loanReadPlatformService.retrieveLoanDisbursementDetails(resolvedLoanId);
            }

            if (associationParameters.contains(DataTableApiConstant.emiAmountVariationsAssociateParamName)
                    || associationParameters.contains(DataTableApiConstant.repaymentScheduleAssociateParamName)) {
                mandatoryResponseParameters.add(DataTableApiConstant.emiAmountVariationsAssociateParamName);
                emiAmountVariations = this.loanReadPlatformService.retrieveLoanTermVariations(resolvedLoanId,
                        LoanTermVariationType.EMI_AMOUNT.getValue());
            }

            if (associationParameters.contains(DataTableApiConstant.repaymentScheduleAssociateParamName)) {
                mandatoryResponseParameters.add(DataTableApiConstant.repaymentScheduleAssociateParamName);
                final RepaymentScheduleRelatedLoanData repaymentScheduleRelatedData = new RepaymentScheduleRelatedLoanData(
                        loanBasicDetails.getTimeline().getExpectedDisbursementDate(),
                        loanBasicDetails.getTimeline().getActualDisbursementDate(), loanBasicDetails.getCurrency(),
                        loanBasicDetails.getPrincipal(), loanBasicDetails.getInArrearsTolerance(),
                        loanBasicDetails.getFeeChargesAtDisbursementCharged());
                repaymentSchedule = this.loanReadPlatformService.retrieveRepaymentSchedule(resolvedLoanId, repaymentScheduleRelatedData,
                        disbursementData, loanBasicDetails.isInterestRecalculationEnabled(),
                        LoanScheduleType.fromEnumOptionData(loanBasicDetails.getLoanScheduleType()));

                if (associationParameters.contains(DataTableApiConstant.futureScheduleAssociateParamName)
                        && loanBasicDetails.isInterestRecalculationEnabled()) {
                    mandatoryResponseParameters.add(DataTableApiConstant.futureScheduleAssociateParamName);
                    this.calculationPlatformService.updateFutureSchedule(repaymentSchedule, resolvedLoanId);
                }

                if (associationParameters.contains(DataTableApiConstant.originalScheduleAssociateParamName)
                        && loanBasicDetails.isInterestRecalculationEnabled()
                        && LoanStatus.fromInt(loanBasicDetails.getStatus().getId().intValue()).isActive()) {
                    mandatoryResponseParameters.add(DataTableApiConstant.originalScheduleAssociateParamName);
                    LoanScheduleData loanScheduleData = this.loanScheduleHistoryReadPlatformService.retrieveRepaymentArchiveSchedule(
                            resolvedLoanId, repaymentScheduleRelatedData, disbursementData,
                            LoanScheduleType.fromEnumOptionData(loanBasicDetails.getLoanScheduleType()));
                    loanBasicDetails = loanBasicDetails.setOriginalSchedule(loanScheduleData);
                }
            }

            if (associationParameters.contains(DataTableApiConstant.chargesAssociateParamName)) {
                mandatoryResponseParameters.add(DataTableApiConstant.chargesAssociateParamName);
                charges = this.loanChargeReadPlatformService.retrieveLoanCharges(resolvedLoanId);
                if (CollectionUtils.isEmpty(charges)) {
                    charges = null;
                }
            }

            if (associationParameters.contains(DataTableApiConstant.collateralAssociateParamName)) {
                mandatoryResponseParameters.add(DataTableApiConstant.collateralAssociateParamName);
                loanCollateralManagements = this.loanCollateralManagementReadPlatformService
                        .getLoanCollateralResponseDataList(resolvedLoanId);
                for (LoanCollateralResponseData loanCollateralManagement : loanCollateralManagements) {
                    loanCollateralManagementData.add(loanCollateralManagement.toCommand());
                }
            }

            if (associationParameters.contains(DataTableApiConstant.meetingAssociateParamName)) {
                mandatoryResponseParameters.add(DataTableApiConstant.meetingAssociateParamName);
                meeting = this.calendarReadPlatformService.retrieveLoanCalendar(resolvedLoanId);
            }

            if (associationParameters.contains(DataTableApiConstant.notesAssociateParamName)) {
                mandatoryResponseParameters.add(DataTableApiConstant.notesAssociateParamName);
                notes = this.noteReadPlatformService.retrieveNotesByResource(resolvedLoanId, NoteType.LOAN.getValue());
                if (CollectionUtils.isEmpty(notes)) {
                    notes = null;
                }
            }

            if (associationParameters.contains(DataTableApiConstant.linkedAccountAssociateParamName)) {
                mandatoryResponseParameters.add(DataTableApiConstant.linkedAccountAssociateParamName);
                linkedAccount = this.accountAssociationsReadPlatformService.retriveLoanLinkedAssociation(resolvedLoanId);
            }
        }

        Collection<LoanProductData> productOptions = null;
        LoanProductData product;
        Collection<EnumOptionData> loanTermFrequencyTypeOptions = null;
        Collection<EnumOptionData> repaymentFrequencyTypeOptions = null;
        Collection<EnumOptionData> repaymentFrequencyNthDayTypeOptions = null;
        Collection<EnumOptionData> repaymentFrequencyDayOfWeekTypeOptions = null;
        Collection<TransactionProcessingStrategyData> repaymentStrategyOptions = null;
        Collection<EnumOptionData> interestRateFrequencyTypeOptions = null;
        Collection<EnumOptionData> amortizationTypeOptions = null;
        Collection<EnumOptionData> interestTypeOptions = null;
        Collection<EnumOptionData> interestCalculationPeriodTypeOptions = null;
        Collection<FundData> fundOptions = null;
        Collection<StaffData> allowedLoanOfficers = null;
        Collection<ChargeData> chargeOptions = null;
        ChargeData chargeTemplate = null;
        Collection<CodeValueData> loanPurposeOptions = null;
        Collection<CodeValueData> loanCollateralOptions = null;
        Collection<CalendarData> calendarOptions = null;
        Collection<PortfolioAccountData> accountLinkingOptions = null;
        PaidInAdvanceData paidInAdvanceTemplate;
        Collection<LoanAccountSummaryData> clientActiveLoanOptions = null;

        final boolean template = ApiParameterHelper.template(uriInfo.getQueryParameters());
        if (template) {
            productOptions = this.loanProductReadPlatformService.retrieveAllLoanProductsForLookup();
            product = this.loanProductReadPlatformService.retrieveLoanProduct(loanBasicDetails.getLoanProductId());
            loanBasicDetails.setProduct(product);
            loanTermFrequencyTypeOptions = this.dropdownReadPlatformService.retrieveLoanTermFrequencyTypeOptions();
            repaymentFrequencyTypeOptions = this.dropdownReadPlatformService.retrieveRepaymentFrequencyTypeOptions();
            repaymentFrequencyNthDayTypeOptions = this.dropdownReadPlatformService.retrieveRepaymentFrequencyOptionsForNthDayOfMonth();
            repaymentFrequencyDayOfWeekTypeOptions = this.dropdownReadPlatformService.retrieveRepaymentFrequencyOptionsForDaysOfWeek();
            interestRateFrequencyTypeOptions = this.dropdownReadPlatformService.retrieveInterestRateFrequencyTypeOptions();

            amortizationTypeOptions = this.dropdownReadPlatformService.retrieveLoanAmortizationTypeOptions();
            if (product.isLinkedToFloatingInterestRates()) {
                interestTypeOptions = Collections.singletonList(interestType(InterestMethod.DECLINING_BALANCE));
            } else {
                interestTypeOptions = this.dropdownReadPlatformService.retrieveLoanInterestTypeOptions();
            }
            interestCalculationPeriodTypeOptions = this.dropdownReadPlatformService.retrieveLoanInterestRateCalculatedInPeriodOptions();

            fundOptions = this.fundReadPlatformService.retrieveAllFunds();
            repaymentStrategyOptions = this.dropdownReadPlatformService.retrieveTransactionProcessingStrategies();
            if (product.getMultiDisburseLoan()) {
                chargeOptions = this.chargeReadPlatformService.retrieveLoanAccountApplicableCharges(resolvedLoanId,
                        new ChargeTimeType[] { ChargeTimeType.OVERDUE_INSTALLMENT });
            } else {
                chargeOptions = this.chargeReadPlatformService.retrieveLoanAccountApplicableCharges(resolvedLoanId,
                        new ChargeTimeType[] { ChargeTimeType.OVERDUE_INSTALLMENT, ChargeTimeType.TRANCHE_DISBURSEMENT });
            }
            chargeTemplate = this.loanChargeReadPlatformService.retrieveLoanChargeTemplate();

            Long officeId = loanBasicDetails.getClientOfficeId();

            if (officeId == null && loanBasicDetails.getGroup() != null) {
                officeId = loanBasicDetails.getGroup().getOfficeId();
            }
            allowedLoanOfficers = this.loanReadPlatformService.retrieveAllowedLoanOfficers(officeId, staffInSelectedOfficeOnly);

            loanPurposeOptions = this.codeValueReadPlatformService.retrieveCodeValuesByCode("LoanPurpose");
            loanCollateralOptions = this.codeValueReadPlatformService.retrieveCodeValuesByCode("LoanCollateral");
            final CurrencyData currencyData = loanBasicDetails.getCurrency();
            String currencyCode = null;
            if (currencyData != null) {
                currencyCode = currencyData.getCode();
            }
            final long[] accountStatus = { SavingsAccountStatusType.ACTIVE.getValue() };
            PortfolioAccountDTO portfolioAccountDTO = new PortfolioAccountDTO(PortfolioAccountType.SAVINGS.getValue(),
                    loanBasicDetails.getClientId(), currencyCode, accountStatus, DepositAccountType.SAVINGS_DEPOSIT.getValue());
            accountLinkingOptions = this.portfolioAccountReadPlatformService.retrieveAllForLookup(portfolioAccountDTO);

            if (!associationParameters.contains(DataTableApiConstant.linkedAccountAssociateParamName)) {
                mandatoryResponseParameters.add(DataTableApiConstant.linkedAccountAssociateParamName);
                linkedAccount = this.accountAssociationsReadPlatformService.retriveLoanLinkedAssociation(resolvedLoanId);
            }
            if (loanBasicDetails.getGroup() != null && loanBasicDetails.getGroup().getId() != null) {
                calendarOptions = this.loanReadPlatformService.retrieveCalendars(loanBasicDetails.getGroup().getId());
            }

            if (loanBasicDetails.getProduct().isCanUseForTopup() && loanBasicDetails.getClientId() != null) {
                clientActiveLoanOptions = this.accountDetailsReadPlatformService
                        .retrieveClientActiveLoanAccountSummary(loanBasicDetails.getClientId());
            }

        }

        Collection<ChargeData> overdueCharges = this.chargeReadPlatformService
                .retrieveLoanProductCharges(loanBasicDetails.getLoanProductId(), ChargeTimeType.OVERDUE_INSTALLMENT);

        paidInAdvanceTemplate = this.loanReadPlatformService.retrieveTotalPaidInAdvance(resolvedLoanId);

        // Get rates from Loan
        boolean isRatesEnabled = this.configurationDomainService.isSubRatesEnabled();
        List<RateData> rates = null;
        if (isRatesEnabled) {
            rates = this.rateReadService.retrieveLoanRates(resolvedLoanId);
        }

        // updating summary with transaction amounts summary
        if (loanBasicDetails.getSummary() != null) {
            loanBasicDetails.setSummary(LoanSummaryData.withTransactionAmountsSummary(loanBasicDetails.getSummary(), repaymentSchedule,
                    loanSummaryBalancesRepository.retrieveLoanSummaryBalancesByTransactionType(loanBasicDetails.getId(),
                            LoanApiConstants.LOAN_SUMMARY_TRANSACTION_TYPES)));
        }

        final LoanAccountData loanAccount = loanBasicDetails.associationsAndTemplate(repaymentSchedule, loanRepayments, charges,
                loanCollateralManagementData, guarantors, meeting, productOptions, loanTermFrequencyTypeOptions,
                repaymentFrequencyTypeOptions, repaymentFrequencyNthDayTypeOptions, repaymentFrequencyDayOfWeekTypeOptions,
                repaymentStrategyOptions, interestRateFrequencyTypeOptions, amortizationTypeOptions, interestTypeOptions,
                interestCalculationPeriodTypeOptions, fundOptions, chargeOptions, chargeTemplate, allowedLoanOfficers, loanPurposeOptions,
                loanCollateralOptions, calendarOptions, notes, accountLinkingOptions, linkedAccount, disbursementData, emiAmountVariations,
                overdueCharges, paidInAdvanceTemplate, interestRatesPeriods, clientActiveLoanOptions, rates, isRatesEnabled, collectionData,
                LoanScheduleType.getValuesAsEnumOptionDataList(), LoanScheduleProcessingType.getValuesAsEnumOptionDataList());

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters(),
                mandatoryResponseParameters);
        return this.toApiJsonSerializer.serialize(settings, loanAccount, LOAN_DATA_PARAMETERS);
    }

    private String modifyLoanApplication(final Long loanId, final String loanExternalIdStr, final String commandParam,
            final String apiRequestBodyAsJson) {
        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);
        CommandWrapper commandRequest;
        ExternalId loanExternalId = ExternalIdFactory.produce(loanExternalIdStr);
        Long resolvedLoanId = getResolvedLoanId(loanId, loanExternalId);
        if (CommandParameterUtil.is(commandParam, LoanApiConstants.MARK_AS_FRAUD_COMMAND)) {
            commandRequest = builder.markAsFraud(resolvedLoanId).build();
        } else {
            commandRequest = builder.updateLoanApplication(resolvedLoanId).build();
        }

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }

    private String deleteLoanApplication(final Long loanId, final String loanExternalIdStr) {
        ExternalId loanExternalId = ExternalIdFactory.produce(loanExternalIdStr);
        Long resolvedLoanId = getResolvedLoanId(loanId, loanExternalId);
        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteLoanApplication(resolvedLoanId).build();
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }

    private String stateTransitions(final Long loanId, final String loanExternalIdStr, final String commandParam,
            final String apiRequestBodyAsJson) {
        ExternalId loanExternalId = ExternalIdFactory.produce(loanExternalIdStr);
        Long resolvedLoanId = getResolvedLoanId(loanId, loanExternalId);
        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);
        CommandWrapper commandRequest = null;
        if (CommandParameterUtil.is(commandParam, "reject")) {
            commandRequest = builder.rejectLoanApplication(resolvedLoanId).build();
        } else if (CommandParameterUtil.is(commandParam, "withdrawnByApplicant")) {
            commandRequest = builder.withdrawLoanApplication(resolvedLoanId).build();
        } else if (CommandParameterUtil.is(commandParam, "approve")) {
            commandRequest = builder.approveLoanApplication(resolvedLoanId).build();
        } else if (CommandParameterUtil.is(commandParam, "disburse")) {
            commandRequest = builder.disburseLoanApplication(resolvedLoanId).build();
        } else if (CommandParameterUtil.is(commandParam, "disburseToSavings")) {
            commandRequest = builder.disburseLoanToSavingsApplication(resolvedLoanId).build();
        } else if (CommandParameterUtil.is(commandParam, "disburseWithoutAutoDownPayment")) {
            commandRequest = builder.disburseWithoutAutoDownPayment(resolvedLoanId).build();
        } else if (CommandParameterUtil.is(commandParam, "undoapproval")) {
            commandRequest = builder.undoLoanApplicationApproval(resolvedLoanId).build();
        } else if (CommandParameterUtil.is(commandParam, "undodisbursal")) {
            commandRequest = builder.undoLoanApplicationDisbursal(resolvedLoanId).build();
        } else if (CommandParameterUtil.is(commandParam, "undolastdisbursal")) {
            commandRequest = builder.undoLastDisbursalLoanApplication(resolvedLoanId).build();
        } else if (CommandParameterUtil.is(commandParam, "assignloanofficer")) {
            commandRequest = builder.assignLoanOfficer(resolvedLoanId).build();
        } else if (CommandParameterUtil.is(commandParam, "unassignloanofficer")) {
            commandRequest = builder.unassignLoanOfficer(resolvedLoanId).build();
        } else if (CommandParameterUtil.is(commandParam, "recoverGuarantees")) {
            commandRequest = new CommandWrapperBuilder().recoverFromGuarantor(resolvedLoanId).build();
        } else if (CommandParameterUtil.is(commandParam, "assigndelinquency")) {
            commandRequest = builder.assignDelinquency(resolvedLoanId).build();
        }

        if (commandRequest == null) {
            throw new UnrecognizedQueryParamException("command", commandParam);
        }
        CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    private String getDelinquencyTagHistory(final Long loanId, final String loanExternalIdStr, final UriInfo uriInfo) {
        context.authenticatedUser().validateHasReadPermission("DELINQUENCY_TAGS");
        ExternalId loanExternalId = ExternalIdFactory.produce(loanExternalIdStr);
        Long resolvedLoanId = getResolvedLoanId(loanId, loanExternalId);
        final Collection<LoanDelinquencyTagHistoryData> loanDelinquencyTagHistoryData = this.delinquencyReadPlatformService
                .retrieveDelinquencyRangeHistory(resolvedLoanId);
        return this.jsonSerializerTagHistory.serialize(loanDelinquencyTagHistoryData);
    }

    private Long getResolvedLoanId(final Long loanId, final ExternalId loanExternalId) {
        Long resolvedLoanId = loanId;
        if (resolvedLoanId == null) {
            loanExternalId.throwExceptionIfEmpty();
            resolvedLoanId = this.loanReadPlatformService.retrieveLoanIdByExternalId(loanExternalId);
            if (resolvedLoanId == null) {
                throw new LoanNotFoundException(loanExternalId);
            }
        }
        return resolvedLoanId;
    }

    private String getLoanDelinquencyActions(final Long loanId, final String loanExternalIdStr, final UriInfo uriInfo) {
        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        ExternalId loanExternalId = ExternalIdFactory.produce(loanExternalIdStr);
        Long resolvedLoanId = getResolvedLoanId(loanId, loanExternalId);

        final Collection<LoanDelinquencyAction> delinquencyActions = this.delinquencyReadPlatformService
                .retrieveLoanDelinquencyActions(resolvedLoanId);
        List<LoanDelinquencyActionData> result = delinquencyActions.stream().map(LoanDelinquencyActionData::new).toList();
        return this.jsonSerializerTagHistory.serialize(result);
    }

    private String createLoanDelinquencyAction(Long loanId, ExternalId loanExternalId, String apiRequestBodyAsJson) {
        context.authenticatedUser().validateHasCreatePermission(RESOURCE_NAME_FOR_DELINQUENCY_ACTION_PERMISSIONS);
        Long resolvedLoanId = getResolvedLoanId(loanId, loanExternalId);

        CommandWrapperBuilder builder = new CommandWrapperBuilder().createDelinquencyAction(resolvedLoanId);
        builder.withJson(apiRequestBodyAsJson);
        CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(builder.build());
        return delinquencyActionSerializer.serialize(result);
    }

}
