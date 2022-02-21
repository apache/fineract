/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.dataqueries.service;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import javax.ws.rs.core.StreamingOutput;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.dataqueries.data.GenericResultsetData;
import org.apache.fineract.infrastructure.dataqueries.data.ReportData;
import org.apache.fineract.infrastructure.dataqueries.data.ReportParameterData;
import org.apache.fineract.infrastructure.dataqueries.data.ReportParameterJoinData;
import org.apache.fineract.infrastructure.dataqueries.data.ResultsetColumnHeaderData;
import org.apache.fineract.infrastructure.dataqueries.data.ResultsetRowData;
import org.apache.fineract.infrastructure.dataqueries.exception.ReportNotFoundException;
import org.apache.fineract.infrastructure.documentmanagement.contentrepository.FileSystemContentRepository;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.useradministration.domain.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

@Service
public class ReadReportingServiceImpl implements ReadReportingService {

    private static final Logger LOG = LoggerFactory.getLogger(ReadReportingServiceImpl.class);

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private final PlatformSecurityContext context;
    private final GenericDataService genericDataService;

    @Autowired
    public ReadReportingServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource,
            final GenericDataService genericDataService) {
        this.context = context;
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(this.dataSource);
        this.genericDataService = genericDataService;
    }

    @Override
    public StreamingOutput retrieveReportCSV(final String name, final String type, final Map<String, String> queryParams,
            final boolean isSelfServiceUserReport) {
        return out -> {
            try {

                final GenericResultsetData result = retrieveGenericResultset(name, type, queryParams, isSelfServiceUserReport);
                final StringBuilder sb = generateCsvFileBuffer(result);

                final InputStream in = new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8));

                final byte[] outputByte = new byte[4096];
                Integer readLen = in.read(outputByte, 0, 4096);

                while (readLen != -1) {
                    out.write(outputByte, 0, readLen);
                    readLen = in.read(outputByte, 0, 4096);
                }
                // in.close();
                // out.flush();
                // out.close();
            } catch (final Exception e) {
                throw new PlatformDataIntegrityException("error.msg.exception.error", e.getMessage(), e);
            }
        };
    }

    private StringBuilder generateCsvFileBuffer(final GenericResultsetData result) {
        final StringBuilder writer = new StringBuilder();

        final List<ResultsetColumnHeaderData> columnHeaders = result.getColumnHeaders();
        LOG.info("NO. of Columns: {}", columnHeaders.size());
        final Integer chSize = columnHeaders.size();
        for (int i = 0; i < chSize; i++) {
            writer.append('"' + columnHeaders.get(i).getColumnName() + '"');
            if (i < (chSize - 1)) {
                writer.append(",");
            }
        }
        writer.append('\n');

        final List<ResultsetRowData> data = result.getData();
        List<String> row;
        Integer rSize;
        // String currCol;
        String currColType;
        String currVal;
        final String doubleQuote = "\"";
        final String twoDoubleQuotes = doubleQuote + doubleQuote;
        LOG.info("NO. of Rows: {}", data.size());
        for (ResultsetRowData element : data) {
            row = element.getRow();
            rSize = row.size();
            for (int j = 0; j < rSize; j++) {
                // currCol = columnHeaders.get(j).getColumnName();
                currColType = columnHeaders.get(j).getColumnType();
                currVal = row.get(j);
                if (currVal != null) {
                    if (currColType.equals("DECIMAL") || currColType.equals("DOUBLE") || currColType.equals("BIGINT")
                            || currColType.equals("SMALLINT") || currColType.equals("INT")) {
                        writer.append(currVal);
                    } else {
                        writer.append('"' + this.genericDataService.replace(currVal, doubleQuote, twoDoubleQuotes) + '"');
                    }

                }
                if (j < (rSize - 1)) {
                    writer.append(",");
                }
            }
            writer.append('\n');
        }

        return writer;
    }

    @Override
    public GenericResultsetData retrieveGenericResultset(final String name, final String type, final Map<String, String> queryParams,
            final boolean isSelfServiceUserReport) {

        final long startTime = System.currentTimeMillis();
        LOG.info("STARTING REPORT: {}   Type: {}", name, type);

        final String sql = getSQLtoRun(name, type, queryParams, isSelfServiceUserReport);

        final GenericResultsetData result = this.genericDataService.fillGenericResultSet(sql);

        final long elapsed = System.currentTimeMillis() - startTime;
        LOG.info("FINISHING Report/Request Name: {} - {}     Elapsed Time: {}", name, type, elapsed);
        return result;
    }

    private String getSQLtoRun(final String name, final String type, final Map<String, String> queryParams,
            final boolean isSelfServiceUserReport) {

        String sql = getSql(name, type);

        final Set<String> keys = queryParams.keySet();
        for (final String key : keys) {
            final String pValue = queryParams.get(key);
            // LOG.info("({} : {})", key, pValue);
            sql = this.genericDataService.replace(sql, key, pValue);
        }

        final AppUser currentUser = this.context.authenticatedUser();
        // Allows sql query to restrict data by office hierarchy if required
        sql = this.genericDataService.replace(sql, "${currentUserHierarchy}", currentUser.getOffice().getHierarchy());
        // Allows sql query to restrict data by current user Id if required
        // (typically used to return report lists containing only reports
        // permitted to be run by the user
        sql = this.genericDataService.replace(sql, "${currentUserId}", currentUser.getId().toString());

        sql = this.genericDataService.replace(sql, "${isSelfServiceUser}", Integer.toString(isSelfServiceUserReport ? 1 : 0));

        sql = this.genericDataService.wrapSQL(sql);

        return sql;
    }

    private String getSql(final String name, final String type) {
        final String inputSql = "select " + type + "_sql as the_sql from stretchy_" + type + " where " + type + "_name = ?";

        final String inputSqlWrapped = this.genericDataService.wrapSQL(inputSql);

        // the return statement contains the exact sql required
        final SqlRowSet rs = this.jdbcTemplate.queryForRowSet(inputSqlWrapped, name);

        if (rs.next() && rs.getString("the_sql") != null) {
            return rs.getString("the_sql");
        }
        throw new ReportNotFoundException(name);
    }

    @Override
    public String getReportType(final String reportName, final boolean isSelfServiceUserReport, final boolean isParameterType) {
        if (isParameterType) {
            return "Table";
        }

        final String sql = "SELECT coalesce(report_type,'') AS report_type FROM stretchy_report WHERE report_name = ? AND self_service_user_report = ?";

        final String sqlWrapped = this.genericDataService.wrapSQL(sql);

        final SqlRowSet rs = this.jdbcTemplate.queryForRowSet(sqlWrapped, reportName, isSelfServiceUserReport);

        if (rs.next()) {
            return rs.getString("report_type");
        }
        throw new ReportNotFoundException(reportName);
    }

    @Override
    public String retrieveReportPDF(final String reportName, final String type, final Map<String, String> queryParams,
            final boolean isSelfServiceUserReport) {

        final String fileLocation = FileSystemContentRepository.FINERACT_BASE_DIR + File.separator + "";
        if (!new File(fileLocation).isDirectory()) {
            new File(fileLocation).mkdirs();
        }

        final String genaratePdf = fileLocation + File.separator + reportName + ".pdf";

        try {
            final GenericResultsetData result = retrieveGenericResultset(reportName, type, queryParams, isSelfServiceUserReport);

            final List<ResultsetColumnHeaderData> columnHeaders = result.getColumnHeaders();
            final List<ResultsetRowData> data = result.getData();
            List<String> row;

            LOG.info("NO. of Columns: {}", columnHeaders.size());
            final Integer chSize = columnHeaders.size();

            final Document document = new Document(PageSize.B0.rotate());

            PdfWriter.getInstance(document, new FileOutputStream(new File(fileLocation + reportName + ".pdf")));
            document.open();

            final PdfPTable table = new PdfPTable(chSize);
            table.setWidthPercentage(100);

            for (int i = 0; i < chSize; i++) {

                table.addCell(columnHeaders.get(i).getColumnName());

            }
            table.completeRow();

            Integer rSize;
            String currColType;
            String currVal;
            LOG.info("NO. of Rows: {}", data.size());
            for (ResultsetRowData element : data) {
                row = element.getRow();
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
        } catch (final Exception e) {
            LOG.error("error.msg.reporting.error:", e);
            throw new PlatformDataIntegrityException("error.msg.exception.error", e.getMessage(), e);
        }
    }

    @Override
    public ReportData retrieveReport(final Long id) {
        final Collection<ReportData> reports = retrieveReports(id);

        for (final ReportData report : reports) {
            return report;
        }
        return null;
    }

    @Override
    public Collection<ReportData> retrieveReportList() {
        return retrieveReports(null);
    }

    private Collection<ReportData> retrieveReports(final Long id) {

        final ReportParameterJoinMapper rm = new ReportParameterJoinMapper();

        final String sql = rm.schema(id);

        final Collection<ReportParameterJoinData> rpJoins = this.jdbcTemplate.query(sql, rm,
                id != null ? new Object[] { id } : new Object[] {});

        final Collection<ReportData> reportList = new ArrayList<>();
        if (rpJoins == null || rpJoins.size() == 0) {
            return reportList;
        }

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
        for (final ReportParameterJoinData rpJoin : rpJoins) {

            if (rpJoin.getReportId().equals(prevReportId)) {
                // more than one parameter for report
                if (reportParameters == null) {
                    reportParameters = new ArrayList<>();
                }
                reportParameters.add(new ReportParameterData(rpJoin.getReportParameterId(), rpJoin.getParameterId(),
                        rpJoin.getReportParameterName(), rpJoin.getParameterName()));

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
                    reportParameters = new ArrayList<>();
                    reportParameters.add(new ReportParameterData(rpJoin.getReportParameterId(), rpJoin.getParameterId(),
                            rpJoin.getReportParameterName(), rpJoin.getParameterName()));
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
        final ReportParameterMapper rm = new ReportParameterMapper();
        final String sql = rm.schema();
        final Collection<ReportParameterData> parameters = this.jdbcTemplate.query(sql, rm);
        return parameters;
    }

    private static final class ReportParameterJoinMapper implements RowMapper<ReportParameterJoinData> {

        public String schema(final Long reportId) {

            String sql = "select r.id as reportId, r.report_name as reportName, r.report_type as reportType, "
                    + " r.report_subtype as reportSubType, r.report_category as reportCategory, r.description, r.core_report as coreReport, r.use_report as useReport, "
                    + " rp.id as reportParameterId, rp.parameter_id as parameterId, rp.report_parameter_name as reportParameterName, p.parameter_name as parameterName";

            if (reportId != null) {
                sql += ", r.report_sql as reportSql ";
            }

            sql += " from stretchy_report r" + " left join stretchy_report_parameter rp on rp.report_id = r.id"
                    + " left join stretchy_parameter p on p.id = rp.parameter_id";
            if (reportId != null) {
                sql += " where r.id = ?";
            } else {
                sql += " order by r.id, rp.parameter_id";
            }

            return sql;

            /*
             * used to only return reports that the use can run as done in report UI but not necessary as there is a
             * read_report permission which should give user access to look all reports + " where exists" +
             * " (select 'f'" + " from m_appuser_role ur " + " join m_role r on r.id = ur.role_id" +
             * " left join m_role_permission rp on rp.role_id = r.id" +
             * " left join m_permission p on p.id = rp.permission_id" + " where ur.appuser_id = " + userId +
             * " and (p.code in ('ALL_FUNCTIONS', 'ALL_FUNCTIONS_READ') or p.code = concat('READ_', r.report_name))) " ;
             */
        }

        @Override
        public ReportParameterJoinData mapRow(final ResultSet rs, final int rowNum) throws SQLException {
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
            } catch (final SQLException e) {
                reportSql = null;
            }

            final Long reportParameterId = JdbcSupport.getLong(rs, "reportParameterId");
            final Long parameterId = JdbcSupport.getLong(rs, "parameterId");
            final String reportParameterName = rs.getString("reportParameterName");
            final String parameterName = rs.getString("parameterName");

            return new ReportParameterJoinData(reportId, reportName, reportType, reportSubType, reportCategory, description, reportSql,
                    coreReport, useReport, reportParameterId, parameterId, reportParameterName, parameterName);
        }
    }

    private static final class ReportParameterMapper implements RowMapper<ReportParameterData> {

        public String schema() {
            return "select p.id as id, p.parameter_name as parameterName from stretchy_parameter p where coalesce(p.special,'') != 'Y' order by p.id";
        }

        @Override
        public ReportParameterData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String parameterName = rs.getString("parameterName");

            return new ReportParameterData(id, null, null, parameterName);
        }
    }

    @Override
    public GenericResultsetData retrieveGenericResultSetForSmsEmailCampaign(String name, String type, Map<String, String> queryParams) {
        final long startTime = System.currentTimeMillis();
        LOG.info("STARTING REPORT: {}   Type: {}", name, type);

        final String sql = sqlToRunForSmsEmailCampaign(name, type, queryParams);

        final GenericResultsetData result = this.genericDataService.fillGenericResultSet(sql);

        final long elapsed = System.currentTimeMillis() - startTime;
        LOG.info("FINISHING Report/Request Name: {} - {}     Elapsed Time: {}", name, type, elapsed);
        return result;
    }

    private String sqlToRunForSmsEmailCampaign(final String name, final String type, final Map<String, String> queryParams) {
        String sql = getSql(name, type);

        final Set<String> keys = queryParams.keySet();
        for (String key : keys) {
            final String pValue = queryParams.get(key);
            key = "${" + key + "}";
            sql = this.genericDataService.replace(sql, key, pValue);
        }

        sql = this.genericDataService.wrapSQL(sql);

        return sql;
    }

    @Override
    public ByteArrayOutputStream generatePentahoReportAsOutputStream(final String reportName, final String outputTypeParam,
            final Map<String, String> queryParams, final Locale locale, final AppUser runReportAsUser, final StringBuilder errorLog) {
        // This complete implementation should be moved to Pentaho Report
        // Service
        /*
         * String outputType = "HTML"; if (StringUtils.isNotBlank(outputTypeParam)) { outputType = outputTypeParam; }
         *
         * if (!(outputType.equalsIgnoreCase("HTML") || outputType.equalsIgnoreCase("PDF") ||
         * outputType.equalsIgnoreCase("XLS") || outputType .equalsIgnoreCase("CSV"))) { throw new
         * PlatformDataIntegrityException("error.msg.invalid.outputType", "No matching Output Type: " + outputType); }
         *
         * if (this.noPentaho) { throw new PlatformDataIntegrityException("error.msg.no.pentaho",
         * "Pentaho is not enabled", "Pentaho is not enabled"); }
         *
         * final String reportPath = FileSystemContentRepository.FINERACT_BASE_DIR + File.separator + "pentahoReports" +
         * File.separator + reportName + ".prpt"; LOG.info("Report path: {}", reportPath);
         *
         * // load report definition final ResourceManager manager = new ResourceManager(); manager.registerDefaults();
         * Resource res;
         *
         * try { res = manager.createDirectly(reportPath, MasterReport.class); final MasterReport masterReport =
         * (MasterReport) res.getResource(); final DefaultReportEnvironment reportEnvironment =
         * (DefaultReportEnvironment) masterReport.getReportEnvironment();
         *
         * if (locale != null) { reportEnvironment.setLocale(locale); } addParametersToReport(masterReport, queryParams,
         * runReportAsUser, errorLog);
         *
         * final ByteArrayOutputStream baos = new ByteArrayOutputStream();
         *
         * if ("PDF".equalsIgnoreCase(outputType)) { PdfReportUtil.createPDF(masterReport, baos); return baos; }
         *
         * if ("XLS".equalsIgnoreCase(outputType)) { ExcelReportUtil.createXLS(masterReport, baos); return baos; }
         *
         * if ("CSV".equalsIgnoreCase(outputType)) { CSVReportUtil.createCSV(masterReport, baos, "UTF-8"); return baos;
         * }
         *
         * if ("HTML".equalsIgnoreCase(outputType)) { HtmlReportUtil.createStreamHTML(masterReport, baos); return baos;
         * }
         *
         * } catch (final ResourceException e) { errorLog.
         * append("ReadReportingServiceImpl.generatePentahoReportAsOutputStream method threw a Pentaho ResourceException "
         * + "exception: " + e.getMessage() + " ---------- "); throw new
         * PlatformDataIntegrityException("error.msg.reporting.error", e.getMessage()); } catch (final
         * ReportProcessingException e) { errorLog.
         * append("ReadReportingServiceImpl.generatePentahoReportAsOutputStream method threw a Pentaho ReportProcessingException "
         * + "exception: " + e.getMessage() + " ---------- "); throw new
         * PlatformDataIntegrityException("error.msg.reporting.error", e.getMessage()); } catch (final IOException e) {
         * errorLog. append("ReadReportingServiceImpl.generatePentahoReportAsOutputStream method threw an IOException "
         * + "exception: " + e.getMessage() + " ---------- "); throw new
         * PlatformDataIntegrityException("error.msg.reporting.error", e.getMessage()); }
         *
         * errorLog.
         * append("ReadReportingServiceImpl.generatePentahoReportAsOutputStream method threw a PlatformDataIntegrityException "
         * + "exception: No matching Output Type: " + outputType + " ---------- "); throw new
         * PlatformDataIntegrityException("error.msg.invalid.outputType", "No matching Output Type: " + outputType);
         *
         */
        return null;
    }
}
