package org.mifosng.platform.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.mifosng.platform.api.commands.ClientIdentifierCommand;
import org.mifosng.platform.api.data.ClientIdentifierData;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.api.infrastructure.ApiDataConversionService;
import org.mifosng.platform.api.infrastructure.ApiJsonSerializerService;
import org.mifosng.platform.api.infrastructure.ApiParameterHelper;
import org.mifosng.platform.client.service.ClientReadPlatformService;
import org.mifosng.platform.client.service.ClientWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/clients/{clientId}/identifiers")
@Component
@Scope("singleton")
public class ClientIdentifiersApiResource {

//	private final static Logger logger = LoggerFactory.getLogger(ClientIdentifiersApiResource.class);

	@Autowired
	private ClientReadPlatformService clientReadPlatformService;

	@Autowired
	private ClientWritePlatformService clientWritePlatformService;

	@Autowired
	private ApiDataConversionService apiDataConversionService;

	@Autowired
	private ApiJsonSerializerService apiJsonSerializerService;

	private static final Set<String> typicalResponseParameters = new HashSet<String>(
			Arrays.asList("id", "clientId", "documentTypeId", "description",
					"documentKey", "documentTypeName"));

	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAllClientIdentifiers(@Context final UriInfo uriInfo,
			@PathParam("clientId") final Long clientId) {

		Set<String> responseParameters = ApiParameterHelper
				.extractFieldsForResponseIfProvided(uriInfo
						.getQueryParameters());
		if (responseParameters.isEmpty()) {
			responseParameters.addAll(typicalResponseParameters);
		}
		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo
				.getQueryParameters());

		Collection<ClientIdentifierData> clientIdentifiers = this.clientReadPlatformService
				.retrieveClientIdentifiers(clientId);

		return this.apiJsonSerializerService
				.serializeClientIdentifierDataToJson(prettyPrint,
						responseParameters, clientIdentifiers);
	}

	@GET
	@Path("template")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String newClientDetails(@Context final UriInfo uriInfo) {

		Set<String> responseParameters = ApiParameterHelper
				.extractFieldsForResponseIfProvided(uriInfo
						.getQueryParameters());
		responseParameters.addAll(typicalResponseParameters);
		responseParameters.add("allowedDocumentTypes");
		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo
				.getQueryParameters());

		ClientIdentifierData clientIdentifierData = this.clientReadPlatformService
				.retrieveNewClientIdentifierDetails();

		return this.apiJsonSerializerService
				.serializeClientIdentifierDataToJson(prettyPrint,
						responseParameters, clientIdentifierData);
	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response createClientIdentifier(
			@PathParam("clientId") final Long clientId,
			final String jsonRequestBody) {

		ClientIdentifierCommand command = this.apiDataConversionService
				.convertJsonToClientIdentifierCommand(null, clientId,
						jsonRequestBody);

		Long clientIdentifierId = this.clientWritePlatformService
				.addClientIdentifier(command);

		return Response.ok().entity(new EntityIdentifier(clientIdentifierId))
				.build();
	}

	@GET
	@Path("{identifierId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String getClientIdentifier(
			@PathParam("clientId") final Long clientId,
			@PathParam("identifierId") final Long clientIdentifierId,
			@Context final UriInfo uriInfo) {

		Set<String> responseParameters = ApiParameterHelper
				.extractFieldsForResponseIfProvided(uriInfo
						.getQueryParameters());
		if (responseParameters.isEmpty()) {
			responseParameters.addAll(typicalResponseParameters);
		}

		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo
				.getQueryParameters());
		boolean template = ApiParameterHelper.template(uriInfo
				.getQueryParameters());

		if (template) {
			responseParameters.add("allowedDocumentTypes");
		}

		ClientIdentifierData clientIdentifierData = this.clientReadPlatformService
				.retrieveClientIdentifier(clientId, clientIdentifierId);

		return this.apiJsonSerializerService
				.serializeClientIdentifierDataToJson(prettyPrint,
						responseParameters, clientIdentifierData);
	}

	@PUT
	@Path("{identifierId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response updateClientIdentifer(
			@PathParam("clientId") final Long clientId,
			@PathParam("identifierId") final Long clientIdentifierId,
			final String jsonRequestBody) {

		ClientIdentifierCommand command = this.apiDataConversionService
				.convertJsonToClientIdentifierCommand(clientIdentifierId,
						clientId, jsonRequestBody);

		EntityIdentifier identifier = this.clientWritePlatformService
				.updateClientIdentifier(command);

		return Response.ok().entity(identifier).build();
	}

	@DELETE
	@Path("{identifierId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response deleteClientIdentifier(
//			@PathParam("clientId") final Long clientId,
			@PathParam("identifierId") final Long clientIdentifierId) {

		this.clientWritePlatformService
				.deleteClientIdentifier(clientIdentifierId);

		return Response.ok(new EntityIdentifier(clientIdentifierId)).build();
	}

}