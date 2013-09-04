package org.mifosplatform.portfolio.transfer.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class ClientNotAwaitingTransferApprovalOrOnHoldException extends AbstractPlatformDomainRuleException {

    public ClientNotAwaitingTransferApprovalOrOnHoldException(final Long clientId) {
        super("error.msg.client.not.awaiting.transfer.approval.or.on.hold.exception", "The Client with id `" + clientId
                + "` is neither awaiting a transfer nor on hold", clientId);
    }

}
