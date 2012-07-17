package org.mifosng.platform.api.infrastructure;

import java.math.BigDecimal;
import java.util.Locale;

import org.joda.time.LocalDate;
import org.mifosng.platform.api.commands.LoanProductCommand;

public interface ApiDataConversionService {

	LocalDate convertFrom(String dateAsString, String parameterName,
			String dateFormat);

	Integer convertToInteger(String digitsAfterDecimal, String string, Locale clientApplicationLocale);
	
	BigDecimal convertFrom(String principalFormatted, String parameterName,
			Locale clientApplicationLocale);

	Locale localeFromString(String locale);
	
	Locale localeFrom(String languageCode, String courntryCode, String variantCode);

	LoanProductCommand convertJsonToCommand(Long productId, String jsonRequestBody);
}
