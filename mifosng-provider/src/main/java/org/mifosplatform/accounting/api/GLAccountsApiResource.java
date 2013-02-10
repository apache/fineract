package org.mifosplatform.accounting.api;

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

import org.mifosplatform.accounting.api.commands.GLAccountCommand;
import org.mifosplatform.accounting.api.data.GLAccountData;
import org.mifosplatform.accounting.api.infrastructure.AccountingApiDataConversionService;
import org.mifosplatform.accounting.api.infrastructure.AccountingApiJsonSerializerService;
import org.mifosplatform.accounting.service.AccountingDropdownReadPlatformService;
import org.mifosplatform.accounting.service.GLAccountReadPlatformService;
import org.mifosplatform.accounting.service.GLAccountWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/glaccounts")
@Component
@Scope("singleton")
public class GLAccountsApiResource {

    @Autowired
    private GLAccountReadPlatformService glAccountReadPlatformService;

    @Autowired
    private GLAccountWritePlatformService glAccountWritePlatformService;

    @Autowired
    private AccountingApiDataConversionService accountingApiDataConversionService;

    @Autowired
    private AccountingApiJsonSerializerService apiJsonSerializerService;

    @Autowired
    private AccountingDropdownReadPlatformService dropdownReadPlatformService;

    private final String entityType = "GL_ACCOUNT";

    @Autowired
    private PlatformSecurityContext context;

    // private final static Logger logger =
    // LoggerFactory.getLogger(GLAccountsApiResource.class);

    /**
     * @param uriInfo
     * @param classification
     * @param searchParam
     *            Option param to search by "Name" or "GL CODE" fragments
     * @return
     */
    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAllAccounts(@Context final UriInfo uriInfo, @QueryParam("type") final Integer classification,
            @QueryParam("searchParam") final String searchParam, @QueryParam("usage") final Integer usage,
            @QueryParam("manualAdjustmentsAllowed") final Boolean manualTransactionsAllowed, @QueryParam("disabled") final Boolean disabled) {

        context.authenticatedUser().validateHasReadPermission(entityType);

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        final List<GLAccountData> glAccountDatas = this.glAccountReadPlatformService.retrieveAllGLAccounts(classification, searchParam,
                usage, manualTransactionsAllowed, disabled);

        return this.apiJsonSerializerService.serializeGLAccountDataToJson(prettyPrint, responseParameters, glAccountDatas);
    }
    
    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveNewLoanProductDetails(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(entityType);

        GLAccountData glAccountData = this.glAccountReadPlatformService.retrieveNewGLAccountDetails();
        glAccountData = handleTemplate(glAccountData);

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
        return this.apiJsonSerializerService.serializeGLAccountDataToJson(prettyPrint, responseParameters, glAccountData);
    }

    @GET
    @Path("{glAccountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retreiveAccount(@PathParam("glAccountId") final Long glAccountId, @Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(entityType);

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
        boolean template = ApiParameterHelper.template(uriInfo.getQueryParameters());

        GLAccountData glAccountData = this.glAccountReadPlatformService.retrieveGLAccountById(glAccountId);
        if (template) {
            glAccountData = handleTemplate(glAccountData);
        }

        return this.apiJsonSerializerService.serializeGLAccountDataToJson(prettyPrint, responseParameters, glAccountData);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createGLAccount(final String jsonRequestBody) {

        final GLAccountCommand command = this.accountingApiDataConversionService.convertJsonToGLAccountCommand(null, jsonRequestBody);

        final Long coaId = glAccountWritePlatformService.createGLAccount(command);

        return this.apiJsonSerializerService.serializeEntityIdentifier(new CommandProcessingResult(coaId));
    }

    @PUT
    @Path("{glAccountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateGLAccount(@PathParam("glAccountId") final Long glAccountId, final String jsonRequestBody) {

        final GLAccountCommand command = this.accountingApiDataConversionService.convertJsonToGLAccountCommand(null, jsonRequestBody);

        final Long coaId = glAccountWritePlatformService.updateGLAccount(glAccountId, command);

        return this.apiJsonSerializerService.serializeEntityIdentifier(new CommandProcessingResult(coaId));
    }

    @DELETE
    @Path("{glAccountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Response deleteGLAccount(@PathParam("glAccountId") final Long glAccountId) {

        this.glAccountWritePlatformService.deleteGLAccount(glAccountId);

        return Response.ok(new CommandProcessingResult(glAccountId)).build();
    }

    private GLAccountData handleTemplate(GLAccountData glAccountData) {
        List<EnumOptionData> accountTypeOptions = dropdownReadPlatformService.retrieveGLAccountTypeOptions();
        List<EnumOptionData> usageOptions = dropdownReadPlatformService.retrieveGLAccountUsageOptions();
        return new GLAccountData(glAccountData, accountTypeOptions, usageOptions);
    }
}