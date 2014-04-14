/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.commands.service;

import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

public interface PortfolioCommandSourceWritePlatformService {

    CommandProcessingResult logCommandSource(CommandWrapper commandRequest);

    CommandProcessingResult approveEntry(Long id);

    Long rejectEntry(Long id);

    Long deleteEntry(Long makerCheckerId);
}