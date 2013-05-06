/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.dataqueries.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "stretchy_report_parameter")
public class ReportParameterUsage extends AbstractPersistable<Long> {
    
	@SuppressWarnings("unused")
	@ManyToOne(optional = false)
	@JoinColumn(name = "report_id", nullable = false)
	private Report report;

	@SuppressWarnings("unused")
	@ManyToOne(optional = false)
	@JoinColumn(name = "parameter_id", nullable = false)
	private ReportParameter parameter;

	@SuppressWarnings("unused")
	@Column(name = "report_parameter_name")
	private String reportParameterName;

	protected ReportParameterUsage() {
		//
	}

	public ReportParameterUsage(final Report report,
			final ReportParameter parameter, final String reportParameterName) {
		this.report = report;
		this.parameter = parameter;
		this.reportParameterName = reportParameterName;
	}

	public void setReport(Report report) {
		this.report = report;
	}

	public void setParameter(ReportParameter parameter) {
		this.parameter = parameter;
	}

	public void setReportParameterName(String reportParameterName) {
		this.reportParameterName = reportParameterName;
	}
	
}
