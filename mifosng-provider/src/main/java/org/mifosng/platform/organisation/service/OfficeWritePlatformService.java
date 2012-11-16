package org.mifosng.platform.organisation.service;

import org.mifosng.platform.api.commands.BranchMoneyTransferCommand;
import org.mifosng.platform.api.commands.OfficeCommand;
import org.springframework.security.access.prepost.PreAuthorize;

public interface OfficeWritePlatformService {

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'ORGANISATION_ADMINISTRATION_SUPER_USER', 'CREATE_OFFICE')")
	Long createOffice(final OfficeCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'ORGANISATION_ADMINISTRATION_SUPER_USER', 'UPDATE_OFFICE')")
	Long updateOffice(final OfficeCommand command);

    //TODO - complete permissions for office transactions when more functionality add or it is replaced by simple accounting equivalent (JPW)
    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'ORGANISATION_ADMINISTRATION_SUPER_USER', 'CREATE_OFFICETRANSACTION')")
	Long externalBranchMoneyTransfer(BranchMoneyTransferCommand command);
}