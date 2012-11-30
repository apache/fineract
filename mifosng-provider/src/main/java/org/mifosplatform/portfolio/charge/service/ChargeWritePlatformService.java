package org.mifosplatform.portfolio.charge.service;

import org.mifosplatform.portfolio.charge.command.ChargeDefinitionCommand;

public interface ChargeWritePlatformService {

    Long createCharge(ChargeDefinitionCommand command);

    Long updateCharge(ChargeDefinitionCommand command);

    Long deleteCharge(ChargeDefinitionCommand command);
}
