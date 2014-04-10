package org.mifosplatform.infrastructure.survey.api;

import com.google.gson.JsonElement;
import org.apache.commons.lang.StringUtils;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiParameterHelper;
import org.mifosplatform.infrastructure.core.api.JsonQuery;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.dataqueries.data.DatatableData;
import org.mifosplatform.infrastructure.dataqueries.data.GenericResultsetData;
import org.mifosplatform.infrastructure.dataqueries.service.GenericDataService;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.infrastructure.survey.data.ClientScoresOverview;
import org.mifosplatform.infrastructure.survey.data.SurveyData;
import org.mifosplatform.infrastructure.survey.data.SurveyDataTableData;
import org.mifosplatform.infrastructure.survey.service.ReadSurveyService;
import org.mifosplatform.infrastructure.survey.service.ReadSurveyServiceImpl;
import org.mifosplatform.portfolio.client.domain.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;

/**
 * Created by Cieyou on 2/27/14.
 */
@Path("/survey")
@Component
@Scope("singleton")
public class SurveyApiResource {

    private final DefaultToApiJsonSerializer<SurveyData> toApiJsonSerializer;
    private final DefaultToApiJsonSerializer<ClientScoresOverview> toApiJsonClientScoreOverviewSerializer;
    private final PlatformSecurityContext context;
    private final ReadSurveyService readSurveyService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final GenericDataService genericDataService;

    private final static Logger logger = LoggerFactory.getLogger(SurveyApiResource.class);

    @Autowired
    public SurveyApiResource(final DefaultToApiJsonSerializer<SurveyData> toApiJsonSerializer,
                             final PlatformSecurityContext context,
                             final ReadSurveyService readSurveyService,
                             final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
                             final DefaultToApiJsonSerializer<ClientScoresOverview> toApiJsonClientScoreOverviewSerializer,
                             final GenericDataService genericDataService){

        this.toApiJsonSerializer = toApiJsonSerializer;
        this.context = context;
        this.readSurveyService = readSurveyService;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.toApiJsonClientScoreOverviewSerializer = toApiJsonClientScoreOverviewSerializer;
        this.genericDataService = genericDataService;
    }


     @GET
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces({ MediaType.APPLICATION_JSON })
     public String retrieveSurveys(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(SurveyApiConstants.SURVEY_RESOURCE_NAME);

        List<SurveyDataTableData> surveys = this.readSurveyService.retrieveAllSurveys();
        return this.toApiJsonSerializer.serialize(surveys);
    }


    @GET
    @Path("{surveyName}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveSurvey(@PathParam("surveyName") final String surveyName) {

        this.context.authenticatedUser().validateHasReadPermission(SurveyApiConstants.SURVEY_RESOURCE_NAME);

        SurveyDataTableData surveys = this.readSurveyService.retrieveSurvey(surveyName);

        return this.toApiJsonSerializer.serialize(surveys);

    }

    @POST
    @Path("{surveyName}/{apptableId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createDatatableEntry(@PathParam("surveyName") final String datatable, @PathParam("apptableId") final Long apptableId,
                                       final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .fullFilSurvey(datatable, apptableId) //
                .withJson(apiRequestBodyAsJson) //
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Path("{surveyName}/{clientId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String getClientSurveyOverview(@PathParam("surveyName") final String surveyName, @PathParam("clientId") final Long clientId) {


        this.context.authenticatedUser().validateHasReadPermission(SurveyApiConstants.SURVEY_RESOURCE_NAME);

        List<ClientScoresOverview> scores = this.readSurveyService.retrieveClientSurveyScoreOverview(clientId);

        return this.toApiJsonClientScoreOverviewSerializer.serialize(scores);
    }

//    @GET
//    @Path("{clientId}")
//    @Consumes({ MediaType.APPLICATION_JSON })
//    @Produces({ MediaType.APPLICATION_JSON })
//    public String getAllClientSurveyOverview( @PathParam("clientId") final Long clientId) {
//
//
//        this.context.authenticatedUser().validateHasReadPermission(SurveyApiConstants.SURVEY_RESOURCE_NAME);
//
//        List<ClientScoresOverview> scores = this.readSurveyService.retrieveClientSurveyScoreOverview(clientId);
//
//        return this.toApiJsonClientScoreOverviewSerializer.serialize(scores);
//    }

    @GET
    @Path("{surveyName}/{clientId}/{entryId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String getSurveyEntry(@PathParam("surveyName") final String surveyName, @PathParam("clientId") final Long clientId,  @PathParam("entryId") final Long entryId) {


        this.context.authenticatedUser().validateHasReadPermission(SurveyApiConstants.SURVEY_RESOURCE_NAME);

        final GenericResultsetData results  = this.readSurveyService.retrieveSurveyEntry(surveyName, clientId,entryId);

        return  this.genericDataService.generateJsonFromGenericResultsetData(results);

    }


    @PUT
    @Path("register/{surveyName}/{apptable}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String register(@PathParam("surveyName") final String datatable, @PathParam("apptable") final String apptable,
                                       final String apiRequestBodyAsJson) {


        final CommandWrapper commandRequest = new CommandWrapperBuilder().registerSurvey( datatable,apptable).withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);

    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

    @DELETE
    @Path("{surveyName}/{clientId}/{fulfilledId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteDatatableEntries(@PathParam("surveyName") final String surveyName,@PathParam("clientId") final Long clientId, @PathParam("fulfilledId") final Long fulfilledId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .deleteDatatable(surveyName,clientId, fulfilledId) //
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }
}
