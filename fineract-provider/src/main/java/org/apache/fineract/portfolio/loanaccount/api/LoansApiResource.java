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

import com.google.gson.JsonElement;
import io.swagger.annotations.*;

import static org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations.interestType;

import java.io.InputStream;
import java.util.*;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import org.apache.commons.lang.StringUtils;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookPopulatorService;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookService;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.core.api.ApiParameterHelper;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.api.JsonQuery;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.dataqueries.data.DatatableData;
import org.apache.fineract.infrastructure.dataqueries.data.EntityTables;
import org.apache.fineract.infrastructure.dataqueries.data.StatusEnum;
import org.apache.fineract.infrastructure.dataqueries.service.EntityDatatableChecksReadService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
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
import org.apache.fineract.portfolio.collateral.data.CollateralData;
import org.apache.fineract.portfolio.collateral.service.CollateralReadPlatformService;
import org.apache.fineract.portfolio.floatingrates.data.InterestRatePeriodData;
import org.apache.fineract.portfolio.fund.data.FundData;
import org.apache.fineract.portfolio.fund.service.FundReadPlatformService;
import org.apache.fineract.portfolio.group.data.GroupGeneralData;
import org.apache.fineract.portfolio.group.service.GroupReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.data.*;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTermVariationType;
import org.apache.fineract.portfolio.loanaccount.exception.LoanTemplateTypeRequiredException;
import org.apache.fineract.portfolio.loanaccount.exception.NotSupportedLoanTemplateTypeException;
import org.apache.fineract.portfolio.loanaccount.guarantor.data.GuarantorData;
import org.apache.fineract.portfolio.loanaccount.guarantor.service.GuarantorReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleModel;
import org.apache.fineract.portfolio.loanaccount.loanschedule.service.LoanScheduleCalculationPlatformService;
import org.apache.fineract.portfolio.loanaccount.loanschedule.service.LoanScheduleHistoryReadPlatformService;
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
import org.apache.fineract.portfolio.note.service.NoteReadPlatformServiceImpl;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountStatusType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;


@Path("/loans")
@Component
@Scope("singleton")
@Api(value = "Loans", description = "The API concept of loans models the loan application process and the loan contract/monitoring process.")
public class LoansApiResource {

    private final Set<String> LOAN_DATA_PARAMETERS = new HashSet<>(Arrays.asList("id", "accountNo", "status", "externalId", "clientId",
            "group", "loanProductId", "loanProductName", "loanProductDescription", "isLoanProductLinkedToFloatingRate", "fundId",
            "fundName", "loanPurposeId", "loanPurposeName", "loanOfficerId", "loanOfficerName", "currency", "principal", "totalOverpaid",
            "inArrearsTolerance", "termFrequency", "termPeriodFrequencyType", "numberOfRepayments", "repaymentEvery",
            "interestRatePerPeriod", "annualInterestRate", "repaymentFrequencyType", "transactionProcessingStrategyId",
            "transactionProcessingStrategyName", "interestRateFrequencyType", "amortizationType", "interestType",
            "interestCalculationPeriodType", LoanProductConstants.allowPartialPeriodInterestCalcualtionParamName, "expectedFirstRepaymentOnDate",
            "graceOnPrincipalPayment", "recurringMoratoriumOnPrincipalPeriods", "graceOnInterestPayment", "graceOnInterestCharged", "interestChargedFromDate", "timeline",
            "totalFeeChargesAtDisbursement", "summary", "repaymentSchedule", "transactions", "charges", "collateral", "guarantors",
            "meeting", "productOptions", "amortizationTypeOptions", "interestTypeOptions", "interestCalculationPeriodTypeOptions",
            "repaymentFrequencyTypeOptions", "repaymentFrequencyNthDayTypeOptions", "repaymentFrequencyDaysOfWeekTypeOptions",
            "termFrequencyTypeOptions", "interestRateFrequencyTypeOptions", "fundOptions", "repaymentStrategyOptions", "chargeOptions",
            "loanOfficerOptions", "loanPurposeOptions", "loanCollateralOptions", "chargeTemplate", "calendarOptions",
            "syncDisbursementWithMeeting", "loanCounter", "loanProductCounter", "notes", "accountLinkingOptions", "linkedAccount",
            "interestRateDifferential", "isFloatingInterestRate", "interestRatesPeriods", LoanApiConstants.canUseForTopup,
            LoanApiConstants.isTopup, LoanApiConstants.loanIdToClose, LoanApiConstants.topupAmount, LoanApiConstants.clientActiveLoanOptions,
            LoanApiConstants.datatables));

    private final Set<String> LOAN_APPROVAL_DATA_PARAMETERS = new HashSet<>(Arrays.asList("approvalDate", "approvalAmount"));
    private final String resourceNameForPermissions = "LOAN";

    private final PlatformSecurityContext context;
    private final LoanReadPlatformService loanReadPlatformService;
    private final LoanProductReadPlatformService loanProductReadPlatformService;
    private final LoanDropdownReadPlatformService dropdownReadPlatformService;
    private final FundReadPlatformService fundReadPlatformService;
    private final ChargeReadPlatformService chargeReadPlatformService;
    private final LoanChargeReadPlatformService loanChargeReadPlatformService;
    private final CollateralReadPlatformService loanCollateralReadPlatformService;
    private final LoanScheduleCalculationPlatformService calculationPlatformService;
    private final GuarantorReadPlatformService guarantorReadPlatformService;
    private final CodeValueReadPlatformService codeValueReadPlatformService;
    private final GroupReadPlatformService groupReadPlatformService;
    private final DefaultToApiJsonSerializer<LoanAccountData> toApiJsonSerializer;
    private final DefaultToApiJsonSerializer<LoanApprovalData> loanApprovalDataToApiJsonSerializer;
    private final DefaultToApiJsonSerializer<LoanScheduleData> loanScheduleToApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final FromJsonHelper fromJsonHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final CalendarReadPlatformService calendarReadPlatformService;
    private final NoteReadPlatformServiceImpl noteReadPlatformService;
    private final PortfolioAccountReadPlatformService portfolioAccountReadPlatformService;
    private final AccountAssociationsReadPlatformService accountAssociationsReadPlatformService;
    private final LoanScheduleHistoryReadPlatformService loanScheduleHistoryReadPlatformService;
    private final AccountDetailsReadPlatformService accountDetailsReadPlatformService;
    private final EntityDatatableChecksReadService entityDatatableChecksReadService;
    private final BulkImportWorkbookService bulkImportWorkbookService;
    private final BulkImportWorkbookPopulatorService bulkImportWorkbookPopulatorService;


    @Autowired
    public LoansApiResource(final PlatformSecurityContext context, final LoanReadPlatformService loanReadPlatformService,
            final LoanProductReadPlatformService loanProductReadPlatformService,
            final LoanDropdownReadPlatformService dropdownReadPlatformService, final FundReadPlatformService fundReadPlatformService,
            final ChargeReadPlatformService chargeReadPlatformService, final LoanChargeReadPlatformService loanChargeReadPlatformService,
            final CollateralReadPlatformService loanCollateralReadPlatformService,
            final LoanScheduleCalculationPlatformService calculationPlatformService,
            final GuarantorReadPlatformService guarantorReadPlatformService,
            final CodeValueReadPlatformService codeValueReadPlatformService, final GroupReadPlatformService groupReadPlatformService,
            final DefaultToApiJsonSerializer<LoanAccountData> toApiJsonSerializer,
            final DefaultToApiJsonSerializer<LoanApprovalData> loanApprovalDataToApiJsonSerializer,
            final DefaultToApiJsonSerializer<LoanScheduleData> loanScheduleToApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper, final FromJsonHelper fromJsonHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final CalendarReadPlatformService calendarReadPlatformService, final NoteReadPlatformServiceImpl noteReadPlatformService,
            final PortfolioAccountReadPlatformService portfolioAccountReadPlatformServiceImpl,
            final AccountAssociationsReadPlatformService accountAssociationsReadPlatformService,
            final LoanScheduleHistoryReadPlatformService loanScheduleHistoryReadPlatformService,
            final AccountDetailsReadPlatformService accountDetailsReadPlatformService,
            final EntityDatatableChecksReadService entityDatatableChecksReadService,
            final BulkImportWorkbookService bulkImportWorkbookService,
            final BulkImportWorkbookPopulatorService bulkImportWorkbookPopulatorService) {
        this.context = context;
        this.loanReadPlatformService = loanReadPlatformService;
        this.loanProductReadPlatformService = loanProductReadPlatformService;
        this.dropdownReadPlatformService = dropdownReadPlatformService;
        this.fundReadPlatformService = fundReadPlatformService;
        this.chargeReadPlatformService = chargeReadPlatformService;
        this.loanChargeReadPlatformService = loanChargeReadPlatformService;
        this.loanCollateralReadPlatformService = loanCollateralReadPlatformService;
        this.calculationPlatformService = calculationPlatformService;
        this.guarantorReadPlatformService = guarantorReadPlatformService;
        this.codeValueReadPlatformService = codeValueReadPlatformService;
        this.groupReadPlatformService = groupReadPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.loanApprovalDataToApiJsonSerializer = loanApprovalDataToApiJsonSerializer;
        this.loanScheduleToApiJsonSerializer = loanScheduleToApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.fromJsonHelper = fromJsonHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.calendarReadPlatformService = calendarReadPlatformService;
        this.noteReadPlatformService = noteReadPlatformService;
        this.portfolioAccountReadPlatformService = portfolioAccountReadPlatformServiceImpl;
        this.accountAssociationsReadPlatformService = accountAssociationsReadPlatformService;
        this.loanScheduleHistoryReadPlatformService = loanScheduleHistoryReadPlatformService;
        this.accountDetailsReadPlatformService = accountDetailsReadPlatformService;
        this.entityDatatableChecksReadService = entityDatatableChecksReadService;
        this.bulkImportWorkbookService=bulkImportWorkbookService;
        this.bulkImportWorkbookPopulatorService=bulkImportWorkbookPopulatorService;
    }

    /*
     * This template API is used for loan approval, ideally this should be
     * invoked on loan that are pending for approval. But system does not
     * validate the status of the loan, it returns the template irrespective of
     * loan status
     */

    @GET
    @Path("{loanId}/template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveApprovalTemplate(@PathParam("loanId") @ApiParam(value = "loanId") final Long loanId, @QueryParam("templateType") @ApiParam(value = "templateType") final String templateType,
                                           @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        LoanApprovalData loanApprovalTemplate = null;

        if (templateType == null) {
            final String errorMsg = "Loan template type must be provided";
            throw new LoanTemplateTypeRequiredException(errorMsg);
        } else if (templateType.equals("approval")) {
            loanApprovalTemplate = this.loanReadPlatformService.retrieveApprovalTemplate(loanId);
        }

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.loanApprovalDataToApiJsonSerializer.serialize(settings, loanApprovalTemplate, this.LOAN_APPROVAL_DATA_PARAMETERS);

    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve Loan Details Template", httpMethod = "GET", notes = "This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n" + "\n" + "Field Defaults\n" + "Allowed Value Lists\n" + "Example Requests:\n" + "\n" + "loans/template?templateType=individual&clientId=1\n" + "\n" + "\n" + "loans/template?templateType=individual&clientId=1&productId=1")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = LoansApiResourceSwagger.GetLoansTemplateResponse.class)})
    public String template(@QueryParam("clientId") @ApiParam(value = "clientId") final Long clientId, @QueryParam("groupId") @ApiParam(value = "groupId") final Long groupId,
            @QueryParam("productId")@ApiParam(value = "productId") final Long productId, @QueryParam("templateType") @ApiParam(value = "templateType") final String templateType,
            @DefaultValue("false") @QueryParam("staffInSelectedOfficeOnly") @ApiParam(value = "staffInSelectedOfficeOnly") final boolean staffInSelectedOfficeOnly,
            @DefaultValue("false") @QueryParam("activeOnly") @ApiParam(value = "activeOnly") final boolean onlyActive, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        // template
        final Collection<LoanProductData> productOptions = this.loanProductReadPlatformService.retrieveAllLoanProductsForLookup(onlyActive);

        // options
        Collection<StaffData> allowedLoanOfficers = null;
        Collection<CodeValueData> loanCollateralOptions = null;
        Collection<CalendarData> calendarOptions = null;
        LoanAccountData newLoanAccount = null;
        Long officeId = null;
        Collection<PortfolioAccountData> accountLinkingOptions = null;

        if (productId != null) {
            newLoanAccount = this.loanReadPlatformService.retrieveLoanProductDetailsTemplate(productId, clientId, groupId);
        }

        if (templateType == null) {
            final String errorMsg = "Loan template type must be provided";
            throw new LoanTemplateTypeRequiredException(errorMsg);
        } else if (templateType.equals("collateral")) {
            loanCollateralOptions = this.codeValueReadPlatformService.retrieveCodeValuesByCode("LoanCollateral");
            newLoanAccount = LoanAccountData.collateralTemplate(loanCollateralOptions);
        } else {
            // for JLG loan both client and group details are required
            if (templateType.equals("individual") || templateType.equals("jlg")) {

                if(clientId == null) {
                    newLoanAccount = newLoanAccount == null ? LoanAccountData.emptyTemplate() : newLoanAccount;
                } else {
                    final LoanAccountData loanAccountClientDetails = this.loanReadPlatformService.retrieveClientDetailsTemplate(clientId);

                    officeId = loanAccountClientDetails.officeId();
                    newLoanAccount = newLoanAccount == null ? loanAccountClientDetails : LoanAccountData.populateClientDefaults(newLoanAccount,
                            loanAccountClientDetails);
                }

                // if it's JLG loan add group details
                if (templateType.equals("jlg")) {
                    final GroupGeneralData group = this.groupReadPlatformService.retrieveOne(groupId);
                    newLoanAccount = LoanAccountData.associateGroup(newLoanAccount, group);
                    calendarOptions = this.loanReadPlatformService.retrieveCalendars(groupId);
                }

            } else if (templateType.equals("group")) {

                final LoanAccountData loanAccountGroupData = this.loanReadPlatformService.retrieveGroupDetailsTemplate(groupId);
                officeId = loanAccountGroupData.groupOfficeId();
                calendarOptions = this.loanReadPlatformService.retrieveCalendars(groupId);
                newLoanAccount = newLoanAccount == null ? loanAccountGroupData : LoanAccountData.populateGroupDefaults(newLoanAccount,
                        loanAccountGroupData);
                accountLinkingOptions = getaccountLinkingOptions(newLoanAccount, clientId, groupId);

            } else if (templateType.equals("jlgbulk")) {
                // get group details along with members in that group
                final LoanAccountData loanAccountGroupData = this.loanReadPlatformService.retrieveGroupAndMembersDetailsTemplate(groupId);
                officeId = loanAccountGroupData.groupOfficeId();
                calendarOptions = this.loanReadPlatformService.retrieveCalendars(groupId);
                newLoanAccount = newLoanAccount == null ? loanAccountGroupData : LoanAccountData.populateGroupDefaults(newLoanAccount,
                        loanAccountGroupData);
                if (productId != null) {
                    Map<Long, Integer> memberLoanCycle = new HashMap<>();
                    Collection<ClientData> members = loanAccountGroupData.groupData().clientMembers();
                    accountLinkingOptions = new ArrayList<>();
                    if(members != null){
                    	for (ClientData clientData : members) {
                            Integer loanCounter = this.loanReadPlatformService.retriveLoanCounter(clientData.id(), productId);
                            memberLoanCycle.put(clientData.id(), loanCounter);
                            accountLinkingOptions.addAll(getaccountLinkingOptions(newLoanAccount, clientData.id(), groupId));
                        }
                    }
                    
                    newLoanAccount = LoanAccountData.associateMemberVariations(newLoanAccount, memberLoanCycle);
                }

            } else {
                final String errorMsg = "Loan template type '" + templateType + "' is not supported";
                throw new NotSupportedLoanTemplateTypeException(errorMsg, templateType);
            }

            allowedLoanOfficers = this.loanReadPlatformService.retrieveAllowedLoanOfficers(officeId, staffInSelectedOfficeOnly);

            if (clientId != null) {
                accountLinkingOptions = getaccountLinkingOptions(newLoanAccount, clientId, groupId);
            }

            // add product options, allowed loan officers and calendar options
            // (calendar options will be null in individual loan)
            newLoanAccount = LoanAccountData.associationsAndTemplate(newLoanAccount, productOptions, allowedLoanOfficers, calendarOptions,
                    accountLinkingOptions);
        }
        final List<DatatableData> datatableTemplates = this.entityDatatableChecksReadService
                .retrieveTemplates(StatusEnum.CREATE.getCode().longValue(), EntityTables.LOAN.getName(), productId);
        newLoanAccount.setDatatables(datatableTemplates);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, newLoanAccount, this.LOAN_DATA_PARAMETERS);
    }

    private Collection<PortfolioAccountData> getaccountLinkingOptions(final LoanAccountData newLoanAccount, final Long clientId,
            final Long groupId) {
        final CurrencyData currencyData = newLoanAccount.currency();
        String currencyCode = null;
        if (currencyData != null) {
            currencyCode = currencyData.code();
        }
        final long[] accountStatus = { SavingsAccountStatusType.ACTIVE.getValue() };
        final PortfolioAccountDTO portfolioAccountDTO = new PortfolioAccountDTO(PortfolioAccountType.SAVINGS.getValue(), clientId, currencyCode,
                accountStatus, DepositAccountType.SAVINGS_DEPOSIT.getValue());
        if (groupId != null) {
            portfolioAccountDTO.setGroupId(groupId);
        }
        return this.portfolioAccountReadPlatformService.retrieveAllForLookup(portfolioAccountDTO);
    }
    
    @GET
    @Path("{loanId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve a Loan", httpMethod = "GET", notes = "Note: template=true parameter doesn't apply to this resource." + "Example Requests:\n" + "\n" + "loans/1\n" + "\n" + "\n" + "loans/1?fields=id,principal,annualInterestRate\n" + "\n" + "\n" + "loans/1?associations=all\n" + "\n" + "loans/1?associations=all&exclude=guarantors\n" + "\n" + "\n" + "loans/1?fields=id,principal,annualInterestRate&associations=repaymentSchedule,transactions")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = LoansApiResourceSwagger.GetLoansLoanIdResponse.class)})
    public String retrieveLoan(@PathParam("loanId") @ApiParam(value = "loanId") final Long loanId,
            @DefaultValue("false") @QueryParam("staffInSelectedOfficeOnly") @ApiParam(value = "staffInSelectedOfficeOnly") final boolean staffInSelectedOfficeOnly,
            @Context final UriInfo uriInfo) {
        long start = System.currentTimeMillis() ;
        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        LoanAccountData loanBasicDetails = this.loanReadPlatformService.retrieveOne(loanId);
        if (loanBasicDetails.isInterestRecalculationEnabled()) {
            Collection<CalendarData> interestRecalculationCalendarDatas = this.calendarReadPlatformService
                    .retrieveCalendarsByEntity(loanBasicDetails.getInterestRecalculationDetailId(),
                            CalendarEntityType.LOAN_RECALCULATION_REST_DETAIL.getValue(), null);
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
            loanBasicDetails = LoanAccountData.withInterestRecalculationCalendarData(loanBasicDetails, calendarData,
                    compoundingCalendarData);
        }
        if (loanBasicDetails.isMonthlyRepaymentFrequencyType()) {
        	Collection<CalendarData> loanCalendarDatas = this.calendarReadPlatformService
                    .retrieveCalendarsByEntity(loanId,
                            CalendarEntityType.LOANS.getValue(), null);
            CalendarData calendarData = null;
            if (!CollectionUtils.isEmpty(loanCalendarDatas)) {
                calendarData = loanCalendarDatas.iterator().next();
            }
            if(calendarData != null)
            	loanBasicDetails = LoanAccountData.withLoanCalendarData(loanBasicDetails, calendarData);
        }
        Collection<InterestRatePeriodData> interestRatesPeriods = this.loanReadPlatformService.retrieveLoanInterestRatePeriodData(loanBasicDetails);
        Collection<LoanTransactionData> loanRepayments = null;
        LoanScheduleData repaymentSchedule = null;
        Collection<LoanChargeData> charges = null;
        Collection<GuarantorData> guarantors = null;
        Collection<CollateralData> collateral = null;
        CalendarData meeting = null;
        Collection<NoteData> notes = null;
        PortfolioAccountData linkedAccount = null;
        Collection<DisbursementData> disbursementData = null;
        Collection<LoanTermVariationsData> emiAmountVariations = null;

        final Set<String> mandatoryResponseParameters = new HashSet<>();
        final Set<String> associationParameters = ApiParameterHelper.extractAssociationsForResponseIfProvided(uriInfo.getQueryParameters());
        if (!associationParameters.isEmpty()) {

            if (associationParameters.contains("all")) {
                associationParameters.addAll(Arrays.asList("repaymentSchedule", "futureSchedule", "originalSchedule", "transactions",
                        "charges", "guarantors", "collateral", "notes", "linkedAccount", "multiDisburseDetails"));
            }

            ApiParameterHelper.excludeAssociationsForResponseIfProvided(uriInfo.getQueryParameters(), associationParameters);

            if (associationParameters.contains("guarantors")) {
                mandatoryResponseParameters.add("guarantors");
                guarantors = this.guarantorReadPlatformService.retrieveGuarantorsForLoan(loanId);
                if (CollectionUtils.isEmpty(guarantors)) {
                    guarantors = null;
                }
            }

            if (associationParameters.contains("transactions")) {
                mandatoryResponseParameters.add("transactions");
                final Collection<LoanTransactionData> currentLoanRepayments = this.loanReadPlatformService.retrieveLoanTransactions(loanId);
                if (!CollectionUtils.isEmpty(currentLoanRepayments)) {
                    loanRepayments = currentLoanRepayments;
                }
            }

            if (associationParameters.contains("multiDisburseDetails") || associationParameters.contains("repaymentSchedule")) {
                mandatoryResponseParameters.add("multiDisburseDetails");
                disbursementData = this.loanReadPlatformService.retrieveLoanDisbursementDetails(loanId);
            }

            if (associationParameters.contains("emiAmountVariations") || associationParameters.contains("repaymentSchedule")) {
                mandatoryResponseParameters.add("emiAmountVariations");
                emiAmountVariations = this.loanReadPlatformService.retrieveLoanTermVariations(loanId,
                        LoanTermVariationType.EMI_AMOUNT.getValue());
            }

            if (associationParameters.contains("repaymentSchedule")) {
                mandatoryResponseParameters.add("repaymentSchedule");
                final RepaymentScheduleRelatedLoanData repaymentScheduleRelatedData = loanBasicDetails.repaymentScheduleRelatedData();
                repaymentSchedule = this.loanReadPlatformService.retrieveRepaymentSchedule(loanId, repaymentScheduleRelatedData,
                        disbursementData, loanBasicDetails.isInterestRecalculationEnabled(), loanBasicDetails.getTotalPaidFeeCharges());

                if (associationParameters.contains("futureSchedule") && loanBasicDetails.isInterestRecalculationEnabled()) {
                    mandatoryResponseParameters.add("futureSchedule");
                    this.calculationPlatformService.updateFutureSchedule(repaymentSchedule, loanId);
                }

                if (associationParameters.contains("originalSchedule") && loanBasicDetails.isInterestRecalculationEnabled()
                        && loanBasicDetails.isActive()) {
                    mandatoryResponseParameters.add("originalSchedule");
                    LoanScheduleData loanScheduleData = this.loanScheduleHistoryReadPlatformService.retrieveRepaymentArchiveSchedule(
                            loanId, repaymentScheduleRelatedData, disbursementData);
                    loanBasicDetails = LoanAccountData.withOriginalSchedule(loanBasicDetails, loanScheduleData);
                }
            }

            if (associationParameters.contains("charges")) {
                mandatoryResponseParameters.add("charges");
                charges = this.loanChargeReadPlatformService.retrieveLoanCharges(loanId);
                if (CollectionUtils.isEmpty(charges)) {
                    charges = null;
                }
            }

            if (associationParameters.contains("collateral")) {
                mandatoryResponseParameters.add("collateral");
                collateral = this.loanCollateralReadPlatformService.retrieveCollaterals(loanId);
                if (CollectionUtils.isEmpty(collateral)) {
                    collateral = null;
                }
            }

            if (associationParameters.contains("meeting")) {
                mandatoryResponseParameters.add("meeting");
                meeting = this.calendarReadPlatformService.retrieveLoanCalendar(loanId);
            }

            if (associationParameters.contains("notes")) {
                mandatoryResponseParameters.add("notes");
                notes = this.noteReadPlatformService.retrieveNotesByResource(loanId, NoteType.LOAN.getValue());
                if (CollectionUtils.isEmpty(notes)) {
                    notes = null;
                }
            }

            if (associationParameters.contains("linkedAccount")) {
                mandatoryResponseParameters.add("linkedAccount");
                linkedAccount = this.accountAssociationsReadPlatformService.retriveLoanLinkedAssociation(loanId);
            }

        }

        Collection<LoanProductData> productOptions = null;
        LoanProductData product = null;
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
        PaidInAdvanceData paidInAdvanceTemplate = null;
        Collection<LoanAccountSummaryData> clientActiveLoanOptions = null;

        final boolean template = ApiParameterHelper.template(uriInfo.getQueryParameters());
        if (template) {
            productOptions = this.loanProductReadPlatformService.retrieveAllLoanProductsForLookup();
            product = this.loanProductReadPlatformService.retrieveLoanProduct(loanBasicDetails.loanProductId());
            loanBasicDetails.setProduct(product);
            loanTermFrequencyTypeOptions = this.dropdownReadPlatformService.retrieveLoanTermFrequencyTypeOptions();
            repaymentFrequencyTypeOptions = this.dropdownReadPlatformService.retrieveRepaymentFrequencyTypeOptions();
            repaymentFrequencyNthDayTypeOptions = this.dropdownReadPlatformService.retrieveRepaymentFrequencyOptionsForNthDayOfMonth();
            repaymentFrequencyDayOfWeekTypeOptions = this.dropdownReadPlatformService.retrieveRepaymentFrequencyOptionsForDaysOfWeek();
            interestRateFrequencyTypeOptions = this.dropdownReadPlatformService.retrieveInterestRateFrequencyTypeOptions();

            amortizationTypeOptions = this.dropdownReadPlatformService.retrieveLoanAmortizationTypeOptions();
            if (product.isLinkedToFloatingInterestRates()) {
                interestTypeOptions = Arrays.asList(interestType(InterestMethod.DECLINING_BALANCE));
            } else {
                interestTypeOptions = this.dropdownReadPlatformService.retrieveLoanInterestTypeOptions();
            }
            interestCalculationPeriodTypeOptions = this.dropdownReadPlatformService.retrieveLoanInterestRateCalculatedInPeriodOptions();

            fundOptions = this.fundReadPlatformService.retrieveAllFunds();
            repaymentStrategyOptions = this.dropdownReadPlatformService.retreiveTransactionProcessingStrategies();
            if (product.getMultiDisburseLoan()) {
                chargeOptions = this.chargeReadPlatformService.retrieveLoanAccountApplicableCharges(loanId,
                        new ChargeTimeType[] { ChargeTimeType.OVERDUE_INSTALLMENT });
            } else {
                chargeOptions = this.chargeReadPlatformService.retrieveLoanAccountApplicableCharges(loanId, new ChargeTimeType[] {
                        ChargeTimeType.OVERDUE_INSTALLMENT, ChargeTimeType.TRANCHE_DISBURSEMENT });
            }
            chargeTemplate = this.loanChargeReadPlatformService.retrieveLoanChargeTemplate();

            allowedLoanOfficers = this.loanReadPlatformService.retrieveAllowedLoanOfficers(loanBasicDetails.officeId(),
                    staffInSelectedOfficeOnly);

            loanPurposeOptions = this.codeValueReadPlatformService.retrieveCodeValuesByCode("LoanPurpose");
            loanCollateralOptions = this.codeValueReadPlatformService.retrieveCodeValuesByCode("LoanCollateral");
            final CurrencyData currencyData = loanBasicDetails.currency();
            String currencyCode = null;
            if (currencyData != null) {
                currencyCode = currencyData.code();
            }
            final long[] accountStatus = { SavingsAccountStatusType.ACTIVE.getValue() };
            PortfolioAccountDTO portfolioAccountDTO = new PortfolioAccountDTO(PortfolioAccountType.SAVINGS.getValue(),
                    loanBasicDetails.clientId(), currencyCode, accountStatus, DepositAccountType.SAVINGS_DEPOSIT.getValue());
            accountLinkingOptions = this.portfolioAccountReadPlatformService.retrieveAllForLookup(portfolioAccountDTO);

            if (!associationParameters.contains("linkedAccount")) {
                mandatoryResponseParameters.add("linkedAccount");
                linkedAccount = this.accountAssociationsReadPlatformService.retriveLoanLinkedAssociation(loanId);
            }
            if (loanBasicDetails.groupId() != null) {
                calendarOptions = this.loanReadPlatformService.retrieveCalendars(loanBasicDetails.groupId());
            }

            if(loanBasicDetails.product().canUseForTopup() && loanBasicDetails.clientId() != null){
                clientActiveLoanOptions = this.accountDetailsReadPlatformService.retrieveClientActiveLoanAccountSummary(loanBasicDetails.clientId());
            }


        }

        Collection<ChargeData> overdueCharges = this.chargeReadPlatformService.retrieveLoanProductCharges(loanBasicDetails.loanProductId(),
                ChargeTimeType.OVERDUE_INSTALLMENT);

        paidInAdvanceTemplate = this.loanReadPlatformService.retrieveTotalPaidInAdvance(loanId);

        final LoanAccountData loanAccount = LoanAccountData.associationsAndTemplate(loanBasicDetails, repaymentSchedule, loanRepayments,
                charges, collateral, guarantors, meeting, productOptions, loanTermFrequencyTypeOptions, repaymentFrequencyTypeOptions,
                repaymentFrequencyNthDayTypeOptions, repaymentFrequencyDayOfWeekTypeOptions, repaymentStrategyOptions, 
                interestRateFrequencyTypeOptions, amortizationTypeOptions, interestTypeOptions, interestCalculationPeriodTypeOptions, 
                fundOptions, chargeOptions, chargeTemplate, allowedLoanOfficers, loanPurposeOptions, loanCollateralOptions, 
                calendarOptions, notes, accountLinkingOptions, linkedAccount, disbursementData, emiAmountVariations,
                overdueCharges, paidInAdvanceTemplate, interestRatesPeriods, clientActiveLoanOptions);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters(),
                mandatoryResponseParameters);
        String toReturn = this.toApiJsonSerializer.serialize(settings, loanAccount, this.LOAN_DATA_PARAMETERS);
        return toReturn ;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "List Loans", httpMethod = "GET", notes = "The list capability of loans can support pagination and sorting.\n" + "Example Requests:\n" + "\n" + "loans\n" + "\n" + "loans?fields=accountNo\n" + "\n" + "loans?offset=10&limit=50\n" + "\n" + "loans?orderBy=accountNo&sortOrder=DESC")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = LoansApiResourceSwagger.GetLoansResponse.class)})
    public String retrieveAll(@Context final UriInfo uriInfo,
            @QueryParam("sqlSearch") @ApiParam(value = "sqlSearch") final String sqlSearch,
            @QueryParam("externalId") @ApiParam(value = "externalId") final String externalId,
            // @QueryParam("underHierarchy") final String hierarchy,
            @QueryParam("offset") @ApiParam(value = "offset") final Integer offset, @QueryParam("limit") @ApiParam(value = "limit") final Integer limit,
            @QueryParam("orderBy") @ApiParam(value = "orderBy") final String orderBy, @QueryParam("sortOrder") @ApiParam(value = "sortOrder")final String sortOrder,
            @QueryParam("accountNo") @ApiParam(value = "accountNo") final String accountNo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final SearchParameters searchParameters = SearchParameters.forLoans(sqlSearch, externalId, offset, limit, orderBy, sortOrder,
                accountNo);

        final Page<LoanAccountData> loanBasicDetails = this.loanReadPlatformService.retrieveAll(searchParameters);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, loanBasicDetails, this.LOAN_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Calculate loan repayment schedule | Submit a new Loan Application", httpMethod = "POST", notes = "It calculates the loan repayment Schedule\n" + "Submits a new loan application\n" + "Mandatory Fields: clientId, productId, principal, loanTermFrequency, loanTermFrequencyType, loanType, numberOfRepayments, repaymentEvery, repaymentFrequencyType, interestRatePerPeriod, amortizationType, interestType, interestCalculationPeriodType, transactionProcessingStrategyId, expectedDisbursementDate, submittedOnDate, loanType\n" + "Optional Fields: graceOnPrincipalPayment, graceOnInterestPayment, graceOnInterestCharged, linkAccountId, allowPartialPeriodInterestCalcualtion, fixedEmiAmount, maxOutstandingLoanBalance, disbursementData, graceOnArrearsAgeing, createStandingInstructionAtDisbursement (requires linkedAccountId if set to true)\n" + "Additional Mandatory Fields if interest recalculation is enabled for product and Rest frequency not same as repayment period: recalculationRestFrequencyDate\n" + "Additional Mandatory Fields if interest recalculation with interest/fee compounding is enabled for product and compounding frequency not same as repayment period: recalculationCompoundingFrequencyDate\n" + "Additional Mandatory Field if Entity-Datatable Check is enabled for the entity of type loan: datatables")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = LoansApiResourceSwagger.PostLoansRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = LoansApiResourceSwagger.PostLoansResponse.class)})
    public String calculateLoanScheduleOrSubmitLoanApplication(@QueryParam("command") @ApiParam(value = "command") final String commandParam,
            @Context final UriInfo uriInfo, @ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        if (is(commandParam, "calculateLoanSchedule")) {

            final JsonElement parsedQuery = this.fromJsonHelper.parse(apiRequestBodyAsJson);
            final JsonQuery query = JsonQuery.from(apiRequestBodyAsJson, parsedQuery, this.fromJsonHelper);

            final LoanScheduleModel loanSchedule = this.calculationPlatformService.calculateLoanSchedule(query, true);

            final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
            return this.loanScheduleToApiJsonSerializer.serialize(settings, loanSchedule.toData(), new HashSet<String>());
        }

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createLoanApplication().withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{loanId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Modify a loan application", httpMethod = "PUT", notes = "Loan application can only be modified when in 'Submitted and pending approval' state. Once the application is approved, the details cannot be changed using this method.")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = LoansApiResourceSwagger.PutLoansLoanIdRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = LoansApiResourceSwagger.PutLoansLoanIdResponse.class)})
    public String modifyLoanApplication(@PathParam("loanId") @ApiParam(value = "loanId") final Long loanId, @ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateLoanApplication(loanId).withJson(apiRequestBodyAsJson)
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{loanId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Delete a Loan Application", httpMethod = "DELETE", notes = "Note: Only loans in \"Submitted and awaiting approval\" status can be deleted.")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = LoansApiResourceSwagger.DeleteLoansLoanIdResponse.class)})
    public String deleteLoanApplication(@PathParam("loanId") @ApiParam(value = "loanId") final Long loanId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteLoanApplication(loanId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("{loanId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Approve Loan Application | Recover Loan Guarantee | Undo Loan Application Approval | Assign a Loan Officer | Unassign a Loan Officer | Reject Loan Application | Applicant Withdraws from Loan Application | Disburse Loan Disburse Loan To Savings Account | Undo Loan Disbursal", httpMethod = "POST", notes = "Approve Loan Application:\n" + "Mandatory Fields: approvedOnDate\n" + "Optional Fields: approvedLoanAmount and expectedDisbursementDate\n" + "Approves the loan application\n\n" + "Recover Loan Guarantee:\n" + "Recovers the loan guarantee\n\n" + "Undo Loan Application Approval:\n" + "Undoes the Loan Application Approval\n\n" + "Assign a Loan Officer:\n" + "Allows you to assign Loan Officer for existing Loan.\n\n" + "Unassign a Loan Officer:\n" + "Allows you to unassign the Loan Officer.\n\n" + "Reject Loan Application:\n" + "Mandatory Fields: rejectedOnDate\n" + "Allows you to reject the loan application\n\n" + "Applicant Withdraws from Loan Application:\n" + "Mandatory Fields: withdrawnOnDate\n" + "Allows the applicant to withdraw the loan application\n\n" + "Disburse Loan:\n" + "Mandatory Fields: actualDisbursementDate\n" + "Optional Fields: transactionAmount and fixedEmiAmount\n" + "Disburses the Loan\n\n" + "Disburse Loan To Savings Account:\n" + "Mandatory Fields: actualDisbursementDate\n" + "Optional Fields: transactionAmount and fixedEmiAmount\n" + "Disburses the loan to Saving Account\n\n" + "Undo Loan Disbursal:\n" + "Undoes the Loan Disbursal\n" + "Showing request and response for Assign a Loan Officer")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = LoansApiResourceSwagger.PostLoansLoanIdRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = LoansApiResourceSwagger.PostLoansLoanIdResponse.class)})
    public String stateTransitions(@PathParam("loanId") @ApiParam(value = "loanId") final Long loanId, @QueryParam("command") @ApiParam(value = "command") final String commandParam,
           @ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);

        CommandProcessingResult result = null;

        if (is(commandParam, "reject")) {
            final CommandWrapper commandRequest = builder.rejectLoanApplication(loanId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "withdrawnByApplicant")) {
            final CommandWrapper commandRequest = builder.withdrawLoanApplication(loanId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "approve")) {
            final CommandWrapper commandRequest = builder.approveLoanApplication(loanId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "disburse")) {
            final CommandWrapper commandRequest = builder.disburseLoanApplication(loanId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "disburseToSavings")) {
            final CommandWrapper commandRequest = builder.disburseLoanToSavingsApplication(loanId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        }

        if (is(commandParam, "undoapproval")) {
            final CommandWrapper commandRequest = builder.undoLoanApplicationApproval(loanId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "undodisbursal")) {
            final CommandWrapper commandRequest = builder.undoLoanApplicationDisbursal(loanId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        }else if (is(commandParam, "undolastdisbursal")) {
            final CommandWrapper commandRequest = builder.undoLastDisbursalLoanApplication(loanId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        }

        if (is(commandParam, "assignloanofficer")) {
            final CommandWrapper commandRequest = builder.assignLoanOfficer(loanId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "unassignloanofficer")) {
            final CommandWrapper commandRequest = builder.unassignLoanOfficer(loanId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "recoverGuarantees")) {
            final CommandWrapper commandRequest = new CommandWrapperBuilder().recoverFromGuarantor(loanId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        }

        if (result == null) { throw new UnrecognizedQueryParamException("command", commandParam); }

        return this.toApiJsonSerializer.serialize(result);
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

    @GET
    @Path("downloadtemplate")
    @Produces("application/vnd.ms-excel")
    public Response getLoansTemplate(@QueryParam("officeId") final Long officeId,
            @QueryParam("staffId") final Long staffId,@QueryParam("dateFormat") final String dateFormat) {
        return bulkImportWorkbookPopulatorService.getTemplate(GlobalEntityType.LOANS.toString(), officeId, staffId,dateFormat);
    }

    @GET
    @Path("repayments/downloadtemplate")
    @Produces("application/vnd.ms-excel")
    public Response getLoanRepaymentTemplate(@QueryParam("officeId") final Long officeId,
            @QueryParam("dateFormat") final String dateFormat) {
        return bulkImportWorkbookPopulatorService.getTemplate(GlobalEntityType.LOAN_TRANSACTIONS.toString(), officeId, null,dateFormat);
    }

    @POST
    @Path("uploadtemplate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String postLoanTemplate(@FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail,
            @FormDataParam("locale") final String locale, @FormDataParam("dateFormat") final String dateFormat){
        final Long importDocumentId = this. bulkImportWorkbookService.importWorkbook(GlobalEntityType.LOANS.toString(), uploadedInputStream,fileDetail,locale,dateFormat);
        return this.toApiJsonSerializer.serialize(importDocumentId);
    }

    @POST
    @Path("repayments/uploadtemplate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String postLoanRepaymentTemplate(@FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail,@FormDataParam("locale") final String locale,
            @FormDataParam("dateFormat") final String dateFormat){
        final Long importDocumentId = this.bulkImportWorkbookService.importWorkbook(GlobalEntityType.LOAN_TRANSACTIONS.toString(), uploadedInputStream,fileDetail,
                locale,dateFormat);
        return this.toApiJsonSerializer.serialize(importDocumentId);
    }
}