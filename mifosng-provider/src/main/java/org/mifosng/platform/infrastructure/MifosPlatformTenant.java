package org.mifosng.platform.infrastructure;

public class MifosPlatformTenant {

	private final Long id;
	private final String name;
	private final String schemaName;

	public MifosPlatformTenant(Long id, String name, String schemaName) {
		this.id = id;
		this.name = name;
		this.schemaName = schemaName;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getSchemaName() {
		return schemaName;
	}
}
