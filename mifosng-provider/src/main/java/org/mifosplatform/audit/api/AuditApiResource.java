package org.mifosplatform.audit.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.mifosplatform.commands.service.PortfolioCommandsReadPlatformService;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/audit")
@Component
@Scope("singleton")
public class AuditApiResource {

    private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id", "taskName", "madeOnDate"));
    private final String resourceNameForPermissions = "MAKERCHECKER";

    private final PlatformSecurityContext context;
    private final PortfolioCommandsReadPlatformService readPlatformService;

    @Autowired
    public AuditApiResource(final PlatformSecurityContext context, final PortfolioCommandsReadPlatformService readPlatformService) {
        this.context = context;
        this.readPlatformService = readPlatformService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAuditEntries(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
/*
        final Collection<CommandSourceData> entries = this.readPlatformService.retrieveAllEntriesToBeChecked();

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, entries, RESPONSE_DATA_PARAMETERS);
     */
		return "nothing";
    }



}