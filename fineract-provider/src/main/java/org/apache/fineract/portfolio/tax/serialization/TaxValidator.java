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
package org.apache.fineract.portfolio.tax.serialization;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.accounting.glaccount.domain.GLAccountType;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.tax.api.TaxApiConstants;
import org.apache.fineract.portfolio.tax.domain.TaxComponent;
import org.apache.fineract.portfolio.tax.domain.TaxGroup;
import org.apache.fineract.portfolio.tax.domain.TaxGroupMappings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TaxValidator {

    public static final String DATE_FORMAT = "dateFormat";
    public static final String LOCALE = "locale";
    public static final String TAX_COMPONENT = "tax.component";
    public static final String TAX_GROUP = "tax.group";
    public static final String TOTAL_PERCENTAGE = "total.percentage";
    public static final String GROUP_TOTAL = "group.total.";
    public static final String COMPONENT = "component";
    public static final String DATES_ARE_OVERLAPPING = "dates.are.overlapping";
    public static final String COMPONENT_START_DATE = "component.start.date";
    private static final Set<String> SUPPORTED_TAX_COMPONENT_CREATE_PARAMETERS = new HashSet<>(
            Arrays.asList(DATE_FORMAT, LOCALE, TaxApiConstants.nameParamName, TaxApiConstants.percentageParamName,
                    TaxApiConstants.startDateParamName, TaxApiConstants.debitAccountTypeParamName, TaxApiConstants.debitAcountIdParamName,
                    TaxApiConstants.creditAccountTypeParamName, TaxApiConstants.creditAcountIdParamName));
    private static final Set<String> SUPPORTED_TAX_COMPONENT_UPDATE_PARAMETERS = new HashSet<>(Arrays.asList(DATE_FORMAT, LOCALE,
            TaxApiConstants.nameParamName, TaxApiConstants.percentageParamName, TaxApiConstants.startDateParamName));
    private static final Set<String> SUPPORTED_TAX_GROUP_PARAMETERS = new HashSet<>(
            Arrays.asList(DATE_FORMAT, LOCALE, TaxApiConstants.nameParamName, TaxApiConstants.taxComponentsParamName));
    private static final Set<String> SUPPORTED_TAX_GROUP_TAX_COMPONENTS_CREATE_PARAMETERS = new HashSet<>(
            Arrays.asList(TaxApiConstants.taxComponentIdParamName, TaxApiConstants.startDateParamName));
    private static final Set<String> SUPPORTED_TAX_GROUP_TAX_COMPONENTS_UPDATE_PARAMETERS = new HashSet<>(
            Arrays.asList(TaxApiConstants.idParamName, TaxApiConstants.taxComponentIdParamName, TaxApiConstants.startDateParamName,
                    TaxApiConstants.endDateParamName));
    public static final String DOT = ".";
    public static final String AT_INDEX = ".at.index.";
    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public TaxValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForTaxComponentCreate(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {

        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SUPPORTED_TAX_COMPONENT_CREATE_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(TAX_COMPONENT);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final String name = this.fromApiJsonHelper.extractStringNamed(TaxApiConstants.nameParamName, element);
        baseDataValidator.reset().parameter(TaxApiConstants.nameParamName).value(name).notBlank();

        final BigDecimal percentage = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(TaxApiConstants.percentageParamName, element);
        baseDataValidator.reset().parameter(TaxApiConstants.percentageParamName).value(percentage).notBlank().positiveAmount()
                .notGreaterThanMax(BigDecimal.valueOf(100));

        final Integer debitAccountType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(TaxApiConstants.debitAccountTypeParamName,
                element);
        baseDataValidator.reset().parameter(TaxApiConstants.debitAccountTypeParamName).value(debitAccountType).ignoreIfNull()
                .isOneOfTheseValues(GLAccountType.ASSET.getValue(), GLAccountType.LIABILITY.getValue(), GLAccountType.EQUITY.getValue(),
                        GLAccountType.INCOME.getValue(), GLAccountType.EXPENSE.getValue());

        final Long debitAccountId = this.fromApiJsonHelper.extractLongNamed(TaxApiConstants.debitAcountIdParamName, element);
        baseDataValidator.reset().parameter(TaxApiConstants.debitAcountIdParamName).value(debitAccountId).longGreaterThanZero();
        if (debitAccountType != null || debitAccountId != null) {
            baseDataValidator.reset().parameter(TaxApiConstants.debitAccountTypeParamName).value(debitAccountType).notBlank();
            baseDataValidator.reset().parameter(TaxApiConstants.debitAcountIdParamName).value(debitAccountId).notBlank();
        }

        final Integer creditAccountType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(TaxApiConstants.creditAccountTypeParamName,
                element);
        baseDataValidator.reset().parameter(TaxApiConstants.creditAccountTypeParamName).value(creditAccountType).ignoreIfNull()
                .isOneOfTheseValues(GLAccountType.ASSET.getValue(), GLAccountType.LIABILITY.getValue(), GLAccountType.EQUITY.getValue(),
                        GLAccountType.INCOME.getValue(), GLAccountType.EXPENSE.getValue());

        final Long creditAccountId = this.fromApiJsonHelper.extractLongNamed(TaxApiConstants.creditAcountIdParamName, element);
        baseDataValidator.reset().parameter(TaxApiConstants.creditAcountIdParamName).value(creditAccountId).longGreaterThanZero();
        if (creditAccountType != null || creditAccountId != null) {
            baseDataValidator.reset().parameter(TaxApiConstants.creditAcountIdParamName).value(creditAccountId).notBlank();
            baseDataValidator.reset().parameter(TaxApiConstants.creditAccountTypeParamName).value(creditAccountType).notBlank();
        }
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForTaxComponentUpdate(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {

        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SUPPORTED_TAX_COMPONENT_UPDATE_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(TAX_COMPONENT);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        if (this.fromApiJsonHelper.parameterExists(TaxApiConstants.nameParamName, element)) {
            final String name = this.fromApiJsonHelper.extractStringNamed(TaxApiConstants.nameParamName, element);
            baseDataValidator.reset().parameter(TaxApiConstants.nameParamName).value(name).notBlank();
        }

        if (this.fromApiJsonHelper.parameterExists(TaxApiConstants.percentageParamName, element)) {
            final BigDecimal percentage = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(TaxApiConstants.percentageParamName,
                    element);
            baseDataValidator.reset().parameter(TaxApiConstants.percentageParamName).value(percentage).notBlank().positiveAmount()
                    .notGreaterThanMax(BigDecimal.valueOf(100));
        }

        if (this.fromApiJsonHelper.parameterExists(TaxApiConstants.startDateParamName, element)) {
            final LocalDate startDate = this.fromApiJsonHelper.extractLocalDateNamed(TaxApiConstants.startDateParamName, element);
            baseDataValidator.reset().parameter(TaxApiConstants.startDateParamName).value(startDate)
                    .validateDateAfter(DateUtils.getBusinessLocalDate());
        }
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForTaxGroupCreate(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {

        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SUPPORTED_TAX_GROUP_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(TAX_GROUP);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final String name = this.fromApiJsonHelper.extractStringNamed(TaxApiConstants.nameParamName, element);
        baseDataValidator.reset().parameter(TaxApiConstants.nameParamName).value(name).notBlank();

        final JsonArray taxComponents = this.fromApiJsonHelper.extractJsonArrayNamed(TaxApiConstants.taxComponentsParamName, element);
        baseDataValidator.reset().parameter(TaxApiConstants.taxComponentsParamName).value(taxComponents).notBlank();

        final JsonObject topLevelJsonElement = element.getAsJsonObject();
        if (topLevelJsonElement.get(TaxApiConstants.taxComponentsParamName).isJsonArray()) {
            final JsonArray array = topLevelJsonElement.get(TaxApiConstants.taxComponentsParamName).getAsJsonArray();
            baseDataValidator.reset().parameter(TaxApiConstants.taxComponentsParamName).value(array.size()).integerGreaterThanZero();
            for (int i = 1; i <= array.size(); i++) {
                final JsonObject taxComponent = array.get(i - 1).getAsJsonObject();
                final String arrayObjectJson = this.fromApiJsonHelper.toJson(taxComponent);
                this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, arrayObjectJson,
                        SUPPORTED_TAX_GROUP_TAX_COMPONENTS_CREATE_PARAMETERS);
                final Long taxComponentId = this.fromApiJsonHelper.extractLongNamed(TaxApiConstants.taxComponentIdParamName, taxComponent);
                baseDataValidator.reset().parameter(TaxApiConstants.taxComponentsParamName)
                        .parameterAtIndexArray(TaxApiConstants.taxComponentIdParamName, i).value(taxComponentId).notNull()
                        .longGreaterThanZero();

            }
        }
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForTaxGroupUpdate(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {

        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SUPPORTED_TAX_GROUP_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(TAX_GROUP);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        if (this.fromApiJsonHelper.parameterExists(TaxApiConstants.nameParamName, element)) {
            final String name = this.fromApiJsonHelper.extractStringNamed(TaxApiConstants.nameParamName, element);
            baseDataValidator.reset().parameter(TaxApiConstants.nameParamName).value(name).notBlank();
        }
        if (this.fromApiJsonHelper.parameterExists(TaxApiConstants.taxComponentsParamName, element)) {
            final JsonArray taxComponents = this.fromApiJsonHelper.extractJsonArrayNamed(TaxApiConstants.taxComponentsParamName, element);
            baseDataValidator.reset().parameter(TaxApiConstants.taxComponentsParamName).value(taxComponents).notBlank();

            final JsonObject topLevelJsonElement = element.getAsJsonObject();
            final String dateFormat = this.fromApiJsonHelper.extractDateFormatParameter(topLevelJsonElement);
            final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);
            if (topLevelJsonElement.get(TaxApiConstants.taxComponentsParamName).isJsonArray()) {
                final JsonArray array = topLevelJsonElement.get(TaxApiConstants.taxComponentsParamName).getAsJsonArray();
                for (int i = 1; i <= array.size(); i++) {
                    final JsonObject taxComponent = array.get(i - 1).getAsJsonObject();
                    final String arrayObjectJson = this.fromApiJsonHelper.toJson(taxComponent);
                    this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, arrayObjectJson,
                            SUPPORTED_TAX_GROUP_TAX_COMPONENTS_UPDATE_PARAMETERS);
                    final Long taxComponentId = this.fromApiJsonHelper.extractLongNamed(TaxApiConstants.taxComponentIdParamName,
                            taxComponent);
                    final Long taxMappingId = this.fromApiJsonHelper.extractLongNamed(TaxApiConstants.taxComponentIdParamName,
                            taxComponent);
                    if (taxMappingId == null) {
                        baseDataValidator.reset().parameter(
                                TaxApiConstants.taxComponentsParamName + DOT + TaxApiConstants.taxComponentIdParamName + AT_INDEX + i)
                                .value(taxComponentId).notNull().longGreaterThanZero();
                    } else {
                        baseDataValidator.reset().parameter(
                                TaxApiConstants.taxComponentsParamName + DOT + TaxApiConstants.taxComponentIdParamName + AT_INDEX + i)
                                .value(taxComponentId).longGreaterThanZero();
                        baseDataValidator.reset()
                                .parameter(TaxApiConstants.taxComponentsParamName + DOT + TaxApiConstants.idParamName + AT_INDEX + i)
                                .value(taxMappingId).longGreaterThanZero();
                    }

                    final LocalDate endDate = this.fromApiJsonHelper.extractLocalDateNamed(TaxApiConstants.endDateParamName, taxComponent,
                            dateFormat, locale);
                    baseDataValidator.reset()
                            .parameter(TaxApiConstants.taxComponentsParamName + DOT + TaxApiConstants.endDateParamName + AT_INDEX + i)
                            .value(endDate).ignoreIfNull().validateDateAfter(DateUtils.getBusinessLocalDate());
                    final LocalDate startDate = this.fromApiJsonHelper.extractLocalDateNamed(TaxApiConstants.startDateParamName,
                            taxComponent, dateFormat, locale);
                    if (endDate != null && startDate != null) {
                        baseDataValidator.reset().parameter(TaxApiConstants.taxComponentsParamName + AT_INDEX + i)
                                .failWithCode("start.date.end.date.both.should.not.be.present", startDate, endDate);
                    }
                }
            }
        }
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateTaxGroupEndDateAndTaxComponent(final TaxGroup taxGroup, final Set<TaxGroupMappings> groupMappings) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(TAX_GROUP);

        for (TaxGroupMappings mapping : groupMappings) {
            if (mapping.getId() != null) {
                TaxGroupMappings existing = taxGroup.findOneBy(mapping);
                if (existing.endDate() != null && mapping.endDate() != null && !DateUtils.isEqual(existing.endDate(), mapping.endDate())) {
                    baseDataValidator.reset().parameter(TaxApiConstants.endDateParamName)
                            .failWithCode("can.not.modify.end.date.once.updated");
                } else {
                    baseDataValidator.reset().parameter(TaxApiConstants.endDateParamName).value(mapping.endDate()).ignoreIfNull()
                            .validateDateAfter(existing.startDate());
                }
                if (mapping.getTaxComponent() != null && !existing.getTaxComponent().getId().equals(mapping.getTaxComponent().getId())) {
                    baseDataValidator.reset().parameter(TaxApiConstants.taxComponentIdParamName).failWithCode("update.not.supported");
                }
            } else if (mapping.endDate() != null) {
                baseDataValidator.reset().parameter(TaxApiConstants.endDateParamName).failWithCode("not.supported.for.new.association");
            }
        }
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateTaxGroup(final TaxGroup taxGroup) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(TAX_GROUP);
        Set<TaxGroupMappings> groupMappings = taxGroup.getTaxGroupMappings();
        validateGroupTotal(groupMappings, baseDataValidator, TOTAL_PERCENTAGE);
        validateOverlappingComponents(groupMappings, baseDataValidator);
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateTaxComponentForUpdate(final TaxComponent taxComponent) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(TAX_COMPONENT);
        validateGroupTotal(taxComponent.getTaxGroupMappings(), baseDataValidator, GROUP_TOTAL + TaxApiConstants.percentageParamName);
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateStartDate(final LocalDate existingStartDate, final JsonCommand command) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(TAX_COMPONENT);
        validateStartDate(existingStartDate, command.localDateValueOfParameterNamed(TaxApiConstants.startDateParamName), baseDataValidator);
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void validateStartDate(final LocalDate existingStartDate, final LocalDate startDate,
            final DataValidatorBuilder baseDataValidator) {
        baseDataValidator.reset().parameter(TaxApiConstants.startDateParamName).value(startDate).validateDateAfter(existingStartDate);
    }

    private void validateOverlappingComponents(final Set<TaxGroupMappings> taxMappings, final DataValidatorBuilder baseDataValidator) {
        for (TaxGroupMappings groupMappingsOne : taxMappings) {
            final List<TaxGroupMappings> mappings = new ArrayList<>(taxMappings);
            mappings.remove(groupMappingsOne);
            for (TaxGroupMappings groupMappings : mappings) {
                if (groupMappingsOne.getTaxComponent().equals(groupMappings.getTaxComponent())) {
                    if (groupMappingsOne.endDate() == null && groupMappings.endDate() == null) {
                        baseDataValidator.reset().parameter(COMPONENT).failWithCode(DATES_ARE_OVERLAPPING);
                    } else if (DateUtils.isAfter(groupMappingsOne.startDate(), groupMappings.startDate())) {
                        baseDataValidator.reset().parameter(COMPONENT_START_DATE).value(groupMappingsOne.startDate())
                                .validateDateAfter(groupMappings.endDate());
                    } else {
                        baseDataValidator.reset().parameter(COMPONENT_START_DATE).value(groupMappings.startDate())
                                .validateDateAfter(groupMappingsOne.endDate());
                    }
                }
            }
        }
    }

    private void validateGroupTotal(final Set<TaxGroupMappings> taxMappings, final DataValidatorBuilder baseDataValidator,
            final String paramenter) {
        for (TaxGroupMappings groupMappingsOne : taxMappings) {
            Collection<LocalDate> dates = groupMappingsOne.getTaxComponent().allStartDates();
            for (LocalDate date : dates) {
                LocalDate applicableDate = date.plusDays(1);
                BigDecimal total = BigDecimal.ZERO;
                for (TaxGroupMappings groupMappings : taxMappings) {
                    if (groupMappings.occursOnDayFromAndUpToAndIncluding(applicableDate)) {
                        BigDecimal applicablePercentage = groupMappings.getTaxComponent().getApplicablePercentage(applicableDate);
                        if (applicablePercentage != null) {
                            total = total.add(applicablePercentage);
                        }
                    }
                }
                baseDataValidator.reset().parameter(paramenter).value(total).notGreaterThanMax(BigDecimal.valueOf(100));
            }
        }
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

}
