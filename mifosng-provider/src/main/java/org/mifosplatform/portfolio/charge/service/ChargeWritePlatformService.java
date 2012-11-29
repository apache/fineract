package org.mifosplatform.portfolio.charge.service;

import org.mifosplatform.portfolio.charge.command.ChargeCommand;
import org.springframework.security.access.prepost.PreAuthorize;

public interface ChargeWritePlatformService {

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'ORGANISATION_ADMINISTRATION_SUPER_USER', 'CREATE_CHARGE')")
    Long createCharge(final ChargeCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'ORGANISATION_ADMINISTRATION_SUPER_USER', 'UPDATE_CHARGE')")
    Long updateCharge(final ChargeCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'ORGANISATION_ADMINISTRATION_SUPER_USER', 'DELETE_CHARGE')")
    Long deleteCharge(final Long chargeId);
}
