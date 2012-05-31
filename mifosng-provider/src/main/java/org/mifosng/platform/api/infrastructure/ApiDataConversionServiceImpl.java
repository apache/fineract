package org.mifosng.platform.api.infrastructure;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.mifosng.platform.api.data.ApiParameterError;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.format.number.NumberFormatter;
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
				
				String source = numericalValueFormatted.trim();
				
				NumberFormat format = NumberFormat.getInstance(clientApplicationLocale);
				DecimalFormat df = (DecimalFormat) format;
				DecimalFormatSymbols symbols = df.getDecimalFormatSymbols();
				df.setParseBigDecimal(true);
				
				// http://bugs.sun.com/view_bug.do?bug_id=4510618
				char groupingSeparator = symbols.getGroupingSeparator();
				if (groupingSeparator == '\u00a0') {
					source = source.replaceAll(" ", Character.toString('\u00a0'));
			    }
				
				Number parsedNumber = df.parse(source);
				
				double parsedNumberDouble = parsedNumber.doubleValue();
				int parsedNumberInteger = parsedNumber.intValue();
				
				if (source.contains(Character.toString(symbols.getDecimalSeparator()))) {
					throw new ParseException(source, 0);
				}
				
				if (!Double.valueOf(parsedNumberDouble).equals(Double.valueOf(Integer.valueOf(parsedNumberInteger)))) {
					throw new ParseException(source, 0);
				}
				
				number = parsedNumber.intValue();
			}

			return number;
		} catch (ParseException e) {

			List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
			ApiParameterError error = ApiParameterError.parameterError(
					"validation.msg.invalid.integer.format", "The parameter "
							+ parameterName + " has value: " + numericalValueFormatted + " which is invalid integer value for provided locale of ["
							+ clientApplicationLocale.toString() + "].",
					parameterName, numericalValueFormatted,
					clientApplicationLocale);
			dataValidationErrors.add(error);

			throw new PlatformApiDataValidationException(
					"validation.msg.validation.errors.exist",
					"Validation errors exist.", dataValidationErrors);
		}
	}

	@Override
	public BigDecimal convertFrom(final String numericalValueFormatted, final String parameterName, final Locale clientApplicationLocale) {

		try {
			BigDecimal number = null;
			
			if (StringUtils.isNotBlank(numericalValueFormatted)) {
				
				String source = numericalValueFormatted.trim();
				
				NumberFormat format = NumberFormat.getNumberInstance(clientApplicationLocale);
				DecimalFormat df = (DecimalFormat) format;
				DecimalFormatSymbols symbols = df.getDecimalFormatSymbols();
				// http://bugs.sun.com/view_bug.do?bug_id=4510618
				char groupingSeparator = symbols.getGroupingSeparator();
				if (groupingSeparator == '\u00a0') {
					source = source.replaceAll(" ", Character.toString('\u00a0'));
			    }
				
				NumberFormatter numberFormatter = new NumberFormatter();
				Number parsedNumber = numberFormatter.parse(source, clientApplicationLocale);
				number = BigDecimal.valueOf(Double.valueOf(parsedNumber.doubleValue()));
			}

			return number;
		} catch (ParseException e) {

			List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
			ApiParameterError error = ApiParameterError.parameterError(
					"validation.msg.invalid.decimal.format", "The parameter "
							+ parameterName + " has value: " + numericalValueFormatted + " which is invalid decimal value for provided locale of ["
							+ clientApplicationLocale.toString() + "].",
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
		
		if (StringUtils.isBlank(localeAsString)) {
			List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.invalid.locale.format", "The parameter locale is invalid. It cannot be blank.", "locale");
			dataValidationErrors.add(error);

			throw new PlatformApiDataValidationException(
					"validation.msg.validation.errors.exist",
					"Validation errors exist.", dataValidationErrors);
		}
		
		String languageCode = "";
		String courntryCode = "";
		String variantCode = "";
		
		String[] localeParts = localeAsString.split("_");
		
		if (localeParts != null && localeParts.length == 1) {
			languageCode = localeParts[0];
		}
		
		if (localeParts != null && localeParts.length == 2) {
			languageCode = localeParts[0];
			courntryCode = localeParts[1];
		}
		
		if (localeParts != null && localeParts.length == 3) {
			languageCode = localeParts[0];
			courntryCode = localeParts[1];
			variantCode = localeParts[2];
		}
		
		return localeFrom(languageCode, courntryCode, variantCode);
	}

	@Override
	public Locale localeFrom(final String languageCode, final String courntryCode, final String variantCode) {
		
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		
		List<String> allowedLanguages = Arrays.asList(Locale.getISOLanguages());
		if (!allowedLanguages.contains(languageCode.toLowerCase())) {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.invalid.locale.format", "The parameter locale has an invalid language value " + languageCode + " .", "locale", languageCode);
			dataValidationErrors.add(error);			
		}
		
		if (StringUtils.isNotBlank(courntryCode.toUpperCase())) {
			List<String> allowedCountries = Arrays.asList(Locale.getISOCountries());
			if (!allowedCountries.contains(courntryCode)) {
				ApiParameterError error = ApiParameterError.parameterError("validation.msg.invalid.locale.format", "The parameter locale has an invalid country value " + courntryCode + " .", "locale", courntryCode);
				dataValidationErrors.add(error);			
			}
		}
		
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException(
					"validation.msg.validation.errors.exist",
					"Validation errors exist.", dataValidationErrors);
		}
		
		return new Locale(languageCode.toLowerCase(), courntryCode.toUpperCase(), variantCode);
	}
}