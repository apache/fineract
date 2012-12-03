package org.mifosplatform.portfolio.client.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
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
import javax.ws.rs.core.UriInfo;

import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.codes.data.CodeValueData;
import org.mifosplatform.infrastructure.codes.service.CodeValueReadPlatformService;
import org.mifosplatform.infrastructure.core.api.ApiParameterHelper;
import org.mifosplatform.infrastructure.core.api.PortfolioApiDataConversionService;
import org.mifosplatform.infrastructure.core.api.PortfolioApiJsonSerializerService;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;
import org.mifosplatform.infrastructure.core.serialization.CommandSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.client.command.ClientIdentifierCommand;
import org.mifosplatform.portfolio.client.data.ClientData;
import org.mifosplatform.portfolio.client.data.ClientIdentifierData;
import org.mifosplatform.portfolio.client.exception.DuplicateClientIdentifierException;
import org.mifosplatform.portfolio.client.service.ClientReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/clients/{clientId}/identifiers")
@Component
@Scope("singleton")
public class ClientIdentifiersApiResource {

    private final String entityType = "CLIENTIDENTIFIER";

    private final PlatformSecurityContext context;
    private final ClientReadPlatformService clientReadPlatformService;
    private final CodeValueReadPlatformService codeValueReadPlatformService;
    private final PortfolioApiDataConversionService apiDataConversionService;
    private final CommandSerializer commandSerializerService;
    private final PortfolioApiJsonSerializerService apiJsonSerializerService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public ClientIdentifiersApiResource(final PlatformSecurityContext context, final ClientReadPlatformService readPlatformService,
            final CodeValueReadPlatformService codeValueReadPlatformService,
            final PortfolioApiDataConversionService apiDataConversionService,
            final CommandSerializer commandSerializerService,
            final PortfolioApiJsonSerializerService apiJsonSerializerService,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.context = context;
        this.clientReadPlatformService = readPlatformService;
        this.codeValueReadPlatformService = codeValueReadPlatformService;
        this.apiDataConversionService = apiDataConversionService;
        this.commandSerializerService = commandSerializerService;
        this.apiJsonSerializerService = apiJsonSerializerService;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAllClientIdentifiers(@Context final UriInfo uriInfo, @PathParam("clientId") final Long clientId) {

        context.authenticatedUser().validateHasReadPermission(entityType);

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        final Collection<ClientIdentifierData> clientIdentifiers = this.clientReadPlatformService.retrieveClientIdentifiers(clientId);

        return this.apiJsonSerializerService.serializeClientIdentifierDataToJson(prettyPrint, responseParameters, clientIdentifiers);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String newClientDetails(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(entityType);

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        Collection<CodeValueData> codeValues = codeValueReadPlatformService.retrieveCustomIdentifierCodeValues();
        ClientIdentifierData clientIdentifierData = ClientIdentifierData.template(codeValues);

        return this.apiJsonSerializerService.serializeClientIdentifierDataToJson(prettyPrint, responseParameters, clientIdentifierData);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createClientIdentifier(@PathParam("clientId") final Long clientId, final String apiRequestBodyAsJson) {

        try {
            final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "PORTFOLIO_MANAGEMENT_SUPER_USER",
                    "CREATE_CLIENTIDENTIFIER");
            context.authenticatedUser().validateHasPermissionTo("CREATE_CLIENTIDENTIFIER", allowedPermissions);

            final ClientIdentifierCommand command = this.apiDataConversionService.convertApiRequestJsonToClientIdentifierCommand(null,
                    clientId, apiRequestBodyAsJson);
            final String commandSerializedAsJson = this.commandSerializerService.serializeCommandToJson(command);

            final String resourceUrl = "clients/" + clientId + "/identifiers";
            final EntityIdentifier result = this.commandsSourceWritePlatformService.logCommandSource("CREATE", resourceUrl, null,
                    commandSerializedAsJson);

            return this.apiJsonSerializerService.serializeEntityIdentifier(result);
        } catch (DuplicateClientIdentifierException e) {
            DuplicateClientIdentifierException rethrowas = e;
            if (e.getDocumentTypeId() != null) {
                // need to fetch client info
                ClientData clientInfo = this.clientReadPlatformService.retrieveClientByIdentifier(e.getDocumentTypeId(),
                        e.getIdentifierKey());
                rethrowas = new DuplicateClientIdentifierException(clientInfo.displayName(), clientInfo.officeName(),
                        e.getIdentifierType(), e.getIdentifierKey());
            }
            throw rethrowas;
        }
    }

    @GET
    @Path("{identifierId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String getClientIdentifier(@PathParam("clientId") final Long clientId, @PathParam("identifierId") final Long clientIdentifierId,
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

        return this.apiJsonSerializerService.serializeClientIdentifierDataToJson(prettyPrint, responseParameters, clientIdentifierData);
    }

    @PUT
    @Path("{identifierId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateClientIdentifer(@PathParam("clientId") final Long clientId,
            @PathParam("identifierId") final Long clientIdentifierId, final String apiRequestBodyAsJson) {

        try {
            final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "PORTFOLIO_MANAGEMENT_SUPER_USER",
                    "UPDATE_CLIENTIDENTIFIER");
            context.authenticatedUser().validateHasPermissionTo("UPDATE_CLIENTIDENTIFIER", allowedPermissions);

            final ClientIdentifierCommand command = this.apiDataConversionService.convertApiRequestJsonToClientIdentifierCommand(
                    clientIdentifierId, clientId, apiRequestBodyAsJson);
            final String commandSerializedAsJson = this.commandSerializerService.serializeCommandToJson(command);

            final String resourceUrl = "clients/" + clientId + "/identifiers";
            final EntityIdentifier result = this.commandsSourceWritePlatformService.logCommandSource("UPDATE", resourceUrl,
                    clientIdentifierId, commandSerializedAsJson);

            return this.apiJsonSerializerService.serializeEntityIdentifier(result);
        } catch (DuplicateClientIdentifierException e) {
            DuplicateClientIdentifierException reThrowAs = e;
            if (e.getDocumentTypeId() != null) {
                ClientData clientInfo = this.clientReadPlatformService.retrieveClientByIdentifier(e.getDocumentTypeId(),
                        e.getIdentifierKey());
                reThrowAs = new DuplicateClientIdentifierException(clientInfo.displayName(), clientInfo.officeName(),
                        e.getIdentifierType(), e.getIdentifierKey());
            }
            throw reThrowAs;
        }
    }

    @DELETE
    @Path("{identifierId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteClientIdentifier(@PathParam("clientId") final Long clientId,
            @PathParam("identifierId") final Long clientIdentifierId) {

        final List<String> allowedPermissions = Arrays
                .asList("ALL_FUNCTIONS", "PORTFOLIO_MANAGEMENT_SUPER_USER", "DELETE_CLIENTIDENTIFIER");
        context.authenticatedUser().validateHasPermissionTo("DELETE_CLIENTIDENTIFIER", allowedPermissions);

        final String resourceUrl = "clients/" + clientId + "/identifiers";
        final EntityIdentifier result = this.commandsSourceWritePlatformService.logCommandSource("DELETE", resourceUrl, clientIdentifierId,
                "{}");

        return this.apiJsonSerializerService.serializeEntityIdentifier(result);
    }
}