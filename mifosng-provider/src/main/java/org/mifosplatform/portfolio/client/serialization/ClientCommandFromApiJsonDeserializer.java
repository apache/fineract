package org.mifosplatform.portfolio.client.serialization;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.AbstractFromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.client.command.ClientCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

/**
 * Implementation of {@link FromApiJsonDeserializer} for {@link ClientCommand} 
 * 's.
 */
@Component
public final class ClientCommandFromApiJsonDeserializer extends AbstractFromApiJsonDeserializer<ClientCommand> {

    /**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("id", "accountNo", "externalId", "firstname",
            "middlename", "lastname", "fullname", "officeId", "joinedDate", "locale", "dateFormat"));

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public ClientCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    @Override
    public ClientCommand commandFromApiJson(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final JsonElement element = fromApiJsonHelper.parse(json);
        final String accountNo = fromApiJsonHelper.extractStringNamed("accountNo", element);
        final String externalId = fromApiJsonHelper.extractStringNamed("externalId", element);
        final String firstname = fromApiJsonHelper.extractStringNamed("firstname", element);
        final String middlename = fromApiJsonHelper.extractStringNamed("middlename", element);
        final String lastname = fromApiJsonHelper.extractStringNamed("lastname", element);
        final String fullname = fromApiJsonHelper.extractStringNamed("fullname", element);
        final Long officeId = fromApiJsonHelper.extractLongNamed("officeId", element);
        final LocalDate joiningDate = fromApiJsonHelper.extractLocalDateNamed("joinedDate", element);

        return new ClientCommand(accountNo, externalId, firstname, middlename, lastname, fullname, officeId, joiningDate);
    }

    public void validateForUpdate(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);
        final JsonElement element = fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("client");

        boolean atLeastOneParameterPassedForUpdate = false;
        final String accountNoParameterName = "accountNo";
        if (fromApiJsonHelper.parameterExists(accountNoParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String accountNo = fromApiJsonHelper.extractStringNamed(accountNoParameterName, element);
            baseDataValidator.reset().parameter(accountNoParameterName).value(accountNo).notBlank().notExceedingLengthOf(20);
        }

        final String externalIdParameterName = "externalId";
        if (fromApiJsonHelper.parameterExists(externalIdParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String externalId = fromApiJsonHelper.extractStringNamed(externalIdParameterName, element);
            baseDataValidator.reset().parameter(externalIdParameterName).value(externalId).ignoreIfNull().notExceedingLengthOf(100);
        }
        
        final String fullnameParameterName = "fullname";
        if (fromApiJsonHelper.parameterExists(fullnameParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
        }
        
        final String lastNameParameterName = "lastname";
        if (fromApiJsonHelper.parameterExists(lastNameParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
        }
        
        final String middleNameParameterName = "middlename";
        if (fromApiJsonHelper.parameterExists(middleNameParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
        }
        
        final String firstNameParameterName = "firstname";
        if (fromApiJsonHelper.parameterExists(firstNameParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
        }

        final String officeIdParameterName = "officeId";
        if (fromApiJsonHelper.parameterExists(officeIdParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Long officeId = fromApiJsonHelper.extractLongNamed(officeIdParameterName, element);
            baseDataValidator.reset().parameter(officeIdParameterName).value(officeId).notNull().integerGreaterThanZero();
        }

        final String joinedDateParameterName = "joinedDate";
        if (fromApiJsonHelper.parameterExists(joinedDateParameterName, element)) {
            atLeastOneParameterPassedForUpdate = true;

            final String joinedDateStr = fromApiJsonHelper.extractStringNamed(joinedDateParameterName, element);
            baseDataValidator.reset().parameter(joinedDateParameterName).value(joinedDateStr).notBlank();

            final LocalDate joinedDate = fromApiJsonHelper.extractLocalDateNamed(joinedDateParameterName, element);
            baseDataValidator.reset().parameter(joinedDateParameterName).value(joinedDate).notNull();
        }

        if (!atLeastOneParameterPassedForUpdate) {
            final Object forceError = null;
            baseDataValidator.reset().anyOfNotNull(forceError);
        }

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    public void validateForCreate(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);
        final JsonElement element = fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("client");

        final String accountNoParameterName = "accountNo";
        if (fromApiJsonHelper.parameterExists(accountNoParameterName, element)) {
            final String accountNo = fromApiJsonHelper.extractStringNamed(accountNoParameterName, element);
            baseDataValidator.reset().parameter(accountNoParameterName).value(accountNo).notBlank().notExceedingLengthOf(20);
        }

        final String externalIdParameterName = "externalId";
        if (fromApiJsonHelper.parameterExists(externalIdParameterName, element)) {
            final String externalId = fromApiJsonHelper.extractStringNamed(externalIdParameterName, element);
            baseDataValidator.reset().parameter(externalIdParameterName).value(externalId).ignoreIfNull().notExceedingLengthOf(100);
        }

        final String officeIdParameterName = "officeId";
        if (fromApiJsonHelper.parameterExists(officeIdParameterName, element)) {
            final Long officeId = fromApiJsonHelper.extractLongNamed(officeIdParameterName, element);
            baseDataValidator.reset().parameter(officeIdParameterName).value(officeId).notNull().integerGreaterThanZero();
        }

        final String joinedDateParameterName = "joinedDate";
        if (fromApiJsonHelper.parameterExists(joinedDateParameterName, element)) {

            final String joinedDateStr = fromApiJsonHelper.extractStringNamed(joinedDateParameterName, element);
            baseDataValidator.reset().parameter(joinedDateParameterName).value(joinedDateStr).notBlank();

            final LocalDate joinedDate = fromApiJsonHelper.extractLocalDateNamed(joinedDateParameterName, element);
            baseDataValidator.reset().parameter(joinedDateParameterName).value(joinedDate).notNull();
        }

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}