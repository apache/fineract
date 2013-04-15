/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.dataqueries.api;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiParameterHelper;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.ToApiJsonSerializer;
import org.mifosplatform.infrastructure.dataqueries.data.GenericResultsetData;
import org.mifosplatform.infrastructure.dataqueries.data.ReportData;
import org.mifosplatform.infrastructure.dataqueries.service.GenericDataService;
import org.mifosplatform.infrastructure.dataqueries.service.ReadReportingService;
import org.mifosplatform.infrastructure.security.exception.NoAuthorizationException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.useradministration.domain.AppUser;
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

	private final String resourceNameForPermissions = "FUND";
	private final PlatformSecurityContext context;
	private final ToApiJsonSerializer<ReportData> toApiJsonSerializer;
	private final ReadReportingService readExtraDataAndReportingService;
	private final GenericDataService genericDataService;
	private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
	private final ApiRequestParameterHelper apiRequestParameterHelper;

	@Autowired
	public ReportsApiResource(
			final PlatformSecurityContext context,
			final ReadReportingService readExtraDataAndReportingService,
			final GenericDataService genericDataService,
			final ToApiJsonSerializer<ReportData> toApiJsonSerializer,
			PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
			final ApiRequestParameterHelper apiRequestParameterHelper) {
		this.context = context;
		this.readExtraDataAndReportingService = readExtraDataAndReportingService;
		this.genericDataService = genericDataService;
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

		final Collection<ReportData> result = this.readExtraDataAndReportingService
				.retrieveReportList();

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
	public String updateReport(@PathParam("id") final Long id, final String apiRequestBodyAsJson) {

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

	@GET
	@Path("{reportName}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON, "application/x-msdownload",
			"application/vnd.ms-excel", "application/pdf", "text/html" })
	public Response retrieveReport(
			@PathParam("reportName") final String reportName,
			@Context final UriInfo uriInfo) {

		MultivaluedMap<String, String> queryParams = uriInfo
				.getQueryParameters();

		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo
				.getQueryParameters());
		boolean exportCsv = ApiParameterHelper.exportCsv(uriInfo
				.getQueryParameters());
		boolean parameterType = ApiParameterHelper.parameterType(uriInfo
				.getQueryParameters());
		boolean exportPdf = ApiParameterHelper.exportPdf(uriInfo
				.getQueryParameters());

		checkUserPermissionForReport(reportName, parameterType);

		String parameterTypeValue = null;
		if (!parameterType) {
			parameterTypeValue = "report";
			if (this.readExtraDataAndReportingService.getReportType(reportName)
					.equalsIgnoreCase("Pentaho")) {
				Map<String, String> reportParams = getReportParams(queryParams,
						true);
				return this.readExtraDataAndReportingService
						.processPentahoRequest(reportName,
								queryParams.getFirst("output-type"),
								reportParams);
			}
		} else {
			parameterTypeValue = "parameter";
		}

		// PDF format

		if (exportPdf) {
			Map<String, String> reportParams = getReportParams(queryParams,
					false);
			String pdfFileName = this.readExtraDataAndReportingService
					.retrieveReportPDF(reportName, parameterTypeValue,
							reportParams);

			File file = new File(pdfFileName);

			ResponseBuilder response = Response.ok(file);
			response.header("Content-Disposition", "attachment; filename=\""
					+ pdfFileName + "\"");
			response.header("content-Type", "application/pdf");

			return response.build();

		}

		if (!exportCsv) {
			Map<String, String> reportParams = getReportParams(queryParams,
					false);

			GenericResultsetData result = this.readExtraDataAndReportingService
					.retrieveGenericResultset(reportName, parameterTypeValue,
							reportParams);

			String json = "";
			final boolean genericResultSetIsPassed = ApiParameterHelper
					.genericResultSetPassed(uriInfo.getQueryParameters());
			final boolean genericResultSet = ApiParameterHelper
					.genericResultSet(uriInfo.getQueryParameters());
			if (genericResultSetIsPassed) {
				if (genericResultSet) {
					json = this.toApiJsonSerializer.serializePretty(
							prettyPrint, result);
				} else {
					json = this.genericDataService
							.generateJsonFromGenericResultsetData(result);
				}
			} else {
				json = this.toApiJsonSerializer.serializePretty(prettyPrint,
						result);
			}

			return Response.ok().entity(json).type(MediaType.APPLICATION_JSON)
					.build();
		}

		// CSV Export
		Map<String, String> reportParams = getReportParams(queryParams, false);
		StreamingOutput result = this.readExtraDataAndReportingService
				.retrieveReportCSV(reportName, parameterTypeValue, reportParams);

		return Response
				.ok()
				.entity(result)
				.type("application/x-msdownload")
				.header("Content-Disposition",
						"attachment;filename=" + reportName.replaceAll(" ", "")
								+ ".csv").build();
	}

	private void checkUserPermissionForReport(final String reportName,
			final boolean parameterType) {

		// Anyone can run a 'report' that is simply getting possible parameter
		// (dropdown listbox) values.
		if (!parameterType) {
			AppUser currentUser = context.authenticatedUser();
			if (currentUser.hasNotPermissionForReport(reportName)) {
				throw new NoAuthorizationException(
						"Not authorised to run report: " + reportName);
			}
		}
	}

	private Map<String, String> getReportParams(
			final MultivaluedMap<String, String> queryParams,
			final Boolean isPentaho) {

		Map<String, String> reportParams = new HashMap<String, String>();
		Set<String> keys = queryParams.keySet();
		String pKey;
		String pValue;
		for (String k : keys) {

			if (k.startsWith("R_")) {
				if (isPentaho)
					pKey = k.substring(2);
				else
					pKey = "${" + k.substring(2) + "}";

				pValue = queryParams.get(k).get(0);
				reportParams.put(pKey, pValue);
			}
		}
		return reportParams;
	}
}