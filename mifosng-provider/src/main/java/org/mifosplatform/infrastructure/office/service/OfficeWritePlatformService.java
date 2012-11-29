package org.mifosplatform.infrastructure.office.service;

import org.mifosplatform.infrastructure.office.command.BranchMoneyTransferCommand;
import org.mifosplatform.infrastructure.office.command.OfficeCommand;

public interface OfficeWritePlatformService {

    Long createOffice(OfficeCommand command);

    Long updateOffice(OfficeCommand command);

    Long externalBranchMoneyTransfer(BranchMoneyTransferCommand command);
}