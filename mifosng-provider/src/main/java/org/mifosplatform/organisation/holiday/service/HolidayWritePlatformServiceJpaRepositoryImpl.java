package org.mifosplatform.organisation.holiday.service;

import java.util.HashSet;
import java.util.Set;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.holiday.api.HolidayApiConstants;
import org.mifosplatform.organisation.holiday.data.HolidayDataValidator;
import org.mifosplatform.organisation.holiday.domain.Holiday;
import org.mifosplatform.organisation.holiday.domain.HolidayRepository;
import org.mifosplatform.organisation.holiday.exception.HolidayDateException;
import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.organisation.office.domain.OfficeRepository;
import org.mifosplatform.organisation.office.exception.OfficeNotFoundException;
import org.mifosplatform.organisation.workingdays.domain.WorkingDays;
import org.mifosplatform.organisation.workingdays.domain.WorkingDaysRepositoryWrapper;
import org.mifosplatform.organisation.workingdays.service.WorkingDaysUtil;
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
    private final HolidayRepository holidayRepository;
    private final WorkingDaysRepositoryWrapper daysRepositoryWrapper;

    private final PlatformSecurityContext context;
    private final OfficeRepository officeRepository;
    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public HolidayWritePlatformServiceJpaRepositoryImpl(final HolidayDataValidator fromApiJsonDeserializer,
            final HolidayRepository holidayRepository, final PlatformSecurityContext context, final OfficeRepository officeRepository,
            final FromJsonHelper fromApiJsonHelper, final WorkingDaysRepositoryWrapper daysRepositoryWrapper) {
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.holidayRepository = holidayRepository;
        this.context = context;
        this.officeRepository = officeRepository;
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
            handleDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

    private Set<Office> getSelectedOffices(final JsonCommand command) {
        Set<Office> offices = null;
        final JsonObject topLevelJsonElement = this.fromApiJsonHelper.parse(command.json()).getAsJsonObject();
        if (topLevelJsonElement.has(HolidayApiConstants.offices) && topLevelJsonElement.get(HolidayApiConstants.offices).isJsonArray()) {

            final JsonArray array = topLevelJsonElement.get(HolidayApiConstants.offices).getAsJsonArray();
            offices = new HashSet<Office>(array.size());
            for (int i = 0; i < array.size(); i++) {
                final JsonObject officeElement = array.get(i).getAsJsonObject();
                final Long officeId = this.fromApiJsonHelper.extractLongNamed(HolidayApiConstants.officeId, officeElement);
                final Office office = this.officeRepository.findOne(officeId);
                if (office == null) { throw new OfficeNotFoundException(officeId); }
                offices.add(office);
            }
        }
        return offices;
    }

    private void handleDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {
        final Throwable realCause = dve.getMostSpecificCause();
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
        final LocalDate fromDate = command.localDateValueOfParameterNamed(HolidayApiConstants.fromDate);
        final LocalDate toDate = command.localDateValueOfParameterNamed(HolidayApiConstants.toDate);
        final LocalDate repaymentsRescheduledTo = command.localDateValueOfParameterNamed(HolidayApiConstants.repaymentsRescheduledTo);
        String defaultUserMessage = "";

        if (toDate.isBefore(fromDate)) {
            defaultUserMessage = "To Date date cannot be before the From Date.";
            throw new HolidayDateException("to.date.cannot.be.before.from.date", defaultUserMessage, fromDate.toString(), toDate.toString());
        }

        if (repaymentsRescheduledTo.isEqual(fromDate) || repaymentsRescheduledTo.isEqual(toDate)
                || (repaymentsRescheduledTo.isAfter(fromDate) && repaymentsRescheduledTo.isBefore(toDate))) {

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
