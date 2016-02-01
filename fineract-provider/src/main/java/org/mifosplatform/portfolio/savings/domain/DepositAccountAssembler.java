/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

import static org.mifosplatform.portfolio.collectionsheet.CollectionSheetConstants.bulkSavingsDueTransactionsParamName;
import static org.mifosplatform.portfolio.collectionsheet.CollectionSheetConstants.savingsIdParamName;
import static org.mifosplatform.portfolio.collectionsheet.CollectionSheetConstants.transactionAmountParamName;
import static org.mifosplatform.portfolio.collectionsheet.CollectionSheetConstants.transactionDateParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.adjustAdvanceTowardsFuturePaymentsParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.allowWithdrawalParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.chartIdParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.depositAmountParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.depositPeriodFrequencyIdParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.depositPeriodParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.expectedFirstDepositOnDateParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.isCalendarInheritedParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.isMandatoryDepositParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.mandatoryRecommendedDepositAmountParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.transferInterestToSavingsParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.accountNoParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.clientIdParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.externalIdParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.fieldOfficerIdParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.groupIdParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.interestCalculationDaysInYearTypeParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.interestCalculationTypeParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.interestCompoundingPeriodTypeParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.interestPostingPeriodTypeParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.lockinPeriodFrequencyParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.lockinPeriodFrequencyTypeParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.minRequiredOpeningBalanceParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.nominalAnnualInterestRateParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.productIdParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.submittedOnDateParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.withdrawalFeeForTransfersParamName;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.staff.domain.Staff;
import org.mifosplatform.organisation.staff.domain.StaffRepositoryWrapper;
import org.mifosplatform.portfolio.account.service.AccountTransfersReadPlatformService;
import org.mifosplatform.portfolio.accountdetails.domain.AccountType;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepositoryWrapper;
import org.mifosplatform.portfolio.client.exception.ClientNotActiveException;
import org.mifosplatform.portfolio.group.domain.Group;
import org.mifosplatform.portfolio.group.domain.GroupRepositoryWrapper;
import org.mifosplatform.portfolio.group.exception.CenterNotActiveException;
import org.mifosplatform.portfolio.group.exception.ClientNotInGroupException;
import org.mifosplatform.portfolio.group.exception.GroupNotActiveException;
import org.mifosplatform.portfolio.interestratechart.domain.InterestRateChart;
import org.mifosplatform.portfolio.paymentdetail.domain.PaymentDetail;
import org.mifosplatform.portfolio.paymentdetail.domain.PaymentDetailAssembler;
import org.mifosplatform.portfolio.savings.DepositAccountOnClosureType;
import org.mifosplatform.portfolio.savings.DepositAccountType;
import org.mifosplatform.portfolio.savings.SavingsCompoundingInterestPeriodType;
import org.mifosplatform.portfolio.savings.SavingsInterestCalculationDaysInYearType;
import org.mifosplatform.portfolio.savings.SavingsInterestCalculationType;
import org.mifosplatform.portfolio.savings.SavingsPeriodFrequencyType;
import org.mifosplatform.portfolio.savings.SavingsPostingInterestPeriodType;
import org.mifosplatform.portfolio.savings.data.SavingsAccountTransactionDTO;
import org.mifosplatform.portfolio.savings.exception.FixedDepositProductNotFoundException;
import org.mifosplatform.portfolio.savings.exception.RecurringDepositProductNotFoundException;
import org.mifosplatform.portfolio.savings.exception.SavingsProductNotFoundException;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Service
public class DepositAccountAssembler {

    private final PlatformSecurityContext context;
    private final SavingsAccountTransactionSummaryWrapper savingsAccountTransactionSummaryWrapper;
    private final SavingsHelper savingsHelper;
    private final ClientRepositoryWrapper clientRepository;
    private final GroupRepositoryWrapper groupRepository;
    private final StaffRepositoryWrapper staffRepository;
    private final FixedDepositProductRepository fixedDepositProductRepository;
    private final RecurringDepositProductRepository recurringDepositProductRepository;
    private final SavingsAccountRepositoryWrapper savingsAccountRepository;
    private final SavingsAccountChargeAssembler savingsAccountChargeAssembler;
    private final FromJsonHelper fromApiJsonHelper;
    private final DepositProductAssembler depositProductAssembler;
    private final PaymentDetailAssembler paymentDetailAssembler;

    @Autowired
    public DepositAccountAssembler(final SavingsAccountTransactionSummaryWrapper savingsAccountTransactionSummaryWrapper,
            final ClientRepositoryWrapper clientRepository, final GroupRepositoryWrapper groupRepository,
            final StaffRepositoryWrapper staffRepository, final FixedDepositProductRepository fixedDepositProductRepository,
            final SavingsAccountRepositoryWrapper savingsAccountRepository,
            final SavingsAccountChargeAssembler savingsAccountChargeAssembler, final FromJsonHelper fromApiJsonHelper,
            final DepositProductAssembler depositProductAssembler,
            final RecurringDepositProductRepository recurringDepositProductRepository,
            final AccountTransfersReadPlatformService accountTransfersReadPlatformService, final PlatformSecurityContext context,
            final PaymentDetailAssembler paymentDetailAssembler) {

        this.savingsAccountTransactionSummaryWrapper = savingsAccountTransactionSummaryWrapper;
        this.clientRepository = clientRepository;
        this.groupRepository = groupRepository;
        this.staffRepository = staffRepository;
        this.fixedDepositProductRepository = fixedDepositProductRepository;
        this.savingsAccountRepository = savingsAccountRepository;
        this.savingsAccountChargeAssembler = savingsAccountChargeAssembler;
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.depositProductAssembler = depositProductAssembler;
        this.recurringDepositProductRepository = recurringDepositProductRepository;
        this.savingsHelper = new SavingsHelper(accountTransfersReadPlatformService);
        this.context = context;
        this.paymentDetailAssembler = paymentDetailAssembler;
    }

    /**
     * Assembles a new {@link SavingsAccount} from JSON details passed in
     * request inheriting details where relevant from chosen
     * {@link SavingsProduct}.
     */
    public SavingsAccount assembleFrom(final JsonCommand command, final AppUser submittedBy, final DepositAccountType depositAccountType) {

        final JsonElement element = command.parsedJson();

        final String accountNo = this.fromApiJsonHelper.extractStringNamed(accountNoParamName, element);
        final String externalId = this.fromApiJsonHelper.extractStringNamed(externalIdParamName, element);
        final Long productId = this.fromApiJsonHelper.extractLongNamed(productIdParamName, element);

        SavingsProduct product = null;
        if (depositAccountType.isFixedDeposit()) {
            product = this.fixedDepositProductRepository.findOne(productId);
            if (product == null) { throw new FixedDepositProductNotFoundException(productId); }
        } else if (depositAccountType.isRecurringDeposit()) {
            product = this.recurringDepositProductRepository.findOne(productId);
            if (product == null) { throw new RecurringDepositProductNotFoundException(productId); }
        }

        if (product == null) { throw new SavingsProductNotFoundException(productId); }

        Client client = null;
        Group group = null;
        Staff fieldOfficer = null;
        AccountType accountType = AccountType.INVALID;
        final Long clientId = this.fromApiJsonHelper.extractLongNamed(clientIdParamName, element);
        if (clientId != null) {
            client = this.clientRepository.findOneWithNotFoundDetection(clientId);
            accountType = AccountType.INDIVIDUAL;
            if (client.isNotActive()) { throw new ClientNotActiveException(clientId); }
        }

        final Long groupId = this.fromApiJsonHelper.extractLongNamed(groupIdParamName, element);
        if (groupId != null) {
            group = this.groupRepository.findOneWithNotFoundDetection(groupId);
            accountType = AccountType.GROUP;
        }

        if (group != null && client != null) {
            if (!group.hasClientAsMember(client)) { throw new ClientNotInGroupException(clientId, groupId); }
            accountType = AccountType.JLG;
            if (group.isNotActive()) {
                if (group.isCenter()) { throw new CenterNotActiveException(groupId); }
                throw new GroupNotActiveException(groupId);
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

        if (command.parameterExists(lockinPeriodFrequencyTypeParamName)) {
            Integer lockinPeriodFrequencyTypeValue = null;
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

        DepositAccountInterestRateChart accountChart = null;
        InterestRateChart productChart = null;

        if (command.parameterExists(chartIdParamName)) {
            Long chartId = command.longValueOfParameterNamed(chartIdParamName);
            productChart = product.findChart(chartId);

        } else {
            productChart = product.applicableChart(submittedOnDate);
        }

        if (productChart != null) {
            accountChart = DepositAccountInterestRateChart.from(productChart);
        }

        SavingsAccount account = null;
        if (depositAccountType.isFixedDeposit()) {
            final DepositProductTermAndPreClosure prodTermAndPreClosure = ((FixedDepositProduct) product).depositProductTermAndPreClosure();
            final DepositAccountTermAndPreClosure accountTermAndPreClosure = this.assembleAccountTermAndPreClosure(command,
                    prodTermAndPreClosure);

            FixedDepositAccount fdAccount = FixedDepositAccount.createNewApplicationForSubmittal(client, group, product, fieldOfficer,
                    accountNo, externalId, accountType, submittedOnDate, submittedBy, interestRate, interestCompoundingPeriodType,
                    interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType, minRequiredOpeningBalance,
                    lockinPeriodFrequency, lockinPeriodFrequencyType, iswithdrawalFeeApplicableForTransfer, charges,
                    accountTermAndPreClosure, accountChart);
            accountTermAndPreClosure.updateAccountReference(fdAccount);
            fdAccount.validateDomainRules();
            account = fdAccount;
        } else if (depositAccountType.isRecurringDeposit()) {
            final DepositProductTermAndPreClosure prodTermAndPreClosure = ((RecurringDepositProduct) product)
                    .depositProductTermAndPreClosure();
            final DepositAccountTermAndPreClosure accountTermAndPreClosure = this.assembleAccountTermAndPreClosure(command,
                    prodTermAndPreClosure);

            final DepositProductRecurringDetail prodRecurringDetail = ((RecurringDepositProduct) product).depositRecurringDetail();
            final DepositAccountRecurringDetail accountRecurringDetail = this.assembleAccountRecurringDetail(command,
                    prodRecurringDetail.recurringDetail());

            RecurringDepositAccount rdAccount = RecurringDepositAccount.createNewApplicationForSubmittal(client, group, product,
                    fieldOfficer, accountNo, externalId, accountType, submittedOnDate, submittedBy, interestRate,
                    interestCompoundingPeriodType, interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType,
                    minRequiredOpeningBalance, lockinPeriodFrequency, lockinPeriodFrequencyType, iswithdrawalFeeApplicableForTransfer,
                    charges, accountTermAndPreClosure, accountRecurringDetail, accountChart);

            accountTermAndPreClosure.updateAccountReference(rdAccount);
            accountRecurringDetail.updateAccountReference(rdAccount);
            rdAccount.validateDomainRules();
            account = rdAccount;
        }

        if (account != null) {
            account.setHelpers(this.savingsAccountTransactionSummaryWrapper, this.savingsHelper);
            account.validateNewApplicationState(DateUtils.getLocalDateOfTenant(), depositAccountType.resourceName());
        }

        return account;
    }

    public SavingsAccount assembleFrom(final Long savingsId, DepositAccountType depositAccountType) {
        final SavingsAccount account = this.savingsAccountRepository.findOneWithNotFoundDetection(savingsId, depositAccountType);
        account.setHelpers(this.savingsAccountTransactionSummaryWrapper, this.savingsHelper);
        return account;
    }

    public void assignSavingAccountHelpers(final SavingsAccount savingsAccount) {
        savingsAccount.setHelpers(this.savingsAccountTransactionSummaryWrapper, this.savingsHelper);
    }

    public DepositAccountTermAndPreClosure assembleAccountTermAndPreClosure(final JsonCommand command,
            final DepositProductTermAndPreClosure productTermAndPreclosure) {
        final DepositPreClosureDetail productPreClosure = (productTermAndPreclosure == null) ? null : productTermAndPreclosure
                .depositPreClosureDetail();
        final DepositTermDetail productTerm = (productTermAndPreclosure == null) ? null : productTermAndPreclosure.depositTermDetail();

        final DepositPreClosureDetail updatedProductPreClosure = this.depositProductAssembler.assemblePreClosureDetail(command,
                productPreClosure);
        final DepositTermDetail updatedProductTerm = this.depositProductAssembler.assembleDepositTermDetail(command, productTerm);

        final BigDecimal depositAmount = command.bigDecimalValueOfParameterNamed(depositAmountParamName);
        final Integer depositPeriod = command.integerValueOfParameterNamed(depositPeriodParamName);
        final Integer depositPeriodFrequencyId = command.integerValueOfParameterNamed(depositPeriodFrequencyIdParamName);
        final SavingsPeriodFrequencyType depositPeriodFrequency = SavingsPeriodFrequencyType.fromInt(depositPeriodFrequencyId);
        final SavingsAccount account = null;
        final LocalDate expectedFirstDepositOnDate = command.localDateValueOfParameterNamed(expectedFirstDepositOnDateParamName);
        final Boolean trasferInterest = command.booleanPrimitiveValueOfParameterNamed(transferInterestToSavingsParamName);

        // calculate maturity amount
        final BigDecimal maturityAmount = null;// calculated and updated in
                                               // account
        final LocalDate maturityDate = null;// calculated and updated in account
        final DepositAccountOnClosureType accountOnClosureType = null;
        return DepositAccountTermAndPreClosure.createNew(updatedProductPreClosure, updatedProductTerm, account, depositAmount,
                maturityAmount, maturityDate, depositPeriod, depositPeriodFrequency, expectedFirstDepositOnDate, accountOnClosureType,
                trasferInterest);
    }

    public DepositAccountRecurringDetail assembleAccountRecurringDetail(final JsonCommand command,
            final DepositRecurringDetail prodRecurringDetail) {

        final BigDecimal recurringDepositAmount = command.bigDecimalValueOfParameterNamed(mandatoryRecommendedDepositAmountParamName);
        boolean isMandatoryDeposit;
        boolean allowWithdrawal;
        boolean adjustAdvanceTowardsFuturePayments;
        boolean isCalendarInherited;

        if (command.parameterExists(isMandatoryDepositParamName)) {
            isMandatoryDeposit = command.booleanObjectValueOfParameterNamed(isMandatoryDepositParamName);
        } else {
            isMandatoryDeposit = prodRecurringDetail.isMandatoryDeposit();
        }

        if (command.parameterExists(allowWithdrawalParamName)) {
            allowWithdrawal = command.booleanObjectValueOfParameterNamed(allowWithdrawalParamName);
        } else {
            allowWithdrawal = prodRecurringDetail.allowWithdrawal();
        }

        if (command.parameterExists(adjustAdvanceTowardsFuturePaymentsParamName)) {
            adjustAdvanceTowardsFuturePayments = command.booleanObjectValueOfParameterNamed(adjustAdvanceTowardsFuturePaymentsParamName);
        } else {
            adjustAdvanceTowardsFuturePayments = prodRecurringDetail.adjustAdvanceTowardsFuturePayments();
        }

        if (command.parameterExists(isCalendarInheritedParamName)) {
            isCalendarInherited = command.booleanObjectValueOfParameterNamed(isCalendarInheritedParamName);
        } else {
            isCalendarInherited = false;
        }

        final DepositRecurringDetail depositRecurringDetail = DepositRecurringDetail.createFrom(isMandatoryDeposit, allowWithdrawal,
                adjustAdvanceTowardsFuturePayments);
        final DepositAccountRecurringDetail depositAccountRecurringDetail = DepositAccountRecurringDetail.createNew(recurringDepositAmount,
                depositRecurringDetail, null, isCalendarInherited);
        return depositAccountRecurringDetail;
    }

    public Collection<SavingsAccountTransactionDTO> assembleBulkMandatorySavingsAccountTransactionDTOs(final JsonCommand command,final PaymentDetail paymentDetail) {
        AppUser user = getAppUserIfPresent();
        final String json = command.json();
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }
        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final Collection<SavingsAccountTransactionDTO> savingsAccountTransactions = new ArrayList<>();
        final LocalDate transactionDate = this.fromApiJsonHelper.extractLocalDateNamed(transactionDateParamName, element);
        final String dateFormat = this.fromApiJsonHelper.extractDateFormatParameter(element.getAsJsonObject());
        final JsonObject topLevelJsonElement = element.getAsJsonObject();
        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);
        final DateTimeFormatter formatter = DateTimeFormat.forPattern(dateFormat).withLocale(locale);

        if (element.isJsonObject()) {
            if (topLevelJsonElement.has(bulkSavingsDueTransactionsParamName)
                    && topLevelJsonElement.get(bulkSavingsDueTransactionsParamName).isJsonArray()) {
                final JsonArray array = topLevelJsonElement.get(bulkSavingsDueTransactionsParamName).getAsJsonArray();

                for (int i = 0; i < array.size(); i++) {
                    final JsonObject savingsTransactionElement = array.get(i).getAsJsonObject();
                    final Long savingsId = this.fromApiJsonHelper.extractLongNamed(savingsIdParamName, savingsTransactionElement);
                    final BigDecimal dueAmount = this.fromApiJsonHelper.extractBigDecimalNamed(transactionAmountParamName,
                            savingsTransactionElement, locale);
                    PaymentDetail detail = paymentDetail;
                    if (paymentDetail == null) {
                        detail = this.paymentDetailAssembler.fetchPaymentDetail(savingsTransactionElement);
                    }
                    final SavingsAccountTransactionDTO savingsAccountTransactionDTO = new SavingsAccountTransactionDTO(formatter,
                            transactionDate, dueAmount, detail, new Date(), savingsId, user);
                    savingsAccountTransactions.add(savingsAccountTransactionDTO);
                }
            }
        }

        return savingsAccountTransactions;
    }

    private AppUser getAppUserIfPresent() {
        AppUser user = null;
        if (this.context != null) {
            user = this.context.getAuthenticatedUserIfPresent();
        }
        return user;
    }

}