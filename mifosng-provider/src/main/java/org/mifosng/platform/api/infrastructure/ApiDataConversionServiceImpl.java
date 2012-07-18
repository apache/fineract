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
import org.mifosng.platform.api.commands.AdjustLoanTransactionCommand;
import org.mifosng.platform.api.commands.BranchMoneyTransferCommand;
import org.mifosng.platform.api.commands.ClientCommand;
import org.mifosng.platform.api.commands.FundCommand;
import org.mifosng.platform.api.commands.LoanProductCommand;
import org.mifosng.platform.api.commands.LoanStateTransitionCommand;
import org.mifosng.platform.api.commands.LoanTransactionCommand;
import org.mifosng.platform.api.commands.OfficeCommand;
import org.mifosng.platform.api.commands.RoleCommand;
import org.mifosng.platform.api.commands.SubmitLoanApplicationCommand;
import org.mifosng.platform.api.commands.UserCommand;
import org.mifosng.platform.api.data.ApiParameterError;
import org.mifosng.platform.api.errorhandling.InvalidJsonException;
import org.mifosng.platform.api.errorhandling.UnsupportedParameterException;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.format.number.NumberFormatter;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

@Service
public class ApiDataConversionServiceImpl implements ApiDataConversionService {

	private final Gson gsonConverter;
	
	public ApiDataConversionServiceImpl() {
		gsonConverter = new Gson();
	}
	
	private LocalDate convertFrom(final String dateAsString, final String parameterName, final String dateFormat) {
		
		if (StringUtils.isBlank(dateFormat)) {
			
			List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
			String defaultMessage = new StringBuilder("The parameter '" + parameterName + "' requires a 'dateFormat' parameter to be passed with it.").toString();
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.missing.dateFormat.parameter", defaultMessage, parameterName);
			dataValidationErrors.add(error);
			
			throw new PlatformApiDataValidationException(
					"validation.msg.validation.errors.exist",
					"Validation errors exist.", dataValidationErrors);
		}
		
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
	
	private Integer convertToInteger(String numericalValueFormatted, String parameterName, Locale clientApplicationLocale) {
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

	private BigDecimal convertFrom(final String numericalValueFormatted, final String parameterName, final Locale clientApplicationLocale) {

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

	private Locale localeFromString(final String localeAsString) {
		
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

	private Locale localeFrom(final String languageCode, final String courntryCode, final String variantCode) {
		
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
	public LoanProductCommand convertJsonToLoanProductCommand(final Long resourceIdentifier, final String json) {
		
		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}
		
		Type typeOfMap = new TypeToken<Map<String, String>>(){}.getType();
	    Map<String, String> requestMap = gsonConverter.fromJson(json, typeOfMap);
	    
	    Set<String> supportedParams = new HashSet<String>(
	    		Arrays.asList("name", "description", "fundId", "currencyCode", "digitsAfterDecimal", 
	    				"principal", "inArrearsTolerance", "interestRatePerPeriod", "repaymentEvery", "numberOfRepayments", 
	    				"repaymentFrequencyType", "interestRateFrequencyType", "amortizationType", "interestType", "interestCalculationPeriodType", "locale")
	    );
	    
	    checkForUnsupportedParameters(requestMap, supportedParams);
	    
	    Set<String> modifiedParameters = new HashSet<String>();

	    String name = extractStringParameter("name", requestMap, modifiedParameters);
	    String description = extractStringParameter("description", requestMap, modifiedParameters);
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
	    
		return new LoanProductCommand(modifiedParameters, resourceIdentifier, name, description, fundId, 
				currencyCode, digitsAfterDecimalValue, principalValue, inArrearsToleranceValue, numberOfRepaymentsValue, repaymentEveryValue, interestRatePerPeriodValue,
				repaymentFrequencyTypeValue, interestRateFrequencyTypeValue, amortizationTypeValue, interestTypeValue, interestCalculationPeriodTypeValue);
	}
	
	@Override
	public FundCommand convertJsonToFundCommand(final Long resourceIdentifier, final String json) {

		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}
		
		Type typeOfMap = new TypeToken<Map<String, String>>(){}.getType();
	    Map<String, String> requestMap = gsonConverter.fromJson(json, typeOfMap);
	    
	    
	    Set<String> supportedParams = new HashSet<String>(
	    		Arrays.asList("name", "externalId")
	    );
	    
	    checkForUnsupportedParameters(requestMap, supportedParams);
	    
	    Set<String> modifiedParameters = new HashSet<String>();
	    
	    String name = extractStringParameter("name", requestMap, modifiedParameters);
	    String externalId = extractStringParameter("externalId", requestMap, modifiedParameters);
	    
		return new FundCommand(modifiedParameters, resourceIdentifier, name, externalId);
	}
	
	@Override
	public OfficeCommand convertJsonToOfficeCommand(final Long resourceIdentifier, final String json) {
		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}
		
		Type typeOfMap = new TypeToken<Map<String, String>>(){}.getType();
	    Map<String, String> requestMap = gsonConverter.fromJson(json, typeOfMap);
	    
	    Set<String> supportedParams = new HashSet<String>(
	    		Arrays.asList("name", "externalId", "parentId", "openingDate", "dateFormat")
	    );
	    
	    checkForUnsupportedParameters(requestMap, supportedParams);
	    
	    Set<String> modifiedParameters = new HashSet<String>();
	    
	    String name = extractStringParameter("name", requestMap, modifiedParameters);
	    String externalId = extractStringParameter("externalId", requestMap, modifiedParameters);
	    Long parentId = extractLongParameter("parentId", requestMap, modifiedParameters);
	    LocalDate openingLocalDate = extractLocalDateParameter("openingDate", requestMap, modifiedParameters);
	    
	    return new OfficeCommand(modifiedParameters, resourceIdentifier, name, externalId, parentId, openingLocalDate);
	}
	
	@Override
	public RoleCommand convertJsonToRoleCommand(final Long resourceIdentifier, final String json) {
		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}
		
		Type typeOfMap = new TypeToken<Map<String, Object>>(){}.getType();
	    Map<String, Object> requestMap = gsonConverter.fromJson(json, typeOfMap);
	    
	    Set<String> supportedParams = new HashSet<String>(
	    		Arrays.asList("name", "description", "permissions")
	    );
	    
	    checkForUnsupportedParameters(requestMap, supportedParams);
	    
	    Set<String> modifiedParameters = new HashSet<String>();
	    
	    // check array
	    JsonParser parser = new JsonParser();
		
		String[] permissionIds = null;
		JsonElement element = parser.parse(json);
		if (element.isJsonObject()) {
			JsonObject object = element.getAsJsonObject();
			if (object.has("permissions")) {
				modifiedParameters.add("permissions");
				JsonArray array = object.get("permissions").getAsJsonArray();
				permissionIds = new String[array.size()];
				for (int i=0; i<array.size(); i++) {
					permissionIds[i] = array.get(i).getAsString();
				}
			}
		}
	    //
	    
	    String name = extractStringParameter("name", requestMap, modifiedParameters);
	    String description = extractStringParameter("description", requestMap, modifiedParameters);
	    
	    return new RoleCommand(modifiedParameters, resourceIdentifier, name, description, permissionIds);
	}
	
	@Override
	public UserCommand convertJsonToUserCommand(final Long resourceIdentifier, final String json) {
		
		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}
		
		Type typeOfMap = new TypeToken<Map<String, Object>>(){}.getType();
	    Map<String, Object> requestMap = gsonConverter.fromJson(json, typeOfMap);
	    
	    Set<String> supportedParams = new HashSet<String>(
	    		Arrays.asList("username", "firstname", "lastname", "password", "repeatPassword", "email", "officeId", "notSelectedRoles", "roles")
	    );
	    
	    checkForUnsupportedParameters(requestMap, supportedParams);
	    
	    Set<String> modifiedParameters = new HashSet<String>();
	    
	    String username = extractStringParameter("username", requestMap, modifiedParameters);
	    String firstname = extractStringParameter("firstname", requestMap, modifiedParameters);
	    String lastname = extractStringParameter("lastname", requestMap, modifiedParameters);
	    String password = extractStringParameter("password", requestMap, modifiedParameters);
	    String repeatPassword = extractStringParameter("repeatPassword", requestMap, modifiedParameters);
	    String email = extractStringParameter("email", requestMap, modifiedParameters);
	    Long officeId = extractLongParameter("officeId", requestMap, modifiedParameters);
	    
	    // check array
	    JsonParser parser = new JsonParser();
		
		String[] notSelectedRoles = null;
		String[] roles = null;
		JsonElement element = parser.parse(json);
		if (element.isJsonObject()) {
			JsonObject object = element.getAsJsonObject();
			if (object.has("notSelectedRoles")) {
				modifiedParameters.add("notSelectedRoles");
				JsonArray array = object.get("notSelectedRoles").getAsJsonArray();
				notSelectedRoles = new String[array.size()];
				for (int i=0; i<array.size(); i++) {
					notSelectedRoles[i] = array.get(i).getAsString();
				}
			}
			
			if (object.has("roles")) {
				modifiedParameters.add("roles");
				JsonArray array = object.get("roles").getAsJsonArray();
				roles = new String[array.size()];
				for (int i=0; i<array.size(); i++) {
					roles[i] = array.get(i).getAsString();
				}
			}
		}
	    //
	    
		return new UserCommand(modifiedParameters, resourceIdentifier, username, firstname, lastname, password, repeatPassword, email, officeId, notSelectedRoles, roles);
	}
	
	@Override
	public BranchMoneyTransferCommand convertJsonToBranchMoneyTransferCommand(final String json) {
		
		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}
		
		Type typeOfMap = new TypeToken<Map<String, Object>>(){}.getType();
	    Map<String, Object> requestMap = gsonConverter.fromJson(json, typeOfMap);
	    
	    Set<String> supportedParams = new HashSet<String>(
	    		Arrays.asList("fromOfficeId", "toOfficeId", "transactionDate", "currencyCode", "transactionAmount", "description", "locale", "dateFormat")
	    );
	    
	    checkForUnsupportedParameters(requestMap, supportedParams);
	    
	    Set<String> modifiedParameters = new HashSet<String>();
	   
	    Long fromOfficeId = extractLongParameter("fromOfficeId", requestMap, modifiedParameters);
	    Long toOfficeId = extractLongParameter("toOfficeId", requestMap, modifiedParameters);
	    LocalDate transactionLocalDate = extractLocalDateParameter("transactionDate", requestMap, modifiedParameters);
	    String currencyCode = extractStringParameter("currencyCode", requestMap, modifiedParameters);
	    BigDecimal transactionAmountValue = extractBigDecimalParameter("transactionAmount", requestMap, modifiedParameters);
	    String description = extractStringParameter("description", requestMap, modifiedParameters);
	    
	    return new BranchMoneyTransferCommand(modifiedParameters, fromOfficeId, toOfficeId, transactionLocalDate, currencyCode, transactionAmountValue, description);
	}
	
	@Override
	public ClientCommand convertJsonToClientCommand(final Long resourceIdentifier, final String json) {
		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}
		
		Type typeOfMap = new TypeToken<Map<String, Object>>(){}.getType();
	    Map<String, Object> requestMap = gsonConverter.fromJson(json, typeOfMap);
	    
	    Set<String> supportedParams = new HashSet<String>(
	    		Arrays.asList("externalId", "firstname", "lastname", "clientOrBusinessName", "officeId", "joiningDate", "dateFormat")
	    );
	    
	    checkForUnsupportedParameters(requestMap, supportedParams);
	    
	    Set<String> modifiedParameters = new HashSet<String>();
	   
	    String externalId = extractStringParameter("externalId", requestMap, modifiedParameters);
	    Long officeId = extractLongParameter("officeId", requestMap, modifiedParameters);
	    LocalDate joiningDate = extractLocalDateParameter("joiningDate", requestMap, modifiedParameters);
	    String firstname = extractStringParameter("firstname", requestMap, modifiedParameters);
	    String lastname = extractStringParameter("lastname", requestMap, modifiedParameters);
	    String clientOrBusinessName = extractStringParameter("clientOrBusinessName", requestMap, modifiedParameters);
	    
	    return new ClientCommand(resourceIdentifier, externalId, firstname, lastname, clientOrBusinessName, officeId, joiningDate);
	}
	
	@Override
	public SubmitLoanApplicationCommand convertJsonToSubmitLoanApplicationCommand(final String json) {
		
		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}
		
		Type typeOfMap = new TypeToken<Map<String, Object>>(){}.getType();
	    Map<String, Object> requestMap = gsonConverter.fromJson(json, typeOfMap);
	    
	    Set<String> supportedParams = new HashSet<String>(
	    		Arrays.asList("clientId", "productId", "externalId", "fundId", 
	    				"principal", "inArrearsTolerance", "interestRatePerPeriod", "repaymentEvery", "numberOfRepayments", 
	    				"repaymentFrequencyType", "interestRateFrequencyType", "amortizationType", "interestType", "interestCalculationPeriodType",
	    				"expectedDisbursementDate", "repaymentsStartingFromDate", "interestChargedFromDate", "submittedOnDate", "submittedOnNote",
	    				"locale", "dateFormat")
	    );
	    
	    checkForUnsupportedParameters(requestMap, supportedParams);
	    
	    Set<String> modifiedParameters = new HashSet<String>();

	    Long clientId = extractLongParameter("clientId", requestMap, modifiedParameters);
	    Long productId = extractLongParameter("productId", requestMap, modifiedParameters);
	    Long fundId = extractLongParameter("fundId", requestMap, modifiedParameters);
	    String externalId = extractStringParameter("externalId", requestMap, modifiedParameters);
	    
	    BigDecimal principal = extractBigDecimalParameter("principal", requestMap, modifiedParameters);
	    BigDecimal inArrearsToleranceValue = extractBigDecimalParameter("inArrearsTolerance", requestMap, modifiedParameters);
	    BigDecimal interestRatePerPeriod = extractBigDecimalParameter("interestRatePerPeriod", requestMap, modifiedParameters);
	    
	    Integer repaymentEvery = extractIntegerParameter("repaymentEvery", requestMap, modifiedParameters);
	    Integer numberOfRepayments = extractIntegerParameter("numberOfRepayments", requestMap, modifiedParameters);
	    Integer repaymentFrequencyType = extractIntegerParameter("repaymentFrequencyType", requestMap, modifiedParameters);
	    
	    Integer interestRateFrequencyTypeValue = extractIntegerParameter("interestRateFrequencyType", requestMap, modifiedParameters);
	    Integer amortizationTypeValue = extractIntegerParameter("amortizationType", requestMap, modifiedParameters);
	    Integer interestTypeValue = extractIntegerParameter("interestType", requestMap, modifiedParameters);
	    Integer interestCalculationPeriodTypeValue = extractIntegerParameter("interestCalculationPeriodType", requestMap, modifiedParameters);
	    
	    LocalDate expectedDisbursementDate = extractLocalDateParameter("expectedDisbursementDate", requestMap, modifiedParameters);
	    LocalDate repaymentsStartingFromDate = extractLocalDateParameter("repaymentsStartingFromDate", requestMap, modifiedParameters);
	    LocalDate interestChargedFromDate = extractLocalDateParameter("interestChargedFromDate", requestMap, modifiedParameters);
	    LocalDate submittedOnDate = extractLocalDateParameter("submittedOnDate", requestMap, modifiedParameters);
	    
	    String submittedOnNote = extractStringParameter("submittedOnNote", requestMap, modifiedParameters);
	    
		return new SubmitLoanApplicationCommand(clientId, productId, externalId, fundId, submittedOnDate, submittedOnNote, 
	    		expectedDisbursementDate, repaymentsStartingFromDate, interestChargedFromDate, 
	    		principal, interestRatePerPeriod, interestRateFrequencyTypeValue, interestTypeValue, interestCalculationPeriodTypeValue, 
	    		repaymentEvery, repaymentFrequencyType, numberOfRepayments, amortizationTypeValue, inArrearsToleranceValue);
	}
	
	@Override
	public LoanStateTransitionCommand convertJsonToLoanStateTransitionCommand(final Long resourceIdentifier, final String json) {
		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}
		
		Type typeOfMap = new TypeToken<Map<String, Object>>(){}.getType();
	    Map<String, Object> requestMap = gsonConverter.fromJson(json, typeOfMap);
	    
	    Set<String> supportedParams = new HashSet<String>(
	    		Arrays.asList("eventDate", "note", "dateFormat")
	    );
	    
	    checkForUnsupportedParameters(requestMap, supportedParams);
	    
	    Set<String> modifiedParameters = new HashSet<String>();

	    LocalDate eventDate = extractLocalDateParameter("eventDate", requestMap, modifiedParameters);
	    String note = extractStringParameter("note", requestMap, modifiedParameters);
	    
	    return new LoanStateTransitionCommand(resourceIdentifier, eventDate, note);
	}
	
	@Override
	public LoanTransactionCommand convertJsonToLoanTransactionCommand(final Long resourceIdentifier, final String json) {
		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}
		
		Type typeOfMap = new TypeToken<Map<String, Object>>(){}.getType();
	    Map<String, Object> requestMap = gsonConverter.fromJson(json, typeOfMap);
	    
	    Set<String> supportedParams = new HashSet<String>(
	    		Arrays.asList("transactionDate", "transactionAmount", "note", "dateFormat", "locale")
	    );
	    
	    checkForUnsupportedParameters(requestMap, supportedParams);
	    
	    Set<String> modifiedParameters = new HashSet<String>();

	    LocalDate paymentDate = extractLocalDateParameter("paymentDate", requestMap, modifiedParameters);
	    BigDecimal paymentAmount = extractBigDecimalParameter("paymentAmount", requestMap, modifiedParameters);
	    String note = extractStringParameter("note", requestMap, modifiedParameters);
	    
	    return new LoanTransactionCommand(resourceIdentifier, paymentDate, paymentAmount, note);
	}
	
	@Override
	public AdjustLoanTransactionCommand convertJsonToAdjustLoanTransactionCommand(
			final Long loanId, final Long transactionId, final String json) {
		
		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}
		
		Type typeOfMap = new TypeToken<Map<String, Object>>(){}.getType();
	    Map<String, Object> requestMap = gsonConverter.fromJson(json, typeOfMap);
	    
	    Set<String> supportedParams = new HashSet<String>(
	    		Arrays.asList("transactionDate", "transactionAmount", "note", "dateFormat", "locale")
	    );
	    
	    checkForUnsupportedParameters(requestMap, supportedParams);
	    
	    Set<String> modifiedParameters = new HashSet<String>();

	    LocalDate transactionDate = extractLocalDateParameter("paymentDate", requestMap, modifiedParameters);
	    BigDecimal transactionAmount = extractBigDecimalParameter("paymentAmount", requestMap, modifiedParameters);
	    String note = extractStringParameter("note", requestMap, modifiedParameters);
	    
	    return new AdjustLoanTransactionCommand(loanId, transactionId, transactionDate, note, transactionAmount);
	}

	private void checkForUnsupportedParameters(Map<String, ?> requestMap, Set<String> supportedParams) {
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

	private String extractStringParameter(final String paramName, final Map<String, ?> requestMap, final Set<String> modifiedParameters) {
		String paramValue = null;
		if (requestMap.containsKey(paramName)) {
			paramValue = (String) requestMap.get(paramName);
			modifiedParameters.add(paramName);
		}
		
		if (paramValue != null) {
			paramValue = paramValue.trim();
		}
		
		return paramValue;
	}
	
	private Integer extractIntegerParameter(final String paramName, final Map<String, ?> requestMap, final Set<String> modifiedParameters) {
		Integer paramValue = null;
		if (requestMap.containsKey(paramName)) {
			String valueAsString = (String) requestMap.get(paramName);
			paramValue = convertToInteger(valueAsString, paramName, extractLocaleValue(requestMap));
			modifiedParameters.add(paramName);
		}
		return paramValue;
	}

	private BigDecimal extractBigDecimalParameter(final String paramName, final Map<String, ?> requestMap, final Set<String> modifiedParameters) {
		BigDecimal paramValue = null;
		if (requestMap.containsKey(paramName)) {
			String valueAsString = (String) requestMap.get(paramName);
			paramValue = convertFrom(valueAsString, paramName, extractLocaleValue(requestMap));
			modifiedParameters.add(paramName);
		}
		return paramValue;
	}
	
	private Long extractLongParameter(final String paramName, final Map<String, ?> requestMap, final Set<String> modifiedParameters) {
		Long paramValue = null;
		if (requestMap.containsKey(paramName)) {
			String valueAsString = (String) requestMap.get(paramName);
			if (StringUtils.isNotBlank(valueAsString)) {
				paramValue = Long.valueOf(valueAsString);
			}
			modifiedParameters.add(paramName);
		}
		return paramValue;
	}
	
	private LocalDate extractLocalDateParameter(final String paramName, final Map<String, ?> requestMap, final Set<String> modifiedParameters) {
		LocalDate paramValue = null;
		if (requestMap.containsKey(paramName)) {
			String valueAsString = (String) requestMap.get(paramName);
			if (StringUtils.isNotBlank(valueAsString)) {
				final String dateFormat = (String) requestMap.get("dateFormat");
				paramValue = convertFrom(valueAsString, paramName, dateFormat);
			}
			modifiedParameters.add(paramName);
		}
		return paramValue;
	}

	private Locale extractLocaleValue(Map<String, ?> requestMap) {
		Locale clientApplicationLocale = null;
	    String locale = null;
	    if (requestMap.containsKey("locale")) {
	    	locale = (String) requestMap.get("locale");
	    	clientApplicationLocale = localeFromString(locale);
	    }
		return clientApplicationLocale;
	}
}