package org.mifosng.platform.staff.service;

import org.apache.commons.lang.StringUtils;
import org.mifosng.platform.api.commands.StaffCommand;
import org.mifosng.platform.exceptions.StaffNotFoundException;
import org.mifosng.platform.exceptions.PlatformDataIntegrityException;
import org.mifosng.platform.staff.domain.Staff;
import org.mifosng.platform.staff.domain.StaffRepository;
import org.mifosng.platform.security.PlatformSecurityContext;
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

	@Autowired
	public StaffWritePlatformServiceJpaRepositoryImpl(
			final PlatformSecurityContext context,
			final StaffRepository staffRepository) {
		this.context = context;
		this.staffRepository = staffRepository;
	}

	@Transactional
	@Override
	public Long createStaff(final StaffCommand command) {

		try {
			context.authenticatedUser();

			StaffCommandValidator validator = new StaffCommandValidator(command);
			validator.validateForCreate();

			Staff staff = Staff.createNew(command.getFirstName(),
					command.getLastName());

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
			staff.update(command);

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