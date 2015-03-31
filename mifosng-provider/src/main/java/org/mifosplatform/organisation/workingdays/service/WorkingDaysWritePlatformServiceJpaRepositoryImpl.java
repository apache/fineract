/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.workingdays.service;

import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.property.RRule;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationDomainService;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.organisation.workingdays.api.WorkingDaysApiConstants;
import org.mifosplatform.organisation.workingdays.data.WorkingDayValidator;
import org.mifosplatform.organisation.workingdays.domain.WorkingDays;
import org.mifosplatform.organisation.workingdays.domain.WorkingDaysRepositoryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.Map;

@Service
public class WorkingDaysWritePlatformServiceJpaRepositoryImpl implements WorkingDaysWritePlatformService {

    private final WorkingDaysRepositoryWrapper daysRepositoryWrapper;
    private final ConfigurationDomainService configurationDomainService;
    private final WorkingDayValidator fromApiJsonDeserializer;

    @Autowired
    public WorkingDaysWritePlatformServiceJpaRepositoryImpl(final WorkingDaysRepositoryWrapper daysRepositoryWrapper,
            final ConfigurationDomainService configurationDomainService, final WorkingDayValidator fromApiJsonDeserializer) {
        this.daysRepositoryWrapper = daysRepositoryWrapper;
        this.configurationDomainService = configurationDomainService;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
    }

    @Override
    public boolean isWorkingDay(LocalDate transactionDate) {
        final WorkingDays workingDays = this.daysRepositoryWrapper.findOne();
        return WorkingDaysUtil.isWorkingDay(workingDays, transactionDate);
    }

    @Override
    public boolean isTransactionAllowedOnNonWorkingDay() {
        return this.configurationDomainService.allowTransactionsOnNonWorkingDayEnabled();
    }

    @Transactional
    @Override
    public CommandProcessingResult updateWorkingDays(JsonCommand command) {
        String recurrence = "";
        RRule rrule = null;
        try {
            this.fromApiJsonDeserializer.validateForUpdate(command.json());
            final WorkingDays workingDays = this.daysRepositoryWrapper.findOne();

            recurrence = command.stringValueOfParameterNamed(WorkingDaysApiConstants.recurrence);
            rrule = new RRule(recurrence);
            rrule.validate();

            Map<String, Object> changes = workingDays.update(command);
            this.daysRepositoryWrapper.saveAndFlush(workingDays);
            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(workingDays.getId()).with(changes)
                    .build();
        } catch (final ValidationException e) {
            throw new PlatformDataIntegrityException("error.msg.invalid.recurring.rule", "The Recurring Rule value: " + recurrence
                    + " is not valid.", "recurrence", recurrence);
        } catch (final IllegalArgumentException | ParseException e) {
            throw new PlatformDataIntegrityException("error.msg.recurring.rule.parsing.error",
                    "Error in passing the Recurring Rule value: " + recurrence, "recurrence", e.getMessage());
        }
    }

}
