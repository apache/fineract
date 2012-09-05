package org.mifosng.platform.api.data;

/**
 * Immutable data object used for basic loan product info for lookup situations.
 * 
 * TODO - kw - this can be replace and the normal {@link LoanProductData} used where only id and name are populated as google gson does 
 * not return null attributes/parameters.
 */
public class LoanProductLookup {

	private final Long id;
	private final String name;

	public LoanProductLookup(final Long id, final String name) {
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