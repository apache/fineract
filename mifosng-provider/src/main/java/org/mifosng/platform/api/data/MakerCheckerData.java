package org.mifosng.platform.api.data;

import org.joda.time.LocalDate;

/**
 * Immutable data object representing maker-checker entry
 */
final public class MakerCheckerData {

	@SuppressWarnings("unused")
	private Long id;
	
	@SuppressWarnings("unused")
	private String taskName;
	
	private String taskJson;

	@SuppressWarnings("unused")
	private final LocalDate madeOnDate;

	public MakerCheckerData(final Long id, final String taskName, final String taskJson, final LocalDate madeOnDate) {
		this.id = id;
		this.taskName = taskName;
		this.taskJson = taskJson;
		this.madeOnDate = madeOnDate;
	}

	public String json() {
		return this.taskJson;
	}
}