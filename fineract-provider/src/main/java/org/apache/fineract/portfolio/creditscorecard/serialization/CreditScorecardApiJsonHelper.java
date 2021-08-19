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
package org.apache.fineract.portfolio.creditscorecard.serialization;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.creditscorecard.domain.FeatureCategory;
import org.apache.fineract.portfolio.creditscorecard.domain.FeatureDataType;
import org.apache.fineract.portfolio.creditscorecard.domain.FeatureValueType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CreditScorecardApiJsonHelper {

    private final Set<String> supportedParameters = new HashSet<>(
            Arrays.asList("name", "valueType", "dataType", "category", "active", "locale"));

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public CreditScorecardApiJsonHelper(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateFeatureForCreate(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("credit_scorecard_feature");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final String name = this.fromApiJsonHelper.extractStringNamed("name", element);
        baseDataValidator.reset().parameter("name").value(name).notBlank().notExceedingLengthOf(100);

        final Integer valueType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed("valueType", element);
        baseDataValidator.reset().parameter("valueType").value(valueType).notNull();
        if (valueType != null) {
            baseDataValidator.reset().parameter("valueType").value(valueType).isOneOfTheseValues(FeatureValueType.validValues());
        }

        final Integer dataType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed("dataType", element);
        baseDataValidator.reset().parameter("dataType").value(dataType).notNull();
        if (valueType != null) {
            baseDataValidator.reset().parameter("dataType").value(dataType).isOneOfTheseValues(FeatureDataType.validValues());
        }

        final Integer category = this.fromApiJsonHelper.extractIntegerSansLocaleNamed("category", element);
        baseDataValidator.reset().parameter("category").value(category).notNull();
        if (valueType != null) {
            baseDataValidator.reset().parameter("category").value(category).isOneOfTheseValues(FeatureCategory.validValues());
        }

        if (this.fromApiJsonHelper.parameterExists("active", element)) {
            final Boolean active = this.fromApiJsonHelper.extractBooleanNamed("active", element);
            baseDataValidator.reset().parameter("active").value(active).notNull();
        }

    }

    public void validateFeatureForUpdate(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("credit_scorecard_feature");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final String name = this.fromApiJsonHelper.extractStringNamed("name", element);
        baseDataValidator.reset().parameter("name").value(name).notBlank().notExceedingLengthOf(100);

        final Integer valueType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed("valueType", element);
        baseDataValidator.reset().parameter("valueType").value(valueType).notNull();
        if (valueType != null) {
            baseDataValidator.reset().parameter("valueType").value(valueType).isOneOfTheseValues(FeatureValueType.validValues());
        }

        final Integer dataType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed("dataType", element);
        baseDataValidator.reset().parameter("dataType").value(dataType).notNull();
        if (valueType != null) {
            baseDataValidator.reset().parameter("dataType").value(dataType).isOneOfTheseValues(FeatureDataType.validValues());
        }

        final Integer category = this.fromApiJsonHelper.extractIntegerSansLocaleNamed("category", element);
        baseDataValidator.reset().parameter("category").value(category).notNull();
        if (valueType != null) {
            baseDataValidator.reset().parameter("category").value(category).isOneOfTheseValues(FeatureCategory.validValues());
        }

        if (this.fromApiJsonHelper.parameterExists("active", element)) {
            final Boolean active = this.fromApiJsonHelper.extractBooleanNamed("active", element);
            baseDataValidator.reset().parameter("active").value(active).notNull();
        }

    }

    public void validateScorecardJson(final JsonElement element) {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("creditScorecard");

        final String scorecardParameterName = "scorecard";
        final JsonObject topLevelJsonElement = element.getAsJsonObject();

        if (topLevelJsonElement.get(scorecardParameterName).isJsonObject()) {
            final Type arrayObjectParameterTypeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
            final Set<String> supportedParameters = new HashSet<>(Arrays.asList("scoringMethod", "scoringModel", "mlScorecard",
                    "statScorecard", "ruleBasedScorecard", "locale", "dateFormat"));

            final JsonObject scorecardElement = topLevelJsonElement.getAsJsonObject(scorecardParameterName);
            this.fromApiJsonHelper.checkForUnsupportedParameters(arrayObjectParameterTypeOfMap,
                    this.fromApiJsonHelper.toJson(scorecardElement), supportedParameters);

            final String dateFormat = this.fromApiJsonHelper.extractDateFormatParameter(scorecardElement);
            final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(scorecardElement);

            final String scoringMethod = this.fromApiJsonHelper.extractStringNamed("scoringMethod", scorecardElement);

            if (scoringMethod != null) {
                baseDataValidator.reset().parameter("scoringMethod").value(scoringMethod).notNull().notExceedingLengthOf(100);

                final String scoringModel = this.fromApiJsonHelper.extractStringNamed("scoringModel", scorecardElement);
                baseDataValidator.reset().parameter("scoringModel").value(scoringModel).notNull().notExceedingLengthOf(100);

                if (!dataValidationErrors.isEmpty()) {
                    throw new PlatformApiDataValidationException(dataValidationErrors);
                }

                switch (scoringMethod) {
                    case "ml":
                        this.validateMLScorecardJson(scorecardElement);
                    break;

                    case "stat":
                        this.validateStatScorecardJson(scorecardElement);
                    break;

                    case "ruleBased":
                        this.validateRuleBasedScorecardJson(scorecardElement);
                    break;
                }
            }
        }
    }

    private void validateRuleBasedScorecardJson(final JsonElement element) {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("ruleBasedScorecard");

        final String rbScorecardParameterName = "ruleBasedScorecard";
        if (element.isJsonObject() && this.fromApiJsonHelper.parameterExists(rbScorecardParameterName, element)) {
            final JsonObject topLevelJsonElement = element.getAsJsonObject();

            if (topLevelJsonElement.get(rbScorecardParameterName).isJsonObject()) {
                final Type arrayObjectParameterTypeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
                final Set<String> supportedParameters = new HashSet<>(Arrays.asList("criteriaScores", "scorecardScore", "scorecardColor"));

                final JsonObject rbScorecardElement = topLevelJsonElement.getAsJsonObject(rbScorecardParameterName);
                this.fromApiJsonHelper.checkForUnsupportedParameters(arrayObjectParameterTypeOfMap,
                        this.fromApiJsonHelper.toJson(rbScorecardElement), supportedParameters);

                final String criteriaScoresParameterName = "criteriaScores";
                if (rbScorecardElement.get(criteriaScoresParameterName).isJsonArray()) {
                    final Type criteriaScoreArrayObjectParameterTypeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
                    final Set<String> criteriaScoreSupportedParameters = new HashSet<>(
                            Arrays.asList("featureId", "value", "score", "color"));

                    final JsonArray array = rbScorecardElement.getAsJsonArray(criteriaScoresParameterName);
                    for (int i = 1; i <= array.size(); i++) {

                        final JsonObject criteriaScoreElement = array.get(i - 1).getAsJsonObject();
                        this.fromApiJsonHelper.checkForUnsupportedParameters(criteriaScoreArrayObjectParameterTypeOfMap,
                                this.fromApiJsonHelper.toJson(criteriaScoreElement), criteriaScoreSupportedParameters);

                        final Long featureId = this.fromApiJsonHelper.extractLongNamed("featureId", criteriaScoreElement);
                        baseDataValidator.reset().parameter(criteriaScoresParameterName).parameterAtIndexArray("featureId", i)
                                .value(featureId).notNull().integerGreaterThanZero();

                        final String feature = this.fromApiJsonHelper.extractStringNamed("feature", criteriaScoreElement);
                        baseDataValidator.reset().parameter(criteriaScoresParameterName).parameterAtIndexArray("feature", i).value(feature)
                                .ignoreIfNull().notExceedingLengthOf(100);

                        final String value = this.fromApiJsonHelper.extractStringNamed("value", criteriaScoreElement);
                        baseDataValidator.reset().parameter(criteriaScoresParameterName).parameterAtIndexArray("value", i).value(value)
                                .notNull().notExceedingLengthOf(100);

                        if (this.fromApiJsonHelper.parameterExists("score", criteriaScoreElement)) {
                            final BigDecimal score = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("score", criteriaScoreElement);
                            baseDataValidator.reset().parameter(criteriaScoresParameterName).parameterAtIndexArray("score", i).value(score)
                                    .ignoreIfNull().positiveAmount();
                        }

                        if (this.fromApiJsonHelper.parameterExists("color", criteriaScoreElement)) {
                            final String color = this.fromApiJsonHelper.extractStringNamed("color", criteriaScoreElement);
                            baseDataValidator.reset().parameter(criteriaScoresParameterName).parameterAtIndexArray("color", i).value(color)
                                    .ignoreIfNull().notExceedingLengthOf(100);
                        }
                    }
                }
            }
        }

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

    }

    private void validateStatScorecardJson(final JsonElement element) {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("statScorecard");

        final String statScorecardParameterName = "statScorecard";
        if (element.isJsonObject() && this.fromApiJsonHelper.parameterExists(statScorecardParameterName, element)) {
            final JsonObject topLevelJsonElement = element.getAsJsonObject();

            if (topLevelJsonElement.get(statScorecardParameterName).isJsonObject()) {
                final Type arrayObjectParameterTypeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
                final Set<String> supportedParameters = new HashSet<>(
                        Arrays.asList("age", "sex", "job", "housing", "creditAmount", "duration", "purpose", "locale", "dateFormat"));

                final JsonObject statScorecardElement = topLevelJsonElement.getAsJsonObject(statScorecardParameterName);
                this.fromApiJsonHelper.checkForUnsupportedParameters(arrayObjectParameterTypeOfMap,
                        this.fromApiJsonHelper.toJson(statScorecardElement), supportedParameters);

                final Integer age = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("age", statScorecardElement);
                baseDataValidator.reset().parameter("age").value(age).ignoreIfNull().integerGreaterThanZero();

                final String sex = this.fromApiJsonHelper.extractStringNamed("sex", statScorecardElement);
                baseDataValidator.reset().parameter("sex").value(sex).ignoreIfNull().notExceedingLengthOf(100);

                final String job = this.fromApiJsonHelper.extractStringNamed("job", statScorecardElement);
                baseDataValidator.reset().parameter("job").value(job).ignoreIfNull().notExceedingLengthOf(100);

                final String housing = this.fromApiJsonHelper.extractStringNamed("housing", statScorecardElement);
                baseDataValidator.reset().parameter("housing").value(housing).ignoreIfNull().notExceedingLengthOf(100);

                final BigDecimal creditAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("creditAmount",
                        statScorecardElement);
                baseDataValidator.reset().parameter("creditAmount").value(creditAmount).notNull().positiveAmount();

                final Integer duration = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("duration", statScorecardElement);
                baseDataValidator.reset().parameter("duration").value(duration).ignoreIfNull().integerGreaterThanZero();

                final String purpose = this.fromApiJsonHelper.extractStringNamed("purpose", statScorecardElement);
                baseDataValidator.reset().parameter("purpose").value(purpose).ignoreIfNull().notExceedingLengthOf(100);

            }
        }

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

    }

    private void validateMLScorecardJson(final JsonElement element) {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("mlScorecard");

        final String mlScorecardParameterName = "mlScorecard";
        if (element.isJsonObject() && this.fromApiJsonHelper.parameterExists(mlScorecardParameterName, element)) {
            final JsonObject topLevelJsonElement = element.getAsJsonObject();

            if (topLevelJsonElement.get(mlScorecardParameterName).isJsonObject()) {
                final Type arrayObjectParameterTypeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
                final Set<String> supportedParameters = new HashSet<>(
                        Arrays.asList("age", "sex", "job", "housing", "creditAmount", "duration", "purpose", "locale", "dateFormat"));

                final JsonObject mlScorecardElement = topLevelJsonElement.getAsJsonObject(mlScorecardParameterName);
                this.fromApiJsonHelper.checkForUnsupportedParameters(arrayObjectParameterTypeOfMap,
                        this.fromApiJsonHelper.toJson(mlScorecardElement), supportedParameters);

                final Integer age = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("age", mlScorecardElement);
                baseDataValidator.reset().parameter("age").value(age).ignoreIfNull().integerGreaterThanZero();

                final String sex = this.fromApiJsonHelper.extractStringNamed("sex", mlScorecardElement);
                baseDataValidator.reset().parameter("sex").value(sex).ignoreIfNull().notExceedingLengthOf(100);

                final String job = this.fromApiJsonHelper.extractStringNamed("job", mlScorecardElement);
                baseDataValidator.reset().parameter("job").value(job).ignoreIfNull().notExceedingLengthOf(100);

                final String housing = this.fromApiJsonHelper.extractStringNamed("housing", mlScorecardElement);
                baseDataValidator.reset().parameter("housing").value(housing).ignoreIfNull().notExceedingLengthOf(100);

                final BigDecimal creditAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("creditAmount", mlScorecardElement);
                baseDataValidator.reset().parameter("creditAmount").value(creditAmount).notNull().positiveAmount();

                final Integer duration = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("duration", mlScorecardElement);
                baseDataValidator.reset().parameter("duration").value(duration).ignoreIfNull().integerGreaterThanZero();

                final String purpose = this.fromApiJsonHelper.extractStringNamed("purpose", mlScorecardElement);
                baseDataValidator.reset().parameter("purpose").value(purpose).ignoreIfNull().notExceedingLengthOf(100);

            }
        }

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

    }
}
