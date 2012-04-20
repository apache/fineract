@XmlJavaTypeAdapters({@XmlJavaTypeAdapter(type=DateTime.class, value=DateTimeAdapter.class),@XmlJavaTypeAdapter(type = LocalDate.class, value = LocalDateAdapter.class) })
package org.mifosng.data.query;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.mifosng.jodaadapters.DateTimeAdapter;
import org.mifosng.jodaadapters.LocalDateAdapter;

