package org.mifosplatform.portfolio.loanaccount.gaurantor.service;

import org.mifosplatform.portfolio.loanaccount.gaurantor.command.GuarantorCommand;
import org.springframework.security.access.prepost.PreAuthorize;

public interface GuarantorWritePlatformService {

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS')")
    void createGuarantor(final Long loanId, final GuarantorCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS')")
    void updateGuarantor(final Long loanId, final GuarantorCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS')")
    void removeGuarantor(final Long loanId);

}