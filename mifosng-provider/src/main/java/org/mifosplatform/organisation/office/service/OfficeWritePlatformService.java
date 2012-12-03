package org.mifosplatform.organisation.office.service;

import org.mifosplatform.organisation.office.command.BranchMoneyTransferCommand;
import org.mifosplatform.organisation.office.command.OfficeCommand;

public interface OfficeWritePlatformService {

    Long createOffice(OfficeCommand command);

    Long updateOffice(OfficeCommand command);

    Long externalBranchMoneyTransfer(BranchMoneyTransferCommand command);
}