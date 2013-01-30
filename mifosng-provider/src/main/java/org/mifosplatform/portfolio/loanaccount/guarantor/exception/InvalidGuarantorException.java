package org.mifosplatform.portfolio.loanaccount.guarantor.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * {@link AbstractPlatformDomainRuleException} thrown when a Client is set as a
 * guarantor for his/her own loans
 */
public class InvalidGuarantorException extends AbstractPlatformDomainRuleException {

    public InvalidGuarantorException(Long clientId, Long loanId) {
        super("error.msg.invalid.guarantor", "Tried to set Client with id " + clientId
                + " as a guarantor to his/her own loan with loan identifier =" + loanId, clientId, loanId);
    }

}
