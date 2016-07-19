package org.apache.fineract.notification.api;


import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.notification.data.NotificationData;
import org.apache.fineract.notification.service.NotificationReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

@Path("/notifications")
@Component
public class NotificationApiResource {

    private final PlatformSecurityContext context;
    private final NotificationReadPlatformService notificationReadPlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final ToApiJsonSerializer<NotificationData> toApiJsonSerializer;

    @Autowired
    public NotificationApiResource(PlatformSecurityContext context,
                                   NotificationReadPlatformService notificationReadPlatformService,
                                   ApiRequestParameterHelper apiRequestParameterHelper,
                                   ToApiJsonSerializer<NotificationData> toApiJsonSerializer) {
        this.context = context;
        this.notificationReadPlatformService = notificationReadPlatformService;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.toApiJsonSerializer = toApiJsonSerializer;
    }

    @GET
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String getAllNotifications(@Context final UriInfo uriInfo, @QueryParam("orderBy") final String orderBy,
                                            @QueryParam("limit") final Integer limit,
                                            @QueryParam("offset") final Integer offset,
                                            @QueryParam("sortOrder") final String sortOrder,
                                            @QueryParam("isRead") final boolean isRead) {

        this.context.authenticatedUser();
        final Page<NotificationData> notificationData;
        final SearchParameters searchParameters = SearchParameters.forNotifications(offset, limit, orderBy, sortOrder);
        if (!isRead) {
            notificationData = this.notificationReadPlatformService.getAllUnreadNotifications(searchParameters);
        } else {
            notificationData = this.notificationReadPlatformService.getAllNotifications(searchParameters);
        }
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, notificationData);
    }

    @PUT
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public void update() {
        this.context.authenticatedUser();
        this.notificationReadPlatformService.updateNotificationReadStatus();
    }
}
