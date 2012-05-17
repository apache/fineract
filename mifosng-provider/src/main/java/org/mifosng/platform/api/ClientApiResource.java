package org.mifosng.platform.api;

import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.joda.time.LocalDate;
import org.mifosng.data.ClientData;
import org.mifosng.data.ClientList;
import org.mifosng.data.ClientLoanAccountSummaryCollectionData;
import org.mifosng.data.EntityIdentifier;
import org.mifosng.data.NoteData;
import org.mifosng.data.NoteDataList;
import org.mifosng.data.command.EnrollClientCommand;
import org.mifosng.data.command.NoteCommand;
import org.mifosng.platform.api.infrastructure.ApiDataConversionService;
import org.mifosng.platform.client.service.ClientReadPlatformService;
import org.mifosng.platform.client.service.ClientWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/v1/clients")
@Component
@Scope("singleton")
public class ClientApiResource {
	
    @Autowired
   	private ClientReadPlatformService clientReadPlatformService;
    
    @Autowired
    private ClientWritePlatformService clientWritePlatformService;
    
    @Autowired
    private ApiDataConversionService apiDataConversionService;
    
	@GET
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON})
	public Response retrieveAllIndividualClients() {

		Collection<ClientData> clients = this.clientReadPlatformService.retrieveAllIndividualClients();

		return Response.ok().entity(new ClientList(clients)).build();
	}
	
	@GET
	@Path("{clientId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON})
	public Response retrieveClientData(@PathParam("clientId") final Long clientId) {

		ClientData clientData = this.clientReadPlatformService.retrieveIndividualClient(clientId);

		return Response.ok().entity(clientData).build();
	}
    
	@GET
	@Path("template")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON})
	public Response newClientDetails() {

		ClientData clientData = this.clientReadPlatformService.retrieveNewClientDetails();

		return Response.ok().entity(clientData).build();
	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response enrollClient(final EnrollClientCommand command) {
		
		LocalDate joiningDate = apiDataConversionService.convertFrom(command.getJoiningDateFormatted(), "joiningDateFormatted", command.getDateFormat());
		command.setJoiningDate(joiningDate);
		
		Long clientId = this.clientWritePlatformService.enrollClient(command);

		return Response.ok().entity(new EntityIdentifier(clientId)).build();
	}
	

	@GET
	@Path("{clientId}/loans")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response retrieveClientAccount(@PathParam("clientId") final Long clientId) {

		ClientLoanAccountSummaryCollectionData clientAccount = this.clientReadPlatformService.retrieveClientAccountDetails(clientId);

		return Response.ok().entity(clientAccount).build();
	}

	@GET
	@Path("{clientId}/notes")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON})
	public Response retrieveAllClientNotes(@PathParam("clientId") final Long clientId) {

		Collection<NoteData> notes = this.clientReadPlatformService.retrieveAllClientNotes(clientId);

		return Response.ok().entity(new NoteDataList(notes)).build();
	}
	
	@POST
	@Path("{clientId}/notes")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON})
	public Response addNewClientNote(@PathParam("clientId") final Long clientId, final NoteCommand command) {
		
		command.setClientId(clientId);
		
		EntityIdentifier identifier = this.clientWritePlatformService.addClientNote(command);

		return Response.ok().entity(identifier).build();
	}
	
	@GET
	@Path("{clientId}/notes/{noteId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response retrieveClientNote(@PathParam("clientId") final Long clientId, @PathParam("noteId") final Long noteId) {

		NoteData note = this.clientReadPlatformService.retrieveClientNote(clientId, noteId);

		return Response.ok().entity(note).build();
	}
	
	@PUT
	@Path("{clientId}/notes/{noteId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response updateClientNote(@PathParam("clientId") final Long clientId, @PathParam("noteId") final Long noteId, final NoteCommand command) {

		command.setClientId(clientId);
		command.setId(noteId);
		
		EntityIdentifier identifier = this.clientWritePlatformService.updateNote(command);

		return Response.ok().entity(identifier).build();
	}
}