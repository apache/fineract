package org.mifosplatform.organisation.staff.service;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.organisation.office.domain.OfficeRepository;
import org.mifosplatform.organisation.office.exception.OfficeNotFoundException;
import org.mifosplatform.organisation.staff.command.StaffCommand;
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

    private final PlatformSecurityContext context;
    private final StaffCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    private final StaffRepository staffRepository;
    private final OfficeRepository officeRepository;

    @Autowired
    public StaffWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final StaffCommandFromApiJsonDeserializer fromApiJsonDeserializer, final StaffRepository staffRepository,
            final OfficeRepository officeRepository) {
        this.context = context;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.staffRepository = staffRepository;
        this.officeRepository = officeRepository;
    }

    @Transactional
    @Override
    public EntityIdentifier createStaff(final JsonCommand command) {

        try {
            context.authenticatedUser();

            final StaffCommand staffCommand = this.fromApiJsonDeserializer.commandFromApiJson(command.json());
            staffCommand.validateForCreate();

            final Long officeId = command.longValueOfParameterNamed("officeId");

            final Office staffOffice = this.officeRepository.findOne(officeId);
            if (staffOffice == null) { throw new OfficeNotFoundException(officeId); }

            final Staff staff = Staff.fromJson(staffOffice, command);

            this.staffRepository.save(staff);

            return EntityIdentifier.resourceResult(staff.getId(), null);
        } catch (DataIntegrityViolationException dve) {
            handleStaffDataIntegrityIssues(command, dve);
            return EntityIdentifier.empty();
        }
    }

    @Transactional
    @Override
    public EntityIdentifier updateStaff(final Long staffId, final JsonCommand command) {

        try {
            context.authenticatedUser();

            final StaffCommand staffCommand = this.fromApiJsonDeserializer.commandFromApiJson(command.json());
            staffCommand.validateForUpdate();

            Staff staffForUpdate = this.staffRepository.findOne(staffId);
            if (staffForUpdate == null) { throw new StaffNotFoundException(staffId); }

            final Map<String, Object> changesOnly = staffForUpdate.update(command);

            if (changesOnly.containsKey("officeId")) {
                final Long officeId = (Long) changesOnly.get("officeId");
                final Office newOffice = this.officeRepository.findOne(officeId);
                if (newOffice == null) { throw new OfficeNotFoundException(officeId); }

                staffForUpdate.changeOffice(newOffice);
            }

            if (!changesOnly.isEmpty()) {
                this.staffRepository.save(staffForUpdate);
            }

            return EntityIdentifier.withChanges(staffForUpdate.getId(), changesOnly);
        } catch (DataIntegrityViolationException dve) {
            handleStaffDataIntegrityIssues(command, dve);
            return EntityIdentifier.empty();
        }
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleStaffDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {
        Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("display_name")) {
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