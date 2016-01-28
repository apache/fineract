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
package org.apache.fineract.portfolio.accounts.serialization;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
import org.apache.fineract.portfolio.accounts.constants.ShareAccountApiConstants;
import org.apache.fineract.portfolio.accounts.domain.PurchasedShares;
import org.apache.fineract.portfolio.accounts.domain.ShareAccount;
import org.apache.fineract.portfolio.accounts.domain.ShareAccountCharge;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanproduct.exception.InvalidCurrencyException;
import org.apache.fineract.portfolio.products.exception.ProductNotFoundException;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.portfolio.shares.domain.ShareProduct;
import org.apache.fineract.portfolio.shares.domain.ShareProductTempRepository;
import org.apache.fineract.useradministration.domain.AppUser;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

@Service
public class ShareAccountDataSerializer {

    private final Set<String> supportedParameters = new HashSet<>(Arrays.asList(""));

    private final PlatformSecurityContext platformSecurityContext;

    private final FromJsonHelper fromApiJsonHelper;

    private final ChargeRepositoryWrapper chargeRepository;

    private final GLAccountRepositoryWrapper glAccountRepository;

    private final SavingsAccountRepositoryWrapper savingsAccountRepositoryWrapper;

    private final ClientRepositoryWrapper clientRepositoryWrapper;

    @Autowired
    public ShareAccountDataSerializer(final PlatformSecurityContext platformSecurityContext, final FromJsonHelper fromApiJsonHelper,
            final ChargeRepositoryWrapper chargeRepository, GLAccountRepositoryWrapper glAccountRepository,
            final SavingsAccountRepositoryWrapper savingsAccountRepositoryWrapper, final ClientRepositoryWrapper clientRepositoryWrapper) {
        this.platformSecurityContext = platformSecurityContext;
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.chargeRepository = chargeRepository;
        this.glAccountRepository = glAccountRepository;
        this.savingsAccountRepositoryWrapper = savingsAccountRepositoryWrapper;
        this.clientRepositoryWrapper = clientRepositoryWrapper;
    }

    public ShareAccount validateAndCreate(JsonCommand jsonCommand) {

        if (StringUtils.isBlank(jsonCommand.json())) { throw new InvalidJsonException(); }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        //this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, jsonCommand.json(), supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("sharesaccount");
        JsonElement element = jsonCommand.parsedJson();

        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(element.getAsJsonObject());
        final Long clientId = this.fromApiJsonHelper.extractLongNamed(ShareAccountApiConstants.clientid_paramname, element);
        final Long productId = this.fromApiJsonHelper.extractLongNamed(ShareAccountApiConstants.productid_paramname, element);
        final Date submittedDate = this.fromApiJsonHelper.extractLocalDateNamed(ShareAccountApiConstants.submitteddate_paramname, element)
                .toDate();
        // On creation submitted date will not be there.
        final Long fieldOfficerId = this.fromApiJsonHelper.extractLongNamed(ShareAccountApiConstants.fieldofferid_paramname, element);
        final String externalId = this.fromApiJsonHelper.extractStringNamed(ShareAccountApiConstants.externalid_paramname, element);

        Long suspenseAccountId = this.fromApiJsonHelper.extractLongNamed(ShareAccountApiConstants.suspenseaccount_paramname, element);
        Long equityAccountId = this.fromApiJsonHelper.extractLongNamed(ShareAccountApiConstants.equityaccount_paramname, element);
        Long savingsAccountId = this.fromApiJsonHelper.extractLongNamed(ShareAccountApiConstants.savingsaccountid_paramname, element);
        Set<PurchasedShares> sharesPurchased = asemblePurchasedShares(element);
        
        Boolean allowdividendsForInactiveClients = this.fromApiJsonHelper.extractBooleanNamed(
                ShareAccountApiConstants.allowdividendcalculationforinactiveclients_paramname, element);
        PeriodFrequencyType lockPeriod = extractPeriodType(ShareAccountApiConstants.lockperiod_paramname, element);
        PeriodFrequencyType minimumActivePeriod = extractPeriodType(ShareAccountApiConstants.minimumactiveperiodfordividends_paramname,
                element);

        Client client = this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);
        GLAccount suspenseAccount = glAccountRepository.findOneWithNotFoundDetection(suspenseAccountId);
        GLAccount equityAccount = glAccountRepository.findOneWithNotFoundDetection(equityAccountId);
        SavingsAccount savingsAccount = this.savingsAccountRepositoryWrapper.findOneWithNotFoundDetection(savingsAccountId);
        ShareProduct shareProduct = ShareProductTempRepository.getInstance().fineOne(productId);
        if(shareProduct == null) {
            throw new ProductNotFoundException(productId, "Share") ;
        }
        final MonetaryCurrency currency = shareProduct.getCurrency() ;
        Set<ShareAccountCharge> charges = assembleListOfAccountCharges(element, currency.getCode());
        AppUser modifiedBy = null;
        DateTime modifiedOn = null;
        Date approvedDate = null;
        AppUser createdBy = platformSecurityContext.authenticatedUser();
        DateTime createdDate = DateUtils.getLocalDateTimeOfTenant().toDateTime();
        ShareAccount account = new ShareAccount(client, shareProduct, submittedDate, approvedDate, fieldOfficerId, externalId, currency,
                suspenseAccount, equityAccount, savingsAccount, sharesPurchased, allowdividendsForInactiveClients, lockPeriod,
                minimumActivePeriod, charges, createdBy, createdDate, modifiedBy, modifiedOn);
        
        for(PurchasedShares pur: sharesPurchased) {
            pur.setShareAccount(account) ;
        }
        
        for(ShareAccountCharge charge: charges) {
            charge.setShareAccount(account) ;
        }
        return account;
    }

    public Map<String, Object> validateAndUpdate(JsonCommand jsonCommand, ShareAccount account) {
        Map<String, Object> actualChanges = new HashMap<>();
        if (StringUtils.isBlank(jsonCommand.json())) { throw new InvalidJsonException(); }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        //this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, jsonCommand.json(), supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("sharesaccount");
        JsonElement element = jsonCommand.parsedJson();

        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(element.getAsJsonObject());
        
        if(this.fromApiJsonHelper.parameterExists(ShareAccountApiConstants.productid_paramname, element)) {
            final Long productId = this.fromApiJsonHelper.extractLongNamed(ShareAccountApiConstants.productid_paramname, element);
            ShareProduct shareProduct = ShareProductTempRepository.getInstance().fineOne(productId);
            if(account.setShareProduct(shareProduct)) {
                actualChanges.put(ShareAccountApiConstants.productid_paramname, shareProduct) ;    
            }
            
        }
        
        if(this.fromApiJsonHelper.parameterExists(ShareAccountApiConstants.submitteddate_paramname, element)) {
            final Date submittedDate = this.fromApiJsonHelper.extractLocalDateNamed(ShareAccountApiConstants.submitteddate_paramname, element)
                    .toDate();
            if(account.setSubmittedDate(submittedDate)) {
                actualChanges.put(ShareAccountApiConstants.submitteddate_paramname, submittedDate) ;    
            }
        }
        
        if(this.fromApiJsonHelper.parameterExists(ShareAccountApiConstants.fieldofferid_paramname, element)) {
            final Long fieldOfficerId = this.fromApiJsonHelper.extractLongNamed(ShareAccountApiConstants.fieldofferid_paramname, element);
            if(account.setFieldOfficer(fieldOfficerId)){
                actualChanges.put(ShareAccountApiConstants.fieldofferid_paramname, fieldOfficerId) ;    
            }
        }
        
        if(this.fromApiJsonHelper.parameterExists(ShareAccountApiConstants.externalid_paramname, element)) {
            final String externalId = this.fromApiJsonHelper.extractStringNamed(ShareAccountApiConstants.externalid_paramname, element);
            if(account.setExternalId(externalId)) {
                actualChanges.put(ShareAccountApiConstants.externalid_paramname, externalId) ;
            }
        }

        if(this.fromApiJsonHelper.parameterExists(ShareAccountApiConstants.suspenseaccount_paramname, element)) {
            Long suspenseAccountId = this.fromApiJsonHelper.extractLongNamed(ShareAccountApiConstants.suspenseaccount_paramname, element);
            GLAccount suspenseAccount = glAccountRepository.findOneWithNotFoundDetection(suspenseAccountId);
            if(account.setSuspenseAccount(suspenseAccount)) {
                actualChanges.put(ShareAccountApiConstants.suspenseaccount_paramname, suspenseAccount) ;
            }
        }
        
        if(this.fromApiJsonHelper.parameterExists(ShareAccountApiConstants.equityaccount_paramname, element)) {
            Long equityAccountId = this.fromApiJsonHelper.extractLongNamed(ShareAccountApiConstants.equityaccount_paramname, element);
            GLAccount equityAccount = glAccountRepository.findOneWithNotFoundDetection(equityAccountId);
            if(account.setEquityAccount(equityAccount)) {
                actualChanges.put(ShareAccountApiConstants.equityaccount_paramname, equityAccount) ;
            }
        }
        
        if(this.fromApiJsonHelper.parameterExists(ShareAccountApiConstants.savingsaccountid_paramname, element)) {
            Long savingsAccountId = this.fromApiJsonHelper.extractLongNamed(ShareAccountApiConstants.savingsaccountid_paramname, element);
            SavingsAccount savingsAccount = this.savingsAccountRepositoryWrapper.findOneWithNotFoundDetection(savingsAccountId);
            if(account.setSavingsAccount(savingsAccount)) {
                actualChanges.put(ShareAccountApiConstants.savingsaccountid_paramname, savingsAccount) ;
            }
        }
        
        if(this.fromApiJsonHelper.parameterExists(ShareAccountApiConstants.purchasedshares_paramname, element)) {
            Set<PurchasedShares> sharesPurchased = asemblePurchasedShares(element);
            account.setPurchasedShares(sharesPurchased) ;
            actualChanges.put(ShareAccountApiConstants.purchasedshares_paramname, sharesPurchased) ;
        }
        
        if (this.fromApiJsonHelper.parameterExists(ShareAccountApiConstants.allowdividendcalculationforinactiveclients_paramname, element)) {
            Boolean allowdividendsForInactiveClients = this.fromApiJsonHelper.extractBooleanNamed(
                    ShareAccountApiConstants.allowdividendcalculationforinactiveclients_paramname, element);
            if (account.setAllowDividendCalculationForInactiveClients(allowdividendsForInactiveClients)) {
                actualChanges.put(ShareAccountApiConstants.allowdividendcalculationforinactiveclients_paramname,
                        allowdividendsForInactiveClients);
            }
        }

        if (this.fromApiJsonHelper.parameterExists(ShareAccountApiConstants.lockperiod_paramname, element)) {
            PeriodFrequencyType lockPeriod = extractPeriodType(ShareAccountApiConstants.lockperiod_paramname, element);
            if (account.setLockPeriod(lockPeriod)) {
                actualChanges.put(ShareAccountApiConstants.lockperiod_paramname, lockPeriod);
            }
        }

        if (this.fromApiJsonHelper.parameterExists(ShareAccountApiConstants.minimumactiveperiodfordividends_paramname, element)) {
            PeriodFrequencyType minimumActivePeriod = extractPeriodType(ShareAccountApiConstants.minimumactiveperiodfordividends_paramname,
                    element);
            if (account.setminimumActivePeriodForDividends(minimumActivePeriod)) {
                actualChanges.put(ShareAccountApiConstants.minimumactiveperiodfordividends_paramname, minimumActivePeriod);
            }
        }
        if (this.fromApiJsonHelper.parameterExists(ShareAccountApiConstants.charges_paramname, element)) {
            ShareProduct shareProduct = account.getShareProduct() ;
            Set<ShareAccountCharge> charges = assembleListOfAccountCharges(element, shareProduct.getCurrency().getCode());
            if(account.setCharges(charges)) {
                actualChanges.put(ShareAccountApiConstants.charges_paramname, charges);
            }
        }
        AppUser modifiedBy = platformSecurityContext.authenticatedUser();
        DateTime modifiedOn = DateUtils.getLocalDateTimeOfTenant().toDateTime();
       
        return actualChanges;
    }

    private Set<ShareAccountCharge> assembleListOfAccountCharges(final JsonElement element, final String currencyCode) {
        final Set<ShareAccountCharge> charges = new HashSet<>();
        if (this.fromApiJsonHelper.parameterExists(ShareAccountApiConstants.charges_paramname, element)) {
            JsonArray chargesArray = this.fromApiJsonHelper.extractJsonArrayNamed(ShareAccountApiConstants.charges_paramname, element);
            if (chargesArray != null) {
                for (int i = 0; i < chargesArray.size(); i++) {
                    final JsonObject jsonObject = chargesArray.get(i).getAsJsonObject();
                    if (jsonObject.has("id")) {
                        final Long id = jsonObject.get("id").getAsLong();
                        final Charge charge = this.chargeRepository.findOneWithNotFoundDetection(id);
                        if (!currencyCode.equals(charge.getCurrencyCode())) {
                            final String errorMessage = "Charge and Share Account must have the same currency.";
                            throw new InvalidCurrencyException("charge", "attach.to.share.account", errorMessage);
                        }
                        ShareAccountCharge accountCharge = new ShareAccountCharge(charge);
                        charges.add(accountCharge);
                    }
                }
            }
        }
        return charges;
    }

    private PeriodFrequencyType extractPeriodType(String paramName, final JsonElement element) {
        PeriodFrequencyType frequencyType = PeriodFrequencyType.INVALID;
        frequencyType = PeriodFrequencyType.fromInt(this.fromApiJsonHelper.extractIntegerWithLocaleNamed(paramName, element));
        return frequencyType;
    }

    private Set<PurchasedShares> asemblePurchasedShares(final JsonElement element) {
        Set<PurchasedShares> set = null;
        if (this.fromApiJsonHelper.parameterExists(ShareAccountApiConstants.purchasedshares_paramname, element)) {
            set = new HashSet<>();
            JsonArray array = this.fromApiJsonHelper.extractJsonArrayNamed(ShareAccountApiConstants.purchasedshares_paramname, element);
            for (JsonElement arrayElement : array) {
                LocalDate localDate = this.fromApiJsonHelper.extractLocalDateNamed(ShareAccountApiConstants.purchaseddate_paramname,
                        arrayElement);
                final Long shares = this.fromApiJsonHelper.extractLongNamed(ShareAccountApiConstants.numberofshares_paramname, arrayElement);
                final BigDecimal shareValue = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(
                        ShareAccountApiConstants.purchasedprice_paramname, arrayElement);

                PurchasedShares obj = new PurchasedShares(localDate.toDate(), shares, shareValue);
                set.add(obj);
            }
        }
        return set;
    }
    
    public Set<PurchasedShares> asembleAdditionalShares(final JsonElement element) {
        Set<PurchasedShares> set = null;
        if (this.fromApiJsonHelper.parameterExists(ShareAccountApiConstants.additionalshares_paramname, element)) {
            set = new HashSet<>();
            JsonArray array = this.fromApiJsonHelper.extractJsonArrayNamed(ShareAccountApiConstants.additionalshares_paramname, element);
            for (JsonElement arrayElement : array) {
                LocalDate localDate = this.fromApiJsonHelper.extractLocalDateNamed(ShareAccountApiConstants.purchaseddate_paramname,
                        arrayElement);
                final Long shares = this.fromApiJsonHelper.extractLongNamed(ShareAccountApiConstants.numberofshares_paramname, arrayElement);
                final BigDecimal shareValue = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(
                        ShareAccountApiConstants.purchasedprice_paramname, arrayElement);

                PurchasedShares obj = new PurchasedShares(localDate.toDate(), shares, shareValue);
                set.add(obj);
            }
        }
        return set;
    }
}
