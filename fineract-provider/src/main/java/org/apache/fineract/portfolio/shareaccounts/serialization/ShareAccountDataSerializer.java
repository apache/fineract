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
package org.apache.fineract.portfolio.shareaccounts.serialization;

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
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.accounts.constants.ShareAccountApiConstants;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanproduct.exception.InvalidCurrencyException;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.portfolio.savings.exception.SavingsAccountNotFoundException;
import org.apache.fineract.portfolio.savings.service.SavingsAccountReadPlatformService;
import org.apache.fineract.portfolio.shareaccounts.domain.ShareAccount;
import org.apache.fineract.portfolio.shareaccounts.domain.ShareAccountCharge;
import org.apache.fineract.portfolio.shareaccounts.domain.ShareAccountChargePaidBy;
import org.apache.fineract.portfolio.shareaccounts.domain.ShareAccountStatusType;
import org.apache.fineract.portfolio.shareaccounts.domain.ShareAccountTransaction;
import org.apache.fineract.portfolio.shareproducts.domain.ShareProduct;
import org.apache.fineract.portfolio.shareproducts.domain.ShareProductRepositoryWrapper;
import org.apache.fineract.useradministration.domain.AppUser;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

@Service
public class ShareAccountDataSerializer {

    private final PlatformSecurityContext platformSecurityContext;

    private final FromJsonHelper fromApiJsonHelper;

    private final ChargeRepositoryWrapper chargeRepository;

    private final SavingsAccountRepositoryWrapper savingsAccountRepositoryWrapper;

    private final ClientRepositoryWrapper clientRepositoryWrapper;

    private final ShareProductRepositoryWrapper shareProductRepository;

    private final SavingsAccountReadPlatformService savingsAccountReadPlatformService ;

    private static final Set<String> approvalParameters = new HashSet<>(Arrays.asList(ShareAccountApiConstants
                    .locale_paramname,
            ShareAccountApiConstants.dateformat_paramname, ShareAccountApiConstants.approveddate_paramname,
            ShareAccountApiConstants.note_paramname));

    private static final Set<String> activateParameters = new HashSet<>(Arrays.asList(ShareAccountApiConstants
                    .locale_paramname,
            ShareAccountApiConstants.dateformat_paramname, ShareAccountApiConstants.activatedate_paramname));

    private static final Set<String> closeParameters = new HashSet<>(Arrays.asList(ShareAccountApiConstants
                    .locale_paramname,
            ShareAccountApiConstants.dateformat_paramname, ShareAccountApiConstants.closeddate_paramname,
            ShareAccountApiConstants.note_paramname));

    private static final Set<String> addtionalSharesParameters = new HashSet<>(Arrays.asList(ShareAccountApiConstants
                    .locale_paramname,
            ShareAccountApiConstants.requesteddate_paramname, ShareAccountApiConstants.requestedshares_paramname,
            ShareAccountApiConstants.purchasedprice_paramname, ShareAccountApiConstants.dateformat_paramname));

    
    @Autowired
    public ShareAccountDataSerializer(final PlatformSecurityContext platformSecurityContext, final FromJsonHelper fromApiJsonHelper,
            final ChargeRepositoryWrapper chargeRepository, final SavingsAccountRepositoryWrapper savingsAccountRepositoryWrapper,
            final ClientRepositoryWrapper clientRepositoryWrapper, final ShareProductRepositoryWrapper shareProductRepository,
            final SavingsAccountReadPlatformService savingsAccountReadPlatformService) {
        this.platformSecurityContext = platformSecurityContext;
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.chargeRepository = chargeRepository;
        this.savingsAccountRepositoryWrapper = savingsAccountRepositoryWrapper;
        this.clientRepositoryWrapper = clientRepositoryWrapper;
        this.shareProductRepository = shareProductRepository;
        this.savingsAccountReadPlatformService = savingsAccountReadPlatformService ;
    }

    public ShareAccount validateAndCreate(JsonCommand jsonCommand) {

        if (StringUtils.isBlank(jsonCommand.json())) { throw new InvalidJsonException(); }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, jsonCommand.json(), ShareAccountApiConstants.supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("sharesaccount");
        JsonElement element = jsonCommand.parsedJson();

        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(element.getAsJsonObject());
        final Long clientId = this.fromApiJsonHelper.extractLongNamed(ShareAccountApiConstants.clientid_paramname, element);
        final Long productId = this.fromApiJsonHelper.extractLongNamed(ShareAccountApiConstants.productid_paramname, element);
        ShareProduct shareProduct = this.shareProductRepository.findOneWithNotFoundDetection(productId);
        final LocalDate submittedDate = this.fromApiJsonHelper.extractLocalDateNamed(ShareAccountApiConstants.submitteddate_paramname,
                element);
        baseDataValidator.reset().parameter(ShareAccountApiConstants.submitteddate_paramname).value(submittedDate).notNull();

        final String externalId = this.fromApiJsonHelper.extractStringNamed(ShareAccountApiConstants.externalid_paramname, element);
        // baseDataValidator.reset().parameter(ShareAccountApiConstants.externalid_paramname).value(externalId).notNull();

        Long savingsAccountId = this.fromApiJsonHelper.extractLongNamed(ShareAccountApiConstants.savingsaccountid_paramname, element);
        baseDataValidator.reset().parameter(ShareAccountApiConstants.savingsaccountid_paramname).value(savingsAccountId).notNull()
                .longGreaterThanZero();
        final Long requestedShares = this.fromApiJsonHelper.extractLongNamed(ShareAccountApiConstants.requestedshares_paramname, element);
        baseDataValidator.reset().parameter(ShareAccountApiConstants.requestedshares_paramname).value(requestedShares).notNull()
                .longGreaterThanZero();
        
        if(shareProduct.getMinimumClientShares() != null && requestedShares < shareProduct.getMinimumClientShares()) {
            baseDataValidator.reset().parameter(ShareAccountApiConstants.requestedshares_paramname).value(requestedShares)
            .failWithCode("client.can.not.purchase.shares.lessthan.product.definition", "Client can not purchase shares less than product definition");
        }
        
        if(shareProduct.getMaximumClientShares() != null && requestedShares > shareProduct.getMaximumClientShares()) {
            baseDataValidator.reset().parameter(ShareAccountApiConstants.requestedshares_paramname).value(requestedShares)
            .failWithCode("client.can.not.purchase.shares.morethan.product.definition", "Client can not purchase shares more than product definition");
        }
        
        LocalDate applicationDate = this.fromApiJsonHelper.extractLocalDateNamed(ShareAccountApiConstants.applicationdate_param, element);
        baseDataValidator.reset().parameter(ShareAccountApiConstants.applicationdate_param).value(applicationDate).notNull();

        Boolean allowdividendsForInactiveClients = this.fromApiJsonHelper.extractBooleanNamed(
                ShareAccountApiConstants.allowdividendcalculationforinactiveclients_paramname, element);

        Integer minimumActivePeriod = this.fromApiJsonHelper.extractIntegerNamed(ShareAccountApiConstants.minimumactiveperiod_paramname,
                element, locale);
        PeriodFrequencyType minimumActivePeriodEnum = extractPeriodType(
                ShareAccountApiConstants.minimumactiveperiodfrequencytype_paramname, element);

        if (minimumActivePeriod != null) {
            baseDataValidator.reset().parameter(ShareAccountApiConstants.minimumactiveperiodfrequencytype_paramname)
                    .value(minimumActivePeriodEnum.getValue()).integerSameAsNumber(PeriodFrequencyType.DAYS.getValue());
        }

        Integer lockinPeriod = this.fromApiJsonHelper.extractIntegerNamed(ShareAccountApiConstants.lockinperiod_paramname, element, locale);
        PeriodFrequencyType lockPeriodEnum = extractPeriodType(ShareAccountApiConstants.lockperiodfrequencytype_paramname, element);

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }

        Client client = this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);
        if(!this.savingsAccountReadPlatformService.isAccountBelongsToClient(clientId, savingsAccountId, DepositAccountType.SAVINGS_DEPOSIT, shareProduct.getCurrency().getCode())) {
            throw new SavingsAccountNotFoundException(savingsAccountId) ;
        }
        SavingsAccount savingsAccount = this.savingsAccountRepositoryWrapper.findOneWithNotFoundDetection(savingsAccountId, DepositAccountType.SAVINGS_DEPOSIT);
        final MonetaryCurrency currency = shareProduct.getCurrency();
        Set<ShareAccountCharge> charges = assembleListOfAccountCharges(element, currency.getCode());

        AppUser submittedBy = platformSecurityContext.authenticatedUser();
        AppUser approvedBy = null;
        Date approvedDate = null;
        AppUser rejectedBy = null;
        Date rejectedDate = null;
        AppUser activatedBy = null;
        Date activatedDate = null;
        AppUser closedBy = null;
        Date closedDate = null;
        AppUser modifiedBy = null;
        Date modifiedDate = null;
        String accountNo = null;
        Long approvedShares = null;
        Long pendingShares = requestedShares;
        BigDecimal unitPrice = shareProduct.deriveMarketPrice(applicationDate.toDate());
        ShareAccountTransaction transaction = new ShareAccountTransaction(applicationDate.toDate(), requestedShares, unitPrice);
        Set<ShareAccountTransaction> sharesPurchased = new HashSet<>();
        sharesPurchased.add(transaction);

        ShareAccount account = new ShareAccount(client, shareProduct, externalId, currency, savingsAccount, accountNo, approvedShares,
                pendingShares, sharesPurchased, allowdividendsForInactiveClients, lockinPeriod, lockPeriodEnum, minimumActivePeriod,
                minimumActivePeriodEnum, charges, submittedBy, submittedDate.toDate(), approvedBy, approvedDate, rejectedBy, rejectedDate,
                activatedBy, activatedDate, closedBy, closedDate, modifiedBy, modifiedDate);

        for (ShareAccountTransaction pur : sharesPurchased) {
            pur.setShareAccount(account);
        }

        if (charges != null) {
            for (ShareAccountCharge charge : charges) {
                charge.update(account);
            }
        }
        createChargeTransaction(account);
        return account;
    }

    private void createChargeTransaction(ShareAccount account) {
        BigDecimal totalChargeAmount = BigDecimal.ZERO;
        Set<ShareAccountCharge> charges = account.getCharges();
        Date currentDate = DateUtils.getLocalDateOfTenant().toDate();
        for (ShareAccountCharge charge : charges) {
            if (charge.isActive() && charge.isShareAccountActivation()) {
                charge.deriveChargeAmount(totalChargeAmount, account.getCurrency());
                ShareAccountTransaction chargeTransaction = ShareAccountTransaction.createChargeTransaction(currentDate, charge);
                ShareAccountChargePaidBy paidBy = new ShareAccountChargePaidBy(chargeTransaction, charge, charge.percentageOrAmount());
                chargeTransaction.addShareAccountChargePaidBy(paidBy);
                account.addChargeTransaction(chargeTransaction);
            }
        }

        Set<ShareAccountTransaction> pendingApprovalTransaction = account.getPendingForApprovalSharePurchaseTransactions();
        for (ShareAccountTransaction pending : pendingApprovalTransaction) {
            for (ShareAccountCharge charge : charges) {
                if (charge.isActive() && charge.isSharesPurchaseCharge()) {
                    BigDecimal amount = charge.deriveChargeAmount(pending.amount(), account.getCurrency());
                    ShareAccountChargePaidBy paidBy = new ShareAccountChargePaidBy(pending, charge, amount);
                    pending.addShareAccountChargePaidBy(paidBy);
                    totalChargeAmount = totalChargeAmount.add(amount);
                }
            }
            pending.updateChargeAmount(totalChargeAmount);
        }
    }

    public Map<String, Object> validateAndUpdate(JsonCommand jsonCommand, ShareAccount account) {
        Map<String, Object> actualChanges = new HashMap<>();
        if (StringUtils.isBlank(jsonCommand.json())) { throw new InvalidJsonException(); }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, jsonCommand.json(), ShareAccountApiConstants.supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("sharesaccount");
        JsonElement element = jsonCommand.parsedJson();
        ShareProduct shareProduct = account.getShareProduct();
        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(element.getAsJsonObject());
        if (this.fromApiJsonHelper.parameterExists(ShareAccountApiConstants.productid_paramname, element)) {
            final Long productId = this.fromApiJsonHelper.extractLongNamed(ShareAccountApiConstants.productid_paramname, element);
            shareProduct = this.shareProductRepository.findOneWithNotFoundDetection(productId);
            if (account.setShareProduct(shareProduct)) {
                actualChanges.put(ShareAccountApiConstants.productid_paramname, productId);
            }
        }

        if (this.fromApiJsonHelper.parameterExists(ShareAccountApiConstants.submitteddate_paramname, element)) {
            final Date submittedDate = this.fromApiJsonHelper.extractLocalDateNamed(ShareAccountApiConstants.submitteddate_paramname,
                    element).toDate();
            baseDataValidator.reset().parameter(ShareAccountApiConstants.submitteddate_paramname).value(submittedDate).notNull();
            if (account.setSubmittedDate(submittedDate)) {
                actualChanges.put(ShareAccountApiConstants.submitteddate_paramname, submittedDate);
            }
        }

        if (this.fromApiJsonHelper.parameterExists(ShareAccountApiConstants.externalid_paramname, element)) {
            final String externalId = this.fromApiJsonHelper.extractStringNamed(ShareAccountApiConstants.externalid_paramname, element);
            // baseDataValidator.reset().parameter(ShareAccountApiConstants.externalid_paramname).value(externalId).notNull();
            if (account.setExternalId(externalId)) {
                actualChanges.put(ShareAccountApiConstants.externalid_paramname, externalId);
            }
        }

        if (this.fromApiJsonHelper.parameterExists(ShareAccountApiConstants.savingsaccountid_paramname, element)) {
            Long savingsAccountId = this.fromApiJsonHelper.extractLongNamed(ShareAccountApiConstants.savingsaccountid_paramname, element);
            baseDataValidator.reset().parameter(ShareAccountApiConstants.savingsaccountid_paramname).value(savingsAccountId).notNull()
            .longGreaterThanZero();
            if(savingsAccountId != null) {
                if(!this.savingsAccountReadPlatformService.isAccountBelongsToClient(account.getClientId(), savingsAccountId, DepositAccountType.SAVINGS_DEPOSIT, shareProduct.getCurrency().getCode())) {
                    throw new SavingsAccountNotFoundException(savingsAccountId) ;
                }
                SavingsAccount savingsAccount = this.savingsAccountRepositoryWrapper.findOneWithNotFoundDetection(savingsAccountId);
                if (account.setSavingsAccount(savingsAccount)) {
                    actualChanges.put(ShareAccountApiConstants.savingsaccountid_paramname, savingsAccount.getId());
                }    
            }
        }

        Date existingApplicationDate = null ;
        List<ShareAccountTransaction> purchaseTransactionsList = new ArrayList<>() ;
        Set<ShareAccountCharge> chargesList = new HashSet<>() ;
        
        if (this.fromApiJsonHelper.parameterExists(ShareAccountApiConstants.requestedshares_paramname, element)
                || this.fromApiJsonHelper.parameterExists(ShareAccountApiConstants.charges_paramname, element)) {
            Set<ShareAccountTransaction> transactions = account.getShareAccountTransactions();
            List<Long> reveralIds = new ArrayList<>();
            for (ShareAccountTransaction transaction : transactions) {
                if (transaction.isActive()) {
                    reveralIds.add(transaction.getId());
                    transaction.setActive(false);
                    if (!transaction.isChargeTransaction()) {
                        existingApplicationDate = transaction.getPurchasedDate();
                        ShareAccountTransaction newtransaction = new ShareAccountTransaction(transaction.getPurchasedDate(),
                                transaction.getTotalShares(), transaction.getPurchasePrice());
                        purchaseTransactionsList.add(newtransaction);
                    }
                }
            }
            
            actualChanges.put("reversalIds", reveralIds);
            Set<ShareAccountCharge> charges = account.getCharges() ;
            for(ShareAccountCharge charge: charges) {
                if(charge.isActive()) {
                    charge.setActive(false);
                    ChargeTimeType chargeTime = null;
                    ChargeCalculationType chargeCalculation = null;
                    Boolean status = Boolean.TRUE;
                    ShareAccountCharge accountCharge = ShareAccountCharge.createNewWithoutShareAccount(charge.getCharge(), charge.percentageOrAmount(), chargeTime,
                            chargeCalculation, status);
                    chargesList.add(accountCharge) ;    
                }
            }
        }

        if (this.fromApiJsonHelper.parameterExists(ShareAccountApiConstants.requestedshares_paramname, element)) {
            Long requestedShares = this.fromApiJsonHelper.extractLongNamed(ShareAccountApiConstants.requestedshares_paramname, element);
            baseDataValidator.reset().parameter(ShareAccountApiConstants.requestedshares_paramname).value(requestedShares).notNull();
            Date applicationDate = null ;
            purchaseTransactionsList.clear() ;
            if(this.fromApiJsonHelper.parameterExists(ShareAccountApiConstants.applicationdate_param, element)) {
                applicationDate = this.fromApiJsonHelper.extractLocalDateNamed(ShareAccountApiConstants.applicationdate_param, element)
                        .toDate();
                baseDataValidator.reset().parameter(ShareAccountApiConstants.applicationdate_param).value(applicationDate).notNull();
            }else {
                applicationDate = existingApplicationDate ;
            }
            BigDecimal unitPrice = shareProduct.deriveMarketPrice(applicationDate);
            ShareAccountTransaction transaction = new ShareAccountTransaction(applicationDate, requestedShares, unitPrice);
            purchaseTransactionsList.add(transaction) ;
            actualChanges.put(ShareAccountApiConstants.requestedshares_paramname, "Transaction");
            
            if(shareProduct.getMinimumClientShares() != null && requestedShares < shareProduct.getMinimumClientShares()) {
                baseDataValidator.reset().parameter(ShareAccountApiConstants.requestedshares_paramname).value(requestedShares)
                .failWithCode("client.can.not.purchase.shares.lessthan.product.definition", "Client can not purchase shares less than product definition");
            }
            
            if(shareProduct.getMaximumClientShares() != null && requestedShares > shareProduct.getMaximumClientShares()) {
                baseDataValidator.reset().parameter(ShareAccountApiConstants.requestedshares_paramname).value(requestedShares)
                .failWithCode("client.can.not.purchase.shares.morethan.product.definition", "Client can not purchase shares more than product definition");
            }
        }
        
        if(!purchaseTransactionsList.isEmpty()) {
            ShareAccountTransaction transaction = purchaseTransactionsList.get(0) ;
            account.addTransaction(transaction);
            account.setTotalPendingShares(transaction.getTotalShares());
        }
        
        if (this.fromApiJsonHelper.parameterExists(ShareAccountApiConstants.allowdividendcalculationforinactiveclients_paramname, element)) {
            Boolean allowdividendsForInactiveClients = this.fromApiJsonHelper.extractBooleanNamed(
                    ShareAccountApiConstants.allowdividendcalculationforinactiveclients_paramname, element);
            if (account.setAllowDividendCalculationForInactiveClients(allowdividendsForInactiveClients)) {
                actualChanges.put(ShareAccountApiConstants.allowdividendcalculationforinactiveclients_paramname,
                        allowdividendsForInactiveClients);
            }
        }

        if (this.fromApiJsonHelper.parameterExists(ShareAccountApiConstants.lockinperiod_paramname, element)) {
            final Integer lockinperiod = this.fromApiJsonHelper.extractIntegerNamed(ShareAccountApiConstants.lockinperiod_paramname,
                    element, locale);
            baseDataValidator.reset().parameter(ShareAccountApiConstants.lockinperiod_paramname).value(lockinperiod).notNull();
            if (account.setLockPeriod(lockinperiod)) {
                actualChanges.put(ShareAccountApiConstants.lockinperiod_paramname, lockinperiod);
            }
        }

        if (this.fromApiJsonHelper.parameterExists(ShareAccountApiConstants.lockperiodfrequencytype_paramname, element)) {
            PeriodFrequencyType lockPeriod = extractPeriodType(ShareAccountApiConstants.lockperiodfrequencytype_paramname, element);
            if (account.setLockPeriodFrequencyEnum(lockPeriod)) {
                actualChanges.put(ShareAccountApiConstants.lockperiodfrequencytype_paramname, lockPeriod);
            }
        }

        if (this.fromApiJsonHelper.parameterExists(ShareAccountApiConstants.minimumactiveperiod_paramname, element)) {
            final Integer minimumActivePeriod = this.fromApiJsonHelper.extractIntegerNamed(
                    ShareAccountApiConstants.minimumactiveperiod_paramname, element, locale);
            baseDataValidator.reset().parameter(ShareAccountApiConstants.minimumactiveperiod_paramname).value(minimumActivePeriod)
                    .notNull();
            if (account.setminimumActivePeriod(minimumActivePeriod)) {
                actualChanges.put(ShareAccountApiConstants.minimumactiveperiod_paramname, minimumActivePeriod);
            }
        }

        if (this.fromApiJsonHelper.parameterExists(ShareAccountApiConstants.minimumactiveperiodfrequencytype_paramname, element)) {
            PeriodFrequencyType minimumActivePeriod = extractPeriodType(
                    ShareAccountApiConstants.minimumactiveperiodfrequencytype_paramname, element);
            if (account.setminimumActivePeriodTypeEnum(minimumActivePeriod)) {
                actualChanges.put(ShareAccountApiConstants.minimumactiveperiodfrequencytype_paramname, minimumActivePeriod);
            }
        }
        if (this.fromApiJsonHelper.parameterExists(ShareAccountApiConstants.charges_paramname, element)) {
            shareProduct = account.getShareProduct();
            final MonetaryCurrency currency = shareProduct.getCurrency();
            chargesList = assembleListOfAccountCharges(element, currency.getCode());
            if (chargesList != null) {
                if (!chargesList.isEmpty()) {
                    actualChanges.put(ShareAccountApiConstants.charges_paramname, new HashSet<ShareAccountCharge>());
                }
            }
        }
        if(chargesList != null && !chargesList.isEmpty()) {
            for (ShareAccountCharge charge : chargesList) {
                charge.update(account);
            }
            account.addCharges(chargesList);
        }
        
        createChargeTransaction(account);
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        return actualChanges;
    }

    @SuppressWarnings("null")
    public Map<String, Object> validateAndApprove(JsonCommand jsonCommand, ShareAccount account) {
        Map<String, Object> actualChanges = new HashMap<>();
        if (StringUtils.isBlank(jsonCommand.json())) { throw new InvalidJsonException(); }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, jsonCommand.json(), approvalParameters);
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("sharesaccount");
        JsonElement element = jsonCommand.parsedJson();
        if(!account.status().equals(ShareAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL.getValue())) {
            baseDataValidator.failWithCodeNoParameterAddedToErrorCode("is.not.pending.for.approval");
        }
        LocalDate approvedDate = this.fromApiJsonHelper.extractLocalDateNamed(ShareAccountApiConstants.approveddate_paramname, element);
        final LocalDate submittalDate = new LocalDate(account.getSubmittedDate());
        if (approvedDate != null && approvedDate.isBefore(submittalDate)) {
            final DateTimeFormatter formatter = DateTimeFormat.forPattern(jsonCommand.dateFormat()).withLocale(jsonCommand.extractLocale());
            final String submittalDateAsString = formatter.print(submittalDate);
            baseDataValidator.reset().parameter(ShareAccountApiConstants.approveddate_paramname).value(submittalDateAsString)
                    .failWithCodeNoParameterAddedToErrorCode("approved.date.cannot.be.before.submitted.date");
        }
        
        Set<ShareAccountTransaction> transactions = account.getShareAccountTransactions() ;
        for(ShareAccountTransaction transaction: transactions) {
            if(transaction.isActive() && transaction.isPendingForApprovalTransaction()) {
                validateTotalSubsribedShares(account, transaction, baseDataValidator) ;
            }
        }
        
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        
        AppUser approvedUser = this.platformSecurityContext.authenticatedUser();
        account.approve(approvedDate.toDate(), approvedUser);
        actualChanges.put(ShareAccountApiConstants.id_paramname, account.getId());
        updateTotalChargeDerived(account);
        return actualChanges;
    }

    private void validateTotalSubsribedShares(final ShareAccount account, final ShareAccountTransaction transaction, final  DataValidatorBuilder baseDataValidator) {
        Long totalSubsribedShares = account.getShareProduct().getSubscribedShares() ;
        Long requested = new Long(0) ;
        if(transaction.isActive() && transaction.isPendingForApprovalTransaction()) {
           requested +=transaction.getTotalShares() ;
        }
        Long totalSharesIssuable = account.getShareProduct().getSharesIssued() ;
        if(totalSharesIssuable == null) totalSharesIssuable = account.getShareProduct().getTotalShares() ;
        if(totalSubsribedShares == null) totalSubsribedShares = new Long(0) ;
        if((totalSubsribedShares+requested) > totalSharesIssuable) {
            baseDataValidator.reset().parameter(ShareAccountApiConstants.requestedshares_paramname).value(requested)
            .failWithCodeNoParameterAddedToErrorCode("shares.requested.can.not.be.approved.exceeding.totalshares.issuable");
        }
    }
    private void updateTotalChargeDerived(final ShareAccount shareAccount) {
        // Set<ShareAccountCharge> charges = shareAccount.getCharges() ;
        Set<ShareAccountTransaction> transactions = shareAccount.getShareAccountTransactions();
        for (ShareAccountTransaction transaction : transactions) {
            if (transaction.isActive()) {
                Set<ShareAccountChargePaidBy> paidBySet = transaction.getChargesPaidBy();
                if (paidBySet != null && !paidBySet.isEmpty()) {
                    for (ShareAccountChargePaidBy chargePaidBy : paidBySet) {
                        ShareAccountCharge charge = chargePaidBy.getCharge();
                        if (charge.isActive() && charge.isSharesPurchaseCharge()) {
                            Money money = Money.of(shareAccount.getCurrency(), chargePaidBy.getAmount());
                            charge.updatePaidAmountBy(money);
                        }
                    }
                }
                if (!transaction.isChargeTransaction()) {
                    transaction.addAmountPaid(transaction.chargeAmount());
                }
            }
        }
    }

    public Map<String, Object> validateAndUndoApprove(JsonCommand jsonCommand, ShareAccount account) {
        Map<String, Object> actualChanges = new HashMap<>();
        if (StringUtils.isBlank(jsonCommand.json())) { throw new InvalidJsonException(); }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, jsonCommand.json(), activateParameters);
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("sharesaccount");
        if(!account.status().equals(ShareAccountStatusType.APPROVED.getValue())) {
            baseDataValidator.failWithCodeNoParameterAddedToErrorCode("is.not.in.approved.status");
        }
        account.undoApprove();
        updateTotalChargeDerivedForUndoApproval(account);
        actualChanges.put(ShareAccountApiConstants.charges_paramname, Boolean.TRUE);
        return actualChanges;
    }

    private void updateTotalChargeDerivedForUndoApproval(final ShareAccount shareAccount) {
        Set<ShareAccountTransaction> purchaseTransactions = shareAccount.getPendingForApprovalSharePurchaseTransactions() ;
        //we will have only 1 purchase transaction. Take that
        ShareAccountTransaction purchaseTransaction = null ;
        for (ShareAccountTransaction transaction : purchaseTransactions) {
            purchaseTransaction = transaction ;
        }
        BigDecimal transactionAmount = BigDecimal.ZERO ;
        if(purchaseTransaction != null) {
            transactionAmount = purchaseTransaction.amount().subtract(purchaseTransaction.chargeAmount()) ;
        }
         Set<ShareAccountCharge> charges = shareAccount.getCharges() ;
         for(ShareAccountCharge charge: charges) {
             if(charge.isActive() && charge.isSharesPurchaseCharge()) {
                 charge.update(transactionAmount, charge.percentageOrAmount()) ;
                 charge.deriveChargeAmount(transactionAmount, shareAccount.getCurrency());
             }
         }
        Set<ShareAccountTransaction> transactions = shareAccount.getShareAccountTransactions();
        for (ShareAccountTransaction transaction : transactions) {
            if (transaction.isActive()) {
                if (!transaction.isChargeTransaction()) {
                    transaction.resetAmountPaid();
                }
            }
        }
    }
    @SuppressWarnings("unused")
    public Map<String, Object> validateAndReject(JsonCommand jsonCommand, ShareAccount account) {
        Map<String, Object> actualChanges = new HashMap<>();
        AppUser rejectedUser = this.platformSecurityContext.authenticatedUser();
        Date rejectedDate = DateUtils.getDateOfTenant();
        account.reject(rejectedDate, rejectedUser);
        actualChanges.put(ShareAccountApiConstants.charges_paramname, Boolean.TRUE);
        return actualChanges;
    }

    @SuppressWarnings("null")
    public Map<String, Object> validateAndActivate(JsonCommand jsonCommand, ShareAccount account) {
        Map<String, Object> actualChanges = new HashMap<>();
        if (StringUtils.isBlank(jsonCommand.json())) { throw new InvalidJsonException(); }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, jsonCommand.json(), activateParameters);
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("sharesaccount");
        JsonElement element = jsonCommand.parsedJson();
        if(!account.status().equals(ShareAccountStatusType.APPROVED.getValue())) {
            baseDataValidator.failWithCodeNoParameterAddedToErrorCode("is.not.in.approved.status");
        }
        LocalDate activatedDate = this.fromApiJsonHelper.extractLocalDateNamed(ShareAccountApiConstants.activatedate_paramname, element);
        baseDataValidator.reset().parameter(ShareAccountApiConstants.activatedate_paramname).value(activatedDate).notNull();
        final LocalDate approvedDate = new LocalDate(account.getApprovedDate());
        if (activatedDate != null && activatedDate.isBefore(approvedDate)) {
            final DateTimeFormatter formatter = DateTimeFormat.forPattern(jsonCommand.dateFormat()).withLocale(jsonCommand.extractLocale());
            final String submittalDateAsString = formatter.print(approvedDate);
            baseDataValidator.reset().parameter(ShareAccountApiConstants.activatedate_paramname).value(submittalDateAsString)
                    .failWithCodeNoParameterAddedToErrorCode("cannot.be.before.approved.date");
        }
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        AppUser approvedUser = this.platformSecurityContext.authenticatedUser();
        account.activate(activatedDate.toDate(), approvedUser);
        handlechargesOnActivation(account);
        actualChanges.put(ShareAccountApiConstants.charges_paramname, activatedDate.toDate());
        return actualChanges;
    }

    private void handlechargesOnActivation(final ShareAccount account) {
        Set<ShareAccountCharge> charges = account.getCharges();
        for (ShareAccountCharge charge : charges) {
            if (charge.isActive() && charge.isShareAccountActivation()) {
                charge.markAsFullyPaid();
            }
        }

        Set<ShareAccountTransaction> transactions = account.getChargeTransactions();
        for (ShareAccountTransaction transaction : transactions) {
            if (transaction.isChargeTransaction()) {
                transaction.updateTransactionDate(account.getActivatedDate());
                transaction.updateAmountPaid(transaction.amount());
            }
        }
    }

    private Set<ShareAccountCharge> assembleListOfAccountCharges(final JsonElement element, final String currencyCode) {
        final Set<ShareAccountCharge> charges = new HashSet<>();
        if (this.fromApiJsonHelper.parameterExists(ShareAccountApiConstants.charges_paramname, element)) {
            JsonArray chargesArray = this.fromApiJsonHelper.extractJsonArrayNamed(ShareAccountApiConstants.charges_paramname, element);
            if (chargesArray != null) {
                for (int i = 0; i < chargesArray.size(); i++) {
                    final JsonObject jsonObject = chargesArray.get(i).getAsJsonObject();
                    if (jsonObject.has("chargeId")) {
                        final Long id = jsonObject.get("chargeId").getAsLong();
                        BigDecimal amount = jsonObject.get("amount").getAsBigDecimal();
                        final Charge charge = this.chargeRepository.findOneWithNotFoundDetection(id);
                        if (!currencyCode.equals(charge.getCurrencyCode())) {
                            final String errorMessage = "Charge and Share Account must have the same currency.";
                            throw new InvalidCurrencyException("charge", "attach.to.share.account", errorMessage);
                        }

                        ChargeTimeType chargeTime = null;
                        ChargeCalculationType chargeCalculation = null;
                        Boolean status = Boolean.TRUE;
                        ShareAccountCharge accountCharge = ShareAccountCharge.createNewWithoutShareAccount(charge, amount, chargeTime,
                                chargeCalculation, status);
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

    public Map<String, Object> validateAndApplyAddtionalShares(JsonCommand jsonCommand, ShareAccount account) {
        Map<String, Object> actualChanges = new HashMap<>();
        if (StringUtils.isBlank(jsonCommand.json())) { throw new InvalidJsonException(); }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, jsonCommand.json(),
                addtionalSharesParameters);
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("sharesaccount");
        JsonElement element = jsonCommand.parsedJson();
        if(!account.status().equals(ShareAccountStatusType.ACTIVE.getValue())) {
            baseDataValidator.failWithCodeNoParameterAddedToErrorCode("is.not.in.active.state") ;
        }
        LocalDate requestedDate = this.fromApiJsonHelper.extractLocalDateNamed(ShareAccountApiConstants.requesteddate_paramname, element);
        baseDataValidator.reset().parameter(ShareAccountApiConstants.requesteddate_paramname).value(requestedDate).notNull();
        final Long sharesRequested = this.fromApiJsonHelper.extractLongNamed(ShareAccountApiConstants.requestedshares_paramname, element);
        baseDataValidator.reset().parameter(ShareAccountApiConstants.requestedshares_paramname).value(sharesRequested).notNull();
        ShareProduct shareProduct = account.getShareProduct();
        if (sharesRequested != null) {
            Long totalSharesAfterapproval = account.getTotalApprovedShares() + sharesRequested;
            if(shareProduct.getMaximumClientShares() != null && totalSharesAfterapproval > shareProduct.getMaximumClientShares()) {
                baseDataValidator.reset().parameter(ShareAccountApiConstants.requestedshares_paramname).value(sharesRequested)
                .failWithCode("exceeding.maximum.limit.defined.in.the.shareproduct", "Existing and requested shares count is more than product definition");
            }
        }
        boolean isTransactionBeforeExistingTransactions = false ;
        Set<ShareAccountTransaction> transactions = account.getShareAccountTransactions() ;
        for(ShareAccountTransaction transaction: transactions) {
            if(!transaction.isChargeTransaction()) {
                LocalDate transactionDate = new LocalDate(transaction.getPurchasedDate()) ;
                if(requestedDate.isBefore(transactionDate)) {
                    isTransactionBeforeExistingTransactions = true ;
                    break ;
                }    
            }
        }
        if(isTransactionBeforeExistingTransactions) {
            baseDataValidator.reset().parameter(ShareAccountApiConstants.requesteddate_paramname)
            .value(requestedDate)
            .failWithCodeNoParameterAddedToErrorCode("purchase.transaction.date.cannot.be.before.existing.transactions");
        }
        
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        final BigDecimal unitPrice = shareProduct.deriveMarketPrice(requestedDate.toDate());
        ShareAccountTransaction purchaseTransaction = new ShareAccountTransaction(requestedDate.toDate(), sharesRequested, unitPrice);
        account.addAdditionalPurchasedShares(purchaseTransaction);
        handleAdditionalSharesChargeTransactions(account, purchaseTransaction);
        actualChanges.put(ShareAccountApiConstants.additionalshares_paramname, purchaseTransaction);
        return actualChanges;
    }

    private void handleAdditionalSharesChargeTransactions(final ShareAccount account, final ShareAccountTransaction purchaseTransaction) {
        Set<ShareAccountCharge> charges = account.getCharges();
        BigDecimal totalChargeAmount = BigDecimal.ZERO;
        for (ShareAccountCharge charge : charges) {
            if (charge.isActive() && charge.isSharesPurchaseCharge()) {
                BigDecimal amount = charge.updateChargeDetailsForAdditionalSharesRequest(purchaseTransaction.amount(), account.getCurrency());
                ShareAccountChargePaidBy paidBy = new ShareAccountChargePaidBy(purchaseTransaction, charge, amount);
                purchaseTransaction.addShareAccountChargePaidBy(paidBy);
                totalChargeAmount = totalChargeAmount.add(amount);
            }
        }
        purchaseTransaction.updateChargeAmount(totalChargeAmount);
    }

    public Map<String, Object> validateAndApproveAddtionalShares(JsonCommand jsonCommand, ShareAccount account) {
        Map<String, Object> actualChanges = new HashMap<>();
        if (StringUtils.isBlank(jsonCommand.json())) { throw new InvalidJsonException(); }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, jsonCommand.json(),
                addtionalSharesParameters);
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("sharesaccount");
        JsonElement element = jsonCommand.parsedJson();
        final ArrayList<Long> purchasedShares = new ArrayList<>();
        if (this.fromApiJsonHelper.parameterExists(ShareAccountApiConstants.requestedshares_paramname, element)) {
            JsonArray array = this.fromApiJsonHelper.extractJsonArrayNamed(ShareAccountApiConstants.requestedshares_paramname, element);
            long totalShares = 0;
            for (JsonElement arrayElement : array) {
                final Long purchasedSharesId = this.fromApiJsonHelper.extractLongNamed(ShareAccountApiConstants.id_paramname, arrayElement);
                baseDataValidator.reset().parameter(ShareAccountApiConstants.requestedshares_paramname).value(purchasedSharesId).notBlank();
                ShareAccountTransaction transaction = account.retrievePurchasedShares(purchasedSharesId);
                if (transaction != null) {
                    validateTotalSubsribedShares(account, transaction, baseDataValidator) ;
                    totalShares += transaction.getTotalShares().longValue();
                    transaction.approve();
                    updateTotalChargeDerivedForAdditonalShares(account, transaction);
                }
                purchasedShares.add(purchasedSharesId);
            }
            if (totalShares > 0) {
                account.updateApprovedShares(new Long(totalShares));
            }
        }
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        actualChanges.put(ShareAccountApiConstants.requestedshares_paramname, purchasedShares);
        return actualChanges;
    }

    private void updateTotalChargeDerivedForAdditonalShares(final ShareAccount shareAccount, final ShareAccountTransaction transaction) {
        Set<ShareAccountChargePaidBy> paidBySet = transaction.getChargesPaidBy();
        if (paidBySet != null && !paidBySet.isEmpty()) {
            for (ShareAccountChargePaidBy chargePaidBy : paidBySet) {
                ShareAccountCharge charge = chargePaidBy.getCharge();
                if (charge.isActive() && charge.isSharesPurchaseCharge()) {
                    Money money = Money.of(shareAccount.getCurrency(), chargePaidBy.getAmount());
                    charge.updatePaidAmountBy(money);
                }
            }
            if (!transaction.isChargeTransaction()) {
                transaction.addAmountPaid(transaction.chargeAmount());
            }
        }
    }

    public Map<String, Object> validateAndRejectAddtionalShares(JsonCommand jsonCommand, ShareAccount account) {
        Map<String, Object> actualChanges = new HashMap<>();
        if (StringUtils.isBlank(jsonCommand.json())) { throw new InvalidJsonException(); }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, jsonCommand.json(),
                addtionalSharesParameters);
        JsonElement element = jsonCommand.parsedJson();
        final ArrayList<Long> purchasedShares = new ArrayList<>();
        if (this.fromApiJsonHelper.parameterExists(ShareAccountApiConstants.requestedshares_paramname, element)) {
            long totalShares = 0;
            JsonArray array = this.fromApiJsonHelper.extractJsonArrayNamed(ShareAccountApiConstants.requestedshares_paramname, element);
            for (JsonElement arrayElement : array) {
                final Long purchasedSharesId = this.fromApiJsonHelper.extractLongNamed(ShareAccountApiConstants.id_paramname, arrayElement);
                ShareAccountTransaction shares = account.retrievePurchasedShares(purchasedSharesId);
                if (shares != null) {
                    shares.reject();
                    updateTotalChargeDerivedForAdditonalSharesReject(account, shares);
                    totalShares += shares.getTotalShares().longValue();
                }
                purchasedShares.add(purchasedSharesId);
            }
            if (totalShares > 0) {
                account.removePendingShares(new Long(totalShares));
            }
        }
        actualChanges.put(ShareAccountApiConstants.requestedshares_paramname, purchasedShares);
        return actualChanges;
    }

    private void updateTotalChargeDerivedForAdditonalSharesReject(final ShareAccount shareAccount, final ShareAccountTransaction transaction) {
        Set<ShareAccountChargePaidBy> paidBySet = transaction.getChargesPaidBy();
        if (paidBySet != null && !paidBySet.isEmpty()) {
            for (ShareAccountChargePaidBy chargePaidBy : paidBySet) {
                ShareAccountCharge charge = chargePaidBy.getCharge();
                if (charge.isActive() && charge.isSharesPurchaseCharge()) {
                    Money money = Money.of(shareAccount.getCurrency(), chargePaidBy.getAmount());
                    charge.updatePaidAmountBy(money);
                }
            }
            if (!transaction.isChargeTransaction()) {
                transaction.addAmountPaid(transaction.chargeAmount());
            }
        }
    }

    public Map<String, Object> validateAndRedeemShares(JsonCommand jsonCommand, ShareAccount account) {
        Map<String, Object> actualChanges = new HashMap<>();
        if (StringUtils.isBlank(jsonCommand.json())) { throw new InvalidJsonException(); }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, jsonCommand.json(),
                addtionalSharesParameters);
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("sharesaccount");
        JsonElement element = jsonCommand.parsedJson();
        LocalDate requestedDate = this.fromApiJsonHelper.extractLocalDateNamed(ShareAccountApiConstants.requesteddate_paramname, element);
        baseDataValidator.reset().parameter(ShareAccountApiConstants.requesteddate_paramname).value(requestedDate).notNull();
        final Long sharesRequested = this.fromApiJsonHelper.extractLongNamed(ShareAccountApiConstants.requestedshares_paramname, element);
        baseDataValidator.reset().parameter(ShareAccountApiConstants.requestedshares_paramname).value(sharesRequested).notNull()
                .longGreaterThanZero();
        boolean isTransactionBeforeExistingTransactions = false ;
        Set<ShareAccountTransaction> transactions = account.getShareAccountTransactions() ;
        for(ShareAccountTransaction transaction: transactions) {
            if(!transaction.isChargeTransaction() && transaction.isActive()) {
                LocalDate transactionDate = new LocalDate(transaction.getPurchasedDate()) ;
                if(requestedDate.isBefore(transactionDate)) {
                    isTransactionBeforeExistingTransactions = true ;
                    break ;
                }    
            }
        }
        if(isTransactionBeforeExistingTransactions) {
            baseDataValidator.reset().parameter(ShareAccountApiConstants.requesteddate_paramname)
            .value(requestedDate)
            .failWithCodeNoParameterAddedToErrorCode("redeem.transaction.date.cannot.be.before.existing.transactions");
        }
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        BigDecimal unitPrice = account.getShareProduct().deriveMarketPrice(requestedDate.toDate()) ;
        ShareAccountTransaction transaction = ShareAccountTransaction.createRedeemTransaction(requestedDate.toDate(), sharesRequested,
                unitPrice);
        validateRedeemRequest(account, transaction, baseDataValidator, dataValidationErrors) ;
        account.addAdditionalPurchasedShares(transaction);
        actualChanges.put(ShareAccountApiConstants.requestedshares_paramname, transaction);
        
        handleRedeemSharesChargeTransactions(account, transaction);
        return actualChanges;
    }
    
    private void validateRedeemRequest(final ShareAccount account, ShareAccountTransaction redeemTransaction,
            final DataValidatorBuilder baseDataValidator,  final List<ApiParameterError> dataValidationErrors ) {
        
        if (account.getTotalApprovedShares() < redeemTransaction.getTotalShares()) {
            baseDataValidator.reset().parameter(ShareAccountApiConstants.requestedshares_paramname)
                    .value(redeemTransaction.getTotalShares())
                    .failWithCodeNoParameterAddedToErrorCode("cannot.be.redeemed.due.to.insufficient.shares");
        }
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        
        LocalDate redeemDate = new LocalDate(redeemTransaction.getPurchasedDate());
        final Integer lockinPeriod = account.getLockinPeriodFrequency();
        final PeriodFrequencyType periodType = account.getLockinPeriodFrequencyType();
        if (lockinPeriod == null && periodType == null) { return; }
        Long totalSharesCanBeRedeemed = new Long(0);
        Long totalSharesPurchasedBeforeRedeem = new Long(0) ;
        boolean isPurchaseTransactionExist = false ;
        
        Set<ShareAccountTransaction> transactions = account.getShareAccountTransactions();
        for (ShareAccountTransaction transaction : transactions) {
            if (transaction.isActive() && !transaction.isChargeTransaction()) {
                LocalDate purchaseDate = new LocalDate(transaction.getPurchasedDate());
                LocalDate lockinDate = deriveLockinPeriodDuration(lockinPeriod, periodType, purchaseDate);
                if (!lockinDate.isAfter(redeemDate)) {
                    if (transaction.isPurchasTransaction()) {
                        totalSharesCanBeRedeemed += transaction.getTotalShares();
                    } else if (transaction.isRedeemTransaction()) {
                        totalSharesCanBeRedeemed -= transaction.getTotalShares();
                    }
                }
                
                if(!purchaseDate.isAfter(redeemDate)) {
                    isPurchaseTransactionExist = true ;
                    if (transaction.isPurchasTransaction()) {
                        totalSharesPurchasedBeforeRedeem += transaction.getTotalShares();
                    } else if (transaction.isRedeemTransaction()) {
                        totalSharesPurchasedBeforeRedeem -= transaction.getTotalShares();
                    }
                }
            }
        }
        if(!isPurchaseTransactionExist) {
            baseDataValidator.reset().parameter(ShareAccountApiConstants.requesteddate_paramname)
            .value(redeemDate)
            .failWithCodeNoParameterAddedToErrorCode("no.purchase.transaction.found.before.redeem.date");
        }else if (totalSharesPurchasedBeforeRedeem < redeemTransaction.getTotalShares()) {
            baseDataValidator.reset().parameter(ShareAccountApiConstants.requestedshares_paramname)
            .value(redeemTransaction.getTotalShares())
            .failWithCodeNoParameterAddedToErrorCode("cannot.be.redeemed.due.to.insufficient.shares.for.this.redeem.date");
        }else if (totalSharesCanBeRedeemed < redeemTransaction.getTotalShares()) {
            baseDataValidator.reset().parameter(ShareAccountApiConstants.requestedshares_paramname)
                    .value(redeemTransaction.getTotalShares())
                    .failWithCodeNoParameterAddedToErrorCode("cannot.be.redeemed.due.to.lockinperiod");
        }
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }
    
    private LocalDate deriveLockinPeriodDuration(final Integer lockinPeriod, final PeriodFrequencyType periodType, LocalDate purchaseDate) {
        LocalDate lockinDate = purchaseDate ;
        if(periodType != null) {
            switch(periodType) {
                case INVALID: //It never comes in to this state.
                    break ;
                case DAYS:
                    lockinDate = purchaseDate.plusDays(lockinPeriod) ;
                    break ;
                case WEEKS:
                    lockinDate = purchaseDate.plusWeeks(lockinPeriod) ;
                    break ;
                case MONTHS:
                    lockinDate = purchaseDate.plusMonths(lockinPeriod) ;
                    break ;
                case YEARS:
                    lockinDate = purchaseDate.plusYears(lockinPeriod) ;
                    break ;
            }    
        }
        return lockinDate ;
    }
    
    private void handleRedeemSharesChargeTransactions(final ShareAccount account, final ShareAccountTransaction transaction) {
        Set<ShareAccountCharge> charges = account.getCharges();
        BigDecimal totalChargeAmount = BigDecimal.ZERO;
        for (ShareAccountCharge charge : charges) {
            if (charge.isActive() && charge.isSharesRedeemCharge()) {
                BigDecimal amount = charge.updateChargeDetailsForAdditionalSharesRequest(transaction.amount(), account.getCurrency());
                ShareAccountChargePaidBy paidBy = new ShareAccountChargePaidBy(transaction, charge, amount);
                transaction.addShareAccountChargePaidBy(paidBy);
                totalChargeAmount = totalChargeAmount.add(amount);
            }
        }
        transaction.deductChargesFromTotalAmount(totalChargeAmount);
        Set<ShareAccountChargePaidBy> paidBySet = transaction.getChargesPaidBy();
        if (paidBySet != null && !paidBySet.isEmpty()) {
            for (ShareAccountChargePaidBy chargePaidBy : paidBySet) {
                ShareAccountCharge charge = chargePaidBy.getCharge();
                if (charge.isActive() && charge.isSharesRedeemCharge()) {
                    Money money = Money.of(account.getCurrency(), chargePaidBy.getAmount());
                    charge.updatePaidAmountBy(money);
                }
            }
        }
        transaction.addAmountPaid(transaction.chargeAmount());
    }

    public Map<String, Object> validateAndClose(JsonCommand jsonCommand, ShareAccount account) {
        Map<String, Object> actualChanges = new HashMap<>();
        if (StringUtils.isBlank(jsonCommand.json())) { throw new InvalidJsonException(); }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, jsonCommand.json(), closeParameters);
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("sharesaccount");
        JsonElement element = jsonCommand.parsedJson();
        LocalDate closedDate = this.fromApiJsonHelper.extractLocalDateNamed(ShareAccountApiConstants.closeddate_paramname, element);
        baseDataValidator.reset().parameter(ShareAccountApiConstants.approveddate_paramname).value(closedDate).notNull();
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        boolean isTransactionBeforeExistingTransactions = false ;
        Set<ShareAccountTransaction> transactions = account.getShareAccountTransactions() ;
        for(ShareAccountTransaction transaction: transactions) {
            if(!transaction.isChargeTransaction()) {
                LocalDate transactionDate = new LocalDate(transaction.getPurchasedDate()) ;
                if(closedDate.isBefore(transactionDate)) {
                    isTransactionBeforeExistingTransactions = true ;
                    break ;
                }    
            }
        }
        if(isTransactionBeforeExistingTransactions) {
            baseDataValidator.reset().parameter(ShareAccountApiConstants.closeddate_paramname)
            .value(closedDate)
            .failWithCodeNoParameterAddedToErrorCode("share.account.cannot.be.closed.before.existing.transactions");
        }
        
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        
        AppUser approvedUser = this.platformSecurityContext.authenticatedUser();
        final BigDecimal unitPrice = account.getShareProduct().deriveMarketPrice(DateUtils.getDateOfTenant());
        ShareAccountTransaction transaction = ShareAccountTransaction.createRedeemTransaction(closedDate.toDate(),
                account.getTotalApprovedShares(), unitPrice);
        account.addAdditionalPurchasedShares(transaction);
        account.close(closedDate.toDate(), approvedUser);
        handleRedeemSharesChargeTransactions(account, transaction);
        actualChanges.put(ShareAccountApiConstants.requestedshares_paramname, transaction);
        return actualChanges;
    }
}
