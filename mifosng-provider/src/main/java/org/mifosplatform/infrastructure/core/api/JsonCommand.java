package org.mifosplatform.infrastructure.core.api;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.security.domain.BasicPasswordEncodablePlatformUser;
import org.mifosplatform.infrastructure.security.domain.PlatformUser;
import org.mifosplatform.infrastructure.security.service.PlatformPasswordEncoder;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

/**
 * Immutable representation of a command.
 * 
 * Wraps the provided JSON with convenience functions for extracting parameter
 * values and checking for changes against an existing value.
 */
public final class JsonCommand {

    private final String jsonCommand;
    private final JsonElement parsedCommand;
    private final FromJsonHelper fromApiJsonHelper;
    private final Long commandId;
    private final Long resourceId;
    private final Long subResourceId;
	private final String entityName;

    public static JsonCommand from(final String jsonCommand, final JsonElement parsedCommand, final FromJsonHelper fromApiJsonHelper,
            final String entityName, final Long resourceId, final Long subResourceId) {
        return new JsonCommand(null, jsonCommand, parsedCommand, fromApiJsonHelper, entityName, resourceId, subResourceId);
    }
    
    public static JsonCommand fromExistingCommand(final Long commandId, final String jsonCommand, final JsonElement parsedCommand, final FromJsonHelper fromApiJsonHelper,
            final Long resourceId, final Long subResourceId) {
        return new JsonCommand(commandId, jsonCommand, parsedCommand, fromApiJsonHelper, null, resourceId, subResourceId);
    }

    public JsonCommand(final Long commandId, final String jsonCommand, final JsonElement parsedCommand, final FromJsonHelper fromApiJsonHelper,
            final String entityName, final Long resourceId, final Long subResourceId) {
        this.commandId = commandId;
        this.jsonCommand = jsonCommand;
        this.parsedCommand = parsedCommand;
        this.fromApiJsonHelper = fromApiJsonHelper;
		this.entityName = entityName;
        this.resourceId = resourceId;
        this.subResourceId = subResourceId;
    }

    public String json() {
        return this.jsonCommand;
    }
    
    public JsonElement parsedJson() {
        return this.parsedCommand;
    }
    
    public Long commandId() {
        return this.commandId;
    }
    
    public String entityName() {
    	return this.entityName;
    }
    
    public Long resourceId() {
        return this.resourceId;
    }
    
    public Long subResourceId() {
        return this.subResourceId;
    }

    private boolean differenceExists(final LocalDate baseValue, final LocalDate workingCopyValue) {
        boolean differenceExists = false;

        if (baseValue != null) {
            differenceExists = !baseValue.equals(workingCopyValue);
        } else {
            differenceExists = workingCopyValue != null;
        }

        return differenceExists;
    }

    private boolean differenceExists(final String baseValue, final String workingCopyValue) {
        boolean differenceExists = false;

        if (StringUtils.isNotBlank(baseValue)) {
            differenceExists = !baseValue.equals(workingCopyValue);
        } else {
            differenceExists = StringUtils.isNotBlank(workingCopyValue);
        }

        return differenceExists;
    }

    private boolean differenceExists(final String[] baseValue, final String[] workingCopyValue) {
        Arrays.sort(baseValue);
        Arrays.sort(workingCopyValue);
        return !Arrays.equals(baseValue, workingCopyValue);
    }

    private boolean differenceExists(final Number baseValue, final Number workingCopyValue) {
        boolean differenceExists = false;

        if (baseValue != null) {
            differenceExists = !baseValue.equals(workingCopyValue);
        } else {
            differenceExists = workingCopyValue != null;
        }

        return differenceExists;
    }

    private boolean differenceExists(final BigDecimal baseValue, final BigDecimal workingCopyValue) {
        boolean differenceExists = false;

        if (baseValue != null) {
            differenceExists = baseValue.compareTo(workingCopyValue) != 0;
        } else {
            differenceExists = workingCopyValue != null;
        }

        return differenceExists;
    }

    private boolean differenceExists(final Boolean baseValue, final Boolean workingCopyValue) {
        boolean differenceExists = false;

        if (baseValue != null) {
            differenceExists = !baseValue.equals(workingCopyValue);
        } else {
            differenceExists = workingCopyValue != null;
        }

        return differenceExists;
    }

    public boolean parameterExists(final String parameterName) {
        return this.fromApiJsonHelper.parameterExists(parameterName, parsedCommand);
    }

    public boolean hasParameter(final String parameterName) {
        return parameterExists(parameterName);
    }

    public String dateFormat() {
        return stringValueOfParameterNamed("dateFormat");
    }

    public String locale() {
        return stringValueOfParameterNamed("locale");
    }

    public Map<String, Boolean> mapValueOfParameterNamed(final String parameterName) {
        final Type typeOfMap = new TypeToken<Map<String, Boolean>>() {}.getType();

        if (parsedCommand.getAsJsonObject().has(parameterName)) {
            parsedCommand.getAsJsonObject().get(parameterName);
        }

        return this.fromApiJsonHelper.extractMap(typeOfMap, jsonCommand);
    }

    public boolean isChangeInLongParameterNamed(final String parameterName, final Long existingValue) {
        boolean isChanged = false;
        if (parameterExists(parameterName)) {
            final Long workingValue = longValueOfParameterNamed(parameterName);
            isChanged = differenceExists(existingValue, workingValue);
        }
        return isChanged;
    }

    public Long longValueOfParameterNamed(final String parameterName) {
        return this.fromApiJsonHelper.extractLongNamed(parameterName, parsedCommand);
    }

    public boolean isChangeInLocalDateParameterNamed(final String parameterName, final LocalDate existingValue) {
        boolean isChanged = false;
        if (parameterExists(parameterName)) {
            final LocalDate workingValue = localDateValueOfParameterNamed(parameterName);
            isChanged = differenceExists(existingValue, workingValue);
        }
        return isChanged;
    }

    public LocalDate localDateValueOfParameterNamed(final String parameterName) {
        return this.fromApiJsonHelper.extractLocalDateNamed(parameterName, parsedCommand);
    }

    public boolean isChangeInStringParameterNamed(final String parameterName, final String existingValue) {
        boolean isChanged = false;
        if (parameterExists(parameterName)) {
            final String workingValue = stringValueOfParameterNamed(parameterName);
            isChanged = differenceExists(existingValue, workingValue);
        }
        return isChanged;
    }

    public String stringValueOfParameterNamed(final String parameterName) {
        final String value = this.fromApiJsonHelper.extractStringNamed(parameterName, parsedCommand);
        return StringUtils.defaultIfEmpty(value, "");
    }

    public boolean isChangeInBigDecimalParameterNamed(final String parameterName, final BigDecimal existingValue) {
        boolean isChanged = false;
        if (parameterExists(parameterName)) {
            final BigDecimal workingValue = bigDecimalValueOfParameterNamed(parameterName);
            isChanged = differenceExists(existingValue, workingValue);
        }
        return isChanged;
    }

    public BigDecimal bigDecimalValueOfParameterNamed(final String parameterName) {
        return this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(parameterName, parsedCommand);
    }

    public boolean isChangeInIntegerParameterNamed(final String parameterName, final Integer existingValue) {
        boolean isChanged = false;
        if (parameterExists(parameterName)) {
            final Integer workingValue = integerValueOfParameterNamed(parameterName);
            isChanged = differenceExists(existingValue, workingValue);
        }
        return isChanged;
    }

    public Integer integerValueOfParameterNamed(final String parameterName) {
        return this.fromApiJsonHelper.extractIntegerWithLocaleNamed(parameterName, parsedCommand);
    }

    public boolean isChangeInBooleanParameterNamed(final String parameterName, final Boolean existingValue) {
        boolean isChanged = false;
        if (parameterExists(parameterName)) {
            final Boolean workingValue = booleanObjectValueOfParameterNamed(parameterName);
            isChanged = differenceExists(existingValue, workingValue);
        }
        return isChanged;
    }

    /**
     * Returns {@link Boolean} that could possibly be null.
     */
    public Boolean booleanObjectValueOfParameterNamed(final String parameterName) {
        return this.fromApiJsonHelper.extractBooleanNamed(parameterName, parsedCommand);
    }

    /**
     * always returns true or false
     */
    public boolean booleanPrimitiveValueOfParameterNamed(final String parameterName) {
        final Boolean value = this.fromApiJsonHelper.extractBooleanNamed(parameterName, parsedCommand);
        return (Boolean) ObjectUtils.defaultIfNull(value, Boolean.FALSE);
    }

    public boolean isChangeInArrayParameterNamed(final String parameterName, final String[] existingValue) {
        boolean isChanged = false;
        if (parameterExists(parameterName)) {
            final String[] workingValue = arrayValueOfParameterNamed(parameterName);
            isChanged = differenceExists(existingValue, workingValue);
        }
        return isChanged;
    }

    public String[] arrayValueOfParameterNamed(final String parameterName) {
        return this.fromApiJsonHelper.extractArrayNamed(parameterName, parsedCommand);
    }

    public boolean isChangeInPasswordParameterNamed(final String parameterName, final String existingValue,
            final PlatformPasswordEncoder platformPasswordEncoder, final Long saltValue) {
        boolean isChanged = false;
        if (parameterExists(parameterName)) {
            final String workingValue = passwordValueOfParameterNamed(parameterName, platformPasswordEncoder, saltValue);
            isChanged = differenceExists(existingValue, workingValue);
        }
        return isChanged;
    }

    public String passwordValueOfParameterNamed(final String parameterName, final PlatformPasswordEncoder platformPasswordEncoder,
            final Long saltValue) {
        final String passwordPlainText = stringValueOfParameterNamed(parameterName);

        final PlatformUser dummyPlatformUser = new BasicPasswordEncodablePlatformUser(saltValue, "", passwordPlainText);
        return platformPasswordEncoder.encode(dummyPlatformUser);
    }
}