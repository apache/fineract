package org.mifosplatform.portfolio.group.api;

import java.util.Arrays;
import java.util.Collection;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.api.ApiParameterHelper;
import org.mifosplatform.infrastructure.core.api.PortfolioApiDataConversionService;
import org.mifosplatform.infrastructure.core.api.PortfolioApiJsonSerializerService;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;
import org.mifosplatform.infrastructure.office.data.OfficeLookup;
import org.mifosplatform.infrastructure.office.service.OfficeReadPlatformService;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.client.data.ClientLookup;
import org.mifosplatform.portfolio.client.service.ClientReadPlatformService;
import org.mifosplatform.portfolio.group.command.GroupCommand;
import org.mifosplatform.portfolio.group.data.GroupAccountSummaryCollectionData;
import org.mifosplatform.portfolio.group.data.GroupData;
import org.mifosplatform.portfolio.group.service.GroupReadPlatformService;
import org.mifosplatform.portfolio.group.service.GroupWritePlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/groups")
@Component
@Scope("singleton")
public class GroupsApiResource {

    private final static Logger logger = LoggerFactory.getLogger(GroupsApiResource.class);

    @Autowired
    private GroupReadPlatformService groupReadPlatformService;

    @Autowired
    private GroupWritePlatformService groupWritePlatformService;

    @Autowired
    private ClientReadPlatformService clientReadPlatformService;

    @Autowired
    private PortfolioApiDataConversionService apiDataConversionService;

    @Autowired
    private OfficeReadPlatformService officeReadPlatformService;

	@Autowired
	private PortfolioApiJsonSerializerService apiJsonSerializerService;

    @Autowired
    private PlatformSecurityContext context;

    private static final Set<String> typicalResponseParameters = new HashSet<String>(
            Arrays.asList("id", "officeId", "name", "externalId", "clientMembers")
    );

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAllGroups(@Context final UriInfo uriInfo,
                                    @QueryParam("sqlSearch") final String sqlSearch,
                                    @QueryParam("officeId") final Long officeId,
                                    @QueryParam("externalId") final String externalId,
                                    @QueryParam("name") final String name,
                                    @QueryParam("underHierarchy") final String hierarchy){
    	
    	context.authenticatedUser().validateHasReadPermission("GROUP");

        final String extraCriteria = getGroupExtraCriteria(sqlSearch, officeId, externalId, name, hierarchy);

        Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        if (responseParameters.isEmpty()) {
            responseParameters.addAll(typicalResponseParameters);
        }

        boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        Collection<GroupData> groups = this.groupReadPlatformService.retrieveAllGroups(extraCriteria);

        return this.apiJsonSerializerService.serializeGroupDataToJson(prettyPrint, responseParameters, groups);
    }

    @GET
    @Path("{groupId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveGroupData(@Context final UriInfo uriInfo,
                                    @PathParam("groupId") final Long groupId,
                                    @QueryParam("officeId") final Long officeId) {

    	context.authenticatedUser().validateHasReadPermission("GROUP");

                Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo
                .getQueryParameters());
        if (responseParameters.isEmpty()) {
            responseParameters.addAll(typicalResponseParameters);
        }

        boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        GroupData group = this.groupReadPlatformService.retrieveGroup(groupId);
        final Collection<ClientLookup> clientMembers = this.groupReadPlatformService.retrieveClientMembers(groupId);
        Collection<ClientLookup> availableClients = null;
        Collection<OfficeLookup> allowedOffices = null;

        group = new GroupData(group, clientMembers, availableClients, allowedOffices);

        boolean template = ApiParameterHelper.template(uriInfo.getQueryParameters());
		if (template) {
			responseParameters.add("allowedClients");
			responseParameters.add("allowedOffices");

            if (officeId != null){
                availableClients = this.clientReadPlatformService.retrieveAllIndividualClientsForLookupByOfficeId(officeId);
            } else {
                availableClients = this.clientReadPlatformService.retrieveAllIndividualClientsForLookupByOfficeId(group.getOfficeId());
            }

			availableClients.removeAll(group.clientMembers());

			allowedOffices = officeReadPlatformService.retrieveAllOfficesForLookup();

			group = new GroupData(group, group.clientMembers(), availableClients, allowedOffices);
		}

        return this.apiJsonSerializerService.serializeGroupDataToJson(prettyPrint, responseParameters, group);
    }

    @GET
    @Path("template")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String newGroupDetails(@Context final UriInfo uriInfo,
                                  @QueryParam("officeId") final Long officeId) {

    	context.authenticatedUser().validateHasReadPermission("GROUP");

        Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        if (responseParameters.isEmpty()) {
            responseParameters.addAll(typicalResponseParameters);
        }
        boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        responseParameters.addAll(Arrays.asList("officeId", "allowedClients", "allowedOffices"));

        GroupData groupData;
        if (officeId != null){
            groupData = this.groupReadPlatformService.retrieveNewGroupDetails(officeId);
        } else {
            groupData = this.groupReadPlatformService.retrieveNewGroupDetails(context.authenticatedUser().getOffice().getId());
        }

        return this.apiJsonSerializerService.serializeGroupDataToJson(prettyPrint, responseParameters, groupData);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Response createGroup(final String jsonRequestBody) {

        final GroupCommand command = this.apiDataConversionService.convertJsonToGroupCommand(null, jsonRequestBody);

        EntityIdentifier entityIdentifier = this.groupWritePlatformService.createGroup(command);

        return Response.ok().entity(entityIdentifier).build();
    }

    @PUT
    @Path("{groupId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Response updateGroup(@PathParam("groupId") final Long groupId, final String jsonRequestBody) {

        final GroupCommand command = this.apiDataConversionService.convertJsonToGroupCommand(groupId, jsonRequestBody);

        EntityIdentifier entityIdentifier = this.groupWritePlatformService.updateGroup(command);

        return Response.ok().entity(entityIdentifier).build();
    }
    
    @DELETE
    @Path("{groupId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Response deleteGroup(@PathParam("groupId") final Long groupId){

        EntityIdentifier entityIdentifier = this.groupWritePlatformService.deleteGroup(groupId);

        return Response.ok().entity(entityIdentifier).build();
    }

    @GET
    @Path("{groupId}/loans")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String retrieveGroupAccount(@PathParam("groupId") final Long groupId,
                                       @Context final UriInfo uriInfo) {

    	context.authenticatedUser().validateHasReadPermission("GROUP");

        Set<String> typicalResponseParameters = new HashSet<String>(
                Arrays.asList("pendingApprovalLoans", "awaitingDisbursalLoans", "openLoans", "closedLoans",
                        "anyLoanCount", "pendingApprovalLoanCount", "awaitingDisbursalLoanCount",
                        "activeLoanCount", "closedLoanCount")
        );

        Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        if (responseParameters.isEmpty()) {
            responseParameters.addAll(typicalResponseParameters);
        }
        boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        GroupAccountSummaryCollectionData clientAccount = this.groupReadPlatformService.retrieveGroupAccountDetails(groupId);

        return this.apiJsonSerializerService.serializeGroupAccountSummaryCollectionDataToJson(prettyPrint, responseParameters, clientAccount);
    }

    // 'g.' preffix because of ERROR 1052 (23000): Column 'column_name' in where clause is ambiguous
    // caused by the same name of columns in m_office and m_group tables
    private String getGroupExtraCriteria(String sqlSearch, Long officeId,
                                         String externalId, String name,
                                         String hierarchy){

        String extraCriteria = "";

        if (sqlSearch != null) {
            sqlSearch = sqlSearch.replaceAll(" name ", " g.name ");
            sqlSearch = sqlSearch.replaceAll("name ", "g.name ");
            extraCriteria = " and (" + sqlSearch + ")";
        }

        if (officeId != null) {
            extraCriteria += " and office_id = " + officeId;
        }

        if (externalId != null) {
            extraCriteria += " and g.external_id like " + ApiParameterHelper.sqlEncodeString(externalId);
        }

        if (name != null) {
            extraCriteria += " and g.name = " + ApiParameterHelper.sqlEncodeString(name);
        }

        if (hierarchy != null) {
            extraCriteria += " and o.hierarchy like " + ApiParameterHelper.sqlEncodeString(hierarchy + "%");
        }

        if (StringUtils.isNotBlank(extraCriteria)) {
            extraCriteria = extraCriteria.substring(4);
        }

        logger.info("extraCriteria; " + extraCriteria);

        return extraCriteria;
    }
}