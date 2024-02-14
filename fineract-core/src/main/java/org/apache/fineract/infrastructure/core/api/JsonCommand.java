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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.security.domain.BasicPasswordEncodablePlatformUser;
import org.apache.fineract.infrastructure.security.domain.PlatformUser;
import org.apache.fineract.infrastructure.security.service.PlatformPasswordEncoder;

/**
 * Immutable representation of a command.
 *
 * Wraps the provided JSON with convenience functions for extracting parameter values and checking for changes against
 * an existing value.
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
    private final Long creditBureauId;
    private final Long organisationCreditBureauId;
    private final String jobName;

    public static JsonCommand from(final String jsonCommand, final JsonElement parsedCommand, final FromJsonHelper fromApiJsonHelper,
            final String entityName, final Long resourceId, final Long subresourceId, final Long groupId, final Long clientId,
            final Long loanId, final Long savingsId, final String transactionId, final String url, final Long productId,
            final Long creditBureauId, final Long organisationCreditBureauId, final String jobName) {
        return new JsonCommand(null, jsonCommand, parsedCommand, fromApiJsonHelper, entityName, resourceId, subresourceId, groupId,
                clientId, loanId, savingsId, transactionId, url, productId, creditBureauId, organisationCreditBureauId, jobName);

    }

    public static JsonCommand fromExistingCommand(final Long commandId, final String jsonCommand, final JsonElement parsedCommand,
            final FromJsonHelper fromApiJsonHelper, final String entityName, final Long resourceId, final Long subresourceId,
            final String url, final Long productId, final Long creditBureauId, final Long organisationCreditBureauId,
            final String jobName) {
        return new JsonCommand(commandId, jsonCommand, parsedCommand, fromApiJsonHelper, entityName, resourceId, subresourceId, null, null,
                null, null, null, url, productId, creditBureauId, organisationCreditBureauId, jobName);
    }

    public static JsonCommand fromExistingCommand(final Long commandId, final String jsonCommand, final JsonElement parsedCommand,
            final FromJsonHelper fromApiJsonHelper, final String entityName, final Long resourceId, final Long subresourceId,
            final Long groupId, final Long clientId, final Long loanId, final Long savingsId, final String transactionId, final String url,
            final Long productId, Long creditBureauId, final Long organisationCreditBureauId, final String jobName) {
        return new JsonCommand(commandId, jsonCommand, parsedCommand, fromApiJsonHelper, entityName, resourceId, subresourceId, groupId,
                clientId, loanId, savingsId, transactionId, url, productId, creditBureauId, organisationCreditBureauId, jobName);

    }

    public static JsonCommand fromExistingCommand(JsonCommand command, final JsonElement parsedCommand) {
        final String jsonCommand = command.fromApiJsonHelper.toJson(parsedCommand);
        return new JsonCommand(command.commandId, jsonCommand, parsedCommand, command.fromApiJsonHelper, command.entityName,
                command.resourceId, command.subresourceId, command.groupId, command.clientId, command.loanId, command.savingsId,
                command.transactionId, command.url, command.productId, command.creditBureauId, command.organisationCreditBureauId,
                command.jobName);
    }

    public static JsonCommand fromExistingCommand(JsonCommand command, final JsonElement parsedCommand, final Long clientId) {
        final String jsonCommand = command.fromApiJsonHelper.toJson(parsedCommand);
        return new JsonCommand(command.commandId, jsonCommand, parsedCommand, command.fromApiJsonHelper, command.entityName,
                command.resourceId, command.subresourceId, command.groupId, clientId, command.loanId, command.savingsId,
                command.transactionId, command.url, command.productId, command.creditBureauId, command.organisationCreditBureauId,
                command.jobName);
    }

    public JsonCommand(final Long commandId, final String jsonCommand, final JsonElement parsedCommand,
            final FromJsonHelper fromApiJsonHelper, final String entityName, final Long resourceId, final Long subresourceId,
            final Long groupId, final Long clientId, final Long loanId, final Long savingsId, final String transactionId, final String url,
            final Long productId, final Long creditBureauId, final Long organisationCreditBureauId, final String jobName) {

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
        this.creditBureauId = creditBureauId;
        this.organisationCreditBureauId = organisationCreditBureauId;
        this.jobName = jobName;
    }

    public static JsonCommand fromJsonElement(final Long resourceId, final JsonElement parsedCommand) {
        return new JsonCommand(resourceId, parsedCommand);
    }

    public static JsonCommand fromJsonElement(final Long resourceId, final JsonElement parsedCommand,
            final FromJsonHelper fromApiJsonHelper) {
        return new JsonCommand(resourceId, parsedCommand, fromApiJsonHelper);
    }

    public JsonCommand(final Long resourceId, final JsonElement parsedCommand) {
        this.parsedCommand = parsedCommand;
        this.resourceId = resourceId;
        this.commandId = null;
        this.jsonCommand = null;
        this.fromApiJsonHelper = null;
        this.entityName = null;
        this.subresourceId = null;
        this.groupId = null;
        this.clientId = null;
        this.loanId = null;
        this.savingsId = null;
        this.transactionId = null;
        this.url = null;
        this.productId = null;
        this.creditBureauId = null;
        this.organisationCreditBureauId = null;
        this.jobName = null;
    }

    public JsonCommand(final Long resourceId, final JsonElement parsedCommand, final FromJsonHelper fromApiJsonHelper) {
        this.parsedCommand = parsedCommand;
        this.resourceId = resourceId;
        this.commandId = null;
        this.jsonCommand = null;
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.entityName = null;
        this.subresourceId = null;
        this.groupId = null;
        this.clientId = null;
        this.loanId = null;
        this.savingsId = null;
        this.transactionId = null;
        this.url = null;
        this.productId = null;
        this.creditBureauId = null;
        this.organisationCreditBureauId = null;
        this.jobName = null;
    }

    public static JsonCommand from(final String jsonCommand) {
        return new JsonCommand(null, jsonCommand, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

    }

    public Long getOrganisationCreditBureauId() {
        return this.organisationCreditBureauId;
    }

    public Long getCreditBureauId() {
        return this.creditBureauId;
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

    public String getJobName() {
        return this.jobName;
    }

    private boolean differenceExists(final TemporalAccessor baseValue, final TemporalAccessor workingCopyValue) {
        return !Objects.equals(baseValue, workingCopyValue);
    }

    private boolean differenceExists(final String baseValue, final String workingCopyValue) {
        return !Objects.equals(baseValue, workingCopyValue);
    }

    private boolean differenceExists(final String[] baseValue, final String[] workingCopyValue) {
        Arrays.sort(baseValue);
        Arrays.sort(workingCopyValue);
        return !Arrays.equals(baseValue, workingCopyValue);
    }

    private boolean differenceExists(final Number baseValue, final Number workingCopyValue) {
        return !Objects.equals(baseValue, workingCopyValue);
    }

    private boolean differenceExists(final BigDecimal baseValue, final BigDecimal workingCopyValue) {
        return !MathUtil.isEqualTo(baseValue, workingCopyValue);
    }

    private boolean differenceExists(final Boolean baseValue, final Boolean workingCopyValue) {
        return !Objects.equals(baseValue, workingCopyValue);
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

    public boolean isChangeInDateParameterNamed(final String parameterName, final LocalDate existingValue) {
        return isChangeInLocalDateParameterNamed(parameterName, existingValue);
    }

    public boolean isChangeInTimeParameterNamed(final String parameterName, final LocalTime existingValue, final String timeFormat) {
        LocalTime time = null;
        if (existingValue != null) {
            DateTimeFormatter timeFormtter = DateTimeFormatter.ofPattern(timeFormat);
            time = LocalTime.parse(existingValue.toString(), timeFormtter);
        }
        return isChangeInLocalTimeParameterNamed(parameterName, time);
    }

    public boolean isChangeInLocalTimeParameterNamed(final String parameterName, final LocalTime existingValue) {
        boolean isChanged = false;
        if (parameterExists(parameterName)) {
            final LocalTime workingValue = localTimeValueOfParameterNamed(parameterName);
            isChanged = differenceExists(existingValue, workingValue);
        }
        return isChanged;
    }

    public boolean isChangeInLocalDateTimeParameterNamed(final String parameterName, final LocalDateTime existingValue) {
        boolean isChanged = false;
        if (parameterExists(parameterName)) {
            final LocalTime workingValue = localTimeValueOfParameterNamed(parameterName);
            isChanged = differenceExists(existingValue, workingValue);
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

    public LocalTime localTimeValueOfParameterNamed(final String parameterName) {
        return this.fromApiJsonHelper.extractLocalTimeNamed(parameterName, this.parsedCommand);
    }

    public MonthDay extractMonthDayNamed(final String parameterName) {
        return this.fromApiJsonHelper.extractMonthDayNamed(parameterName, this.parsedCommand);
    }

    public LocalDate dateValueOfParameterNamed(final String parameterName) {
        return this.fromApiJsonHelper.extractLocalDateNamed(parameterName, this.parsedCommand);
    }

    public boolean isChangeInStringParameterNamed(final String parameterName, final String existingValue) {
        boolean isChanged = false;
        if (parameterExists(parameterName)) {
            final String workingValue = stringValueOfParameterNamed(parameterName);
            isChanged = differenceExists(existingValue, workingValue);
        }
        return isChanged;
    }

    public <T extends Enum<T>> T enumValueOfParameterNamed(String parameterName, Class<T> enumType) {
        try {
            String value = stringValueOfParameterNamedAllowingNull(parameterName);
            if (value != null) {
                return Enum.valueOf(enumType, value);
            } else {
                return null;
            }
        } catch (IllegalArgumentException e) {
            return null;
        }
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

    public Map<String, Object> mapObjectValueOfParameterNamed(final String json) {
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        final Map<String, Object> value = this.fromApiJsonHelper.extractObjectMap(typeOfMap, json);
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
        return ObjectUtils.defaultIfNull(value, Boolean.FALSE);
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

    public boolean isChangeInExternalIdParameterNamed(final String parameterName, final ExternalId externalId) {
        boolean isChanged = false;
        if (parameterExists(parameterName)) {
            final String workingValue = stringValueOfParameterNamed(parameterName);
            String existingValue = externalId.getValue();
            isChanged = differenceExists(existingValue, workingValue);
        }
        return isChanged;
    }

    public String passwordValueOfParameterNamed(final String parameterName, final PlatformPasswordEncoder platformPasswordEncoder,
            final Long saltValue) {
        final String passwordPlainText = stringValueOfParameterNamed(parameterName);

        final PlatformUser dummyPlatformUser = new BasicPasswordEncodablePlatformUser().setId(saltValue).setUsername("")
                .setPassword(passwordPlainText);
        return platformPasswordEncoder.encode(dummyPlatformUser);
    }

    public Locale extractLocale() {
        return this.fromApiJsonHelper.extractLocaleParameter(this.parsedCommand.getAsJsonObject());
    }

    public void checkForUnsupportedParameters(final Type typeOfMap, final String json, final Set<String> requestDataParameters) {
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, requestDataParameters);
    }

}
