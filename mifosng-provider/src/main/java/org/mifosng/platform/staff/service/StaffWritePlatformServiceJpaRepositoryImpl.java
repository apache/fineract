package org.mifosng.platform.staff.service;

import org.apache.commons.lang.StringUtils;
import org.mifosng.platform.api.commands.StaffCommand;
import org.mifosng.platform.exceptions.OfficeNotFoundException;
import org.mifosng.platform.exceptions.PlatformDataIntegrityException;
import org.mifosng.platform.exceptions.StaffNotFoundException;
import org.mifosng.platform.organisation.domain.Office;
import org.mifosng.platform.organisation.domain.OfficeRepository;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.mifosng.platform.staff.domain.Staff;
import org.mifosng.platform.staff.domain.StaffRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StaffWritePlatformServiceJpaRepositoryImpl implements
		StaffWritePlatformService {

	private final static Logger logger = LoggerFactory
			.getLogger(StaffWritePlatformServiceJpaRepositoryImpl.class);

	private final PlatformSecurityContext context;
	private final StaffRepository staffRepository;
	private final OfficeRepository officeRepository;

	@Autowired
	public StaffWritePlatformServiceJpaRepositoryImpl(
			final PlatformSecurityContext context,
			final StaffRepository staffRepository,
			final OfficeRepository officeRepository) {
		this.context = context;
		this.staffRepository = staffRepository;
		this.officeRepository = officeRepository;
	}

	@Transactional
	@Override
	public Long createStaff(final StaffCommand command) {

		try {
			context.authenticatedUser();

			StaffCommandValidator validator = new StaffCommandValidator(command);
			validator.validateForCreate();

			Office staffOffice = this.officeRepository.findOne(command.getOfficeId());
			if (staffOffice == null) {
				throw new OfficeNotFoundException(command.getOfficeId());
			}

			Staff staff = Staff.createNew(staffOffice, command.getFirstName(),
					command.getLastName(), command.isLoanOfficerFlag());

			this.staffRepository.saveAndFlush(staff);

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

			StaffCommandValidator validator = new StaffCommandValidator(command);
			validator.validateForUpdate();

			final Long staffId = command.getId();
			Staff staff = this.staffRepository.findOne(staffId);
			if (staff == null) {
				throw new StaffNotFoundException(staffId);
			}

			Office staffOffice = null;
			Long officeId = command.getOfficeId();
			if (command.isOfficeChanged() && officeId != null) {
				staffOffice = this.officeRepository.findOne(officeId);
				if (staffOffice == null) {
					throw new OfficeNotFoundException(command.getOfficeId());
				}
			}

			staff.update(command, staffOffice);

			this.staffRepository.saveAndFlush(staff);

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
	private void handleStaffDataIntegrityIssues(final StaffCommand command,
			DataIntegrityViolationException dve) {
		Throwable realCause = dve.getMostSpecificCause();
		if (realCause.getMessage().contains("display_name")) {
			String displayName = command.getLastName();
			if (!StringUtils.isBlank(command.getFirstName())) {
				displayName = command.getLastName() + ", "
						+ command.getFirstName();
			}
			throw new PlatformDataIntegrityException(
					"error.msg.staff.duplicate.displayName",
					"A staff with the given display name '" + displayName
							+ "' already exists", "displayName", displayName);
		}

		logger.error(dve.getMessage(), dve);
		throw new PlatformDataIntegrityException(
				"error.msg.staff.unknown.data.integrity.issue",
				"Unknown data integrity issue with resource: "
						+ realCause.getMessage());
	}
}