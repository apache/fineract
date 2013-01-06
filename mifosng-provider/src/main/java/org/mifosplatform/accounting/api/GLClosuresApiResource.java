package org.mifosplatform.accounting.api;

import java.util.ArrayList;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.mifosplatform.accounting.api.commands.GLClosureCommand;
import org.mifosplatform.accounting.api.data.GLClosureData;
import org.mifosplatform.accounting.api.infrastructure.AccountingApiDataConversionService;
import org.mifosplatform.accounting.api.infrastructure.AccountingApiJsonSerializerService;
import org.mifosplatform.accounting.service.GLClosureReadPlatformService;
import org.mifosplatform.accounting.service.GLClosureWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.office.data.OfficeLookup;
import org.mifosplatform.organisation.office.service.OfficeReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/glclosures")
@Component
@Scope("singleton")
public class GLClosuresApiResource {

    @Autowired
    private GLClosureReadPlatformService glClosureReadPlatformService;

    @Autowired
    private GLClosureWritePlatformService glClosureWritePlatformService;

    @Autowired
    private OfficeReadPlatformService officeReadPlatformService;

    @Autowired
    private AccountingApiDataConversionService accountingApiDataConversionService;

    @Autowired
    private AccountingApiJsonSerializerService apiJsonSerializerService;

    private final String entityType = "GL_CLOSURE";

    @Autowired
    private PlatformSecurityContext context;

    // private final static Logger logger =
    // LoggerFactory.getLogger(GLClosuresApiResource.class);

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAllClosures(@Context final UriInfo uriInfo, @QueryParam("officeId") final Long officeId) {

        context.authenticatedUser().validateHasReadPermission(entityType);

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        final List<GLClosureData> glClosureDatas = this.glClosureReadPlatformService.retrieveAllGLClosures(officeId);

        return this.apiJsonSerializerService.serializeGLClosureDataToJson(prettyPrint, responseParameters, glClosureDatas);
    }

    @GET
    @Path("{glClosureId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retreiveClosure(@PathParam("glClosureId") final Long glClosureId, @Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(entityType);

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
        boolean template = ApiParameterHelper.template(uriInfo.getQueryParameters());

        final GLClosureData glClosureData = this.glClosureReadPlatformService.retrieveGLClosureById(glClosureId);
        if (template) {
            glClosureData.setAllowedOffices(new ArrayList<OfficeLookup>(officeReadPlatformService.retrieveAllOfficesForLookup()));
        }

        return this.apiJsonSerializerService.serializeGLClosureDataToJson(prettyPrint, responseParameters, glClosureData);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createGLClosure(final String jsonRequestBody) {

        final GLClosureCommand command = this.accountingApiDataConversionService.convertJsonToGLClosureCommand(null, jsonRequestBody);

        final Long coaId = glClosureWritePlatformService.createGLClosure(command);

        return this.apiJsonSerializerService.serializeEntityIdentifier(new CommandProcessingResult(coaId));
    }

    @PUT
    @Path("{glClosureId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateGLClosure(@PathParam("glClosureId") final Long glClosureId, final String jsonRequestBody) {

        final GLClosureCommand command = this.accountingApiDataConversionService.convertJsonToGLClosureCommand(null, jsonRequestBody);

        final Long coaId = glClosureWritePlatformService.updateGLClosure(glClosureId, command);

        return this.apiJsonSerializerService.serializeEntityIdentifier(new CommandProcessingResult(coaId));
    }

    @DELETE
    @Path("{glClosureId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Response deleteGLClosure(@PathParam("glClosureId") final Long glClosureId) {

        this.glClosureWritePlatformService.deleteGLClosure(glClosureId);

        return Response.ok(new CommandProcessingResult(glClosureId)).build();
    }
}