package org.mifosng.platform.api.data;

public class DepositProductLookup {

	private final Long id;
	private final String name;

	public DepositProductLookup(final Long id, final String name) {
		this.id = id;
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}