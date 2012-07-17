package org.mifosng.platform.api.infrastructure;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.mifosng.platform.api.commands.LoanProductCommand;
import org.mifosng.platform.api.data.ApiParameterError;
import org.mifosng.platform.api.errorhandling.InvalidJsonException;
import org.mifosng.platform.api.errorhandling.UnsupportedParameterException;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.format.number.NumberFormatter;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Service
public class ApiDataConversionServiceImpl implements ApiDataConversionService {

	private final Gson gsonConverter;
	
	public ApiDataConversionServiceImpl() {
		gsonConverter = new Gson();
	}
	
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

		if (clientApplicationLocale == null) {
			
			List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
			String defaultMessage = new StringBuilder("The parameter '" + parameterName + "' requires a 'locale' parameter to be passed with it.").toString();
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.missing.locale.parameter", defaultMessage, parameterName);
			dataValidationErrors.add(error);
			
			throw new PlatformApiDataValidationException(
					"validation.msg.validation.errors.exist",
					"Validation errors exist.", dataValidationErrors);
		}
		
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

	@Override
	public LoanProductCommand convertJsonToCommand(final Long resourceIdentifier, final String json) {
		
		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}
		
		Type typeOfMap = new TypeToken<Map<String, String>>(){}.getType();
	    Map<String, String> requestMap = gsonConverter.fromJson(json, typeOfMap);
	    
	    
	    Set<String> supportedParams = new HashSet<String>(
	    		Arrays.asList("name", "description", "externalId", "fundId", "currencyCode", "digitsAfterDecimalValue", 
	    				"principal", "inArrearsTolerance", "interestRatePerPeriod", "repaymentEvery", "numberOfRepayments", 
	    				"repaymentFrequencyType", "interestRateFrequencyType", "amortizationType", "interestType", "interestCalculationPeriodType")
	    );
	    
	    checkForUnsupportedParameters(requestMap, supportedParams);
	    
	    Set<String> modifiedParameters = new HashSet<String>();

	    String name = extractStringParameter("name", requestMap, modifiedParameters);
	    String description = extractStringParameter("description", requestMap, modifiedParameters);
	    String externalId = extractStringParameter("externalId", requestMap, modifiedParameters);
	    Long fundId = extractLongParameter("fundId", requestMap, modifiedParameters);
	    String currencyCode = extractStringParameter("currencyCode", requestMap, modifiedParameters);
	    Integer digitsAfterDecimalValue = extractIntegerParameter("digitsAfterDecimal", requestMap, modifiedParameters);
	    BigDecimal principalValue = extractBigDecimalParameter("principal", requestMap, modifiedParameters);
	    BigDecimal inArrearsToleranceValue = extractBigDecimalParameter("inArrearsTolerance", requestMap, modifiedParameters);
	    BigDecimal interestRatePerPeriodValue = extractBigDecimalParameter("interestRatePerPeriod", requestMap, modifiedParameters);
	    
	    Integer repaymentEveryValue = extractIntegerParameter("repaymentEvery", requestMap, modifiedParameters);
	    Integer numberOfRepaymentsValue = extractIntegerParameter("numberOfRepayments", requestMap, modifiedParameters);
	    Integer repaymentFrequencyTypeValue = extractIntegerParameter("repaymentFrequencyType", requestMap, modifiedParameters);
	    
	    Integer interestRateFrequencyTypeValue = extractIntegerParameter("interestRateFrequencyType", requestMap, modifiedParameters);
	    Integer amortizationTypeValue = extractIntegerParameter("amortizationType", requestMap, modifiedParameters);
	    Integer interestTypeValue = extractIntegerParameter("interestType", requestMap, modifiedParameters);
	    Integer interestCalculationPeriodTypeValue = extractIntegerParameter("interestCalculationPeriodType", requestMap, modifiedParameters);
	    
		return new LoanProductCommand(modifiedParameters, resourceIdentifier, name, description, externalId, fundId, 
				currencyCode, digitsAfterDecimalValue, principalValue, inArrearsToleranceValue, numberOfRepaymentsValue, repaymentEveryValue, interestRatePerPeriodValue,
				repaymentFrequencyTypeValue, interestRateFrequencyTypeValue, amortizationTypeValue, interestTypeValue, interestCalculationPeriodTypeValue);
	}

	private void checkForUnsupportedParameters(Map<String, String> requestMap, Set<String> supportedParams) {
		List<String> unsupportedParameterList = new ArrayList<String>();
		for (String providedParameter : requestMap.keySet()) {
			if (!supportedParams.contains(providedParameter)) {
				unsupportedParameterList.add(providedParameter);
			}
		}
		
		if (!unsupportedParameterList.isEmpty()) {
			throw new UnsupportedParameterException(unsupportedParameterList);
		}
	}

	private String extractStringParameter(final String paramName, final Map<String, String> requestMap, final Set<String> modifiedParameters) {
		String paramValue = null;
		if (requestMap.containsKey(paramName)) {
			paramValue = requestMap.get(paramName);
			modifiedParameters.add(paramName);
		}
		return paramValue;
	}
	
	private Integer extractIntegerParameter(final String paramName, final Map<String, String> requestMap, final Set<String> modifiedParameters) {
		Integer paramValue = null;
		if (requestMap.containsKey(paramName)) {
			paramValue = convertToInteger(requestMap.get(paramName), paramName, extractLocaleValue(requestMap));
			modifiedParameters.add(paramName);
		}
		return paramValue;
	}

	private BigDecimal extractBigDecimalParameter(final String paramName, final Map<String, String> requestMap, final Set<String> modifiedParameters) {
		BigDecimal paramValue = null;
		if (requestMap.containsKey(paramName)) {
			paramValue = convertFrom(requestMap.get(paramName), paramName, extractLocaleValue(requestMap));
			modifiedParameters.add(paramName);
		}
		return paramValue;
	}
	
	private Long extractLongParameter(final String paramName, final Map<String, String> requestMap, final Set<String> modifiedParameters) {
		Long paramValue = null;
		if (requestMap.containsKey(paramName)) {
			String valueAsString = requestMap.get(paramName);
			if (StringUtils.isNotBlank(valueAsString)) {
				paramValue = Long.valueOf(valueAsString);
			}
			modifiedParameters.add(paramName);
		}
		return paramValue;
	}

	private Locale extractLocaleValue(Map<String, String> requestMap) {
		Locale clientApplicationLocale = null;
	    String locale = null;
	    if (requestMap.containsKey("locale")) {
	    	locale = requestMap.get("locale");
	    	clientApplicationLocale = localeFromString(locale);
	    }
		return clientApplicationLocale;
	}
}