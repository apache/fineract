/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.accounts.service;


public interface AccountsCommandsService {

    public Object handleCommand(final Long accountId, final String command, final String jsonBody) ;
}
