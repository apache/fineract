/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.dataqueries.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

final public class ReportData {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final String reportName;
    @SuppressWarnings("unused")
    private final String reportType;
    @SuppressWarnings("unused")
    private final String reportSubType;
    @SuppressWarnings("unused")
    private final String reportCategory;
    @SuppressWarnings("unused")
    private final String description;
    @SuppressWarnings("unused")
    private final String reportSql;
    @SuppressWarnings("unused")
    private final Boolean coreReport;
    @SuppressWarnings("unused")
    private final Boolean useReport;
    @SuppressWarnings("unused")
    private final Collection<ReportParameterData> reportParameters;

    @SuppressWarnings("unused")
    private List<String> allowedReportTypes;
    @SuppressWarnings("unused")
    private List<String> allowedReportSubTypes;
    @SuppressWarnings("unused")
    private Collection<ReportParameterData> allowedParameters;

    public ReportData(final Long id, final String reportName, final String reportType, final String reportSubType,
            final String reportCategory, final String description, final String reportSql, final Boolean coreReport,
            final Boolean useReport, final Collection<ReportParameterData> reportParameters) {
        this.id = id;
        this.reportName = reportName;
        this.reportType = reportType;
        this.reportSubType = reportSubType;
        this.reportCategory = reportCategory;
        this.description = description;
        this.reportParameters = reportParameters;
        this.reportSql = reportSql;
        this.coreReport = coreReport;
        this.useReport = useReport;
        this.allowedReportTypes = null;
        this.allowedReportSubTypes = null;
        this.allowedParameters = null;
    }

    public ReportData() {
        this.id = null;
        this.reportName = null;
        this.reportType = null;
        this.reportSubType = null;
        this.reportCategory = null;
        this.description = null;
        this.reportParameters = null;
        this.reportSql = null;
        this.coreReport = null;
        this.useReport = null;
        this.allowedReportTypes = null;
        this.allowedReportSubTypes = null;
        this.allowedParameters = null;
    }

    public void appendedTemplate(final Collection<ReportParameterData> allowedParameters) {

        final List<String> reportTypes = new ArrayList<>();
        reportTypes.add("Table");
        reportTypes.add("Pentaho");
        reportTypes.add("Chart");
        this.allowedReportTypes = reportTypes;

        final List<String> reportSubTypes = new ArrayList<>();
        reportSubTypes.add("Bar");
        reportSubTypes.add("Pie");
        this.allowedReportSubTypes = reportSubTypes;

        this.allowedParameters = allowedParameters;

    }

}