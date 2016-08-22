package org.apache.fineract.CreditCheck.serialization;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public class CBCommandFromApiJsonDeserializer {

    private final Set<String> supportedParameters = new HashSet<>(
            Arrays.asList("alias","is_active"));

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public CBCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json,final Long cb_id) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("CreditBureau");

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        
        baseDataValidator.reset().value(cb_id).notBlank().integerGreaterThanZero();

        final String alias = this.fromApiJsonHelper.extractStringNamed("alias", element);
        baseDataValidator.reset().parameter("alias").value(alias).notBlank().notExceedingLengthOf(100);
        
        final String is_activeParameter="is_active";
        if(this.fromApiJsonHelper.parameterExists(is_activeParameter, element))
        {
           final boolean  is_active= this.fromApiJsonHelper.extractBooleanNamed("is_active", element);
            baseDataValidator.reset().parameter("is_active").value(is_active).notBlank().trueOrFalseRequired(is_active);
        }
        

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

}
