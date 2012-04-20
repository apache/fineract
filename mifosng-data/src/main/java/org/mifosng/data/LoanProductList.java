package org.mifosng.data;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class LoanProductList {

	private Collection<LoanProductData> products = new ArrayList<LoanProductData>();

	protected LoanProductList() {
		//
	}

	public LoanProductList(final Collection<LoanProductData> products) {
		this.products = products;
	}

	public Collection<LoanProductData> getProducts() {
		return this.products;
	}

	public void setProducts(final Collection<LoanProductData> products) {
		this.products = products;
	}
}