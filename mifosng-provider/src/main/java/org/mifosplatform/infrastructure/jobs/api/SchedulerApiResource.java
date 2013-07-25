package org.mifosplatform.infrastructure.jobs.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.ToApiJsonSerializer;
import org.mifosplatform.infrastructure.jobs.data.SchedulerDetailData;
import org.mifosplatform.infrastructure.jobs.service.JobRegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Path("/scheduler")
@Component
public class SchedulerApiResource {

    private final JobRegisterService jobRegisterService;
    private final ToApiJsonSerializer<SchedulerDetailData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;

    @Autowired
    public SchedulerApiResource(final JobRegisterService jobRegisterService,
            final ToApiJsonSerializer<SchedulerDetailData> toApiJsonSerializer, final ApiRequestParameterHelper apiRequestParameterHelper) {
        this.jobRegisterService = jobRegisterService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveStatus(@Context final UriInfo uriInfo) {
        boolean isSchedulerRunning = jobRegisterService.isSchedulerRunning();
        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        SchedulerDetailData schedulerDetailData = new SchedulerDetailData(isSchedulerRunning);
        return this.toApiJsonSerializer.serialize(settings, schedulerDetailData,
                SchedulerJobApiConstants.SCHEDULER_DETAIL_RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Response changeSchedulerStatus(@QueryParam(SchedulerJobApiConstants.COMMAND) final String commandParam) {
        Response response = Response.status(400).build();
        if (is(commandParam, SchedulerJobApiConstants.COMMAND_START_SCHEDULER)) {
            jobRegisterService.startScheduler();
            response = Response.status(202).build();
        } else if (is(commandParam, SchedulerJobApiConstants.COMMAND_STOP_SCHEDULER)) {
            jobRegisterService.pauseScheduler();
            response = Response.status(202).build();
        } else {
            throw new UnrecognizedQueryParamException(SchedulerJobApiConstants.COMMAND, commandParam);
        }
        return response;
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }
}
