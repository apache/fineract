package org.mifosng.platform.api;

import org.mifosng.platform.api.commands.ChargeCommand;
import org.mifosng.platform.api.data.ChargeData;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.api.infrastructure.ApiDataConversionService;
import org.mifosng.platform.api.infrastructure.ApiParameterHelper;
import org.mifosng.platform.charge.service.ChargeReadPlatformService;
import org.mifosng.platform.charge.service.ChargeWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Set;

@Path("/charges")
@Component
@Scope("singleton")
public class ChargesApiResource {

    @Autowired
    private ChargeReadPlatformService chargeReadPlatformService;

    @Autowired
    private ChargeWritePlatformService chargeWritePlatformService;

    @Autowired
    private ApiDataConversionService apiDataConversionService;

    @GET
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String retrieveAllCharges(@Context final UriInfo uriInfo){

        Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        Collection<ChargeData> charges = this.chargeReadPlatformService.retrieveAllCharges();

        return this.apiDataConversionService.convertChargeDataToJson(prettyPrint,
                responseParameters, charges.toArray(new ChargeData[charges.size()]));
    }

    @GET
    @Path("{chargeId}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String retrieveCharge(@PathParam("chargeId") final Long chargeId, @Context final UriInfo uriInfo){

        Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        ChargeData charge = this.chargeReadPlatformService.retrieveCharge(chargeId);

        return this.apiDataConversionService.convertChargeDataToJson(prettyPrint,
                responseParameters, charge);
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response createCharge(final String jsonRequestBody){

        ChargeCommand command = this.apiDataConversionService.convertJsonToChargeCommand(null, jsonRequestBody);

        Long chargeId = this.chargeWritePlatformService.createCharge(command);

        return Response.ok().entity(new EntityIdentifier(chargeId)).build();
    }

    @PUT
    @Path("{chargeId}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response updateCharge(@PathParam("chargeId") final Long chargeId, final String jsonRequestBody){

        ChargeCommand command = this.apiDataConversionService.convertJsonToChargeCommand(chargeId, jsonRequestBody);

        Long entityId = this.chargeWritePlatformService.updateCharge(command);

        return Response.ok().entity(new EntityIdentifier(entityId)).build();
    }

    @DELETE
    @Path("{chargeId}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response deleteCharge(@PathParam("chargeId") final Long chargeId){

        Long entityId = this.chargeWritePlatformService.deleteCharge(chargeId);

        return Response.ok().entity(new EntityIdentifier(entityId)).build();
    }
}
