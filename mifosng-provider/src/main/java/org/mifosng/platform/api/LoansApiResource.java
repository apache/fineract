package org.mifosng.platform.api;

import java.util.ArrayList;
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
import org.mifosng.platform.api.commands.AdjustLoanTransactionCommand;
import org.mifosng.platform.api.commands.CalculateLoanScheduleCommand;
import org.mifosng.platform.api.commands.LoanApplicationCommand;
import org.mifosng.platform.api.commands.LoanStateTransitionCommand;
import org.mifosng.platform.api.commands.LoanTransactionCommand;
import org.mifosng.platform.api.commands.UndoStateTransitionCommand;
import org.mifosng.platform.api.data.ChargeData;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.api.data.EnumOptionData;
import org.mifosng.platform.api.data.FundData;
import org.mifosng.platform.api.data.LoanAccountData;
import org.mifosng.platform.api.data.LoanAccountSummaryData;
import org.mifosng.platform.api.data.LoanBasicDetailsData;
import org.mifosng.platform.api.data.LoanPermissionData;
import org.mifosng.platform.api.data.LoanProductLookup;
import org.mifosng.platform.api.data.LoanRepaymentPeriodData;
import org.mifosng.platform.api.data.LoanRepaymentTransactionData;
import org.mifosng.platform.api.data.LoanSchedule;
import org.mifosng.platform.api.data.LoanTransactionData;
import org.mifosng.platform.api.data.StaffData;
import org.mifosng.platform.api.data.TransactionProcessingStrategyData;
import org.mifosng.platform.api.infrastructure.ApiDataConversionService;
import org.mifosng.platform.api.infrastructure.ApiJsonSerializerService;
import org.mifosng.platform.api.infrastructure.ApiParameterHelper;
import org.mifosng.platform.charge.service.ChargeReadPlatformService;
import org.mifosng.platform.exceptions.UnrecognizedQueryParamException;
import org.mifosng.platform.fund.service.FundReadPlatformService;
import org.mifosng.platform.loan.service.CalculationPlatformService;
import org.mifosng.platform.loan.service.LoanReadPlatformService;
import org.mifosng.platform.loan.service.LoanWritePlatformService;
import org.mifosng.platform.loanproduct.service.LoanDropdownReadPlatformService;
import org.mifosng.platform.loanproduct.service.LoanProductReadPlatformService;
import org.mifosng.platform.staff.service.StaffReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/loans")
@Component
@Scope("singleton")
public class LoansApiResource {

	@Autowired
	private LoanReadPlatformService loanReadPlatformService;

	@Autowired
	private LoanWritePlatformService loanWritePlatformService;
	
	@Autowired
	private LoanProductReadPlatformService loanProductReadPlatformService;

	@Autowired
	private LoanDropdownReadPlatformService dropdownReadPlatformService;

	@Autowired
	private FundReadPlatformService fundReadPlatformService;
	
    @Autowired
    private ChargeReadPlatformService chargeReadPlatformService;

	@Autowired
	private CalculationPlatformService calculationPlatformService;

	@Autowired
	private ApiDataConversionService apiDataConversionService;
	
	@Autowired
	private ApiJsonSerializerService apiJsonSerializerService;
	
	@Autowired
	private StaffReadPlatformService staffReadPlatformService;
	
	@GET
	@Path("template")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveDetailsForNewLoanApplicationStepOne(
			@QueryParam("clientId") final Long clientId,
			@QueryParam("productId") final Long productId,
			@Context final UriInfo uriInfo) {
		
		Set<String> typicalResponseParameters = new HashSet<String>(
				Arrays.asList("id", "externalId", "clientId", "clientName", "fundId", "fundName",
						"loanProductId", "loanProductName", "loanProductDescription", 
						"currency", "principal",
						"inArrearsTolerance", "numberOfRepayments",
						"repaymentEvery", "interestRatePerPeriod",
						"annualInterestRate", "repaymentFrequencyType",
						"interestRateFrequencyType", "amortizationType",
						"interestType", "interestCalculationPeriodType",
						"submittedOnDate", "approvedOnDate",
						"expectedDisbursementDate", "actualDisbursementDate",
						"expectedFirstRepaymentOnDate", "interestChargedFromDate",
						"closedOnDate", "expectedMaturityDate",
						"status",
						"lifeCycleStatusDate", "summary", "repaymentSchedule",
						"loanRepayments", "permissions", "convenienceData", "charges")
				);
		
		Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
		if (responseParameters.isEmpty()) {
			responseParameters.addAll(typicalResponseParameters);
		}else if (responseParameters.contains("loanOfficerOptions")  && (responseParameters.size() == 1)) {
			// TODO Vishwas: check if response parameters should be additive
			responseParameters.addAll(typicalResponseParameters);
		}
		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
		
		responseParameters.addAll(Arrays.asList("productOptions", "amortizationTypeOptions", "interestTypeOptions", "interestCalculationPeriodTypeOptions", 
				"repaymentFrequencyTypeOptions", "interestRateFrequencyTypeOptions", "fundOptions", "transactionProcessingStrategyOptions", "chargeOptions"));
	
		// tempate related
		Collection<LoanProductLookup> productOptions = this.loanProductReadPlatformService.retrieveAllLoanProductsForLookup();
		Collection<EnumOptionData>loanTermFrequencyTypeOptions = dropdownReadPlatformService.retrieveLoanTermFrequencyTypeOptions();
		Collection<EnumOptionData>repaymentFrequencyTypeOptions = dropdownReadPlatformService.retrieveRepaymentFrequencyTypeOptions();
		Collection<EnumOptionData>interestRateFrequencyTypeOptions = dropdownReadPlatformService.retrieveInterestRateFrequencyTypeOptions();
		
		Collection<EnumOptionData>amortizationTypeOptions = dropdownReadPlatformService.retrieveLoanAmortizationTypeOptions();
		Collection<EnumOptionData>interestTypeOptions = dropdownReadPlatformService.retrieveLoanInterestTypeOptions();
		Collection<EnumOptionData>interestCalculationPeriodTypeOptions = dropdownReadPlatformService.retrieveLoanInterestRateCalculatedInPeriodOptions();
	
		Collection<FundData> fundOptions = this.fundReadPlatformService.retrieveAllFunds();
		Collection<TransactionProcessingStrategyData> transactionProcessingStrategyOptions = this.dropdownReadPlatformService.retreiveTransactionProcessingStrategies();
		
        Collection<ChargeData> chargeOptions = this.chargeReadPlatformService.retrieveLoanApplicableCharges();

        LoanBasicDetailsData loanBasicDetails = this.loanReadPlatformService.retrieveClientAndProductDetails(clientId, productId);
        if (loanBasicDetails.getCharges() != null) {
        	chargeOptions.removeAll(loanBasicDetails.getCharges());
        }
		
		final boolean convenienceDataRequired = false;
		Collection<ChargeData> charges = loanBasicDetails.getCharges();
		
		// populate loan officers if query param is passed in
		Collection<StaffData> allowedLoanOfficers = null;
		if (responseParameters.contains("loanOfficerOptions")) {
			// get applicable loan officers
			allowedLoanOfficers = this.staffReadPlatformService
					.retrieveAllStaff(" office_id = "
							+ loanBasicDetails.getClientOfficeId() + " and is_loan_officer = 1");
		}
		final LoanAccountData newLoanAccount = new LoanAccountData(loanBasicDetails, convenienceDataRequired, null, null, null, null, charges, 
				productOptions, loanTermFrequencyTypeOptions, repaymentFrequencyTypeOptions, 
				transactionProcessingStrategyOptions, interestRateFrequencyTypeOptions, 
				amortizationTypeOptions, interestTypeOptions, interestCalculationPeriodTypeOptions, fundOptions, chargeOptions, allowedLoanOfficers);

		return this.apiJsonSerializerService.serialzieLoanAccountDataToJson(prettyPrint, responseParameters, newLoanAccount);
	}

	@GET
	@Path("{loanId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveLoanAccountDetails(
			@PathParam("loanId") final Long loanId,
			@Context final UriInfo uriInfo) {

		Set<String> typicalResponseParameters = new HashSet<String>(
				Arrays.asList("id", "externalId", "clientId", "clientName", "fundId", "fundName", "loanProductId", "loanProductName", "loanProductDescription",
						"currency",
						"principal", "inArrearsTolerance", "numberOfRepayments",
						"repaymentEvery", "interestRatePerPeriod", "annualInterestRate", 
						"repaymentFrequencyType", "interestRateFrequencyType", "amortizationType", "interestType", "interestCalculationPeriodType",
						"submittedOnDate", "approvedOnDate", "expectedDisbursementDate", "actualDisbursementDate", 
						"expectedFirstRepaymentOnDate", "interestChargedFromDate", "closedOnDate", "expectedMaturityDate", 
						"status", "lifeCycleStatusDate")
				);
		
		Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
		if (responseParameters.isEmpty()) {
			responseParameters.addAll(typicalResponseParameters);
		} else if ((responseParameters.contains("loanOfficerName") || responseParameters
				.contains("loanOfficerId")) && (responseParameters.size() <= 2)) {
			// TODO Vishwas: check if response parameters should be additive
			responseParameters.addAll(typicalResponseParameters);
		}
		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
		
		LoanBasicDetailsData loanBasicDetails = this.loanReadPlatformService.retrieveLoanAccountDetails(loanId);
		
		Collection<LoanRepaymentTransactionData> loanRepayments = null;
		Collection<LoanRepaymentPeriodData> repaymentSchedule = null;
		LoanAccountSummaryData summary = null;
		LoanPermissionData permissions = null;
        Collection<ChargeData> charges = null;

        boolean convenienceDataRequired = false;
		Set<String> associationParameters = ApiParameterHelper.extractAssociationsForResponseIfProvided(uriInfo.getQueryParameters());
		if (!associationParameters.isEmpty()) {
			if (associationParameters.contains("all")) {
				responseParameters.addAll(Arrays.asList("summary", "repaymentSchedule", "loanRepayments", "permissions", "convenienceData", "charges"));
			} else {
				responseParameters.addAll(associationParameters);
			}
			
			loanRepayments = this.loanReadPlatformService.retrieveLoanPayments(loanId);
			repaymentSchedule = this.loanReadPlatformService.retrieveRepaymentSchedule(loanId);

			summary = this.loanReadPlatformService.retrieveSummary(loanBasicDetails.getCurrency(), repaymentSchedule, loanRepayments);

			boolean isWaiveAllowed = summary.isWaiveAllowed(loanBasicDetails.getCurrency(), loanBasicDetails.getInArrearsTolerance());
			permissions = this.loanReadPlatformService.retrieveLoanPermissions(loanBasicDetails, isWaiveAllowed, loanRepayments.size());
			convenienceDataRequired = true;
			
            charges = this.chargeReadPlatformService.retrieveLoanCharges(loanId);
		}

		Collection<LoanProductLookup> productOptions = new ArrayList<LoanProductLookup>();
		Collection<EnumOptionData> loanTermFrequencyTypeOptions = new ArrayList<EnumOptionData>();
		Collection<EnumOptionData> repaymentFrequencyTypeOptions = new ArrayList<EnumOptionData>();
		Collection<TransactionProcessingStrategyData> transactionProcessingStrategyOptions = new ArrayList<TransactionProcessingStrategyData>();
		
		Collection<EnumOptionData> interestRateFrequencyTypeOptions = new ArrayList<EnumOptionData>();
		Collection<EnumOptionData> amortizationTypeOptions = new ArrayList<EnumOptionData>();
		Collection<EnumOptionData> interestTypeOptions = new ArrayList<EnumOptionData>();
		Collection<EnumOptionData> interestCalculationPeriodTypeOptions = new ArrayList<EnumOptionData>();
		Collection<FundData> fundOptions = new ArrayList<FundData>();
		Collection<ChargeData> chargeOptions = null;
		
		final boolean template = ApiParameterHelper.template(uriInfo.getQueryParameters());
		if(template) {
			responseParameters.addAll(Arrays.asList("productOptions", "amortizationTypeOptions", "interestTypeOptions", "interestCalculationPeriodTypeOptions", 
						"repaymentFrequencyTypeOptions", "interestRateFrequencyTypeOptions", "fundOptions", "transactionProcessingStrategyOptions", "chargeOptions"));
			
			productOptions = this.loanProductReadPlatformService.retrieveAllLoanProductsForLookup();
			loanTermFrequencyTypeOptions = dropdownReadPlatformService.retrieveLoanTermFrequencyTypeOptions();
			repaymentFrequencyTypeOptions = dropdownReadPlatformService.retrieveRepaymentFrequencyTypeOptions();
			interestRateFrequencyTypeOptions = dropdownReadPlatformService.retrieveInterestRateFrequencyTypeOptions();
			
			amortizationTypeOptions = dropdownReadPlatformService.retrieveLoanAmortizationTypeOptions();
			interestTypeOptions = dropdownReadPlatformService.retrieveLoanInterestTypeOptions();
			interestCalculationPeriodTypeOptions = dropdownReadPlatformService.retrieveLoanInterestRateCalculatedInPeriodOptions();

			fundOptions = this.fundReadPlatformService.retrieveAllFunds();
			transactionProcessingStrategyOptions = this.dropdownReadPlatformService.retreiveTransactionProcessingStrategies();
			chargeOptions = this.chargeReadPlatformService.retrieveLoanApplicableCharges();
			if (charges != null) {
				chargeOptions.removeAll(charges);
			}
		}
		
		Collection<StaffData> allowedLoanOfficers = null;
		if (responseParameters.contains("loanOfficerOptions")) {
			// get applicable loan officers
			allowedLoanOfficers = this.staffReadPlatformService
					.retrieveAllStaff(" office_id = "
							+ loanBasicDetails.getClientOfficeId() + " and is_loan_officer = 1");
		}

		final LoanAccountData loanAccount = new LoanAccountData(loanBasicDetails, convenienceDataRequired, summary, repaymentSchedule, loanRepayments, permissions, charges, 
				productOptions, loanTermFrequencyTypeOptions, repaymentFrequencyTypeOptions, 
				transactionProcessingStrategyOptions, interestRateFrequencyTypeOptions, 
				amortizationTypeOptions, interestTypeOptions, interestCalculationPeriodTypeOptions, fundOptions, chargeOptions, allowedLoanOfficers);
		
		return this.apiJsonSerializerService.serialzieLoanAccountDataToJson(prettyPrint, responseParameters, loanAccount);
	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response calculateLoanScheduleOrSubmitLoanApplication(
			@QueryParam("command") final String commandParam,
			final String jsonRequestBody) {

		LoanApplicationCommand command = this.apiDataConversionService.convertJsonToLoanApplicationCommand(null, jsonRequestBody);

		CalculateLoanScheduleCommand calculateLoanScheduleCommand = command.toCalculateLoanScheduleCommand();
		LoanSchedule loanSchedule = this.calculationPlatformService.calculateLoanSchedule(calculateLoanScheduleCommand);

		// for now just auto generating the loan schedule and setting support
		// for 'manual' loan schedule creation later.
		command.setLoanSchedule(loanSchedule);

		if (is(commandParam, "calculateLoanSchedule")) {
			return Response.ok().entity(loanSchedule).build();
		}

		EntityIdentifier identifier = this.loanWritePlatformService.submitLoanApplication(command);

		return Response.ok().entity(identifier).build();
	}
	
	@PUT
	@Path("{loanId}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response modifyLoanApplication(
			@PathParam("loanId") final Long loanId,
			@QueryParam("command") final String commandParam,
			final String jsonRequestBody) {

		final LoanApplicationCommand command = this.apiDataConversionService.convertJsonToLoanApplicationCommand(loanId, jsonRequestBody);

		final CalculateLoanScheduleCommand calculateLoanScheduleCommand = command.toCalculateLoanScheduleCommand();
		final LoanSchedule loanSchedule = this.calculationPlatformService.calculateLoanSchedule(calculateLoanScheduleCommand);

		// for now just auto generating the loan schedule and setting support
		// for 'manual' loan schedule creation later.
		command.setLoanSchedule(loanSchedule);

		if (is(commandParam, "calculateLoanSchedule")) {
			return Response.ok().entity(loanSchedule).build();
		}

		EntityIdentifier identifier = this.loanWritePlatformService.modifyLoanApplication(command);

		return Response.ok().entity(identifier).build();
	}

	@DELETE
	@Path("{loanId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response deleteLoanApplication(@PathParam("loanId") final Long loanId) {

		EntityIdentifier identifier = this.loanWritePlatformService
				.deleteLoan(loanId);

		return Response.ok().entity(identifier).build();
	}

	@POST
	@Path("{loanId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response stateTransitions(@PathParam("loanId") final Long loanId,
			@QueryParam("command") final String commandParam,
			final String jsonRequestBody) {

		LoanStateTransitionCommand command = this.apiDataConversionService.convertJsonToLoanStateTransitionCommand(loanId, jsonRequestBody);

		Response response = null;

		if (is(commandParam, "reject")) {
			EntityIdentifier identifier = this.loanWritePlatformService
					.rejectLoan(command);
			response = Response.ok().entity(identifier).build();
		} else if (is(commandParam, "withdrewbyclient")) {
			EntityIdentifier identifier = this.loanWritePlatformService
					.withdrawLoan(command);
			response = Response.ok().entity(identifier).build();
		} else if (is(commandParam, "approve")) {
			EntityIdentifier identifier = this.loanWritePlatformService
					.approveLoanApplication(command);
			response = Response.ok().entity(identifier).build();
		} else if (is(commandParam, "disburse")) {
			EntityIdentifier identifier = this.loanWritePlatformService
					.disburseLoan(command);
			response = Response.ok().entity(identifier).build();
		}

		UndoStateTransitionCommand undoCommand = new UndoStateTransitionCommand(
				loanId, command.getNote());

		if (is(commandParam, "undoapproval")) {
			EntityIdentifier identifier = this.loanWritePlatformService
					.undoLoanApproval(undoCommand);
			response = Response.ok().entity(identifier).build();
		} else if (is(commandParam, "undodisbursal")) {
			EntityIdentifier identifier = this.loanWritePlatformService
					.undoLoanDisbursal(undoCommand);
			response = Response.ok().entity(identifier).build();
		}

		if (response == null) {
			throw new UnrecognizedQueryParamException("command", commandParam);
		}

		return response;
	}

	private boolean is(final String commandParam, final String commandValue) {
		return StringUtils.isNotBlank(commandParam)
				&& commandParam.trim().equalsIgnoreCase(commandValue);
	}

	@POST
	@Path("{loanId}/transactions")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response executeLoanTransaction(
			@PathParam("loanId") final Long loanId,
			@QueryParam("command") final String commandParam,
			final String jsonRequestBody) {

		final LoanTransactionCommand command = this.apiDataConversionService
				.convertJsonToLoanTransactionCommand(loanId, jsonRequestBody);

		Response response = null;

		if (is(commandParam, "repayment")) {
			EntityIdentifier identifier = this.loanWritePlatformService
					.makeLoanRepayment(command);
			response = Response.ok().entity(identifier).build();
		} else if (is(commandParam, "waiver")) {
			EntityIdentifier identifier = this.loanWritePlatformService
					.waiveLoanAmount(command);
			response = Response.ok().entity(identifier).build();
		}

		if (response == null) {
			throw new UnrecognizedQueryParamException("command", commandParam);
		}

		return response;
	}

	@GET
	@Path("{loanId}/transactions/template")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveNewRepaymentDetails(
			@PathParam("loanId") final Long loanId,
			@QueryParam("command") final String commandParam,
			@Context final UriInfo uriInfo) {
		
		Set<String> typicalResponseParameters = new HashSet<String>(
				Arrays.asList("id", "transactionType", "date", "principal", "interest", "total", "totalWaived", "overpaid")
				);
		
		Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
		if (responseParameters.isEmpty()) {
			responseParameters.addAll(typicalResponseParameters);
		}
		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
		
		LoanTransactionData transactionData = null;
		if (is(commandParam, "repayment")) {
			transactionData = this.loanReadPlatformService.retrieveNewLoanRepaymentDetails(loanId);
		} else if (is(commandParam, "waiver")) {
			transactionData = this.loanReadPlatformService.retrieveNewLoanWaiverDetails(loanId);
		} else {
			throw new UnrecognizedQueryParamException("command", commandParam);
		}

		return this.apiJsonSerializerService.serializeLoanTransactionDataToJson(prettyPrint, responseParameters, transactionData);
	}

	@GET
	@Path("{loanId}/transactions/{transactionId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveTransaction(@PathParam("loanId") final Long loanId,
			@PathParam("transactionId") final Long transactionId,
			@Context final UriInfo uriInfo) {
		
		Set<String> typicalResponseParameters = new HashSet<String>(
				Arrays.asList("id", "transactionType", "date", "principal", "interest", "total", "totalWaived", "overpaid")
				);
		
		Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
		if (responseParameters.isEmpty()) {
			responseParameters.addAll(typicalResponseParameters);
		}
		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
		
		LoanTransactionData transactionData = this.loanReadPlatformService.retrieveLoanTransactionDetails(loanId, transactionId);

		return this.apiJsonSerializerService.serializeLoanTransactionDataToJson(prettyPrint, responseParameters, transactionData);
	}

	@POST
	@Path("{loanId}/transactions/{transactionId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response adjustLoanTransaction(
			@PathParam("loanId") final Long loanId,
			@PathParam("transactionId") final Long transactionId,
			final String jsonRequestBody) {

		final AdjustLoanTransactionCommand command = this.apiDataConversionService
				.convertJsonToAdjustLoanTransactionCommand(loanId,
						transactionId, jsonRequestBody);

		EntityIdentifier identifier = this.loanWritePlatformService
				.adjustLoanTransaction(command);

		return Response.ok().entity(identifier).build();
	}
}