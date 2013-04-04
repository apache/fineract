/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.commands.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.commands.data.AuditData;
import org.mifosplatform.commands.data.AuditSearchData;
import org.mifosplatform.commands.service.AuditReadPlatformService;
import org.mifosplatform.infrastructure.core.api.ApiParameterHelper;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/audits")
@Component
@Scope("singleton")
public class AuditsApiResource {

	private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<String>(
			Arrays.asList("id", "actionName", "entityName", "resourceId",
					"subresourceId", "maker", "madeOnDate", "checker",
					"checkedOnDate", "processingResult", "commandAsJson",
					"officeName", "groupLevelName", "groupName", "clientName",
					"loanAccountNo", "savingsAccountNo"));

	private final String resourceNameForPermissions = "AUDIT";

	private final PlatformSecurityContext context;
	private final AuditReadPlatformService auditReadPlatformService;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final DefaultToApiJsonSerializer<AuditData> toApiJsonSerializer;
	private final DefaultToApiJsonSerializer<AuditSearchData> toApiJsonSerializerSearchTemplate;

	@Autowired
	public AuditsApiResource(
			final PlatformSecurityContext context,
			final AuditReadPlatformService auditReadPlatformService,
			final ApiRequestParameterHelper apiRequestParameterHelper,
			final DefaultToApiJsonSerializer<AuditData> toApiJsonSerializer,
			final DefaultToApiJsonSerializer<AuditSearchData> toApiJsonSerializerSearchTemplate) {
		this.context = context;
		this.auditReadPlatformService = auditReadPlatformService;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.toApiJsonSerializerSearchTemplate = toApiJsonSerializerSearchTemplate;
	}

	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAuditEntries(
			@Context final UriInfo uriInfo,
			@QueryParam("actionName") final String actionName,
			@QueryParam("entityName") final String entityName,
			@QueryParam("resourceId") final Long resourceId,
			@QueryParam("makerId") final Long makerId,
			@QueryParam("makerDateTimeFrom") final String makerDateTimeFrom,
			@QueryParam("makerDateTimeTo") final String makerDateTimeTo,
			@QueryParam("checkerId") final Long checkerId,
			@QueryParam("checkerDateTimeFrom") final String checkerDateTimeFrom,
			@QueryParam("checkerDateTimeTo") final String checkerDateTimeTo,
			@QueryParam("processingResult") final Integer processingResult,
			@QueryParam("officeId") final Integer officeId,
			@QueryParam("groupId") final Integer groupId,
			@QueryParam("clientId") final Integer clientId,
			@QueryParam("loanid") final Integer loanId,
			@QueryParam("savingsAccountId") final Integer savingsAccountId) {

		context.authenticatedUser().validateHasReadPermission(
				resourceNameForPermissions);

		final String extraCriteria = getExtraCriteria(actionName, entityName,
				resourceId, makerId, makerDateTimeFrom, makerDateTimeTo,
				checkerId, checkerDateTimeFrom, checkerDateTimeTo,
				processingResult, officeId, groupId, clientId, loanId,
				savingsAccountId);

		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper
				.process(uriInfo.getQueryParameters());

		final Collection<AuditData> auditEntries = this.auditReadPlatformService
				.retrieveAuditEntries(extraCriteria, settings.isIncludeJson());

		return this.toApiJsonSerializer.serialize(settings, auditEntries,
				RESPONSE_DATA_PARAMETERS);
	}

	@GET
	@Path("{auditId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAuditEntry(@PathParam("auditId") final Long auditId,
			@Context final UriInfo uriInfo) {

		context.authenticatedUser().validateHasReadPermission(
				resourceNameForPermissions);

		final AuditData auditEntry = this.auditReadPlatformService
				.retrieveAuditEntry(auditId);

		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper
				.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, auditEntry,
				RESPONSE_DATA_PARAMETERS);
	}

	@GET
	@Path("/searchtemplate")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAuditSearchTemplate(@Context final UriInfo uriInfo) {

		context.authenticatedUser().validateHasReadPermission(
				resourceNameForPermissions);

		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper
				.process(uriInfo.getQueryParameters());

		final AuditSearchData auditSearchData = this.auditReadPlatformService
				.retrieveSearchTemplate("audit");

		final Set<String> RESPONSE_DATA_PARAMETERS_SEARCH_TEMPLATE = new HashSet<String>(
				Arrays.asList("appUsers", "actionNames", "entityNames",
						"processingResults"));

		return this.toApiJsonSerializerSearchTemplate.serialize(settings,
				auditSearchData, RESPONSE_DATA_PARAMETERS_SEARCH_TEMPLATE);
	}

	private String getExtraCriteria(final String actionName,
			final String entityName, final Long resourceId, final Long makerId,
			final String makerDateTimeFrom, final String makerDateTimeTo,
			final Long checkerId, final String checkerDateTimeFrom,
			final String checkerDateTimeTo, final Integer processingResult,
			final Integer officeId, final Integer groupId,
			final Integer clientId, final Integer loanId,
			final Integer savingsAccountId) {

		String extraCriteria = "";

		if (actionName != null) {
			extraCriteria += " and aud.action_name = "
					+ ApiParameterHelper.sqlEncodeString(actionName);
		}
		if (entityName != null) {
			extraCriteria += " and aud.entity_name like "
					+ ApiParameterHelper.sqlEncodeString(entityName + "%");
		}

		if (resourceId != null) {
			extraCriteria += " and aud.resource_id = " + resourceId;
		}
		if (makerId != null) {
			extraCriteria += " and aud.maker_id = " + makerId;
		}
		if (checkerId != null) {
			extraCriteria += " and aud.checker_id = " + checkerId;
		}
		if (makerDateTimeFrom != null) {
			extraCriteria += " and aud.made_on_date >= "
					+ ApiParameterHelper.sqlEncodeString(makerDateTimeFrom);
		}
		if (makerDateTimeTo != null) {
			extraCriteria += " and aud.made_on_date <= "
					+ ApiParameterHelper.sqlEncodeString(makerDateTimeTo);
		}
		if (checkerDateTimeFrom != null) {
			extraCriteria += " and aud.checked_on_date >= "
					+ ApiParameterHelper.sqlEncodeString(checkerDateTimeFrom);
		}
		if (checkerDateTimeTo != null) {
			extraCriteria += " and aud.checked_on_date <= "
					+ ApiParameterHelper.sqlEncodeString(checkerDateTimeTo);
		}

		if (processingResult != null) {
			extraCriteria += " and aud.processing_result_enum = "
					+ processingResult;
		}

		if (officeId != null) {
			extraCriteria += " and aud.office_id = " + officeId;
		}

		if (groupId != null) {
			extraCriteria += " and aud.group_id = " + groupId;
		}

		if (clientId != null) {
			extraCriteria += " and aud.client_id = " + clientId;
		}

		if (loanId != null) {
			extraCriteria += " and aud.loan_id = " + loanId;
		}

		if (savingsAccountId != null) {
			extraCriteria += " and aud.savings_account_id = " + savingsAccountId;
		}

		if (StringUtils.isNotBlank(extraCriteria)) {
			extraCriteria = extraCriteria.substring(4);
		}

		return extraCriteria;
	}
}