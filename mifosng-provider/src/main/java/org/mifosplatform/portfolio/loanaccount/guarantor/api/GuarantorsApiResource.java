package org.mifosplatform.portfolio.loanaccount.guarantor.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.loanaccount.guarantor.data.GuarantorData;
import org.mifosplatform.portfolio.loanaccount.guarantor.domain.GuarantorType;
import org.mifosplatform.portfolio.loanaccount.guarantor.service.GuarantorEnumerations;
import org.mifosplatform.portfolio.loanaccount.guarantor.service.GuarantorReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/loans/{loanId}/guarantors")
@Component
@Scope("singleton")
public class GuarantorsApiResource {

    private static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("guarantorTypeId", "entityId",
            "firstname", "lastname", "addressLine1", "addressLine2", "city", "state", "zip", "country", "mobileNumber", "housePhoneNumber",
            "comment", "dob"));

    private final String resourceNameForPermission = "GUARANTOR";

    private final GuarantorReadPlatformService guarantorReadPlatformService;
    private final DefaultToApiJsonSerializer<GuarantorData> apiJsonSerializerService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final PlatformSecurityContext context;

    @Autowired
    public GuarantorsApiResource(final PlatformSecurityContext context, final GuarantorReadPlatformService guarantorReadPlatformService,
            final DefaultToApiJsonSerializer<GuarantorData> toApiJsonSerializer, final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.context = context;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.apiJsonSerializerService = toApiJsonSerializer;
        this.guarantorReadPlatformService = guarantorReadPlatformService;
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String newGuarantorTemplate(@Context final UriInfo uriInfo) {
        context.authenticatedUser().validateHasReadPermission(resourceNameForPermission);

        GuarantorData guarantorData = guarantorReadPlatformService.retrieveNewGuarantorDetails();

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.apiJsonSerializerService.serialize(settings, guarantorData, RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveGuarantorDetails(@Context final UriInfo uriInfo, @PathParam("loanId") final Long loanId) {
        context.authenticatedUser().validateHasReadPermission(resourceNameForPermission);

        List<GuarantorData> guarantorDatas = guarantorReadPlatformService.retrieveGuarantorsForValidLoan(loanId);

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return this.apiJsonSerializerService.serialize(settings, guarantorDatas, RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{guarantorId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveGuarantorDetails(@Context final UriInfo uriInfo, @PathParam("loanId") final Long loanId,
            @PathParam("guarantorId") final Long guarantorId) {
        context.authenticatedUser().validateHasReadPermission(resourceNameForPermission);

        GuarantorData guarantorData = guarantorReadPlatformService.retrieveGuarantor(loanId, guarantorId);

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        if (settings.isTemplate()) {
            final List<EnumOptionData> guarantorTypeOptions = GuarantorEnumerations.guarantorType(GuarantorType.values());
            guarantorData = GuarantorData.templateOnTop(guarantorData, guarantorTypeOptions);
        }

        return this.apiJsonSerializerService.serialize(settings, guarantorData, RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createGuarantor(@PathParam("loanId") final Long loanId, final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createGuarantor(loanId).withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.apiJsonSerializerService.serialize(result);
    }

    @PUT
    @Path("{guarantorId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateGuarantor(@PathParam("loanId") final Long loanId, @PathParam("guarantorId") final Long guarantorId,
            final String jsonRequestBody) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateGuarantor(loanId, guarantorId).withJson(jsonRequestBody)
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.apiJsonSerializerService.serialize(result);
    }

    @DELETE
    @Path("{guarantorId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteGuarantor(@PathParam("loanId") final Long loanId, @PathParam("guarantorId") final Long guarantorId) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteGuarantor(loanId, guarantorId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.apiJsonSerializerService.serialize(result);
    }
}