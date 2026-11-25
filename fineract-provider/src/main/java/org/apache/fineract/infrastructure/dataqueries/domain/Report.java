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
package org.apache.fineract.infrastructure.dataqueries.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;

@Entity
@Table(name = "stretchy_report", uniqueConstraints = { @UniqueConstraint(columnNames = { "report_name" }, name = "unq_report_name") })
public final class Report extends AbstractPersistableCustom<Long> {

    @Column(name = "report_name", nullable = false, unique = true)
    private String reportName;

    @Column(name = "report_type", nullable = false)
    private String reportType;

    @Column(name = "report_subtype")
    private String reportSubType;

    @Column(name = "report_category")
    private String reportCategory;

    @Column(name = "description")
    private String description;

    @Column(name = "core_report", nullable = false)
    private boolean coreReport;

    // only defines if report should appear in reference app UI List
    @Column(name = "use_report", nullable = false)
    private boolean useReport;

    @Column(name = "report_sql")
    private String reportSql;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "report", orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<ReportParameterUsage> reportParameterUsages = new HashSet<>();

    Report() {
        //
    }

    public Report(final String reportName, final String reportType, final String reportSubType, final String reportCategory,
            final String description, final boolean useReport, final String reportSql, final Collection<String> reportTypes) {
        this.reportName = reportName;
        this.reportType = reportType;
        this.reportSubType = reportSubType;
        this.reportCategory = reportCategory;
        this.description = description;
        this.coreReport = false;
        this.useReport = useReport;
        this.reportSql = reportSql;
        validate(reportTypes);
    }

    public void updateReportName(final String reportName) {
        this.reportName = StringUtils.defaultIfEmpty(reportName, null);
    }

    public void updateReportType(final String reportType) {
        this.reportType = StringUtils.defaultIfEmpty(reportType, null);
    }

    public void updateReportSubType(final String reportSubType) {
        this.reportSubType = StringUtils.defaultIfEmpty(reportSubType, null);
    }

    public void updateReportCategory(final String reportCategory) {
        this.reportCategory = StringUtils.defaultIfEmpty(reportCategory, null);
    }

    public void updateDescription(final String description) {
        this.description = StringUtils.defaultIfEmpty(description, null);
    }

    public void updateUseReport(final boolean useReport) {
        this.useReport = useReport;
    }

    public void updateReportSql(final String reportSql) {
        this.reportSql = StringUtils.defaultIfEmpty(reportSql, null);
    }

    public boolean isCoreReport() {
        return this.coreReport;
    }

    public ReportParameterUsage findReportParameterById(final Long reportParameterId) {
        ReportParameterUsage reportParameterUsage = null;
        for (final ReportParameterUsage rpu : this.reportParameterUsages) {
            if (rpu.hasIdOf(reportParameterId)) {
                reportParameterUsage = rpu;
                break;
            }
        }
        return reportParameterUsage;
    }

    public void validate(final Collection<String> reportTypes) {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("report");

        baseDataValidator.reset().parameter("reportName").value(this.reportName).notBlank().notExceedingLengthOf(100);

        baseDataValidator.reset().parameter("reportType").value(this.reportType).notBlank().isOneOfTheseValues(reportTypes.toArray());

        baseDataValidator.reset().parameter("reportSubType").value(this.reportSubType).notExceedingLengthOf(20);

        if (StringUtils.isNotBlank(this.reportType)) {
            if (this.reportType.equals("Chart")) {
                baseDataValidator.reset().parameter("reportSubType").value(this.reportSubType)
                        .cantBeBlankWhenParameterProvidedIs("reportType", this.reportType)
                        .isOneOfTheseValues(new Object[] { "Bar", "Pie" });
            } else {
                baseDataValidator.reset().parameter("reportSubType").value(this.reportSubType)
                        .mustBeBlankWhenParameterProvidedIs("reportType", this.reportType);
            }
        }

        baseDataValidator.reset().parameter("reportCategory").value(this.reportCategory).notExceedingLengthOf(45);

        if (StringUtils.isNotBlank(this.reportType)) {
            if (this.reportType.equals("Table") || this.reportType.equals("Chart")) {
                baseDataValidator.reset().parameter("reportSql").value(this.reportSql).cantBeBlankWhenParameterProvidedIs("reportType",
                        this.reportType);
            } else {
                baseDataValidator.reset().parameter("reportSql").value(this.reportSql).mustBeBlankWhenParameterProvidedIs("reportType",
                        this.reportType);
            }
        }
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
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

    public String getDescription() {
        return this.description;
    }

    public boolean isUseReport() {
        return this.useReport;
    }

    public String getReportSql() {
        return this.reportSql;
    }

    public boolean update(final Set<ReportParameterUsage> newReportParameterUsages) {
        if (newReportParameterUsages == null) {
            return false;
        }

        boolean updated = false;

        if (changeInReportParameters(newReportParameterUsages)) {
            updated = true;
            this.reportParameterUsages.clear();
            this.reportParameterUsages.addAll(newReportParameterUsages);
        }
        return updated;
    }

    private boolean changeInReportParameters(final Set<ReportParameterUsage> newReportParameterUsages) {

        if (!this.reportParameterUsages.equals(newReportParameterUsages)) {
            return true;
        }

        return false;
    }

    public Set<ReportParameterUsage> getReportParameterUsages() {
        return this.reportParameterUsages;
    }

}
