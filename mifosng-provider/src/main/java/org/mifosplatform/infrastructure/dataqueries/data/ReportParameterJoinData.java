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
    private final Long parameterId;
    private final String reportParameterName;
    private final String parameterName;

    public ReportParameterJoinData(final Long reportId, final String reportName, final String reportType, final String reportSubType,
            final String reportCategory, final String description, final String reportSql, final Boolean coreReport,
            final Boolean useReport, final Long reportParameterId, final Long parameterId, final String reportParameterName,
            final String parameterName) {
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
        this.parameterId = parameterId;
        this.reportParameterName = reportParameterName;
        this.parameterName = parameterName;
    }

    public Long getReportId() {
        return this.reportId;
    }

    public String getReportName() {
        return this.reportName;
    }

    public String getReportType() {
        return this.reportType;
    }

    public String getReportSubType() {
        return this.reportSubType;
    }

    public String getReportCategory() {
        return this.reportCategory;
    }

    public String getReportSql() {
        return this.reportSql;
    }

    public Boolean getCoreReport() {
        return this.coreReport;
    }

    public Boolean getUseReport() {
        return this.useReport;
    }

    public Long getReportParameterId() {
        return this.reportParameterId;
    }

    public Long getParameterId() {
        return this.parameterId;
    }

    public String getReportParameterName() {
        return this.reportParameterName;
    }

    public String getParameterName() {
        return this.parameterName;
    }

    public String getDescription() {
        return this.description;
    }
}