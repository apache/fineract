package org.mifosng.jodaadapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.joda.time.LocalDate;

public class LocalDateAdapter extends XmlAdapter<String, LocalDate> {

	@Override
	public LocalDate unmarshal(final String v) throws Exception {
		return new LocalDate(v);
	}

	@Override
	public String marshal(final LocalDate v) throws Exception {
		return v.toString();
	}

}