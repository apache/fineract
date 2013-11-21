/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.staff.service;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.organisation.office.domain.OfficeRepository;
import org.mifosplatform.organisation.office.exception.OfficeNotFoundException;
import org.mifosplatform.organisation.staff.domain.Staff;
import org.mifosplatform.organisation.staff.domain.StaffRepository;
import org.mifosplatform.organisation.staff.exception.StaffNotFoundException;
import org.mifosplatform.organisation.staff.serialization.StaffCommandFromApiJsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StaffWritePlatformServiceJpaRepositoryImpl implements StaffWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(StaffWritePlatformServiceJpaRepositoryImpl.class);

    private final StaffCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    private final StaffRepository staffRepository;
    private final OfficeRepository officeRepository;

    @Autowired
    public StaffWritePlatformServiceJpaRepositoryImpl(final StaffCommandFromApiJsonDeserializer fromApiJsonDeserializer,
            final StaffRepository staffRepository, final OfficeRepository officeRepository) {
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.staffRepository = staffRepository;
        this.officeRepository = officeRepository;
    }

    @Transactional
    @Override
    public CommandProcessingResult createStaff(final JsonCommand command) {

        try {
            this.fromApiJsonDeserializer.validateForCreate(command.json());

            final Long officeId = command.longValueOfParameterNamed("officeId");

            final Office staffOffice = this.officeRepository.findOne(officeId);
            if (staffOffice == null) { throw new OfficeNotFoundException(officeId); }

            final Staff staff = Staff.fromJson(staffOffice, command);

            this.staffRepository.save(staff);

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(staff.getId()).withOfficeId(officeId) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleStaffDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult updateStaff(final Long staffId, final JsonCommand command) {

        try {
            this.fromApiJsonDeserializer.validateForUpdate(command.json());

            final Staff staffForUpdate = this.staffRepository.findOne(staffId);
            if (staffForUpdate == null) { throw new StaffNotFoundException(staffId); }

            final Map<String, Object> changesOnly = staffForUpdate.update(command);

            if (changesOnly.containsKey("officeId")) {
                final Long officeId = (Long) changesOnly.get("officeId");
                final Office newOffice = this.officeRepository.findOne(officeId);
                if (newOffice == null) { throw new OfficeNotFoundException(officeId); }

                staffForUpdate.changeOffice(newOffice);
            }

            if (!changesOnly.isEmpty()) {
                this.staffRepository.saveAndFlush(staffForUpdate);
            }

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(staffId)
                    .withOfficeId(staffForUpdate.officeId()).with(changesOnly).build();
        } catch (final DataIntegrityViolationException dve) {
            handleStaffDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleStaffDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {
        final Throwable realCause = dve.getMostSpecificCause();

        if (realCause.getMessage().contains("external_id")) {

            final String externalId = command.stringValueOfParameterNamed("externalId");
            throw new PlatformDataIntegrityException("error.msg.staff.duplicate.externalId", "Staff with externalId `" + externalId
                    + "` already exists", "externalId", externalId);
        } else if (realCause.getMessage().contains("display_name")) {
            final String lastname = command.stringValueOfParameterNamed("lastname");
            String displayName = lastname;
            if (!StringUtils.isBlank(displayName)) {
                final String firstname = command.stringValueOfParameterNamed("firstname");
                displayName = lastname + ", " + firstname;
            }
            throw new PlatformDataIntegrityException("error.msg.staff.duplicate.displayName", "A staff with the given display name '"
                    + displayName + "' already exists", "displayName", displayName);
        }

        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.staff.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }
}