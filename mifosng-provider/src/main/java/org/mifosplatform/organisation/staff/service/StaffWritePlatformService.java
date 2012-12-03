package org.mifosplatform.organisation.staff.service;

import org.mifosplatform.organisation.staff.command.StaffCommand;

public interface StaffWritePlatformService {

    Long createStaff(final StaffCommand command);

    Long updateStaff(final StaffCommand command);
}