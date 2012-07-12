package org.mifosng.platform.organisation.service;

import org.mifosng.platform.api.commands.BranchMoneyTransferCommand;
import org.mifosng.platform.api.commands.OfficeCommand;
import org.springframework.security.access.prepost.PreAuthorize;

public interface OfficeWritePlatformService {

	@PreAuthorize(value = "hasRole('ORGANISATION_ADMINISTRATION_SUPER_USER_ROLE')")
	Long createOffice(final OfficeCommand command);
	
	@PreAuthorize(value = "hasRole('ORGANISATION_ADMINISTRATION_SUPER_USER_ROLE')")
	Long updateOffice(final OfficeCommand command);

	@PreAuthorize(value = "hasRole('ORGANISATION_ADMINISTRATION_SUPER_USER_ROLE')")
	Long interBranchMoneyTransfer(BranchMoneyTransferCommand command);

	@PreAuthorize(value = "hasRole('ORGANISATION_ADMINISTRATION_SUPER_USER_ROLE')")
	Long externalBranchMoneyTransfer(BranchMoneyTransferCommand command);
}