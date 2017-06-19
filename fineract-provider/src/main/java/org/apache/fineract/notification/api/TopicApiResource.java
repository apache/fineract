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
package org.apache.fineract.notification.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.notification.data.TopicData;
import org.apache.fineract.notification.service.TopicReadPlatformService;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/topics")
@Component
@Scope("singleton")
public class TopicApiResource {
	
	/**
     * The set of parameters that are supported in response for
     * {@link OfficeData}.
     */
    private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList("id", "name", "permissionId",
    		"active", "activationDate"));
	
	private final String resourceNameForPermissions = "TOPIC";
	
	private final PlatformSecurityContext context;
    private final TopicReadPlatformService readPlatformService;
    private final DefaultToApiJsonSerializer<TopicData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
	
    @Autowired
    public TopicApiResource(PlatformSecurityContext context, TopicReadPlatformService readPlatformService,
			DefaultToApiJsonSerializer<TopicData> toApiJsonSerializer,
			ApiRequestParameterHelper apiRequestParameterHelper,
			PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
		this.context = context;
		this.readPlatformService = readPlatformService;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
	}
    
    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String getTopics(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final Collection<TopicData> topics = this.readPlatformService.getAllTopics();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, topics, this.RESPONSE_DATA_PARAMETERS);
    }
    
    @GET
    @Path("{topicId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String getTopic(@PathParam("officeId") final Long topicId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final TopicData topic = this.readPlatformService.findById(topicId);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, topic, this.RESPONSE_DATA_PARAMETERS);
    }
    
    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createTopic(final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
        		.createTopic()
                .withJson(apiRequestBodyAsJson) //
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }
    
    @PUT
    @Path("{topicId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateTopic(@PathParam("topicId") final Long topicId, final String apiRequestBodyAsJson) {

    	final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .updateTopic(topicId) //
                .withJson(apiRequestBodyAsJson) //
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }
    
}
