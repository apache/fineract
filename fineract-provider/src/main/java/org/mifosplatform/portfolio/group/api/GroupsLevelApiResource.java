/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.group.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.ToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.group.data.GroupLevelData;
import org.mifosplatform.portfolio.group.service.GroupLevelReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/grouplevels")
@Component
@Scope("singleton")
public class GroupsLevelApiResource {

    private static final Set<String> GROUPLEVEL_DATA_PARAMETERS = new HashSet<>(Arrays.asList("levelId", "levelName",
            "parentLevelId", "parentLevelName", "childLevelId", "childLevelName", "superParent", "recursable", "canHaveClients"));

    private final PlatformSecurityContext context;
    private final GroupLevelReadPlatformService groupLevelReadPlatformService;
    private final ToApiJsonSerializer<GroupLevelData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;

    @Autowired
    public GroupsLevelApiResource(final PlatformSecurityContext context, final GroupLevelReadPlatformService groupLevelReadPlatformService,
            final ToApiJsonSerializer<GroupLevelData> toApiJsonSerializer, final ApiRequestParameterHelper apiRequestParameterHelper) {
        this.context = context;
        this.groupLevelReadPlatformService = groupLevelReadPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAllGroups(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission("GROUP");

        final Collection<GroupLevelData> groupLevel = this.groupLevelReadPlatformService.retrieveAllLevels();
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return this.toApiJsonSerializer.serialize(settings, groupLevel, GROUPLEVEL_DATA_PARAMETERS);

    }
}