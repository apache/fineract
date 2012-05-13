package org.mifosng.platform.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mifosng.data.reports.GenericResultset;
import org.mifosng.data.reports.ResultsetColumnHeader;
import org.mifosng.data.reports.ResultsetDataRow;
import org.mifosng.platform.ReadExtraDataAndReportingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.ServletContextAware;

@Controller
public class ExportCSVController implements ServletContextAware {

	private final static Logger logger = LoggerFactory
			.getLogger(ExportCSVController.class);

	private final ReadExtraDataAndReportingService readExtraDataAndReportingService;

	@Autowired
	public ExportCSVController(
			final ReadExtraDataAndReportingService readExtraDataAndReportingService) {
		logger.info("ExportCSVController started");
		this.readExtraDataAndReportingService = readExtraDataAndReportingService;
	}

	@ExceptionHandler(AccessDeniedException.class)
	public String accessDeniedException() {
		return "unAuthorizedAction";
	}

	/*
	 * @ExceptionHandler(ClientValidationException.class) public @ResponseBody
	 * Collection<ErrorResponse> validationException(ClientValidationException
	 * ex, HttpServletResponse response) {
	 * 
	 * response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	 * response.setContentType("application/json");
	 * 
	 * return ex.getValidationErrors(); }
	 */
	@RequestMapping(value = "/exportcsv", method = RequestMethod.GET)
	public void handleExportCSV(final HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		logger.info("handleExportCSV started");
		@SuppressWarnings("unchecked")
		Map<String, String[]> queryParams = request.getParameterMap();

		String rptDB = queryParams.get("MRP_rptDB")[0];
		String name = queryParams.get("MRP_Name")[0];
		String type = queryParams.get("MRP_Type")[0];

		Set<String> keys = queryParams.keySet();
		Map<String, String> extractedQueryParams = new HashMap<String, String>();
		String pKey;
		String pValue;
		for (String k : keys) {

			if (k.startsWith("MRP_")) {
				pKey = "${" + k.substring(4) + "}";
				pValue = queryParams.get(k)[0];
				// logger.info(name + ":  " + pKey + ": " + pValue);
				extractedQueryParams.put(pKey, pValue);
			}
		}

		// if (extractedQueryParams.size() > 3) {
		// throw new
		// UnsupportedOperationException("Not able to support this report for export to CSV right now.");
		// }

		GenericResultset result = this.readExtraDataAndReportingService
				.retrieveGenericResultset(rptDB, name, type,
						extractedQueryParams);

		// response.setContentType("application/octet-stream");
		response.setContentType("application/x-msdownload");

		response.setHeader("Content-Disposition",
				"attachment;filename=" + name.replaceAll(" ", "") + ".csv");

		ServletOutputStream out = response.getOutputStream();
		StringBuffer sb = generateCsvFileBuffer(result);

		InputStream in = new ByteArrayInputStream(sb.toString().getBytes(
				"UTF-8"));

		byte[] outputByte = new byte[4096];
		Integer readLen = in.read(outputByte, 0, 4096);

		while (readLen != -1) {
			out.write(outputByte, 0, readLen);
			readLen = in.read(outputByte, 0, 4096);
		}
		in.close();
		out.flush();
		out.close();

	}

	private static StringBuffer generateCsvFileBuffer(GenericResultset result) {
		StringBuffer writer = new StringBuffer();

		List<ResultsetColumnHeader> columnHeaders = result.getColumnHeaders();
		logger.info("NO. of Columns: " + columnHeaders.size());
		Integer chSize = columnHeaders.size();
		for (int i = 0; i < chSize; i++) {
			writer.append('"' + columnHeaders.get(i).getColumnName() + '"');
			if (i < (chSize - 1))
				writer.append(",");
		}
		writer.append('\n');

		List<ResultsetDataRow> data = result.getData();
		List<String> row;
		Integer rSize;
		// String currCol;
		String currColType;
		String currVal;
		logger.info("NO. of Rows: " + data.size());
		for (int i = 0; i < data.size(); i++) {
			row = data.get(i).getRow();
			rSize = row.size();
			for (int j = 0; j < rSize; j++) {
				// currCol = columnHeaders.get(j).getColumnName();
				currColType = columnHeaders.get(j).getColumnType();
				currVal = row.get(j);
				if (currVal != null) {
					if (currColType.equals("DECIMAL")
							|| currColType.equals("DOUBLE")
							|| currColType.equals("BIGINT")
							|| currColType.equals("SMALLINT")
							|| currColType.equals("INT"))
						writer.append(currVal);
					else
						writer.append('"' + currVal + '"');
				}
				if (j < (rSize - 1))
					writer.append(",");
			}
			writer.append('\n');
		}

		return writer;
	}

	@Override
	public void setServletContext(ServletContext servletContext) {
		// TODO Auto-generated method stub
		
	}

}