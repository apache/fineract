package org.mifosng.platform.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.mifosng.data.reports.GenericResultset;
import org.mifosng.platform.ReadExtraDataAndReportingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/v1/reports")
@Component
@Scope("singleton")
public class ReportingApiResource {

	private final static Logger logger = LoggerFactory
			.getLogger(ReportingApiResource.class);

	@Autowired
	private ReadExtraDataAndReportingService ReadExtraDataAndReportingService;

	private String _corsHeaders;

	private Response makeCORS(ResponseBuilder req, String returnMethod) {
		ResponseBuilder rb = req.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, OPTIONS");

		if (!"".equals(returnMethod)) {
			rb.header("Access-Control-Allow-Headers", returnMethod);
		}

		return rb.build();
	}

	private Response makeCORS(ResponseBuilder req) {
		return makeCORS(req, _corsHeaders);
	}

	private Response makeCORSExport(ResponseBuilder req, String exportFilename,
			String returnMethod) {
		ResponseBuilder rb = req
				.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
				.header("Content-Disposition",
						"attachment;filename=" + exportFilename);

		if (!"".equals(returnMethod)) {
			rb.header("Access-Control-Allow-Headers", returnMethod);
		}

		return rb.build();
	}

	private Response makeCORSExport(ResponseBuilder req, String exportFilename) {
		return makeCORSExport(req, exportFilename, _corsHeaders);
	}

	// This OPTIONS request/response is necessary
	// if you consumes other format than text/plain or
	// if you use other HTTP verbs than GET and POST
	@OPTIONS
	public Response corsMyResource(
			@HeaderParam("Access-Control-Request-Headers") String requestH) {
		_corsHeaders = requestH;
		return makeCORS(Response.ok(), requestH);
	}

	@GET
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, "application/x-msdownload" })
	public Response retrieveReport(@Context UriInfo uriInfo,
			@Context HttpServletResponse httpServletResponse) {

		MultivaluedMap<String, String> queryParams = uriInfo
				.getQueryParameters();
		String name = queryParams.getFirst("MRP_Name");
		String type = queryParams.getFirst("MRP_Type");
		String exportCSV = queryParams.getFirst("exportCSV");

		Map<String, String> extractedQueryParams = new HashMap<String, String>();

		Set<String> keys = queryParams.keySet();
		String pKey;
		String pValue;
		for (String k : keys) {

			if (k.startsWith("MRP_")) {
				pKey = "${" + k.substring(4) + "}";
				pValue = queryParams.get(k).get(0);

				extractedQueryParams.put(pKey, pValue);
			}
		}
		logger.info("BEGINNING REQUEST FOR: " + name);

		if ((exportCSV == null) || (!(exportCSV.equalsIgnoreCase("true")))) {
			GenericResultset result = this.ReadExtraDataAndReportingService
					.retrieveGenericResultset(name, type, extractedQueryParams);
			return makeCORS(Response.ok().entity(result));
		} else {
			StreamingOutput result = this.ReadExtraDataAndReportingService
					.retrieveReportCSV(name, type, extractedQueryParams);

			return makeCORSExport(Response.ok().entity(result),
					name.replaceAll(" ", "") + ".csv");
		}

	}

	@GET
	@Path("forceauth")
	public Response hackToForceAuthentication() {
		return Response.ok().build();
	}
}