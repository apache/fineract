/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class ClientActiveForUpdateException extends AbstractPlatformDomainRuleException {

    public ClientActiveForUpdateException(final Long clientId, final String parameterName) {
        super("error.msg.client.active.for.update.parameter." + parameterName, "The Client with id `" + clientId
                + "` is active,can't update parameter " + parameterName, clientId, parameterName);
    }

}
