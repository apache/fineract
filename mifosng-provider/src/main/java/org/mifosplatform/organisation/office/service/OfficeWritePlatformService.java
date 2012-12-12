package org.mifosplatform.organisation.office.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;

public interface OfficeWritePlatformService {

    EntityIdentifier createOffice(JsonCommand command);

    EntityIdentifier updateOffice(Long officeId, JsonCommand command);

    EntityIdentifier externalBranchMoneyTransfer(JsonCommand command);
}