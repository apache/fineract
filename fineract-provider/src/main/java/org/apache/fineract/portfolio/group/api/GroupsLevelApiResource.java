/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.group.api;

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

import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.group.data.GroupLevelData;
import org.apache.fineract.portfolio.group.service.GroupLevelReadPlatformService;
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