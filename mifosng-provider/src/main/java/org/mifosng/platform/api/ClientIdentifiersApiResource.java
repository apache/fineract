package org.mifosng.platform.api;

import java.util.Collection;
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
import org.mifosng.platform.api.data.ClientData;
import org.mifosng.platform.api.data.ClientIdentifierData;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.api.infrastructure.PortfolioApiDataConversionService;
import org.mifosng.platform.api.infrastructure.PortfolioApiJsonSerializerService;
import org.mifosng.platform.client.service.ClientReadPlatformService;
import org.mifosng.platform.client.service.ClientWritePlatformService;
import org.mifosng.platform.exceptions.DuplicateClientIdentifierException;
import org.mifosng.platform.infrastructure.api.ApiParameterHelper;
import org.mifosplatform.infrastructure.codes.data.CodeValueData;
import org.mifosplatform.infrastructure.codes.service.CodeValueReadPlatformService;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/clients/{clientId}/identifiers")
@Component
@Scope("singleton")
public class ClientIdentifiersApiResource {

	@Autowired
	private ClientReadPlatformService clientReadPlatformService;
	
	@Autowired
	private CodeValueReadPlatformService codeValueReadPlatformService;

	@Autowired
	private ClientWritePlatformService clientWritePlatformService;

	@Autowired
	private PortfolioApiDataConversionService apiDataConversionService;

	@Autowired
	private PortfolioApiJsonSerializerService apiJsonSerializerService;

	@Autowired
    private PlatformSecurityContext context;
	
	private final String entityType = "CLIENTIDENTIFIER";
	
	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAllClientIdentifiers(
			@Context final UriInfo uriInfo,
			@PathParam("clientId") final Long clientId) {

		context.authenticatedUser().validateHasReadPermission(entityType);
		
		final Set<String> responseParameters = ApiParameterHelper
				.extractFieldsForResponseIfProvided(uriInfo
						.getQueryParameters());
		final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

		final Collection<ClientIdentifierData> clientIdentifiers = this.clientReadPlatformService.retrieveClientIdentifiers(clientId);

		return this.apiJsonSerializerService
				.serializeClientIdentifierDataToJson(prettyPrint,
						responseParameters, clientIdentifiers);
	}

	@GET
	@Path("template")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String newClientDetails(@Context final UriInfo uriInfo) {

		context.authenticatedUser().validateHasReadPermission(entityType);
		
		final Set<String> responseParameters = ApiParameterHelper
				.extractFieldsForResponseIfProvided(uriInfo
						.getQueryParameters());
		final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

		Collection<CodeValueData> codeValues = codeValueReadPlatformService.retrieveCustomIdentifierCodeValues();
		ClientIdentifierData clientIdentifierData = ClientIdentifierData.template(codeValues);

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

		try {
			final ClientIdentifierCommand command = this.apiDataConversionService.convertJsonToClientIdentifierCommand(null, clientId, jsonRequestBody);
			
			Long clientIdentifierId = this.clientWritePlatformService.addClientIdentifier(command);
			
			return Response.ok().entity(new EntityIdentifier(clientIdentifierId)).build();
		}
		catch (DuplicateClientIdentifierException e) {
			DuplicateClientIdentifierException rethrowas = e;
			if (e.getDocumentTypeId() != null) {
				// need to fetch client info
				ClientData clientInfo = this.clientReadPlatformService.retrieveClientByIdentifier(e.getDocumentTypeId(), e.getIdentifierKey());
				rethrowas = new DuplicateClientIdentifierException(clientInfo.displayName(), clientInfo.officeName(), e.getIdentifierType(), e.getIdentifierKey());
			}
			throw rethrowas;
		}
	}

	@GET
	@Path("{identifierId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String getClientIdentifier(
			@PathParam("clientId") final Long clientId,
			@PathParam("identifierId") final Long clientIdentifierId,
			@Context final UriInfo uriInfo) {

		context.authenticatedUser().validateHasReadPermission(entityType);
		
		final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
		final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
		final boolean template = ApiParameterHelper.template(uriInfo.getQueryParameters());

		ClientIdentifierData clientIdentifierData = this.clientReadPlatformService.retrieveClientIdentifier(clientId, clientIdentifierId);
		if (template) {
			final Collection<CodeValueData> codeValues = codeValueReadPlatformService.retrieveCustomIdentifierCodeValues();
			clientIdentifierData = ClientIdentifierData.template(clientIdentifierData, codeValues);
		}

		return this.apiJsonSerializerService
				.serializeClientIdentifierDataToJson(prettyPrint,
						responseParameters, clientIdentifierData);
	}

	@PUT
	@Path("{identifierId}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response updateClientIdentifer(
			@PathParam("clientId") final Long clientId,
			@PathParam("identifierId") final Long clientIdentifierId,
			final String jsonRequestBody) {

		try {
			final ClientIdentifierCommand command = this.apiDataConversionService.convertJsonToClientIdentifierCommand(clientIdentifierId, clientId, jsonRequestBody);

			final EntityIdentifier identifier = this.clientWritePlatformService.updateClientIdentifier(command);

			return Response.ok().entity(identifier).build();
		}
		catch (DuplicateClientIdentifierException e) {
			DuplicateClientIdentifierException reThrowAs = e;
			if (e.getDocumentTypeId() != null) {
				ClientData clientInfo = this.clientReadPlatformService.retrieveClientByIdentifier(e.getDocumentTypeId(), e.getIdentifierKey());
				reThrowAs = new DuplicateClientIdentifierException(clientInfo.displayName(), clientInfo.officeName(), e.getIdentifierType(), e.getIdentifierKey());
			}
			throw reThrowAs;
		}
	}

	@DELETE
	@Path("{identifierId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response deleteClientIdentifier(
			@PathParam("identifierId") final Long clientIdentifierId) {

		this.clientWritePlatformService.deleteClientIdentifier(clientIdentifierId);

		return Response.ok(new EntityIdentifier(clientIdentifierId)).build();
	}
}