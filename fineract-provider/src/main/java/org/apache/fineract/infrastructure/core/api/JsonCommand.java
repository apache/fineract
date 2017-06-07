/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.core.api;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.security.domain.BasicPasswordEncodablePlatformUser;
import org.apache.fineract.infrastructure.security.domain.PlatformUser;
import org.apache.fineract.infrastructure.security.service.PlatformPasswordEncoder;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.MonthDay;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.gson.JsonArray;
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
    private final Long subresourceId;
    private final Long groupId;
    private final Long clientId;
    private final Long loanId;
    private final Long savingsId;
    private final String entityName;
    private final String transactionId;
    private final String url;
    private final Long productId;

    public static JsonCommand from(final String jsonCommand, final JsonElement parsedCommand, final FromJsonHelper fromApiJsonHelper,
            final String entityName, final Long resourceId, final Long subresourceId, final Long groupId, final Long clientId,
            final Long loanId, final Long savingsId, final String transactionId, final String url, final Long productId) {
        return new JsonCommand(null, jsonCommand, parsedCommand, fromApiJsonHelper, entityName, resourceId, subresourceId, groupId,
                clientId, loanId, savingsId, transactionId, url, productId);

    }

    public static JsonCommand fromExistingCommand(final Long commandId, final String jsonCommand, final JsonElement parsedCommand,
            final FromJsonHelper fromApiJsonHelper, final String entityName, final Long resourceId, final Long subresourceId,
            final String url, final Long productId) {
        return new JsonCommand(commandId, jsonCommand, parsedCommand, fromApiJsonHelper, entityName, resourceId, subresourceId, null, null,
                null, null, null, url, productId);
    }

    public static JsonCommand fromExistingCommand(final Long commandId, final String jsonCommand, final JsonElement parsedCommand,
            final FromJsonHelper fromApiJsonHelper, final String entityName, final Long resourceId, final Long subresourceId,
            final Long groupId, final Long clientId, final Long loanId, final Long savingsId, final String transactionId, final String url,
            final Long productId) {
        return new JsonCommand(commandId, jsonCommand, parsedCommand, fromApiJsonHelper, entityName, resourceId, subresourceId, groupId,
                clientId, loanId, savingsId, transactionId, url, productId);

    }

    public static JsonCommand fromExistingCommand(JsonCommand command, final JsonElement parsedCommand) {
        final String jsonCommand = command.fromApiJsonHelper.toJson(parsedCommand);
        return new JsonCommand(command.commandId, jsonCommand, parsedCommand, command.fromApiJsonHelper, command.entityName,
                command.resourceId, command.subresourceId, command.groupId, command.clientId, command.loanId, command.savingsId,
                command.transactionId, command.url, command.productId);
    }

    public JsonCommand(final Long commandId, final String jsonCommand, final JsonElement parsedCommand,
            final FromJsonHelper fromApiJsonHelper, final String entityName, final Long resourceId, final Long subresourceId,
            final Long groupId, final Long clientId, final Long loanId, final Long savingsId, final String transactionId, final String url,
            final Long productId) {

        this.commandId = commandId;
        this.jsonCommand = jsonCommand;
        this.parsedCommand = parsedCommand;
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.entityName = entityName;
        this.resourceId = resourceId;
        this.subresourceId = subresourceId;
        this.groupId = groupId;
        this.clientId = clientId;
        this.loanId = loanId;
        this.savingsId = savingsId;
        this.transactionId = transactionId;
        this.url = url;
        this.productId = productId;
    }

    public String json() {
        return this.jsonCommand;
    }

    public JsonElement parsedJson() {
        return this.parsedCommand;
    }

    public JsonElement jsonElement(final String paramName) {
        if (this.parsedCommand.getAsJsonObject().has(paramName)) {
            return this.parsedCommand.getAsJsonObject().get(paramName);
        }
        return null;
    }
    
    public String jsonFragment(final String paramName) {
        String jsonFragment = null;
        if (this.parsedCommand.getAsJsonObject().has(paramName)) {
            final JsonElement fragment = this.parsedCommand.getAsJsonObject().get(paramName);
            jsonFragment = this.fromApiJsonHelper.toJson(fragment);
        }
        return jsonFragment;
    }

    public Long commandId() {
        return this.commandId;
    }

    public String entityName() {
        return this.entityName;
    }

    public Long entityId() {
        return this.resourceId;
    }

    public Long subentityId() {
        return this.subresourceId;
    }

    public Long getGroupId() {
        return this.groupId;
    }

    public Long getClientId() {
        return this.clientId;
    }

    public Long getLoanId() {
        return this.loanId;
    }

    public Long getSavingsId() {
        return this.savingsId;
    }

    public String getTransactionId() {
        return this.transactionId;
    }

    public String getUrl() {
        return this.url;
    }

    public Long getProductId() {
        return this.productId;
    }

    private boolean differenceExistsTime(final LocalDateTime baseValue, final LocalDateTime workingCopyValue) {
        boolean differenceExists = false;

        if (baseValue != null) {
            differenceExists = !baseValue.equals(workingCopyValue);
        } else {
            differenceExists = workingCopyValue != null;
        }

        return differenceExists;
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
            if (workingCopyValue != null) {
                differenceExists = !baseValue.equals(workingCopyValue);
            } else {
                differenceExists = true;
            }
        } else {
            differenceExists = workingCopyValue != null;
        }

        return differenceExists;
    }

    private boolean differenceExists(final BigDecimal baseValue, final BigDecimal workingCopyValue) {
        boolean differenceExists = false;

        if (baseValue != null) {
            if (workingCopyValue != null) {
                differenceExists = baseValue.compareTo(workingCopyValue) != 0;
            } else {
                differenceExists = true;
            }
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
        return this.fromApiJsonHelper.parameterExists(parameterName, this.parsedCommand);
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

    public boolean isChangeInLongParameterNamed(final String parameterName, final Long existingValue) {
        boolean isChanged = false;
        if (parameterExists(parameterName)) {
            final Long workingValue = longValueOfParameterNamed(parameterName);
            isChanged = differenceExists(existingValue, workingValue);
        }
        return isChanged;
    }

    public Long longValueOfParameterNamed(final String parameterName) {
        return this.fromApiJsonHelper.extractLongNamed(parameterName, this.parsedCommand);
    }

    public boolean isChangeInDateParameterNamed(final String parameterName, final Date existingValue) {
        LocalDate localDate = null;
        if (existingValue != null) {
            localDate = LocalDate.fromDateFields(existingValue);
        }
        return isChangeInLocalDateParameterNamed(parameterName, localDate);
    }
    
    public boolean isChangeInTimeParameterNamed(final String parameterName, final Date existingValue,final String timeFormat) {
        LocalDateTime time = null;
        if (existingValue != null) {
            DateTimeFormatter timeFormtter = DateTimeFormat.forPattern(timeFormat);
            time = LocalDateTime.parse(existingValue.toString(), timeFormtter);
        }
        return isChangeInLocalTimeParameterNamed(parameterName, time);
    }

    public boolean isChangeInLocalTimeParameterNamed(final String parameterName, final LocalDateTime existingValue) {
        boolean isChanged = false;
        if (parameterExists(parameterName)) {
            final LocalDateTime workingValue = localTimeValueOfParameterNamed(parameterName);
            isChanged = differenceExistsTime(existingValue, workingValue);
        }
        return isChanged;
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
        return this.fromApiJsonHelper.extractLocalDateNamed(parameterName, this.parsedCommand);
    }
    public LocalDateTime localTimeValueOfParameterNamed(final String parameterName) {
        return this.fromApiJsonHelper.extractLocalTimeNamed(parameterName, this.parsedCommand);
    }

    public MonthDay extractMonthDayNamed(final String parameterName) {
        return this.fromApiJsonHelper.extractMonthDayNamed(parameterName, this.parsedCommand);
    }

    public Date DateValueOfParameterNamed(final String parameterName) {
        final LocalDate localDate = this.fromApiJsonHelper.extractLocalDateNamed(parameterName, this.parsedCommand);
        if (localDate == null) { return null; }
        return localDate.toDateTimeAtStartOfDay().toDate();
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
        final String value = this.fromApiJsonHelper.extractStringNamed(parameterName, this.parsedCommand);
        return StringUtils.defaultIfEmpty(value, "");
    }

    public String stringValueOfParameterNamedAllowingNull(final String parameterName) {
        return this.fromApiJsonHelper.extractStringNamed(parameterName, this.parsedCommand);
    }

    public Map<String, String> mapValueOfParameterNamed(final String json) {
        final Type typeOfMap = new TypeToken<Map<String, String>>() {}.getType();
        final Map<String, String> value = this.fromApiJsonHelper.extractDataMap(typeOfMap, json);
        return value;
    }

    public boolean isChangeInBigDecimalParameterNamedDefaultingZeroToNull(final String parameterName, final BigDecimal existingValue) {
        boolean isChanged = false;
        if (parameterExists(parameterName)) {
            final BigDecimal workingValue = bigDecimalValueOfParameterNamedDefaultToNullIfZero(parameterName);
            isChanged = differenceExists(existingValue, workingValue);
        }
        return isChanged;
    }

    public boolean isChangeInBigDecimalParameterNamed(final String parameterName, final BigDecimal existingValue) {
        boolean isChanged = false;
        if (parameterExists(parameterName)) {
            final BigDecimal workingValue = bigDecimalValueOfParameterNamed(parameterName);
            isChanged = differenceExists(existingValue, workingValue);
        }
        return isChanged;
    }

    public boolean isChangeInBigDecimalParameterNamed(final String parameterName, final BigDecimal existingValue, final Locale locale) {
        boolean isChanged = false;
        if (parameterExists(parameterName)) {
            final BigDecimal workingValue = bigDecimalValueOfParameterNamed(parameterName, locale);
            isChanged = differenceExists(existingValue, workingValue);
        }
        return isChanged;
    }

    public boolean isChangeInBigDecimalParameterNamedWithNullCheck(final String parameterName, final BigDecimal existingValue) {
        boolean isChanged = false;
        if (parameterExists(parameterName)) {
            final BigDecimal workingValue = bigDecimalValueOfParameterNamed(parameterName);
            if (workingValue == null && existingValue != null) {
                isChanged = true;
            } else {
                isChanged = differenceExists(existingValue, workingValue);
            }
        }
        return isChanged;
    }

    private static BigDecimal defaultToNullIfZero(final BigDecimal value) {
        BigDecimal result = value;
        if (value != null && BigDecimal.ZERO.compareTo(value) == 0) {
            result = null;
        }
        return result;
    }

    private static Integer defaultToNullIfZero(final Integer value) {
        Integer result = value;
        if (value != null && value == 0) {
            result = null;
        }
        return result;
    }

    public BigDecimal bigDecimalValueOfParameterNamed(final String parameterName) {
        return this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(parameterName, this.parsedCommand);
    }

    public BigDecimal bigDecimalValueOfParameterNamed(final String parameterName, final Locale locale) {
        return this.fromApiJsonHelper.extractBigDecimalNamed(parameterName, this.parsedCommand, locale);
    }

    public BigDecimal bigDecimalValueOfParameterNamedDefaultToNullIfZero(final String parameterName) {
        return defaultToNullIfZero(bigDecimalValueOfParameterNamed(parameterName));
    }

    public boolean isChangeInIntegerParameterNamedDefaultingZeroToNull(final String parameterName, final Integer existingValue) {
        boolean isChanged = false;
        if (parameterExists(parameterName)) {
            final Integer workingValue = integerValueOfParameterNamedDefaultToNullIfZero(parameterName);
            isChanged = differenceExists(existingValue, workingValue);
        }
        return isChanged;
    }

    public boolean isChangeInIntegerParameterNamed(final String parameterName, final Integer existingValue) {
        boolean isChanged = false;
        if (parameterExists(parameterName)) {
            final Integer workingValue = integerValueOfParameterNamed(parameterName);
            isChanged = differenceExists(existingValue, workingValue);
        }
        return isChanged;
    }

    public boolean isChangeInIntegerParameterNamed(final String parameterName, final Integer existingValue, final Locale locale) {
        boolean isChanged = false;
        if (parameterExists(parameterName)) {
            final Integer workingValue = integerValueOfParameterNamed(parameterName, locale);
            isChanged = differenceExists(existingValue, workingValue);
        }
        return isChanged;
    }

    public boolean isChangeInIntegerParameterNamedWithNullCheck(final String parameterName, final Integer existingValue) {
        boolean isChanged = false;
        if (parameterExists(parameterName)) {
            final Integer workingValue = integerValueOfParameterNamed(parameterName);
            if (workingValue == null && existingValue != null) {
                isChanged = true;
            } else {
                isChanged = differenceExists(existingValue, workingValue);
            }
        }
        return isChanged;
    }

    public Integer integerValueOfParameterNamed(final String parameterName) {
        return this.fromApiJsonHelper.extractIntegerWithLocaleNamed(parameterName, this.parsedCommand);
    }

    public Integer integerValueOfParameterNamed(final String parameterName, final Locale locale) {
        return this.fromApiJsonHelper.extractIntegerNamed(parameterName, this.parsedCommand, locale);
    }

    public Integer integerValueOfParameterNamedDefaultToNullIfZero(final String parameterName) {
        return defaultToNullIfZero(integerValueOfParameterNamed(parameterName));
    }

    public boolean isChangeInIntegerSansLocaleParameterNamed(final String parameterName, final Integer existingValue) {
        boolean isChanged = false;
        if (parameterExists(parameterName)) {
            final Integer workingValue = integerValueSansLocaleOfParameterNamed(parameterName);
            isChanged = differenceExists(existingValue, workingValue);
        }
        return isChanged;
    }

    public Integer integerValueSansLocaleOfParameterNamed(final String parameterName) {
        return this.fromApiJsonHelper.extractIntegerSansLocaleNamed(parameterName, this.parsedCommand);
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
        return this.fromApiJsonHelper.extractBooleanNamed(parameterName, this.parsedCommand);
    }

    /**
     * always returns true or false
     */
    public boolean booleanPrimitiveValueOfParameterNamed(final String parameterName) {
        final Boolean value = this.fromApiJsonHelper.extractBooleanNamed(parameterName, this.parsedCommand);
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
        return this.fromApiJsonHelper.extractArrayNamed(parameterName, this.parsedCommand);
    }

    public JsonArray arrayOfParameterNamed(final String parameterName) {
        return this.fromApiJsonHelper.extractJsonArrayNamed(parameterName, this.parsedCommand);
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

    public Locale extractLocale() {
        return this.fromApiJsonHelper.extractLocaleParameter(this.parsedCommand.getAsJsonObject());
    }

    public void checkForUnsupportedParameters(final Type typeOfMap, final String json, final Set<String> requestDataParameters) {
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, requestDataParameters);
    }

}