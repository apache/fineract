package org.mifosplatform.organisation.staff.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

public interface StaffWritePlatformService {

    CommandProcessingResult createStaff(final JsonCommand command);

    CommandProcessingResult updateStaff(final Long staffId, final JsonCommand command);
}