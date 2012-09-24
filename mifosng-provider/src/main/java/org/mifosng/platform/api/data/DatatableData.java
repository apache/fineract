package org.mifosng.platform.api.data;

public class DatatableData {

	private final String applicationTableName;
	private final String registeredTableName;

	public DatatableData(final String applicationTableName,
			final String registeredTableName) {
		this.applicationTableName = applicationTableName;
		this.registeredTableName = registeredTableName;
	}

	public String getApplicationTableName() {
		return applicationTableName;
	}

	public String getRegisteredTableName() {
		return registeredTableName;
	}

}