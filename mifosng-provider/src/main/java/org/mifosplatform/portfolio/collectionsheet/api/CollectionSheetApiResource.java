package org.mifosplatform.portfolio.collectionsheet.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.ToApiJsonSerializer;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.collectionsheet.data.JLGCollectionSheetData;
import org.mifosplatform.portfolio.collectionsheet.service.CollectionSheetReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/*
 *  At present API supports groups/{groupId}
 *  Future version supports staffs/{staffId}
 *  Future version supports offices/{officeId}
 */

@Path("/groups/{groupId}/collectionsheet")
@Component
@Scope("singleton")
public class CollectionSheetApiResource {

    private final Set<String> COLLECTIONSHEET_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("dueDate", "groups"));
    /*
     * dueDate format is ISO 8601 Calendar Date (YYYYMMDD)
     */
    private final String datePattern = "YYYYMMDD";
    private final PlatformSecurityContext context;
    private final CollectionSheetReadPlatformService collectionSheetReadPlatformService;
    private final ToApiJsonSerializer<JLGCollectionSheetData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;

    @Autowired
    public CollectionSheetApiResource(final PlatformSecurityContext context,
            final CollectionSheetReadPlatformService collectionSheetReadPlatformService,
            final ToApiJsonSerializer<JLGCollectionSheetData> toApiJsonSerializer, final ApiRequestParameterHelper apiRequestParameterHelper) {

        this.context = context;
        this.collectionSheetReadPlatformService = collectionSheetReadPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;

    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String collectionSheetData(@Context final UriInfo uriInfo, @PathParam("groupId") final Long groupId,
            @QueryParam("dueDate") final String dueDate) {

        this.context.authenticatedUser().validateHasReadPermission("COLLECTIONSHEET");
        final LocalDate localDate = DateUtils.parseLocalDate(dueDate, this.datePattern);
        final JLGCollectionSheetData collectionSheet = this.collectionSheetReadPlatformService.retriveCollectionSheet(localDate, groupId);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, collectionSheet, this.COLLECTIONSHEET_DATA_PARAMETERS);

    }
}