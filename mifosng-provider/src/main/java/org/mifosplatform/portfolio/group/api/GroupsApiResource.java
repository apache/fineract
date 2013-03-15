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
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiParameterHelper;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.ToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.group.data.GroupAccountSummaryCollectionData;
import org.mifosplatform.portfolio.group.data.GroupData;
import org.mifosplatform.portfolio.group.service.GroupReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/groups")
@Component
@Scope("singleton")
public class GroupsApiResource {

    /*
     * GROUP_DATA_PARAMETERS is used by ToApiJsonSerializer<E>, make sure E's properties and E_PARAMETERS are in same 
     */
    private static final Set<String> GROUP_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id", "name", "externalId", "officeId",
            "officeName", "staffId", "staffName", "parentId", "parentName", "hierarchy", "groupSummaryData", "groupLevelData",
            "clientMembers", "allowedClients", "allowedOffices", "allowedParentGroups", "allowedStaffs", "childGroups"));
    
    private final PlatformSecurityContext context;
    private final GroupReadPlatformService groupReadPlatformService;
    private final ToApiJsonSerializer<GroupData> toApiJsonSerializer;
    private final ToApiJsonSerializer<GroupAccountSummaryCollectionData> groupSummaryToApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public GroupsApiResource(final PlatformSecurityContext context, final GroupReadPlatformService groupReadPlatformService,
            final ToApiJsonSerializer<GroupData> toApiJsonSerializer,
            final ToApiJsonSerializer<GroupAccountSummaryCollectionData> groupSummaryToApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.context = context;
        this.groupReadPlatformService = groupReadPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.groupSummaryToApiJsonSerializer = groupSummaryToApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAllGroups(@Context final UriInfo uriInfo, @QueryParam("sqlSearch") final String sqlSearch,
            @QueryParam("officeId") final Long officeId, @QueryParam("externalId") final String externalId,
            @QueryParam("name") final String name, @QueryParam("underHierarchy") final String hierarchy,
            @QueryParam("levelId") final Long levelId) {

        this.context.authenticatedUser().validateHasReadPermission("GROUP");

        final String extraCriteria = getGroupExtraCriteria(sqlSearch, officeId, externalId, name, hierarchy, levelId);
        final Collection<GroupData> groups = this.groupReadPlatformService.retrieveAllGroups(extraCriteria);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, groups, GROUP_DATA_PARAMETERS);
    }

    @GET
    @Path("{groupId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveGroupData(@Context final UriInfo uriInfo, @PathParam("groupId") final Long groupId) {

        this.context.authenticatedUser().validateHasReadPermission("GROUP");

        final boolean template = ApiParameterHelper.template(uriInfo.getQueryParameters());
        final GroupData group = this.groupReadPlatformService.retrieveGroupDetails(groupId, template);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, group, GROUP_DATA_PARAMETERS);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String newGroupDetails(@Context final UriInfo uriInfo, @QueryParam("officeId") final Long officeId,
            @QueryParam("levelId") final Long levelId, @QueryParam("parentGroupId") final Long parentGroupId) {

        this.context.authenticatedUser().validateHasReadPermission("GROUP");

        GroupData groupTemplateData = null;
        if (levelId != null && parentGroupId != null) {
            groupTemplateData = this.groupReadPlatformService.retrieveNewChildGroupDetails(officeId, levelId, parentGroupId);
        } else if (officeId != null && levelId != null) {
            groupTemplateData = this.groupReadPlatformService.retrieveNewGroupDetails(officeId, levelId);
        } else if (levelId != null) {
            groupTemplateData = this.groupReadPlatformService.retrieveNewGroupDetails(this.context.authenticatedUser().getOffice().getId(),
                    levelId);
        }

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, groupTemplateData, GROUP_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createGroup(final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .createGroup() //
                .withJson(apiRequestBodyAsJson) //
                .build(); //
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);

    }

    @POST
    @Path("{groupId}/command/unassign_staff")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String unassignLoanOfficer(@PathParam("groupId") final Long groupId, final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .unassignStaff(groupId) //
                .withJson(apiRequestBodyAsJson) //
                .build(); //
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);

    }

    @PUT
    @Path("{groupId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateGroup(@PathParam("groupId") final Long groupId, final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .updateGroup(groupId) //
                .withJson(apiRequestBodyAsJson) //
                .build(); //
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{groupId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteGroup(@PathParam("groupId") final Long groupId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .deleteGroup(groupId) //
                .build(); //
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);

    }

    @GET
    @Path("{groupId}/loans")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveGroupAccount(@PathParam("groupId") final Long groupId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission("GROUP");

        final GroupAccountSummaryCollectionData groupAccount = this.groupReadPlatformService.retrieveGroupAccountDetails(groupId);

        final Set<String> GROUP_ACCOUNTS_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("pendingApprovalLoans",
                "awaitingDisbursalLoans", "openLoans", "closedLoans", "anyLoanCount", "pendingApprovalLoanCount",
                "awaitingDisbursalLoanCount", "activeLoanCount", "closedLoanCount"));

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.groupSummaryToApiJsonSerializer.serialize(settings, groupAccount, GROUP_ACCOUNTS_DATA_PARAMETERS);
    }

    // 'g.' preffix because of ERROR 1052 (23000): Column 'column_name' in where
    // clause is ambiguous
    // caused by the same name of columns in m_office and m_group tables
    private String getGroupExtraCriteria(String sqlSearch, final Long officeId, final String externalId, final String name,
            final String hierarchy, final Long levelId) {

        String extraCriteria = "";

        if (sqlSearch != null) {
            sqlSearch = sqlSearch.replaceAll(" name ", " g.name ");
            sqlSearch = sqlSearch.replaceAll("name ", "g.name ");
            extraCriteria = " and (" + sqlSearch + ")";
        }

        if (officeId != null) {
            extraCriteria += " and g.office_id = " + officeId;
        }

        if (levelId != null) {
            extraCriteria += " and g.level_Id = " + levelId;
        }

        if (externalId != null) {
            extraCriteria += " and g.external_id = " + ApiParameterHelper.sqlEncodeString(externalId);
        }

        if (name != null) {
            extraCriteria += " and g.name like " + ApiParameterHelper.sqlEncodeString(name + "%");
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