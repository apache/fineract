package org.mifosng.platform.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
import org.mifosng.platform.exceptions.PlatformDataIntegrityException;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.ReportProcessingException;
import org.pentaho.reporting.engine.classic.core.modules.output.pageable.pdf.PdfReportUtil;
import org.pentaho.reporting.engine.classic.core.modules.output.table.csv.CSVReportUtil;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.HtmlReportUtil;
import org.pentaho.reporting.engine.classic.core.modules.output.table.xls.ExcelReportUtil;
import org.pentaho.reporting.libraries.resourceloader.Resource;
import org.pentaho.reporting.libraries.resourceloader.ResourceException;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
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
	private ReadExtraDataAndReportingService readExtraDataAndReportingService;

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
			GenericResultset result = this.readExtraDataAndReportingService
					.retrieveGenericResultset(".", ".", extractedQueryParams);
			String selectedFields = "";
			String json = this.jsonFormattingService.convertRequest(result,
					filterName, allowedFieldList, selectedFields,
					uriInfo.getQueryParameters());
			return Response.ok().entity(json).build();
		}

		StreamingOutput result = this.readExtraDataAndReportingService
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
	@Produces({ MediaType.APPLICATION_JSON, "application/x-msdownload",
			"application/vnd.ms-excel", "application/pdf", "text/html" })
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

		if (this.readExtraDataAndReportingService.getReportType(reportName)
				.equalsIgnoreCase("Pentaho")) {

			return processPentahoRequest(reportName,
					queryParams.getFirst("output-type"), extractedQueryParams);
		}

		String exportCSV = queryParams.getFirst("exportCSV");

		if ((exportCSV == null) || (!(exportCSV.equalsIgnoreCase("true")))) {
			GenericResultset result = this.readExtraDataAndReportingService
					.retrieveGenericResultset(reportName, parameterType,
							extractedQueryParams);

			String selectedFields = "";
			String json = this.jsonFormattingService.convertRequest(result,
					filterName, allowedFieldList, selectedFields,
					uriInfo.getQueryParameters());
			return Response.ok().entity(json).type(MediaType.APPLICATION_JSON)
					.build();
		}

		StreamingOutput result = this.readExtraDataAndReportingService
				.retrieveReportCSV(reportName, parameterType,
						extractedQueryParams);

		return Response
				.ok()
				.entity(result)
				.type("application/x-msdownload")
				.header("Content-Disposition",
						"attachment;filename=" + reportName.replaceAll(" ", "")
								+ ".csv").build();
	}

	private Response processPentahoRequest(String reportName,
			String outputType, Map<String, String> queryParams) {
		String reportPath = "C:\\dev\\apache-tomcat-7.0.25\\webapps\\ROOT\\PentahoReports\\"
				+ reportName + ".prpt";
		// String reportPath =
		// "/var/lib/tomcat6/webapps/ROOT/PentahoReports/"
		// + reportName + ".prpt";
		logger.info("Report path: " + reportPath);

		// load report definition
		ResourceManager manager = new ResourceManager();
		manager.registerDefaults();
		Resource res;

		logger.info("outputType: " + outputType);
		try {
			res = manager.createDirectly(reportPath, MasterReport.class);
			MasterReport masterReport = (MasterReport) res.getResource();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			if ("PDF".equalsIgnoreCase(outputType)) {
				PdfReportUtil.createPDF(masterReport, baos);
				return Response.ok().entity(baos.toByteArray())
						.type("application/pdf").build();
			}

			if ("XLS".equalsIgnoreCase(outputType)) {
				ExcelReportUtil.createXLS(masterReport, baos);
				return Response
						.ok()
						.entity(baos.toByteArray())
						.type("application/vnd.ms-excel")
						.header("Content-Disposition",
								"attachment;filename="
										+ reportName.replaceAll(" ", "")
										+ ".xls").build();
			}

			if ("CSV".equalsIgnoreCase(outputType)) {
				CSVReportUtil.createCSV(masterReport, baos, "UTF-8");
				return Response
						.ok()
						.entity(baos.toByteArray())
						.type("application/x-msdownload")
						.header("Content-Disposition",
								"attachment;filename="
										+ reportName.replaceAll(" ", "")
										+ ".csv").build();
			}

			if ("HTML".equalsIgnoreCase(outputType)) {
				HtmlReportUtil.createStreamHTML(masterReport, baos);
				return Response.ok().entity(baos.toByteArray()).build();
			}
		} catch (ResourceException e) {
			throw new PlatformDataIntegrityException(
					"error.msg.reporting.error", e.getMessage());
		} catch (ReportProcessingException e) {
			throw new PlatformDataIntegrityException(
					"error.msg.reporting.error", e.getMessage());
		} catch (IOException e) {
			throw new PlatformDataIntegrityException(
					"error.msg.reporting.error", e.getMessage());
		}

		throw new PlatformDataIntegrityException(
				"error.msg.invalid.outputType", "No matching Output Type: "
						+ outputType);

	}
}