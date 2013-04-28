/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.dataqueries.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.service.FileUtils;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.dataqueries.data.GenericResultsetData;
import org.mifosplatform.infrastructure.dataqueries.data.ReportData;
import org.mifosplatform.infrastructure.dataqueries.data.ReportParameterData;
import org.mifosplatform.infrastructure.dataqueries.data.ReportParameterJoinData;
import org.mifosplatform.infrastructure.dataqueries.data.ResultsetColumnHeaderData;
import org.mifosplatform.infrastructure.dataqueries.data.ResultsetRowData;
import org.mifosplatform.infrastructure.dataqueries.exception.ReportNotFoundException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.useradministration.domain.AppUser;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@Service
public class ReadReportingServiceImpl implements ReadReportingService {

    private final static Logger logger = LoggerFactory.getLogger(ReadReportingServiceImpl.class);

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private final PlatformSecurityContext context;
    private final GenericDataService genericDataService;
    private boolean noPentaho = false;

    @Autowired
    public ReadReportingServiceImpl(final PlatformSecurityContext context, final TenantAwareRoutingDataSource dataSource,
            final GenericDataService genericDataService) {
        // kick off pentaho reports server
        ClassicEngineBoot.getInstance().start();
        noPentaho = false;

        this.context = context;
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(this.dataSource);
        this.genericDataService = genericDataService;
    }

    @Override
    public StreamingOutput retrieveReportCSV(final String name, final String type, final Map<String, String> queryParams) {

        return new StreamingOutput() {

            @Override
            public void write(final OutputStream out) {
                try {

                    GenericResultsetData result = retrieveGenericResultset(name, type, queryParams);
                    StringBuffer sb = generateCsvFileBuffer(result);

                    InputStream in = new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));

                    byte[] outputByte = new byte[4096];
                    Integer readLen = in.read(outputByte, 0, 4096);

                    while (readLen != -1) {
                        out.write(outputByte, 0, readLen);
                        readLen = in.read(outputByte, 0, 4096);
                    }
                    // in.close();
                    // out.flush();
                    // out.close();
                } catch (Exception e) {
                    throw new PlatformDataIntegrityException("error.msg.exception.error", e.getMessage());
                }
            }
        };

    }

    private StringBuffer generateCsvFileBuffer(final GenericResultsetData result) {
        StringBuffer writer = new StringBuffer();

        List<ResultsetColumnHeaderData> columnHeaders = result.getColumnHeaders();
        logger.info("NO. of Columns: " + columnHeaders.size());
        Integer chSize = columnHeaders.size();
        for (int i = 0; i < chSize; i++) {
            writer.append('"' + columnHeaders.get(i).getColumnName() + '"');
            if (i < (chSize - 1)) writer.append(",");
        }
        writer.append('\n');

        List<ResultsetRowData> data = result.getData();
        List<String> row;
        Integer rSize;
        // String currCol;
        String currColType;
        String currVal;
        String doubleQuote = "\"";
        String twoDoubleQuotes = doubleQuote + doubleQuote;
        logger.info("NO. of Rows: " + data.size());
        for (int i = 0; i < data.size(); i++) {
            row = data.get(i).getRow();
            rSize = row.size();
            for (int j = 0; j < rSize; j++) {
                // currCol = columnHeaders.get(j).getColumnName();
                currColType = columnHeaders.get(j).getColumnType();
                currVal = row.get(j);
                if (currVal != null) {
                    if (currColType.equals("DECIMAL") || currColType.equals("DOUBLE") || currColType.equals("BIGINT")
                            || currColType.equals("SMALLINT") || currColType.equals("INT"))
                        writer.append(currVal);
                    else
                        writer.append('"' + genericDataService.replace(currVal, doubleQuote, twoDoubleQuotes) + '"');

                }
                if (j < (rSize - 1)) writer.append(",");
            }
            writer.append('\n');
        }

        return writer;
    }

    @Override
    public GenericResultsetData retrieveGenericResultset(final String name, final String type, final Map<String, String> queryParams) {

        long startTime = System.currentTimeMillis();
        logger.info("STARTING REPORT: " + name + "   Type: " + type);

        String sql = getSQLtoRun(name, type, queryParams);

        GenericResultsetData result = genericDataService.fillGenericResultSet(sql);

        long elapsed = System.currentTimeMillis() - startTime;
        logger.info("FINISHING Report/Request Name: " + name + " - " + type + "     Elapsed Time: " + elapsed);
        return result;
    }

    private String getSQLtoRun(final String name, final String type, final Map<String, String> queryParams) {

        String sql = getSql(name, type);

        Set<String> keys = queryParams.keySet();
        for (String key : keys) {
            String pValue = queryParams.get(key);
            // logger.info("(" + key + " : " + pValue + ")");
            sql = genericDataService.replace(sql, key, pValue);
        }

        AppUser currentUser = context.authenticatedUser();
        // Allows sql query to restrict data by office hierarchy if required
        sql = genericDataService.replace(sql, "${currentUserHierarchy}", currentUser.getOffice().getHierarchy());
        // Allows sql query to restrict data by current user Id if required
        // (typically used to return report lists containing only reports
        // permitted to be run by the user
        sql = genericDataService.replace(sql, "${currentUserId}", currentUser.getId().toString());

        sql = genericDataService.wrapSQL(sql);

        return sql;

    }

    private String getSql(final String name, final String type) {

        final String inputSql = "select " + type + "_sql as the_sql from stretchy_" + type + " where " + type + "_name = '" + name + "'";
        final String inputSqlWrapped = genericDataService.wrapSQL(inputSql);

        final SqlRowSet rs = this.jdbcTemplate.queryForRowSet(inputSqlWrapped);

        if (rs.next()) { return rs.getString("the_sql"); }
        throw new ReportNotFoundException(inputSql);
    }

    @Override
    public String getReportType(final String reportName) {

        final String sql = "SELECT ifnull(report_type,'') as report_type FROM `stretchy_report` where report_name = '" + reportName + "'";

        final String sqlWrapped = genericDataService.wrapSQL(sql);

        final SqlRowSet rs = this.jdbcTemplate.queryForRowSet(sqlWrapped);

        if (rs.next()) { return rs.getString("report_type"); }
        throw new ReportNotFoundException(sql);
    }

    @Override
    public Response processPentahoRequest(final String reportName, final String outputTypeParam, final Map<String, String> queryParams) {

        String outputType = "HTML";
        if (StringUtils.isNotBlank(outputTypeParam)) outputType = outputTypeParam;

        if (!(outputType.equalsIgnoreCase("HTML") || outputType.equalsIgnoreCase("PDF") || outputType.equalsIgnoreCase("XLS") || outputType
                .equalsIgnoreCase("CSV")))
            throw new PlatformDataIntegrityException("error.msg.invalid.outputType", "No matching Output Type: " + outputType);

        if (noPentaho) { throw new PlatformDataIntegrityException("error.msg.no.pentaho", "Pentaho is not enabled",
                "Pentaho is not enabled"); }

        final String reportPath = FileUtils.MIFOSX_BASE_DIR + File.separator + "pentahoReports" + File.separator + reportName + ".prpt";
        logger.info("Report path: " + reportPath);

        // load report definition
        ResourceManager manager = new ResourceManager();
        manager.registerDefaults();
        Resource res;

        try {
            res = manager.createDirectly(reportPath, MasterReport.class);
            MasterReport masterReport = (MasterReport) res.getResource();

            addParametersToReport(masterReport, queryParams);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            if ("PDF".equalsIgnoreCase(outputType)) {
                PdfReportUtil.createPDF(masterReport, baos);
                return Response.ok().entity(baos.toByteArray()).type("application/pdf").build();
            }

            if ("XLS".equalsIgnoreCase(outputType)) {
                ExcelReportUtil.createXLS(masterReport, baos);
                return Response.ok().entity(baos.toByteArray()).type("application/vnd.ms-excel")
                        .header("Content-Disposition", "attachment;filename=" + reportName.replaceAll(" ", "") + ".xls").build();
            }

            if ("CSV".equalsIgnoreCase(outputType)) {
                CSVReportUtil.createCSV(masterReport, baos, "UTF-8");
                return Response.ok().entity(baos.toByteArray()).type("application/x-msdownload")
                        .header("Content-Disposition", "attachment;filename=" + reportName.replaceAll(" ", "") + ".csv").build();
            }

            if ("HTML".equalsIgnoreCase(outputType)) {
                HtmlReportUtil.createStreamHTML(masterReport, baos);
                return Response.ok().entity(baos.toByteArray()).type("text/html").build();
            }
        } catch (ResourceException e) {
            throw new PlatformDataIntegrityException("error.msg.reporting.error", e.getMessage());
        } catch (ReportProcessingException e) {
            throw new PlatformDataIntegrityException("error.msg.reporting.error", e.getMessage());
        } catch (IOException e) {
            throw new PlatformDataIntegrityException("error.msg.reporting.error", e.getMessage());
        }

        throw new PlatformDataIntegrityException("error.msg.invalid.outputType", "No matching Output Type: " + outputType);
    }

    private void addParametersToReport(final MasterReport report, final Map<String, String> queryParams) {

        AppUser currentUser = context.authenticatedUser();

        try {

            ReportParameterValues rptParamValues = report.getParameterValues();
            ReportParameterDefinition paramsDefinition = report.getParameterDefinition();

            /*
             * only allow integer, long, date and string parameter types and
             * assume all mandatory - could go more detailed like Pawel did in
             * Mifos later and could match incoming and pentaho parameters
             * better... currently assuming they come in ok... and if not an
             * error
             */
            for (ParameterDefinitionEntry paramDefEntry : paramsDefinition.getParameterDefinitions()) {
                String paramName = paramDefEntry.getName();
                if (!((paramName.equals("tenantdb")) || (paramName.equals("userhierarchy")))) {
                    logger.info("paramName:" + paramName);
                    String pValue = queryParams.get(paramName);
                    if (StringUtils.isBlank(pValue))
                        throw new PlatformDataIntegrityException("error.msg.reporting.error", "Pentaho Parameter: " + paramName
                                + " - not Provided");

                    Class<?> clazz = paramDefEntry.getValueType();
                    logger.info("addParametersToReport(" + paramName + " : " + pValue + " : " + clazz.getCanonicalName() + ")");
                    if (clazz.getCanonicalName().equalsIgnoreCase("java.lang.Integer"))
                        rptParamValues.put(paramName, Integer.parseInt(pValue));
                    else if (clazz.getCanonicalName().equalsIgnoreCase("java.lang.Long"))
                        rptParamValues.put(paramName, Long.parseLong(pValue));
                    else if (clazz.getCanonicalName().equalsIgnoreCase("java.sql.Date"))
                        rptParamValues.put(paramName, Date.valueOf(pValue));
                    else
                        rptParamValues.put(paramName, pValue);
                }

            }

            // tenant database name and current user's office hierarchy
            // passed as parameters to allow multitenant penaho reporting
            // and
            // data scoping
            String tenantdb = dataSource.getConnection().getCatalog();
            String userhierarchy = currentUser.getOffice().getHierarchy();
            logger.info("db name:" + tenantdb + "      userhierarchy:" + userhierarchy);
            rptParamValues.put("tenantdb", tenantdb);
            rptParamValues.put("userhierarchy", userhierarchy);
        } catch (Exception e) {
            logger.error("error.msg.reporting.error:" + e.getMessage());
            throw new PlatformDataIntegrityException("error.msg.reporting.error", e.getMessage());
        }
    }

    @Override
    public String retrieveReportPDF(final String reportName, final String type, final Map<String, String> queryParams) {

        String fileLocation = FileUtils.MIFOSX_BASE_DIR + File.separator + "";
        if (!new File(fileLocation).isDirectory()) {
            new File(fileLocation).mkdirs();
        }

        String genaratePdf = fileLocation + File.separator + reportName + ".pdf";

        try {
            GenericResultsetData result = retrieveGenericResultset(reportName, type, queryParams);

            List<ResultsetColumnHeaderData> columnHeaders = result.getColumnHeaders();
            List<ResultsetRowData> data = result.getData();
            List<String> row;

            logger.info("NO. of Columns: " + columnHeaders.size());
            Integer chSize = columnHeaders.size();

            Document document = new Document(PageSize.B0.rotate());

            PdfWriter.getInstance(document, new FileOutputStream(new File(fileLocation + reportName + ".pdf")));
            document.open();

            PdfPTable table = new PdfPTable(chSize);
            table.setWidthPercentage(100);

            for (int i = 0; i < chSize; i++) {

                table.addCell(columnHeaders.get(i).getColumnName());

            }
            table.completeRow();

            Integer rSize;
            String currColType;
            String currVal;
            logger.info("NO. of Rows: " + data.size());
            for (int i = 0; i < data.size(); i++) {
                row = data.get(i).getRow();
                rSize = row.size();
                for (int j = 0; j < rSize; j++) {
                    currColType = columnHeaders.get(j).getColumnType();
                    currVal = row.get(j);
                    if (currVal != null) {
                        if (currColType.equals("DECIMAL") || currColType.equals("DOUBLE") || currColType.equals("BIGINT")
                                || currColType.equals("SMALLINT") || currColType.equals("INT")) {

                            table.addCell(currVal.toString());
                        } else {
                            table.addCell(currVal.toString());
                        }
                    }
                }
            }
            table.completeRow();
            document.add(table);
            document.close();
            return genaratePdf;
        } catch (Exception e) {
            logger.error("error.msg.reporting.error:" + e.getMessage());
            throw new PlatformDataIntegrityException("error.msg.exception.error", e.getMessage());
        }
    }

    @Override
    public ReportData retrieveReport(final Long id) {
        Collection<ReportData> reports = retrieveReports(id);

        for (ReportData report : reports) {
            return report;
        }
        return null;
    }

    @Override
    public Collection<ReportData> retrieveReportList() {
        return retrieveReports(null);
    }

    private Collection<ReportData> retrieveReports(final Long id) {

        ReportParameterJoinMapper rm = new ReportParameterJoinMapper();

        String sql = rm.schema(id);

        Collection<ReportParameterJoinData> rpJoins = this.jdbcTemplate.query(sql, rm, new Object[] {});

        Collection<ReportData> reportList = new ArrayList<ReportData>();
        if (rpJoins == null || rpJoins.size() == 0) return reportList;

        Collection<ReportParameterData> reportParameters = null;

        Long reportId = null;
        String reportName = null;
        String reportType = null;
        String reportSubType = null;
        String reportCategory = null;
        String description = null;
        Boolean coreReport = null;
        Boolean useReport = null;
        String reportSql = null;

        Long prevReportId = (long) -1234;
        Boolean firstReport = true;
        for (ReportParameterJoinData rpJoin : rpJoins) {

            if (rpJoin.getReportId().equals(prevReportId)) {
                // more than one parameter for report
                if (reportParameters == null) {
                    reportParameters = new ArrayList<ReportParameterData>();
                }
                reportParameters.add(new ReportParameterData(rpJoin.getReportParameterId(), rpJoin.getReportParameterName(), rpJoin
                        .getParameterName()));

            } else {
                if (firstReport) {
                    firstReport = false;
                } else {
                    // write report entry
                    reportList.add(new ReportData(reportId, reportName, reportType, reportSubType, reportCategory, description, reportSql,
                            coreReport, useReport, reportParameters));
                }

                prevReportId = rpJoin.getReportId();

                reportId = rpJoin.getReportId();
                reportName = rpJoin.getReportName();
                reportType = rpJoin.getReportType();
                reportSubType = rpJoin.getReportSubType();
                reportCategory = rpJoin.getReportCategory();
                description = rpJoin.getDescription();
                reportSql = rpJoin.getReportSql();
                coreReport = rpJoin.getCoreReport();
                useReport = rpJoin.getUseReport();

                if (rpJoin.getReportParameterId() != null) {
                    // report has at least one parameter
                    reportParameters = new ArrayList<ReportParameterData>();
                    reportParameters.add(new ReportParameterData(rpJoin.getReportParameterId(), rpJoin.getReportParameterName(), rpJoin
                            .getParameterName()));
                } else {
                    reportParameters = null;
                }
            }

        }
        // write last report
        reportList.add(new ReportData(reportId, reportName, reportType, reportSubType, reportCategory, description, reportSql, coreReport,
                useReport, reportParameters));

        return reportList;
    }


	@Override
	public Collection<ReportParameterData> getAllowedParameters() {
		
        ReportParameterMapper rm = new ReportParameterMapper();
        String sql = rm.schema();
        Collection<ReportParameterData> parameters = this.jdbcTemplate.query(sql, rm, new Object[] {});
        return parameters;
	}
	
	
    private static final class ReportParameterJoinMapper implements RowMapper<ReportParameterJoinData> {

        public String schema(final Long reportId) {

            String sql = "select r.id as reportId, r.report_name as reportName, r.report_type as reportType, "
                    + " r.report_subtype as reportSubType, r.report_category as reportCategory, r.description, r.core_report as coreReport, r.use_report as useReport, "
                    + " rp.parameter_id as reportParameterId, rp.report_parameter_name as reportParameterName, p.parameter_name as parameterName";

            if (reportId != null) sql += ", r.report_sql as reportSql ";

            sql += " from stretchy_report r" + " left join stretchy_report_parameter rp on rp.report_id = r.id"
                    + " left join stretchy_parameter p on p.id = rp.parameter_id";
            if (reportId != null)
                sql += " where r.id = " + reportId;
            else
                sql += " order by r.id, rp.parameter_id";

            return sql;

            /*
             * used to only return reports that the use can run as done in
             * report UI but not necessary as there is a read_report permission
             * which should give user access to look all reports +
             * " where exists" + " (select 'f'" + " from m_appuser_role ur " +
             * " join m_role r on r.id = ur.role_id" +
             * " left join m_role_permission rp on rp.role_id = r.id" +
             * " left join m_permission p on p.id = rp.permission_id" +
             * " where ur.appuser_id = " + userId +
             * " and (p.code in ('ALL_FUNCTIONS', 'ALL_FUNCTIONS_READ') or p.code = concat('READ_', r.report_name))) "
             * ;
             */
        }

        @Override
        public ReportParameterJoinData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long reportId = rs.getLong("reportId");
            final String reportName = rs.getString("reportName");
            final String reportType = rs.getString("reportType");
            final String reportSubType = rs.getString("reportSubType");
            final String reportCategory = rs.getString("reportCategory");
            final String description = rs.getString("description");
            final Boolean coreReport = rs.getBoolean("coreReport");
            final Boolean useReport = rs.getBoolean("useReport");

            String reportSql;
            // reportSql might not be on the select list of columns
            try {
                reportSql = rs.getString("reportSql");
            } catch (SQLException e) {
                reportSql = null;
            }

            final Long reportParameterId = JdbcSupport.getLong(rs, "reportParameterId");
            final String reportParameterName = rs.getString("reportParameterName");
            final String parameterName = rs.getString("parameterName");

            return new ReportParameterJoinData(reportId, reportName, reportType, reportSubType, reportCategory, description, reportSql,
                    coreReport, useReport, reportParameterId, reportParameterName, parameterName);
        }
    }

    private static final class ReportParameterMapper implements RowMapper<ReportParameterData> {

        public String schema() {

            return "select p.id as id, p.parameter_name as parameterName from stretchy_parameter p where ifnull(p.special,'') != 'Y' order by p.id";

        }

        @Override
        public ReportParameterData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String parameterName = rs.getString("parameterName");

            return new ReportParameterData(id, null, parameterName);
        }
    }

}