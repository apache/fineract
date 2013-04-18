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
import org.mifosplatform.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.ToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.client.data.ClientData;
import org.mifosplatform.portfolio.client.service.ClientReadPlatformService;
import org.mifosplatform.portfolio.group.data.GroupAccountSummaryCollectionData;
import org.mifosplatform.portfolio.group.data.GroupGeneralData;
import org.mifosplatform.portfolio.group.service.CenterReadPlatformService;
import org.mifosplatform.portfolio.group.service.GroupReadPlatformService;
import org.mifosplatform.portfolio.group.service.SearchParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Path("/groups")
@Component
@Scope("singleton")
public class GroupsApiResource {

    private final PlatformSecurityContext context;
    private final GroupReadPlatformService groupReadPlatformService;
    private final CenterReadPlatformService centerReadPlatformService;
    private final ClientReadPlatformService clientReadPlatformService;
    private final ToApiJsonSerializer<Object> toApiJsonSerializer;
    private final ToApiJsonSerializer<GroupGeneralData> groupGeneralApiJsonSerializer;
    private final ToApiJsonSerializer<GroupAccountSummaryCollectionData> groupSummaryToApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public GroupsApiResource(final PlatformSecurityContext context, final GroupReadPlatformService groupReadPlatformService,
            final CenterReadPlatformService centerReadPlatformService, final ClientReadPlatformService clientReadPlatformService,
            final ToApiJsonSerializer<Object> toApiJsonSerializer,
            final ToApiJsonSerializer<GroupGeneralData> groupTopOfHierarchyApiJsonSerializer,
            final ToApiJsonSerializer<GroupAccountSummaryCollectionData> groupSummaryToApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.context = context;
        this.groupReadPlatformService = groupReadPlatformService;
        this.centerReadPlatformService = centerReadPlatformService;
        this.clientReadPlatformService = clientReadPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.groupGeneralApiJsonSerializer = groupTopOfHierarchyApiJsonSerializer;
        this.groupSummaryToApiJsonSerializer = groupSummaryToApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveTemplate(@Context final UriInfo uriInfo, @QueryParam("officeId") final Long officeId,
            @QueryParam("center") final boolean isCenterGroup,
            @QueryParam("centerId") final Long centerId) {

        this.context.authenticatedUser().validateHasReadPermission(GroupingTypesApiConstants.GROUP_RESOURCE_NAME);

        if (centerId != null) {
            final GroupGeneralData centerGroupTemplate = this.centerReadPlatformService.retrieveCenterGroupTemplate(centerId);
            final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
            return this.groupGeneralApiJsonSerializer.serialize(settings, centerGroupTemplate,
                    GroupingTypesApiConstants.CENTER_GROUP_RESPONSE_DATA_PARAMETERS);
        }

        final GroupGeneralData groupTemplate = this.groupReadPlatformService.retrieveTemplate(officeId, isCenterGroup);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.groupGeneralApiJsonSerializer.serialize(settings, groupTemplate,
                GroupingTypesApiConstants.GROUP_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAll(@Context final UriInfo uriInfo, @QueryParam("sqlSearch") final String sqlSearch,
            @QueryParam("officeId") final Long officeId, @QueryParam("externalId") final String externalId,
            @QueryParam("name") final String name, @QueryParam("underHierarchy") final String hierarchy) {

        this.context.authenticatedUser().validateHasReadPermission(GroupingTypesApiConstants.GROUP_RESOURCE_NAME);

        final SearchParameters searchCriteria = SearchParameters.from(sqlSearch, officeId, externalId, name, hierarchy);
        final Collection<GroupGeneralData> groups = this.groupReadPlatformService.retrieveAll(searchCriteria);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.groupGeneralApiJsonSerializer.serialize(settings, groups, GroupingTypesApiConstants.GROUP_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{groupId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveOne(@Context final UriInfo uriInfo, @PathParam("groupId") final Long groupId) {

        this.context.authenticatedUser().validateHasReadPermission(GroupingTypesApiConstants.GROUP_RESOURCE_NAME);
        final Set<String> associationParameters = ApiParameterHelper.extractAssociationsForResponseIfProvided(uriInfo.getQueryParameters());

        GroupGeneralData group = this.groupReadPlatformService.retrieveOne(groupId);
        
        // associations
        Collection<ClientData> membersOfGroup = null;
        if (!associationParameters.isEmpty()) {
            if (associationParameters.contains("clientMembers")) {
                membersOfGroup = this.clientReadPlatformService.retrieveClientMembersOfGroup(groupId);
                if (CollectionUtils.isEmpty(membersOfGroup)) {
                    membersOfGroup = null;
                }
            }
        }
        
        final boolean template = ApiParameterHelper.template(uriInfo.getQueryParameters());
        if (template) {
            final GroupGeneralData templateGroup = this.groupReadPlatformService.retrieveTemplate(group.officeId(), false);
            group = GroupGeneralData.withTemplateAndAssociations(templateGroup, group, membersOfGroup);
        } else {
            group = GroupGeneralData.withAssocations(group, membersOfGroup);
        }

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.groupGeneralApiJsonSerializer.serialize(settings, group, GroupingTypesApiConstants.GROUP_RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String create(final String apiRequestBodyAsJson) {

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
    public String update(@PathParam("groupId") final Long groupId, final String apiRequestBodyAsJson) {

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
    public String delete(@PathParam("groupId") final Long groupId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .deleteGroup(groupId) //
                .build(); //
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("{groupId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String activate(@PathParam("groupId") final Long groupId, @QueryParam("command") final String commandParam,
            final String apiRequestBodyAsJson) {

        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);

        CommandProcessingResult result = null;
        if (is(commandParam, "activate")) {
            final CommandWrapper commandRequest = builder.activateGroup(groupId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        }

        if (result == null) {
            //
            throw new UnrecognizedQueryParamException("command", commandParam, new Object[] { "activate" });
        }

        return this.toApiJsonSerializer.serialize(result);
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

    @GET
    @Path("{groupId}/loans")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveLoans(@PathParam("groupId") final Long groupId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission("GROUP");

        final GroupAccountSummaryCollectionData groupAccount = this.groupReadPlatformService.retrieveGroupAccountDetails(groupId);

        final Set<String> GROUP_ACCOUNTS_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("pendingApprovalLoans",
                "awaitingDisbursalLoans", "openLoans", "closedLoans", "anyLoanCount", "pendingApprovalLoanCount",
                "awaitingDisbursalLoanCount", "activeLoanCount", "closedLoanCount"));

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.groupSummaryToApiJsonSerializer.serialize(settings, groupAccount, GROUP_ACCOUNTS_DATA_PARAMETERS);
    }
}