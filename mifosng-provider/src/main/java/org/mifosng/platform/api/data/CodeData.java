package org.mifosng.platform.api.data;

/**
 * Immutable data object representing a code.
 */
public class CodeData {

	@SuppressWarnings("unused")
	private final Long id;
	@SuppressWarnings("unused")
	private final String name;

	public CodeData(final Long id, final String name) {
		this.id = id;
		this.name = name;
	}
}