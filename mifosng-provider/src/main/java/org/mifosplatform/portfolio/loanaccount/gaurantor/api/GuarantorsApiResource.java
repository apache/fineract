package org.mifosplatform.portfolio.loanaccount.gaurantor.api;

import java.util.Arrays;
import java.util.HashSet;
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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.mifosplatform.infrastructure.core.api.ApiParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.loanaccount.gaurantor.command.GuarantorCommand;
import org.mifosplatform.portfolio.loanaccount.gaurantor.data.GuarantorData;
import org.mifosplatform.portfolio.loanaccount.gaurantor.service.GuarantorReadPlatformService;
import org.mifosplatform.portfolio.loanaccount.gaurantor.service.GuarantorWritePlatformService;
import org.mifosplatform.portfolio.savingsaccount.PortfolioApiDataConversionService;
import org.mifosplatform.portfolio.savingsaccount.PortfolioApiJsonSerializerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/loans/{loanId}/guarantor")
@Component
@Scope("singleton")
public class GuarantorsApiResource {

    @Autowired
    private GuarantorWritePlatformService guarantorWritePlatformService;

    @Autowired
    private GuarantorReadPlatformService guarantorReadPlatformService;

    @Autowired
    private PortfolioApiDataConversionService apiDataConversionService;

    @Autowired
    private PortfolioApiJsonSerializerService apiJsonSerializerService;

    private static final Set<String> typicalResponseParameters = new HashSet<String>(Arrays.asList("externalGuarantor", "existingClientId",
            "firstname", "lastname", "addressLine1", "addressLine2", "city", "state", "zip", "country", "mobileNumber", "housePhoneNumber",
            "comment", "dob"));

    private final String SystemEntityType = "m_guarantor_external";

    @Autowired
    private PlatformSecurityContext context;

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveGuarantorDetails(@Context final UriInfo uriInfo, @PathParam("loanId") final Long loanId) {
        context.authenticatedUser().validateHasReadPermission(SystemEntityType);
        Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        if (responseParameters.isEmpty()) {
            responseParameters.addAll(typicalResponseParameters);
        }
        boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        GuarantorData guarantorData = guarantorReadPlatformService.retrieveGuarantor(loanId);

        return this.apiJsonSerializerService.serializeGuarantorDataToJson(prettyPrint, responseParameters, guarantorData);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Response createGuarantor(@PathParam("loanId") final Long loanId, final String jsonRequestBody) {

        final GuarantorCommand command = this.apiDataConversionService.convertJsonToGuarantorCommand(null, loanId, jsonRequestBody);

        this.guarantorWritePlatformService.createGuarantor(loanId, command);

        // returns the loan Identifier for which the guarantor was associated
        return Response.ok().entity(new CommandProcessingResult(loanId)).build();
    }

    @PUT
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Response updateGuarantor(@PathParam("loanId") final Long loanId, final String jsonRequestBody) {
        final GuarantorCommand command = this.apiDataConversionService.convertJsonToGuarantorCommand(null, loanId, jsonRequestBody);

        this.guarantorWritePlatformService.updateGuarantor(loanId, command);

        return Response.ok().entity(new CommandProcessingResult(loanId)).build();
    }

    @DELETE
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Response deleteGuarantor(@PathParam("loanId") final Long loanId) {
        this.guarantorWritePlatformService.removeGuarantor(loanId);
        return Response.ok(new CommandProcessingResult(loanId)).build();
    }
}