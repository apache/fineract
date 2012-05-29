package org.mifosng.platform.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.mifosng.data.command.ImportLoanCommand;
import org.mifosng.data.command.ImportLoanRepaymentsCommand;
import org.mifosng.platform.ImportPlatformService;
import org.mifosng.platform.api.commands.ImportClientCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/protected/import/trigger")
@Component
@Scope("singleton")
public class ImportTriggerResource {

	@Autowired
	private ImportPlatformService importPlatformService;
	
	@Autowired
	private ImportResource importResource;
	
	// TODO - REMOVE THIS HARDCODED IMPORT OF CREOCORE CSV FILE
	@GET
	@Path("clients/creocore")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response triggerCreoCoreClientImport() {
		
		ImportClientCommand command = this.importPlatformService.populateClientImportFromCsv();
		return importResource.importClients(command);
	}
	
	// TODO - REMOVE THIS HARDCODED IMPORT OF CREOCORE CSV FILE
	@GET
	@Path("loans/creocore")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response triggerCreoCoreLoanImport() {

		ImportLoanCommand command = this.importPlatformService.populateLoanImportFromCsv();
		return importResource.importLoans(command);
	}
	
	@GET
	@Path("repayments/creocore")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response triggerCreoCoreLoanRepaymentsImport() {

		ImportLoanRepaymentsCommand command = this.importPlatformService.populateLoanRepaymentsImportFromCsv();
		return importResource.importLoanRepayments(command);
	}
}