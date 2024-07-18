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
package org.apache.fineract.portfolio.savings.domain;

import static org.apache.fineract.portfolio.savings.SavingsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.accountNoParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.allowOverdraftParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.clientIdParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.enforceMinRequiredBalanceParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.externalIdParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.fieldOfficerIdParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.groupIdParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.interestCalculationDaysInYearTypeParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.interestCalculationTypeParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.interestCompoundingPeriodTypeParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.interestPostingPeriodTypeParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.lienAllowedParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.lockinPeriodFrequencyParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.lockinPeriodFrequencyTypeParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.maxAllowedLienLimitParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.minOverdraftForInterestCalculationParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.minRequiredBalanceParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.minRequiredOpeningBalanceParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.nominalAnnualInterestRateOverdraftParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.nominalAnnualInterestRateParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.overdraftLimitParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.productIdParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.submittedOnDateParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.withHoldTaxParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.withdrawalFeeForTransfersParamName;

import com.google.gson.JsonElement;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.exception.UnsupportedParameterException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.organisation.staff.domain.StaffRepositoryWrapper;
import org.apache.fineract.portfolio.account.service.AccountTransfersReadPlatformService;
import org.apache.fineract.portfolio.accountdetails.domain.AccountType;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.client.exception.ClientNotActiveException;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.domain.GroupRepositoryWrapper;
import org.apache.fineract.portfolio.group.exception.CenterNotActiveException;
import org.apache.fineract.portfolio.group.exception.ClientNotInGroupException;
import org.apache.fineract.portfolio.group.exception.GroupNotActiveException;
import org.apache.fineract.portfolio.savings.SavingsCompoundingInterestPeriodType;
import org.apache.fineract.portfolio.savings.SavingsInterestCalculationDaysInYearType;
import org.apache.fineract.portfolio.savings.SavingsInterestCalculationType;
import org.apache.fineract.portfolio.savings.SavingsPeriodFrequencyType;
import org.apache.fineract.portfolio.savings.SavingsPostingInterestPeriodType;
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionData;
import org.apache.fineract.portfolio.savings.exception.SavingsProductNotFoundException;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SavingsAccountAssembler {

    private final SavingsAccountTransactionSummaryWrapper savingsAccountTransactionSummaryWrapper;
    private final SavingsAccountTransactionDataSummaryWrapper savingsAccountTransactionDataSummaryWrapper;
    private final SavingsHelper savingsHelper;
    private final ClientRepositoryWrapper clientRepository;
    private final GroupRepositoryWrapper groupRepository;
    private final StaffRepositoryWrapper staffRepository;
    private final SavingsProductRepository savingProductRepository;
    private final SavingsAccountRepositoryWrapper savingsAccountRepository;
    private final SavingsAccountChargeAssembler savingsAccountChargeAssembler;
    private final FromJsonHelper fromApiJsonHelper;
    private final ConfigurationDomainService configurationDomainService;
    private final ExternalIdFactory externalIdFactory;

    @Autowired
    public SavingsAccountAssembler(final SavingsAccountTransactionSummaryWrapper savingsAccountTransactionSummaryWrapper,
            final SavingsAccountTransactionDataSummaryWrapper savingsAccountTransactionDataSummaryWrapper,
            final ClientRepositoryWrapper clientRepository, final GroupRepositoryWrapper groupRepository,
            final StaffRepositoryWrapper staffRepository, final SavingsProductRepository savingProductRepository,
            final SavingsAccountRepositoryWrapper savingsAccountRepository,
            final SavingsAccountChargeAssembler savingsAccountChargeAssembler, final FromJsonHelper fromApiJsonHelper,
            final AccountTransfersReadPlatformService accountTransfersReadPlatformService,
            final ConfigurationDomainService configurationDomainService, ExternalIdFactory externalIdFactory) {
        this.savingsAccountTransactionSummaryWrapper = savingsAccountTransactionSummaryWrapper;
        this.savingsAccountTransactionDataSummaryWrapper = savingsAccountTransactionDataSummaryWrapper;
        this.clientRepository = clientRepository;
        this.groupRepository = groupRepository;
        this.staffRepository = staffRepository;
        this.savingProductRepository = savingProductRepository;
        this.savingsAccountRepository = savingsAccountRepository;
        this.savingsAccountChargeAssembler = savingsAccountChargeAssembler;
        this.fromApiJsonHelper = fromApiJsonHelper;
        savingsHelper = new SavingsHelper(accountTransfersReadPlatformService);
        this.configurationDomainService = configurationDomainService;
        this.externalIdFactory = externalIdFactory;
    }

    /**
     * Assembles a new {@link SavingsAccount} from JSON details passed in request inheriting details where relevant from
     * chosen {@link SavingsProduct}.
     */
    public SavingsAccount assembleFrom(final JsonCommand command, final AppUser submittedBy) {

        final JsonElement element = command.parsedJson();

        final String accountNo = this.fromApiJsonHelper.extractStringNamed(accountNoParamName, element);
        final String externalId = this.fromApiJsonHelper.extractStringNamed(externalIdParamName, element);
        final Long productId = this.fromApiJsonHelper.extractLongNamed(productIdParamName, element);

        final SavingsProduct product = this.savingProductRepository.findById(productId)
                .orElseThrow(() -> new SavingsProductNotFoundException(productId));

        Client client = null;
        Group group = null;
        Staff fieldOfficer = null;
        AccountType accountType = AccountType.INVALID;

        final Long clientId = this.fromApiJsonHelper.extractLongNamed(clientIdParamName, element);
        if (clientId != null) {
            client = this.clientRepository.findOneWithNotFoundDetection(clientId);
            accountType = AccountType.INDIVIDUAL;
            if (client.isNotActive()) {
                throw new ClientNotActiveException(clientId);
            }
        }

        final Long groupId = this.fromApiJsonHelper.extractLongNamed(groupIdParamName, element);
        if (groupId != null) {
            group = this.groupRepository.findOneWithNotFoundDetection(groupId);
            accountType = AccountType.GROUP;
            if (group.isNotActive()) {
                if (group.isCenter()) {
                    throw new CenterNotActiveException(groupId);
                }
                throw new GroupNotActiveException(groupId);
            }
        }

        if (group != null && client != null) {
            if (!group.hasClientAsMember(client)) {
                throw new ClientNotInGroupException(clientId, groupId);
            }
            accountType = AccountType.JLG;
        }

        if ((Boolean) command.booleanPrimitiveValueOfParameterNamed("isGSIM") != null) {
            if (command.booleanPrimitiveValueOfParameterNamed("isGSIM")) {
                accountType = AccountType.GSIM;
            }
        }

        final Long fieldOfficerId = this.fromApiJsonHelper.extractLongNamed(fieldOfficerIdParamName, element);
        if (fieldOfficerId != null) {
            fieldOfficer = this.staffRepository.findOneWithNotFoundDetection(fieldOfficerId);
        }

        final LocalDate submittedOnDate = this.fromApiJsonHelper.extractLocalDateNamed(submittedOnDateParamName, element);

        BigDecimal interestRate = null;
        if (command.parameterExists(nominalAnnualInterestRateParamName)) {
            interestRate = command.bigDecimalValueOfParameterNamed(nominalAnnualInterestRateParamName);
        } else {
            interestRate = product.nominalAnnualInterestRate();
        }

        SavingsCompoundingInterestPeriodType interestCompoundingPeriodType = null;
        final Integer interestPeriodTypeValue = command.integerValueOfParameterNamed(interestCompoundingPeriodTypeParamName);
        if (interestPeriodTypeValue != null) {
            interestCompoundingPeriodType = SavingsCompoundingInterestPeriodType.fromInt(interestPeriodTypeValue);
        } else {
            interestCompoundingPeriodType = product.interestCompoundingPeriodType();
        }

        SavingsPostingInterestPeriodType interestPostingPeriodType = null;
        final Integer interestPostingPeriodTypeValue = command.integerValueOfParameterNamed(interestPostingPeriodTypeParamName);
        if (interestPostingPeriodTypeValue != null) {
            interestPostingPeriodType = SavingsPostingInterestPeriodType.fromInt(interestPostingPeriodTypeValue);
        } else {
            interestPostingPeriodType = product.interestPostingPeriodType();
        }

        SavingsInterestCalculationType interestCalculationType = null;
        final Integer interestCalculationTypeValue = command.integerValueOfParameterNamed(interestCalculationTypeParamName);
        if (interestCalculationTypeValue != null) {
            interestCalculationType = SavingsInterestCalculationType.fromInt(interestCalculationTypeValue);
        } else {
            interestCalculationType = product.interestCalculationType();
        }

        SavingsInterestCalculationDaysInYearType interestCalculationDaysInYearType = null;
        final Integer interestCalculationDaysInYearTypeValue = command
                .integerValueOfParameterNamed(interestCalculationDaysInYearTypeParamName);
        if (interestCalculationDaysInYearTypeValue != null) {
            interestCalculationDaysInYearType = SavingsInterestCalculationDaysInYearType.fromInt(interestCalculationDaysInYearTypeValue);
        } else {
            interestCalculationDaysInYearType = product.interestCalculationDaysInYearType();
        }

        BigDecimal minRequiredOpeningBalance = null;
        if (command.parameterExists(minRequiredOpeningBalanceParamName)) {
            minRequiredOpeningBalance = command.bigDecimalValueOfParameterNamed(minRequiredOpeningBalanceParamName);
        } else {
            minRequiredOpeningBalance = product.minRequiredOpeningBalance();
        }

        Integer lockinPeriodFrequency = null;
        if (command.parameterExists(lockinPeriodFrequencyParamName)) {
            lockinPeriodFrequency = command.integerValueOfParameterNamed(lockinPeriodFrequencyParamName);
        } else {
            lockinPeriodFrequency = product.lockinPeriodFrequency();
        }

        SavingsPeriodFrequencyType lockinPeriodFrequencyType = null;
        Integer lockinPeriodFrequencyTypeValue = null;
        if (command.parameterExists(lockinPeriodFrequencyTypeParamName)) {
            lockinPeriodFrequencyTypeValue = command.integerValueOfParameterNamed(lockinPeriodFrequencyTypeParamName);
            if (lockinPeriodFrequencyTypeValue != null) {
                lockinPeriodFrequencyType = SavingsPeriodFrequencyType.fromInt(lockinPeriodFrequencyTypeValue);
            }
        } else {
            lockinPeriodFrequencyType = product.lockinPeriodFrequencyType();
        }
        boolean iswithdrawalFeeApplicableForTransfer = false;
        if (command.parameterExists(withdrawalFeeForTransfersParamName)) {
            iswithdrawalFeeApplicableForTransfer = command.booleanPrimitiveValueOfParameterNamed(withdrawalFeeForTransfersParamName);
        }

        final Set<SavingsAccountCharge> charges = this.savingsAccountChargeAssembler.fromParsedJson(element, product.currency().getCode());

        boolean allowOverdraft = false;
        if (command.parameterExists(allowOverdraftParamName)) {
            allowOverdraft = command.booleanPrimitiveValueOfParameterNamed(allowOverdraftParamName);
        } else {
            allowOverdraft = product.isAllowOverdraft();
        }

        BigDecimal overdraftLimit = BigDecimal.ZERO;
        if (command.parameterExists(overdraftLimitParamName)) {
            overdraftLimit = command.bigDecimalValueOfParameterNamedDefaultToNullIfZero(overdraftLimitParamName);
        } else {
            overdraftLimit = product.overdraftLimit();
        }

        BigDecimal nominalAnnualInterestRateOverdraft = BigDecimal.ZERO;
        if (command.parameterExists(nominalAnnualInterestRateOverdraftParamName)) {
            nominalAnnualInterestRateOverdraft = command
                    .bigDecimalValueOfParameterNamedDefaultToNullIfZero(nominalAnnualInterestRateOverdraftParamName);
        } else {
            nominalAnnualInterestRateOverdraft = product.nominalAnnualInterestRateOverdraft();
        }

        BigDecimal minOverdraftForInterestCalculation = BigDecimal.ZERO;
        if (command.parameterExists(minOverdraftForInterestCalculationParamName)) {
            minOverdraftForInterestCalculation = command
                    .bigDecimalValueOfParameterNamedDefaultToNullIfZero(minOverdraftForInterestCalculationParamName);
        } else {
            minOverdraftForInterestCalculation = product.minOverdraftForInterestCalculation();
        }

        boolean enforceMinRequiredBalance = false;
        if (command.parameterExists(enforceMinRequiredBalanceParamName)) {
            enforceMinRequiredBalance = command.booleanPrimitiveValueOfParameterNamed(enforceMinRequiredBalanceParamName);
        } else {
            enforceMinRequiredBalance = product.isMinRequiredBalanceEnforced();
        }

        BigDecimal minRequiredBalance = BigDecimal.ZERO;
        if (command.parameterExists(minRequiredBalanceParamName)) {
            minRequiredBalance = command.bigDecimalValueOfParameterNamedDefaultToNullIfZero(minRequiredBalanceParamName);
        } else {
            minRequiredBalance = product.minRequiredBalance();
        }

        boolean lienAllowed = false;
        if (command.parameterExists(lienAllowedParamName)) {
            lienAllowed = command.booleanPrimitiveValueOfParameterNamed(lienAllowedParamName);
        } else {
            lienAllowed = product.isLienAllowed();
        }

        BigDecimal maxAllowedLienLimit = BigDecimal.ZERO;
        if (command.parameterExists(maxAllowedLienLimitParamName)) {
            maxAllowedLienLimit = command.bigDecimalValueOfParameterNamedDefaultToNullIfZero(maxAllowedLienLimitParamName);
        } else {
            maxAllowedLienLimit = product.maxAllowedLienLimit();
        }

        boolean withHoldTax = product.withHoldTax();
        if (command.parameterExists(withHoldTaxParamName)) {
            withHoldTax = command.booleanPrimitiveValueOfParameterNamed(withHoldTaxParamName);
            if (withHoldTax && product.getTaxGroup() == null) {
                throw new UnsupportedParameterException(Arrays.asList(withHoldTaxParamName));
            }
        }

        final SavingsAccount account = SavingsAccount.createNewApplicationForSubmittal(client, group, product, fieldOfficer, accountNo,
                externalIdFactory.create(externalId), accountType, submittedOnDate, submittedBy, interestRate,
                interestCompoundingPeriodType, interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType,
                minRequiredOpeningBalance, lockinPeriodFrequency, lockinPeriodFrequencyType, iswithdrawalFeeApplicableForTransfer, charges,
                allowOverdraft, overdraftLimit, enforceMinRequiredBalance, minRequiredBalance, maxAllowedLienLimit, lienAllowed,
                nominalAnnualInterestRateOverdraft, minOverdraftForInterestCalculation, withHoldTax);
        account.setHelpers(this.savingsAccountTransactionSummaryWrapper, this.savingsHelper);

        account.validateNewApplicationState(SAVINGS_ACCOUNT_RESOURCE_NAME);

        account.validateAccountValuesWithProduct();

        return account;
    }

    public SavingsAccount assembleFrom(final Long savingsId, final boolean backdatedTxnsAllowedTill) {
        SavingsAccount account = this.savingsAccountRepository.findSavingsWithNotFoundDetection(savingsId, backdatedTxnsAllowedTill);
        return loadTransactionsToSavingsAccount(account, backdatedTxnsAllowedTill);
    }

    public SavingsAccount loadTransactionsToSavingsAccount(final SavingsAccount account, final boolean backdatedTxnsAllowedTill) {
        List<SavingsAccountTransaction> savingsAccountTransactions = null;
        if (backdatedTxnsAllowedTill) {
            LocalDate pivotDate = account.getSummary().getInterestPostedTillDate();
            boolean isNotPresent = pivotDate == null;
            if (!isNotPresent) {
                // Get savings account transactions
                if (isRelaxingDaysConfigForPivotDateEnabled()) {
                    final Long relaxingDaysForPivotDate = this.configurationDomainService.retrieveRelaxingDaysConfigForPivotDate();
                    LocalDate interestPostedTillDate = account.getSummary().getInterestPostedTillDate();
                    savingsAccountTransactions = this.savingsAccountRepository.findTransactionsAfterPivotDate(account,
                            interestPostedTillDate.minusDays(relaxingDaysForPivotDate));

                    savingsAccountTransactions.get(0).getSavingsAccount()
                            .setStartInterestCalculationDate(interestPostedTillDate.minusDays(relaxingDaysForPivotDate));
                    List<SavingsAccountTransaction> pivotDateTransaction = this.savingsAccountRepository
                            .findTransactionRunningBalanceBeforePivotDate(account,
                                    interestPostedTillDate.minusDays(relaxingDaysForPivotDate + 1));
                    if (pivotDateTransaction != null && !pivotDateTransaction.isEmpty()) {
                        account.getSummary().setRunningBalanceOnInterestPostingTillDate(pivotDateTransaction
                                .get(pivotDateTransaction.size() - 1).getRunningBalance(account.getCurrency()).getAmount());
                    }
                } else {
                    savingsAccountTransactions = this.savingsAccountRepository.findTransactionsAfterPivotDate(account,
                            account.getSummary().getInterestPostedTillDate());
                }

                if (savingsAccountTransactions != null && savingsAccountTransactions.size() > 0) {
                    // Update transient variable
                    account.setSavingsAccountTransactions(savingsAccountTransactions);
                }
            } else {
                savingsAccountTransactions = this.savingsAccountRepository.findAllTransactions(account);
                account.setSavingsAccountTransactions(savingsAccountTransactions);
            }
        }

        account.setHelpers(this.savingsAccountTransactionSummaryWrapper, this.savingsHelper);
        return account;
    }

    public SavingsAccountData assembleSavings(final SavingsAccountData account) {

        // Update last running balance on account level
        final boolean backdatedTxnsAllowedTill = this.configurationDomainService.retrievePivotDateConfig();
        if (backdatedTxnsAllowedTill && account.getSavingsAccountTransactionData() != null
                && account.getSummary().getInterestPostedTillDate() != null) {
            List<SavingsAccountTransactionData> removalList = new ArrayList<>();

            for (int i = 0; i < account.getSavingsAccountTransactionData().size(); i++) {
                SavingsAccountTransactionData savingsAccountTransaction = account.getSavingsAccountTransactionData().get(i);
                removalList.add(savingsAccountTransaction);
                if ((savingsAccountTransaction.isInterestPostingAndNotReversed()
                        || savingsAccountTransaction.isOverdraftInterestAndNotReversed())
                        && !savingsAccountTransaction.isReversalTransaction()) {
                    account.getSummary().setRunningBalanceOnPivotDate(savingsAccountTransaction.getRunningBalance());
                    account.setLastSavingsAccountTransaction(savingsAccountTransaction);
                    break;
                }
            }
            account.getSavingsAccountTransactionData().removeAll(removalList);
        } else {
            account.getSummary().setRunningBalanceOnPivotDate(BigDecimal.ZERO);
        }
        account.setHelpers(this.savingsAccountTransactionDataSummaryWrapper, this.savingsHelper);
        return account;
    }

    public boolean getPivotConfigStatus() {
        return this.configurationDomainService.retrievePivotDateConfig();
    }

    public boolean isRelaxingDaysConfigForPivotDateEnabled() {
        return this.configurationDomainService.isRelaxingDaysConfigForPivotDateEnabled();
    }

    public void setHelpers(final SavingsAccount account) {
        account.setHelpers(this.savingsAccountTransactionSummaryWrapper, this.savingsHelper);
    }

    /**
     * Assembles a new {@link SavingsAccount} from JSON details passed in request inheriting details where relevant from
     * chosen {@link SavingsProduct}.
     */
    public SavingsAccount assembleFrom(final Client client, final Group group, final Long productId, final LocalDate appliedonDate,
            final AppUser appliedBy) {

        AccountType accountType = AccountType.INVALID;
        if (client != null) {
            accountType = AccountType.INDIVIDUAL;
            if (client.isNotActive()) {
                throw new ClientNotActiveException(client.getId());
            }
        }

        if (group != null) {
            accountType = AccountType.GROUP;
            if (group.isNotActive()) {
                if (group.isCenter()) {
                    throw new CenterNotActiveException(group.getId());
                }
                throw new GroupNotActiveException(group.getId());
            }
        }

        if (group != null && client != null) {
            if (!group.hasClientAsMember(client)) {
                throw new ClientNotInGroupException(client.getId(), group.getId());
            }
            accountType = AccountType.JLG;
        }
        final SavingsProduct product = this.savingProductRepository.findById(productId).orElseThrow();
        final Set<SavingsAccountCharge> charges = this.savingsAccountChargeAssembler.fromSavingsProduct(product);
        final SavingsAccount account = SavingsAccount.createNewApplicationForSubmittal(client, group, product, null, null, null,
                accountType, appliedonDate, appliedBy, product.nominalAnnualInterestRate(), product.interestCompoundingPeriodType(),
                product.interestPostingPeriodType(), product.interestCalculationType(), product.interestCalculationDaysInYearType(),
                product.minRequiredOpeningBalance(), product.lockinPeriodFrequency(), product.lockinPeriodFrequencyType(),
                product.isWithdrawalFeeApplicableForTransfer(), charges, product.isAllowOverdraft(), product.overdraftLimit(),
                product.isMinRequiredBalanceEnforced(), product.minRequiredBalance(), product.maxAllowedLienLimit(),
                product.isLienAllowed(), product.nominalAnnualInterestRateOverdraft(), product.minOverdraftForInterestCalculation(),
                product.withHoldTax());
        account.setHelpers(this.savingsAccountTransactionSummaryWrapper, this.savingsHelper);

        account.validateNewApplicationState(SAVINGS_ACCOUNT_RESOURCE_NAME);

        account.validateAccountValuesWithProduct();

        return account;
    }

    public void assignSavingAccountHelpers(final SavingsAccount savingsAccount) {
        savingsAccount.setHelpers(this.savingsAccountTransactionSummaryWrapper, this.savingsHelper);
    }

    public void assignSavingAccountHelpers(final SavingsAccountData savingsAccountData) {
        savingsAccountData.setHelpers(this.savingsAccountTransactionDataSummaryWrapper, this.savingsHelper);
    }
}
