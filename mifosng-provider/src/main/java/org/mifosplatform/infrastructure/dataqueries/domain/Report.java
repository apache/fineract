/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.dataqueries.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

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
private boolean core_Report;

//only defines if report should appear in reference app UI List
@Column(name = "use_report", nullable = false)
private boolean useReport;

@Column(name = "report_sql")
private String reportSql;

/*
    @ManyToMany
    @JoinTable(name = "stretchy_report_parameter", joinColumns = @JoinColumn(name = "reportn_id"), inverseJoinColumns = @JoinColumn(name = "parameter_id"))
    private List<Charge> charges;
*/


    public Report(final String reportName, final String reportType, final String reportSubType, final String reportCategory, final String description,
    		final boolean core_Report, final boolean useReport, final String reportSql) {
    	this.reportName = reportName;
    	this.reportType = reportType;
    	this.reportSubType = reportSubType;
    	this.reportCategory = reportCategory;
    	this.description = description;
    	this.core_Report = core_Report;
    	this.useReport = useReport;
    	this.reportSql = reportSql;
    }

	public String getReportName() {
		return reportName;
	}

	public void setReportName(String reportName) {
		this.reportName = reportName;
	}

	public String getReportType() {
		return reportType;
	}

	public void setReportType(String reportType) {
		this.reportType = reportType;
	}

	public String getReportSubType() {
		return reportSubType;
	}

	public void setReportSubType(String reportSubType) {
		this.reportSubType = reportSubType;
	}

	public String getReportCategory() {
		return reportCategory;
	}

	public void setReportCategory(String reportCategory) {
		this.reportCategory = reportCategory;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isCore_Report() {
		return core_Report;
	}

	public void setCore_Report(boolean core_Report) {
		this.core_Report = core_Report;
	}

	public boolean isUseReport() {
		return useReport;
	}

	public void setUseReport(boolean useReport) {
		this.useReport = useReport;
	}

	public String getReportSql() {
		return reportSql;
	}

	public void setReportSql(String reportSql) {
		this.reportSql = reportSql;
	}

}
