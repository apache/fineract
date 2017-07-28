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

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
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
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

@Component
public class TaxValidator {

    final Set<String> supportedTaxComponentCreateParameters = new HashSet<>(Arrays.asList("dateFormat", "locale",
            TaxApiConstants.nameParamName, TaxApiConstants.percentageParamName, TaxApiConstants.startDateParamName,
            TaxApiConstants.debitAccountTypeParamName, TaxApiConstants.debitAcountIdParamName, TaxApiConstants.creditAccountTypeParamName,
            TaxApiConstants.creditAcountIdParamName));

    final Set<String> supportedTaxComponentUpdateParameters = new HashSet<>(Arrays.asList("dateFormat", "locale",
            TaxApiConstants.nameParamName, TaxApiConstants.percentageParamName, TaxApiConstants.startDateParamName));

    final Set<String> supportedTaxGroupParameters = new HashSet<>(Arrays.asList("dateFormat", "locale", TaxApiConstants.nameParamName,
            TaxApiConstants.taxComponentsParamName));

    final Set<String> supportedTaxGroupTaxComponentsCreateParameters = new HashSet<>(Arrays.asList(TaxApiConstants.taxComponentIdParamName,
            TaxApiConstants.startDateParamName));

    final Set<String> supportedTaxGroupTaxComponentsUpdateParameters = new HashSet<>(Arrays.asList(TaxApiConstants.idParamName,
            TaxApiConstants.taxComponentIdParamName, TaxApiConstants.startDateParamName, TaxApiConstants.endDateParamName));

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public TaxValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForTaxComponentCreate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedTaxComponentCreateParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("tax.component");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final String name = this.fromApiJsonHelper.extractStringNamed(TaxApiConstants.nameParamName, element);
        baseDataValidator.reset().parameter(TaxApiConstants.nameParamName).value(name).notBlank();

        final BigDecimal percentage = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(TaxApiConstants.percentageParamName, element);
        baseDataValidator.reset().parameter(TaxApiConstants.percentageParamName).value(percentage).notBlank().positiveAmount()
                .notGreaterThanMax(BigDecimal.valueOf(100));

        final Integer debitAccountType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(TaxApiConstants.debitAccountTypeParamName,
                element);
        baseDataValidator
                .reset()
                .parameter(TaxApiConstants.debitAccountTypeParamName)
                .value(debitAccountType)
                .ignoreIfNull()
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
        baseDataValidator
                .reset()
                .parameter(TaxApiConstants.creditAccountTypeParamName)
                .value(creditAccountType)
                .ignoreIfNull()
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
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedTaxComponentUpdateParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("tax.component");

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
                    .validateDateAfter(DateUtils.getLocalDateOfTenant());
        }
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForTaxGroupCreate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedTaxGroupParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("tax.group");

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
                        supportedTaxGroupTaxComponentsCreateParameters);
                final Long taxComponentId = this.fromApiJsonHelper.extractLongNamed(TaxApiConstants.taxComponentIdParamName, taxComponent);
                baseDataValidator.reset().parameter(TaxApiConstants.taxComponentsParamName)
                        .parameterAtIndexArray(TaxApiConstants.taxComponentIdParamName, i).value(taxComponentId).notNull()
                        .longGreaterThanZero();

            }
        }
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForTaxGroupUpdate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedTaxGroupParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("tax.group");

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
                            supportedTaxGroupTaxComponentsUpdateParameters);
                    final Long taxComponentId = this.fromApiJsonHelper.extractLongNamed(TaxApiConstants.taxComponentIdParamName,
                            taxComponent);
                    final Long taxMappingId = this.fromApiJsonHelper
                            .extractLongNamed(TaxApiConstants.taxComponentIdParamName, taxComponent);
                    if (taxMappingId == null) {
                        baseDataValidator
                                .reset()
                                .parameter(
                                        TaxApiConstants.taxComponentsParamName + "." + TaxApiConstants.taxComponentIdParamName
                                                + ".at.index." + i).value(taxComponentId).notNull().longGreaterThanZero();
                    } else {
                        baseDataValidator
                                .reset()
                                .parameter(
                                        TaxApiConstants.taxComponentsParamName + "." + TaxApiConstants.taxComponentIdParamName
                                                + ".at.index." + i).value(taxComponentId).longGreaterThanZero();
                        baseDataValidator.reset()
                                .parameter(TaxApiConstants.taxComponentsParamName + "." + TaxApiConstants.idParamName + ".at.index." + i)
                                .value(taxMappingId).longGreaterThanZero();
                    }

                    final LocalDate endDate = this.fromApiJsonHelper.extractLocalDateNamed(TaxApiConstants.endDateParamName, taxComponent,
                            dateFormat, locale);
                    baseDataValidator.reset()
                            .parameter(TaxApiConstants.taxComponentsParamName + "." + TaxApiConstants.endDateParamName + ".at.index." + i)
                            .value(endDate).ignoreIfNull().validateDateAfter(DateUtils.getLocalDateOfTenant());
                    final LocalDate startDate = this.fromApiJsonHelper.extractLocalDateNamed(TaxApiConstants.startDateParamName,
                            taxComponent, dateFormat, locale);
                    if (endDate != null && startDate != null) {
                        baseDataValidator.reset().parameter(TaxApiConstants.taxComponentsParamName + ".at.index." + i)
                                .failWithCode("start.date.end.date.both.should.not.be.present", startDate, endDate);
                    }
                }
            }
        }
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateTaxGroupEndDateAndTaxComponent(final TaxGroup taxGroup, final Set<TaxGroupMappings> groupMappings) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("tax.group");

        for (TaxGroupMappings mapping : groupMappings) {
            if (mapping.getId() != null) {
                TaxGroupMappings existing = taxGroup.findOneBy(mapping);
                if (existing.endDate() != null && mapping.endDate() != null && !existing.endDate().isEqual(mapping.endDate())) {
                    baseDataValidator.reset().parameter(TaxApiConstants.endDateParamName)
                            .failWithCode("can.not.modify.end.date.once.updated");
                } else {
                    baseDataValidator.reset().parameter(TaxApiConstants.endDateParamName).value(mapping.endDate()).ignoreIfNull()
                            .validateDateAfter(existing.startDate());
                }
                if(mapping.getTaxComponent()!= null && !existing.getTaxComponent().getId().equals(mapping.getTaxComponent().getId())){
                    baseDataValidator.reset().parameter(TaxApiConstants.taxComponentIdParamName).failWithCode("update.not.supported");;
                }
            } else if (mapping.endDate() != null) {
                baseDataValidator.reset().parameter(TaxApiConstants.endDateParamName).failWithCode("not.supported.for.new.association");
            }
        }
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateTaxGroup(final TaxGroup taxGroup) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("tax.group");
        Set<TaxGroupMappings> groupMappings = taxGroup.getTaxGroupMappings();
        validateGroupTotal(groupMappings, baseDataValidator, "total.percentage");
        validateOverlappingComponents(groupMappings, baseDataValidator);
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateTaxComponentForUpdate(final TaxComponent taxComponent) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("tax.component");
        validateGroupTotal(taxComponent.getTaxGroupMappings(), baseDataValidator, "group.total." + TaxApiConstants.percentageParamName);
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateStartDate(final LocalDate existingStartDate, final JsonCommand command) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("tax.component");
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
                        baseDataValidator.reset().parameter("component").failWithCode("dates.are.overlapping");
                    } else if (groupMappingsOne.startDate().isAfter(groupMappings.startDate())) {
                        baseDataValidator.reset().parameter("component.start.date").value(groupMappingsOne.startDate())
                                .validateDateAfter(groupMappings.endDate());
                    } else {
                        baseDataValidator.reset().parameter("component.start.date").value(groupMappings.startDate())
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
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }

}
