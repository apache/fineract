/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.dataqueries.data;

import java.util.Collection;

final public class ReportData {

	@SuppressWarnings("unused")
	private final Long reportId;
	@SuppressWarnings("unused")
	private final String reportName;
	@SuppressWarnings("unused")
	private final String reportType;
	@SuppressWarnings("unused")
	private final String reportSubType;
	@SuppressWarnings("unused")
	private final String reportCategory;
	@SuppressWarnings("unused")
	private final String reportSql;
	@SuppressWarnings("unused")
	private final Boolean coreReport;
	@SuppressWarnings("unused")
	private final Boolean useReport;
	@SuppressWarnings("unused")
	private final Collection<ReportParameterData> reportParameters;

	public ReportData(final Long reportId, final String reportName,
			final String reportType, final String reportSubType,
			final String reportCategory, final String reportSql,
			final Boolean coreReport, final Boolean useReport,
			final Collection<ReportParameterData> reportParameters) {
		this.reportId = reportId;
		this.reportName = reportName;
		this.reportType = reportType;
		this.reportSubType = reportSubType;
		this.reportCategory = reportCategory;
		this.reportParameters = reportParameters;
		this.reportSql = reportSql;
		this.coreReport = coreReport;
		this.useReport = useReport;
	}

}