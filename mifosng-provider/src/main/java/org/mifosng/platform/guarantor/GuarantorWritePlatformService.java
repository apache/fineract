package org.mifosng.platform.guarantor;

import org.mifosng.platform.api.commands.GuarantorCommand;
import org.springframework.security.access.prepost.PreAuthorize;

public interface GuarantorWritePlatformService {

	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS','PORTFOLIO_MANAGEMENT_SUPER_USER')")
	void createGuarantor(final Long loanId, final GuarantorCommand command);

	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS','PORTFOLIO_MANAGEMENT_SUPER_USER')")
	void updateGuarantor(final Long loanId, final GuarantorCommand command);

	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS','PORTFOLIO_MANAGEMENT_SUPER_USER')")
	void removeGuarantor(final Long loanId);

}