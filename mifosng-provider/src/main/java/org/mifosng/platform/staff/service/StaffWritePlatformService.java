package org.mifosng.platform.staff.service;

import org.mifosng.platform.api.commands.StaffCommand;
import org.springframework.security.access.prepost.PreAuthorize;

public interface StaffWritePlatformService {

	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'ORGANISATION_ADMINISTRATION_SUPER_USER', 'CREATE_STAFF')")
	Long createStaff(final StaffCommand command);
	
	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'ORGANISATION_ADMINISTRATION_SUPER_USER', 'UPDATE_STAFF')")
	Long updateStaff(final StaffCommand command);
}