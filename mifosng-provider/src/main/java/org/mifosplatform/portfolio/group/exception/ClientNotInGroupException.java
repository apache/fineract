/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.group.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class ClientNotInGroupException extends AbstractPlatformDomainRuleException {

    public ClientNotInGroupException(final Long clientId, final Long groupId) {
        this("group.client.not.in.group", "Client with identifier " + clientId + " is not in Group with identifier " + groupId, clientId,
                groupId);

    }

    public ClientNotInGroupException(final String postFix, final String defaultUserMessage, final Object... defaultUserMessageArgs) {
        super("error.msg." + postFix, defaultUserMessage, defaultUserMessageArgs);
    }

}
