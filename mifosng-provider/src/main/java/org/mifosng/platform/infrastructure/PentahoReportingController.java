package org.mifosng.platform.infrastructure;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

//@Controller
public class PentahoReportingController //implements ServletContextAware 
{

	// private int reportNum = 0; // used for building HTML reports
	// private ServletContext myServletContext;

	//private final static Logger logger = LoggerFactory.getLogger(PentahoReportingController.class);

	public PentahoReportingController() {

// JPW commented out for now ClassicEngineBoot.getInstance().start();
	}

	@RequestMapping(value = "/pentahoreport", method = RequestMethod.GET)
	public void handlePentahoReport(final HttpServletRequest request,
			HttpServletResponse response) throws IOException {
/* JPW commented out for now
		try {
			// load report definition
			ResourceManager manager = new ResourceManager();
			manager.registerDefaults();

			// various ways of pointing to the report directory - hardcoded here
			// for testing so if you put the test.prpt in a directory
			// c:\dev\PentahoTestReports it should find it

			// String reportPath = "file:" +
			// this.getServletContext().getRealPath(request.getParameter("pentahoReportName"));
			// String reportPath = "file:" + request.getServletPath();
			// String reportPath =
			// "http://ec2-46-137-17-252.eu-west-1.compute.amazonaws.com:8080/PentahoReports/"
			// + request.getParameter("pentahoReportName") + ".prpt";
			//	String reportPath = "file:///C:/dev/mifosbiGithub/bi/reports/standardReports/prpts/" + request.getParameter("pentahoReportName") + ".prpt";

			String reportPath = "file:///C:/dev/PentahoTestReports/"
					+ request.getParameter("pentahoReportName") + ".prpt";

			logger.info("pentahoReportName: "
					+ request.getParameter("pentahoReportName"));
			logger.info("Report path: " + reportPath);

			Resource res = manager.createDirectly(new URL(reportPath),
					MasterReport.class);
			MasterReport report = (MasterReport) res.getResource();
			ReportParameterValues rptParamValues = report.getParameterValues();

			String name;
			String value;
			@SuppressWarnings("unchecked")
			Enumeration<String> parms = request.getParameterNames();
			while (parms.hasMoreElements()) {
				// Get the name of the request parameter
				name = (String) parms.nextElement();
				logger.info("parm name: " + name);

				if (!((name.equals("pentahoReportName")) || (name
						.equals("output-type")))) {
					// Get the value of the request parameter
					value = request.getParameter(name);
					rptParamValues.put(name, value);
					logger.info("parm name: " + name + "    parm value: "
							+ value);
				}

			}

			String outputType = request.getParameter("output-type");

			if ("PDF".equals(outputType)) {
				response.setContentType("application/pdf");
				PdfReportUtil.createPDF(report, response.getOutputStream());
			} else if ("XLS".equals(outputType)) {
				response.setContentType("application/vnd.ms-excel");
				try {
					ExcelReportUtil.createXLS(report,
							response.getOutputStream());
				} catch (ReportProcessingException e) {
					e.printStackTrace();
				}
			} else if ("HTML".equals(outputType)) {
				String reportLoc = "report_" + reportNum++;

				logger.info("request context path: " + request.getContextPath());
				logger.info("request.getServletPath(): "
						+ request.getServletPath());

				logger.info("hopoe: "
						+ myServletContext.getRealPath(request.getContextPath()));
				String path = myServletContext
						.getRealPath("../ROOT/PentahoHTMLReports/" + reportLoc);
				logger.info("The path is: " + path);

				File folder = new File(path);
				folder.mkdir();
				try {
					HtmlReportUtil.createDirectoryHTML(report, path
							+ File.separator + "index.html");
				} catch (ReportProcessingException e) {
					e.printStackTrace();
				}

				String redirectLink = "/PentahoHTMLReports/" + reportLoc
						+ "/index.html";
				logger.info(redirectLink);
				response.sendRedirect(redirectLink);
			} else {
				response.setContentType("application/rtf");
				try {
					RTFReportUtil.createRTF(report, response.getOutputStream());
				} catch (ReportProcessingException e) {
					e.printStackTrace();
				}
			}
		} catch (ResourceException e) {
			e.printStackTrace();
		}*/
	}
	
	/* jpw comment out
	@Override
	public void setServletContext(ServletContext servletContext) {
		myServletContext = servletContext;
		String pentahoHTMLFolderName = myServletContext
				.getRealPath("../ROOT/PentahoHTMLReports");

		if (pentahoHTMLFolderName == null) {
			logger.info("pentaho HTML directory doesn't exist for non-Tomcat use");
		} else {
			File pentahoHTMLFolder = new File(pentahoHTMLFolderName);
			if (!(deleteDir(pentahoHTMLFolder)))
				logger.info("Failed to delete folder: "
						+ pentahoHTMLFolder.getName());
		}

	}*/

	// Deletes all files and subdirectories under dir.
	// Returns true if all deletions were successful.
	// If a deletion fails, the method stops attempting to delete and returns
	// false.
//	private static boolean deleteDir(File dir) {
//		if (dir.isDirectory()) {
//			String[] children = dir.list();
//			for (int i = 0; i < children.length; i++) {
//				boolean success = deleteDir(new File(dir, children[i]));
//				if (!success) {
//					return false;
//				}
//			}
//		}
//
//		// The directory is now empty so delete it
//		return dir.delete();
//	}

}