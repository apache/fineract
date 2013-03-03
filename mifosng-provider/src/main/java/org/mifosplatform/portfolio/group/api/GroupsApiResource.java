/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
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
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.api.ApiParameterHelper;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.ToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.group.PortfolioApiDataConversionService;
import org.mifosplatform.portfolio.group.command.GroupCommand;
import org.mifosplatform.portfolio.group.data.GroupAccountSummaryCollectionData;
import org.mifosplatform.portfolio.group.data.GroupData;
import org.mifosplatform.portfolio.group.service.GroupReadPlatformService;
import org.mifosplatform.portfolio.group.service.GroupWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/groups")
@Component
@Scope("singleton")
public class GroupsApiResource {

    private static final Set<String> GROUP_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id", "officeId", "name", "externalId",
            "clientMembers", "allowedClients", "allowedOffices" , "allowedParentGroups" ,"groupLevel" ,"allowedStaffs" ,"childGroups"));
    
    private final PlatformSecurityContext context;
    private final GroupReadPlatformService groupReadPlatformService;
    private final GroupWritePlatformService groupWritePlatformService;
    private final ToApiJsonSerializer<GroupData> toApiJsonSerializer;
    private final ToApiJsonSerializer<GroupAccountSummaryCollectionData> groupSummaryToApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioApiDataConversionService apiDataConversionService;

    @Autowired
    public GroupsApiResource(
            final PlatformSecurityContext context, 
            final GroupReadPlatformService groupReadPlatformService,
            final GroupWritePlatformService groupWritePlatformService,
            final ToApiJsonSerializer<GroupData> toApiJsonSerializer,
            final ToApiJsonSerializer<GroupAccountSummaryCollectionData> groupSummaryToApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper, 
            final PortfolioApiDataConversionService apiDataConversionService) {
        this.context = context;
        this.groupReadPlatformService = groupReadPlatformService;
        this.groupWritePlatformService = groupWritePlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.groupSummaryToApiJsonSerializer = groupSummaryToApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.apiDataConversionService = apiDataConversionService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAllGroups(@Context final UriInfo uriInfo, @QueryParam("sqlSearch") final String sqlSearch,
            @QueryParam("officeId") final Long officeId, @QueryParam("externalId") final String externalId,
            @QueryParam("name") final String name, @QueryParam("underHierarchy") final String hierarchy) {

        context.authenticatedUser().validateHasReadPermission("GROUP");

        final String extraCriteria = getGroupExtraCriteria(sqlSearch, officeId, externalId, name, hierarchy);
        Collection<GroupData> groups = this.groupReadPlatformService.retrieveAllGroups(extraCriteria);

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, groups, GROUP_DATA_PARAMETERS);
    }

    @GET
    @Path("{groupId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveGroupData(@Context final UriInfo uriInfo, @PathParam("groupId") final Long groupId) {

        context.authenticatedUser().validateHasReadPermission("GROUP");
        
        boolean template = ApiParameterHelper.template(uriInfo.getQueryParameters());
        GroupData group = this.groupReadPlatformService.retrieveGroupDetails(groupId, template);
        
        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, group, GROUP_DATA_PARAMETERS);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String newGroupDetails(@Context final UriInfo uriInfo, @QueryParam("officeId") final Long officeId,
            @QueryParam("levelId") final Long levelId) {

        context.authenticatedUser().validateHasReadPermission("GROUP");

        GroupData groupTemplateData = null;
        if (officeId != null && levelId != null) {
            groupTemplateData = this.groupReadPlatformService.retrieveNewGroupDetails(officeId, levelId);
        } else if (levelId != null) {
            groupTemplateData = this.groupReadPlatformService.retrieveNewGroupDetails(context.authenticatedUser().getOffice().getId(), levelId);
        }

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, groupTemplateData, GROUP_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createGroup(final String jsonRequestBody) {

        final GroupCommand command = this.apiDataConversionService.convertJsonToGroupCommand(null, jsonRequestBody);

        CommandProcessingResult entityIdentifier = this.groupWritePlatformService.createGroup(command);

        return this.toApiJsonSerializer.serialize(entityIdentifier);
    }
    
    @POST
    @Path("{groupId}/command/assign_loanofficer")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String assignLoanOfficer(@PathParam("groupId") final Long groupId, final String jsonRequestBody) {

        final GroupCommand command = this.apiDataConversionService.convertJsonToGroupCommand(groupId, jsonRequestBody);

        CommandProcessingResult entityIdentifier = this.groupWritePlatformService.assignLoanOfficer(command);

        return this.toApiJsonSerializer.serialize(entityIdentifier);
    }

    @POST
    @Path("{groupId}/command/unassign_loanofficer")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String unassignLoanOfficer(@PathParam("groupId") final Long groupId, final String jsonRequestBody) {

        final GroupCommand command = this.apiDataConversionService.convertJsonToGroupCommand(groupId, jsonRequestBody);

        CommandProcessingResult entityIdentifier = this.groupWritePlatformService.unassignLoanOfficer(command);

        return this.toApiJsonSerializer.serialize(entityIdentifier);
    }

    @PUT
    @Path("{groupId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateGroup(@PathParam("groupId") final Long groupId, final String jsonRequestBody) {

        final GroupCommand command = this.apiDataConversionService.convertJsonToGroupCommand(groupId, jsonRequestBody);

        CommandProcessingResult entityIdentifier = this.groupWritePlatformService.updateGroup(command);

        return this.toApiJsonSerializer.serialize(entityIdentifier);
    }

    @DELETE
    @Path("{groupId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteGroup(@PathParam("groupId") final Long groupId) {

        CommandProcessingResult entityIdentifier = this.groupWritePlatformService.deleteGroup(groupId);

        return this.toApiJsonSerializer.serialize(entityIdentifier);
    }

    @GET
    @Path("{groupId}/loans")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveGroupAccount(@PathParam("groupId") final Long groupId, @Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission("GROUP");

        GroupAccountSummaryCollectionData groupAccount = this.groupReadPlatformService.retrieveGroupAccountDetails(groupId);
        
        final Set<String> GROUP_ACCOUNTS_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("pendingApprovalLoans",
                "awaitingDisbursalLoans", "openLoans", "closedLoans", "anyLoanCount", "pendingApprovalLoanCount", "awaitingDisbursalLoanCount",
                "activeLoanCount", "closedLoanCount"));

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.groupSummaryToApiJsonSerializer.serialize(settings, groupAccount, GROUP_ACCOUNTS_DATA_PARAMETERS);
    }

    // 'g.' preffix because of ERROR 1052 (23000): Column 'column_name' in where
    // clause is ambiguous
    // caused by the same name of columns in m_office and m_group tables
    private String getGroupExtraCriteria(String sqlSearch, final Long officeId, final String externalId, final String name, final String hierarchy) {

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

        return extraCriteria;
    }
}