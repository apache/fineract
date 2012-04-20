package org.mifosng.data.query;

import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.LocalDate;

@XmlRootElement
public class CalculatePayoffQuery {

	private LocalDate payoffDate;

	protected CalculatePayoffQuery() {
		//
	}

	public LocalDate getPayoffDate() {
		return this.payoffDate;
	}

	public void setPayoffDate(final LocalDate payoffDate) {
		this.payoffDate = payoffDate;
	}
}