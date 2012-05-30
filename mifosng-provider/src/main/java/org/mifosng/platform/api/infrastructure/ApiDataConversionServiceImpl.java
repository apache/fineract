package org.mifosng.platform.api.infrastructure;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.mifosng.platform.api.data.ApiParameterError;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class ApiDataConversionServiceImpl implements ApiDataConversionService {

	@Override
	public LocalDate convertFrom(String dateAsString, String parameterName,
			String dateFormat) {
		LocalDate eventLocalDate = null;
		if (StringUtils.isNotBlank(dateAsString)) {
			try {
				Locale locale = LocaleContextHolder.getLocale();
				eventLocalDate = DateTimeFormat.forPattern(dateFormat)
						.withLocale(locale)
						.parseLocalDate(dateAsString.toLowerCase(locale));
			} catch (IllegalArgumentException e) {
				List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
				ApiParameterError error = ApiParameterError
						.parameterError(
								"validation.msg.invalid.date.format",
								"The parameter "
										+ parameterName
										+ " is invalid based on the dateFormat provided:"
										+ dateFormat, parameterName,
								dateAsString, dateFormat);
				dataValidationErrors.add(error);

				throw new PlatformApiDataValidationException(
						"validation.msg.validation.errors.exist",
						"Validation errors exist.", dataValidationErrors);
			}
		}

		return eventLocalDate;
	}
	
	@Override
	public Integer convertToInteger(String numericalValueFormatted, String parameterName, Locale clientApplicationLocale) {
		try {
			Integer number = null;

			if (StringUtils.isNotBlank(numericalValueFormatted)) {
				String sourceWithoutSpaces = numericalValueFormatted.replaceAll(" ", "");
				NumberFormat format = NumberFormat.getNumberInstance(clientApplicationLocale);
				Number parsedNumber = format.parse(sourceWithoutSpaces);
				number = parsedNumber.intValue();
			}

			return number;
		} catch (ParseException e) {

			List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
			ApiParameterError error = ApiParameterError.parameterError(
					"validation.msg.invalid.number.format", "The parameter "
							+ parameterName + " is invalid for provided locale"
							+ clientApplicationLocale.toString() + ".",
					parameterName, numericalValueFormatted,
					clientApplicationLocale);
			dataValidationErrors.add(error);

			throw new PlatformApiDataValidationException(
					"validation.msg.validation.errors.exist",
					"Validation errors exist.", dataValidationErrors);
		}
	}

	@Override
	public BigDecimal convertFrom(String numericalValueFormatted,
			String parameterName, Locale clientApplicationLocale) {

		try {
			BigDecimal number = null;

			if (StringUtils.isNotBlank(numericalValueFormatted)) {
				String sourceWithoutSpaces = numericalValueFormatted
						.replaceAll(" ", "");
				NumberFormat format = NumberFormat
						.getNumberInstance(clientApplicationLocale);
				Number parsedNumber = format.parse(sourceWithoutSpaces);
				number = BigDecimal.valueOf(Double.valueOf(parsedNumber
						.doubleValue()));
			}

			return number;
		} catch (ParseException e) {

			List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
			ApiParameterError error = ApiParameterError.parameterError(
					"validation.msg.invalid.number.format", "The parameter "
							+ parameterName + " is invalid for provided locale"
							+ clientApplicationLocale.toString() + ".",
					parameterName, numericalValueFormatted,
					clientApplicationLocale);
			dataValidationErrors.add(error);

			throw new PlatformApiDataValidationException(
					"validation.msg.validation.errors.exist",
					"Validation errors exist.", dataValidationErrors);
		}
	}

	@Override
	public Locale localeFromString(final String localeAsString) {
		
		Locale locale = Locale.getDefault();

		if (StringUtils.isNotBlank(localeAsString)) {
			String[] localeParts = localeAsString.split("_");
			
			String language = "en";
			String country = "GB";
			String variant = "";
			if (localeParts != null && localeParts.length == 1) {
				language = localeParts[0];
				locale = new Locale(language);
			}
			
			if (localeParts != null && localeParts.length == 2) {
				language = localeParts[0];
				country = localeParts[1];
				locale = new Locale(language, country);
			}
			
			if (localeParts != null && localeParts.length == 3) {
				language = localeParts[0];
				country = localeParts[1];
				variant = localeParts[2];
				locale = new Locale(language, country, variant);
			}
		}
		
		return locale;
	}
}