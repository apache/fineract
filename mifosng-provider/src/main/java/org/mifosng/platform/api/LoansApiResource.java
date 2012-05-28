package org.mifosng.platform.api;

import java.math.BigDecimal;
import java.util.Locale;

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
import org.mifosng.data.EntityIdentifier;
import org.mifosng.data.LoanAccountData;
import org.mifosng.data.LoanRepaymentData;
import org.mifosng.data.LoanSchedule;
import org.mifosng.data.NewLoanWorkflowStepOneData;
import org.mifosng.data.command.AdjustLoanTransactionCommand;
import org.mifosng.data.command.CalculateLoanScheduleCommand;
import org.mifosng.data.command.LoanStateTransitionCommand;
import org.mifosng.data.command.LoanTransactionCommand;
import org.mifosng.data.command.SubmitLoanApplicationCommand;
import org.mifosng.data.command.UndoStateTransitionCommand;
import org.mifosng.platform.api.infrastructure.ApiDataConversionService;
import org.mifosng.platform.api.infrastructure.ApiJSONFormattingService;
import org.mifosng.platform.exceptions.UnrecognizedQueryParamException;
import org.mifosng.platform.loan.service.CalculationPlatformService;
import org.mifosng.platform.loan.service.LoanReadPlatformService;
import org.mifosng.platform.loan.service.LoanWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/v1/loans")
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

		NewLoanWorkflowStepOneData workflowData = this.loanReadPlatformService
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

		LocalDate expectedDisbursementDate = apiDataConversionService
				.convertFrom(command.getExpectedDisbursementDateFormatted(),
						"expectedDisbursementDateFormatted",
						command.getDateFormat());
		LocalDate repaymentsStartingFromDate = apiDataConversionService
				.convertFrom(command.getRepaymentsStartingFromDateFormatted(),
						"repaymentsStartingFromDateFormatted",
						command.getDateFormat());
		LocalDate interestCalculatedFromDate = apiDataConversionService
				.convertFrom(command.getInterestCalculatedFromDateFormatted(),
						"interestCalculatedFromDateFormatted",
						command.getDateFormat());
		LocalDate submittedOnDate = apiDataConversionService.convertFrom(
				command.getSubmittedOnDateFormatted(),
				"submittedOnDateFormatted", command.getDateFormat());
		command.setExpectedDisbursementDate(expectedDisbursementDate);
		command.setRepaymentsStartingFromDate(repaymentsStartingFromDate);
		command.setInterestCalculatedFromDate(interestCalculatedFromDate);
		command.setSubmittedOnDate(submittedOnDate);

		// FIXME - pass in locale through query string or in 'request body'
		Locale clientApplicationLocale = Locale.UK;
		BigDecimal principal = this.apiDataConversionService.convertFrom(
				command.getPrincipalFormatted(), "principalFormatted",
				clientApplicationLocale);
		BigDecimal interestRatePerPeriod = this.apiDataConversionService
				.convertFrom(command.getInterestRatePerPeriodFormatted(),
						"interestRatePerPeriodFormatted",
						clientApplicationLocale);
		BigDecimal inArrearsToleranceAmount = this.apiDataConversionService
				.convertFrom(command.getInArrearsToleranceAmountFormatted(),
						"inArrearsToleranceAmountFormatted",
						clientApplicationLocale);
		command.setPrincipal(principal);
		command.setInterestRatePerPeriod(interestRatePerPeriod);
		command.setInArrearsToleranceAmount(inArrearsToleranceAmount);

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

		LocalDate eventDate = this.apiDataConversionService.convertFrom(
				command.getEventDateFormatted(), "eventDateFormatted",
				command.getDateFormat());

		command.setLoanId(loanId);
		command.setEventDate(eventDate);
		
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
		
		UndoStateTransitionCommand undoCommand = new UndoStateTransitionCommand(loanId, command.getComment());
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
			@QueryParam("transactionType") final String transactionType,
			final LoanTransactionCommand command) {

		command.setLoanId(loanId);

		LocalDate transactionDate = apiDataConversionService.convertFrom(
				command.getTransactionDateFormatted(),
				"transactionDateFormatted", command.getDateFormat());
		command.setTransactionDate(transactionDate);

		BigDecimal transactionAmount = apiDataConversionService.convertFrom(
				command.getTransactionAmountFormatted(),
				"transactionAmountFormatted", Locale.UK);
		command.setTransactionAmount(transactionAmount);

		if (StringUtils.isNotBlank(transactionType)
				&& transactionType.trim().equalsIgnoreCase("waiver")) {
			EntityIdentifier identifier = this.loanWritePlatformService
					.waiveLoanAmount(command);
			return Response.ok().entity(identifier).build();
		}

		EntityIdentifier identifier = this.loanWritePlatformService
				.makeLoanRepayment(command);
		return Response.ok().entity(identifier).build();
	}

	@GET
	@Path("{loanId}/transactions/template")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON})
	public String retrieveNewRepaymentDetails(
			@PathParam("loanId") final Long loanId,
			@QueryParam("type") final String transactionType,
			@Context UriInfo uriInfo) {

		String selectedFields = loanRepaymentDefaultFieldList;
		if (StringUtils.isNotBlank(transactionType)
				&& transactionType.trim().equalsIgnoreCase("waiver")) {
			LoanRepaymentData loanWaiverData = this.loanReadPlatformService
					.retrieveNewLoanWaiverDetails(loanId);
			return this.jsonFormattingService.convertRequest(loanWaiverData,
					loanRepaymentFilterName, loanRepaymentAllowedFieldList, selectedFields,
					uriInfo.getQueryParameters());
		}

		LoanRepaymentData loanRepaymentData = this.loanReadPlatformService
				.retrieveNewLoanRepaymentDetails(loanId);
		return this.jsonFormattingService.convertRequest(loanRepaymentData,
				loanRepaymentFilterName, loanRepaymentAllowedFieldList, selectedFields,
				uriInfo.getQueryParameters());
	}

	@GET
	@Path("{loanId}/transactions/{repaymentId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON})
	public String retrieveRepaymentDetails(
			@PathParam("loanId") final Long loanId,
			@PathParam("repaymentId") final Long repaymentId,
			@Context UriInfo uriInfo) {

		LoanRepaymentData loanRepaymentData = this.loanReadPlatformService
				.retrieveLoanRepaymentDetails(loanId, repaymentId);

		String selectedFields = "";
		return this.jsonFormattingService.convertRequest(loanRepaymentData,
				loanRepaymentFilterName, loanRepaymentAllowedFieldList, selectedFields,
				uriInfo.getQueryParameters());
	}

	@PUT
	@Path("{loanId}/transactions/{repaymentId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON})
	public Response adjustLoanTransaction(
			@PathParam("loanId") final Long loanId,
			@PathParam("repaymentId") final Long repaymentId,
			final AdjustLoanTransactionCommand command) {

		command.setLoanId(loanId);
		command.setRepaymentId(repaymentId);

		LocalDate transactionDate = apiDataConversionService.convertFrom(
				command.getTransactionDateFormatted(),
				"transactionDateFormatted", command.getDateFormat());
		command.setTransactionDate(transactionDate);

		BigDecimal transactionAmount = apiDataConversionService.convertFrom(
				command.getTransactionAmountFormatted(),
				"transactionAmountFormatted", Locale.UK);
		command.setTransactionAmount(transactionAmount);

		EntityIdentifier identifier = this.loanWritePlatformService
				.adjustLoanTransaction(command);

		return Response.ok().entity(identifier).build();
	}
}