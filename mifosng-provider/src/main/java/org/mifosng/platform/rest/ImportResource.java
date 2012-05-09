package org.mifosng.platform.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mifosng.data.ErrorResponse;
import org.mifosng.data.command.ImportClientCommand;
import org.mifosng.data.command.ImportLoanCommand;
import org.mifosng.data.command.ImportLoanRepaymentsCommand;
import org.mifosng.platform.ImportPlatformService;
import org.mifosng.platform.exceptions.UnAuthenticatedUserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/protected/import")
@Component
@Scope("singleton")
public class ImportResource {

	@Autowired
	private ImportPlatformService importPlatformService;
	 
	@POST
	@Path("clients")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response importClients(final ImportClientCommand command) {
		try {
			this.importPlatformService.importClients(command);
			
			return Response.ok().build();
		} catch (UnAuthenticatedUserException e) {
			ErrorResponse err = new ErrorResponse("client.not.authenticated",
					"authentication", "");
			throw new WebApplicationException(Response
					.status(Status.UNAUTHORIZED).entity(err).build());
		}
	}
	
	@POST
	@Path("loans")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response importLoans(final ImportLoanCommand command) {
		try {
			this.importPlatformService.importLoans(command);
			
			return Response.ok().build();
		} catch (UnAuthenticatedUserException e) {
			ErrorResponse err = new ErrorResponse("client.not.authenticated",
					"authentication", "");
			throw new WebApplicationException(Response
					.status(Status.UNAUTHORIZED).entity(err).build());
		}
	}
	
	@POST
	@Path("loanrepayments")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response importLoanRepayments(final ImportLoanRepaymentsCommand command) {
		try {
			this.importPlatformService.importLoanRepayments(command);
			
			return Response.ok().build();
		} catch (UnAuthenticatedUserException e) {
			ErrorResponse err = new ErrorResponse("client.not.authenticated",
					"authentication", "");
			throw new WebApplicationException(Response
					.status(Status.UNAUTHORIZED).entity(err).build());
		}
	}
	
}