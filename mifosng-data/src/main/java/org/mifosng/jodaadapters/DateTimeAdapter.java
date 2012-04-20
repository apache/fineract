package org.mifosng.jodaadapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.joda.time.DateTime;

public class DateTimeAdapter extends XmlAdapter<String, DateTime> {

	@Override
	public DateTime unmarshal(final String v) throws Exception {
		return new DateTime(v);
	}

	@Override
	public String marshal(final DateTime v) throws Exception {
		return v.toString();
	}

}