package org.mifosng.platform.api.infrastructure;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.mifosng.platform.api.commands.AdjustLoanTransactionCommand;
import org.mifosng.platform.api.commands.BranchMoneyTransferCommand;
import org.mifosng.platform.api.commands.ChargeCommand;
import org.mifosng.platform.api.commands.ClientCommand;
import org.mifosng.platform.api.commands.DepositAccountCommand;
import org.mifosng.platform.api.commands.DepositProductCommand;
import org.mifosng.platform.api.commands.FundCommand;
import org.mifosng.platform.api.commands.GroupCommand;
import org.mifosng.platform.api.commands.LoanProductCommand;
import org.mifosng.platform.api.commands.LoanStateTransitionCommand;
import org.mifosng.platform.api.commands.LoanTransactionCommand;
import org.mifosng.platform.api.commands.NoteCommand;
import org.mifosng.platform.api.commands.OfficeCommand;
import org.mifosng.platform.api.commands.OrganisationCurrencyCommand;
import org.mifosng.platform.api.commands.RoleCommand;
import org.mifosng.platform.api.commands.SavingProductCommand;
import org.mifosng.platform.api.commands.SubmitLoanApplicationCommand;
import org.mifosng.platform.api.commands.UserCommand;
import org.mifosng.platform.api.data.ApiParameterError;
import org.mifosng.platform.api.data.ChargeData;
import org.mifosng.platform.api.data.ClientData;
import org.mifosng.platform.api.data.ClientLoanAccountSummaryCollectionData;
import org.mifosng.platform.api.data.DepositAccountData;
import org.mifosng.platform.api.data.GroupData;
import org.mifosng.platform.api.data.LoanAccountData;
import org.mifosng.platform.api.data.LoanTransactionData;
import org.mifosng.platform.api.data.NewLoanData;
import org.mifosng.platform.api.data.NoteData;
import org.mifosng.platform.api.errorhandling.InvalidJsonException;
import org.mifosng.platform.api.errorhandling.UnsupportedParameterException;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.format.number.NumberFormatter;
import org.springframework.stereotype.Service;

import com.google.gson.ExclusionStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

	@Override
	public String convertLoanTransactionDataToJson(final boolean prettyPrint, final Set<String> responseParameters, final LoanTransactionData... transactions) {
		final Set<String> supportedParameters = new HashSet<String>(
				Arrays.asList("id", "transactionType", "date", "principal", "interest", "total", "totalWaived", "overpaid")
		);
		
		final Set<String> parameterNamesToSkip = new HashSet<String>();
		
		if (!responseParameters.isEmpty()) {
			if (!supportedParameters.containsAll(responseParameters)) {
				throw new UnsupportedParameterException(new ArrayList<String>(responseParameters));
			}
			
			parameterNamesToSkip.addAll(supportedParameters);
			parameterNamesToSkip.removeAll(responseParameters);
		}
		
		ExclusionStrategy strategy = new ParameterListExclusionStrategy(parameterNamesToSkip);
		
		GsonBuilder builder = new GsonBuilder().addSerializationExclusionStrategy(strategy);
		builder.registerTypeAdapter(LocalDate.class, new JodaLocalDateAdapter());
		builder.registerTypeAdapter(DateTime.class, new JodaDateTimeAdapter());
		if (prettyPrint) {
			builder.setPrettyPrinting();
		}
		Gson gsonDeserializer = builder.create();
		
		String json = "";
		if (transactions != null && transactions.length == 1) {
			json = gsonDeserializer.toJson(transactions[0]);
		} else {
			json = gsonDeserializer.toJson(transactions);
		}
		
		return json;
	}
	
	@Override
	public String convertLoanAccountDataToJson(final boolean prettyPrint, final Set<String> responseParameters, final LoanAccountData loanAccount) {
		final Set<String> supportedParameters = new HashSet<String>(
				Arrays.asList("id", "externalId", "fundId", "fundName", "loanProductId", "loanProductName", "principal", "inArrearsTolerance", "numberOfRepayments",
						"repaymentEvery", "interestRatePerPeriod", "annualInterestRate", 
						"repaymentFrequencyType", "interestRateFrequencyType", "amortizationType", "interestType", "interestCalculationPeriodType",
						"submittedOnDate", "approvedOnDate", "expectedDisbursementDate", "actualDisbursementDate", 
						"expectedFirstRepaymentOnDate", "interestChargedFromDate", "closedOnDate", "expectedMaturityDate", 
						"lifeCycleStatusId", "lifeCycleStatusText", "lifeCycleStatusDate", 
						"summary", "repaymentSchedule", "loanRepayments", "permissions", "convenienceData")
		);
		
		final Set<String> parameterNamesToSkip = new HashSet<String>();
		
		if (!responseParameters.isEmpty()) {
			if (!supportedParameters.containsAll(responseParameters)) {
				throw new UnsupportedParameterException(new ArrayList<String>(responseParameters));
			}
			
			parameterNamesToSkip.addAll(supportedParameters);
			parameterNamesToSkip.removeAll(responseParameters);
		}
		
		ExclusionStrategy strategy = new ParameterListExclusionStrategy(parameterNamesToSkip);
		
		GsonBuilder builder = new GsonBuilder().addSerializationExclusionStrategy(strategy);
		builder.registerTypeAdapter(LocalDate.class, new JodaLocalDateAdapter());
		builder.registerTypeAdapter(DateTime.class, new JodaDateTimeAdapter());
		if (prettyPrint) {
			builder.setPrettyPrinting();
		}
		Gson gsonDeserializer = builder.create();
		
		return gsonDeserializer.toJson(loanAccount);
	}
	
	@Override
	public String convertNewLoanDataToJson(final boolean prettyPrint, final Set<String> responseParameters, final NewLoanData newLoanData) {
		
		final Set<String> supportedParameters = new HashSet<String>(
				Arrays.asList("clientId", "clientName", "productId", "productName",
						"selectedProduct", "expectedDisbursementDate", "allowedProducts")
		);
		
		final Set<String> parameterNamesToSkip = new HashSet<String>();
		
		if (!responseParameters.isEmpty()) {
			if (!supportedParameters.containsAll(responseParameters)) {
				throw new UnsupportedParameterException(new ArrayList<String>(responseParameters));
			}
			
			parameterNamesToSkip.addAll(supportedParameters);
			parameterNamesToSkip.removeAll(responseParameters);
		}
		
		ExclusionStrategy strategy = new ParameterListExclusionStrategy(parameterNamesToSkip);
		
		GsonBuilder builder = new GsonBuilder().addSerializationExclusionStrategy(strategy);
		builder.registerTypeAdapter(LocalDate.class, new JodaLocalDateAdapter());
		builder.registerTypeAdapter(DateTime.class, new JodaDateTimeAdapter());
		if (prettyPrint) {
			builder.setPrettyPrinting();
		}
		Gson gsonDeserializer = builder.create();
		
		return gsonDeserializer.toJson(newLoanData);
	}
	
	@Override
	public String convertNoteDataToJson(boolean prettyPrint, Set<String> responseParameters, Collection<NoteData> notes) {
		final boolean returnAsArray = true;
		return doConvertNoteDataToJson(prettyPrint, returnAsArray, responseParameters, notes.toArray(new NoteData[notes.size()]));
	}
	
	@Override
	public String convertNoteDataToJson(final boolean prettyPrint, final Set<String> responseParameters, final NoteData note) {
		final boolean returnAsArray = false;
		return doConvertNoteDataToJson(prettyPrint, returnAsArray, responseParameters, note);
	}
	
	private String doConvertNoteDataToJson(final boolean prettyPrint, final boolean returnAsArray, final Set<String> responseParameters, final NoteData... notes) {
		final Set<String> supportedParameters = new HashSet<String>(
				Arrays.asList("id", "clientId", "loanId", "loanTransactionId", "noteType", "note", "createdById", "createdByUsername", 
						"createdOn", "updatedById", "updatedByUsername", "updatedOn")
		);
		
		final Set<String> parameterNamesToSkip = new HashSet<String>();
		
		if (!responseParameters.isEmpty()) {
			if (!supportedParameters.containsAll(responseParameters)) {
				throw new UnsupportedParameterException(new ArrayList<String>(responseParameters));
			}
			
			parameterNamesToSkip.addAll(supportedParameters);
			parameterNamesToSkip.removeAll(responseParameters);
		}
		
		ExclusionStrategy strategy = new ParameterListExclusionStrategy(parameterNamesToSkip);
		
		GsonBuilder builder = new GsonBuilder().addSerializationExclusionStrategy(strategy);
		builder.registerTypeAdapter(LocalDate.class, new JodaLocalDateAdapter());
		builder.registerTypeAdapter(DateTime.class, new JodaDateTimeAdapter());
		if (prettyPrint) {
			builder.setPrettyPrinting();
		}
		Gson gsonDeserializer = builder.create();
		
		String json = "";
		if (notes != null && notes.length == 1 && !returnAsArray) {
			json = gsonDeserializer.toJson(notes[0]);
		} else {
			json = gsonDeserializer.toJson(notes);
		}
		
		return json;
	}
	
	@Override
	public String convertClientLoanAccountSummaryCollectionDataToJson(final boolean prettyPrint, final Set<String> responseParameters,
			final ClientLoanAccountSummaryCollectionData clientAccountsData) {
		final Set<String> supportedParameters = new HashSet<String>(
				Arrays.asList("pendingApprovalLoans", "awaitingDisbursalLoans", "openLoans", "closedLoans", 
						"anyLoanCount", "pendingApprovalLoanCount", "awaitingDisbursalLoanCount", "activeLoanCount", "closedLoanCount")
		);
		
		final Set<String> parameterNamesToSkip = new HashSet<String>();
		
		if (!responseParameters.isEmpty()) {
			if (!supportedParameters.containsAll(responseParameters)) {
				throw new UnsupportedParameterException(new ArrayList<String>(responseParameters));
			}
			
			parameterNamesToSkip.addAll(supportedParameters);
			parameterNamesToSkip.removeAll(responseParameters);
		}
		
		ExclusionStrategy strategy = new ParameterListExclusionStrategy(parameterNamesToSkip);
		
		GsonBuilder builder = new GsonBuilder().addSerializationExclusionStrategy(strategy);
		builder.registerTypeAdapter(LocalDate.class, new JodaLocalDateAdapter());
		if (prettyPrint) {
			builder.setPrettyPrinting();
		}
		Gson gsonDeserializer = builder.create();
		
		return gsonDeserializer.toJson(clientAccountsData);
	}
	
	@Override
	public String convertClientDataToJson(final boolean prettyPrint, final Set<String> responseParameters, final ClientData... clients) {
		final Set<String> supportedParameters = new HashSet<String>(
				Arrays.asList("id", "officeId", "officeName", "externalId", "firstname", "lastname", "joinedDate", "displayName", "allowedOffices")
		);
		
		final Set<String> parameterNamesToSkip = new HashSet<String>();
		
		if (!responseParameters.isEmpty()) {
			if (!supportedParameters.containsAll(responseParameters)) {
				throw new UnsupportedParameterException(new ArrayList<String>(responseParameters));
			}
			
			parameterNamesToSkip.addAll(supportedParameters);
			parameterNamesToSkip.removeAll(responseParameters);
		}
		
		ExclusionStrategy strategy = new ParameterListExclusionStrategy(parameterNamesToSkip);
		
		GsonBuilder builder = new GsonBuilder().addSerializationExclusionStrategy(strategy);
		builder.registerTypeAdapter(LocalDate.class, new JodaLocalDateAdapter());
		if (prettyPrint) {
			builder.setPrettyPrinting();
		}
		Gson gsonDeserializer = builder.create();
		
		String json = "";
		if (clients != null && clients.length == 1) {
			json = gsonDeserializer.toJson(clients[0]);
		} else {
			json = gsonDeserializer.toJson(clients);
		}
		
		return json;
	}
	
	@Override
    public String convertGroupDataToJson(boolean prettyPrint, Set<String> responseParameters, GroupData... groups) {
        final Set<String> supportedParameters = new HashSet<String>(
                Arrays.asList("id", "name", "externalId", "clientMembers", "allowedClients")
        );
        
        final Set<String> parameterNamesToSkip = new HashSet<String>();
        
        if (!responseParameters.isEmpty()) {
            if (!supportedParameters.containsAll(responseParameters)) {
                throw new UnsupportedParameterException(new ArrayList<String>(responseParameters));
            }
            
            parameterNamesToSkip.addAll(supportedParameters);
            parameterNamesToSkip.removeAll(responseParameters);
        }
        
        ExclusionStrategy strategy = new ParameterListExclusionStrategy(parameterNamesToSkip);
        GsonBuilder builder = new GsonBuilder().addSerializationExclusionStrategy(strategy);
        if (prettyPrint) {
            builder.setPrettyPrinting();
        }
        Gson gsonDeserializer = builder.create();
        String json = "";
        
        if (groups != null && groups.length == 1) {
            json = gsonDeserializer.toJson(groups[0]);
        } else {
            json = gsonDeserializer.toJson(groups);
        }
        
        return json;
    }

    @Override
    public ChargeCommand convertJsonToChargeCommand(Long resourceIdentifier, String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        Type typeOfMap = new TypeToken<Map<String, String>>(){}.getType();
        Map<String, String> requestMap = gsonConverter.fromJson(json, typeOfMap);


        Set<String> supportedParams = new HashSet<String>(
                Arrays.asList("name", "amount", "locale")
        );

        checkForUnsupportedParameters(requestMap, supportedParams);

        Set<String> modifiedParameters = new HashSet<String>();

        String name = extractStringParameter("name", requestMap, modifiedParameters);
        BigDecimal amount = extractBigDecimalParameter("amount", requestMap, modifiedParameters);

        return new ChargeCommand(modifiedParameters, resourceIdentifier, name, amount);
    }

    @Override
    public String convertChargeDataToJson(boolean prettyPrint, Set<String> responseParameters, ChargeData... charges) {
        Set<String> supportedParameters = new HashSet<String>(Arrays.asList("id", "name", "amount"));

        final Set<String> parameterNamesToSkip = new HashSet<String>();

        if (!responseParameters.isEmpty()) {
            if (!supportedParameters.containsAll(responseParameters)) {
                throw new UnsupportedParameterException(new ArrayList<String>(responseParameters));
            }

            parameterNamesToSkip.addAll(supportedParameters);
            parameterNamesToSkip.removeAll(responseParameters);
        }

        ExclusionStrategy strategy = new ParameterListExclusionStrategy(parameterNamesToSkip);

        GsonBuilder builder = new GsonBuilder().addSerializationExclusionStrategy(strategy);
        if (prettyPrint) {
            builder.setPrettyPrinting();
        }
        Gson gsonDeserializer = builder.create();

        String json = "";
        if (charges != null && charges.length == 1) {
            json = gsonDeserializer.toJson(charges[0]);
        } else {
            json = gsonDeserializer.toJson(charges);
        }

        return json;
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
		
		Type typeOfMap = new TypeToken<Map<String, String>>(){}.getType();
	    Map<String, String> requestMap = gsonConverter.fromJson(json, typeOfMap);
	    
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
		
		Type typeOfMap = new TypeToken<Map<String, String>>(){}.getType();
	    Map<String, String> requestMap = gsonConverter.fromJson(json, typeOfMap);
	    
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
	    
	    return new ClientCommand(modifiedParameters, resourceIdentifier, externalId, firstname, lastname, clientOrBusinessName, officeId, joiningDate);
	}
	
	@Override
    public GroupCommand convertJsonToGroupCommand(final Long resourceIdentifier, final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }
        
        Type typeOfMap = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, String> requestMap = gsonConverter.fromJson(json, typeOfMap);

        Set<String> supportedParams = new HashSet<String>(
                Arrays.asList("name", "externalId", "clientMembers")
        );
        
        checkForUnsupportedParameters(requestMap, supportedParams);

        Set<String> modifiedParameters = new HashSet<String>();
        
        String externalId = extractStringParameter("externalId", requestMap, modifiedParameters);
        String name = extractStringParameter("name", requestMap, modifiedParameters);

        // check array
        JsonParser parser = new JsonParser();
        
        String[] clientMembers = null;
        JsonElement element = parser.parse(json);
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("clientMembers")) {
                modifiedParameters.add("clientMembers");
                JsonArray array = object.get("clientMembers").getAsJsonArray();
                clientMembers = new String[array.size()];
                for (int i=0; i<array.size(); i++) {
                    clientMembers[i] = array.get(i).getAsString();
                }
            }
        }
        //
        
        return new GroupCommand(modifiedParameters, resourceIdentifier, externalId, name, clientMembers);
    }

    @Override
	public LoanProductCommand convertJsonToLoanProductCommand(final Long resourceIdentifier, final String json) {
		
		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}
		
		Type typeOfMap = new TypeToken<Map<String, String>>(){}.getType();
	    Map<String, String> requestMap = gsonConverter.fromJson(json, typeOfMap);
	    
	    Set<String> supportedParams = new HashSet<String>(
	    		Arrays.asList("name", "description", "fundId", "transactionProcessingStrategyId", "currencyCode", "digitsAfterDecimal", 
	    				"principal", "inArrearsTolerance", "interestRatePerPeriod", "repaymentEvery", "numberOfRepayments", 
	    				"repaymentFrequencyType", "interestRateFrequencyType", "amortizationType", "interestType", "interestCalculationPeriodType", "locale")
	    );
	    
	    checkForUnsupportedParameters(requestMap, supportedParams);
	    
	    Set<String> modifiedParameters = new HashSet<String>();

	    String name = extractStringParameter("name", requestMap, modifiedParameters);
	    String description = extractStringParameter("description", requestMap, modifiedParameters);
	    Long fundId = extractLongParameter("fundId", requestMap, modifiedParameters);
	    Long transactionProcessingStrategyId = extractLongParameter("transactionProcessingStrategyId", requestMap, modifiedParameters);
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
	    
		return new LoanProductCommand(modifiedParameters, resourceIdentifier, name, description, fundId, transactionProcessingStrategyId, 
				currencyCode, digitsAfterDecimalValue, principalValue, inArrearsToleranceValue, numberOfRepaymentsValue, repaymentEveryValue, interestRatePerPeriodValue,
				repaymentFrequencyTypeValue, interestRateFrequencyTypeValue, amortizationTypeValue, interestTypeValue, interestCalculationPeriodTypeValue);
	}
	
	@Override
	public SubmitLoanApplicationCommand convertJsonToSubmitLoanApplicationCommand(final String json) {
		
		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}
		
		Type typeOfMap = new TypeToken<Map<String, String>>(){}.getType();
	    Map<String, String> requestMap = gsonConverter.fromJson(json, typeOfMap);
	    
	    Set<String> supportedParams = new HashSet<String>(
	    		Arrays.asList("clientId", "productId", "externalId", "fundId", "transactionProcessingStrategyId",
	    				"principal", "inArrearsTolerance", "interestRatePerPeriod", "repaymentEvery", "numberOfRepayments", 
	    				"loanTermFrequency", "loanTermFrequencyType",
	    				"repaymentFrequencyType", "interestRateFrequencyType", "amortizationType", "interestType", "interestCalculationPeriodType",
	    				"expectedDisbursementDate", "repaymentsStartingFromDate", "interestChargedFromDate", "submittedOnDate", "submittedOnNote",
	    				"locale", "dateFormat")
	    );
	    
	    checkForUnsupportedParameters(requestMap, supportedParams);
	    
	    Set<String> modifiedParameters = new HashSet<String>();

	    Long clientId = extractLongParameter("clientId", requestMap, modifiedParameters);
	    Long productId = extractLongParameter("productId", requestMap, modifiedParameters);
	    Long fundId = extractLongParameter("fundId", requestMap, modifiedParameters);
	    Long transactionProcessingStrategyId = extractLongParameter("transactionProcessingStrategyId", requestMap, modifiedParameters);
	    String externalId = extractStringParameter("externalId", requestMap, modifiedParameters);
	    
	    BigDecimal principal = extractBigDecimalParameter("principal", requestMap, modifiedParameters);
	    BigDecimal inArrearsToleranceValue = extractBigDecimalParameter("inArrearsTolerance", requestMap, modifiedParameters);
	    BigDecimal interestRatePerPeriod = extractBigDecimalParameter("interestRatePerPeriod", requestMap, modifiedParameters);
	    
	    Integer repaymentEvery = extractIntegerParameter("repaymentEvery", requestMap, modifiedParameters);
	    Integer numberOfRepayments = extractIntegerParameter("numberOfRepayments", requestMap, modifiedParameters);
	    Integer repaymentFrequencyType = extractIntegerParameter("repaymentFrequencyType", requestMap, modifiedParameters);
	    
	    Integer loanTermFrequency = extractIntegerParameter("loanTermFrequency", requestMap, modifiedParameters);
	    Integer loanTermFrequencyType = extractIntegerParameter("loanTermFrequencyType", requestMap, modifiedParameters);
	    
	    Integer interestRateFrequencyTypeValue = extractIntegerParameter("interestRateFrequencyType", requestMap, modifiedParameters);
	    Integer amortizationTypeValue = extractIntegerParameter("amortizationType", requestMap, modifiedParameters);
	    Integer interestTypeValue = extractIntegerParameter("interestType", requestMap, modifiedParameters);
	    Integer interestCalculationPeriodTypeValue = extractIntegerParameter("interestCalculationPeriodType", requestMap, modifiedParameters);
	    
	    LocalDate expectedDisbursementDate = extractLocalDateParameter("expectedDisbursementDate", requestMap, modifiedParameters);
	    LocalDate repaymentsStartingFromDate = extractLocalDateParameter("repaymentsStartingFromDate", requestMap, modifiedParameters);
	    LocalDate interestChargedFromDate = extractLocalDateParameter("interestChargedFromDate", requestMap, modifiedParameters);
	    LocalDate submittedOnDate = extractLocalDateParameter("submittedOnDate", requestMap, modifiedParameters);
	    
	    String submittedOnNote = extractStringParameter("submittedOnNote", requestMap, modifiedParameters);
	    
		return new SubmitLoanApplicationCommand(clientId, productId, externalId, fundId, transactionProcessingStrategyId,
				submittedOnDate, submittedOnNote, 
	    		expectedDisbursementDate, repaymentsStartingFromDate, interestChargedFromDate, 
	    		principal, interestRatePerPeriod, interestRateFrequencyTypeValue, interestTypeValue, interestCalculationPeriodTypeValue, 
	    		repaymentEvery, repaymentFrequencyType, numberOfRepayments, amortizationTypeValue, 
	    		loanTermFrequency, loanTermFrequencyType,
	    		inArrearsToleranceValue);
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

	    LocalDate transactionDate = extractLocalDateParameter("transactionDate", requestMap, modifiedParameters);
	    BigDecimal transactionAmount = extractBigDecimalParameter("transactionAmount", requestMap, modifiedParameters);
	    String note = extractStringParameter("note", requestMap, modifiedParameters);
	    
	    return new LoanTransactionCommand(resourceIdentifier, transactionDate, transactionAmount, note);
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

	    LocalDate transactionDate = extractLocalDateParameter("transactionDate", requestMap, modifiedParameters);
	    BigDecimal transactionAmount = extractBigDecimalParameter("transactionAmount", requestMap, modifiedParameters);
	    String note = extractStringParameter("note", requestMap, modifiedParameters);
	    
	    return new AdjustLoanTransactionCommand(loanId, transactionId, transactionDate, note, transactionAmount);
	}
	
	@Override
	public NoteCommand convertJsonToNoteCommand(final Long noteId, final Long clientId, final String json) {
		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}
		
		Type typeOfMap = new TypeToken<Map<String, Object>>(){}.getType();
	    Map<String, Object> requestMap = gsonConverter.fromJson(json, typeOfMap);
	    
	    Set<String> supportedParams = new HashSet<String>(
	    		Arrays.asList("note")
	    );
	    
	    checkForUnsupportedParameters(requestMap, supportedParams);
	    
	    Set<String> modifiedParameters = new HashSet<String>();

	    String note = extractStringParameter("note", requestMap, modifiedParameters);
	    
	    return new NoteCommand(noteId, clientId, note);
	}
	
	@Override
	public OrganisationCurrencyCommand convertJsonToOrganisationCurrencyCommand(final String json) {
		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}
		
		Type typeOfMap = new TypeToken<Map<String, Object>>(){}.getType();
	    Map<String, Object> requestMap = gsonConverter.fromJson(json, typeOfMap);
	    
	    Set<String> supportedParams = new HashSet<String>(
	    		Arrays.asList("currencies")
	    );
	    
	    checkForUnsupportedParameters(requestMap, supportedParams);
	    
	    Set<String> modifiedParameters = new HashSet<String>();

	    // check array
	    JsonParser parser = new JsonParser();
		
		String[] currencies = null;
		JsonElement element = parser.parse(json);
		if (element.isJsonObject()) {
			JsonObject object = element.getAsJsonObject();
			if (object.has("currencies")) {
				modifiedParameters.add("currencies");
				JsonArray array = object.get("currencies").getAsJsonArray();
				currencies = new String[array.size()];
				for (int i=0; i<array.size(); i++) {
					currencies[i] = array.get(i).getAsString();
				}
			}
		}
	    //
	    
	    return new OrganisationCurrencyCommand(currencies);
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
	
	private boolean extractBooleanParameter(final String paramName, final Map<String, ?> requestMap, final Set<String> modifiedParameters) {
		boolean paramValue = false;
		String paramValueAsString = null;
		if (requestMap.containsKey(paramName)) {
			paramValueAsString = (String) requestMap.get(paramName);

			if (paramValueAsString != null) {
				paramValueAsString = paramValueAsString.trim();
			}

			paramValue = Boolean.valueOf(paramValueAsString);
			modifiedParameters.add(paramName);
		}
		return paramValue;
	}
	
	private Long extractLongParameter(final String paramName, final Map<String, ?> requestMap, final Set<String> modifiedParameters) {
		Long paramValue = null;
		if (requestMap.containsKey(paramName)) {
			String valueAsString = (String) requestMap.get(paramName);
			if (StringUtils.isNotBlank(valueAsString)) {
				paramValue = Long.valueOf(Double.valueOf(valueAsString).longValue());
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
	
	private Integer convertToInteger(final String numericalValueFormatted, final String parameterName, final Locale clientApplicationLocale) {
		
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
	public SavingProductCommand convertJsonToSavingProductCommand(final Long resourceIdentifier, final String json) {

		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}
		Type typeOfMap = new TypeToken<Map<String, String>>() {}.getType();
		Map<String, String> requestMap = gsonConverter.fromJson(json, typeOfMap);

		Set<String> supportedParams = new HashSet<String>(Arrays.asList("name","description","currencyCode", "digitsAfterDecimal","interestRate","locale","minimumBalance","maximumBalance"));
		checkForUnsupportedParameters(requestMap, supportedParams);
		Set<String> modifiedParameters = new HashSet<String>();
		String name = extractStringParameter("name", requestMap,modifiedParameters);
		String description = extractStringParameter("description", requestMap,modifiedParameters);
		String currencyCode=extractStringParameter("currencyCode", requestMap,modifiedParameters);
		Integer digitsAfterDecimalValue = extractIntegerParameter("digitsAfterDecimal", requestMap, modifiedParameters);
		BigDecimal interestRate = extractBigDecimalParameter("interestRate", requestMap, modifiedParameters);
		BigDecimal minimumBalance=extractBigDecimalParameter("minimumBalance", requestMap, modifiedParameters);
		BigDecimal maximumBalance=extractBigDecimalParameter("maximumBalance", requestMap, modifiedParameters);
		

		return new SavingProductCommand(modifiedParameters, resourceIdentifier,name, description,currencyCode,digitsAfterDecimalValue,interestRate,minimumBalance,maximumBalance);
	}
	
	@Override
	public DepositAccountCommand convertJsonToDepositAccountCommand(final Long resourceIdentifier, final String json) {
		
		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}
		
		Type typeOfMap = new TypeToken<Map<String, String>>() {}.getType();
		Map<String, String> requestMap = gsonConverter.fromJson(json, typeOfMap);

		// preClosureInterestRate
		Set<String> supportedParams = new HashSet<String>(
				Arrays.asList("clientId", "productId", "externalId", "depositAmount", "maturityInterestRate", 
						"tenureInMonths", "interestCompoundedEvery", "interestCompoundedEveryPeriodType", "commencementDate",
						"renewalAllowed", "preClosureAllowed",
						"locale", "dateFormat")
		);
		checkForUnsupportedParameters(requestMap, supportedParams);
		Set<String> modifiedParameters = new HashSet<String>();
		
		Long clientId = extractLongParameter("clientId", requestMap, modifiedParameters);
		Long productId = extractLongParameter("productId", requestMap, modifiedParameters);
		String externalId = extractStringParameter("externalId", requestMap,modifiedParameters);
		BigDecimal depositAmount=extractBigDecimalParameter("depositAmount", requestMap, modifiedParameters);
		BigDecimal interestRate = extractBigDecimalParameter("maturityInterestRate", requestMap, modifiedParameters);
		Integer tenureInMonths = extractIntegerParameter("tenureInMonths", requestMap, modifiedParameters);
		
		Integer interestCompoundedEvery = extractIntegerParameter("interestCompoundedEvery", requestMap, modifiedParameters);
		Integer interestCompoundedEveryPeriodType = extractIntegerParameter("interestCompoundedEveryPeriodType", requestMap, modifiedParameters);
		LocalDate commencementDate = extractLocalDateParameter("commencementDate", requestMap, modifiedParameters);

		boolean renewalAllowed = extractBooleanParameter("renewalAllowed", requestMap, modifiedParameters);
		boolean preClosureAllowed = extractBooleanParameter("preClosureAllowed", requestMap, modifiedParameters);
		
		return new DepositAccountCommand(modifiedParameters, resourceIdentifier, clientId, productId, 
				externalId, depositAmount, interestRate, tenureInMonths, 
				interestCompoundedEvery, interestCompoundedEveryPeriodType, commencementDate, renewalAllowed, preClosureAllowed);
	}

	@Override
	public DepositProductCommand convertJsonToDepositProductCommand(final Long resourceIdentifier, final String json) {
		
		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}
		
		Type typeOfMap = new TypeToken<Map<String, String>>() {}.getType();
		Map<String, String> requestMap = gsonConverter.fromJson(json, typeOfMap);

		Set<String> supportedParams = new HashSet<String>(
				Arrays.asList("locale", "name", "externalId", "description","currencyCode", "digitsAfterDecimal","minimumBalance","maximumBalance","tenureInMonths",
						"maturityDefaultInterestRate","maturityMinInterestRate","maturityMaxInterestRate", 
						"interestCompoundedEvery", "interestCompoundedEveryPeriodType",
						"renewalAllowed","preClosureAllowed","preClosureInterestRate")
				);
		checkForUnsupportedParameters(requestMap, supportedParams);
		Set<String> modifiedParameters = new HashSet<String>();
		String name = extractStringParameter("name", requestMap,modifiedParameters);
		String externalId = extractStringParameter("externalId", requestMap,modifiedParameters);
		
		String description = extractStringParameter("description", requestMap,modifiedParameters);
		String currencyCode=extractStringParameter("currencyCode", requestMap,modifiedParameters);
		Integer digitsAfterDecimalValue = extractIntegerParameter("digitsAfterDecimal", requestMap, modifiedParameters);
		BigDecimal minimumBalance=extractBigDecimalParameter("minimumBalance", requestMap, modifiedParameters);
		BigDecimal maximumBalance=extractBigDecimalParameter("maximumBalance", requestMap, modifiedParameters);
		
		Integer tenureMonths = extractIntegerParameter("tenureInMonths", requestMap, modifiedParameters);
		BigDecimal maturityDefaultInterestRate = extractBigDecimalParameter("maturityDefaultInterestRate", requestMap, modifiedParameters);
		BigDecimal maturityMinInterestRate = extractBigDecimalParameter("maturityMinInterestRate", requestMap, modifiedParameters);
		BigDecimal maturityMaxInterestRate = extractBigDecimalParameter("maturityMaxInterestRate", requestMap, modifiedParameters);
		
		Integer interestCompoundedEvery = extractIntegerParameter("interestCompoundedEvery", requestMap, modifiedParameters);
		Integer interestCompoundedEveryPeriodType = extractIntegerParameter("interestCompoundedEveryPeriodType", requestMap, modifiedParameters);
		
	    boolean canRenew = extractBooleanParameter("renewalAllowed", requestMap, modifiedParameters);
	    boolean canPreClose = extractBooleanParameter("preClosureAllowed", requestMap, modifiedParameters);
	    BigDecimal preClosureInterestRate = extractBigDecimalParameter("preClosureInterestRate", requestMap, modifiedParameters);
		
		return new DepositProductCommand(modifiedParameters, resourceIdentifier, externalId, 
				name, description, currencyCode, digitsAfterDecimalValue, minimumBalance,maximumBalance, 
				tenureMonths, maturityDefaultInterestRate, maturityMinInterestRate, maturityMaxInterestRate, 
				interestCompoundedEvery, interestCompoundedEveryPeriodType,
				canRenew, canPreClose, preClosureInterestRate);
	}

	@Override
	public String convertDepositAccountDataToJson(
			final boolean prettyPrint,
			final Set<String> responseParameters, 
			final DepositAccountData... accounts) {
		
		Set<String> supportedParameters = new HashSet<String>(
				Arrays.asList("productOptions", "interestCompoundedEveryPeriodTypeOptions",
						"createdOn", "lastModifedOn", 
						"id", "externalId", "clientId", "clientName", "productId", "productName", 
						"currency", "deposit", "maturityInterestRate", "tenureInMonths", 
						"interestCompoundedEvery", "interestCompoundedEveryPeriodType",
						"renewalAllowed","preClosureAllowed","preClosureInterestRate"
						)
		);

		final Set<String> parameterNamesToSkip = new HashSet<String>();
		
		if (!responseParameters.isEmpty()) {
			
			// strip out all know support params from expected response to see if unsupported parameters requested for response.
			Set<String> differentParametersSet = new HashSet<String>(responseParameters);
			differentParametersSet.removeAll(supportedParameters);
			
			if (!differentParametersSet.isEmpty()) {
				throw new UnsupportedParameterException(new ArrayList<String>(differentParametersSet));
			}
			
			parameterNamesToSkip.addAll(supportedParameters);
			parameterNamesToSkip.removeAll(responseParameters);
		}
		
		ExclusionStrategy strategy = new ParameterListExclusionStrategy(parameterNamesToSkip);
		
		GsonBuilder builder = new GsonBuilder().addSerializationExclusionStrategy(strategy);
		builder.registerTypeAdapter(LocalDate.class, new JodaLocalDateAdapter());
		builder.registerTypeAdapter(DateTime.class, new JodaDateTimeAdapter());
		if (prettyPrint) {
			builder.setPrettyPrinting();
		}
		Gson gsonDeserializer = builder.create();
		
		String json = "";
		if (accounts != null && accounts.length == 1) {
			json = gsonDeserializer.toJson(accounts[0]);
		} else {
			json = gsonDeserializer.toJson(accounts);
		}
		
		return json;
	}
}