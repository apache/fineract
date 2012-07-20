package org.mifosng.platform.api;

import java.util.Collection;

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
import org.mifosng.platform.api.commands.AdjustLoanTransactionCommand;
import org.mifosng.platform.api.commands.CalculateLoanScheduleCommand;
import org.mifosng.platform.api.commands.LoanStateTransitionCommand;
import org.mifosng.platform.api.commands.LoanTransactionCommand;
import org.mifosng.platform.api.commands.SubmitLoanApplicationCommand;
import org.mifosng.platform.api.commands.UndoStateTransitionCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.api.data.LoanAccountData;
import org.mifosng.platform.api.data.LoanAccountSummaryData;
import org.mifosng.platform.api.data.LoanBasicDetailsData;
import org.mifosng.platform.api.data.LoanRepaymentPeriodDatajpw;
import org.mifosng.platform.api.data.LoanSchedule;
import org.mifosng.platform.api.data.LoanTransactionData;
import org.mifosng.platform.api.data.LoanTransactionDatajpw;
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
	private String loanRepaymentDefaultFieldList = "transactionType,date,total";
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
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retrieveDetailsForNewLoanApplicationStepOne(
			@QueryParam("clientId") final Long clientId,
			@QueryParam("productId") final Long productId,
			@Context final UriInfo uriInfo) {

		NewLoanData workflowData = this.loanReadPlatformService.retrieveClientAndProductDetails(clientId, productId);

		String selectedFields = "";
		return this.jsonFormattingService.convertRequest(workflowData,
				filterName, allowedFieldList, selectedFields,
				uriInfo.getQueryParameters());
	}

	@GET
	@Path("{loanId}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retrieveLoanAccountDetails(
			@PathParam("loanId") final Long loanId, @Context final UriInfo uriInfo) {

		LoanBasicDetailsData loanBasicDetails = this.loanReadPlatformService
				.retrieveLoanAccountDetails(loanId);
		//LoanAccountData loanAccount = this.loanReadPlatformService.convertToData(loanBasicDetails);
		
		Collection<LoanRepaymentPeriodDatajpw> repaymentSchedule = this.loanReadPlatformService.retrieveRepaymentSchedule(loanId);
		
		LoanAccountSummaryData summary = this.loanReadPlatformService.retrieveSummary(loanBasicDetails.getPrincipal(), repaymentSchedule);
		
		Collection<LoanTransactionDatajpw> loanRepayments = this.loanReadPlatformService.retrieveLoanPayments(loanId);
		
		LoanAccountData loanAccount = new LoanAccountData(loanBasicDetails, summary, repaymentSchedule, loanRepayments, null );

		String selectedFields = "";
		String associatedFields = "summary,repaymentSchedule,loanRepayments,permissions,convenienceData";
		return this.jsonFormattingService.convertRequest(loanAccount,
				loanFilterName, loanAllowedFieldList, selectedFields,
				associatedFields, uriInfo.getQueryParameters());
	}

	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response calculateLoanScheduleOrSubmitLoanApplication(
			@QueryParam("command") final String commandParam,
			final String jsonRequestBody) {

		SubmitLoanApplicationCommand command = this.apiDataConversionService.convertJsonToSubmitLoanApplicationCommand(jsonRequestBody);
		
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

	@DELETE
	@Path("{loanId}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response deleteLoanApplication(@PathParam("loanId") final Long loanId) {

		EntityIdentifier identifier = this.loanWritePlatformService.deleteLoan(loanId);

		return Response.ok().entity(identifier).build();
	}

	@POST
	@Path("{loanId}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response stateTransitions(@PathParam("loanId") final Long loanId,
			@QueryParam("command") final String commandParam, final String jsonRequestBody) {

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

		UndoStateTransitionCommand undoCommand = new UndoStateTransitionCommand(loanId, command.getNote());
		
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
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response executeLoanTransaction(
			@PathParam("loanId") final Long loanId,
			@QueryParam("command") final String commandParam, final String jsonRequestBody) {

		final LoanTransactionCommand command = this.apiDataConversionService.convertJsonToLoanTransactionCommand(loanId, jsonRequestBody);
		
		Response response = null;

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
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retrieveNewRepaymentDetails(
			@PathParam("loanId") final Long loanId,
			@QueryParam("command") final String commandParam,
			@Context final UriInfo uriInfo) {

		String json = "";
		String selectedFields = loanRepaymentDefaultFieldList;
		if (is(commandParam, "repayment")) {
			LoanTransactionData loanRepaymentData = this.loanReadPlatformService
					.retrieveNewLoanRepaymentDetails(loanId);
			json = this.jsonFormattingService.convertRequest(loanRepaymentData,
					loanRepaymentFilterName, loanRepaymentAllowedFieldList,
					selectedFields, uriInfo.getQueryParameters());
		} else if (is(commandParam, "waiver")) {
			LoanTransactionData loanWaiverData = this.loanReadPlatformService
					.retrieveNewLoanWaiverDetails(loanId);
			json = this.jsonFormattingService.convertRequest(loanWaiverData,
					loanRepaymentFilterName, loanRepaymentAllowedFieldList,
					selectedFields, uriInfo.getQueryParameters());
		}

		if (StringUtils.isBlank(json)) {
			throw new UnrecognizedQueryParamException("command", commandParam);
		}

		return json;
	}

	@GET
	@Path("{loanId}/transactions/{transactionId}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retrieveTransaction(@PathParam("loanId") final Long loanId,
			@PathParam("transactionId") final Long transactionId,
			@Context final UriInfo uriInfo) {

		LoanTransactionData loanRepaymentData = this.loanReadPlatformService
				.retrieveLoanTransactionDetails(loanId, transactionId);

		String selectedFields = loanRepaymentDefaultFieldList;
		return this.jsonFormattingService.convertRequest(loanRepaymentData,
				loanRepaymentFilterName, loanRepaymentAllowedFieldList,
				selectedFields, uriInfo.getQueryParameters());
	}

	@POST
	@Path("{loanId}/transactions/{transactionId}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response adjustLoanTransaction(
			@PathParam("loanId") final Long loanId,
			@PathParam("transactionId") final Long transactionId, final String jsonRequestBody) {

		final AdjustLoanTransactionCommand command = this.apiDataConversionService.convertJsonToAdjustLoanTransactionCommand(loanId, transactionId, jsonRequestBody);
			
		EntityIdentifier identifier = this.loanWritePlatformService.adjustLoanTransaction(command);

		return Response.ok().entity(identifier).build();
	}
}