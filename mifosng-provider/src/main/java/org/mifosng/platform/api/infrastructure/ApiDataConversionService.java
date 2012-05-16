package org.mifosng.platform.api.infrastructure;

import java.math.BigDecimal;
import java.util.Locale;

import org.joda.time.LocalDate;

public interface ApiDataConversionService {

	LocalDate convertFrom(String dateAsString, String parameterName, String dateFormat);

	BigDecimal convertFrom(String principalFormatted, String parameterName, Locale clientApplicationLocale);

}
