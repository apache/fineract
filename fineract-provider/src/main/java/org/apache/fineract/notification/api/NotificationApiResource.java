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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.service.SqlValidator;
import org.apache.fineract.notification.data.NotificationData;
import org.apache.fineract.notification.service.NotificationReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/notifications")
@Component
@Tag(name = "Notification", description = "")
@RequiredArgsConstructor
public class NotificationApiResource {

    private final PlatformSecurityContext context;
    private final NotificationReadPlatformService notificationReadPlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final ToApiJsonSerializer<NotificationData> toApiJsonSerializer;
    private final SqlValidator sqlValidator;

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = NotificationApiResourceSwagger.GetNotificationsResponse.class))) })
    public String getAllNotifications(@Context final UriInfo uriInfo,
            @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder,
            @QueryParam("isRead") @Parameter(description = "isRead") final boolean isRead) {

        this.context.authenticatedUser();
        final Page<NotificationData> notificationData;
        sqlValidator.validate(orderBy);
        sqlValidator.validate(sortOrder);
        final SearchParameters searchParameters = SearchParameters.builder().limit(limit).offset(offset).orderBy(orderBy)
                .sortOrder(sortOrder).build();
        if (!isRead) {
            notificationData = this.notificationReadPlatformService.getAllUnreadNotifications(searchParameters);
        } else {
            notificationData = this.notificationReadPlatformService.getAllNotifications(searchParameters);
        }
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, notificationData);
    }

    @PUT
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public void update() {
        this.context.authenticatedUser();
        this.notificationReadPlatformService.updateNotificationReadStatus();
    }
}
