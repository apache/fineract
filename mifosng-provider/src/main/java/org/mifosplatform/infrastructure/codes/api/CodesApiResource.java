package org.mifosplatform.infrastructure.codes.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
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
import org.mifosplatform.infrastructure.codes.data.CodeData;
import org.mifosplatform.infrastructure.codes.service.CodeReadPlatformService;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/codes")
@Component
@Scope("singleton")
public class CodesApiResource {

    /**
     * The set of parameters that are supported in response for {@link CodeData}
     */
    private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id", "name", "systemDefined"));
    private final String resourceNameForPermissions = "CODE";

    private final PlatformSecurityContext context;
    private final CodeReadPlatformService readPlatformService;
    private final DefaultToApiJsonSerializer<CodeData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public CodesApiResource(final PlatformSecurityContext context, final CodeReadPlatformService readPlatformService,
            final DefaultToApiJsonSerializer<CodeData> toApiJsonSerializer, final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.context = context;
        this.readPlatformService = readPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveCodes(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);

        final Collection<CodeData> codes = this.readPlatformService.retrieveAllCodes();

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, codes, RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createCode(final String apiRequestBodyAsJson) {

        final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "CREATE_CODE");
        context.authenticatedUser().validateHasPermissionTo("CREATE_CODE", allowedPermissions);

        final EntityIdentifier result = this.commandsSourceWritePlatformService.logCommandSource("CREATE", "codes", null,
                apiRequestBodyAsJson);

        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Path("{codeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retreiveOffice(@PathParam("codeId") final Long codeId, @Context final UriInfo uriInfo) {

        final CodeData code = this.readPlatformService.retrieveCode(codeId);

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, code, RESPONSE_DATA_PARAMETERS);
    }

    @PUT
    @Path("{codeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateCode(@PathParam("codeId") final Long codeId, final String apiRequestBodyAsJson) {

        final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "UPDATE_CODE");
        context.authenticatedUser().validateHasPermissionTo("UPDATE_CODE", allowedPermissions);

        final EntityIdentifier result = this.commandsSourceWritePlatformService.logCommandSource("UPDATE", "codes", codeId,
                apiRequestBodyAsJson);

        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{codeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteCodeApplication(@PathParam("codeId") final Long codeId) {

        final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "DELETE_CODE");
        context.authenticatedUser().validateHasPermissionTo("DELETE_CODE", allowedPermissions);

        final EntityIdentifier result = this.commandsSourceWritePlatformService.logCommandSource("DELETE", "codes", codeId, "{}");

        return this.toApiJsonSerializer.serialize(result);
    }
}