package org.mifosng.platform.exceptions;

/**
 * A {@link RuntimeException} thrown when report resources are not found.
 */
public class ReportNotFoundException extends AbstractPlatformResourceNotFoundException {

	public ReportNotFoundException(final String reportSql) {
		super("error.msg.report.name.not.found", "Reporting Meta Data Entry Not Found", "Input Sql: " + reportSql);
	}
}