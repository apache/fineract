package org.mifosplatform.infrastructure.dataqueries.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.rowset.CachedRowSet;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.service.FileUtils;
import org.mifosplatform.infrastructure.core.service.ThreadLocalContextUtil;
import org.mifosplatform.infrastructure.dataqueries.data.GenericResultsetData;
import org.mifosplatform.infrastructure.dataqueries.data.ResultsetColumnHeader;
import org.mifosplatform.infrastructure.dataqueries.data.ResultsetDataRow;
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
import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@Service
public class ReadReportingServiceImpl implements ReadReportingService {

    private final PlatformSecurityContext context;

    private final static Logger logger = LoggerFactory.getLogger(ReadReportingServiceImpl.class);

    private Boolean noPentaho = false;

    @Autowired
    public ReadReportingServiceImpl(final PlatformSecurityContext context) {
        // kick off pentaho reports server
        ClassicEngineBoot.getInstance().start();
        noPentaho = false;

        this.context = context;
    }

    @Autowired
    private GenericDataService genericDataService;

    @Override
    public StreamingOutput retrieveReportCSV(final String name, final String type, final Map<String, String> queryParams) {

        return new StreamingOutput() {

            @Override
            public void write(OutputStream out) {
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

    private static StringBuffer generateCsvFileBuffer(GenericResultsetData result) {
        StringBuffer writer = new StringBuffer();

        List<ResultsetColumnHeader> columnHeaders = result.getColumnHeaders();
        logger.info("NO. of Columns: " + columnHeaders.size());
        Integer chSize = columnHeaders.size();
        for (int i = 0; i < chSize; i++) {
            writer.append('"' + columnHeaders.get(i).getColumnName() + '"');
            if (i < (chSize - 1)) writer.append(",");
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
                    if (currColType.equals("DECIMAL") || currColType.equals("DOUBLE") || currColType.equals("BIGINT")
                            || currColType.equals("SMALLINT") || currColType.equals("INT"))
                        writer.append(currVal);
                    else
                        writer.append('"' + currVal + '"');
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

        String sql;
        if (name.equals(".")) {
            // this is to support api /reports - which isn't an important
            // call. It isn't used in the default reporting UI. But there is a
            // need to provide an api that does bring back 'permitted' reports
            // PERMITTED REPORTS SQL
            sql = "select r.report_id, r.report_name, r.report_type, r.report_subtype, r.report_category,"
                    + " rp.parameter_id, rp.report_parameter_name, p.parameter_name" + " from stretchy_report r"
                    + " left join stretchy_report_parameter rp on rp.report_id = r.report_id"
                    + " left join stretchy_parameter p on p.parameter_id = rp.parameter_id" + " where exists" + " (select 'f'"
                    + " from m_appuser_role ur " + " join m_role r on r.id = ur.role_id"
                    + " left join m_role_permission rp on rp.role_id = r.id" + " left join m_permission p on p.id = rp.permission_id"
                    + " where ur.appuser_id = " + context.authenticatedUser().getId()
                    + " and (p.code in ('ALL_FUNCTIONS', 'ALL_FUNCTIONS_READ') or p.code = concat('READ_', r.report_name))) "
                    + " order by r.report_name, rp.parameter_id";
        } else {
            sql = getSQLtoRun(name, type, queryParams);
        }

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

    private String getSql(String name, String type) {

        String inputSql = "select " + type + "_sql as the_sql from stretchy_" + type + " where " + type + "_name = '" + name + "'";
        inputSql = genericDataService.wrapSQL(inputSql);

        String sqlErrorMsg = "Sql: " + inputSql;
        CachedRowSet rs = genericDataService.getCachedResultSet(inputSql, sqlErrorMsg);

        try {
            if (rs.next()) { return rs.getString("the_sql"); }
            throw new ReportNotFoundException(inputSql);

        } catch (SQLException e) {
            logger.info("msg: " + e.getMessage() + "   sql: " + inputSql);
            throw new PlatformDataIntegrityException("error.msg.sql.error", e.getMessage(), sqlErrorMsg);
        }

    }

    @Override
    public String getReportType(String reportName) {
        String sql = "SELECT ifnull(report_type,'') as report_type FROM `stretchy_report` where report_name = '" + reportName + "'";

        sql = genericDataService.wrapSQL(sql);

        String sqlErrorMsg = "Report Name: " + reportName + "   Sql: " + sql;
        CachedRowSet rs = genericDataService.getCachedResultSet(sql, sqlErrorMsg);

        try {

            if (rs.next()) { return rs.getString("report_type"); }
            throw new ReportNotFoundException(sql);

        } catch (SQLException e) {
            throw new PlatformDataIntegrityException("error.msg.sql.error", e.getMessage(), sqlErrorMsg);
        }

    }

    @Override
    public Response processPentahoRequest(String reportName, String outputTypeParam, Map<String, String> queryParams) {

        String outputType = "HTML";
        if (StringUtils.isNotBlank(outputTypeParam)) outputType = outputTypeParam;

        if (!(outputType.equalsIgnoreCase("HTML") || outputType.equalsIgnoreCase("PDF") || outputType.equalsIgnoreCase("XLS") || outputType
                .equalsIgnoreCase("CSV")))
            throw new PlatformDataIntegrityException("error.msg.invalid.outputType", "No matching Output Type: " + outputType);

        if (noPentaho)
            throw new PlatformDataIntegrityException("error.msg.no.pentaho", "Pentaho is not enabled", "Pentaho is not enabled");

        // TODO - use pentaho location finder like Pawel does in Mifos
        // String reportPath =
        // "C:\\dev\\apache-tomcat-7.0.25\\webapps\\ROOT\\PentahoReports\\"
        // + reportName + ".prpt";
        String reportPath = FileUtils.MIFOSX_BASE_DIR + File.separator
                + ThreadLocalContextUtil.getTenant().getName().replaceAll(" ", "").trim() + File.separator + "reports" + File.separator
                + reportName + ".prpt";
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

    private void addParametersToReport(MasterReport report, Map<String, String> queryParams) {

        try {
            ReportParameterValues rptParamValues = report.getParameterValues();
            ReportParameterDefinition paramsDefinition = report.getParameterDefinition();

            /*
             * only allow integer and string parameter types and assume all
             * mandatory - could go more detailed like Pawel did in Mifos later
             * and could match incoming and pentaho parameters better...
             * currently assuming they come in ok... and if not an error
             */
            for (ParameterDefinitionEntry paramDefEntry : paramsDefinition.getParameterDefinitions()) {
                String paramName = paramDefEntry.getName();
                String pValue = queryParams.get(paramName);
                if (StringUtils.isBlank(pValue))
                    throw new PlatformDataIntegrityException("error.msg.reporting.error", "Pentaho Parameter: " + paramName
                            + " - not Provided");

                Class<?> clazz = paramDefEntry.getValueType();
                logger.info("addParametersToReport(" + paramName + " : " + pValue + " : " + clazz.getCanonicalName() + ")");

                if (clazz.getCanonicalName().equalsIgnoreCase("java.lang.Integer"))
                    rptParamValues.put(paramName, Integer.parseInt(pValue));
                else if (clazz.getCanonicalName().equalsIgnoreCase("java.sql.Date"))
                    rptParamValues.put(paramName, Date.valueOf(pValue));
                else
                    rptParamValues.put(paramName, pValue);
            }

        } catch (Exception e) {
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

            List<ResultsetColumnHeader> columnHeaders = result.getColumnHeaders();
            List<ResultsetDataRow> data = result.getData();
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
        }
        catch (Exception e) {
            throw new PlatformDataIntegrityException("error.msg.exception.error", e.getMessage());
        }
    }
}