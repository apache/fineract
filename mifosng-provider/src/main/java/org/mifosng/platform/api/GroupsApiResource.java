package org.mifosng.platform.api;

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

import org.mifosng.platform.api.commands.GroupCommand;
import org.mifosng.platform.api.data.ClientLookup;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.api.data.GroupAccountSummaryCollectionData;
import org.mifosng.platform.api.data.GroupData;
import org.mifosng.platform.api.data.OfficeLookup;
import org.mifosng.platform.api.infrastructure.ApiDataConversionService;
import org.mifosng.platform.api.infrastructure.ApiJsonSerializerService;
import org.mifosng.platform.api.infrastructure.ApiParameterHelper;
import org.mifosng.platform.client.service.ClientReadPlatformService;
import org.mifosng.platform.group.service.GroupReadPlatformService;
import org.mifosng.platform.group.service.GroupWritePlatformService;
import org.mifosng.platform.organisation.service.OfficeReadPlatformService;
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
    private ApiDataConversionService apiDataConversionService;

    @Autowired
    private OfficeReadPlatformService officeReadPlatformService;

	@Autowired
	private ApiJsonSerializerService apiJsonSerializerService;

    private static final Set<String> typicalResponseParameters = new HashSet<String>(
            Arrays.asList("id", "officeId", "name", "externalId", "clientMembers")
    );

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAllGroups(@Context final UriInfo uriInfo){

        Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        if (responseParameters.isEmpty()) {
            responseParameters.addAll(typicalResponseParameters);
        }

        boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        Collection<GroupData> groups = this.groupReadPlatformService.retrieveAllGroups();

        return this.apiJsonSerializerService.serializeGroupDataToJson(prettyPrint, responseParameters, groups);
    }

    @GET
    @Path("{groupId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveGroupData(@Context final UriInfo uriInfo,
                                    @PathParam("groupId") final Long groupId,
                                    @QueryParam("officeId") final Long officeId) {

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

            final String extraCriteria;
            if (officeId != null){
                extraCriteria = getGroupExtraCriteria(officeId);
            } else {
                extraCriteria = getGroupExtraCriteria(group.getOfficeId());
            }

			availableClients = this.clientReadPlatformService.retrieveAllIndividualClientsForLookup(extraCriteria);
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

        Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        if (responseParameters.isEmpty()) {
            responseParameters.addAll(typicalResponseParameters);
        }
        boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        responseParameters.addAll(Arrays.asList("allowedClients", "allowedOffices"));

        final String extraCriteria = getGroupExtraCriteria(officeId);

        GroupData groupData = this.groupReadPlatformService.retrieveNewGroupDetails(extraCriteria);

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

    private String getGroupExtraCriteria(Long officeId){

        String extraCriteria = "";

        if (officeId != null) {
            extraCriteria += " office_id = " + officeId;
        }

        logger.info("extraCriteria; " + extraCriteria);

        return extraCriteria;
    }
}