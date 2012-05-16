package org.mifosng.platform.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.mifosng.data.LoanAccountData;
import org.mifosng.data.NewLoanWorkflowStepOneData;
import org.mifosng.platform.ReadPlatformService;
import org.mifosng.platform.loan.service.LoanReadPlatformService;
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
   	private ReadPlatformService readPlatformService;
    
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
}