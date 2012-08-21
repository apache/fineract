package org.mifosng.platform.api.data;

/**
 * Immutable data object for 'additional data'.
 */
public class AdditionalFieldsSetData {

	private final Integer id;
	private final String name;
	private final String type;

	public AdditionalFieldsSetData(final Integer id, final String name, final String type) {
		this.id = id;
		this.name = name;
		this.type = type;
	}

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}
}