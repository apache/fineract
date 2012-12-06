package org.mifosplatform.infrastructure.core.api;

import java.lang.reflect.Type;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;

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
    private final transient boolean approvedByChecker;
    private final JsonElement parsedCommand;
    private final FromJsonHelper fromApiJsonHelper;

    public static JsonCommand from(final String jsonCommand, final JsonElement parsedCommand, final FromJsonHelper fromApiJsonHelper) {
        return new JsonCommand(jsonCommand, parsedCommand, fromApiJsonHelper, false);
    }

    public static JsonCommand withMakerCheckerApproval(final String jsonCommand, final JsonElement parsedCommand,
            final FromJsonHelper fromApiJsonHelper) {
        return new JsonCommand(jsonCommand, parsedCommand, fromApiJsonHelper, true);
    }

    public JsonCommand(final String jsonCommand, final JsonElement parsedCommand, final FromJsonHelper fromApiJsonHelper,
            final boolean approvedByChecker) {
        this.jsonCommand = jsonCommand;
        this.parsedCommand = parsedCommand;
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.approvedByChecker = approvedByChecker;
    }

    public String json() {
        return this.jsonCommand;
    }

    public boolean isApprovedByChecker() {
        return this.approvedByChecker;
    }

    private boolean differenceExists(LocalDate baseValue, LocalDate workingCopyValue) {
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

    private boolean differenceExists(final Long baseValue, final Long workingCopyValue) {
        boolean differenceExists = false;

        if (baseValue != null) {
            differenceExists = !baseValue.equals(workingCopyValue);
        } else {
            differenceExists = workingCopyValue != null;
        }

        return differenceExists;
    }

    private boolean parameterExists(final String parameterName) {
        return this.fromApiJsonHelper.parameterExists(parameterName, parsedCommand);
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
}