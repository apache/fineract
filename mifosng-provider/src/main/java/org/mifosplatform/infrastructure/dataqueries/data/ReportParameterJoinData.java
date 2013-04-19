/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.dataqueries.data;

final public class ReportParameterJoinData {

    private final Long reportId;
    private final String reportName;
    private final String reportType;
    private final String reportSubType;
    private final String reportCategory;
    private final String description;
    private final String reportSql;
    private final Boolean coreReport;
    private final Boolean useReport;

    private final Long reportParameterId;
    private final String reportParameterName;
    private final String parameterName;

    public ReportParameterJoinData(final Long reportId, final String reportName, final String reportType, final String reportSubType,
            final String reportCategory, final String description, final String reportSql, final Boolean coreReport,
            final Boolean useReport, final Long reportParameterId, final String reportParameterName, final String parameterName) {
        this.reportId = reportId;
        this.reportName = reportName;
        this.reportType = reportType;
        this.reportSubType = reportSubType;
        this.reportCategory = reportCategory;
        this.description = description;
        this.reportSql = reportSql;
        this.coreReport = coreReport;
        this.useReport = useReport;
        this.reportParameterId = reportParameterId;
        this.reportParameterName = reportParameterName;
        this.parameterName = parameterName;
    }

    public Long getReportId() {
        return reportId;
    }

    public String getReportName() {
        return reportName;
    }

    public String getReportType() {
        return reportType;
    }

    public String getReportSubType() {
        return reportSubType;
    }

    public String getReportCategory() {
        return reportCategory;
    }

    public String getReportSql() {
        return reportSql;
    }

    public Boolean getCoreReport() {
        return coreReport;
    }

    public Boolean getUseReport() {
        return useReport;
    }

    public Long getReportParameterId() {
        return reportParameterId;
    }

    public String getReportParameterName() {
        return reportParameterName;
    }

    public String getParameterName() {
        return parameterName;
    }

    public String getDescription() {
        return description;
    }
}