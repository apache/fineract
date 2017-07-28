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
package org.apache.fineract.organisation.holiday.service;

import static org.apache.fineract.organisation.holiday.api.HolidayApiConstants.officesParamName;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.PersistenceException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.holiday.api.HolidayApiConstants;
import org.apache.fineract.organisation.holiday.data.HolidayDataValidator;
import org.apache.fineract.organisation.holiday.domain.Holiday;
import org.apache.fineract.organisation.holiday.domain.HolidayRepositoryWrapper;
import org.apache.fineract.organisation.holiday.exception.HolidayDateException;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.office.domain.OfficeRepositoryWrapper;
import org.apache.fineract.organisation.workingdays.domain.WorkingDays;
import org.apache.fineract.organisation.workingdays.domain.WorkingDaysRepositoryWrapper;
import org.apache.fineract.organisation.workingdays.service.WorkingDaysUtil;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@Service
public class HolidayWritePlatformServiceJpaRepositoryImpl implements HolidayWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(HolidayWritePlatformServiceJpaRepositoryImpl.class);

    private final HolidayDataValidator fromApiJsonDeserializer;
    private final HolidayRepositoryWrapper holidayRepository;
    private final WorkingDaysRepositoryWrapper daysRepositoryWrapper;
    private final PlatformSecurityContext context;
    private final OfficeRepositoryWrapper officeRepositoryWrapper;
    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public HolidayWritePlatformServiceJpaRepositoryImpl(final HolidayDataValidator fromApiJsonDeserializer,
            final HolidayRepositoryWrapper holidayRepository, final PlatformSecurityContext context,
            final OfficeRepositoryWrapper officeRepositoryWrapper, final FromJsonHelper fromApiJsonHelper,
            final WorkingDaysRepositoryWrapper daysRepositoryWrapper) {
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.holidayRepository = holidayRepository;
        this.context = context;
        this.officeRepositoryWrapper = officeRepositoryWrapper;
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.daysRepositoryWrapper = daysRepositoryWrapper;
    }

    @Transactional
    @Override
    public CommandProcessingResult createHoliday(final JsonCommand command) {

        try {
            this.context.authenticatedUser();
            this.fromApiJsonDeserializer.validateForCreate(command.json());

            validateInputDates(command);

            final Set<Office> offices = getSelectedOffices(command);

            final Holiday holiday = Holiday.createNew(offices, command);

            this.holidayRepository.save(holiday);

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(holiday.getId()).build();
        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        }catch (final PersistenceException dve) {
        	Throwable throwable = ExceptionUtils.getRootCause(dve.getCause()) ;
        	handleDataIntegrityIssues(command, throwable, dve);
        	return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult updateHoliday(final JsonCommand command) {

        try {
            this.context.authenticatedUser();
            this.fromApiJsonDeserializer.validateForUpdate(command.json());

            final Holiday holiday = this.holidayRepository.findOneWithNotFoundDetection(command.entityId());
            Map<String, Object> changes = holiday.update(command);

            validateInputDates(holiday.getFromDateLocalDate(), holiday.getToDateLocalDate(), holiday.getRepaymentsRescheduledToLocalDate());

            if (changes.containsKey(officesParamName)) {
                final Set<Office> offices = getSelectedOffices(command);
                final boolean updated = holiday.update(offices);
                if (!updated) {
                    changes.remove(officesParamName);
                }
            }

            this.holidayRepository.saveAndFlush(holiday);

            return new CommandProcessingResultBuilder().withEntityId(holiday.getId()).with(changes).build();
        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        }catch (final PersistenceException dve) {
        	Throwable throwable = ExceptionUtils.getRootCause(dve.getCause()) ;
        	handleDataIntegrityIssues(command, throwable, dve);
        	return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult activateHoliday(final Long holidayId) {
        this.context.authenticatedUser();
        final Holiday holiday = this.holidayRepository.findOneWithNotFoundDetection(holidayId);

        holiday.activate();
        this.holidayRepository.saveAndFlush(holiday);
        return new CommandProcessingResultBuilder().withEntityId(holiday.getId()).build();
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteHoliday(final Long holidayId) {
        this.context.authenticatedUser();
        final Holiday holiday = this.holidayRepository.findOneWithNotFoundDetection(holidayId);
        holiday.delete();
        this.holidayRepository.saveAndFlush(holiday);
        return new CommandProcessingResultBuilder().withEntityId(holidayId).build();
    }

    private Set<Office> getSelectedOffices(final JsonCommand command) {
        Set<Office> offices = null;
        final JsonObject topLevelJsonElement = this.fromApiJsonHelper.parse(command.json()).getAsJsonObject();
        if (topLevelJsonElement.has(HolidayApiConstants.officesParamName)
                && topLevelJsonElement.get(HolidayApiConstants.officesParamName).isJsonArray()) {

            final JsonArray array = topLevelJsonElement.get(HolidayApiConstants.officesParamName).getAsJsonArray();
            offices = new HashSet<>(array.size());
            for (int i = 0; i < array.size(); i++) {
                final JsonObject officeElement = array.get(i).getAsJsonObject();
                final Long officeId = this.fromApiJsonHelper.extractLongNamed(HolidayApiConstants.officeIdParamName, officeElement);
                final Office office = this.officeRepositoryWrapper.findOneWithNotFoundDetection(officeId);
                offices.add(office);
            }
        }
        return offices;
    }

    private void handleDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dve) {
        if (realCause.getMessage().contains("holiday_name")) {
            final String name = command.stringValueOfParameterNamed("name");
            throw new PlatformDataIntegrityException("error.msg.holiday.duplicate.name", "Holiday with name `" + name + "` already exists",
                    "name", name);
        }

        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.office.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    private void validateInputDates(final JsonCommand command) {
        final LocalDate fromDate = command.localDateValueOfParameterNamed(HolidayApiConstants.fromDateParamName);
        final LocalDate toDate = command.localDateValueOfParameterNamed(HolidayApiConstants.toDateParamName);
        Integer reshedulingType = null;
        if(command.parameterExists(HolidayApiConstants.reschedulingType)){
            reshedulingType = command.integerValueOfParameterNamed(HolidayApiConstants.reschedulingType);
        }
        LocalDate repaymentsRescheduledTo = null;
        if(reshedulingType != null && reshedulingType.equals(2)){
            repaymentsRescheduledTo = command
                    .localDateValueOfParameterNamed(HolidayApiConstants.repaymentsRescheduledToParamName);
        }
        if(repaymentsRescheduledTo != null){
            this.validateInputDates(fromDate, toDate, repaymentsRescheduledTo);
        }
    }

    private void validateInputDates(final LocalDate fromDate, final LocalDate toDate, final LocalDate repaymentsRescheduledTo) {

        String defaultUserMessage = "";

        if (toDate.isBefore(fromDate)) {
            defaultUserMessage = "To Date date cannot be before the From Date.";
            throw new HolidayDateException("to.date.cannot.be.before.from.date", defaultUserMessage, fromDate.toString(),
                    toDate.toString());
        }
        if(repaymentsRescheduledTo != null){
            if ((repaymentsRescheduledTo.isEqual(fromDate) || repaymentsRescheduledTo.isEqual(toDate)
                    || (repaymentsRescheduledTo.isAfter(fromDate) && repaymentsRescheduledTo.isBefore(toDate)))) {

                defaultUserMessage = "Repayments rescheduled date should be before from date or after to date.";
                throw new HolidayDateException("repayments.rescheduled.date.should.be.before.from.date.or.after.to.date", defaultUserMessage,
                        repaymentsRescheduledTo.toString());
            }

            final WorkingDays workingDays = this.daysRepositoryWrapper.findOne();
            final Boolean isRepaymentOnWorkingDay = WorkingDaysUtil.isWorkingDay(workingDays, repaymentsRescheduledTo);

            if (!isRepaymentOnWorkingDay) {
                defaultUserMessage = "Repayments rescheduled date should not fall on non working days";
                throw new HolidayDateException("repayments.rescheduled.date.should.not.fall.on.non.working.day", defaultUserMessage,
                        repaymentsRescheduledTo.toString());
            }

            // validate repaymentsRescheduledTo date
            // 1. should be within a 7 days date range.
            // 2. Alternative date should not be an exist holiday.//TBD
            // 3. Holiday should not be on an repaymentsRescheduledTo date of
            // another holiday.//TBD

            // restricting repaymentsRescheduledTo date to be within 7 days range
            // before or after from date and to date.
            if (repaymentsRescheduledTo.isBefore(fromDate.minusDays(7)) || repaymentsRescheduledTo.isAfter(toDate.plusDays(7))) {
                defaultUserMessage = "Repayments Rescheduled to date must be within 7 days before or after from and to dates";
                throw new HolidayDateException("repayments.rescheduled.to.must.be.within.range", defaultUserMessage, fromDate.toString(),
                        toDate.toString(), repaymentsRescheduledTo.toString());
            }
        }


    }

}
