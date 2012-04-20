package org.mifosng.ui.client;

import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;

public class PayoffFormBean {

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private DateTime payoffDate = new DateTime();

	public DateTime getPayoffDate() {
		return this.payoffDate;
	}

	public void setPayoffDate(final DateTime payoffDate) {
		this.payoffDate = payoffDate;
	}
}