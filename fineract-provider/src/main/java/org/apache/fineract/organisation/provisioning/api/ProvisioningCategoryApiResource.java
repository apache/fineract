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
package org.apache.fineract.organisation.provisioning.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.provisioning.data.ProvisioningCategoryData;
import org.apache.fineract.organisation.provisioning.service.ProvisioningCategoryReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/provisioningcategory")
@Component
@Tag(name = "Provisioning Category", description = "")
@RequiredArgsConstructor
public class ProvisioningCategoryApiResource {

    private final PlatformSecurityContext platformSecurityContext;
    private final ProvisioningCategoryReadPlatformService provisioningCategoryReadPlatformService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public List<ProvisioningCategoryData> retrieveAll() {
        platformSecurityContext.authenticatedUser();
        return provisioningCategoryReadPlatformService.retrieveAllProvisionCategories();
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public CommandProcessingResult createProvisioningCategory(final String apiRequestBodyAsJson) {
        platformSecurityContext.authenticatedUser();
        CommandWrapper commandWrapper = new CommandWrapperBuilder().createProvisioningCategory().withJson(apiRequestBodyAsJson).build();
        return commandsSourceWritePlatformService.logCommandSource(commandWrapper);
    }

    @PUT
    @Path("{categoryId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public CommandProcessingResult updateProvisioningCategory(@PathParam("categoryId") final Long categoryId,
            final String apiRequestBodyAsJson) {
        platformSecurityContext.authenticatedUser();
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateProvisioningCategory(categoryId)
                .withJson(apiRequestBodyAsJson).build();
        return commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @DELETE
    @Path("{categoryId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public CommandProcessingResult deleteProvisioningCategory(@PathParam("categoryId") final Long categoryId) {
        platformSecurityContext.authenticatedUser();
        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteProvisioningCategory(categoryId).build();
        return commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }
}
