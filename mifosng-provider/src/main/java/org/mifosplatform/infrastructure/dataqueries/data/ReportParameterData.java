/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.dataqueries.data;

/* used to show list of parameters used by a report and also for getting a list of parameters available (the reportParameterName is left null */
final public class ReportParameterData {

	@SuppressWarnings("unused")
	private final Long id;
	@SuppressWarnings("unused")
	private final String reportParameterName;
	@SuppressWarnings("unused")
	private final String parameterName;

	public ReportParameterData(final Long id, final String reportParameterName,
			final String parameterName) {
		this.id = id;
		this.reportParameterName = reportParameterName;
		this.parameterName = parameterName;
	}
}