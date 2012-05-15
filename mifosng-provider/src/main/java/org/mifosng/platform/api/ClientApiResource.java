package org.mifosng.platform.api;

import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.mifosng.data.ClientData;
import org.mifosng.data.ClientList;
import org.mifosng.platform.WritePlatformService;
import org.mifosng.platform.client.service.ClientReadPlatformService;
import org.mifosng.platform.user.domain.AppUser;
import org.mifosng.platform.user.domain.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Path("/v1/clients")
@Component
@Scope("singleton")
public class ClientApiResource {
	
    @Autowired
   	private ClientReadPlatformService clientReadPlatformService;
    
    @Autowired
    private WritePlatformService writePlatformService;
    
	@Autowired
	private AppUserRepository appUserRepository;
	
	private void hardcodeUserIntoSecurityContext() {
		AppUser currentUser = this.appUserRepository.findOne(Long.valueOf(1));
    	
    	Authentication auth = new UsernamePasswordAuthenticationToken(currentUser, currentUser, currentUser.getAuthorities());
		SecurityContext context = SecurityContextHolder.getContext();
		context.setAuthentication(auth);
	}
	
	@GET
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON})
	public Response retrieveAllIndividualClients() {

		hardcodeUserIntoSecurityContext();

		Collection<ClientData> clients = this.clientReadPlatformService.retrieveAllIndividualClients();

		return Response.ok().entity(new ClientList(clients)).build();
	}
	
	@GET
	@Path("{clientId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON})
	public Response retrieveClientData(@PathParam("clientId") final Long clientId) {

		hardcodeUserIntoSecurityContext();

		ClientData clientData = this.clientReadPlatformService.retrieveIndividualClient(clientId);

		return Response.ok().entity(clientData).build();
	}
    
//	@GET
//	@Path("template")
//	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
//	@Produces({ MediaType.APPLICATION_JSON})
//	public Response newClientDetails() {
//
//		hardcodeUserIntoSecurityContext();
//
//		ClientData clientData = this.clientReadPlatformService.retrieveNewClientDetails();
//
//		return Response.ok().entity(clientData).build();
//	}
//
//	@POST
//	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//	@Produces({ MediaType.APPLICATION_JSON })
//	public Response enrollClient(final EnrollClientCommand command) {
//		
//		hardcodeUserIntoSecurityContext();
//
//		Long clientId = this.writePlatformService.enrollClient(command);
//
//		return Response.ok().entity(new EntityIdentifier(clientId)).build();
//	}
//	
//
//	@GET
//	@Path("{clientId}/withaccounts")
//	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//	@Produces({ MediaType.APPLICATION_JSON })
//	public Response retrieveClientAccount(@PathParam("clientId") final Long clientId) {
//
//		hardcodeUserIntoSecurityContext();
//		
//		ClientDataWithAccountsData clientAccount = this.clientReadPlatformService.retrieveClientAccountDetails(clientId);
//
//		return Response.ok().entity(clientAccount).build();
//	}
//	
//	@POST
//	@Path("{clientId}/note/new")
//	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
//	@Produces({ MediaType.APPLICATION_JSON})
//	public Response addNewClientNote(@PathParam("clientId") final Long clientId, final NoteCommand command) {
//		
//		hardcodeUserIntoSecurityContext();
//		
//		EntityIdentifier identifier = this.writePlatformService.addClientNote(command);
//
//		return Response.ok().entity(identifier).build();
//	}
//	
//	@GET
//	@Path("{clientId}/note/all")
//	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
//	@Produces({ MediaType.APPLICATION_JSON})
//	public Response retrieveAllClientNotes(@PathParam("clientId") final Long clientId) {
//
//		Collection<NoteData> notes = this.clientReadPlatformService.retrieveAllClientNotes(clientId);
//
//		return Response.ok().entity(new NoteDataList(notes)).build();
//	}
//	
//	@GET
//	@Path("{clientId}/note/{noteId}")
//	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//	@Produces({ MediaType.APPLICATION_JSON })
//	public Response retrieveClientNote(@PathParam("clientId") final Long clientId, @PathParam("noteId") final Long noteId) {
//
//		NoteData note = this.clientReadPlatformService.retrieveClientNote(clientId, noteId);
//
//		return Response.ok().entity(note).build();
//	}
}