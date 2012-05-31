package org.mifosng.platform.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.mifosng.platform.ReadExtraDataAndReportingService;
import org.mifosng.platform.api.data.GenericResultset;
import org.mifosng.platform.api.infrastructure.ApiJSONFormattingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/reports")
@Component
@Scope("singleton")
public class ReportingApiResource {

	private final static Logger logger = LoggerFactory
			.getLogger(ReportingApiResource.class);

	private String allowedFieldList = "";
	private String filterName = "myFilter";

	@Autowired
	private ReadExtraDataAndReportingService ReadExtraDataAndReportingService;

	@Autowired
	private ApiJSONFormattingService jsonFormattingService;

	@GET
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, "application/x-msdownload" })
	public Response retrieveReportList(@Context UriInfo uriInfo) {

		MultivaluedMap<String, String> queryParams = uriInfo
				.getQueryParameters();
		Map<String, String> extractedQueryParams = new HashMap<String, String>();

		String exportCSV = queryParams.getFirst("exportCSV");

		if ((exportCSV == null) || (!(exportCSV.equalsIgnoreCase("true")))) {
			GenericResultset result = this.ReadExtraDataAndReportingService
					.retrieveGenericResultset(".", ".", extractedQueryParams);
			String selectedFields = "";
			String json = this.jsonFormattingService.convertRequest(result,
					filterName, allowedFieldList, selectedFields,
					uriInfo.getQueryParameters());
			return Response.ok().entity(json).build();
		}

		StreamingOutput result = this.ReadExtraDataAndReportingService
				.retrieveReportCSV(".", ".", extractedQueryParams);

		return Response
				.ok()
				.entity(result)
				.header("Content-Disposition",
						"attachment;filename=ReportList.csv").build();
	}

	@GET
	@Path("{reportName}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, "application/x-msdownload" })
	public Response retrieveReport(
			@PathParam("reportName") final String reportName,
			@Context UriInfo uriInfo) {

		MultivaluedMap<String, String> queryParams = uriInfo
				.getQueryParameters();
		String parameterType = queryParams.getFirst("parameterType");
		if ((parameterType == null)
				|| (!(parameterType.equalsIgnoreCase("true")))) {
			parameterType = "report";
		} else {
			parameterType = "parameter";
		}
		String exportCSV = queryParams.getFirst("exportCSV");

		Map<String, String> extractedQueryParams = new HashMap<String, String>();

		Set<String> keys = queryParams.keySet();
		String pKey;
		String pValue;
		for (String k : keys) {

			if (k.startsWith("R_")) {
				pKey = "${" + k.substring(2) + "}";
				pValue = queryParams.get(k).get(0);
				extractedQueryParams.put(pKey, pValue);
			}
		}
		logger.info("BEGINNING REQUEST FOR: " + reportName);

		if ((exportCSV == null) || (!(exportCSV.equalsIgnoreCase("true")))) {
			GenericResultset result = this.ReadExtraDataAndReportingService
					.retrieveGenericResultset(reportName, parameterType,
							extractedQueryParams);

			String selectedFields = "";
			String json = this.jsonFormattingService.convertRequest(result,
					filterName, allowedFieldList, selectedFields,
					uriInfo.getQueryParameters());
			return Response.ok().entity(json).build();
		}

		StreamingOutput result = this.ReadExtraDataAndReportingService
				.retrieveReportCSV(reportName, parameterType,
						extractedQueryParams);

		return Response
				.ok()
				.entity(result)
				.header("Content-Disposition",
						"attachment;filename=" + reportName.replaceAll(" ", "")
								+ ".csv").build();
	}
}