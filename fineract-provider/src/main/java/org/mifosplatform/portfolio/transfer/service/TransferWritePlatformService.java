/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.transfer.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

public interface TransferWritePlatformService {

    CommandProcessingResult transferClientsBetweenGroups(final Long sourceGroupId, final JsonCommand jsonCommand);

    CommandProcessingResult proposeClientTransfer(final Long clientId, final JsonCommand jsonCommand);

    CommandProcessingResult withdrawClientTransfer(final Long clientId, final JsonCommand jsonCommand);

    CommandProcessingResult acceptClientTransfer(final Long clientId, final JsonCommand jsonCommand);

    CommandProcessingResult rejectClientTransfer(final Long clientId, final JsonCommand jsonCommand);

    CommandProcessingResult proposeAndAcceptClientTransfer(final Long clientId, final JsonCommand jsonCommand);

}