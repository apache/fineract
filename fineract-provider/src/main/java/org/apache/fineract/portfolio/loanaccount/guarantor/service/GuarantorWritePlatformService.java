/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.guarantor.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

public interface GuarantorWritePlatformService {

    CommandProcessingResult createGuarantor(final Long loanId, final JsonCommand command);

    CommandProcessingResult updateGuarantor(final Long loanId, final Long guarantorId, final JsonCommand command);

    CommandProcessingResult removeGuarantor(final Long loanId, final Long guarantorId, Long guarantorFundingId);

}