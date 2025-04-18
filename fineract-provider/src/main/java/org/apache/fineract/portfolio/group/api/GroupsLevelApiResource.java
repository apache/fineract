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

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.group.data.GroupLevelData;
import org.apache.fineract.portfolio.group.service.GroupLevelReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/grouplevels")
@Component
@Tag(name = "Groups Level", description = "")
@RequiredArgsConstructor
public class GroupsLevelApiResource {

    private final PlatformSecurityContext context;
    private final GroupLevelReadPlatformService groupLevelReadPlatformService;

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public List<GroupLevelData> retrieveAllGroups() {

        this.context.authenticatedUser().validateHasReadPermission("GROUP");

        return this.groupLevelReadPlatformService.retrieveAllLevels();
    }
}
