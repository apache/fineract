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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

import com.google.gson.JsonArray;

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

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "report", orphanRemoval = true, fetch=FetchType.EAGER)
    private Set<ReportParameterUsage> reportParameterUsages = new HashSet<>();

    public static Report fromJson(final JsonCommand command, final Collection<String> reportTypes) {

        String reportName = null;
        String reportType = null;
        String reportSubType = null;
        String reportCategory = null;
        String description = null;
        boolean useReport = false;
        String reportSql = null;

        if (command.parameterExists("reportName")) {
            reportName = command.stringValueOfParameterNamed("reportName");
        }
        if (command.parameterExists("reportType")) {
            reportType = command.stringValueOfParameterNamed("reportType");
        }
        if (command.parameterExists("reportSubType")) {
            reportSubType = command.stringValueOfParameterNamed("reportSubType");
        }
        if (command.parameterExists("reportCategory")) {
            reportCategory = command.stringValueOfParameterNamed("reportCategory");
        }
        if (command.parameterExists("description")) {
            description = command.stringValueOfParameterNamed("description");
        }
        if (command.parameterExists("useReport")) {
            useReport = command.booleanPrimitiveValueOfParameterNamed("useReport");
        }
        if (command.parameterExists("reportSql")) {
            reportSql = command.stringValueOfParameterNamed("reportSql");
        }

        return new Report(reportName, reportType, reportSubType, reportCategory, description, useReport, reportSql, reportTypes);
    }

    protected Report() {
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

    public Map<String, Object> update(final JsonCommand command, final Collection<String> reportTypes) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>(8);

        String paramName = "reportName";
        if (command.isChangeInStringParameterNamed(paramName, this.reportName)) {
            final String newValue = command.stringValueOfParameterNamed(paramName);
            actualChanges.put(paramName, newValue);
            this.reportName = StringUtils.defaultIfEmpty(newValue, null);
        }
        paramName = "reportType";
        if (command.isChangeInStringParameterNamed(paramName, this.reportType)) {
            final String newValue = command.stringValueOfParameterNamed(paramName);
            actualChanges.put(paramName, newValue);
            this.reportType = StringUtils.defaultIfEmpty(newValue, null);
        }
        paramName = "reportSubType";
        if (command.isChangeInStringParameterNamed(paramName, this.reportSubType)) {
            final String newValue = command.stringValueOfParameterNamed(paramName);
            actualChanges.put(paramName, newValue);
            this.reportSubType = StringUtils.defaultIfEmpty(newValue, null);
        }
        paramName = "reportCategory";
        if (command.isChangeInStringParameterNamed(paramName, this.reportCategory)) {
            final String newValue = command.stringValueOfParameterNamed(paramName);
            actualChanges.put(paramName, newValue);
            this.reportCategory = StringUtils.defaultIfEmpty(newValue, null);
        }
        paramName = "description";
        if (command.isChangeInStringParameterNamed(paramName, this.description)) {
            final String newValue = command.stringValueOfParameterNamed(paramName);
            actualChanges.put(paramName, newValue);
            this.description = StringUtils.defaultIfEmpty(newValue, null);
        }
        paramName = "useReport";
        if (command.isChangeInBooleanParameterNamed(paramName, this.useReport)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(paramName);
            actualChanges.put(paramName, newValue);
            this.useReport = newValue;
        }
        paramName = "reportSql";
        if (command.isChangeInStringParameterNamed(paramName, this.reportSql)) {
            final String newValue = command.stringValueOfParameterNamed(paramName);
            actualChanges.put(paramName, newValue);
            this.reportSql = StringUtils.defaultIfEmpty(newValue, null);
        }

        final String reportParametersParamName = "reportParameters";
        if (command.hasParameter(reportParametersParamName)) {
            final JsonArray jsonArray = command.arrayOfParameterNamed(reportParametersParamName);
            if (jsonArray != null) {
                actualChanges.put(reportParametersParamName, command.jsonFragment(reportParametersParamName));
            }
        }

        validate(reportTypes);

        if (!actualChanges.isEmpty()) {
            if (isCoreReport()) {
                for (final String key : actualChanges.keySet()) {
                    if (!(key.equals("useReport"))) { throw new PlatformDataIntegrityException(
                            "error.msg.only.use.report.can.be.updated.for.core.report",
                            "Only the Use Report field can be updated for Core Reports", key); }
                }
            }
        }

        return actualChanges;
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

    private void validate(final Collection<String> reportTypes) {

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
            if ((this.reportType.equals("Table")) || (this.reportType.equals("Chart"))) {
                baseDataValidator.reset().parameter("reportSql").value(this.reportSql)
                        .cantBeBlankWhenParameterProvidedIs("reportType", this.reportType);
            } else {
                baseDataValidator.reset().parameter("reportSql").value(this.reportSql)
                        .mustBeBlankWhenParameterProvidedIs("reportType", this.reportType);
            }
        }
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }

    public String getReportName() {
        return this.reportName;
    }

    public boolean update(final Set<ReportParameterUsage> newReportParameterUsages) {
        if (newReportParameterUsages == null) { return false; }

        boolean updated = false;

        if (changeInReportParameters(newReportParameterUsages)) {
            updated = true;
            this.reportParameterUsages.clear();
            this.reportParameterUsages.addAll(newReportParameterUsages);
        }
        return updated;
    }

    private boolean changeInReportParameters(final Set<ReportParameterUsage> newReportParameterUsages) {

        if (!(this.reportParameterUsages.equals(newReportParameterUsages))) { return true; }

        return false;
    }
	
	public Set<ReportParameterUsage> getReportParameterUsages() {
        return this.reportParameterUsages;
    }
}