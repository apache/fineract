package org.mifosplatform.infrastructure.staff.service;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.configuration.service.ConfigurationDomainService;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.office.domain.Office;
import org.mifosplatform.infrastructure.office.domain.OfficeRepository;
import org.mifosplatform.infrastructure.office.exception.OfficeNotFoundException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.infrastructure.staff.command.StaffCommand;
import org.mifosplatform.infrastructure.staff.domain.Staff;
import org.mifosplatform.infrastructure.staff.domain.StaffRepository;
import org.mifosplatform.infrastructure.staff.exception.StaffNotFoundException;
import org.mifosplatform.portfolio.client.service.RollbackTransactionAsCommandIsNotApprovedByCheckerException;
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
    private final StaffRepository staffRepository;
    private final OfficeRepository officeRepository;
    private final ConfigurationDomainService configurationDomainService;

    @Autowired
    public StaffWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context, final StaffRepository staffRepository,
            final OfficeRepository officeRepository, final ConfigurationDomainService configurationDomainService) {
        this.context = context;
        this.staffRepository = staffRepository;
        this.officeRepository = officeRepository;
        this.configurationDomainService = configurationDomainService;
    }

    @Transactional
    @Override
    public Long createStaff(final StaffCommand command) {

        try {
            context.authenticatedUser();
            command.validateForCreate();

            final Office staffOffice = this.officeRepository.findOne(command.getOfficeId());
            if (staffOffice == null) { throw new OfficeNotFoundException(command.getOfficeId()); }

            final Staff staff = Staff.createNew(staffOffice, command.getFirstname(), command.getLastname(), command.getIsLoanOfficer());

            this.staffRepository.save(staff);

            if (this.configurationDomainService.isMakerCheckerEnabledForTask("CREATE_STAFF") && !command.isApprovedByChecker()) { throw new RollbackTransactionAsCommandIsNotApprovedByCheckerException(); }

            return staff.getId();
        } catch (DataIntegrityViolationException dve) {
            handleStaffDataIntegrityIssues(command, dve);
            return Long.valueOf(-1);
        }
    }

    @Transactional
    @Override
    public Long updateStaff(final StaffCommand command) {

        try {
            context.authenticatedUser();
            command.validateForUpdate();

            final Long staffId = command.getId();
            Staff staff = this.staffRepository.findOne(staffId);
            if (staff == null) { throw new StaffNotFoundException(staffId); }

            Office staffOffice = null;
            Long officeId = command.getOfficeId();
            if (command.isOfficeChanged() && officeId != null) {
                staffOffice = this.officeRepository.findOne(officeId);
                if (staffOffice == null) { throw new OfficeNotFoundException(command.getOfficeId()); }
            }

            staff.update(command, staffOffice);

            this.staffRepository.save(staff);

            if (this.configurationDomainService.isMakerCheckerEnabledForTask("UPDATE_STAFF") && !command.isApprovedByChecker()) { throw new RollbackTransactionAsCommandIsNotApprovedByCheckerException(); }

            return staff.getId();
        } catch (DataIntegrityViolationException dve) {
            handleStaffDataIntegrityIssues(command, dve);
            return Long.valueOf(-1);
        }
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleStaffDataIntegrityIssues(final StaffCommand command, DataIntegrityViolationException dve) {
        Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("display_name")) {
            String displayName = command.getLastname();
            if (!StringUtils.isBlank(command.getFirstname())) {
                displayName = command.getLastname() + ", " + command.getFirstname();
            }
            throw new PlatformDataIntegrityException("error.msg.staff.duplicate.displayName", "A staff with the given display name '"
                    + displayName + "' already exists", "displayName", displayName);
        }

        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.staff.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }
}