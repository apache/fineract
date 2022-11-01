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
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.serialization.CommandProcessingResultJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.notification.domain.Notification;
import org.apache.fineract.notification.domain.NotificationMapperRepository;
import org.apache.fineract.notification.service.NotificationMapperWritePlatformService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Path("/notifications")
@Component
@Tag(name = "Notification", description = "")
@RequiredArgsConstructor
public class NotificationApiResource {

    private final PlatformSecurityContext context;
    private final NotificationMapperRepository notificationMapperRepository;
    private final NotificationMapperWritePlatformService notificationMapperWritePlatformService;
    private final CommandProcessingResultJsonSerializer commandProcessingResultJsonSerializer;

    // private final ApiRequestParameterHelper apiRequestParameterHelper;
    // private final ToApiJsonSerializer<NotificationData> toApiJsonSerializer;

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = NotificationApiResourceSwagger.GetNotificationsResponse.class))) })
    public String getAllNotifications(@Context final UriInfo uriInfo,
            @DefaultValue("id") @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @DefaultValue("200") @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @DefaultValue("0") @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @DefaultValue("desc") @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder,
            @DefaultValue("false") @QueryParam("isRead") @Parameter(description = "isRead") final boolean isRead) {

        AppUser user = this.context.authenticatedUser();
        // final Page<NotificationData> notificationData;
        Sort.Direction sortDirection = sortOrder.compareToIgnoreCase("asc") == 1 ? Sort.Direction.ASC
                : (sortOrder.compareToIgnoreCase("desc") == 1 ? Sort.Direction.DESC : Sort.DEFAULT_DIRECTION);
        Pageable pageable = PageRequest.of(offset, limit, sortDirection, orderBy);
        Page<Notification> page;
        // final SearchParameters searchParameters = SearchParameters.forPagination(offset, limit, orderBy, sortOrder);
        if (!isRead) {
            page = notificationMapperRepository.getUnreadNotificationsForAUserWithParameters(user.getId(), pageable);
        } else {
            page = notificationMapperRepository.getAllNotificationsForAUserWithParameters(user.getId(), pageable);
        }

        String response = commandProcessingResultJsonSerializer.serialize(page);
        response = response.replace("content", "pageItems");
        response = response.replace("totalElements", "totalFilteredRecords");
        response = response.replaceAll("notificationContent", "content");
        response = response.replaceAll("objectIdentifier", "objectId");
        return response;
    }

    @PUT
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public void update() {
        final AppUser user = this.context.authenticatedUser();
        this.notificationMapperWritePlatformService.markAllNotificationsForAUserAsRead(user.getId());
    }

    @PUT
    @Path("{notificationId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public void updateOne(@PathParam("notificationId") @Parameter(description = "notificationId") final Long notificationId) {
        final AppUser user = this.context.authenticatedUser();
        this.notificationMapperWritePlatformService.markASingleNotificationForAUserAsRead(user.getId(), notificationId);
    }
}
