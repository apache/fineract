/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.report.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.api.ApiParameterHelper;
import org.apache.fineract.infrastructure.core.boot.JDBCDriverConfig;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenantConnection;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.report.annotation.ReportService;
import org.apache.fineract.infrastructure.report.service.ReportingProcessService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.useradministration.domain.AppUser;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.DefaultReportEnvironment;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.ReportProcessingException;
import org.pentaho.reporting.engine.classic.core.modules.output.pageable.pdf.PdfReportUtil;
import org.pentaho.reporting.engine.classic.core.modules.output.table.csv.CSVReportUtil;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.HtmlReportUtil;
import org.pentaho.reporting.engine.classic.core.modules.output.table.xls.ExcelReportUtil;
import org.pentaho.reporting.engine.classic.core.parameters.ParameterDefinitionEntry;
import org.pentaho.reporting.engine.classic.core.parameters.ReportParameterDefinition;
import org.pentaho.reporting.engine.classic.core.util.ReportParameterValues;
import org.pentaho.reporting.libraries.resourceloader.Resource;
import org.pentaho.reporting.libraries.resourceloader.ResourceException;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@ReportService(type = "Pentaho")
public class PentahoReportingProcessServiceImpl implements ReportingProcessService {

    private final static Logger logger = LoggerFactory.getLogger(PentahoReportingProcessServiceImpl.class);
    public static final String MIFOS_BASE_DIR = System.getProperty("user.home") + File.separator + ".mifosx";

    private final PlatformSecurityContext context;
    private boolean noPentaho = false;

    @Autowired private JDBCDriverConfig driverConfig ;
    
    @Autowired
    public PentahoReportingProcessServiceImpl(final PlatformSecurityContext context) {
        // kick off pentaho reports server
        ClassicEngineBoot.getInstance().start();
        this.noPentaho = false;

        this.context = context;
    }

    @Override
    public Response processRequest(final String reportName, final MultivaluedMap<String, String> queryParams) {

        final String outputTypeParam = queryParams.getFirst("output-type");
        final Map<String, String> reportParams = getReportParams(queryParams);
        final Locale locale = ApiParameterHelper.extractLocale(queryParams);

        String outputType = "HTML";
        if (StringUtils.isNotBlank(outputTypeParam)) {
            outputType = outputTypeParam;
        }

        if (!(outputType.equalsIgnoreCase("HTML") || outputType.equalsIgnoreCase("PDF") || outputType.equalsIgnoreCase("XLS")
                || outputType.equalsIgnoreCase("XLSX") || outputType.equalsIgnoreCase("CSV"))) { throw new PlatformDataIntegrityException(
                "error.msg.invalid.outputType", "No matching Output Type: " + outputType); }

        if (this.noPentaho) { throw new PlatformDataIntegrityException("error.msg.no.pentaho", "Pentaho is not enabled",
                "Pentaho is not enabled"); }

        final String reportPath = MIFOS_BASE_DIR + File.separator + "pentahoReports" + File.separator + reportName + ".prpt";
        logger.info("Report path: " + reportPath);

        // load report definition
        final ResourceManager manager = new ResourceManager();
        manager.registerDefaults();
        Resource res;

        try {
            res = manager.createDirectly(reportPath, MasterReport.class);
            final MasterReport masterReport = (MasterReport) res.getResource();
            final DefaultReportEnvironment reportEnvironment = (DefaultReportEnvironment) masterReport.getReportEnvironment();
            if (locale != null) {
                reportEnvironment.setLocale(locale);
            }
            addParametersToReport(masterReport, reportParams);

            final ByteArrayOutputStream baos = new ByteArrayOutputStream();

            if ("PDF".equalsIgnoreCase(outputType)) {
                PdfReportUtil.createPDF(masterReport, baos);
                return Response.ok().entity(baos.toByteArray()).type("application/pdf").build();
            }

            if ("XLS".equalsIgnoreCase(outputType)) {
                ExcelReportUtil.createXLS(masterReport, baos);
                return Response.ok().entity(baos.toByteArray()).type("application/vnd.ms-excel")
                        .header("Content-Disposition", "attachment;filename=" + reportName.replaceAll(" ", "") + ".xls").build();
            }

            if ("XLSX".equalsIgnoreCase(outputType)) {
                ExcelReportUtil.createXLSX(masterReport, baos);
                return Response.ok().entity(baos.toByteArray()).type("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                        .header("Content-Disposition", "attachment;filename=" + reportName.replaceAll(" ", "") + ".xlsx").build();
            }

            if ("CSV".equalsIgnoreCase(outputType)) {
                CSVReportUtil.createCSV(masterReport, baos, "UTF-8");
                return Response.ok().entity(baos.toByteArray()).type("text/csv")
                        .header("Content-Disposition", "attachment;filename=" + reportName.replaceAll(" ", "") + ".csv").build();
            }

            if ("HTML".equalsIgnoreCase(outputType)) {
                HtmlReportUtil.createStreamHTML(masterReport, baos);
                return Response.ok().entity(baos.toByteArray()).type("text/html").build();
            }
        } catch (final ResourceException e) {
            throw new PlatformDataIntegrityException("error.msg.reporting.error", e.getMessage());
        } catch (final ReportProcessingException e) {
            throw new PlatformDataIntegrityException("error.msg.reporting.error", e.getMessage());
        } catch (final IOException e) {
            throw new PlatformDataIntegrityException("error.msg.reporting.error", e.getMessage());
        }

        throw new PlatformDataIntegrityException("error.msg.invalid.outputType", "No matching Output Type: " + outputType);
    }

    private void addParametersToReport(final MasterReport report, final Map<String, String> queryParams) {

        final AppUser currentUser = this.context.authenticatedUser();

        try {

            final ReportParameterValues rptParamValues = report.getParameterValues();
            final ReportParameterDefinition paramsDefinition = report.getParameterDefinition();

            /*
             * only allow integer, long, date and string parameter types and
             * assume all mandatory - could go more detailed like Pawel did in
             * Mifos later and could match incoming and pentaho parameters
             * better... currently assuming they come in ok... and if not an
             * error
             */
            for (final ParameterDefinitionEntry paramDefEntry : paramsDefinition.getParameterDefinitions()) {
                final String paramName = paramDefEntry.getName();
                if (!((paramName.equals("tenantUrl")) || (paramName.equals("userhierarchy") || (paramName.equals("username")) || (paramName
                        .equals("password") || (paramName.equals("userid")))))) {
                    logger.info("paramName:" + paramName);
                    final String pValue = queryParams.get(paramName);
                    if (StringUtils.isBlank(pValue)) { throw new PlatformDataIntegrityException("error.msg.reporting.error",
                            "Pentaho Parameter: " + paramName + " - not Provided"); }

                    final Class<?> clazz = paramDefEntry.getValueType();
                    logger.info("addParametersToReport(" + paramName + " : " + pValue + " : " + clazz.getCanonicalName() + ")");
                    if (clazz.getCanonicalName().equalsIgnoreCase("java.lang.Integer")) {
                        rptParamValues.put(paramName, Integer.parseInt(pValue));
                    } else if (clazz.getCanonicalName().equalsIgnoreCase("java.lang.Long")) {
                        rptParamValues.put(paramName, Long.parseLong(pValue));
                    } else if (clazz.getCanonicalName().equalsIgnoreCase("java.sql.Date")) {
                        rptParamValues.put(paramName, Date.valueOf(pValue));
                    } else {
                        rptParamValues.put(paramName, pValue);
                    }
                }

            }

            // tenant database name and current user's office hierarchy
            // passed as parameters to allow multitenant penaho reporting
            // and
            // data scoping
            final FineractPlatformTenant tenant = ThreadLocalContextUtil.getTenant();
            final FineractPlatformTenantConnection tenantConnection = tenant.getConnection();
            String tenantUrl = driverConfig.constructProtocol(tenantConnection.getSchemaServer(), tenantConnection.getSchemaServerPort(), tenantConnection.getSchemaName()) ;
            final String userhierarchy = currentUser.getOffice().getHierarchy();
            logger.info("db URL:" + tenantUrl + "      userhierarchy:" + userhierarchy);
            rptParamValues.put("userhierarchy", userhierarchy);

            final Long userid = currentUser.getId();
            logger.info("db URL:" + tenantUrl + "      userid:" + userid);
            rptParamValues.put("userid", userid);

            rptParamValues.put("tenantUrl", tenantUrl);
            rptParamValues.put("username", tenantConnection.getSchemaUsername());
            rptParamValues.put("password", tenantConnection.getSchemaPassword());
        } catch (final Exception e) {
            logger.error("error.msg.reporting.error:" + e.getMessage());
            throw new PlatformDataIntegrityException("error.msg.reporting.error", e.getMessage());
        }
    }

    private Map<String, String> getReportParams(final MultivaluedMap<String, String> queryParams) {

        final Map<String, String> reportParams = new HashMap<>();
        final Set<String> keys = queryParams.keySet();
        String pKey;
        String pValue;
        for (final String k : keys) {

            if (k.startsWith("R_")) {
                pKey = k.substring(2);
                pValue = queryParams.get(k).get(0);
                reportParams.put(pKey, pValue);
            }
        }
        return reportParams;
    }

}