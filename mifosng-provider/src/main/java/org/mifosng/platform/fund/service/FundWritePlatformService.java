package org.mifosng.platform.fund.service;

import org.mifosng.platform.api.commands.FundCommand;
import org.springframework.security.access.prepost.PreAuthorize;

public interface FundWritePlatformService {

	@PreAuthorize(value = "hasRole('ORGANISATION_ADMINISTRATION_SUPER_USER_ROLE')")
	Long createFund(final FundCommand command);
	
	@PreAuthorize(value = "hasRole('ORGANISATION_ADMINISTRATION_SUPER_USER_ROLE')")
	Long updateFund(final FundCommand command);
}