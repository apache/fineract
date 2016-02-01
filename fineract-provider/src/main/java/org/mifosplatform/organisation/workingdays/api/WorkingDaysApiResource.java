/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.workingdays.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.workingdays.data.WorkingDaysData;
import org.mifosplatform.organisation.workingdays.service.WorkingDaysReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/workingdays")
@Component
@Scope("singleton")
public class WorkingDaysApiResource {

    private final DefaultToApiJsonSerializer<WorkingDaysData> toApiJsonSerializer;
    private final WorkingDaysReadPlatformService workingDaysReadPlatformService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final PlatformSecurityContext context;
    private final ApiRequestParameterHelper apiRequestParameterHelper;

    @Autowired
    public WorkingDaysApiResource(DefaultToApiJsonSerializer<WorkingDaysData> toApiJsonSerializer,
            WorkingDaysReadPlatformService workingDaysReadPlatformService,
            PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService, PlatformSecurityContext context,
            ApiRequestParameterHelper apiRequestParameterHelper) {
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.workingDaysReadPlatformService = workingDaysReadPlatformService;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.context = context;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAll(@Context final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission(WorkingDaysApiConstants.WORKING_DAYS_RESOURCE_NAME);
        final WorkingDaysData workingDaysData = this.workingDaysReadPlatformService.retrieve();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, workingDaysData);
    }

    @PUT
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String update(final String jsonRequestBody) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateWorkingDays().withJson(jsonRequestBody).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Path("/template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String template(@Context final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission(WorkingDaysApiConstants.WORKING_DAYS_RESOURCE_NAME);

        final WorkingDaysData repaymentRescheduleOptions = this.workingDaysReadPlatformService.repaymentRescheduleType();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, repaymentRescheduleOptions,
                WorkingDaysApiConstants.WORKING_DAYS_TEMPLATE_PARAMETERS);
    }

}
