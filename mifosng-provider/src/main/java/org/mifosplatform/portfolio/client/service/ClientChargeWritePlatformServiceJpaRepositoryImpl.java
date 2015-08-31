/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.mifosplatform.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationDomainService;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.holiday.domain.HolidayRepositoryWrapper;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.organisation.workingdays.domain.WorkingDaysRepositoryWrapper;
import org.mifosplatform.portfolio.charge.domain.Charge;
import org.mifosplatform.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.mifosplatform.portfolio.charge.exception.ChargeCannotBeAppliedToException;
import org.mifosplatform.portfolio.client.api.ClientApiConstants;
import org.mifosplatform.portfolio.client.data.ClientChargeDataValidator;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientCharge;
import org.mifosplatform.portfolio.client.domain.ClientChargePaidBy;
import org.mifosplatform.portfolio.client.domain.ClientChargeRepositoryWrapper;
import org.mifosplatform.portfolio.client.domain.ClientRepositoryWrapper;
import org.mifosplatform.portfolio.client.domain.ClientTransaction;
import org.mifosplatform.portfolio.client.domain.ClientTransactionRepository;
import org.mifosplatform.portfolio.paymentdetail.domain.PaymentDetail;
import org.mifosplatform.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClientChargeWritePlatformServiceJpaRepositoryImpl implements ClientChargeWritePlatformService {

    private final PlatformSecurityContext context;
    private final ChargeRepositoryWrapper chargeRepository;
    private final ClientRepositoryWrapper clientRepository;
    private final ClientChargeDataValidator clientChargeDataValidator;
    private final ConfigurationDomainService configurationDomainService;
    private final HolidayRepositoryWrapper holidayRepository;
    private final WorkingDaysRepositoryWrapper workingDaysRepository;
    private final ClientChargeRepositoryWrapper clientChargeRepository;
    private final ClientTransactionRepository clientTransactionRepository;
    private final PaymentDetailWritePlatformService paymentDetailWritePlatformService;
    private final JournalEntryWritePlatformService journalEntryWritePlatformService;

    @Autowired
    public ClientChargeWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final ChargeRepositoryWrapper chargeRepository, final ClientChargeDataValidator clientChargeDataValidator,
            final ClientRepositoryWrapper clientRepository, final HolidayRepositoryWrapper holidayRepositoryWrapper,
            final ConfigurationDomainService configurationDomainService, final ClientChargeRepositoryWrapper clientChargeRepository,
            final WorkingDaysRepositoryWrapper workingDaysRepository, final ClientTransactionRepository clientTransactionRepository,
            final PaymentDetailWritePlatformService paymentDetailWritePlatformService,
            final JournalEntryWritePlatformService journalEntryWritePlatformService) {
        this.context = context;
        this.chargeRepository = chargeRepository;
        this.clientChargeDataValidator = clientChargeDataValidator;
        this.clientRepository = clientRepository;
        this.holidayRepository = holidayRepositoryWrapper;
        this.configurationDomainService = configurationDomainService;
        this.clientChargeRepository = clientChargeRepository;
        this.workingDaysRepository = workingDaysRepository;
        this.clientTransactionRepository = clientTransactionRepository;
        this.paymentDetailWritePlatformService = paymentDetailWritePlatformService;
        this.journalEntryWritePlatformService = journalEntryWritePlatformService;
    }

    @Override
    public CommandProcessingResult addCharge(Long clientId, JsonCommand command) {

        this.clientChargeDataValidator.validateAdd(command.json());

        final Client client = clientRepository.getActiveClient(clientId);

        final Long chargeDefinitionId = command.longValueOfParameterNamed(ClientApiConstants.chargeIdParamName);
        final Charge charge = this.chargeRepository.findOneWithNotFoundDetection(chargeDefinitionId);

        // validate for client charge
        if (!charge.isClientCharge()) {
            final String errorMessage = "Charge with identifier " + charge.getId() + " cannot be applied to a Client";
            throw new ChargeCannotBeAppliedToException("client", errorMessage, charge.getId());
        }

        final ClientCharge clientCharge = ClientCharge.createNew(client, charge, command);

        final DateTimeFormatter fmt = DateTimeFormat.forPattern(command.dateFormat());
        validateDueDateOnWorkingDay(clientCharge, fmt);

        this.clientChargeRepository.save(clientCharge);

        return new CommandProcessingResultBuilder() //
                .withEntityId(clientCharge.getId()) //
                .withOfficeId(clientCharge.getClient().getOffice().getId()) //
                .withClientId(clientCharge.getClient().getId()) //
                .build();
    }

    @Override
    public CommandProcessingResult payCharge(Long clientId, Long clientChargeId, JsonCommand command) {
        this.clientChargeDataValidator.validatePayCharge(command.json());

        final Client client = this.clientRepository.getActiveClient(clientId);

        final ClientCharge clientCharge = this.clientChargeRepository.findOneWithNotFoundDetection(clientChargeId);

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormat.forPattern(command.dateFormat()).withLocale(locale);
        final LocalDate transactionDate = command.localDateValueOfParameterNamed(ClientApiConstants.transactionDateParamName);
        final BigDecimal amountPaid = command.bigDecimalValueOfParameterNamed(ClientApiConstants.amountParamName);
        final Money chargePaid = Money.of(clientCharge.getCurrency(), amountPaid);

        // Validate business rules for payment
        validatePaymentTransaction(client, clientCharge, fmt, transactionDate, amountPaid);

        // pay the charge
        clientCharge.pay(chargePaid);

        // create Payment Transaction
        final Map<String, Object> changes = new LinkedHashMap<>();
        final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);

        ClientTransaction clientTransaction = ClientTransaction.payCharge(client, client.getOffice(), paymentDetail, transactionDate,
                chargePaid, clientCharge.getCurrency().getCode(), getAppUserIfPresent());
        this.clientTransactionRepository.saveAndFlush(clientTransaction);

        // update charge paid by associations
        final ClientChargePaidBy chargePaidBy = ClientChargePaidBy.instance(clientTransaction, clientCharge, amountPaid);
        clientTransaction.getClientChargePaidByCollection().add(chargePaidBy);

        // generate accounting entries
        generateAccountingEntries(clientTransaction);

        return new CommandProcessingResultBuilder() //
                .withEntityId(clientCharge.getId()) //
                .withOfficeId(clientCharge.getClient().getOffice().getId()) //
                .withClientId(clientCharge.getClient().getId()) //
                .build();

    }

    private void generateAccountingEntries(ClientTransaction clientTransaction) {
        Map<String, Object> accountingBridgeData = clientTransaction.toMapData();
        journalEntryWritePlatformService.createJournalEntriesForClientTransactions(accountingBridgeData);
    }

    @Override
    public CommandProcessingResult waiveCharge(Long clientId, Long clientChargeId) {
        final Client client = this.clientRepository.getActiveClient(clientId);
        final ClientCharge clientCharge = this.clientChargeRepository.findOneWithNotFoundDetection(clientChargeId);
        final LocalDate transactionDate = DateUtils.getLocalDateOfTenant();

        // Validate business rules for payment
        validateWaiverTransaction(client, clientCharge);

        // waive the charge
        Money waivedAmount = clientCharge.waive();

        // create Waiver Transaction
        ClientTransaction clientTransaction = ClientTransaction.waiver(client, client.getOffice(), transactionDate, waivedAmount,
                clientCharge.getCurrency().getCode(), getAppUserIfPresent());
        this.clientTransactionRepository.save(clientTransaction);

        // update charge paid by associations
        final ClientChargePaidBy chargePaidBy = ClientChargePaidBy.instance(clientTransaction, clientCharge, waivedAmount.getAmount());
        clientTransaction.getClientChargePaidByCollection().add(chargePaidBy);

        return new CommandProcessingResultBuilder() //
                .withEntityId(clientCharge.getId()) //
                .withOfficeId(clientCharge.getClient().getOffice().getId()) //
                .withClientId(clientCharge.getClient().getId()) //
                .build();
    }

    @Override
    public CommandProcessingResult deleteCharge(Long clientId, Long clientChargeId) {
        final Client client = this.clientRepository.getActiveClient(clientId);
        final ClientCharge clientCharge = this.clientChargeRepository.findOneWithNotFoundDetection(clientChargeId);

        // Validate business rules for charge deletion
        validateChargeDeletion(client, clientCharge);

        // delete the charge
        clientChargeRepository.delete(clientCharge);

        return new CommandProcessingResultBuilder() //
                .withEntityId(clientCharge.getId()) //
                .withOfficeId(clientCharge.getClient().getOffice().getId()) //
                .withClientId(clientCharge.getClient().getId()) //
                .build();
    }

    /**
     * Validates transaction to ensure that <br>
     * charge is active <br>
     * transaction date is valid (between client activation and todays date)
     * <br>
     * charge is not already paid or waived <br>
     * amount is not more than total due
     * 
     * @param client
     * @param clientCharge
     * @param fmt
     * @param transactionDate
     * @param amountPaid
     * @param requiresTransactionDateValidation
     *            if set to false, transaction date specific validation is
     *            skipped
     * @param requiresTransactionAmountValidation
     *            if set to false transaction amount validation is skipped
     * @return
     */
    private void validatePaymentDateAndAmount(final Client client, final ClientCharge clientCharge, final DateTimeFormatter fmt,
            final LocalDate transactionDate, final BigDecimal amountPaid, final boolean requiresTransactionDateValidation,
            final boolean requiresTransactionAmountValidation) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(ClientApiConstants.CLIENT_CHARGES_RESOURCE_NAME);

        if (clientCharge.isNotActive()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("charge.is.not.active");
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }

        if (requiresTransactionDateValidation) {
            validateTransactionDateOnWorkingDay(transactionDate, clientCharge, fmt);

            if (client.getActivationLocalDate() != null && transactionDate.isBefore(client.getActivationLocalDate())) {
                baseDataValidator.reset().parameter(ClientApiConstants.transactionDateParamName).value(transactionDate.toString(fmt))
                        .failWithCodeNoParameterAddedToErrorCode("transaction.before.activationDate");
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }

            if (DateUtils.isDateInTheFuture(transactionDate)) {
                baseDataValidator.reset().parameter(ClientApiConstants.transactionDateParamName).value(transactionDate.toString(fmt))
                        .failWithCodeNoParameterAddedToErrorCode("transaction.is.futureDate");
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        // validate charge is not already paid or waived
        if (clientCharge.isWaived()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("transaction.invalid.account.charge.is.already.waived");
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        } else if (clientCharge.isPaid()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("transaction.invalid.account.charge.is.paid");
            if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        }

        if (requiresTransactionAmountValidation) {
            final Money chargePaid = Money.of(clientCharge.getCurrency(), amountPaid);
            if (!clientCharge.getAmountOutstanding().isGreaterThanOrEqualTo(chargePaid)) {
                baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("transaction.invalid.charge.amount.paid.in.access");
                if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
            }
        }
    }

    public void validateWaiverTransaction(final Client client, final ClientCharge clientCharge) {
        DateTimeFormatter fmt = null;
        LocalDate transactionDate = null;
        BigDecimal amountPaid = null;
        boolean requiresTransactionDateValidation = false;
        boolean requiresTransactionAmountValidation = false;
        validatePaymentDateAndAmount(client, clientCharge, fmt, transactionDate, amountPaid, requiresTransactionDateValidation,
                requiresTransactionAmountValidation);
    }

    public void validatePaymentTransaction(final Client client, final ClientCharge clientCharge, final DateTimeFormatter fmt,
            final LocalDate transactionDate, final BigDecimal amountPaid) {
        boolean requiresTransactionDateValidation = true;
        boolean requiresTransactionAmountValidation = true;
        validatePaymentDateAndAmount(client, clientCharge, fmt, transactionDate, amountPaid, requiresTransactionDateValidation,
                requiresTransactionAmountValidation);
    }

    public void validateChargeDeletion(final Client client, final ClientCharge clientCharge) {
        DateTimeFormatter fmt = null;
        LocalDate transactionDate = null;
        BigDecimal amountPaid = null;
        boolean requiresTransactionDateValidation = false;
        boolean requiresTransactionAmountValidation = false;
        validatePaymentDateAndAmount(client, clientCharge, fmt, transactionDate, amountPaid, requiresTransactionDateValidation,
                requiresTransactionAmountValidation);
    }

    /**
     * @param clientId
     * @return
     */
    @Override
    public CommandProcessingResult updateCharge(Long clientId, JsonCommand command) {
        // this.clientChargeDataValidator.validateAdd(command.json());
        return null;
    }

    @Override
    public CommandProcessingResult inactivateCharge(Long clientId, Long clientChargeId) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Ensures that the charge due date is not on a holiday or a non working day
     * 
     * @param clientCharge
     * @param fmt
     */
    private void validateDueDateOnWorkingDay(final ClientCharge clientCharge, final DateTimeFormatter fmt) {
        validateActivityDateFallOnAWorkingDay(clientCharge.getDueLocalDate(), clientCharge.getOfficeId(),
                ClientApiConstants.dueAsOfDateParamName, "charge.due.date.is.on.holiday", "charge.due.date.is.a.non.workingday", fmt);
    }

    /**
     * Ensures that the charge transaction date (for payments) is not on a
     * holiday or a non working day
     * 
     * @param savingsAccountCharge
     * @param fmt
     */
    private void validateTransactionDateOnWorkingDay(final LocalDate transactionDate, final ClientCharge clientCharge,
            final DateTimeFormatter fmt) {
        validateActivityDateFallOnAWorkingDay(transactionDate, clientCharge.getOfficeId(), ClientApiConstants.transactionDateParamName,
                "transaction.not.allowed.transaction.date.is.on.holiday", "transaction.not.allowed.transaction.date.is.a.non.workingday",
                fmt);
    }

    /**
     * @param date
     * @param officeId
     * @param jsonPropertyName
     * @param errorMessageFragment
     * @param fmt
     */
    private void validateActivityDateFallOnAWorkingDay(final LocalDate date, final Long officeId, final String jsonPropertyName,
            final String errorMessageFragmentForActivityOnHoliday, final String errorMessageFragmentForActivityOnNonWorkingDay,
            final DateTimeFormatter fmt) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(ClientApiConstants.CLIENT_CHARGES_RESOURCE_NAME);
        if (date != null) {
            // transaction date should not be on a holiday or non working day
            if (!this.configurationDomainService.allowTransactionsOnHolidayEnabled() && this.holidayRepository.isHoliday(officeId, date)) {
                baseDataValidator.reset().parameter(jsonPropertyName).value(date.toString(fmt))
                        .failWithCodeNoParameterAddedToErrorCode(errorMessageFragmentForActivityOnHoliday);
                if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
            }

            if (!this.configurationDomainService.allowTransactionsOnNonWorkingDayEnabled()
                    && !this.workingDaysRepository.isWorkingDay(date)) {
                baseDataValidator.reset().parameter(jsonPropertyName).value(date.toString(fmt))
                        .failWithCodeNoParameterAddedToErrorCode(errorMessageFragmentForActivityOnNonWorkingDay);
                if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
            }
        }
    }

    private AppUser getAppUserIfPresent() {
        AppUser user = null;
        if (this.context != null) {
            user = this.context.getAuthenticatedUserIfPresent();
        }
        return user;
    }

}
