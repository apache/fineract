package org.mifosplatform.organisation.holiday.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.holiday.api.HolidayApiConstants;
import org.mifosplatform.organisation.holiday.data.HolidayDataValidator;
import org.mifosplatform.organisation.holiday.domain.Holiday;
import org.mifosplatform.organisation.holiday.domain.HolidayRepository;
import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.organisation.office.domain.OfficeRepository;
import org.mifosplatform.organisation.office.exception.OfficeNotFoundException;
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
    private final PlatformSecurityContext context;
    private final OfficeRepository officeRepository;
    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public HolidayWritePlatformServiceJpaRepositoryImpl(final HolidayDataValidator fromApiJsonDeserializer,
            final HolidayRepository holidayRepository, final PlatformSecurityContext context, final OfficeRepository officeRepository,
            final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.holidayRepository = holidayRepository;
        this.context = context;
        this.officeRepository = officeRepository;
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    @Transactional
    @Override
    public CommandProcessingResult createHoliday(final JsonCommand command) {

        try {
            this.context.authenticatedUser();
            this.fromApiJsonDeserializer.validateForCreate(command.json());

            validateToDateBeforeFromDate(command);

            Set<Office> offices = null;
            final JsonObject topLevelJsonElement = this.fromApiJsonHelper.parse(command.json()).getAsJsonObject();
            if (topLevelJsonElement.has(HolidayApiConstants.offices) && topLevelJsonElement.get(HolidayApiConstants.offices).isJsonArray()) {

                final JsonArray array = topLevelJsonElement.get(HolidayApiConstants.offices).getAsJsonArray();
                offices = new HashSet<Office>(array.size());
                for (int i = 0; i < array.size(); i++) {
                    final JsonObject officeElement = array.get(i).getAsJsonObject();
                    final Long officeId = this.fromApiJsonHelper.extractLongNamed("officeId", officeElement);
                    final Office office = this.officeRepository.findOne(officeId);
                    if (office == null) { throw new OfficeNotFoundException(officeId); }
                    offices.add(office);
                }
            }

            final Holiday holiday = Holiday.createNew(offices, command);

            this.holidayRepository.save(holiday);

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(holiday.getId()).build();
        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
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

    private void validateToDateBeforeFromDate(final JsonCommand command) {
        final LocalDate fromDate = command.localDateValueOfParameterNamed(HolidayApiConstants.fromDate);
        final LocalDate toDate = command.localDateValueOfParameterNamed(HolidayApiConstants.toDate);
        if (toDate.isBefore(fromDate)) {

            final String defaultUserMessage = "ToDate date cannot be before the FromDate.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.holiday.toDate.cannot be.before.the.FromDate",
                    defaultUserMessage, "ToDate");
            final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }
}
