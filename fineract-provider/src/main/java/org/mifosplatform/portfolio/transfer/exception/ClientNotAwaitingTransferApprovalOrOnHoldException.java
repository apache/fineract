/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.transfer.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class ClientNotAwaitingTransferApprovalOrOnHoldException extends AbstractPlatformDomainRuleException {

    public ClientNotAwaitingTransferApprovalOrOnHoldException(final Long clientId) {
        super("error.msg.client.not.awaiting.transfer.approval.or.on.hold.exception", "The Client with id `" + clientId
                + "` is neither awaiting a transfer nor on hold", clientId);
    }

}
