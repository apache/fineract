package org.mifosplatform.commands.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.commands.data.CommandSourceData;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.commands.service.PortfolioCommandsReadPlatformService;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/commands")
@Component
@Scope("singleton")
public class CommandsApiResource {

    private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id", "taskName", "madeOnDate"));
    private final String resourceNameForPermissions = "MAKERCHECKER";

    private final PlatformSecurityContext context;
    private final PortfolioCommandsReadPlatformService readPlatformService;
    private final DefaultToApiJsonSerializer<CommandSourceData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService writePlatformService;

    @Autowired
    public CommandsApiResource(final PlatformSecurityContext context, final PortfolioCommandsReadPlatformService readPlatformService,
            final DefaultToApiJsonSerializer<CommandSourceData> toApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper, final PortfolioCommandSourceWritePlatformService writePlatformService) {
        this.context = context;
        this.readPlatformService = readPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.writePlatformService = writePlatformService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveCommands(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);

        final Collection<CommandSourceData> entries = this.readPlatformService.retrieveAllEntriesToBeChecked();

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, entries, RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Path("{commandId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String approveMakerCheckerEntry(@PathParam("commandId") final Long commandId, @QueryParam("command") final String commandParam) {

        CommandProcessingResult result = null;
        if (is(commandParam, "approve")) {
            result = this.writePlatformService.approveEntry(commandId);
        } else {
            throw new UnrecognizedQueryParamException("command", commandParam);
        }

        return this.toApiJsonSerializer.serialize(result);
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

    @DELETE
    @Path("{commandId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteMakerCheckerEntry(@PathParam("commandId") final Long commandId) {

        final Long id = this.writePlatformService.deleteEntry(commandId);

        return this.toApiJsonSerializer.serialize(CommandProcessingResult.commandOnlyResult(id));
    }
}