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
package org.apache.fineract.portfolio.shareproducts.serialization;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.accounting.common.AccountingConstants;
import org.apache.fineract.accounting.common.AccountingRuleType;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.exception.InvalidCurrencyException;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.shareproducts.constants.ShareProductApiConstants;
import org.apache.fineract.portfolio.shareproducts.data.ShareProductMarketPriceData;
import org.apache.fineract.portfolio.shareproducts.domain.ShareProduct;
import org.apache.fineract.portfolio.shareproducts.domain.ShareProductMarketPrice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ShareProductDataSerializer {

    private static final Set<String> supportedParametersForCreate = new HashSet<>(Arrays.asList(ShareProductApiConstants.locale_paramname,
            ShareProductApiConstants.name_paramname, ShareProductApiConstants.shortname_paramname,
            ShareProductApiConstants.shortname_paramname, ShareProductApiConstants.description_paramname,
            ShareProductApiConstants.externalid_paramname, ShareProductApiConstants.totalshares_paramname,
            ShareProductApiConstants.currency_paramname, ShareProductApiConstants.digitsafterdecimal_paramname,
            ShareProductApiConstants.digitsafterdecimal_paramname, ShareProductApiConstants.inmultiplesof_paramname,
            ShareProductApiConstants.totalsharesissued_paramname, ShareProductApiConstants.unitprice_paramname,
            ShareProductApiConstants.minimumshares_paramname, ShareProductApiConstants.nominaltshares_paramname,
            ShareProductApiConstants.maximumshares_paramname, ShareProductApiConstants.marketprice_paramname,
            ShareProductApiConstants.charges_paramname, ShareProductApiConstants.allowdividendcalculationforinactiveclients_paramname,
            ShareProductApiConstants.lockperiod_paramname, ShareProductApiConstants.lockinperiodfrequencytype_paramname,
            ShareProductApiConstants.minimumactiveperiodfordividends_paramname,
            ShareProductApiConstants.minimumactiveperiodfrequencytype_paramname, ShareProductApiConstants.sharecapital_paramname,
            ShareProductApiConstants.accountingRuleParamName, AccountingConstants.SharesProductAccountingParams.INCOME_FROM_FEES.getValue(),
            AccountingConstants.SharesProductAccountingParams.SHARES_EQUITY.getValue(),
            AccountingConstants.SharesProductAccountingParams.SHARES_REFERENCE.getValue(),
            AccountingConstants.SharesProductAccountingParams.SHARES_SUSPENSE.getValue()));
    private static final Set<String> supportedParametersForDivident = new HashSet<>(Arrays.asList(ShareProductApiConstants.locale_paramname,
            ShareProductApiConstants.dateFormatParamName, ShareProductApiConstants.dividendPeriodStartDateParamName,
            ShareProductApiConstants.dividendPeriodEndDateParamName, ShareProductApiConstants.dividendAmountParamName));
    private final FromJsonHelper fromApiJsonHelper;
    private final ChargeRepositoryWrapper chargeRepository;
    private final PlatformSecurityContext platformSecurityContext;

    @Autowired
    public ShareProductDataSerializer(final FromJsonHelper fromApiJsonHelper, final ChargeRepositoryWrapper chargeRepository,
            final PlatformSecurityContext platformSecurityContext) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.chargeRepository = chargeRepository;
        this.platformSecurityContext = platformSecurityContext;
    }

    public ShareProduct validateAndCreate(JsonCommand jsonCommand) {
        if (StringUtils.isBlank(jsonCommand.json())) {
            throw new InvalidJsonException();
        }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {

        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, jsonCommand.json(), supportedParametersForCreate);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("sharesproduct");
        JsonElement element = jsonCommand.parsedJson();
        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(element.getAsJsonObject());
        final String productName = this.fromApiJsonHelper.extractStringNamed(ShareProductApiConstants.name_paramname, element);
        baseDataValidator.reset().parameter(ShareProductApiConstants.name_paramname).value(productName).notBlank()
                .notExceedingLengthOf(200);
        final String shortName = this.fromApiJsonHelper.extractStringNamed(ShareProductApiConstants.shortname_paramname, element);
        baseDataValidator.reset().parameter(ShareProductApiConstants.shortname_paramname).value(shortName).notBlank()
                .notExceedingLengthOf(4);

        final String description = this.fromApiJsonHelper.extractStringNamed(ShareProductApiConstants.description_paramname, element);
        baseDataValidator.reset().parameter(ShareProductApiConstants.description_paramname).value(description).notBlank()
                .notExceedingLengthOf(500);

        String externalId = this.fromApiJsonHelper.extractStringNamed(ShareProductApiConstants.externalid_paramname, element);
        // baseDataValidator.reset().parameter(ShareProductApiConstants.externalid_paramname).value(externalId).notBlank();

        Long totalNumberOfShares = this.fromApiJsonHelper.extractLongNamed(ShareProductApiConstants.totalshares_paramname, element);
        baseDataValidator.reset().parameter(ShareProductApiConstants.totalshares_paramname).value(totalNumberOfShares).notNull()
                .longGreaterThanZero();
        final Long sharesIssued = this.fromApiJsonHelper.extractLongNamed(ShareProductApiConstants.totalsharesissued_paramname, element);
        if (sharesIssued != null && totalNumberOfShares != null && sharesIssued > totalNumberOfShares) {
            baseDataValidator.reset().parameter(ShareProductApiConstants.totalsharesissued_paramname).value(sharesIssued)
                    .failWithCodeNoParameterAddedToErrorCode("sharesIssued.cannot.be.greater.than.totalNumberOfShares");
        }
        final String currencyCode = this.fromApiJsonHelper.extractStringNamed(ShareProductApiConstants.currency_paramname, element);
        final Integer digitsAfterDecimal = this.fromApiJsonHelper
                .extractIntegerWithLocaleNamed(ShareProductApiConstants.digitsafterdecimal_paramname, element);
        final Integer inMultiplesOf = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(ShareProductApiConstants.inmultiplesof_paramname,
                element);
        final MonetaryCurrency currency = new MonetaryCurrency(currencyCode, digitsAfterDecimal, inMultiplesOf);

        final BigDecimal unitPrice = this.fromApiJsonHelper.extractBigDecimalNamed(ShareProductApiConstants.unitprice_paramname, element,
                locale);
        baseDataValidator.reset().parameter(ShareProductApiConstants.unitprice_paramname).value(unitPrice).notNull().positiveAmount();

        BigDecimal shareCapitalValue = BigDecimal.ONE;
        if (sharesIssued != null && unitPrice != null) {
            shareCapitalValue = BigDecimal.valueOf(sharesIssued).multiply(unitPrice);
        }

        Integer accountingRule = this.fromApiJsonHelper.extractIntegerNamed(ShareProductApiConstants.accountingRuleParamName, element,
                locale);
        baseDataValidator.reset().parameter(ShareProductApiConstants.accountingRuleParamName).value(accountingRule).notNull()
                .integerGreaterThanZero();
        AccountingRuleType accountingRuleType = null;
        if (accountingRule != null) {
            accountingRuleType = AccountingRuleType.fromInt(accountingRule);
        }

        Long minimumClientShares = this.fromApiJsonHelper.extractLongNamed(ShareProductApiConstants.minimumshares_paramname, element);
        Long nominalClientShares = this.fromApiJsonHelper.extractLongNamed(ShareProductApiConstants.nominaltshares_paramname, element);
        baseDataValidator.reset().parameter(ShareProductApiConstants.nominaltshares_paramname).value(nominalClientShares).notNull()
                .longGreaterThanZero();
        if (minimumClientShares != null && nominalClientShares != null && !minimumClientShares.equals(nominalClientShares)) {
            baseDataValidator.reset().parameter(ShareProductApiConstants.nominaltshares_paramname).value(nominalClientShares)
                    .longGreaterThanNumber(minimumClientShares);
        }
        Long maximumClientShares = this.fromApiJsonHelper.extractLongNamed(ShareProductApiConstants.maximumshares_paramname, element);
        if (maximumClientShares != null && nominalClientShares != null && !maximumClientShares.equals(nominalClientShares)) {
            baseDataValidator.reset().parameter(ShareProductApiConstants.maximumshares_paramname).value(maximumClientShares)
                    .longGreaterThanNumber(nominalClientShares);
        }

        Set<ShareProductMarketPrice> marketPriceSet = asembleShareMarketPrice(element);
        Set<Charge> charges = assembleListOfProductCharges(element, currencyCode);
        Boolean allowdividendsForInactiveClients = this.fromApiJsonHelper
                .extractBooleanNamed(ShareProductApiConstants.allowdividendcalculationforinactiveclients_paramname, element);

        Integer minimumActivePeriod = this.fromApiJsonHelper
                .extractIntegerNamed(ShareProductApiConstants.minimumactiveperiodfordividends_paramname, element, locale);
        PeriodFrequencyType minimumActivePeriodType = extractPeriodType(ShareProductApiConstants.minimumactiveperiodfrequencytype_paramname,
                element);
        if (minimumActivePeriod != null) {
            baseDataValidator.reset().parameter(ShareProductApiConstants.minimumactiveperiodfrequencytype_paramname)
                    .value(minimumActivePeriodType.getValue()).integerSameAsNumber(PeriodFrequencyType.DAYS.getValue());
        }

        Integer lockinPeriod = this.fromApiJsonHelper.extractIntegerNamed(ShareProductApiConstants.lockperiod_paramname, element, locale);
        PeriodFrequencyType lockPeriodType = extractPeriodType(ShareProductApiConstants.lockinperiodfrequencytype_paramname, element);

        ShareProduct product = new ShareProduct(productName, shortName, description, externalId, currency, totalNumberOfShares,
                sharesIssued, unitPrice, shareCapitalValue, minimumClientShares, nominalClientShares, maximumClientShares, marketPriceSet,
                charges, allowdividendsForInactiveClients, lockinPeriod, lockPeriodType, minimumActivePeriod, minimumActivePeriodType,
                accountingRuleType);

        for (ShareProductMarketPrice data : marketPriceSet) {
            data.setShareProduct(product);
        }
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
        return product;
    }

    private PeriodFrequencyType extractPeriodType(String paramName, final JsonElement element) {
        PeriodFrequencyType frequencyType = PeriodFrequencyType.INVALID;
        frequencyType = PeriodFrequencyType.fromInt(this.fromApiJsonHelper.extractIntegerWithLocaleNamed(paramName, element));
        return frequencyType;
    }

    private Set<ShareProductMarketPriceData> asembleShareMarketPriceForUpdate(final JsonElement element) {
        Set<ShareProductMarketPriceData> set = null;
        if (this.fromApiJsonHelper.parameterExists(ShareProductApiConstants.marketprice_paramname, element)) {
            set = new HashSet<>();
            JsonArray array = this.fromApiJsonHelper.extractJsonArrayNamed(ShareProductApiConstants.marketprice_paramname, element);
            for (JsonElement arrayElement : array) {
                Long id = this.fromApiJsonHelper.extractLongNamed(ShareProductApiConstants.id_paramname, arrayElement);
                LocalDate localDate = this.fromApiJsonHelper.extractLocalDateNamed(ShareProductApiConstants.startdate_paramname,
                        arrayElement);
                final BigDecimal shareValue = this.fromApiJsonHelper
                        .extractBigDecimalWithLocaleNamed(ShareProductApiConstants.sharevalue_paramname, arrayElement);
                ShareProductMarketPriceData obj = new ShareProductMarketPriceData(id, localDate, shareValue);
                set.add(obj);
            }
        }
        return set;
    }

    private Set<ShareProductMarketPrice> asembleShareMarketPrice(final JsonElement element) {
        Set<ShareProductMarketPrice> set = new HashSet<>();
        if (this.fromApiJsonHelper.parameterExists(ShareProductApiConstants.marketprice_paramname, element)) {
            set = new HashSet<>();
            JsonArray array = this.fromApiJsonHelper.extractJsonArrayNamed(ShareProductApiConstants.marketprice_paramname, element);
            for (JsonElement arrayElement : array) {
                LocalDate localDate = this.fromApiJsonHelper.extractLocalDateNamed(ShareProductApiConstants.startdate_paramname,
                        arrayElement);
                final BigDecimal shareValue = this.fromApiJsonHelper
                        .extractBigDecimalWithLocaleNamed(ShareProductApiConstants.sharevalue_paramname, arrayElement);
                ShareProductMarketPrice obj = new ShareProductMarketPrice(localDate, shareValue);
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
                            final String errorMessage = "Charge and Share Product must have the same currency.";
                            throw new InvalidCurrencyException("charge", "attach.to.share.product", errorMessage);
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

        if (StringUtils.isBlank(jsonCommand.json())) {
            throw new InvalidJsonException();
        }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {

        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, jsonCommand.json(), supportedParametersForCreate);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("sharesproduct");

        JsonElement element = jsonCommand.parsedJson();
        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(element.getAsJsonObject());
        if (this.fromApiJsonHelper.parameterExists(ShareProductApiConstants.name_paramname, element)) {
            final String productName = this.fromApiJsonHelper.extractStringNamed(ShareProductApiConstants.name_paramname, element);
            baseDataValidator.reset().parameter(ShareProductApiConstants.name_paramname).value(productName).notBlank();
            if (product.setProductName(productName)) {
                actualChanges.put(ShareProductApiConstants.name_paramname, productName);
            }
        }

        if (this.fromApiJsonHelper.parameterExists(ShareProductApiConstants.shortname_paramname, element)) {
            final String shortName = this.fromApiJsonHelper.extractStringNamed(ShareProductApiConstants.shortname_paramname, element);
            baseDataValidator.reset().parameter(ShareProductApiConstants.shortname_paramname).value(shortName).notBlank();
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
            baseDataValidator.reset().parameter(ShareProductApiConstants.externalid_paramname).value(externalId).notBlank();
            if (product.setExternalId(externalId)) {
                actualChanges.put(ShareProductApiConstants.externalid_paramname, externalId);
            }
        }

        if (this.fromApiJsonHelper.parameterExists(ShareProductApiConstants.totalshares_paramname, element)) {
            Long totalShares = this.fromApiJsonHelper.extractLongNamed(ShareProductApiConstants.totalshares_paramname, element);
            baseDataValidator.reset().parameter(ShareProductApiConstants.totalshares_paramname).value(totalShares).notNull()
                    .longGreaterThanZero();
            if (product.setTotalShares(totalShares)) {
                actualChanges.put(ShareProductApiConstants.totalshares_paramname, totalShares);
            }
        }
        Long sharesIssued = null;
        if (this.fromApiJsonHelper.parameterExists(ShareProductApiConstants.totalsharesissued_paramname, element)) {
            sharesIssued = this.fromApiJsonHelper.extractLongNamed(ShareProductApiConstants.totalsharesissued_paramname, element);
            if (product.setTotalIssuedShares(sharesIssued)) {
                actualChanges.put(ShareProductApiConstants.totalsharesissued_paramname, sharesIssued);
            }
        } else {
            product.setTotalIssuedShares(sharesIssued);
        }

        if (sharesIssued != null && product.getSubscribedShares() != null && sharesIssued < product.getSubscribedShares()) {
            baseDataValidator.reset().parameter(ShareProductApiConstants.totalsharesissued_paramname).value(sharesIssued)
                    .failWithCodeNoParameterAddedToErrorCode("sharesissued.can.not.be.lessthan.accounts.subscribed.shares");
        }

        if (this.fromApiJsonHelper.parameterExists(ShareProductApiConstants.currency_paramname, element)
                && this.fromApiJsonHelper.parameterExists(ShareProductApiConstants.digitsafterdecimal_paramname, element)
                && this.fromApiJsonHelper.parameterExists(ShareProductApiConstants.inmultiplesof_paramname, element)) {
            final String currencyCode = this.fromApiJsonHelper.extractStringNamed(ShareProductApiConstants.currency_paramname, element);
            final Integer digitsAfterDecimal = this.fromApiJsonHelper
                    .extractIntegerWithLocaleNamed(ShareProductApiConstants.digitsafterdecimal_paramname, element);
            final Integer inMultiplesOf = this.fromApiJsonHelper
                    .extractIntegerWithLocaleNamed(ShareProductApiConstants.inmultiplesof_paramname, element);
            final MonetaryCurrency currency = new MonetaryCurrency(currencyCode, digitsAfterDecimal, inMultiplesOf);
            if (product.setMonetaryCurrency(currency)) {
                actualChanges.put(ShareProductApiConstants.currency_paramname, currency);
            }
        }

        BigDecimal unitPrice = null;
        if (this.fromApiJsonHelper.parameterExists(ShareProductApiConstants.unitprice_paramname, element)) {
            unitPrice = this.fromApiJsonHelper.extractBigDecimalNamed(ShareProductApiConstants.unitprice_paramname, element, locale);
            baseDataValidator.reset().parameter(ShareProductApiConstants.unitprice_paramname).value(unitPrice).notNull().positiveAmount();
            if (product.setUnitPrice(unitPrice)) {
                actualChanges.put(ShareProductApiConstants.unitprice_paramname, unitPrice);
            }
        }

        if (this.fromApiJsonHelper.parameterExists(ShareProductApiConstants.accountingRuleParamName, element)) {
            Integer accountingRule = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(ShareProductApiConstants.accountingRuleParamName,
                    element);
            baseDataValidator.reset().parameter(ShareProductApiConstants.accountingRuleParamName).value(accountingRule).notNull()
                    .integerGreaterThanZero();
            if (product.setAccountingRule(accountingRule)) {
                actualChanges.put(ShareProductApiConstants.accountingRuleParamName, accountingRule);
            }
        }

        if (this.fromApiJsonHelper.parameterExists(ShareProductApiConstants.minimumshares_paramname, element)) {
            Long minimumClientShares = this.fromApiJsonHelper.extractLongNamed(ShareProductApiConstants.minimumshares_paramname, element);
            baseDataValidator.reset().parameter(ShareProductApiConstants.minimumshares_paramname).value(minimumClientShares).notNull()
                    .longGreaterThanZero();
            if (product.setMinimumShares(minimumClientShares)) {
                actualChanges.put(ShareProductApiConstants.minimumshares_paramname, minimumClientShares);
            }
        }

        if (this.fromApiJsonHelper.parameterExists(ShareProductApiConstants.nominaltshares_paramname, element)) {
            Long nominalClientShares = this.fromApiJsonHelper.extractLongNamed(ShareProductApiConstants.nominaltshares_paramname, element);
            baseDataValidator.reset().parameter(ShareProductApiConstants.nominaltshares_paramname).value(nominalClientShares).notNull()
                    .longGreaterThanZero();
            if (product.setNominalShares(nominalClientShares)) {
                actualChanges.put(ShareProductApiConstants.nominaltshares_paramname, nominalClientShares);
            }
        }

        if (this.fromApiJsonHelper.parameterExists(ShareProductApiConstants.maximumshares_paramname, element)) {
            Long maximumClientShares = this.fromApiJsonHelper.extractLongNamed(ShareProductApiConstants.maximumshares_paramname, element);
            baseDataValidator.reset().parameter(ShareProductApiConstants.maximumshares_paramname).value(maximumClientShares).notNull()
                    .longGreaterThanZero();
            if (product.setMaximumShares(maximumClientShares)) {
                actualChanges.put(ShareProductApiConstants.maximumshares_paramname, maximumClientShares);
            }
        }

        if (this.fromApiJsonHelper.parameterExists(ShareProductApiConstants.marketprice_paramname, element)) {
            Set<ShareProductMarketPriceData> marketPrice = asembleShareMarketPriceForUpdate(element);
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

        if (this.fromApiJsonHelper.parameterExists(ShareProductApiConstants.allowdividendcalculationforinactiveclients_paramname,
                element)) {
            Boolean allowdividendsForInactiveClients = this.fromApiJsonHelper
                    .extractBooleanNamed(ShareProductApiConstants.allowdividendcalculationforinactiveclients_paramname, element);
            if (product.setAllowDividendCalculationForInactiveClients(allowdividendsForInactiveClients)) {
                actualChanges.put(ShareProductApiConstants.allowdividendcalculationforinactiveclients_paramname,
                        allowdividendsForInactiveClients);
            }
        }

        Integer minimumActivePeriod = null;

        if (this.fromApiJsonHelper.parameterExists(ShareProductApiConstants.minimumactiveperiodfordividends_paramname, element)) {
            minimumActivePeriod = this.fromApiJsonHelper
                    .extractIntegerNamed(ShareProductApiConstants.minimumactiveperiodfordividends_paramname, element, locale);
            if (product.setminimumActivePeriod(minimumActivePeriod)) {
                actualChanges.put(ShareProductApiConstants.minimumactiveperiodfordividends_paramname, minimumActivePeriod);
            }
        }

        if (this.fromApiJsonHelper.parameterExists(ShareProductApiConstants.minimumactiveperiodfrequencytype_paramname, element)) {
            PeriodFrequencyType minimumActivePeriodType = extractPeriodType(
                    ShareProductApiConstants.minimumactiveperiodfrequencytype_paramname, element);
            if (minimumActivePeriod != null) {
                baseDataValidator.reset().parameter(ShareProductApiConstants.minimumactiveperiodfrequencytype_paramname)
                        .value(minimumActivePeriodType.getValue()).integerSameAsNumber(PeriodFrequencyType.DAYS.getValue());
            }
            if (product.setminimumActivePeriodFrequencyType(minimumActivePeriodType)) {
                actualChanges.put(ShareProductApiConstants.minimumactiveperiodfrequencytype_paramname, minimumActivePeriodType);
            }
        }
        final Integer lockinPeriod;
        if (this.fromApiJsonHelper.parameterExists(ShareProductApiConstants.lockperiod_paramname, element)) {
            lockinPeriod = this.fromApiJsonHelper.extractIntegerNamed(ShareProductApiConstants.lockperiod_paramname, element, locale);
            if (product.setLockinPeriod(lockinPeriod)) {
                actualChanges.put(ShareProductApiConstants.lockperiod_paramname, lockinPeriod);
            }
        }

        if (this.fromApiJsonHelper.parameterExists(ShareProductApiConstants.lockinperiodfrequencytype_paramname, element)) {
            PeriodFrequencyType lockPeriod = extractPeriodType(ShareProductApiConstants.lockinperiodfrequencytype_paramname, element);
            if (product.setLockPeriodFrequencyType(lockPeriod)) {
                actualChanges.put(ShareProductApiConstants.lockinperiodfrequencytype_paramname, lockPeriod);
            }
        }

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        BigDecimal shareCapitalValue;
        if (sharesIssued != null || unitPrice != null) {
            if (sharesIssued == null) {
                sharesIssued = product.getTotalShares();
            }
            if (unitPrice == null) {
                unitPrice = product.getUnitPrice();
            }
            shareCapitalValue = BigDecimal.valueOf(sharesIssued).multiply(unitPrice);
            if (product.setshareCapitalValue(shareCapitalValue)) {
                actualChanges.put(ShareProductApiConstants.sharecapital_paramname, shareCapitalValue);
            }
        }
        return actualChanges;
    }

    public void validateDividendDetails(JsonCommand jsonCommand) {
        if (StringUtils.isBlank(jsonCommand.json())) {
            throw new InvalidJsonException();
        }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {

        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, jsonCommand.json(), supportedParametersForDivident);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource("sharesproduct.dividend.processing");

        JsonElement element = jsonCommand.parsedJson();

        final LocalDate dividendPeriodStartDate = this.fromApiJsonHelper
                .extractLocalDateNamed(ShareProductApiConstants.dividendPeriodStartDateParamName, element);
        baseDataValidator.reset().parameter(ShareProductApiConstants.dividendPeriodStartDateParamName).value(dividendPeriodStartDate)
                .notBlank();

        final LocalDate dividendPeriodEndDate = this.fromApiJsonHelper
                .extractLocalDateNamed(ShareProductApiConstants.dividendPeriodEndDateParamName, element);
        baseDataValidator.reset().parameter(ShareProductApiConstants.dividendPeriodStartDateParamName).value(dividendPeriodEndDate)
                .notBlank().validateDateAfter(dividendPeriodStartDate);
        final BigDecimal dividendAmount = this.fromApiJsonHelper
                .extractBigDecimalWithLocaleNamed(ShareProductApiConstants.dividendAmountParamName, element);
        baseDataValidator.reset().parameter(ShareProductApiConstants.dividendAmountParamName).value(dividendAmount).notBlank()
                .positiveAmount();
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

}
