package org.mifosng.platform.api.data;

public class DatatableData {

	private final String applicationTableName;
	private final String registeredTableName;
	private final String registeredTableLabel;

	public DatatableData(final String applicationTableName,
			final String registeredTableName, final String registeredTableLabel) {
		this.applicationTableName = applicationTableName;
		this.registeredTableName = registeredTableName;
		this.registeredTableLabel = registeredTableLabel;
	}

	public String getApplicationTableName() {
		return applicationTableName;
	}

	public String getRegisteredTableName() {
		return registeredTableName;
	}

	public String getRegisteredTableLabel() {
		return registeredTableLabel;
	}

}