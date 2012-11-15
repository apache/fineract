package org.mifosng.platform.fund.service;

import org.mifosng.platform.api.commands.FundCommand;
import org.springframework.security.access.prepost.PreAuthorize;

public interface FundWritePlatformService {

	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'ORGANISATION_ADMINISTRATION_SUPER_USER', 'CREATE_FUND')")
	Long createFund(final FundCommand command);
	
	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'ORGANISATION_ADMINISTRATION_SUPER_USER', 'UPDATE_FUND')")
	Long updateFund(final FundCommand command);
}