package org.mifosplatform.infrastructure.staff.service;

import org.mifosplatform.infrastructure.staff.command.StaffCommand;

public interface StaffWritePlatformService {

    Long createStaff(final StaffCommand command);

    Long updateStaff(final StaffCommand command);
}