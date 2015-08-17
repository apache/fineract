package org.mifosplatform.portfolio.client.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationDomainService;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.holiday.domain.HolidayRepositoryWrapper;
import org.mifosplatform.organisation.workingdays.domain.WorkingDaysRepository;
import org.mifosplatform.organisation.workingdays.domain.WorkingDaysRepositoryWrapper;
import org.mifosplatform.portfolio.charge.domain.Charge;
import org.mifosplatform.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.mifosplatform.portfolio.client.api.ClientApiConstants;
import org.mifosplatform.portfolio.client.data.ClientChargeDataValidator;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientCharge;
import org.mifosplatform.portfolio.client.domain.ClientChargeRepository;
import org.mifosplatform.portfolio.client.domain.ClientRepository;
import org.mifosplatform.portfolio.client.domain.ClientRepositoryWrapper;
import org.mifosplatform.portfolio.client.exception.ClientNotActiveException;
import org.mifosplatform.portfolio.savings.data.SavingsAccountDataValidator;
import org.mifosplatform.portfolio.savings.domain.SavingsAccount;
import org.mifosplatform.portfolio.savings.domain.SavingsAccountCharge;
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
    private final ClientChargeRepository clientChargeRepository;

    @Autowired
    public ClientChargeWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final ChargeRepositoryWrapper chargeRepository, final ClientChargeDataValidator clientChargeDataValidator,
            final ClientRepositoryWrapper clientRepository, final HolidayRepositoryWrapper holidayRepositoryWrapper,
            final ConfigurationDomainService configurationDomainService, final ClientChargeRepository clientChargeRepository,
            final WorkingDaysRepositoryWrapper workingDaysRepository) {
        this.context = context;
        this.chargeRepository = chargeRepository;
        this.clientChargeDataValidator = clientChargeDataValidator;
        this.clientRepository = clientRepository;
        this.holidayRepository = holidayRepositoryWrapper;
        this.configurationDomainService = configurationDomainService;
        this.clientChargeRepository = clientChargeRepository;
        this.workingDaysRepository = workingDaysRepository;
    }

    @Override
    public CommandProcessingResult addCharge(Long clientId, JsonCommand command) {

        this.clientChargeDataValidator.validateAdd(command.json());

        final Client client = this.clientRepository.findOneWithNotFoundDetection(clientId);
        checkClientActive(client);

        final Long chargeDefinitionId = command.longValueOfParameterNamed(ClientApiConstants.chargeIdParamName);
        final Charge charge = this.chargeRepository.findOneWithNotFoundDetection(chargeDefinitionId);

        final ClientCharge clientCharge = ClientCharge.createNew(client, charge, command);

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormat.forPattern(command.dateFormat());

        validateDueDateOnWorkingDay(clientCharge, fmt);

        this.clientChargeRepository.save(clientCharge);

        return new CommandProcessingResultBuilder() //
                .withEntityId(clientCharge.getId()) //
                .withOfficeId(clientCharge.getClient().getOffice().getId()) //
                .withClientId(clientCharge.getClient().getId()) //
                .build();
    }

    /**
     * Ensures that the charge due date is not on a holiday or a non working day
     * 
     * @param savingsAccountCharge
     * @param fmt
     */
    private void validateDueDateOnWorkingDay(final SavingsAccountCharge savingsAccountCharge, final DateTimeFormatter fmt) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(ClientApiConstants.CLIENT_CHARGES_RESOURCE_NAME);
        if (savingsAccountCharge.getDueLocalDate() != null) {
            // transaction date should not be on a holiday or non working day
            if (!this.configurationDomainService.allowTransactionsOnHolidayEnabled()
                    && this.holidayRepository.isHoliday(savingsAccount.officeId(), savingsAccountCharge.getDueLocalDate())) {
                baseDataValidator.reset().parameter(dueAsOfDateParamName).value(savingsAccountCharge.getDueLocalDate().toString(fmt))
                        .failWithCodeNoParameterAddedToErrorCode("charge.due.date.is.on.holiday");
                if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
            }

            if (!this.configurationDomainService.allowTransactionsOnNonWorkingDayEnabled()
                    && !this.workingDaysRepository.isWorkingDay(savingsAccountCharge.getDueLocalDate())) {
                baseDataValidator.reset().parameter(dueAsOfDateParamName).value(savingsAccountCharge.getDueLocalDate().toString(fmt))
                        .failWithCodeNoParameterAddedToErrorCode("charge.due.date.is.a.nonworking.day");
                if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
            }
        }
    }

    @Override
    public CommandProcessingResult updateCharge(Long clientId, JsonCommand command) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CommandProcessingResult deleteCharge(Long clientId, Long clientChargeId, JsonCommand command) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CommandProcessingResult waiveCharge(Long clientId, Long clientChargeId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CommandProcessingResult payCharge(Long clientId, Long clientChargeId, JsonCommand command) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CommandProcessingResult inactivateCharge(Long clientId, Long clientChargeId) {
        // TODO Auto-generated method stub
        return null;
    }

    private void checkClientActive(Client client) {
        if (client != null) {
            if (client.isNotActive()) { throw new ClientNotActiveException(client.getId()); }
        }
    }

}
