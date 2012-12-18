package org.mifosplatform.portfolio.loanaccount.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiParameterHelper;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.api.JsonQuery;
import org.mifosplatform.infrastructure.core.api.PortfolioApiDataConversionService;
import org.mifosplatform.infrastructure.core.api.PortfolioApiJsonSerializerService;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.organisation.office.data.OfficeLookup;
import org.mifosplatform.organisation.office.service.OfficeReadPlatformService;
import org.mifosplatform.organisation.staff.command.BulkTransferLoanOfficerCommand;
import org.mifosplatform.organisation.staff.data.BulkTransferLoanOfficerData;
import org.mifosplatform.organisation.staff.data.StaffAccountSummaryCollectionData;
import org.mifosplatform.organisation.staff.data.StaffData;
import org.mifosplatform.organisation.staff.service.StaffReadPlatformService;
import org.mifosplatform.portfolio.charge.data.ChargeData;
import org.mifosplatform.portfolio.charge.service.ChargeReadPlatformService;
import org.mifosplatform.portfolio.fund.data.FundData;
import org.mifosplatform.portfolio.fund.service.FundReadPlatformService;
import org.mifosplatform.portfolio.loanaccount.command.AdjustLoanTransactionCommand;
import org.mifosplatform.portfolio.loanaccount.command.LoanChargeCommand;
import org.mifosplatform.portfolio.loanaccount.command.LoanTransactionCommand;
import org.mifosplatform.portfolio.loanaccount.data.DisbursementData;
import org.mifosplatform.portfolio.loanaccount.data.LoanAccountData;
import org.mifosplatform.portfolio.loanaccount.data.LoanBasicDetailsData;
import org.mifosplatform.portfolio.loanaccount.data.LoanChargeData;
import org.mifosplatform.portfolio.loanaccount.data.LoanPermissionData;
import org.mifosplatform.portfolio.loanaccount.data.LoanTransactionData;
import org.mifosplatform.portfolio.loanaccount.gaurantor.data.GuarantorData;
import org.mifosplatform.portfolio.loanaccount.gaurantor.service.GuarantorReadPlatformService;
import org.mifosplatform.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.mifosplatform.portfolio.loanaccount.loanschedule.service.LoanScheduleCalculationPlatformService;
import org.mifosplatform.portfolio.loanaccount.service.LoanReadPlatformService;
import org.mifosplatform.portfolio.loanaccount.service.LoanWritePlatformService;
import org.mifosplatform.portfolio.loanproduct.data.LoanProductData;
import org.mifosplatform.portfolio.loanproduct.data.TransactionProcessingStrategyData;
import org.mifosplatform.portfolio.loanproduct.service.LoanDropdownReadPlatformService;
import org.mifosplatform.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.google.gson.JsonElement;

@Path("/loans")
@Component
@Scope("singleton")
public class LoansApiResource {

    private final Set<String> LOAN_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id", "externalId", "clientId", "groupId",
            "clientName", "groupName", "fundId", "fundName", "loanProductId", "loanProductName", "loanProductDescription", "currency",
            "principal", "inArrearsTolerance", "numberOfRepayments", "repaymentEvery", "interestRatePerPeriod", "annualInterestRate",
            "repaymentFrequencyType", "interestRateFrequencyType", "amortizationType", "interestType", "interestCalculationPeriodType",
            "submittedOnDate", "approvedOnDate", "expectedDisbursementDate", "actualDisbursementDate", "expectedFirstRepaymentOnDate",
            "interestChargedFromDate", "closedOnDate", "expectedMaturityDate", "status", "lifeCycleStatusDate", "repaymentSchedule",
            "transactions", "permissions", "convenienceData", "charges", "productOptions", "amortizationTypeOptions",
            "interestTypeOptions", "interestCalculationPeriodTypeOptions", "repaymentFrequencyTypeOptions",
            "interestRateFrequencyTypeOptions", "fundOptions", "repaymentStrategyOptions", "chargeOptions", "loanOfficerId",
            "loanOfficerName", "loanOfficerOptions", "chargeTemplate"));

    private final String resourceNameForPermissions = "LOAN";

    private final PlatformSecurityContext context;
    private final LoanReadPlatformService loanReadPlatformService;
    private final LoanWritePlatformService loanWritePlatformService;
    private final LoanProductReadPlatformService loanProductReadPlatformService;
    private final LoanDropdownReadPlatformService dropdownReadPlatformService;
    private final FundReadPlatformService fundReadPlatformService;
    private final ChargeReadPlatformService chargeReadPlatformService;
    private final LoanScheduleCalculationPlatformService calculationPlatformService;
    private final StaffReadPlatformService staffReadPlatformService;
    private final OfficeReadPlatformService officeReadPlatformService;
    private final GuarantorReadPlatformService guarantorReadPlatformService;
    private final PortfolioApiJsonSerializerService apiJsonSerializerService;
    private final PortfolioApiDataConversionService apiDataConversionService;
    private final DefaultToApiJsonSerializer<LoanAccountData> toApiJsonSerializer;
    private final DefaultToApiJsonSerializer<LoanScheduleData> loanScheduleToApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final FromJsonHelper fromJsonHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public LoansApiResource(final PlatformSecurityContext context, final LoanReadPlatformService loanReadPlatformService,
            final LoanWritePlatformService loanWritePlatformService, final LoanProductReadPlatformService loanProductReadPlatformService,
            final LoanDropdownReadPlatformService dropdownReadPlatformService, final FundReadPlatformService fundReadPlatformService,
            final ChargeReadPlatformService chargeReadPlatformService,
            final LoanScheduleCalculationPlatformService calculationPlatformService,
            final StaffReadPlatformService staffReadPlatformService, final OfficeReadPlatformService officeReadPlatformService,
            final GuarantorReadPlatformService guarantorReadPlatformService,
            final DefaultToApiJsonSerializer<LoanAccountData> toApiJsonSerializer,
            final DefaultToApiJsonSerializer<LoanScheduleData> loanScheduleToApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper, final PortfolioApiJsonSerializerService apiJsonSerializerService,
            final PortfolioApiDataConversionService apiDataConversionService, final FromJsonHelper fromJsonHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.context = context;
        this.loanReadPlatformService = loanReadPlatformService;
        this.loanWritePlatformService = loanWritePlatformService;
        this.loanProductReadPlatformService = loanProductReadPlatformService;
        this.dropdownReadPlatformService = dropdownReadPlatformService;
        this.fundReadPlatformService = fundReadPlatformService;
        this.chargeReadPlatformService = chargeReadPlatformService;
        this.calculationPlatformService = calculationPlatformService;
        this.staffReadPlatformService = staffReadPlatformService;
        this.officeReadPlatformService = officeReadPlatformService;
        this.guarantorReadPlatformService = guarantorReadPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.loanScheduleToApiJsonSerializer = loanScheduleToApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.apiJsonSerializerService = apiJsonSerializerService;
        this.apiDataConversionService = apiDataConversionService;
        this.fromJsonHelper = fromJsonHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveDetailsForNewLoanApplicationStepOne(@QueryParam("clientId") final Long clientId,
            @QueryParam("groupId") final Long groupId, @QueryParam("productId") final Long productId, @Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);

        // tempate related
        Collection<LoanProductData> productOptions = this.loanProductReadPlatformService.retrieveAllLoanProductsForLookup();
        Collection<EnumOptionData> loanTermFrequencyTypeOptions = dropdownReadPlatformService.retrieveLoanTermFrequencyTypeOptions();
        Collection<EnumOptionData> repaymentFrequencyTypeOptions = dropdownReadPlatformService.retrieveRepaymentFrequencyTypeOptions();
        Collection<EnumOptionData> interestRateFrequencyTypeOptions = dropdownReadPlatformService
                .retrieveInterestRateFrequencyTypeOptions();

        Collection<EnumOptionData> amortizationTypeOptions = dropdownReadPlatformService.retrieveLoanAmortizationTypeOptions();
        Collection<EnumOptionData> interestTypeOptions = dropdownReadPlatformService.retrieveLoanInterestTypeOptions();
        Collection<EnumOptionData> interestCalculationPeriodTypeOptions = dropdownReadPlatformService
                .retrieveLoanInterestRateCalculatedInPeriodOptions();

        Collection<FundData> fundOptions = this.fundReadPlatformService.retrieveAllFunds();
        Collection<TransactionProcessingStrategyData> repaymentStrategyOptions = this.dropdownReadPlatformService
                .retreiveTransactionProcessingStrategies();

        final boolean feeChargesOnly = false;
        Collection<ChargeData> chargeOptions = this.chargeReadPlatformService.retrieveLoanApplicableCharges(feeChargesOnly);
        ChargeData chargeTemplate = this.chargeReadPlatformService.retrieveLoanChargeTemplate();

        LoanBasicDetailsData loanBasicDetails;
        Long officeId;

        if (clientId != null) {
            loanBasicDetails = this.loanReadPlatformService.retrieveClientAndProductDetails(clientId, productId);
            officeId = loanBasicDetails.getClientOfficeId();
        } else {
            loanBasicDetails = this.loanReadPlatformService.retrieveGroupAndProductDetails(groupId, productId);
            officeId = loanBasicDetails.getGroupOfficeId();
        }

        final boolean convenienceDataRequired = false;
        Collection<LoanChargeData> charges = loanBasicDetails.getCharges();

        Collection<StaffData> allowedLoanOfficers = this.staffReadPlatformService.retrieveAllLoanOfficersByOffice(officeId);

        final LoanAccountData newLoanAccount = new LoanAccountData(loanBasicDetails, convenienceDataRequired, null, null, null, charges,
                productOptions, loanTermFrequencyTypeOptions, repaymentFrequencyTypeOptions, repaymentStrategyOptions,
                interestRateFrequencyTypeOptions, amortizationTypeOptions, interestTypeOptions, interestCalculationPeriodTypeOptions,
                fundOptions, chargeOptions, chargeTemplate, allowedLoanOfficers, null);

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, newLoanAccount, LOAN_DATA_PARAMETERS);
    }

    @GET
    @Path("{loanId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveLoanAccountDetails(@PathParam("loanId") final Long loanId, @Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);

        final LoanBasicDetailsData loanBasicDetails = this.loanReadPlatformService.retrieveLoanAccountDetails(loanId);

        int loanRepaymentsCount = 0;
        Collection<LoanTransactionData> loanRepayments = null;
        LoanScheduleData repaymentSchedule = null;
        LoanPermissionData permissions = null;
        Collection<LoanChargeData> charges = null;
        GuarantorData guarantorData = null;

        boolean convenienceDataRequired = false;
        final Set<String> associationParameters = ApiParameterHelper.extractAssociationsForResponseIfProvided(uriInfo.getQueryParameters());
        if (!associationParameters.isEmpty()) {

            if (associationParameters.contains("all")) {
                associationParameters.addAll(Arrays.asList("repaymentSchedule", "transactions", "permissions", "convenienceData",
                        "charges", "guarantor"));
            }

            boolean existsGuarantor = false;
            if (associationParameters.contains("guarantor")) {
                if (guarantorReadPlatformService.existsGuarantor(loanId)) {
                    guarantorData = this.guarantorReadPlatformService.retrieveGuarantor(loanId);
                    existsGuarantor = true;
                }
            }

            if (associationParameters.contains("transactions")) {
                final Collection<LoanTransactionData> currentLoanRepayments = this.loanReadPlatformService.retrieveLoanTransactions(loanId);
                if (!CollectionUtils.isEmpty(currentLoanRepayments)) {
                    loanRepayments = currentLoanRepayments;
                }
            }

            if (associationParameters.contains("repaymentSchedule")) {

                DisbursementData singleDisbursement = loanBasicDetails.toDisburementData();
                repaymentSchedule = this.loanReadPlatformService.retrieveRepaymentSchedule(loanId, loanBasicDetails.getCurrency(),
                        singleDisbursement, loanBasicDetails.getTotalDisbursementCharges(), loanBasicDetails.getInArrearsTolerance());

                // FIXME - KW - Waive feature was changed to waive interest at
                // anytime so this permission checking is probably not needed -
                // look into.
                final MonetaryCurrency currency = new MonetaryCurrency(loanBasicDetails.getCurrency().code(), loanBasicDetails
                        .getCurrency().decimalPlaces());
                final Money tolerance = Money.of(currency, loanBasicDetails.getInArrearsTolerance());
                final Money totalOutstandingMoney = Money.of(currency, repaymentSchedule.totalOutstanding());

                boolean isWaiveAllowed = totalOutstandingMoney.isGreaterThanZero()
                        && (tolerance.isGreaterThan(totalOutstandingMoney) || tolerance.isEqualTo(totalOutstandingMoney));

                if (associationParameters.contains("permissions")) {
                    loanRepaymentsCount = retrieveNonDisbursementTransactions(loanRepayments);
                    permissions = this.loanReadPlatformService.retrieveLoanPermissions(loanBasicDetails, isWaiveAllowed,
                            loanRepaymentsCount, existsGuarantor);
                }
                convenienceDataRequired = true;
            }

            if (associationParameters.contains("charges")) {
                charges = this.chargeReadPlatformService.retrieveLoanCharges(loanId);
                if (CollectionUtils.isEmpty(charges)) {
                    charges = null; // set back to null so doesnt appear in JSON
                                    // is no charges exist.
                }
            }
        }

        Collection<LoanProductData> productOptions = null;
        Collection<EnumOptionData> loanTermFrequencyTypeOptions = null;
        Collection<EnumOptionData> repaymentFrequencyTypeOptions = null;
        Collection<TransactionProcessingStrategyData> repaymentStrategyOptions = null;
        Collection<EnumOptionData> interestRateFrequencyTypeOptions = null;
        Collection<EnumOptionData> amortizationTypeOptions = null;
        Collection<EnumOptionData> interestTypeOptions = null;
        Collection<EnumOptionData> interestCalculationPeriodTypeOptions = null;
        Collection<FundData> fundOptions = null;
        Collection<ChargeData> chargeOptions = null;
        ChargeData chargeTemplate = null;

        final boolean template = ApiParameterHelper.template(uriInfo.getQueryParameters());
        if (template) {
            productOptions = this.loanProductReadPlatformService.retrieveAllLoanProductsForLookup();
            loanTermFrequencyTypeOptions = dropdownReadPlatformService.retrieveLoanTermFrequencyTypeOptions();
            repaymentFrequencyTypeOptions = dropdownReadPlatformService.retrieveRepaymentFrequencyTypeOptions();
            interestRateFrequencyTypeOptions = dropdownReadPlatformService.retrieveInterestRateFrequencyTypeOptions();

            amortizationTypeOptions = dropdownReadPlatformService.retrieveLoanAmortizationTypeOptions();
            interestTypeOptions = dropdownReadPlatformService.retrieveLoanInterestTypeOptions();
            interestCalculationPeriodTypeOptions = dropdownReadPlatformService.retrieveLoanInterestRateCalculatedInPeriodOptions();

            fundOptions = this.fundReadPlatformService.retrieveAllFunds();
            repaymentStrategyOptions = this.dropdownReadPlatformService.retreiveTransactionProcessingStrategies();
            final boolean feeChargesOnly = false;
            chargeOptions = this.chargeReadPlatformService.retrieveLoanApplicableCharges(feeChargesOnly);
            chargeTemplate = this.chargeReadPlatformService.retrieveLoanChargeTemplate();
        }

        final LoanAccountData loanAccount = new LoanAccountData(loanBasicDetails, convenienceDataRequired, repaymentSchedule,
                loanRepayments, permissions, charges, productOptions, loanTermFrequencyTypeOptions, repaymentFrequencyTypeOptions,
                repaymentStrategyOptions, interestRateFrequencyTypeOptions, amortizationTypeOptions, interestTypeOptions,
                interestCalculationPeriodTypeOptions, fundOptions, chargeOptions, chargeTemplate, null, guarantorData);

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, loanAccount, LOAN_DATA_PARAMETERS);
    }

    private int retrieveNonDisbursementTransactions(final Collection<LoanTransactionData> loanRepayments) {
        int loanRepaymentsCount = 0;
        if (!CollectionUtils.isEmpty(loanRepayments)) {
            for (LoanTransactionData transaction : loanRepayments) {
                if (transaction.isNotDisbursement()) {
                    // use this to decide if undo disbural should permission
                    // should be set to true.
                    loanRepaymentsCount++;
                }
            }
        }
        return loanRepaymentsCount;
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String calculateLoanScheduleOrSubmitLoanApplication(@QueryParam("command") final String commandParam,
            @Context final UriInfo uriInfo, final String apiRequestBodyAsJson) {

        if (is(commandParam, "calculateLoanSchedule")) {

            final JsonElement parsedQuery = this.fromJsonHelper.parse(apiRequestBodyAsJson);
            final JsonQuery query = JsonQuery.from(apiRequestBodyAsJson, parsedQuery, this.fromJsonHelper);

            final LoanScheduleData loanSchedule = this.calculationPlatformService.calculateLoanSchedule(query);

            final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
            return this.loanScheduleToApiJsonSerializer.serialize(settings, loanSchedule, new HashSet<String>());
        }

        final EntityIdentifier result = this.commandsSourceWritePlatformService.logCommandSource("CREATE", "LOAN", "CREATE", "loans", null,
                apiRequestBodyAsJson);

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{loanId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String modifyLoanApplication(@PathParam("loanId") final Long loanId, final String apiRequestBodyAsJson) {

        final EntityIdentifier result = this.commandsSourceWritePlatformService.logCommandSource("UPDATE", "LOAN", "UPDATE", "loans",
                loanId, apiRequestBodyAsJson);

        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{loanId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteLoanApplication(@PathParam("loanId") final Long loanId) {

        final EntityIdentifier result = this.commandsSourceWritePlatformService.logCommandSource("DELETE", "LOAN", "DELETE", "loans",
                loanId, "{}");

        return this.toApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("{loanId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String stateTransitions(@PathParam("loanId") final Long loanId, @QueryParam("command") final String commandParam,
            final String apiRequestBodyAsJson) {

        EntityIdentifier result = null;

        if (is(commandParam, "reject")) {
            result = this.commandsSourceWritePlatformService.logCommandSource("REJECT", "LOAN", "N/A", "loans", loanId,
                    apiRequestBodyAsJson);
        } else if (is(commandParam, "withdrewbyclient")) {
            result = this.commandsSourceWritePlatformService.logCommandSource("WITHDRAW", "LOAN", "N/A", "loans", loanId,
                    apiRequestBodyAsJson);
        } else if (is(commandParam, "approve")) {
            result = this.commandsSourceWritePlatformService.logCommandSource("APPROVE", "LOAN", "N/A", "loans", loanId,
                    apiRequestBodyAsJson);
        } else if (is(commandParam, "disburse")) {
            result = this.commandsSourceWritePlatformService.logCommandSource("DISBURSE", "LOAN", "N/A", "loans", loanId,
                    apiRequestBodyAsJson);
        }

        if (is(commandParam, "undoapproval")) {
            result = this.commandsSourceWritePlatformService.logCommandSource("APPROVALUNDO", "LOAN", "N/A", "loans", loanId,
                    apiRequestBodyAsJson);
        } else if (is(commandParam, "undodisbursal")) {
            result = this.commandsSourceWritePlatformService.logCommandSource("DISBURSALUNDO", "LOAN", "N/A", "loans", loanId,
                    apiRequestBodyAsJson);
        }

        if (result == null) { throw new UnrecognizedQueryParamException("command", commandParam); }

        return this.toApiJsonSerializer.serialize(result);
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

    @POST
    @Path("{loanId}/transactions")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Response executeLoanTransaction(@PathParam("loanId") final Long loanId, @QueryParam("command") final String commandParam,
            final String jsonRequestBody) {

        final LoanTransactionCommand command = this.apiDataConversionService.convertJsonToLoanTransactionCommand(loanId, jsonRequestBody);

        EntityIdentifier identifier = null;
        if (is(commandParam, "repayment")) {
            identifier = this.loanWritePlatformService.makeLoanRepayment(command);
        } else if (is(commandParam, "waiveinterest")) {
            identifier = this.loanWritePlatformService.waiveInterestOnLoan(command);
        } else if (is(commandParam, "writeoff")) {
            identifier = this.loanWritePlatformService.writeOff(command);
        } else if (is(commandParam, "close-rescheduled")) {
            identifier = this.loanWritePlatformService.closeAsRescheduled(command);
        } else if (is(commandParam, "close")) {
            identifier = this.loanWritePlatformService.closeLoan(command);
        }

        if (identifier == null) { throw new UnrecognizedQueryParamException("command", commandParam); }

        return Response.ok().entity(identifier).build();
    }

    @GET
    @Path("{loanId}/transactions/template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveNewRepaymentDetails(@PathParam("loanId") final Long loanId, @QueryParam("command") final String commandParam,
            @Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission("LOAN");

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        LoanTransactionData transactionData = null;
        if (is(commandParam, "repayment")) {
            transactionData = this.loanReadPlatformService.retrieveNewLoanRepaymentDetails(loanId);
        } else if (is(commandParam, "waiveinterest")) {
            transactionData = this.loanReadPlatformService.retrieveNewLoanWaiveInterestDetails(loanId);
        } else if (is(commandParam, "writeoff")) {
            transactionData = this.loanReadPlatformService.retrieveNewClosureDetails();
        } else if (is(commandParam, "close-rescheduled")) {
            transactionData = this.loanReadPlatformService.retrieveNewClosureDetails();
        } else if (is(commandParam, "close")) {
            transactionData = this.loanReadPlatformService.retrieveNewClosureDetails();
        } else {
            throw new UnrecognizedQueryParamException("command", commandParam);
        }

        return this.apiJsonSerializerService.serializeLoanTransactionDataToJson(prettyPrint, responseParameters, transactionData);
    }

    @GET
    @Path("{loanId}/transactions/{transactionId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveTransaction(@PathParam("loanId") final Long loanId, @PathParam("transactionId") final Long transactionId,
            @Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission("LOAN");

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        final LoanTransactionData transactionData = this.loanReadPlatformService.retrieveLoanTransactionDetails(loanId, transactionId);

        return this.apiJsonSerializerService.serializeLoanTransactionDataToJson(prettyPrint, responseParameters, transactionData);
    }

    @POST
    @Path("{loanId}/transactions/{transactionId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Response adjustLoanTransaction(@PathParam("loanId") final Long loanId, @PathParam("transactionId") final Long transactionId,
            final String jsonRequestBody) {

        final AdjustLoanTransactionCommand command = this.apiDataConversionService.convertJsonToAdjustLoanTransactionCommand(loanId,
                transactionId, jsonRequestBody);

        EntityIdentifier identifier = this.loanWritePlatformService.adjustLoanTransaction(command);

        return Response.ok().entity(identifier).build();
    }

    @POST
    @Path("{loanId}/charges")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Response addLoanCharge(@PathParam("loanId") final Long loanId, final String jsonRequestBody) {

        final LoanChargeCommand command = this.apiDataConversionService.convertJsonToLoanChargeCommand(null, loanId, jsonRequestBody);

        final EntityIdentifier identifier = this.loanWritePlatformService.addLoanCharge(command);

        return Response.ok().entity(identifier).build();
    }

    @GET
    @Path("{loanId}/charges/template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveNewLoanChargeDetails(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission("LOAN");

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        final boolean feeChargesOnly = false;
        final Collection<ChargeData> chargeOptions = this.chargeReadPlatformService.retrieveLoanApplicableCharges(feeChargesOnly);
        final LoanChargeData loanChargeTemplate = LoanChargeData.template(chargeOptions);

        return this.apiJsonSerializerService.serializeLoanChargeDataToJson(prettyPrint, responseParameters, loanChargeTemplate);
    }

    @GET
    @Path("{loanId}/charges/{chargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveLoanCharge(@PathParam("loanId") final Long loanId, @PathParam("chargeId") final Long loanChargeId,
            @Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission("LOAN");

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        final LoanChargeData loanCharge = this.chargeReadPlatformService.retrieveLoanChargeDetails(loanChargeId, loanId);

        return this.apiJsonSerializerService.serializeLoanChargeDataToJson(prettyPrint, responseParameters, loanCharge);
    }

    @PUT
    @Path("{loanId}/charges/{chargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateLoanCharge(@PathParam("loanId") final Long loanId, @PathParam("chargeId") final Long loanChargeId,
            final String jsonRequestBody) {

        final LoanChargeCommand command = this.apiDataConversionService.convertJsonToLoanChargeCommand(loanChargeId, loanId,
                jsonRequestBody);

        final EntityIdentifier identifier = this.loanWritePlatformService.updateLoanCharge(command);

        return this.toApiJsonSerializer.serialize(identifier);
    }

    @POST
    @Path("{loanId}/charges/{chargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String waiveLoanCharge(@PathParam("loanId") final Long loanId, @PathParam("chargeId") final Long loanChargeId,
            @QueryParam("command") final String commandParam) {

        final LoanChargeCommand command = LoanChargeCommand.forWaiver(loanChargeId, loanId);

        String json = "";
        if (is(commandParam, "waive")) {
            final EntityIdentifier identifier = this.loanWritePlatformService.waiveLoanCharge(command);
            json = this.toApiJsonSerializer.serialize(identifier);
        } else {
            throw new UnrecognizedQueryParamException("command", commandParam);
        }

        return json;
    }

    @DELETE
    @Path("{loanId}/charges/{chargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteLoanCharge(@PathParam("loanId") final Long loanId, @PathParam("chargeId") final Long loanChargeId) {

        final EntityIdentifier identifier = this.loanWritePlatformService.deleteLoanCharge(loanId, loanChargeId);
        return this.toApiJsonSerializer.serialize(identifier);
    }

    @POST
    @Path("{loanId}/assign")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Response assignLoanOfficer(@PathParam("loanId") final Long loanId, final String jsonRequestBody) {

        final BulkTransferLoanOfficerCommand command = this.apiDataConversionService.convertJsonToLoanReassignmentCommand(loanId,
                jsonRequestBody);

        EntityIdentifier identifier = this.loanWritePlatformService.loanReassignment(command);

        return Response.ok().entity(identifier).build();
    }

    @GET
    @Path("{loanId}/assign/template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String assignLoanOfficerTemplate(@PathParam("loanId") final Long loanId, @Context final UriInfo uriInfo) {

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());

        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        final LoanBasicDetailsData loanBasicDetails = this.loanReadPlatformService.retrieveLoanAccountDetails(loanId);

        final Collection<StaffData> allowedLoanOfficers = this.staffReadPlatformService.retrieveAllLoanOfficersByOffice(loanBasicDetails
                .getOfficeId());
        final Long fromLoanOfficerId = loanBasicDetails.getLoanOfficerId();

        final BulkTransferLoanOfficerData loanReassignmentData = BulkTransferLoanOfficerData.template(fromLoanOfficerId,
                allowedLoanOfficers, new LocalDate());

        return this.apiJsonSerializerService.serializeLoanReassignmentDataToJson(prettyPrint, responseParameters, loanReassignmentData);
    }

    @POST
    @Path("loanreassignment")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Response loanReassignment(final String apiRequestBodyAsJson) {

        // final List<String> allowedPermissions =
        // Arrays.asList("ALL_FUNCTIONS", "PORTFOLIO_MANAGEMENT_SUPER_USER",
        // "BULKREASSIGN_LOAN");
        // context.authenticatedUser().validateHasPermissionTo("BULKREASSIGN_LOAN",
        // allowedPermissions);
        //
        // final EntityIdentifier result =
        // this.commandsSourceWritePlatformService.logCommandSource("BULKREASSIGN_LOAN",
        // "staff", null,
        // apiRequestBodyAsJson);
        //
        // return
        // this.apiJsonSerializerService.serializeEntityIdentifier(result);

        final BulkTransferLoanOfficerCommand command = this.apiDataConversionService
                .convertJsonToBulkLoanReassignmentCommand(apiRequestBodyAsJson);

        final EntityIdentifier loanOfficerIdentifier = this.loanWritePlatformService.bulkLoanReassignment(command);

        return Response.ok().entity(loanOfficerIdentifier).build();
    }

    @GET
    @Path("loanreassignment/template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String loanReassignmentTemplate(@QueryParam("officeId") final Long officeId,
            @QueryParam("fromLoanOfficerId") final Long loanOfficerId, @Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission("LOAN");

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        final Collection<OfficeLookup> offices = this.officeReadPlatformService.retrieveAllOfficesForLookup();

        Collection<StaffData> loanOfficers = null;
        StaffAccountSummaryCollectionData staffAccountSummaryCollectionData = null;

        if (officeId != null) {
            loanOfficers = this.staffReadPlatformService.retrieveAllLoanOfficersByOffice(officeId);
        }

        if (loanOfficerId != null) {
            staffAccountSummaryCollectionData = this.staffReadPlatformService.retrieveLoanOfficerAccountSummary(loanOfficerId);
        }

        final BulkTransferLoanOfficerData loanReassignmentData = BulkTransferLoanOfficerData.templateForBulk(officeId, loanOfficerId,
                new LocalDate(), offices, loanOfficers, staffAccountSummaryCollectionData);

        return this.apiJsonSerializerService.serializeLoanReassignmentDataToJson(prettyPrint, responseParameters, loanReassignmentData);
    }
}