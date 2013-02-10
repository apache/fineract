package org.mifosplatform.accounting.api;

import java.util.Date;
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
import org.mifosplatform.accounting.api.commands.GLJournalEntryCommand;
import org.mifosplatform.accounting.api.data.GLJournalEntryData;
import org.mifosplatform.accounting.api.data.JournalEntryIdentifier;
import org.mifosplatform.accounting.api.infrastructure.AccountingApiDataConversionService;
import org.mifosplatform.accounting.api.infrastructure.AccountingApiJsonSerializerService;
import org.mifosplatform.accounting.api.infrastructure.DateParam;
import org.mifosplatform.accounting.service.GLJournalEntryReadPlatformService;
import org.mifosplatform.accounting.service.GLJournalEntryWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiParameterHelper;
import org.mifosplatform.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/journalentries")
@Component
@Scope("singleton")
public class GLJournalEntriesApiResource {

    @Autowired
    private GLJournalEntryReadPlatformService glJournalEntryReadPlatformService;

    @Autowired
    private GLJournalEntryWritePlatformService glJournalEntryWritePlatformService;

    @Autowired
    private AccountingApiDataConversionService accountingApiDataConversionService;

    @Autowired
    private AccountingApiJsonSerializerService apiJsonSerializerService;

    private final String entityType = "JOURNAL_ENTRY";

    @Autowired
    private PlatformSecurityContext context;

    // private final static Logger logger =
    // LoggerFactory.getLogger(GLJournalEntriesApiResource.class);

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAllJournalEntries(@Context final UriInfo uriInfo, @QueryParam("officeId") final Long officeId,
            @QueryParam("glAccountId") final Long glAccountId, @QueryParam("portfolioGenerated") final Boolean portfolioGenerated,
            @QueryParam("fromDate") final DateParam fromDateParam, @QueryParam("toDate") final DateParam toDateParam,
            @QueryParam("transactionId") String transactionId) {

        // TODO: Vishwas Add pagination, approach at
        // http://www.javaworld.com/community/node/8295 looks good

        context.authenticatedUser().validateHasReadPermission(entityType);

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
        List<GLJournalEntryData> glJournalEntryDatas = null;
        // get dates from date params
        Date fromDate = null;
        if (fromDateParam != null) {
            fromDate = fromDateParam.getDate();
        }
        Date toDate = null;
        if (toDateParam != null) {
            toDate = toDateParam.getDate();
        }
        if (StringUtils.isBlank(transactionId)) {
            glJournalEntryDatas = this.glJournalEntryReadPlatformService.retrieveAllGLJournalEntries(officeId, glAccountId,
                    portfolioGenerated, fromDate, toDate);
        } else {
            glJournalEntryDatas = this.glJournalEntryReadPlatformService.retrieveRelatedJournalEntries(transactionId);
        }

        return this.apiJsonSerializerService.serializeGLJournalEntryDataToJson(prettyPrint, responseParameters, glJournalEntryDatas);
    }

    @GET
    @Path("{glJournalEntryId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retreiveJournalEntryById(@PathParam("glJournalEntryId") final Long glJournalEntryId, @Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(entityType);

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        final GLJournalEntryData glJournalEntryData = this.glJournalEntryReadPlatformService.retrieveGLJournalEntryById(glJournalEntryId);
        return this.apiJsonSerializerService.serializeGLJournalEntryDataToJson(prettyPrint, responseParameters, glJournalEntryData);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createGLJournalEntry(final String jsonRequestBody) {

        final GLJournalEntryCommand command = this.accountingApiDataConversionService.convertJsonToGLJournalEntryCommand(jsonRequestBody);

        final String transactionId = glJournalEntryWritePlatformService.createJournalEntry(command);
        return this.apiJsonSerializerService.serializeJournalEntryIdentifier(new JournalEntryIdentifier(transactionId));
    }

    @POST
    @Path("{transactionId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createReversalJournalEntry(@PathParam("transactionId") final String transactionId,
            @QueryParam("command") final String commandParam) {
        if (is(commandParam, "reverse")) {
            this.glJournalEntryWritePlatformService.revertJournalEntry(transactionId);
        } else {
            throw new UnrecognizedQueryParamException("command", commandParam);
        }

        return this.apiJsonSerializerService.serializeJournalEntryIdentifier(new JournalEntryIdentifier(transactionId));
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }
}