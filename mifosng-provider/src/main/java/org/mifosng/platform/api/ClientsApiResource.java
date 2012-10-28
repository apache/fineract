package org.mifosng.platform.api;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
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
import org.mifosng.platform.api.commands.ClientCommand;
import org.mifosng.platform.api.commands.NoteCommand;
import org.mifosng.platform.api.data.ClientAccountSummaryCollectionData;
import org.mifosng.platform.api.data.ClientData;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.api.data.NoteData;
import org.mifosng.platform.api.data.OfficeLookup;
import org.mifosng.platform.api.infrastructure.ApiDataConversionService;
import org.mifosng.platform.api.infrastructure.ApiJsonSerializerService;
import org.mifosng.platform.api.infrastructure.ApiParameterHelper;
import org.mifosng.platform.client.service.ClientReadPlatformService;
import org.mifosng.platform.client.service.ClientWritePlatformService;
import org.mifosng.platform.common.ApplicationConstants;
import org.mifosng.platform.common.FileUtils;
import org.mifosng.platform.exceptions.ClientNotFoundException;
import org.mifosng.platform.exceptions.ImageNotFoundException;
import org.mifosng.platform.organisation.service.OfficeReadPlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.lowagie.text.pdf.codec.Base64;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataParam;

@Path("/clients")
@Component
@Scope("singleton")
public class ClientsApiResource {

	private final static Logger logger = LoggerFactory.getLogger(ClientsApiResource.class);

	@Autowired
	private ClientReadPlatformService clientReadPlatformService;

	@Autowired
	private ClientWritePlatformService clientWritePlatformService;

	@Autowired
	private ApiDataConversionService apiDataConversionService;

	@Autowired
	private OfficeReadPlatformService officeReadPlatformService;
	
	@Autowired
	private ApiJsonSerializerService apiJsonSerializerService;

	private static final Set<String> typicalResponseParameters = new HashSet<String>(
			Arrays.asList("id", "officeId", "officeName", "externalId", "firstname", "lastname", "joinedDate", "displayName", "clientOrBusinessName", "imagePresent")
	);
	
	@GET
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retrieveAllIndividualClients(@Context final UriInfo uriInfo,
			@QueryParam("sqlSearch") final String sqlSearch,
			@QueryParam("officeId") final Integer officeId,
			@QueryParam("externalId") final String externalId,
			@QueryParam("displayName") final String displayName,
			@QueryParam("firstName") final String firstName,
			@QueryParam("lastName") final String lastName,
			@QueryParam("underHierarchy") final String hierarchy) {

		final String extraCriteria = getClientCriteria(sqlSearch, officeId, externalId, displayName, firstName, lastName, hierarchy);
		
		Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
		if (responseParameters.isEmpty()) {
			responseParameters.addAll(typicalResponseParameters);
		}
		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

		Collection<ClientData> clients = this.clientReadPlatformService.retrieveAllIndividualClients(extraCriteria);
		
		return this.apiJsonSerializerService.serializeClientDataToJson(prettyPrint, responseParameters, clients);
	}

	private String getClientCriteria(String sqlSearch, Integer officeId,
			String externalId, String displayName, String firstName,
			String lastName, String hierarchy) {

		String extraCriteria = "";

		if (sqlSearch != null) {
			extraCriteria = " and (" + sqlSearch + ")";
		}

		if (officeId != null) {
			extraCriteria += " and office_id = " + officeId;
		}
		
		if (externalId != null) {
			extraCriteria += " and external_id like " + ApiParameterHelper.sqlEncodeString(externalId);
		}
		
		if (displayName != null) {
			extraCriteria += " and concat(ifnull(firstname, ''), if(firstname > '',' ', '') , ifnull(lastname, '')) like "
					+ ApiParameterHelper.sqlEncodeString(displayName);
		}
		
		if (firstName != null) {
			extraCriteria += " and firstname like " + ApiParameterHelper.sqlEncodeString(firstName);
		}
		
		if (lastName != null) {
			extraCriteria += " and lastname like " + ApiParameterHelper.sqlEncodeString(lastName);
		}
		
		if (hierarchy != null) {
			extraCriteria += " and o.hierarchy like " + ApiParameterHelper.sqlEncodeString(hierarchy+"%");
		}

		if (StringUtils.isNotBlank(extraCriteria)) {
			extraCriteria = extraCriteria.substring(4);
		}

		logger.info("extraCriteria; " + extraCriteria);

		return extraCriteria;
	}

	@GET
	@Path("{clientId}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retrieveClientData(
			@PathParam("clientId") final Long clientId,
			@Context final UriInfo uriInfo) {
		
		Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
		if (responseParameters.isEmpty()) {
			responseParameters.addAll(typicalResponseParameters);
		}
		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
		boolean template = ApiParameterHelper.template(uriInfo.getQueryParameters());
		
		ClientData clientData = this.clientReadPlatformService.retrieveIndividualClient(clientId);
		if (template) {
			clientData.setAllowedOffices(new ArrayList<OfficeLookup>(officeReadPlatformService.retrieveAllOfficesForLookup()));
			responseParameters.add("allowedOffices");
		}

		return this.apiJsonSerializerService.serializeClientDataToJson(prettyPrint, responseParameters, clientData);
	}

	@GET
	@Path("template")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String newClientDetails(@Context final UriInfo uriInfo) {
		
		Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
		if (responseParameters.isEmpty()) {
			responseParameters.addAll(typicalResponseParameters);
			responseParameters.add("allowedOffices");
		}
		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
		
		ClientData clientData = this.clientReadPlatformService.retrieveNewClientDetails();
		
		return this.apiJsonSerializerService.serializeClientDataToJson(prettyPrint, responseParameters, clientData);
	}

	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response enrollClient(final String jsonRequestBody) {

		ClientCommand command = this.apiDataConversionService.convertJsonToClientCommand(null, jsonRequestBody);
		
		Long clientId = this.clientWritePlatformService.enrollClient(command);

		return Response.ok().entity(new EntityIdentifier(clientId)).build();
	}

	@PUT
	@Path("{clientId}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response updateClient(@PathParam("clientId") final Long clientId, final String jsonRequestBody) {

		ClientCommand command = this.apiDataConversionService.convertJsonToClientCommand(clientId, jsonRequestBody);

		EntityIdentifier identifier = this.clientWritePlatformService.updateClientDetails(command);

		return Response.ok().entity(identifier).build();
	}
	
	@DELETE
	@Path("{clientId}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response deleteClient(@PathParam("clientId") final Long clientId) {

		this.clientWritePlatformService.deleteClient(clientId);

		return Response.ok(new EntityIdentifier(clientId)).build();
	}

	@GET
	@Path("{clientId}/loans")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retrieveClientAccount(@PathParam("clientId") final Long clientId, 
										@Context final UriInfo uriInfo) {

		Set<String> typicalResponseParameters = new HashSet<String>(
				Arrays.asList("pendingApprovalLoans", "awaitingDisbursalLoans", "openLoans", "closedLoans", 
						"anyLoanCount", "pendingApprovalLoanCount", "awaitingDisbursalLoanCount", "activeLoanCount", "closedLoanCount",
						"pendingApprovalDespositAccountsCount", "pendingApprovalDespositAccounts", "approvedDespositAccountsCount", "approvedDespositAccounts",
						"withdrawnByClientDespositAccountsCount","withdrawnByClientDespositAccounts","closedDepositAccountsCount","closedDepositAccounts",
						"rejectedDepositAccountsCount","rejectedDepositAccounts","preclosedDepositAccountsCount","preclosedDepositAccounts")
		);
		
		Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
		if (responseParameters.isEmpty()) {
			responseParameters.addAll(typicalResponseParameters);
		}
		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
		
		ClientAccountSummaryCollectionData clientAccount = this.clientReadPlatformService.retrieveClientAccountDetails(clientId);
		
		return this.apiJsonSerializerService.serializeClientAccountSummaryCollectionDataToJson(prettyPrint, responseParameters, clientAccount);
	}

	@GET
	@Path("{clientId}/notes")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retrieveAllClientNotes(@PathParam("clientId") final Long clientId, @Context final UriInfo uriInfo) {

		Set<String> typicalResponseParameters = new HashSet<String>(
				Arrays.asList("id", "clientId", "loanId", "loanTransactionId", "noteType", "note", "createdById", "createdByUsername", 
						"createdOn", "updatedById", "updatedByUsername", "updatedOn")
		);
		
		Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
		if (responseParameters.isEmpty()) {
			responseParameters.addAll(typicalResponseParameters);
		}
		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
		
		Collection<NoteData> notes = this.clientReadPlatformService.retrieveAllClientNotes(clientId);
		
		return this.apiJsonSerializerService.serializeNoteDataToJson(prettyPrint, responseParameters, notes);
	}

	@POST
	@Path("{clientId}/notes")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response addNewClientNote(@PathParam("clientId") final Long clientId, final String jsonRequestBody) {

		NoteCommand command = this.apiDataConversionService.convertJsonToNoteCommand(null, clientId, jsonRequestBody);
		
		EntityIdentifier identifier = this.clientWritePlatformService.addClientNote(command);

		return Response.ok().entity(identifier).build();
	}

	@GET
	@Path("{clientId}/notes/{noteId}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retrieveClientNote(
			@PathParam("clientId") final Long clientId,
			@PathParam("noteId") final Long noteId, 
			@Context final UriInfo uriInfo) {
		
		Set<String> typicalResponseParameters = new HashSet<String>(
				Arrays.asList("id", "clientId", "loanId", "loanTransactionId", "noteType", "note", "createdById", "createdByUsername", 
						"createdOn", "updatedById", "updatedByUsername", "updatedOn")
		);
		
		Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
		if (responseParameters.isEmpty()) {
			responseParameters.addAll(typicalResponseParameters);
		}
		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
		
		NoteData note = this.clientReadPlatformService.retrieveClientNote(clientId, noteId);
		
		return this.apiJsonSerializerService.serializeNoteDataToJson(prettyPrint, responseParameters, note);
	}

	@PUT
	@Path("{clientId}/notes/{noteId}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response updateClientNote(
			@PathParam("clientId") final Long clientId,
			@PathParam("noteId") final Long noteId, final String jsonRequestBody) {

		NoteCommand command = this.apiDataConversionService.convertJsonToNoteCommand(noteId, clientId, jsonRequestBody);
		
		EntityIdentifier identifier = this.clientWritePlatformService.updateNote(command);

		return Response.ok().entity(identifier).build();
	}
	
	@POST
	@Path("{clientId}/image")
	@Consumes({ MediaType.MULTIPART_FORM_DATA })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response addNewClientImage(
			@PathParam("clientId") final Long clientId,
			@HeaderParam("Content-Length") Long fileSize,
			@FormDataParam("file") InputStream inputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetails,
			@FormDataParam("file") FormDataBodyPart bodyPart) {

		
		//TODO: vishwas might need more advances validation (like reading magic number) for handling malicious clients
		//and clients not setting mime type
		FileUtils.validateClientImageNotEmpty(fileDetails.getFileName());
		FileUtils.validateImageMimeType(bodyPart.getMediaType().toString());
		FileUtils.validateFileSizeWithinPermissibleRange(fileSize,
				fileDetails.getFileName(),
				ApplicationConstants.MAX_FILE_UPLOAD_SIZE_IN_MB);
		
		System.out.println( bodyPart.getMediaType().toString());

		EntityIdentifier entityIdentifier = this.clientWritePlatformService
				.saveOrUpdateClientImage(clientId, fileDetails.getFileName(),
						inputStream);

		// return identifier of the client whose image was updated
		return Response.ok().entity(entityIdentifier).build();

	}

	/**Returns a base 64 encoded client image
	 * @param clientId
	 * @return
	 */
	@GET
	@Path("{clientId}/image")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response retrieveClientImage(
			@PathParam("clientId") final Long clientId) {
		
		ClientData clientData = this.clientReadPlatformService.retrieveIndividualClient(clientId);
		//validate if client does not exist or image key is null
		if(clientData == null){
			throw new ClientNotFoundException(clientId);
		}
		else if(clientData.getImageKey()== null){
			throw new ImageNotFoundException(ApplicationConstants.IMAGE_MANAGEMENT_ENTITY.CLIENTS, clientId);
		}
		return Response.ok().entity(Base64.encodeFromFile(clientData.getImageKey())).build();
	}

	
	
	/** This method is added only for consistency with other URL patterns
	 *  and for maintaining consistency of usage of the HTTP "verb"
	 *  at the client side
	 * 
	 * @param clientId
	 * @param fileSize
	 * @param inputStream
	 * @param fileDetails
	 * @param bodyPart
	 * @return
	 */
	@PUT
	@Path("{clientId}/image")
	@Consumes({ MediaType.MULTIPART_FORM_DATA })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response updateClientImage(
			@PathParam("clientId") final Long clientId,
			@HeaderParam("Content-Length") Long fileSize,
			@FormDataParam("file") InputStream inputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetails,
			@FormDataParam("file") FormDataBodyPart bodyPart) {
		return addNewClientImage(clientId, fileSize, inputStream, fileDetails,
				bodyPart);
	}
	
	@DELETE
	@Path("{clientId}/image")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response deleteClientImage(@PathParam("clientId") final Long clientId) {
		this.clientWritePlatformService.deleteClientImage(clientId);
		return Response.ok(new EntityIdentifier(clientId)).build();
	}
}