package org.mifosng.platform.api;

import java.math.BigDecimal;
import java.util.Locale;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
import org.mifosng.data.LoanAccountData;
import org.mifosng.data.LoanRepaymentData;
import org.mifosng.data.LoanSchedule;
import org.mifosng.platform.api.commands.AdjustLoanTransactionCommand;
import org.mifosng.platform.api.commands.CalculateLoanScheduleCommand;
import org.mifosng.platform.api.commands.LoanStateTransitionCommand;
import org.mifosng.platform.api.commands.LoanTransactionCommand;
import org.mifosng.platform.api.commands.SubmitLoanApplicationCommand;
import org.mifosng.platform.api.commands.UndoStateTransitionCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.api.data.NewLoanData;
import org.mifosng.platform.api.infrastructure.ApiDataConversionService;
import org.mifosng.platform.api.infrastructure.ApiJSONFormattingService;
import org.mifosng.platform.exceptions.UnrecognizedQueryParamException;
import org.mifosng.platform.loan.service.CalculationPlatformService;
import org.mifosng.platform.loan.service.LoanReadPlatformService;
import org.mifosng.platform.loan.service.LoanWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/loans")
@Component
@Scope("singleton")
public class LoansApiResource {

	private String filterName = "myFilter";
	private String allowedFieldList = "";
	private String loanFilterName = "loanFilter";
	private String loanAllowedFieldList = "";
	private String loanRepaymentFilterName = "loanRepaymentFilter";
	private String loanRepaymentDefaultFieldList = "date,total";
	private String loanRepaymentAllowedFieldList = "";

	@Autowired
	private LoanReadPlatformService loanReadPlatformService;

	@Autowired
	private LoanWritePlatformService loanWritePlatformService;

	@Autowired
	private CalculationPlatformService calculationPlatformService;

	@Autowired
	private ApiDataConversionService apiDataConversionService;

	@Autowired
	private ApiJSONFormattingService jsonFormattingService;

	@GET
	@Path("template")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON})
	public String retrieveDetailsForNewLoanApplicationStepOne(
			@QueryParam("clientId") final Long clientId,
			@QueryParam("productId") final Long productId,
			@Context UriInfo uriInfo) {

		NewLoanData workflowData = this.loanReadPlatformService
				.retrieveClientAndProductDetails(clientId, productId);

		String selectedFields = "";
		return this.jsonFormattingService.convertRequest(workflowData,
				filterName, allowedFieldList, selectedFields,
				uriInfo.getQueryParameters());
	}

	@GET
	@Path("{loanId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON})
	public String retrieveLoanAccountDetails(
			@PathParam("loanId") final Long loanId, @Context UriInfo uriInfo) {

		LoanAccountData loanAccount = this.loanReadPlatformService
				.retrieveLoanAccountDetails(loanId);

		String selectedFields = "";
		return this.jsonFormattingService.convertRequest(loanAccount,
				loanFilterName, loanAllowedFieldList, selectedFields,
				uriInfo.getQueryParameters());
	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON})
	public Response calculateLoanSchedule(
			@QueryParam("command") final String commandParam,
			final SubmitLoanApplicationCommand command) {

		LocalDate expectedDisbursementLocalDate = apiDataConversionService
				.convertFrom(command.getExpectedDisbursementDate(),
						"expectedDisbursementDate",
						command.getDateFormat());
		LocalDate repaymentsStartingFromLocalDate = apiDataConversionService
				.convertFrom(command.getRepaymentsStartingFromDate(),
						"repaymentsStartingFromDate",
						command.getDateFormat());
		LocalDate interestChargedFromLocalDate = apiDataConversionService
				.convertFrom(command.getInterestChargedFromDate(),
						"interestChargedFromDate",
						command.getDateFormat());
		LocalDate submittedOnLocalDate = apiDataConversionService.convertFrom(
				command.getSubmittedOnDate(),
				"submittedOnDate", command.getDateFormat());
		
		command.setExpectedDisbursementLocalDate(expectedDisbursementLocalDate);
		command.setRepaymentsStartingFromLocalDate(repaymentsStartingFromLocalDate);
		command.setInterestChargedFromLocalDate(interestChargedFromLocalDate);
		command.setSubmittedOnLocalDate(submittedOnLocalDate);

		Locale clientApplicationLocale = this.apiDataConversionService.localeFromString(command.getLocale());
		BigDecimal principalValue = this.apiDataConversionService.convertFrom(command.getPrincipal(), "principal", clientApplicationLocale);
		BigDecimal inArrearsToleranceValue = this.apiDataConversionService.convertFrom(command.getInArrearsTolerance(), "inArrearsTolerance", clientApplicationLocale);
		BigDecimal interestRatePerPeriodValue = this.apiDataConversionService.convertFrom(command.getInterestRatePerPeriod(),"interestRatePerPeriod", clientApplicationLocale);
		
		Integer digitsAfterDecimalValue = this.apiDataConversionService.convertToInteger(command.getDigitsAfterDecimal(), "digitsAfterDecimal", clientApplicationLocale);
		Integer repaymentEveryValue = this.apiDataConversionService.convertToInteger(command.getRepaymentEvery(), "repaymentEvery", clientApplicationLocale);
		Integer numberOfRepaymentsValue = this.apiDataConversionService.convertToInteger(command.getNumberOfRepayments(), "numberOfRepayments", clientApplicationLocale);
		
		command.setPrincipalValue(principalValue);
		command.setInArrearsToleranceValue(inArrearsToleranceValue);
		command.setInterestRatePerPeriodValue(interestRatePerPeriodValue);
		command.setDigitsAfterDecimalValue(digitsAfterDecimalValue);
		command.setRepaymentEveryValue(repaymentEveryValue);
		command.setNumberOfRepaymentsValue(numberOfRepaymentsValue);

		CalculateLoanScheduleCommand calculateLoanScheduleCommand = command
				.toCalculateLoanScheduleCommand();
		LoanSchedule loanSchedule = this.calculationPlatformService
				.calculateLoanSchedule(calculateLoanScheduleCommand);

		// for now just auto generating the loan schedule and setting support
		// for 'manual' loan schedule creation later.
		command.setLoanSchedule(loanSchedule);

		if (StringUtils.isNotBlank(commandParam)
				&& commandParam.trim()
						.equalsIgnoreCase("calculateLoanSchedule")) {

			return Response.ok().entity(loanSchedule).build();
		}

		EntityIdentifier identifier = this.loanWritePlatformService
				.submitLoanApplication(command);

		return Response.ok().entity(identifier).build();
	}

	@DELETE
	@Path("{loanId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON})
	public Response deleteLoanApplication(@PathParam("loanId") final Long loanId) {

		EntityIdentifier identifier = this.loanWritePlatformService
				.deleteLoan(loanId);

		return Response.ok().entity(identifier).build();
	}

	@POST
	@Path("{loanId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON})
	public Response stateTransitions(@PathParam("loanId") final Long loanId, @QueryParam("command") final String commandParam,
			final LoanStateTransitionCommand command) {

		LocalDate eventLocalDate = this.apiDataConversionService.convertFrom(
				command.getEventDate(), "eventDate",
				command.getDateFormat());

		command.setLoanId(loanId);
		command.setEventLocalDate(eventLocalDate);
		
		Response response = null;

		if (is(commandParam, "reject")) {
			EntityIdentifier identifier = this.loanWritePlatformService.rejectLoan(command);
			response = Response.ok().entity(identifier).build();
		} else if (is(commandParam, "withdrewbyclient")) {
			EntityIdentifier identifier = this.loanWritePlatformService.withdrawLoan(command);
			response = Response.ok().entity(identifier).build();
		} else if (is(commandParam, "approve")) {
			EntityIdentifier identifier = this.loanWritePlatformService.approveLoanApplication(command);
			response = Response.ok().entity(identifier).build();
		} else if (is(commandParam, "disburse")) {
			EntityIdentifier identifier = this.loanWritePlatformService.disburseLoan(command);
			response = Response.ok().entity(identifier).build();
		}
		
		UndoStateTransitionCommand undoCommand = new UndoStateTransitionCommand(loanId, command.getNote());
		if (is(commandParam, "undoapproval")) {
			EntityIdentifier identifier = this.loanWritePlatformService.undoLoanApproval(undoCommand);
			response = Response.ok().entity(identifier).build();
		} else if (is(commandParam, "undodisbursal")) {
			EntityIdentifier identifier = this.loanWritePlatformService.undoLoanDisbursal(undoCommand);
			response = Response.ok().entity(identifier).build();
		}
		
		if (response == null) {
			throw new UnrecognizedQueryParamException("command", commandParam);
		}

		return response;
	}

	private boolean is(final String commandParam, final String commandValue) {
		return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
	}

	@POST
	@Path("{loanId}/transactions")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON})
	public Response executeLoanTransaction(
			@PathParam("loanId") final Long loanId,
			@QueryParam("command") final String commandParam,
			final LoanTransactionCommand command) {

		Response response = null;
		command.setLoanId(loanId);
		
		Locale clientLocale = this.apiDataConversionService.localeFromString(command.getLocale());

		LocalDate transactionLocalDate = apiDataConversionService.convertFrom(
				command.getTransactionDate(),
				"transactionDate", command.getDateFormat());
		command.setTransactionLocalDate(transactionLocalDate);

		BigDecimal transactionAmountValue = apiDataConversionService.convertFrom(
				command.getTransactionAmount(),
				"transactionAmount", clientLocale);
		command.setTransactionAmountValue(transactionAmountValue);

		if (is(commandParam, "repayment")) {
			EntityIdentifier identifier = this.loanWritePlatformService.makeLoanRepayment(command);
			response = Response.ok().entity(identifier).build();
		} else if (is(commandParam, "waiver")) {
			EntityIdentifier identifier = this.loanWritePlatformService.waiveLoanAmount(command);
			response = Response.ok().entity(identifier).build();
		}
		
		if (response == null) {
			throw new UnrecognizedQueryParamException("command", commandParam);
		}

		return response;
	}

	@GET
	@Path("{loanId}/transactions/template")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON})
	public String retrieveNewRepaymentDetails(
			@PathParam("loanId") final Long loanId,
			@QueryParam("command") final String commandParam,
			@Context UriInfo uriInfo) {

		String json = "";
		String selectedFields = loanRepaymentDefaultFieldList;
		if (is(commandParam, "repayment")) {
			LoanRepaymentData loanRepaymentData = this.loanReadPlatformService
					.retrieveNewLoanRepaymentDetails(loanId);
			json = this.jsonFormattingService.convertRequest(loanRepaymentData,
					loanRepaymentFilterName, loanRepaymentAllowedFieldList, selectedFields,
					uriInfo.getQueryParameters());
		} else if (is(commandParam, "waiver")) {
			LoanRepaymentData loanWaiverData = this.loanReadPlatformService
					.retrieveNewLoanWaiverDetails(loanId);
			json = this.jsonFormattingService.convertRequest(loanWaiverData,
					loanRepaymentFilterName, loanRepaymentAllowedFieldList, selectedFields,
					uriInfo.getQueryParameters());
		}

		if (StringUtils.isBlank(json)) {
			throw new UnrecognizedQueryParamException("command", commandParam);
		}
		
		return json;
	}

	@GET
	@Path("{loanId}/transactions/{transactionId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON})
	public String retrieveRepaymentDetails(
			@PathParam("loanId") final Long loanId,
			@PathParam("transactionId") final Long transactionId,
			@Context UriInfo uriInfo) {

		LoanRepaymentData loanRepaymentData = this.loanReadPlatformService
				.retrieveLoanRepaymentDetails(loanId, transactionId);

		String selectedFields = "";
		return this.jsonFormattingService.convertRequest(loanRepaymentData,
				loanRepaymentFilterName, loanRepaymentAllowedFieldList, selectedFields,
				uriInfo.getQueryParameters());
	}

	@POST
	@Path("{loanId}/transactions/{transactionId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON})
	public Response adjustLoanTransaction(
			@PathParam("loanId") final Long loanId,
			@PathParam("transactionId") final Long transactionId,
			final AdjustLoanTransactionCommand command) {

		command.setLoanId(loanId);
		command.setTransactionId(transactionId);
		
		Locale clientLocale = this.apiDataConversionService.localeFromString(command.getLocale());

		LocalDate transactionLocalDate = apiDataConversionService.convertFrom(
				command.getTransactionDate(),
				"transactionDate", command.getDateFormat());
		command.setTransactionLocalDate(transactionLocalDate);

		BigDecimal transactionAmountValue = apiDataConversionService.convertFrom(
				command.getTransactionAmount(),
				"transactionAmount", clientLocale);
		command.setTransactionAmountValue(transactionAmountValue);

		EntityIdentifier identifier = this.loanWritePlatformService.adjustLoanTransaction(command);

		return Response.ok().entity(identifier).build();
	}
}