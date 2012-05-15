package org.mifosng.platform.api.infrastructure;

import org.joda.time.LocalDate;

public interface ApiDataConversionService {

	LocalDate convertFrom(String dateAsString, String parameterName, String dateFormat);

}
