package org.mifosplatform.portfolio.collectionsheet.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
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

    private final Set<String> COLLECTIONSHEET_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("dueDate", "loanProducts", "groups"));
    /*
     * dueDate format is ISO 8601 Calendar Date (YYYYMMDD)
     */
    private final String datePattern = "yyyyMMdd";
    private final PlatformSecurityContext context;
    private final CollectionSheetReadPlatformService collectionSheetReadPlatformService;
    private final ToApiJsonSerializer<JLGCollectionSheetData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    
    @Autowired
    public CollectionSheetApiResource(final PlatformSecurityContext context,
            final CollectionSheetReadPlatformService collectionSheetReadPlatformService,
            final ToApiJsonSerializer<JLGCollectionSheetData> toApiJsonSerializer, final ApiRequestParameterHelper apiRequestParameterHelper, final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {

        this.context = context;
        this.collectionSheetReadPlatformService = collectionSheetReadPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String collectionSheetData(@Context final UriInfo uriInfo, @PathParam("groupId") final Long groupId,
            @QueryParam("dueDate") final String dueDate) {

        this.context.authenticatedUser().validateHasReadPermission("COLLECTIONSHEET");
        
        validateRequest(groupId, dueDate);
        
        LocalDate localDate = null;
        if(StringUtils.isBlank(dueDate)){
            localDate = DateUtils.getLocalDateOfTenant();
        }else{
            localDate = DateUtils.parseLocalDate(dueDate, datePattern);
        }
        
        
        final JLGCollectionSheetData collectionSheet = this.collectionSheetReadPlatformService.retriveCollectionSheet(localDate, groupId);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, collectionSheet, this.COLLECTIONSHEET_DATA_PARAMETERS);

    }
    
    private void validateRequest(Long groupId, String dueDate) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("collectionsheet");

        baseDataValidator.reset().parameter("groupId").value(groupId).notBlank().longGreaterThanZero();

        baseDataValidator.reset().parameter("dueDate").value(dueDate).notBlank();
        
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }

    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateCalendar(@PathParam("groupId") final Long groupId, final String jsonRequestBody) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateCollectionSheet(groupId).withJson(jsonRequestBody).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }
}