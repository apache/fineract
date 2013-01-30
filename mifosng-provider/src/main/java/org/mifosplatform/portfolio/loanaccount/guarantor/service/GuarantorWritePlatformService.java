package org.mifosplatform.portfolio.loanaccount.guarantor.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

public interface GuarantorWritePlatformService {

    CommandProcessingResult createGuarantor(final JsonCommand command);

    CommandProcessingResult updateGuarantor(final Long guarantorId, final JsonCommand command);

    CommandProcessingResult removeGuarantor(final Long guarantorId);

}