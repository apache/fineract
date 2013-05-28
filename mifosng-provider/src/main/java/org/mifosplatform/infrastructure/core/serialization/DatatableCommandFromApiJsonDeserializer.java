package org.mifosplatform.infrastructure.core.serialization;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public class DatatableCommandFromApiJsonDeserializer {

	private final static String DATATABLE_NAME_REGEX_PATTERN = "^[a-zA-Z][a-zA-Z0-9\\-_\\s]{0,48}[a-zA-Z0-9]$";
	/**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParametersForCreate = new HashSet<String>(Arrays.asList(
    		"datatableName", "apptableName", "multiRow", "columns"));
    private final Set<String> supportedParametersForCreateColumns = new HashSet<String>(Arrays.asList(
    		"name", "type", "length", "mandatory"));
    private final Set<String> supportedParametersForUpdate = new HashSet<String>(Arrays.asList(
    		"apptableName", "changeColumns", "addColumns", "dropColumns"));
    private final Set<String> supportedParametersForAddColumns = new HashSet<String>(Arrays.asList(
    		"name", "type", "length", "mandatory", "after"));
    private final Set<String> supportedParametersForChangeColumns = new HashSet<String>(Arrays.asList(
    		"name", "newName", "type", "length", "mandatory", "after"));
    private final Set<String> supportedParametersForDropColumns = new HashSet<String>(Arrays.asList(
    		"name"));
    private final Object[] supportedColumnTypes = { "String", "Number", "Decimal", "Date" };

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public DatatableCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParametersForCreate);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("datatable");

        final JsonElement element = fromApiJsonHelper.parse(json);
        final String apptableName = fromApiJsonHelper.extractStringNamed("apptableName", element);
        final String fkColumnName = apptableName.substring(2) + "_id";
        baseDataValidator.reset().parameter("apptableName").value(apptableName).notNull().notBlank().notExceedingLengthOf(50);

        final String datatableName = fromApiJsonHelper.extractStringNamed("datatableName", element);
        baseDataValidator.reset().parameter("datatableName").value(datatableName).notNull().notBlank().notExceedingLengthOf(50)
        	.matchesRegularExpression(DATATABLE_NAME_REGEX_PATTERN);

        final Boolean multiRow = fromApiJsonHelper.extractBooleanNamed("multiRow", element);
        baseDataValidator.reset().parameter("multiRow").value(multiRow).ignoreIfNull().trueOrFalseRequired(multiRow);

        final JsonArray columns = fromApiJsonHelper.extractJsonArrayNamed("columns", element);
        baseDataValidator.reset().parameter("columns").value(columns).notNull();

        for (JsonElement column : columns) {
        	fromApiJsonHelper.checkForUnsupportedParameters(column.getAsJsonObject(), supportedParametersForCreateColumns);

        	final String name = fromApiJsonHelper.extractStringNamed("name", column);
        	baseDataValidator.reset().parameter("name").value(name).notNull().notBlank().notExceedingLengthOf(50)
        		.isNotOneOfTheseValues("id", fkColumnName).matchesRegularExpression(DATATABLE_NAME_REGEX_PATTERN);

        	final String type = fromApiJsonHelper.extractStringNamed("type", column);
        	baseDataValidator.reset().parameter("type").value(type).notNull().notBlank().isOneOfTheseValues(supportedColumnTypes);

        	final Integer length = fromApiJsonHelper.extractIntegerSansLocaleNamed("length", column);
        	if (type.equals("String")) {
	        	baseDataValidator.reset().parameter("length").value(length).notNull().notBlank()
	        		.zeroOrPositiveAmount();
        	} else {
        		baseDataValidator.reset().parameter("length").value(length).ignoreIfNull().notBlank()
        			.zeroOrPositiveAmount();
        	}

        	final Boolean mandatory = fromApiJsonHelper.extractBooleanNamed("mandatory", column);
        	baseDataValidator.reset().parameter("mandatory").value(mandatory).ignoreIfNull().trueOrFalseRequired(mandatory);
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForUpdate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParametersForUpdate);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("datatable");

        final JsonElement element = fromApiJsonHelper.parse(json);
        final String apptableName = fromApiJsonHelper.extractStringNamed("apptableName", element);
        final String fkColumnName = apptableName.substring(2) + "_id";
        baseDataValidator.reset().parameter("apptableName").value(apptableName).notBlank().notExceedingLengthOf(50);

        if (fromApiJsonHelper.parameterExists("changeColumns", element)) { 
	        final JsonArray changeColumns = fromApiJsonHelper.extractJsonArrayNamed("changeColumns", element);
	        baseDataValidator.reset().parameter("changeColumns").value(changeColumns).ignoreIfNull();

	        for (JsonElement column : changeColumns) {
	        	fromApiJsonHelper.checkForUnsupportedParameters(column.getAsJsonObject(), supportedParametersForChangeColumns);

	        	final String name = fromApiJsonHelper.extractStringNamed("name", column);
	        	baseDataValidator.reset().parameter("name").value(name).notNull().notBlank().notExceedingLengthOf(50)
	        		.isNotOneOfTheseValues("id", fkColumnName).matchesRegularExpression(DATATABLE_NAME_REGEX_PATTERN);

	        	final String newName = fromApiJsonHelper.extractStringNamed("newName", column);
	        	baseDataValidator.reset().parameter("newName").value(newName).ignoreIfNull().notBlank()
	        		.notExceedingLengthOf(50).isNotOneOfTheseValues("id", fkColumnName)
	        		.matchesRegularExpression(DATATABLE_NAME_REGEX_PATTERN);

	        	final String type = fromApiJsonHelper.extractStringNamed("type", column);
	        	baseDataValidator.reset().parameter("type").value(type).notNull().notBlank()
	        		.isOneOfTheseValues(supportedColumnTypes);

	        	final Integer length = fromApiJsonHelper.extractIntegerSansLocaleNamed("length", column);
	        	if (type.equals("String")) {
		        	baseDataValidator.reset().parameter("length").value(length).notNull().notBlank()
		        		.zeroOrPositiveAmount();
	        	} else {
	        		baseDataValidator.reset().parameter("length").value(length).ignoreIfNull().notBlank()
	        			.zeroOrPositiveAmount();
	        	}

	        	final Boolean mandatory = fromApiJsonHelper.extractBooleanNamed("mandatory", column);
	        	baseDataValidator.reset().parameter("mandatory").value(mandatory).ignoreIfNull().trueOrFalseRequired(mandatory);

	        	final Boolean after = fromApiJsonHelper.extractBooleanNamed("after", column);
	        	baseDataValidator.reset().parameter("after").value(after).ignoreIfNull().notBlank();
	        }
        }
        if (fromApiJsonHelper.parameterExists("addColumns", element)) { 
	        final JsonArray addColumns = fromApiJsonHelper.extractJsonArrayNamed("addColumns", element);
	        baseDataValidator.reset().parameter("addColumns").value(addColumns).ignoreIfNull();
	        
	        for (JsonElement column : addColumns) {
	        	fromApiJsonHelper.checkForUnsupportedParameters(column.getAsJsonObject(), supportedParametersForAddColumns);

	        	final String name = fromApiJsonHelper.extractStringNamed("name", column);
	        	baseDataValidator.reset().parameter("name").value(name).notNull().notBlank().notExceedingLengthOf(50)
	        		.isNotOneOfTheseValues("id", fkColumnName).matchesRegularExpression(DATATABLE_NAME_REGEX_PATTERN);

	        	final String type = fromApiJsonHelper.extractStringNamed("type", column);
	        	baseDataValidator.reset().parameter("type").value(type).notNull().notBlank()
	        		.isOneOfTheseValues(supportedColumnTypes);

	        	final Integer length = fromApiJsonHelper.extractIntegerSansLocaleNamed("length", column);
	        	if (type.equals("String")) {
		        	baseDataValidator.reset().parameter("length").value(length).notNull().notBlank()
		        		.zeroOrPositiveAmount();
	        	} else {
	        		baseDataValidator.reset().parameter("length").value(length).ignoreIfNull().notBlank()
	        			.zeroOrPositiveAmount();
	        	}

	        	final Boolean mandatory = fromApiJsonHelper.extractBooleanNamed("mandatory", column);
	        	baseDataValidator.reset().parameter("mandatory").value(mandatory).ignoreIfNull().trueOrFalseRequired(mandatory);

	        	final Boolean after = fromApiJsonHelper.extractBooleanNamed("after", column);
	        	baseDataValidator.reset().parameter("after").value(after).ignoreIfNull().notBlank();
	        }
        }
        if (fromApiJsonHelper.parameterExists("dropColumns", element)) { 
        	final JsonArray dropColumns = fromApiJsonHelper.extractJsonArrayNamed("dropColumns", element);
	        baseDataValidator.reset().parameter("dropColumns").value(dropColumns).ignoreIfNull();

	        for (JsonElement column : dropColumns) {
	        	fromApiJsonHelper.checkForUnsupportedParameters(column.getAsJsonObject(), supportedParametersForDropColumns);

	        	final String name = fromApiJsonHelper.extractStringNamed("name", column);
	        	baseDataValidator.reset().parameter("name").value(name).notNull().notBlank().notExceedingLengthOf(50)
	        		.isNotOneOfTheseValues("id", fkColumnName).matchesRegularExpression(DATATABLE_NAME_REGEX_PATTERN);
	        }
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}
