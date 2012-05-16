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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosng.data.EntityIdentifier;
import org.mifosng.data.LoanAccountData;
import org.mifosng.data.LoanSchedule;
import org.mifosng.data.NewLoanWorkflowStepOneData;
import org.mifosng.data.command.CalculateLoanScheduleCommand;
import org.mifosng.data.command.SubmitLoanApplicationCommand;
import org.mifosng.platform.ReadPlatformService;
import org.mifosng.platform.WritePlatformService;
import org.mifosng.platform.api.infrastructure.ApiDataConversionService;
import org.mifosng.platform.loan.service.CalculationPlatformService;
import org.mifosng.platform.loan.service.LoanReadPlatformService;
import org.mifosng.platform.loan.service.LoanWritePlatformService;
import org.mifosng.platform.user.domain.AppUser;
import org.mifosng.platform.user.domain.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Path("/v1/loans")
@Component
@Scope("singleton")
public class LoansApiResource {
	
    @Autowired
   	private LoanReadPlatformService loanReadPlatformService;
    
    @Autowired
   	private LoanWritePlatformService loanWritePlatformService;
    
    @Autowired
   	private ReadPlatformService readPlatformService;
    
    @Autowired
   	private WritePlatformService writePlatformService;
    
    @Autowired
	private CalculationPlatformService calculationPlatformService;
    
    @Autowired
    private ApiDataConversionService apiDataConversionService;
    
	@Autowired
	private AppUserRepository appUserRepository;
	
	private void hardcodeUserIntoSecurityContext() {
		AppUser currentUser = this.appUserRepository.findOne(Long.valueOf(1));
    	
    	Authentication auth = new UsernamePasswordAuthenticationToken(currentUser, currentUser, currentUser.getAuthorities());
		SecurityContext context = SecurityContextHolder.getContext();
		context.setAuthentication(auth);
	}
	
	@GET
	@Path("template")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON})
	public Response retrieveDetailsForNewLoanApplicationStepOne(@QueryParam("clientId") final Long clientId, @QueryParam("productId") final Long productId) {

		hardcodeUserIntoSecurityContext();
		
		NewLoanWorkflowStepOneData workflowData = this.loanReadPlatformService.retrieveClientAndProductDetails(clientId, productId);

		return Response.ok().entity(workflowData).build();
	}
	
	@GET
	@Path("{loanId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON})
	public Response retrieveLoanAccountDetails(@PathParam("loanId") final Long loanId) {

		hardcodeUserIntoSecurityContext();
		
		LoanAccountData loanAccount = this.loanReadPlatformService.retrieveLoanAccountDetails(loanId);

		return Response.ok().entity(loanAccount).build();
	}
	
	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON})
	public Response calculateLoanSchedule(@QueryParam("command") final String commandParam, final SubmitLoanApplicationCommand command) {

		hardcodeUserIntoSecurityContext();
		
		LocalDate expectedDisbursementDate = apiDataConversionService.convertFrom(command.getExpectedDisbursementDateFormatted(), "expectedDisbursementDateFormatted", command.getDateFormat());
		LocalDate repaymentsStartingFromDate = apiDataConversionService.convertFrom(command.getRepaymentsStartingFromDateFormatted(), "repaymentsStartingFromDateFormatted", command.getDateFormat());
		LocalDate interestCalculatedFromDate = apiDataConversionService.convertFrom(command.getInterestCalculatedFromDateFormatted(), "interestCalculatedFromDateFormatted", command.getDateFormat());
		LocalDate submittedOnDate = apiDataConversionService.convertFrom(command.getSubmittedOnDateFormatted(), "submittedOnDateFormatted", command.getDateFormat());
		command.setExpectedDisbursementDate(expectedDisbursementDate);
		command.setRepaymentsStartingFromDate(repaymentsStartingFromDate);
		command.setInterestCalculatedFromDate(interestCalculatedFromDate);
		command.setSubmittedOnDate(submittedOnDate);
		
		// FIXME - pass in locale through query string or in 'request body'
		Locale clientApplicationLocale = Locale.UK;
		BigDecimal principal = this.apiDataConversionService.convertFrom(command.getPrincipalFormatted(), "principalFormatted", clientApplicationLocale);
		BigDecimal interestRatePerPeriod = this.apiDataConversionService.convertFrom(command.getInterestRatePerPeriodFormatted(), "interestRatePerPeriodFormatted", clientApplicationLocale);
		BigDecimal inArrearsToleranceAmount = this.apiDataConversionService.convertFrom(command.getInArrearsToleranceAmountFormatted(), "inArrearsToleranceAmountFormatted", clientApplicationLocale);
		command.setPrincipal(principal);
		command.setInterestRatePerPeriod(interestRatePerPeriod);
		command.setInArrearsToleranceAmount(inArrearsToleranceAmount);

		CalculateLoanScheduleCommand calculateLoanScheduleCommand = command.toCalculateLoanScheduleCommand();
		LoanSchedule loanSchedule = this.calculationPlatformService.calculateLoanSchedule(calculateLoanScheduleCommand);
		
		// for now just auto generating the loan schedule and setting support for 'manual' loan schedule creation later.
		command.setLoanSchedule(loanSchedule);
		
		if (StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase("calculateLoanSchedule")) {

			return Response.ok().entity(loanSchedule).build();	
		}
		
		EntityIdentifier identifier = this.loanWritePlatformService.submitLoanApplication(command);
		
		return Response.ok().entity(identifier).build();
    }
	
	@DELETE
	@Path("{loanId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON})
	public Response deleteLoanApplication(@PathParam("loanId") final Long loanId) {

		hardcodeUserIntoSecurityContext();
		
		EntityIdentifier identifier = this.loanWritePlatformService.deleteLoan(loanId);

		return Response.ok().entity(identifier).build();
	}
}