/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.dataqueries.domain;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "stretchy_report", uniqueConstraints = { @UniqueConstraint(columnNames = { "report_name" }, name = "unq_report_name") })
public class Report extends AbstractPersistable<Long> {

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

	/*
	 * @ManyToMany
	 * 
	 * @JoinTable(name = "stretchy_report_parameter", joinColumns =
	 * @JoinColumn(name = "reportn_id"), inverseJoinColumns = @JoinColumn(name =
	 * "parameter_id")) private List<Charge> charges;
	 */

	public static Report fromJson(final JsonCommand command) {

		final String reportName = command
				.stringValueOfParameterNamed("reportName");
		final String reportType = command
				.stringValueOfParameterNamed("reportType");
		final String reportSubType = command
				.stringValueOfParameterNamed("reportSubType");
		final String reportCategory = command
				.stringValueOfParameterNamed("reportCategory");
		final String description = command
				.stringValueOfParameterNamed("description");
		final boolean useReport = command
				.booleanPrimitiveValueOfParameterNamed("useReport");
		final String reportSql = command
				.stringValueOfParameterNamed("reportSql");

		return new Report(reportName, reportType, reportSubType,
				reportCategory, description, useReport, reportSql);
	}

	protected Report() {
		//
	}

	public Report(final String reportName, final String reportType,
			final String reportSubType, final String reportCategory,
			final String description, 
			final boolean useReport, final String reportSql) {
		this.reportName = reportName;
		this.reportType = reportType;
		this.reportSubType = reportSubType;
		this.reportCategory = reportCategory;
		this.description = description;
		this.coreReport = false;
		this.useReport = useReport;
		this.reportSql = reportSql;
	}

    public Map<String, Object> update(final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(8);

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

        return actualChanges;
    }

	public boolean isCoreReport() {
		return coreReport;
	}  
    
}
