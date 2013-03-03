/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.group;

import org.mifosplatform.portfolio.group.command.GroupCommand;

public interface PortfolioApiDataConversionService {

    GroupCommand convertJsonToGroupCommand(Long resourceIdentifier, String json);
}