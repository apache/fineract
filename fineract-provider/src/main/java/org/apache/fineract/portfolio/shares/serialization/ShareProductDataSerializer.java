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
package org.apache.fineract.portfolio.shares.serialization;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.glaccount.domain.GLAccountRepositoryWrapper;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanproduct.exception.InvalidCurrencyException;
import org.apache.fineract.portfolio.shares.constants.ShareProductApiConstants;
import org.apache.fineract.portfolio.shares.domain.ShareMarketPrice;
import org.apache.fineract.portfolio.shares.domain.ShareProduct;
import org.apache.fineract.useradministration.domain.AppUser;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

//This class responsibility is to validate data and serialize to entity and return object to the caller 
@Service
public class ShareProductDataSerializer {

    private final Set<String> supportedParameters = new HashSet<>(Arrays.asList(""));

    private final FromJsonHelper fromApiJsonHelper;

    private final ChargeRepositoryWrapper chargeRepository;

    private final GLAccountRepositoryWrapper glAccountRepository;

    private final PlatformSecurityContext platformSecurityContext ;
    
    @Autowired
    public ShareProductDataSerializer(final FromJsonHelper fromApiJsonHelper, final ChargeRepositoryWrapper chargeRepository,
            final GLAccountRepositoryWrapper glAccountRepository,
            final PlatformSecurityContext platformSecurityContext) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.chargeRepository = chargeRepository;
        this.glAccountRepository = glAccountRepository;
        this.platformSecurityContext = platformSecurityContext ;
    }

    public ShareProduct validateAndCreate(JsonCommand jsonCommand) {
        if (StringUtils.isBlank(jsonCommand.json())) { throw new InvalidJsonException(); }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        //this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, jsonCommand.json(), supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("sharesproduct");
        JsonElement element = jsonCommand.parsedJson();
        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(element.getAsJsonObject());
        final String productName = this.fromApiJsonHelper.extractStringNamed(ShareProductApiConstants.name_paramname, element);
        baseDataValidator.reset().parameter(ShareProductApiConstants.name_paramname).value(productName).notBlank()
                .notExceedingLengthOf(200);
        final String shortName = this.fromApiJsonHelper.extractStringNamed(ShareProductApiConstants.shortname_paramname, element);
        baseDataValidator.reset().parameter(ShareProductApiConstants.shortname_paramname).value(shortName).notBlank()
                .notExceedingLengthOf(10);
        String description = null;
        if (this.fromApiJsonHelper.parameterExists(ShareProductApiConstants.description_paramname, element)) {
            description = this.fromApiJsonHelper.extractStringNamed(ShareProductApiConstants.description_paramname, element);
        }

        String externalId = this.fromApiJsonHelper.extractStringNamed(ShareProductApiConstants.externalid_paramname, element);
        Long totalNumberOfShares = this.fromApiJsonHelper.extractLongNamed(ShareProductApiConstants.totalshares_paramname, element);
        final String currencyCode = this.fromApiJsonHelper.extractStringNamed(ShareProductApiConstants.currency_paramname, element);
        final Integer digitsAfterDecimal = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(
                ShareProductApiConstants.digitsafterdecimal_paramname, element);
        final Integer inMultiplesOf = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(
                ShareProductApiConstants.inmultiplesof_paramname, element);
        final MonetaryCurrency currency = new MonetaryCurrency(currencyCode, digitsAfterDecimal, inMultiplesOf);
        final Long sharesIssued = this.fromApiJsonHelper.extractLongNamed(ShareProductApiConstants.totalsharesissued_paramname, element);
        final BigDecimal unitPrice = this.fromApiJsonHelper.extractBigDecimalNamed(ShareProductApiConstants.unitprice_paramname, element,
                locale);
        final BigDecimal shareCapitalValue = null;
        Long suspenseAccountId = this.fromApiJsonHelper.extractLongNamed(ShareProductApiConstants.suspenseaccount_paramname, element);
        Long equityAccountId = this.fromApiJsonHelper.extractLongNamed(ShareProductApiConstants.equityaccount_paramname, element);
        Long minimumClientShares = this.fromApiJsonHelper.extractLongNamed(ShareProductApiConstants.minimumshares_paramname, element);
        Long nominalClientShares = this.fromApiJsonHelper.extractLongNamed(ShareProductApiConstants.nominaltshares_paramname, element);
        Long maximumClientShares = this.fromApiJsonHelper.extractLongNamed(ShareProductApiConstants.maximumshares_paramname, element);
        Set<ShareMarketPrice> marketPriceSet = asembleShareMarketPrice(element);
        Set<Charge> charges = assembleListOfProductCharges(element, currencyCode);
        Boolean allowdividendsForInactiveClients = this.fromApiJsonHelper.extractBooleanNamed(
                ShareProductApiConstants.allowdividendcalculationforinactiveclients_paramname, element);
        PeriodFrequencyType lockPeriod = extractPeriodType(ShareProductApiConstants.lockperiod_paramname, element);
        PeriodFrequencyType minimumActivePeriod = extractPeriodType(ShareProductApiConstants.minimumactiveperiodfordividends_paramname,
                element);
        GLAccount suspenseAccount = glAccountRepository.findOneWithNotFoundDetection(suspenseAccountId);
        GLAccount equityAccount = glAccountRepository.findOneWithNotFoundDetection(equityAccountId);
        AppUser modifiedBy = null;
        DateTime modifiedOn = null;
        AppUser createdBy = platformSecurityContext.authenticatedUser() ;
        DateTime createdDate = DateUtils.getLocalDateTimeOfTenant().toDateTime() ;
        ShareProduct product = new ShareProduct(productName, shortName, description, externalId, currency, totalNumberOfShares,
                sharesIssued, unitPrice, shareCapitalValue, suspenseAccount, equityAccount, minimumClientShares, nominalClientShares,
                maximumClientShares, marketPriceSet, charges, allowdividendsForInactiveClients, lockPeriod, minimumActivePeriod,
                createdBy, createdDate, modifiedBy, modifiedOn);
        return product;
    }

    private PeriodFrequencyType extractPeriodType(String paramName, final JsonElement element) {
        PeriodFrequencyType frequencyType = PeriodFrequencyType.INVALID;
        frequencyType = PeriodFrequencyType.fromInt(this.fromApiJsonHelper.extractIntegerWithLocaleNamed(paramName, element));
        return frequencyType;
    }

    private Set<ShareMarketPrice> asembleShareMarketPrice(final JsonElement element) {
        Set<ShareMarketPrice> set = null;
        if (this.fromApiJsonHelper.parameterExists(ShareProductApiConstants.marketprice_paramname, element)) {
            set = new HashSet<>();
            JsonArray array = this.fromApiJsonHelper.extractJsonArrayNamed(ShareProductApiConstants.marketprice_paramname, element);
            for (JsonElement arrayElement : array) {
                LocalDate localDate = this.fromApiJsonHelper.extractLocalDateNamed(ShareProductApiConstants.startdate_paramname,
                        arrayElement);
                final BigDecimal shareValue = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(
                        ShareProductApiConstants.sharevalue_paramname, arrayElement);
                ShareMarketPrice obj = new ShareMarketPrice(localDate.toDate(), shareValue);
                set.add(obj);
            }
        }
        return set;
    }

    private Set<Charge> assembleListOfProductCharges(final JsonElement element, final String currencyCode) {
        final Set<Charge> charges = new HashSet<>();
        if (this.fromApiJsonHelper.parameterExists(ShareProductApiConstants.charges_paramname, element)) {
            JsonArray chargesArray = this.fromApiJsonHelper.extractJsonArrayNamed(ShareProductApiConstants.charges_paramname, element);
            if (chargesArray != null) {
                for (int i = 0; i < chargesArray.size(); i++) {
                    final JsonObject jsonObject = chargesArray.get(i).getAsJsonObject();
                    if (jsonObject.has("id")) {
                        final Long id = jsonObject.get("id").getAsLong();
                        final Charge charge = this.chargeRepository.findOneWithNotFoundDetection(id);
                        if (!currencyCode.equals(charge.getCurrencyCode())) {
                            final String errorMessage = "Charge and Loan Product must have the same currency.";
                            throw new InvalidCurrencyException("charge", "attach.to.loan.product", errorMessage);
                        }
                        charges.add(charge);
                    }
                }
            }
        }
        return charges;
    }

    public Map<String, Object> validateAndUpdate(JsonCommand jsonCommand, ShareProduct product) {
        Map<String, Object> actualChanges = new HashMap<>();

        if (StringUtils.isBlank(jsonCommand.json())) { throw new InvalidJsonException(); }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        //this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, jsonCommand.json(), supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("sharesproduct");

        JsonElement element = jsonCommand.parsedJson();
        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(element.getAsJsonObject());
        if (this.fromApiJsonHelper.parameterExists(ShareProductApiConstants.name_paramname, element)) {
            final String productName = this.fromApiJsonHelper.extractStringNamed(ShareProductApiConstants.name_paramname, element);
            if (product.setProductName(productName)) {
                actualChanges.put(ShareProductApiConstants.name_paramname, productName);
            }
        }

        if (this.fromApiJsonHelper.parameterExists(ShareProductApiConstants.shortname_paramname, element)) {
            final String shortName = this.fromApiJsonHelper.extractStringNamed(ShareProductApiConstants.shortname_paramname, element);
            if (product.setShortName(shortName)) {
                actualChanges.put(ShareProductApiConstants.shortname_paramname, shortName);
            }
        }
        if (this.fromApiJsonHelper.parameterExists(ShareProductApiConstants.description_paramname, element)) {
            String description = this.fromApiJsonHelper.extractStringNamed(ShareProductApiConstants.description_paramname, element);
            if (product.setDescription(description)) {
                actualChanges.put(ShareProductApiConstants.description_paramname, description);
            }
        }

        if (this.fromApiJsonHelper.parameterExists(ShareProductApiConstants.externalid_paramname, element)) {
            String externalId = this.fromApiJsonHelper.extractStringNamed(ShareProductApiConstants.externalid_paramname, element);
            if (product.setExternalId(externalId)) {
                actualChanges.put(ShareProductApiConstants.externalid_paramname, externalId);
            }
        }

        if (this.fromApiJsonHelper.parameterExists(ShareProductApiConstants.totalshares_paramname, element)) {
            Long totalShares = this.fromApiJsonHelper.extractLongNamed(ShareProductApiConstants.totalshares_paramname, element);
            if (product.setTotalShares(totalShares)) {
                actualChanges.put(ShareProductApiConstants.totalshares_paramname, totalShares);
            }
        }

        if (this.fromApiJsonHelper.parameterExists(ShareProductApiConstants.totalsharesissued_paramname, element)) {
            final Long sharesIssued = this.fromApiJsonHelper
                    .extractLongNamed(ShareProductApiConstants.totalsharesissued_paramname, element);
            if (product.setTotalIssuedShares(sharesIssued)) {
                actualChanges.put(ShareProductApiConstants.totalsharesissued_paramname, sharesIssued);
            }
        }

        if (this.fromApiJsonHelper.parameterExists(ShareProductApiConstants.currency_paramname, element)
                && this.fromApiJsonHelper.parameterExists(ShareProductApiConstants.digitsafterdecimal_paramname, element)
                && this.fromApiJsonHelper.parameterExists(ShareProductApiConstants.inmultiplesof_paramname, element)) {
            final String currencyCode = this.fromApiJsonHelper.extractStringNamed(ShareProductApiConstants.currency_paramname, element);
            final Integer digitsAfterDecimal = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(
                    ShareProductApiConstants.digitsafterdecimal_paramname, element);
            final Integer inMultiplesOf = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(
                    ShareProductApiConstants.inmultiplesof_paramname, element);
            final MonetaryCurrency currency = new MonetaryCurrency(currencyCode, digitsAfterDecimal, inMultiplesOf);
            if (product.setMonetaryCurrency(currency)) {
                actualChanges.put(ShareProductApiConstants.currency_paramname, currency);
            }
        }

        if (this.fromApiJsonHelper.parameterExists(ShareProductApiConstants.unitprice_paramname, element)) {
            final BigDecimal unitPrice = this.fromApiJsonHelper.extractBigDecimalNamed(ShareProductApiConstants.unitprice_paramname,
                    element, locale);
            if (product.setUnitPrice(unitPrice)) {
                actualChanges.put(ShareProductApiConstants.unitprice_paramname, unitPrice);
            }
        }

        if (this.fromApiJsonHelper.parameterExists(ShareProductApiConstants.suspenseaccount_paramname, element)) {
            Long suspenseAccountId = this.fromApiJsonHelper.extractLongNamed(ShareProductApiConstants.suspenseaccount_paramname, element);
            GLAccount suspenseAccount = glAccountRepository.findOneWithNotFoundDetection(suspenseAccountId);
            if (product.setSuspenseAccount(suspenseAccount)) {
                actualChanges.put(ShareProductApiConstants.suspenseaccount_paramname, suspenseAccount);
            }
        }

        if (this.fromApiJsonHelper.parameterExists(ShareProductApiConstants.equityaccount_paramname, element)) {
            Long equityAccountId = this.fromApiJsonHelper.extractLongNamed(ShareProductApiConstants.equityaccount_paramname, element);
            GLAccount equityAccount = glAccountRepository.findOneWithNotFoundDetection(equityAccountId);
            if (product.setEquityAccount(equityAccount)) {
                actualChanges.put(ShareProductApiConstants.equityaccount_paramname, equityAccount);
            }
        }

        if (this.fromApiJsonHelper.parameterExists(ShareProductApiConstants.minimumshares_paramname, element)) {
            Long minimumClientShares = this.fromApiJsonHelper.extractLongNamed(ShareProductApiConstants.minimumshares_paramname, element);
            if (product.setMinimumShares(minimumClientShares)) {
                actualChanges.put(ShareProductApiConstants.minimumshares_paramname, minimumClientShares);
            }
        }

        if (this.fromApiJsonHelper.parameterExists(ShareProductApiConstants.nominaltshares_paramname, element)) {
            Long nominalClientShares = this.fromApiJsonHelper.extractLongNamed(ShareProductApiConstants.nominaltshares_paramname, element);
            if (product.setNominalShares(nominalClientShares)) {
                actualChanges.put(ShareProductApiConstants.nominaltshares_paramname, nominalClientShares);
            }
        }

        if (this.fromApiJsonHelper.parameterExists(ShareProductApiConstants.maximumshares_paramname, element)) {
            Long maximumClientShares = this.fromApiJsonHelper.extractLongNamed(ShareProductApiConstants.maximumshares_paramname, element);
            if (product.setMaximumShares(maximumClientShares)) {
                actualChanges.put(ShareProductApiConstants.maximumshares_paramname, maximumClientShares);
            }
        }

        if (this.fromApiJsonHelper.parameterExists(ShareProductApiConstants.marketprice_paramname, element)) {
            Set<ShareMarketPrice> marketPrice = asembleShareMarketPrice(element);
            if (product.setMarketPrice(marketPrice)) {
                actualChanges.put(ShareProductApiConstants.marketprice_paramname, marketPrice);
            }
        }

        if (this.fromApiJsonHelper.parameterExists(ShareProductApiConstants.charges_paramname, element)) {
            final String currencyCode = this.fromApiJsonHelper.extractStringNamed(ShareProductApiConstants.currency_paramname, element);
            Set<Charge> charges = assembleListOfProductCharges(element, currencyCode);
            if (product.setCharges(charges)) {
                actualChanges.put(ShareProductApiConstants.charges_paramname, charges);
            }
        }

        if (this.fromApiJsonHelper.parameterExists(ShareProductApiConstants.allowdividendcalculationforinactiveclients_paramname, element)) {
            Boolean allowdividendsForInactiveClients = this.fromApiJsonHelper.extractBooleanNamed(
                    ShareProductApiConstants.allowdividendcalculationforinactiveclients_paramname, element);
            if (product.setAllowDividendCalculationForInactiveClients(allowdividendsForInactiveClients)) {
                actualChanges.put(ShareProductApiConstants.allowdividendcalculationforinactiveclients_paramname,
                        allowdividendsForInactiveClients);
            }
        }

        if (this.fromApiJsonHelper.parameterExists(ShareProductApiConstants.lockperiod_paramname, element)) {
            PeriodFrequencyType lockPeriod = extractPeriodType(ShareProductApiConstants.lockperiod_paramname, element);
            if (product.setLockPeriod(lockPeriod)) {
                actualChanges.put(ShareProductApiConstants.lockperiod_paramname, lockPeriod);
            }
        }

        if (this.fromApiJsonHelper.parameterExists(ShareProductApiConstants.minimumactiveperiodfordividends_paramname, element)) {
            PeriodFrequencyType minimumActivePeriod = extractPeriodType(ShareProductApiConstants.minimumactiveperiodfordividends_paramname,
                    element);
            if (product.setminimumActivePeriodForDividends(minimumActivePeriod)) {
                actualChanges.put(ShareProductApiConstants.minimumactiveperiodfordividends_paramname, minimumActivePeriod);
            }
        }
        return actualChanges;
    }

}
