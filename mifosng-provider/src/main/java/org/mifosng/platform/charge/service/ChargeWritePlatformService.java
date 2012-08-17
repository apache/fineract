package org.mifosng.platform.charge.service;

import org.mifosng.platform.api.commands.ChargeCommand;
import org.springframework.security.access.prepost.PreAuthorize;

public interface ChargeWritePlatformService {

    @PreAuthorize(value = "hasRole('ORGANISATION_ADMINISTRATION_SUPER_USER_ROLE')")
    Long createCharge(final ChargeCommand command);

    @PreAuthorize(value = "hasRole('ORGANISATION_ADMINISTRATION_SUPER_USER_ROLE')")
    Long updateCharge(final ChargeCommand command);

    @PreAuthorize(value = "hasRole('ORGANISATION_ADMINISTRATION_SUPER_USER_ROLE')")
    Long deleteCharge(final Long chargeId);
}
