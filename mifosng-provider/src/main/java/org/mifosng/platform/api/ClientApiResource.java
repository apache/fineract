package org.mifosng.platform.api;

import java.util.ArrayList;
import java.util.Collection;

import javax.ws.rs.Consumes;
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

import org.joda.time.LocalDate;
import org.mifosng.platform.api.commands.ClientCommand;
import org.mifosng.platform.api.commands.NoteCommand;
import org.mifosng.platform.api.data.ClientData;
import org.mifosng.platform.api.data.ClientLoanAccountSummaryCollectionData;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.api.data.NoteData;
import org.mifosng.platform.api.data.OfficeLookup;
import org.mifosng.platform.api.infrastructure.ApiDataConversionService;
import org.mifosng.platform.api.infrastructure.ApiJSONFormattingService;
import org.mifosng.platform.client.service.ClientReadPlatformService;
import org.mifosng.platform.client.service.ClientWritePlatformService;
import org.mifosng.platform.organisation.service.OfficeReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/clients")
@Component
@Scope("singleton")
public class ClientApiResource {

	private String defaultFieldList = "joinedDate";
	private String allowedFieldList = "allowedOffices";
	private String filterName = "myFilter";

	@Autowired
	private ClientReadPlatformService clientReadPlatformService;

	@Autowired
	private ClientWritePlatformService clientWritePlatformService;

	@Autowired
	private ApiDataConversionService apiDataConversionService;

	@Autowired
	private OfficeReadPlatformService officeReadPlatformService;

	@Autowired
	private ApiJSONFormattingService jsonFormattingService;

	@GET
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAllIndividualClients(@Context UriInfo uriInfo) {

		Collection<ClientData> clients = this.clientReadPlatformService
				.retrieveAllIndividualClients();

		String selectedFields = "";
		return this.jsonFormattingService.convertRequest(clients, filterName,
				allowedFieldList, selectedFields, uriInfo.getQueryParameters());
	}

	@GET
	@Path("{clientId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveClientData(
			@PathParam("clientId") final Long clientId,
			@QueryParam("template") String template, @Context UriInfo uriInfo) {

		ClientData clientData = this.clientReadPlatformService
				.retrieveIndividualClient(clientId);

		if (template != null && template.equalsIgnoreCase("true")) {
			clientData.setAllowedOffices(new ArrayList<OfficeLookup>(
					officeReadPlatformService.retrieveAllOfficesForLookup()));
		}

		String selectedFields = "";
		return this.jsonFormattingService.convertRequest(clientData,
				filterName, allowedFieldList, selectedFields,
				uriInfo.getQueryParameters());
	}

	@GET
	@Path("template")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON })
	public String newClientDetails(@Context UriInfo uriInfo) {

		ClientData clientData = this.clientReadPlatformService
				.retrieveNewClientDetails();

		String selectedFields = defaultFieldList + "," + allowedFieldList;
		return this.jsonFormattingService.convertRequest(clientData,
				filterName, allowedFieldList, selectedFields,
				uriInfo.getQueryParameters());
	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response enrollClient(final ClientCommand command) {

		LocalDate joiningLocalDate = apiDataConversionService.convertFrom(
				command.getJoiningDate(), "joiningDate",
				command.getDateFormat());
		command.setJoiningLocalDate(joiningLocalDate);

		Long clientId = this.clientWritePlatformService.enrollClient(command);

		return Response.ok().entity(new EntityIdentifier(clientId)).build();
	}

	@PUT
	@Path("{clientId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response updateClient(@PathParam("clientId") final Long clientId,
			final ClientCommand command) {

		LocalDate joiningLocalDate = apiDataConversionService.convertFrom(
				command.getJoiningDate(), "joiningDate",
				command.getDateFormat());
		command.setJoiningLocalDate(joiningLocalDate);
		command.setId(clientId);

		EntityIdentifier identifier = this.clientWritePlatformService
				.updateClientDetails(command);

		return Response.ok().entity(identifier).build();
	}

	@GET
	@Path("{clientId}/loans")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveClientAccount(
			@PathParam("clientId") final Long clientId, @Context UriInfo uriInfo) {

		ClientLoanAccountSummaryCollectionData clientAccount = this.clientReadPlatformService
				.retrieveClientAccountDetails(clientId);

		String selectedFields = "";
		return this.jsonFormattingService.convertRequest(clientAccount,
				filterName, allowedFieldList, selectedFields,
				uriInfo.getQueryParameters());
	}

	@GET
	@Path("{clientId}/notes")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAllClientNotes(
			@PathParam("clientId") final Long clientId, @Context UriInfo uriInfo) {

		Collection<NoteData> notes = this.clientReadPlatformService
				.retrieveAllClientNotes(clientId);

		String selectedFields = "";
		return this.jsonFormattingService.convertRequest(notes, filterName,
				allowedFieldList, selectedFields, uriInfo.getQueryParameters());
	}

	@POST
	@Path("{clientId}/notes")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response addNewClientNote(
			@PathParam("clientId") final Long clientId,
			final NoteCommand command) {

		command.setClientId(clientId);

		EntityIdentifier identifier = this.clientWritePlatformService
				.addClientNote(command);

		return Response.ok().entity(identifier).build();
	}

	@GET
	@Path("{clientId}/notes/{noteId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveClientNote(
			@PathParam("clientId") final Long clientId,
			@PathParam("noteId") final Long noteId, @Context UriInfo uriInfo) {

		NoteData note = this.clientReadPlatformService.retrieveClientNote(
				clientId, noteId);

		String selectedFields = "";
		return this.jsonFormattingService.convertRequest(note, filterName,
				allowedFieldList, selectedFields, uriInfo.getQueryParameters());
	}

	@PUT
	@Path("{clientId}/notes/{noteId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response updateClientNote(
			@PathParam("clientId") final Long clientId,
			@PathParam("noteId") final Long noteId, final NoteCommand command) {

		command.setClientId(clientId);
		command.setId(noteId);

		EntityIdentifier identifier = this.clientWritePlatformService
				.updateNote(command);

		return Response.ok().entity(identifier).build();
	}
}