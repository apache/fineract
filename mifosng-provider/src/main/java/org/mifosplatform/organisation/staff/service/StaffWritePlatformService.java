package org.mifosplatform.organisation.staff.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;

public interface StaffWritePlatformService {

    EntityIdentifier createStaff(final JsonCommand command);

    EntityIdentifier updateStaff(final Long staffId, final JsonCommand command);
}