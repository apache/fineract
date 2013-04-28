/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.dataqueries.api;

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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.ToApiJsonSerializer;
import org.mifosplatform.infrastructure.dataqueries.data.ReportData;
import org.mifosplatform.infrastructure.dataqueries.service.ReadReportingService;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/reports")
@Component
@Scope("singleton")
public class ReportsApiResource {

	private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<String>(
			Arrays.asList("id", "reportName", "reportType", "reportSubType",
					"reportCategory", "description", "reportSql", "coreReport",
					"useReport", "reportParameters"));

	private final String resourceNameForPermissions = "REPORT";
	private final PlatformSecurityContext context;
	private final ToApiJsonSerializer<ReportData> toApiJsonSerializer;
	private final ReadReportingService readReportingService;
	private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
	private final ApiRequestParameterHelper apiRequestParameterHelper;

	@Autowired
	public ReportsApiResource(
			final PlatformSecurityContext context,
			final ReadReportingService readReportingService,
			final ToApiJsonSerializer<ReportData> toApiJsonSerializer,
			PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
			final ApiRequestParameterHelper apiRequestParameterHelper) {
		this.context = context;
		this.readReportingService = readReportingService;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
	}

	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveReportList(@Context final UriInfo uriInfo) {

		context.authenticatedUser().validateHasReadPermission(
				resourceNameForPermissions);

		final Collection<ReportData> result = this.readReportingService
				.retrieveReportList();

		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper
				.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, result,
				RESPONSE_DATA_PARAMETERS);
	}

	@GET
	@Path("{id}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveReport(@PathParam("id") final Long id,
			@Context final UriInfo uriInfo) {

		context.authenticatedUser().validateHasReadPermission(
				resourceNameForPermissions);

		final ReportData result = this.readReportingService
				.retrieveReport(id);

		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper
				.process(uriInfo.getQueryParameters());

		if (settings.isTemplate()) {
			result.appendedTemplate(this.readReportingService
					.getAllowedParameters());
		}
		return this.toApiJsonSerializer.serialize(settings, result,
				RESPONSE_DATA_PARAMETERS);
	}

	@GET
	@Path("template")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveOfficeTemplate(@Context final UriInfo uriInfo) {

		context.authenticatedUser().validateHasReadPermission(
				resourceNameForPermissions);

		ReportData result = new ReportData();
		result.appendedTemplate(this.readReportingService
				.getAllowedParameters());

		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper
				.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, result,
				RESPONSE_DATA_PARAMETERS);
	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createReport(final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder()
				.createReport().withJson(apiRequestBodyAsJson).build();

		final CommandProcessingResult result = this.commandsSourceWritePlatformService
				.logCommandSource(commandRequest);

		return this.toApiJsonSerializer.serialize(result);
	}

	@PUT
	@Path("{id}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String updateReport(@PathParam("id") final Long id,
			final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder()
				.updateReport(id).withJson(apiRequestBodyAsJson).build();

		final CommandProcessingResult result = this.commandsSourceWritePlatformService
				.logCommandSource(commandRequest);

		return this.toApiJsonSerializer.serialize(result);
	}

	@DELETE
	@Path("{id}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String deleteReport(@PathParam("id") final Long id) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder()
				.deleteReport(id).build();

		final CommandProcessingResult result = this.commandsSourceWritePlatformService
				.logCommandSource(commandRequest);

		return this.toApiJsonSerializer.serialize(result);
	}

}