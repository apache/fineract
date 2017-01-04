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
package org.apache.fineract.portfolio.tax.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.glaccount.domain.GLAccountRepositoryWrapper;
import org.apache.fineract.accounting.glaccount.domain.GLAccountType;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.tax.api.TaxApiConstants;
import org.apache.fineract.portfolio.tax.domain.TaxComponent;
import org.apache.fineract.portfolio.tax.domain.TaxComponentRepositoryWrapper;
import org.apache.fineract.portfolio.tax.domain.TaxGroup;
import org.apache.fineract.portfolio.tax.domain.TaxGroupMappings;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Component
public class TaxAssembler {

    private final FromJsonHelper fromApiJsonHelper;
    private final GLAccountRepositoryWrapper glAccountRepositoryWrapper;
    private final TaxComponentRepositoryWrapper taxComponentRepositoryWrapper;

    @Autowired
    public TaxAssembler(final FromJsonHelper fromApiJsonHelper, final GLAccountRepositoryWrapper glAccountRepositoryWrapper,
            final TaxComponentRepositoryWrapper taxComponentRepositoryWrapper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.glAccountRepositoryWrapper = glAccountRepositoryWrapper;
        this.taxComponentRepositoryWrapper = taxComponentRepositoryWrapper;
    }

    public TaxComponent assembleTaxComponentFrom(final JsonCommand command) {
        final JsonElement element = command.parsedJson();

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("tax.component");

        final String name = this.fromApiJsonHelper.extractStringNamed(TaxApiConstants.nameParamName, element);
        final BigDecimal percentage = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(TaxApiConstants.percentageParamName, element);
        final Integer debitAccountType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(TaxApiConstants.debitAccountTypeParamName,
                element);
        final Long debitAccountId = this.fromApiJsonHelper.extractLongNamed(TaxApiConstants.debitAcountIdParamName, element);
        GLAccountType debitGlAccountType = null;
        if (debitAccountType != null) {
            debitGlAccountType = GLAccountType.fromInt(debitAccountType);
        }
        GLAccount debitGlAccount = null;
        if (debitAccountId != null) {
            debitGlAccount = this.glAccountRepositoryWrapper.findOneWithNotFoundDetection(debitAccountId);
            if (!debitGlAccount.getType().equals(debitAccountType) || debitGlAccount.isHeaderAccount()) {
                baseDataValidator.parameter(TaxApiConstants.debitAcountIdParamName).value(debitAccountId)
                        .failWithCode("not.a.valid.account");
            }
        }

        final Integer creditAccountType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(TaxApiConstants.creditAccountTypeParamName,
                element);
        GLAccountType creditGlAccountType = null;
        if (creditAccountType != null) {
            creditGlAccountType = GLAccountType.fromInt(creditAccountType);
        }
        final Long creditAccountId = this.fromApiJsonHelper.extractLongNamed(TaxApiConstants.creditAcountIdParamName, element);
        GLAccount creditGlAccount = null;
        if (creditAccountId != null) {
            creditGlAccount = this.glAccountRepositoryWrapper.findOneWithNotFoundDetection(creditAccountId);
            if (!creditGlAccount.getType().equals(creditAccountType) || creditGlAccount.isHeaderAccount()) {
                baseDataValidator.parameter(TaxApiConstants.creditAcountIdParamName).value(creditAccountId)
                        .failWithCode("not.a.valid.account");
            }
        }
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
        LocalDate startDate = this.fromApiJsonHelper.extractLocalDateNamed(TaxApiConstants.startDateParamName, element);
        if (startDate == null) {
            startDate = DateUtils.getLocalDateOfTenant();
        }

        return TaxComponent.createTaxComponent(name, percentage, debitGlAccountType, debitGlAccount, creditGlAccountType, creditGlAccount,
                startDate);
    }

    public TaxGroup assembleTaxGroupFrom(final JsonCommand command) {
        final JsonElement element = command.parsedJson();

        final String name = this.fromApiJsonHelper.extractStringNamed(TaxApiConstants.nameParamName, element);
        boolean isUpdate = false;
        final Set<TaxGroupMappings> groupMappings = assembleTaxGroupMappingsFrom(command, isUpdate);
        return TaxGroup.createTaxGroup(name, groupMappings);
    }

    public Set<TaxGroupMappings> assembleTaxGroupMappingsFrom(final JsonCommand command, boolean isUpdate) {
        Set<TaxGroupMappings> groupMappings = new HashSet<>();
        final JsonElement element = command.parsedJson();

        final JsonObject topLevelJsonElement = element.getAsJsonObject();
        final String dateFormat = this.fromApiJsonHelper.extractDateFormatParameter(topLevelJsonElement);
        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);

        if (topLevelJsonElement.get(TaxApiConstants.taxComponentsParamName).isJsonArray()) {
            final JsonArray array = topLevelJsonElement.get(TaxApiConstants.taxComponentsParamName).getAsJsonArray();
            for (int i = 0; i < array.size(); i++) {
                final JsonObject taxComponent = array.get(i).getAsJsonObject();
                final Long mappingId = this.fromApiJsonHelper.extractLongNamed(TaxApiConstants.idParamName, taxComponent);
                final Long taxComponentId = this.fromApiJsonHelper.extractLongNamed(TaxApiConstants.taxComponentIdParamName, taxComponent);
                TaxComponent component =  null;
                if(taxComponentId != null){
                    component = this.taxComponentRepositoryWrapper.findOneWithNotFoundDetection(taxComponentId);
                }
                LocalDate startDate = this.fromApiJsonHelper.extractLocalDateNamed(TaxApiConstants.startDateParamName, taxComponent,
                        dateFormat, locale);
                final LocalDate endDate = this.fromApiJsonHelper.extractLocalDateNamed(TaxApiConstants.endDateParamName, taxComponent,
                        dateFormat, locale);
                if (endDate == null && startDate == null) {
                    startDate = DateUtils.getLocalDateOfTenant();
                }
                TaxGroupMappings mappings = null;
                if (isUpdate && mappingId != null) {
                    mappings = TaxGroupMappings.createTaxGroupMappings(mappingId, component, endDate);
                } else {
                    mappings = TaxGroupMappings.createTaxGroupMappings(component, startDate);
                }
                groupMappings.add(mappings);

            }
        }

        return groupMappings;
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }
}
