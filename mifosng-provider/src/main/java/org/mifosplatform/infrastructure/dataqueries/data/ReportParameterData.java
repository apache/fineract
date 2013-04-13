/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.dataqueries.data;

final public class ReportParameterData {

	@SuppressWarnings("unused")
	private final Long reportParameterId;
	@SuppressWarnings("unused")
	private final String reportParameterName;
	@SuppressWarnings("unused")
	private final String parameterName;

	public ReportParameterData(final Long reportParameterId,
			final String reportParameterName, final String parameterName) {
		this.reportParameterId = reportParameterId;
		this.reportParameterName = reportParameterName;
		this.parameterName = parameterName;
	}

}