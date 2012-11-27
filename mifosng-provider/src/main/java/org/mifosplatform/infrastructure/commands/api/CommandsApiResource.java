package org.mifosplatform.infrastructure.commands.api;

import java.util.Collection;
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
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.api.infrastructure.PortfolioApiJsonSerializerService;
import org.mifosng.platform.exceptions.UnrecognizedQueryParamException;
import org.mifosng.platform.infrastructure.api.ApiParameterHelper;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.mifosplatform.infrastructure.commands.api.data.CommandSourceData;
import org.mifosplatform.infrastructure.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.commands.service.PortfolioCommandsReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/commands")
@Component
@Scope("singleton")
public class CommandsApiResource {

    @Autowired
    private PortfolioCommandsReadPlatformService readPlatformService;

    @Autowired
    private PortfolioCommandSourceWritePlatformService writePlatformService;

    @Autowired
    private PortfolioApiJsonSerializerService apiJsonSerializerService;

    private final String entityType = "MAKERCHECKER";
    @Autowired
    private PlatformSecurityContext context;

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveCodes(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(entityType);

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        final Collection<CommandSourceData> entries = this.readPlatformService.retrieveAllEntriesToBeChecked();

        return this.apiJsonSerializerService.serializeMakerCheckerDataToJson(prettyPrint, responseParameters, entries);
    }

    @POST
    @Path("{commandId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String approveMakerCheckerEntry(@PathParam("commandId") final Long commandId,
            @QueryParam("command") final String commandParam) {

        EntityIdentifier result = null;
        if (is(commandParam, "approve")) {
            result = this.writePlatformService.approveEntry(commandId);
        } else {
            throw new UnrecognizedQueryParamException("command", commandParam);
        }

        return this.apiJsonSerializerService.serializeEntityIdentifier(result);
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

        return this.apiJsonSerializerService.serializeEntityIdentifier(EntityIdentifier.makerChecker(id));
    }
}